(ns dev.fisher.ui.root
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]

    [dev.fisher.ui.workspaces.workspaces-manager :as ws-manager]
    [dev.fisher.ui.cards.cards-importer]
    [dev.fisher.fluentui-wrappers :as fui]
    [taoensso.encore :as enc]))



(defsc Root [this {::keys [workspace-manager] :as props}]
  {:query         [{::workspace-manager (comp/get-query ws-manager/WorkspacesManager)}]
   :initial-state {::workspace-manager {:id :ws-manager-singleton}}}
  (fui/theme-provider {:applyTo "body" :theme fui/dark-theme}
    (dom/div :.root
      (ws-manager/ui-workspaces-manager workspace-manager))))