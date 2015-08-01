# aeroclj

Idiomatic Clojure wrapper around AeroSpike Java client.

## Usage

    (connect! "192.168.99.100" 32771)
    (put! "demo" "test1" {"bin1" "value1"})
    (get "demo" "test1")


### Implemented
Only Key value store operations are covered so far
####Connection
* connect (connect!) (connect-to-multi!)
####Key Value Store
* write (put!)
* read (get)
* batch read (mget)
* delete (delete!)
* operate (operate!)


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
