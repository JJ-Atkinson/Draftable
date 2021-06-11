(ns dev.fisher.ui.keyboard.whichkey-display
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [dev.fisher.ui.action.action-registry :as action-registry]
    [dev.fisher.utils :as u]
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

(let [comparator (u/compose-compare
                   #(compare (:key-code %1) (:key-code %2))
                   #(compare (:modifier-count %1) (:modifier-count %2)))]
  (defn sort-by-key [contents-map]
    (sort-by (comp k-const/coerce-key-combo-matcher first) comparator contents-map)))

(defn trigger-key! [key-desc]
  (when-not (:modifier-key? key-desc)
    (uism/trigger! SPA :dev.fisher.ui.root/keyboard-listener
      :event/global-key-pressed
      {:key-desc key-desc})))

(defsc WhichkeyDisplay [this {:keys [::id ui/visible? ui/contents-map] :as props}]
  {:query         [::id
                   :ui/visible?
                   :ui/contents-map]
   :initial-state {}
   :ident         (fn [_] [:component/id ::id])}
  (when visible?
    (dom/div :.over-content-position
      (bottom-whatever
        (dom/div :.whichkey-root
          (map (fn [[combo {::action-registry/keys [title description] :as action}]]
                 (let [group? (not (action-registry/action? action))
                       item (dom/div :.whichkey-item
                              {:key     combo
                               :classes [(when group? "whichkey-group-identifier")]
                               :onClick #(trigger-key! combo)}
                              (str combo)
                              (dom/div :.whichkey-dot)
                              (str (or title "untitled-group")))]
                   (if description
                     (fui/tooltip-host
                       (assoc fui/tooltip-bottom
                         :content description
                         :delay 0
                         :id (str (hash action)))
                       item)
                     item)))
            (->> contents-map
              ;; remove ::action-registry/title and ::action-registry/description
              (filter (comp string? key))
              (sort-by-key))))))))

(def ui-whichkey-display (comp/factory WhichkeyDisplay {:keyfn :component/id}))

