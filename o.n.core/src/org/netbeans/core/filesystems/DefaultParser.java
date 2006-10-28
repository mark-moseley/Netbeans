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

package org.netbeans.core.filesystems;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.xml.*;
import org.openide.*;


/**
 * Implements default interruptible silent parser behaviour.
 * Errors can be tested by quering parser state.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
abstract class DefaultParser  extends DefaultHandler {

    protected FileObject fo;
    private Locator locator = null;

    protected short state = INIT;

    protected static final short PARSED = 1000;
    protected static final short ERROR = -1;
    protected static final short INIT = 0;

    protected DefaultParser() {        
    }
    
    protected DefaultParser(FileObject fo) {
        this.fo = fo;
    }

    /**
     * Preconfigure parser and return it.
     */
    protected XMLReader createXMLReader() throws IOException, SAXException {
        return XMLUtil.createXMLReader(false);
    }

    /**
     * Check if the given exception is one thrown from the handler
     * for stopping the parser.
     */
    protected boolean isStopException(Exception e) {
        return false;
    }

    /**
     * @return current parser state
     */
    protected short getState() {
        return state;
    }

    protected final Locator getLocator() {
        return locator;
    }
    
    /**
     * Parser content workarounding known parser implementation
     * problems.
     */
    protected void parse(FileObject fo) {
        state = INIT; // #15672
        InputStream is = null;
        this.fo = fo;
        try {
            XMLReader parser = createXMLReader();
            parser.setEntityResolver(this);
            parser.setErrorHandler(this);
            parser.setContentHandler(this);

//            try {
//                // do not read DTD
//                parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);  //NOI18N
//            } catch (SAXException ignore) {
//                // parsing may be slower :-(
//            }

            InputSource in = new InputSource();                
            is = fo.getInputStream();
            in.setByteStream(is);
            in.setSystemId(fo.getURL().toExternalForm());
            customizeInputSource(in);
            
            parser.parse(in);

        } catch (IOException io) {
            if (!isStopException(io)) {
                if (fo.isValid() && fo.canRead()) {
                    Exceptions.attachMessage(io, "While parsing: " + fo); // NOI18N
                    Logger.getLogger(DefaultParser.class.getName()).log(Level.INFO, null, io);
                    state = ERROR;
                }
            }
        } catch (SAXException sex) {
            if (!isStopException(sex)) {
                Exceptions.attachMessage(sex, "While parsing: " + fo); // NOI18N
                Logger.getLogger(DefaultParser.class.getName()).log(Level.INFO, null, sex);
                state = ERROR;
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // already closed
                }
            }
        }                        
    }


    protected void customizeInputSource(InputSource in) {
    }
    
    /**
     * Parser default file object
     */
    protected final void parse() {
        if (fo == null) throw new NullPointerException();
        parse(fo);
    }

    /** Report error occured during custom validation. */
    protected void error() throws SAXException {
        String reason = org.openide.util.NbBundle.getMessage(DefaultParser.class, "Invalid_XML_document");
        error(reason);
    }

    /** Report error occured during custom validation. */
    protected void error(String reason) throws SAXException {
        StringBuffer buf = new StringBuffer (reason).append(": ").append(fo.toString());//NOI18N
        if (locator != null) {
            buf.append(" line: ").append(locator.getLineNumber());//NOI18N
            buf.append(" column: ").append(locator.getColumnNumber());//NOI18N
        }
        String msg = buf.toString();  //NOI18N
        SAXException sex = new SAXException(msg);
        throw sex;
    }

    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }

    public void endDocument() throws SAXException {
        state = PARSED;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public InputSource resolveEntity (String publicID, String systemID) {
        // Read nothing whatsoever.
        return new InputSource (new ByteArrayInputStream (new byte[] { }));
    }
    
}
