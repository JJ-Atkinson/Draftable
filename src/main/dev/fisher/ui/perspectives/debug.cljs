(ns dev.fisher.ui.perspectives.debug
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.data-model.card-data :as card-data]
    [zprint.core :as zprint]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]))


(defsc DebugPerspective [this props]
  {:query         [::card-content/id
                   '*]
   :initial-state (fn [x] x)
   :ident         ::card-content/id}
  (dom/pre
    (with-out-str (zprint/zprint props))))


(def ui-debug-perspective (comp/factory DebugPerspective {:keyfn card-content/content-ident-key}))

(perspective-registry/register-perspective!
  #::perspective-registry{:predicate (constantly true)
                          :id        :perspective/debug
                          :name      "Card Debug"
                          :class     DebugPerspective})