(ns aeroclj.core-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.core :refer :all])
  (:import (com.aerospike.client Bin Key)
           (com.aerospike.client.policy WritePolicy)))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! "192.168.99.100" 32771))
  (init-once! @conn "test")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)

(deftest create-entry
  (testing "mkbin"
    (is (= (Bin. "bin1" "value1")
           (first (mkbin {"bin1" "value1"}))
           ))
    )
  (testing " put! and get"
    (is (= {"bin1" "value1"}
           (do
             (put! @conn "test" "demo" "test1" {"bin1" "value1"})
             (get @conn "test" "demo" "test1")))))
  (testing "write multiple values"
    (is (= {"bin1" "value1" "bin2" "value2"}
           (do
             (put! @conn "test" "demo" "test2" {"bin1" "value1" "bin2" "value2"})
             (get @conn "test" "demo" "test2")))))
  (testing "put and get with global vars"
    (is (= {"bin3" "value3"}
           (do
             (put! "demo" "test3" {"bin3" "value3"})
             (get "demo" "test3")
             )
           ))
    )
  (testing "non-string bin values"
    (is (= {"bin4" 25}
           (do
             (put! "demo" "test4" {"bin4" 25})
             (get "demo" "test4")
             )
           ))
    )
  (testing "drop bin"
    (is (= {"bin5" "v5"}
           (do
             (put! "demo" "t5" {"bin5" "v5" "bin6" "v6"})
             (put! "demo" "t5" {"bin5" "v5" "bin6" nil})
             (get "demo" "t5")
             )
           ))
    )
  (testing "ttl with write policy"
    (is (nil? (with-bindings {#'*aero-wp* (mk-ttl 1)}
                (do
                  (put! "demo" "t7" {"bin7" "v7"})
                  (Thread/sleep 2000)
                  (get "demo" "t7"))
                ))))
  (testing "delete key"
    (is (nil? (do
                (put! "demo" "t8" {"bin8" "v8"})
                (delete! "demo" "t8")
                (get "demo" "t8")
                )))
    )
  (testing "multi-get"
    (is (= [{"bin1" "value1"} {"bin1" "value1" "bin2" "value2"}]
           (let [keys [(mkkey "test" "demo" "test1")
                       (mkkey "test" "demo" "test2")]
                 akeys (into-array Key keys)]
             (mget akeys))
           ))
    )

  )




