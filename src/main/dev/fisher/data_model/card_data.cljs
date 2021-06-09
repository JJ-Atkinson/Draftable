(ns dev.fisher.data-model.card-data
  (:require [clojure.spec.alpha :as s]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))


(def content-ident-key
  "This is the table that stores information about card contents (e.g. editor state, code, latest eval, etc)"
  ::id)


(defsc BlankCard [this props]
  {:query [::id]
   :ident ::id}
  nil)


(defmutation attach-card-info [{::keys [id] :as updated-data}]
  (action [{:keys [state]}]
    (swap! state update-in [::id id] merge updated-data)))

(s/def ::code string?)
(s/def ::namespace symbol?)
(s/def ::file-name string?)
(s/def ::latest-evaluation map?)

(s/def ::card-data
  (s/keys :opt [::code
                ::namespace
                ::file-name
                ::latest-evaluation]))
