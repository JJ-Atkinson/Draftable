(ns dev.fisher.ui.status-bar
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


(defsc StatusBar [this {:keys [::id ui/status-key-stack] :as props}]
  {:query         [::id
                   :ui/status-key-stack]
   :initial-state {}
   :ident         (fn [_] [:component/id ::id])}
  (fui/hstack (assoc fui/lowgap-stack
                :className "status-bar"
                :verticalAlign "center")
    (fui/Mtext (str/join " " status-key-stack))))

(def ui-status-bar (comp/factory StatusBar {:keyfn :component/id}))
