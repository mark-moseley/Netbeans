/**
 *	This generated bean class WsdlTemplateType
 *	matches the schema element 'wsdlTemplateType'.
 *  The root bean class is TemplateGroup
 *
 *	Generated on Wed Oct 22 17:28:35 PDT 2008
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template;

public class WsdlTemplateType {
	public static final String FILE = "File";	// NOI18N

	private java.lang.String _File;

	/**
	 * Normal starting point constructor.
	 */
	public WsdlTemplateType() {
	}

	/**
	 * Deep copy
	 */
	public WsdlTemplateType(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public WsdlTemplateType(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType source, boolean justData) {
		_File = source._File;
	}

	// This attribute is optional
	public void setFile(java.lang.String value) {
		_File = value;
	}

	public java.lang.String getFile() {
		return _File;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "wsdlTemplateType";
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
		// file is an attribute with namespace http://xml.netbeans.org/schema/templates
		if (_File != null) {
			out.write(" file='");
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup.writeXML(out, _File, true);
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("file");
		if (attr != null) {
			attrValue = attr.getValue();
			_File = attrValue;
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "file")
			setFile((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for WsdlTemplateType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "file")
			return getFile();
		throw new IllegalArgumentException(name+" is not a valid property name for WsdlTemplateType");
	}

	public String nameSelf() {
		return "WsdlTemplateType";
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
			if (child == _File) {
				if (returnConstName) {
					return FILE;
				} else if (returnSchemaName) {
					return "file";
				} else if (returnXPathName) {
					return "@file";
				} else {
					return "File";
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
		return o instanceof org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType && equals((org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlTemplateType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_File == null ? inst._File == null : _File.equals(inst._File))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_File == null ? 0 : _File.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

 The Original Software is NetBeans. The Initial Developer of the Original
 Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->


<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/templates"
            xmlns:tns="http://xml.netbeans.org/schema/templates"
            elementFormDefault="qualified">
    <xsd:element name="templateGroup">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="template" type="tns:templateType" maxOccurs="unbounded"/>
            </xsd:sequence>
            <xsd:attribute name="namespace" type="xsd:string"/>
            <xsd:attribute name="prefix" type="xsd:string"/>
            <xsd:attribute name="skeleton" type="xsd:boolean"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:complexType name="templateType">
        <xsd:sequence>
            <xsd:element name="wsdlTemplate" type="tns:wsdlTemplateType" maxOccurs="1"/>
            <xsd:element name="wsdlElement" type="tns:wsdlElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="default" type="xsd:boolean"/>
        <xsd:attribute name="skeleton" type="xsd:boolean"/>
        <xsd:attribute name="mode" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="wsdlElementType">
        <xsd:sequence>
            <xsd:element name="extensionElement" type="tns:extensionElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="wsdlTemplateType">
        <xsd:attribute name="file" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionElementType">
        <xsd:sequence>
            <xsd:element name="extensionAttr" type="tns:extensionAttrType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionAttrType">
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="defaultValue" type="xsd:string"/>
    </xsd:complexType>
</xsd:schema>

*/
