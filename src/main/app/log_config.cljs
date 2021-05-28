(ns app.log-config
  (:require
    [com.fulcrologic.fulcro.algorithms.timbre-support :as ts]
    [cljs.spec.alpha :as spec]
    [taoensso.timbre :as log]
    [clojure.string :as str]))

(spec/check-asserts true)
(js/console.log "Turning logging to :debug (in app.development-preload)")
(log/set-level! :debug)
(log/merge-config! {:output-fn ts/prefix-output-fn
                    :appenders {:console (ts/console-appender)}})