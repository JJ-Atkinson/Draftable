(ns dev.fisher.ui.card.impl-fulcro-floating-root
  (:require-macros [com.fulcrologic.fulcro.react.hooks :refer [use-effect use-lifecycle]])
  (:require
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [goog.object :as gobj]
    cljsjs.react
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]
    [com.fulcrologic.fulcro.react.hooks :refer [useState use-state]]
    [taoensso.timbre :as log]))

(let [initial-mount-state (fn []
                            (let [componentName (keyword "com.fulcrologic.fulcro.floating-root" (gensym "generated-root"))]
                              #js [componentName nil]))]
  (defn use-fulcro-mount
    "
    Generate a new sub-root that is controlled and rendered by Fulcro's multi-root-renderer.

    ```
    ;; important, you must use hooks (`defhc` or `:use-hooks? true`)
    (defsc NewRoot [this props]
      {:use-hooks? true}
      (let [f (use-fulcro-mount this {:child-class SomeChild})]
        ;; parent props will show up in SomeChild as computed props.
        (f props)))
    ```

    WARNING: Requires you use multi-root-renderer."
    [parent-this {:keys [child-class
                         initial-state-params]}]
    ;; factories are functions, and if you pass a function to setState it will run it, which is NOT what we want...
    (let [st                 (useState initial-mount-state)
          pass-through-props (atom {})
          key-and-root       (aget st 0)
          setRoot!           (aget st 1)
          child-factory-st   (useState nil)
          _                  (println "adding set child factory")
          setChildFactory!   (aget child-factory-st 1)
          _                  (use-lifecycle
                               (fn setup* []
                                 (let [join-key      (aget key-and-root 0)
                                       child-factory (comp/factory child-class {:qualifier join-key})
                                       initial-state (comp/get-initial-state child-class (or initial-state-params {}))
                                       _             (println [:compfact
                                                               child-class
                                                               key-and-root])
                                       cls           (comp/configure-hooks-component!
                                                       (fn [this fulcro-props]
                                                         (use-lifecycle
                                                           (fn [] (mrr/register-root! this))
                                                           (fn [] (mrr/deregister-root! this)))
                                                         (comp/with-parent-context parent-this
                                                           (child-factory
                                                             (comp/computed
                                                               (get fulcro-props join-key initial-state)
                                                               @pass-through-props))))
                                                       {:query         (fn [_] [{join-key (comp/get-query child-factory)}])
                                                        :initial-state (fn [_] {join-key initial-state})
                                                        :componentName join-key})
                                       real-factory  (comp/factory cls {:keyfn (fn [_] join-key)})
                                       factory       (with-meta
                                                       (fn [props]
                                                         (reset! pass-through-props props)
                                                         (real-factory {}))
                                                       (meta real-factory))]
                                   (println "setting child factory now!")
                                   (setChildFactory! child-factory)
                                   (setRoot! #js [join-key factory])))
                               (fn teardown* []
                                 (let [join-key (aget key-and-root 0)
                                       state    (-> parent-this comp/any->app :com.fulcrologic.fulcro.application/state-atom)]
                                   (swap! state dissoc join-key))))]
      [(aget child-factory-st 0)
       (aget key-and-root 1)])))
