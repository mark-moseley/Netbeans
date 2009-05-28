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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    @Override
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
                if (values[1]!=null) page.setErrorCode((Integer)values[1]);
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