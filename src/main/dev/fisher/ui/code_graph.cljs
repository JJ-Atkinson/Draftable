(ns dev.fisher.ui.code-graph
  (:require
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    ["react-d3-graph" :refer [Graph]]
    ))

(def graph
  '{server.pathom-wrappers
    {pathom-registry
     {:filename "src/main/server/pathom_wrappers.clj",
      :ns server.pathom-wrappers,
      :name pathom-registry},
     register!
     {:filename "src/main/server/pathom_wrappers.clj",
      :ns server.pathom-wrappers,
      :name register!,
      :usages
      [{:name debug, :to taoensso.timbre}
       {:name pathom-registry, :to server.pathom-wrappers}]},
     defpathom-backend-endpoint*
     {:filename "src/main/server/pathom_wrappers.clj",
      :ns server.pathom-wrappers,
      :name defpathom-backend-endpoint*,
      :usages
      [{:name conform!, :to com.fulcrologic.fulcro.algorithms.do-not-use}
       {:name env#, :to :clj-kondo/unknown-namespace}
       {:name params#, :to :clj-kondo/unknown-namespace}
       {:name env#, :to :clj-kondo/unknown-namespace}
       {:name params#, :to :clj-kondo/unknown-namespace}
       {:name result#, :to :clj-kondo/unknown-namespace}
       {:name result#, :to :clj-kondo/unknown-namespace}
       {:name result#, :to :clj-kondo/unknown-namespace}
       {:name result#, :to :clj-kondo/unknown-namespace}
       {:name result#, :to :clj-kondo/unknown-namespace}
       {:name env#, :to :clj-kondo/unknown-namespace}
       {:name params#, :to :clj-kondo/unknown-namespace}
       {:name env#, :to :clj-kondo/unknown-namespace}
       {:name params#, :to :clj-kondo/unknown-namespace}
       {:name register!, :to server.pathom-wrappers}]},
     defmutation
     {:filename "src/main/server/pathom_wrappers.clj",
      :ns server.pathom-wrappers,
      :name defmutation,
      :usages
      [{:name defmutation, :to com.wsscode.pathom.connect}
       {:name defpathom-backend-endpoint*, :to server.pathom-wrappers}]},
     defresolver
     {:filename "src/main/server/pathom_wrappers.clj",
      :ns server.pathom-wrappers,
      :name defresolver,
      :usages
      [{:name defresolver, :to com.wsscode.pathom.connect}
       {:name defpathom-backend-endpoint*, :to server.pathom-wrappers}]}},
    server.components.http-server
    {http-server
     {:filename "src/main/server/components/http_server.clj",
      :ns server.components.http-server,
      :name http-server,
      :usages
      [{:name config, :to server.components.config}
       {:name pprint, :to clojure.pprint}
       {:name info, :to taoensso.timbre}
       {:name middleware, :to server.components.middleware}
       {:name run-server, :to org.httpkit.server}
       {:name http-server, :to server.components.http-server}]}},
    server.components.middleware
    {not-found-handler
     {:filename "src/main/server/components/middleware.clj",
      :ns server.components.middleware,
      :name not-found-handler},
     wrap-api
     {:filename "src/main/server/components/middleware.clj",
      :ns server.components.middleware,
      :name wrap-api,
      :usages
      [{:name parser, :to server.components.pathom}
       {:name handle-api-request,
        :to com.fulcrologic.fulcro.server.api-middleware}]},
     index
     {:filename "src/main/server/components/middleware.clj",
      :ns server.components.middleware,
      :name index,
      :usages
      [{:name debug, :to taoensso.timbre}
       {:name html5, :to hiccup.page}]},
     wrap-html-routes
     {:filename "src/main/server/components/middleware.clj",
      :ns server.components.middleware,
      :name wrap-html-routes,
      :usages
      [{:name index, :to server.components.middleware}
       {:name response, :to ring.util.response}
       {:name content-type, :to ring.util.response}]},
     middleware
     {:filename "src/main/server/components/middleware.clj",
      :ns server.components.middleware,
      :name middleware,
      :usages
      [{:name config, :to server.components.config}
       {:name config, :to server.components.config}
       {:name not-found-handler, :to server.components.middleware}
       {:name wrap-api, :to server.components.middleware}
       {:name wrap-transit-params,
        :to com.fulcrologic.fulcro.server.api-middleware}
       {:name wrap-transit-response,
        :to com.fulcrologic.fulcro.server.api-middleware}
       {:name wrap-html-routes, :to server.components.middleware}
       {:name wrap-defaults, :to ring.middleware.defaults}]}},
    server.components.pathom
    {index-explorer
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name index-explorer},
     all-resolvers
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name all-resolvers,
      :usages
      [{:name index-explorer, :to server.components.pathom}
       {:name all-resolvers, :to server.api.editor}
       {:name all-resolvers, :to server.api.project}]},
     preprocess-parser-plugin
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name preprocess-parser-plugin},
     log-requests
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name log-requests,
      :usages [{:name debug, :to taoensso.timbre}]},
     process-error
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name process-error},
     query-params-to-env-plugin
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name query-params-to-env-plugin,
      :usages [{:name query->ast, :to edn-query-language.core}]},
     build-parser
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name build-parser,
      :usages
      [{:name mutate-async, :to com.wsscode.pathom.connect}
       {:name map-reader, :to com.wsscode.pathom.core}
       {:name parallel-reader, :to com.wsscode.pathom.connect}
       {:name open-ident-reader, :to com.wsscode.pathom.connect}
       {:name env-placeholder-reader, :to com.wsscode.pathom.core}
       {:name process-error, :to server.components.pathom}
       {:name all-resolvers, :to server.components.pathom}
       {:name connect-plugin, :to com.wsscode.pathom.connect}
       {:name config, :to server.components.config}
       {:name env-wrap-plugin, :to com.wsscode.pathom.core}
       {:name query-params-to-env-plugin, :to server.components.pathom}
       {:name log-requests, :to server.components.pathom}
       {:name preprocess-parser-plugin, :to server.components.pathom}
       {:name error-handler-plugin, :to com.wsscode.pathom.core}
       {:name request-cache-plugin, :to com.wsscode.pathom.core}
       {:name elide-not-found, :to com.wsscode.pathom.core}
       {:name post-process-parser-plugin, :to com.wsscode.pathom.core}
       {:name trace-plugin, :to com.wsscode.pathom.core}
       {:name parallel-parser, :to com.wsscode.pathom.core}
       {:name getProperty, :to java.lang.System}
       {:name <!!, :to clojure.core.async}]},
     parser
     {:filename "src/main/server/components/pathom.clj",
      :ns server.components.pathom,
      :name parser,
      :usages [{:name build-parser, :to server.components.pathom}]}},
    server.components.config
    {configure-logging!
     {:filename "src/main/server/components/config.clj",
      :ns server.components.config,
      :name configure-logging!,
      :usages
      [{:name info, :to taoensso.timbre}
       {:name merge-config!, :to taoensso.timbre}]},
     config
     {:filename "src/main/server/components/config.clj",
      :ns server.components.config,
      :name config,
      :usages
      [{:name args, :to mount.core}
       {:name load-config!, :to com.fulcrologic.fulcro.server.config}
       {:name info, :to taoensso.timbre}
       {:name configure-logging!, :to server.components.config}]}},
    server.main
    {-main
     {:filename "src/main/server/main.clj",
      :ns server.main,
      :name -main,
      :usages [{:name start-with-args, :to mount.core}]}},
    server.api.project
    {all-project-ns
     {:filename "src/main/server/api/project.clj",
      :ns server.api.project,
      :name all-project-ns,
      :usages
      [{:name run!, :to clj-kondo.core}
       {:name map-vals, :to taoensso.encore}]},
     all-resolvers
     {:filename "src/main/server/api/project.clj",
      :ns server.api.project,
      :name all-resolvers,
      :usages
      [{:name project-namespaces, :to :clj-kondo/unknown-namespace}]}},
    server.api.editor
    {project-dir
     {:filename "src/main/server/api/editor.clj",
      :ns server.api.editor,
      :name project-dir,
      :usages [{:name getProperty, :to java.lang.System}]},
     all-resolvers
     {:filename "src/main/server/api/editor.clj",
      :ns server.api.editor,
      :name all-resolvers,
      :usages
      [{:name open-file, :to :clj-kondo/unknown-namespace}
       {:name save-text, :to :clj-kondo/unknown-namespace}]}}})

(defn graph->d3 [data]
  (let [nodes (->> data
                vals
                (mapcat vals)
                (map #(assoc % :id (str (:ns %) "/" (:name %))))
                (distinct))
        links (->> nodes
                (mapcat
                  (fn [node]
                    (->> (:usages node)
                      (filter #(get-in data [(:to %) (:name %)]))
                      (map #(do {:source (:id node)
                                 :target (str (:to %) "/" (:name %))})))))
                (distinct))]
    {:nodes nodes
     :links links}))

(def ui-graph (react-interop/react-factory Graph))

(defn ui-code-graph []
  (let [data (graph->d3 graph)
        config {:nodeHighlightBehavior true
                :directed true
                }]
    (dom/div
      "HELLO d3"
      (ui-graph
        (clj->js
          {:id "graph-id"
           :data data
           :config config})))))
