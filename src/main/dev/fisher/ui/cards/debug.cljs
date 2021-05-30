(ns dev.fisher.ui.cards.debug
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.data-model.card-data :as card-data]
    [zprint.core :as zprint]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]))


(defsc DebugCard [this props]
  {:query         [::card-content/id
                   '*]
   :ident         ::card-content/id}
  (dom/pre
    (with-out-str (zprint/zprint props))))


(def ui-debug-card (comp/factory DebugCard {:keyfn card-content/content-ident-key}))

(perspective-registry/register-perspective
  {::card-data/predicate (constantly true)
   ::card-data/id        (comp/component-name DebugCard)
   ::card-data/name      "Card Debug"
   ::card-data/class     DebugCard})
