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

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.openide.filesystems.FileObject;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.UserCatalog;

/**
 * Perform Transform action on XML document.
 * Default implementation of {@link TransformableCookie} cookie.
 *
 * @author     Libor Kramolis
 * @deprecated XML tools SPI candidate
 */
public final class TransformableSupport implements TransformableCookie {

    // associated source
    private final Source source;
    /** cached TransformerFactory instance. */
    private static TransformerFactory transformerFactory;
    
    
    /** 
     * Create new TransformableSupport for given data object.
     * @param source Supported <code>Source</code>.
     */    
    public TransformableSupport (Source source) {
        if (source == null) throw new NullPointerException();
        this.source = source;
    }

    /**
     * Transform this object by XSL Transformation.
     *
     * @param transformSource source of transformation.
     * @param outputResult result of transformation.
     * @param listener optional listener (<code>null</code> allowed)
     *                 giving judgement details.
     * @throws TransformerException if an unrecoverable error occurs during the course of the transformation
     */
    public void transform (Source transformSource, Result outputResult, CookieObserver notifier) throws TransformerException {
        try {
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("TransformableSupport.transform");
                Util.THIS.debug ("   transformSource = " + transformSource.getSystemId());
                Util.THIS.debug ("   outputResult = " + outputResult.getSystemId());
            }

            
            Source xmlSource = source;

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("   xmlSource = " + xmlSource.getSystemId());

            // prepare transformer == parse stylesheet, errors may occur
            Transformer transformer = newTransformer (transformSource);
            
            // transform
            if (notifier != null) {
                Proxy proxy = new Proxy (notifier);
                transformer.setErrorListener (proxy);
            }
            transformer.transform (xmlSource, outputResult);
            
        } catch (Exception exc) { // TransformerException, ParserConfigurationException, SAXException, FileStateInvalidException
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("    EXCEPTION during transformation", exc);
                Util.THIS.debug ("    exception's message = " + exc.getLocalizedMessage());

                Throwable tempExc = unwrapException (exc);
                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
            }

            TransformerException transExcept = null;
            Object detail = null;
            
            if ( exc instanceof TransformerException ) {
                transExcept = (TransformerException)exc;                
                detail = new DefaultXMLProcessorDetail (transExcept);
                
            } else if ( exc instanceof SAXParseException ) {
                transExcept = new TransformerException (exc);
                detail = new DefaultXMLProcessorDetail ((SAXParseException)exc);
                
            } else {
                transExcept = new TransformerException (exc);
                detail = new DefaultXMLProcessorDetail (transExcept);
            }

            if ( notifier != null ) {            
                CookieMessage message = new CookieMessage(
                    unwrapException(exc).getLocalizedMessage(), 
                    CookieMessage.FATAL_ERROR_LEVEL,
                    detail
                );                
                notifier.receive (message);
            }
            
            throw transExcept;
        }                
    }


    //
    // utils
    //

    private static Throwable unwrapException (Throwable exc) {
        Throwable wrapped = null;
        if (exc instanceof TransformerException) {
            wrapped = ((TransformerException) exc).getException();
        } else if (exc instanceof SAXException) {
            wrapped = ((SAXException) exc).getException();
        } else {
            return exc;
        }

        if ( wrapped == null ) {
            return exc;
        }

        return unwrapException (wrapped);
    }

    private static URIResolver getURIResolver () {
        UserCatalog catalog = UserCatalog.getDefault();
        URIResolver res = (catalog == null ? null : catalog.getURIResolver());
        return res;
    }

    private static TransformerFactory getTransformerFactory () {
        if ( transformerFactory == null ) {
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver (getURIResolver()); //!!! maybe that it should be set every call if UsersCatalog instances are dynamic
        }
        return transformerFactory;
    }


    private static Transformer newTransformer (Source xsl) throws TransformerConfigurationException {
        return getTransformerFactory().newTransformer (xsl);
    }




    //
    // class Proxy
    //

    private static class Proxy implements ErrorListener {
        
        private final CookieObserver peer;
        
        public Proxy (CookieObserver peer) {
            if (peer == null) {
                throw new NullPointerException();
            }
            this.peer = peer;
        }
        
        public void error (TransformerException tex) throws TransformerException {
            report (CookieMessage.ERROR_LEVEL, tex);
        }
        
        public void fatalError (TransformerException tex) throws TransformerException {
            report (CookieMessage.FATAL_ERROR_LEVEL, tex);
        }
        

        public void warning (TransformerException tex) throws TransformerException {
            report (CookieMessage.WARNING_LEVEL, tex);
        }

        private void report (int level, TransformerException tex) throws TransformerException {
            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("[TransformableSupport::Proxy]: report [" + level + "]: ", tex);
                Util.THIS.debug ("    exception's message = " + tex.getLocalizedMessage());

                Throwable tempExc = unwrapException (tex);
                Util.THIS.debug ("    wrapped exception = " + tempExc.getLocalizedMessage());
            }

            Throwable unwrappedExc = unwrapException (tex);
            CookieMessage message = new CookieMessage (
                unwrappedExc.getLocalizedMessage(), 
                level,
                new DefaultXMLProcessorDetail (tex)
            );

            peer.receive (message);
        }
        
    } // class Proxy
    
}
