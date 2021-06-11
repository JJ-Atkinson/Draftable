(ns dev.fisher.ui.root
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom]

    [dev.fisher.ui.workspaces.workspaces-manager :as ws-manager]
    [dev.fisher.ui.search.search-view :as search-view]
    [dev.fisher.ui.status-bar :as status-bar]
    [dev.fisher.ui.keyboard.whichkey-display :as whichkey-display]
    [dev.fisher.ui.perspectives.perspectives-importer]
    [dev.fisher.fluentui-wrappers :as fui]
    [dev.fisher.ui.keyboard.keyboard-sm :as keyboard-sm]

    [taoensso.encore :as enc]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]))



(defsc Root [this {::keys [workspace-manager search-view status-bar whichkey] :as props}]
  {:query         [{::workspace-manager (comp/get-query ws-manager/WorkspacesManager)}
                   {::search-view (comp/get-query search-view/SearchView)}
                   {::status-bar (comp/get-query status-bar/StatusBar)}
                   {::whichkey (comp/get-query whichkey-display/WhichkeyDisplay)}]
   :initial-state {::workspace-manager {:id :ws-manager-singleton}
                   ::search-view       {}
                   ::status-bar        {}
                   ::whichkey          {}}}
  (fui/theme-provider {:applyTo "body" :theme fui/dark-theme}
    (dom/div :.root
      (fui/vstack (assoc fui/nogap-stack
                    :verticalFill true)
        (fui/stack-item {:grow 1}
          (ws-manager/ui-workspaces-manager workspace-manager))
        (fui/stack-item {:grow 0}
          (status-bar/ui-status-bar status-bar))))
    (search-view/ui-search-view search-view)
    (whichkey-display/ui-whichkey-display whichkey)))

(defmutation initialize [{:as params}]
  (action [{:keys [state app]}]
    (uism/begin! app keyboard-sm/keyboard-listener-uism
      ::keyboard-listener
      {:actor/whichkey-display [:component/id ::whichkey-display/id]
       :actor/status-display   [:component/id ::status-bar/id]})))
