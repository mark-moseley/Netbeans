/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestSuiteTargetPanel.java
 *
 * Created on April 10, 2002, 1:46 PM
 */

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.StringTokenizer;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.JDialog;
import javax.swing.text.Document;
import javax.swing.SwingUtilities;
import org.openide.util.Utilities;
import javax.swing.DefaultComboBoxModel;
import org.openide.loaders.TemplateWizard;
import org.netbeans.modules.java.JavaDataObject;
import java.awt.CardLayout;
import org.openide.loaders.DataObject;
import java.net.URL;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestTypeTemplatePanel extends javax.swing.JPanel implements WizardDescriptor.Panel {
    
    private ChangeListener listener=null;
    private static final String DEFAULT_NAME="<default name>";
    
    /** Creates new form TestSuitePanel1 */
    public TestTypeTemplatePanel() {
        initComponents();
        templateCombo.setRenderer(new WizardIterator.MyCellRenderer());
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {fireStateChanged();}
        };
        nameField.getDocument().addDocumentListener(list);
        fireStateChanged();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        templateCombo = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();
        noDescription = new javax.swing.JLabel();
        htmlBrowser = new org.openide.awt.HtmlBrowser();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Name: ");
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(nameLabel, gridBagConstraints);

        nameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(nameField, gridBagConstraints);

        templateLabel.setText("Select a Template: ");
        templateLabel.setLabelFor(templateCombo);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(templateLabel, gridBagConstraints);

        templateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                templateComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(templateCombo, gridBagConstraints);

        descriptionLabel.setText("Template Description: ");
        descriptionLabel.setLabelFor(htmlBrowser);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(descriptionLabel, gridBagConstraints);

        descriptionPanel.setLayout(new java.awt.CardLayout());

        descriptionPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        noDescription.setText("No description");
        noDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionPanel.add(noDescription, "no");

        htmlBrowser.setEnableLocation(false);
        htmlBrowser.setStatusLineVisible(false);
        htmlBrowser.setPreferredSize(new java.awt.Dimension(400, 200));
        htmlBrowser.setToolbarVisible(false);
        htmlBrowser.setEnableHome(false);
        htmlBrowser.setAutoscrolls(true);
        descriptionPanel.add(htmlBrowser, "yes");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(descriptionPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void templateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboActionPerformed
        URL url=null;
        DataObject dob=(DataObject)templateCombo.getSelectedItem();
        if (dob!=null)
            url=TemplateWizard.getDescription(dob);
        if (url==null) {
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "no");
        } else {
            htmlBrowser.setURL(url);
            ((CardLayout)descriptionPanel.getLayout()).show(descriptionPanel, "yes");
        }
    }//GEN-LAST:event_templateComboActionPerformed

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        nameField.selectAll();
    }//GEN-LAST:event_nameFieldFocusGained

    public void addChangeListener(ChangeListener changeListener) {
        if (listener != null) throw new IllegalStateException ();
        listener = changeListener;
    }    
    
    public java.awt.Component getComponent() {
        return this;
    }    
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(TestSuiteTargetPanel.class);
    }
    
    public void readSettings(Object obj) {
        templateCombo.setModel(new DefaultComboBoxModel(WizardIterator.getTestTypeTemplates()));
        templateComboActionPerformed(null);
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        listener = null;
    }
    
    public void storeSettings(Object obj) {
        TemplateWizard wizard=(TemplateWizard)obj;
        String name=nameField.getText();
        if (DEFAULT_NAME.equals(name))
            name=null;
        wizard.putProperty(WizardIterator.TESTTYPE_NAME_PROPERTY, name);
        Object template=templateCombo.getSelectedItem();
        wizard.putProperty(WizardIterator.TESTTYPE_TEMPLATE_PROPERTY, template);

    }

    private void fireStateChanged() {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (listener != null) {
                    listener.stateChanged (new ChangeEvent (this));
                }
                if (nameField.getText().equals ("")) {
                    nameField.setText(DEFAULT_NAME);
                    nameField.selectAll();
                }
            }
        });            
    }
    
    public boolean isValid() {
        return DEFAULT_NAME.equals(nameField.getText())||Utilities.isJavaIdentifier(nameField.getText());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField nameField;
    private org.openide.awt.HtmlBrowser htmlBrowser;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JComboBox templateCombo;
    private javax.swing.JLabel noDescription;
    // End of variables declaration//GEN-END:variables
    
}
