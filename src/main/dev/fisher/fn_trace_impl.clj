(ns dev.fisher.fn-trace-impl
  (:require [meander.epsilon :as me]
            [taoensso.encore :as enc]
            [zprint.core :as zprint])
  (:import (java.util UUID)
           (clojure.lang IMeta)))


(def test-expr
  (clojure.walk/macroexpand-all
    '(defn x [arga argb]
       (let [n (inc arga)
             [a b :as rst] (seq argb)]
         (fn f-name [a k] nil)
         (concat rst [n a b])))))

(defmacro wrap-trace [x]
  (println (clojure.walk/macroexpand-all x))
  x)

(defn prm [n]
  (binding [*print-meta* true]
    (pr n)))

(defn gen-id []
  (UUID/randomUUID))


;; TASK: handle :or lets
(defn rewrite-lhs-let
  "In:

   x
   {:keys [x] :as a}
   [{{:keys [n m]} :as inner} _ third]

   Out:
   [^:id x [x]]
   [{:keys [^:id x] :as ^:id a} [x a]]
   [[{{:keys [^:id n ^:id m]} :as ^:id inner} ^:id _ ^:id third]
    [n m inner _ third]]"
  [lhs-let]
  (let [seen (atom [])]
    [(clojure.walk/postwalk (fn [x]
                              (if (symbol? x)
                                (let [x (vary-meta x assoc :trace-id (gen-id))]
                                  (swap! seen conj x)
                                  x)
                                x))
       lhs-let)
     @seen]))

(defn unzipmap
  "Turn a vector of pairs into a pair of vectors.
   [[a1 b1] [a2 b2]] => [[a1 a2] [b1 b]]"
  [coll]
  (reduce (fn [[as bs] [a b]] [(conj as a) (conj bs b)])
    [[] []] coll))

(def scratch-var (gensym "trace-scratch-var"))

(defn fn-form? [x]
  (= `fn x))

(defmulti dispatch-form-type (fn [type form args] type))

(defn dispatch-unknown [form]
  (dispatch-form-type nil form nil))

(defmethod dispatch-form-type :default [_ form _]
  (me/match form
    (let [!lhs-let !val ...]
      .
      !outs ...)
    (dispatch-form-type :let form {:bindings !lhs-let :statements !val :body !outs})

    ((me/pred (complement fn-form?) ?fn) . !args ...)
    (dispatch-form-type :fn-invocation form {:function ?fn :args !args})

    ;(`fn ?args !body)
    ;(me/pred symbol? ?x)
    ;`(capture ~(rand-int 100) ~?x)

    ?x ?x))



(defn capture> [name data])

(defn capture-local-var [statement val]
  (println (str "capt " (when-let [m (meta statement)] (str "^" m " ")) statement " " val))
  )

(defn capture-syms* [syms]
  (with-meta `(do ~@(map (fn [s] `(capture-local-var (quote ~s) ~s)) syms))
    {:elide-print? true}))

(defmethod dispatch-form-type :let [_ form {:keys [bindings statements body]}]
  (let [[new-lhs vars-to-capture] (unzipmap (map rewrite-lhs-let bindings))]
    `(let ~(vec (interleave
                  new-lhs statements
                  (repeat (with-meta scratch-var {:elide-print? true}))
                  (map capture-syms* vars-to-capture)))
       ~@(map dispatch-unknown body))))

(defn capture-statement! [id statement]
  (println id statement)
  statement)

(defn capture-statement* [statement]
  (let [id (gen-id)]
    (if (instance? IMeta statement)
      (vary-meta `(capture-statement! ~id ~(vary-meta statement assoc :trace-id id))
        assoc :raise-last? true)
      statement)))

(defmethod dispatch-form-type :fn-invocation [_ form {:keys [function args]}]
  `(~function ~@(map (comp capture-statement* dispatch-unknown) args)))

(defmacro nn [a b]
  (prm &env)
  (println)
  (prm &form)
  a)

(let [a 8]
  (inc (nn 1 4)))

(comment
  (zprint/zprint-file-str
    (with-out-str
      (prm '(let [a 1
                  k (complicated (inner
                                   (function does nothing)))
                  b 2]
              (-> 4
                (a 88)
                (b)))))
    ))

(comment
  (prm '(let [a 1
            k (complicated (inner
                             (function does nothing)))
            b 2]
        (-> 4
          (a 88)
          (b)))))



(comment (dispatch-form-type :let
           nil
           {:bindings   ['a 'b {:keys ['n 'r]}]
            :statements [1 8 {:n :n-here :r :r-here}]
            :body       '[(inc a)]}))

(defn get-printable-form [traced-form]
  (let [elide?      #(and (instance? IMeta %) (:elide-print? %))
        raise-last? #(and (instance? IMeta %) (:raise-last? %))]
    (clojure.walk/postwalk
      (fn [x]
        (cond (map? x) (->> x (enc/remove-keys elide?) (enc/remove-vals elide?))
              (seq? x) (into (empty x) (remove elide?) x)
              (raise-last? x) (last x)
              :else x))
      traced-form)))


(defmacro c [all]
  (dispatch-form-type nil all nil))

(comment
  (macroexpand-1 '(c (let [a (comp inc inc)] (a (inc 3)))))
  (prm (macroexpand-1 '(c (let [a (comp inc inc)] (a (inc 3))))))
  (get-printable-form (macroexpand-1 '(c (let [a (comp inc inc)] (a (inc 3)))))))

(comment
  (prm (rewrite-lhs-let '{:keys [a b c]}))
  (capture-syms* ['a 'b]))

(def captures (atom {}))
(defn capture-var [fn-id uuid name depth value]
  (swap! captures assoc uuid {:name name :uuid uuid :fn-id fn-id :depth depth})
  value)


(comment
  (me/match
    ))
