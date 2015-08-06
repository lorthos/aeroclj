(ns aeroclj.scan-test
  (:refer-clojure :exclude [get])
  (:require [clojure.test :refer :all]
            [aeroclj.scan :refer :all]
            [aeroclj.core :refer :all]
            [clojure.java.io :as io]
            [aeroclj.env :as e]))

(def conn (atom nil))

(defn aero-fixture [f]
  (reset! conn (connect! (:host e/props) (:port e/props)))
  (init-once! @conn "test" "demo")
  (f)
  (close! @conn)
  (reset! conn nil))

(use-fixtures :once aero-fixture)


(deftest scan-policy-test
  (testing "make policy"
    (is (not (nil? (mk-scanpolicy :concurrent-nodes true :include-bin-data true :priority :low))))
    )
  )


(def counting-scan-fun (fn [at k rec]
                         (if @at
                           (swap! at inc)
                           (reset! at 1))))
(def all-counts (atom 0))

(deftest scanner-test
  (testing "make scanner"
    (is (not (nil? (make-scanner (atom nil) counting-scan-fun))))
    (is (< 1 (do
               (scan-all @conn (mk-scanpolicy) @ns-atom @set-atom (make-scanner all-counts counting-scan-fun))
               @all-counts
               )))
    )
  )
