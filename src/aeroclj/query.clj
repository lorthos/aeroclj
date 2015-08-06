(ns aeroclj.query
  (:import (com.aerospike.client AerospikeClient Value)
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

(defn mk-statement [{ns :ns set-name :set index :index bins :bins} & filters]
  (let [stmt (Statement.)]
    (when ns (.setNamespace stmt ns))
    (when set-name (.setSetName stmt set-name))
    (when index (.setIndexName stmt index))
    (when bins (.setBinNames stmt bins))
    (when filters (.setFilters stmt (into-array Filter filters)))
    stmt)
  )


(defn f-equal [name value]
  (Filter/equal name value))

(defn f-range [name begin end]
  (Filter/range name begin end))


;TODO doall?
(defn query [^AerospikeClient conn ^Statement stmt]
  (with-open [rs (.query conn *qp* stmt)]
    (let [iseq (doall (iterator-seq (.iterator rs)))]
      iseq
      )))

(defn queryAggregate [^AerospikeClient conn ^Statement stmt ^String package-name
                      ^String function-name & values]
  (with-open [rs (.queryAggregate conn *qp* stmt package-name function-name (into-array Value values))]
    (let [iseq (doall (iterator-seq (.iterator rs)))]
      iseq
      )))