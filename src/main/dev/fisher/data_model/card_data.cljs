(ns dev.fisher.data-model.card-data
  (:require [clojure.spec.alpha :as s]))




(s/def ::code string?)
(s/def ::namespace symbol?)
(s/def ::file-name string?)
(s/def ::latest-evaluation map?)



(s/def ::card-data 
  (s/keys :opt [::code 
                ::namespace 
                ::file-name
                ::latest-evaluation]))
