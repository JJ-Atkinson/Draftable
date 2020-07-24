(ns app.SPA
  (:require [com.fulcrologic.fulcro.networking.http-remote :as net]
            [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
            [com.fulcrologic.fulcro.networking.file-upload :as file-upload]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [jra.ui.pages :as pages]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.rendering.keyframe-render2 :as k2r]
            ))


(defonce SPA
  (app/fulcro-app
    {:optimized-render!    k2r/render!
     #_#_:remotes              {:remote (net/fulcro-http-remote {:url                "/api/datomic"
                                                             :request-middleware secured-request-middleware})}
     :client-did-mount     (fn [app]
                             (log/merge-config! {:output-fn prefix-output-fn
                                                 :appenders {:console (console-appender)}})
                             )}))