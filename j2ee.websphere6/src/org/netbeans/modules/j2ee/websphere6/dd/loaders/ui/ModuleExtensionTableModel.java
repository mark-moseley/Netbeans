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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

// Netbeans
import java.util.List;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppExt;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ModuleExtensionsType;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.xml.multiview.*;

public class ModuleExtensionTableModel extends org.netbeans.modules.j2ee.websphere6.dd.loaders.ui.InnerTableModel {

    private List children;
    private BaseBean parent;
    private static final String[] columnNames = {
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_ModuleExtensionId"),
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_ModuleId"),
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_ModuleExtensionAltRoot"),
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_ModuleExtensionAltBindigs"),
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_ModuleExtensionAltExtensions"),
        NbBundle.getMessage(ModuleExtensionTableModel.class,"TTL_Type")
    };
    
    private static final int [] columnWidths= {200, 160, 180};
    
    public ModuleExtensionTableModel(XmlMultiViewDataSynchronizer synchronizer)    {
        super(synchronizer,columnNames,columnWidths);
    }
    
    
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    protected BaseBean getParent() {
        return parent;
    }
    
    protected List getChildren() {
        return children;
    }
    
    public int getColumnCount() {
        return getColumnNames().length;
    }
    
    public int getRowCount() {
        if (children != null) {
            return (children.size());
        } else {
            return (0);
        }
    }
    
    
    public String getColumnName(int column) {
        return getColumnNames()[column];
    }
    
    public boolean isCellEditable(int row, int column) {
        return (false);
    }
    
    public int getRowWithValue(int column, Object value) {
        for(int row = 0; row < getRowCount(); row++) {
            Object obj = getValueAt(row, column);
            if (obj.equals(value)) {
                return (row);
            }
        }
        
        return (-1);
    }
    
    
    
    public void setData(BaseBean parent,BaseBean[] children) {
        this.parent = parent;
        this.children = new java.util.ArrayList();
        if (children==null) return;
        for(int i=0;i<children.length;i++)
            this.children.add(children[i]);
        fireTableDataChanged();
    }
    
    
    public void setValueAt(Object value, int row, int column) {
        ModuleExtensionsType moduleExtension = (ModuleExtensionsType)getChildren().get(row);
        
        if (column == 0) moduleExtension.setXmiId((String)value);
        else if(column==1) moduleExtension.setHref((String)value);
        else if(column==2) moduleExtension.setAltRoot((String)value);
        else if(column==3) moduleExtension.setAltBindings((String)value);
        else if(column==4) moduleExtension.setAltExtensions((String)value);
        else if(column==5) moduleExtension.setType((String)value);
    }
    
    
    public Object getValueAt(int row, int column) {
        ModuleExtensionsType moduleExtension = (ModuleExtensionsType)getChildren().get(row);
        
        if (column == 0) return moduleExtension.getXmiId();
        else if(column == 1) return moduleExtension.getHref();
        else if(column == 2) return moduleExtension.getAltRoot();
        else if(column == 3) return moduleExtension.getAltBindings();
        else if(column == 4) return moduleExtension.getAltExtensions();
        else if(column == 5) return moduleExtension.getType();
        return null;
    }
    
    public BaseBean addRow(Object[] values) {
        //try {
        ModuleExtensionsType moduleExtension = new ModuleExtensionsType();
        moduleExtension.setXmiId((String)values[0]);
        moduleExtension.setHref((String)values[1]);
        moduleExtension.setAltRoot((String)values[2]);
        moduleExtension.setAltBindings((String)values[3]);
        moduleExtension.setAltExtensions((String)values[4]);
        moduleExtension.setType((String)values[5]);
        ((WSAppExt)getParent()).addModuleExtensions(moduleExtension);
        getChildren().add(moduleExtension);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        return moduleExtension;
        //} //catch (ClassNotFoundException ex) {}
        
        //return null;
    }
    
    public int addRow() {
        return -1;
    }
    
    
    public void editRow(int row, Object[] values) {
        ModuleExtensionsType moduleExtension = (ModuleExtensionsType)getChildren().get(row);
        moduleExtension.setXmiId((String)values[0]);
        moduleExtension.setHref((String)values[1]);
        moduleExtension.setAltRoot((String)values[2]);
        moduleExtension.setAltBindings((String)values[3]);
        moduleExtension.setAltExtensions((String)values[4]);
        moduleExtension.setType((String)values[5]);
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        ((WSAppExt)getParent()).removeModuleExtensions((ModuleExtensionsType)getChildren().get(row));
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
        
    }
}