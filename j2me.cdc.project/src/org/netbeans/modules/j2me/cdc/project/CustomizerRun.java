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

package org.netbeans.modules.j2me.cdc.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;


/**
 *
 * @author  Adam Sotona
 */
public class CustomizerRun extends JPanel implements CustomizerPanel, VisualPropertyGroup  {
    
    private static String[] PROPERTY_NAMES = new String[] {
        CDCPropertiesDescriptor.MAIN_CLASS,
        CDCPropertiesDescriptor.MAIN_CLASS_CLASS,
        CDCPropertiesDescriptor.APPLICATION_ARGS,
        DefaultPropertiesDescriptor.RUN_CMD_OPTIONS
    };
    
    private VisualPropertySupport vps;
    private ProjectProperties prop;
    
    public CustomizerRun() {
        initComponents();
    }
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(jCheckBox1, configuration, this);
        prop=props;
        setRunOptions();
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    public void setRunOptions()
    {
        Map<String,String> executionModes=CDCProjectUtil.getExecutionModes(prop);
        String specialExecFqnXlet = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_XLET)  : null;
        String specialExecFqnApplet = (executionModes != null) ? executionModes.get(CDCPlatform.PROP_EXEC_APPLET)  : null;        

        jRadioButton1.setSelected( CDCProjectUtil.isMainClass(jTextFieldMainClass.getText(), prop.getSourceRoot()) );
        jRadioButton2.setSelected( CDCProjectUtil.isXletClass(jTextFieldMainClass.getText(), prop.getSourceRoot(), specialExecFqnXlet) );
        jRadioButton3.setSelected( CDCProjectUtil.isAppletClass(jTextFieldMainClass.getText(), prop.getSourceRoot(), specialExecFqnApplet) );
    }
    
    public void initGroupValues(boolean useDefault) {
        vps.register(jTextFieldMainClass, CDCPropertiesDescriptor.MAIN_CLASS, useDefault);
        vps.register(jRadioButton1, CDCPropertiesDescriptor.MAIN_CLASS_CLASS, useDefault);
        vps.register(jRadioButton2, CDCPropertiesDescriptor.MAIN_CLASS_CLASS, useDefault);
        vps.register(jRadioButton3, CDCPropertiesDescriptor.MAIN_CLASS_CLASS, useDefault);
        vps.register(jTextFieldArgs, CDCPropertiesDescriptor.APPLICATION_ARGS, useDefault);
        vps.register(jTextVMOptions, DefaultPropertiesDescriptor.RUN_CMD_OPTIONS, useDefault);
        
        jButtonMainClass.addActionListener( new MainClassListener( jTextFieldMainClass));
        
        jLabel1.setEnabled(!useDefault);
        jLabelArgs.setEnabled(!useDefault);
        jLabelMainClass.setEnabled(!useDefault);
        jLabelVMOptions.setEnabled(!useDefault);
        jLabelVMOptionsExample.setEnabled(!useDefault);
        jButtonMainClass.setEnabled(!useDefault);
                
        jRadioButton1.setEnabled(false);
        jRadioButton2.setEnabled(false);
        jRadioButton3.setEnabled(false);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jLabelVMOptions = new javax.swing.JLabel();
        jTextVMOptions = new javax.swing.JTextField();
        jLabelVMOptionsExample = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage(CustomizerRun.class, "LBL_UseDefault")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBox1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelMainClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jTextFieldMainClass, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jButtonMainClass, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/cdc/project/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_ExecutionType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel1, gridBagConstraints);

        buttonGroup1.add(jRadioButton1);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton1, NbBundle.getMessage(CustomizerRun.class, "LBL_CustRun_MainMethod")); // NOI18N
        jRadioButton1.setActionCommand("main"); // NOI18N
        jRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jRadioButton1, gridBagConstraints);

        buttonGroup1.add(jRadioButton2);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton2, NbBundle.getMessage(CustomizerRun.class, "LBL_CustRun_XletMethod")); // NOI18N
        jRadioButton2.setActionCommand("xlet"); // NOI18N
        jRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jRadioButton2, gridBagConstraints);

        buttonGroup1.add(jRadioButton3);
        org.openide.awt.Mnemonics.setLocalizedText(jRadioButton3, NbBundle.getMessage(CustomizerRun.class, "LBL_CustRun_AppletMethod")); // NOI18N
        jRadioButton3.setActionCommand("applet"); // NOI18N
        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 12, 0);
        add(jRadioButton3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelArgs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jTextFieldArgs, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabelVMOptions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jTextVMOptions, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(jLabelVMOptionsExample, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextField jTextVMOptions;
    // End of variables declaration//GEN-END:variables
    
    // Innercasses -------------------------------------------------------------
    
    private class MainClassListener implements ActionListener  {
        
        protected final JButton okButton;
        private JTextField mainClassTextField;
        
        MainClassListener( JTextField mainClassTextField ) {            
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton (NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_OK"));
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (CustomizerRun.class, "AD_ChooseMainClass_OK"));
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) { 
            // only chooseMainClassButton can be performed
            final MainClassChooser panel = new MainClassChooser (prop.getSourceRoot(),
                    CDCProjectUtil.getExecutionModes(prop));

            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };

            panel.setSelectedMainClass(mainClassTextField.getText());
            if (jRadioButton2.isSelected()){
                panel.setXletExecution(true);
            } else if (jRadioButton3.isSelected()){
                panel.setAppletExecution(true);
            }
            panel.addChangeListener (new ChangeListener () {
               public void stateChanged(ChangeEvent e) {
                   if ((e.getSource () instanceof MouseEvent) && 
                           MouseUtils.isDoubleClick (((MouseEvent)e.getSource ())) ) {
                       // click button and finish the dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
               }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                panel,
                NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_Title" ),
                true, 
                options, 
                options[0], 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
               mainClassTextField.setText (panel.getSelectedMainClass ());
               setRunOptions();
            } 
            dlg.dispose();
            
        }                
    }

    
}
