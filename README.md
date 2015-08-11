# aeroclj

Idiomatic Clojure wrapper around AeroSpike Java client.

##Artifacts


With Leiningen:

        [aeroclj "0.1.0"]

With Maven:

        <dependency>
          <groupId>aeroclj</groupId>
          <artifactId>aeroclj</artifactId>
          <version>0.1.0</version>
        </dependency>


## Usage

    (connect! "192.168.99.100" 32771)
    (put! "demo" "test1" {"bin1" "value1"})
    (get "demo" "test1")


## Api

###Connection
* connect (connect!) (connect-to-multi!)

###Key Value Store
* write (put!)
* read (get)
* batch read (mget)
* delete (delete!)
* operate (operate!)

###UDF
* register (register-and-wait!)
* execute (execute!)

###Query
* manage indexes (create-index!) (drop-index!)
* query (query)

###Aggregation
* query aggregate (queryAggregate)

###Scan
* scan all (scan-all) (mk-scanner)


###TODO
* Large data type
* logging
* async client
* doc


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
