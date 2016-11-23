(ns common.core
  (:require [slingshot.slingshot :as ss]
            [clojure.java.io :as io])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)))

(defn find-first [predicate collection]
  (first (filter predicate collection)))

(defn third [collection] (fnext (next collection)))

(defn is-divisible-by [num divisor]
  (zero? (mod num divisor)))

(defn maybe-nil [f default maybe-nil]
  (if (nil? maybe-nil) default (f maybe-nil)))

(defn last-by-index [vec] (nth vec (dec (count vec))))

(defmacro if-let* [bindings expr else]
  (if (seq bindings)
    `(if-let [~(first bindings) ~(second bindings)]
       (if-let* ~(drop 2 bindings) ~expr ~else) ~else)
    expr))

(def not-nil? (comp not nil?))

(defn partial-first-arg [f & rest-args] (fn [first-arg] (apply f first-arg rest-args)))

(defn nil-safe [f]
  (fn ([x] (if x (f x)))
      ([x & next] (if (and x (every? not-nil? next)) (apply f x next)))))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [input]
  (with-open [^ByteArrayOutputStream out (ByteArrayOutputStream.)]
    (io/copy (io/input-stream input) out)
    (.toByteArray out)))

(defn setup-shutdown-hook! [f]
  (.addShutdownHook (Runtime/getRuntime) (Thread. f)))

(defmacro bindings->map [& bindings]
    (into {} (map (fn [s] [(keyword (name s)) s]) bindings)))



