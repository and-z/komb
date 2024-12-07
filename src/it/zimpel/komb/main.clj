(ns it.zimpel.komb.main
  (:require [cheshire.core :as json]
            [it.zimpel.komb.core :as core]))

(set! *warn-on-reflection* true)

(def usage
  "komb - sort (nested) JSON & pretty-print

Usage: komb [path]

Reads content of a JSON file from *stdin* if no path provided.

Params:
  path - path to JSON file

Help:
  --help, -h    Print this help.
")

(def exit-codes
  {:ok 0 :nok 1})

(defn need-help? [s]
  (or (= "-h" s)
      (= "--help" s)
      (= "help" s)))

(defmacro try-run
  "Converts eventual exception to an error value."
  [body]
  `(try
     ~body
     (catch Exception e#
       {:error e#
        :message (ex-message e#)})))

(defn main* [args]
  (let [[param & more] args]
    (cond
      (or (seq more)
          (need-help? param))
      (println usage)

      :else
      (try-run
       (let [{:keys [error data] :as result}
             (if (nil? param)
               (core/process-from-input)
               (core/process-from-file param))]
         (if (some? error)
           result
           {:json-string (json/generate-string data {:pretty true})}))))))

(defn -main [& args]
  (let [{:keys [error json-string]} (main* args)]
    (when error
      (binding [*out* *err*]
        (println "Error ocurred:")
        (println error)))
    (when json-string
      (println json-string))
    (System/exit (exit-codes (if json-string :ok :nok)))))

(comment

  (main* ["test/it/zimpel/komb/unsorted.json"])
  ;; => {:json-string ",,,"}

  (main* ["bb.edn"])
  ;; => {:error {:message "Not a JSON file", :path "bb.edn"}}

  (core/get-json-file "bb.edn")

  (core/get-json-file "test/it/zimpel/komb/unsorted.json")

  (-> (main* ["test/it/zimpel/komb/unsorted.json"])
      :json-string
      println)

  \n)
