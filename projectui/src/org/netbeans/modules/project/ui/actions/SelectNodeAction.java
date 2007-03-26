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

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class SelectNodeAction extends LookupSensitiveAction implements Presenter.Menu, Presenter.Popup {
    
    // XXX Better icons
    private static final Icon SELECT_IN_PROJECTS_ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/projectTab.png" ) ); //NOI18N
    private static final Icon SELECT_IN_FILES_ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/filesTab.png" ) ); //NOI18N
    
    private static final String SELECT_IN_PROJECTS_NAME = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_Name" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_Name" ); // NOI18N
    
    private static final String SELECT_IN_PROJECTS_NAME_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_MenuName" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_MenuName" ); // NOI18N
    private static final String SELECT_IN_PROJECTS_NAME_MAIN_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_MainMenuName" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME_MAIN_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_MainMenuName" ); // NOI18N
    
    private String command;
    private ProjectActionPerformer performer;
    private String namePattern;
    
    private String findIn;
    
    public static Action inProjects() {
        SelectNodeAction a = new SelectNodeAction( SELECT_IN_PROJECTS_ICON, SELECT_IN_PROJECTS_NAME );
        a.findIn = ProjectTab.ID_LOGICAL;
        return a;
    }
    
    public static Action inFiles() {
        SelectNodeAction a = new SelectNodeAction( SELECT_IN_FILES_ICON, SELECT_IN_FILES_NAME );
        a.findIn = ProjectTab.ID_PHYSICAL;
        return a;
    }
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public SelectNodeAction( Icon icon, String name ) {
        super( icon, null, new Class[] { DataObject.class, FileObject.class } );
        this.setDisplayName( name );
    }
    
    private SelectNodeAction(String command, ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        super( icon, lookup, new Class[] { Project.class, DataObject.class, FileObject.class } );
        this.command = command;
        this.performer = performer;
        this.namePattern = namePattern;
        refresh( getLookup() );
    }
       
    protected void actionPerformed( Lookup context ) {
        
        FileObject fo = getFileFromLookup( context );
        if ( fo != null ) {
            ProjectTab pt  = ProjectTab.findDefault( findIn );      
            pt.selectNodeAsync( fo );
        }
    }
    
    protected void refresh( Lookup context ) {        
        FileObject fo = getFileFromLookup( context );
        setEnabled( fo != null );        
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    // Presenter.Menu implementation ------------------------------------------
    
    public JMenuItem getMenuPresenter () {
        if (ProjectTab.ID_LOGICAL.equals (this.findIn)) {
            return buildPresenter(SELECT_IN_PROJECTS_NAME_MAIN_MENU);
        } else {
            return buildPresenter(SELECT_IN_FILES_NAME_MAIN_MENU);
        }
    }

    // Presenter.Popup implementation ------------------------------------------
    
    public JMenuItem getPopupPresenter() {
        if (ProjectTab.ID_LOGICAL.equals (this.findIn)) {
            return buildPresenter(SELECT_IN_PROJECTS_NAME_MENU);
        } else {
            return buildPresenter(SELECT_IN_FILES_NAME_MENU);
        }
    }

    
   // Private methods ---------------------------------------------------------
    
    private FileObject getFileFromLookup( Lookup context ) {
   
        FileObject fo = (FileObject) context.lookup(FileObject.class);     
        if (fo != null) {
            return fo;
        }

        DataObject dobj = (DataObject)context.lookup( DataObject.class );
        
        return dobj == null ? null : dobj.getPrimaryFile();
    }
    
    private JMenuItem buildPresenter (String title) {
        JMenuItem menuPresenter = new JMenuItem (this);
        menuPresenter.setText (title);
        menuPresenter.setIcon(null);
        
        return menuPresenter;
    }

    
}
