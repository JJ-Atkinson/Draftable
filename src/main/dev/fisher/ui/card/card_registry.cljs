(ns dev.fisher.ui.card.card-registry
  (:require [dev.fisher.data-model.card-data :as card-data]))



(defonce registered-cards (atom {}))

;; what is needed to register new types of cards
;;  ::predicate
;;  ::class
;;  ::init-state-fn??
;;  ::name
;;  ::id
;;  ::actions??, could be defined on the class as a class option
;;                ^^ No go, if we want anonymous card types with *GenericCard*

(defn register-card 
  [{::card-data/keys [id predicate class name actions] :as card-data}]
  (swap! registered-cards assoc id card-data))


(defn find-cards [card-data]
  (filter (fn [x] ((::card-data/predicate x) card-data))
    (vals @registered-cards)))


;; SearchBox "new file: xxx.clj"
;; New File
;; new ....