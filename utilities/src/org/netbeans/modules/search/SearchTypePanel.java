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


package org.netbeans.modules.search;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.DialogDescriptor;
import org.openide.ServiceType;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openidex.search.SearchType;


/**
 * Panel which shows to user one search type allowing it to customize.
 *
 * @author  Peter Zavadsky
 * @see SearchPanel
 */
public class SearchTypePanel extends JPanel implements PropertyChangeListener {

    /** Name of customized property. */
    public static final String PROP_CUSTOMIZED = "customized"; // NOI18N
    /** Modificator suffix.  */
    private static final String MODIFICATOR_SUFFIX = " *"; // NOI18N
    /** Customized property. Indicates this criterion model 
     * was customized by user. */
    private boolean customized;
    /** Search type this model is customized by. */
    private SearchType searchType;
    /** Beaninfo of search type. */
    private BeanInfo beanInfo;
    /** Customizer for search type. */
    private Customizer customizer;
    /** Customizer component. */
    private Component customizerComponent;

    private String lastSavedName;
    
    
    /** Creates new form <code>SearchTypePanel</code>. */
    public SearchTypePanel(SearchType searchType) {
        initComponents();
        initAccessibility();
                
        this.searchType = searchType;

        try {
            beanInfo = Utilities.getBeanInfo(this.searchType.getClass());

            if(hasCustomizer()) {
                customizer = getCustomizer();
                customizerComponent = (Component)customizer;
            } else {
                // PENDING use property sheet as it will implement Customizer
                // allow hiding tabs, ....
                System.err.println("No customizer for " + this.searchType.getName() + ", skipping...");
            }
        } catch(IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ie);
        }

        customizer.setObject(this.searchType);
        this.searchType.addPropertyChangeListener(this);
        
