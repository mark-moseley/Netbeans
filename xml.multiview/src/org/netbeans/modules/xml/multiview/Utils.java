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

import java.awt.*;

/**
 * Utils.java
 *
 * Created on November 16, 2004, 3:21 PM
 * @author mkuchtiak
 */
public class Utils {
    
    /** This method update document in editor after change in beans hierarchy.
     * It takes old document and new document in String.
     * To avoid regeneration of whole document in text editor following steps are done:
     *  1) compare the begin of both documents (old one and new one)
     *     - find the first position where both documents differ
     *  2) do the same from the ends of documents
     *  3) remove old middle part of text (modified part) and insert new one
     * 
     * @param doc original document
     * @param newDoc new value of whole document
     * @param prefixMark - beginning part of the document before this mark should be preserved
     */
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc, String prefixMark) throws javax.swing.text.BadLocationException {
        int origLen = doc.getLength();        
        String origDoc = doc.getText(0, origLen);
        int prefixInd=0;
        if (prefixMark!=null) {
            prefixInd = origDoc.indexOf(prefixMark);
            if (prefixInd>0) {
                origLen-=prefixInd;
                origDoc=doc.getText(prefixInd,origLen);
            }
            else {
                prefixInd=0;
            }
            int prefixIndNewDoc=newDoc.indexOf(prefixMark);
            if (prefixIndNewDoc>0)
            newDoc=newDoc.substring(prefixIndNewDoc);
        }
        newDoc=filterEndLines(newDoc);
        int newLen = newDoc.length();
        
        if (origDoc.equals(newDoc)) {
            // no change in document
            return;
        }

        final int granularity = 20;
        
        int prefix = -1;
        int postfix = -1;
        String toInsert = newDoc;
        
        if ((origLen > granularity) && (newLen > granularity)) {
            int pos1 = 0;
            int len = Math.min(origLen, newLen);
            // find the prefix which both Strings begin with
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos1, granularity)) {
                    pos1 += granularity;
                    if (pos1 + granularity >= len) {
                        break;
                    }
                }
                else {
                    break;
                }
            }
            if (pos1 > 0)
                prefix = pos1;
            
            pos1 = origLen - granularity;
            int pos2 = newLen - granularity;
            for (;;) {
                if (origDoc.regionMatches(pos1, newDoc, pos2, granularity)) {
                    pos1 -= granularity;
                    pos2 -= granularity;
                    if (pos1 < 0) {
                        pos1 += granularity;
                        break;
                    }
                    if (pos2 < 0) {
                        pos2 += granularity;
                        break;
                    }
                }
                else {
                    pos1 += granularity;
                    pos2 += granularity;
                    break;
                }
            }
            if (pos1 < origLen - granularity) {
                postfix = pos1;
            }
        }

        if ((prefix != -1) && (postfix != -1)) {
            if (postfix < prefix) {
                postfix = prefix;
            }
            
            int delta = (prefix + (origLen - postfix) - newLen);
            if (delta > 0) {
                postfix += delta;
            }
        }
        
        int removeBeginIndex = (prefix == -1) ? 0 : prefix;
        int removeEndIndex;
        if (postfix == -1){
            if(doc.getText(0, doc.getLength()).charAt(doc.getLength()-1) == '>'){
                removeEndIndex = origLen;
            }
            else
                removeEndIndex = origLen-1;
        }
        else 
            removeEndIndex = postfix;
        
        doc.remove(prefixInd+removeBeginIndex, removeEndIndex - removeBeginIndex);
        
        if (toInsert.length() > 0) {
            int p1 = (prefix == -1) ? 0 : prefix;
            int p2 = toInsert.length();
            if (postfix != -1)
                p2 = p2 - (origLen - postfix);

            if (p2 > p1) {
                toInsert = toInsert.substring(p1, p2);
                doc.insertString(prefixInd+removeBeginIndex, toInsert, null);
            }
        }
    }
    
    public static void replaceDocument(javax.swing.text.Document doc, String newDoc) throws javax.swing.text.BadLocationException {
        replaceDocument(doc,newDoc,null);
    }
    
    /** Filter characters #13 (CR) from the specified String
     * @param str original string
     * @return the string without #13 characters
     */
    public static String filterEndLines(String str) {
        char[] text = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < text.length; i++) {
            char c = text[i];
            if (c != 13) {
                if (pos != i)
                    text[pos] = c;
                pos++;
            }
        }
        return new String(text, 0, pos - 1);
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
}
