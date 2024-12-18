(ns it.zimpel.komb.cli
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [it.zimpel.komb.io :as io]))

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

(defn to-str [{:keys [message path]}]
  (str message ": '" path "'"))

(defn parse-arguments [{:keys [arguments] :as result}]
  (let [[path & unexpected] arguments
        {:keys [file error]} (some-> path (io/valid-json-file))]
    (cond
      (seq unexpected)
      (update result :errors (fnil conj []) "Single path argument expected")

      (some? error)
      (update result :errors (fnil conj []) (to-str error))

      (some? file)
      (assoc result :file/json file)

      :else result)))

(defn parse-opts [args]
  (-> (cli/parse-opts args cli-spec)
      (parse-arguments)))

(comment

  (let [args
        ["--fiz=35" "dev"]
        #_["bb.edn"]
        #_["test/it/zimpel/komb/unsorted.json" "foo"]
        #_["--no-semantic" "test/it/zimpel/komb/unsorted.json"]
        #_["--no-semantic" "-h"]]
    (parse-opts args))

  \n)
