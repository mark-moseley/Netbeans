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

import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.openide.util.NbBundle;

import org.netbeans.api.project.libraries.Library;

import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.netbeans.spi.java.project.support.ui.EditJarSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.NbCollections;

/**
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class ClassPathUiSupport {
    
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
    
    public static Iterator<ClassPathSupport.Item> getIterator( DefaultListModel model ) {        
        // XXX Better performing impl. would be nice
        return getList( model ).iterator();        
    }
    
    public static List<ClassPathSupport.Item> getList( DefaultListModel model ) {
        return Collections.list(NbCollections.checkedEnumerationByFilter(model.elements(), ClassPathSupport.Item.class, true));
    }
        
    
    public static boolean canEdit( ListSelectionModel selectionModel, DefaultListModel listModel ) {        
        boolean can =  selectionModel.getMinSelectionIndex() == selectionModel.getMaxSelectionIndex() 
                          && selectionModel.getMinSelectionIndex() != -1;
        if (can) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) listModel.get(selectionModel.getMinSelectionIndex());
            can =  item.getType() == ClassPathSupport.Item.TYPE_JAR || item.getType() == ClassPathSupport.Item.TYPE_LIBRARY;
            if (item.isBroken()) {
                can = false;
            }
        }
        return can;
    }
    
    static void edit(DefaultListModel listModel, int[] selectedIndices, AntProjectHelper helper) {
        ClassPathSupport.Item item = (ClassPathSupport.Item) listModel.getElementAt(selectedIndices[0]);
        if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
            EditJarSupport.Item eji = new EditJarSupport.Item();
            eji.setJarFile(item.getFile());
            eji.setSourceFile(item.getSourceFile());
            eji.setJavadocFile(item.getJavadocFile());
            eji = EditJarSupport.showEditDialog(helper, eji);
            if (eji != null) {
                item.setJavadocFile(eji.getJavadocFile());
                item.setSourceFile(eji.getSourceFile());
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
            if (item.getLibrary() != null) {
                LibrariesCustomizer.showSingleLibraryCustomizer(item.getLibrary());
            }
        }
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
    
    public static int[] addLibraries( DefaultListModel listModel, int[] indices, Library[] libraries, Set<Library> alreadyIncludedLibs) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        for (int i = 0, j=1; i < libraries.length; i++) {
            if (!alreadyIncludedLibs.contains(libraries[i])) {
                listModel.add( lastIndex + j++, ClassPathSupport.Item.create( libraries[i], null, ClassPathSupport.Item.PATH_IN_WAR_LIB) );
            }
        }
        Set<Library> addedLibs = new HashSet<Library>(Arrays.asList(libraries));
        int[] indexes = new int[libraries.length];
        for (int i=0, j=0; i<listModel.getSize(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get (i);
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && !item.isBroken() ) {
                if (addedLibs.contains(item.getLibrary())) {
                    indexes[j++] =i;
                }
            }
        }
        return indexes;        
    }

    public static int[] addJarFiles( DefaultListModel listModel, int[] indices, File files[]) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        int[] indexes = new int[files.length];
        for( int i = 0, delta = 0; i+delta < files.length; ) {            
            int current = lastIndex + 1 + i;
            File f = files[i+delta];
            String pathInWar = (f.isDirectory() ? ClassPathSupport.Item.PATH_IN_WAR_DIR : ClassPathSupport.Item.PATH_IN_WAR_LIB);
            ClassPathSupport.Item item = ClassPathSupport.Item.create( f, null, pathInWar);
            if ( !listModel.contains( item ) ) {
                listModel.add( current, item );
                indexes[delta + i] = current;
                i++;
            }
            else {
                indexes[i + delta] = listModel.indexOf( item );
                delta++;
            }            
        }
        return indexes;

    }
    
    public static int[] addArtifacts( DefaultListModel listModel, int[] indices, AntArtifactChooser.ArtifactItem artifactItems[]) {
        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
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
                String pathInWar = (item.getType() == ClassPathSupport.Item.TYPE_JAR && item.getFile().isDirectory()) ? ClassPathSupport.Item.PATH_IN_WAR_DIR : ClassPathSupport.Item.PATH_IN_WAR_LIB;
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
    }
}
