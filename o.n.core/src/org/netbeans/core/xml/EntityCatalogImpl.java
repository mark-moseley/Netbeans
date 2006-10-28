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

package org.netbeans.core.xml;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.*;
import org.openide.xml.*;

/**
 * This Entity Catalog implementation recognizes registrations defined at XMLayer.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class EntityCatalogImpl extends EntityCatalog {

    /** map between publicId and privateId (String, String); must be synchronized */
    private Map<String, String> id2uri;  

    private static final RequestProcessor catalogRP = new RequestProcessor("EntityCatalog/parser"); // NOI18N

    /** Creates new EntityCatalogImpl */
    private EntityCatalogImpl(Map<String,String> map) {
        id2uri = map;
    }
    
    /**
     * Resolve an entity using cached mapping.
     */
    public InputSource resolveEntity(String publicID, String systemID) {
        if (publicID == null) return null;

        String res = id2uri.get(publicID); // note this is synchronized Hashtable

        InputSource ret = null;
        if (res != null) {
            ret = new InputSource(res);
        }
            
//            System.err.println("" + publicID + " => " + ret);
        return ret;
    }

    /** 
     * XMLDataObject.Processor implementation recognizing EntityCatalog.PUBLIC_ID DTDs
     * giving them instance cookie returning registered entries.
     */
    public static class RegistrationProcessor extends DefaultHandler implements XMLDataObject.Processor, InstanceCookie, Runnable, PropertyChangeListener {

        private XMLDataObject peer;
        private Map<String, String> map;
        private RequestProcessor.Task parsingTask = catalogRP.create(this);
        private EntityCatalogImpl instance = null;

        // Processor impl

        public void attachTo (XMLDataObject xmlDO) {
            
            if (xmlDO == peer) return;  //ignore double attachements
            
            peer = xmlDO;                        
            peer.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(this, peer));  //listen at PROP_DOCUMENT
            parsingTask.schedule(0);
        }

        // DefaultHandler extension

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            if ("public".equals(qName)) {  //NOI18N
                String key = atts.getValue("publicId");  //NOI18N
                String val = atts.getValue("uri");  //NOI18N

                if (key != null && val != null) {
                    map.put(key, val);
                } else {
                    throw new SAXException ("invalid <public> element: missing publicId or uri"); // NOI18N
                }
            }
        }

        public InputSource resolveEntity(String pid, String sid) {
            if (EntityCatalog.PUBLIC_ID.equals(pid)) {
                // Don't use a nbres: URL here; can deadlock NbURLStreamHandlerFactory during startup
                return new InputSource(EntityCatalogImpl.class.getClassLoader().getResource("org/openide/xml/EntityCatalog.dtd").toExternalForm()); // NOI18N
            }
            return null;
        }

        // Runnable impl (can be a task body)

        public void run() {
            map = new Hashtable<String, String>();  //be synchronized

            try {
                String loc = peer.getPrimaryFile().getURL().toExternalForm();
                InputSource src = new InputSource(loc);

                // XXX(-ttran) don't validate
                XMLReader reader = XMLUtil.createXMLReader(false);
                reader.setErrorHandler(this);
                reader.setContentHandler(this);
                reader.setEntityResolver(this);
                reader.parse(src);
            } catch (SAXException ex) {
                // ignore
                Logger.getLogger(EntityCatalogImpl.class.getName()).log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                // ignore
        	Logger.getLogger(EntityCatalogImpl.class.getName()).log(Level.WARNING, null, ex);
    	    }
        }

        // InstanceCookie impl

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return EntityCatalog.class;
        }

        /** We return singleton instance */
        public Object instanceCreate() throws IOException, ClassNotFoundException {
            
            synchronized (this) {
                if (instance == null) {
                    parsingTask.waitFinished();                        
                    instance = new EntityCatalogImpl (map);
                }
            }
            return instance;
        }

        public String instanceName() {
            return "org.openide.xml.EntityCatalog"; // NOI18N
        }

        /**
          * Perform synchronous update on fileobject change.
          */
        public void propertyChange(PropertyChangeEvent e) {
            
            synchronized(this) {
                if (instance == null) return;
            }
            
            if (XMLDataObject.PROP_DOCUMENT.equals(e.getPropertyName())) {
                // Please use ErrorManager if debugging messages are required
                // (try TM.getErrorManager().getInstance(thisClassName).log(message))
                //System.err.println("XML file have changed. reparsing " + peer.getPrimaryFile() ); // NOI18N
                //update it sync
                run();
                instance.id2uri = map;  //replace map
            }
        }
        
    }
}
