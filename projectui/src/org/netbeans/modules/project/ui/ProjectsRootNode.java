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

import java.awt.Image;
import org.openide.nodes.FilterNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.LogicalViews;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openidex.search.SearchInfo;
import org.openidex.search.SimpleSearchInfo;

/** Root node for list of open projects
 * @author Petr Hrebejk
 */
public class ProjectsRootNode extends AbstractNode {
    
    static final int PHYSICAL_VIEW = 0;
    static final int LOGICAL_VIEW = 1;
        
    private static final String ICON_BASE = "org/netbeans/modules/project/ui/resources/projectsRootNode"; //NOI18N
    
    private static final Action[] NO_ACTIONS = new Action[0];
    
    private static Action[] ACTIONS;
    
    private ResourceBundle bundle;
    
    private Node.Handle handle;
    
    public ProjectsRootNode( int type ) {
        super( new ProjectChildren( type ) ); 
        setIconBase( ICON_BASE );
        handle = new Handle( type );
    }
        
    public String getName() {
        return ( "OpenProjects" ); // NOI18N
    }
    
    public String getDisplayName() {
        if ( this.bundle == null ) {
            this.bundle = NbBundle.getBundle( ProjectsRootNode.class );
        }
        return bundle.getString( "LBL_OpenProjectsNode_Name" ); // NOI18N
    }
    
    public boolean canRename() {
        return false;
    }
        
    public Node.Handle getHandle() {        
        return handle;        
    }
    
    public Action[] getActions( boolean context ) {
        
        if ( context ) {
            return NO_ACTIONS;
        }
        else {
            if ( ACTIONS == null ) {
                // Create the actions
                ACTIONS = new Action[] {
                    // XXX                    
                    // SystemAction.get( NodeNewProjectAction.class ),
                    // SystemAction.get( NodeOpenProjectAction.class ),                    
                };
            }
            
            return ACTIONS;
        }
        
    }
    
    /** Finds node for given object in the view
     * @return the node or null if the node was not found
     */
    Node findNode( Object target ) {        
        
        ProjectChildren ch = (ProjectChildren)getChildren();
        
        if ( ch.type == LOGICAL_VIEW ) {
            Node[] nodes = ch.getNodes( true );
            for( int i = 0; i < nodes.length; i++  ) {
                
                Project p = (Project)nodes[i].getLookup().lookup( Project.class );
                if ( p == null ) {
                    continue;
                }
                LogicalViewProvider lvp = (LogicalViewProvider)p.getLookup().lookup( LogicalViewProvider.class );
                if ( lvp != null ) {
                    Node selectedNode = lvp.findPath( nodes[i], target );
                    if ( selectedNode != null ) {
                        return selectedNode;
                    }
                }
            }
            return null;
            
        }
        else {
            
            return null;
        }        
    }
    
    private static class Handle implements Node.Handle {

        private static final long serialVersionUID = 78374332058L;
        
        private int viewType;
        
        public Handle( int viewType ) {
            this.viewType = viewType;
        }
        
        public Node getNode() {
            return new ProjectsRootNode( viewType );
        }
        
    }
    
    
    // XXX Needs to listen to project rename
    // However project rename is currently disabled so it is not a big deal
    private static class ProjectChildren extends Children.Keys implements PropertyChangeListener {
        
        int type;
        
        public ProjectChildren( int type ) {
            this.type = type;
            OpenProjectList.getDefault().addPropertyChangeListener( this );
        }
        
        // Children.Keys impl --------------------------------------------------
        
        public void addNotify() {            
            setKeys( getKeys() );
        }
        
        public void removeNotify() {
            setKeys( Collections.EMPTY_LIST );
        }
        
