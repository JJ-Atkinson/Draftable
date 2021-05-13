(ns development
  (:require
    [clojure.tools.namespace.repl :as tools-ns]
    [mount.core :as mount]
    [server.components.http-server]))

(defn start [] (mount/start))

(defn stop [] (mount/stop))

(defn restart [] (stop) (tools-ns/refresh :after 'development/start))
