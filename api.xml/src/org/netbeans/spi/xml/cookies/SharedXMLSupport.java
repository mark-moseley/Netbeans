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
import java.security.ProtectionDomain;
import java.security.CodeSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.text.Document;

import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.filesystems.FileStateInvalidException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.api.xml.services.*;
import org.netbeans.api.xml.parsers.*;


/**
 * <code>CheckXMLCookie</code> and <code>ValidateXMLCookie</code> cookie 
 * implementation support simplifing cookie providers based on 
 * <code>InputSource</code>s representing XML documents and entities.
 *
 * @author      Petr Kuzel
 * @deprecated  XML tools SPI candidate
 * @see         CheckXMLSupport
 * @see         ValidateXMLSupport
 */
class SharedXMLSupport {

    // it will viasualize our results
    private CookieObserver console;
    
    // associated input source
    private final InputSource inputSource;
    
    // one of above modes CheckXMLSupport modes
    private final int mode;
    
    // error locator or null
    private Locator locator;

    // fatal error counter
    private int fatalErrors;
    
    // error counter
    private int errors;
    
    /** 
     * Create new CheckXMLSupport for given InputSource in DOCUMENT_MODE.
     * @param inputSource Supported InputSource.
     */    
    public SharedXMLSupport(InputSource inputSource) {
        this(inputSource, CheckXMLSupport.DOCUMENT_MODE);
    }    
    
    /** 
     * Create new CheckXMLSupport for given data object
     * @param inputSource Supported InputSource.
     * @param mode one of <code>*_MODE</code> constants
     */
    public SharedXMLSupport(InputSource inputSource, int mode) {

        if (inputSource == null) throw new NullPointerException();
        if (mode < CheckXMLSupport.CHECK_ENTITY_MODE || mode > CheckXMLSupport.DOCUMENT_MODE) {
            throw new IllegalArgumentException();
        }
        
        this.inputSource = inputSource;
        this.mode = mode;
    }

    // inherit JavaDoc
    boolean checkXML(CookieObserver l) {
        try {
            console = l;

            parse(false);

            return fatalErrors == 0;
        } finally {
            console = null;
            locator = null;            
        }
    }
    
    // inherit JavaDoc
    boolean validateXML(CookieObserver l) {
        try {
            console = l;

            if (mode != CheckXMLSupport.DOCUMENT_MODE) {
                sendMessage(Util.THIS.getString("MSG_not_a_doc"));
                return false;
            } else {        
                parse(true);
                return errors == 0 && fatalErrors == 0;
            }
        } finally {
            console = null;
            locator = null;
        }
    }
                

    
    /**
     * Perform parsing in current thread.
     */
    private void parse (boolean validate) {
        
        fatalErrors = 0;
        errors = 0;
                
        String checkedFile = inputSource.getSystemId();
        sendMessage(Util.THIS.getString("MSG_checking", checkedFile));

        Handler handler = new Handler();
        
        try {

            // set up parser
            
            XMLReader parser = createParser(validate);
            
            if (parser == null) {
                fatalErrors++;
                console.receive(new CookieMessage(
                    Util.THIS.getString("MSG_cannot_create_parser"),
                    CookieMessage.FATAL_ERROR_LEVEL
                ));
                return;
            }

            parser.setErrorHandler(handler);           
            parser.setContentHandler(handler);
            
            if ( Util.THIS.isLoggable()) {
                Util.THIS.debug(checkedFile + ":" + parserDescription(parser));
            }

            // parse
            
            final InputSource input = createInputSource();

            if (mode == CheckXMLSupport.CHECK_ENTITY_MODE) {
                new SAXEntityParser(parser, true).parse(input);
            } else if (mode == CheckXMLSupport.CHECK_PARAMETER_ENTITY_MODE) {
                new SAXEntityParser(parser, false).parse(input);
            } else {
                parser.parse (input);
            }

        } catch (SAXException ex) {

            // same as one catched by ErrorHandler
            // because we do not have content handler

        } catch (FileStateInvalidException ex) {

            // bad luck report as fatal error
            handler.fatalError(new SAXParseException(ex.getLocalizedMessage(), locator, ex));

        } catch (IOException ex) {

            // bad luck probably because cannot resolve entity
            // report as error at -1,-1 if we do not have Locator
            handler.fatalError(new SAXParseException (ex.getLocalizedMessage(), locator, ex));

        } catch (RuntimeException ex) {

            // probably an internal parser error
            String msg = Util.THIS.getString("EX_parser_ierr", ex.getMessage());
            handler.fatalError(new SAXParseException (msg, locator, ex));

        }
        
    }

    /**
     * Parametrizes default parser creatin process. Default implementation
     * takes user's catalog entity resolver.
     * @return EntityResolver entity resolver or <code>null</code>
     */
    protected EntityResolver createEntityResolver() {
        UserCatalog catalog = UserCatalog.getDefault();
        return catalog == null ? null : catalog.getEntityResolver();
    }
    
