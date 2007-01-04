/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.xml;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;

import java.text.MessageFormat;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 * Drive the reading of and receive notification of the content of an
 * XML document.
 * <p>
 * While one can implement the {@link XMLDecoder} interface directly,
 * the recommended practice
 * is to define one or more specialized <code>XMLDecoder</code>s for the
 * expected top-level elements and register them using
 * {@link XMLDecoder#registerXMLDecoder} while leaving all the other 
 * <code>XMLDecoder</code> callbacks empty.
 */

abstract public class XMLDocReader extends XMLDecoder {

    /**
     * Set to true to get a trace of what's being read.
     */

    public boolean debug = false;	// echo SAX callbacks

    private String sourceName;			// remember for error messages

    // This probably SHOULD be per nested XMLDecoder!
    private String currentText = null;

    public XMLDocReader() {
    }


    /**
     * Drive the reading of XML from the given InputStream.
     * <p>
     * This typically results in the callback of implemented 
     * {@link XMLDecoder} getting called, either directly or recursively
     * through an {@link XMLDecoder} registered at construction time.
     *
     * @param sourceName the name of the source of data used by error messages
     */

    public boolean read(InputStream inputStream, String sourceName) {
	this.sourceName = sourceName;
	if (sourceName == null)
	    this.sourceName = Catalog.get("UNKNOWN_sourceName");// NOI18N

	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setValidating(false);

	org.xml.sax.XMLReader xmlReader = null;
	try {
	    SAXParser saxParser = spf.newSAXParser();
	    xmlReader = saxParser.getXMLReader();
	} catch(Exception ex) {
	    ErrorManager.getDefault().notify(ex);
	    return false;
	}

	Parser parser = new Parser();

	xmlReader.setContentHandler(parser);
	xmlReader.setEntityResolver(parser);
	xmlReader.setErrorHandler(new ErrHandler());

	String fmt = Catalog.get("MSG_Whilereading");	// NOI18N
	String whileMsg = MessageFormat.format(fmt, new Object[] {sourceName});

	try {
	    InputSource inputSource = new InputSource(inputStream);
	    xmlReader.parse(inputSource);

	} catch (SAXException ex) {

	    VersionException versionException = null;
	    if (ex.getException() instanceof VersionException) {
		versionException = (VersionException) ex.getException();
	    }

	    if (versionException != null) {
		String what = versionException.element();
		int expectedVersion = versionException.expectedVersion();
		int actualVersion = versionException.actualVersion();

		fmt = Catalog.get("MSG_versionerror");	// NOI18N
		String errmsg = whileMsg + MessageFormat.format(fmt,
		    new Object[] {what,
				  "" + actualVersion,
				  "" + expectedVersion});

		NotifyDescriptor.Message msg = new NotifyDescriptor.
		    Message(errmsg, NotifyDescriptor.ERROR_MESSAGE);

		DialogDisplayer.getDefault().notify(msg);

	    } else {
		ErrorManager.getDefault().annotate(ex, whileMsg);
		ErrorManager.getDefault().notify(ex);
	    }
	    return false;

	} catch (IOException ex) {
	    ErrorManager.getDefault().annotate(ex, whileMsg);
	    ErrorManager.getDefault().notify(ex);
	    return false;

	} catch (Exception ex) {
	    // catchall
	    ErrorManager.getDefault().annotate(ex, whileMsg);
	    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
	    return false;
	}
	return true;
    }


    private class Parser
	implements ContentHandler, EntityResolver {

	/**
	 * Set out own EntityResolver to return an "empty" stream. AFAIK this
	 * is to bypass DTD's and errors of this sort:
	 *
	 *	Warning: in nbrescurr:/<URL>, the nbrescurr URL protocol
	 *	has been deprecated as it assumes Filesystems == classpath.
	 *
	 * followed by IOExceptions
	 */

	// interface EntityResolver
	public InputSource resolveEntity(String pubid, String sysid) {
	    if (debug) {
		System.out.println("SAX resolveEntity: " + pubid + " " + sysid); // NOI18N
	    }
	    byte[] empty = new byte[0];
	    return new InputSource(new java.io.ByteArrayInputStream(empty));
	}

	// interface ContentHandler
	public void startDocument() throws SAXException {
	    if (debug) {
		System.out.println("SAX startDocument"); // NOI18N
	    }
	    try {
		start(null);
	    } catch (VersionException x) {
		throw new SAXException(x);
	    } 
	}

	// interface ContentHandler
	public void endDocument() {
	    if (debug) {
		System.out.println("SAX endDocument"); // NOI18N
	    }
	    end();
	} 

	// interface ContentHandler
	public void characters(char[] ch, int start, int length) {
	    String s = new String(ch, start, length);
	    currentText = currentText + s;
	    if (debug) {
		s = s.trim();
		if (s.length() == 0)
		    System.out.println("SAX characters[" + length + "]: " + // NOI18N
				       "<trimmed>"); // NOI18N
		else
		    System.out.println("SAX characters[" + length + "]: " + s); // NOI18N
	    }
	}


	// interface ContentHandler
	public void startElement(String uri,
				 String localName, String qName,
				 org.xml.sax.Attributes atts)
	     throws SAXException {

	    if (debug) {
		System.out.println("SAX startElement: " + // NOI18N
		    uri + " " + localName + "/" + qName); // NOI18N
		for (int ax = 0; ax < atts.getLength(); ax++) {
		    String AlocalName = atts.getLocalName(ax);
		    String AqName = atts.getQName(ax);
		    String Avalue = atts.getValue(ax);
		    System.out.println("SAX\t" + AlocalName + "/" + AqName + "=" // NOI18N
				       + Avalue);
		}
	    }
	    currentText = "";	// NOI18N
	    try {
		_startElement(qName, atts);
	    } catch (VersionException x) {
		throw new SAXException(x);
	    } 
	}

	// interface ContentHandler
	public void endElement(String uri, String localName, String qName) {
	    if (debug) {
		System.out.println("SAX endElement: " + uri + " " + localName + " " + // NOI18N
		    qName);
	    }
	    _endElement(qName, currentText);
	}

	// interface ContentHandler
	public void startPrefixMapping(String prefix, String uri) {
	}

	// interface ContentHandler
	public void endPrefixMapping(String prefix) {
	    if (debug) {
		System.out.println("SAX endPrefixMapping: " + prefix); // NOI18N
	    }
	}

	// interface ContentHandler
	public void ignorableWhitespace(char[] ch, int start, int length) {
	    if (debug) {
		System.out.println("SAX ignorableWhitespace " + length); // NOI18N
	    }
	}

	// interface ContentHandler
	public void processingInstruction(String target, String data) {
	    if (debug) {
		System.out.println("SAX processingInstruction: " + target + " " + // NOI18N
		    data);
	    }
	}

	// interface ContentHandler
	public void setDocumentLocator(org.xml.sax.Locator locator) {
	    if (debug) {
		System.out.println("SAX setDocumentLocator"); // NOI18N
	    }
	}

	// interface ContentHandler
	public void skippedEntity(String name)  {
	    if (debug) {
		System.out.println("SAX skippedEntity: " + name); // NOI18N
	    }
	}

    }

    private final static class ErrHandler implements ErrorHandler {
	public ErrHandler() {
	} 

	private void annotate(SAXParseException ex) {
	    String fmt = Catalog.get("MSG_sax_error_location");	// NOI18N
	    String msg = MessageFormat.format(fmt, new Object[] {
			    ex.getSystemId(),
			    "" + ex.getLineNumber()
			});
	    ErrorManager.getDefault().annotate(ex,
					       ErrorManager.UNKNOWN,
					       msg,
					       null, null, null);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
	    annotate(ex);
	    throw ex;
	}

	public void error(SAXParseException ex) throws SAXException {
	    annotate(ex);
	    throw ex;
	}

	public void warning(SAXParseException ex) throws SAXException {
	    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
	}
    }
}
