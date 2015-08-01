(ns aeroclj.udf-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.udf :refer :all]
            [aeroclj.core :refer :all])
  (:import (com.aerospike.client Value)))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! "192.168.99.100" 32771))
  (init-once! @conn "test")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)


(deftest udf-test
  (testing "register"
    (is (nil?
          (register-and-wait! @conn-atom "resources/readbin.lua" "readbin.lua")
          ))
    )
  (testing "execute"
    (is (= 1
           (do
             (put! @conn "test" "demo" "test1" {"bin1" "value1"})
             (execute! @conn (mkkey "test" "demo" "test1")
                       "readbin.lua" "read_bin" (Value/get "bin1"))

             )
           ))
    )
  )