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

package org.netbeans.modules.i18n.java;


import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import org.netbeans.modules.i18n.HelpStringCustomEditor;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.cookies.SourceCookie;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;


/**
 * Custom panel used by i18n session for customizing java source 
 * specific replacing values stored in <code>JavaI18nSupport</code> instance.
 *
 * @author  Peter Zavadsky
 */
public class JavaReplacePanel extends JPanel {

    /** <code>JavaI18nSupport</code> which additional values to customize. */
    private JavaI18nSupport javaI18nSupport;
    
    /** Bundle in which are stored resources used in this source. */
    private final ResourceBundle bundle;
    
    
    /** Creates new form JavaCustomPanel.
     * @param <code>JavaI18nSupport</code> which additional values to customize. */
    public JavaReplacePanel(JavaI18nSupport javaI18nSupport) {
        this.javaI18nSupport = javaI18nSupport;
        
        // Init bundle.
        bundle = org.openide.util.NbBundle.getBundle(Util.class);
        initComponents();
        initAccessibility();

        updateValues();
    }

    
    /** Updates values to UI. */
    private void updateValues() {
        javaI18nSupport.createIdentifier();

        // Init generate check and formats.
        generateCheck.setSelected(javaI18nSupport.isGenerateField());
        setAllEnabled(generateCheck.isSelected());
        
        identifierTextField.setText(javaI18nSupport.getIdentifier());
        
        initTextField.setText(javaI18nSupport.getInitString());
    }

    /** Updates modifier components according to identifier changes. */
    private void updateModifiers() {
        FieldElement field = getFieldElement(identifierTextField.getText());

        int modifiers;
        
        if(field != null) {
            modifiers = field.getModifiers();
            enableModifiers(false);
            fieldTextField.setText(field.toString());
        } else {
            modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
            enableModifiers(true);
            fieldTextField.setText(""); // NOI18N
        }
        
        javaI18nSupport.setModifiers(modifiers);

        if(identifierTextField.getText().length() != 0) {
            if(Modifier.isPrivate(modifiers))
                privateRadio.setSelected(true);
            else if(Modifier.isProtected(modifiers))
                protectedRadio.setSelected(true);
            else if(Modifier.isPublic(modifiers))
                publicRadio.setSelected(true);
            else
                defaultRadio.setSelected(true);

            staticCheck.setSelected(Modifier.isStatic(modifiers));
            finalCheck.setSelected(Modifier.isFinal(modifiers));
            transientCheck.setSelected(Modifier.isTransient(modifiers));
        } else { 
            // default set of modifiers
            privateRadio.setSelected(true);
            staticCheck.setSelected(true);
            finalCheck.setSelected(true);
        }
    }

    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_JavaReplacePanel"));
        identifierLabel.setDisplayedMnemonic((bundle.getString("LBL_Identifier_Mnem")).charAt(0));
        initLabel.setDisplayedMnemonic((bundle.getString("LBL_InitFormat_Mnem")).charAt(0));
        fieldLabel.setDisplayedMnemonic((bundle.getString("LBL_Field_Mnem")).charAt(0));        
        fieldTextField.selectAll();
        initTextField.selectAll();
        staticCheck.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_staticCheck"));
        identifierTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_identifierTextField"));
        transientCheck.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_transientCheck"));
        defaultRadio.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_defaultRadio"));
        fieldTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_fieldTextField"));
        initButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_initButton"));
        publicRadio.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_publicRadio"));        
        generateCheck.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_generateCheck"));
        finalCheck.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_finalCheck"));
        privateRadio.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_privateRadio"));
        initTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_initTextField"));
        protectedRadio.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_protectedRadio"));
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        generateCheck = new javax.swing.JCheckBox();
        modifiersLabel = new javax.swing.JLabel();
        defaultRadio = new javax.swing.JRadioButton();
        privateRadio = new javax.swing.JRadioButton();
        protectedRadio = new javax.swing.JRadioButton();
        publicRadio = new javax.swing.JRadioButton();
        staticCheck = new javax.swing.JCheckBox();
        finalCheck = new javax.swing.JCheckBox();
        transientCheck = new javax.swing.JCheckBox();
        identifierLabel = new javax.swing.JLabel();
        identifierTextField = new javax.swing.JTextField();
        initLabel = new javax.swing.JLabel();
        initTextField = new javax.swing.JTextField();
        fieldLabel = new javax.swing.JLabel();
        fieldTextField = new javax.swing.JTextField();
        initButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        generateCheck.setMnemonic(Util.getChar("CTL_GenerateField_Mnem"));
        generateCheck.setSelected(true);
        generateCheck.setText(bundle.getString("CTL_GenerateField"));
        generateCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(generateCheck, gridBagConstraints);

