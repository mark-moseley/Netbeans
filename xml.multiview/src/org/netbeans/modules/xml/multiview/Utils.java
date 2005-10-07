/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.api.EncodingUtil;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;

/**
 * Utils.java
 *
 * Created on November 16, 2004, 3:21 PM
 * @author mkuchtiak
 */
public class Utils {
    private static final int WAIT_FINISHED_TIMEOUT = 10000;

    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To preserve changes outside of root element only root element is replaced.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     *
     * @param doc original document
     * @param newDoc new value of whole document
     */
    public static boolean replaceDocument(final StyledDocument doc, final String newDoc) {
        if (doc == null) {
            return true;
        }
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    String origDocument = filterEndLines(doc.getText(0, doc.getLength()));
                    String newDocument = filterEndLines(newDoc);

                    if (origDocument.equals(newDocument)) {
                        // no change in document
                        return;
                    }

                    char[] origChars = origDocument.toCharArray();
                    char[] newcChars = newDocument.toCharArray();
                    int tailIndex = origChars.length;
                    int delta = newcChars.length - tailIndex;
                    int n = delta < 0 ? tailIndex + delta : tailIndex;
                    int offset;
                    for (offset = 0; offset < n; offset++) {
                        if (origChars[offset] != newcChars[offset]) {
                            break;
                        }
                    }
                    n = delta < 0 ? offset - delta : offset;
                    for (int i = tailIndex - 1; i >= n; i--) {
                        if (origChars[i] == newcChars[i + delta]) {
                            tailIndex = i;
                        } else {
                            break;
                        }
                    }

                    String s = newDocument.substring(offset, tailIndex + delta);
                    int length = tailIndex - offset;
                    if (doc instanceof AbstractDocument) {
                        ((AbstractDocument) doc).replace(offset, length, s, null);
                    } else {
                        if (length > 0) {
                            doc.remove(offset, length);
                        }
                        if (s.length() > 0) {
                            doc.insertString(offset, s, null);
                        }
                    }
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };
        NbDocument.runAtomic(doc, runnable);
        return true;
    }

    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    private static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        if (text.length == 0) {
            return "";
        }
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i) {
                    text[pos] = c;
                }
                pos++;
            }
        }
        return new String(text, 0, pos);
    }

    /**
     * Sets focus to the next focusable component according to focus traversal policy
     * @param component currently focused component
     */
    public static void focusNextComponent(Component component) {
        Container focusCycleRoot = component.getFocusCycleRootAncestor();
        if (focusCycleRoot == null) {
            return;
        }
        final FocusTraversalPolicy focusTraversalPolicy = focusCycleRoot.getFocusTraversalPolicy();
        if (focusTraversalPolicy == null) {
            return;
        }
        final Component componentAfter = focusTraversalPolicy.getComponentAfter(focusCycleRoot, component);
        if (componentAfter != null) {
            componentAfter.requestFocus();
        }
    }

    /**
     * Scroll panel to make the component visible
     * @param component
     */
    public static void scrollToVisible(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.scrollRectToVisible(new Rectangle(10, component.getHeight()));
            }
        });
    }

    /**
     * Make sure that the code will run in AWT dispatch thread
     * @param runnable
     */
    public static void runInAwtDispatchThread(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }

    }

    /**
     * Utility that sets border and traversal keys for JTextArea in JTextField style
     */
    public static void makeTextAreaLikeTextField(javax.swing.JTextArea ta, javax.swing.JTextField tf) {
        ta.setBorder(tf.getBorder());
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        ta.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                 tf.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    }

    public static void waitFinished(RequestProcessor.Task task) {
        if (task.getDelay() > 0 && !task.isFinished()) {
            try {
                task.waitFinished(WAIT_FINISHED_TIMEOUT);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public static boolean checkEncoding(final StyledDocument doc) throws IOException {
        // dependency on xml/core
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) enc = "UTF8"; //!!! // NOI18N

        try {
            //test encoding on dummy stream
            new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream(1), enc);
            return true;
        } catch (java.io.UnsupportedEncodingException ex) {
            // ask user what next?
            String message = NbBundle.getMessage(Utils.class,"TEXT_SAVE_AS_UTF", enc);
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message);
            Object res = DialogDisplayer.getDefault().notify(descriptor);

            if (res.equals(NotifyDescriptor.YES_OPTION)) {

                // update prolog to new valid encoding

                try {
                    final int MAX_PROLOG = 1000;
                    int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
                    final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
                    int prologLen = 0;  // actual prolog length

                    //parse prolog and get prolog end
                    if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {

                        // look for delimitting ?>
                        for (int i = 3; i<maxPrologLen; i++) {
                            if (prolog[i] == '?' && prolog[i+1] == '>') {
                                prologLen = i + 1;
                                break;
                            }
                        }
                    }

                    final int passPrologLen = prologLen;

                    Runnable edit = new Runnable() {
                         public void run() {
                             try {

                                doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                                doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N

                             } catch (BadLocationException e) {
                                 if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                     e.printStackTrace();
                             }
                         }
                    };

                    NbDocument.runAtomic(doc, edit);
                    return true;

                } catch (BadLocationException lex) {
                    ErrorManager.getDefault().notify(lex);
                }
            }
        }
        return false;
    }
}
