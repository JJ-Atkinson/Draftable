(ns server.api.project
  (:require
    [clj-kondo.core :as kondo]
    [com.wsscode.pathom.connect :as pc]
    [server.pathom-wrappers :refer [defresolver]]
    [taoensso.encore :as enc]))

(comment
  (letfn [(index [f coll]
            (reduce (fn [m v] (assoc m (f v) v)) {} coll))
          (index-in [f coll]
            (reduce (fn [m v] (assoc-in m (f v) v)) {} coll))]
    (let [{:as analysis :keys [var-definitions var-usages]}
          (-> {:lint ["src/main/server"]
               :config {:output {:analysis true}}}
            (kondo/run!)
            :analysis)
          defs (map #(select-keys % [:filename :ns :name]) var-definitions)
          indexed-defs (index-in (juxt :ns :name) defs)]
      (reduce (fn [idefs {:as usage :keys [from from-var]}]
                (cond-> idefs
                  (and from from-var)
                  (update-in [from from-var :usages]
                    (fnil conj [])
                    (select-keys usage
                      [:name :to]))))
        indexed-defs
        (remove (comp #{'clojure.core} :to) var-usages))
      ))
  )

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
