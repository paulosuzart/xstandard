===========
Intro
===========

A really simple abstraction on top of `saxon`_. xstandard works with assertions lists that are applyed against a XML.

xstandard comes with default assertions and helper functions to help you create your own assertions. 

.. _`saxon`: https://github.com/pjt/saxon

================
Sample assertion
================

A simple assertion to check if a given element attribute matches a given format::

 {:msg "element %s does not match [a-z].*." :path "//xsd:element[@name]" :validator (attr-matches "name" #"[a-z].*")}

The result should be::
  
 :msg element Item does not match [a-z].*., :path /xs:schema/xs:element[1]/xs:complexType[1]/xs:sequence[1]/xs:element[3]} 

Taking the xml::

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
