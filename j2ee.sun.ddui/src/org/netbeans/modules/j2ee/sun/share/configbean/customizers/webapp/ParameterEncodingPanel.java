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
/*
 * ParameterEncodingPanel.java
 *
 * Created on August 31, 2005, 2:27 PM
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.sun.share.CharsetMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;


/**
 *
 * @author  peterw99
 */
public class ParameterEncodingPanel extends javax.swing.JPanel {

    private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
        "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
    
    public static final String PROP_FORM_HINT_FIELD = "formHintField"; // NOI18N
    public static final String PROP_DEFAULT_CHARSET = "defaultCharset"; // NOI18N
    
    // Temporary storage for quick determination of presence of parameter encoding
    // entry
    private String defaultCharset;
    private String formHintField;

    private DefaultComboBoxModel defaultCharsetCbxModel;

    // Listens for selections in the charset combobox.
    private ActionListener defaultCharsetActionListener;
    
    // Listens for changes to the default list of charsets
    private PropertyChangeListener charsetChangeListener;

    // true if AS 8.1+ fields are visible.
    private boolean as81FeaturesVisible;
    
    /** Creates new form ParameterEncodingPanel */
    public ParameterEncodingPanel() {
        initComponents();
        initUserComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblParameterEncoding = new javax.swing.JLabel();
        jPnlParameterEncoding = new javax.swing.JPanel();
        jLblDefaultCharset = new javax.swing.JLabel();
        jCbxDefaultCharset = new javax.swing.JComboBox();
        jLblFormHintField = new javax.swing.JLabel();
        jTxtFormHintField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLblParameterEncoding.setLabelFor(jPnlParameterEncoding);
        jLblParameterEncoding.setText(webappBundle.getString("LBL_ParameterEncoding"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 4);
        add(jLblParameterEncoding, gridBagConstraints);

        jPnlParameterEncoding.setLayout(new java.awt.GridBagLayout());

        jPnlParameterEncoding.setBorder(new javax.swing.border.EtchedBorder());
        jLblDefaultCharset.setLabelFor(jCbxDefaultCharset);
        jLblDefaultCharset.setText(webappBundle.getString("LBL_DefaultCharset_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        jPnlParameterEncoding.add(jLblDefaultCharset, gridBagConstraints);

        jCbxDefaultCharset.setPrototypeDisplayValue("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        jPnlParameterEncoding.add(jCbxDefaultCharset, gridBagConstraints);

        jLblFormHintField.setLabelFor(jTxtFormHintField);
        jLblFormHintField.setText(webappBundle.getString("LBL_FormHintField_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlParameterEncoding.add(jLblFormHintField, gridBagConstraints);

        jTxtFormHintField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtFormHintFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPnlParameterEncoding.add(jTxtFormHintField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        add(jPnlParameterEncoding, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jTxtFormHintFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtFormHintFieldKeyReleased
        String oldFormHintField = formHintField;
        formHintField = jTxtFormHintField.getText();
        firePropertyChange(PROP_FORM_HINT_FIELD, oldFormHintField, formHintField);
    }//GEN-LAST:event_jTxtFormHintFieldKeyReleased
    
    private void jCbxDefaultCharsetActionPerformed(java.awt.event.ActionEvent evt) {
        String oldDefaultCharset = defaultCharset;
        Object item = defaultCharsetCbxModel.getSelectedItem();
        if(item instanceof CharsetMapping) {
            defaultCharset = ((CharsetMapping) item).getAlias();
        } else {
            defaultCharset = null;
        }
        firePropertyChange(PROP_DEFAULT_CHARSET, oldDefaultCharset, defaultCharset);        
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxDefaultCharset;
    private javax.swing.JLabel jLblDefaultCharset;
    private javax.swing.JLabel jLblFormHintField;
    private javax.swing.JLabel jLblParameterEncoding;
    private javax.swing.JPanel jPnlParameterEncoding;
    private javax.swing.JTextField jTxtFormHintField;
    // End of variables declaration//GEN-END:variables

    private void initUserComponents() {
        // Init default charset combo box
        defaultCharsetCbxModel = new DefaultComboBoxModel();
        defaultCharsetCbxModel.addElement(""); // NOI18N
        SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
        for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
            CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
            defaultCharsetCbxModel.addElement(cm);
        }
        jCbxDefaultCharset.setModel(defaultCharsetCbxModel);

        defaultCharsetActionListener = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxDefaultCharsetActionPerformed(evt);
            }
        };
        
        charsetChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                updateDefaultCharsetModel();
            }
        };
    }
    
    public void addListeners() {
        jCbxDefaultCharset.addActionListener(defaultCharsetActionListener);
        CharsetMapping.addPropertyChangeListener(charsetChangeListener);
    }

    public void removeListeners() {
        jCbxDefaultCharset.removeActionListener(defaultCharsetActionListener);
        CharsetMapping.removePropertyChangeListener(charsetChangeListener);
    }
    
    public void initFields(ASDDVersion asVersion, String defaultCharset, String formHintField, boolean enabled) {
        if(ASDDVersion.SUN_APPSERVER_8_0.compareTo(asVersion) >= 0) {
            showAS81Fields();
        } else {
            hideAS81Fields();
        }
        
        enableFields(enabled);
        
        if(enabled) {
            // init parameter encoding fields
            this.defaultCharset = defaultCharset;
            this.formHintField = formHintField;

            if(as81FeaturesVisible) {
                defaultCharsetCbxModel.setSelectedItem(CharsetMapping.getCharsetMapping(defaultCharset));
            }
            jTxtFormHintField.setText(formHintField);
        } else {
            if(as81FeaturesVisible) {
                jCbxDefaultCharset.setSelectedItem(null);
            }
            jTxtFormHintField.setText("");
        }
    }
        
    // TODO after 5.0, generalize version based field display for multiple (> 2)
    // appserver versions.
    private void showAS81Fields() {
        if(!as81FeaturesVisible) {
            jLblDefaultCharset.setVisible(true);
            jCbxDefaultCharset.setVisible(true);
            as81FeaturesVisible = true;
        }
    }
    
    private void hideAS81Fields() {
        if(as81FeaturesVisible) {
            jLblDefaultCharset.setVisible(false);
            jCbxDefaultCharset.setVisible(false);
            as81FeaturesVisible = false;
        }
    }
    
    public String getFormHintField() {
        return formHintField;
    }
    
    public String getDefaultCharset() {
        return defaultCharset;
    }
    
    private void enableFields(boolean enable) {
        jLblParameterEncoding.setEnabled(enable);
        jLblDefaultCharset.setEnabled(enable);
        jCbxDefaultCharset.setEnabled(enable);
        jLblFormHintField.setEnabled(enable);
        jTxtFormHintField.setEnabled(enable);
        jTxtFormHintField.setEditable(enable);
    }
    
    private void updateDefaultCharsetModel() {
        Object mapping = defaultCharsetCbxModel.getSelectedItem();
        CharsetMapping oldMapping;

        if(mapping instanceof CharsetMapping) {
            oldMapping = (CharsetMapping) mapping;
        } else {
            oldMapping = null;
        }

        defaultCharsetCbxModel = new DefaultComboBoxModel();
        defaultCharsetCbxModel.addElement(""); // NOI18N
        SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
        for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
            CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
            defaultCharsetCbxModel.addElement(cm);
        }
        jCbxDefaultCharset.setModel(defaultCharsetCbxModel);

        if(oldMapping != null) {
            oldMapping = CharsetMapping.getCharsetMapping(oldMapping.getCharset());
        }

        defaultCharsetCbxModel.setSelectedItem(oldMapping);
    }
    
}
