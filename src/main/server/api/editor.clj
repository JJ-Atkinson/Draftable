(ns server.api.editor
  (:require
    [clojure.java.io :as io]
    [com.wsscode.pathom.connect :as pc]
    [server.pathom-wrappers :refer [defmutation defresolver]]
    [taoensso.timbre :as log]))

(def project-dir (System/getProperty "user.dir"))

(defresolver open-file [{{:keys [file]} :query-params} _]
  {::pc/output [:text]}
  (log/infof "OPEN_TEXT %s" file)
  {:text (try (slurp (str project-dir "/" file))
           (catch java.io.FileNotFoundException _
             "FILE NOT FOUND"))})

(defmutation save-text [env {:keys [file text]}]
  {::pc/input #{:file :text}}
  (log/infof "SAVE_TEXT %s <- %s" file text)
  (spit (io/file project-dir file) text)
  {})

(def all-resolvers
  [open-file save-text])
