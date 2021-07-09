(ns dev.fisher.core.comm.client-ws-transit
  (:require
    [dev.fisher.core.comm.client :as client]
    [com.wsscode.transito :as transito]
    [manifold.stream :as stream]
    [clojure.core.async :as asy :refer [go >! >!! <! <!! chan]]))

(deftype CommLinkWS [ws-conn receive-atom]
  client/CommLink
  (send [this message]
    (stream/put! ws-conn (transito/write-str )))
  (set-receive! [this receiver-fn]
    (reset! receive-atom (comp receiver-fn transito/read-str))))

(defn new-comm-link-stream [])


(comment
  
  (let [c1 (chan 8)
        c2 (chan 3)
        lut {(hash c1) "c1"
             (hash c2) "c2"}
        ports [c1 c2]]
    (go
      (let [[msg port] (asy/alts! ports)]
        (println
          [msg
           (get lut (hash port))])))
    (go 
      (<! (asy/timeout 100))
      (>! c1 1))
    (go 
      (<! (asy/timeout 80))
      (>! c2 2))))


;; One off
;; [message id resp-chan]
;; many 
;; [message-chan id resp-chan]

(>! port-adder [message id (chan)])
(<! chan)

(let [fwd-port (chan)]
  (loop
    (>! port-adder [fwd-port id resp-chan])
    (<! resp-chan))
  fwd-port)


(let [pid1-chan> (chan)
      pid2-chan> (chan)
      pid1-chan< (reg #_id #_1 )])



[{:pid 1 ...}
 {:pid 2 ...}]


(defn reflector []
  (let [port-adder (chan)]
    (asy/go-loop [lut {#_ (hash message-chan) #_ {:mark id :resp-chan chan}
                       #_id #_{:resp-chan chan}}
                  chan-set #{}]
      (let [[msg chan] (asy/alts! (conj port-adder))]
        (if (= port-adder chan)
          (let []))))
    port-adder))