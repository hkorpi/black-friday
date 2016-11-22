(ns black-friday.bot.handler-test
  (require [clojure.test :refer :all]
           [black-friday.bot.handler :as h]
           [clojure.string :as str]))

(defmacro test-floor-tiles [n m]
  `(is (= (h/tiles->map ~(vec (repeat m (str/join "" (repeat n "_")))))
          ~(vec (repeat m (vec (repeat n 0)))))))

(testing "Tiles without any walls"
  (test-floor-tiles 1 1)
  (test-floor-tiles 2 2)
  (test-floor-tiles 3 3)
  (test-floor-tiles 4 4)
  (test-floor-tiles 2 1)
  (test-floor-tiles 1 2))