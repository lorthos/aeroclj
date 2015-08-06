(ns aeroclj.udf-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.udf :refer :all]
            [aeroclj.core :refer :all]
            [clojure.java.io :as io]
            [aeroclj.env :as e])
  (:import (com.aerospike.client Value)))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! (:host e/props) (:port e/props)))
  (init-once! @conn "test" "demo")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)


(deftest udf-test
  (testing "register"
    (is (nil?
          (register-and-wait! @conn-atom (.getPath (io/resource "readbin.lua")) "readbin.lua")
          ))
    )
  (testing "execute"
    (is (= "value1"
           (do
             (put! @conn "test" "demo" "utest1" {"bin1" "value1"})
             (execute! @conn (mk-key "test" "demo" "test1")
                       "readbin" "read_bin" (Value/get "bin1"))

             )
           ))
    )
  (testing "execute udf with multiple arguments"
    (is (= 25 (do
               (register-and-wait! @conn-atom (.getPath (io/resource "multiadd.lua")) "multiadd.lua")
               (put! @conn "test" "demo" "utest2" {"bin0" 2 })
               (execute! @conn (mk-key "test" "demo" "utest2")
                         "multiadd" "multi_add" (Value/get 10)(Value/get 5))
               )))
    )
  )