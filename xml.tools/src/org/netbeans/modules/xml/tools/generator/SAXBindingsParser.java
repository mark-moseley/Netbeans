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
package org.netbeans.modules.xml.tools.generator;

import org.xml.sax.*;

/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into SAXBindingsHandler events.
 * <p>Usage sample:
 * <pre>
 *    SAXBindingsParser parser = new SAXBindingsParser(...);
 *    parser.parse(new InputSource("..."));
 * </pre>
 * <p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>
 */
public class SAXBindingsParser extends org.xml.sax.helpers.DefaultHandler {

    private java.lang.StringBuffer buffer;
    
    private SAXBindingsHandler handler;
    
    private java.util.Stack context;
        
    public SAXBindingsParser(final SAXBindingsHandler handler) {
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new java.util.Stack();
    }
    
    public void setDocumentLocator(Locator locator) {
    }
    
    
    public void startElement(String ns, String ln, String name, Attributes attrs) throws SAXException {
        dispatch(true);
        context.push(new Object[] {name, new org.xml.sax.helpers.AttributesImpl(attrs)});
        if ("parslet".equals(name)) { // NOI18N
            handler.handle_parslet(attrs);
        } else if ("SAX-bindings".equals(name)) { // NOI18N
            handler.start_SAX_bindings(attrs);
        } else if ("attbind".equals(name)) { // NOI18N
            handler.handle_attbind(attrs);
        } else if ("bind".equals(name)) { // NOI18N
            handler.start_bind(attrs);
        }
    }
    
    public void endElement(String ns, String ln, java.lang.String name) throws SAXException {
        dispatch(false);
        context.pop();
        if ("SAX-bindings".equals(name)) { // NOI18N
            handler.end_SAX_bindings();
        } else if ("bind".equals(name)) { // NOI18N
            handler.end_bind();
        }
    }
    
    public void characters(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }
    
    public void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
    }
    
    public void processingInstruction(java.lang.String target, java.lang.String data) throws SAXException {
    }
    
    private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it
        
        Object[] ctx = (Object[]) context.peek();
        String here = (String) ctx[0];
        Attributes attrs = (Attributes) ctx[1];
        buffer.delete(0, buffer.length());
    }
    
    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final InputSource input) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(input, this);
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final java.net.URL url) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new InputSource(url.toExternalForm()), this);
    }
    
    /**
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final InputSource input, final SAXBindingsHandler handler) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(input, new SAXBindingsParser(handler));
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final java.net.URL url, final SAXBindingsHandler handler) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new InputSource(url.toExternalForm()), handler);
    }
    
    private static void parse(final InputSource input, final SAXBindingsParser recognizer) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(true);  //the code was generated according DTD
        factory.setNamespaceAware(false);  //the code was generated according DTD
        XMLReader parser = factory.newSAXParser().getXMLReader();
        parser.setContentHandler(recognizer);
        parser.setErrorHandler(recognizer.getDefaultErrorHandler());
        parser.parse(input);
    }
    
    private ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            public void error(SAXParseException ex) throws SAXException  {
                if (context.isEmpty()) {
                    if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Missing DOCTYPE."); // NOI18N
                }
                throw ex;
            }
            
            public void fatalError(SAXParseException ex) throws SAXException {
                throw ex;
            }
            
            public void warning(SAXParseException ex) throws SAXException {
                // ignore
            }
        };
        
    }
    
}
