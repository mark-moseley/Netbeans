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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */

public class InstalledTableModel extends UnitCategoryTableModel {
    static final String STATE_ENABLED = NbBundle.getMessage(UpdateTableModel.class,"InstalledTableModel_State_Enabled");
    static final String STATE_DISABLED = NbBundle.getMessage(UpdateTableModel.class,"InstalledTableModel_State_Disabled");
            
    //just prevents from gc, do not delete
    private OperationContainer<OperationSupport> enableContainer = Containers.forEnable();
    private OperationContainer<OperationSupport> disableContainer = Containers.forDisable();
    private OperationContainer<OperationSupport> uninstallContainer = Containers.forUninstall();
    
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.InstalledTableModel");
    
    /** Creates a new instance of InstalledTableModel */
    public InstalledTableModel(List<UpdateUnit> units) {
        setUnits(units);
    }

    public final void setUnits (List<UpdateUnit> units) {    
        setData(Utilities.makeInstalledCategories (units));
    }

    @Override
    public String getToolTipText(int row, int col) {
        if (col == 3) {
            Unit.Installed u = (Unit.Installed) getUnitAtRow (row);
            assert u != null : "Unit must found at row " + row;
            String key = null;
            UpdateElement ue = u.getRelevantElement ();
            UpdateUnit uu = u.getRelevantElement ().getUpdateUnit ();
            if (uu.isPending ()) {
                // installed?
                if (ue.getUpdateUnit ().getInstalled () == null || ! ue.getUpdateUnit ().getInstalled ().equals (ue)) {
                    key = "InstallTab_PendingForInstall_Tooltip";
                } else {
                    key = "InstallTab_PendingForDeactivate_Tooltip";
                }
            } else if (ue.isEnabled ()) {
                key = "InstallTab_Active_Tooltip";
            } else {
                key = "InstallTab_InActive_Tooltip";
            }
            return (key != null) ? getBundle(key) : null;
        }
        return super.getToolTipText(row, col);
    }

    
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        super.setValueAt(anValue, row, col);
        
        if (col == 1) {
            // second column handles buttons
            return ;
        }
        assert col == 0 : "First column.";
        if (anValue == null) {
            return ;
        }
        //assert getCategoryAtRow(row).isExpanded();
        Unit.Installed u = (Unit.Installed) getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        boolean beforeMarked = u.isMarked();
        u.setMarked(! beforeMarked);
        if (u.isMarked() != beforeMarked) {
            fireButtonsChange();
        } else {
            //TODO: message should contain spec.version
            String message = NbBundle.getMessage(UpdateTableModel.class,"NotificationAlreadyPreparedToIntsall",u.getDisplayName());
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
        }
        
    }
    
    public Object getValueAt(int row, int col) {
        Object res = null;
        
        Unit.Installed u = (Unit.Installed) getUnitAtRow(row);
        switch (col) {
        case 0 :
            res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
            break;
        case 1 :
            res = u.getDisplayName();
            break;
        case 2 :
            res = u.getCategoryName();
            break;
        case 3 :
            res = u.getRelevantElement().isEnabled();
            break;
        case 4 :
            res = u.getInstalledVersion();
            break;
        }
        
        return res;
    }

    public int getColumnCount() {
        return 4;
    }
    
    public Class getColumnClass(int c) {
        Class res = null;
        
        switch (c) {
        case 0 :
            res = Boolean.class;
            break;
        case 1 :
            res = String.class;
            break;            
        case 2 :
            res = String.class;
            break;
        case 3 :
            res = Boolean.class;
            break;
        case 4 :
            res = String.class;
            break;
        }
        
        return res;
    }
    
    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0 :
            return getBundle ("InstalledTableModel_Columns_Uninstall");
        case 1 :
            return getBundle ("InstalledTableModel_Columns_Name");
        case 2:
            return getBundle("InstalledTableModel_Columns_Category");
        case 3 :
            return getBundle ("InstalledTableModel_Columns_Enabled");                        
        case 4 :
            return getBundle ("InstalledTableModel_Columns_Installed");
        }
        
        assert false;
        return super.getColumnName( column );
    }

    @Override
    public int getMinWidth(JTableHeader header, int col) {
        return super.getMinWidth(header, col);
    }
    
    
    public int getPreferredWidth(JTableHeader header, int col) {
        switch (col) {
        case 1:
            return super.getMinWidth(header, col)*4;
        case 2:
            return super.getMinWidth(header, col)*2;
        }
        return super.getMinWidth(header, col);
    }
    
    
    public Type getType() {
        return UnitCategoryTableModel.Type.INSTALLED;
    }

    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isUninstall = getColumnName(0).equals(columnIdentifier);
        return isUninstall ? false : true;
    }

    protected Comparator<Unit> getComparator(final Object columnIdentifier, final boolean sortAscending) {
        return new Comparator<Unit>(){
            public int compare(Unit o1, Unit o2) {
                Unit unit1 = sortAscending ? o1 : o2;
                Unit unit2 = sortAscending ? o2 : o1;
                if (getColumnName(0).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                } else if (getColumnName(1).equals(columnIdentifier)) {
                    return Unit.compareDisplayNames(unit1, unit2);
                } else if (getColumnName(2).equals(columnIdentifier)) {
                    return Unit.compareCategories(unit1, unit2);
                } else if (getColumnName(3).equals(columnIdentifier)) {
                    return Unit.Installed.compareEnabledState(unit1, unit2);
                } else if (getColumnName(4).equals(columnIdentifier)) {
                    return Unit.Installed.compareInstalledVersions(unit1, unit2);
                }                 
                return 0;
            }
        };
    }

    public int getDownloadSize () {
        // no need to download anything in Installed tab
        return 0;
    }
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        Unit.Installed u = (Unit.Installed)getUnitAtRow(row);
        return (col == 0) ? u != null && u.canBeMarked() : super.isCellEditable(row, col);
    }

    public String getTabTitle() {
        return NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Installed_Title");//NOI18N
    }

    public int getTabIndex() {
        return 3;
    }

    public boolean needsRestart () {
        return true;
    }

}
