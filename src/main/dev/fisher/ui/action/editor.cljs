(ns dev.fisher.ui.action.editor
  (:require
    [app.SPA :refer [SPA]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.card.card :as card]
    
    [dev.fisher.ui.perspectives.code :as code-card]
    [dev.fisher.ui.workspaces.workspaces-manager :as wm]))

(defn new-card
  ([] (new-card "WIP" ";; TODO your code goes here FRESH EDITZ"))
  ([file {{code :text NS :namespace} :file}]
   (let [cardid   (gensym "CARD_")
         carddata {::card-data/code      code
                   ::card-data/namespace NS
                   ::card-data/id     cardid
                   :source-file          file}]
     (comp/transact! SPA
       [(card/set-perspective {:id             cardid
                               :perspective-id :perspective/code
                               :merge-state    carddata})
        (wm/add-card-to-current-workspace {:wsm-id :ws-manager-singleton :card-id cardid})]))))

(defn open-file-as-card
  ([] (open-file-as-card "src/dev/user.clj"))
  ([file]
   (df/load! SPA :file nil
     {:params      {:file file}
      :post-action #(new-card file (get-in % [:result :body]))})))

(defn open-namespace-as-card [NS]
   (let [file (get-in @(:com.fulcrologic.fulcro.application/state-atom SPA)
                [:project-namespaces NS :file])]
     (when file
       (df/load! SPA :file nil
         {:params      {:file file}
          :post-action #(new-card file (get-in % [:result :body]))}))))
