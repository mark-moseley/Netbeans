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

package org.netbeans.modules.web.debug;

import org.openide.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.debugger.support.util.Utils;
import org.netbeans.modules.debugger.Controller;
import org.openide.text.Line;

import org.netbeans.modules.web.core.jsploader.JspDataObject;
import org.netbeans.modules.web.html.HtmlDataObject;

import org.openide.loaders.DataObject;

/**
* Customizer of LineBreakpointEvent.
*
* @author  Jan Jancura, Marian Petras
*/
class JspBreakpointPanel extends javax.swing.JPanel implements Controller {

    private final String DOT = ".";    //NOI18N
    private JspCompoundEvent event;
    private String cls;
    
    static final long serialVersionUID =-8164649328980808272L;
    /** Creates new form LineBreakpointPanel */
    public JspBreakpointPanel(JspCompoundEvent e) {
        event = e;
        initComponents ();
        putClientProperty("HelpID", "jsp_breakpoint");//NOI18N
        // a11y
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_LineBreakpointPanel")); // NOI18N
        cboxClass.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Class_name")); // NOI18N
        tfLineNumber.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Line_number")); // NOI18N
        //
        //initialize fields:
        if (e!=null) {
            Line line = Utils.getCurrentLine();
            if ((line != null) && (org.openide.text.DataEditorSupport.findDataObject (line) instanceof JspDataObject)) {
                e.setLine(line);
            }
        }
        fillPackageAndClass();
        fillLineNumber ();
        //
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        lblClassName = new javax.swing.JLabel();
        lblLineNumber = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        cboxClass = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        lblClassName.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Class_name"));
        lblClassName.setLabelFor(cboxClass);
        lblClassName.setDisplayedMnemonic(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Class_name_mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lblClassName, gridBagConstraints);

        lblLineNumber.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number"));
        lblLineNumber.setDisplayedMnemonic(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number_mnemonic").charAt(0));
        lblLineNumber.setLabelFor(tfLineNumber);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(lblLineNumber, gridBagConstraints);

        tfLineNumber.setColumns(7);
        tfLineNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfLineNumberActionPerformed(evt);
            }
        });

        tfLineNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tfLineNumber, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        cboxClass.setColumns(20);
        cboxClass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                setEventSource(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(cboxClass, gridBagConstraints);

    }//GEN-END:initComponents

    private void setEventSource(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_setEventSource
        // Add your handling code here:
        String cls = cboxClass.getText();
        cls = cls.trim(); 
        if (event != null) event.setSourceName(cls);
        fillPackageAndClass();
    }//GEN-LAST:event_setEventSource

    private void tfLineNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLineNumberActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_tfLineNumberActionPerformed

    private void tfLineNumberFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLineNumberFocusGained
        if (!evt.isTemporary()) {
            ((javax.swing.JTextField) evt.getComponent ()).selectAll ();
        }
    }//GEN-LAST:event_tfLineNumberFocusGained

    private void tfLineNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLineNumberFocusLost
        if (!evt.isTemporary()) {
            if (tfLineNumber.getText ().trim ().length () > 0)
                try {
                    int i = Integer.parseInt (tfLineNumber.getText ());
                    if (i < 1) {
                        DialogDisplayer.getDefault().notify (
                            new NotifyDescriptor.Message (
                                NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
                                NotifyDescriptor.ERROR_MESSAGE
                            )
                        );
                    } else
                        if (event != null) event.setLineNumber (i);
                } catch (NumberFormatException e) {
                    DialogDisplayer.getDefault().notify (
                        new NotifyDescriptor.Message (
                            NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                }
            fillLineNumber ();
        }
    }//GEN-LAST:event_tfLineNumberFocusLost

    private void fillPackageAndClass() {
        if (!isAcceptableDataObject()) return;

        if (event.getSourceName()==null) return;
        String s = event.getSourceName().trim();
        
        if (s.length() < 1) {
            cls = "";   //NOI18N
        }
        else {
            cls = s;
            cboxClass.setText(cls);
        }
    }

    private void fillLineNumber () {
        if (!isAcceptableDataObject()) return;
        if (event.getLineNumber () < 1) 
            tfLineNumber.setText ("");  //NOI18N
        else    
            tfLineNumber.setText ("" + event.getLineNumber ()); // NOI18N
    }

    private boolean isAcceptableDataObject() {

        if (event == null) return false;
        
        Line l = event.getLine();
        if (l == null) return false;

        DataObject dobj = org.openide.text.DataEditorSupport.findDataObject (l);
        if (dobj == null) return false;
        
        if ((dobj instanceof JspDataObject) || (dobj instanceof HtmlDataObject)) return true;
        
        return false;
        
    }
    
    /******************************/
    /* CONTROLLER:                */
    /******************************/
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean ok() {
        return true; //TEMP
    }
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean cancel() {
        return true;
    }
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean isValid() {
        return true; //TEMP
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblLineNumber;
    private javax.swing.JTextField tfLineNumber;
    private javax.swing.JTextField cboxClass;
    private javax.swing.JLabel lblClassName;
    // End of variables declaration//GEN-END:variables

}
