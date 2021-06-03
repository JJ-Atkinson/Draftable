(ns dev.fisher.ui.perspectives.custom
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [dev.fisher.ui.card.card-content :as card-content]
    [dev.fisher.data-model.card-data :as card-data]
    [dev.fisher.ui.editor.codemirror-core :as codemirror]
    [dev.fisher.ui.card.perspective-registry :as perspective-registry]
    [dev.fisher.ui.perspectives.view-registry :as view-registry]
    [dev.fisher.ui.perspectives.transform-registry :as transform-registry]
    [taoensso.encore :as enc]
    [dev.fisher.fluentui-wrappers :as fui]
    [com.fulcrologic.fulcro.mutations :as m]))


(defsc CustomPerspective [this {::keys [selected-view selected-transform show-options?] :as props}]
  {:query         [::card-content/id
                   ::selected-view
                   ::selected-transform
                   ::show-options?
                   '*]
   :ident         ::card-content/id
   :initial-state (fn [{:keys [default-view default-transform show-options?]
                        :or   {show-options? true}}]
                    (enc/assoc-some {::show-options? show-options?}
                      ::selected-transform default-transform
                      ::selected-view default-view))}
  (letfn [(wrap-dropdown [x]
            (fui/vstack fui/lowgap-stack
              (fui/hstack fui/lowgap-stack
                (fui/dropdown
                  {:placeholder "Transform"
                   :options     (fui/simple-dropdown-opts ::transform-registry/id ::transform-registry/name
                                  (transform-registry/available-transforms props))
                   :selected    selected-transform
                   :onChange    (fn [trans] (m/set-value!! this ::selected-transform trans))})
                (fui/dropdown
                  {:placeholder "View"
                   :options     (fui/simple-dropdown-opts ::view-registry/id ::view-registry/name
                                  (view-registry/available-views props))
                   :selected    selected-view
                   :onChange    (fn [view] (m/set-value!! this ::selected-view view))}))
              x))]
    (let [res (enc/when-let [view      (view-registry/get-view selected-view)
                             transform (transform-registry/get-transform selected-transform)]
                (view (transform (props))))]
      (if show-options?
        (wrap-dropdown res)
        res))))

(def ui-custom-perspective (comp/factory CustomPerspective {:keyfn card-content/content-ident-key}))

(perspective-registry/register-perspective!
  #::perspective-registry{:id            :perspective/custom
                          :initial-state (partial comp/get-initial-state CustomPerspective)
                          :name          "Custom"
                          :class         CustomPerspective})
