/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.Component;
import java.beans.*;
import java.util.*;

import org.openide.actions.NewTemplateAction;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceSupport;
import org.openide.filesystems.*;
import org.openide.util.datatransfer.*;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.ErrorManager;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.util.actions.CallableSystemAction;

/** Data system encapsulates logical structure of more file systems.
* It also allows filtering of content of DataFolders
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public final class DataSystem extends AbstractNode 
implements RepositoryListener, NewTemplateAction.Cookie {
    /** default instance */
    private static DataSystem def;

    /** the file system pool to work with */
    private transient Repository fileSystemPool;

    /** filter for the data system */
    DataFilter filter;

    /** Constructor.
    * @param fsp file system pool
    * @param filter the filter for filtering files
    */
    private DataSystem(Children ch, Repository fsp, DataFilter filter) {
        super (ch);
        fileSystemPool = fsp;
        this.filter = filter;
        initialize();
        setIconBase ("org/netbeans/core/resources/repository"); // NOI18N
        setName (NbBundle.getBundle (DataSystem.class).getString ("dataSystemName"));
        setShortDescription (NbBundle.getBundle (DataSystem.class).getString ("CTL_Repository_Hint"));
        getCookieSet ().add (new InstanceSupport.Instance (fsp));
        getCookieSet ().add (this);
    }

    /** Constructor. Uses default file system pool.
    * @param filter the filter to use
    */
    private DataSystem(Children ch, DataFilter filter) {
        this (ch, Repository.getDefault(), filter);
    }

    // delegate Index cookie to Filesystems Settings for convenience
    public Node.Cookie getCookie(Class clazz) {
        if (clazz == Index.class) {
            return NbPlaces.getDefault().repositorySettings().getCookie(clazz);
        } else {
            return super.getCookie(clazz);
        }
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DataSystem.class);
    }

    /** Factory for DataSystem instances */
    public static Node getDataSystem(DataFilter filter) {
        if (filter == null) {
            if (def != null) {
                return def;
            }
            return def = new DataSystem(new DSMap (), DataFilter.ALL);
        } else {
            return new DataSystem(new DSMap (), filter);
        }
    }

    /** Gets a DataSystem */
    public static Node getDataSystem() {
        return getDataSystem(null);
    }

    void initialize () {
        fileSystemPool.addRepositoryListener (WeakListener.repository (this, fileSystemPool));
        Enumeration en = fileSystemPool.getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            fs.addPropertyChangeListener (WeakListener.propertyChange ((DSMap)getChildren (), fs));
        }
        refresh ();
    }

    /** writes this node to ObjectOutputStream and its display name
    */
    public Handle getHandle() {
        return filter == DataFilter.ALL ? new DSHandle (null) : new DSHandle(filter);
    }


    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public org.openide.util.actions.SystemAction[] createActions() {
        return new SystemAction[] {
                   SystemAction.get (org.openide.actions.FindAction.class),
                   null,
                   SystemAction.get(RefreshAllAction.class), // #31047
                   null,
/*
                   SystemAction.get (org.netbeans.core.actions.AddFSAction.class),
                   SystemAction.get (org.netbeans.core.actions.AddJarAction.class),
                   null,
*/
                   SystemAction.get (org.netbeans.core.actions.MountAction.class),
                   null,
                   // See getCookie override:
                   SystemAction.get(org.openide.actions.ReorderAction.class),
                   null,
                   SystemAction.get (org.openide.actions.ToolsAction.class),
                   //SystemAction.get (org.openide.actions.PropertiesAction.class), // #12072
                   SystemAction.get (org.openide.actions.CustomizeAction.class),
               };
    }

    /** Called when new file system is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded (RepositoryEvent ev) {
        ev.getFileSystem ().addPropertyChangeListener (
            WeakListener.propertyChange ((DSMap)getChildren (), ev.getFileSystem ())
        );
        refresh ();
    }

    /** Called when a file system is deleted from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved (RepositoryEvent ev) {
        refresh ();
    }
    /** Called when the fsp is reordered */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
        refresh ();
    }

    /** Refreshes the pool.
    */
    void refresh () {
        refresh (null);
    }

    /** Refreshes the pool.
    * @param fs file system to remove
    */
    void refresh (FileSystem fs) {
        ((DSMap)getChildren ()).refresh (fileSystemPool, fs);
    }

    /** We have customizer */
    public boolean hasCustomizer() {
        return true;
    }

    /** Create the customizer */
    public Component getCustomizer () {
        NbMainExplorer.SettingsTab nb = new NbMainExplorer.SettingsTab ();
        nb.getExplorerManager ().setRootContext (
            NbPlaces.getDefault().repositorySettings()
        );
        nb.getAccessibleContext().setAccessibleDescription(
            NbBundle.getBundle(DataSystem.class).getString("ACSD_DataSystemCustomizer"));
        return nb;
    }

    /** Getter for the wizard that should be used for this cookie.
     */
    public org.openide.loaders.TemplateWizard getTemplateWizard() {
        return org.netbeans.core.ui.MountNode.wizard();
    }

    /** Children that listens to changes in filesystem pool.
    */
    static class DSMap extends Children.Keys implements PropertyChangeListener {

        public void propertyChange (PropertyChangeEvent ev) {
            //System.out.println ("Property change"); // NOI18N
            DataSystem ds = getDS ();
            if (ds == null) return;

            if (ev.getPropertyName ().equals ("hidden")) {
                // change in the hidden state of a file system
                ds.refresh ();
            } else if (ev.getPropertyName().equals("root")) {
                FileSystem fs = (FileSystem)ev.getSource ();
                ds.refresh (fs);
                ds.refresh ();
            }
        }

        /** The node */
        private DataSystem getDS() {
            return (DataSystem)getNode ();
        }

        protected Node[] createNodes (Object key) {
            DataFolder df = (DataFolder)key;
            Node n = new RootFolderNode (df, df.createNodeChildren (getDS ().filter));
            try {
                n = org.netbeans.core.ui.MountNode.customize (n, df.getPrimaryFile ().getFileSystem ());
            } catch (FileStateInvalidException fsi) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsi);
            }
            Node[] retVal = (n != null) ? new Node[] { n } : new Node[] {};
            return retVal;
        }

        /** Refreshes the pool.
        * @param fileSystemPool the pool
        * @param fs file system to remove
        */
        public void refresh(Repository fileSystemPool, FileSystem fs) {
            Enumeration en = fileSystemPool.getFileSystems();
            ArrayList list = new ArrayList();
            while (en.hasMoreElements()) {
                Object o = en.nextElement();
                if (fs != o && !((FileSystem)o).isHidden()) {
                    DataObject root = null;
                    try {
                        root = DataObject.find(((FileSystem)o).getRoot());
                    }
                    catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        // root will remain null and will be accepted
                        // (as that seems safer than not accepting it)
                    }
                    if ((root instanceof DataFolder) && getDS().filter.acceptDataObject(root))  {
                        list.add(root);
                    }
                }
            }
            setKeys(list);
        }            
    }

    /** Serialization. */
    private static class DSHandle implements Handle {
        DataFilter filter;

        static final long serialVersionUID =-2266375092419944364L;
        public DSHandle(DataFilter f) {
            filter = f;
        }

        public Node getNode() {
            return getDataSystem (filter);
        }
    }
    
    public static final class NbRepositoryNodeFactory extends RepositoryNodeFactory {
        
        public Node repository(DataFilter f) {
            return DataSystem.getDataSystem(f == DataFilter.ALL ? null : f);
        }
        
    }
    
    public static final class RefreshAllAction extends CallableSystemAction {
        
        public String getName() {
            return NbBundle.getMessage(DataSystem.class, "LAB_refresh_all_filesystems");
        }
        
        public void performAction() {
            Enumeration e = Repository.getDefault().fileSystems();
            while (e.hasMoreElements()) {
                // Do not restrict to just visible filesystems; hidden ones
                // may be important too for various reasons.
                FileSystem fs = (FileSystem)e.nextElement();
                fs.refresh(false);
            }
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(DataSystem.class);
        }
        
    }

}
