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
;; (fn invoke! [action-context-map] any?)
(s/def ::invoke ifn?)
(s/def ::description string?)
;; (fn [context-map] boolean?)
(s/def ::context-pred ifn?)
(s/def ::id (s/or :k qualified-keyword? :s string?))

(s/def ::action
  (s/keys :req [::id
                ::title
                ::invoke]
    :opt [::default-key-combo
          ::description
          ::context-pred]))

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
     (swap! actions-by-id assoc (::id action) (s/assert ::action compiled))))

  ([id title invoke description]
   (register-action!
     {::id id ::title title ::invoke invoke ::description description})))

(defn all-actions [] (vals @actions-by-id))

(def available-actions
  (enc/memoize (* 1000 60 60)
    (fn [context-info]
      (filter (fn [{::keys [context-pred]}]
                (if context-pred
                  (context-pred context-info)
                  true))
        (all-actions)))))

;; more directly keyboard related but oh well

(defonce 
  ^{:doc "Shortcut path -> {::title ::description?}, used for paths on the way
          to other actions."}
  default-shortcut-group-descriptions
  (atom {}))

(defn register-default-group-name
  "Register a title / description for actions under a shared shortcut path"
  ([shortcut-path title]
   (register-default-group-name shortcut-path title nil))
  ([shortcut-path title description]
   (swap! default-shortcut-group-descriptions 
     assoc 
     (compile-key-combo shortcut-path)
     (enc/assoc-some {::title title}
       ::description description))))

(defn shortcut-group-descriptions [] @default-shortcut-group-descriptions)


(register-action!
  {::id                :action/fancy-search
   ::title             "Fancy Search"
   ::invoke            #(js/console.log "Fancy Action!!!")
   ::description       "Runs search"
   ::default-key-combo [["c" "b"] "R"]})

(register-action!
  {::id                :action/alert
   ::title             "Alert"
   ::invoke            #(js/alert "Hiya")
   ::description       "Alerts ya"
   ::default-key-combo ["N"]})

(register-default-group-name ["f"]
  "Config" "Configuration options")
(register-default-group-name ["f" "e"]
  "Edit")

(register-action!
  {::id                :action/edit-config
   ::title             "Edit config"
   ::invoke            #(js/console.log "Edit config")
   ::description       "Alerts ya"
   ::default-key-combo ["f" "e" "d"]})

(register-action!
  {::id                :action/edit-clj-config
   ::title             "Edit clj config"
   ::invoke            #(js/console.log "Edit clj config")
   ::description       "Alerts ya"
   ::default-key-combo ["f" "e" "c"]})
