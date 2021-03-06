/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.properties;


import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


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
        List<Locale> entryList = new ArrayList<Locale>();

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
                 
        nameText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_BundleName"));
        localesList.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_LocalesList"));
        addLocale.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_AddLocale"));
        removeLocales.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(LocaleNodeCustomizer.class).getString("ACS_CTL_RemoveLocale"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        localesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        localesList = new javax.swing.JList();
        addLocale = new javax.swing.JButton();
        removeLocales = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setLabelFor(nameText);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Name")); // NOI18N
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
        org.openide.awt.Mnemonics.setLocalizedText(localesLabel, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("LBL_Locales")); // NOI18N
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
                label.setText(NbBundle.getBundle(BundleNodeCustomizer.class).getString("LAB_defaultLanguage"));//NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(addLocale, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_AddLocale")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(removeLocales, NbBundle.getBundle(LocaleNodeCustomizer.class).getString("CTL_RemoveLocale")); // NOI18N
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
    }// </editor-fold>//GEN-END:initComponents

    private void localesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_localesListValueChanged
        if(localesList.isSelectionEmpty() 
                || new Locale("", "").equals(localesList.getSelectedValue())) {

            removeLocales.setEnabled(false);
        } else {
            removeLocales.setEnabled(true);
        }
    }//GEN-LAST:event_localesListValueChanged

    private void removeLocalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLocalesActionPerformed
        Object[] selectedValues = localesList.getSelectedValues();

        String basicName = propDataObject.getPrimaryFile().getName();
        
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(BundleNodeCustomizer.class, "CTL_Deletebundle_Prompt"));
        descriptor.setTitle(NbBundle.getMessage(BundleNodeCustomizer.class, "CTL_Deletebundle_Title"));
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }

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
        
        if (newName == null || "".equals(newName)) {                    //NOI18N
            return;
        }
        propDataObject.getNodeDelegate().setName(newName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addLocale;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel localesLabel;
    private javax.swing.JList localesList;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JButton removeLocales;
    // End of variables declaration//GEN-END:variables

}
