/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.editors;

import com.netbeans.ide.explorer.propertysheet.NbCustomPropertyEditor;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;

/** A custom editor for Strings.
*
* @author  Ian Formanek
* @version 1.00, Sep 21, 1998
*/
public class StringCustomEditor extends javax.swing.JPanel implements NbCustomPropertyEditor {

  private StringEditor editor;

  /** Initializes the Form */
  public StringCustomEditor(StringEditor ed) {
    editor = ed;
    String s = (String) editor.getValue ();
    initComponents ();
    textArea.setText (s);
  }

  /**
  * @return Returns the property value that is result of the CustomPropertyEditor.
  * @exception InvalidStateException when the custom property editor does not represent valid property value
  *            (and thus it should not be set)
  */
  public Object getPropertyValue () throws IllegalStateException {
    return textArea.getText ();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    setPreferredSize (new java.awt.Dimension(500, 300));
    setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 8, 8)));
    setLayout (new java.awt.BorderLayout ());

    textAreaScroll = new javax.swing.JScrollPane ();

      textArea = new javax.swing.JTextArea ();
      textAreaScroll.add (textArea);

    textAreaScroll.setViewportView (textArea);
    add (textAreaScroll, "Center");

  }//GEN-END:initComponents



// Variables declaration - do not modify//GEN-BEGIN:variables
  javax.swing.JScrollPane textAreaScroll;
  javax.swing.JTextArea textArea;
// End of variables declaration//GEN-END:variables

}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
