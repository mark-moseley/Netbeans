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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;

import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.netbeans.modules.web.project.ui.customizer.WarIncludesUiSupport.ClasspathTableModel;

/** Classes containing code speciic for handling UI of J2SE project classpath 
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class WarIncludesUi {
    
    private WarIncludesUi() {}
           
    // Innerclasses ------------------------------------------------------------

    public static class EditMediator implements ActionListener, ListSelectionListener, TableModelListener {
                
        private final Project project;
        private final JTable list;
        private final ClasspathTableModel listModel;
        private final ListSelectionModel selectionModel;
        private final ButtonModel addJar;
        private final ButtonModel addLibrary;
        private final ButtonModel addAntArtifact;
        private final ButtonModel remove;
                    
        public EditMediator( Project project,
                             JTable list,
                             ButtonModel addJar,
                             ButtonModel addLibrary, 
                             ButtonModel addAntArtifact,
                             ButtonModel remove) {
                             
            this.list = list;
            
            if ( !( list.getModel() instanceof ClasspathTableModel ) ) {
                throw new IllegalArgumentException( "The list's model has to be of class DefaultListModel" ); // NOI18N
            }
            
            this.listModel = (ClasspathTableModel) list.getModel();
            this.selectionModel = list.getSelectionModel();
            
            this.addJar = addJar;
            this.addLibrary = addLibrary;
            this.addAntArtifact = addAntArtifact;
            this.remove = remove;

            this.project = project;
        }

        public static void register(Project project,
                                    JTable list,
                                    ButtonModel addJar,
                                    ButtonModel addLibrary, 
                                    ButtonModel addAntArtifact,
                                    ButtonModel remove) {
            
            EditMediator em = new EditMediator(project, list, addJar, addLibrary, addAntArtifact, remove);
                        
            // Register the listener on all buttons
            addJar.addActionListener( em ); 
            addLibrary.addActionListener( em );
            addAntArtifact.addActionListener( em );
            remove.addActionListener( em );
            // On list selection
            em.selectionModel.addListSelectionListener( em );
            em.listModel.addTableModelListener(em);
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
                JFileChooser chooser = new JFileChooser();
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( WebClassPathUi.class, "LBL_AddJar_DialogTitle" ) ); // NOI18N
                chooser.setFileFilter( new SimpleFileFilter( 
                    NbBundle.getMessage( WebClassPathUi.class, "LBL_ZipJarFolderFilter" ),                  // NOI18N
                    new String[] {"ZIP","JAR"} ) );                                                                 // NOI18N 
                chooser.setAcceptAllFileFilterUsed( false );
                File curDir = FoldersListSettings.getDefault().getLastUsedClassPathFolder(); 
                chooser.setCurrentDirectory (curDir);
                int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( list ) ); // Show the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    File files[] = chooser.getSelectedFiles();
                    WarIncludesUiSupport.addJarFiles(files, listModel);
                    curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                    FoldersListSettings.getDefault().setLastUsedClassPathFolder(curDir);
                }
            }
            else if ( source == addLibrary ) {
                Set/*<Library>*/includedLibraries = new HashSet ();
                Iterator it = WarIncludesUiSupport.getIterator(listModel);
                while (it.hasNext()) {
                    ClassPathSupport.Item item = (ClassPathSupport.Item) it.next();
                    if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                        includedLibraries.add( item.getLibrary() );
                    }
                }
                Object[] options = new Object[] {
                    new JButton (NbBundle.getMessage (WebClassPathUi.class,"LBL_AddLibrary")),
                    DialogDescriptor.CANCEL_OPTION
                };
                ((JButton)options[0]).setEnabled(false);
                ((JButton)options[0]).getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (WebClassPathUi.class,"AD_AddLibrary"));
                LibrariesChooser panel = new LibrariesChooser ((JButton)options[0], includedLibraries);
                DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( WebClassPathUi.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                dlg.setVisible(true);
                if (desc.getValue() == options[0]) {
                   WarIncludesUiSupport.addLibraries(panel.getSelectedLibraries(), includedLibraries, list);
                }
                dlg.dispose();
            }
            else if ( source == addAntArtifact ) { 
                AntArtifactChooser.ArtifactItem artifactItems[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, project, list.getParent());
                if (artifactItems != null) {
                    WarIncludesUiSupport.addArtifacts(artifactItems, listModel);
                }
            }
            else if ( source == remove ) { 
                WarIncludesUiSupport.remove( list);
            }
        }    
        
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            DefaultListSelectionModel sm = (DefaultListSelectionModel) list.getSelectionModel();
            int index = sm.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean canRemove = index != -1;
            // and when the selection does not contain unremovable item
            if (canRemove) {
                ClassPathSupport.Item vcpi = (ClassPathSupport.Item) listModel.getValueAt(index, 0);
                if (!vcpi.canDelete())
                    canRemove = false;
            }
                        
            remove.setEnabled(canRemove);

        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == 1) {
                ClassPathSupport.Item cpItem = (ClassPathSupport.Item) listModel.getValueAt(e.getFirstRow(), 0);
                String newPathInWar = (String) listModel.getValueAt(e.getFirstRow(), 1);
                String message = null;
//                if (userInitiatedChange && cpItem.getType() == ClassPathSupport.Item.TYPE_JAR && newPathInWar.startsWith("WEB-INF")) { //NOI18N
                if (cpItem.getType() == ClassPathSupport.Item.TYPE_JAR && newPathInWar.startsWith("WEB-INF")) { //NOI18N
                    if (newPathInWar.equals("WEB-INF\\lib") || newPathInWar.equals("WEB-INF/lib")) { //NOI18N
                        if (((File) cpItem.getObject()).isDirectory()) {
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FOLDER_IN_WEBINF_LIB", newPathInWar); // NOI18N
                        } else {
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FILE_IN_WEBINF_LIB", newPathInWar); // NOI18N
                        }
                    } else if (newPathInWar.equals("WEB-INF\\classes") || newPathInWar.equals("WEB-INF/classes")) { //NOI18N
                            message = NbBundle.getMessage(WarIncludesUi.class,
                                "MSG_NO_FOLDER_IN_WEBINF_CLASSES", newPathInWar); // NOI18N
                    }
                }
                if (message != null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE));
                }
                cpItem.setPathInWAR((String) listModel.getValueAt(e.getFirstRow(), 1));
            }
        }
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

    static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        
        private static String RESOURCE_ICON_JAR = "org/netbeans/modules/web/project/ui/resources/jar.gif"; //NOI18N
        private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/web/project/ui/resources/libraries.gif"; //NOI18N
        private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/web/project/ui/resources/projectDependencies.gif"; //NOI18N
        private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/web/project/ui/resources/referencedClasspath.gif"; //NOI18N
        private static String RESOURCE_ICON_BROKEN_BADGE = "org/netbeans/modules/web/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
        
        private static ImageIcon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
        private static ImageIcon ICON_FOLDER = null; 
        private static ImageIcon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
        private static ImageIcon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
        private static ImageIcon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );
        private static ImageIcon ICON_BROKEN_BADGE  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_BROKEN_BADGE ) );
        
        private static ImageIcon ICON_BROKEN_JAR;
        private static ImageIcon ICON_BROKEN_LIBRARY;
        private static ImageIcon ICON_BROKEN_ARTIFACT;

        // Contains well known paths in the WebProject
        private static final Map WELL_KNOWN_PATHS_NAMES = new HashMap();
        static {
            WELL_KNOWN_PATHS_NAMES.put( WebProjectProperties.JAVAC_CLASSPATH, NbBundle.getMessage( WebProjectProperties.class, "LBL_JavacClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( WebProjectProperties.JAVAC_TEST_CLASSPATH, NbBundle.getMessage( WebProjectProperties.class,"LBL_JavacTestClasspath_DisplayName") );
            WELL_KNOWN_PATHS_NAMES.put( WebProjectProperties.RUN_TEST_CLASSPATH, NbBundle.getMessage( WebProjectProperties.class, "LBL_RunTestClasspath_DisplayName" ) );
            WELL_KNOWN_PATHS_NAMES.put( WebProjectProperties.BUILD_CLASSES_DIR, NbBundle.getMessage( WebProjectProperties.class, "LBL_BuildClassesDir_DisplayName" ) );            
            WELL_KNOWN_PATHS_NAMES.put( WebProjectProperties.BUILD_TEST_CLASSES_DIR, NbBundle.getMessage (WebProjectProperties.class,"LBL_BuildTestClassesDir_DisplayName") );
        };

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String s = null;
            if (value instanceof ClassPathSupport.Item) {
                final ClassPathSupport.Item item = (ClassPathSupport.Item) value;
                setIcon(getIcon(item));
                setToolTipText(getToolTipText(item));
                s = getDisplayName(item);
            }
            return super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
        
        private String getDisplayName( ClassPathSupport.Item item ) {
            switch ( item.getType() ) {

                case ClassPathSupport.Item.TYPE_LIBRARY:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WebClassPathUi.class, "LBL_MISSING_LIBRARY", getLibraryName( item ) );
                    }
                    else { 
                        return item.getLibrary().getDisplayName();
                    }
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    String name = (String)WELL_KNOWN_PATHS_NAMES.get( WebProjectProperties.getAntPropertyName( item.getReference() ) );
                    return name == null ? item.getReference() : name;
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WebClassPathUi.class, "LBL_MISSING_PROJECT", getProjectName( item ) );
                    }
                    else {
                        return item.getArtifactURI().toString();
                    }
                case ClassPathSupport.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        return NbBundle.getMessage( WebClassPathUi.class, "LBL_MISSING_FILE", getFileRefName( item ) );
                    }
                    else {
                        return item.getFile().getPath();
                    }
            }

            return item.getReference(); // XXX            
        }

        static Icon getIcon( ClassPathSupport.Item item ) {
            
            switch ( item.getType() ) {
                
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_LIBRARY == null ) {
                            ICON_BROKEN_LIBRARY = new ImageIcon( Utilities.mergeImages( ICON_LIBRARY.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_LIBRARY;
                    }
                    else {
                        return ICON_LIBRARY;
                    }
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_ARTIFACT == null ) {
                            ICON_BROKEN_ARTIFACT = new ImageIcon( Utilities.mergeImages( ICON_ARTIFACT.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_ARTIFACT;
                    }
                    else {
                        return ICON_ARTIFACT;
                    }
                case ClassPathSupport.Item.TYPE_JAR:
                    if ( item.isBroken() ) {
                        if ( ICON_BROKEN_JAR == null ) {
                            ICON_BROKEN_JAR = new ImageIcon( Utilities.mergeImages( ICON_JAR.getImage(), ICON_BROKEN_BADGE.getImage(), 7, 7 ) );
                        }
                        return ICON_BROKEN_JAR;
                    }
                    else {
                        File file = item.getFile();
                        return file.isDirectory() ? getFolderIcon() : ICON_JAR;
                    }
                case ClassPathSupport.Item.TYPE_CLASSPATH:
                    return ICON_CLASSPATH;
                
            }
            
            return null; // XXX 
        }
        
        private String getToolTipText( ClassPathSupport.Item item ) {
//            if ( item.isBroken() && 
//                 ( item.getType() == ClassPathSupport.Item.TYPE_JAR || 
//                   item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT )  ) {
//                return evaluator.evaluate( item.getReference() );
//            }
            
            return getDisplayName( item ); // XXX
        }
        
        private static ImageIcon getFolderIcon() {
        
            if ( ICON_FOLDER == null ) {
                FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
                DataFolder dataFolder = DataFolder.findFolder( root );
                ICON_FOLDER = new ImageIcon( dataFolder.getNodeDelegate().getIcon( BeanInfo.ICON_COLOR_16x16 ) );            
            }

            return ICON_FOLDER;   
        }

        private String getProjectName( ClassPathSupport.Item item ) {
            String ID = item.getReference();
            // something in the form of "${reference.project-name.id}"
            return ID.substring(12, ID.indexOf(".", 12)); // NOI18N
        }

        private String getLibraryName( ClassPathSupport.Item item ) {
            String ID = item.getReference();
            // something in the form of "${libs.junit.classpath}"
            return ID.substring(7, ID.indexOf(".classpath")); // NOI18N
        }

        private String getFileRefName( ClassPathSupport.Item item ) {
            String ID = item.getReference();        
            // something in the form of "${file.reference.smth.jar}"
            return ID.substring(17, ID.length()-1);
        }
    }
}
