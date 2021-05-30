(ns dev.fisher.ui.card.perspective-registry
  (:require [dev.fisher.data-model.card-data :as card-data]))



(defonce registered-perspectives (atom {}))

;; what is needed to register new types of cards
;;  ::predicate
;;  ::class
;;  ::init-state-fn??
;;  ::name
;;  ::id
;;  ::actions??, could be defined on the class as a class option
;;                ^^ No go, if we want anonymous card types with *GenericCard*

(defn register-perspective 
  [{::card-data/keys [id predicate class name actions] :as card-data}]
  (swap! registered-perspectives assoc id card-data))


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
  (let [card-defn (get @registered-perspectives card-register-id)]))
