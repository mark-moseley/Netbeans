/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.dtd.grammar;

import java.io.*;
import java.util.*;
import java.util.StringTokenizer;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import org.openide.xml.*;
import org.openide.util.Lookup;

import org.netbeans.api.xml.parsers.SAXEntityParser;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Produces {@link DTDGrammar} from passed SAX declaration handler events.
 *
 * @author  Petr Kuzel
 * @author  asgeir@dimonsoftware.com
 */
public class DTDParser {

    static final String SAX_PROPERTY = "http://xml.org/sax/properties/";  //NOI18N
    static final String DECL_HANDLER = "declaration-handler"; //NOI18N
    
    /** If true, the InputSource parameter of the parse method is expected to be a
     * DTD document, otherwise if is expected to be a XML document. */
    private boolean dtdOnly;
    
    /** Creates new DTDParser 
     * The InputSource parameter of the parse method should be a XML document
     */
    public DTDParser() {
        this(false);
    }

    /** Creates new DTDParser 
     * @param dtdOnly If true the InputSource parameter into the parse method
     *                should be a DTD document, otherwise it should be an XML
     *                document.
     */
    public DTDParser(boolean dtdOnly) {
        this.dtdOnly = dtdOnly;
    }
    
    public DTDGrammar parse(InputSource in) {

        Handler handler = new Handler();
        
        try {
            XMLReader parser = XMLUtil.createXMLReader(dtdOnly == false);   // we do not want Crimson, it does not understand relative SYSTEM ids
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            parser.setDTDHandler(handler);
            
            UserCatalog catalog = UserCatalog.getDefault();
            EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
            
            if (res != null) parser.setEntityResolver(res);
            parser.setProperty(SAX_PROPERTY + DECL_HANDLER, handler);
            
            if (dtdOnly) {
                new SAXEntityParser(parser, false).parse(in);
            } else {
                parser.parse(in);
            }
            throw new IllegalStateException("How we can get here?");
        } catch (Stop stop) {
            return handler.getDTDGrammar();  // expected
        } catch (SAXException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml") ||  Boolean.getBoolean("netbeans.debug.exceptions")) {  //NOI18N
                ex.printStackTrace();            
                if (ex.getException() instanceof RuntimeException) {
                    ex.getException().printStackTrace();  //???
                }            
            }
            return handler.getDTDGrammar();  // better partial result than nothing
        } catch (IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml")) {  // NOI18N
                ex.printStackTrace();
            }
            return handler.getDTDGrammar();  // better partial result than nothing
        }                
    }

    /**
     * Actually create a grammar from callback information.
     */
    private class Handler extends DefaultHandler implements DeclHandler {

        private Map attrs, elements, models, enums, attrDefaults;
        private Set notations, entities, anys;
        private DTDGrammar dtd;
        
        Handler() {
            attrs = new HashMap();
            elements = new HashMap();
            models = new HashMap();
            notations = new TreeSet();
            entities = new TreeSet();
            anys = new HashSet();
            enums = new HashMap();
            attrDefaults = new HashMap();
            dtd = new DTDGrammar(elements, models, attrs, attrDefaults, enums, entities, notations);
        }

        /**
         * Update value of ANY declared content models
         */
        DTDGrammar getDTDGrammar() {
            Iterator it = anys.iterator();
            while (it.hasNext()) {
                String name = (String) it.next();
                elements.put(name, elements.keySet());
            }            
            
            return dtd;
        }
        
        public void elementDecl(String name, String model) throws SAXException {

            // special cases
            
            if ("ANY".equals(model)) {
                anys.add(name);
                elements.put(name, Collections.EMPTY_SET);  // see anys resolving
                return;
            } else if ("EMPTY".equals(model)) {
                elements.put(name, Collections.EMPTY_SET);
                return;                
            } else if ("(#PCDATA)".equals(model)) {
                elements.put(name, Collections.EMPTY_SET);
                return;
            }

            // parse content model
            
            StringTokenizer tokenizer = new StringTokenizer(model, " \t\n|,()?+*");
            Set modelset = new TreeSet();
            while (tokenizer.hasMoreTokens()) {
                String next = tokenizer.nextToken().trim();
                if ("#PCDATA".equals(next)) continue;
                modelset.add(next);
            }
            
            elements.put(name, modelset);
            models.put(name, model);
        }
        
        public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
            if (name.startsWith("%")) return;  // NOI18N
            entities.add(name);
        }
                
        public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) throws SAXException {
            Set set = (Set) attrs.get(eName);
            if (set == null) {
                set = new TreeSet();
                attrs.put(eName, set);
            }             
            set.add(aName);
            
            // if enumeration type place into enumeration map new entry
            if (type != null && type.startsWith("(")) {
                StringTokenizer tokenizer = new StringTokenizer(type, "()|", false);
                List tokens = new ArrayList(7);
                while (tokenizer.hasMoreTokens()) {
                    tokens.add(tokenizer.nextToken());
                }
                enums.put(eName + " " + aName, tokens);                         // NOI18N
            }

            // store defaults
            String key = eName + " " + aName;                                   // NOI18N
            attrDefaults.put(key, valueDefault);            
        }
        
        public void internalEntityDecl(String name, String value) throws SAXException {
            if (name.startsWith("%")) return;  // NOI18N
            entities.add(name);
        }
        
        public void notationDecl (String name, String publicId, String systemId) throws SAXException {
            notations.add(name);
        }                
        
        public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            throw new Stop();
        }
        
    }

    
    private class Stop extends SAXException {
        
        private static final long serialVersionUID = -6466279601744402792L;
        
        Stop() {
            super("STOP");  //NOI18N
        }
        
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
