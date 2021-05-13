(ns dev.fisher.ui.workspaces.workspace-manager
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [app.SPA :refer [SPA]]))



(defsc WMManager [this {::keys [id children] :as props}]
  {:query [::id
           ::children]
   :ident ::id}
  (dom/div
    ))

(def ui-wmmanager (comp/factory WMManager {:keyfn ::id}))
