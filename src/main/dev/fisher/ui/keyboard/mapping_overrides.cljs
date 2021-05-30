(ns dev.fisher.ui.keyboard.mapping-overrides
  (:require [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
            [dev.fisher.ui.action.action-registry :as action-registry]
            [cljs.spec.alpha :as s]
            [taoensso.encore :as enc]))


(defonce
  ^{:doc "A map of action-id -> new-shortcut-vec"}
  shortcut-overrides (atom {}))

(defn compile-shortcut [shortcut]
  (s/assert (s/coll-of k-const/str-ified-key-combo?)
    (mapv k-const/coerce-str-ified-key-combo shortcut)))

(defn register-shortcut-override [id shortcut]
  (swap! shortcut-overrides assoc id (compile-shortcut shortcut)))

(defn all-actions [actions]
  (reduce (fn [actions [action-id new-shortcut]]
            (if (contains? actions action-id)
              (assoc-in actions [action-id ::action-registry/default-key-combo]
                new-shortcut)
              actions))
    actions
    @shortcut-overrides))

(defonce
  ^{:doc "A map of shortcut-vec -> {::action-registry/title 
                                    ?::action-registry/description}"}
  shortcut-description-overrides
  (atom {}))

(defn register-override-group-name
  ([shortcut title]
   (register-override-group-name shortcut title nil))
  ([shortcut title description]
   (swap! assoc shortcut-description-overrides
     (compile-shortcut shortcut)
     (enc/assoc-when {::action-registry/title title}
       ::action-registry/description description))))

(defn shortcut-group-descriptions [default-descriptions]
  (merge default-descriptions
    @shortcut-description-overrides))

(defn build-keyboard-action-map [actions group-descriptions]
  (let [merge-ex (fn [a b] (when a (merge a b)))
        combo-tree (k-const/build-key-combo-tree
                     (into {} (map (fn [x]
                                     [(::action-registry/default-key-combo x) x])
                                actions)))]
    (reduce (fn [acc [shortcut desc]]
              (update-in acc shortcut merge-ex desc))
      combo-tree
      group-descriptions)))