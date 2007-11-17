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
 *	This generated bean class BeanGraph
 *	matches the schema element 'bean-graph'.
 *
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	beanGraph <bean-graph> : BeanGraph
 * 		schemaTypeMapping <schema-type-mapping> : SchemaTypeMappingType[0,n]
 * 			schemaTypeNamespace <schema-type-namespace> : java.lang.String[0,1]
 * 			schemaTypeName <schema-type-name> : java.lang.String
 * 			javaType <java-type> : java.lang.String
 * 			root <root> : boolean[0,1]
 * 			bean <bean> : boolean[0,1]
 * 			canBeEmpty <can-be-empty> : boolean[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.schema2beansdev.beangraph;

public class BeanGraph implements org.netbeans.modules.schema2beansdev.beangraph.CommonBean {
	public static final String SCHEMA_TYPE_MAPPING = "SchemaTypeMapping";	// NOI18N

	private java.util.List _SchemaTypeMapping = new java.util.ArrayList();	// List<SchemaTypeMappingType>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public BeanGraph() {
	}

	/**
	 * Deep copy
	 */
	public BeanGraph(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public BeanGraph(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph source, boolean justData) {
		for (java.util.Iterator it = source._SchemaTypeMapping.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType srcElement = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it.next();
			_SchemaTypeMapping.add((srcElement == null) ? null : newSchemaTypeMappingType(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is an array, possibly empty
	public void setSchemaTypeMapping(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType[] value) {
		if (value == null)
			value = new SchemaTypeMappingType[0];
		_SchemaTypeMapping.clear();
		((java.util.ArrayList) _SchemaTypeMapping).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_SchemaTypeMapping.add(value[i]);
		}
	}

	public void setSchemaTypeMapping(int index, org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType value) {
		_SchemaTypeMapping.set(index, value);
	}

	public org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType[] getSchemaTypeMapping() {
		SchemaTypeMappingType[] arr = new SchemaTypeMappingType[_SchemaTypeMapping.size()];
		return (SchemaTypeMappingType[]) _SchemaTypeMapping.toArray(arr);
	}

	public java.util.List fetchSchemaTypeMappingList() {
		return _SchemaTypeMapping;
	}

	public org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType getSchemaTypeMapping(int index) {
		return (SchemaTypeMappingType)_SchemaTypeMapping.get(index);
	}

	// Return the number of schemaTypeMapping
	public int sizeSchemaTypeMapping() {
		return _SchemaTypeMapping.size();
	}

	public int addSchemaTypeMapping(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType value) {
		_SchemaTypeMapping.add(value);
		int positionOfNewItem = _SchemaTypeMapping.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeSchemaTypeMapping(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType value) {
		int pos = _SchemaTypeMapping.indexOf(value);
		if (pos >= 0) {
			_SchemaTypeMapping.remove(pos);
		}
		return pos;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType newSchemaTypeMappingType() {
		return new org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType newSchemaTypeMappingType(SchemaTypeMappingType source, boolean justData) {
		return new org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType(source, justData);
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "bean-graph", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "bean-graph";
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
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _SchemaTypeMapping.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it.next();
			if (element != null) {
				element.writeNode(out, "schema-type-mapping", null, nextIndent, namespaceMap);
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

	public static BeanGraph read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static BeanGraph read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static BeanGraph readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static BeanGraph read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static BeanGraph read(org.w3c.dom.Document document) {
		BeanGraph aBeanGraph = new BeanGraph();
		aBeanGraph.readFromDocument(document);
		return aBeanGraph;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
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
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
				it.hasNext(); ) {
				String prefix = (String) it.next();
				String ns = (String) namespacePrefixes.get(prefix);
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
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
			if (childNodeName == "schema-type-mapping") {
				SchemaTypeMappingType aSchemaTypeMapping = newSchemaTypeMappingType();
				aSchemaTypeMapping.readNode(childNode, namespacePrefixes);
				_SchemaTypeMapping.add(aSchemaTypeMapping);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public static class ValidateException extends Exception {
		private org.netbeans.modules.schema2beansdev.beangraph.CommonBean failedBean;
		private String failedPropertyName;
		private FailureType failureType;
		public ValidateException(String msg, String failedPropertyName, org.netbeans.modules.schema2beansdev.beangraph.CommonBean failedBean) {
			super(msg);
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public ValidateException(String msg, FailureType ft, String failedPropertyName, org.netbeans.modules.schema2beansdev.beangraph.CommonBean failedBean) {
			super(msg);
			this.failureType = ft;
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public String getFailedPropertyName() {return failedPropertyName;}
		public FailureType getFailureType() {return failureType;}
		public org.netbeans.modules.schema2beansdev.beangraph.CommonBean getFailedBean() {return failedBean;}
		public static class FailureType {
			private final String name;
			private FailureType(String name) {this.name = name;}
			public String toString() { return name;}
			public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
			public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
			public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
			public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
			public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
		}
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property schemaTypeMapping
		for (int _index = 0; _index < sizeSchemaTypeMapping(); ++_index) {
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element = getSchemaTypeMapping(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "schemaTypeMapping")
			addSchemaTypeMapping((SchemaTypeMappingType)value);
		else if (name == "schemaTypeMapping[]")
			setSchemaTypeMapping((SchemaTypeMappingType[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for BeanGraph");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "schemaTypeMapping[]")
			return getSchemaTypeMapping();
		throw new IllegalArgumentException(name+" is not a valid property name for BeanGraph");
	}

	public String nameSelf() {
		return "/BeanGraph";
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
		if (childObj instanceof SchemaTypeMappingType) {
			SchemaTypeMappingType child = (SchemaTypeMappingType) childObj;
			int index = 0;
			for (java.util.Iterator it = _SchemaTypeMapping.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it.next();
				if (child == element) {
					if (returnConstName) {
						return SCHEMA_TYPE_MAPPING;
					} else if (returnSchemaName) {
						return "schema-type-mapping";
					} else if (returnXPathName) {
						return "schema-type-mapping[position()="+index+"]";
					} else {
						return "SchemaTypeMapping."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.schema2beansdev.beangraph.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.schema2beansdev.beangraph.CommonBean[] result = new org.netbeans.modules.schema2beansdev.beangraph.CommonBean[children.size()];
		return (org.netbeans.modules.schema2beansdev.beangraph.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
		for (java.util.Iterator it = _SchemaTypeMapping.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.beangraph.BeanGraph && equals((org.netbeans.modules.schema2beansdev.beangraph.BeanGraph) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizeSchemaTypeMapping() != inst.sizeSchemaTypeMapping())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _SchemaTypeMapping.iterator(), it2 = inst._SchemaTypeMapping.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it.next();
			org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType element2 = (org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_SchemaTypeMapping == null ? 0 : _SchemaTypeMapping.hashCode());
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

<?xml version="1.0" ?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
  <xsd:element name="bean-graph">
	<xsd:complexType>
	  <xsd:sequence>
		<xsd:element name="schema-type-mapping" type="schemaTypeMappingType"
		  minOccurs="0" maxOccurs="unbounded"/>
	  </xsd:sequence>
	</xsd:complexType>
  </xsd:element>

  <xsd:complexType name="schemaTypeMappingType">
	<xsd:annotation>
	  <xsd:documentation>
		Map between schema types and java types.
	  </xsd:documentation>
	</xsd:annotation>
	<xsd:sequence>
	  <xsd:element name="schema-type-namespace" type="xsd:string" minOccurs="0"/>
	  <xsd:element name="schema-type-name" type="xsd:string">
		<xsd:annotation>
		  <xsd:documentation>
			The schema type; for instance, "string"
		  </xsd:documentation>
		</xsd:annotation>
	  </xsd:element>
	  <xsd:element name="java-type" type="xsd:string">
		<xsd:annotation>
		  <xsd:documentation>
			The java type; for instance, "java.lang.String", or "int"
		  </xsd:documentation>
		</xsd:annotation>
	  </xsd:element>
	  <xsd:element name="root" type="xsd:boolean" minOccurs="0"/>
	  <xsd:element name="bean" type="xsd:boolean" minOccurs="0"/>
	  <xsd:element name="can-be-empty" type="xsd:boolean" minOccurs="0"/>
	</xsd:sequence>
  </xsd:complexType>
</xsd:schema>

*/
