(ns server.components.pathom
  (:require
    [edn-query-language.core :as eql]
    [server.components.config :refer [config]]
    [clojure.core.async :as async]
    [server.api.editor :as api.editor]
    [server.api.project :as api.project]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [mount.core :refer [defstate]]
    [taoensso.timbre :as log]))

(pc/defresolver index-explorer [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
     (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
     (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(def all-resolvers [index-explorer api.editor/all-resolvers api.project/all-resolvers])

(defn preprocess-parser-plugin
  "Helper to create a plugin that can view/modify the env/tx of a top-level request.

  f - (fn [{:keys [env tx]}] {:env new-env :tx new-tx})

  If the function returns no env or tx, then the parser will not be called (aborts the parse)"
  [f]
  {::p/wrap-parser
   (fn transform-parser-out-plugin-external [parser]
     (fn transform-parser-out-plugin-internal [env tx]
       (let [{:keys [env tx] :as req} (f {:env env :tx tx})]
         (if (and (map? env) (seq tx))
           (parser env tx)
           {}))))})

(defn log-requests [{:keys [env tx] :as req}]
  (log/debug "Pathom transaction:" (pr-str tx))
  req)

(defn process-error [env err] (prn err))

(def query-params-to-env-plugin
  "Adds top-level load params to env, so nested parsing layers can see them."
  {::p/wrap-parser
   (fn [parser]
     (fn [env tx]
       (let [children     (-> tx eql/query->ast :children)
             query-params (reduce
                            (fn [qps {:keys [type params] :as x}]
                              (cond-> qps
                                (and (not= :call type) (seq params)) (merge params)))
                            {}
                            children)
             env          (assoc env :query-params query-params)]
         (parser env tx))))})

(defn build-parser []
  (let [real-parser (p/parallel-parser
                      {::p/mutate  pc/mutate-async
                       ::p/env     {::p/reader               [p/map-reader pc/parallel-reader
                                                              pc/open-ident-reader p/env-placeholder-reader]
                                    ::p/placeholder-prefixes #{">"}
                                    ::p/process-error        process-error}
                       ::p/plugins [(pc/connect-plugin {::pc/register all-resolvers})
                                    (p/env-wrap-plugin (fn [env]
                                                         (assoc env
                                                           :config config)))
                                    query-params-to-env-plugin
                                    (preprocess-parser-plugin log-requests)
                                    p/error-handler-plugin
                                    p/request-cache-plugin
                                    (p/post-process-parser-plugin p/elide-not-found)
                                    p/trace-plugin]})
        ;; NOTE: Add -Dtrace to the server JVM to enable Fulcro Inspect query performance traces to the network tab.
        ;; Understand that this makes the network responses much larger and should not be used in production.
        trace?      (not (nil? (System/getProperty "trace")))]
    (fn wrapped-parser [env tx]
      (async/<!! (real-parser env (if trace?
                                    (conj tx :com.wsscode.pathom/trace)
                                    tx))))))

(defstate parser
  :start (build-parser))
