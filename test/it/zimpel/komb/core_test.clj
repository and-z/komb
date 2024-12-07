(ns it.zimpel.komb.core-test
  (:require
   [clojure.test :as t]
   [clojure.pprint :as pp]
   [clojure.test :refer [deftest is testing]]
   [cheshire.core :as json]
   [it.zimpel.komb.core :as sut]))

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

(def sorted-json-str
  "{:array [\"1\" \"a\" \"b\" \"c\" \"d\"],\n :collection\n [{:a 1, :b 2, :c 3, :d 4}\n  {:__a2 1, :__b1 2, :__c4 3, :__d3 4}\n  [\"1\" \"a\" \"b\" \"c\" \"d\"]],\n :object {:a 1, :b 2, :c 3, :d 4}}\n")

(deftest sort-json-test
  (testing "Should sort object keys and sortable values"
    (let [result
          (with-out-str
            (-> (json/parse-string json-str true)
                (sut/sort-json)
                (pp/pprint)))]
      (is (= sorted-json-str result)))))

(deftest sortable?-test
  (testing "Heterogenous collections are not sortable"
    (is (false? (sut/sortable? [1 :a "foo"]))))
  (testing "Nested collections are not sortable"
    (is (false? (sut/sortable? [[1] [2]]))))
  (testing "Scalar numbers are sortable"
    (is (true? (sut/sortable? [1 2 3.0]))))
  (testing "Strings are sortable"
    (is (true? (sut/sortable? ["one" "two" "three"]))))
  (testing "Keywords are sortable"
    (is (true? (sut/sortable? [:a :b :c])))))
