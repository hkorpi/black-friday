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

(def settings {
   :bot {
      :host (or (System/getProperty "bot.host") "192.168.50.1")
      :port (Long/parseLong (or (System/getProperty "bot.port") "8080"))}
   :server (or (System/getProperty "server") "192.168.50.100:8080")})


(defn register [move-path player-name]
  (client/post (str "http://" (:server settings) "/register")
               {:form-params  {:playerName player-name
                               :url (str "http://" (get-in settings [:bot :host]) ":"
                                         (get-in settings [:bot :port]) move-path)}
                :content-type :json
                :as           :json}))

(defn start []
  (let [response (register "/move" "Roberto")]
    (doseq [i (range 1 (inc (-> response :body :gameState :map :maxItemCount)))]
      (register "/move/minion" (str "Minion-" i))))
  (register "/move/random" "Walker"))
