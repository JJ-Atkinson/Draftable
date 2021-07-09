(ns dev.fisher.core.comm.client)


(defprotocol CommLink
  (send [this message]
    "Send some EDN payload to Fisher.Core via the link. For some implementations,
     this will be limited by what Transit can serialize.")
  (set-receive! [this receiver-id receiver-fn]
    "Designate the function that receives messages from the server."))


