(ns fisher.plugin-support.dynamic-mount-support
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.SPA :refer [SPA]]
    [taoensso.timbre :as log]))


;; Turns out dynamic queries are not capable of supporting cases


(defsc DynamicQueryInner [this {:keys [:dqt-i/id :dqt-i/render-fn] :as props}]
  {:query         [:dqt-i/id
                   :dqt-i/render-fn
                   :dqt-i/bottom-right-render]
   :ident         :dqt-i/id
   :initial-state #:dqt-i{:id        :param/id
                          :render-fn (comp/factory DynamicQueryInner
                                       {:key-fn    :dqt-i/id
                                        :qualifier (random-uuid)})}}
  (dom/div
    (dom/div (str "current query -" (comp/get-query render-fn (app/current-state this))))
    (dom/div (str "props" props))
    (dom/br)
    (when-let [rf (:dqt-i/bottom-left-render props)]
      (rf (:dqt-i/bottom-left-props props)))
    (when-let [rf (:dqt-i/bottom-right-render props)]
      (rf (:dqt-i/bottom-right-props props)))))



(defsc DynamicQueryTest [this {:dqt/keys [id qinner] :as props}]
  {:query         [:dqt/id
                   {:dqt/qinner (comp/get-query DynamicQueryInner)}]
   :ident         :dqt/id
   :initial-state {:dqt/id     1
                   :dqt/qinner {:id 1}}}
  (dom/div
    (dom/div {:style {:color "blue"}}
      (str (comp/get-query
             (:dqt-i/render-fn qinner)
             (app/current-state this))))
    ((:dqt-i/render-fn qinner) qinner)))

(def ui-dynamic-query-test (comp/factory DynamicQueryTest {:keyfn :dqt/id}))


(defsc RedComp [this {:keys [:red/id :red/text] :as props}]
  {:query         [:red/id :red/text]
   :ident         :red/id
   :initial-state #:red{:id   :param/id
                        :text :param/text}}
  (dom/div {:style {:color "red"}} text))

(def ui-red-comp (comp/factory RedComp {:keyfn :red/id}))

(defsc GreenComp [this {:keys [:green/id :green/text] :as props}]
  {:query         [:green/id :green/text]
   :ident         :green/id
   :initial-state #:green{:id   :param/id
                          :text :param/text}}
  (dom/div {:style {:color "green"}} text))

(def ui-green-comp (comp/factory GreenComp {:keyfn :green/id}))

(defn merge-query [a b]
  (let [join-query-to-remove (->> a
                               (filter map?)
                               (map #(-> % keys first))
                               set)
        filtered-first-query (remove
                               #(and (map? %) (contains? join-query-to-remove
                                                (-> % keys first)))
                               a)]
    (vec (clojure.set/union (set filtered-first-query) (set b)))))

(defmutation mount-comp [{:keys [init-state-args
                                 render-fn
                                 component
                                 render-fn-mount-point
                                 props-mount-point]}]
  (action [{:keys [state]}]
    (let [s @state
          factory (get-in s [:dqt-i/id 1 :dqt-i/render-fn])
          original-query (comp/get-query factory s)
          init-state (comp/get-initial-state (log/spy component) (log/spy init-state-args))
          new-query (merge-query original-query
                      [render-fn-mount-point
                       {props-mount-point (comp/get-query component)}])
          ident (log/spy (comp/get-ident
                           component (log/spy init-state)))
          new-state (update-in s [:dqt-i/id 1]
                      assoc render-fn-mount-point render-fn
                      props-mount-point ident)
          new-state (assoc-in new-state ident init-state)]
      (comp/set-query! SPA factory {:query new-query})
      (reset! state new-state))))

(comment
  (comp/get-initial-state RedComp {:id 89 :text "RED!"})
  (let [s (app/current-state SPA)
        factory (get-in s [:dqt-i/id 1 :dqt-i/render-fn])]
    (comp/set-query! SPA factory {:query [:dqt-i/id
                                          :dqt-i/render-fn
                                          :dqt-i/bottom-right-render
                                          {:dqt-i/bottom-right-props (comp/get-query RedComp)}]}))
  (app/force-root-render! SPA)
  (comp/transact!
    SPA
    [(mount-comp {:init-state-args       {:id 89 :text "RED!"}
                  :render-fn             ui-red-comp
                  :component             RedComp
                  :render-fn-mount-point :dqt-i/bottom-right-render
                  :props-mount-point     :dqt-i/bottom-right-props})]))



