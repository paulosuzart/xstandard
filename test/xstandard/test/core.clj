(ns xstandard.test.core
  (:use [xstandard.core :as xs] :reload)
  (:use [clojure.test]))

(def xmldoc (xs/make-xml "untitled.xsd"))

;; this is a dummy test. TODO cover all the lib.
(deftest test-total
  (testing "total nodes analyzed should be > 0"
    (let [result (xs/check-default xmldoc)]
      (print result)
      (is (> (count result) 0)))))

