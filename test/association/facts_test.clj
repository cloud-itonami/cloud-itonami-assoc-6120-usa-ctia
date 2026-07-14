(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest ctia-has-spec-basis
  (let [sb (facts/spec-basis "ctia")]
    (is (= 2 (count sb)))
    (is (every? #(= "6120" (:association-rule/isic %)) sb))
    (is (every? #(= "USA" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "gsma")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["ctia" "gsma"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["gsma"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["ctia.smartphone-anti-theft-voluntary-commitment"]
         (mapv :association-rule/id (facts/by-topic "ctia" :device-security))))
  (is (empty? (facts/by-topic "ctia" :labor)))
  (is (empty? (facts/by-topic "gsma" :consumer-protection))))
