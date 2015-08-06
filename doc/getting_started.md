# Getting Started with aeroclj

##Create a connection

Connect function in core will return an AerospikeClient instance which should be reused

    (def conn (atom nil))
    (reset! conn (connect! "localhost" 3000)


If you are working against a single namespace and set, you can set those as defaults for convenience

    (init-once! @conn "test" "demo")


##Basic Operations

Crud operations:

    (put! "test1" {"bin1" "value1"})
    (get "test1")
    (delete! "test1")

Use a different write policy with ttl:

    (with-bindings {#'*wp* (mk-wp :ttl 1)}
                      (put! "key1" {"bin1" "value1"})
                    )

##More Examples

Check the tests