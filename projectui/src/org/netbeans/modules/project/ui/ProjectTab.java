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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import org.openide.ErrorManager;
import org.openide.actions.PopupAction;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.*;

/** TopComponment for viewing open projects. 
 * <P>
 * PENEDING : Fix persistence when new Winsys allows 
 *
 * @author Petr Hrebejk
 */
public class ProjectTab extends TopComponent 
                        implements ExplorerManager.Provider {
                
    public static final String ID_LOGICAL = "projectTabLogical_tc"; // NOI18N                            
    public static final String ID_PHYSICAL = "projectTab_tc"; // NOI18N                        
    
    private static final Image ICON_LOGICAL = org.openide.util.Utilities.loadImage( "org/netbeans/modules/project/ui/resources/projectTab.gif" );
    private static final Image ICON_PHYSICAL = org.openide.util.Utilities.loadImage( "org/netbeans/modules/project/ui/resources/filesTab.gif" );
    
    private static Map tabs = new HashMap();                            
                            
    private transient final ExplorerManager manager;
    private transient Node rootNode;
    
    private String id;
    private transient final ProjectTreeView btv;
                         
    public ProjectTab( String id ) {
        this();
        this.id = id;
        initValues( id );
    }
    
    public ProjectTab() {

        // This should not be here byt for some reason it does not work
        // In the module installer
        Hacks.hackFolderActions();
        
        // See #36315        
        manager = new ExplorerManager();
        
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        
        initComponents();
        
        btv = new ProjectTreeView();    // Add the BeanTreeView
        
        btv.setRootVisible(false);
        
        add( btv, BorderLayout.CENTER ); 
        
        associateLookup( ExplorerUtils.createLookup(manager, map) );
        
    }

    private void initValues( String tcID ) {
        
        String name = NbBundle.getMessage(ProjectTab.class, "LBL_" + tcID ); // NOI18N
        setName( name );
        setToolTipText( name );
        
        if ( tcID.equals( ID_LOGICAL ) ) {
            setIcon( ICON_LOGICAL ); 
        }
        else {
            setIcon( ICON_PHYSICAL );
        }
            
        if ( rootNode == null ) {
            // Create the node which lists open projects      
            rootNode = new ProjectsRootNode( tcID.equals( ID_LOGICAL ) ? ProjectsRootNode.LOGICAL_VIEW : ProjectsRootNode.PHYSICAL_VIEW );
        }
        manager.setRootContext( rootNode );
    }
            
    /** Explorer manager implementation 
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /* Singleton accessor. As ProjectTab is persistent singleton this
     * accessor makes sure that ProjectTab is deserialized by window system.
     * Uses known unique TopComponent ID TC_ID = "projectTab_tc" to get ProjectTab instance
     * from window system. "projectTab_tc" is name of settings file defined in module layer.
     * For example ProjectTabAction uses this method to create instance if necessary.
     */
    public static synchronized ProjectTab findDefault( String tcID ) {

        ProjectTab tab = (ProjectTab)tabs.get( tcID );
        
        if ( tab == null ) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            
            TopComponent tc = WindowManager.getDefault().findTopComponent( tcID ); 
            if (tc != null) {
                if (!(tc instanceof ProjectTab)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + ProjectTab.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    tab = ProjectTab.getDefault( tcID );
                }
                else {
                    tab = (ProjectTab)tc;
                }
            } 
            else {
                //This should not happen when settings file is correctly defined in module layer.
                //TestComponent00 cannot be deserialized
                //Fallback to accessor reserved for window system.
                tab = ProjectTab.getDefault( tcID );
            }
        }
        return tab;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * ProjectTab instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of ProjectTab */
    public static synchronized ProjectTab getDefault( String tcID ) {
        
        ProjectTab tab = (ProjectTab)tabs.get( tcID );
        
        if ( tab == null ) {
            tab = new ProjectTab( tcID );            
            tabs.put( tcID, tab );
        }
        
        return tab;        
    }
    
    public static TopComponent getLogical() {
        return getDefault( ID_LOGICAL );
    }
    
    public static TopComponent getPhysical() {
        return getDefault( ID_PHYSICAL );
    }
    
    protected String preferredID () {
        return id;
    }
    
    public HelpCtx getHelpCtx() {
        return ExplorerUtils.getHelpCtx( 
            manager.getSelectedNodes(),
            ID_LOGICAL.equals( id ) ? new HelpCtx( "ProjectTab_Projects" ) : new HelpCtx( "ProjectTab_Files" ) );
    }

     
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    // APPEARANCE
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
        
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return btv.requestFocusInWindow();
    }
    
    // PERSISTENCE
    
    private static final long serialVersionUID = 9374872358L;
    
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal( out );
        
        out.writeObject( id );
        out.writeObject( rootNode.getHandle() );                
        out.writeObject( btv.getExpandedPaths() );
    }

    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {        
        super.readExternal( in );
        id = (String)in.readObject();
        rootNode = ((Node.Handle)in.readObject()).getNode();
        List exPaths = (List)in.readObject();
        initValues( id );
        btv.expandNodes( exPaths );
    }
    
    // MANAGING ACTIONS
    
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
    // SEARCHING NODES
    
    public boolean selectNode( Object object ) {
        return selectNode (object, true);
    }
    
    public boolean selectNode( Object object, boolean requestFocus ) {
        // System.out.println("Selecting node " + id + " : " + object );
        
        ProjectsRootNode root = (ProjectsRootNode)manager.getRootContext();
        Node selectedNode = root.findNode( object );
        if ( selectedNode != null ) {
            try {
                manager.setSelectedNodes( new Node[] { selectedNode } );
                open();
                if (requestFocus) {
                    requestActive();                
                }
                btv.scrollToNode(selectedNode);
                return true;
            }
            catch ( PropertyVetoException e ) {
                // Bad day node found but can't be selected
                return false;
            }
        }
        
        return false;
        
                
        /* Nice old version with lookup and names 
        Node root = manager.getRootContext();
        
        Collection pathResolvers  = root.getLookup().lookup( new Lookup.Template( NodePathResolver.class ) ).allInstances();
        
        String path[] = null;
        for( Iterator it = pathResolvers.iterator(); it.hasNext(); ) {
            NodePathResolver npr = (NodePathResolver)it.next();
            path = npr.getNodePath( object );
            if ( path != null ) {
                try {
                    Node selectedNode = NodeOp.findPath( root, path );
                    if ( selectedNode != null ) {
                        open();
                        requestActive();
                        manager.setSelectedNodes( new Node[] { selectedNode } );
                        return;
                    }
                }
                catch ( NodeNotFoundException e ) {
                    // The nNode does not exist keep searching     
                    // System.out.println("NOT FOUND " );
                    // print( path );
                }
                catch ( PropertyVetoException e ) {
                    // Bad day node found but can't be selected
                    return;
                }
            }            
        }
        */
        
    }
    
    /*
    private static  void print( String[] path ) {
        for( int i = 0; i < path.length; i++ ) {
            System.out.print( path[i] + "/" );
        }
        System.out.println("");
    }
    */
    
    // Private innerclasses ----------------------------------------------------
    
    /** Extending bean treeview. To be able to persist the selected paths
     */
    private class ProjectTreeView extends BeanTreeView {
        public void scrollToNode(Node n) {
            TreeNode tn = Visualizer.findVisualizer( n );
            if (tn == null) return;

            TreeModel model = tree.getModel();
            if (!(model instanceof DefaultTreeModel)) return;

            TreePath path = new TreePath(((DefaultTreeModel)model).getPathToRoot(tn));
            Rectangle r = tree.getPathBounds(path);
            if (r != null) tree.scrollRectToVisible(r);
	}
                        
        public List getExpandedPaths() { 

            List result = new ArrayList();
            
            TreeNode rtn = Visualizer.findVisualizer( rootNode );
            TreePath tp = new TreePath( rtn ); // Get the root
            
            for( Enumeration exPaths = tree.getExpandedDescendants( tp ); exPaths.hasMoreElements(); ) {
                TreePath ep = (TreePath)exPaths.nextElement();
                Node en = Visualizer.findNode( ep.getLastPathComponent() );                
                String[] path = NodeOp.createPath( en, rootNode );
                
                // System.out.print("EXP "); ProjectTab.print( path );
                
                result.add( path );
            }
            
            return result;
            
        }
        
        /** Expands all the paths, when exists
         */
        public void expandNodes( List exPaths ) {
            
            for( Iterator it = exPaths.iterator(); it.hasNext(); ) {
                String[] sp = (String[])it.next();
                TreePath tp = stringPath2TreePath( sp );
                
                if ( tp != null ) {                
                    showPath( tp );
                }
            }
        }
        
                
        
        /** Converts path of strings to TreePath if exists null otherwise
         */
        private TreePath stringPath2TreePath( String[] sp ) {

            try {
                Node n = NodeOp.findPath( rootNode, sp ); 
                
                // Create the tree path
                TreeNode tns[] = new TreeNode[ sp.length + 1 ];
                
                for ( int i = sp.length; i >= 0; i--) {
                    tns[i] = Visualizer.findVisualizer( n );
                    n = n.getParentNode();                    
                }                
                return new TreePath( tns );
            }
            catch ( NodeNotFoundException e ) {
                return null;
            }
        }
        
    }
    
}
