(ns aeroclj.scan
  (:import (com.aerospike.client.policy ScanPolicy Priority)
           (com.aerospike.client AerospikeClient ScanCallback Key Record)))

(defn mk-scanpolicy [& {:keys [:concurrent-nodes :include-bin-data :priority]}]
  (let [sp (ScanPolicy.)]
    (when concurrent-nodes (set! (. sp concurrentNodes) concurrent-nodes))
    (when include-bin-data (set! (. sp includeBinData) include-bin-data))
    (when priority (case priority
                     (:low Priority/LOW)
                     (:default Priority/DEFAULT)
                     (throw (new RuntimeException "unknown priority value!"))))
    sp))

(defn scan-all [^AerospikeClient conn ^ScanPolicy sp
                ^String ns ^String set-name ^ScanCallback sc & bin-names]
  (.scanAll conn sp ns set-name sc (into-array String bin-names))
  )

(defn make-scanner [state-atom func]
  (let [scanner (reify ScanCallback
                  (^void scanCallback [this ^Key k ^Record rec]
                    (func state-atom k rec)
                    nil
                    ))]
    scanner))