        modifiersLabel.setText(bundle.getString("LBL_Modifiers"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(modifiersLabel, gridBagConstraints);

        defaultRadio.setMnemonic(Util.getChar("CTL_DefaultRadio_Mnem"));
        defaultRadio.setText(bundle.getString("CTL_DefaultRadio"));
        defaultRadio.setEnabled(false);
        defaultRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(defaultRadio, gridBagConstraints);

        privateRadio.setMnemonic('p');
        privateRadio.setSelected(true);
        privateRadio.setText(" private");
        privateRadio.setEnabled(false);
        privateRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(privateRadio, gridBagConstraints);

        protectedRadio.setMnemonic('o');
        protectedRadio.setText(" protected");
        protectedRadio.setEnabled(false);
        protectedRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protectedRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(protectedRadio, gridBagConstraints);

        publicRadio.setMnemonic('u');
        publicRadio.setText(" public");
        publicRadio.setEnabled(false);
        ButtonGroup radioGroup = new ButtonGroup();

        radioGroup.add(defaultRadio);
        radioGroup.add(privateRadio);
        radioGroup.add(protectedRadio);
        radioGroup.add(publicRadio);
        publicRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(publicRadio, gridBagConstraints);

        staticCheck.setMnemonic('a');
        staticCheck.setSelected(true);
        staticCheck.setText(" static");
        staticCheck.setEnabled(false);
        staticCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(staticCheck, gridBagConstraints);

        finalCheck.setMnemonic('f');
        finalCheck.setSelected(true);
        finalCheck.setText(" final");
        finalCheck.setEnabled(false);
        finalCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finalCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(finalCheck, gridBagConstraints);

        transientCheck.setMnemonic('t');
        transientCheck.setText(" transient");
        transientCheck.setEnabled(false);
        transientCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transientCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(transientCheck, gridBagConstraints);

        identifierLabel.setLabelFor(identifierTextField);
        identifierLabel.setText(bundle.getString("LBL_Identifier"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(identifierLabel, gridBagConstraints);

        identifierTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                identifierTextFieldActionPerformed(evt);
            }
        });

        identifierTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                identifierTextFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 11);
        add(identifierTextField, gridBagConstraints);

        initLabel.setLabelFor(initTextField);
        initLabel.setText(bundle.getString("LBL_InitFormat"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(initLabel, gridBagConstraints);

        initTextField.setEditable(false);
        initTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                initTextFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(initTextField, gridBagConstraints);

        fieldLabel.setLabelFor(fieldTextField);
        fieldLabel.setText(bundle.getString("LBL_Field"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 11, 0);
        add(fieldLabel, gridBagConstraints);

        fieldTextField.setEditable(false);
        fieldTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                fieldTextFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(fieldTextField, gridBagConstraints);

        initButton.setMnemonic(Util.getChar("CTL_Format_mne"));
        initButton.setText(bundle.getString("CTL_Format"));
        initButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(initButton, gridBagConstraints);

    }//GEN-END:initComponents

    private void fieldTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fieldTextFieldFocusGained
        // Accessibility:
        fieldTextField.selectAll();
    }//GEN-LAST:event_fieldTextFieldFocusGained

    private void initTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_initTextFieldFocusGained
        // Accessibility:
        initTextField.selectAll();
    }//GEN-LAST:event_initTextFieldFocusGained

    private void initButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initButtonActionPerformed
        final Dialog[] dialogs = new Dialog[1];
        final HelpStringCustomEditor customPanel = new HelpStringCustomEditor(
                                                    javaI18nSupport.getInitFormat(),
                                                    I18nUtil.getInitFormatItems(),
                                                    I18nUtil.getInitHelpItems(),
                                                    Util.getString("LBL_InitCodeFormat"),
                                                    Util.getChar("LBL_InitCodeFormat_mne"),
                                                    I18nUtil.PE_BUNDLE_CODE_HELP_ID);

        DialogDescriptor dd = new DialogDescriptor(
            customPanel,
            bundle.getString("LBL_InitStringFormatEditor"),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                        String newText = (String)customPanel.getPropertyValue();
                        
                        if(!newText.equals(javaI18nSupport.getInitFormat())) {
                            javaI18nSupport.setInitFormat(newText);
                            initTextField.setText(javaI18nSupport.getInitString());
                            
                            // Reset option as well.
                            I18nUtil.getOptions().setInitJavaCode(newText);
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
    }//GEN-LAST:event_initButtonActionPerformed

    private void identifierTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_identifierTextFieldFocusLost
        identifierTextFieldEventHandlerDelegate(evt);
    }//GEN-LAST:event_identifierTextFieldFocusLost

