(ns fisher.plugins.editor1.editor1-root
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as mut]
            [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mroot]
            [com.fulcrologic.fulcro.react.hooks :as hooks]
            [app.SPA :as SPA]
            [taoensso.timbre :as log]))


(defsc Editor1 [this {:editor/keys [id text] :as props}]
  {:query [:editor/id
           :editor/text]
   :ident :editor/id
   :initial-state (fn [{:keys [id]}]
                    {:editor/id id})}
  (log/info :text-of props )
  (dom/div
    (dom/input {:onChange (fn [e] (mut/set-string! this :editor/text :event e))
                :value    (or text "")})))

(def ui-editor-1 (comp/factory Editor1 {:keyfn :editor/id}))


(defsc Editor1Root [this props]
  {:use-hooks? true}
  (let [props (comp/get-computed props)
        {:keys [:editor/ident]} props
        f (hooks/use-fulcro-mount this {:initial-state-params {:id (second ident)}
                                        :child-class          Editor1})]
    (log/info :re-mounting props)
    (dom/div
      "E1 Root!"
      (str props)
      (when f (f props)))))

(def ui-editor-1-root (mroot/floating-root-factory Editor1Root {:keyfn ::root-id}))