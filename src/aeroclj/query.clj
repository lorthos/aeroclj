(ns aeroclj.query
  (:import (com.aerospike.client AerospikeClient)
           (com.aerospike.client.query IndexType)
           (com.aerospike.client.task IndexTask))
  (:require [aeroclj.core :as core]))

(defn create-index! [^AerospikeClient conn ^String ns
                     ^String set ^String index-name
                     ^String bin-name index-type]
  (let [^IndexType itype (case index-type
                           :string (IndexType/STRING)
                           :numeric (IndexType/NUMERIC)
                           (throw (new RuntimeException "unknown index type")))
        task ^IndexTask (.createIndex conn core/*wp* ns set index-name bin-name itype)]
    (.waitTillComplete task core/*sleep-interval*)))

(defn drop-index! [^AerospikeClient conn ^String ns
                   ^String set ^String index-name]
  (.dropIndex conn core/*wp* ns set index-name))