        protected Node[] createNodes( Object key ) {
            
            Project project = (Project)key;
            
            LogicalViewProvider lvp = (LogicalViewProvider)project.getLookup().lookup( LogicalViewProvider.class );
            
            Node nodes[] = null;
                        
            if ( type == PHYSICAL_VIEW ) {
                nodes = PhysicalView.createNodesForProject( project );
            }            
            else if ( lvp == null ) {
                nodes = new Node[] { Node.EMPTY };
            }
            else {
                nodes = new Node[] { lvp.createLogicalView() };
                if (nodes[0].getLookup().lookup(Project.class) != project) {
                    // Various actions, badging, etc. are not going to work.
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - project " + ProjectUtils.getInformation(project).getName() + " failed to supply itself in the lookup of the root node of its own logical view");
                }
            }

            Node[] badgedNodes = new Node[ nodes.length ];
            for( int i = 0; i < nodes.length; i++ ) {
                if ( type == PHYSICAL_VIEW && !PhysicalView.isProjectDirNode( nodes[i] ) ) {
                    // Don't badge external sources
                    badgedNodes[i] = nodes[i];
                }
                else {
                    badgedNodes[i] = new BadgingNode( nodes[i] );
                }
            }
                        
            return badgedNodes;
        }        
        
        // PropertyChangeListener impl -------------------------------------------------
        
        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
                setKeys( getKeys() );
            }
        }
        
        // Own methods ---------------------------------------------------------
        
        public Collection getKeys() {
            List projects = Arrays.asList( OpenProjectList.getDefault().getOpenProjects() );
            Collections.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
            
            return projects;
        }
                                                
    }
        
    private static final class BadgingNode extends FilterNode implements PropertyChangeListener {

        private static Image mainProjectBadge = Utilities.loadImage( "org/netbeans/modules/project/ui/resources/mainProjectBadge.gif" ); // NOI18N
        
        private static String badgedNamePattern = NbBundle.getMessage( ProjectsRootNode.class, "LBL_MainProject_BadgedNamePattern" );
        
        public BadgingNode( Node n) {
            super( n,
                   null,
                   new ProxyLookup(new Lookup[] {n.getLookup(), Lookups.singleton(new ProjectNodeSearchInfo(n))}) );
            OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        }
        
        public String getDisplayName() {
            String original = super.getDisplayName();
            return isMain() ? MessageFormat.format( badgedNamePattern, new Object[] { original } ) : original;
        }

        public String getHtmlDisplayName() {
            String dispName = getOriginal().getHtmlDisplayName();
            return isMain() ? "<b>" + (dispName == null ? super.getDisplayName() : dispName) + "</b>" : dispName; //NOI18N
        }

        public Image getIcon( int type ) {
            Image original = super.getIcon( type );                
            return isMain() ? Utilities.mergeImages( original, mainProjectBadge, 5, 10 ) : original;
        }

        public Image getOpenedIcon( int type ) {
            Image original = super.getOpenedIcon(type);                
            return isMain() ? Utilities.mergeImages( original, mainProjectBadge, 5, 10 ) : original;            
        }            

        public void propertyChange( PropertyChangeEvent e ) {
            if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
                fireIconChange();
                fireDisplayNameChange( null, null );
            }
        }

        private boolean isMain() {
            Project p = (Project)getLookup().lookup( Project.class );
            return p != null && OpenProjectList.getDefault().isMainProject( p );
        }

        /**
         * SearchInfo object for project nodes.
         *
         * @see  SearchInfo
         */
        static final class ProjectNodeSearchInfo implements SearchInfo {

            /** */
            private final Node projectNode;

            /**
             */
            ProjectNodeSearchInfo(Node n) {
                projectNode = n;
            }

            /**
             */
            public boolean canSearch() {
                return true;
            }

            /**
             */
            public Iterator objectsToSearch() {
                return createIterator();
            }

            /**
             */
            private Iterator createIterator() {
                /* get the project: */
                Project project = (Project) projectNode.getLookup().lookup(Project.class);
                assert project != null;

                /* get the Sources object: */
                Sources sources = ProjectUtils.getSources(project);

                /* build a delegated SearchInfo object: */
                SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                if (sourceGroups.length == 0) {
                    return Collections.EMPTY_LIST.iterator();
                }
                SimpleSearchInfo searchInfo = new SimpleSearchInfo();
                for (int i = 0; i < sourceGroups.length; i++) {
                    FileObject sourceRoot = sourceGroups[i].getRootFolder();
                    DataFolder dataFolder = DataFolder.findFolder(sourceRoot);
                    searchInfo.add(new SimpleSearchInfo(dataFolder, true));
                }

                /* return the SearchInfo's iterator: */
                return searchInfo.objectsToSearch();
            }

        }
        
    }
    
}
