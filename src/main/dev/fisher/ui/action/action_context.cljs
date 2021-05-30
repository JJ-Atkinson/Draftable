(ns dev.fisher.ui.action.action-context
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.components :as comp]))


(defn action-context* [state]
  (::action-context state))

(defmutation -merge-context [{:keys [context-id info]}]
  (action [{:keys [state]}]
    (swap! state update-in [::action-context context-id] merge info)))

(defmutation -remove-context [{:keys [context-id]}]
  (action [{:keys [state]}]
    (swap! state update ::action-context dissoc context-id)))

(defn merge-context! [this context-id info]
  (comp/transact! this [(-merge-context {:context-id context-id :info info})]))

(defn remove-context! [this context-id]
  (comp/transact! this [(-remove-context {:context-id context-id})]))

(defn track-focus-props
  "Returns a convenient map of `:onFocus/:onBlur` for react dom usage, where
   `[context-id info]` are added to context on focus, and removed on blur."
  [this context-id info]
  {:onFocus (fn [_] (merge-context! this context-id info))
   :onBlur  (fn [_] (remove-context! this context-id))})
