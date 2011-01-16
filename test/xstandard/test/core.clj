(ns xstandard.test.core
  (:use [xstandard.core :as xs] :reload)
  (:use [clojure.test]
    [saxon :as xml]))

(def xmldoc (xs/make-xml "./test/sample_1.xsd"))

(def sample2 (xs/make-xml "./test/sample_2.xsd"))

(def schema-node (xml/query "/xsd:schema" xs/*nss* sample2))

;; this is a dummy test. TODO cover all the lib.
(deftest test-total
    (testing "should work"
      (let [result (xs/as-html "output.html" (xs/run xs/*default-assertions* xs/*nss* xmldoc))]
        (println result)
        (is (= 1 1))))

    (testing "elementFormDefault is present"
      (is (true? ((xs/attr-present "elementFormDefault") schema-node))))

    (testing "elementFormDefault value should eq \"qualified\""
      (is (true? ((xs/attr-eq "elementFormDefault" "qualified") schema-node)))))