        applyCheckBox.setText(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_APPLY")); // NOI18N
        applyCheckBox.setMnemonic(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_APPLY_MNEM").charAt(0)); // NOI18N
        
        saveButton.setText(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_SAVE_AS")); // NOI18N
        saveButton.setMnemonic(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_SAVE_AS_MNEM").charAt(0)); // NOI18N
        
        saveButton.setEnabled(false);
        
        restoreButton.setText(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_RESTORE")); // NOI18N
        restoreButton.setMnemonic(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_BUTTON_RESTORE_MNEM").charAt(0)); // NOI18N

        customizerPanel.add(customizerComponent, BorderLayout.CENTER);

        setCustomized(this.searchType.isValid());
        
        // obtain tab label string & icon
        setName(createName());
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_DIALOG_DESC")); // NOI18N        
        restoreButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_TEXT_BUTTON_RESTORE")); // NOI18N        
        saveButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_TEXT_BUTTON_SAVE_AS")); // NOI18N        
        applyCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_TEXT_BUTTON_APPLY")); // NOI18N        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        customizerPanel = new javax.swing.JPanel();
        applyCheckBox = new javax.swing.JCheckBox();
        saveButton = new javax.swing.JButton();
        restoreButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        customizerPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(customizerPanel, gridBagConstraints);

        applyCheckBox.setText("jCheckBox2");
        applyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 11, 0, 11);
        add(applyCheckBox, gridBagConstraints);

        saveButton.setText("jButton3");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 0);
        add(saveButton, gridBagConstraints);

        restoreButton.setText("jButton4");
        restoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        add(restoreButton, gridBagConstraints);

    }//GEN-END:initComponents

    private void restoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreButtonActionPerformed
        restoreCriterion();
    }//GEN-LAST:event_restoreButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveCriterion();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void applyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyCheckBoxActionPerformed
        // PENDING Some better solution of valid / customized needed.
        boolean selected = applyCheckBox.isSelected();
        setCustomized(selected);
        searchType.setValid(selected);
    }//GEN-LAST:event_applyCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton restoreButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBox applyCheckBox;
    // End of variables declaration//GEN-END:variables

    // PENDING Better solution for these properties are needed.
    /** Listens on search type PROP_VALID property change and sets
     * customized property accordingly. */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == searchType) {

            // if Type fires valid property change listens for
            // its invalidity -> mark itself as unmodified
            if (SearchType.PROP_VALID.equals(evt.getPropertyName()) ) {
                
                if (evt.getNewValue().equals(Boolean.FALSE)) {
                    setCustomized (false);
                    return;
                } else {
                    setCustomized (true);
                }
            }
        }
    }
    
    /**
     * Creates name used as tab name, 
     * @return name. */
    private String createName() {
        String name = searchType.getName();

        if(customized) {        
            return  name + MODIFICATOR_SUFFIX;
        } else {
            return  name;
        }
    }

    /** Indicates whether the search type has customizer. */
    private boolean hasCustomizer() {
        // true if we have already computed beanInfo and it has customizer class
        return beanInfo.getBeanDescriptor().getCustomizerClass() != null;
    }

    /** Gets customizer for the search type. 
     * @return customizer object. */
    private Customizer getCustomizer() {
        if (customizer != null) return customizer;

        Class clazz = beanInfo.getBeanDescriptor ().getCustomizerClass ();
        if (clazz == null) return null;

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }

        if (!(o instanceof Component) ||
                !(o instanceof Customizer)) return null;

        return (Customizer) o;
    }
    
    public Component getComponent() {
        return customizerComponent;
    }

    /**
     * Sets customized property.
     *
     * @param cust value to which customized property to set.
     */
    private void setCustomized(boolean cust) {
        customized = cust;

        saveButton.setEnabled(customized);
        applyCheckBox.setSelected(customized);

        setName(createName());

        firePropertyChange(PROP_CUSTOMIZED, !cust, cust);
    }

    /** Tests whether this panel is customized. */
    public boolean isCustomized() {
        return customized;
    }

    /** Saves the criterion. */
    private void saveCriterion() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(12,0));
        
        JLabel nameLab = new JLabel(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_NAME")); // NOI18N
        nameLab.setDisplayedMnemonic(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_NAME_MNEM").charAt(0)); // NOI18N
        
        pane.add(nameLab, BorderLayout.WEST); 
        pane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_SaveAsPanel")); // NOI18N
        
        JTextField textField;
        if (lastSavedName != null) {
            textField = new JTextField(lastSavedName, 20);
        } else {
            textField = new JTextField(20);
        }
        textField.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_TEXT_LABEL_SELECT")); // NOI18N
        
        nameLab.setLabelFor(textField);
        pane.add(textField, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,11));
        
        DialogDescriptor desc = new DialogDescriptor(pane, NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_SAVE_CRITERION")); // NOI18N        
        Dialog dialog = TopManager.getDefault().createDialog(desc);
        
        while (true) {
            dialog.show();
            Object retVal = desc.getValue();
            
            if (retVal.toString().equals("0")) { // NOI18N
                String name = textField.getText();
                if (name.length() > 0) {
                    saveSearchType(name);
                    lastSavedName = name;
                    break;
                }
            } else
                return; // cancel
        }
    }
    
    /**
     * Saves the search type.
     *
     * @return true if new value was created
     */
    private boolean saveSearchType(String name) throws IllegalArgumentException {
        boolean savedNew;

        SearchType copy = (SearchType)searchType.clone();
        copy.setName(name);

        // overwrite existing
        if(existInRegistry(copy)) {
            removeFromRegistry(copy);
            savedNew = false;
        } else savedNew = true;

        appendToRegistry(copy);

        return savedNew;
    }
    
    /**
     * Restores the criterion.
     */
    private void restoreCriterion() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(12,0));
        pane.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SearchTypePanel.class).getString("ACS_RestorePanel")); // NOI18N
        
        JLabel resLabel = new JLabel(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_SELECT")); // NOI18N
        resLabel.setDisplayedMnemonic(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_SELECT_MNEM").charAt(0)); // NOI18N        
        
        pane.add(resLabel, BorderLayout.WEST); 
        
        Map searchTypesMap = new HashMap(10);
        Enumeration en = TopManager.getDefault().getServices().services(searchType.getClass());
        
        while (en.hasMoreElements()) {
            SearchType type = (SearchType)en.nextElement();
            String name = type.getName();
            
            if(name != null) {
                if(name.equals(searchType.getName())) {
                    searchTypesMap.put(NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_DEFAULT_CRITERION"), type);
                } else { 
                    searchTypesMap.put(name, type);
                }
            }
        }
        
        JComboBox combo = new JComboBox(searchTypesMap.keySet().toArray());
        Dimension dim = combo.getPreferredSize();
        dim.width = 160;
        combo.setPreferredSize(dim);
        resLabel.setLabelFor(combo);
        pane.add(combo, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,11));
        
        DialogDescriptor desc = new DialogDescriptor(pane, NbBundle.getBundle(SearchTypePanel.class).getString("TEXT_LABEL_RESTORE_CRITERION")); // NOI18N
        Dialog dialog = TopManager.getDefault().createDialog(desc);
        
        dialog.show();
        
        if (desc.getValue().toString().equals("0")) { // NOI18N
            String name = (String)combo.getSelectedItem();
            if (name != null)
                restoreSearchType((SearchType)searchTypesMap.get(name));
        }
    }

    /** Restores the search type. */
    private void restoreSearchType(SearchType searchType) {
        this.searchType.removePropertyChangeListener(this);

        this.searchType = (SearchType)searchType.clone();
        getCustomizer().setObject(this.searchType);
        this.searchType.addPropertyChangeListener(this);

        setCustomized(true);
    }    

    /** Return currently hold bean. */
    public SearchType getSearchType() {
        return searchType;
    }
    
    /**
     * Class equality
     *
     * @return this.bean.getClass().equals(bean.getClass());
     */
    public boolean equals(Object obj) {
        try {
            return searchType.getClass().equals(((SearchTypePanel)obj).getSearchType().getClass());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /** Gets help context. */
    public HelpCtx getHelpCtx() {
        return searchType.getHelpCtx();
    }
    
    // PENDING: It shoudn't be stored services this way
    // in registry. It's necessary to meka out cleaner solution.
    /** Tests whether exist specified search type. */
    private static boolean existInRegistry(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        Enumeration en = registry.services(obj.getClass());

        while (en.hasMoreElements()) {
            SearchType next = (SearchType) en.nextElement();

            if (next.getName().equals(obj.getName()))
                return true;
        }

        return false;
    }

    /** Adds specified search type to services registry. */
    private static void appendToRegistry(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        List result = registry.getServiceTypes();
        result.add(obj);
        registry.setServiceTypes(result);
    }

    /**
     * Remove specified search type from service registry.
     *
     * @param obj service template - used name and class
     */
    private static void removeFromRegistry(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        List result = registry.getServiceTypes();

        ArrayList ret = new ArrayList();

        Iterator it = result.iterator();
        while (it.hasNext()) {
            ServiceType next = (ServiceType) it.next();

            if ( ! next.getName().equals(obj.getName()) ||
                    ! next.getClass().equals(obj.getClass()) )
                ret.add(next);
        }

        registry.setServiceTypes(ret);
    }    
    
}
