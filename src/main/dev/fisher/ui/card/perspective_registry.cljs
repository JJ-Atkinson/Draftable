(ns dev.fisher.ui.card.perspective-registry
  (:require [dev.fisher.data-model.card-data :as card-data]
            [cljs.spec.alpha :as s]
            [com.fulcrologic.guardrails.core :refer [>defn =>]]
            [com.fulcrologic.fulcro.components :as comp]
            [taoensso.timbre :as log]))



(defonce registered-perspectives (atom {}))

;; what is needed to register new types of cards
;;  ::cleanup??, could be required for some perspectives on dismount
;;  ::actions??, could be defined on the class as a class option
;;                ^^ No go, if we want anonymous card types with *GenericCard*

;; (fn [...] boolean?)
(s/def ::predicate ifn?)
(s/def ::class comp/component-class?)
;; (fn [card-data*] init-state for merge)
(s/def ::initial-state ifn?)
(s/def ::name string?)
(s/def ::id qualified-keyword?)
(s/def ::perspective
  (s/keys
    :req [::predicate ::class ::name ::id]
    :opt [::initial-state]))

(>defn register-perspective
  [{::keys [id predicate class name actions] :as perspective-data}]
  [::perspective => any?]
  (swap! registered-perspectives assoc id perspective-data))


(defn find-perspective [card-data]
  (filter (fn [x] ((::card-data/predicate x) card-data))
    (vals @registered-perspectives)))

(defn get-perspective
  [perspective-register-id]
  (get @registered-perspectives perspective-register-id))

(defn perspective-name [perspective-register-name]
  (::name (get-perspective perspective-register-name)))

(defn available-perspectives
  ([] (vals @registered-perspectives))
  ([data] 
   (filter (fn [{:as pers ::keys [predicate]}]
             (if predicate (predicate data) pers))
     (available-perspectives))))

(defn build-perspective
  "Given a card and the mounted card data, build up the initial state for
   the said perspective."
  [card-register-id card-data]
  (let [{::keys [class initial-state id] :as perspective}
        (get @registered-perspectives card-register-id)]
    (when-not perspective
      (log/error "Didn't find perspective [p] while building" perspective))
    (or
      (when initial-state
        (initial-state card-data))
      {})))
