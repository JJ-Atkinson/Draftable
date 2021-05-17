(ns dev.fisher.ui.cards.code
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [dev.fisher.ui.card.card-content :as card-content]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.editor.codemirror-core :as codemirror]))


(defsc CodeCard [this {:keys [::codemirror] :as props}]
  {:query         [::card-content/id
                   {::codemirror (comp/get-query codemirror/CodeMirror)}]
   :ident         ::card-content/id
   :initial-state (fn [{:keys [::card-content/id
                               ::card-data/code]}]
                    {::card-content/id id
                     ::codemirror     (comp/get-initial-state codemirror/CodeMirror
                                         {:id   id
                                          :initial-code code})})}
  (log/spy [codemirror props])
  (codemirror/ui-code-mirror codemirror))

(def ui-code-card (comp/factory CodeCard {:keyfn card-content/content-ident-key}))
