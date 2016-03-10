(ns aeroclj.core
  "Idiomatic Clojure wrapper around AeroSpike Java client."

  (:import (com.aerospike.client AerospikeClient Key Bin Record Operation)
           (com.aerospike.client.policy WritePolicy ClientPolicy GenerationPolicy RecordExistsAction CommitLevel Policy BatchPolicy)
           (clojure.lang IPersistentMap))
  (:refer-clojure :exclude [get]))

(def ^:dynamic ^String *aero-host* "192.168.99.100")
(def ^:dynamic ^Integer *aero-port* 32771)
(def ^:dynamic ^WritePolicy *wp* (WritePolicy.))
(def ^:dynamic ^Policy *rp* (Policy.))
(def ^:dynamic ^BatchPolicy *bp* (BatchPolicy.))
(def ^:dynamic ^Integer *sleep-interval* 1000)

(def ns-atom (atom "test"))
(def set-atom (atom "demo"))
(def conn-atom (atom nil))


(defn connect!
  ([] (AerospikeClient. *aero-host* *aero-port*))
  ([^String host ^Integer port] (AerospikeClient. host port)))

(defn connect-to-multi!
  ([^ClientPolicy cp #^"[Lcom.aerospike.client.Host;" hosts]
   (AerospikeClient. cp hosts)))

(defn close! [^AerospikeClient conn]
  (.close conn))

(defn mk-key [^String ns ^String set ^String key]
  (Key. ns set key))

(defn mk-bin [^String name value]
  (Bin. ^String name value))

(defn ->bin [^IPersistentMap bins]
  (let [bin-seq (map #(mk-bin (first %) (second %)) bins)]
    (into-array Bin bin-seq)))

(defn init-once!
  "Iinitalize a global connection and a namespace
  to be used for all future requests
  since namespace is not dynamic, we can maybe
  use a default with a shorter aritry"
  [^AerospikeClient conn ^String ns ^String set-name]
  (reset! ns-atom ns)
  (reset! set-atom set-name)
  (reset! conn-atom conn))

(defn put!
  ([key bins]
   (put! @conn-atom @ns-atom @set-atom key bins))
  ([^AerospikeClient conn ns set key bins]
   (.put conn *wp* (mk-key ns set key) (->bin bins)))
  )

(defn mk-wp [& {:keys [:ttl :gen :gen-policy :commit-level :record-exists-action]}]
  (let [wp (WritePolicy.)]
    (when ttl (set! (. wp expiration) ttl))
    (when gen (set! (. wp generation) gen))
    (when gen-policy (set! (. wp generationPolicy)
                           (case gen-policy
                             (:none GenerationPolicy/NONE)
                             (:eq GenerationPolicy/EXPECT_GEN_EQUAL)
                             (:gt GenerationPolicy/EXPECT_GEN_GT)
                             (throw (new RuntimeException "unknown GenerationPolicy value!")))))
    (when record-exists-action (set! (. wp recordExistsAction)
                                     (case record-exists-action
                                       (:create-only RecordExistsAction/CREATE_ONLY)
                                       (:replace RecordExistsAction/REPLACE)
                                       (:replace-only RecordExistsAction/REPLACE_ONLY)
                                       (:update RecordExistsAction/UPDATE)
                                       (:update-only RecordExistsAction/UPDATE_ONLY)
                                       (throw (new RuntimeException "unknown RecordExistsAction value!")))))
    (when commit-level (set! (. wp commitLevel)
                           (case commit-level
                             (:all CommitLevel/COMMIT_ALL)
                             (:master CommitLevel/COMMIT_MASTER)
                             (throw (new RuntimeException "unknown CommitLevel value!")))))


    wp))

(defn get
  ([key]
   (get @conn-atom @ns-atom @set-atom key))
  ([^AerospikeClient conn ns set key]
   (let [record ^Record (.get conn *rp* (mk-key ns set key))]
     (when record
       (.bins record)))))

(defn mget
  ([#^"[Lcom.aerospike.client.Key;" keys]
   (mget @conn-atom keys))
  ([^AerospikeClient conn keys]
   (let [records (seq (.get conn *bp* keys))]
     (when records
       (map #(.bins %) records))))
  )

(defn delete!
  ([key]
   (delete! @conn-atom @ns-atom @set-atom key))
  ([^AerospikeClient conn ns set key]
   (.delete conn *wp* (mk-key ns set key)))
  )

(defn mk-op
  "create the operation based on the type and bin
  [:get bin1 :put bin2 :delete bin3]
  "
  [op-type bin]
  (case op-type
    :get (Operation/get)
    :put (Operation/put bin)
    :add (Operation/add bin)
    (throw (new RuntimeException "unknown op type")))
  )

(defn ->ops [params]
  (let [op-parts (partition 3 (seq params))]
    (map #(mk-op (first %) (mk-bin (first (rest %)) (second (rest %)))) op-parts)))


(defn operate!
  "multiple operations on a single key
  ops syntax:
  :add \"b1\" 1 :delete \"b2\" nil


  "
  ([^AerospikeClient conn ns set key & ops]
   (.operate conn *wp* (mk-key ns set key) (into-array Operation (->ops ops))))
  )
