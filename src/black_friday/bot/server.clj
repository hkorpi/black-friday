(ns black-friday.bot.server
  (:gen-class)
  (:require [org.httpkit.server :as http-kit]
            [common.core :as c]
            [clj-http.client :as client]
            [black-friday.bot.handler :as handler]
            [clojure.tools.logging :as log]
            [black-friday.bot.settings :as s]))

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

(defn register [move-path player-name]
  (client/post (str "http://" (:server s/settings) "/register")
               {:form-params  {:playerName player-name
                               :url (str "http://" (s/bot-url) move-path)}
                :content-type :json
                :as           :json}))

(defn start []
  (let [response (register "/move" "Roberto")]
    (when (s/minions?)
      (doseq [i (range 1 (inc (-> response :body :gameState :map :maxItemCount)))]
        (register "/move/minion" (str "Minion-" i))))))
