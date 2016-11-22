(ns black-friday.bot.server
  (:gen-class)
  (:require [org.httpkit.server :as http-kit]
            [common.core :as c]
            [clj-http.client :as client]
            [black-friday.bot.handler :as handler]
            [clojure.tools.logging :as log]))

;;
;; HTTP-kit server life-cycle:
;; http://www.http-kit.org/server.html
;;

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (log/info "Stopping web server")
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server [port]
  (stop-server)
  (log/info (str "Starting web server on port " port))
  (reset! server (http-kit/run-server #'handler/app {:port port})))

(defn stop [] (stop-server))

(c/setup-shutdown-hook! stop)

(defn register [port]
  (client/post "http://192.168.50.100:8080/register"
               {:form-params  {:playerName "Roberto"
                               :url (str "http://192.168.50.1:" port "/move")}
                :content-type :json
                :as           :json}))
