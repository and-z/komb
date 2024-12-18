(ns it.zimpel.komb.io
  (:require
   [clojure.string :as str]
   #?@(:clj
       [[cheshire.core :as json]
        [clojure.java.io :as io]]

       :cljs
       [["fs" :as fs]]))
  #?(:clj
     (:import
      (java.io File))))

#?(:clj (set! *warn-on-reflection* true))

(defn parse-string [s]
  #?(:clj (json/parse-string s true)
     :cljs (js->clj (js/JSON.parse s))))

(defn stringify [json {:keys [pretty] :as options}]
  #?(:clj (json/generate-string json options)
     :cljs (js/JSON.stringify (clj->js json) nil (if pretty 2 0))))

(defn to-str
  "Read file contents as string"
  [file]
  #?(:clj (slurp file)
     :cljs (fs/readFileSync file "UTF-8")))

(defn stdio->str []
  #?(:clj
     (with-open [r (io/reader *in*)]
       (slurp r))
     :cljs
     (fs/readFileSync 0 "UTF-8")))

(defn ->error [path msg anomaly]
  {:error {:message msg :path path}
   :anomaly anomaly})

#?(:clj
   (defn valid-json-file-jvm [path]
     (let [^File file (io/file path)
           ->error (partial ->error path)]
       (cond
         (not (.exists file))
         (->error "File not exists" :file/missing)

         (not (.isFile file))
         (->error "Not a file" :file/not-a-file)

         (not (str/ends-with? (.getName file) ".json"))
         (->error "Not a JSON file" :file/unexpected-type)

         (not (.canRead file))
         (->error "File not readable" :file/access)

         :else {:file file}))))

#?(:cljs
   (defn valid-json-file-js [path]
     (let [->error (partial ->error path)]
       (cond
         (not (fs/existsSync path))
         (->error "File not exists" :file/missing)

         (not (.isFile (fs/statSync path)))
         (->error "Not a file" :file/not-a-file)

         (not (str/ends-with? path ".json"))
         (->error "Not a JSON file" :file/unexpected-type)

         :else {:file path}))))

(defn valid-json-file [path]
  #?(:clj (valid-json-file-jvm path)
     :cljs (valid-json-file-js path)))
