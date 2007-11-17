/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/**
 *	This generated bean class MetaProperty
 *	matches the schema element 'meta-property'.
 *  The root bean class is MetaDD
 *
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.metadd;

public class MetaProperty implements org.netbeans.modules.schema2beansdev.metadd.CommonBean {
	public static final String BEAN_NAME = "BeanName";	// NOI18N
	public static final String DEFAULT_VALUE = "DefaultValue";	// NOI18N
	public static final String KNOWN_VALUE = "KnownValue";	// NOI18N
	public static final String KEY = "Key";	// NOI18N
	public static final String VETOABLE = "Vetoable";	// NOI18N

	private String _BeanName;
	private java.util.List _DefaultValue = new java.util.ArrayList();	// List<String>
	private java.util.List _KnownValue = new java.util.ArrayList();	// List<String>
	private boolean _Key;
	private boolean _isSet_Key = false;
	private boolean _Vetoable;
	private boolean _isSet_Vetoable = false;

	/**
	 * Normal starting point constructor.
	 */
	public MetaProperty() {
		_BeanName = "";
	}

	/**
	 * Required parameters constructor
	 */
	public MetaProperty(String beanName) {
		_BeanName = beanName;
	}

	/**
	 * Deep copy
	 */
	public MetaProperty(org.netbeans.modules.schema2beansdev.metadd.MetaProperty source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public MetaProperty(org.netbeans.modules.schema2beansdev.metadd.MetaProperty source, boolean justData) {
		_BeanName = source._BeanName;
		for (java.util.Iterator it = source._DefaultValue.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_DefaultValue.add(srcElement);
		}
		for (java.util.Iterator it = source._KnownValue.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_KnownValue.add(srcElement);
		}
		_Key = source._Key;
		_isSet_Key = source._isSet_Key;
		_Vetoable = source._Vetoable;
		_isSet_Vetoable = source._isSet_Vetoable;
	}

	// This attribute is mandatory
	public void setBeanName(String value) {
		_BeanName = value;
	}

	public String getBeanName() {
		return _BeanName;
	}

	// This attribute is an array, possibly empty
	public void setDefaultValue(String[] value) {
		if (value == null)
			value = new String[0];
		_DefaultValue.clear();
		((java.util.ArrayList) _DefaultValue).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DefaultValue.add(value[i]);
		}
	}

	public void setDefaultValue(int index, String value) {
		_DefaultValue.set(index, value);
	}

	public String[] getDefaultValue() {
		String[] arr = new String[_DefaultValue.size()];
		return (String[]) _DefaultValue.toArray(arr);
	}

	public java.util.List fetchDefaultValueList() {
		return _DefaultValue;
	}

	public String getDefaultValue(int index) {
		return (String)_DefaultValue.get(index);
	}

	// Return the number of defaultValue
	public int sizeDefaultValue() {
		return _DefaultValue.size();
	}

	public int addDefaultValue(String value) {
		_DefaultValue.add(value);
		int positionOfNewItem = _DefaultValue.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDefaultValue(String value) {
		int pos = _DefaultValue.indexOf(value);
		if (pos >= 0) {
			_DefaultValue.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setKnownValue(String[] value) {
		if (value == null)
			value = new String[0];
		_KnownValue.clear();
		((java.util.ArrayList) _KnownValue).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_KnownValue.add(value[i]);
		}
	}

	public void setKnownValue(int index, String value) {
		_KnownValue.set(index, value);
	}

	public String[] getKnownValue() {
		String[] arr = new String[_KnownValue.size()];
		return (String[]) _KnownValue.toArray(arr);
	}

	public java.util.List fetchKnownValueList() {
		return _KnownValue;
	}

	public String getKnownValue(int index) {
		return (String)_KnownValue.get(index);
	}

	// Return the number of knownValue
	public int sizeKnownValue() {
		return _KnownValue.size();
	}

	public int addKnownValue(String value) {
		_KnownValue.add(value);
		int positionOfNewItem = _KnownValue.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeKnownValue(String value) {
		int pos = _KnownValue.indexOf(value);
		if (pos >= 0) {
			_KnownValue.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setKey(boolean value) {
		_Key = value;
		_isSet_Key = true;
	}

	public boolean isKey() {
		return _Key;
	}

	// This attribute is optional
	public void setVetoable(boolean value) {
		_Vetoable = value;
		_isSet_Vetoable = true;
	}

	public boolean isVetoable() {
		return _Vetoable;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "meta-property";
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
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_BeanName != null) {
			out.write(nextIndent);
			out.write("<bean-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, _BeanName, false);
			out.write("</bean-name>\n");	// NOI18N
		}
		for (java.util.Iterator it = _DefaultValue.iterator(); 
			it.hasNext(); ) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<default-value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</default-value>\n");	// NOI18N
			}
		}
		for (java.util.Iterator it = _KnownValue.iterator(); it.hasNext(); 
			) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<known-value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.metadd.MetaDD.writeXML(out, element, false);
				out.write("</known-value>\n");	// NOI18N
			}
		}
		if (_isSet_Key) {
			if (_Key) {
				out.write(nextIndent);
				out.write("<key");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		if (_isSet_Vetoable) {
			if (_Vetoable) {
				out.write(nextIndent);
				out.write("<vetoable");	// NOI18N
				out.write("/>\n");	// NOI18N
			}
		}
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
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
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "bean-name") {
				_BeanName = childNodeValue;
			}
			else if (childNodeName == "default-value") {
				String aDefaultValue;
				aDefaultValue = childNodeValue;
				_DefaultValue.add(aDefaultValue);
			}
			else if (childNodeName == "known-value") {
				String aKnownValue;
				aKnownValue = childNodeValue;
				_KnownValue.add(aKnownValue);
			}
			else if (childNodeName == "key") {
				if (childNode.getFirstChild() == null)
					_Key = true;
				else
					_Key = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
				_isSet_Key = true;
			}
			else if (childNodeName == "vetoable") {
				if (childNode.getFirstChild() == null)
					_Vetoable = true;
				else
					_Vetoable = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
				_isSet_Vetoable = true;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property beanName
		if (getBeanName() == null) {
			throw new org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException("getBeanName() == null", org.netbeans.modules.schema2beansdev.metadd.MetaDD.ValidateException.FailureType.NULL_VALUE, "beanName", this);	// NOI18N
		}
		// Validating property defaultValue
		// Validating property knownValue
		// Validating property key
		// Validating property vetoable
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "beanName")
			setBeanName((String)value);
		else if (name == "defaultValue")
			addDefaultValue((String)value);
		else if (name == "defaultValue[]")
			setDefaultValue((String[]) value);
		else if (name == "knownValue")
			addKnownValue((String)value);
		else if (name == "knownValue[]")
			setKnownValue((String[]) value);
		else if (name == "key")
			setKey(((java.lang.Boolean)value).booleanValue());
		else if (name == "vetoable")
			setVetoable(((java.lang.Boolean)value).booleanValue());
		else
			throw new IllegalArgumentException(name+" is not a valid property name for MetaProperty");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "beanName")
			return getBeanName();
		if (name == "defaultValue[]")
			return getDefaultValue();
		if (name == "knownValue[]")
			return getKnownValue();
		if (name == "key")
			return (isKey() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "vetoable")
			return (isVetoable() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		throw new IllegalArgumentException(name+" is not a valid property name for MetaProperty");
	}

	public String nameSelf() {
		return "MetaProperty";
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
			if (child == _BeanName) {
				if (returnConstName) {
					return BEAN_NAME;
				} else if (returnSchemaName) {
					return "bean-name";
				} else if (returnXPathName) {
					return "bean-name";
				} else {
					return "BeanName";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _DefaultValue.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DEFAULT_VALUE;
					} else if (returnSchemaName) {
						return "default-value";
					} else if (returnXPathName) {
						return "default-value[position()="+index+"]";
					} else {
						return "DefaultValue."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _KnownValue.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child == element) {
					if (returnConstName) {
						return KNOWN_VALUE;
					} else if (returnSchemaName) {
						return "known-value";
					} else if (returnXPathName) {
						return "known-value[position()="+index+"]";
					} else {
						return "KnownValue."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _Key) {
				if (returnConstName) {
					return KEY;
				} else if (returnSchemaName) {
					return "key";
				} else if (returnXPathName) {
					return "key";
				} else {
					return "Key";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Vetoable) {
				if (returnConstName) {
					return VETOABLE;
				} else if (returnSchemaName) {
					return "vetoable";
				} else if (returnXPathName) {
					return "vetoable";
				} else {
					return "Vetoable";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.schema2beansdev.metadd.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.schema2beansdev.metadd.CommonBean[] result = new org.netbeans.modules.schema2beansdev.metadd.CommonBean[children.size()];
		return (org.netbeans.modules.schema2beansdev.metadd.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.metadd.MetaProperty && equals((org.netbeans.modules.schema2beansdev.metadd.MetaProperty) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.metadd.MetaProperty inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_BeanName == null ? inst._BeanName == null : _BeanName.equals(inst._BeanName))) {
			return false;
		}
		if (sizeDefaultValue() != inst.sizeDefaultValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DefaultValue.iterator(), it2 = inst._DefaultValue.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeKnownValue() != inst.sizeKnownValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _KnownValue.iterator(), it2 = inst._KnownValue.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (_isSet_Key != inst._isSet_Key) {
			return false;
		}
		if (_isSet_Key) {
			if (!(_Key == inst._Key)) {
				return false;
			}
		}
		if (_isSet_Vetoable != inst._isSet_Vetoable) {
			return false;
		}
		if (_isSet_Vetoable) {
			if (!(_Vetoable == inst._Vetoable)) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_BeanName == null ? 0 : _BeanName.hashCode());
		result = 37*result + (_DefaultValue == null ? 0 : _DefaultValue.hashCode());
		result = 37*result + (_KnownValue == null ? 0 : _KnownValue.hashCode());
		result = 37*result + (_isSet_Key ? 0 : (_Key ? 0 : 1));
		result = 37*result + (_isSet_Vetoable ? 0 : (_Vetoable ? 0 : 1));
		return result;
	}

	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		try {
			writeNode(sw);
		} catch (java.io.IOException e) {
			// How can we actually get an IOException on a StringWriter?
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

}


/*
		The following schema file has been used for generation:

<!-- This holds data about the schema that is not part of DTDs and/or XML Schemas.
-->

<!--
-->
<!ELEMENT metaDD (meta-element*, implements?, extends?, import*, vetoable?, throw-exceptions?, schemaLocation?, finder*)>

<!--
-->
<!ELEMENT meta-element (dtd-name, namespace?, bean-name?, bean-class?, wrapper-class?, default-value*, known-value*, meta-property*, comparator-class*, implements?, extends?, import*, user-code?, vetoable?, skip-generation?, delegator-name?, delegator-extends?, bean-interface-extends?, can-be-empty?>

<!--
-->
<!ELEMENT meta-property (bean-name, default-value*, known-value*, key?, vetoable?)>

<!ELEMENT delegator-name (#PCDATA)>

<!--
-->
<!ELEMENT implements (#PCDATA)>

<!--
-->
<!ELEMENT extends (#PCDATA)>

<!--
-->
<!ELEMENT import (#PCDATA)>

<!--
-->
<!ELEMENT dtd-name (#PCDATA)>

<!ELEMENT namespace (#PCDATA)>

<!--
-->
<!ELEMENT default-value (#PCDATA)>

<!--
-->
<!ELEMENT skip-generation EMPTY>

<!--
-->
<!ELEMENT key EMPTY>

<!--
-->
<!ELEMENT vetoable EMPTY>

<!--
-->
<!ELEMENT known-value (#PCDATA)>

<!--
-->
<!ELEMENT bean-name (#PCDATA)>

<!--
-->
<!ELEMENT bean-class (#PCDATA)>

<!--
-->
<!ELEMENT wrapper-class (#PCDATA)>

<!--
-->
<!ELEMENT comparator-class (#PCDATA)>

<!--
-->
<!ELEMENT user-code (#PCDATA)>

<!ELEMENT throw-exceptions EMPTY>

<!-- Automatically set the schemaLocation -->
<!ELEMENT schemaLocation (#PCDATA)>

<!ELEMENT finder (#PCDATA)>

<!ELEMENT bean-interface-extends (#PCDATA)>

<!ELEMENT can-be-empty EMPTY>

*/
