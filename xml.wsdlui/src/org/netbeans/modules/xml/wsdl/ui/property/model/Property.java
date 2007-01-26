/**
 *	This generated bean class Property
 *	matches the schema element 'Property'.
 *  The root bean class is ElementProperties
 *
 *	Generated on Tue Jan 23 19:08:48 PST 2007
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class Property {
	public static final String ATTRIBUTENAME = "AttributeName";	// NOI18N
	public static final String GROUPNAME = "GroupName";	// NOI18N
	public static final String PROPERTYORDER = "PropertyOrder";	// NOI18N
	public static final String BUILTINCUSTOMIZER = "BuiltInCustomizer";	// NOI18N
	public static final String NEWCUSTOMIZER = "NewCustomizer";	// NOI18N

	private java.lang.String _AttributeName;
	private java.lang.String _GroupName;
	private int _PropertyOrder;
	private BuiltInCustomizer _BuiltInCustomizer;
	private NewCustomizer _NewCustomizer;

	/**
	 * Normal starting point constructor.
	 */
	public Property() {
		_AttributeName = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Property(java.lang.String attributeName) {
		_AttributeName = attributeName;
	}

	/**
	 * Deep copy
	 */
	public Property(org.netbeans.modules.xml.wsdl.ui.property.model.Property source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Property(org.netbeans.modules.xml.wsdl.ui.property.model.Property source, boolean justData) {
		_AttributeName = source._AttributeName;
		_GroupName = source._GroupName;
		_PropertyOrder = source._PropertyOrder;
		_BuiltInCustomizer = (source._BuiltInCustomizer == null) ? null : newBuiltInCustomizer(source._BuiltInCustomizer, justData);
		_NewCustomizer = (source._NewCustomizer == null) ? null : newNewCustomizer(source._NewCustomizer, justData);
	}

	// This attribute is mandatory
	public void setAttributeName(java.lang.String value) {
		_AttributeName = value;
	}

	public java.lang.String getAttributeName() {
		return _AttributeName;
	}

	// This attribute is optional
	public void setGroupName(java.lang.String value) {
		_GroupName = value;
	}

	public java.lang.String getGroupName() {
		return _GroupName;
	}

	// This attribute is optional
	public void setPropertyOrder(int value) {
		_PropertyOrder = value;
	}

	public int getPropertyOrder() {
		return _PropertyOrder;
	}

	// This attribute is mandatory
	public void setBuiltInCustomizer(org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer value) {
		_BuiltInCustomizer = value;
		if (value != null) {
			// It's a mutually exclusive property.
			setNewCustomizer(null);
		}
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer getBuiltInCustomizer() {
		return _BuiltInCustomizer;
	}

	// This attribute is mandatory
	public void setNewCustomizer(org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer value) {
		_NewCustomizer = value;
		if (value != null) {
			// It's a mutually exclusive property.
			setBuiltInCustomizer(null);
		}
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer getNewCustomizer() {
		return _NewCustomizer;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer newBuiltInCustomizer() {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer newBuiltInCustomizer(BuiltInCustomizer source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer newNewCustomizer() {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer newNewCustomizer(NewCustomizer source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "Property";
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
		out.write(">\n");
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		// attributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
		if (_AttributeName != null) {
			out.write(" attributeName='");
			org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _AttributeName, true);
			out.write("'");	// NOI18N
		}
		// groupName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
		if (_GroupName != null) {
			out.write(" groupName='");
			org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _GroupName, true);
			out.write("'");	// NOI18N
		}
		// propertyOrder is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
		out.write(" propertyOrder='");
		out.write(""+_PropertyOrder);
		out.write("'");	// NOI18N
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_BuiltInCustomizer != null) {
			_BuiltInCustomizer.writeNode(out, "BuiltInCustomizer", null, nextIndent, namespaceMap);
		}
		if (_NewCustomizer != null) {
			_NewCustomizer.writeNode(out, "NewCustomizer", null, nextIndent, namespaceMap);
		}
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("attributeName");
		if (attr != null) {
			attrValue = attr.getValue();
			_AttributeName = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("groupName");
		if (attr != null) {
			attrValue = attr.getValue();
			_GroupName = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("propertyOrder");
		if (attr != null) {
			attrValue = attr.getValue();
			_PropertyOrder = Integer.parseInt(attrValue);
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			if (!(childNode instanceof org.w3c.dom.Element)) {
				continue;
			}
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			boolean recognized = readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes);
			if (!recognized) {
				// Found extra unrecognized childNode
			}
		}
	}

	protected boolean readNodeChild(org.w3c.dom.Node childNode, String childNodeName, String childNodeValue, java.util.Map namespacePrefixes) {
		// assert childNodeName == childNodeName.intern()
		if (childNodeName == "BuiltInCustomizer") {
			_BuiltInCustomizer = newBuiltInCustomizer();
			_BuiltInCustomizer.readNode(childNode, namespacePrefixes);
		}
		else if (childNodeName == "NewCustomizer") {
			_NewCustomizer = newNewCustomizer();
			_NewCustomizer.readNode(childNode, namespacePrefixes);
		}
		else {
			return false;
		}
		return true;
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "attributeName")
			setAttributeName((java.lang.String)value);
		else if (name == "groupName")
			setGroupName((java.lang.String)value);
		else if (name == "propertyOrder")
			setPropertyOrder(((java.lang.Integer)value).intValue());
		else if (name == "builtInCustomizer")
			setBuiltInCustomizer((BuiltInCustomizer)value);
		else if (name == "newCustomizer")
			setNewCustomizer((NewCustomizer)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Property");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "attributeName")
			return getAttributeName();
		if (name == "groupName")
			return getGroupName();
		if (name == "propertyOrder")
			return new java.lang.Integer(getPropertyOrder());
		if (name == "builtInCustomizer")
			return getBuiltInCustomizer();
		if (name == "newCustomizer")
			return getNewCustomizer();
		throw new IllegalArgumentException(name+" is not a valid property name for Property");
	}

	public String nameSelf() {
		return "Property";
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
			if (child == _AttributeName) {
				if (returnConstName) {
					return ATTRIBUTENAME;
				} else if (returnSchemaName) {
					return "attributeName";
				} else if (returnXPathName) {
					return "@attributeName";
				} else {
					return "AttributeName";
				}
			}
			if (child == _GroupName) {
				if (returnConstName) {
					return GROUPNAME;
				} else if (returnSchemaName) {
					return "groupName";
				} else if (returnXPathName) {
					return "@groupName";
				} else {
					return "GroupName";
				}
			}
		}
		if (childObj instanceof java.lang.Integer) {
			java.lang.Integer child = (java.lang.Integer) childObj;
			if (((java.lang.Integer)child).intValue() == _PropertyOrder) {
				if (returnConstName) {
					return PROPERTYORDER;
				} else if (returnSchemaName) {
					return "propertyOrder";
				} else if (returnXPathName) {
					return "@propertyOrder";
				} else {
					return "PropertyOrder";
				}
			}
		}
		if (childObj instanceof BuiltInCustomizer) {
			BuiltInCustomizer child = (BuiltInCustomizer) childObj;
			if (child == _BuiltInCustomizer) {
				if (returnConstName) {
					return BUILTINCUSTOMIZER;
				} else if (returnSchemaName) {
					return "BuiltInCustomizer";
				} else if (returnXPathName) {
					return "BuiltInCustomizer";
				} else {
					return "BuiltInCustomizer";
				}
			}
		}
		if (childObj instanceof NewCustomizer) {
			NewCustomizer child = (NewCustomizer) childObj;
			if (child == _NewCustomizer) {
				if (returnConstName) {
					return NEWCUSTOMIZER;
				} else if (returnSchemaName) {
					return "NewCustomizer";
				} else if (returnXPathName) {
					return "NewCustomizer";
				} else {
					return "NewCustomizer";
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
		if (_BuiltInCustomizer != null) {
			if (recursive) {
				_BuiltInCustomizer.childBeans(true, beans);
			}
			beans.add(_BuiltInCustomizer);
		}
		if (_NewCustomizer != null) {
			if (recursive) {
				_NewCustomizer.childBeans(true, beans);
			}
			beans.add(_NewCustomizer);
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.Property && equals((org.netbeans.modules.xml.wsdl.ui.property.model.Property) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.Property inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_AttributeName == null ? inst._AttributeName == null : _AttributeName.equals(inst._AttributeName))) {
			return false;
		}
		if (!(_GroupName == null ? inst._GroupName == null : _GroupName.equals(inst._GroupName))) {
			return false;
		}
		if (!(_PropertyOrder == inst._PropertyOrder)) {
			return false;
		}
		if (!(_BuiltInCustomizer == null ? inst._BuiltInCustomizer == null : _BuiltInCustomizer.equals(inst._BuiltInCustomizer))) {
			return false;
		}
		if (!(_NewCustomizer == null ? inst._NewCustomizer == null : _NewCustomizer.equals(inst._NewCustomizer))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_AttributeName == null ? 0 : _AttributeName.hashCode());
		result = 37*result + (_GroupName == null ? 0 : _GroupName.hashCode());
		result = 37*result + (_PropertyOrder);
		result = 37*result + (_BuiltInCustomizer == null ? 0 : _BuiltInCustomizer.hashCode());
		result = 37*result + (_NewCustomizer == null ? 0 : _NewCustomizer.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/wsdlui/property"
            xmlns:tns="http://xml.netbeans.org/schema/wsdlui/property"
            elementFormDefault="qualified">
    <xsd:element name="ElementProperties">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:PropertyGroup" maxOccurs="unbounded" />
                <xsd:element ref="tns:Property" maxOccurs="unbounded" />
            </xsd:sequence>
            <xsd:attribute name="elementName" type="xsd:string" use="required"/>
        </xsd:complexType>
    </xsd:element>            
    <xsd:element name="PropertyGroup">
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="groupOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="Property">
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <xsd:sequence/>
                        <xsd:attribute name="name" type="xsd:string"/>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element name="NewCustomizer">
                    <xsd:complexType>
                        <xsd:attribute name="className" type="xsd:string"/>
                    </xsd:complexType>
                </xsd:element>
            </xsd:choice>
            <xsd:attribute name="attributeName" type="xsd:string" use="required"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
