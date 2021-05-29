(ns dev.fisher.ui.search.search-view
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns :refer [swap!->]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [dev.fisher.ui.action.action-registry :as action-registry]
    [dev.fisher.ui.keyboard.event-interceptor :as event-interceptor]
    [dev.fisher.ui.search.search-provider :as search-provider]
    [app.SPA :refer [SPA]]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.dom.events :as events]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [clojure.string :as str]
    [taoensso.timbre :as log]))


(defmutation set-visible-search-view [{:keys [visible? type]}]
  (action [{:keys [state]}]
    (swap!-> state
      (assoc-in [:component/id ::id ::display?] visible?))))

(defmutation start-search-view [{:keys [type]}]
  (action [{:keys [state]}]
    (swap!-> state
      (assoc-in [:component/id ::id] {::display?               true
                                      ::active-search-provider type
                                      ::current-results        []}))))

(defn providers-for
  "if `:all` returns all providers in a list (unsorted), otherwise selects the 
   search provider by id."
  [selector-or-id]
  (let [all-prov (search-provider/all-search-providers-by-id)]
    (if (= :all (log/spy selector-or-id))
      (vals (search-provider/all-search-providers-by-id))
      [(get all-prov selector-or-id)])))

(def max-results
  "maximum results from each search provider. Prevent a bunch of dom thrashing."
  40)

(defmutation do-search [{:keys [input]}]
  (action [{:keys [state]}]
    (let [active-search-provider
                    (get-in @state [:component/id ::id ::active-search-provider])
          providers (providers-for active-search-provider)
          results   (map (fn [{::search-provider/keys [search-fn]
                               :as                    search-provider}]
                           [search-provider
                            (when-not (str/blank? input)
                              (take max-results (search-fn input)))])
                      (log/spy providers))]
      (swap!-> state
        (assoc-in [:component/id ::id ::current-results] results)))))

(comment
  (comp/transact! SPA [(set-visible-search-view {:visible? true})])
  (comp/transact! SPA [(set-visible-search-view {:visible? false})])
  )

(defn esc-listener [this]
  (comp/transact! this [(set-visible-search-view {:visible? false})]))

(defn change-listener [this _ev-obj new-str]
  (comp/transact! this [(do-search {:input new-str})]))

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
        (fui/vstack (assoc fui/nogap-stack
                      :verticalFill true
                      :onClick (fn [evt] (events/stop-propagation! evt))
                      :className "search-panel")
          content)))))

(defn simple-render-results [{::search-provider/keys [title] :as search-provider}
                             results]
  (when (seq results)
    (apply comp/fragment
      (dom/div :.result-header title)
      (map (fn [{:as map s :com.wsscode.fuzzy/string}]
             (dom/div :.result {:key (hash map)} s))
        results))))

(defsc SearchView [this {::keys [display? current-results] :as props}]
  {:query         [::display?
                   ::current-results]
   :initial-state {}
   :ident         (fn [_] [:component/id ::id])}
  (when display?
    (dom/div :.over-content-position.modal-background
      {:onClick (fn [_] (comp/transact! this [(set-visible-search-view
                                                {:visible? false})]))}
      (center-two-thirds
        (fui/stack-item {:grow 0}
          (fui/searchbox {:placeholder      "Search Everywhere"
                          :disableAnimation true
                          :underlined       true
                          :onChange         (partial change-listener this)
                          :componentRef     (fn [x] (when x (.focus x)))
                          :onEscape         (partial esc-listener this)}))

        (fui/stack-item {:grow 1}
          (dom/div :.results
            (map (fn [[prov res]] (simple-render-results prov res))
              current-results)))))))

(def ui-search-view (comp/factory SearchView {:keyfn :component/id}))
