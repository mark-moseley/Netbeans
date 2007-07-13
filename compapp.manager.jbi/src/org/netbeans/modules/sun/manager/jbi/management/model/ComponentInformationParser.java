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

package org.netbeans.modules.sun.manager.jbi.management.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parses the XML response received from the backend regarding the
 * status of JBI components installed on the JBI Container on the Server.
 *
 * @author Graj
 */
public class ComponentInformationParser extends DefaultHandler implements Serializable {

    //  Private members needed to parse the XML document
    private boolean parsingInProgress; // keep track of parsing

    private Stack<String> qNameStack = new Stack<String>(); // keep track of QName

    JBIComponentStatus component = null;

    JBIComponentDocument container = new JBIComponentDocument();

    Map<String, String> namespaceMap = new HashMap<String, String>();

    /**
     *
     */
    public ComponentInformationParser() {
        super();
        // TODO Auto-generated constructor stub
    }



    /**
     * @return Returns the document.
     */
    public JBIComponentDocument getDocument() {
        return this.container;
    }
    /**
     * @param document The document to set.
     */
    public void setDocument(JBIComponentDocument jbiDocument) {
        this.container = jbiDocument;
    }

    public static JBIComponentDocument parse(String documentString) throws IOException, SAXException, FileNotFoundException,
    ParserConfigurationException, SAXException {
        JBIComponentDocument container = null;
        if(documentString == null) {
            return container;
        }
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();

        StringReader reader = new StringReader(documentString);

        // Create an InputSource from the Reader
        InputSource inputSource = new InputSource(reader);
        // Parse the input XML document stream, using my event handler
        ComponentInformationParser myEventHandler = new ComponentInformationParser();
        if(inputSource != null) {
            saxParser.parse(inputSource, myEventHandler);
            container = myEventHandler.getDocument();
        }
        return container;
    }


    public static JBIComponentDocument parse(File documentFile) throws IOException, SAXException, FileNotFoundException,
    ParserConfigurationException, SAXException {
        JBIComponentDocument container = null;
        // Get an instance of the SAX parser factory
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // Get an instance of the SAX parser
        SAXParser saxParser = factory.newSAXParser();

        FileReader reader = new FileReader(documentFile);

        // Create an InputSource from the Reader
        InputSource inputSource = new InputSource(reader);
        // Parse the input XML document stream, using my event handler
        ComponentInformationParser myEventHandler = new ComponentInformationParser();
        saxParser.parse(inputSource, myEventHandler);
        container = myEventHandler.getDocument();
        return container;
    }

    /**
     * Start of document processing.
     *
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void startDocument() throws SAXException {
        parsingInProgress = true;
        qNameStack.removeAllElements();
        container.getJbiComponentList().clear();
    }

    /**
     * End of document processing.
     *
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void endDocument() throws SAXException {
        parsingInProgress = false;
        // We have encountered the end of the document. Do any processing that
        // is desired, for example dump all collected element2 values.
//        for (int i = 0; i < jbiComponentList.size(); i++) {
//            JBIComponentStatus o = (JBIComponentStatus) jbiComponentList.get(i);
//            o.dump();
//        }
//        container.setJbiComponentList(this.jbiComponentList);
    }

    /**
     * Process the new element.
     *
     * @param uri
     *            is the Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            is the The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            is the qualified name (with prefix), or the empty string if
     *            qualified names are not available.
     * @param attributes
     *            is the attributes attached to the element. If there are no
     *            attributes, it shall be an empty Attributes object.
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (JBIComponentDocument.COMP_INFO_LIST_NODE_NAME.equals(qName)) {
            String key = null, value = null;
            for (int index = 0; index < attributes.getLength(); index++) {
                key = (String) attributes.getQName(index);
                if (key != null) {
                    value = (String) attributes.getValue(key);
                    if (value != null) {
                        namespaceMap.put(key, value);
                    }
                }
            }
        } else {
            if (JBIComponentDocument.COMP_INFO_NODE_NAME.equals(qName)) {
                component = new JBIComponentStatus();
                String key = null, value = null;
                for (int index = 0; index < attributes.getLength(); index++) {
                    key = (String) attributes.getQName(index);
                    if (key != null) {
                        value = (String) attributes.getValue(key);
                        if (value != null) {
                            if(key.equals("type") == true) { // NOI18N
                                component.setType(value);
                            }
                            if(key.equals("name") == true) { // NOI18N
                                component.setName(value);
                            }
                            if(key.equals("state") == true) { // NOI18N
                                component.setState(value);
                            }
                        }
                    }
                }
            }
        }
        //     Keep track of QNames
        qNameStack.push(qName);
    }

    /**
     * Process the character data for current tag.
     *
     * @param ch
     *            are the element's characters.
     * @param start
     *            is the start position in the character array.
     * @param length
     *            is the number of characters to use from the character array.
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String qName;
        String chars = new String(ch, start, length);
        //  Get current QName
        qName = (String) qNameStack.peek();
        if (JBIComponentDocument.COMP_INFO_LIST_NODE_NAME.equals(qName)) {
            //  Nothing to process
        }
        if (JBIComponentDocument.COMP_INFO_NODE_NAME.equals(qName)) {
            //  Keep track of the value of element2
//            if (jbiComponentList.size() > 0) {
                // JBIComponentStatus o = (JBIComponentStatus) jbiComponentList.lastElement();
                // o.setValue(chars);
//            }
        }
        if (JBIComponentDocument.DESCRIPTION_NODE_NAME.equals(qName)) {
            if((component != null) && (chars != null)) {
                component.setDescription(chars.trim());
            }
        }
//        if (JBIComponentDocument.ID_NODE_NAME.equals(qName)) {
//            if((component != null) && (chars != null)) {
//                component.setComponentId(chars);
//            }
//        }
        if (JBIComponentDocument.NAME_NODE_NAME.equals(qName)) {
            if((component != null) && (chars != null)) {
                component.setName(chars);
            }
        }
        if (JBIComponentDocument.STATUS_NODE_NAME.equals(qName)) {
            if((component != null) && (chars != null)) {
                component.setState(chars);
            }
        }
        if (JBIComponentDocument.TYPE_NODE_NAME.equals(qName)) {
            if((component != null) && (chars != null)) {
                component.setType(chars);
            }
        }
    }

    /**
     * Process the end element tag.
     *
     * @param uri
     *            is the Namespace URI, or the empty string if the element has
     *            no Namespace URI or if Namespace processing is not being
     *            performed.
     * @param localName
     *            is the The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param qName
     *            is the qualified name (with prefix), or the empty string if
     *            qualified names are not available.
     * @throws org.xml.sax.SAXException
     *             is any SAX exception, possibly wrapping another exception.
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        //  Pop QName, since we are done with it
        qNameStack.pop();
        if (JBIComponentDocument.COMP_INFO_LIST_NODE_NAME.equals(qName)) {
            //  We have encountered the end of
            // JBIComponentDocument.COMP_INFO_LIST_NODE_NAME
            //  ...
        } else {
            if (JBIComponentDocument.COMP_INFO_NODE_NAME.equals(qName)) {
                //  We have encountered the end of
                // JBIComponentDocument.COMP_INFO_NODE_NAME
                //  ...
                //this.component.dump();
                this.container.getJbiComponentList().add(this.component);
                this.component = null;
            }
        }
    }


    public static void main(String[] args) {
    }
}
