/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
/** A custom editor for Strings.
*
* @author  Ian Formanek
* @version 1.00, Sep 21, 1998
*/
public class StringCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {
    
    static final long serialVersionUID =7348579663907322425L;
    
    boolean oneline=false;
    String instructions = null;
    //enh 29294, provide one line editor on request
    /** Create a StringCustomEditor.
     * @param value the initial value for the string
     * @param editable whether to show the editor in read only or read-write mode
     * @param oneline whether the text component should be a single-line or multi-line component
     * @param instructions any instructions that should be displayed
     */
    StringCustomEditor (String value, boolean editable, boolean oneline, String instructions) {
        this.oneline = oneline;
        this.instructions = instructions;
        init (value, editable);
   }
    
    /** Initializes the Form 
     * @deprecated Nothing should be using this constructor */
    public StringCustomEditor(String s, boolean editable) {
        init (s, editable);
    }
    
    private void init (String s, boolean editable) {
        setLayout (new java.awt.BorderLayout ());
        if (oneline) {
            textArea = new javax.swing.JTextField();
            add (textArea, BorderLayout.CENTER);
        } else {
            textAreaScroll = new javax.swing.JScrollPane ();
            textArea = new javax.swing.JTextArea ();
            textAreaScroll.setViewportView (textArea);
            add (textAreaScroll, BorderLayout.CENTER);
        }
        //original constructor code
        textArea.setEditable(editable);
        textArea.setText (s);
        if (textArea instanceof JTextArea) {
            ((JTextArea) textArea).setWrapStyleWord( true );
            ((JTextArea)textArea).setLineWrap( true );
            setPreferredSize (new java.awt.Dimension(500, 300));
            if ( !editable ) {
                // hack to fix #9219
                //TODO Fix this to use UIManager values, this is silly
                JTextField hack = new JTextField();
                hack.setEditable( false );
                textArea.setBackground( hack.getBackground() );
                textArea.setForeground( hack.getForeground() );
            }
        } else {
            textArea.setMinimumSize (new java.awt.Dimension (100, 20));
        }
        setBorder (BorderFactory.createEmptyBorder(12,12,0,11));
        
        textArea.getAccessibleContext().setAccessibleName(NbBundle.getBundle(StringCustomEditor.class).getString("ACS_TextArea")); //NOI18N
        if (instructions == null) {
            textArea.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(StringCustomEditor.class).getString("ACSD_TextArea")); //NOI18N
        } else {
            textArea.getAccessibleContext().setAccessibleDescription(instructions);
        }
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(StringCustomEditor.class).getString("ACSD_CustomStringEditor")); //NOI18N
        //Layout is not quite smart enough about text field along with variable
        //size text area
        int prefHeight = textArea.getPreferredSize().height + 8;
        
        if (instructions != null) {
            final JTextArea jta = new JTextArea(instructions);
            jta.setEditable (false);
            java.awt.Color c = UIManager.getColor("control");  //NOI18N
            if (c != null) {
                jta.setBackground (c);
            } else {
                jta.setBackground (getBackground());
            }
            jta.setLineWrap(true);
            jta.setWrapStyleWord(true);
            jta.setFont (getFont());
            add (jta, BorderLayout.NORTH, 0);
            jta.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(StringCustomEditor.class, 
                "ACS_Instructions")); //NOI18N
            jta.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(StringCustomEditor.class, 
                "ACSD_Instructions")); //NOI18N
            prefHeight += jta.getPreferredSize().height;
            //jlf guidelines - auto select text when clicked
            jta.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(jta.getText().length());
                }
                public void focusLost(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(0);
                }
            });          
        }
        if (textArea instanceof JTextField) {
            setPreferredSize (new java.awt.Dimension (300, 
                prefHeight));
        }
    }
    
    public void addNotify () {
        super.addNotify();
        //force focus to the editable area
        textArea.requestFocus();
    }

    /**
    * @return Returns the property value that is result of the CustomPropertyEditor.
    * @exception InvalidStateException when the custom property editor does not represent valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        return textArea.getText ();
    }

    private javax.swing.JScrollPane textAreaScroll;
    private JTextComponent textArea;
}
