(ns dev.fisher.ui.workspaces.workspaces-manager
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [dev.fisher.ui.workspaces.workspace :as workspace]

    [com.fulcrologic.fulcro.mutations :as mut]
    [dev.fisher.ui.card.card :as card]
    [dev.fisher.ui.dom-utils :as dom-utils]
    [dev.fisher.fluentui-wrappers :as fui]))




(defsc WorkspacesManager [this {::keys [id workspaces selected-workspace] :as props}]
  {:query         [::id
                   {::workspaces (comp/get-query workspace/WorkspacePanel)}
                   ::selected-workspace]
   :initial-state {::id         :param/id
                   ::workspaces [{:id      :primary-ws
                                  :ui-name "Primary WS"}]}
   :ident         ::id}
  (fui/vstack (assoc fui/lowgap-stack
                :verticalFill true)
    (dom-utils/tab-panel
      {:items       [{:text "hi" :id ::hi}
                     {:text "bye" :id ::bye}]
       :selected-id ::hi
       :on-add      #(js/alert "Adding new!")
       :on-select   #(js/alert (str "Selecting " %))
       :on-close    #(js/alert (str "Closing " %))})
    ;(fui/button {:text (::workspace/ui-name (first workspaces))})
    (workspace/ui-workspace-panel (first workspaces))))

(def ui-workspaces-manager (comp/factory WorkspacesManager {:keyfn ::id}))