    private void identifierTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_identifierTextFieldActionPerformed
        identifierTextFieldEventHandlerDelegate(evt);
    }//GEN-LAST:event_identifierTextFieldActionPerformed

    /** Event handler delegate. */
    public void identifierTextFieldEventHandlerDelegate(AWTEvent evt) {
        // If the identifer was changed change identifier and update modifiers.
        if(!identifierTextField.getText().equals(javaI18nSupport.getIdentifier())) {
            javaI18nSupport.setIdentifier(identifierTextField.getText());
            updateModifiers();
        }
    }
    
    private void transientCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transientCheckActionPerformed
        if(transientCheck.isSelected()) {
            staticCheck.setSelected(false);
            staticCheck.setEnabled(false);
        } else {
            staticCheck.setEnabled(true);
        }
        modifiersActionPerformed();
    }//GEN-LAST:event_transientCheckActionPerformed

    private void finalCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finalCheckActionPerformed
        modifiersActionPerformed();
    }//GEN-LAST:event_finalCheckActionPerformed

    private void staticCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticCheckActionPerformed
        if(staticCheck.isSelected()) {
            transientCheck.setSelected(false);
            transientCheck.setEnabled(false);
        } else {
            transientCheck.setEnabled(true);
        }

        modifiersActionPerformed();
    }//GEN-LAST:event_staticCheckActionPerformed

    private void publicRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioActionPerformed
        modifiersActionPerformed();
    }//GEN-LAST:event_publicRadioActionPerformed

    private void protectedRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protectedRadioActionPerformed
        modifiersActionPerformed();
    }//GEN-LAST:event_protectedRadioActionPerformed

    private void privateRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioActionPerformed
        modifiersActionPerformed();
    }//GEN-LAST:event_privateRadioActionPerformed

    private void defaultRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultRadioActionPerformed
        modifiersActionPerformed();
    }//GEN-LAST:event_defaultRadioActionPerformed

    private void generateCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateCheckActionPerformed
        boolean selected = generateCheck.isSelected();
        
        if(selected != javaI18nSupport.isGenerateField()) {
            javaI18nSupport.setGenerateField(selected);
            
            setAllEnabled(selected);
        }
    }//GEN-LAST:event_generateCheckActionPerformed

    /** Event handler delegate. */
    private void modifiersActionPerformed() {
        int modifiers = 0;
        
        if(privateRadio.isSelected())
            modifiers |= Modifier.PRIVATE;
        else if(protectedRadio.isSelected())
            modifiers |= Modifier.PROTECTED;
        else if(publicRadio.isSelected())
            modifiers |= Modifier.PUBLIC;
        
        if(staticCheck.isSelected())
            modifiers |= Modifier.STATIC;
        if(finalCheck.isSelected())
            modifiers |= Modifier.FINAL;
        if(transientCheck.isSelected())
            modifiers |= Modifier.TRANSIENT;
        
        if(modifiers != javaI18nSupport.getModifiers())
            javaI18nSupport.setModifiers(modifiers);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox staticCheck;
    private javax.swing.JRadioButton defaultRadio;
    private javax.swing.JRadioButton privateRadio;
    private javax.swing.JTextField fieldTextField;
    private javax.swing.JCheckBox finalCheck;
    private javax.swing.JRadioButton protectedRadio;
    private javax.swing.JTextField identifierTextField;
    private javax.swing.JButton initButton;
    private javax.swing.JCheckBox transientCheck;
    private javax.swing.JTextField initTextField;
    private javax.swing.JLabel fieldLabel;
    private javax.swing.JLabel modifiersLabel;
    private javax.swing.JCheckBox generateCheck;
    private javax.swing.JRadioButton publicRadio;
    private javax.swing.JLabel identifierLabel;
    private javax.swing.JLabel initLabel;
    // End of variables declaration//GEN-END:variables

    /** Helper method. Enables/disables all componnent in field panel. */
    private void setAllEnabled(boolean enable) {
        defaultRadio.setEnabled(enable);
        privateRadio.setEnabled(enable);
        protectedRadio.setEnabled(enable);
        publicRadio.setEnabled(enable);
        
        staticCheck.setEnabled(enable);
        transientCheck.setEnabled(enable);
        finalCheck.setEnabled(enable);
        
        identifierTextField.setEnabled(enable);
        fieldTextField.setEnabled(enable);
        initButton.setEnabled(enable);
        
        if(enable)
            updateModifiers();
    }

    /** Helper method to find <code>FieldElement</code> in <code>sourceDataObject</code> 
     * document for specified string. */
    private FieldElement getFieldElement(String identifier) {
        DataObject sourceDataObject = javaI18nSupport.getSourceDataObject();
        if(sourceDataObject == null)
            return null;
        
        SourceElement sourceElem = ((SourceCookie)sourceDataObject.getCookie(SourceCookie.class)).getSource();
        ClassElement sourceClass = sourceElem.getClass(Identifier.create(sourceDataObject.getName()));

        if(sourceClass == null) {
            ClassElement[] classes = sourceElem.getClasses();

            // Find source class.
            for(int i=0; i<classes.length; i++) {
                int modifs = classes[i].getModifiers();
                if(classes[i].isClass() && Modifier.isPublic(modifs)) {
                    sourceClass = classes[i];
                    break;
                }
            }
        }
        
        if(sourceClass == null)
            return null;
        
        return sourceClass.getField(Identifier.create(identifier));
    }

    /** Helper method. Enables/disables modifiers components. */
    private void enableModifiers(boolean enable) {
        defaultRadio.setEnabled(enable);
        privateRadio.setEnabled(enable);
        protectedRadio.setEnabled(enable);
        publicRadio.setEnabled(enable);
        staticCheck.setEnabled(enable);
        finalCheck.setEnabled(enable);
        transientCheck.setEnabled(enable);
        
        // Enable/disable init format as well.
        initTextField.setEnabled(enable);
        initButton.setEnabled(enable);
    }
    
}
