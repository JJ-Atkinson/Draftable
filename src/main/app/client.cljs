(ns app.client
  (:require
    [app.log-config]
    [com.fulcrologic.fulcro.application :as app]
    [app.SPA :refer [SPA]]
    [dev.fisher.ui.root :as root]
    [com.fulcrologic.fulcro.components :as comp]))


(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! SPA root/Root "app")
  (comp/transact! SPA [(root/initialize {})])
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! SPA root/Root "app")
  (js/console.log "Hot reload"))
