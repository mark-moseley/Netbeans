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

package org.netbeans.api.xml.parsers;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.ErrorManager;
import org.openide.TopManager;

import org.xml.sax.*;

/**
 * SAX parser wrapper allowing to parse parsed XML entities (including DTDs) for 
 * wellformedness. 
 * <p>
 * Default implementation cannot be used for parsing of XML document entities!
 * It wraps client's parser that it actually used for performing the parsing task.
 * <p>
 * <b>Primary use case (parse general entity):</b>
 * <pre>
 *   XMLReader entityParser = new SAXEntityParser(xmlReader);
 *   entityParser.setErrorHandler(errorHandler);
 *   entityParser.parse(inputSource);
 * </pre>
 * <b>Secondary use case (delegating parser):</b> It requires subclassing and
 * allow subclass entirely define internal wrapping logic.
 * 
 * <b>Warning:</b> Implementation gurantees only proper ErrorHandler callbacks.
 *
 * @author  Petr Kuzel
 * @deprecated XML tools API candidate
 */
public class SAXEntityParser implements XMLReader {
    
    //??? we are not fully bullet proof
    private static final long RANDOM = System.currentTimeMillis();
    
    private static final String FAKE_SYSTEM_ID = 
        "NetBeans:Fake-System-ID-" + RANDOM;                                    // NOI18N
    
    private static final String FAKE_PUBLIC_ID = 
        "-//NetBeans//Fake Public ID " + RANDOM + "//EN";                       // NOI18N

    // we delegate almost everything on it
    private final XMLReader peer;

    // defines wrapping logic
    private final boolean generalEntity;

    // was client parser already used
    private boolean used = false;
    
    /** 
     * Creates a new instance of general entity parser.
     * @param peer parser that will be used for parsing. Wrapped parser is 
     * exclusively owned by this class no other clients can share it.
     */
    public SAXEntityParser(XMLReader peer) {
        this( peer, true);
    }
    
    /** 
     * Creates a new instance of SAXEntityParser.
     * @param peer parser that will be used for parsing
     * @param generalEntity if <code>false</code> treat entity as parameter
     *        entity (i.e. DTD entities).
     */
    public SAXEntityParser(XMLReader peer, boolean generalEntity) {
        if (peer == null) throw new NullPointerException();
        this.peer = peer;
        this.generalEntity = generalEntity;
    }

    /**
     * Start entity parsing using peer parser. Staring from this moment
     * all other methods calls are not supported.
     * @param entity entity input source
     */
    public void parse( InputSource entity) throws IOException, SAXException {     
        
        if (entity == null) throw new NullPointerException();
        
        synchronized (this) {
            checkUsed();
            used = true;
        }

        // log warning for common errors

        String originalSID = entity.getSystemId();
        if (originalSID == null) {
            ErrorManager err = TopManager.getDefault().getErrorManager();
            if (err.isLoggable(err.WARNING)) {
                StringWriter writer = new StringWriter();
                PrintWriter out = new PrintWriter(writer);
                new IllegalArgumentException("Missing system ID may cause serious errors while resolving relative references!").printStackTrace(out);  // NOI18N
                out.flush();
                err.log(err.WARNING, writer.getBuffer().toString());
            }
        }
        
        // provide fake entity resolver and input source
        
        EntityResolver resolver = peer.getEntityResolver();
        peer.setEntityResolver(new ER(resolver, entity));
                
        ErrorHandler errorHandler = peer.getErrorHandler();
        if (errorHandler != null) {
            peer.setErrorHandler( new EH( errorHandler));
        }
        
        InputSource fakeInput = wrapInputSource(entity);
        if (fakeInput.getSystemId() == null) {
            fakeInput.setSystemId(originalSID);
        }
        if (fakeInput.getPublicId() == null) {
            fakeInput.setPublicId(FAKE_PUBLIC_ID);
        }
        peer.parse(fakeInput);
        
    }

    /**
     * Create wrapper input source. Default implementation utilizes fact that
     * default <code>EntityResolver</code> redirects the first query to wrapped 
     * <code>InputSource</code>.
     * @param input InputSource to be wrapped.
     * @return InputSource that hosts of client's one
     * @since  0.6
     */
    protected InputSource wrapInputSource(InputSource input) {
        String sid = input.getSystemId();
        InputSource fakeInput = new InputSource(FAKE_SYSTEM_ID);
        String fakeDocument;
        if (generalEntity) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<!DOCTYPE fakeDocument" + RANDOM + " [\n");          // NOI18N
            String entityRef = " PUBLIC '" + FAKE_PUBLIC_ID + "' '" + sid + "'";// NOI18N
            buffer.append("<!ENTITY fakeEntity" + RANDOM + entityRef + ">\n");  // NOI18N
            buffer.append("]>\n");                                              // NOI18N
            buffer.append("<fakeDocument" + RANDOM + ">\n");                    // NOI18N
            buffer.append("&fakeEntity" + RANDOM + ";\n");                      // NOI18N
            buffer.append("</fakeDocument" + RANDOM + ">\n");                   // NOI18N
            fakeDocument = buffer.toString();
        } else {
            StringBuffer buffer = new StringBuffer();
            String extRef = " PUBLIC '" + FAKE_PUBLIC_ID + "' '" + sid + "'";   // NOI18N
            buffer.append("<!DOCTYPE fakeDocument" + RANDOM + extRef + ">\n");  // NOI18N
            buffer.append("<fakeDocument" + RANDOM + "/>\n");                   // NOI18N
            fakeDocument = buffer.toString();
        }
        fakeInput.setCharacterStream(new StringReader(fakeDocument));        
        return fakeInput;
    }
    
