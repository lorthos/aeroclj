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
  (testing "query"
    (put! @conn "test" "demo" "qtest1" {"bin1" "value1"})
    (put! @conn "test" "demo" "qtest2" {"bin1" "value1"})
    (put! @conn "test" "demo" "qtest3" {"bin1" "value1"})
    (is (= 1 (query @conn (mk-statement "test" "demo"))))
    (is (= 1 (count
               (query @conn (mk-statement "test" "demo" (f-equal "bin1" "value1"))))))

    )

  )