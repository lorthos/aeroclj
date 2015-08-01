(ns aeroclj.udf
  (:refer-clojure :exclude [get])
  (:import (com.aerospike.client Language AerospikeClient Key Value)
           (com.aerospike.client.task Task))
  (:require [aeroclj.core :as core]))

(defn register-and-wait! [^AerospikeClient conn
                          ^String client-path ^String server-path]
  (let [task ^Task (.register conn core/*wp* client-path server-path Language/LUA)]
    (.waitTillComplete task core/*sleep-interval*)))

(defn execute! [^AerospikeClient conn
                ^Key key ^String package-name ^String function-name
                & args]
  (.execute conn core/*wp* key package-name function-name (into-array Value args))
  )