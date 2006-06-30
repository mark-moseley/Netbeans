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

/** MessageDestRefTableModel - table model for message desctination references
 *
 * Created on April 14, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class MessageDestRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(MessageDestRefTableModel.class,"TTL_MessageDestRefName"),
            NbBundle.getMessage(MessageDestRefTableModel.class,"TTL_MessageDestRefType"),
            NbBundle.getMessage(MessageDestRefTableModel.class,"TTL_MessageDestRefUsage"),
            NbBundle.getMessage(MessageDestRefTableModel.class,"TTL_MessageDestRefLink"),
            NbBundle.getMessage(MessageDestRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		MessageDestinationRef param = getMessageDestRef(row);

		if (column == 0) param.setMessageDestinationRefName((String)value);
		else if (column == 1) param.setMessageDestinationType((String)value);
		else if (column == 2) param.setMessageDestinationUsage((String)value);
                else if (column == 3) param.setMessageDestinationLink((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		MessageDestinationRef param = getMessageDestRef(row);

		if (column == 0) return param.getMessageDestinationRefName();
		else if (column == 1) return param.getMessageDestinationType();
		else if (column == 2) return param.getMessageDestinationUsage();
                else if (column == 3) return param.getMessageDestinationLink();
		else {
                    String desc = param.getDefaultDescription();
                    return desc==null?null:desc.trim();
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                MessageDestinationRef param=(MessageDestinationRef)webApp.createBean("MessageDestinationRef"); //NOI18N
                param.setMessageDestinationRefName((String)values[0]);
                param.setMessageDestinationType((String)values[1]);
                param.setMessageDestinationUsage((String)values[2]);
                String link = (String)values[3];
                param.setMessageDestinationLink(link.length()>0?link:null);
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                webApp.addMessageDestinationRef(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
              catch (VersionNotSupportedException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
                MessageDestinationRef param = getMessageDestRef(row);
                param.setMessageDestinationRefName((String)values[0]);
                param.setMessageDestinationType((String)values[1]);
                param.setMessageDestinationUsage((String)values[2]);
                String link = (String)values[3];
                param.setMessageDestinationLink(link.length()>0?link:null);
                String desc = (String)values[4];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                webApp.removeMessageDestinationRef(getMessageDestRef(row));
                getChildren().remove(row);
                fireTableRowsDeleted(row, row);
            } catch (VersionNotSupportedException ex) {}
            
	}
        
        MessageDestinationRef getMessageDestRef(int row) {
            return (MessageDestinationRef)getChildren().get(row);
        }
}