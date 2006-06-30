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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xsd;

import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;

/**
 * ContentHandler impl for building XSD grammar
 * @author  anovak
 */
class XSDContentHandler implements ContentHandler {

    /** Stack for parsed elements */
    private List /*<Element>*/ elementsStack;
    /** All elements */
    private Map elements;
    /** All types */
    private Map types;
    
    private PrintStream ps;
    
    // namespace processing
    private boolean resolveNamespaces;
    private Map /* <String, Namespace> */ uri2Namespace;
    private Map /* <String, Namespace> */ prefix2Namespace;
    private Namespace schemaNamespace;
    private Namespace targetNamespace;
    
    /** Creates a new instance of XSDContentHandler */
    public XSDContentHandler(PrintStream ps) {
        this.ps = ps;
        this.elements = new HashMap();
        this.types = new HashMap();
        this.elementsStack = new ArrayList();
        this.resolveNamespaces = true;
        this.uri2Namespace = new HashMap();
        this.prefix2Namespace = new HashMap();
    }

    public XSDGrammar getGrammar() {
        return new XSDGrammar(elements, types, targetNamespace, schemaNamespace);
    }
    
    private void println(String s) {
        ps.println(s);
    }
    
    private void printlnAttributes(Attributes atts) {
        println("START Attributes");
        for (int i = 0; i < atts.getLength(); i++) {
            println("Attr[" + i + "] localname: " + atts.getLocalName(i) + " qname: " + atts.getQName(i) + " value: " + atts.getValue(i) + " URI: " + atts.getURI(i) + " type: " + atts.getType(i));
        }
        println("END Attributes");
    }
    
    
    private static void printlnElement(SchemaElement e, String prefix) {
        System.out.println(prefix + e.toString());
        Iterator it = e.getSubelements();
        while (it.hasNext()) {
            printlnElement((SchemaElement) it.next(), prefix + "    ");
        }
        
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    //    println("characters: " + new String(ch, start, length));
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
        System.out.println("Stack size: " + elementsStack.size());
        
        SchemaElement e = (SchemaElement) elementsStack.get(0);
        //printlnElement(e, "    ");
        
        // println("END Document");
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
        // println("endELement: " + namespaceURI + " name: " + localName + " qname: " + qName);
        // pop element
        elementsStack.remove(elementsStack.size() - 1);
    }
    
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
        // println("endPrefixMapping: " + prefix);
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(String target, String data) throws org.xml.sax.SAXException {
        // println("Processing instruction: " + target + " data: " + data);
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
    
    public void skippedEntity(String name) throws org.xml.sax.SAXException {
        // println("skippedEntity: " + name);
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
        elementsStack.add(SchemaElement.createSchemaElement(null, "TOP_LEVEL", null, null));
        // println("START Doc");
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
        if (resolveNamespaces) {
            resolveNamespaces = false;
            
            for (int i = 0; i < atts.getLength(); i++) {
                String name = atts.getQName(i);
                if (name.startsWith(Namespace.XMLNS_ATTR)) {
                    String uri = atts.getValue(i);
                    String prefix = Namespace.getSufix(name);
                    Namespace ns = new Namespace(uri, prefix);
                    if (prefix != null) {
                        this.prefix2Namespace.put(prefix, ns);
                    }
                    this.uri2Namespace.put(uri, ns);
                    System.err.println("NAMESPACE ADDED: " + prefix + " uri: " + uri);
                } else {
                    System.err.println("ATTR not taken: " + name + " xxx " + atts.getQName(i) + " xxx " + atts.getValue(i));
                }
            }
            
            // expect qName as xs:schema, xsd:schema and the likes or just schema
            String myprefix = Namespace.getPrefix(qName);
            Namespace xs = (Namespace) uri2Namespace.get(Namespace.XSD_SCHEMA_URI);
            assert xs != null : "Namespace http://www.w3.org/2001/XMLSchema not found";
            if (myprefix == xs.getPrefix() || myprefix.equals(xs.getPrefix())) {
                this.schemaNamespace = xs;
                String uri = atts.getValue("targetNamespace");
                this.targetNamespace = (Namespace) uri2Namespace.get(uri);
                assert targetNamespace != null;
                // OK
            } else {
                // ERRORneous schema
                assert false : "Unknown schema, prefix of schema element does not match http://www.w3.org/2001/XMLSchema namespace";
            }
        }
        
        SchemaElement e = SchemaElement.createSchemaElement(namespaceURI, qName, atts, schemaNamespace.getPrefix());
        System.err.println("ELEMENTS ADDING: " + atts.getValue("name") + " qname: " + qName);
        SchemaElement parent = (SchemaElement) elementsStack.get(elementsStack.size() - 1);
        if (parent != null && parent.getSAXAttributes() != null) {
            System.err.println("INTO: " + parent.getSAXAttributes().getValue("name"));
        }
        elements.put(atts.getValue("name"),  e);
        parent.addSubelement(e);
        // push
        elementsStack.add(e);
       // println("startElements: " + namespaceURI + "locName: " + localName + " qName: " + qName);
       // printlnAttributes(atts);
    }
    
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
        // println("startPrefixMapping: prefix:  " + prefix + " URI: " + uri);
    }    
}
