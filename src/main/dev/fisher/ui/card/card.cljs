(ns dev.fisher.ui.card.card
  (:require
    [goog.object :as gobj]
    [com.fulcrologic.fulcro.application :as application]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [app.SPA :refer [SPA]]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [dev.fisher.ui.card.impl-fulcro-floating-root :as *floating-root]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [dev.fisher.ui.card.card-content :as card-content]
    [taoensso.timbre :as log]
    [zprint.core :as zp]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]))


(defonce card-root-factory-registry
  ;"Atom of ::id -> (factory Card {:qualifier ...})"
  (atom {}))


(defmutation set-card-content [{:keys [id clazz initial-state]}]
  (action [{:keys [state app]}]
    (if-not clazz
      (log/errorf "Card with id %s cannot have it's content set to a nil class")
      (let [factory (comp/factory clazz)]
        (swap!-> state
          (assoc-in [::id id ::sub-renderer] factory)
          (assoc-in [::id id ::default-card-clazz] clazz)
          (merge/merge-component clazz (assoc initial-state
                                         card-content/content-ident-key id)))

        ;; using the ! version because it has the indexing information built in
        (if-let [class-query-factory (get @card-root-factory-registry id)]
          (comp/set-query! app class-query-factory
            {:query [::id
                     ::default-card-clazz
                     ::sub-renderer
                     {::backing-data (comp/get-query clazz)}]}))))))

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
    (log/spy)
    (last)
    (not= ::un-initialized-query)))


(defsc Card [this {::keys [id backing-data sub-renderer default-card-clazz] :as props
                   :or {backing-data card-content/BlankCard}}]
  {:query                   [::id
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
  (if-not (class-query-initialized? this id)
    (do (comp/transact! this [(set-card-content {:id id :clazz default-card-clazz :initial-state {}})])
        nil)
    (dom/div "card-header" (str id)
      (when sub-renderer
        (sub-renderer backing-data)))))

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
