/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.xml.cookies;

import java.io.*;
import java.net.*;
import java.util.*;

import org.xml.sax.*;

import javax.swing.text.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Adapt <code>DataObject</code> to other common XML interfaces.
 *
 * @author      Petr Kuzel
 * @deprecated  XML tools SPI candidate
 * @since       0.9
 */
public final class DataObjectAdapters {
    
    /** SAX feature: Perform namespace processing. */
    private static final String SAX_FEATURES_NAMESPACES = "http://xml.org/sax/features/namespaces"; // NOI18N
    
    /** cached SAXParserFactory instance. */
    private static SAXParserFactory saxParserFactory;
    
    private  DataObjectAdapters() {
    }
    
    /**
     * Create InputSource from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @return <code>DataObject</code> never <code>null</code>
     */           
    public static InputSource inputSource (DataObject dataObject) {
        if (dataObject == null) throw new NullPointerException();
        return new DataObjectInputSource(dataObject);
    }

    /**
     * Lazy evaluated wrapper.
     */
    private static class DataObjectInputSource extends InputSource {
        
        private final DataObject dataObject;
        
        public DataObjectInputSource (DataObject dataObject) {
            this.dataObject = dataObject;
        }
                
        public String getSystemId() {
            return DataObjectAdapters.getSystemId (dataObject);
        }
        
        public Reader getCharacterStream() {

            EditorCookie editor = (EditorCookie) dataObject.getCookie(EditorCookie.class);

            if (editor != null) {
                Document doc = editor.getDocument();
                if (doc != null) {
                    return  new DocumentInputSource(doc).getCharacterStream();
                }
            }             
            
            return null;
        }
        
    }

    
    /**
     * Create Source from DataObject. Default implementation prefers opened
     * Swing <code>Document</code> over primary file URL.
     * @return <code>DataObject</code> never <code>null</code>
     */               
    public static Source source (DataObject dataObject) {
        if (dataObject == null) throw new NullPointerException();        
        return new DataObjectSAXSource(dataObject);
    }

    /**
     * Lazy evaluated wrapper.
     */    
    private static class DataObjectSAXSource extends SAXSource {
        
        private final DataObject dataObject;
        
        public DataObjectSAXSource(DataObject dataObject) {
            this.dataObject = dataObject;
        }
        
        public String getSystemId() {
            return DataObjectAdapters.getSystemId (dataObject);
        }
        
        public XMLReader getXMLReader() {
            try {
                XMLReader reader = newXMLReader();
                reader.setEntityResolver (getEntityResolver());
                return reader;
            } catch (ParserConfigurationException ex) {
                Util.THIS.debug(ex);
            } catch (SAXNotRecognizedException ex) {
                Util.THIS.debug(ex);
            } catch (SAXNotSupportedException ex) {
                Util.THIS.debug(ex);
            } catch (SAXException ex) {
                Util.THIS.debug(ex);
            }
            return null;            
        }
        
        public InputSource getInputSource() {
            return inputSource (dataObject);
        }

    } // class DataObjectSAXSource


    /** Try to find the best URL name of <code>dataObject</code>.
     * @return system Id of <code>dataObject</code>
     */
    private static String getSystemId (DataObject dataObject) {
        String systemId = null;
        try {
            FileObject fileObject = dataObject.getPrimaryFile();
            URL url = preferFileURL (fileObject);
            systemId = url.toExternalForm();
        } catch (FileStateInvalidException exc) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);

            // nothing to do -> return null; //???
        }
        return systemId;
    }

    /**
     * If possible it finds "file:" URL if <code>fileObject</code> is on LocalFileSystem.
     * @return URL of <code>fileObject</code>.
     */
    private static URL preferFileURL (FileObject fileObject) throws FileStateInvalidException {
        URL fileURL = null;
        File file = FileUtil.toFile (fileObject);
        
        if ( file != null ) {
            try {
                fileURL = file.toURL();
            } catch (MalformedURLException exc) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);

                fileURL = fileObject.getURL();
            }
        } else {
            fileURL = fileObject.getURL();
        }
        return fileURL;
    }
    
    private static synchronized SAXParserFactory getSAXParserFactory () throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if ( saxParserFactory == null ) {
            saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature (SAX_FEATURES_NAMESPACES, true);
        }
        return saxParserFactory;
    }

    /**
     *
     * @throws ParserConfigurationException if a parser cannot
     *         be created which satisfies the requested configuration.
     * @throws SAXException if a parser cannot be created which satisfies the requested configuration.
     */
    private static XMLReader newXMLReader () throws ParserConfigurationException, SAXException {
        SAXParser parser = getSAXParserFactory().newSAXParser();  //!!! it is expensive!
        return parser.getXMLReader();
    }
    
    private static EntityResolver getEntityResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());
        return res;
    }        
}
