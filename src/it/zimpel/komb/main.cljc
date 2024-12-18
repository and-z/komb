(ns it.zimpel.komb.main
  (:require
   [it.zimpel.komb.cli :as cli]
   [it.zimpel.komb.core :as core]
   [it.zimpel.komb.io :as io]
   #?(:cljs [nbb.core])))

(def exit-codes
  {:ok 0 :nok 1})

(defmacro try-run
  "Converts eventual exception to an error value."
  [body]
  `(try
     ~body
     (catch #?(:clj Exception :cljs js/Error) e#
       {:error e#
        :message (ex-message e#)})))

(defn main* [args]
  (let [{:keys [options file/json] :as parsed-args} (cli/parse-opts args)]
    (if (cli/abort? parsed-args)
      parsed-args
      (try-run
        (let [sorted (if (some? json)
                       (core/process-from-file options json)
                       (core/process-from-input options))]
          {:json-string (io/stringify sorted options)})))))

(comment

  (main* ["test/it/zimpel/komb/unsorted.json"])

  \n)

(defn -main [& args]
  (let [{:keys [error json-string] :as result} (main* args)]
    (cond
      (cli/abort? result)
      (run! println (cli/usage result))

      (some? error)
      #?(:clj
         (binding [*out* *err*]
           (println "Error ocurred:")
           (println error))
         :cljs
         (do
           (println "Error ocurred:")
           (println error)))

      (some? json-string)
      (println json-string))
    #?(:clj (System/exit (exit-codes (if json-string :ok :nok)))
       :cljs (set! (. js/process -exitCode) (exit-codes (if json-string :ok :nok))))))

#?(:cljs
   (when (= nbb.core/*file* (nbb.core/invoked-file))
     (apply -main (.slice js/process.argv 2))))
