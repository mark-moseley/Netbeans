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


package org.netbeans.modules.properties;


import java.awt.Component;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Customizer for bundle node (<code>PropertiesDataNode</code>) which represents
 * bundle of .properties files with same basic name and set of Locales.
 *
 * @author Peter Zavadsky
 * @see PropertiesDataNode
 * @see PropertiesDataObject
 */
public class BundleNodeCustomizer extends JPanel {
    
    /** Properties data object to customize. */
    private PropertiesDataObject propDataObject;

    /** Creates new form BundleNodeCustomizer */
    public BundleNodeCustomizer(PropertiesDataObject propDataObject) {
        this.propDataObject = propDataObject;
        
        initComponents();
        initAccessibility();
        
        nameText.setText(propDataObject.getNodeDelegate().getName());

        localesList.setListData(retrieveLocales(propDataObject));
        
        removeLocales.setEnabled(false);
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_ADDLOCALE);
    }

    /** Utility method. Gets icon for key item in key list. */    
    private static Icon getLocaleIcon() {
        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/properties/propertiesLocale.gif")); // NOI18N
    }
    
    /** Retrieves entry locales. Utility method.
     * @param propDataObject properties data object to retrieve entry names for */
    private static Locale[] retrieveLocales(PropertiesDataObject propDataObject) {
        ArrayList entryList = new ArrayList();

        entryList.add(LocaleNodeCustomizer.getLocale((PropertiesFileEntry)propDataObject.getPrimaryEntry()));
        
        for (Iterator it = propDataObject.secondaryEntries().iterator(); it.hasNext(); ) {
            entryList.add(LocaleNodeCustomizer.getLocale((PropertiesFileEntry)it.next()));
        }
        
        Locale[] entryLocales = new Locale[entryList.size()];
        entryList.toArray(entryLocales);
        
        return entryLocales;
    }

    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_BundleNodeCustomizer"));
                 
        nameText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_NameText"));
        localesList.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_LocalesList"));
        addLocale.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_AddLocale"));
        removeLocales.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_RemoveLocale"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameLabel.setDisplayedMnemonic((NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Name_Mnem")).charAt(0));
        nameText = new javax.swing.JTextField();
        localesLabel = new javax.swing.JLabel();
        localesLabel.setDisplayedMnemonic((NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Locales_Mnem")).charAt(0));
        jScrollPane1 = new javax.swing.JScrollPane();
        localesList = new javax.swing.JList();
        addLocale = new javax.swing.JButton();
        removeLocales = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(nameText);
        nameLabel.setText(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(nameLabel, gridBagConstraints);

        nameText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextActionPerformed(evt);
            }
        });

        nameText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameTextFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(nameText, gridBagConstraints);

        localesLabel.setLabelFor(localesList);
        localesLabel.setText(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Locales"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(localesLabel, gridBagConstraints);

        localesList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if(value instanceof String)
                return label;

                Locale locale = (Locale)value;

                if(locale.equals(new Locale("", ""))) // NOI18N
                label.setText(NbBundle.getBundle(BundleNodeCustomizer.class).getString("LAB_DefaultBundle_Label"));
                else {
                    label.setText(locale.toString() +
                        (locale.getLanguage().equals("") ? "" : " - " + locale.getDisplayLanguage()) + // NOI18N
                        (locale.getCountry().equals("") ? "" : " / " + locale.getDisplayCountry()) + // NOI18N
                        (locale.getVariant().equals("") ? "" : " / " + locale.getDisplayVariant()) // NOI18N
                    );
                }

                label.setIcon(getLocaleIcon());

                return label;
            }
        });
        localesList.setPrototypeCellValue("0123456789012345678901234567890123456789");
        localesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                localesListValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(localesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 5, 0);
        add(jScrollPane1, gridBagConstraints);

        addLocale.setMnemonic((NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_AddLocale_Mnem")).charAt(0) );
        addLocale.setText(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_AddLocale"));
        addLocale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocaleActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(addLocale, gridBagConstraints);

        removeLocales.setMnemonic((NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_RemoveLocale_Mnem")).charAt(0));
        removeLocales.setText(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_RemoveLocale"));
        removeLocales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLocalesActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 11);
        add(removeLocales, gridBagConstraints);

    }//GEN-END:initComponents

    private void localesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_localesListValueChanged
        if(localesList.isSelectionEmpty() 
            || new Locale("", "").equals(localesList.getSelectedValue()))
            
            removeLocales.setEnabled(false);
        else
            removeLocales.setEnabled(true);
    }//GEN-LAST:event_localesListValueChanged

    private void removeLocalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLocalesActionPerformed
        Object[] selectedValues = localesList.getSelectedValues();

        String basicName = propDataObject.getPrimaryFile().getName();
        
        for(int i=0; i<selectedValues.length; i++) {
            PropertiesFileEntry entry = propDataObject.getBundleStructure().getEntryByFileName(basicName + PropertiesDataLoader.PRB_SEPARATOR_CHAR + selectedValues[i].toString());
            try {
                entry.delete();
            } catch(IOException ioe) {
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ioe);
            }
        }
        
        localesList.setListData(retrieveLocales(propDataObject));
    }//GEN-LAST:event_removeLocalesActionPerformed

    private void addLocaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocaleActionPerformed
        try {
            propDataObject.getNodeDelegate().getNewTypes()[0].create();
        } catch(IOException ioe) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ioe);
        }
        
        localesList.setListData(retrieveLocales(propDataObject));
    }//GEN-LAST:event_addLocaleActionPerformed

    private void nameTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameTextFocusLost
        nameTextHandler();
    }//GEN-LAST:event_nameTextFocusLost

    private void nameTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextActionPerformed
        nameTextHandler();
    }//GEN-LAST:event_nameTextActionPerformed

    /** Name text field event handler delegate. */
    private void nameTextHandler() {
        String newName = nameText.getText();
        
        if(newName == null || "".equals(newName)) // NOI18N
            return;
        
        propDataObject.getNodeDelegate().setName(newName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList localesList;
    private javax.swing.JLabel localesLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton removeLocales;
    private javax.swing.JButton addLocale;
    // End of variables declaration//GEN-END:variables

}
