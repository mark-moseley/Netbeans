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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;


/**
 * Panel which provides GUI for i18n action.
 * Customizes <code>I18nString</code> object and is used by <code>I18nSupport<code> for i18n-izing 
 * one source.
 *
 * @author  Peter Zavadsky
 */
public class I18nPanel extends JPanel {

    /** <code>I18nString</code> cusomized in this panel. */
    private I18nString i18nString;
    
    /** Helper bundle used for i18n-zing strings in this source.  */
    private static ResourceBundle bundle;

    /** Helper property change support. */
    private PropertyChangeListener propListener;
    
    /** Generated serial version ID. */
    static final long serialVersionUID =-6982482491017390786L;
    
    
    /** Creates new I18nPanel. */
    public I18nPanel(PropertyPanel propertyPanel) {
        this(propertyPanel, true);
    }

    /** Creates i18n panel.
     * @param propertyPanel panel for customizing i18n strings 
     * @param withButtons if panel with replace, skip ect. buttons should be added */
    public I18nPanel(PropertyPanel propertyPanel, boolean withButtons) {
        // Init bundle.
        bundle = I18nUtil.getBundle();
        
        this.propertyPanel = propertyPanel;
        
        initComponents();
        
        if(!withButtons)
            remove(buttonsPanel);
        
    }

    
    /** Overrides superclass method to set default button. */
    public void addNotify() {
        super.addNotify();
        
        if(SwingUtilities.isDescendingFrom(replaceButton, this))
            getRootPane().setDefaultButton(replaceButton);
    }
    
    /** Getter for <code>i18nString</code>. */
    public I18nString getI18nString() {
        return i18nString;
    }
    
    /** Setter for i18nString property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;

        ((PropertyPanel)propertyPanel).setI18nString(i18nString);
        ((ResourcePanel)resourcePanel).setI18nString(i18nString);
        
        // Set listener to enable/disable replace button.
        resourcePanel.addPropertyChangeListener(WeakListener.propertyChange(
            propListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(ResourcePanel.PROP_RESOURCE.equals(evt.getPropertyName())) {
                        replaceButton.setEnabled(evt.getNewValue() != null);
                        ((PropertyPanel)propertyPanel).updateBundleKeys();
                    }
                }
            },
            resourcePanel
        ));

        replaceButton.setEnabled(i18nString.getSupport().getResourceHolder().getResource() != null);
    }

    /** Replace button accessor. */
    JButton getReplaceButton() {
        return replaceButton;
    }
    
    /** Skip button accessor. */
    JButton getSkipButton() {
        return skipButton;
    }

    /** Info button accessor. */
    JButton getInfoButton() {
        return infoButton;
    }
    
    /** Cancel button accessor. */
    JButton getCancelButton() {
        return cancelButton;
    }
    
    /** Enables/disables buttons. */
    private void buttonsEnableDisable() {
        boolean isBundle = i18nString.getSupport().getResourceHolder().getResource() != null;
        replaceButton.setEnabled(isBundle);
    }

    /** Creates <code>ResourcePanel</code>. */
    private JPanel createResourcePanel() {
        return new ResourcePanel();
    }
    
    /** Creates <code>PropertyPanel</code>. */
    private JPanel createPropertyPanel() {
        return new PropertyPanel();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonsPanel = new javax.swing.JPanel();
        replaceButton = new javax.swing.JButton();
        skipButton = new javax.swing.JButton();
        infoButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        propertyPanel = propertyPanel; // Ugly trick to cheat form.
        resourcePanel = createResourcePanel();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        buttonsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        replaceButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/i18n/Bundle").getString("CTL_ReplaceButton"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(17, 12, 11, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints2.weightx = 1.0;
        buttonsPanel.add(replaceButton, gridBagConstraints2);
        
        
        skipButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/i18n/Bundle").getString("CTL_SkipButton"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(17, 5, 11, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        buttonsPanel.add(skipButton, gridBagConstraints2);
        
        
        infoButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/i18n/Bundle").getString("CTL_InfoButton"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.insets = new java.awt.Insets(17, 5, 11, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
        buttonsPanel.add(infoButton, gridBagConstraints2);
        
        
        helpButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/i18n/Bundle").getString("CTL_HelpButton"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        }
        );
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 4;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.insets = new java.awt.Insets(17, 5, 11, 11);
        buttonsPanel.add(helpButton, gridBagConstraints2);
        
        
        cancelButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/i18n/Bundle").getString("CTL_CancelButton"));
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 3;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.insets = new java.awt.Insets(17, 5, 11, 0);
        buttonsPanel.add(cancelButton, gridBagConstraints2);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.weightx = 1.0;
        add(buttonsPanel, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(11, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(propertyPanel, gridBagConstraints1);
        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        add(resourcePanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

  private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
      HelpCtx help = new HelpCtx(I18nPanel.class);
      String sysprop = System.getProperty ("org.openide.actions.HelpAction.DEBUG"); // NOI18N
      if ("true".equals (sysprop) || "full".equals (sysprop)) // NOI18N
          System.err.println ("I18n module: Help button showing: " + help); // NOI18N, please do not comment out
      
      TopManager.getDefault ().showHelp (help);
      
      return;
  }//GEN-LAST:event_helpButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonsPanel;
  private javax.swing.JButton replaceButton;
  private javax.swing.JButton skipButton;
  private javax.swing.JButton infoButton;
  private javax.swing.JButton helpButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel propertyPanel;
  private javax.swing.JPanel resourcePanel;
  // End of variables declaration//GEN-END:variables

}