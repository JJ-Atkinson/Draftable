(ns server.pathom-wrappers
  (:require
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.algorithms.do-not-use :as futil]
    [com.wsscode.pathom.connect :as pc]
    [taoensso.timbre :as log]))

(defonce pathom-registry (atom {}))

(defn register! [resolver]
  (log/debug "Registering resolver" (::pc/sym resolver))
  (swap! pathom-registry assoc (::pc/sym resolver) resolver))

(s/def ::mutation-args (s/cat
                         :sym simple-symbol?
                         :doc (s/? string?)
                         :arglist vector?
                         :config map?
                         :body (s/* any?)))

(defn defpathom-backend-endpoint* [endpoint args update-database?]
  (let [{:keys [sym arglist doc config body]} (futil/conform! ::mutation-args args)
        internal-fn-sym (symbol (str (name sym) "__internal-fn__"))
        fqsym           (if (namespace sym)
                          sym
                          (symbol (name (ns-name *ns*)) (name sym)))
        env-arg         (first arglist)
        params-arg      (second arglist)]
    `(do
       ;; Use this internal function so we can dynamically update a resolver in
       ;; dev without having to restart the whole pathom parser.
       (defn ~internal-fn-sym [env# params#]
         (let [~env-arg env#
               ~params-arg params#
               result# (do ~@body)]
           ;; Pathom doesn't expect nils
           (cond
             (sequential? result#) (vec (remove nil? result#))
             (nil? result#) {}
             :else result#)))
       (~endpoint ~(cond-> sym
                     doc (with-meta {:doc doc})) [env# params#]
         ~config
         (~internal-fn-sym env# params#))
       (register! ~sym)
       ::done)))

(defmacro
  ^{:doc      "Defines a server-side PATHOM mutation.

               Example:

               (defmutation do-thing
                 \"Optional docstring\"
                 [env params]
                 {::pc/input [:param/name]  ; PATHOM config (optional)
                  ::pc/output [:result/prop]}
                 ...)  ; actual action (required)"
    :arglists '([sym docstring? arglist config & body])} defmutation
  [& args]
  (defpathom-backend-endpoint* `pc/defmutation args true))

(defmacro ^{:doc      "Defines a pathom resolver but with authorization.

                        Example:

                        (defresolver resolver-name [env input]
                          {::pc/input [:customer/id]
                           ...}
                          {:customer/name \"Bob\"})
                        "
            :arglists '([sym docstring? arglist config & body])} defresolver
  [& args]
  (defpathom-backend-endpoint* `pc/defresolver args false))
