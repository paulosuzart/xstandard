===========
Intro
===========

A really simple abstraction on top of `saxon`_. xstandard works with assertions lists that are applyied against a XML.

An assertion is a map literal containing three keys: msg shows a message at the end of assertion application (can receive only one parameter, the currente analyzed node); path is used to select the node (an xpath expression); validate any function that receives the current node as argument to be validated.

xstandard comes with default assertions and helper functions to help you create your own assertions. 

.. _`saxon`: https://github.com/pjt/saxon

================
Sample assertion
================

A simple assertion to select any element with the name attribute (:path "//xsd:element[@name]) and validade it againts attr-matches (a sample validator)::

 {:msg "element %s does not match [a-z].*." :path "//xsd:element[@name]" :validator (attr-matches "name" #"[a-z].*")}

The result should be::
  
 {:result-msg element Item does not match [a-z].*., :node-path /xs:schema/xs:element[1]/xs:complexType[1]/xs:sequence[1]/xs:element[3]} 

The result is the formated message and the path - in the proven xml - to the node that fails. 

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
#. Add (as-html (check-default xmldoc)) to return the result in HTML.
#. Add (as-json (check-default xmldoc)) to return the result as json.
#. Add (as-xml (check-default xmldoc)) to return the result in xml.
#. Actually use the options arg of check to filter the results by valid results, invalid results, element, etc.
#. Maybe put it on top of compojure
