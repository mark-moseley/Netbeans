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

package org.netbeans.modules.tasklist.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskComparator;
import org.netbeans.modules.tasklist.impl.TaskList;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;

/**
 *
 * @author S. Aubrecht
 */
class FoldingTaskListModel extends TaskListModel {
    
    private LinkedList<FoldingGroup> groups = new LinkedList<FoldingGroup>();
    private HashMap<String,FoldingGroup> groupMap = new HashMap<String,FoldingGroup>(10);
    
    /** Creates a new instance of FoldingTaskListModel */
    public FoldingTaskListModel( TaskList taskList ) {
        super( taskList );
        
        sortTaskList();

        tasksAdded( taskList.getTasks() );
    }
    
    @Override
    public int getRowCount() {
        if( null == list )
            return 0;
        int count = 0;
        for( FoldingGroup g : groups ) {
            count += g.getRowCount();
        }
        return count;
    }

    @Override
    public Class<?> getColumnClass( int column ) {
        if( COL_GROUP == column )
            return FoldingGroup.class;
        return super.getColumnClass( column );
    }
    
    @Override
    protected Task getTaskAtRow( int row ) {
        if( isGroupRow( row ) )
            return null;
        int groupRow = 0;
        for( FoldingGroup g : groups ) {
            if( row < groupRow+g.getRowCount() )
                return g.getTaskAt( row-groupRow-1 );
            groupRow += g.getRowCount();
        }
        return null;
    }

    @Override
    public Object getValueAt(int row, int col) {
        FoldingGroup group = getGroupAtRow( row );
        if( null != group ) {
            switch( col ) {
                case COL_GROUP: {
                    return group;
                }
                default:
                    return null;
            }
        }
        return super.getValueAt( row, col );
    }
    
    FoldingGroup getGroupAtRow( int row ) {
        int groupRow = 0;
        for( FoldingGroup g : groups ) {
            if( g.isEmpty() )
                continue;
            if( row == groupRow )
                return g;
            groupRow += g.getRowCount();
        }
        return null;
    }
    
    private Map<FoldingGroup,List<Task>> divideByGroup( List<? extends Task> tasks ) {
        Map<FoldingGroup,List<Task>> grouppedTasksMap = new HashMap<FoldingGroup,List<Task>>( groupMap.size() );
        for( Task t : tasks ) {
            TaskGroup tg = Accessor.getGroup( t );
            FoldingGroup group = groupMap.get( tg.getName() );
            if( null == group ) {
                group = new FoldingGroup( tg );
                groupMap.put( tg.getName(), group );
                groups.add( group );
                Collections.sort( groups );
            }
            List<Task> tasksInGroup = grouppedTasksMap.get( group );
            if( null == tasksInGroup ) {
                tasksInGroup = new LinkedList<Task>();
                grouppedTasksMap.put( group, tasksInGroup );
            }
            tasksInGroup.add( t );
        }
        return grouppedTasksMap;
    }
    
    @Override
    public void tasksAdded( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        Map<FoldingGroup,List<Task>> grouppedTasksMap = divideByGroup( tasks );
        for( FoldingGroup fg : grouppedTasksMap.keySet() ) {
            List<Task> tasksInGroup = grouppedTasksMap.get( fg );
            fg.add( tasksInGroup );
        }
    }

    @Override
    public void tasksRemoved( List<? extends Task> tasks ) {
        if( tasks.isEmpty() )
            return;
        Map<FoldingGroup,List<Task>> grouppedTasksMap = divideByGroup( tasks );
        for( FoldingGroup fg : grouppedTasksMap.keySet() ) {
            List<Task> tasksInGroup = grouppedTasksMap.get( fg );
            fg.remove( tasksInGroup );
        }
    }

    @Override
    public void cleared() {
        for( FoldingGroup fg : groups ) {
            fg.clear();
        }
    }
    
    public boolean isGroupRow( int row ) {
        return null != getGroupAtRow( row );
    }
    
    public void toggleGroupExpanded( int row ) {
        FoldingGroup fg = getGroupAtRow( row );
        if( null != fg )
            fg.toggleExpanded();
    }
    
    private int getFoldingGroupStartingRow( FoldingGroup fg ) {
        if( fg.isEmpty() )
            return -1;
        int startingRow = 0;
        int groupIndex = groups.indexOf( fg );
        for( int i=0; i<groupIndex; i++ ) {
            startingRow += groups.get( i ).getRowCount();
        }
        return startingRow;
    }
    
    @Override
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
        if( null != groups ) {
            for( FoldingGroup fg : groups ) {
                fg.setComparator( comparator );
            }

            Settings.getDefault().setSortingColumn( sortingCol );
            Settings.getDefault().setAscendingSort( ascending );
        }
        
