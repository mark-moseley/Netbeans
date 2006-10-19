/*
 * MobilityPaletteUtilities.java
 *
 * Created on August 21, 2006, 3:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.snippets;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;


/**
 *
 * @author bohemius
 */
public class SnippetsPaletteUtilities {
    
    /** Creates a new instance of MobilityPaletteUtilities */
    public SnippetsPaletteUtilities() {
    }
    
    public static void insert(String s, JTextComponent target)
    throws BadLocationException {
        insert(s, target, true);
    }
    
    public static void insert(String s, JTextComponent target, boolean reformat)
    throws BadLocationException {
        
        if (s == null)
            s = "";
        
        Document doc = target.getDocument();
        if (doc == null)
            return;
        
        if (doc instanceof BaseDocument)
            ((BaseDocument)doc).atomicLock();
        
        int start = insert(s, target, doc);
        
        if (reformat && start >= 0 && doc instanceof BaseDocument) {  // format the inserted text
            int end = start + s.length();
            Formatter f = ((BaseDocument)doc).getFormatter();
            f.reformat((BaseDocument)doc, start, end);
        }
        
        if (doc instanceof BaseDocument)
            ((BaseDocument)doc).atomicUnlock();
        
    }
    
    private static int insert(String s, JTextComponent target, Document doc)
    throws BadLocationException {
        
        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {}
        
        return start;
    }
}
