(ns black-friday.bot.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [org.httpkit.server :as hs]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.logger :as logger]
            [cheshire.core :as json]
            [clojure.pprint :refer [pprint]]
            [black-friday.geometry :as g]
            [black-friday.schema :as sc]
            [common.core :as c]))

(defn position->vec [position]
  [(:x position) (:y position)])

(defn distance-to-item [player item]
  (let [start (:position player)
        end   (:position item)]
    (assoc item :distance (g/manhattan-distance (position->vec start) (position->vec end)))))

(defn can-afford? [player item]
  (>= (:money player) (* (/ (- 100 (:discountPercent item)) 100) (:price item))))

(defn find-closest [player items]
  (let [items-distances (map (partial distance-to-item player) items)]
    (first (filter (partial can-afford? player) (sort-by :distance items-distances)))))

(defn tiles->map [tiles]
  (map (fn [row] (map (fn [tile] (if (#{\_ \o} tile ) 0 1)) row)) tiles))

(defn goto-position [tiles player position]
  {:pre [(c/not-nil? tiles)
         (c/not-nil? player)
         (c/not-nil? position)
         (not= position (:position player))]}
  (let [path (g/search (tiles->map tiles) (position->vec (:position player)) (position->vec position))
        direction (vec (g/minus (second path) (first path)))]
    (pprint path)
    (case direction
      [0 -1] "UP"
      [0 1] "DOWN"
      [-1 0] "LEFT"
      [1 0] "RIGHT")))

(defn make-move [gs]
  (let [items (get-in gs [:gameState :items])
        tiles (get-in gs [:gameState :map :tiles])
        player (:playerState gs)
        closest-item (find-closest player items)]
    (pprint closest-item)
    (pprint player)
    (cond
      (nil? closest-item) (goto-position tiles player (get-in gs [:gameState :map :exit]))
      (= (:position player) (:position closest-item)) "PICK"
      :else (goto-position tiles player (:position closest-item)))))

(defn make-move-random [gs]
  (first (shuffle ["LEFT" "RIGHT" "UP" "DOWN"])))

(defn move [gs]
  (let [move (time (make-move gs))]
    (pprint move)
    move))

(def app
  (api
    {:swagger
     {:ui "/api-docs"
      :spec "/swagger.json"
      :data {:info {:title "Black friday bot API"
                    :description ""}
             :tags [{:name "api", :description "some apis"}]}}}

    (POST "/move" []
          :return s/Str
          :body [state sc/Game-State-Changed]
          :summary ""
          (ok (move state)))))

