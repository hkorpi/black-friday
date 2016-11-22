(ns black-friday.geometry
  (:require [clojure.data.priority-map :as pm]
            [common.core :as c]))

(defn minus [x y] (map - x y))

(defn manhattan-distance [[x1 y1] [x2 y2]]
  (+ (Math/abs ^Integer (- x2 x1)) (Math/abs ^Integer (- y2 y1))))

(defn edges [map width height closed [x y]]
  (for [tx (range (- x 1) (+ x 2))
        ty (range (- y 1) (+ y 2))
        :when (and (>= tx 0)
                   (>= ty 0)
                   (<= tx width)
                   (<= ty height)
                   (not= [x y] [tx ty])
                   (not= (nth (nth map ty) tx) 1)
                   (not (contains? closed [tx ty]))
                   (some #{0} (minus [tx ty] [x y])))]
    [tx ty]))

(defn path [current came-from]
  (reverse
    (loop [path []
           node current]
      (if (nil? node)
        path
        (recur (conj path node) (came-from node))))))

(defn- search-a*
  ([map width height open closed came-from score end]
   (if-let [[current _] (peek open)]
     (if-not (= current end)
       (let [closed (conj closed current)
             neighbours (edges map width height closed current)
             [_ g-score] (score current)
             [open came-from score]
             (reduce
               (fn [[open came-from score] neighbour]
                 (let [tentative-g-score (+ g-score (manhattan-distance current neighbour))
                       neighbour-score (or (score neighbour) [Long/MAX_VALUE Long/MAX_VALUE])]
                   (if (< tentative-g-score (second neighbour-score))
                     (let [neighbour-f-score (+ tentative-g-score (manhattan-distance neighbour end))]
                       [(assoc open neighbour neighbour-f-score)
                        (assoc came-from neighbour current)
                        (assoc score neighbour [neighbour-f-score tentative-g-score])])
                     (let [open+ (if (not (contains? open neighbour))
                                   (assoc open neighbour (first neighbour-score))
                                   open)]
                       [open+ came-from score]))))
               [(pop open) came-from score] neighbours)]
         (recur map width height open closed came-from score end))
       (path current came-from)))))

(defn search
  [map start end]
  {:pre [(c/not-nil? map)
         (vector? start)
         (vector? end)]}
  (let [[sx sy] start
        [ex ey] end
        f-score (manhattan-distance start end)
        open (pm/priority-map start f-score)
        closed #{}
        came-from {}
        score {start [f-score 0]}
        width (-> map first count dec)
        height (-> map count dec)]
    (when (and (not= (nth (nth map sy) sx) 1)
               (not= (nth (nth map ey) ex) 1))
      (search-a* map width height open closed came-from score end))))