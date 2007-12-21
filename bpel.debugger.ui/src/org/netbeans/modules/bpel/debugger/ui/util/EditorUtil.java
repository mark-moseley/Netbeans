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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.bpel.core.BPELDataLoader;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.27
 */
public final class EditorUtil {

    private EditorUtil() {}
    
    public static Line getLine(DataObject dataObject, int lineNumber) {
        LineCookie lineCookie = (LineCookie)dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) {
            Log.out("Line cookie is null: " + dataObject); // NOI18N
            return null;
        }
        
        Line.Set lineSet = lineCookie.getLineSet();
        if (lineSet == null) {
            Log.out("Line set is null :" + lineCookie); // NOI18N
            return null;
        }
        
        try {
            return lineSet.getCurrent(lineNumber - 1);
        } catch (IndexOutOfBoundsException e) {
            Log.out("Out of bounds: " + lineNumber); // NOI18N
            return null;
        }
    }
    
    public static DataObject getDataObject(String url) {
        FileObject fileObject = FileUtil.toFileObject(new File(url));
        if (fileObject == null) {
            Log.out("fileObject is null :" + url); // NOI18N
            return null;
        }
        
        try {
            return DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {
            Log.out("Can't find data object for " + fileObject); // NOI18N
            return null;
        }
    }
    
    public static BpelModel getBpelModel(DataObject dataObject) {
        if (dataObject instanceof Lookup.Provider) {
            Lookup.Provider lookupProvider = (Lookup.Provider)dataObject;
            return (BpelModel)lookupProvider.getLookup().lookup(BpelModel.class);
        } else {
            Log.out("Can't lookup a BpelModel - not a Lookup.Provider :" + dataObject); // NOI18N
            return null;
        }
    }
    
    public static BpelModel getBpelModel(String url) {
        DataObject dataObject = getDataObject(url);
        if (dataObject == null) {
            Log.out("DataObject is null :" + url); // NOI18N
            return null;
        }
        
        return getBpelModel(dataObject);
    }
    
    public static StyledDocument getDocument(DataObject dataObject) {
        EditorCookie editorCookie =
                (EditorCookie)dataObject.getCookie(EditorCookie.class);
        if (editorCookie == null) {
            Log.out("Editor cookie is null"); // NOI18N
            return null;
        }
        
        return editorCookie.getDocument();
    }
    
    public static String getFileName(String name) {
        return getTail(getTail(name, "/"), "\\"); // NOI18N
    }
    
    public static int findOffset(Document doc, int lineNumber) {
        Element rootElement = doc.getDefaultRootElement();
        Element lineElement = rootElement.getElement(lineNumber - 1);
        int lineOffset = lineElement.getStartOffset();
        String lineText = "";
        try {
            lineText = doc.getText(
                    lineElement.getStartOffset(),
                    lineElement.getEndOffset() - lineElement.getStartOffset() + 1);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        int column = findNotSpace(lineText);
        if (column < 0) {
            column = 0;
        }
        return lineOffset + column;
    }
    
    public static int getLineNumber(Node node) {
        EditorCookie editorCookie = node.getLookup().lookup(EditorCookie.class);
        if (editorCookie == null) {
            return -1;
        }
        
        
        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();
        if (editorPanes == null || editorPanes.length == 0) {
            return -1;
        }
        
        Caret caret = editorPanes[0].getCaret();
        if (caret == null) {
            return -1;
        }
        
        int offset = caret.getDot();
        
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {
            return -1;
        }
        
        return NbDocument.findLineNumber(document, offset) + 1;
    }
    
    /**
     * Returns the current line of the opened editor. Works only for 
     * BPEL sources (i.e. whose mime type equals BPELDataLoader.MIME_TYPE), for 
     * others return <code>null</code>. This is used to check whether the 
     * "Toggle Line Breakpoint" action should be enabled.
     * 
     * Mostly copied from org.netbeans.modules.ruby.debugger.EditorUtil
     */
    public static Line getCurrentLine() {
        final Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        
        if (nodes == null) return null;
        if (nodes.length != 1) return null;
        
        Node node = nodes[0];
        
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            DataObject dobj = node.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                fileObject = dobj.getPrimaryFile();
            }
        }
        if (fileObject == null) return null;
        
        if (!BPELDataLoader.MIME_TYPE.equals(fileObject.getMIMEType())) {
            return null;
        }
        
        final LineCookie lineCookie = node.getCookie(LineCookie.class);
        if (lineCookie == null) return null;
        
        final EditorCookie editorCookie = node.getCookie(EditorCookie.class);
        if (editorCookie == null) return null;
                
        final JEditorPane jEditorPane = getEditorPane(editorCookie);
        if (jEditorPane == null) return null;
        
        final StyledDocument document = editorCookie.getDocument();
        if (document == null) return null;
        
        final Caret caret = jEditorPane.getCaret();
        if (caret == null) return null;
        
        final int lineNumber = 
                NbDocument.findLineNumber(document, caret.getDot());
        try {
            Line.Set lineSet = lineCookie.getLineSet();
            assert lineSet != null : lineCookie;
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    /* (non-javadoc)
     * 
     * Mostly copied from org.netbeans.modules.ruby.debugger.EditorUtil
     */
    private static JEditorPane getEditorPane_(EditorCookie editorCookie) {
        JEditorPane[] op = editorCookie.getOpenedPanes();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    /* (non-javadoc)
     * 
     * Mostly copied from org.netbeans.modules.ruby.debugger.EditorUtil
     */
    private static JEditorPane getEditorPane(final EditorCookie editorCookie) {
        if (SwingUtilities.isEventDispatchThread()) {
            return getEditorPane_(editorCookie);
        } else {
            final JEditorPane[] ce = new JEditorPane[1];
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        ce[0] = getEditorPane_(editorCookie);
                    }
                });
            } catch (InvocationTargetException ex) {
                //Util.severe(ex);
            } catch (InterruptedException ex) {
                //Util.severe(ex);
                Thread.currentThread().interrupt();
            }
            return ce[0];
        }
    }
    
    private static int findNotSpace(String str) {
        for (int i=0; i < str.length(); i++) {
            if (str.charAt(i) > ' ') {
                return i;
            }
        }
        return -1;
    }
    
    private static String getTail (String value, String delim) {
        int k = value.lastIndexOf (delim);

        if (k != -1) {
          return value.substring(k+1, value.length());
        }
        return value;
    }
}
