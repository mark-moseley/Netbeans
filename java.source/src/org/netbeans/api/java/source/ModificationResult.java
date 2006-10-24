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

package org.netbeans.api.java.source;

import java.io.*;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.editor.BaseDocument;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.PositionRef;

/**
 * Class that collects changes built during a modification task run.
 *
 * @author Dusan Balek
 */
public final class ModificationResult {

    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();
    
    /** Creates a new instance of ModificationResult */
    ModificationResult() {
    }

    // API of the class --------------------------------------------------------
    
    public Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }
    
    public List<? extends Difference> getDifferences(FileObject fo) {
        return diffs.get(fo);
    }
    
    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
            FileObject fo = me.getKey();
            DataObject dObj = DataObject.find(fo);
            EditorCookie ec = dObj != null ? (EditorCookie) dObj.getCookie(EditorCookie.class) : null;
            if (ec != null) {
                Document doc = ec.getDocument();
                if (doc != null) {
                    if (doc instanceof BaseDocument)
                        ((BaseDocument)doc).atomicLock();
                    try {
                        for (Difference diff : me.getValue()) {
                            if (diff.isExcluded())
                                continue;
                            try {
                                switch (diff.getKind()) {
                                    case INSERT:
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                    case REMOVE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        break;
                                    case CHANGE:
                                        doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                                        doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                                        break;
                                }
                            } catch (BadLocationException ex) {
                                IOException ioe = new IOException();
                                ioe.initCause(ex);
                                throw ioe;
                            }
                        }
                    } finally {
                        if (doc instanceof BaseDocument)
                            ((BaseDocument)doc).atomicUnlock();
                    }
                    continue;
                }
            }
            InputStream ins = null;
            ByteArrayOutputStream baos = null;           
            Reader in = null;
            Writer out = null;
            try {
                ins = fo.getInputStream();
                baos = new ByteArrayOutputStream();
                FileUtil.copy(ins, baos);
                
                ins.close();
                ins = null;
                byte[] arr = baos.toByteArray();
                baos.close();
                baos = null;
                in = new InputStreamReader(new ByteArrayInputStream(arr));
                out = new OutputStreamWriter(fo.getOutputStream());
                int offset = 0;                
                for (Difference diff : me.getValue()) {
                    if (diff.isExcluded())
                        continue;
                    int pos = diff.getStartPosition().getOffset();
                    char[] buff = new char[pos - offset];
                    int n;
                    if ((n = in.read(buff)) > 0) {
                        out.write(buff, 0, n);
                        offset += n;
                    }
                    switch (diff.getKind()) {
                        case INSERT:
                            out.write(diff.getNewText());
                            break;
                        case REMOVE:
                            int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                            in.skip(len);
                            offset += len;
                            break;
                        case CHANGE:
                            len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                            in.skip(len);
                            offset += len;
                            out.write(diff.getNewText());
                            break;
                    }
                }                    
                char[] buff = new char[1024];
                int n;
                while ((n = in.read(buff)) > 0)
                    out.write(buff, 0, n);
            } finally {
                if (ins != null)
                    ins.close();
                if (baos != null)
                    baos.close();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }            
        }
    }
    
    public static final class Difference {
        Kind kind;
        PositionRef startPos;
        PositionRef endPos;
        String oldText;
        String newText;
        private boolean excluded;

        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.excluded = false;
        }
        
        public Kind getKind() {
            return kind;
        }
        
        public PositionRef getStartPosition() {
            return startPos;
        }
        
        public PositionRef getEndPosition() {
            return endPos;
        }
        
        public String getOldText() {
            return oldText;
        }
        
        public String getNewText() {
            return newText;
        }
        
        public boolean isExcluded() {
            return excluded;
        }
        
        public void exclude(boolean b) {
            excluded = b;
        }

        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText;
        }

        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE
        }
    }
}
