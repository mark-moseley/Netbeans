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


package org.netbeans.modules.i18n;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakListener;


/**
 * Panel which is used for customizing key-value pair (and comment also) encapsulated by <code>I18nString</code> object.
 * It's used inside <code>I18nPanel</code>.
 *
 * @author  Peter Zavadsky
 * @see I18nString
 * @see I18nPanel
 */
public class PropertyPanel extends JPanel {
    
    /** Helper name for dummy action command. */
    private static final String DUMMY_ACTION = "dont_proceed"; // NOI18N
    
    /** Cusomized <code>I18nString</code>. */
    private I18nString i18nString;

    /** Helper listener. */
    private PropertyChangeListener propListener;
    
    /** Creates new <code>PropertyPanel</code>. */
    public PropertyPanel() {
        initComponents();

    }

    /** Seter for <code>i18nString</code> property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;
        
        i18nString.addPropertyChangeListener(WeakListener.propertyChange(
            propListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(I18nString.PROP_KEY.equals(evt.getPropertyName()))
                        updateKey();
                    else if(I18nString.PROP_VALUE.equals(evt.getPropertyName()))
                        updateValue();
                    else if(I18nString.PROP_COMMENT.equals(evt.getPropertyName()))
                        updateComment();
                    else if(I18nString.PROP_RESOURCE.equals(evt.getPropertyName()))
                        updateBundleKeys();
                    
                }
            },
            i18nString
        ));
        
        initAllValues();
    }

    
    /** Initializes UI values. */
    private void initAllValues() {
        updateKey();
        updateValue();
        updateComment();
        updateBundleKeys();
        
        replaceFormatTextField.setText(i18nString.getReplaceFormat());
    }
    
    /** Updates selected item of <code>keyBundleCombo</code> UI. */
    private void updateKey() {
        String key = i18nString.getKey();
        
        if(key == null || !key.equals(keyBundleCombo.getSelectedItem()) )
            keyBundleCombo.setSelectedItem(key == null ? "" : key); // NOI18N
    }

    /** Updates <code>valueText</code> UI. */
    private void updateValue() {            
        String value = i18nString.getValue();
        
        if(!valueText.getText().equals(value))
            valueText.setText(value == null ? "" : value); // NOI18N
    }
    
    /** Updates <code>commentText</code> UI. */
    private void updateComment() {
        String comment = i18nString.getComment();
        
        if(!commentText.getText().equals(comment))
            commentText.setText(comment == null ? "" : comment); // NOI18N
    }
    
    /** Updates <code>keyBundleCombo</code> UI. */
    private void updateBundleKeys() {
        // Trick to avoid firing key selected property change.
        String oldActionCommand = keyBundleCombo.getActionCommand();
        keyBundleCombo.setActionCommand(DUMMY_ACTION);

        keyBundleCombo.setModel(new DefaultComboBoxModel(i18nString.getAllKeys()));
        updateKey();

        keyBundleCombo.setActionCommand(oldActionCommand);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        commentLabel = new javax.swing.JLabel();
        commentScroll = new javax.swing.JScrollPane();
        commentText = new javax.swing.JTextArea();
        keyLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valueScroll = new javax.swing.JScrollPane();
        valueText = new javax.swing.JTextArea();
        keyBundleCombo = new javax.swing.JComboBox();
        replaceFormatTextField = new javax.swing.JTextField();
        replaceFormatButton = new javax.swing.JButton();
        replaceFormatLabel = new javax.swing.JLabel();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        commentLabel.setText(I18nUtil.getBundle().getString("LBL_Comment"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(17, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(commentLabel, gridBagConstraints1);
        
        
        
        commentText.setColumns(40);
        commentText.setRows(3);
        commentText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        }
        );
        commentScroll.setViewportView(commentText);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(17, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 0.5;
        add(commentScroll, gridBagConstraints1);
        
        
        keyLabel.setText(I18nUtil.getBundle().getString("LBL_Key"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(keyLabel, gridBagConstraints1);
        
        
        valueLabel.setText(I18nUtil.getBundle().getString("LBL_Value"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 11, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(valueLabel, gridBagConstraints1);
        
        
        
        valueText.setColumns(40);
        valueText.setRows(3);
        valueText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        }
        );
        valueScroll.setViewportView(valueText);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 11, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(valueScroll, gridBagConstraints1);
        
        
        keyBundleCombo.setEditable(true);
        keyBundleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBundleComboActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(7, 12, 0, 11);
        gridBagConstraints1.weighty = 1.0;
        add(keyBundleCombo, gridBagConstraints1);
        
        
        replaceFormatTextField.setEditable(false);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        add(replaceFormatTextField, gridBagConstraints1);
        
        
        replaceFormatButton.setText("...");
        replaceFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceFormatButtonActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(12, 0, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
        add(replaceFormatButton, gridBagConstraints1);
        
        
        replaceFormatLabel.setText(I18nUtil.getBundle().getString("LBL_ReplaceFormat"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(replaceFormatLabel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void replaceFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceFormatButtonActionPerformed
        final Dialog[] dialogs = new Dialog[1];
        final HelpStringCustomEditor customPanel = new HelpStringCustomEditor(replaceFormatTextField.getText(), I18nUtil.getReplaceFormatItems(), I18nUtil.getReplaceHelpItems());

        DialogDescriptor dd = new DialogDescriptor(
            customPanel,
            I18nUtil.getBundle().getString("LBL_ReplaceStringFormatEditor"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        String newText = (String)customPanel.getPropertyValue();
                        
                        if(!newText.equals(replaceFormatTextField.getText())) {
                            replaceFormatTextField.setText(newText);
                            i18nString.setReplaceFormat(newText);
                            
                            // Reset option as well.
                            ((I18nOptions)SharedClassObject.findObject(I18nOptions.class, true)).setReplaceJavaCode(newText);
                        }
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }
        });
        dialogs[0] = TopManager.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
    }//GEN-LAST:event_replaceFormatButtonActionPerformed

    private void keyBundleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBundleComboActionPerformed
        if(DUMMY_ACTION.equals(evt.getActionCommand()))
            return;

        String key = (String)keyBundleCombo.getSelectedItem();
        
        i18nString.setKey(key);
        
        if(i18nString.getValueForKey(key) != null)
            i18nString.setValue(i18nString.getValueForKey(key));
        
        if(i18nString.getCommentForKey(key) != null)
            i18nString.setComment(i18nString.getCommentForKey(key));
    }//GEN-LAST:event_keyBundleComboActionPerformed

    private void commentTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextFocusLost
        i18nString.setComment(commentText.getText());
    }//GEN-LAST:event_commentTextFocusLost

    private void valueTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextFocusLost
        i18nString.setValue(valueText.getText());
    }//GEN-LAST:event_valueTextFocusLost
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane commentScroll;
    private javax.swing.JTextArea commentText;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane valueScroll;
    private javax.swing.JTextArea valueText;
    private javax.swing.JComboBox keyBundleCombo;
    private javax.swing.JTextField replaceFormatTextField;
    private javax.swing.JButton replaceFormatButton;
    private javax.swing.JLabel replaceFormatLabel;
    // End of variables declaration//GEN-END:variables

}
