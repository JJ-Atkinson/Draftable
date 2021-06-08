(ns dev.fisher.trace-data)


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