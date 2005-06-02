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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * Represents <em>Display</em> panel in Netbeans Module customizer.
 *
 * @author mkrauskopf
 */
final class CustomizerDisplay extends JPanel implements ComponentFactory.StoragePanel {
    
    private EditableProperties bundleProps;
    
    /** Creates new form CustomizerDisplay */
    CustomizerDisplay(final EditableProperties bundleProps,
            final Set categories) {
        initComponents();
        this.bundleProps = bundleProps;
        for (Iterator it = categories.iterator(); it.hasNext(); ) {
            Object next = it.next();
            this.categoryValue.addItem(next);
        }
        if (!categories.contains(getCategory())) {
            // put module's own category at the beginning
            categoryValue.insertItemAt(getCategory(), 0);
        }
        readFromProperties();
    }
    
    public void store() {
        storeToProperties();
    }
    
    private void readFromProperties() {
        nameValue.setText(bundleProps.getProperty("OpenIDE-Module-Name")); // NOI18N
        shortDescValue.setText(bundleProps.getProperty("OpenIDE-Module-Short-Description")); // NOI18N
        longDescValue.setText(bundleProps.getProperty("OpenIDE-Module-Long-Description")); // NOI18N
        categoryValue.setSelectedItem(getCategory()); // NOI18N)
    }
    
    private String getCategory() {
        String category = bundleProps.getProperty("OpenIDE-Module-Display-Category"); // NOI18N
        return category != null ? category : ""; // NOI18N
    }
    
    private void storeToProperties() {
        storeOneProperty("OpenIDE-Module-Name", nameValue.getText(), false); // NOI18N
        storeOneProperty("OpenIDE-Module-Display-Category", (String) categoryValue.getSelectedItem(), false); // NOI18N
        storeOneProperty("OpenIDE-Module-Short-Description", shortDescValue.getText(), false); // NOI18N
        storeOneProperty("OpenIDE-Module-Long-Description", longDescValue.getText(), true); // NOI18N
    }
    
    private void storeOneProperty(String name, String value, boolean split) {
        if (value != null) {
            value = value.trim();
        }
        if (value != null && value.length() > 0) {
            if (split) {
                bundleProps.setProperty(name, splitBySentence(value));
            } else {
                bundleProps.setProperty(name, value);
            }
        } else {
            bundleProps.remove(name);
        }
    }
    
    private static String[] splitBySentence(String text) {
        List/*<String>*/ sentences = new ArrayList();
        // Use Locale.US since the customizer is setting the default (US) locale text only:
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.US);
        it.setText(text);
        int start = it.first();
        int end;
        while ((end = it.next()) != BreakIterator.DONE) {
            sentences.add(text.substring(start, end));
            start = end;
        }
        return (String[]) sentences.toArray(new String[sentences.size()]);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        name = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        category = new javax.swing.JLabel();
        categoryValue = new javax.swing.JComboBox();
        shortDesc = new javax.swing.JLabel();
        shortDescValue = new javax.swing.JTextField();
        longDesc = new javax.swing.JLabel();
        hackPanel = new javax.swing.JPanel();
        longDescValueSP = new javax.swing.JScrollPane();
        longDescValue = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        name.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayName_Mnem").charAt(0));
        name.setLabelFor(nameValue);
        name.setText(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(name, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(nameValue, gridBagConstraints);

        category.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayCategory_Mnem").charAt(0));
        category.setLabelFor(categoryValue);
        category.setText(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_DisplayCategory"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(category, gridBagConstraints);

        categoryValue.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(categoryValue, gridBagConstraints);

        shortDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_ShortDescription_Mnem").charAt(0));
        shortDesc.setLabelFor(shortDescValue);
        shortDesc.setText(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_ShortDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(shortDesc, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(shortDescValue, gridBagConstraints);

        longDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_LongDescription_Mnem").charAt(0));
        longDesc.setLabelFor(longDescValue);
        longDesc.setText(org.openide.util.NbBundle.getMessage(CustomizerDisplay.class, "LBL_LongDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(longDesc, gridBagConstraints);

        hackPanel.setLayout(new java.awt.BorderLayout());

        longDescValue.setLineWrap(true);
        longDescValue.setRows(4);
        longDescValue.setWrapStyleWord(true);
        longDescValueSP.setViewportView(longDescValue);

        hackPanel.add(longDescValueSP, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(hackPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel category;
    private javax.swing.JComboBox categoryValue;
    private javax.swing.JPanel hackPanel;
    private javax.swing.JLabel longDesc;
    private javax.swing.JTextArea longDescValue;
    private javax.swing.JScrollPane longDescValueSP;
    private javax.swing.JLabel name;
    private javax.swing.JTextField nameValue;
    private javax.swing.JLabel shortDesc;
    private javax.swing.JTextField shortDescValue;
    // End of variables declaration//GEN-END:variables
}
