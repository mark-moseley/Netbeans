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


package org.netbeans.modules.i18n;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;


/**
 * Panel which is used for customizing key-value pair (and comment also) encapsulated by <code>I18nString</code> object.
 * It's used inside <code>I18nPanel</code>.
 *
 * @author  Peter Zavadsky
 * @see I18nString
 * @see I18nPanel
 */
public class PropertyPanel extends JPanel {
    
    /** property representing the I18String. Change is fired when the i18string changes.
     * Old and new objects are not sent with the notification.
     */
    public static final String PROP_STRING = "propString"; 
    
    /** Helper name for dummy action command. */
    private static final String DUMMY_ACTION = "dont_proceed"; // NOI18N
    
    /** Customized <code>I18nString</code>. */
    protected I18nString i18nString;

    /** Internal flag to block handling of changes to the key jtextfield,
     * which didn't originate from the user but from the code. If this is >0, 
     * values are just being pushed to the UI, if <=0, values are being received
     * from the ui.
     **/
    private int internalTextChange = 0;    
    
    
    /** Creates new <code>PropertyPanel</code>. */
    public PropertyPanel() {
        initComponents();
        myInitComponents();
        initAccessibility();
    }

    public void setEnabled(boolean ena) {
        super.setEnabled(ena);
        commentText.setEnabled(ena);
        commentLabel.setEnabled(ena);
        commentScroll.setEnabled(ena);
        
        keyBundleCombo.setEnabled(ena);
        keyLabel.setEnabled(ena);
        
        replaceFormatButton.setEnabled(ena);
        replaceFormatLabel.setEnabled(ena);
        replaceFormatTextField.setEnabled(ena);
        
        valueLabel.setEnabled(ena); 
        valueText.setEnabled(ena);
        valueScroll.setEnabled(ena);
    }
    
    /** Seter for <code>i18nString</code> property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;
        
        updateAllValues();
        firePropertyChange(PROP_STRING, null,null);
    }

    
    /** Initializes UI values. */
    void updateAllValues() {
        updateBundleKeys();        
        updateKey();
        updateValue();
        updateComment();
    } 
    
    /** Updates selected item of <code>keyBundleCombo</code> UI. 
     */
    private void updateKey() {       
        String key = i18nString.getKey();
        
        if(key == null || !key.equals(keyBundleCombo.getSelectedItem()) ) {
            // Trick to avoid firing key selected property change.
            String oldActionCommand = keyBundleCombo.getActionCommand();
            keyBundleCombo.setActionCommand(DUMMY_ACTION);

            internalTextChange++;
            keyBundleCombo.setSelectedItem(key == null ? "" : key); // NOI18N
            internalTextChange--;
            
            keyBundleCombo.setActionCommand(oldActionCommand);
        }
        
        updateReplaceText();
    }

    /** Updates <code>valueText</code> UI. 
     */
    private void updateValue() {            
        String value = i18nString.getValue();
        
        if(!valueText.getText().equals(value)) {
            valueText.setText(value == null ? "" : value); // NOI18N
        }
       
       updateReplaceText();            
    }
    
    /** Updates <code>commentText</code> UI. */
    private void updateComment() {
        String comment = i18nString.getComment();
        
        if(!commentText.getText().equals(comment)) {
            commentText.setText(comment == null ? "" : comment); // NOI18N
        }
    }
    
    /** Updates <code>replaceFormatTextField</code>. */
    protected void updateReplaceText() {
        replaceFormatTextField.setText(i18nString.getReplaceString());
    }
    
