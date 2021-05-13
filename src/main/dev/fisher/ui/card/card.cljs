(ns dev.fisher.ui.card.card
  (:require
    [goog.object :as gobj]
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
    (let [factory (comp/factory clazz)]
      (swap!-> state
        (assoc-in [::id id ::sub-renderer] factory)
        (merge/merge-component clazz (assoc initial-state
                                       card-content/content-ident-key id)))

      ;; using the ! version because it has the indexing information built in
      (comp/set-query! app (get @card-root-factory-registry id)
        {:query [::id
                 ::sub-renderer
                 {::backing-data (comp/get-query clazz)}]}))))


(defsc Card [this {::keys [id backing-data sub-renderer] :as props
                   :or {backing-data card-content/BlankCard}}]
  {:query                   [::id
                             ::sub-renderer
                             {::backing-data (comp/get-query card-content/BlankCard)}]
   :ident                   ::id
   ;; both this and the card content share the same id -- different tables though
   :initial-state           (fn [{:keys [id]}]
                              {::id           id
                               ::backing-data {card-content/content-ident-key id}})
   :preserve-dynamic-query? true}
  (dom/div "card-header" (str id)
    (when sub-renderer
      (sub-renderer backing-data))))

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
