(ns xstandard.core
  (:use [hiccup.core :only [html]]
        [clojure.java.io :only [file]])
  (:require [saxon :as xml]))

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


(defonce
  ^{:private true}
  *default-assertions*
  (list {:error-msg "element %s does not match [a-z].*."
         :path "//xsd:element[@name]"
         :validator (attr-matches "name" #"[a-z].*")
         :node-name name-attr}

    {:error-msg "type %s does not match [A-Z].*Type."
     :path "//xsd:complexType[@name]"
     :validator (attr-matches "name" #"[A-Z].*Type")
     :node-name name-attr}

    {:error-msg "schema hasn't attr elementFormDefault=\"qualified\""
     :path "/xsd:schema"
     :validator (attr-eq "elementFormDefault" "qualified")}

    {:error-msg "schema hasn't targetNamespace attr"
     :path "/xsd:schema"
     :validator (attr-present "targetNamespace")}))


(defn make-xml
  "Utility method to help build xml from file path. For file only"
  [p]
  (xml/compile-xml (file p)))

(defn check
  "main method takes an xml compiled and apply the assertions"
  [xmldoc assertions nss & options]
  (flatten
    (for [c assertions]
      (let [v (:validator c)
            p (:path c)
            node-name (or (:node-name c) name-attr)]
        (for [node (flatten (list (xml/query p nss xmldoc)))
              :when (not (v node))]
          {:result-msg (format (:error-msg c) (node-name node))
           :node-path (xml/node-path node)})))))


(defn check-default
  "wraps the check function with default assertions and namespaces"
  [xmldoc & options]
  (check xmldoc *default-assertions* *nss* options))


(defmacro as-html [& f]
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
