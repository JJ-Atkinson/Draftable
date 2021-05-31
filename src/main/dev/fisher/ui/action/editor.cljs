(ns dev.fisher.ui.action.editor
  (:require
    [app.SPA :refer [SPA]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.card.card :as card]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.ui.cards.code :as code-card]
    [dev.fisher.ui.workspaces.workspaces-manager :as wm]))

(defn new-card
  ([] (new-card nil ";; TODO your code goes here"))
  ([file code]
   (let [cardid   (gensym)
         carddata {::card-data/code   code
                   ::card-content/id  cardid
                   :source-file file}]
     (comp/transact! SPA
       [(card/set-card-content {:id            cardid
                                :clazz         code-card/CodeCard
                                :initial-state (comp/get-initial-state
                                                 code-card/CodeCard carddata)})
        (wm/add-card-to-current-workspace {:wsm-id :ws-manager-singleton :card-id cardid})]))))

(defn open-file-as-card []
  (let [file "src/dev/user.clj"]
    (df/load! SPA :text nil
      {:params      {:file file}
       :post-action #(new-card file (get-in % [:result :body :text]))})))
