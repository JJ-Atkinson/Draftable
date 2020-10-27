(ns app.client
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.SPA :refer [SPA]]
    [fisher.plugins.editor1.editor1-root :as e1r]
    [rewrite-clj.parser :as p]
    [rewrite-clj.node :as n]
    [rewrite-clj.zip :as z]
    [taoensso.timbre :as log]))


(def print-node
  (z/of-string
    (str '(defn parse-fn-syntax
            "Multiple syntax are available for defining the function.
            (implicit syntax 0) Any constant that is not wrapped in a list is a constant.
            Syntax 1: `(* :a/a :b/b :c)` will pull from the map :a/a, :b/b, and :c. All keywords will be mangled.
            Syntax 2: `([fn]? [:a/a :b/b :c] (* a b c)) will be equivalent. fn is optional. ALL usages of the
                symbols a, b, and c WILL be mangled, even if you nest them in a let block.
            Syntax 3: `([fn]? {kwa :a/a kwb :b/b kwc :c} (* kwa kwb kwc))` will also be equivalent. ALL usages of
                the symbols kwa, kwb, and kwc WILL be mangled."
            {:attr "whatever"}
            [fn-form]
            (let [fn-form (if (not (list? fn-form)) (list 'identity fn-form) fn-form)
                  fn-form (if (= 'fn (first fn-form)) (rest fn-form) fn-form)
                  head (first fn-form)
                  type (cond (vector? head) 2
                             (map? head) 3
                             :default 1)
                  deps-form (if (= 1 type) fn-form head)
                  no-arg-list (if (#{2 3} type) (rest fn-form) fn-form)
                  fn-form (cond
                            (= type 1) no-arg-list
                            (not= 1 (count no-arg-list)) (cons 'do no-arg-list)
                            :default (first no-arg-list)) ;; if they have multiple statements, wrap in an implicit do
                  deps (->> deps-form flatten-maps (filter keyword?))
                  destructure-mapping (case type 1 (create-map identity deps)
                                                 2 (create-map (comp symbol name) deps)
                                                 3 (set/map-invert head))]
              {:destructure-mapping destructure-mapping
               :raw-form            fn-form
               :dependencies        deps})))))

(defn choose-wrap [exp]
  (if (< 50 (n/length exp)) "wrap-down" "no-wrap"))

(defmulti render-exp (fn [exp zippr] (n/tag exp)))

(defn render-children [zippr]
  (loop [rendered []
         z zippr]
    (let [r (conj rendered (render-exp (z/node z) z))]
      (if (z/rightmost? z)
        r
        (recur r (z/right z))))))

(defmethod render-exp :list [exp zippr]
  (let [childs (z/down zippr)
        rc (render-children childs)]
    (dom/div :.d-expr.d-list {:classes [(choose-wrap exp)]}
      (dom/div :.d-element.list-head "(")
      (dom/div :.d-content rc))))

(defmethod render-exp :vector [exp zippr]
  (let [childs (z/down zippr)
        rc (render-children childs)]
    (dom/div :.d-expr.d-vec {:classes [(choose-wrap exp)]}
      (dom/div :.d-element.vec-head "[")
      (dom/div :.d-content rc))))

(defmethod render-exp :map [exp zippr]
  (let [childs (z/down zippr)
        rc (render-children childs)]
    (dom/div :.d-expr.d-map
      (dom/div :.d-element.map-head "{")
      (dom/div :.d-content rc))))

(defmethod render-exp :token [exp zippr]
  (dom/div :.d-token
    (cond (contains? exp :string-value) (:string-value exp)
          (contains? exp :k) (str "" (:k exp))
          (contains? exp :lines) (str "\"" (first (:lines exp)) "\"")
          :else "Unknown")))

(defmethod render-exp :default [exp zippr]
  (dom/div (str "Not implemented: " (n/tag exp))))

(comment
  (-> print-node z/down z/right z/right z/right z/down z/node ))


(defsc Root [this props]
  (dom/div (render-exp (z/node print-node) print-node)))



(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! SPA Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! SPA Root "app")
  (js/console.log "Hot reload"))
