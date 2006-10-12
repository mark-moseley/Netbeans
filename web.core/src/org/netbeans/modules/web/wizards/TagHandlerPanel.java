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

package org.netbeans.modules.web.wizards;

import org.openide.util.NbBundle;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** A single panel for a wizard - the GUI portion.
 *
 * @author  mk115033
 */
public class TagHandlerPanel extends javax.swing.JPanel {

    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
    private TagHandlerSelection wizardPanel;
    private String j2eeVersion;
    /** Create the wizard panel and set up some basic properties. */
    public TagHandlerPanel(TagHandlerSelection wizardPanel, String j2eeVersion) {
        this.wizardPanel=wizardPanel;
        this.j2eeVersion=j2eeVersion;
        initComponents();
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(TagHandlerPanel.class, "TITLE_tagHandlerPanel"));
        if (WebModule.J2EE_13_LEVEL.equals(j2eeVersion)) {
            simpleTagButton.setEnabled(false);
            bodyTagButton.setSelected(true);
        } else {
            simpleTagButton.setSelected(true);
        }
        /*
        // Optional: provide a special description for this pane.
        // You must have turned on WizardDescriptor.WizardPanel_helpDisplayed
        // (see descriptor in standard iterator template for an example of this).
        try {
            putClientProperty ("WizardPanel_helpURL", // NOI18N
                new URL ("nbresloc:/org/netbeans/modules/web/wizards/TagHandlerHelp.html")); // NOI18N
        } catch (MalformedURLException mfue) {
            throw new IllegalStateException (mfue.toString ());
        }
         */
        // a11y part
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
        jPanel1 = new javax.swing.JPanel();
        simpleTagButton = new javax.swing.JRadioButton();
        bodyTagButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        buttonGroup1.add(simpleTagButton);
        simpleTagButton.setMnemonic(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "LBL_SimpleTag_Mnemonic").charAt(0));
        simpleTagButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "OPT_SimpleTag"));
        simpleTagButton.setMargin(new java.awt.Insets(2, 2, 0, 2));
        simpleTagButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TagHandlerPanel.this.itemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(simpleTagButton, gridBagConstraints);
        simpleTagButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("DESC_SimpleTag"));

        buttonGroup1.add(bodyTagButton);
        bodyTagButton.setMnemonic(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "LBL_BodyTag_Mnemonic").charAt(0));
        bodyTagButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "OPT_BodyTag"));
        bodyTagButton.setMargin(new java.awt.Insets(0, 2, 2, 2));
        bodyTagButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TagHandlerPanel.this.itemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(bodyTagButton, gridBagConstraints);
        bodyTagButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("DESC_BodyTag"));

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "LBL_TagSupportClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 2, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_Description_mnem").charAt(0));
        jLabel2.setLabelFor(descriptionArea);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "LBL_description"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "DESC_SimpleTag"));
        descriptionArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(descriptionArea);
        descriptionArea.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_Description"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jScrollPane1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void itemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_itemStateChanged
        // TODO add your handling code here:
        if (simpleTagButton.isSelected())
            descriptionArea.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "DESC_SimpleTag"));
        else
            descriptionArea.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanel.class, "DESC_BodyTag"));
        wizardPanel.fireChangeEvent();
    }//GEN-LAST:event_itemStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bodyTagButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton simpleTagButton;
    // End of variables declaration//GEN-END:variables
    
    boolean isBodyTagSupport() {
        return bodyTagButton.isSelected();
    }

}
