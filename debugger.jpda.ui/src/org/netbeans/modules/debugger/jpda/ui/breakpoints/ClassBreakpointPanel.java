/*
 * LineBreakpointPanel.java
 *
 * Created on 24. b�ezen 2004, 16:18
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import javax.swing.JPanel;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.Context;
import org.netbeans.spi.debugger.ui.Controller;


/**
 *
 * @author  Jan Jancura
 */
public class ClassBreakpointPanel extends JPanel implements Controller {
    
    private ActionsPanel                actionsPanel; 
    private ClassLoadUnloadBreakpoint   breakpoint;
    private boolean                     createBreakpoint = false;
    
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel () {
        this (ClassLoadUnloadBreakpoint.create (
            Context.getCurrentClassName (),
            false, 
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED
        ));
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel (ClassLoadUnloadBreakpoint b) {
        breakpoint = b;
        initComponents ();
        
        String[] cf = b.getClassExclusionFilters ();
        String className = "";
        if (cf.length > 0) {
            cbExclusionFilter.setSelected (true);
            className = cf [0];
        } else {
            cbExclusionFilter.setSelected (false);
            cf = b.getClassFilters ();
            if (cf.length > 0)
                className = cf [0];
        }
        int i = className.lastIndexOf ('.');
        if (i < 0) {
            tfPackageName.setText ("");
            tfClassName.setText (className);
        } else {
            tfPackageName.setText (className.substring (0, i));
            tfClassName.setText (className.substring (i + 1, className.length ()));
        }
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Class_Breakpoint_Type_Prepare"));
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Class_Breakpoint_Type_Unload"));
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Class_Breakpoint_Type_Prepare_or_Unload"));
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
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfPackageName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tfClassName = new javax.swing.JTextField();
        cbExclusionFilter = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setLayout(new java.awt.GridBagLayout());

        pSettings.setBorder(new javax.swing.border.TitledBorder("Settings"));
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Class_Breakpoint_filter_hint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_L_Class_Breakpoint_filter_hint"));

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_L_Class_Breakpoint_Package_Name").charAt(0));
        jLabel2.setLabelFor(tfPackageName);
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Class_Breakpoint_Package_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pSettings.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_L_Class_Breakpoint_Package_Name"));

        tfPackageName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_TF_Class_Breakpoint_Package_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.weightx = 1.0;
        pSettings.add(tfPackageName, gridBagConstraints);
        tfPackageName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_TF_Class_Breakpoint_Package_Name"));

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_L_Class_Breakpoint_Class_Name").charAt(0));
        jLabel3.setLabelFor(tfClassName);
        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Class_Breakpoint_Class_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_L_Class_Breakpoint_Class_Name"));

        tfClassName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_TF_Class_Breakpoint_Class_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.weightx = 1.0;
        pSettings.add(tfClassName, gridBagConstraints);
        tfClassName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_TF_Class_Breakpoint_Class_Name"));

        cbExclusionFilter.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_CB_Class_Breakpoint_Exclusion_Filter").charAt(0));
        cbExclusionFilter.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("CB_Class_Breakpoint_Exclusion_Filter"));
        cbExclusionFilter.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_CB_Class_Breakpoint_Exclusion_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pSettings.add(cbExclusionFilter, gridBagConstraints);
        cbExclusionFilter.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_CB_Class_Breakpoint_Exclusion_Filter"));

        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_L_Class_Breakpoint_Type").charAt(0));
        jLabel4.setLabelFor(cbBreakpointType);
        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Class_Breakpoint_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ASCD_L_Class_Breakpoint_Type"));

        cbBreakpointType.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_CB_Class_Breakpoint_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_CB_Class_Breakpoint_Type"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        actionsPanel.ok ();
        String className = tfPackageName.getText ().trim ();
        if (className.length () > 0)
            className += '.';
        className += tfClassName.getText ().trim ();
        if (cbExclusionFilter.isSelected ()) {
            breakpoint.setClassFilters (new String [0]);
            breakpoint.setClassExclusionFilters (new String [] {className});
        } else {
            breakpoint.setClassFilters (new String [] {className});
            breakpoint.setClassExclusionFilters (new String [0]);
        }
        
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
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JCheckBox cbExclusionFilter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfClassName;
    private javax.swing.JTextField tfPackageName;
    // End of variables declaration//GEN-END:variables
    
}
