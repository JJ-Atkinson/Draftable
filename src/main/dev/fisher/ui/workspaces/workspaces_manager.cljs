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
    [com.fulcrologic.fulcro.algorithms.merge :as merge]

    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.ui.cards.code :as code-card]
    [app.SPA :refer [SPA]]
    ))



(defmutation add-workspace [{:keys [wsm-id]}]
  (action [{:keys [state]}]
    (swap!-> state
      (merge/merge-component workspace/WorkspacePanel
        (comp/get-initial-state workspace/WorkspacePanel
          {:id      (random-uuid)
           :ui-name (str (gensym "TAB"))})
        :append [::id wsm-id ::workspaces]
        :replace [::id wsm-id ::selected-workspace]))))

(defmutation remove-workspace [{:keys [ws-id]}]
  (action [{:keys [state]}]
    (swap!-> state
      (fns/remove-entity [::workspace/id ws-id]))))

(defmutation add-card-to-current-workspace [{:keys [wsm-id card-id]}]
  (action [{:keys [state app]}]
    (let [wm-id (fns/get-in-graph @state [::id wsm-id ::selected-workspace
                                          ::workspace/id])]
      (comp/transact! app [(workspace/add-card {:wm-id   wm-id
                                                :card-id card-id})]))))


(defsc WorkspacesManager [this {::keys [id workspaces selected-workspace] :as props}]
  {:query         [::id
                   {::workspaces (comp/get-query workspace/WorkspacePanel)}
                   ::selected-workspace ;; ident
                   ]
   :initial-state {::id                 :param/id
                   ::workspaces         [{:id      :primary-ws
                                          :ui-name "Primary WS"}]
                   ::selected-workspace [::workspace/id :primary-ws]}
   :ident         ::id}
  (let [[_ selected-ws-id] selected-workspace
        selected-ws (first (filter #(= (::workspace/id %) selected-ws-id) workspaces))]
    (fui/vstack (assoc fui/lowgap-stack
                  :verticalFill true)
      (dom-utils/tab-panel
        {:items       (map (fn [{::workspace/keys [ui-name id]}]
                             {:text ui-name :id id})
                        workspaces)
         :selected-id selected-ws-id
         :on-add      #(comp/transact! this [(add-workspace {:wsm-id id})])
         :on-select   #(m/set-value! this ::selected-workspace [::workspace/id %])
         :on-close    #(comp/transact! this [(remove-workspace {:ws-id %})])})

      (workspace/ui-workspace-panel selected-ws))))

(def ui-workspaces-manager (comp/factory WorkspacesManager {:keyfn ::id}))


(comment
  (let [cardid   (gensym)
        code     ";; I AM Z OR COE"
        carddata {::card-data/code  code
                  ::card-content/id cardid}]
    (comp/transact! SPA
      [(card/set-perspective {:id             cardid
                              :perspective-id :perspective/code-card
                              :merge-state    carddata})
       (add-card-to-current-workspace {:wsm-id :ws-manager-singleton :card-id cardid})])))