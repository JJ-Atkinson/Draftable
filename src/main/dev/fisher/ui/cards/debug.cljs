(ns dev.fisher.ui.cards.debug
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.data-model.card-data :as card-data]
    [zprint.core :as zprint]
    [dev.fisher.ui.card.card-registry :as card-registry]))


(defsc DebugCard [this props]
  {:query         [::card-content/id
                   '*]
   :ident         ::card-content/id}
  (dom/pre
    (with-out-str (zprint/zprint props))))


(def ui-debug-card (comp/factory DebugCard {:keyfn card-content/content-ident-key}))

(card-registry/register-card
  {::card-data/predicate (constantly true)
   ::card-data/id        (comp/component-name DebugCard)
   ::card-data/name      "Card Debug"
   ::card-data/class     DebugCard})
