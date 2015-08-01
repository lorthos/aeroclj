(ns aeroclj.query-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.query :refer :all]
            [aeroclj.core :refer :all]))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! "192.168.99.100" 32771))
  (init-once! @conn "test")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)


(deftest query-test
  (testing "create secondary index"
    (put! @conn "test" "demo" "test1" {"bin1" "value1"})
    (is (nil?
          (create-index! @conn "test" "demo" "si1" "bin1" :string)))
    (is (thrown? RuntimeException (create-index! @conn "test" "demo" "si1" "bin1" :asd)))
    (is (nil? (drop-index! @conn "test" "demo" "si1")))
    )

  )