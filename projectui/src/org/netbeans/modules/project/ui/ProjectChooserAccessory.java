/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectListSettings;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  phrebejk
 */
public class ProjectChooserAccessory extends javax.swing.JPanel 
    implements ActionListener, PropertyChangeListener {
    
    /** Creates new form ProjectChooserAccessory */
    public ProjectChooserAccessory( JFileChooser chooser, boolean isOpenSubprojects, boolean isOpenAsMain ) {
        initComponents();
        
        // Workaround for strange behavior of FormEditor
        jLabelProjectName.setText( NbBundle.getMessage( ProjectChooserAccessory.class, "LBL_PrjChooser_ProjectName_Label" ) ); //NOI18N
        jCheckBoxMain.setText( NbBundle.getMessage( ProjectChooserAccessory.class, "LBL_PrjChooser_Main_CheckBox" ) ); //NOI18N
        jCheckBoxMain.setMnemonic ( NbBundle.getMessage( ProjectChooserAccessory.class, "MNM_PrjChooser_Main_CheckBox" ).charAt (0) ); //NOI18N
        jCheckBoxSubprojects.setText( NbBundle.getMessage( ProjectChooserAccessory.class, "LBL_PrjChooser_Subprojects_CheckBox" ) ); //NOI18N
        jCheckBoxSubprojects.setMnemonic( NbBundle.getMessage( ProjectChooserAccessory.class, "MNM_PrjChooser_Subprojects_CheckBox" ).charAt (0) ); //NOI18N
        
        
        
        // Listen on the subproject checkbox to change the option accordingly
        jCheckBoxSubprojects.setSelected( isOpenSubprojects );
        jCheckBoxSubprojects.addActionListener( this );
        
        // Listen on the main checkbox to change the option accordingly
        jCheckBoxMain.setSelected( isOpenAsMain );
        jCheckBoxMain.addActionListener( this );
        
        // Listen on the chooser to update the Accessory
        chooser.addPropertyChangeListener( this );
        
        // Set default list model for the subprojects list
        jListSubprojects.setModel( new DefaultListModel() );
        
        // Disable the Accessory. JFileChooser does not select a file
        // by default
        setAccessoryEnablement( false );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelProjectName = new javax.swing.JLabel();
        jTextFieldProjectName = new javax.swing.JTextField();
        jCheckBoxMain = new javax.swing.JCheckBox();
        jCheckBoxSubprojects = new javax.swing.JCheckBox();
        jScrollPaneSubprojects = new javax.swing.JScrollPane();
        jListSubprojects = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 12, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabelProjectName, gridBagConstraints);

        jTextFieldProjectName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jTextFieldProjectName, gridBagConstraints);

        jCheckBoxMain.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBoxMain, gridBagConstraints);

        jCheckBoxSubprojects.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jCheckBoxSubprojects, gridBagConstraints);

        jListSubprojects.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListSubprojects.setEnabled(false);
        jScrollPaneSubprojects.setViewportView(jListSubprojects);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPaneSubprojects, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxMain;
    private javax.swing.JCheckBox jCheckBoxSubprojects;
    private javax.swing.JLabel jLabelProjectName;
    private javax.swing.JList jListSubprojects;
    private javax.swing.JScrollPane jScrollPaneSubprojects;
    private javax.swing.JTextField jTextFieldProjectName;
    // End of variables declaration//GEN-END:variables
    
    // Implementation of action listener ---------------------------------------
    
    public void actionPerformed( ActionEvent e ) {
        if ( e.getSource() == jCheckBoxSubprojects ) {
            OpenProjectListSettings.getInstance().setOpenSubprojects( jCheckBoxSubprojects.isSelected() );
        }
        else if ( e.getSource() == jCheckBoxMain ) {
            OpenProjectListSettings.getInstance().setOpenAsMain( jCheckBoxMain.isSelected() );
        }
    }

    // Implementayion of PropertyChange listener
    
    public void propertyChange( PropertyChangeEvent e ) {
        if ( JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals( e.getPropertyName() ) ) {             
            // We have to update the Accessory
            JFileChooser chooser = (JFileChooser)e.getSource();
            File rawdir = chooser.getSelectedFile();
            File dir = rawdir != null ? FileUtil.normalizeFile(rawdir) : null;
            DefaultListModel spListModel = (DefaultListModel)jListSubprojects.getModel();
            
            Project project = null;            
            if ( isProjectDir( dir ) ) {
                project = getProject( dir );
                // may still be null
            }
            
            if (project != null) {
                // Enable all components acessory
                setAccessoryEnablement( true );
                
                jTextFieldProjectName.setText(ProjectUtils.getInformation(project).getDisplayName());
                
                spListModel.clear();                
                
                boolean hasSubprojects = fillSubProjectsModel( project, spListModel );
                
                // If no soubprojects checkbox should be disabled
                jCheckBoxSubprojects.setEnabled( hasSubprojects );
                
            }
            else {            
                // Clear the accessory data if the dir is not project dir
                jTextFieldProjectName.setText( "" ); // NOI18N
                spListModel.clear();
                
                // Disable all components in accessory
                setAccessoryEnablement( false );
            }
                        
        }
        else if ( JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals( e.getPropertyName() ) ) {
            // Selection lost => disable accessory
            setAccessoryEnablement( false );
        }
    }    
    
    
    // Private methods ---------------------------------------------------------
    
    private static boolean isProjectDir( File dir ) {
        
        if ( dir == null ) {
            return false;
        }
        
        // Wondering why we are doing this? Surprise, surprise on windows
        // some files comming from the JFileChooser have a registry ID as
        // parent even if they are roots of file systems. That's bad because
        // it will cause the floppy be checked which is very slow. So, sorry
        // this seemed to be the easiest and most effective fix
        File testFile = new File( dir.getPath() );
        if ( testFile == null || testFile.getParent() == null ) {
            // BTW this means that roots of file systems can't be project
            // directories.
            return false;
        }
        
        if ( !dir.isDirectory() ) {
            return false;
        }
        
        FileObject fo = FileUtil.toFileObject(dir);
        return fo == null ? false : ProjectManager.getDefault().isProject( fo );
    }
    
    private static Project getProject( File dir ) {
        return OpenProjectList.fileToProject( dir );
    }
    
    private void setAccessoryEnablement( boolean enable ) {
        jLabelProjectName.setEnabled( enable );
        jTextFieldProjectName.setEnabled( enable );
        jCheckBoxMain.setEnabled( enable );
        jCheckBoxSubprojects.setEnabled( enable );
        jScrollPaneSubprojects.setEnabled( enable );
    }
    
    private boolean fillSubProjectsModel( Project project, DefaultListModel model ) {
        
        ArrayList subprojects = new ArrayList( 5 );
        addSubprojects( project, subprojects ); // Find the projects recursively
        
        if ( subprojects.isEmpty() ) {
            return false; // No subprojects => no fun
        }
        
        String pattern = NbBundle.getMessage( ProjectChooserAccessory.class, "LBL_PrjChooser_SubprojectName_Format" ); // NOI18N
        File pDir = FileUtil.toFile( project.getProjectDirectory() );
                
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            Project p = (Project)subprojects.get( i );
           
            // Try to compute relative path            
            FileObject spDir = p.getProjectDirectory();
            // XXX this does not work:
            String relPath = relativizePath( pDir, FileUtil.toFile( spDir ), 3 );
                        
            if ( relPath == null ) { // Can't realtivize                
                // XXX this should be FileUtil.toFile and getAbsolutePath:
                relPath = spDir.getPath();
            }
            String displayName = MessageFormat.format( 
                pattern, 
                new Object[] { ProjectUtils.getInformation(p).getDisplayName(), relPath } );
            subprojects.set( i, displayName );
        }
        
        // Sort the list
        Collections.sort( subprojects, Collator.getInstance() );
        
        // Put all the strings into the list model
        for( Iterator it = subprojects.iterator(); it.hasNext(); ) {
            model.addElement( it.next() );
        }
                
        return true;
    }
    
    /** Gets all subprojects recursively
     */
    private void addSubprojects( Project p, List result ) {
        
        SubprojectProvider spp = (SubprojectProvider)p.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator/*<Project>*/ it = spp.getSubProjects().iterator(); it.hasNext(); ) {
            Project sp = (Project)it.next(); 
            if ( !result.contains( sp ) ) {
                result.add( sp );
            }
            addSubprojects( sp, result );            
        }
        
    }
    
    private static String relativizePath( File f1, File f2, int maxDif ) {

        /*
        String relPath = FileUtil.getRelativePath( pDir, spDir );
        
        if ( relPath != null ) {
            return relPath;
        }
        */
                        
        List p1 = filePath( f1 );
        List p2 = filePath( f2 );
        
        int maxLen = Math.min( p1.size(), p2.size() );
        
        int i = 0;
        
        while( i < maxLen ) {
            if ( !p1.get(i).equals( p2.get(i) ) ) {
                break;
            }
            i++;
        }
        
        if ( i == 0 ) {
            return null; // Completely different
        }
        if ( i == p1.size() ) { // P2 under p1
            
        }
    
        
        return null;
    }
    
    
    
    private static List filePath( File f ) {
        ArrayList result = new ArrayList();
        
        do {
            result.add( 0, f );
            f = f.getParentFile(); 
        } 
        while( f != null ); 
        
        return result;
    }
    
    // Other methods -----------------------------------------------------------
    
    /** Factory method for project chooser
     */    
    public static JFileChooser createProjectChooser( boolean defaultAccessory ) {
        
        OpenProjectListSettings opls = OpenProjectListSettings.getInstance();
        JFileChooser chooser = new ProjectFileChooser();
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );        
        chooser.setApproveButtonText( NbBundle.getMessage( ProjectChooserAccessory.class, "BTN_PrjChooser_ApproveButtonText" ) ); // NOI18N
        chooser.setApproveButtonMnemonic( NbBundle.getMessage( ProjectChooserAccessory.class, "MNM_PrjChooser_ApproveButtonText" ).charAt (0) ); // NOI18N
        chooser.setMultiSelectionEnabled( false );        
        chooser.setDialogTitle( NbBundle.getMessage( ProjectChooserAccessory.class, "LBL_PrjChooser_Title" ) ); // NOI18N
        chooser.setFileFilter( ProjectDirFilter.INSTANCE );        
        chooser.setAcceptAllFileFilterUsed( false );
        if ( defaultAccessory ) {
            chooser.setAccessory( new ProjectChooserAccessory( chooser, opls.isOpenSubprojects(), opls.isOpenAsMain() ) );
        }
        chooser.setFileView( new ProjectFileView( chooser.getFileSystemView() ) );                
        
        String dir = opls.getLastOpenProjectDir();
        if ( dir != null ) {
            File d = new File( dir );
            if ( d.exists() && d.isDirectory() ) {
                chooser.setCurrentDirectory( d );
            }
        }
        
        return chooser;    
        
    }
    
    
    // Aditional innerclasses for the file chooser -----------------------------
    
    private static class ProjectFileChooser extends JFileChooser {
        
        public void approveSelection() {
            File dir = FileUtil.normalizeFile(getSelectedFile());
            
            if ( isProjectDir( dir ) && getProject( dir ) != null ) {
                super.approveSelection();
            }
            else {
                setCurrentDirectory( dir );
            }
            
        }
        
        
    }
    
    private static class ProjectDirFilter extends FileFilter {
        
        private static final FileFilter INSTANCE = new ProjectDirFilter( );
                       
        public boolean accept( File f ) {
            
            if ( f.isDirectory() ) {                
                return true; // Directory selected
            }
            
            return false;
        }        
        
        public String getDescription() {
            return NbBundle.getMessage( ProjectDirFilter.class, "LBL_PrjChooser_ProjectDirectoryFilter_Name" ); // NOI18N
        }        
        
    }
    
    private static class ProjectFileView extends FileView {
        
        private static final Icon BADGE = new ImageIcon(Utilities.loadImage("org/netbeans/modules/project/ui/resources/projectBadge.gif")); // NOI18N
        private static final Icon EMPTY = new ImageIcon(Utilities.loadImage("org/netbeans/modules/project/ui/resources/empty.gif")); // NOI18N
        
        private FileSystemView fsv;
        private Icon lastOriginal;
        private Icon lastMerged;
        
        public ProjectFileView( FileSystemView fsv ) {
            this.fsv = fsv;            
        }
                
        public Icon getIcon(File _f) {
            File f = FileUtil.normalizeFile(_f);
            Icon original = fsv.getSystemIcon(f);
            if (original == null) {
                // L&F (e.g. GTK) did not specify any icon.
                original = EMPTY;
            }
            if ( isProjectDir( f ) ) {
                if ( original.equals( lastOriginal ) ) {
                    return lastMerged;
                }
                lastOriginal = original;
                lastMerged = new MergedIcon(original, BADGE, -1, -1);                
                return lastMerged;
            }
            else {
                return original;
            }
        }
        
        // private static File evilNetFolder = new File( "/net" ); // NOI18N
        
        private static boolean isRoot( File f ) {
           
            return f.getParent() == null;
            
            /*
            if ( evilNetFolder.equals( f ) ) {
                return true; // Autoumount on Unixes would make it slow
            }
             */
            /*
            File[] roots = File.listRoots();
            for( int i = 0; i < roots.length; i++ ) {
                if ( roots[i].equals( f ) ) {
                    System.out.println(f + " is root" );
                    return true;
                }
            }
            System.out.println(f + " is not root");
            return false;
            */
        }
                
    }
    
    private static class MergedIcon implements Icon {
        
        private Icon icon1;
        private Icon icon2;
        private int xMerge;
        private int yMerge;
        
        MergedIcon( Icon icon1, Icon icon2, int xMerge, int yMerge ) {
            
            this.icon1 = icon1;
            this.icon2 = icon2;
            
            if ( xMerge == -1 ) {
                xMerge = icon1.getIconWidth() - icon2.getIconWidth();
            }
            
            if ( yMerge == -1 ) {
                yMerge = icon1.getIconHeight() - icon2.getIconHeight();
            }
            
            this.xMerge = xMerge;
            this.yMerge = yMerge;
        }
        
        public int getIconHeight() {
            return Math.max( icon1.getIconHeight(), yMerge + icon2.getIconHeight() );
        }
        
        public int getIconWidth() {
            return Math.max( icon1.getIconWidth(), yMerge + icon2.getIconWidth() );
        }
        
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            icon1.paintIcon( c, g, x, y );
            icon2.paintIcon( c, g, x + xMerge, y + yMerge );
        }
        
    }
    
}
