(defproject aeroclj "0.1.1-SNAPSHOT"
  :description "Idiomatic Clojure wrapper around AeroSpike Java client."
  :url "https://github.com/codemomentum/aeroclj"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.aerospike/aerospike-client "3.1.3"]]
  :min-lein-version "2.4.3"
  ;release
  :scm {:name "git"
        :url "https://github.com/codemomentum/aeroclj"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :signing {:gpg-key "218C6198"}
  ;; This directive (and the {:creds :gpg} section) tells
  ;; Leiningen how to find your Clojars account credentials.
  ;; You set those up already, right?
  :deploy-repositories [["clojars" {:creds :gpg}]]
  )
