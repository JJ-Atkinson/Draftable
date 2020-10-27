(ns scratch-fisher.rewrite-clj-test
  (:require [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [rewrite-clj.zip :as z]))

(def form (p/parse-string "(defn my-function [a]\n  (* a 3))"))
(def mapp (p/parse-string "{:a {:b {:c (fn [x] x)}}}"))


(comment
  (n/tag form)          ;; => :list
  (n/children form)     ;; => (<token: defn> <whitespace: " "> <token: my-function> ...)
  (n/sexpr form)        ;; => (defn my-function [a] (* a 3))
  (n/child-sexprs form) ;; => (defn my-function [a] (* a 3)))
  
  (n/sexpr mapp)
  (n/child-sexprs mapp)
  
  (n/eval-node)
  
  (n/coerce '(defn a [{:keys [b c]}]
               (reduce + a b)))
  

  )