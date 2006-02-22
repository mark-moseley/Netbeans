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


package org.netbeans.modules.form;

import java.beans.*;
import java.util.*;

import org.openide.awt.Mnemonics;

/** The PropertyPicker is a form which allows user to choose from property set
 * of specified required class.
 *
 * @author  Ian Formanek
 */
public class PropertyPicker extends javax.swing.JPanel {

    public static final int CANCEL = 0;
    public static final int OK = 1;

    static final long serialVersionUID =5689122601606238081L;
    /** Initializes the Form */
    public PropertyPicker(FormModel formModel, RADComponent componentToSelect, Class requiredType) {
        this.requiredType = requiredType;
        initComponents();

        java.util.List componentsList = formModel.getMetaComponents();
        Collections.sort(componentsList, new ParametersPicker.ComponentComparator());
        components = new RADComponent[componentsList.size()];
        componentsList.toArray(components);

        int selIndex = -1;
        for (Iterator it = componentsList.iterator(); it.hasNext(); ) {
            RADComponent radComp = (RADComponent) it.next();
            if (componentToSelect != null && componentToSelect == radComp)
                selIndex = componentsCombo.getItemCount();
            if (radComp == formModel.getTopRADComponent())
                componentsCombo.addItem(
                    FormUtils.getBundleString("CTL_FormTopContainerName")); // NOI18N
            else
                componentsCombo.addItem(radComp.getName());
        }
        if (selIndex >= 0)
            componentsCombo.setSelectedIndex(selIndex);

        propertyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        updatePropertyList();

        // localize components
        Mnemonics.setLocalizedText(componentLabel, FormUtils.getBundleString("CTL_CW_Component")); // NOI18N
        Mnemonics.setLocalizedText(listLabel, FormUtils.getBundleString("CTL_CW_PropertyList")); // NOI18N

        componentsCombo.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_Component")); // NOI18N
        propertyList.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_CW_PropertyList")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_PropertyPicker")); // NOI18N