    /**
     * Create InputSource to be checked.
     * @throws IOException if I/O error occurs.
     * @return InputSource never <code>null</code>
     */
    protected InputSource createInputSource() throws IOException {        
        return inputSource;
    }

    /** 
     * Create and preconfigure new parser. Default implementation uses JAXP.
     * @param validate true if validation module is required
     * @return SAX reader that is used for command performing or <code>null</code>
     * @see #createEntityResolver
     */
    protected XMLReader createParser(boolean validate) {
       
        XMLReader ret = null;
        final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";         // NOI18N
        final String XERCES_PROPERTY_PREFIX = "http://apache.org/xml/properties/";      // NOI18N
        
       // JAXP plugin parser (bastarded by core factories!)
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(validate);

        try {
            SAXParser parser = factory.newSAXParser();
            ret = parser.getXMLReader();                
        } catch (Exception ex) {
            sendMessage(Util.THIS.getString("MSG_parser_err_1"));
            return null;
        }

        //??? It is Xerces specifics, but no general API for XML Schema based validation exists
        if (validate) {
            try {
                ret.setFeature(XERCES_FEATURE_PREFIX + "validation/schema", validate); // NOI18N
            } catch (SAXException ex) {
                sendMessage(Util.THIS.getString("MSG_parser_no_schema"));
            }                
        }

        if (ret != null) {
            EntityResolver res = createEntityResolver();
            if (res != null) ret.setEntityResolver(new VerboseEntityResolver(res));
        }
        
        return ret;
        
    }

    /**
     * It may be helpfull for tracing down some oddities.
     */
    private String parserDescription(XMLReader parser) {

        // report which parser implementation is used
        
        Class klass = parser.getClass();
        try {
            ProtectionDomain domain = klass.getProtectionDomain();
            CodeSource source = domain.getCodeSource();
            
            if (source == null && (klass.getClassLoader() == null || klass.getClassLoader().equals(Object.class.getClassLoader()))) {
                return Util.THIS.getString("MSG_platform_parser");
            } else if (source == null) {
                return Util.THIS.getString("MSG_unknown_parser", klass.getName());
            } else {
                URL location = source.getLocation();
                return Util.THIS.getString("MSG_parser_plug", location.toExternalForm());
            }
            
        } catch (SecurityException ex) {
            return Util.THIS.getString("MSG_unknown_parser", klass.getName());
        }
        
    }
    
    // Content & ErrorHandler implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    private class Handler extends DefaultHandler {
    
        public void warning (SAXParseException ex) {
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.WARNING_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            if (console != null) console.receive(message);
        }

        /**
         * Report maximally getMaxErrorCount() errors then stop the parser.
         */
        public void error (SAXParseException ex) throws SAXException {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Just diagnostic exception", ex); // NOI18N
            if (errors++ == getMaxErrorCount()) {
                String msg = Util.THIS.getString("MSG_too_many_errs");
                sendMessage(msg);
                throw ex; // stop the parser                
            } else {
                CookieMessage message = new CookieMessage(
                    ex.getLocalizedMessage(), 
                    CookieMessage.ERROR_LEVEL,
                    new DefaultXMLProcessorDetail(ex)
                );
                if (console != null) console.receive(message);
            }
        }

        public void fatalError (SAXParseException ex) {        
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Just diagnostic exception", ex); // NOI18N
            fatalErrors++;
            CookieMessage message = new CookieMessage(
                ex.getLocalizedMessage(), 
                CookieMessage.FATAL_ERROR_LEVEL,
                new DefaultXMLProcessorDetail(ex)
            );
            if (console != null) console.receive(message);
        }
        
        public void setDocumentLocator(Locator locator) {
            SharedXMLSupport.this.locator = locator;
        }

        private int getMaxErrorCount() {
            return 20;  //??? load from option
        }    
        
    }


    /**
     * EntityResolver that reports unresolved entities.
     */
    private class VerboseEntityResolver implements EntityResolver {
        
        private final EntityResolver peer;
        
        public VerboseEntityResolver(EntityResolver res) {
            if (res == null) throw new NullPointerException();
            peer = res;
        }
        
        public InputSource resolveEntity(String pid, String sid) throws SAXException, IOException {
            InputSource result = peer.resolveEntity(pid, sid);
            
            // null result may be suspicious, may be no Schema location found etc.
            
            if (result == null) {
                                
                String warning;
                String pidLabel = pid != null ? pid : Util.THIS.getString("MSG_no_pid");
                try {
                    String file = new URL(sid).getFile();
                    if (file != null) {
                        warning = Util.THIS.getString("MSG_resolver_1", pidLabel, sid);
                    } else {  // probably NS id
                        warning = Util.THIS.getString("MSG_resolver_2", pidLabel, sid);
                    }
                } catch (MalformedURLException ex) {
                    warning = Util.THIS.getString("MSG_resolver_3", pidLabel, sid);
                }
                sendMessage(warning);
            }
            return result;
        }
        
    }
    
    private void sendMessage(String message) {
        if (console != null) {
            console.receive(new CookieMessage(message));
        }
    }
    
}
