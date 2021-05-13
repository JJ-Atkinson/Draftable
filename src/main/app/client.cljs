(ns app.client
  (:require
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
    [app.log-config]
    ))


(defn cmc-is [x] (comp/get-initial-state cmc/CodeMirror x))

(defsc Root [this
             {:keys [codemirror] :as props}]
  {:query         [{:codemirror (comp/get-query cmc/CodeMirror)}]
   :initial-state {:codemirror [{:id 1 :initial-code ";; I'm number 1"}
                                {:id 2 :initial-code ";; I'm number 2 "}]}}

  (dom/div :.root
    (dom/div
      (map cmc/ui-code-mirror codemirror))
    (card/ui-content-root {::card/id :cardid})))


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
