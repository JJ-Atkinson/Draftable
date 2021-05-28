(ns dev.fisher.ui.action.action-registry
  (:require
    [cljs.spec.alpha :as s]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [com.fulcrologic.guardrails.core :refer [>defn =>]]
    [app.SPA :refer [SPA]]
    [taoensso.encore :as enc]
    [com.fulcrologic.fulcro.components :as comp]))


;; TASK: add context and focus ?

(s/def ::default-key-combo (s/coll-of k-const/str-ified-key-combo?))
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

(defn action? [x]
  (and (map? x) (contains? x ::id)))

(defonce
  ^{:doc ""}
  actions-by-id
  (atom {}))

(defn- compile-key-combo [combos]
  (when combos
    (mapv k-const/coerce-str-ified-key-combo combos)))

(defn register-action!
  ([action]
   (let [compiled (enc/assoc-when action
                    ::default-key-combo (compile-key-combo (::default-key-combo action)))]
     (swap! actions-by-id assoc (::id action) (s/assert ::action compiled)))
   nil)

  ([id title invoke description]
   (register-action!
     {::id id ::title title ::invoke invoke ::description description})))

(defn all-actions [] (vals @actions-by-id))

(register-action!
  {::id                :action/search
   ::title             "Search"
   ::invoke            #(comp/transact! SPA
                          [(dev.fisher.ui.search.search-view/set-visible-search-view
                             {:visible? true})])
   ::description       "Runs search"
   ::default-key-combo ["SPC" ["c" "b"] "K"]})

(register-action!
  {::id                :action/fancy-search
   ::title             "Fancy Search"
   ::invoke            #(js/console.log "Fancy Action!!!")
   ::description       "Runs search"
   ::default-key-combo ["SPC" ["c" "b"] "R"]})

(register-action!
  {::id                :action/alert
   ::title             "Alert"
   ::invoke            #(js/alert "Hiya")
   ::description       "Alerts ya"
   ::default-key-combo ["SPC" "N"]})

(register-action!
  {::id                :action/edit-config
   ::title             "Edit config"
   ::invoke            #(js/console.log "Edit config")
   ::description       "Alerts ya"
   ::default-key-combo ["SPC" "f" "e" "d"]})

(register-action!
  {::id                :action/edit-clj-config
   ::title             "Edit clj config"
   ::invoke            #(js/console.log "Edit clj config")
   ::description       "Alerts ya"
   ::default-key-combo ["SPC" "f" "e" "c"]})
