(ns dev.fisher.ui.keyboard.keyboard-sm
  (:require
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [dev.fisher.ui.keyboard.event-interceptor :as k-event]
    [app.SPA :refer [SPA]]
    [dev.fisher.ui.action.action-registry :as action-registry]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [taoensso.timbre :as log]))


(def start-key "SPC")
(def exit-key "ESC")
(declare key-event-completed)

(defn key-listener [asm-id key-desc]
  (when-not (:modifier-key? key-desc)
    (uism/trigger! SPA asm-id :event/global-key-pressed
      {:key-desc (k-const/str-ify key-desc)})))

(defn build-action-map [env]
  (k-const/build-key-combo-tree
    (into {} (map (fn [x]
                    [(::action-registry/default-key-combo x) x])
               (action-registry/all-actions)))))

(defn invoke-action [action]
  ((::action-registry/invoke action)))

(def whichkey-display-delay 900)

(defn handler [x]
  {::uism/handler x})

(defn timeout-show-whichkey [env]
  (uism/set-timeout env
    :whichkey
    :event/show-whichkey {}
    whichkey-display-delay))

(defn update-stack 
  "Update the keystack in local storage and in the status bar, and update the keymap in both 
   the local storage and in whichkey. If the appended item results in an action being invoked,
   the action is invoked and the UISM is reset with `key-event-completed` and `(update-stack nil)`"
  [env append? & [reset-key-map?]]
  (let [new-stack
        (if append?
          (conj (uism/retrieve env ::current-key-stack) append?)
          [])

        new-key-command-map
        (if (and append? (not reset-key-map?))
          (get (uism/retrieve env ::current-key-command-map) append?)
          (build-action-map env))]
    (cond
      (nil? new-key-command-map)
      (key-event-completed env)

      (action-registry/action? new-key-command-map)
      (do (invoke-action new-key-command-map)
          (-> env
            (key-event-completed)
            (update-stack nil)))

      :else
      (-> env
        (uism/assoc-aliased :status-key-stack new-stack)
        (uism/store ::current-key-stack new-stack)
        (uism/assoc-aliased :whichkey-contents-map new-key-command-map)
        (uism/store ::current-key-command-map new-key-command-map)))))

(defn key-event-completed [env]
  (-> env
    (uism/assoc-aliased :whichkey-visible? false)
    (update-stack nil)
    (uism/activate :state/idle)))

(defstatemachine keyboard-listener-uism
  {::uism/actor-names
   #{:actor/whichkey-display
     :actor/status-display}

   ::uism/aliases
   {:status-key-stack      [:actor/status-display :ui/status-key-stack]
    :whichkey-visible?     [:actor/whichkey-display :ui/visible?]
    :whichkey-contents-map [:actor/whichkey-display :ui/contents-map]}

   ::uism/states
   {:initial
    (handler (fn [env]
               (k-event/register-document-listener "keydown"
                 (fn [k]
                   (key-listener (::uism/asm-id env) k)))
               (-> env
                 (uism/activate :state/idle)
                 (update-stack nil))))

    :state/idle
    {::uism/events
     {:event/global-key-pressed
      (handler
        (fn [{{:keys [key-desc]} ::uism/event-data :as env}]
          (-> env
            (uism/activate :state/listening)
            ;; reset the keymap to default if space is hit. This allows 
            ;; the command "c" to be invoked either by "c" or by "SPC c"
            (update-stack key-desc (= key-desc start-key))
            (timeout-show-whichkey))))

      :event/show-whichkey
      (handler identity)}}

    :state/listening
    {::uism/events
     {:event/global-key-pressed
      (handler
        (fn [{{:keys [key-desc]} ::uism/event-data :as env}]
          (if (= key-desc exit-key)
            (key-event-completed env)
            (-> env
              (update-stack key-desc)
              (timeout-show-whichkey)))))

      :event/show-whichkey
      (handler (fn [env] (uism/set-aliased-value env :whichkey-visible? true)))}}}})