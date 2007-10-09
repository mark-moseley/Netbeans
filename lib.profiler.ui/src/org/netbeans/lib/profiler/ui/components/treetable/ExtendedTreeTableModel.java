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

package org.netbeans.lib.profiler.ui.components.treetable;


/**
 * TreeTable model that extends AbstractTableModel and allows to hide columns
 *
 * @author  Jiri Sedlacek
 */
public class ExtendedTreeTableModel extends AbstractTreeTableModel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractTreeTableModel realModel;
    private int[] columnsMapping; // mapping virtual columns -> real columns
    private boolean[] columnsVisibility; // visibility flags of real columns
    private int realColumnsCount;
    private int virtualColumnsCount;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ExtendedTreeTableModel(AbstractTreeTableModel realModel) {
        super(realModel.root, realModel.supportsSorting, realModel.initialSortingColumn, realModel.initialSortingOrder);

        realColumnsCount = realModel.getColumnCount();
        virtualColumnsCount = realColumnsCount;

        this.realModel = realModel;
        columnsMapping = new int[realColumnsCount];

        boolean[] initialColumnsVisibility = new boolean[realColumnsCount];

        for (int i = 0; i < realColumnsCount; i++) {
            initialColumnsVisibility[i] = true;
        }

        setColumnsVisibility(initialColumnsVisibility);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isCellEditable(Object node, int column) {
        return realModel.isCellEditable(node, getRealColumn(column));
    }

    public Class getColumnClass(int col) {
        return realModel.getColumnClass(getRealColumn(col));
    }

    public int getColumnCount() {
        return virtualColumnsCount;
    }

    //---------------------
    // AbstractTreeTableModel interface
    public String getColumnName(int col) {
        return realModel.getColumnName(getRealColumn(col));
    }

    public String getColumnToolTipText(int columnIndex) {
        int realColumn = getRealColumn(columnIndex);

        if (realColumn == -1) {
            return null;
        }

        return realModel.getColumnToolTipText(realColumn);
    }

    public void setColumnsVisibility(boolean[] columnsVisibility) {
        this.columnsVisibility = columnsVisibility;
        recomputeColumnsMapping();
    }

    public boolean[] getColumnsVisibility() {
        return columnsVisibility;
    }

    public boolean getInitialSorting(int column) {
        return realModel.getInitialSorting(getRealColumn(column));
    }

    public int getInitialSortingColumn() {
        return realModel.getInitialSortingColumn();
    }

    public boolean getInitialSortingOrder() {
        return realModel.getInitialSortingOrder();
    }

    public boolean isLeaf(Object node) {
        return realModel.isLeaf(node);
    }

    public int getRealColumn(int column) {
        if ((column > -1) && (column < columnsMapping.length)) {
            return columnsMapping[column];
        }

        return -1;
    }

    public void setRealColumnVisibility(int column, boolean visible) {
        if (visible) {
            showRealColumn(column);
        } else {
            hideRealColumn(column);
        }
    }

    public boolean isRealColumnVisible(int column) {
        if ((column > -1) && (column < columnsMapping.length)) {
            return columnsVisibility[column];
        }

        return false;
    }

    public void setRoot(Object root) {
        realModel.setRoot(root);
    }

    public Object getRoot() {
        return realModel.getRoot();
    }

    public void setValueAt(Object aValue, Object node, int column) {
        realModel.setValueAt(aValue, node, getRealColumn(column));
    }

    /*public Object getValueAt (int rowIndex, int columnIndex) {
       return realModel.getValueAt(rowIndex, getRealColumn(columnIndex));
       }*/
    public Object getValueAt(Object node, int column) {
        return realModel.getValueAt(node, getRealColumn(column));
    }

    public int getVirtualColumn(int column) {
        for (int i = 0; i < virtualColumnsCount; i++) {
            if (getRealColumn(i) == column) {
                return i;
            }
        }

        return -1;
    }

    public void hideRealColumn(int column) {
        if (isRealColumnVisible(column)) {
            columnsVisibility[column] = false;
            recomputeColumnsMapping();
        }
    }

    public void showRealColumn(int column) {
        if (!isRealColumnVisible(column)) {
            columnsVisibility[column] = true;
            recomputeColumnsMapping();
        }
    }

    public void sortByColumn(int column, boolean order) {
        realModel.sortByColumn(getRealColumn(column), order);
    }

    private void recomputeColumnsMapping() {
        virtualColumnsCount = 0;

        int virtualColumnIndex = 0;

        // set indexes virtual columns -> real columns
        for (int i = 0; i < realColumnsCount; i++) {
            if (columnsVisibility[i] == true) {
                columnsMapping[virtualColumnIndex] = i;
                virtualColumnsCount++;
                virtualColumnIndex++;
            }
        }

        // clear mappings of unused real columns
        for (int i = virtualColumnIndex; i < realColumnsCount; i++) {
            columnsMapping[i] = -1;
        }
    }
}
