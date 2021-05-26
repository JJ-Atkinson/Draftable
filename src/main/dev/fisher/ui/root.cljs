(ns dev.fisher.ui.root
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]

    [dev.fisher.ui.workspaces.workspaces-manager :as ws-manager]
    [dev.fisher.ui.search.search-view :as search-view]
    [dev.fisher.ui.cards.cards-importer]
    [dev.fisher.fluentui-wrappers :as fui]
    [taoensso.encore :as enc]))



(defsc Root [this {::keys [workspace-manager search-view] :as props}]
  {:query         [{::workspace-manager (comp/get-query ws-manager/WorkspacesManager)}
                   {::search-view (comp/get-query search-view/SearchView)}]
   :initial-state {::workspace-manager {:id :ws-manager-singleton}
                   ::search-view       {}}}
  (fui/theme-provider {:applyTo "body" :theme fui/dark-theme}
    (dom/div :.root
      (ws-manager/ui-workspaces-manager workspace-manager))
    (search-view/ui-search-view search-view)))