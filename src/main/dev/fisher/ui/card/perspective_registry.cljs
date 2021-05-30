(ns dev.fisher.ui.card.perspective-registry
  (:require [dev.fisher.data-model.card-data :as card-data]
            [cljs.spec.alpha :as s]
            [com.fulcrologic.guardrails.core :refer [>defn =>]]
            [com.fulcrologic.fulcro.components :as comp]))



(defonce registered-perspectives (atom {}))

;; what is needed to register new types of cards
;;  ::predicate
;;  ::class
;;  ::init-state-fn??
;;  ::name
;;  ::id
;;  ::actions??, could be defined on the class as a class option
;;                ^^ No go, if we want anonymous card types with *GenericCard*

;; (fn [...] boolean?)
(s/def ::predicate ifn?)
(s/def ::class comp/component-class?)
;; (fn [card-data*] init-state for merge)
(s/def ::init-state-fn ifn?)
(s/def ::name string?)
(s/def ::id qualified-keyword?)
(s/def ::perspective
  (s/keys
    :req [::predicate ::class ::name ::id]
    :opt [::init-state-fn]))

(>defn register-perspective
  [{::keys [id predicate class name actions] :as perspective-data}]
  [::perspective => any?]
  (swap! registered-perspectives assoc id perspective-data))


(defn find-perspective [card-data]
  (filter (fn [x] ((::card-data/predicate x) card-data))
    (vals @registered-perspectives)))

(defn get-perspective
  [perspective-register-name]
  (get @registered-perspectives perspective-register-name))

(defn perspective-name [perspective-register-name] (::name (get-perspective perspective-register-name)))

(defn build-perspective
  "Given a card"
  [card-register-id]
  (let [{::keys [class init-state-fn id] :as perspective}
        (get @registered-perspectives card-register-id)]))