//        HelpCtx.setHelpIDString(this, "gui.connecting.code"); // NOI18N
    }

    public boolean isPickerValid() {
        return pickerValid;
    }
    
    private void setPickerValid(boolean v) {
        boolean old = pickerValid;
        pickerValid = v;
        firePropertyChange("pickerValid", old, pickerValid); // NOI18N
    }

    RADComponent getSelectedComponent() {
        return selectedComponent;
    }

    void setSelectedComponent(RADComponent selectedComponent) {
        if (selectedComponent != null)
            componentsCombo.setSelectedItem(selectedComponent.getName());
    }

    PropertyPickerItem getSelectedProperty() {
        if ((selectedComponent == null) ||(propertyList.getSelectedIndex() == -1))
            return null;
        return items [propertyList.getSelectedIndex()];
    }

    void setSelectedProperty(PropertyDescriptor selectedProperty) {
        if (selectedProperty == null) {
            propertyList.setSelectedIndex(-1);
        } else {
            propertyList.setSelectedValue(selectedProperty.getName(), true);
        }
    }
    // ----------------------------------------------------------------------------
    // private methods

    private void updatePropertyList() {	
        RADComponent sel = getSelectedComponent();
        if (sel == null) {
            propertyList.setListData(new Object [0]);
            propertyList.revalidate();
            propertyList.repaint();
        } else {
            PropertyDescriptor[] descs = sel.getBeanInfo().getPropertyDescriptors();
            Map filtered = new HashMap();
            for (int i = 0; i < descs.length; i ++) {
                if ((descs[i].getReadMethod() != null) &&       // filter out non-readable properties
                    (descs[i].getPropertyType() != null) &&  // indexed properties return null from getPropertyType
                    requiredType.isAssignableFrom(descs[i].getPropertyType())) {
		    PropertyPickerItem item = createItem(descs[i]);
                    filtered.put(item.getPropertyName(), item);
                }
            }
	    
	    if(sel == sel.getFormModel().getTopRADComponent() ) {
		String[] names = FormEditor.getFormJavaSource(sel.getFormModel()).getPropertyReadMethodNames(requiredType);
		for (int i = 0; i < names.length; i++) {
		    PropertyPickerItem item = createItem(names[i]);
		    if(!filtered.keySet().contains(item.getPropertyName())){
			filtered.put(item.getPropertyName(), item);	
		    }                    
		}		
	    } 
	    
	    items = new PropertyPickerItem[filtered.size()];
            filtered.values().toArray(items);	    

            // sort the properties by name
            Arrays.sort(items, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((PropertyPickerItem)o1).getPropertyName()
			    .compareTo(((PropertyPickerItem)o2).getPropertyName());
                }
            });
            
	    String[] listItems = new String [items.length];
            for (int i = 0; i < listItems.length; i++)
                listItems[i] = items[i].getPropertyName();
	    
            propertyList.setListData(listItems);
            propertyList.revalidate();
            propertyList.repaint();
        }
    }

    private PropertyPickerItem createItem(final PropertyDescriptor desc) {
	return new PropertyPickerItem() {
	    public String getPropertyName() {
		return desc.getName();
	    }
	    public String getReadMethodName() {
		return desc.getReadMethod().getName();
	    }
	    public boolean providesPropertyDescriptor() {
		return true;
	    }
	    public PropertyDescriptor getPropertyDescriptor() {
		return desc;
	    }
	};		
    }
	    
    private PropertyPickerItem createItem(final String name) {
	return new PropertyPickerItem() {
	    public String getPropertyName() {
		RADComponent sel = getSelectedComponent();
		return FormJavaSource.extractPropertyName(name);
	    }
	    public String getReadMethodName() {
		return FormUtils.getMethodName(name, NO_PARAMETERS);
	    }
	    public boolean providesPropertyDescriptor() {
		return false;
	    }
	    public PropertyDescriptor getPropertyDescriptor() {
		return null;
	    }
	};		
    }

    private void updateState() {
        setPickerValid((getSelectedComponent() != null) &&(getSelectedProperty() != null));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        componentsCombo = new javax.swing.JComboBox();
        propertiesScrollPane = new javax.swing.JScrollPane();
        propertyList = new javax.swing.JList();
        componentLabel = new javax.swing.JLabel();
        listLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        componentsCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                componentsComboItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(componentsCombo, gridBagConstraints);

        propertyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                propertyListValueChanged(evt);
            }
        });

        propertiesScrollPane.setViewportView(propertyList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(propertiesScrollPane, gridBagConstraints);

        componentLabel.setLabelFor(componentsCombo);
        componentLabel.setText("Component:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 6);
        add(componentLabel, gridBagConstraints);

        listLabel.setLabelFor(propertyList);
        listLabel.setText("Properties");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(listLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents


    private void propertyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_propertyListValueChanged
        updateState();
    }//GEN-LAST:event_propertyListValueChanged

    private void componentsComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_componentsComboItemStateChanged
        if (componentsCombo.getSelectedIndex() == -1)
            selectedComponent = null;
        else
            selectedComponent = components[componentsCombo.getSelectedIndex()];
        updatePropertyList();
    }//GEN-LAST:event_componentsComboItemStateChanged

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:closeDialog
    }//GEN-LAST:closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel componentLabel;
    private javax.swing.JComboBox componentsCombo;
    private javax.swing.JLabel listLabel;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JList propertyList;
    // End of variables declaration//GEN-END:variables

    private boolean pickerValid = false;

    private RADComponent[] components;
    private Class requiredType;
    private PropertyPickerItem[] items;
    private RADComponent selectedComponent;    
    private static Class[] NO_PARAMETERS = new Class[0];	

    interface PropertyPickerItem {
	public String getPropertyName();
	public String getReadMethodName();
	public PropertyDescriptor getPropertyDescriptor();
    }

}
