(ns it.zimpel.komb.core-test
  (:require
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]
   [clojure.test :as t]
   [clojure.test :refer [deftest is testing]]
   [it.zimpel.komb.core :as sut]
   [it.zimpel.komb.io :as komb.io]))

(defn test-file []
  (io/file "test/it/zimpel/komb/unsorted.json"))

(defn json-str []
  (slurp (test-file)))

(def sorted-json-str
  "{:array [\"1\" \"a\" \"b\" \"c\" \"d\"],\n :collection\n [{:a 1, :b 2, :c 3, :d 4}\n  {:__a2 1, :__b1 2, :__c4 3, :__d3 4}\n  [\"1\" \"a\" \"b\" \"c\" \"d\"]],\n :object {:a 1, :b 2, :c 3, :d 4}}\n")

(def sorted-semantically-json-str
  "{\"array\":[\"d\",\"1\",\"c\",\"a\",\"b\"],\"collection\":[{\"a\":1,\"b\":2,\"c\":3,\"d\":4},{\"__a2\":1,\"__b1\":2,\"__c4\":3,\"__d3\":4},[\"d\",\"1\",\"c\",\"a\",\"b\"]],\"object\":{\"a\":1,\"b\":2,\"c\":3,\"d\":4}}")

(deftest sort-json-test
  (testing "Should sort object keys and sortable values"
    (let [result
          (with-out-str
            (-> (json/parse-string (json-str) true)
                (sut/sort-json {:semantic? false})
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

(deftest sort-semantically
  (testing "should retain array ordering"
    (let [options {:semantic? true :pretty false}]
      (is (= ["b" "c" "a"]
             (sut/sort-json-str options "[\"b\", \"c\", \"a\"]")))
      (is (= sorted-semantically-json-str
             (-> (sut/process-from-file options (test-file))
                 (komb.io/stringify options)))))))
