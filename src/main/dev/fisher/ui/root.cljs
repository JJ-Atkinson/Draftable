(ns dev.fisher.ui.root
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]

    [dev.fisher.ui.workspaces.workspaces-manager :as ws-manager]
    [dev.fisher.ui.search.search-view :as search-view]
    [dev.fisher.ui.status-bar :as status-bar]
    [dev.fisher.ui.cards.cards-importer]
    [dev.fisher.fluentui-wrappers :as fui]
    [dev.fisher.ui.keyboard.keyboard-sm]
    
    [taoensso.encore :as enc]))



(defsc Root [this {::keys [workspace-manager search-view status-bar] :as props}]
  {:query         [{::workspace-manager (comp/get-query ws-manager/WorkspacesManager)}
                   {::search-view (comp/get-query search-view/SearchView)}
                   {::status-bar (comp/get-query status-bar/StatusBar)}]
   :initial-state {::workspace-manager {:id :ws-manager-singleton}
                   ::search-view       {}
                   ::status-bar        {}}}
  (fui/theme-provider {:applyTo "body" :theme fui/dark-theme}
    (dom/div :.root
      (fui/vstack (assoc fui/nogap-stack
                    :verticalFill true)
        (fui/stack-item {:grow 1}
          (ws-manager/ui-workspaces-manager workspace-manager))
        (fui/stack-item {:grow 0}
          (status-bar/ui-status-bar status-bar))))
    (search-view/ui-search-view search-view)))