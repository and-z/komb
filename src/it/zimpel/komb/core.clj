(ns it.zimpel.komb.core
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.walk :as walk]))

(set! *warn-on-reflection* true)

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
  (-> (json/parse-string input true)
      (sort-json options)))

(defn process-from-file [options file]
  (sort-json-str options (slurp file)))

(defn process-from-input [options]
  (with-open [r (io/reader *in*)]
    (sort-json-str options (slurp r))))

(defn stringify [json options]
  (json/generate-string json options))
