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
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

public class SetMainProject extends ProjectAction implements Presenter.Menu, PropertyChangeListener {
    
    private static final String namePattern = NbBundle.getMessage( SetMainProject.class, "LBL_SetAsMainProjectAction_Name" ); // NOI18N
        
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.project.ui.MainProjectItem"; // NOI18N
    
    protected JMenu subMenu;
    
    // private PropertyChangeListener wpcl;
    
    public SetMainProject() {
        this( null );
    }
    
    public SetMainProject( Lookup context ) {
        super( (String)null, namePattern, null, context );
        // wpcl = WeakListeners.propertyChange( this, OpenProjectList.getDefault() );
        // OpenProjectList.getDefault().addPropertyChangeListener( wpcl );
        if ( context == null ) { 
            OpenProjectList.getDefault().addPropertyChangeListener( this );
        }
        refresh( getLookup() );
    }
    
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        
        if ( projects != null && projects.length > 0 ) 
        OpenProjectList.getDefault().setMainProject( projects[0] );
        
    }
    
    public void refresh( Lookup context ) {
        
        super.refresh( context );
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length != 1 /* Some projects have to be open !OpenProjectList.getDefault().isOpen( projects[0] ) */ ) {
            setEnabled( false );
        }
        else {
            setEnabled( true );
        }        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new SetMainProject( actionContext );
    }
    
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    private void createSubMenu() {
        
        Project projects[] = OpenProjectList.getDefault().getOpenProjects();
        
        Arrays.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        
        // Enable disable the action according to number of open projects
        if ( projects == null || projects.length == 0 ) {
            setEnabled( false );
        }
        else {
            setEnabled( true );
        }
        
        if ( subMenu == null ) {
            String label = NbBundle.getMessage( SetMainProject.class, "LBL_SetMainProjectAction_Name" ); // NOI18N
            subMenu = new JMenu( label );
            subMenu.setMnemonic (NbBundle.getMessage (SetMainProject.class, "MNE_SetMainProjectAction_Name").charAt (0)); // NOI18N
        }
        
        subMenu.removeAll();
        ActionListener jmiActionListener = new MenuItemActionListener(); 
        
        
        // Fill menu with items
        for ( int i = 0; i < projects.length; i++ ) {
            ProjectInformation pi = ProjectUtils.getInformation(projects[i]);
            JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(pi.getDisplayName(), pi.getIcon(), false);
            subMenu.add( jmi );
            jmi.putClientProperty( PROJECT_KEY, projects[i] );
            jmi.addActionListener( jmiActionListener );
        }

        // Set main project
        selectMainProject();
        
        subMenu.setEnabled( projects.length > 0 );
        
    }
    
    private void selectMainProject() {
        
        for( int i = 0; i < subMenu.getItemCount(); i++ ) {
            JMenuItem jmi = subMenu.getItem( i );
            Project project = (Project)jmi.getClientProperty( PROJECT_KEY );
            
            if ( jmi instanceof JRadioButtonMenuItem ) {
                if ( OpenProjectList.getDefault().isMainProject( project ) ) {
                    ((JRadioButtonMenuItem)jmi).setSelected( true );                    
                }   
                else {
                    ((JRadioButtonMenuItem)jmi).setSelected( false );                    
                }
            }    
        }
        
    }
    
    // Implementation of change listener ---------------------------------------
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
            createSubMenu();
        }
        else if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) && subMenu != null ) {
            selectMainProject();
        }
        
        
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        public void actionPerformed( ActionEvent e ) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                Project project = (Project)jmi.getClientProperty( PROJECT_KEY );
                if ( project != null ) {
                    OpenProjectList.getDefault().setMainProject( project );
                }
                
            }
            
        }
        
    }
    
    /**
     * Variant which behaves just like the menu presenter without a context, but
     * can be displayed in a context menu.
     */
    public static final class PopupWithoutContext extends SetMainProject implements Presenter.Popup {
        
        public PopupWithoutContext() {}
        
        private PopupWithoutContext(Lookup actionContext) {
            super(actionContext);
        }
        
        public JMenuItem getPopupPresenter() {
            // Hack!
            subMenu = null;
            return getMenuPresenter();
        }
        
        public Action createContextAwareInstance(Lookup actionContext) {
            return new PopupWithoutContext(actionContext);
        }

    }
    
}
