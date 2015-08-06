(ns aeroclj.query-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.query :refer :all]
            [aeroclj.core :refer :all]
            [aeroclj.env :as e]
            [aeroclj.udf :as udf]
            [clojure.java.io :as io])
  (:import (com.aerospike.client.lua LuaConfig)))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! (:host e/props) (:port e/props)))
  (init-once! @conn "test" "demo")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)


(deftest query-test
  (testing "create secondary index"
    (put! "qtest1" {"bin1" "value1"})
    (is (nil?
          (create-index! @conn "test" "demo" "si1" "bin1" :string)))
    (is (thrown? RuntimeException (create-index! @conn "test" "demo" "si1" "bin1" :asd)))
    (is (nil? (drop-index! @conn "test" "demo" "si1")))
    )
  (testing "query"
    (put! "qtest1" {"qbin1" "value1x"})
    (put! "qtest2" {"qbin1" "value1x"})
    (put! "qtest3" {"qbin1" "value1x"})
    (create-index! @conn "test" "demo" "si2" "qbin1" :string)
    (is (< 0 (count (query @conn (mk-statement {:ns "test" :set "demo" :index "si2"})))))
    (is (< 0 (count
               (query @conn (mk-statement {:ns "test" :set "demo" :index "si2"} (f-equal "qbin1" "value1x"))))))
    (drop-index! @conn "test" "demo" "si2"))
  (testing "query aggregate"
    (set! (. LuaConfig SourceDirectory) "resources")
    (udf/register-and-wait! @conn-atom (.getPath (io/resource "agg.lua")) "agg.lua")
    (is (< 0 (count (queryAggregate @conn (mk-statement {:ns "test" :set "demo" :index "si2"}) "agg" "count")))))
  )