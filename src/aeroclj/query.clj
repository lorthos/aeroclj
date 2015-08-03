(ns aeroclj.query
  (:import (com.aerospike.client AerospikeClient)
           (com.aerospike.client.query IndexType Statement Filter)
           (com.aerospike.client.task IndexTask)
           (com.aerospike.client.policy QueryPolicy))
  (:require [aeroclj.core :as core]))

(def ^:dynamic ^QueryPolicy *qp* (QueryPolicy.))

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

(defn mk-statement [^String ns ^String set-name & filters]
  (let [stmt (doto (Statement.)
               (.setNamespace ns)
               (.setSetName set-name)
               (.setFilters (into-array Filter filters)))]
    stmt
    )
  )


(defn f-equal [name value]
  (Filter/equal name value))

(defn f-range [name begin end]
  (Filter/range name begin end))


(defn query [^AerospikeClient conn ^Statement stmt]
  (with-open [rs (.query conn *qp* stmt)]
    (iterator-seq (.iterator rs))))
