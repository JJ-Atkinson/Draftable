(ns dev.fisher.ui.search.search-view-utils
  (:require [com.fulcrologic.fulcro.components :as comp]
            [com.fulcrologic.fulcro.dom :as dom]))


(defn simple-render-results
  
  [line-content-fn
   search-provider
   results
   highlight-index]
  (when (seq results)
    (apply comp/fragment
      (dom/div :.result-header (:dev.fisher.ui.search.search-provider/title search-provider))
      (map-indexed
        (fn [idx res]
          (dom/div :.result
            {:key     (hash res)
             :classes [(when (= highlight-index idx) "active")]}
            (line-content-fn res)))
        results))))