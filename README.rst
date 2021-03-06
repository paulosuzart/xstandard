===========
Intro
===========

A really simple abstraction on top of `saxon`_. xstandard works with assertions lists that are applyied against a XML.

An assertion is set with a message to show if it fails, a path to select the node(s) (an xpath expression) and a validate function that receives the current node as argument to be validated.

xstandard comes with default assertions and helper functions to help you create your own assertions. 

.. _`saxon`: https://github.com/pjt/saxon

================
Sample assertion
================

A simple assertion to select all elements with 'name' attribute (:path "//xsd:element[@name]) and validade them against attr-matches (a sample validator)::

 (defassertion element-name "//xsd:element[@name]"
     :msg "element %s does not match [a-z].*."
     :validator (attr-matches "name" #"[a-z].*")
     :display-name "data(./@name)")

The result should be::
  
 {:assertion :element-name, :status false, :display-name Item, 
  :details {:result-msg element Item does not match [a-z].*., 
            :line 25, 
            :path /xs:schema/xs:element[1]/xs:complexType[1]/xs:sequence[1]/xs:element[3]}}

The result (a map) is the formated message and the path - in the xmldoc - to the node that fails. 

You can either group a bunch of assertions using defassertions macro like this::

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
      :validator (attr-eq "elementFormDefault" "qualified")))


Using the macro ``as-html`` wrapps the result as html string with hiccup. (temporary unavailable)

Regarding the xml::

  <?xml version="1.0" encoding="ISO-8859-1" ?>
  <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.example.com/schema" elementFormDefault="qualified">

  <xs:element name="shiporder">
    <xs:complexType>
      <xs:sequence>
        <xs:element name="orderperson" type="xs:string"/>
        <xs:element name="shipto">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="name" type="xs:string"/>
              <xs:element name="address" type="xs:string"/>
              <xs:element name="city" type="xs:string"/>
              <xs:element name="country" type="xs:string"/>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
        <xs:element name="Item" maxOccurs="unbounded">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="title" type="xs:string"/>
              <xs:element name="note" type="xs:string" minOccurs="0"/>
              <xs:element name="quantity" type="xs:positiveInteger"/>
              <xs:element name="price" type="xs:decimal"/>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
      </xs:sequence>
      <xs:attribute name="orderid" type="xs:string" use="required"/>
    </xs:complexType>
  </xs:element>
  <xs:complexType name="MyType">
    <xs:sequence>
      <xs:element name="title" type="xs:string"/>
     </xs:sequence>
  </xs:complexType>
 </xs:schema>

====
TODO
====
#. Add (as-html (check-default xmldoc)) to return the result as html. OK
#. Add (as-json (check-default xmldoc)) to return the result as json.
#. Add (as-xml (check-default xmldoc)) to return the result in xml.
#. Actually use the options arg of check to filter the results by valid results, invalid results, element, etc.
#. Maybe put it on top of compojure.
#. Use an xpath to select the node identifier, not just the attribute named 'name'. OK
#. Validate assertions. Partially tested.
#. Execute assertions grouped by path to avoid several queries on the xmldoc. OK