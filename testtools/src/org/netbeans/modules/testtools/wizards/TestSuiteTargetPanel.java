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

import java.net.URL;
import java.awt.Component;
import java.awt.CardLayout;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.SwingUtilities;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.java.JavaDataObject;

/** Wizard Panel with Test Suite Target selection
* @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
*/
public class TestSuiteTargetPanel extends JPanel implements WizardDescriptor.Panel {

    private ChangeListener listener=null;
    private static final String DEFAULT_NAME="<default name>";
    private boolean modified=true;

    /** Creates new form TestSuiteTargetPanel */
    public TestSuiteTargetPanel() {
        initComponents();
        templateCombo.setRenderer(new WizardIterator.MyCellRenderer());
        templateCombo.setModel(new DefaultComboBoxModel(WizardIterator.getSuiteTemplates()));
        templateComboActionPerformed(null);
        DocumentListener list=new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {fireStateChanged();}
            public void removeUpdate(DocumentEvent e) {fireStateChanged();}
            public void changedUpdate(DocumentEvent e) {fireStateChanged();}
        };
        nameField.getDocument().addDocumentListener(list);
        packageField.getDocument().addDocumentListener(list);
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
        packageLabel = new javax.swing.JLabel();
        packageField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        templateCombo = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();
        descriptionPanel = new javax.swing.JPanel();
        noDescription = new javax.swing.JLabel();
        htmlBrowser = new org.openide.awt.HtmlBrowser();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(408, 334));
        setMinimumSize(new java.awt.Dimension(135, 156));
        nameLabel.setText("Name: ");
        nameLabel.setDisplayedMnemonic(110);
        nameLabel.setLabelFor(nameField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(nameField, gridBagConstraints);

        packageLabel.setText("Package: ");
        packageLabel.setDisplayedMnemonic(112);
        packageLabel.setLabelFor(packageField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(packageLabel, gridBagConstraints);

        packageField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                packageFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(packageField, gridBagConstraints);

        templateLabel.setText("Select a Template: ");
        templateLabel.setDisplayedMnemonic(84);
        templateLabel.setLabelFor(templateCombo);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(templateCombo, gridBagConstraints);

        descriptionLabel.setText("Template Description: ");
        descriptionLabel.setDisplayedMnemonic(68);
        descriptionLabel.setLabelFor(htmlBrowser);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.weighty = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(descriptionPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void templateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboActionPerformed
        modified=true;
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

    private void packageFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_packageFieldFocusGained
        packageField.selectAll();
    }//GEN-LAST:event_packageFieldFocusGained

    private void nameFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFieldFocusGained
        nameField.selectAll();
    }//GEN-LAST:event_nameFieldFocusGained

    /** adds ChangeListener of current Panel
     * @param changeListener ChangeListener */    
    public void addChangeListener(ChangeListener changeListener) {
        if (listener != null) throw new IllegalStateException ();
        listener = changeListener;
    }    
    
    /** returns current Panel
     * @return Component */    
    public Component getComponent() {
        return this;
    }    
    
    /** returns Help Context
     * @return HelpCtx */    
    public HelpCtx getHelp() {
        return new HelpCtx(TestSuiteTargetPanel.class);
    }
    
    /** read settings from given Object
     * @param obj TemplateWizard with settings */    
    public void readSettings(Object obj) {}
    
    /** removes Change Listener of current Panel
     * @param changeListener ChangeListener */    
    public void removeChangeListener(ChangeListener changeListener) {
        listener = null;
    }
    
    /** stores settings to given Object
     * @param obj TemplateWizard with settings */    
    public void storeSettings(Object obj) {
        WizardSettings set=WizardSettings.get(obj);
        String name=nameField.getText();
        if (DEFAULT_NAME.equals(name))
            name=null;
        set.suiteName=name;
        set.suitePackage=packageField.getText().replace('.','/');
        set.suiteTemplate=(DataObject)templateCombo.getSelectedItem();
        if (modified) {
            set.templateMethods=WizardIterator.getTemplateMethods((JavaDataObject)set.suiteTemplate);
            set.methods=null;
            modified=false;
        }

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
    
    /** test current Panel state for data validity
     * @return boolean true if data are valid and Wizard can continue */    
    public boolean isValid() {
        StringTokenizer st=new StringTokenizer(packageField.getText().replace('.','/'),"/");
        while (st.hasMoreTokens())
            if (!Utilities.isJavaIdentifier(st.nextToken()))
                return false;
        return DEFAULT_NAME.equals(nameField.getText())||Utilities.isJavaIdentifier(nameField.getText());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField packageField;
    private org.openide.awt.HtmlBrowser htmlBrowser;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JComboBox templateCombo;
    private javax.swing.JLabel noDescription;
    // End of variables declaration//GEN-END:variables
     
}
