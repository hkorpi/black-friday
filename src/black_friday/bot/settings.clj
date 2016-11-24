(ns black-friday.bot.settings
  (:require [common.string :as xstr]))

(def settings {
 :bot {
       :host (or (System/getProperty "bot.host") "192.168.50.1")
       :port (Long/parseLong (or (System/getProperty "bot.port") "8080"))
       :mode (or (System/getProperty "bot.mode") "gray")}
 :server (or (System/getProperty "server") "192.168.50.100:8080")})

(defn bot-url []
  (str (-> settings :bot :host) ":" (-> settings :bot :port)))

(defn dos-attact-active? []
  (or (xstr/substring? "evil" (-> settings :bot :mode))
      (xstr/substring? "dos" (-> settings :bot :mode))))

(defn minions? []
  (or (xstr/substring? "gray" (-> settings :bot :mode))
      (xstr/substring? "evil" (-> settings :bot :mode))
      (xstr/substring? "minions" (-> settings :bot :mode))))