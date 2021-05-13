(ns dev.fisher.ui.workspaces.workspace-manager
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [app.SPA :refer [SPA]]))


(defmutation attach-component [{:keys [id clazz state]}]
  )

(defsc WMManager [this {::keys [id children] :as props}]
  {:query [::id
           ::children]
   :ident ::id}
  (dom/div
    ))

(def ui-wmmanager (comp/factory WMManager {:keyfn ::id}))


;; Workspace (layout)
;; Card (position, etc)
;; V
;; Views (Editor, viewer, ...)



(defsc Card [this {:keys [::id] :as props}]
  {:query [::id
           {::view (comp/get-query ...)}]
   :ident ::id}
  (dom/div))

(def ui-card (comp/factory Card {:keyfn ::id}))

(def rroot (fn [this] (mrr/register-root! this {:app SPA})))
(def drroot (fn [this] (mrr/deregister-root! this {:app SPA})))

(defsc A [this {:keys [:aid :aprop] :as props}]
  {:query                [:aid
                          :aprop]
   :componentDidMount    rroot
   :componentWillUnmount drroot
   :ident                :aid}
  (dom/div {:onClick #(m/set-integer!! this :aprop (inc aprop))}
    "A" aprop))

(defsc B [this {:keys [:bid bprop] :as props}]
  {:query                [:bid
                          :bprop]
   :componentDidMount    rroot
   :componentWillUnmount drroot
   :ident                :bid}
  (dom/div {:onClick #(m/set-integer!! this :bprop (inc bprop))}
    "B" bprop))

(def ui-b (comp/factory B {:keyfn :bid}))

(comment
  (mrr/floating-root-factory A))