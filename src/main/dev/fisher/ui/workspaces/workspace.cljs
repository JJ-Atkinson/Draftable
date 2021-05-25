(ns dev.fisher.ui.workspaces.workspace
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [app.SPA :refer [SPA]]

    ["react-grid-layout" :as RGL]
    ["react-grid-layout" :refer (WidthProvider)]
    [com.fulcrologic.fulcro.mutations :as mut]
    [dev.fisher.ui.card.card :as card]))


(defmutation add-card
  "Add a cardid to the workspace. Relies in the cardid existing beforehand. Will
   not manage the state of the card in any way."
  [{:keys [wsm-id cardid]}]
  (action [{:keys [state]}]
    (swap!-> state
      (update-in [::id wsm-id ::children] conj cardid))))


(def ReactGridLayout (react-interop/react-factory (new WidthProvider RGL)))

(defsc WorkspacePanel [this {::keys [id children layout] :as props}]
  {:query         [::id
                   ;; children is a list of card ids
                   ::children
                   ::ui-name
                   ::layout]
   :initial-state (fn [{:keys [id ui-name]}]
                    {::id       id
                     ::ui-name  ui-name
                     ::children []
                     ::layout   []})
   :ident         ::id}
  (ReactGridLayout
    {:layout         layout
     :onLayoutChange (fn [layout] (mut/set-value!! this ::layout layout))}
    (map (fn [child]
           (dom/div {:key (str child)}
             (card/ui-content-root {::card/id child})))
      children)))

(def ui-workspace-panel (comp/factory WorkspacePanel {:keyfn ::id}))
