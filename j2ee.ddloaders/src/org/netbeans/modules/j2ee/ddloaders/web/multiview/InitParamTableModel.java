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

import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class InitParamTableModel extends DDBeanTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(InitParamTableModel.class, "TTL_InitParamName"),
        NbBundle.getMessage(InitParamTableModel.class, "TTL_InitParamValue"),
        NbBundle.getMessage(InitParamTableModel.class, "TTL_Description")
    };

    protected String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        InitParam param = (InitParam) getChildren().get(row);

        if (column == 0) {
            param.setParamName((String) value);
        } else if (column == 1) {
            param.setParamValue((String) value);
        } else {
            param.setDescription((String) value);
        }
    }

    public Object getValueAt(int row, int column) {
        InitParam param = (InitParam) getChildren().get(row);

        if (column == 0) {
            return param.getParamName();
        } else if (column == 1) {
            return param.getParamValue();
        } else {
            String desc = param.getDefaultDescription();
            return (desc == null ? null : desc.trim());
        }
    }

    public CommonDDBean addRow(Object[] values) {
        try {
            Object parent = getParent();
            InitParam param = null;
            if (parent instanceof Servlet) {
                param = (InitParam) ((Servlet) parent).createBean("InitParam"); //NOI18N
            } else if (parent instanceof Filter) {
                param = (InitParam) ((Filter) parent).createBean("InitParam"); //NOI18N
            } else {
                param = (InitParam) ((WebApp) parent).createBean("InitParam"); //NOI18N
            }
            param.setParamName((String) values[0]);
            param.setParamValue((String) values[1]);
            String desc = (String) values[2];
            if (desc.length() > 0) {
                param.setDescription(desc);
            }
            if (parent instanceof Servlet) {
                ((Servlet) parent).addInitParam(param);
            } else if (parent instanceof Filter) {
                ((Filter) parent).addInitParam(param);
            } else {
                ((WebApp) parent).addContextParam(param);
            }
            getChildren().add(param);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
            return param;
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    public void editRow(int row, Object[] values) {
        InitParam param = (InitParam) getChildren().get(row);
        param.setParamName((String) values[0]);
        param.setParamValue((String) values[1]);
        String desc = (String) values[2];
        if (desc.length() > 0) {
            param.setDescription(desc);
        }
        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        Object parent = getParent();
        if (parent instanceof Servlet) {
            ((Servlet) parent).removeInitParam((InitParam) getChildren().get(row));
        } else if (parent instanceof Filter) {
            ((Filter) parent).removeInitParam((InitParam) getChildren().get(row));
        } else {
            ((WebApp) parent).removeContextParam((InitParam) getChildren().get(row));
        }
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);

    }
}