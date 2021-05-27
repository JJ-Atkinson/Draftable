(ns dev.fisher.ui.keyboard.whichkey-display
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [clojure.string :as str]))

(defn bottom-whatever [& content]
  (fui/vstack (assoc fui/nogap-stack
                :verticalFill true
                :verticalAlign "start")
    (fui/stack-item {:grow 1}
      (dom/div))
    (fui/stack-item {:grow 0}
      content)
    (fui/stack-item {:grow 0 :className "offset-status-bar-height"}
      (dom/div))))

(defsc WhichkeyDisplay [this {:keys [::id ui/visible?] :as props}]
  {:query         [::id
                   :ui/visible?]
   :initial-state {}
   :ident         (fn [_] [:component/id ::id])}
  (when visible?
    (dom/div :.over-content-position
      (bottom-whatever 
        (dom/div :.whichkey-root "whichkey")))
    
    ))

(def ui-whichkey-display (comp/factory WhichkeyDisplay {:keyfn :component/id}))

