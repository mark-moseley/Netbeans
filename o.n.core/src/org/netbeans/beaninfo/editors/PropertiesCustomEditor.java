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

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import java.io.*;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.*;

/** A custom editor for Properties.
*
* @author  Ian Formanek
*/
public class PropertiesCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

  private PropertiesEditor editor;

  /** Initializes the Form */
  public PropertiesCustomEditor(PropertiesEditor ed) {
    editor = ed;
    initComponents ();
    Properties props = (Properties) editor.getValue ();
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    try {
      props.store (baos, "");
    } catch (IOException e) {
      // strange, strange -> ignore
    }
    textArea.setText (baos.toString ());
    setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(8, 8, 8, 8)));
  }

  /**
  * @return Returns the property value that is result of the CustomPropertyEditor.
  * @exception InvalidStateException when the custom property editor does not represent valid property value
  *            (and thus it should not be set)
  */
  public Object getPropertyValue () throws IllegalStateException {
    Properties props = new Properties ();
    try {
      props.load (new ByteArrayInputStream (textArea.getText ().getBytes ()));
    } catch (IOException e) {
      // strange, strange -> ignore
    }
    return props;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the FormEditor.
   */
  private void initComponents () {//GEN-BEGIN:initComponents
    setLayout (new java.awt.BorderLayout ());

    textAreaScroll = new javax.swing.JScrollPane ();

      textArea = new javax.swing.JTextArea ();

    textAreaScroll.setViewportView (textArea);


    add (textAreaScroll, "Center");

  }//GEN-END:initComponents



// Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane textAreaScroll;
  private javax.swing.JTextArea textArea;
// End of variables declaration//GEN-END:variables

}

/*
 * Log
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    Reflecting changes in 
 *       editors packages and enhanced property editor interfaces
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/4/99   Ian Formanek    
 * $
 */

