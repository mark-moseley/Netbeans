/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.border.TitledBorder;
import javax.jmi.reflect.JmiException;

import org.openide.DialogDisplayer;

import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaClass;
/** Customizer for new Unicast Event Set Pattern
 *
 * @author Petr Hrebejk
 */
public final class UEventSetPatternPanel extends javax.swing.JPanel
    implements java.awt.event.ActionListener {

    /** Dialog for displaiyng this panel */
    private Dialog dialog;
    /** Group node under which the new pattern will below */
    private PatternGroupNode groupNode;
    /** Geneartion for interface/class */
    private boolean forInterface = false;

    private transient PatternAnalyser patternAnalyser;
    
    static final long serialVersionUID =4317314528606244073L;


    /** Initializes the Form */
    public UEventSetPatternPanel( PatternAnalyser patternAnalyser ) {
        this.patternAnalyser = patternAnalyser;
        
        initComponents ();
        initAccessibility();

        for( int i = 0; i < EventSetPattern.WELL_KNOWN_LISTENERS.length; i++ ) {
            typeComboBox.addItem( EventSetPattern.WELL_KNOWN_LISTENERS[i] );
        }
        typeComboBox.setSelectedItem( "" ); // NOI18N

        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
        bg.add( emptyRadioButton );
        bg.add( implRadioButton );

        ((TitledBorder)eventSetPanel.getBorder()).setTitle(
            PatternNode.getString( "CTL_UEventSetPanel_eventSetPanel" ));
        ((TitledBorder)optionsPanel.getBorder()).setTitle(
            PatternNode.getString( "CTL_UEventSetPanel_optionsPanel" ) );
        typeLabel.setText( PatternNode.getString( "CTL_UEventSetPanel_typeLabel" ) );
        typeLabel.setDisplayedMnemonic(PatternNode.getString("CTL_UEventSetPanel_typeLabel_Mnemonic").charAt(0));
        typeLabel.setLabelFor(typeComboBox);
        typeComboBox.setToolTipText(PatternNode.getString("ACS_UEventSetPanel_typeComboBoxA11yDesc"));
        textLabel.setText( PatternNode.getString( "CTL_UEventSetPanel_textLabel" ) );
        emptyRadioButton.setText( PatternNode.getString( "CTL_UEventSetPanel_emptyRadioButton" ) );
        emptyRadioButton.setMnemonic(PatternNode.getString("CTL_UEventSetPanel_emptyRadioButton_Mnemonic").charAt(0));
        emptyRadioButton.setToolTipText(PatternNode.getString("ACS_UEventSetPanel_emptyRadioButtonA11yDesc"));
        implRadioButton.setText( PatternNode.getString( "CTL_UEventSetPanel_implRadioButton" ) );
        implRadioButton.setMnemonic(PatternNode.getString("CTL_UEventSetPanel_implRadioButton_Mnemonic").charAt(0));
        implRadioButton.setToolTipText(PatternNode.getString("ACS_UEventSetPanel_implRadioButtonA11yDesc"));
        fireCheckBox.setText( PatternNode.getString( "CTL_UEventSetPanel_fireCheckBox" ) );
        fireCheckBox.setMnemonic(PatternNode.getString("CTL_UEventSetPanel_fireCheckBox_Mnemonic").charAt(0));
        fireCheckBox.setToolTipText(PatternNode.getString("ACS_UEventSetPanel_fireCheckBoxA11yDesc"));
        passEventCheckBox.setText( PatternNode.getString( "CTL_UEventSetPanel_passEventCheckBox" ) );
        passEventCheckBox.setMnemonic(PatternNode.getString("CTL_UEventSetPanel_passEventCheckBox_Mnemonic").charAt(0));
        passEventCheckBox.setToolTipText(PatternNode.getString("ACS_UEventSetPanel_passEventCheckBoxA11yDesc"));
        
        HelpCtx.setHelpIDString(this, HelpCtxKeys.BEAN_EVENTSETS_HELP); //NOI18N
    }

    private void initAccessibility()
    {
        this.getAccessibleContext().setAccessibleDescription(PatternNode.getString("ACSD_UEventSetPanelDialog"));
        typeLabel.getAccessibleContext().setAccessibleDescription(PatternNode.getString("ACS_UEventSetPanel_typeLabelA11yDesc"));
        typeComboBox.getAccessibleContext().setAccessibleName(PatternNode.getString("ACS_UEventSetPanel_typeComboBoxA11yName"));
        textLabel.getAccessibleContext().setAccessibleDescription(PatternNode.getString("ACS_UEventSetPanel_textLabelA11yDesc"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        eventSetPanel = new javax.swing.JPanel();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        textLabel = new javax.swing.JLabel();
        optionsPanel = new javax.swing.JPanel();
        emptyRadioButton = new javax.swing.JRadioButton();
        implRadioButton = new javax.swing.JRadioButton();
        fireCheckBox = new javax.swing.JCheckBox();
        passEventCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        eventSetPanel.setLayout(new java.awt.GridBagLayout());

        eventSetPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(149, 142, 130)), "eventSetPanel"));
        typeLabel.setText("typeLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        eventSetPanel.add(typeLabel, gridBagConstraints);

        typeComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        eventSetPanel.add(typeComboBox, gridBagConstraints);

        textLabel.setText("textLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 2);
        eventSetPanel.add(textLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(eventSetPanel, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(null, new java.awt.Color(149, 142, 130)), "optionsPanel"));
        emptyRadioButton.setSelected(true);
        emptyRadioButton.setText("emptyRadioButton");
        emptyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emptyRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 4);
        optionsPanel.add(emptyRadioButton, gridBagConstraints);

        implRadioButton.setText("implRadioButton");
        implRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                implRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 4);
        optionsPanel.add(implRadioButton, gridBagConstraints);

        fireCheckBox.setText("fireCheckBox");
        fireCheckBox.setEnabled(false);
        fireCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fireCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        optionsPanel.add(fireCheckBox, gridBagConstraints);

        passEventCheckBox.setText("passEventCheckBox");
        passEventCheckBox.setEnabled(false);
        passEventCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passEventCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 2, 4);
        optionsPanel.add(passEventCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(optionsPanel, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void fireCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fireCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_fireCheckBoxActionPerformed

    private void implRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_implRadioButtonActionPerformed
        protectControls();
    }//GEN-LAST:event_implRadioButtonActionPerformed

    private void emptyRadioButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emptyRadioButtonActionPerformed
        protectControls();
    }//GEN-LAST:event_emptyRadioButtonActionPerformed

    private void passEventCheckBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passEventCheckBoxActionPerformed
        protectControls();
    }//GEN-LAST:event_passEventCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eventSetPanel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JCheckBox passEventCheckBox;
    private javax.swing.JCheckBox fireCheckBox;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel textLabel;
    private javax.swing.JRadioButton emptyRadioButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JRadioButton implRadioButton;
    // End of variables declaration//GEN-END:variables


    class Result {

        String type;
        int    implementation = 0;

        boolean firing = false;
        boolean passEvent = false;
    }

    UEventSetPatternPanel.Result getResult( ) {
        Result result = new Result();


        result.type = typeComboBox.getEditor().getItem().toString();

        if ( implRadioButton.isSelected() )
            result.implementation = 1;

        if ( fireCheckBox.isSelected() )
            result.firing = true;

        if ( passEventCheckBox.isSelected() )
            result.passEvent = true;

        return result;
    }

    private void protectControls() {
        implRadioButton.setEnabled( !forInterface );

        fireCheckBox.setEnabled( !emptyRadioButton.isSelected() );
        passEventCheckBox.setEnabled( !emptyRadioButton.isSelected() && fireCheckBox.isSelected() );
    }

    void setDialog( Dialog dialog ) {
        this.dialog = dialog;
    }

    void setForInterface( boolean forInterface ) {
        this.forInterface = forInterface;
        protectControls();
    }

    void setGroupNode( PatternGroupNode groupNode ) {
        this.groupNode = groupNode;
    }

    public void actionPerformed( java.awt.event.ActionEvent e ) {
        if (dialog == null ) {
            return;
        }
        if ( e.getSource() == org.openide.DialogDescriptor.OK_OPTION ) {
            //Test wether the string is empty
            String userText = typeComboBox.getEditor().getItem().toString().trim();
            if ( userText.length() <= 0) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        PatternNode.getString("MSG_Not_Valid_Type"),
                        NotifyDescriptor.ERROR_MESSAGE) );
                typeComboBox.requestFocus();
                return;
            }
            try {
                JMIUtils.beginTrans(false);
                try {
                    validateType(userText);
                } finally {
                    JMIUtils.endTrans();
                }
            } catch (JmiException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }

        dialog.setVisible( false );
        dialog.dispose();
    }
    
    private void validateType(String typeName) throws JmiException {
        Type type;
                
        try {
            assert JMIUtils.isInsideTrans();
            type = patternAnalyser.findType(typeName);
            if (type == null || !(type instanceof JavaClass)) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        PatternNode.getString("MSG_Not_Valid_Type"),
                        NotifyDescriptor.ERROR_MESSAGE) );
                typeComboBox.requestFocus();
                return;
            }
            // Test wheter property with this name already exists
            EventSetPattern eventSetPattern = groupNode.findEventSetPattern( type );
            if (eventSetPattern != null) {
                String msg = MessageFormat.format( PatternNode.getString("MSG_EventSet_Exists"),
                                                   new Object[] { eventSetPattern.getName() } );
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE) );

                typeComboBox.requestFocus();
                return;
            }
        } catch ( JmiException ex ) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return;
        }
                
        // Check whether the property points to a valid listener
        JavaClass elClass = patternAnalyser.findClassElement("java.util.EventListener"); //NOI18N
        if (!((JavaClass) type).isSubTypeOf(elClass)) {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(PatternNode.getString("MSG_InvalidListenerInterface"),
                                             NotifyDescriptor.ERROR_MESSAGE) );
            return;
        }
    }

}
