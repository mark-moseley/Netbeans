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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter.Popup;


/** Action for invoking the project sensitive NewFile Wizard
 */
public class NewFile extends ProjectAction implements PropertyChangeListener, Popup {

    private static final Icon ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/newFile.gif" ) ); //NOI18N        
    private static final String NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_Name" ); // NI18N
    private static final String POPUP_NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_PopupName" ); // NOI18N
    private static final String FILE_POPUP_NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_File_PopupName" ); // NOI18N
    private static final String TEMPLATE_NAME_FORMAT = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_Template_PopupName" ); // NOI18N
    
    public NewFile() {
        this( null );
    }
    
    public NewFile( Lookup context ) {
        super( (String)null, NAME, ICON, context ); //NOI18N    
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newFile.gif"); //NOI18N
        OpenProjectList.getDefault().addPropertyChangeListener( this );
        refresh( getLookup() );
    }

    protected void refresh( Lookup context ) {
        setEnabled( OpenProjectList.getDefault().getOpenProjects().length > 0 );
        setDisplayName( NAME );
    }

    //private NewFileWizard wizardIterator;  

    protected void actionPerformed( Lookup context ) {
        doPerform( context, null );
    }    
        
    private void doPerform( Lookup context, DataObject template ) {
        
        if ( context == null ) {
            context = getLookup();
        }
    
        NewFileWizard wd = new NewFileWizard( preselectedProject( context ) /* , null */ );

        DataFolder preselectedFolder = preselectedFolder( context );
        if ( preselectedFolder != null ) {
            wd.setTargetFolder( preselectedFolder );
        }

        try { 
            Set resultSet = template == null ? wd.instantiate () : wd.instantiate( template );
            
            if (resultSet == null) {
                // no new object, no work
                return ;
            }
            
            Iterator it = resultSet.iterator ();
            
            while (it.hasNext ()) {
                Object obj = it.next ();
                DataObject newDO = null;
                if (obj instanceof DataObject) {
                    newDO = (DataObject) obj;
                } else if (obj instanceof FileObject) {
                    try {
                        newDO = DataObject.find ((FileObject) obj);
                    } catch (DataObjectNotFoundException x) {
                        // XXX
                        assert false : obj;
                    }
                } else {
                    assert false : obj;
                }
                if (newDO != null) {
                    ProjectUtilities.openAndSelectNewObject (newDO);
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }
        
        // Update the Templates LRU for given project
        Project project = Templates.getProject( wd );
        FileObject foTemplate = Templates.getTemplate( wd );
        OpenProjectList.getDefault().updateTemplatesLRU( project, foTemplate );

    }
    
    // Context Aware action implementation -------------------------------------
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new NewFile( actionContext );
    }
    
    // Presenter.Popup implementation ------------------------------------------
    
    public JMenuItem getPopupPresenter() {
        Project projects[] = ActionsUtil.getProjectsFromLookup( getLookup(), null );
        if ( projects != null && projects.length > 0 ) {
            return createSubmenu( projects[0] );
        }
        // cannot return null 
        return new JMenuItem ();
    }

    private Project preselectedProject( Lookup context ) {
        Project preselectedProject = null;

        // if ( activatedNodes != null && activatedNodes.length != 0 ) {

        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length > 0 ) {
            preselectedProject = projects[0];
        }

        
        if ( preselectedProject == null ) {
            // No project context => use main project
            preselectedProject = OpenProjectList.getDefault().getMainProject();
            if ( preselectedProject == null ) {
                // No main project => use the first one
                preselectedProject = OpenProjectList.getDefault().getOpenProjects()[0];
            }
        }

        if ( preselectedProject == null ) {
            assert false : "Action should be disabled"; // NOI18N
        }

        return preselectedProject;    
    }

    private DataFolder preselectedFolder( Lookup context ) {
        
        DataFolder preselectedFolder = null;
        
        // Try to find selected folder
        preselectedFolder = (DataFolder)context.lookup( DataFolder.class );
        if ( preselectedFolder == null ) {
            // No folder selectd try with DataObject
            DataObject dobj = (DataObject)context.lookup( DataObject.class );
            if ( dobj != null) {
                // DataObject found => we'll use the parent folder
                preselectedFolder = dobj.getFolder();
            }
        }
        
        return preselectedFolder;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh( Lookup.EMPTY );
    }
    
    public static String TEMPLATE_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.Template"; // NOI18N
    
    JMenuItem createSubmenu( Project project ) {
        JMenu menuItem = new JMenu( POPUP_NAME );
        
        ActionListener menuListener = new PopupMenuListener();
        
        JMenuItem fileItem = new JMenuItem( FILE_POPUP_NAME, (Icon)getValue( Action.SMALL_ICON ) );
        fileItem.addActionListener( menuListener );
        fileItem.putClientProperty( TEMPLATE_PROPERTY, null );
        menuItem.add( fileItem );
        menuItem.add( new Separator() );
        
        List lruList = OpenProjectList.getDefault().getTemplatesLRU( project );
        for( Iterator it = lruList.iterator(); it.hasNext(); ) {
            DataObject template = (DataObject)it.next();
            
            Node delegate = template.getNodeDelegate();
            JMenuItem item = new JMenuItem( 
                MessageFormat.format( TEMPLATE_NAME_FORMAT, new Object[] { delegate.getDisplayName() } ),
                new ImageIcon( delegate.getIcon( BeanInfo.ICON_COLOR_16x16 ) ) );
            item.putClientProperty( TEMPLATE_PROPERTY, template );
            item.addActionListener( menuListener );
            menuItem.add( item );
        }
        
        return menuItem;
    }
    
    
    private class PopupMenuListener implements ActionListener {
                
        public void actionPerformed( ActionEvent e ) {
            JMenuItem source = (JMenuItem)e.getSource();
            DataObject template = (DataObject)source.getClientProperty( TEMPLATE_PROPERTY );
                        
            doPerform( null, template );            
        }
        
    }
        
}
    
    
    