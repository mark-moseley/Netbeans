/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.openide.cookies.LineCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import java.lang.IllegalArgumentException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import java.util.MissingResourceException;
import java.awt.Toolkit;

/**
* Various utilities
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUtilities {

    /** Get the dataobject from the document's StreamDescriptionProperty property. */
    public static DataObject getDataObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            return (DataObject)sdp;
        }
        return null;
    }

    /** This method is a composition of <tt>Utilities.getIdentifierBlock()</tt>
    * and <tt>SyntaxSupport.getFunctionBlock()</tt>.
    * @return null if there's no identifier at the given position.
    *   identifier block if there's identifier but it's not a function call.
    *   three member array for the case that there is an identifier followed
    *   by the function call character. The first two members are members
    *   of the identifier block and the third member is the second member
    *   of the function block.
    */
    public static int[] getIdentifierAndMethodBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] idBlk = Utilities.getIdentifierBlock(doc, offset);
        if (idBlk != null) {
            int[] funBlk = ((ExtSyntaxSupport)doc.getSyntaxSupport()).getFunctionBlock(idBlk);
            if (funBlk != null) {
                return new int[] { idBlk[0], idBlk[1], funBlk[1] };
            }
        }
        return idBlk;
    }

    /** Get the line object from the given position.
    * @param doc document for which the line is being retrieved
    * @param offset position in the document
    * @param original whether to retrieve the original line (true) before
    *   the modifications were done or the current line (false)
    * @return the line object
    */
    public static Line getLine(BaseDocument doc, int offset, boolean original) {
        DataObject dob = getDataObject(doc);
        if (dob != null) {
            LineCookie lc = (LineCookie)dob.getCookie(LineCookie.class);
            if (lc != null) {
                Line.Set lineSet = lc.getLineSet();
                if (lineSet != null) {
                    try {
                        int lineOffset = Utilities.getLineOffset(doc, offset);
                        return original
                               ? lineSet.getOriginal(lineOffset)
                               : lineSet.getCurrent(lineOffset);
                    } catch (BadLocationException e) {
                    }

                }
            }
        }
        return null;
    }

    /** Get the line object from the component's document and caret position */
    public static Line getLine(JTextComponent target, boolean original) {
        return getLine((BaseDocument)target.getDocument(),
                       target.getCaret().getDot(), original);
    }

    /** Get the top-component for the target copmonent */
    public static TopComponent getTopComponent(JTextComponent target) {
        return (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, target);
    }

    /** Add the jump-list entry for the for the component that's opened
    * over the given dataobject if any.
    */
    public static void addJumpListEntry(DataObject dob) {
        final EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
        if (ec != null) {
            final Timer timer = new Timer(500, null);
            timer.addActionListener(
                new ActionListener() {

                    private int countDown = 10;

                    public void actionPerformed(ActionEvent evt) {
                        SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    if (--countDown >= 0) {
                                        JEditorPane[] panes = ec.getOpenedPanes();
                                        if (panes != null && panes.length > 0) {
                                            JumpList.checkAddEntry(panes[0]);
                                            timer.stop();
                                        }
                                    } else {
                                        timer.stop();
                                    }
                                }
                            }
                        );
                    }
                }
            );
            timer.start();
        }
    }

    /** Merge two string arrays into one. */
    public static String[] mergeStringArrays(String[] a1, String[] a2) {
        String[] ret = new String[a1.length + a2.length];
        for (int i = 0; i < a1.length; i++) {
            ret[i] = a1[i];
        }
        for (int i = 0; i < a2.length; i++) {
            ret[a1.length + i] = a2[i];
        }
        return ret;
    }

    /** Get mime type of the given document. */
    public static String getMimeType(Document doc) {
        return (String)doc.getProperty(NbEditorDocument.MIME_TYPE_PROP);
    }

    /** Displays ErrorManager window with the localized message. If bundleKey parameter is not founded in bundle
     *  it is considered as displayable text value. */
    public static void invalidArgument(String bundleKey) {
        IllegalArgumentException iae=new IllegalArgumentException("Invalid argument"); //NOI18N
        Toolkit.getDefaultToolkit().beep();
        ErrorManager errMan=(ErrorManager)Lookup.getDefault().lookup(ErrorManager.class);
        
        if (errMan!=null) {
            errMan.annotate(iae, ErrorManager.USER, iae.getMessage(), getString(bundleKey), null, null); //NOI18N
        }
        throw iae;
    }
    
    private static String getString(String key) {
        try {
            return NbBundle.getBundle(FormatterIndentEngine.class).getString(key);
        } catch (MissingResourceException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            return key;
        }
    }


}
