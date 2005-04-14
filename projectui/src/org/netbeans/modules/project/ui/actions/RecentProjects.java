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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class RecentProjects extends AbstractAction implements Presenter.Menu, Presenter.Popup, PropertyChangeListener, PopupMenuListener {
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_URL_KEY = "org.netbeans.modules.project.ui.RecentProjectItem.Project_URL"; // NOI18N
    private final ProjectDirListener prjDirListener = new ProjectDirListener(); 
    
    private JMenu subMenu;
    
    private boolean recreate;
    
    public RecentProjects() {
        super( NbBundle.getMessage(RecentProjects.class, "LBL_RecentProjectsAction_Name")); // NOI18N
        OpenProjectList.getDefault().addPropertyChangeListener( this );
        recreate = true;
    }
    
        
    public boolean isEnabled() {
        return !OpenProjectList.getDefault().isRecentProjectsEmpty();
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        createMainSubMenu();
        return subMenu;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenu menu = createSubMenu();
        fillSubMenu(menu);
        return menu;
    }
    
    private JMenu createSubMenu() {
        JMenu menu = new JMenu(this);
        menu.setMnemonic(NbBundle.getMessage(RecentProjects.class, "MNE_RecentProjectsAction_Name").charAt(0));
        return menu;
    }
    
    private void createMainSubMenu() {
        if ( subMenu == null ) {
            subMenu = createSubMenu();
            subMenu.getPopupMenu().addPopupMenuListener( this );
        }
    }
        
    private void fillSubMenu(JMenu menu) {
        menu.removeAll();
        
        List projects = OpenProjectList.getDefault().getRecentProjects();
        if ( projects.isEmpty() ) {
            menu.setEnabled( false );
            return;
        }
        
        menu.setEnabled( true );
        ActionListener jmiActionListener = new MenuItemActionListener(); 
                        
        // Fill menu with items
        
        for ( Iterator it = projects.iterator(); it.hasNext(); ) {
            Project p = (Project)it.next();
            FileObject prjDir = p.getProjectDirectory();
            try { 
                URL prjDirURL = prjDir.getURL();
                if ( prjDirURL == null || prjDir == null || !prjDir.isValid()) {
                    continue;
                }
                prjDir.removeFileChangeListener(prjDirListener);            
                prjDir.addFileChangeListener(prjDirListener);
                ProjectInformation pi = ProjectUtils.getInformation(p);
                JMenuItem jmi = new JMenuItem(pi.getDisplayName(), pi.getIcon());
                menu.add( jmi );            
                jmi.putClientProperty( PROJECT_URL_KEY, prjDirURL );
                jmi.addActionListener( jmiActionListener );
            }
            catch( FileStateInvalidException ex ) {
                // Don't put the project into the menu
            }
        }
    }

    // Implementation of change listener ---------------------------------------
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( OpenProjectList.PROPERTY_RECENT_PROJECTS.equals( e.getPropertyName() ) ) {
            createMainSubMenu();
            subMenu.setEnabled( !OpenProjectList.getDefault().isRecentProjectsEmpty() );
            recreate = true;
        }
        
    }
    
    
    // Implementation of PopupMenuListener -------------------------------------
    
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if ( recreate ) {
            createMainSubMenu();
            fillSubMenu(subMenu);
            recreate = false;
        }
    }
    
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
    }

    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        public void actionPerformed( ActionEvent e ) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                
                URL url = (URL)jmi.getClientProperty( PROJECT_URL_KEY );                
                Project project = null;

                FileObject dir = URLMapper.findFileObject( url );
                if ( dir != null && dir.isFolder() ) {
                    try {
                        project = ProjectManager.getDefault().findProject( dir );
                    }       
                    catch ( IOException ioEx ) {
                        // Ignore invalid folders
                    }
                }
                
                if ( project != null ) {
                    OpenProjectList.getDefault().open( project );
                    ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                    Node root = ptLogial.getExplorerManager ().getRootContext ();
                    Node projNode = root.getChildren ().findChild ( project.getProjectDirectory().getName ());
                    try {
                        ptLogial.getExplorerManager ().setSelectedNodes (new Node[] {projNode});
                        ptLogial.open ();
                        ptLogial.requestActive ();
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
                
            }
            
        }
        
    }
    
    private class ProjectDirListener extends FileChangeAdapter {
        public void fileDeleted(FileEvent fe) {
            recreate = true;
        }
    }
    
}
