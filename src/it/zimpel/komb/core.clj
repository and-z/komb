(ns it.zimpel.komb.core
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.walk :as walk])
  (:import
   (java.io File)))

(set! *warn-on-reflection* true)

(def sortable?
  (some-fn
   #(every? number? %)
   #(every? string? %)
   #(every? keyword? %)))

(defn sort-json [data]
  (walk/prewalk
   (fn [form]
     (cond
       (map? form)
       (into (sorted-map) form)

       ;; excluding
       ;; - subtree MapEntry [:foo <some-children>]
       ;; - heterogeneous values
       (and (sequential? form)
            (sortable? form))
       (into [] (sort form))

       :else form))
   data))

(defn sort-json-str [input]
  (-> (json/parse-string input true)
      (sort-json)))

(defn get-json-file [path]
  (let [^File file (io/file path)
        ->error (fn [msg] {:error {:message msg :path path}})]
    (cond
      (not (.exists file))
      (->error "File not exists")

      (not (.isFile file))
      (->error "Not a file")

      (not (str/ends-with? (.getName file) ".json"))
      (->error "Not a JSON file")

      (not (.canRead file))
      (->error "File not readable")

      :else {:file file})))

(defn process-from-file [path]
  (let [{:keys [error file] :as result} (get-json-file path)]
    (if (some? error)
      result
      {:data (sort-json-str (slurp file))})))

(defn process-from-input []
  (with-open [r (io/reader *in*)]
    {:data (sort-json-str (slurp r))}))