        fireTableDataChanged();
    }
    
    class FoldingGroup implements Comparable<FoldingTaskListModel.FoldingGroup> {
        private TaskGroup tg;
        private ArrayList<Task> tasks = new ArrayList<Task>( 100 );
        private boolean isExpanded;
        private Comparator<Task> comparator;
        
        public FoldingGroup( TaskGroup tg ) {
            this.tg = tg;
            isExpanded = Settings.getDefault().isGroupExpanded( tg.getName() );
        }
        
        public void add( List<Task> newTasks ) {
            boolean wasEmpty = isEmpty();
            tasks.addAll( newTasks );
            Collections.sort( tasks, getComparator() );
            
            int startingRow = getFoldingGroupStartingRow( this );
            
            if( wasEmpty ) {
                fireTableRowsInserted( startingRow, startingRow+getRowCount() );
            } else {
                if( isExpanded ) {
                    int firstRow = Integer.MAX_VALUE;
                    int lastRow = Integer.MIN_VALUE;
                    for( Task t : newTasks ) {
                        int index = tasks.indexOf( t );
                        if( index < firstRow )
                            firstRow = index;
                        if( index > lastRow )
                            lastRow = index;
                    }
                    fireTableRowsInserted( firstRow+startingRow+1, lastRow+startingRow+1 );
                }
                fireTableCellUpdated( startingRow, COL_DESCRIPTION );
            }
        }
        
        public void remove( List<Task> removedTasks ) {
            int firstRow = Integer.MAX_VALUE;
            int lastRow = Integer.MIN_VALUE;
            int rowCount = getRowCount();
            if( isExpanded ) {
                for( Task t : removedTasks ) {
                    int index = tasks.indexOf( t );
                    if( index < firstRow )
                        firstRow = index;
                    if( index > lastRow )
                        lastRow = index;
                }
            }
            tasks.removeAll( removedTasks );
            
            int startingRow = getFoldingGroupStartingRow( this );
            if( isEmpty() ) {
                fireTableRowsDeleted( startingRow, startingRow+rowCount );
            } else {
                if( isExpanded ) {
                    fireTableRowsDeleted( firstRow+startingRow+1, lastRow+startingRow+1 );
                }
                fireTableCellUpdated( startingRow, COL_DESCRIPTION );
            }
        }
        
        public void clear() {
            if( isEmpty() )
                return;
            
            int rowCount = getRowCount();
            int startingRow = getFoldingGroupStartingRow( this );
            tasks.clear();
            
            fireTableRowsDeleted( startingRow, startingRow+rowCount );
        }
        
        public boolean isEmpty() {
            return tasks.isEmpty();
        }
        
        public void setExpanded( boolean expand ) {
            if( isExpanded == expand )
                return;
            toggleExpanded();
        }
        
        public void toggleExpanded() {
            this.isExpanded = !isExpanded;
            
            Settings.getDefault().setGroupExpanded( tg.getName(), isExpanded );
            
            int firstRow = 0;
            int groupIndex = groups.indexOf( this );
            for( int i=0; i<groupIndex; i++ ) {
                firstRow += groups.get( i ).getRowCount();
            }
            int lastRow = firstRow + getTaskCount();
            firstRow += 1;
            
            if( isExpanded )
                fireTableRowsInserted( firstRow, lastRow );
            else
                fireTableRowsDeleted( firstRow, lastRow );
            fireTableCellUpdated( firstRow-1, COL_GROUP );
        }
        
        public int getRowCount() {
            return isEmpty() ? 0 : (isExpanded ? 1+tasks.size() : 1);
        }
        
        public int getTaskCount() {
            return tasks.size();
        }
        
        public Task getTaskAt( int index ) {
            return tasks.get( index );
        }
    
        public int compareTo(org.netbeans.modules.tasklist.ui.FoldingTaskListModel.FoldingGroup other) {
            List<? extends TaskGroup> groupList = TaskGroup.getGroups();
            int myIndex = groupList.indexOf( tg );
            int otherIndex = groupList.indexOf( other.tg );
            return myIndex - otherIndex;
        }
        
        public boolean isExpanded() {
            return isExpanded;
        }
        
        public TaskGroup getGroup() {
            return tg;
        }
        
        private Comparator<Task> getComparator() {
            if( null == comparator )
                comparator = TaskComparator.getDefault();
            return comparator;
        }
        
        private void setComparator( Comparator<Task> newComparator ) {
            if( getComparator().equals( newComparator ) )
                return;
            comparator = newComparator;
            if( !tasks.isEmpty() ) {
                Collections.sort( tasks, getComparator() );
                if( isExpanded() ) {
                    int firstRow = 0;
                    int groupIndex = groups.indexOf( this );
                    for( int i=0; i<groupIndex; i++ ) {
                        firstRow += groups.get( i ).getRowCount();
                    }
                    int lastRow = firstRow + getTaskCount();
                    firstRow += 1;

                    fireTableRowsUpdated( firstRow, lastRow );
                }
            }
        }
    }
}
