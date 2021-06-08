(ns dev.fisher.fn-trace-impl
  (:require [meander.epsilon :as me]
            [dev.fisher.trace-data :as trace-data])
  (:import (clojure.lang IMeta)))


(def ^:dynamic *fn-name* nil)
(declare rewrite-capture*)

;; TASK: handle :or lets
(defn symbols-of-destructure
  "In:

   x
   {:keys [x] :as a}
   [{{:keys [n m]} :as inner} _ third]

   Out:
   [x [x]]
   [{:keys [x] :as a} [x a]]
   [[{{:keys [n m]} :as inner} _ third]
    [n m inner _ third]]"
  [lhs-let]
  (let [seen (atom [])]
    (clojure.walk/postwalk (fn [x]
                             (if (and (symbol? x) (not= '& x))
                               (do
                                 (swap! seen conj x)
                                 x)
                               x))
      lhs-let)
    @seen))

(def scratch-var (gensym "trace-scratch-var"))

(defn fn-form? [x]
  (= `fn x))

(defn partition-first
  "Splits first false" [f coll]
  [(take-while f coll) (drop-while f coll)])

(defn postwalk-doall [form]
  (clojure.walk/postwalk (fn [x]
                           (if (list? x)
                             (doall x)
                             x))
    form))

(defn capture-syms* [form symbols]
  (let [meta? (when (and form (instance? IMeta form)) (meta form))]
    `(trace-data/capture-group (quote ~*fn-name*)
       ~(->> symbols
          (mapv (fn [sym] [sym
                           (assoc meta?
                             :form `(quote ~sym)
                             :symbol `(quote ~sym))]))))))

(defn rewrite-defn [name-sym args]
  (let [[?docstr-?arg-map rest] (partition-first #(not (or (vector? %) (list? %)))
                                  args)
        bodies (if (vector? (first rest))
                 [(seq rest)]
                 rest)]
    (binding [*fn-name* name-sym]
      (postwalk-doall
        `(defn ~name-sym
           ~@(concat ?docstr-?arg-map
               (map (fn [[args & body]]
                      `(~args
                         (trace-data/erase-content (quote ~name-sym))
                         ~(capture-syms* nil (symbols-of-destructure args))
                         ~@(map rewrite-capture* body)))
                 bodies)))))))

(defn rewrite-let [bindings statements body]
  (let [vars-to-capture (map symbols-of-destructure bindings)]
    `(let ~(vec (interleave
                  bindings statements
                  (repeat scratch-var)
                  (map #(capture-syms* %1 %2) bindings vars-to-capture)))
       ~@body)))

(defn capture-form-threaded*
  ([thread-first? form]
   (let [meta? (when (instance? IMeta form) (meta form))
         m     (assoc meta? :form `(quote ~form) :fn-name `(quote ~*fn-name*))]
     (if thread-first?
       `(trace-data/capture> ~m)
       `(trace-data/capture>> ~m)))))

(defn capture-form-inline*
  ([form]
   (if (and (instance? IMeta form)
         (contains? (meta form) :line))
     (let [m (assoc (meta form) :form `(quote ~form) :fn-name `(quote ~*fn-name*))]
       (-> `(trace-data/capture>> ~m ~form)
         ;; don't stomp on compiler meta
         (with-meta (meta form))))
     form)))


(defn rewrite-capture* [form]
  (me/match form
    (defn ?name . !args ...)
    (rewrite-defn ?name !args)

    (let [!lhs-let (me/cata !val) ...]
      .
      (me/cata !outs) ...)
    (rewrite-let !lhs-let !val !outs)

    (me/and ?form (->> . !forms ...))
    (with-meta
      (cons `->>
        (interleave !forms (map (partial capture-form-threaded* false) !forms)))
      (meta ?form))

    (me/and ?form (-> . !forms ...))
    (with-meta
      (cons `->
        (interleave !forms (map (partial capture-form-threaded* true) !forms)))
      (meta ?form))

    (me/and ?form ((me/pred (complement fn-form?) ?fn) . (me/cata !args) ...))
    (with-meta (cons ?fn (map capture-form-inline* !args)) (meta ?form))

    (me/and ?form [(me/cata !things) ...])
    (with-meta (apply vector !things) (meta ?form))

    ?x ?x))

(rewrite-capture* '(defn function [arg1 arg2 & more]

                     (let [a 1
                           {:keys [n r]} (f (call (of a)))]
                       (->>
                         (f-call n r)
                         (thread)
                         (funk))
                       (println "hi"))))

(defmacro capture [fn-defn]
  (rewrite-capture* fn-defn))


(comment
  (capture
    (defn function [x y & more]
      [x (inc (last more))]))

  #_(clojure.core/defn
      function
      ([x y & more]
       (dev.fisher.trace-data/erase-content (quote function))
       (dev.fisher.trace-data/capture-group
         (quote function)
         [[x {:form (quote x), :symbol (quote x)}]
          [y {:form (quote y), :symbol (quote y)}]
          [more {:form (quote more), :symbol (quote more)}]])
       (inc
         (dev.fisher.trace-data/capture>>
           (last more)
           {:line 4, :column 12, :form (quote (last more)), :fn-name (quote function)}))))

  (function 1 2 3 4 5 6 7 8))