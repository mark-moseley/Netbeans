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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author Peter Williams
 */
public class ParentManagedDDBeanTableModel extends InnerTableModel {

    // Fields required to interpret parentBean correctly (comments wrt/ JspConfig)
    private CommonDDBean parentBean;
    private String parentPropertyName; // = JspConfig.PROPERTY;
    private List<TableEntry> properties;
    private ParentPropertyFactory beanFactory; // JspConfigPropertyFactory
    private Class entryPanelClass; // Class to use for new rows, or null if use default.
    
    public ParentManagedDDBeanTableModel(XmlMultiViewDataSynchronizer synchronizer, 
            CommonDDBean parentBean, String propertyName, List<TableEntry> properties, 
            Class entryPanelClass, ParentPropertyFactory factory) {
        super(synchronizer, computeColumnNames(properties), computeColumnWidths(properties));
        
        this.parentBean = parentBean;
        this.parentPropertyName = propertyName;
        this.properties = properties;
        this.beanFactory = factory;
        this.entryPanelClass = entryPanelClass;
    }

    private static String [] computeColumnNames(List<TableEntry> props) {
		String [] names = new String [props.size()];
		for(int i = 0; i < props.size(); i++) {
			names[i] = props.get(i).getColumnName();
		}
        return names;
    }
    
    private static int [] computeColumnWidths(List<TableEntry> props) {
		int [] width = new int [props.size()];
		for(int i = 0; i < props.size(); i++) {
            // !PW TODO add this to TableEntry
//			width[i] = props.get(i).getColumnWidth();
            width[i] = 150;
		}
        return width;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // !PW TODO implement this if supporting inline editing of table values.
//        SecurityRoleRef securityRoleRef = ejb.getSecurityRoleRef(rowIndex);
//        switch (columnIndex) {
//            case 0:
//                securityRoleRef.setRoleName((String) value);
//                break;
//            case 1:
//                securityRoleRef.setRoleLink((String) value);
//                break;
//            case 2:
//                securityRoleRef.setDescription((String) value);
//                break;
//        }
//        modelUpdatedFromUI();
//        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public int getRowCount() {
//        CommonDDBean [] children = (CommonDDBean[]) parentBean.getValues(parentPropertyName);
//        return children != null ? children.length : 0;
        return parentBean.size(parentPropertyName);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;
        Object row = parentBean.getValue(parentPropertyName, rowIndex);
        if(row instanceof CommonDDBean) {
    		TableEntry columnEntry = properties.get(columnIndex);
            result = columnEntry.getEntry((CommonDDBean) row);
        }
		return result;
    }

    public int addRow() {
//        if (assemblyDesc == null) {
//            assemblyDesc = getAssemblyDesc();
//        }
//        
//        final SecurityRolePanel dialogPanel = new SecurityRolePanel();
//        final String currentRoleName = null;
//        
//        EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_SecurityRole"), true) {
//            protected String validate() {
//                String name = dialogPanel.getRoleName().trim();
//                
//                if (name.length()==0) {
//                    return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_EmptySecurityRoleName");
//                } else {
//                    SecurityRole[] roles = assemblyDesc.getSecurityRole();
//                    boolean exists=false;
//                    
//                    for (int i = 0; i < roles.length; i++) {
//                        if (name.equals(roles[i].getRoleName())){
//                            return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_SecurityRoleNameExists",name);
//                        }
//                    }
//                }
//                
//                return null;
//            }
//        };
//        dialog.setValid(false);
//        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
//        dialogPanel.getRoleNameTF().getDocument().addDocumentListener(docListener);
//        dialogPanel.getDescriptionTA().getDocument().addDocumentListener(docListener);
//        
//        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
//        d.setVisible(true);
//        
//        dialogPanel.getRoleNameTF().getDocument().removeDocumentListener(docListener);
//        dialogPanel.getDescriptionTA().getDocument().removeDocumentListener(docListener);
//        
//        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
//            SecurityRole role = assemblyDesc.newSecurityRole();
//            role.setRoleName(dialogPanel.getRoleName());
//            role.setDescription(dialogPanel.getDescription());
//            assemblyDesc.addSecurityRole(role);
//            modelUpdatedFromUI();
//        }        
        
        
        CommonDDBean param = beanFactory.newInstance(parentBean);
        parentBean.addValue(parentPropertyName, param);
        modelUpdatedFromUI();
        return getRowCount() - 1;
    }

    public void removeRow(int row) {
        parentBean.removeValue(parentPropertyName, row);
        modelUpdatedFromUI();
    }
    
    private GenericTableDialogPanelAccessor internalGetDialogPanel() {
        GenericTableDialogPanelAccessor subPanel = null;

//        try {
//            subPanel = (GenericTableDialogPanelAccessor) entryPanelClass.newInstance();
//            subPanel.init(getAppServerVersion(), 
//                    GenericTablePanel.this.getWidth()*3/4, properties, extraData);
//
//// TODO accessibility for popup panels.  (help context as well?)            
////            ((JPanel) subPanel).getAccessibleContext().setAccessibleName(
////                resourceBundle.getString("ACSN_POPUP_" + resourceBase));	// NOI18N
////            ((JPanel) subPanel).getAccessibleContext().setAccessibleDescription(
////                resourceBundle.getString("ACSD_POPUP_" + resourceBase));	// NOI18N
//        } catch(Exception ex) {
//            // Coding error if we get here.
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }

        return subPanel;
    }

    
    /** New interface added for migration to Sun DD API model.  If the backing
     *  model stores the properties in a parent property, then this is the factory
     *  for creating instances of the parent to store each row, as added by the
     *  user.
     */
    public interface ParentPropertyFactory {

        /* Implement this method to return a new blank instance of the correct
         * bean type, e.g. WebserviceEndpoint, etc.
         * 
         * Glorified function pointer really, guess we do need closures :o
         */
//        public CommonDDBean newParentProperty(ASDDVersion asVersion);
        public CommonDDBean newInstance(CommonDDBean parent);

    }
}
