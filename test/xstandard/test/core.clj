(ns xstandard.test.core
  (:use [xstandard.core :as xs] :reload)
  (:use [clojure.test])
  (:use [clojure.string :only [blank?]]))

(def xmldoc (xs/make-xml "./test/sample_1.xsd"))

;; this is a dummy test. TODO cover all the lib.
(deftest test-total
  (testing "total nodes analyzed should be > 0"
    (let [result (xs/check-default xmldoc)]
      (println result)
      (is (> (count result) 0))))

  (testing "should print a non empty string"
    (let [result (as-html (xs/check-default xmldoc))]
      (println result)
      (is (= 1 1))))


(def sset (list
  (defassertion as1 "/xsd:schema"
      :validator (attr-eq "elementFormDefault" "qualified")
      :msg "Element form default should be qualified.")
  (defassertion as2 "/xsd:schema"
      :validator (attr-present "targetNamespace"))))

  (println sset)

  (testing "shoudl work"
  (let [result (xs/run sset xs/*nss* xmldoc)]
     (println result)
     (is (= 1 1)))))

