(ns app.SPA
  (:require [com.fulcrologic.fulcro.networking.http-remote :as net]
            [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.components :as comp]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.rendering.keyframe-render2 :as k2r]
            [com.fulcrologic.fulcro.rendering.multiple-roots-renderer :as mrr]))


(defonce SPA
  (app/fulcro-app
    {:optimized-render! mrr/render!
     :remotes {:remote (net/fulcro-http-remote {:url "/api"})}
     :client-did-mount  (fn [app]
                          (log/merge-config! {:output-fn prefix-output-fn
                                              :appenders {:console (console-appender)}})
                          )}))
