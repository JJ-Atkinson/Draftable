(ns dev.fisher.ui.card.card-content
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
    [taoensso.timbre :as log]))



(def content-ident-key
  "This is the table that stores information about card contents (e.g. editor state, code, latest eval, etc)"
  ::id)



(defsc RandomCard [this {::keys [id]
                         :keys  [count-random]
                         :as    props}]
  {:query [::id :count-random]
   :ident ::id}
  (log/info props)
  (dom/div
    "RandomCard"
    (dom/button {:onClick #(m/set-integer!! this :count-random :value (inc (or count-random 0)))}
      count-random)))

(def ui-random-card (comp/factory RandomCard {:keyfn :random-card}))


(defsc DifferentCard [this {::keys [id]
                            :keys  [back-count]
                            :as    props}]
  {:query [::id :back-count]
   :ident ::id}
  (dom/div
    "DifferentCard"
    (dom/button {:onClick #(m/set-integer!! this :back-count :value (dec (or back-count 0)))}
      back-count)))

(def ui-different-card (comp/factory DifferentCard {:keyfn ::id}))


(comment
  (comp/transact! SPA [(dev.fisher.ui.card.card/set-card-content
                         {:id            :cardid
                          :clazz         RandomCard
                          :initial-state {:count-random 45}})])
  

  (comp/transact! SPA [(dev.fisher.ui.card.card/set-card-content
                         {:id            :cardid
                          :clazz         DifferentCard
                          :initial-state {:back-count 22}})])
  )