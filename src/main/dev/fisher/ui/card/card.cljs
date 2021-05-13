(ns dev.fisher.ui.card.card
  (:require
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
    [zprint.core :as zp]))


(def card-root-factory-registry
  "Atom of ::id -> (factory Card {:qualifier ...})"
  (atom {}))

(comment

  (= (second (get @card-root-factory-registry :cardid)) (first (get @card-root-factory-registry :cardid)))

  (comp/set-query! SPA (get @card-root-factory-registry :cardid)
    {:query [::id
             ::sub-renderer
             {::backing-data (comp/get-query card-content/RandomCard)}]})
  (comp/get-query (get @card-root-factory-registry :cardid)
    (com.fulcrologic.fulcro.application/current-state SPA)))

(defmutation set-card-content [{:keys [id clazz initial-state]}]
  (action [{:keys [state]}]
    (let [factory (comp/factory clazz)]
      (swap!-> state
        (assoc-in [::id id ::sub-renderer] factory)
        (comp/set-query* (first (get @card-root-factory-registry id))
          (log/spy :info :thing [::id
                                 ::sub-renderer
                                 {::backing-data (comp/get-query clazz)}]))
        (merge/merge-component clazz (assoc initial-state
                                       card-content/content-ident-key id))))))


(defsc Card [this {::keys [id backing-data sub-renderer] :as props}]
  {:query         [::id
                   ::sub-renderer
                   {::backing-data [card-content/content-ident-key]}]
   :ident         ::id
   ;; both this and the card content share the same id -- different tables though
   :initial-state (fn [{:keys [id]}]
                    {::id           id
                     ::backing-data {card-content/content-ident-key id}})}
  (if sub-renderer
    (dom/div "need more space"
      (dom/br)
      "props"
      (dom/br)
      (dom/pre (with-out-str (zp/zprint props)))
      (dom/br)
      "query"
      (dom/br)
      (dom/pre (with-out-str (zp/zprint (comp/get-query (first (get @card-root-factory-registry id))
                                          (com.fulcrologic.fulcro.application/current-state this)))))
      (sub-renderer backing-data))
    (dom/div {:style {:color "red"}}
      "No bueno! Missing the renderer for this component.")))

(def ui-card (comp/factory Card {:keyfn ::id}))
;(comp/get-query ui-card)



(defsc ContentRoot [this props]
  {:use-hooks? true}
  (let [#_#_id (hooks/use-generated-id)
        id (::id (comp/get-computed props))


        [original-id _] (hooks/use-state id)
        [Card-factory factory] (*floating-root/use-fulcro-mount this {:initial-state-params {:id id}
                                                                      :child-class          Card})
        _  (swap! card-root-factory-registry update original-id conj Card-factory)

        content #_(dom/div
                    (dom/h4 "Content Root")
                    (pr-str props)
                    (js/console.log (str "HI! Printing Props " (pr-str props)))
                    (when factory
                      (factory props)))
           (when factory
             (factory props))]
    (js/console.log "ID" (pr-str [id original-id]) "Printing PROPS" (pr-str props))

    (if (= original-id id)
      content
      (dom/div
        (dom/h4 {:style {:color "red"}} (str "ID Changed!!! " original-id " " id))
        content))
    ))

(def ui-content-root (let [global-factory (mrr/floating-root-factory ContentRoot)]
                       (fn [computed-props]
                         (global-factory (assoc computed-props :react-key
                                                               (::id computed-props))))))
