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


(defn make-assertion
  "Actually builds an assertion as fn.
  fn get the namespaces and a single node selected by any other part of the code. The result is:

  {:assertion assertion name as symbol
   :status true or false ;true the node passed, false otherwise.
   :display-name resunting name
   :details {:result-msg formated result message.
             :line the node line
             :path to the node}}.

   Note however, that details will be returned for failed nodes."
  [aname p & {:keys [validator msg display-name]}]
  (fn [nss n]
    (let [display-name-exp (xml/compile-xpath (or display-name "data(./@name)") nss)
          p-exp (xml/compile-xpath p nss)
          result-msg (cond (empty? msg) "Assertion failed."
                      :else (format msg (display-name-exp n)))
          line-number (line n)
          result-status (validator n)]
      {:assertion aname
       :status result-status
       :display-name (or (display-name-exp n) (.toString (xml/node-name n)))
       :details
       (if (not result-status)
         {:result-msg result-msg
          :line line-number
          :path (xml/node-path n)}
         {})
       })))


(defmacro defassertion
  "An assertion is supposed to be created i.e.:
    (defassertion element-name \"//xsd:element[@name]\"
      :msg \"element %s does not match [a-z].*.\"
      :validator (attr-matches \"name\" #\"[a-z].*\")
      :display-name \"data(./@name)\")

      A name should be passed to label the assertion. Assertions becomes Vars in the namespace. p is the string path (in xpath) to the node(s)."
  [name p & options]
  (let [[name options] (name-with-attributes name options)]
    `(do
        (def ~name {:path ~p :assertion (make-assertion ~(keyword name) ~p ~@options)})
         ~name)))


(defn make-assertions
  "Actually builds a set of assertions. To optimize the performance, all assertions are grouped by path. That is, defassertions should produce something like:

  {:set-name myAssertions
   :assertions {/xsd:schema (a b),  //xsd:element (c)}

   Where a, b and c are assertions defined by defassertion."

  [n assertions]
  (loop [as assertions
         fs {:set-name n}]
    (if (empty? as)
          fs
      (let [[c & rest] as
             p (:path c)
             a (:assertion c)]
        (recur rest (update-in fs [:assertions p] #(cons a %)))))))


(defmacro defassertions
  "A macro to prepare the definition of a set of assertions.
  name is the name of the set and a* the assertions. i.e.:

  (defassertions my-set (defassertion ...) (defassertion ...))"

  [name & a]
  (let [[n a] (name-with-attributes name a)
        sname (str name)]
    `(def ~n (make-assertions ~sname (list ~@a)))))

(defn run
  "Run every assertion path againts the xmldocument and applies every found node to every assertion set for that path."
  [aset nss xmldoc]
  (flatten
    (for [[p a] (:assertions aset)]
      (for [i a]
        (for [n (flatten (list (xml/query p nss xmldoc)))]
          (i nss n))))))


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
