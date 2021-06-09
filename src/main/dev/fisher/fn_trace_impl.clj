(ns dev.fisher.fn-trace-impl
  (:require [meander.epsilon :as me]
            [dev.fisher.trace-data :as trace-data]
            [clojure.set :as set])
  (:import (clojure.lang IMeta)))


(def ^:dynamic *fn-name* nil)
(def ^:dynamic *previously-captured-symbols*
  "An atom with a set of all previously captured symbols. Prevents double capturing
   of variables seen in let bindings. e.g. (let [x 1] (inc x)) shouldn't capture
   the second instance of 'x."
  nil)
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
                             (when (and (symbol? x) (not= '& x))
                               (swap! seen conj x))
                             x)
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
        arities (if (vector? (first rest))
                  [(seq rest)]
                  rest)]
    (binding [*fn-name* name-sym]
      (postwalk-doall
        `(defn ~name-sym
           ~@(concat ?docstr-?arg-map
               (map (fn [[args & body]]
                      (let [captured-body   (map rewrite-capture* body)
                            vars-to-capture (symbols-of-destructure args)]
                        (swap! *previously-captured-symbols* set/union
                          (set vars-to-capture))
                        `(~args
                           (trace-data/erase-content (quote ~name-sym))
                           ~(capture-syms* nil vars-to-capture)
                           ~@(butlast captured-body)

                           ;; this breaks tail recursive functions
                           (let [~scratch-var ~(last captured-body)]
                             (trace-data/complete-trace (quote ~name-sym))
                             ~scratch-var))))
                 arities)))))))

(defn rewrite-let [bindings statements body]
  (let [vars-to-capture (map symbols-of-destructure bindings)]
    (swap! *previously-captured-symbols* set/union (set (apply concat vars-to-capture)))
    `(let ~(vec (interleave
                  bindings (map rewrite-capture* statements)
                  (repeat scratch-var)
                  (map #(capture-syms* %1 %2) bindings vars-to-capture)))
       ~@(map rewrite-capture* body))))

(defn capture-form-threaded*
  ([thread-first? form]
   (let [meta? (when (instance? IMeta form) (meta form))
         m     (assoc meta?
                 :form `(quote ~form)
                 :fn-name `(quote ~*fn-name*)
                 ;:capture-form-threaded? true ;; useful for debugging
                 )]
     (if thread-first?
       `(trace-data/capture> ~m)
       `(trace-data/capture>> ~m)))))

(defn capture-form-inline*
  ([form]
   (if (and (instance? IMeta form)
         (contains? (meta form) :line)
         (not (contains? @*previously-captured-symbols* form)))
     (let [m (assoc (meta form)
               :form `(quote ~form)
               :fn-name `(quote ~*fn-name*)
               ;:capture-form-inline? true ;; useful for debugging
               )]
       ;; rewrite after collecting metadata, so our capture code is not caught in the meta
       (-> `(trace-data/capture>> ~m ~(rewrite-capture* form))
         ;; don't stomp on compiler meta
         (with-meta (meta form))))
     (rewrite-capture* form))))

(defn rewrite-capture* [form]
  (me/match form
    (defn ?name . !args ...)
    (rewrite-defn ?name !args)

    ((me/or let loop for) [!lhs-let !val ...]
     .
     !outs ...)
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

    (me/and ?form ((me/pred (complement fn-form?) ?fn) . !args ...))
    (with-meta (cons ?fn (map capture-form-inline* !args)) (meta ?form))

    (me/and ?form [!things ...])
    (with-meta (mapv rewrite-capture* !things) (meta ?form))

    ?x ?x))

(defn rewrite-capture [form]
  (binding [*previously-captured-symbols* (atom #{})]
    (rewrite-capture* form)))

(defmacro capture [fn-defn]
  (rewrite-capture fn-defn))


(comment
  (capture
    (defn function [x y & more]
      [x (inc (last more))]))

  (macroexpand-1 '(capture
                    (defn function [x y & more]
                      (let [n [x (inc (last more))]
                            r 8]
                        n))))

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