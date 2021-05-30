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
    [taoensso.timbre :as log]
    [dev.fisher.ui.action.action-context :as action-context]))


;; TASK:  move me out!
(defn constrain [low n high]
  (if (< n low)
    low
    (if (> n high)
      high
      n)))

(defmutation set-visible-search-view [{:keys [visible? type]}]
  (action [{:keys [state app]}]
    (action-context/remove-context! app ::id)
    (swap!-> state
      (assoc-in [:component/id ::id ::display?] visible?))))

(defmutation start-search-view [{:keys [type]}]
  (action [{:keys [state]}]
    (swap!-> state
      (assoc-in [:component/id ::id] {::display?               true
                                      ::active-search-provider type
                                      ::selected-index         0
                                      ::current-results        []}))))

(defn count-result-map [current-results]
  (reduce + 0 (map (comp count second) current-results)))

(defmutation nav-list [{:keys [direction]}]
  (action [{:keys [state]}]
    (swap! state
      update-in [:component/id ::id]
      (fn [{::keys [current-results] :as search}]
        (update search ::selected-index
          #(constrain 0
             ((get {:up dec :down inc} direction) (or % 0))
             (dec (count-result-map current-results))))))))

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
                      providers)]
      (swap!-> state
        (assoc-in [:component/id ::id ::current-results] results)
        (update-in [:component/id ::id ::selected-index]
          #(constrain 0 % (count-result-map results)))))))

(defmutation run-action [{:keys [override-object]}]
  (action [{:keys [state]}]
    (let [{::keys [current-results selected-index]}
          (get-in @state [:component/id ::id])

          [{:as                    search-prov
            ::search-provider/keys [on-pick]}
           obj]
          (reduce (fn [cnt-remaining [search-prov results]]
                    (if (< cnt-remaining (count results))
                      (reduced [search-prov (nth results cnt-remaining)])
                      (- cnt-remaining (count results))))
            selected-index
            current-results)]
      (when on-pick (on-pick obj))
      (swap!-> state
        (assoc-in [:component/id ::id ::display?] false)))))

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
                             results
                             highlight-index]
  (when (seq results)
    (apply comp/fragment
      (dom/div :.result-header title)
      (map-indexed
        (fn [idx {:as map s :com.wsscode.fuzzy/string}]
          (dom/div :.result
            {:key     (hash map)
             :classes [(when (= highlight-index idx) "active")]} s))
        results))))

(defsc SearchView [this {::keys [display? current-results selected-index
                                 active-search-provider] :as props}]
  {:query         [::display?
                   ::current-results
                   ::selected-index
                   ::active-search-provider]
   :initial-state {}
   :ident         (fn [_] [:component/id ::id])}
  (when display?
    (dom/div :.over-content-position.modal-background
      (assoc (action-context/track-focus-props this ::id {::display? true})
        :onClick (fn [_] (comp/transact! this [(set-visible-search-view
                                                 {:visible? false})])))
      (center-two-thirds
        (fui/stack-item {:grow 0}
          (let [[{::search-provider/keys [title]} & rest]
                (providers-for active-search-provider)]
            (fui/searchbox {:placeholder      (str "Search "
                                                (if (seq rest) "Everywhere"
                                                               title))
                            :disableAnimation true
                            :underlined       true
                            :onChange         (partial change-listener this)
                            :onSearch         #(comp/transact! this [(run-action {})])
                            :componentRef     (fn [x] (when x (.focus x)))
                            :onEscape         (partial esc-listener this)})))

        (fui/stack-item {:grow 1}
          (dom/div :.results
            ;; mess, but I don't want to fix right now :/
            (first
              (reduce
                (fn [[out cnt] [prov res]]
                  [(conj out (simple-render-results prov res (- selected-index cnt)))
                   (+ cnt (count res))])
                [[] 0]
                current-results))))))))

(def ui-search-view (comp/factory SearchView {:keyfn :component/id}))

(action-registry/register-action!
  #::action-registry
      {:id                :search-action/nav-up
       :title             "Nav up list"
       :invoke            #(comp/transact! SPA [(nav-list {:direction :up})])
       :description       "Navigate up search contents"
       :context-pred      (fn [ctx] (contains? ctx ::id))
       :default-key-combo ["i-UP"]})

(action-registry/register-action!
  #::action-registry
      {:id                :search-action/nav-down
       :title             "Nav down list"
       :invoke            #(comp/transact! SPA [(nav-list {:direction :down})])
       :description       "Navigate down search contents"
       :context-pred      (fn [ctx] (contains? ctx ::id))
       :default-key-combo ["i-DOWN"]})