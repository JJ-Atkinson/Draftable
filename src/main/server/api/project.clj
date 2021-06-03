(ns server.api.project
  (:require
    [clj-kondo.core :as kondo]
    [com.wsscode.pathom.connect :as pc]
    [server.pathom-wrappers :refer [defresolver]]
    [taoensso.encore :as enc]))

(comment
  (-> {:lint ["src"] :config {:output {:analysis true}}}
    (kondo/run!)))

(defn all-project-ns []
  (->> {:lint ["src"] :config {:output {:analysis true}}}
    (kondo/run!)
    (:analysis)
    (:namespace-definitions)
    (mapv #(do {:name (str (:name %))
                :file (:filename %)}))
    (group-by :name)
    (enc/map-vals first)))

(defresolver project-namespaces [env _]
  {::pc/output [:project-namespaces]}
  (let [nss (all-project-ns)]
    {:project-namespaces nss}))

(def all-resolvers
  [project-namespaces])
