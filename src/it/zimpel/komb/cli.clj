(ns it.zimpel.komb.cli
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.cli :as cli])
  (:import (java.io File)))

(set! *warn-on-reflection* true)

(def cli-spec
  [[nil "--[no-]semantic" "Enable to preserve order of array elements"
    :id :semantic?
    :default true]
   [nil "--[no-]pretty" "Pretty-print the result" :default true]
   ["-h" "--help" "Print this help."]])

(defn pr-errors [errors]
  (->> errors
       (map (partial str "- "))
       (str/join \newline)))

(defn error? [{:keys [errors]}]
  (seq errors))

(defn help? [{:keys [options]}]
  (:help options))

(defn abort? [result]
  (or (error? result)
      (help? result)))

(defn usage [{:keys [summary errors]}]
  (into ["komb - sort JSON & pretty-print

Usage: komb [options] [path]

Reads content of a JSON file from *stdin* if no path provided.

Options:"
         summary
         "
Arguments:
  path - path to JSON file"]
        (when (seq errors)
          ["" "Errors:" (pr-errors errors)])))

(defn valid-json-file [path]
  (let [^File file (io/file path)
        ->error (fn [msg anomaly] {:error {:message msg :path path}
                                  :anomaly anomaly})]
    (cond
      (not (.exists file))
      (->error "File not exists" :file/missing)

      (not (.isFile file))
      (->error "Not a file" :file/not-a-file)

      (not (str/ends-with? (.getName file) ".json"))
      (->error "Not a JSON file" :file/unexpected-type)

      (not (.canRead file))
      (->error "File not readable" :file/access)

      :else {:file file})))

(defn to-str [{:keys [message path]}]
  (format "%s: '%s'" message path))

(defn parse-arguments [{:keys [arguments] :as result}]
  (let [[path & unexpected] arguments
        {:keys [file error]} (some-> path (valid-json-file))]
    (cond
      (seq unexpected)
      (update result :errors (fnil conj []) "Single path argument expected")

      (some? error)
      (update result :errors (fnil conj []) (to-str error))

      (some? file)
      (assoc result :file/json file)

      :else result)))

(defn parse-opts [args]
  (let [result (-> (cli/parse-opts args cli-spec)
                   (parse-arguments))]
    #_(when (abort? result)
      (run! println (usage result)))
    result))

(comment

  (let [args
        ["--fiz=35" "dev"]
        #_["bb.edn"]
        #_["test/it/zimpel/komb/unsorted.json" "foo"]
        #_["--no-semantic" "test/it/zimpel/komb/unsorted.json"]
        #_["--no-semantic" "-h"]]
    (parse-opts args))

  \n
  )
