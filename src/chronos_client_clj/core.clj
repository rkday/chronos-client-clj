(ns chronos-client-clj.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clojurewerkz.urly.core :as urly]))

(defn- canonicalise [url path]
  (let [u (urly/url-like url)]
    (-> u
     (#(if (== -1 (urly/port-of %))
          (.mutatePort % 7253)
          %))
     (.mutatePath path)
     str)))

(defn- make-body [callback-uri callback-body interval duration replication-factor]
  (let [required-params {"timing" {"interval" interval},
                         "callback" {"http" {"uri" callback-uri, "opaque" callback-body,}}}]
    (-> required-params
        (#(if duration
            (assoc-in % ["timing" "repeat-for"] duration)
            %))
        (#(if replication-factor
            (assoc % "reliability" {"replication-factor" replication-factor})
            %))
        json/generate-string)))

(defn- error-handling [{:keys [status headers body error] :as resp}]
  (if error
    (throw error)
    (if (> status 299)
      (Exception. (str "HTTP error " status " received for " (:url resp)))
      (if (:location headers)
        (urly/path-of (urly/url-like (:location headers)))
        true))))

(defn set-timer
  ([chronos-hostname callback-uri callback-body when]
     (set-timer chronos-hostname callback-uri callback-body when when nil))

  ([chronos-hostname callback-uri callback-body interval duration replication-factor]
     (let [path (canonicalise chronos-hostname "/timers/")
           body (make-body callback-uri callback-body interval duration replication-factor)
           resp @(http/post path {:body body})]
       (error-handling resp))))

(defn update-timer
  ([chronos-hostname timer-path callback-uri callback-body when]
     (update-timer timer-path chronos-hostname callback-uri callback-body when when nil))

  ([chronos-hostname timer-path callback-uri callback-body interval duration replication-factor]
     (let [path (canonicalise chronos-hostname timer-path)
           body (make-body callback-uri callback-body interval duration replication-factor)
           resp @(http/put path {:body body})]
       (error-handling resp))))

(defn delete-timer
  ([chronos-hostname timer-path]
     (let [path (canonicalise chronos-hostname timer-path)
           resp @(http/delete path)]
       (error-handling resp))))
