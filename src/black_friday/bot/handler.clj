(ns black-friday.bot.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [clojure.pprint :refer [pprint]]
            [black-friday.geometry :as g]
            [black-friday.schema :as sc]
            [black-friday.bot.settings :as settings]
            [black-friday.bot.dos :as dos]
            [common.core :as c]
            [common.predicate :as p]
            [common.string :as xstr]))

(defn position->vec [position]
  [(:x position) (:y position)])

(defn distance [x y]
  (g/manhattan-distance (position->vec (:position x)) (position->vec (:position y))))

(defn can-afford? [player item]
  (>= (:money player) (* (/ (- 100 (:discountPercent item)) 100) (:price item))))

(def descending-sort #(compare %2 %1))

(def ascending-sort compare)

(defn vec-comparator [& comparators]
  (fn [a b]
    (or (c/find-first #(not= % 0) (map eval (map list comparators a b))) 0)))

(defn find-cheapest-item [player items]
  (first (sort-by :discountPercent descending-sort (filter (partial can-afford? player) items))))

(defn find-weapon-target [player players]
  (first (sort-by (partial distance player) descending-sort players)))

(defn tiles->map [tiles]
  (map (fn [row] (map (fn [tile] (if (#{\_ \o} tile ) 0 1)) row)) tiles))

(defn goto-position [tiles player position]
  {:pre [(c/not-nil? tiles)
         (c/not-nil? player)
         (c/not-nil? position)
         (not= position (:position player))]}
  (let [path (g/search (tiles->map tiles) (position->vec (:position player)) (position->vec position))
        direction (vec (g/minus (second path) (first path)))]
    (case direction
      [0 -1] "UP"
      [0 1] "DOWN"
      [-1 0] "LEFT"
      [1 0] "RIGHT")))

(def target (atom nil))

(defn move [gs]
  (let [items (get-in gs [:gameState :items])
        tiles (get-in gs [:gameState :map :tiles])
        player (:playerState gs)
        target-item (find-cheapest-item player items)]

    (dos/update-dos-targets (map :url (get-in gs [:gameState :players])))
    (reset! target target-item)

    (cond
      (nil? target-item) (goto-position tiles player (get-in gs [:gameState :map :exit]))
      (= (:position player) (:position target-item)) "PICK"
      :else (goto-position tiles player (:position target-item)))))

(defn move+log [gs]
  (let [move (time (move gs))]
    (pprint (str (get-in gs [:playerState :position]) " -> " move))
    move))

(defn random-move [gs]
  (first (shuffle ["LEFT" "RIGHT" "UP" "DOWN"])))

(defn find-closest-usable-item [player items not-in-position]
  (first (sort-by (juxt :isUsable (partial distance player) :discountPercent)
                  (vec-comparator descending-sort ascending-sort descending-sort)
                  (filter (p/and* (partial can-afford? player)
                                  (p/not-eq :position not-in-position))
                          items))))

(defn minion-move [gs]
  (let [items (get-in gs [:gameState :items])
        tiles (get-in gs [:gameState :map :tiles])
        player (:playerState gs)
        target-item (find-closest-usable-item player items (:position @target))
        target-player (find-weapon-target player (get-in gs [:gameState :players]))]

    (cond
      (not-empty (:usableItems player))
        (if (xstr/substring? (settings/bot-url) (:url target-player))
          (goto-position tiles player (:position target-player))
          "USE")
      (nil? target-item) (goto-position tiles player (get-in gs [:gameState :map :exit]))
      (= (:position player) (:position target-item)) "PICK"
      :else (goto-position tiles player (:position target-item)))))

(def app
  (api
    {:swagger
     {:ui "/api-docs"
      :spec "/swagger.json"
      :data {:info {:title "Black friday bot API"
                    :description ""}
             :tags [{:name "api", :description "Black friday bot API"}]}}}

    (POST "/move" []
      :return s/Str
      :body [state sc/Game-State-Changed]
      :summary ""
      (ok (move+log state)))

    (POST "/move/random" []
      :return s/Str
      :body [state sc/Game-State-Changed]
      :summary ""
      (ok (random-move state)))

    (POST "/move/minion" []
      :return s/Str
      :body [state sc/Game-State-Changed]
      :summary ""
      (ok (minion-move state)))))

