(ns app.client
  (:require
    [app.log-config]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.SPA :refer [SPA]]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [com.fulcrologic.fulcro.mutations :as m]
    [dev.fisher.ui.editor.codemirror-core :as cmc]

    [dev.fisher.ui.card.card :as card]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.ui.workspaces.workspace-manager :as wsm]
    [dev.fisher.ui.cards.code :as code-card]


    [dev.fisher.fluentui-wrappers :as fui]))


;(defsc NotQuiteRoot [this {:keys [::id] :as props} {:keys [injected-props]}]
;  {:query         [::id]
;   :ident         ::id
;   :initial-state {::id :param/id}}
;  (dom/div
;    ))

;(def ui-not-quite-root (fui/theme-provider NotQuiteRoot))


(defsc Root [this
             {:keys [codemirror wsmanager dropdown-v] :as props}]
  {:query         [{:codemirror (comp/get-query cmc/CodeMirror)}
                   {:wsmanager (comp/get-query wsm/WSManager)}
                   :dropdown-v
                   ::card-content/id]
   :initial-state {:codemirror       [{:id 1 :initial-code ";; I'm number 1"}
                                      {:id 2 :initial-code ";; I'm number 2 "}]
                   :wsmanager        {:id :wsmanager}
                   :dropdown-v       :key}}
  (fui/theme-provider {:applyTo "body" :theme fui/dark-theme}
    (dom/div :.root
      (wsm/ui-wsmanager wsmanager))))

(comment
  (let [cardid   (gensym)
        code     ";; I AM Z CODE"
        carddata {::card-data/code  code
                  ::card-content/id cardid}]
    (comp/transact! SPA
      [(card/set-card-content {:id            cardid
                               :clazz         code-card/CodeCard
                               :initial-state (comp/get-initial-state code-card/CodeCard carddata)})
       (wsm/add-card {:wsm-id :wsmanager :cardid cardid})])))





(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! SPA Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! SPA Root "app")
  (js/console.log "Hot reload"))
