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

package org.netbeans.modules.java;

import java.io.*;
import java.nio.charset.Charset;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import org.netbeans.api.queries.FileEncodingQuery;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.text.IndentEngine;

/**
 *
 * @author  Svata
 * @version 1.0
 */
public abstract class IndentFileEntry extends FileEntry.Format {
    private static final String NEWLINE = "\n"; // NOI18N
    private static final String EA_PREFORMATTED = "org-netbeans-modules-java-preformattedSource"; // NOI18N

    private ThreadLocal indentEngine;
    
    /** Creates new JavaFileEntry */
    IndentFileEntry(MultiDataObject dobj, FileObject file) {
        super(dobj, file);
    }

    private EditorKit createEditorKit(String mimeType) {
        EditorKit kit;
        
        kit = JEditorPane.createEditorKitForContentType(mimeType);
        if (kit == null) {
            kit = new javax.swing.text.DefaultEditorKit();
        }
        return kit;
    }
    
    /* package private */ final void setIndentEngine(IndentEngine engine) {
        synchronized (this) {
            if (indentEngine == null)
                indentEngine = new ThreadLocal();
        }
        indentEngine.set(engine);
    }
    
    /* package private */ final void initializeIndentEngine() {
        StyledDocument doc = createDocument(createEditorKit(getFile().getMIMEType()));
        IndentEngine engine = IndentEngine.find(doc); // NOI18N
        setIndentEngine(engine);
    }

    private StyledDocument createDocument(EditorKit kit) {
        Document doc = kit.createDefaultDocument();
        if (doc instanceof StyledDocument) {
            return (StyledDocument)doc;
        } else {
            return new org.openide.text.FilterDocument(doc);
        }
    }
    
    /** Creates a new Java source from the template. Unlike the standard FileEntry.Format,
        this indents the resulting text using an indentation engine.
    */
    public FileObject createFromTemplate (FileObject f, String name) throws IOException {
        String ext = getFile ().getExt ();

        if (name == null) {
            name = FileUtil.findFreeFileName(f, getFile ().getName (), ext);
        }
        FileObject fo = f.createData (name, ext);
        java.text.Format frm = createFormat (f, name, ext);
        InputStream is=getFile ().getInputStream ();
        Charset encoding = FileEncodingQuery.getEncoding(getFile());
        Reader reader = new InputStreamReader(is,encoding);
        BufferedReader r = new BufferedReader (reader);
        StyledDocument doc = createDocument(createEditorKit(fo.getMIMEType()));
        IndentEngine eng = (IndentEngine)indentEngine.get();
        if (eng == null) eng = IndentEngine.find(doc);
        Object attr = getFile().getAttribute(EA_PREFORMATTED);
        boolean preformatted = false;
        
        if (attr != null && attr instanceof Boolean) {
            preformatted = ((Boolean)attr).booleanValue();
        }

        try {
            FileLock lock = fo.lock ();
            try {
                encoding = FileEncodingQuery.getEncoding(fo);
                OutputStream os=fo.getOutputStream(lock);
                OutputStreamWriter w = new OutputStreamWriter(os, encoding);
                try {
                    String line = null;
                    String current;
                    int offset = 0;

                    while ((current = r.readLine ()) != null) {
                        if (line != null) {
                            // newline between lines
                            doc.insertString(offset, NEWLINE, null);
                            offset++;
                        }
                        line = frm.format (current);

                        // partial indentation used only for pre-formatted sources
                        // see #19178 etc.
                        if (!preformatted || !line.equals(current)) {
                            line = fixupGuardedBlocks(safeIndent(eng, line, doc, offset));
                        }
                        doc.insertString(offset, line, null);
                            offset += line.length();
                    }
                    doc.insertString(doc.getLength(), NEWLINE, null);
                    w.write(doc.getText(0, doc.getLength()));
                } catch (javax.swing.text.BadLocationException e) {
                } finally {
                    w.close ();
                }
            } finally {
                lock.releaseLock ();
            }
        } finally {
            r.close ();
        }
        // copy attributes
        FileUtil.copyAttributes (getFile (), fo);
	// hack to overcome package-private modifier in setTemplate(fo, boolean)
        fo.setAttribute(DataObject.PROP_TEMPLATE, null);
        return fo;
    }
    
    static String fixupGuardedBlocks(String indentedLine) {
        int offset = indentedLine.indexOf(Util.MAGIC_PREFIX);
        if (offset == -1)
            return indentedLine;
        // move the guarded block at the end of the first line in the string
        int firstLineEnd = indentedLine.indexOf('\n'); // NOI18N
        if (firstLineEnd == -1 || firstLineEnd > offset)
            // already on the first line.
            return indentedLine;
        int guardedLineEnd = indentedLine.indexOf('\n', offset); // NOI18N
        StringBuffer sb = new StringBuffer(indentedLine.length());
        sb.append(indentedLine.substring(0, firstLineEnd));
        if (guardedLineEnd != -1) {
            sb.append(indentedLine.substring(offset, guardedLineEnd));
        } else {
            sb.append(indentedLine.substring(offset));
        }
        sb.append(indentedLine.substring(firstLineEnd, offset));
        if (guardedLineEnd != -1)
            sb.append(indentedLine.substring(guardedLineEnd));
        return sb.toString();
    }

    public static String safeIndent(IndentEngine engine, String text, StyledDocument doc, int offset) {
        if (engine == null)
            return text;
        try {
            StringWriter writer = new StringWriter();
            Writer indentator = engine.createWriter(doc, offset, writer);
            indentator.write(text);
            indentator.close();
            return writer.toString();
        } catch (Exception ex) {
	    ErrorManager.getDefault().annotate(
		ex, ErrorManager.WARNING, "Indentation engine error",  // NOI18N
                    Util.getString("EXMSG_IndentationEngineError"), ex, null);
            ErrorManager.getDefault().notify(ex);
            return text;
        }
    }
}
