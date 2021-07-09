(ns dev.fisher.core.comm.server
  (:require
    [clojure.core.async :as a]
    [aleph.http :as http]
    [ring.middleware.params :as params]
    [manifold.stream :as stream]
    [manifold.deferred :as defer]
    [manifold.bus :as bus]
    [manifold.deferred :as d]
    [com.wsscode.transito :as transito])
  (:import
    (java.io Closeable)))


(def non-websocket-request
  {:status  400
   :headers {"content-type" "application/text"}
   :body    "Expected a websocket request."})

(defn handler [req]
  (tap> [:req req])
  (-> (http/websocket-connection req)
    (defer/chain (fn [socket]
                   (tap> socket)
                   (stream/connect socket socket)))
    (defer/catch
      (fn [_]
        non-websocket-request))))

(def http-server-handler
  (params/wrap-params handler))

(def server (http/start-server http-server-handler {:port 10000}))
(comment (.close server))


(comment
  (let [conn @(http/websocket-client "ws://localhost:10000")]
    (stream/put-all! conn (->> 10 range (map str)))
    (->> conn
      (stream/transform (take 10))
      stream/stream->seq
      doall))
  (let [conn (stream/stream)]
    (stream/put-all! conn (->> 10 range (map str)))
    (->> conn
      (stream/transform (take 10))
      stream/stream->seq
      doall))
  (stream/splice))

(def cconn @(http/websocket-client "ws://localhost:10000"))

(comment
  (d/on-realized (stream/take! cconn)
    (fn [v] (println "got " v))
    (constantly nil))
  (stream/->source)
  (stream/consume (fn [x] (println "xxx" (transito/read-str x))) cconn)
  (stream/put! cconn (transito/write-str {(rand-nth [:a :b :c]) (rand-int 6)})))

(def server-demux )
(def server-s (stream/stream))
(def client-1 (stream/stream))
(def client-2 (stream/stream))

(comment


  (stream/consume (fn [x] (println "client1" x)) client-1)
  (stream/consume (fn [x] (println "client2" x)) client-2)

  
  )