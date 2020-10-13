(ns fisher.plugin-support.alting-root)



;; store id-key -> 
;; {
;;  :known-renderers
;;       {key component-id

#_{:editor/id
   {
    :known-renderers      {:.../editor1 {:root Editor1_DEFSC
                                         ; optional
                                         :init-transaction  
                                         }}}}
(defonce component-details (atom {}))

