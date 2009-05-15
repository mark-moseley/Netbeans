/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class AvailableTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer container = Containers.forAvailable();
    private OperationContainer containerCustom = Containers.forCustomInstall ();
    private static String col0, col1, col2, col3;
    
    /** Creates a new instance of AvailableTableModel
     * @param units 
     */
    public AvailableTableModel (List<UpdateUnit> units) {
        setUnits(units);
    }
    
    public final void setUnits (List<UpdateUnit> units) {
        setData(Utilities.makeAvailableCategories (units, false));
    }
    
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        // second column is editable but doesn't want to edit its value
        if (isExpansionControlAtRow(row)) return;//NOI18N        
        if (col == 1) {
            return ;
        }
        super.setValueAt(anValue, row, col);
        if (anValue == null) {
            return ;
        }
        //assert getCategoryAtRow (row).isExpanded ();
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        boolean beforeMarked = u.isMarked();
        u.setMarked(!beforeMarked);
        if (u.isMarked() != beforeMarked) {
            fireButtonsChange();
        }
    }

    public Object getValueAt(int row, int col) {
        Object res = null;
        if (isExpansionControlAtRow(row)) return "";//NOI18N
        
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        if (u == null) {
            return null;
        }
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
            res = u.getSourceCategory();
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
            res = CATEGORY.class;
            break;
        }
        
        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0 :
                if (col0 == null) {
                    col0 = getBundle ("AvailableTableModel_Columns_Install");
                }
                return col0;
            case 1 :
                if (col1 == null) {
                    col1 = getBundle ("AvailableTableModel_Columns_Name");
                }
                return col1;
            case 2 :
                if (col2 == null) {
                    col2 = getBundle("AvailableTableModel_Columns_Category");
                }
                return col2;
            case 3 :
                if (col3 == null) {
                    col3 = getBundle ("AvailableTableModel_Source_Category");
                }
                return col3;
        }
        
        assert false;
        return super.getColumnName( column );
    }

    @Override
    public String getToolTipText(int row, int col) {
        if (col == 3) {
            CATEGORY category = (CATEGORY) getValueAt (row, 3);
            return Utilities.getCategoryName(category);
        }
        return super.getToolTipText(row, col);
    }

    public int getPreferredWidth(JTableHeader header, int col) {
        final int minWidth = super.getMinWidth(header, col);
        switch (col) {
        case 1:
            return minWidth*4;
        case 2:
            return minWidth*2;
        }
        return minWidth;
    }
    
    public Type getType () {
        return UnitCategoryTableModel.Type.AVAILABLE;
    }
    
    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isInstall = getColumnName(0).equals(columnIdentifier);
        return isInstall  ? false : true;
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
                    return Unit.Available.compareSourceCategories(unit1, unit2);
                }                
                return 0;
            }
        };
    }

    @SuppressWarnings ("unchecked")
    public int getDownloadSize () {
        int res = 0;
        assert container != null || containerCustom != null: "OperationContainer found when asking for download size.";
        Set<OperationInfo> infos = new HashSet<OperationInfo> ();
        infos.addAll (container.listAll ());
        infos.addAll (containerCustom.listAll ());
        Set<UpdateElement> elements = new HashSet<UpdateElement> ();
        for (OperationInfo info : infos) {
            elements.add (info.getUpdateElement ());
            elements.addAll (info.getRequiredElements ());
        }
        for (UpdateElement el : elements) {
            res += el.getDownloadSize ();
        }
        return res;
    }
        
    private static String getBundle (String key) {
        return NbBundle.getMessage (AvailableTableModel.class, key);
    }

    @Override
    public String getTabTooltipText() {
        if (isTabEnabled()) {
            return super.getTabTooltipText(); 
        }
        return NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_ToolTip");
    }
    
    public String getTabTitle() {
        return NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_Title");//NOI18N
    }

    public int getTabIndex() {
        return PluginManagerUI.INDEX_OF_AVAILABLE_TAB;
    }

    @Override
    public boolean isTabEnabled() {
        return getRawItemCount() > 0;
    }

    public boolean needsRestart () {
        return false;
    }
}
