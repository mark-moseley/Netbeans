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

package org.netbeans.modules.tasklist.ui;

import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskComparator;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class TaskListModel extends AbstractTableModel implements TaskList.Listener {
    
    protected TaskList list;
    
    protected static final int COL_GROUP = 0;
    protected static final int COL_DESCRIPTION = 1;
    protected static final int COL_FILE = 2;
    protected static final int COL_LOCATION = 4;
    protected static final int COL_LINE = 3;
            
    /** Creates a new instance of TaskListModel */
    public TaskListModel( TaskList taskList ) {
        this.list = taskList;
        
        sortingCol = Settings.getDefault().getSortingColumn();
        ascending = Settings.getDefault().isAscendingSort();
        sortTaskList();
    }
    
    public int getRowCount() {
        return null == list ? 0 : list.size();
    }
    
    public int getColumnCount() {
        return 5;
    }
    
    @Override
    public Class<?> getColumnClass( int column ) {
        if( COL_GROUP == column )
            return TaskGroup.class;
        return super.getColumnClass( column );
    }
    
    @Override
    public String getColumnName(int column) {
        switch( column ) {
            case COL_GROUP: //group icon
                return ""; //NOI18N
            case COL_DESCRIPTION:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_Description" ); //NOI18N
            case COL_FILE:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_File" ); //NOI18N
            case COL_LOCATION:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_Location" ); //NOI18N
            case COL_LINE:
                return NbBundle.getMessage( TaskListModel.class, "LBL_COL_Line" ); //NOI18N
        }
        return super.getColumnName( column );
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return false;
    }
    
    public Object getValueAt(int row, int col) {
        Task t = getTaskAtRow( row );
        if( null != t ) {
            switch( col ) {
                case COL_GROUP: //group icon
                    return Accessor.getGroup( t );
                case COL_DESCRIPTION:
                    return Accessor.getDescription( t );
                case COL_FILE: {
                    FileObject fo = Accessor.getResource( t );
                    if( null == fo || fo.isFolder() )
                        return null;
                    return fo.getNameExt();
                }
                case COL_LOCATION: {
                    FileObject fo = Accessor.getResource( t );
                    if( null == fo || fo.isFolder() )
                        return FileUtil.getFileDisplayName( fo );
                    return FileUtil.getFileDisplayName( fo.getParent() );
                }
                case COL_LINE:
                    int lineNo = Accessor.getLine( t );
                    return lineNo > 0 ? String.valueOf( lineNo ) : null;
            }
        }
        return null;
    }
    
    protected Task getTaskAtRow( int row ) {
        return list.getTask( row );
    }

    public void tasksAdded( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        int startRow = list.indexOf( tasks.get(0) );
        int endRow = list.indexOf( tasks.get(tasks.size()-1) );
        fireTableRowsInserted( startRow, endRow );
    }

    public void tasksRemoved( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        int startRow = list.indexOf( tasks.get(0) );
        int endRow = list.indexOf( tasks.get(tasks.size()-1) );
        fireTableRowsDeleted( startRow, endRow );
    }

    public void cleared() {
        fireTableDataChanged();
    }
    
    protected int sortingCol = -1;
    protected boolean ascending = true;
    
    public void toggleSort( int column ) {
        if( column != sortingCol ) {
            sortingCol = column;
            ascending = true;
        } else {
            if( ascending ) {
                ascending = false;
            } else {
                sortingCol = -1;
            }
        }
        
        sortTaskList();
    }
    
    protected void sortTaskList() {
        Comparator<Task> comparator = null;
        switch( sortingCol ) {
        case COL_DESCRIPTION:
            comparator = TaskComparator.getDescriptionComparator( ascending );
            break;
        case COL_LINE:
            comparator = TaskComparator.getLineComparator( ascending );
            break;
        case COL_LOCATION:
            comparator = TaskComparator.getLocationComparator( ascending );
            break;
        case COL_FILE:
            comparator = TaskComparator.getFileComparator( ascending );
            break;
        default:
            comparator = TaskComparator.getDefault();
            break;
        }
        list.setComparator( comparator );

        Settings.getDefault().setSortingColumn( sortingCol );
        Settings.getDefault().setAscendingSort( ascending );

        fireTableDataChanged();
    }
    
    public int getSortingColumnn() {
        return sortingCol;
    }
    
    public boolean isAscendingSort() {
        return ascending;
    }
    
    public void setAscendingSort( boolean asc ) {
        if( sortingCol >= 0 ) {
            ascending = asc;
        
            sortTaskList();
        }
    }
    
    void attach() {
        list.addListener( this );
    }
    
    void detach() {
        list.removeListener( this );
    }
}
