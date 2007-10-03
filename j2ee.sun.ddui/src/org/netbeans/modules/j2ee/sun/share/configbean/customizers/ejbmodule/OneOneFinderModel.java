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
/*
 * OneOneFinderModel.java        November 3, 2003, 12:14 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.share.configbean.CmpEntityEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class OneOneFinderModel extends BeanTableModel {
    /* A class implementation comment can go here. */

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N


    private static final String[] columnNames = {
        bundle.getString("LBL_Method_Name"),                            //NOI18N
        bundle.getString("LBL_Query_Params"),                           //NOI18N
        bundle.getString("LBL_Query_Filter"),                           //NOI18N
        bundle.getString("LBL_Query_Variables"),                        //NOI18N
        bundle.getString("LBL_Query_Ordering")};                        //NOI18N


    protected String[] getColumnNames() {
        return columnNames;
    }


    public Object getValueAt(int row, int column){
        Object retValue = null;
        Finder param = (Finder)getChildren().get(row);
        if(param != null){
            switch(column){
                case 0: {
                    retValue = param.getMethodName();
                }
                break;
                case 1: {
                    retValue = param.getQueryParams();
                }
                break;
                case 2: {
                    retValue = param.getQueryFilter();
                }
                break;
                case 3: {
                    retValue = param.getQueryVariables();
                }
                break;
                case 4: {
                    retValue = param.getQueryOrdering();
                }
                break;
                default: {
                    //control should never reach here
                    assert(false);
                }
                break;
            }
        }
        return retValue;
    }


    //BeanTableModel Methods
    public Object addRow(Object[] values){
        StorageBeanFactory storageFactory = ((CmpEntityEjb) getParent()).getConfig().getStorageFactory();
        Finder param = storageFactory.createFinder();
        param.setMethodName((String)values[0]);
        param.setQueryParams((String)values[1]);
        param.setQueryFilter((String)values[2]);
        param.setQueryVariables((String)values[3]);
        param.setQueryOrdering((String)values[4]);

        ((CmpEntityEjb)getParent()).addFinder(param);
        getChildren().add(param);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        return param;
    }


    public void editRow(int row, Object[] values){
        Finder param = (Finder)getChildren().get(row);
        if(param != null){
            param.setMethodName((String)values[0]);
            param.setQueryParams((String)values[1]);
            param.setQueryFilter((String)values[2]);
            param.setQueryVariables((String)values[3]);
            param.setQueryOrdering((String)values[4]);
            fireTableDataChanged();
        }
    }


    public void removeRow(int row){
        ((CmpEntityEjb)getParent()).removeFinder(
            (Finder)getChildren().get(row));
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }


    public Object[] getValues(int row){
        Object[] values = new Object[5];
        Finder finder =
            (Finder)getChildren().get(row);
        if(finder != null){
            values[0] = (Object)finder.getMethodName();
            values[1] = (Object)finder.getQueryParams();
            values[2] = (Object)finder.getQueryFilter();
            values[3] = (Object)finder.getQueryVariables();
            values[4] = (Object)finder.getQueryOrdering();
        }
        return values; 
    }


    //check whether the given object already exists.
    public boolean alreadyExists(Object[] values){
        boolean exists = false;

        if(values != null){
            String methodName = (String)values[0];
            if(methodName != null){
                int count = getRowCount();
                Finder finder;
                for(int i=0; i<count; i++){
                    finder = (Finder)getChildren().get(i);
                    if(finder != null){
                        if(methodName.equals(
                                finder.getMethodName())){
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }
}
