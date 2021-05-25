(ns dev.fisher.ui.workspaces.workspaces-manager
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [dev.fisher.ui.workspaces.workspace :as workspace]

    [com.fulcrologic.fulcro.mutations :as mut]
    [dev.fisher.ui.card.card :as card]
    [dev.fisher.ui.dom-utils :as dom-utils]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))



(defmutation add-workspace [{:keys [wsm-id]}]
  (action [{:keys [state]}]
    (swap!-> state
      (merge/merge-component workspace/WorkspacePanel
        (comp/get-initial-state workspace/WorkspacePanel
          {:id      (random-uuid)
           :ui-name (str (gensym "TAB"))})
        :append [::id wsm-id ::workspaces]))))

(defmutation remove-workspace [{:keys [ws-id]}]
  (action [{:keys [state]}]
    (swap!-> state 
      (fns/remove-entity [::workspace/id ws-id]))))


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
      {:items       (map (fn [{::workspace/keys [ui-name id]}]
                           {:text  ui-name :id id})
                      workspaces)
       :selected-id selected-workspace
       :on-add      #(comp/transact! this [(add-workspace {:wsm-id id})])
       :on-select   #(m/set-value! this ::selected-workspace %)
       :on-close    #(comp/transact! this [(remove-workspace {:ws-id %})])})
    (workspace/ui-workspace-panel (first workspaces))))

(def ui-workspaces-manager (comp/factory WorkspacesManager {:keyfn ::id}))