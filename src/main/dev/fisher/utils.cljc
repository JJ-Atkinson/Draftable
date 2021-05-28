(ns dev.fisher.utils)

(defn compose-compare
  "Takes n compare fns, and composes them in LTR order, returning
   the first non zero result. *Tests are run left to right, unlike `comp`*!"
  ([comp] comp)
  ([a b]
   (fn composed-compare* [x y]
     (let [acomp (a x y)]
       (if (zero? acomp)
         (b x y)
         acomp))))
  ([a b & rest]
   (let [comparator-list (->> rest seq (cons b) (cons a))]
     (fn composed-compare* [x y]
       (or (some #(let [result (% x y)]
                    (when (not= 0 result) result))
             comparator-list)
         0)))))