    /** Updates <code>keyBundleCombo</code> UI. */
    void updateBundleKeys() {
        // Trick to avoid firing key selected property change.
        String oldActionCommand = keyBundleCombo.getActionCommand();
        keyBundleCombo.setActionCommand(DUMMY_ACTION);

        internalTextChange++;
        keyBundleCombo.setModel(new DefaultComboBoxModel(i18nString.getSupport().getResourceHolder().getAllKeys()));
        internalTextChange--;
        
        keyBundleCombo.setActionCommand(oldActionCommand);
        
        updateKey();
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString("ACS_PropertyPanel"));        
        valueText.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString("ACS_valueText"));        
        commentText.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString("ACS_commentText"));        
        replaceFormatButton.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString("ACS_CTL_Format"));        
        replaceFormatTextField.getAccessibleContext().setAccessibleDescription(I18nUtil.getBundle().getString("ACS_replaceFormatTextField"));        
    }
    
    private void myInitComponents() {
        // hook the Key combobox edit-field for changes
        ((javax.swing.JTextField)keyBundleCombo.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {              
                    public void changedUpdate(DocumentEvent e) { keyBundleTextChanged();}
                    public void insertUpdate(DocumentEvent e) {keyBundleTextChanged();}
                    public void removeUpdate(DocumentEvent e) {keyBundleTextChanged();}
                }
               );
        valueText.getDocument().addDocumentListener(new DocumentListener() {              
                    public void changedUpdate(DocumentEvent e) { valueTextChanged();}
                    public void insertUpdate(DocumentEvent e) {valueTextChanged();}
                    public void removeUpdate(DocumentEvent e) {valueTextChanged();}
                }
               );

    }
    
    private void keyBundleTextChanged() {
        if (internalTextChange==0) {
            String key = ((javax.swing.JTextField)keyBundleCombo.getEditor().getEditorComponent()).getText();

            if (!key.equals(i18nString.getKey())) {        
                i18nString.setKey(key);
                firePropertyChange(PROP_STRING,null,null);
            } 
        }
    }

    private void valueTextChanged() {
        i18nString.setValue(valueText.getText());
//        updateValue();
        firePropertyChange(PROP_STRING,null,null);                
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        commentLabel = new javax.swing.JLabel();
        commentScroll = new javax.swing.JScrollPane();
        commentText = new javax.swing.JTextArea();
        keyLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valueScroll = new javax.swing.JScrollPane();
        valueText = new javax.swing.JTextArea();
        keyBundleCombo = new javax.swing.JComboBox();
        replaceFormatTextField = new javax.swing.JTextField();
        replaceFormatLabel = new javax.swing.JLabel();
        replaceFormatButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        commentLabel.setLabelFor(commentText);
        commentLabel.setText(I18nUtil.getBundle().getString("LBL_Comment"));
        commentLabel.setDisplayedMnemonic((I18nUtil.getBundle().getString("LBL_Comment_Mnem")).charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(commentLabel, gridBagConstraints);

        commentText.setColumns(40);
        commentText.setRows(3);
        commentText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        });

        commentScroll.setViewportView(commentText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(commentScroll, gridBagConstraints);

        keyLabel.setLabelFor(keyBundleCombo);
        keyLabel.setText(I18nUtil.getBundle().getString("LBL_Key"));
        keyLabel.setDisplayedMnemonic((I18nUtil.getBundle().getString("LBL_Key_Mnem")).charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(keyLabel, gridBagConstraints);

        valueLabel.setLabelFor(valueText);
        valueLabel.setText(I18nUtil.getBundle().getString("LBL_Value"));
        valueLabel.setDisplayedMnemonic((I18nUtil.getBundle().getString("LBL_Value_Mnem")).charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 0);
        add(valueLabel, gridBagConstraints);

        valueText.setColumns(40);
        valueText.setRows(3);
        valueText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        });

        valueScroll.setViewportView(valueText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 11);
        add(valueScroll, gridBagConstraints);

        keyBundleCombo.setEditable(true);
        keyBundleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyBundleComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(keyBundleCombo, gridBagConstraints);

        replaceFormatTextField.setColumns(40);
        replaceFormatTextField.setEditable(false);
        replaceFormatTextField.selectAll();
        replaceFormatTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                replaceFormatTextFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(replaceFormatTextField, gridBagConstraints);

        replaceFormatLabel.setLabelFor(replaceFormatTextField);
        replaceFormatLabel.setText(I18nUtil.getBundle().getString("LBL_ReplaceFormat"));
        replaceFormatLabel.setDisplayedMnemonic((I18nUtil.getBundle().getString("LBL_ReplaceFormat_Mnem")).charAt(0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(replaceFormatLabel, gridBagConstraints);

        replaceFormatButton.setMnemonic((I18nUtil.getBundle().getString("CTL_Format_Mnem")).charAt(0));
        replaceFormatButton.setText(I18nUtil.getBundle().getString("CTL_Format"));
        replaceFormatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceFormatButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 11);
        add(replaceFormatButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void replaceFormatTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_replaceFormatTextFieldFocusGained
        // Accessibility
        replaceFormatTextField.selectAll();
    }//GEN-LAST:event_replaceFormatTextFieldFocusGained

    private void replaceFormatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceFormatButtonActionPerformed
        final Dialog[] dialogs = new Dialog[1];
        final HelpStringCustomEditor customPanel = new HelpStringCustomEditor(
                                                        i18nString.getReplaceFormat(),
                                                        I18nUtil.getReplaceFormatItems(),
                                                        I18nUtil.getReplaceHelpItems(),
                                                        I18nUtil.getBundle().getString("LBL_ReplaceCodeFormat"),
                                                        Util.getChar("LBL_ReplaceCodeFormat_mne"),
                                                        I18nUtil.PE_REPLACE_CODE_HELP_ID);

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
                            i18nString.setReplaceFormat(newText);                            
                            updateReplaceText();
                            firePropertyChange(PROP_STRING,null,null);
                            
                            // Reset option as well.
                            I18nUtil.getOptions().setReplaceJavaCode(newText);
                        }
                        
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                        dialogs[0].setVisible(false);
                        dialogs[0].dispose();
                    }
                }
                       });
                       dialogs[0] = DialogDisplayer.getDefault().createDialog(dd);
        dialogs[0].setVisible(true);
    }//GEN-LAST:event_replaceFormatButtonActionPerformed

    private void keyBundleComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyBundleComboActionPerformed
        if(DUMMY_ACTION.equals(evt.getActionCommand()))
            return;

        String key = (String)keyBundleCombo.getSelectedItem();
        i18nString.setKey(key);
        updateKey();
        
        String value = i18nString.getSupport().getResourceHolder().getValueForKey(key);
        if(value != null) {
            i18nString.setValue(value);
            updateValue();
        }
        
        String comment = i18nString.getSupport().getResourceHolder().getCommentForKey(key);
        if(comment != null) {
            i18nString.setComment(comment);
            updateComment();
        }
        firePropertyChange(PROP_STRING,null,null);
    }//GEN-LAST:event_keyBundleComboActionPerformed

    private void commentTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextFocusLost
        i18nString.setComment(commentText.getText());
        updateComment();
        firePropertyChange(PROP_STRING,null,null);        
    }//GEN-LAST:event_commentTextFocusLost

    private void valueTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextFocusLost
        valueTextChanged();
    }//GEN-LAST:event_valueTextFocusLost
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel commentLabel;
    private javax.swing.JScrollPane commentScroll;
    private javax.swing.JTextArea commentText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox keyBundleCombo;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JButton replaceFormatButton;
    private javax.swing.JLabel replaceFormatLabel;
    private javax.swing.JTextField replaceFormatTextField;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane valueScroll;
    private javax.swing.JTextArea valueText;
    // End of variables declaration//GEN-END:variables
        
}
