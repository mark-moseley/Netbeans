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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Hrebejk
 */
public final class JavaTargetChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private final SpecificationVersion JDK_14 = new SpecificationVersion ("1.4");   //NOI18N
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private JavaTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    private boolean isPackage;
    
    public JavaTargetChooserPanel( Project project, SourceGroup folders[], WizardDescriptor.Panel bottomPanel, boolean isPackage ) {
        this.project = project;
        this.folders = folders;
        this.bottomPanel = bottomPanel;
        this.isPackage = isPackage;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new JavaTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), isPackage );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {              
        if (gui == null || gui.getTargetName() == null) {
           setErrorMessage( null );
           return false;
        }        
        if ( isPackage ) {
            if ( !isValidPackageName( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
        }
        else {
            if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" );
                return false;
            }
            else if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }            
        }
        
        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        boolean returnValue=true;
        FileObject rootFolder = gui.getRootFolder();
        String errorMessage = canUseFileName (rootFolder, gui.getPackageFileName(), gui.getTargetName(), template.getExt ());        
        if (gui.isShowing ()) {
            setLocalizedErrorMessage (errorMessage);
        }
        if (errorMessage!=null) returnValue=false;
        
        // this enables to display error messages from the bottom panel
        // Nevertheless, the previous error messages have bigger priorities 
        if (returnValue && bottomPanel != null) {
           if (!bottomPanel.isValid())
               return false;
        }
        //Only warning, display it only if everything else id OK.
        String sl = SourceLevelQuery.getSourceLevel(rootFolder);   
        if (!isPackage && returnValue && gui.getPackageName().length() == 0 && sl != null && JDK_14.compareTo(new SpecificationVersion(sl))<=0) {                
                setErrorMessage( "ERR_JavaTargetChooser_DefaultPackage" );            
        }
        return returnValue;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
        
        wizard = (WizardDescriptor)settings;
        
        if ( gui != null ) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder( wizard );            
            // Init values
            gui.initValues( Templates.getTemplate( wizard ), preselectedFolder );
        }
        
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
        }        
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
        }
    }

    public void storeSettings(Object settings) { 
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            Templates.setTargetFolder( (WizardDescriptor)settings, getTargetFolderFromGUI () );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    // Private methods ---------------------------------------------------------
    
    private void setErrorMessage( String key ) {
        if ( key == null ) {
            setLocalizedErrorMessage ( "" ); // NOI18N
        }
        else {
            setLocalizedErrorMessage ( NbBundle.getMessage( JavaTargetChooserPanelGUI.class, key) ); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage (String message) {
        wizard.putProperty ("WizardPanel_errorMessage", message); // NOI18N
    }
    
    private FileObject getTargetFolderFromGUI () {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        FileObject folder = null;
        if ( !isPackage ) {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject( packageFileName );
            if ( folder == null ) {
                try {
                    folder = FileUtil.createFolder( rootFolder, packageFileName );
                }
                catch( IOException e ) {
                    ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
                }
            }
        }
        else {
            folder = rootFolder;
        }
        return folder;
    }
    
    // Nice copy of useful methods (Taken from JavaModule)
    
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    static boolean isValidTypeIdentifier(String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier( ident ) ) {
            return false;
        }
        else {
            return true;
        }
    }
    
    // helper methods copied from project/ui/ProjectUtilities
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    final public static String canUseFileName (FileObject targetFolder, String folderName, String newObjectName, String extension) {
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (newObjectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        
        String relFileName = folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!targetFolder.canWrite ()) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        if (targetFolder.getFileObject (relFileName) != null) {
            return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }
        
        FileObject existingFolder = folderName == null ? targetFolder : targetFolder.getFileObject( folderName );
        if (Utilities.isWindows () &&  existingFolder != null ) {
            if (checkCaseInsensitiveName (existingFolder, newObjectName)) {
                return NbBundle.getMessage (JavaTargetChooserPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
            }
        }

        // all ok
        return null;
    }
    
    // helper check for windows, its filesystem is case insensitive (workaround the bug #33612)
    /** Check existence of file on case insensitive filesystem.
     * Returns true if folder contains file with given name and extension.
     * @param folder folder for search
     * @param name name of file
     * @param extension extension of file
     * @return true if file with name and extension exists, false otherwise.
     */    
    private static boolean checkCaseInsensitiveName (FileObject folder, String name) {
        // bugfix #41277, check only direct children
        Enumeration children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = (FileObject) children.nextElement ();
            if (name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }

}