    /**
     * Examine if the exception should be propagated into client's <code>ErrorHandler</code>.
     * @param ex examined exception
     * @return <code>true</code> if the exception originates from client's
     * <code>InputSource</code> and should be propagated.
     */
    protected boolean propagateException(SAXParseException ex) {
        if (ex == null) return false;
        return (FAKE_SYSTEM_ID.equals(ex.getSystemId()) == false);
    }
    
    public org.xml.sax.ContentHandler getContentHandler() {
        return peer.getContentHandler();
    }
    
    public org.xml.sax.DTDHandler getDTDHandler() {
        return peer.getDTDHandler();
    }
    
    public org.xml.sax.EntityResolver getEntityResolver() {
        return peer.getEntityResolver();
    }
    
    public org.xml.sax.ErrorHandler getErrorHandler() {
        return peer.getErrorHandler();
    }
    
    public boolean getFeature(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
        return peer.getFeature(name);
    }
    
    public Object getProperty(String name) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
        return peer.getProperty(name);
    }
    
    public void parse(String sid) throws java.io.IOException, org.xml.sax.SAXException {
        this.parse(new InputSource(sid));
    }
    
    public void setContentHandler(org.xml.sax.ContentHandler contentHandler) {
        peer.setContentHandler(contentHandler);
    }
    
    public void setDTDHandler(org.xml.sax.DTDHandler dTDHandler) {
        peer.setDTDHandler(dTDHandler);
    }
    
    public void setEntityResolver(org.xml.sax.EntityResolver entityResolver) {
        peer.setEntityResolver(entityResolver);
    }
    
    public void setErrorHandler(org.xml.sax.ErrorHandler errorHandler) {
        peer.setErrorHandler(errorHandler);
    }
    
    public void setFeature(String name, boolean val) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
        peer.setFeature(name, val);
    }
    
    public void setProperty(String name, Object val) throws org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException {
        peer.setProperty(name, val);
    }

    private synchronized void checkUsed() {
        if (used == true) throw new IllegalStateException();        
    }
    
    /**
     * Redirect to entity input source, it is always the first request.
     * Pure FAKE_SYSTEM_ID approach have problems with some parser implementations.
     */
    private class ER implements EntityResolver {

        private boolean entityResolved;
        private final EntityResolver peer;
        private final InputSource entity;
        
        public ER(EntityResolver peer, InputSource entity) {
            this.peer = peer;
            this.entity = entity;
        }
        
        public InputSource resolveEntity(String pid, String sid) throws SAXException, IOException {
            
            Util.THIS.debug("SAXEntityParser:resolving PID: " + pid + " SID: " + sid);
                
            if (isFirstRequest()) {

                // normalize passed entity InputSource using parent entity resolver
                Util.THIS.debug("SAXEntityParser:redirecting to " + entity + " SID: " + entity.getSystemId());
                
                if (peer != null && entity.getByteStream() == null && entity.getCharacterStream() == null) {                    
                    return peer.resolveEntity(entity.getPublicId(), entity.getSystemId());
                } else {
                    return entity;
                }
            } else {
                if (peer == null) {
                    return null;
                } else {
                    return peer.resolveEntity(pid, sid);
                }
            }
        }
                
        private synchronized boolean isFirstRequest() {
            if (entityResolved == false) {
                entityResolved = true;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Filter out errors in our fake document
     */
    private class EH implements ErrorHandler {
        
        private final ErrorHandler peer;
        
        public EH( ErrorHandler peer) {
            this.peer = peer;
        }
        
        public void error(SAXParseException ex) throws SAXException {
            if (propagateException(ex)) {
                peer.error(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }
        
        public void fatalError(SAXParseException ex) throws SAXException {
            if (propagateException(ex)) {
                peer.fatalError(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }
        
        public void warning(SAXParseException ex) throws SAXException {
            if (propagateException(ex)) {
                peer.warning(ex);
            } else {
                Util.THIS.debug("SAXEntityParser: filtering out:", ex);
            }
        }        
    }
    
}
