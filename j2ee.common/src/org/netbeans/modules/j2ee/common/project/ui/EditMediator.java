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
package org.netbeans.modules.j2ee.common.project.ui;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.FileChooser;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 */
public final class EditMediator implements ActionListener, ListSelectionListener {

    private static String[] DEFAULT_ANT_ARTIFACT_TYPES = new String[] {
        JavaProjectConstants.ARTIFACT_TYPE_JAR, JavaProjectConstants.ARTIFACT_TYPE_FOLDER};
    
    private final ListComponent list;
    private final DefaultListModel listModel;
    private final ListSelectionModel selectionModel;
    private final ButtonModel addJar;
    private final ButtonModel addLibrary;
    private final ButtonModel addAntArtifact;
    private final ButtonModel remove;
    private final ButtonModel moveUp;
    private final ButtonModel moveDown;
    private final ButtonModel edit;
    private Document libraryPath;
    private ClassPathUiSupport.Callback callback;
    private AntProjectHelper helper;
    private ReferenceHelper refHelper;
    private Project project;
    private FileFilter filter;
    private String[] antArtifactTypes;

    private EditMediator( Project project,
                         AntProjectHelper helper,
                         ReferenceHelper refHelper,
                         ListComponent list,
                         ButtonModel addJar,
                         ButtonModel addLibrary, 
                         ButtonModel addAntArtifact,
                         ButtonModel remove, 
                         ButtonModel moveUp,
                         ButtonModel moveDown, 
                         ButtonModel edit,
                         Document libPath,
                         ClassPathUiSupport.Callback callback,
                         String[] antArtifactTypes) {

        // Remember all buttons

        this.list = list;

        if ( !( list.getModel() instanceof DefaultListModel ) ) {
            throw new IllegalArgumentException( "The list's model has to be of class DefaultListModel" ); // NOI18N
        }

        this.listModel = (DefaultListModel)list.getModel();
        this.selectionModel = list.getSelectionModel();

        this.addJar = addJar;
        this.addLibrary = addLibrary;
        this.addAntArtifact = addAntArtifact;
        this.remove = remove;
        this.moveUp = moveUp;
        this.moveDown = moveDown;
        this.edit = edit;
        this.libraryPath = libPath;
        this.callback = callback;
        this.helper = helper;
        this.refHelper = refHelper;
        this.project = project;
        this.filter = new SimpleFileFilter( 
            NbBundle.getMessage( EditMediator.class, "LBL_ZipJarFolderFilter" ), // NOI18N
            new String[] {"ZIP","JAR"} ); // NOI18N
        this.antArtifactTypes = antArtifactTypes;
    }

    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary, 
                                ButtonModel addAntArtifact,
                                ButtonModel remove, 
                                ButtonModel moveUp,
                                ButtonModel moveDown, 
                                ButtonModel edit,
                                Document libPath,
                                ClassPathUiSupport.Callback callback) {    
        register(project, helper, refHelper, list, addJar, addLibrary, 
                addAntArtifact, remove, moveUp, moveDown, edit, libPath, 
                callback, DEFAULT_ANT_ARTIFACT_TYPES);
    }
    
    public static void register(Project project,
                                AntProjectHelper helper,
                                ReferenceHelper refHelper,
                                ListComponent list,
                                ButtonModel addJar,
                                ButtonModel addLibrary, 
                                ButtonModel addAntArtifact,
                                ButtonModel remove, 
                                ButtonModel moveUp,
                                ButtonModel moveDown, 
                                ButtonModel edit,
                                Document libPath,
                                ClassPathUiSupport.Callback callback,
                                String[] antArtifactTypes) {    

        EditMediator em = new EditMediator( project, 
                                            helper,
                                            refHelper,
                                            list,
                                            addJar,
                                            addLibrary, 
                                            addAntArtifact,
                                            remove,    
                                            moveUp,
                                            moveDown,
                                            edit,
                                            libPath,
                                            callback,
                                            antArtifactTypes);

        // Register the listener on all buttons
        addJar.addActionListener( em ); 
        addLibrary.addActionListener( em );
        addAntArtifact.addActionListener( em );
        remove.addActionListener( em );
        moveUp.addActionListener( em );
        moveDown.addActionListener( em );
        edit.addActionListener(em);
        // On list selection
        em.selectionModel.addListSelectionListener( em );
        // Set the initial state of the buttons
        em.valueChanged( null );
    }

    // Implementation of ActionListener ------------------------------------

    /** Handles button events
     */        
    public void actionPerformed( ActionEvent e ) {

        Object source = e.getSource();

        if ( source == addJar ) { 
            // Let user search for the Jar file
            FileChooser chooser = new FileChooser(helper, true);
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
            chooser.setMultiSelectionEnabled( true );
            chooser.setDialogTitle( NbBundle.getMessage( EditMediator.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
            //#61789 on old macosx (jdk 1.4.1) these two method need to be called in this order.
            chooser.setAcceptAllFileFilterUsed( false );
            chooser.setFileFilter(filter);
            File curDir = UserProjectSettings.getDefault().getLastUsedClassPathFolder(); 
            chooser.setCurrentDirectory (curDir);
            int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( list.getComponent() ) ); // Show the chooser

            if ( option == JFileChooser.APPROVE_OPTION ) {

                String filePaths[];
                try {
                    filePaths = chooser.getSelectedPaths();
                } catch (IOException ex) {
                    // TODO: add localized message
                    Exceptions.printStackTrace(ex);
                    return;
                }
                // value of PATH_IN_DEPLOYMENT depends on whether file or folder is being added.
                // do not override value set by callback.initAdditionalProperties if includeNewFilesInDeployment
                int[] newSelection = ClassPathUiSupport.addJarFiles( listModel, list.getSelectedIndices(), 
                        filePaths, FileUtil.toFile(helper.getProjectDirectory()), callback);
                list.setSelectedIndices( newSelection );
                curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                UserProjectSettings.getDefault().setLastUsedClassPathFolder(curDir);
            }
        }
        else if ( source == addLibrary ) {
            //TODO this piece needs to go somewhere else?
            LibraryManager manager = null;
            boolean empty = false;
            try {
                String path = libraryPath.getText(0, libraryPath.getLength());
                if (path != null && path.length() > 0) {
                    File fil = PropertyUtils.resolveFile(FileUtil.toFile(helper.getProjectDirectory()), path);
                    URL url = FileUtil.normalizeFile(fil).toURI().toURL();
                    manager = LibraryManager.forLocation(url);
                } else {
                    empty = true;
                }
            } catch (BadLocationException ex) {
                empty = true;
                Exceptions.printStackTrace(ex);
            } catch (MalformedURLException ex2) {
                Exceptions.printStackTrace(ex2);
            }
            if (manager == null && empty) {
                manager = LibraryManager.getDefault();
            }
            if (manager == null) {
                //TODO some error message
                return;
            }

            Set<Library> added = LibraryChooser.showDialog(manager,
                    null, refHelper.getLibraryChooserImportHandler()); // XXX filter to j2se libs only?
            if (added != null) {
                Set<Library> includedLibraries = new HashSet<Library>();
               int[] newSelection = ClassPathUiSupport.addLibraries(listModel, list.getSelectedIndices(), 
                       added.toArray(new Library[added.size()]), includedLibraries, callback);
               list.setSelectedIndices( newSelection );
            }
        }
        else if ( source == edit ) { 
            ClassPathUiSupport.edit( listModel, list.getSelectedIndices(),  helper);
            if (list instanceof JListListComponent) {
                ((JListListComponent)list).list.repaint();
            } else if (list instanceof JTableListComponent) {
                ((JTableListComponent)list).table.repaint();
            } else {
                assert false : "do not know how to handle " + list.getClass().getName();
            }
        }
        else if ( source == addAntArtifact ) { 
            AntArtifactChooser.ArtifactItem artifactItems[] = AntArtifactChooser.showDialog(
                    antArtifactTypes, project, list.getComponent().getParent());
            if (artifactItems != null) {
                int[] newSelection = ClassPathUiSupport.addArtifacts( listModel, list.getSelectedIndices(), artifactItems, callback);
                list.setSelectedIndices( newSelection );
            }
        }
        else if ( source == remove ) { 
            int[] newSelection = ClassPathUiSupport.remove( listModel, list.getSelectedIndices() );
            list.setSelectedIndices( newSelection );
        }
        else if ( source == moveUp ) {
            int[] newSelection = ClassPathUiSupport.moveUp( listModel, list.getSelectedIndices() );
            list.setSelectedIndices( newSelection );
        }
        else if ( source == moveDown ) {
            int[] newSelection = ClassPathUiSupport.moveDown( listModel, list.getSelectedIndices() );
            list.setSelectedIndices( newSelection );
        }
    }    


    /** Handles changes in the selection
     */        
    public void valueChanged( ListSelectionEvent e ) {

        // remove enabled only if selection is not empty
        boolean canRemove = false;
        // and when the selection does not contain unremovable item
        if ( selectionModel.getMinSelectionIndex() != -1 ) {
            canRemove = true;
            int iMin = selectionModel.getMinSelectionIndex();
            int iMax = selectionModel.getMinSelectionIndex();
            for ( int i = iMin; i <= iMax; i++ ) {

                if ( selectionModel.isSelectedIndex( i ) ) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item)listModel.get( i );
                    if ( item.getType() == ClassPathSupport.Item.TYPE_CLASSPATH ) {
                        canRemove = false;
                        break;
                    }
                }
            }
        }

        // addJar allways enabled            
        // addLibrary allways enabled            
        // addArtifact allways enabled            
        edit.setEnabled(ClassPathUiSupport.canEdit(selectionModel, listModel));            
        remove.setEnabled( canRemove );
        moveUp.setEnabled( ClassPathUiSupport.canMoveUp( selectionModel ) );
        moveDown.setEnabled( ClassPathUiSupport.canMoveDown( selectionModel, listModel.getSize() ) );       

    }

    public interface ListComponent {
        public Component getComponent();
        public int[] getSelectedIndices();
        public void setSelectedIndices(int[] indices);
        public DefaultListModel getModel();
        public ListSelectionModel getSelectionModel();
    }

    private static final class JListListComponent implements ListComponent {
        private JList list;

        public JListListComponent(JList list) {
            this.list = list;
        }

        public Component getComponent() {
            return list;
        }

        public int[] getSelectedIndices() {
            return list.getSelectedIndices();
        }

        public void setSelectedIndices(int[] indices) {
            list.setSelectedIndices(indices);
        }

        public DefaultListModel getModel() {
            return (DefaultListModel)list.getModel();
        }

        public ListSelectionModel getSelectionModel() {
            return list.getSelectionModel();
        }
    }

    private static final class JTableListComponent implements ListComponent {
        private JTable table;
        private DefaultListModel model;

        public JTableListComponent(JTable table, DefaultListModel model) {
            this.table = table;
            this.model = model;
        }

        public Component getComponent() {
            return table;
        }

        public int[] getSelectedIndices() {
            return table.getSelectedRows();
        }

        public void setSelectedIndices(int[] indices) {
            table.clearSelection();
            for (int i = 0; i < indices.length; i++) {
                table.addRowSelectionInterval(indices[i], indices[i]);
            }
        }

        public DefaultListModel getModel() {
            return model;
        }

        public ListSelectionModel getSelectionModel() {
            return table.getSelectionModel();
        }
    }

    public static ListComponent createListComponent(JList list) {
        return new JListListComponent(list);
    }

    public static ListComponent createListComponent(JTable table, DefaultListModel model) {
        return new JTableListComponent(table, model);
    }
    
    private static class SimpleFileFilter extends FileFilter {

        private String description;
        private Collection extensions;


        public SimpleFileFilter (String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.');   //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring (index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }
    
}

