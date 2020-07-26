(ns fisher.plugins.editor1.editor1-root
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mroot]
            [app.SPA :as SPA]
            ))


(defsc Editor1Root [this {:keys [::root-id] :as props}]
  {:query [::root-id]
   :ident ::root-id
   }
  (dom/div
    "E1 Root!"))

(def ui-editor-1-root (mroot/floating-root-factory Editor1Root {:keyfn ::root-id}))