(ns aeroclj.core-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.core :refer :all]
            [aeroclj.env :as e])
  (:import (com.aerospike.client Bin Key)))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! (:host e/props) (:port e/props)))
  (init-once! @conn "test" "demo")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)

(deftest core-test
  (testing "mkbin"
    (is (= (Bin. "bin1" "value1")
           (mk-bin "bin1" "value1")
           ))
    )
  (testing "put! and get"
    (is (= {"bin1" "value1"}
           (do
             (delete! "test1")
             (put! "test1" {"bin1" "value1"})
             (get "test1")))))
  (testing "write multiple values"
    (is (= {"bin1" "value1" "bin2" "value2"}
           (do
             (delete! "test2")
             (put! "test2" {"bin1" "value1" "bin2" "value2"})
             (get "test2")))))
  (testing "put and get with global vars"
    (is (= {"bin3" "value3"}
           (do
             (put! "test3" {"bin3" "value3"})
             (get "test3")
             )
           ))
    )
  (testing "non-string bin values"
    (is (= {"bin4" 25}
           (do
             (put! "test4" {"bin4" 25})
             (get "test4")
             )
           ))
    )
  (testing "drop bin"
    (is (= {"bin5" "v5"}
           (do
             (put! "t5" {"bin5" "v5" "bin6" "v6"})
             (put! "t5" {"bin5" "v5" "bin6" nil})
             (get "t5")
             )
           ))
    )
  (testing "ttl with write policy"
    (is (nil? (with-bindings {#'*wp* (mk-wp :ttl 1)}
                (do
                  (put! "t7" {"bin7" "v7"})
                  (Thread/sleep 2000)
                  (get "t7"))
                ))))
  (testing "delete key"
    (is (nil? (do
                (put! "t8" {"bin8" "v8"})
                (delete! "t8")
                (get "t8")
                )))
    )
  (testing "multi-get"
    (is (= [{"bin1" "value1"} {"bin1" "value1" "bin2" "value2"}]
           (let [keys [(mk-key "test" "demo" "test1")
                       (mk-key "test" "demo" "test2")]
                 akeys (into-array Key keys)]
             (mget akeys))
           ))
    )
  (testing "make operation"
    (is (not (nil? (mk-op :get (mk-bin "name" 1)))))
    )
  (testing "operate"
    (is (= {"b1" 2}
           (do
             (put! "t9" {"b1" 1 "b2" 2})
             (operate! @conn-atom @ns-atom @set-atom "t9" :add "b1" 1 :put "b2" nil)
             (get "t9")
             )))
    )

  )




