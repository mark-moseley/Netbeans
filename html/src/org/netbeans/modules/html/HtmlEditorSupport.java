/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.html;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.SequenceInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;

import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;


/** 
 * Editor support for HTML data objects.
 *
 * @author Radim Kubacki
 * @see org.openide.text.DataEditorSupport
 */
public final class HtmlEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie {

    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            HtmlEditorSupport.this.saveDocument();
            HtmlEditorSupport.this.getDataObject().setModified(false);
        }
    };
    
    
    /** Constructor. */
    HtmlEditorSupport(HtmlDataObject obj) {
        super(obj, new Environment(obj));
        
        setMIMEType("text/html"); // NOI18N
    }
    
    /** 
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;

        addSaveCookie();

        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();

        removeSaveCookie();
    }

    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        HtmlDataObject obj = (HtmlDataObject)getDataObject();

        // Adds save cookie to the data object.
        if(obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        HtmlDataObject obj = (HtmlDataObject)getDataObject();
        
        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if(cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }

    /** From the begining part of the stream tries to guess the correct encoding
     * to use for loading the document into memory.
     *
     * @param doc the document to read into
     * @param stream the open stream to read from
     * @param kit the associated editor kit
     * @throws IOException if there was a problem reading the file
     * @throws BadLocationException should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        byte[] arr = new byte[4096];
        int len = stream.read (arr, 0, arr.length);
        String txt = new String (arr, 0, len).toUpperCase();
        // encoding
        txt = findEncoding (txt);
        
        // join the streams
        if (len < arr.length) {
            stream = new ByteArrayInputStream (arr, 0, len);
        } else {
            stream = new SequenceInputStream (
                new ByteArrayInputStream (arr), stream
            );
        }
        
        if (txt != null) {
            try {
                InputStreamReader r = new InputStreamReader (stream, txt);
                kit.read (r, doc, 0);
                return;
            } catch (UnsupportedEncodingException ex) {
                // ok unsupported encoding, lets go on
            }
        }
        
        // no or bad encoding, just read the stream
        kit.read (stream, doc, 0);
    }    
    
    /** 
     * @param doc the document to write from
     * @param kit the associated editor kit
     * @param stream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        int len = doc.getLength();
        if (len > 4096) {
            len = 4096;
        }
        String txt = doc.getText(0, len).toUpperCase();
        // encoding
        txt = findEncoding (txt);

        if (txt != null) {
            // try to save in that encoding
            try {
                OutputStreamWriter w = new OutputStreamWriter (stream, txt);
                kit.write (w, doc, 0, doc.getLength());
                return;
            } catch (UnsupportedEncodingException ex) {
                // ok unsupported encoding, lets go on
            }
        }

        // no encoding or unsupported => save in regular way
        super.saveFromKitToStream(doc, kit, stream);
    }
    
    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    private static String findEncoding (String txt) {
        int headLen = txt.indexOf ("</HEAD>");
        if (headLen == -1) headLen = txt.length ();
        
        int content = txt.indexOf ("CONTENT-TYPE");
        if (content == -1 || content > headLen) {
            return null;
        }
        
        int charset = txt.indexOf ("CHARSET=", content);
        if (charset == -1) {
            return null;
        }
        
        int charend = txt.indexOf ('"', charset);
        int charend2 = txt.indexOf ('\'', charset);
        if (charend == -1 && charend2 == -1) {
            return null;
        }

        if (charend2 != -1) {
            if (charend == -1 || charend > charend2) {
                charend = charend2;
            }
        }
        
        return txt.substring (charset + "CHARSET=".length (), charend);
    }
    
    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 3035543168452715818L;
        
        /** Constructor. */
        public Environment(HtmlDataObject obj) {
            super(obj);
        }

        
        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return ((HtmlDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }

        /** 
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (HtmlEditorSupport)getDataObject().getCookie(HtmlEditorSupport.class);
        }
    } // End of nested Environment class.

}
