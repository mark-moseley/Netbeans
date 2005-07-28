/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.mbeanwizard.table.WrapperAttributeTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanWrapperAttributeTableModel;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.MBeanAttribute;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.jmi.javamodel.JavaClass;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanAttributeTableModel;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


/**
 *
 * @author an156382
 */
public class MBeanWrapperAttributePanel extends MBeanAttributePanel {
    
    /** Creates a new instance of WrapperPanel */
    public MBeanWrapperAttributePanel(WrapperAttributesWizardPanel wiz) {
        super(wiz);
        initWrapperComponents();
        String str = NbBundle.getMessage(MBeanWrapperAttributePanel.class,"LBL_Attribute_Panel");// NOI18N
        setName(str);
        affectTableMouseListener();
    }
    
    private void affectTableMouseListener() {
        /* Affects a mouseListener to the wrapper table to enable /disable
         * the remove button when the user selects an attribute that has been
         * introspected, i.e he is not allowed to remove
         * Further an event is thrown to notify the table of that change
         */ 
        attributeTable.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e)  {
                boolean isOK = attributeTable.getSelectedRow() < getModel().getFirstEditableRow();
                attrRemoveJButton.setEnabled(!isOK);
                wiz.event();
            }
     
            public void mouseEntered(MouseEvent e) {}        
            public void mouseExited(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {
                boolean isOK = attributeTable.getSelectedRow() < getModel().getFirstEditableRow();
                attrRemoveJButton.setEnabled(!isOK);
                wiz.event();
            }
            public void mouseReleased(MouseEvent e) {
                boolean isOK = attributeTable.getSelectedRow() < getModel().getFirstEditableRow();
                attrRemoveJButton.setEnabled(!isOK);
                wiz.event();
            }
        });
    }
    
    protected void initJTables() {
        
        attributeModel = new MBeanWrapperAttributeTableModel(); 
        attributeTable = new WrapperAttributeTable(attributeModel,wiz); 
    }
    
    /**
     * Overriding super class method to force emty treatment
     * The reason is a Layout manager change that is incompatible
     * This method is therefore empty thus it is called by the constructor
     * of the super class
     * The real treatment of component initialization is in 
     * initWrapperComponents() which is called later
     */
    protected void initComponents() {
    }
    
    protected void initWrapperComponents() {
        
        initJTables();
        
        attrColumnModel = attributeTable.getColumnModel();
        affectAttributeTableComponents(attrColumnModel);
    }
    
    public MBeanWrapperAttributeTableModel getModel() {
        return (MBeanWrapperAttributeTableModel)attributeModel;
    }
    
    protected void affectAttributeTableComponents(TableColumnModel columnModel) {
        super.affectAttributeTableComponents(columnModel);
        tableLabel.setText(NbBundle.getMessage(MBeanWrapperAttributePanel.class,  
                "LBL_AttrTable_FromExistingClass"));// NOI18N
        
        /* New ActionListener for the remove button that overrides the one from
         * the super class: Now, to be able to remove a line, it must not be 
         * an introspected attribute
         */ 
        attrRemoveJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final int selectedRow = attributeTable.getSelectedRow();
                int firstEditableRow = getModel().getFirstEditableRow();
                
                //No row selected
                if (selectedRow == -1) return;
                
                if (selectedRow >= firstEditableRow) { // remove allowed
                    try {
                        //attrRemoveJButton.setEnabled(true);
                        attributeModel.remRow(selectedRow, attributeTable);
                        //attributeModel.selectNextRow(selectedRow, attributeTable);  
                    } catch (Exception ex) {
                        System.out.println("Exception here : ");// NOI18N
                        ex.printStackTrace();
                    }
                } else {
                    attrRemoveJButton.setEnabled(false);
                }
                
                // if the model has no rows, disable the remove button
                if (attributeModel.size() == getModel().getFirstEditableRow())
                    attrRemoveJButton.setEnabled(false);
                
                wiz.event();
            }
        });
    }
    
      private boolean AttributeNameAlreadyChecked() {
            
            ArrayList attributeNames = new ArrayList(attributeModel.size());
            
            //get all the attribute names
            for (int i=0; i < attributeModel.size(); i++)
                attributeNames.add(attributeModel.getAttribute(i).getName());
            
            // if the list does not contain any doubled bloom no further treatment
            if (!NameAlreadyContained(attributeNames))
                return false;
            
            // else remove all attributes that are not checked
            removeUnchecked(attributeNames);
            
            // verify that no doubled bloom any more
            return (NameAlreadyContained(attributeNames));
        }
        
        private ArrayList<String> removeUnchecked(ArrayList<String> array) {
            if (array.size() > 0) {
                int k = 0; // counter for array element
                for (Iterator<String> iter = array.iterator(); iter.hasNext();) {
                    String elem = iter.next();
                    
                    if (!((MBeanWrapperAttribute)attributeModel.getAttribute(k)).isSelected()) 
                        iter.remove(); // remove all non checked elements
          
                    k++;
                }
            }
            return array; //return the array containing all checked elements
        }
    
        private boolean NameAlreadyContained(ArrayList<String> attributeNames) {

            for (int i=0; i < attributeNames.size(); i++) {
                int count = 0;
                String currentValue = attributeNames.get(i);
                for(int j=0; j < attributeNames.size(); j++) {
                    String compareValue = attributeNames.get(j);
                    if (compareValue.equals(currentValue))
                        count ++;
                    if (count >= 2)
                        return true;
                }
            }
            return false;
        }
    
    /**
     * Inner static class which defines the wizard descriptor and fills it with 
     * user information
     */
    public static class WrapperAttributesWizardPanel extends AttributesWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel {
        private MBeanWrapperAttributePanel panel = null;
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return boolean true if the panel can be the last one 
         * and enables the finish button
         */
        public boolean isFinishPanel() {
            return true;
        }
        
        /**
         * Method which enables the next button
         * @return boolean true if the information in the panel 
         * is sufficient to go to the next step
         */
        public boolean isValid() {
            
            boolean attrValid = true;
            String msg = WizardConstants.EMPTY_STRING;
           
            if (getPanel() != null) {
                if (getPanel().AttributeNameAlreadyChecked()) { 
                    attrValid = false;
                    msg = NbBundle.getMessage(MBeanAttrAndMethodPanel.class,"LBL_State_Same_Attribute_Name");// NOI18N
                }
                setErrorMsg(msg);
            }
            return attrValid;
        }
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (wiz != null) {
                wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE,
                        message);
            }
        }
        
        /**
         * Method which fires an event to notify that there was a change in 
         * the data
         */
        public void event() {
            fireChangeEvent();
        }
        
        /**
         * Method returning the corresponding panel; here 
         * the MBeanAttrAndMethodPanel
         * @return Component the panel
         */
        public Component getComponent() { return getPanel(); }
        
        private MBeanWrapperAttributePanel getPanel() {
            if (panel == null) {
                panel = new MBeanWrapperAttributePanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         *        attribute table
         */
        public void readSettings(Object settings) {
            
            wiz = (WizardDescriptor) settings;
            
            // if the user loads the panel for the first time, perform introspection
            // else do nothing ...
            //Object prop_user = wiz.getProperty(WizardConstants.PROP_USER_ADDED_ATTR);
            //if (prop_user == null) { // the user loads the panel for the first time
                MBeanDO mbdo = null;
                try {
                    mbdo = org.netbeans.modules.jmx.Introspector.introspectClass(
                            (JavaClass)wiz.getProperty(WizardConstants.PROP_MBEAN_EXISTING_CLASS));
                    
                    List<MBeanAttribute> attributes = mbdo.getAttributes();
                    for (Iterator<MBeanAttribute> it = attributes.iterator(); it.hasNext();) {
                        ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).addRow(it.next());
                    }
                    ((MBeanWrapperAttributeTableModel) getPanel().getAttributeModel()).setFirstEditableRow(attributes.size());
                    
                    event();
                } catch (Exception e) {e.printStackTrace();}
            //}
            
            wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, "");// NOI18N
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            getPanel().attributeTable.editingStopped(new ChangeEvent(this));
            
            //read the contents of the attribute table
            MBeanWrapperAttributeTableModel attrModel = 
                    (MBeanWrapperAttributeTableModel)getPanel().attributeModel;
            
            int nbAttrs = attrModel.size();
            int firstEditableRow = attrModel.getFirstEditableRow();
            
            wiz.putProperty(WizardConstants.PROP_ATTR_NB, 
                    new Integer(nbAttrs).toString());
            
            // counter for the attribute storage
            int j = 0;
            
            // two loops; one for the wrapped atributes and the other for the
            // attributes added by the user
            for (int i = firstEditableRow ; i < nbAttrs; i++) {
                
                // the current attribute (number i)
                MBeanWrapperAttribute attr = attrModel.getWrapperAttribute(i);
                
                    wiz.putProperty(WizardConstants.PROP_ATTR_NAME + j,
                            attr.getName());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_TYPE + j,
                            attr.getTypeName());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_RW + j,
                            attr.getAccess());
                    
                    wiz.putProperty(WizardConstants.PROP_ATTR_DESCR + j,
                            attr.getDescription());
                    j++;
            }
            
            for (int i = 0 ; i < firstEditableRow; i++) {
                
                // the current attribute (number i)
                MBeanWrapperAttribute attr = attrModel.getWrapperAttribute(i);
                
                if (attr.isSelected()) { // the attribute has to be included for management
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_NAME + i,
                            attr.getName());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_TYPE + i,
                            attr.getTypeName());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_RW + i,
                            attr.getAccess());
                    
                    wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_DESCR + i,
                            attr.getDescription());
                }
            }
            
            // sets the number of introspected attributes and the number of
            // added attributes
            wiz.putProperty(WizardConstants.PROP_INTRO_ATTR_NB, firstEditableRow);
            wiz.putProperty(WizardConstants.PROP_USER_ADDED_ATTR, j);
            
            // sets the property if the user has added some attributes
            wiz.putProperty(WizardConstants.PROP_USER_ADDED_ATTR,
                    new Boolean(true));
        }
        
        /**
         * Returns a help context
         * @return HelpCtxt the help context
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");// NOI18N
        }
    }
}
