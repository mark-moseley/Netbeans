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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
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
public class UpdateTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer<InstallSupport> container = Containers.forUpdate ();
    private OperationContainer<OperationSupport> containerCustom = Containers.forCustomInstall ();
    
    /** Creates a new instance of UpdateTableModel */
    public UpdateTableModel (List<UpdateUnit> units) {
        setUnits (units);
    }
    
    public final void setUnits (List<UpdateUnit> units) {        
        setData (Utilities.makeUpdateCategories (units, false));
    }
    
    @Override
    public void setValueAt (Object anValue, int row, int col) {
        // second column is editable but doesn't want to edit its value
        if (col == 1) {
            return ;
        }
        super.setValueAt (anValue, row, col);
        if (! isCategoryAtRow (row)) {
            //assert getCategoryAtRow (row).isExpanded ();
            Unit.Update u = (Unit.Update) getUnitAtRow (row);
            assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
            boolean beforeMarked = u.isMarked ();
            u.setMarked (!beforeMarked);
            if (u.isMarked () != beforeMarked) {
                fireButtonsChange ();
            } else {
                //TODO: message should contain spec.version
                String message = NbBundle.getMessage (UpdateTableModel.class, "NotificationAlreadyPreparedToIntsall", u.getDisplayName ()); // NOI18N
                DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message (message));
            }
        }
    }
    
    public Object getValueAt (int row, int col) {
        Object res = null;
        
        if (isCategoryAtRow (row)) {
            res = col == 0 ? getCategoryAtRow (row) : null;
        } else {
            //assert getCategoryAtRow (row).isExpanded ();
            Unit.Update u = (Unit.Update) getUnitAtRow (row);
            switch (col) {
            case 0 :
                res = u.isMarked () ? Boolean.TRUE : Boolean.FALSE;
                break;
            case 1 :
                res = u.getDisplayName ();
                break;
            case 2 :
                if (Utilities.modulesOnly ()) {
                    res = u.getCategoryName ();
                } else {
                    res = u.getDisplayDate ();
                }
                break;
            case 3 :
                res = u.getInstalledVersion ();
                break;
            case 4 :
                res = u.getAvailableVersion ();
                break;
            case 5 :
                res = Utilities.getDownloadSizeAsString (u.getCompleteSize ());
                break;
            }
        }
        return res;
    }
    
    public int getColumnCount () {
        return 3;
    }
    
    public Class getColumnClass (int c) {
        Class res = null;
        
        switch (c) {
        case 0 :
            res = Boolean.class;
            break;
        case 1 :
            res = DisplayName.class;
            break;
        case 2 :
            res = String.class;
            break;
        case 3 :
            res = String.class;
            break;
        case 4 :
            res = String.class;
            break;
        case 5 :
            res = String.class;
            break;
        }
        
        return res;
    }
    
    @Override
    public String getColumnName (int column) {
        switch (column) {
        case 0 :
            return getBundle ("UpdateTableModel_Columns_Update");
        case 1 :
            return getBundle ("UpdateTableModel_Columns_Name");
        case 2 :
            if (Utilities.modulesOnly ()) {
                return getBundle ("UpdateTableModel_Columns_Category");
            } else {
                return getBundle ("UpdateTableModel_Columns_ReleaseDate");
            }
        case 3 :
            return getBundle ("UpdateTableModel_Columns_Installed");
        case 4 :
            return getBundle ("UpdateTableModel_Columns_Available");
        case 5 :
            return getBundle ("UpdateTableModel_Columns_Size");
        }
        
        assert false;
        return super.getColumnName ( column );
    }
    
    public int getPreferredWidth (JTableHeader header, int col) {
        switch (col) {
        case 1:
            return super.getMinWidth (header, col)*4;
        case 2:
            return super.getMinWidth (header, col)*2;
        }
        return super.getMinWidth (header, col);
    }
    
    public Type getType () {
        return UnitCategoryTableModel.Type.UPDATE;
    }
    
    public class DisplayName {
        public DisplayName (String name) {
            
        }
    }
    public boolean isSortAllowed (Object columnIdentifier) {
        boolean isUpdate = getColumnName (0).equals (columnIdentifier);
        return isUpdate ? false : true;
    }
    
    protected Comparator<Unit> getComparator (final Object columnIdentifier, final boolean sortAscending) {
        return new Comparator<Unit>(){
            public int compare (Unit o1, Unit o2) {
                Unit unit1 = sortAscending ? o1 : o2;
                Unit unit2 = sortAscending ? o2 : o1;
                if (getColumnName (0).equals (columnIdentifier)) {
                    assert false : columnIdentifier.toString ();
                } else if (getColumnName (1).equals (columnIdentifier)) {
                    return Unit.compareDisplayNames (unit1, unit2);
                } else if (getColumnName (2).equals (columnIdentifier)) {
                    if (Utilities.modulesOnly ()) {
                        return Unit.compareCategories (unit1, unit2);
                    } else {
                        return Unit.compareSimpleFormatDates (unit1, unit2);
                    }
                } else if (getColumnName (3).equals (columnIdentifier)) {
                    return Unit.Update.compareInstalledVersions (unit1, unit2);
                } else if (getColumnName (4).equals (columnIdentifier)) {
                    return Unit.Update.compareAvailableVersions (unit1, unit2);
                } else if (getColumnName (5).equals (columnIdentifier)) {
                    return Unit.compareCompleteSizes (unit1, unit2);
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
    
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }
}
