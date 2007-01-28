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


package org.netbeans.modules.visualweb.designer.cssengine;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.text.BadLocationException;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.css.parser.Scanner;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.ParsedURL;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.InputSource;


/**
 * XXX Class to get rid of the RAVE modifications
 * from the original batik code, this case: batik/Parser.
 *
 * @author Peter Zavadsky
 */
class RaveParser extends Parser {

    /** Creates a new instance of Parser */
    public RaveParser() {
    }


    /** XXX Overriden to use the openide facility to get input stream from file.
     * is that actually correct?
     * Copied from formerly modified superclass. */
    protected Scanner createScanner(InputSource source) {
        documentURI = source.getURI();
        if (documentURI == null) {
            documentURI = "";
        }

        Reader r = source.getCharacterStream();
        if (r != null) {
            return new Scanner(r);
        }

        InputStream is = source.getByteStream();
        if (is != null) {
            return new Scanner(is, source.getEncoding());
        }

        String uri = source.getURI();
        if (uri == null) {
            throw new CSSException(formatMessage("empty.source", null));
        }

        try {
            ParsedURL purl = new ParsedURL(uri);
// BEGIN RAVE MODIFICATIONS
            if ("file".equals(purl.getProtocol())) { // NOI18N
//                is = MarkupService.getOpenCssStream(uri);
                is = getOpenCssStream(uri);
            }
            if (is == null) // NOTE NOTE NOTE: fall through to next line
// END RAVE MODIFICATIONS
                is = purl.openStreamRaw(CSSConstants.CSS_MIME_TYPE);
            return new Scanner(is, source.getEncoding());
        } catch (IOException e) {
            throw new CSSException(e);
        }
    }

// <rave> Moved from MarkupUtilities.
    /**
     * Return an InputStream for the given CSS URI, if the corresponding CSS
     * file is open and edited. Otherwise return null.
     *
     * @param uri The URI to the CSS file. <b>MUST</b> be an absolute file url!
     * @return An InputStream for the live edited CSS
     */
    private static InputStream getOpenCssStream(String uriString) {
        try {
            URI uri = new URI(uriString);
            File file = new File(uri);

            if (file != null) {
                FileObject fobj = FileUtil.toFileObject(file);

                if (fobj != null) {
                    try {
                        DataObject dobj = DataObject.find(fobj);
                        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);

                        if (ec != null) {
                            javax.swing.text.Document doc = ec.getDocument();

                            if (doc != null) {
                                // XXX Isn't there a better way to return an input stream
                                // for a String? Should I have my own?
                                String s = doc.getText(0, doc.getLength());

                                return new StringBufferInputStream(s);
                            }
                        }
                    } catch (BadLocationException ble) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
                    } catch (DataObjectNotFoundException dnfe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
                    }
                }
            }
        } catch (URISyntaxException e) {
            ErrorManager.getDefault().notify(e);
        }

        return null;
    }
// </rave>
}

