(ns dev.fisher.ui.keyboard.keyboard-sm
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [dev.fisher.ui.keyboard.event-interceptor :as k-event]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]))



(defstatemachine keyboard-listener-uism
  {::uism/actor-names
   #{:actor/whichkey-display
     :actor/status-display}

   ::uism/states
   {:initial
    {::uism/handler (fn [env]
                      (uism/activate env :state/idle))}

    :state/idle
    {::uism/events
     {:event/global-key-pressed (fn [env])
      :event/show-whichkey      identity}}

    :state/listening
    {::uism/events
     {:event/global-key-pressed (fn [env])
      :event/show-whichkey      (fn [env])}}}})