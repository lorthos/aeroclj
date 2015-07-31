(ns aeroclj.core
  "Idiomatic Clojure wrapper around AeroSpike Java client."

  (:import (com.aerospike.client AerospikeClient Key Bin Record)
           (com.aerospike.client.policy WritePolicy ClientPolicy)
           (clojure.lang IPersistentMap))
  (:refer-clojure :exclude [get]))

(def ^:dynamic ^String *aero-host* "192.168.99.100")
(def ^:dynamic ^Integer *aero-port* 32771)
(def ^:dynamic ^WritePolicy *aero-wp* (WritePolicy.))
(def ^:dynamic ^ClientPolicy *aero-cp* (ClientPolicy.))
(def ns-atom (atom "test"))
(def conn-atom (atom nil))


(defn connect!
  ([] (AerospikeClient. *aero-host* *aero-port*))
  ([^String host ^Integer port] (AerospikeClient. host port)))

(defn connect-to-multi!
  ([^ClientPolicy cp #^"[Lcom.aerospike.client.Host;" hosts]
   (AerospikeClient. cp hosts)))

(defn close! [^AerospikeClient conn]
  (.close conn))

(defn mkkey [^String ns ^String set ^String key]
  (Key. ns set key))

(defn ->bin [^IPersistentMap bins]
  (let [bin-seq (map #(Bin. ^String (first %) (second %)) bins)]
    (into-array Bin bin-seq)))

(defn init-once!
  "Iinitalize a global connection and a namespace
  to be used for all future requests
  since namespace is not dynamic, we can maybe
  use a default with a shorter aritry"
  [^AerospikeClient conn ^String ns]
  (reset! ns-atom ns)
  (reset! conn-atom conn))

(defn put!
  ([set key bins]
   (put! @conn-atom @ns-atom set key bins))
  ([^AerospikeClient conn ns set key bins]
   (.put conn *aero-wp* (mkkey ns set key) (->bin bins)))
  )

(defn mk-ttl [^Integer sec]
  (let [wp (WritePolicy.)]
    (set! (. wp expiration) sec)
    wp))

(defn get
  ([set key]
   (get @conn-atom @ns-atom set key))
  ([^AerospikeClient conn ns set key]
   (let [record ^Record (.get conn *aero-wp* (mkkey ns set key))]
     (when record
       (.bins record)))))

(defn mget
  ([#^"[Lcom.aerospike.client.Key;" keys]
   (mget @conn-atom keys))
  ([^AerospikeClient conn keys]
   (let [records (seq (.get conn *aero-wp* keys)) ]
     (when records
       (map #(.bins %) records))))
  )

(defn delete!
  ([set key]
   (delete! @conn-atom @ns-atom set key))
  ([^AerospikeClient conn ns set key]
   (.delete conn *aero-wp* (mkkey ns set key)))
  )
