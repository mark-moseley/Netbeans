/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.*;
import java.lang.ref.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;

import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.lookup.*;
import org.openide.xml.*;
import org.openide.*;

/**
 * This source represents a <b>XML rules</b> core plugin to <tt>MIMEReolverImpl</tt>.
 *
 * @author  Petr Kuzel
 * @version 
 */
final class XMLMIMEComponent extends DefaultParser implements MIMEComponent {

    private short state = INIT;
    
    // template obtained form parsed description
    private final Smell template = new Smell();

    // cached and reused parser used for sniffing    
    private static final LocalSniffingParser local = new LocalSniffingParser();

    // FileObjectFilter ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public boolean acceptFileObject(FileObject fo) {

        // it may come from arbitrary thread
        // retrive per thread instance
        
        SniffingParser sniffer = (SniffingParser) local.get();
        Smell print = sniffer.sniff(fo);
//        System.err.println("Print of " + fo);
//        System.err.println("print " + print);
//        System.err.println("template " + template);
        boolean res = template.match(print);
//        System.err.println("Match " + res);
        return res;
    }

    public String toString() {
       return template.toString();
    }

    // XML description -> memory representation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    // pseudo validation states
    private static final short INIT = 0;
    private static final short IN_ROOT = 1;
    private static final short IN_DOCTYPE = 2;
    private static final short IN_ELEMENT = 3;

    // grammar elements
    private static final String ROOT = "xml-rule"; // NOI18N
    private static final String PI = "pi"; // NOI18N
    private static final String ELEMENT = "element"; // NOI18N
    private static final String DOCTYPE  = "doctype"; // NOI18N
    private static final String PUBLIC_ID = "public-id"; // NOI18N
    private static final String ID = "id"; // NOI18N
    private static final String ATTR = "attr"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String VALUE = "text"; // NOI18N
    private static final String NS = "ns"; // NOI18N
    private static final String TARGET = "target"; // NOI18N


    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

        String s;
        int i;

        switch (state) {

            case INIT:
                if (ROOT.equals(qName) ==  false) error();
                state = IN_ROOT;
                break;

            case IN_ROOT:
                if (PI.equals(qName)) {
                    s = atts.getValue(TARGET); if (s == null) error();
                    template.addPI(s);

                    //!!! TODO presudo atts

                } else if (DOCTYPE.equals(qName)) {
                    s = atts.getValue(PUBLIC_ID);
                    if (s == null) {
                        state = IN_DOCTYPE;
                        break;
                    } else {
                        template.addDoctype(s);
                    }

                } else if (ELEMENT.equals(qName)) {

                    s = atts.getValue(NAME);
                    if (s == null) {
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    } else {
                        template.addElementName(s);
                        s = atts.getValue(NS);
                        if (s != null) template.addElementNS(s);
                    }

                    state = IN_ELEMENT;

                } else {
                    error();
                }
                break;

            case IN_DOCTYPE:
                if (PUBLIC_ID.equals(qName) == false) error();
                s = atts.getValue(ID); if (s == null) error();
                template.addDoctype(s);
                break;

            case IN_ELEMENT:
                if (ATTR.equals(qName)) {
                    s = atts.getValue(NAME); if (s == null) error();
                    template.addElementAtt(s, atts.getValue(VALUE));

                } else if (NS.equals(qName)) {
                    s = atts.getValue(NAME); if (s == null) error();
                    template.addElementNS(s);

                } else {
                    error();
                }

        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {

        switch (state) {
            case IN_ELEMENT:
                if (ELEMENT.equals(qName)) state = IN_ROOT;
                break;      

            case IN_DOCTYPE:
                if (DOCTYPE.equals(qName)) state = IN_ROOT;
                break;
        }
    }
    
    // Sniffing parser ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    

    /**
     * Create just one shared parser instance per thread.
     * Consequently one instance cannot be run in paralel eliminating need for sync.
     */
    private static class LocalSniffingParser extends ThreadLocal {
        
        private WeakReference wref = null;
        
        protected Object initialValue() {            
            SniffingParser parser = new SniffingParser();
            wref = new WeakReference(parser);
            return wref;
        }
        
        public Object get() {
            WeakReference cache = (WeakReference) super.get();
            Object cached = cache.get();
            if (cached == null) {
                cached = new SniffingParser();
                wref = new WeakReference(cached);                
                super.set(wref);
            }
            return cached;            
        }
        
        public void set(Object data) {
            // we are read only!
        }
    }

        
    /**
     * Parser that test XML Document header.
     */
    private static class SniffingParser extends DefaultParser implements LexicalHandler {

        SniffingParser() {
            super(null);
        }

        // last succesfully sniffed fileobject
        private FileObject lastFileObject = null;
        
        private Smell print = null;
        
        // the only way how to stop parser is throwing an exception
        private static final SAXException STOP = new SAXException("STOP");  //NOI18N

        /**
         * Go ahead and retrieve a print
         */
        protected Smell sniff(FileObject fo) {

            if (fo == null) return null;
            
            if (fo.equals(lastFileObject)) return print;

            print = new Smell();
            parse(fo);
            if (this.state == ERROR) {
                return null;
            }
            
            lastFileObject = fo;
            return print;
        }
        
        protected XMLReader createXMLReader() {
            XMLReader parser = null;
            
            try {
                parser = XMLUtil.createXMLReader(false, true);           
                try {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);  //NOI18N
                } catch (SAXException sex) {
                    ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                    if (emgr != null) {
                        emgr.log(NbBundle.getMessage(XMLMIMEComponent.class, "W-003"));  //NOI18N
                    }
                }
            } catch (SAXException ex) {
                ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);
                emgr.notify(emgr.INFORMATIONAL, ex);
            }
            return parser;
        }
        
        protected Exception stopException() {
            return STOP;
        }        
        
        
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {            
            if (namespaceURI != null) {
                print.addElementNS(namespaceURI);
            }
            if ("".equals(localName)) localName = null;  //#16484  //NOI18N
            print.addElementName(localName != null ? localName : qName);
            for (int i = 0; i<atts.getLength(); i++) {
                print.addElementAtt(atts.getQName(i), atts.getValue(i));
            }
            throw STOP;
        }
        
        public void processingInstruction(String target, String data) throws SAXException {
            print.addPI(target);
        }
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
            print.addDoctype(pID);
        }

        public void endDTD() {}

        public void startEntity(String name) {}

        public void endEntity(String name) {}

        public void startCDATA() {}

        public void endCDATA() {}

        public void comment(char[] ch, int start, int length) {}
        
        public void error(SAXParseException exception) throws SAXException {            
            // we are not validating should not occure
            ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);           
            if (emgr != null) {
                emgr.notify(emgr.WARNING, exception);  
            }
            this.state = ERROR;
            throw STOP;
        }

       public void fatalError(SAXParseException exception) throws SAXException {

            //??? it may be caused by wrong user XML document
            ErrorManager emgr = (ErrorManager) Lookup.getDefault().lookup(ErrorManager.class);           
            if (emgr != null) {
                emgr.log(NbBundle.getMessage(XMLMIMEComponent.class, "W-001", fo, new Integer(exception.getLineNumber()))); //NOI18N
                emgr.log(NbBundle.getMessage(XMLMIMEComponent.class, "W-002"));  //NOI18N
                emgr.notify(emgr.INFORMATIONAL, exception);  
            }

            this.state = ERROR;
            throw STOP;
        }
        
        
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
     * Template smell per resolver and print data per FileObject.
     */
    private static class Smell {
        
        private String[] doctypes = null;
        private String[] pis = null;
        
        private String   root = null;
        private String[] nss = null;
        
        private String[] attns = null;
        private String[] attvs = null;

        public String toString() {
            StringBuffer buf = new StringBuffer();
            int i = 0;
            buf.append("xml-check(");
            
            if (doctypes != null) {
                buf.append("doctypes:");
                for (i = 0; i<doctypes.length; i++)
                    buf.append(doctypes[i] + ", ");
            }

            if (pis != null) {
                buf.append("PIs:");
                for (i = 0; i<pis.length; i++)
                    buf.append(pis[i] + ", ");
            }

            if (root != null) {
               buf.append("root:" + root);
            }

            if (nss != null) {
                buf.append("root-namespaces:");
                for (i = 0; i<nss.length; i++)
                    buf.append(nss[i] + ", ");
            }

            if (attns != null) {
                buf.append("attributes:");
                for (i = 0; i<attns.length; i++)
                    buf.append(attns[i] + "='" + attvs[i] + "'");
            }

            buf.append(")");
            return buf.toString();

        }

        private void addDoctype(String s) {
            if (doctypes == null) {
                doctypes = new String[] { s };
            } else {
                doctypes = Util.addString(doctypes, s);
            }            
        }
        
        private void addPI(String s) {
            if (pis == null) {
                pis = new String[] { s };
            } else {
                pis = Util.addString(pis, s);
            }                        
        }
        
        private void addElementNS(String s) {
            if (nss == null) {
                nss = new String[] { s };
            } else {
                nss = Util.addString(nss, s);
            }                        
        }
        
        private void addElementName(String name) {
            root = name;
        }
        
        private void addElementAtt(String name, String value) {
            if (attns == null) {
                attns = new String[] {name};
                attvs = new String[] {value};
            } else {
                attns = Util.addString(attns, name);
                attvs = Util.addString(attvs, value);
            }
            
        }

        /**
         * Matches passed data this template?
         * Any of constructs must match.
         */
        public boolean match(Smell t) {

            if (t == null) return false;
            
            // try if a doctype public-id matches
            
            if (doctypes != null && t.doctypes != null) {
                if (Util.contains(doctypes, t.doctypes[0])) return true;
            }
            
            // try root element match
            
            if (root != null && root.equals(t.root)) {
                if (nss == null) {                                                            
                    if (attMatch(t)) return true;
                } else {                                        
                    if (t.nss != null && Util.contains(nss, t.nss[0])) {
                        if (attMatch(t)) return true;
                    }                    
                }
            } else {
                if (root == null && nss != null && t.nss != null && Util.contains(nss, t.nss[0])) {
                    if (attMatch(t)) return true;
                }                                    
            }
            
            // try if a PI matches
            
            if (pis != null && t.pis!=null) {
                for (int i = 0; i<pis.length; i++) {
                    for (int j = 0; j<t.pis.length; j++) {
                        if (pis[i].equals(t.pis[j])) return true;
                    }
                }
            }            
            
            return false;
        }
        
        
        private boolean attMatch(Smell t) {

            if (attns == null) return true;
            if (t.attns == null) return false;
            
            // all attributes must match by name ...
            for (int i = 0 ; i<attns.length; i++) {
                int match = Util.indexOf(t.attns, attns[i]);
                if (match == -1) {
                    return false;
                }

                // ... and value if specified in template

                if (attvs[i] != null && (!attvs[i].equals(t.attvs[match]))) {
                    return  false;
                }
            }
            
            return true;
            
        }

    }
}
