/**
 *	This generated bean class DynamicProperties
 *	matches the schema element 'dynamic-properties'.
 *
 *	Generated on Wed Sep 29 16:29:53 PDT 2004
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the bean graph.
 *
 * 	dynamicProperties <dynamic-properties> : DynamicProperties
 * 		propertyList <property-list> : PropertyList[0,n]
 * 			[attr: editable CDATA false]
 * 			[attr: description CDATA true]
 * 			[attr: bundle-path CDATA #IMPLIED ]
 * 			propertyName <property-name> : String
 * 			propertyParam <property-param> : PropertyParam[1,n]
 * 				paramName <param-name> : String
 * 				paramType <param-type> : ParamType
 * 					[attr: type CDATA text]
 * 					[attr: editable CDATA false]
 * 					[attr: required CDATA true]
 * 					| paramValue <param-value> : String[0,n]
 * 					| paramLocale <param-locale> : boolean
 * 					| 	EMPTY : String
 * 					| paramCharset <param-charset> : boolean
 * 					| 	EMPTY : String
 * 					paramMin <param-min> : String[0,1]
 * 					paramMax <param-max> : String[0,1]
 * 				paramLabel <param-label> : String[0,1]
 * 				paramValidator <param-validator> : String[0,1]
 * 				defaultValue <default-value> : String[0,1]
 * 				helpId <help-id> : String[0,1]
 * 				paramDescription <param-description> : String[0,1]
 * 			helpId <help-id> : String[0,1]
 * 		validator <validator> : Validator[0,n]
 * 			validatorName <validator-name> : String
 * 			validatorPattern <validator-pattern> : String
 *
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

public class DynamicProperties {
	public static final String PROPERTY_LIST = "PropertyList";	// NOI18N
	public static final String VALIDATOR = "Validator";	// NOI18N

	private java.util.List _PropertyList = new java.util.ArrayList();	// List<PropertyList>
	private java.util.List _Validator = new java.util.ArrayList();	// List<Validator>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public DynamicProperties() {
	}

	/**
	 * Deep copy
 	 */
	public DynamicProperties(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public DynamicProperties(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties source, boolean justData) {
		for (java.util.Iterator it = source._PropertyList.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList srcElement = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it.next();
			_PropertyList.add((srcElement == null) ? null : newPropertyList(srcElement, justData));
		}
		for (java.util.Iterator it = source._Validator.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator srcElement = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it.next();
			_Validator.add((srcElement == null) ? null : newValidator(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is an array, possibly empty
	public void setPropertyList(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList[] value) {
		if (value == null)
			value = new PropertyList[0];
		_PropertyList.clear();
		((java.util.ArrayList) _PropertyList).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_PropertyList.add(value[i]);
		}
	}

	public void setPropertyList(int index, org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList value) {
		_PropertyList.set(index, value);
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList[] getPropertyList() {
		PropertyList[] arr = new PropertyList[_PropertyList.size()];
		return (PropertyList[]) _PropertyList.toArray(arr);
	}

	public java.util.List fetchPropertyListList() {
		return _PropertyList;
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList getPropertyList(int index) {
		return (PropertyList)_PropertyList.get(index);
	}

	// Return the number of propertyList
	public int sizePropertyList() {
		return _PropertyList.size();
	}

	public int addPropertyList(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList value) {
		_PropertyList.add(value);
		int positionOfNewItem = _PropertyList.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePropertyList(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList value) {
		int pos = _PropertyList.indexOf(value);
		if (pos >= 0) {
			_PropertyList.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setValidator(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator[] value) {
		if (value == null)
			value = new Validator[0];
		_Validator.clear();
		((java.util.ArrayList) _Validator).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Validator.add(value[i]);
		}
	}

	public void setValidator(int index, org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator value) {
		_Validator.set(index, value);
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator[] getValidator() {
		Validator[] arr = new Validator[_Validator.size()];
		return (Validator[]) _Validator.toArray(arr);
	}

	public java.util.List fetchValidatorList() {
		return _Validator;
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator getValidator(int index) {
		return (Validator)_Validator.get(index);
	}

	// Return the number of validator
	public int sizeValidator() {
		return _Validator.size();
	}

	public int addValidator(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator value) {
		_Validator.add(value);
		int positionOfNewItem = _Validator.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeValidator(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator value) {
		int pos = _Validator.indexOf(value);
		if (pos >= 0) {
			_Validator.remove(pos);
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
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList newPropertyList() {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList newPropertyList(PropertyList source, boolean justData) {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator newValidator() {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator newValidator(Validator source, boolean justData) {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator(source, justData);
	}

	public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
		org.openide.filesystems.FileLock lock = fo.lock();
		try {
			java.io.OutputStream out = fo.getOutputStream(lock);
			write(out);
			out.close();
		} finally {
			lock.releaseLock();
		}
	}

	public void write(org.openide.filesystems.FileObject dir, String filename) throws java.io.IOException {
		org.openide.filesystems.FileObject file = dir.getFileObject(filename);
		if (file == null) {
			file = dir.createData(filename);
		}
		write(file);
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
		writeNode(out, "dynamic-properties", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "dynamic-properties";
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
		for (java.util.Iterator it = _PropertyList.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it.next();
			if (element != null) {
				element.writeNode(out, "property-list", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _Validator.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it.next();
			if (element != null) {
				element.writeNode(out, "validator", null, nextIndent, namespaceMap);
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

	public static DynamicProperties read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = fo.getInputStream();
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static DynamicProperties read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static DynamicProperties read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
 	 */
	public static DynamicProperties readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static DynamicProperties read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static DynamicProperties read(org.w3c.dom.Document document) {
		DynamicProperties aDynamicProperties = new DynamicProperties();
		aDynamicProperties.readFromDocument(document);
		return aDynamicProperties;
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
			if (childNodeName == "property-list") {
				PropertyList aPropertyList = newPropertyList();
				aPropertyList.readNode(childNode, namespacePrefixes);
				_PropertyList.add(aPropertyList);
			}
			else if (childNodeName == "validator") {
				Validator aValidator = newValidator();
				aValidator.readNode(childNode, namespacePrefixes);
				_Validator.add(aValidator);
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

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "propertyList")
			addPropertyList((PropertyList)value);
		else if (name == "propertyList[]")
			setPropertyList((PropertyList[]) value);
		else if (name == "validator")
			addValidator((Validator)value);
		else if (name == "validator[]")
			setValidator((Validator[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for DynamicProperties");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "propertyList[]")
			return getPropertyList();
		if (name == "validator[]")
			return getValidator();
		throw new IllegalArgumentException(name+" is not a valid property name for DynamicProperties");
	}

	public String nameSelf() {
		return "/DynamicProperties";
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
		if (childObj instanceof PropertyList) {
			PropertyList child = (PropertyList) childObj;
			int index = 0;
			for (java.util.Iterator it = _PropertyList.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it.next();
				if (child == element) {
					if (returnConstName) {
						return PROPERTY_LIST;
					} else if (returnSchemaName) {
						return "property-list";
					} else if (returnXPathName) {
						return "property-list[position()="+index+"]";
					} else {
						return "PropertyList."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof Validator) {
			Validator child = (Validator) childObj;
			int index = 0;
			for (java.util.Iterator it = _Validator.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it.next();
				if (child == element) {
					if (returnConstName) {
						return VALIDATOR;
					} else if (returnSchemaName) {
						return "validator";
					} else if (returnXPathName) {
						return "validator[position()="+index+"]";
					} else {
						return "Validator."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _PropertyList.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _Validator.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties && equals((org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties) o);
	}

	public boolean equals(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizePropertyList() != inst.sizePropertyList())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _PropertyList.iterator(), it2 = inst._PropertyList.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it.next();
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList element2 = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeValidator() != inst.sizeValidator())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Validator.iterator(), it2 = inst._Validator.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it.next();
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator element2 = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_PropertyList == null ? 0 : _PropertyList.hashCode());
		result = 37*result + (_Validator == null ? 0 : _Validator.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : dynamic-properties.dtd
    Created on : January 28, 2004, 8:48 PM
    Author     : Peter Williams
    Description:
        Purpose of the document follows.
		
	DTD for definition of properties, their editors and validators to allow for
	name/value pair property editing to be handled nicely in the plugin.
-->

<!-- The file is a list of property lists -->
<!ELEMENT dynamic-properties (property-list*, validator*)>

<!-- Each property list can be fixed or editable and has a name -->
<!ELEMENT property-list (property-name, property-param+, help-id?)>
<!ATTLIST property-list editable CDATA "false"
						description CDATA "true"
						bundle-path CDATA #IMPLIED>

<!ELEMENT property-name (#PCDATA)>

<!-- 
	Each element in a property list has a name.  It may also have a type, a
    validator, and possibly a min and/or max if the type is 'number'.  Lastly,
	it could have a helpId
-->
<!ELEMENT property-param (param-name, param-type, param-label?, param-validator?, default-value?, 
						  help-id?, param-description?)>

<!ELEMENT param-name (#PCDATA)>

<!-- 
	There are four allowed types: boolean, text, number, and list.  The editable
	attribute is ignored for all types except the list type.
	
	For boolean properties, default value should be string using the boolean
	  'ENTITY' definitions in the sun-xxx dtd's, preferably true/false.
	For text properties, you should provide a validator (or none to allow
	  arbitrary text) and a default value if desired.
	For number properties, the number is assumed to be a signed long integer.
	  Use the min & max params to specify a range if required.
	For list properties, use as many param-value entries as necessary to represent
	  the list.  If the list is editable, set the editable attribute on the type.
	  One exception here is if the list is the list of charsets or locales, use
	  param-locale or param-charset to specify this.  These lists are provided by
	  the Locale and Charset classes in the JVM.

	(I'm not actually defining ENTITY's here because Schema2Beans does not support them.) 
-->
<!ELEMENT param-type ((param-value* | param-locale | param-charset), param-min?, param-max?)>
<!ATTLIST param-type type CDATA "text" 
					 editable CDATA "false"
					 required CDATA "true">

<!ELEMENT param-value (#PCDATA)>
<!ELEMENT param-locale EMPTY>
<!ELEMENT param-charset EMPTY>
<!ELEMENT param-min (#PCDATA)>
<!ELEMENT param-max (#PCDATA)>

<!--
	The text label (actually, will become bundle string id) to be used for the 
	value field instead of the word 'Value'
-->
<!ELEMENT param-label (#PCDATA)>

<!--
	Validators are used to ensure the text in a text field matches a specific
	pattern.  The following validators are supported:  (Can I support java
	regular expression patterns here?  It would make it lots easier!!!)
	
		directory:	A directory path specification
		javaid:		A legal java identifier (allows java keywords though)
		url:		A URL string
		domain:		A domain.  This is probably similar to javaid + represents a server domain
		package:	A legal java package name, e.g. javaid's separated by periods.
		memorysize:	A number followed by kb or mb (case insensitive)
		classid:	A windows classid (GUID)
-->
<!ELEMENT param-validator (#PCDATA)>

<!--
	String that will become the default value for the property.  If the property
	value must fit a specific pattern, the default-value must qualify.
-->
<!ELEMENT default-value (#PCDATA)>

<!--
	The help id for this field (or panel if specified at the property level,
	which is likely what we'll do.
-->
<!ELEMENT help-id (#PCDATA)>


<!--
	ID of string in bundle (see property-list attributes) to use for default
	description.
-->
<!ELEMENT param-description (#PCDATA)>

<!--  !PW this would be used by property-param once it's done

	Version of the appserver this property-param belongs to.  Not present means
	the property is applicable to all versions.
	
	Allowable Strings:  major[.minor][pe|se|ee]
		Major version is require.
		Minor is optional (not present matches all)
		Type is optional (not present matches all)
		
	For range attribute, valid values are:
		ending, only, starting
<!ELEMENT appserver-version (#PCDATA)>
<!ATTLIST appserver-version range CDATA #IMPLIED>
-->

<!--
	Validator definition.  These are referred to by name from the <param-validator>
	entry in <property-param>, above.
-->
<!ELEMENT validator (validator-name, validator-pattern)>

<!ELEMENT validator-name (#PCDATA)>

<!ELEMENT validator-pattern (#PCDATA)>



*/
