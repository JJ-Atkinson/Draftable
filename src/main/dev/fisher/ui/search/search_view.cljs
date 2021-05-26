(ns dev.fisher.ui.search.search-view
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]))


(defmutation set-visible-search-view [{:keys [visible?]}]
  (action [{:keys [state]}]
    (swap!-> state
      (assoc-in [::id :singleton ::display?] visible?))))

(comment
  (comp/transact! SPA [(set-visible-search-view {:visible? true})])
  (comp/transact! SPA [(set-visible-search-view {:visible? false})])
  )

(defn center-two-thirds [& content]
  (fui/vstack (assoc fui/nogap-stack
                :verticalFill true)
    (fui/stack-item {:grow 1}
      ;; prevents the grow1 item from being ignored when it has no content
      (dom/div))
    (fui/stack-item {:grow 2}
      (fui/hstack (assoc fui/nogap-stack
                    :verticalAlign "start"
                    :horizontalAlign "center")
        (fui/vstack (assoc fui/lowgap-stack
                      :onClick (fn [evt] (events/stop-propagation! evt)))
          content)))))

(defsc SearchView [this {::keys [id display?] :as props}]
  {:query         [::id
                   ::display?]
   :initial-state {}
   :ident         (fn [_] [::id :singleton])}
  (when display?
    (dom/div :.modal-background
      {:onClick (fn [_] (comp/transact! this [(set-visible-search-view {:visible? false})]))}
      (center-two-thirds
        (fui/input {:placeholder "search"
                    :autoFocus   true
                    :componentRef (fn [x] (.focus x))})))))

(def ui-search-view (comp/factory SearchView {:keyfn ::id}))
