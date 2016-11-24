(ns common.predicate
  (require [common.core :as c]))

(defn predicate
  "This function creates a predicate function, which can be used in different query functions e.g. filter:

  (filter (predicate :a = 1) [{:a 1}]) => {:a 1}

  The predicate function is created from
  - a binary predicate (b -> b -> boolean),
  - a getter function (a -> b) and
  - a value (b)
  The created unary predicate (a -> boolean) 'compares' a value of (getter predicate-argument)
  to the given value using the binary predicate function.

  Example:

  (predicate :a > 1) => (fn [argument] (> (:a argument) 1))

  The binary predicate is e.g. = < > <= >= etc and it is a function: b -> b -> boolean.
  Here a and b represents any types. The notation a -> b refers to a function, which domain is a and range is b.

  The getter is a function: a -> b.
  This build function is (b -> b -> boolean) -> (a -> b) -> b -> (a -> boolean).
  "

  [getter binary-predicate value]
  (fn [argument] (binary-predicate (getter argument) value)))

(defn eq
  "A shorthand to create a predicate from an equals binary predicate."
  [getter value] (predicate getter = value))

(defn not-eq
  "A shorthand to create a predicate from an not equals binary predicate."
  [getter value] (predicate getter not= value))

(defn or*
  "This higher order function forms a new predicate function from a set of predicates.
   The resulting predicate g is g(x) = f1(x) or f2(x) or ... or fn(x)."
  [& predicates] (fn [obj] (c/find-first identity (map (fn [f] (f obj)) predicates))))

(defn and*
  "This higher order function forms a new predicate function from a set of predicates.
   The resulting predicate g is g(x) = f1(x) and f2(x) and ... and fn(x)."
  [& predicates] (fn [obj] (every? identity (map (fn [f] (f obj)) predicates))))

(defn not* [predicate] (fn [value] (not (predicate value))))

(defn map->and
  [map]
  (cons `and* (for [[k v] map] `(eq ~k ~v))))

