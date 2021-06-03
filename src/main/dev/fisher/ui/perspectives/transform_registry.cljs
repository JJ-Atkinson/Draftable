(ns dev.fisher.ui.perspectives.transform-registry
  (:require
    [cljs.spec.alpha :as s]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]))

(defonce registered-transforms (atom {}))

;; (fn [...] boolean?)
(s/def ::predicate ifn?)
;; (fn [card-data*] init-state for merge)
(s/def ::name string?)
(s/def ::id qualified-keyword?)
;; (fn [card-data*] transform-data)
(s/def ::transform ifn?)
(s/def ::transform-desc (s/keys
                          :req [::name ::id ::transform]
                          :opt [::predicate]))

(>defn register-transform!
  [{::keys [id predicate name transform] :as transform-data}]
  [::transform-desc => any?]
  (swap! registered-transforms assoc id transform-data))

(defn get-transform
  [transform-register-id]
  (get @registered-transforms transform-register-id))

(defn transform-name [transform-register-name]
  (::name (get-transform transform-register-name)))

(defn available-transforms
  ([] (vals @registered-transforms))
  ([data]
   (filter (fn [{:as pers ::keys [predicate]}]
             (if predicate (predicate data) pers))
     (available-transforms))))

(register-transform! {::id        :transform/identity
                     ::name      "Identity"
                     ::transform identity})
