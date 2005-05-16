/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

// Netbeans
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ErrorPagesTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ErrorPageLocation"),
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ErrorCode"),
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ExceptionType")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		ErrorPage page = (ErrorPage)getChildren().get(row);
		if (column == 0) page.setLocation((String)value);
		else if (column == 1) page.setErrorCode((Integer)value);
		else page.setExceptionType((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		ErrorPage page = (ErrorPage)getChildren().get(row);

		if (column == 0) return page.getLocation();
		else if (column == 1) return page.getErrorCode();
		else return page.getExceptionType();
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                ErrorPage page = (ErrorPage)((WebApp)getParent()).createBean("ErrorPage"); //NOI18N
                page.setLocation((String)values[0]);
                if (values[1]!=null) page.setErrorCode((Integer)values[1]);;
                if (values[2]!=null) page.setExceptionType((String)values[2]);
                ((WebApp)getParent()).addErrorPage(page);
                getChildren().add(page);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return page;
            } catch (ClassNotFoundException ex) {}
            return null;
	}


	public void editRow(int row, Object[] values)
	{
                ErrorPage page = (ErrorPage)getChildren().get(row);
		page.setLocation((String)values[0]);
                page.setErrorCode((Integer)values[1]);
                page.setExceptionType((String)values[2]);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            ((WebApp)getParent()).removeErrorPage((ErrorPage)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
}