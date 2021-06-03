(ns dev.fisher.ui.card.card
  (:require
    [com.fulcrologic.fulcro.application :as application]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]

    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [dev.fisher.ui.card.impl-fulcro-floating-root :as *floating-root]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [dev.fisher.ui.card.card-content :as card-content]
    [taoensso.timbre :as log]
    [dev.fisher.ui.perspectives.perspectives-importer]
    [dev.fisher.ui.action.action-context :as action-context]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]
    [dev.fisher.fluentui-wrappers :as fui]
    [taoensso.encore :as enc]))


(defonce
  ^{:doc "Atom of ::id -> (factory Card {:qualifier ...})"}
  card-root-factory-registry
  (atom {}))

(defn set-card-content* [state-atom app* id clazz initial-state]
  (if-not clazz
    (log/error nil "Card with [id] cannot have it's content set to a nil class" id)
    (let [factory   (comp/factory clazz)
          new-state (swap!-> state-atom
                      (assoc-in [::id id ::sub-renderer] factory)
                      (assoc-in [::id id ::default-card-clazz] clazz)
                      (merge/merge-component clazz (log/spy (assoc initial-state
                                                              card-content/content-ident-key id))))]

      ;; using the ! version because it has the indexing information built in
      (when-let [class-query-factory (get @card-root-factory-registry id)]
        (comp/set-query! app* class-query-factory
          {:query [::id
                   ::selected-perspective
                   ::default-card-clazz
                   ::sub-renderer
                   {::backing-data (comp/get-query clazz)}]}))
      new-state)))

(defmutation set-card-content [{:keys [id clazz initial-state]}]
  (action [{:keys [state app]}]
    (set-card-content* state app id clazz initial-state)))

(defn set-perspective* [state-atom app* id perspective-id merge-state]
  (let [{::perspective-registry/keys [class]}
        (perspective-registry/get-perspective perspective-id)

        init-state (perspective-registry/build-perspective
                     perspective-id
                     (merge (fns/get-in-graph @state-atom [::id id ::backing-data])
                       merge-state))]
    (set-card-content* state-atom app* id class init-state)
    (swap!-> state-atom
      (assoc-in [::id id ::selected-perspective] perspective-id))))

(defmutation set-perspective [{:keys [id perspective-id merge-state]}]
  (action [{:keys [state app]}]
    (set-perspective* state app id perspective-id merge-state)))

(def build-perspective-dropdown
  (enc/memoize-last
    (fn [backing-data]
      (map (fn [{::perspective-registry/keys [id name]}]
             {:key id :text name})
        (perspective-registry/available-perspectives backing-data)))))

(defn class-query-initialized?
  "roundabout hack to prevent running the first frame of ::sub-renderer with the wrong
   query. The first frame of Card will have the wrong query, and if a sub-renderer is 
   provided before the first frame of Card (re-mounting card, mounting new card with 
   a default view, etc) the query *will* be wrong, providing the wrong initial-data 
   to the ::sub-renderer."
  [appish cardid]
  (some->
    (get @card-root-factory-registry cardid)
    (comp/get-query (application/current-state appish))
    (last)
    (not= ::un-initialized-query)))


(defsc Card [this {::keys [id backing-data sub-renderer default-card-clazz
                           selected-perspective] :as props
                   :or {backing-data card-content/BlankCard}}]
  {:query                   [::id
                             ::selected-perspective
                             ;; only used on first load, allows selection of the card class before
                             ;; the card is rendered. (the mutation cannot be called until the card is mounted)
                             ::default-card-clazz
                             ::sub-renderer
                             {::backing-data (comp/get-query card-content/BlankCard)}
                             ::un-initialized-query]
   :ident                   ::id
   ;; both this and the card content share the same id -- different tables though
   :initial-state           (fn [{:keys [id]}]
                              {::id           id
                               ::backing-data {card-content/content-ident-key id}})
   :preserve-dynamic-query? true}
  (let [raw-backing-data (fns/get-in-graph (application/current-state this)
                           [::id id ::backing-data])]
    ;; wrapping div because FUI is stupid and doesn't have onFocus/Blur everywhere
    (dom/div {:onFocus (fn [_] (action-context/merge-context! this ::id {::id id}))
              :style   {:width "100%" :height "100%"}}
      (fui/vstack {:verticalFill true
                   :className    "no-cursor"}
        (dom/div :.card-header-wrapper
          (dom/div :.default-card-header
            (fui/Stext "Perspective: " (perspective-registry/perspective-name selected-perspective)))
          (fui/hstack {:horizontalAlign "space-between"
                       :className       "cursor react-grid-layout-handle expanded-card-header"}
            (fui/Mtext "Card header" (str id))
            (fui/dropdown
              (fui/with-dropdown-styles
                {:dropdown {:width 300}}
                {:placeholder "Card View"
                 :selected    selected-perspective
                 :onChange    #(comp/transact! this
                                 [(set-perspective {:id             id
                                                    :perspective-id %})])
                 :options     (build-perspective-dropdown raw-backing-data)}))))

        (if (and (not (class-query-initialized? this id)) default-card-clazz)
          (do (comp/transact!! this [(set-card-content {:id            id
                                                        :clazz         default-card-clazz
                                                        :initial-state {}})])
              nil)
          (when sub-renderer
            (sub-renderer backing-data)))))))

(def ui-card (comp/factory Card {:keyfn ::id}))

(defsc ContentRoot [this props]
  {:use-hooks? true}
  (let [id      (::id (comp/get-computed props))

        [original-id _] (hooks/use-state id)
        [Card-factory factory] (*floating-root/use-fulcro-mount this {:initial-state-params {:id id}
                                                                      :child-class          Card})
        content (when factory
                  (factory props))]
    (swap! card-root-factory-registry assoc original-id Card-factory)
    (if (= original-id id)
      content
      (dom/div
        (dom/h4 {:style {:color "red"}} (str "ID Changed!!! " original-id " " id))
        content))))

(def ui-content-root (let [global-factory (comp/computed-factory ContentRoot {:key-fn ::id})]
                       (fn [props]
                         (global-factory props props))))
