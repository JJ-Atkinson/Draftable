(ns server.main
  (:require
    [mount.core :as mount]
    server.components.http-server)
  (:gen-class))

(defn -main [& args]
  (mount/start-with-args {:config "config/prod.edn"}))
