/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *	This generated bean class Binding
 *	matches the schema element 'binding'.
 *  The root bean class is Schemas
 *
 *	Generated on Thu Apr 05 11:55:09 PDT 2007
 * @Generated
 */

package org.netbeans.modules.xml.jaxb.cfg.schema;

public class Binding {
	public static final String ORIGLOCATION = "OrigLocation";	// NOI18N
	public static final String LOCATION = "Location";	// NOI18N

	private java.lang.String _OrigLocation;
	private java.lang.String _Location;

	/**
	 * Normal starting point constructor.
	 */
	public Binding() {
	}

	/**
	 * Deep copy
	 */
	public Binding(org.netbeans.modules.xml.jaxb.cfg.schema.Binding source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Binding(org.netbeans.modules.xml.jaxb.cfg.schema.Binding source, boolean justData) {
		_OrigLocation = source._OrigLocation;
		_Location = source._Location;
	}

	// This attribute is optional
	public void setOrigLocation(java.lang.String value) {
		_OrigLocation = value;
	}

	public java.lang.String getOrigLocation() {
		return _OrigLocation;
	}

	// This attribute is optional
	public void setLocation(java.lang.String value) {
		_Location = value;
	}

	public java.lang.String getLocation() {
		return _Location;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "binding";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write("/>\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		// origLocation is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_OrigLocation != null) {
			out.write(" origLocation='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _OrigLocation, true);
			out.write("'");	// NOI18N
		}
		// location is an attribute with namespace http://xml.netbeans.org/schema/JAXBWizConfig
		if (_Location != null) {
			out.write(" location='");
			org.netbeans.modules.xml.jaxb.cfg.schema.Schemas.writeXML(out, _Location, true);
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6, attrName.length());
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
			readNodeAttributes(node, namespacePrefixes, attrs);
		}
		readNodeChildren(node, namespacePrefixes);
	}

	protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
		org.w3c.dom.Attr attr;
		java.lang.String attrValue;
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("origLocation");
		if (attr != null) {
			attrValue = attr.getValue();
			_OrigLocation = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("location");
		if (attr != null) {
			attrValue = attr.getValue();
			_Location = attrValue;
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "origLocation")
			setOrigLocation((java.lang.String)value);
		else if (name == "location")
			setLocation((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Binding");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "origLocation")
			return getOrigLocation();
		if (name == "location")
			return getLocation();
		throw new IllegalArgumentException(name+" is not a valid property name for Binding");
	}

	public String nameSelf() {
		return "Binding";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _OrigLocation) {
				if (returnConstName) {
					return ORIGLOCATION;
				} else if (returnSchemaName) {
					return "origLocation";
				} else if (returnXPathName) {
					return "@origLocation";
				} else {
					return "OrigLocation";
				}
			}
			if (child == _Location) {
				if (returnConstName) {
					return LOCATION;
				} else if (returnSchemaName) {
					return "location";
				} else if (returnXPathName) {
					return "@location";
				} else {
					return "Location";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public java.lang.Object[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		java.lang.Object[] result = new java.lang.Object[children.size()];
		return (java.lang.Object[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.jaxb.cfg.schema.Binding && equals((org.netbeans.modules.xml.jaxb.cfg.schema.Binding) o);
	}

	public boolean equals(org.netbeans.modules.xml.jaxb.cfg.schema.Binding inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_OrigLocation == null ? inst._OrigLocation == null : _OrigLocation.equals(inst._OrigLocation))) {
			return false;
		}
		if (!(_Location == null ? inst._Location == null : _Location.equals(inst._Location))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_OrigLocation == null ? 0 : _OrigLocation.hashCode());
		result = 37*result + (_Location == null ? 0 : _Location.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/JAXBWizConfig"
            xmlns:tns="http://xml.netbeans.org/schema/JAXBWizConfig"
            elementFormDefault="qualified">
                
    <xsd:element name="schemas">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:schema" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="destdir" type="xsd:string"/>
            <xsd:attribute name="projectName" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element ref="tns:xjc-options" minOccurs="0"/>
                <xsd:element ref="tns:schema-sources" minOccurs="0"/>
                <xsd:element ref="tns:bindings" minOccurs="0"/>
                <xsd:element ref="tns:catalog" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="type" type="xsd:string"/>
            <xsd:attribute name="package" type="xsd:string"/>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="xjc-options">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:xjc-option" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="xjc-option">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="name" type="xsd:string"/>
            <xsd:attribute name="value" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema-sources">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:schema-source" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="schema-source">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="location" type="xsd:string">
                <xsd:annotation>
                    <xsd:documentation>location is relative to Project Root. origLocation is either local file path or URL</xsd:documentation>
                </xsd:annotation>
            </xsd:attribute>
            <xsd:attribute name="origLocation" type="xsd:string"/>
            <xsd:attribute name="origLocationType" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="bindings">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:binding" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="binding">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="origLocation" type="xsd:string"/>
            <xsd:attribute name="location" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="catalog">
        <xsd:complexType>
            <xsd:sequence/>
            <xsd:attribute name="location" type="xsd:string"/>
            <xsd:attribute name="origLocation" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
