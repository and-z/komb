(ns scratch
  (:require [cheshire.core :as json]
            [clojure.walk :as walk]
            [clojure.pprint :as pp]
            [it.zimpel.komb.core :as komb]))

(def json-str
  "
{
 \"object\": {
   \"b\": 2,
   \"a\": 1,
   \"d\": 4,
   \"c\": 3
  },
 \"array\": [\"d\",\"1\",\"c\",\"a\",\"b\"],
 \"collection\": [
    {
     \"b\": 2,
     \"a\": 1,
     \"d\": 4,
     \"c\": 3
    },
    {
     \"__b1\": 2,
     \"__a2\": 1,
     \"__d3\": 4,
     \"__c4\": 3
    },
    [\"d\",\"1\",\"c\",\"a\",\"b\"]
  ]
}")


(comment

  (-> (json/parse-string json-str true)
      (komb/sort-json)
      (pp/pprint))


  (-> (json/parse-string json-str true)
      (walk/prewalk-demo))

  \n)
