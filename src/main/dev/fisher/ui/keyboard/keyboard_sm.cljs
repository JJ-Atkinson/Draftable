(ns dev.fisher.ui.keyboard.keyboard-sm
  (:require
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [dev.fisher.ui.keyboard.event-interceptor :as k-event]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]))


(def start-key (k-const/build-key-combo-matcher "SPC"))
(def exit-key (k-const/build-key-combo-matcher "ESC"))

(defn key-listener [asm-id key-desc]
  (uism/trigger! SPA asm-id :event/global-key-pressed {:key-desc key-desc}))

(def whichkey-display-delay 900)

(defn handler [x]
  {::uism/handler x})

(defn update-stack [env append?]
  (let [new-stack (if append? 
                    (conj (uism/retrieve env ::current-key-stack) append?)
                    [])]
    (-> env 
      (uism/assoc-aliased :status-key-stack new-stack)
      (uism/store ::current-key-stack new-stack))))

(defn key-event-completed [env]
  (-> env
    (uism/assoc-aliased
      :whichkey-visible? false)
    (update-stack nil)
    (uism/activate :state/idle)))

(defstatemachine keyboard-listener-uism
  {::uism/actor-names
   #{:actor/whichkey-display
     :actor/status-display}

   ::uism/aliases
   {:status-key-stack  [:actor/status-display :ui/status-key-stack]
    :whichkey-visible? [:actor/whichkey-display :ui/visible?]}

   ::uism/states
   {:initial
    (handler (fn [env]
               (k-event/register-document-listener "keydown"
                 (fn [k]
                   (key-listener (::uism/asm-id env) k)))
               (-> env
                 (uism/activate :state/idle)
                 (uism/store ::current-key-stack []))))

    :state/idle
    {::uism/events
     {:event/global-key-pressed
      (handler
        (fn [{{:keys [key-desc]} ::uism/event-data :as env}]
          (cond-> env
            (k-const/evt-matches? key-desc start-key)
            (->
              (uism/activate :state/listening)
              (update-stack key-desc)
              (uism/set-timeout
                :whichkey
                :event/show-whichkey {}
                whichkey-display-delay)))))

      :event/show-whichkey
      (handler identity)}}

    :state/listening
    {::uism/events
     {:event/global-key-pressed
      (handler
        (fn [{{:keys [key-desc]} ::uism/event-data :as env}]
          (if (:modifier-key? key-desc)
            env
            (if (k-const/evt-matches? key-desc exit-key)
              (key-event-completed env)
              (update-stack env key-desc)))))

      :event/show-whichkey
      (handler (fn [env] (uism/set-aliased-value env :whichkey-visible? true)))}}}})