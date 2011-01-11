(ns xstandard.test.core
  (:use [xstandard.core :as xs] :reload)
  (:use [clojure.test]))

(def xmldoc (xs/make-xml "./test/sample_1.xsd"))

;; this is a dummy test. TODO cover all the lib.
(deftest test-total
  (testing "shoudl work"
  (let [result (xs/run xs/*default-assertions* xs/*nss* xmldoc)]
     (println result)
     (is (= 1 1)))
  (testing "should work2"
    (let [result (xs/as-html "output.html" (xs/run xs/*default-assertions* xs/*nss* xmldoc))]
      (println result)
      (is (= 1 1))))))

