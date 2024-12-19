(ns it.zimpel.komb.core
  (:require
   [clojure.walk :as walk]
   [it.zimpel.komb.io :as io]))

(def sortable?
  (some-fn
    #(every? number? %)
    #(every? string? %)
    #(every? keyword? %)))

(defn sort-json [data {:keys [semantic?]}]
  (walk/prewalk
    (fn [form]
      (cond
        (map? form)
        (into (sorted-map) form)

        ;; excluding
        ;; - subtree MapEntry [:foo <some-children>]
        ;; - heterogeneous values
        (and (sequential? form)
             (sortable? form)
             (not semantic?))
        (into [] (sort form))

        :else form))
    data))

(defn sort-json-str [options input]
  (-> (io/parse-string input)
      (sort-json options)))

(defn process-from-file [options file]
  (sort-json-str options (io/to-str file)))

(defn process-from-input [options]
  (sort-json-str options (io/stdio->str)))
