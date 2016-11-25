(defproject black-friday-bot "0.1.0-SNAPSHOT"
            :description "Black friday bot"
            :dependencies [[org.clojure/clojure "1.8.0"]
                           [metosin/compojure-api "1.1.9"]
                           [metosin/ring-swagger-ui "2.2.5-0"]
                           [clj-http "3.4.1"]
                           [ring.middleware.logger "0.5.0"]
                           [cheshire "5.6.3"]
                           [http-kit "2.2.0"]
                           [org.clojure/tools.logging "0.3.1"]]
            :main black-friday.bot.server
            :ring {:handler black-friday.bot.handler/app
                   :port 3002}
            :uberjar-name "black-friday-bot.jar"
            :profiles {:uberjar {:resource-paths ["swagger-ui"] :aot :all}
                       :dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                             :plugins [[lein-ring "0.9.0"]
                                       [lein-ancient "0.6.10"]]}})
