/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import org.openide.util.NbBundle;

import org.netbeans.api.project.libraries.Library;

import org.netbeans.modules.web.project.classpath.ClassPathSupport;

/**
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class ClassPathUiSupport {
    
    private ClassPathSupport cps;
             
    // Methods for working with list models ------------------------------------
    
    public static DefaultListModel createListModel( Iterator it ) {
        
        DefaultListModel model = new DefaultListModel();
        
        while( it.hasNext() ) {
            model.addElement( it.next() );
        }
        
        return model;
    }
    
    public static ClassPathTableModel createTableModel ( Iterator it ) {
        return new ClassPathTableModel( createListModel( it ) );
    }
    
    public static Iterator getIterator( DefaultListModel model ) {        
        // XXX Better performing impl. would be nice
        return getList( model ).iterator();        
    }
    
    public static List getList( DefaultListModel model ) {
        return Collections.list( model.elements() );
    }
        
    
    /** Moves items up in the list. The indices array will contain 
     * indices to be selected after the change was done.
     */
    public static int[] moveUp( DefaultListModel listModel, int indices[]) {
                
        if( indices == null || indices.length == 0 ) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = 0; i < indices.length; i++ ) {
            Object item = listModel.get( indices[i] );
            listModel.remove( indices[i] );
            listModel.add( indices[i] - 1, item ); 
        }
        
        // Keep the selection a before
        for( int i = 0; i < indices.length; i++ ) {
            indices[i] -= 1;
        }
        
        return indices;
        
    } 
        
    public static boolean canMoveUp( ListSelectionModel selectionModel ) {        
        return selectionModel.getMinSelectionIndex() > 0;
    }
    
    /** Moves items down in the list. The indices array will contain 
     * indices to be selected after the change was done.
     */
    public static int[] moveDown( DefaultListModel listModel, int indices[]) {
        
        if(  indices == null || indices.length == 0 ) {
            assert false : "MoveDown button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = indices.length -1 ; i >= 0 ; i-- ) {
            Object item = listModel.get( indices[i] );
            listModel.remove( indices[i] );
            listModel.add( indices[i] + 1, item ); 
        }
        
        // Keep the selection a before
        for( int i = 0; i < indices.length; i++ ) {
            indices[i] += 1;
        }
        
        return indices;

    }    
        
    public static boolean canMoveDown( ListSelectionModel selectionModel, int modelSize ) {
        int iMax = selectionModel.getMaxSelectionIndex();
        return iMax != -1 && iMax < modelSize - 1;         
    }
    
    /** Removes selected indices from the model. Returns the index to be selected 
     */
    public static int[] remove( DefaultListModel listModel, int[] indices ) {
        
        if(  indices == null || indices.length == 0 ) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        // Remove the items
        for( int i = indices.length - 1 ; i >= 0 ; i-- ) {
            listModel.remove( indices[i] );
        }
                
        if ( !listModel.isEmpty() ) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1; 
            if ( selectedIndex > listModel.size() - 1) {
                selectedIndex = listModel.size() - 1;
            }
            return new int[] { selectedIndex };
        }
        else {
            return new int[] {};
        }
        
    }
    
    public static int[] addLibraries( DefaultListModel listModel, int[] indices, Library[] libraries, Set/*<Library>*/ alreadyIncludedLibs) {
        
        int lastIndex = indices == null || indices.length == 0 ? -1 : indices[indices.length - 1];
        for (int i = 0, j=1; i < libraries.length; i++) {
            if (!alreadyIncludedLibs.contains(libraries[i])) {
                listModel.add( lastIndex + j++, ClassPathSupport.Item.create( libraries[i], null, ClassPathSupport.Item.PATH_IN_WAR_LIB) );
            }
        }
        Set addedLibs = new HashSet (Arrays.asList(libraries));
        int[] indexes = new int[libraries.length];
        for (int i=0, j=0; i<listModel.getSize(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get (i);
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                if (addedLibs.contains(item.getLibrary())) {
                    indexes[j++] =i;
                }
            }
        }
        return indexes;        
    }

    public static int[] addJarFiles( DefaultListModel listModel, int[] indices, File files[]) {
        
        int lastIndex = indices == null || indices.length == 0 ? -1 : indices[indices.length - 1];
        int[] indexes = new int[files.length];
        for( int i = 0; i < files.length; i++ ) {
            int current = lastIndex + 1 + i;
            File f = files[i];
            String pathInWar = (f.isDirectory() ? ClassPathSupport.Item.PATH_IN_WAR_DIR : ClassPathSupport.Item.PATH_IN_WAR_LIB);
            ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, pathInWar);
            if ( !listModel.contains( item ) ) {
                listModel.add( current, item );
                indexes[i] = current;
            }
            else {
                indexes[i] = listModel.indexOf( item );
            }            
        }
        return indexes;

    }
    
    public static int[] addArtifacts( DefaultListModel listModel, int[] indices, AntArtifactChooser.ArtifactItem artifactItems[]) {
        
        int lastIndex = indices == null || indices.length == 0 ? -1 : indices[indices.length - 1];
        int[] indexes = new int[artifactItems.length];
        for( int i = 0; i < artifactItems.length; i++ ) {
            int current = lastIndex + 1 + i;
            ClassPathSupport.Item item = ClassPathSupport.Item.create( artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI(), null, ClassPathSupport.Item.PATH_IN_WAR_LIB) ;
            if ( !listModel.contains( item ) ) {
                listModel.add( current, item );
                indexes[i] = current;
            }
            else {
                indexes[i] = listModel.indexOf( item );
            }            
        }
        return indexes;
    }
    
    // Inner classes -----------------------------------------------------------
    
    /** 
     * Implements a TableModel backed up by a DefaultListModel.
     * This allows the TableModel's data to be used in EditMediator
     */
    public static final class ClassPathTableModel extends AbstractTableModel implements ListDataListener {
        private DefaultListModel model;
        
        public ClassPathTableModel(DefaultListModel model) {
            this.model = model;
            model.addListDataListener(this);
        }
        
        public DefaultListModel getDefaultListModel() {
            return model;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public int getRowCount() {
            return model.getSize();
        }
        
        public String getColumnName(int column) {
            if (column == 0) {
                return NbBundle.getMessage(ClassPathUiSupport.class, "LBL_CustomizeCompile_TableHeader_Name");
            } else {
                return NbBundle.getMessage(ClassPathUiSupport.class, "LBL_CustomizeCompile_TableHeader_Deploy");
            }
        }
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return ClassPathSupport.Item.class;
            } else {
                return Boolean.class;
            }
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex != 0);
        }
        
        public Object getValueAt(int row, int column) {
            if (column == 0)
                return getItem(row);
            else {
                String pathInWar = getItem(row).getPathInWAR();
                return (ClassPathSupport.Item.PATH_IN_WAR_LIB.equals(pathInWar) || ClassPathSupport.Item.PATH_IN_WAR_DIR.equals(pathInWar)) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        
        public void setValueAt(Object value, int row, int column) {
            if (column != 1 || !(value instanceof Boolean))
                return;
            
            if (value == Boolean.TRUE) {
                ClassPathSupport.Item item = getItem(row);
                String pathInWar = (item.getFile().isDirectory() ? ClassPathSupport.Item.PATH_IN_WAR_DIR : ClassPathSupport.Item.PATH_IN_WAR_LIB);
                item.setPathInWAR(pathInWar);
            } else
                getItem(row).setPathInWAR(ClassPathSupport.Item.PATH_IN_WAR_NONE);
            fireTableCellUpdated(row, column);
        }
        
        public void contentsChanged(ListDataEvent e) {
            fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
        }
        
        public void intervalAdded(ListDataEvent e) {
            fireTableRowsInserted(e.getIndex0(), e.getIndex1());
        }
        
        public void intervalRemoved(ListDataEvent e) {
            fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
        }
        
        private ClassPathSupport.Item getItem(int index) {
            return (ClassPathSupport.Item)model.get(index);
        }
        
        private void setItem(ClassPathSupport.Item item, int index) {
            model.set(index, item);
        }
    }
}
