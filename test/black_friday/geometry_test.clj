(ns black-friday.geometry-test
  (require [clojure.test :refer :all]
           [black-friday.geometry :as g]
           [black-friday.bot.handler :as h]))

(def maze1 [[0 0 0 0 0 0 0]
            [0 0 0 1 0 0 0]
            [0 0 0 1 0 0 0]
            [0 0 0 1 0 0 0]
            [0 0 0 0 0 0 0]])

(def maze (h/tiles->map
    ["xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
     "xx________________________________________________________________________________________xx"
     "xx________________________________________________________________________________________xx"
     "xx_______o___________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx___________________________________________xx___________________________________________xx"
     "xx________________________________________________________________________________________xx"
     "xx________________________________________________________________________________________xx"
     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
     "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"]))

(testing "substraction"
  (is (g/minus [2 2] [1 1]) [1 1]))

(testing "paths"
  (is (= (g/search [[0 0] [0 0]] [0 0] [1 1]) [[0 0] [1 0] [1 1]]))
  (is (= (g/search maze1 [0 0] [6 4]) [[0 0] [1 0] [1 1] [2 1] [2 2] [2 3] [2 4] [3 4] [4 4] [5 4] [6 4]]))
  (is (= (g/search maze1 [0 2] [6 2]) [[0 2] [1 2] [2 2] [2 3] [2 4] [3 4] [4 4] [4 3] [5 3] [6 3] [6 2]])))


(testing "real paths"
  (is (= (g/search maze [44 4] [47 4]) [[44 4] [44 3] [45 3] [46 3] [47 3] [47 4]]))
  (is (= (g/search maze [49 4] [44 7]) [[49 4] [48 4] [47 4] [47 3] [46 3] [45 3] [44 3] [44 4] [44 5] [44 6] [44 7]]))
  #_(is (= (g/search maze [81 17] [24 21]) [])))

