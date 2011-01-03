(ns xstandard.core
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
  (list {:msg "element %s does not match [a-z].*." :path "//xsd:element[@name]" :validator (attr-matches "name" #"[a-z].*")}
        {:msg "type %s does not match [A-Z].*Type." :path "//xsd:complexType[@name]" :validator (attr-matches "name" #"[A-Z].*Type")}
        {:msg "schema hasn't attr elementFormDefault=\"qualified\"" :path "/xsd:schema" :validator (attr-eq "elementFormDefault" "qualified")}
        {:msg "schema hasn't targetNamespace attr" :path "/xsd:schema" :validator (attr-present "targetNamespace")}))


(defn make-xml
  "Utility method to help build xml from file path. For file only"
  [p]
  (try
    (xml/compile-xml (java.io.File. p))
    (catch java.io.IOException ex (.printStackTrace ex))))

(defn check
  "main method takes an xml compiled and apply the assertions"
  [xmldoc assertions nss & options]
  (flatten
    (for [c assertions]
      (let [v (:validator c)
            p (:path c)]
        (for [node (flatten (list (xml/query p nss xmldoc)))
              :when (not (v node))]
          {:result-msg (format (:msg c) (name-attr node)) :node-path (xml/node-path node)})))))


(defn check-default
  "wraps the check function with default assertions and namespaces"
  [xmldoc]
  (check xmldoc *default-assertions* *nss*))