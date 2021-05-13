(ns app.log-config
  (:require 
    [taoensso.timbre :as log]))

;; default
(log/set-level! :debug)
