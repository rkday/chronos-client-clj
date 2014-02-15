# chronos-client-clj

A Clojure library providing a simle API to the open-source Chronos network timer service.

## Usage

In project.clj: `[chronos-client-clj "0.1.0"]`

In the ns declaration: `(:require [chronos-client-clj.core :as chronos])`

This provides three functions:

* `chronos/set-timer` sets a timer. Its simplest signature is `(set-timer chronos-server http-callback-uri http-callback-body when)`, which will trigger a POST to `http-callback-uri` with a body of `http-callback-body` after `when` seconds. There is a longer form `(set-timer chronos-server http-callback-uri http-callback-body interval repeat-for replication-factor)`, which calls the HTTP callback every `interval` seconds until `repeat-for seconds` have passed. It also allows you to configure how many Chronos nodes in the cluster the timer is replicated amongst (i.e. how resilient to node failure the timer is) by setting `replication-factor` (which defaults to 2).

Both forms return the timer's HTTP path on success (for use in update-timer and delete-timer) and throw an exception on failure.

* `chronos/update-timer` updates a timer - its two function signatures are `(chronos/update-timer chronos-hostname timer-path callback-uri callback-body when)` and `(chronos/update-timer chronos-hostname timer-path callback-uri callback-body interval duration replication-factor)`. Apart from the need to specify the HTTP path to the timer (as returned by set-timer) the parameters are identical to set-timer.

* `chronos/delete-timer` deletes the given timer. Its only function signature is `(chronos/delete-timer chronos-hostname error-path)`.

Both update-timer and delete-timer return a truthy value on success and throw an exception on failure. (Currently, the truthy value is the timer's HTTP path, as with set-timer, but **this is not guaranteed by the API**).

## Examples

```clojure
user> (ns user (:require [chronos-client-clj.core :as chronos]))
nil
user> (def chronos-server "ec2-54-216-61-135.eu-west-1.compute.amazonaws.com")
#'user/chronos-server
;; set a timer to pop in one minute using the simple method
user> (chronos/set-timer chronos-server "http://example.com/callback-uri" "hello world" 60)
"/timers/00555f8ec00000088000000800600500"
;; set a non-fault-tolerant timer to pop every minute for ten minutes
user> (chronos/set-timer chronos-server "http://example.com/callback-uri" "hello world" 60 600 1)
"/timers/00556ad7000000098000000800600500"
user> (def timer-id (chronos/set-timer chronos-server "http://example.com/callback-uri" "hello world" 60))
#'user/timer-id
;; use update-timer to modify any of the timer's properties you like - changing the callback or the pop frequency
user> (chronos/update-timer chronos-server timer-id "http://example.com/callback-uri-2" "hello again" 30 1200 1)
"/timers/00557d6e8000000a8000000800600500" ;; truthy - which is all the API guarantees
user> (chronos/delete-timer chronos-server timer-id)
"/timers/00557d6e8000000a8000000800600500" ;; truthy - which is all the API guarantees
user>
```

## License

Copyright © 2014 Robert Day

Distributed under the 3-clause BSD License.
