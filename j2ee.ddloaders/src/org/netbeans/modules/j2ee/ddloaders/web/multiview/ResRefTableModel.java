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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

/** ResRefTableModel - table model for resource references
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ResRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResRefName"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResType"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResAuth"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_ResSharingScope"),
            NbBundle.getMessage(ResRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		ResourceRef param = getResourceRef(row);

		if (column == 0) param.setResRefName((String)value);
		else if (column == 1) param.setResType((String)value);
		else if (column == 2) param.setResAuth((String)value);
                else if (column == 3) param.setResSharingScope((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		ResourceRef param = getResourceRef(row);

		if (column == 0) return param.getResRefName();
		else if (column == 1) return param.getResType();
		else if (column == 2) return param.getResAuth();
                else if (column == 3) {
                    String scope = param.getResSharingScope();
                    return ("Unshareable".equals(scope)?scope:"Shareable"); //NOI18N
                }
		else {
                    String desc = param.getDefaultDescription();
                    return desc==null?null:desc.trim();
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                ResourceRef param=(ResourceRef)webApp.createBean("ResourceRef"); //NOI18N
                param.setResRefName((String)values[0]);
                param.setResType((String)values[1]);
                param.setResAuth((String)values[2]);
                param.setResSharingScope((String)values[3]);
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                webApp.addResourceRef(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
                ResourceRef param = getResourceRef(row);
                param.setResRefName((String)values[0]);
                param.setResType((String)values[1]);
                param.setResAuth((String)values[2]);
                String scope = (String)values[3];
                String oldScope = param.getResSharingScope();
                if (oldScope==null && "Unshareable".equals(scope)) param.setResSharingScope(scope); //NOI18N
                else if (!oldScope.equals(scope)) param.setResSharingScope(scope);
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            WebApp webApp = (WebApp)getParent();
            webApp.removeResourceRef(getResourceRef(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        ResourceRef getResourceRef(int row) {
            return (ResourceRef)getChildren().get(row);
        }
}