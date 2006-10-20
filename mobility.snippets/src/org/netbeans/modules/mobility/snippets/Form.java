/*
 * Form.java
 *
 * Created on October 20, 2006, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.snippets;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;

/**
 *
 * @author bohemius
 */
public class Form implements ActiveEditorDrop {
    
    /** Creates a new instance of Form */
    public Form() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        StringBuffer body=new StringBuffer("");
     
        body.append("private javax.microedition.lcdui.Form myForm=null;\n\n");
        body.append("public Form get_myForm() {\n");
        body.append("   if (myForm==null)\n");
        body.append("       myForm=new Form(\"Sample Form\");\n");
        body.append("   return myForm;\n");
        body.append("}\n\n");
                
        try {
            SnippetsPaletteUtilities.insert(body.toString(), targetComponent);
        } catch (BadLocationException ble) {
            return false;
        }
        return true;
    }
    
}
