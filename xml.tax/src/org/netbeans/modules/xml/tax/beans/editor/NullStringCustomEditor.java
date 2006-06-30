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
package org.netbeans.modules.xml.tax.beans.editor;

import javax.swing.JPanel;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
 *
 * @author  Libor Kramolis
 * @version
 */
public class NullStringCustomEditor extends JPanel implements EnhancedCustomPropertyEditor {

    /** Serial Version UID */
    private static final long serialVersionUID =-7120244860529998751L;

    //
    // init
    //

    /** Creates new customizer NullStringCustomEditor */
    public NullStringCustomEditor (NullStringEditor editor) {
        initComponents ();

        textArea.setText (editor.getAsText());
        textArea.setEditable (editor.isEditable());
        
        initAccessibility();
    }


    //
    // EnhancedCustomPropertyEditor
    //

    /**
     * @return Returns the property value that is result of the CustomPropertyEditor.
     * @exception InvalidStateException when the custom property editor does not represent valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue () throws IllegalStateException {
	String text = textArea.getText();

	if ( NullStringEditor.DEFAULT_NULL.equals (text) ) {
	    return null;
	} else if ( text.length() == 0 ) {
	    return null;
	} else {
	    return text;
	}
    }

    //
    // form
    //

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        textAreaScroll = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 6, 6, 6)));
        setPreferredSize(new java.awt.Dimension(350, 230));
        textAreaScroll.setViewportView(textArea);

        add(textAreaScroll, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea textArea;
    private javax.swing.JScrollPane textAreaScroll;
    // End of variables declaration//GEN-END:variables
    
   /** Initialize accesibility
     */
    public void initAccessibility(){
        
       this.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_NullStringCustomEditor"));
       textArea.getAccessibleContext().setAccessibleDescription(Util.THIS.getString("ACSD_textArea")); 
    }
}
