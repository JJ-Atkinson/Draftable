(ns dev.fisher.trace-data
  #?(:cljs
     (:require
       [dev.fisher.data-model.card-data :as card-data]
       [com.fulcrologic.fulcro.components :as comp]
       [app.SPA :refer [SPA]])))


;;             capture group
;; fn-name -> [[value meta-map] ...]
;; meta-map {?line ?column form fn-name}  
(defonce latest-evals-atom (atom {}))

(defn erase-content [fn-name]
  (swap! latest-evals-atom assoc fn-name [])
  nil)

(defn capture-group [fn-name v-meta-pairs]
  (swap! latest-evals-atom update fn-name conj v-meta-pairs)
  nil)

(defn capture> [value meta-map]
  (swap! latest-evals-atom update (:fn-name meta-map) conj [[value meta-map]])
  value)

(defn capture>> [meta-map value] (capture> value meta-map))

#?(:cljs
   (defn complete-trace [fn-name]
     (comp/transact! SPA [(card-data/attach-card-info
                            {::card-data/id                [:function fn-name]
                             ::card-data/latest-evaluation (get @latest-evals-atom fn-name)})]))

   :clj
   (defn complete-trace [fn-name]
     nil))