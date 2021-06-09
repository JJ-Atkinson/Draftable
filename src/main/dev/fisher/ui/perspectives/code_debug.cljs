(ns dev.fisher.ui.perspectives.code-debug
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]

    [dev.fisher.data-model.card-data :as card-data]
    [zprint.core :as zprint]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]
    [dev.fisher.ui.action.action-registry :as action-registry]
    [app.SPA :refer [SPA]]
    [taoensso.timbre :as log]
    [dev.freeformsoftware.metacomet.prim.seq-utils :as mc.su]
    [clojure.string :as str]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.mutations :as m]))

(defn reduce-eval-stack [stack]
  (reduce (fn [[order-of-appearance acc :as unchanged] [val {:keys [symbol]}]]
            (if symbol
              [(if (mc.su/in? order-of-appearance symbol)
                 order-of-appearance
                 (conj order-of-appearance symbol))
               (assoc acc symbol val)]
              unchanged))
    [[]
     {}]
    (apply concat stack)))

(defn zprint-small [val]
  (try (zprint/zprint-str val 80
         {:max-length [4 4]})
       (catch :default e "stack overflow")))

(defsc CodeDebugPerspective [this {::card-data/keys [latest-evaluation]
                                   ::keys           [current-position-idx]}]
  {:query         [::card-data/id
                   ::card-data/latest-evaluation
                   ::current-position-idx]
   :initial-state (fn [x] x)
   :ident         ::card-data/id}
  (let [current-position-idx (or current-position-idx 1)
        eval-stack           (take current-position-idx latest-evaluation)
        [symbol-order context-map] (reduce-eval-stack eval-stack)]
    (fui/vstack fui/lowgap-stack
      (fui/slider {:min       1 :max (count latest-evaluation)
                   :value     current-position-idx
                   :showValue true
                   :step      1
                   :onChange  (fn [x] (m/set-value! this ::current-position-idx (log/spy x)))})
      (dom/pre
        (str/join "\n"
          (concat (map (fn [symbol] (str symbol "\t"
                                      (zprint-small (get context-map symbol)))) symbol-order)
            (let [[[val {:keys [form] :as latest-form}] & other-forms] (last eval-stack)]
              (when-not (seq other-forms)
                [(str "Current form:\n"
                   form "\n"
                   (zprint-small val))]))))))))

(perspective-registry/register-perspective!
  #::perspective-registry{:predicate     #(do (log/spy %) true)
                          :id            :perspective/code-debug
                          :initial-state (partial comp/get-initial-state CodeDebugPerspective)
                          :name          "Code Debug"
                          :class         CodeDebugPerspective})

(action-registry/register-action!
  #::action-registry
      {:id                :action/start-debug-card
       :title             "Start debug card"
       :invoke            #(let [card-id (gensym)]
                             (comp/transact! SPA
                               [`(dev.fisher.ui.card.card/set-perspective
                                   ~{:perspective-id :perspective/code-debug
                                     :id             card-id
                                     :merge-state    {::card-data/id [:function 'update-stack]}})
                                `(dev.fisher.ui.workspaces.workspaces-manager/add-card-to-current-workspace
                                   ~{:wsm-id  :ws-manager-singleton
                                     :card-id card-id})]))
       :default-key-combo ["c" "d"]})
