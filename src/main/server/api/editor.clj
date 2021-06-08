(ns server.api.editor
  (:require
    [clj-kondo.core :as kondo]
    [clojure.java.io :as io]
    [com.wsscode.pathom.connect :as pc]
    [server.pathom-wrappers :refer [defmutation defresolver]]
    [taoensso.timbre :as log]))

(def project-dir (System/getProperty "user.dir"))

(defresolver open-file [{{:keys [file]} :query-params} _]
  {::pc/output [:file]}
  (log/infof "OPEN_TEXT %s" file)
  (try {:file {:text      (slurp (str project-dir "/" file))
               ;; TODO: only do if clojure file
               :namespace (-> (kondo/run! {:lint [file] :config {:output {:analysis true}}})
                            :analysis
                            :namespace-definitions
                            first
                            :name)}}
    (catch java.io.FileNotFoundException _
      {:text "FILE NOT FOUND"})))

(defmutation save-text [env {:keys [file text]}]
  {::pc/input #{:file :text}}
  (log/infof "SAVE_TEXT %s <- %s" file text)
  (spit (io/file project-dir file) text)
  {})

(def all-resolvers
  [open-file save-text])
