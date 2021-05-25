(ns dev.fisher.ui.dom-utils
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as events]
            [dev.fisher.fluentui-wrappers :as fui]))


(defn tab-panel
  ""
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
              :classes [(when (= selected-id id) "active")]}
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
