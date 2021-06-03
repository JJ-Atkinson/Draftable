(ns dev.fisher.ui.search.search-provider
  (:require
    [app.SPA :refer [SPA]]
    [cljs.spec.alpha :as s]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]
    [com.wsscode.fuzzy :as fuz]
    [dev.fisher.ui.action.action-context :as action-context]
    [dev.fisher.ui.action.action-registry :as action-registry]
    [dev.fisher.ui.action.editor :as actions.editor]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [dev.fisher.ui.search.search-view-utils :as search-view-utils]
    [goog.functions :as goog.fns]
    [taoensso.encore :as enc]))

(s/def ::id keyword?)
(s/def ::title string?)
;; (fn [string] [{:as :result ...} ...])
(s/def ::search-fn ifn?)
;; (fn [result] ...)
(s/def ::on-pick ifn?)
;; (fn [search-provider-inst search-fn-results highlight-index] html)
;; highlight-index is not required to be contained within (count search-fn-results)
(s/def ::result-printer ifn?)
(s/def ::default-keyboard-shortcut (s/coll-of k-const/str-ified-key-combo?))

(s/def ::search-provider
  (s/keys :req [::id ::title ::search-fn ::on-pick ::result-printer]
    :opt [::default-keyboard-shortcut]))

(defonce
  ^{:doc "Search provider id -> search-provider"}
  search-providers
  (atom {}))


(>defn register-search-provider!
  ""
  [{::keys [id] :as search-provider}]
  [::search-provider => any?]
  (swap! search-providers assoc id search-provider)
  (when-let [kbd-shortcut (::default-keyboard-shortcut search-provider)]
    (action-registry/register-action!
      #::action-registry{:id                id
                         :title             (::title search-provider)
                         :invoke            (fn []
                                              (comp/transact! SPA
                                                [`(dev.fisher.ui.search.search-view/start-search-view
                                                    {:type ~id})]))
                         :default-key-combo kbd-shortcut})))

(defn all-search-providers-by-id [] @search-providers)

(action-registry/register-action!
  #::action-registry{:id                :search/search-everywhere
                     :title             "Search Everywhere"
                     :invoke            (fn []
                                          (comp/transact! SPA
                                            [`(dev.fisher.ui.search.search-view/start-search-view
                                                {:type :all})]))
                     :default-key-combo ["SPC"]})

(defn action-context []
  (action-context/action-context*
    @(:com.fulcrologic.fulcro.application/state-atom SPA)))

(def -actions-searchable
  (let [f (enc/memoize-last (fn [actions]
                              (map (fn [{:as x ::action-registry/keys [title]}]
                                     (assoc x ::fuz/string title))
                                actions)))]
    #(f (action-registry/available-actions (action-context)))))

(action-registry/register-default-group-name ["s"] "Search" "Different classes of search")

(register-search-provider!
  {::id                        :search/search-actions
   ::title                     "Actions"
   ::search-fn                 (fn [s]
                                 (fuz/fuzzy-match
                                   {::fuz/options      (-actions-searchable)
                                    ::fuz/search-input s}))
   ::on-pick                   (fn [s]
                                 ((::action-registry/invoke s)
                                  (action-context)))
   ::result-printer            (partial search-view-utils/simple-render-results 
                                 ::fuz/string)
   ::default-keyboard-shortcut ["s" "a"]})

(register-search-provider!
  {::id                        :search/search-namespaces
   ::title                     "Namespaces"
   ::search-fn                 (goog.fns/debounce
                                 (fn [s]
                                   (df/load! SPA :project-namespaces nil
                                     {:post-mutation 'dev.fisher.ui.search.search-view/finish-search
                                      :post-mutation-params {:provider-id  :search/search-namespaces
                                                             :search-input s}})
                                   nil)
                                 200)
   ::on-pick                   (fn [{ns-str :com.wsscode.fuzzy/string}]
                                 (actions.editor/open-namespace-as-card ns-str))
   ::result-printer            (partial search-view-utils/simple-render-results
                                 ::fuz/string)
   ::default-keyboard-shortcut ["s" "n"]})
