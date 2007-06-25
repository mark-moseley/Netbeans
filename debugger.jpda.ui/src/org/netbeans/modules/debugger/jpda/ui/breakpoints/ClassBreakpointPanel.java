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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.Dimension;
import javax.swing.JPanel;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ui.Controller;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author  Jan Jancura
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class ClassBreakpointPanel extends JPanel implements Controller {
// ====
public class ClassBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private ClassLoadUnloadBreakpoint   breakpoint;
    private boolean                     createBreakpoint = false;
    
    
    private static ClassLoadUnloadBreakpoint creteBreakpoint () {
        String className;
        try {
            className = EditorContextBridge.getCurrentClassName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            className = "";
        }
        ClassLoadUnloadBreakpoint mb = ClassLoadUnloadBreakpoint.create (
            className,
            false, 
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED
        );
        mb.setPrintText (
            NbBundle.getBundle (ClassBreakpointPanel.class).getString 
                ("CTL_Class_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel () {
        this (creteBreakpoint ());
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel (ClassLoadUnloadBreakpoint b) {
        breakpoint = b;
        initComponents ();
        
        String[] cf = b.getClassFilters ();
        tfClassName.setText(concatClassFilters(cf));
        
        cbBreakpointType.addItem (NbBundle.getMessage(ClassBreakpointPanel.class, "LBL_Class_Breakpoint_Type_Prepare"));
        cbBreakpointType.addItem (NbBundle.getMessage(ClassBreakpointPanel.class, "LBL_Class_Breakpoint_Type_Unload"));
        cbBreakpointType.addItem (NbBundle.getMessage(ClassBreakpointPanel.class, "LBL_Class_Breakpoint_Type_Prepare_or_Unload"));
        switch (b.getBreakpointType ()) {
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED:
                cbBreakpointType.setSelectedIndex (0);
                break;
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED:
                cbBreakpointType.setSelectedIndex (1);
                break;
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED:
                cbBreakpointType.setSelectedIndex (2);
                break;
        }
        
        conditionsPanel = new ConditionsPanel();
        cPanel.add(conditionsPanel, "Center");
        conditionsPanel.showExclusionClassFilter(true);
        conditionsPanel.showCondition(false);
        conditionsPanel.setClassExcludeFilter(b.getClassExclusionFilters());
        conditionsPanel.setHitCountFilteringStyle(b.getHitCountFilteringStyle());
        conditionsPanel.setHitCount(b.getHitCountFilter());
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");
        // <RAVE>
        // The help IDs for the AddBreakpointPanel panels have to be different from the
        // values returned by getHelpCtx() because they provide different help
        // in the 'Add Breakpoint' dialog and when invoked in the 'Breakpoints' view
        putClientProperty("HelpID_AddBreakpointPanel", "debug.add.breakpoint.java.class"); // NOI18N
        // </RAVE>
    }

    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
       return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointClassJPDA"); // NOI18N
    }
    // </RAVE>
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tfClassName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        cPanel = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Class_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setLayout(new java.awt.GridBagLayout());

        jLabel3.setLabelFor(tfClassName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("L_Class_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Class_Breakpoint_Class_Name")); // NOI18N

        tfClassName.setToolTipText(bundle.getString("TTT_TF_Class_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfClassName, gridBagConstraints);
        tfClassName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Class_Breakpoint_Class_Name")); // NOI18N

        jLabel4.setLabelFor(cbBreakpointType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("L_Class_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(bundle.getString("ASCD_L_Class_Breakpoint_Type")); // NOI18N

        cbBreakpointType.setToolTipText(bundle.getString("TTT_CB_Class_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Class_Breakpoint_Type")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(cPanel, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        String msg = valiadateMsg();
        if (msg == null) {
            msg = conditionsPanel.valiadateMsg();
        }
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        actionsPanel.ok ();
        
        String className = tfClassName.getText ().trim ();
        breakpoint.setClassFilters(parseClassFilters(className));
        breakpoint.setClassExclusionFilters(conditionsPanel.getClassExcludeFilter());//parseClassFilters(className));
        
        switch (cbBreakpointType.getSelectedIndex ()) {
            case 0:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
                break;
            case 1:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED);
                break;
            case 2:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED);
                break;
        }
        breakpoint.setHitCountFilter(conditionsPanel.getHitCount(),
                conditionsPanel.getHitCountFilteringStyle());
        if (createBreakpoint)
            DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        return true;
    }
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel () {
        return true;
    }
    
    /**
     * Return <code>true</code> whether value of this customizer 
     * is valid (and OK button can be enabled).
     *
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    public boolean isValid () {
        return true;
    }
    
    private String valiadateMsg () {
        if (tfClassName.getText().trim ().length() == 0) {
            return NbBundle.getMessage(ClassBreakpointPanel.class, "MSG_No_Class_Name_Spec");
        }
        return null;
    }
    
    static String concatClassFilters(String[] cf) {
        if (cf.length > 0) {
            StringBuilder sb = new StringBuilder(cf[0]);
            for (int i = 1; i < cf.length; i++) {
                sb.append(", ");
                sb.append(cf[i]);
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    static String[] parseClassFilters(String classFilter) {
        int numFilters = 1;
        int length = classFilter.length();
        if (length == 0) {
            return new String[0];
        }
        for (int i = 0; i < length; i++) {
            if (classFilter.charAt(i) == ',') numFilters++;
        }
        String[] classFilters = new String[numFilters];
        if (numFilters == 1) {
            classFilters[0] = classFilter;
        } else {
            int i = 0;
            int pos = 0;
            while (pos < length) {
                int end = classFilter.indexOf(",");
                if (end < 0) end = length;
                classFilters[i] = classFilter.substring(pos, end).trim();
                i++;
                pos = end + 1;
            }
        }
        return classFilters;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfClassName;
    // End of variables declaration//GEN-END:variables
    
}
