(ns dev.fisher.ui.perspectives.view-registry
  (:require
    [cljs.spec.alpha :as s]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]))

(defonce registered-views (atom {}))

;; (fn [...] boolean?)
(s/def ::predicate ifn?)
;; (fn [card-data*] init-state for merge)
(s/def ::name string?)
(s/def ::id qualified-keyword?)
;; (fn [transformed-data] html-view)
(s/def ::view ifn?)
(s/def ::view-desc (s/keys
                     :req [ ::name ::id ::view]
                     :opt [::predicate]))

(>defn register-view!
  [{::keys [id predicate name view] :as view-data}]
  [::view-desc => any?]
  (swap! registered-views assoc id view-data))

(defn get-view
  [view-register-id]
  (get @registered-views view-register-id))

(defn view-name [view-register-name]
  (::name (get-view view-register-name)))

(defn available-views
  ([] (vals @registered-views))
  ([data]
   (filter (fn [{:as pers ::keys [predicate]}]
             (if predicate (predicate data) pers))
     (available-views))))


