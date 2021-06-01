(ns dev.fisher.ui.cards.code
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.editor.codemirror-core :as codemirror]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]))


(defsc CodeCard [this {:keys [::codemirror] :as props}]
  {:query         [::card-content/id
                   ::card-data/code
                   {::codemirror (comp/get-query codemirror/CodeMirror)}]
   :ident         ::card-content/id
   :initial-state (fn [{:as   props
                        :keys [::card-content/id
                               ::card-data/code]}]
                    {::card-content/id id
                     ::card-data/code  code
                     ::codemirror      (comp/get-initial-state codemirror/CodeMirror
                                         (merge props
                                           {:id           id
                                            :initial-code code}))})}
  (codemirror/ui-code-mirror codemirror))

(def ui-code-card (comp/factory CodeCard {:keyfn card-content/content-ident-key}))

(perspective-registry/register-perspective
  #::perspective-registry{:predicate     (fn [x] (contains? x ::card-data/code))
                          :id            :perspective/code-card
                          :initial-state (partial comp/get-initial-state CodeCard)
                          :name          "Code"
                          :class         CodeCard})
