(ns xstandard.core
  (:use [hiccup.core :only [html]]
    [clojure.contrib.def :only [name-with-attributes]]
    [clojure.java.io :only [file]])
  (:require [saxon :as xml]))

(xml/set-config-property! :line-numbering true)

(defonce *nss* {:xsd "http://www.w3.org/2001/XMLSchema"})

(defn get-attr
  "return the attr value of n"
  [n attr]
  (xml/query (str "data(./@" attr ")") n))

(defn name-attr
  "wraps a call to get-attr. Returns the name attribute of n"
  [n] (get-attr n "name"))

(defn attr-present [attr]
  "return true if the attr is present on n"
  (fn [n]
    (xml/query (str "exists(./@" attr ")") n)))

(defn attr-eq
  "true if the attr of n is equal to v"
  [attr v]
  (fn [n]
    (= (get-attr n attr) v)))


(defn attr-matches
  "validates the format of a given node n against regex"
  [attr regex]
  (fn [n]
    (not (nil? (re-matches regex (get-attr n attr))))))


(defn make-xml
  "Utility method to help build xml from file path. For file only"
  [p]
  (xml/compile-xml (file p)))

(defn- line
  "Wrapps the call to .getLineNumber in the current node n"
  [n]
  (.getLineNumber n))

(defn check
  "main method takes an xml compiled and apply the assertions"
  [xmldoc assertions nss & options]
  (flatten
    (for [c assertions]
      (let [v (:validator c)
            p (:path c)
            node-name (or (:node-name c) "data(./@name)")]
        (for [node (flatten (list (xml/query p nss xmldoc)))
              :when (not (v node))]
          {:result-msg (format (:error-msg c) (xml/query node-name nss node))
           :node-path (xml/node-path node)
           :line (line node)})))))


(defn make-assertion
  "Actually builds an assertion fn."
  [aname p & {:keys [validator msg display-name]}]
  (fn [nss doc]
    (let [display-name-exp (xml/compile-xpath (or display-name "data(./@name)") nss)
          p-exp (xml/compile-xpath p nss)
          n (p-exp doc)
          result-msg (cond (empty? msg) "Assertion failed."
        :else (format msg (display-name-exp n)))
          line-number (line n)
          result-status (validator n)]
      {:assertion aname
       :status result-status
       :details
       (if (not result-status)
         {:result-msg result-msg
          :line line-number
          :path (xml/node-path n)}
         {})
       })))

(defmacro defassertion [name p & options]
  (let [[name options] (name-with-attributes name options)]
    `(def ~name {:path ~p :assertion (make-assertion ~(keyword name) ~p ~@options)})))

(defmacro defassertions [name & a]
  (let [[name a] (name-with-attributes name a)]
    `(def ~name
        (loop [as# (list ~@a) fs# {:set-name ~name :assertions {}}]
          (if (empty? as#)
                fs#
            (let [[c# & rest#] as#
                  p# (:path c#)
                  a# (:assertion c#)
                  pre# (get (:assertions fs#) p#)]
              (recur rest# (assoc-in fs# [:assertions p#] (cons a# pre#)))))))))

(defn run [aset nss xmldoc]
  (flatten
    (for [[p a] (:assertions aset)
          n (xml/query (name p) nss xmldoc)]
      (a nss xmldoc))))


(defassertions *default-assertions*
    (defassertion element-name "//xsd:element[@name]"
      :msg "element %s does not match [a-z].*."
      :validator (attr-matches "name" #"[a-z].*")
      :display-name "data(./@name)")

    (defassertion type-name "//xsd:complexType[@name]"
      :msg "type %s does not match [A-Z].*Type."
      :validator (attr-matches "name" #"[A-Z].*Type")
      :display-name "data(./@name)")

    (defassertion element-form-default "/xsd:schema"
      :msg "schema hasn't attr elementFormDefault=\"qualified\""
      :validator (attr-eq "elementFormDefault" "qualified"))

    (defassertion target-ns "/xsd:schema"
      :msg "schema hasn't targetNamespace attr"
      :validator (attr-present "targetNamespace")))



(defn check-default
  "wraps the check function with default assertions and namespaces"
  [xmldoc & options]
  (check xmldoc *default-assertions* *nss* options))



(defmacro as-html
  "Simply wrapps the execution of check or check-default in html output."
  [& f]
  `(html [:html
          [:head
           [:title "xstandard assertion result."]]
          [:body
           [:h2 {:class "header"} "Assertion result."]
           [:table {:class "result-table"}
            [:tr {:class "result-head"} [:th "Result Message"] [:th "Path to node in the XML document"]]
            (for [r# ~@f]
              [:tr
               [:td (:result-msg r#)]
               [:td (:node-path r#)]])]]]))




(def xmldoc (make-xml "/Users/paulo/Documents/workspace/cljprojects/xstandard/test/sample_1.xsd"))

(println *default-assertions*)

;(let [result (run *default-assertions* *nss* xmldoc)]
;  (println result))
