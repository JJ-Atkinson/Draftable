(ns dev.fisher.ui.perspectives.code
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.action.action-context :as action-context]
    [dev.fisher.ui.editor.codemirror-core :as codemirror]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]))


(defsc CodePerspective [this {::keys [codemirror] :as props}]
  {:query         [::card-data/id
                   ::card-data/code
                   ::card-data/namespace
                   {::codemirror (comp/get-query codemirror/CodeMirror)}]
   :ident         ::card-data/id
   :initial-state (fn [{:as   props
                        :keys [::card-data/id
                               ::card-data/code
                               ::card-data/namespace]}]
                    {::card-data/id id
                     ::card-data/code  code
                     ::card-data/namespace namespace
                     ::codemirror      (comp/get-initial-state codemirror/CodeMirror
                                         (merge props
                                           {:id           id
                                            :initial-code code}))})}
  (dom/div
    (-> (action-context/track-focus-props this ::id props)
      (dissoc :onBlur)
      (assoc :style {:max-height "100%"}))
    (codemirror/ui-code-mirror codemirror)))

(def ui-code-perspective (comp/factory CodePerspective {:keyfn card-data/content-ident-key}))

(perspective-registry/register-perspective!
  #::perspective-registry{:predicate     (fn [x] (contains? x ::card-data/code))
                          :id            :perspective/code
                          :initial-state (partial comp/get-initial-state CodePerspective)
                          :name          "Code"
                          :class         CodePerspective})
