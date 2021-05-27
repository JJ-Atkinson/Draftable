(ns dev.fisher.ui.action.action-registry
  (:require
    [cljs.spec.alpha :as s]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]
    [taoensso.encore :as enc]))


;; TASK: add context and focus ?

(s/def ::default-key-combo (s/or :compiled (s/coll-of ::k-const/key-combo)
                           :not-compiled (s/coll-of vector?)))
(s/def ::title (s/and string? #(< (count %) 30)))
(s/def ::invoke ifn?)
(s/def ::description string?)
(s/def ::id (s/or :k qualified-keyword? :s string?))

(s/def ::action 
  (s/keys :req [::id
                ::title 
                ::invoke
                ::description]
    :opt [::default-key-combo]))

(defonce
  ^{:doc ""}
  actions-by-id
  (atom {}))

(defn- compile-key-combo [combos]
  (when combos
    (mapv (partial apply k-const/build-key-combo-matcher) combos)))

(>defn register-action!
  ([action]
   [::action => nil?]
   (swap! actions-by-id assoc (::id action)
     (enc/assoc-when action
       ::default-key-combo (compile-key-combo (::default-key-combo action))))
   nil)
  
  ([id title invoke description]
   [::id ::title ::invoke ::description => nil?]
   (register-action! 
     {::id id ::title title ::invoke invoke ::description description})))

(register-action!
  {::id :action/search
   ::title "Search"
   ::invoke #(js/console.log "Action!!!")
   ::description "Runs search"
   ::default-key-combo [["c" "a"]]}
  )

(register-action!
  {::id :action/fancy-search
   ::title "Fancy Search"
   ::invoke #(js/console.log "Fancy Action!!!")
   ::description "Runs search"
   ::default-key-combo [["c" "b"] ["R"]]}
  )