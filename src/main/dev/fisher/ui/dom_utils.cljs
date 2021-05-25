(ns dev.fisher.ui.dom-utils
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as events]
            [dev.fisher.fluentui-wrappers :as fui]))


(defn tab-panel
  "{:selected-id  id of tab to highlight
    :items        [{:text string :id any?} ...}
    :on-select    (fn [id] ...)
    :on-close     (fn [id] ...)
    :on-add       (fn [] ...), the + button is only shown if this is not nil
    }"
  [{:keys [selected-id
           items
           on-select
           on-close
           on-add]}]

  (fui/hstack (assoc fui/nogap-stack
                :className "tab-panel")
    (map (fn [{:keys [text id]}]
           (dom/div :.tab
             {:onClick #(on-select id)
              :classes [(when (= selected-id id) "active")]
              :key     (hash id)}
             (fui/M+text text)
             (fui/icon {:iconName "Cancel"
                        :onClick  (fn [evt]
                                    (on-close id)
                                    (events/stop-propagation! evt))})))
      items)
    (when on-add
      (dom/div :.tab-add
        {:onClick #(on-add)}
        (fui/icon {:iconName "Add"})))))
