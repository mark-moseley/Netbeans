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

package org.netbeans.core;

import java.awt.*;
import java.beans.*;
import java.io.ObjectInput;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.core.IDESettings;

import org.openide.*;
import org.openide.actions.*;
import org.openide.loaders.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Main explorer - the class remains here for backward compatibility
* with older serialization protocol. Its responsibilty is also
* to listen to the changes of "roots" nodes and open / close 
* explorer's top components properly.
*
* @author Ian Formanek, David Simonek, Jaroslav Tulach
*/
public final class NbMainExplorer extends CloneableTopComponent {

    static final long serialVersionUID=6021472310669753679L;
    //  static final long serialVersionUID=-9070275145808944151L;

    /** holds list of roots (Node) */
    private List<Node> prevRoots;

    /** assignes to each node one top component holding explorer panel
    * (Node, ExplorerTab) */
    private Map<Node, ExplorerTab> rootsToTCs;

    /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
    private transient RootsListener rootsListener;

    /** Minimal initial height of this top component */
    public static final int MIN_HEIGHT = 150;
    /** Default width of main explorer */
    public static final int DEFAULT_WIDTH = 350;
    
    /** Mapping module tabs to their root node classes */
    private static Map<Node, ModuleTab> moduleTabs;

    /** Default constructor */
    public NbMainExplorer () {
//	System.out.println("NbMainExplorer.<init>");
        // listening on changes of roots
        rootsListener = new RootsListener();
        NbPlaces p = NbPlaces.getDefault();
        p.addChangeListener(WeakListeners.change (rootsListener, p));

        refreshRoots();
    }

    public HelpCtx getHelpCtx () {
        return ExplorerUtils.getHelpCtx (getActivatedNodes (),
                                         new HelpCtx (NbMainExplorer.class));
    }
    
    /** Finds module tab in mapping of module tabs to their root node classes.
     * If it is not found it is added when parameter tc is not null. When parameter
     * tc is null new ModuleTab is created using default constructor. */
    private static synchronized ModuleTab findModuleTab (Node root, ModuleTab tc) {
	System.out.println("NbMainExplorer.findModuleTab "+root);
        if (moduleTabs == null) {
            moduleTabs = new WeakHashMap<Node, ModuleTab>(5);
        }
        ModuleTab tab = moduleTabs.get(root);
        if (tab != null) {
            return tab;
        } else {
            if (tc != null) {
                moduleTabs.put(root, tc);
                return tc;
            } else {
                ModuleTab newTC = new ModuleTab();
                moduleTabs.put(root, newTC);
                return newTC;
            }
        }
    }
    
    /** Overriden to open all top components of main explorer and
    * close this top component, as this top component exists only because of 
    * backward serialization compatibility.
    * Performed with delay, when WS is in consistent state. */
    @SuppressWarnings("deprecation")
    public void open (org.openide.windows.Workspace workspace) {
        doOpen(workspace);
    }

    @SuppressWarnings("deprecation")
    private void doOpen(org.openide.windows.Workspace workspace) {
        if (workspace == null) {
            // refresh roots request
            refreshRoots ();
        } else {
            // old explorer open request
            super.open(workspace);
            close(workspace);
            // now open new main explorer top components
            NbMainExplorer singleton = NbMainExplorer.getExplorer();
            singleton.openRoots(workspace);
        }
    }

    /** Open all main explorer's top components on current workspace */
    @SuppressWarnings("deprecation")
    public void openRoots () {
        openRoots(WindowManager.getDefault().getCurrentWorkspace());
    }

    /** Open all main explorer's top components on given workspace */
    @SuppressWarnings("deprecation")
    public void openRoots (org.openide.windows.Workspace workspace) {
        // save the tab we should activate
        ExplorerTab toBeActivated = MainTab.lastActivated;
        // perform open operation
        refreshRoots();
        Node[] rootsArray = getRoots().toArray(new Node[0]);
        TopComponent tc = null;
        for (int i = 0; i < rootsArray.length; i++) {
            tc = getRootPanel(rootsArray[i]);
            if (tc != null) {
                tc.open(workspace);
            }
        }
        // set focus to saved last activated tab or repository tab
        if (toBeActivated == null) {
            toBeActivated = getRootPanel(rootsArray[0]);
        }
        
        //Bugfix #9352 20 Feb 2001 by Marek Slama
        //requestFocus called directly on mode - it sets
        //deferred request so that requestFocus is performed
        //on correct workspace when component is shown.
        //Delayed call of requestFocus on ExplorerTab
        //was performed on incorrect workspace.
        /*final ExplorerTab localActivated = toBeActivated;
        SwingUtilities.invokeLater(new Runnable () {
                                       public void run () {
        System.out.println("++*** localActivated:" + localActivated);
                                           if (localActivated != null) {
        System.out.println("++*** Call of localActivated.requestFocus()");
                                               localActivated.requestFocus();
                                           }
                                       }
                                   });*/
        
        //Bugfix #9815: added check if toBeActivated is null before
        //request focus is called.
        //Bugfix #17956: Make sure that findMode is called after top component
        //is added to mode.
        if (SwingUtilities.isEventDispatchThread()) {
            if (toBeActivated != null) {
                Mode mode = workspace.findMode(toBeActivated);
                if (mode != null) {
                    toBeActivated.requestActive();
                }
            }
        } else {
            if (toBeActivated != null) {
                final ExplorerTab localActivated = toBeActivated;
                final org.openide.windows.Workspace localWorkspace = workspace;
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        Mode mode = localWorkspace.findMode(localActivated);
                        if (mode != null) {
                            localActivated.requestActive();
                        }
                    }
                });
            }
        }
        //End of bugfix #9815
        //End of bugfix #9352
    }

    /** Refreshes current state of main explorer's top components, so they
    * will reflect new nodes. Called when content of "roots" nodes is changed.
    */
    @SuppressWarnings("deprecation") final void refreshRoots () {
        List<Node> curRoots = getRoots ();
        // first of all we have to close top components for
        // the roots that are no longer present in the roots content
        if (prevRoots != null) {
            HashSet<Node> toRemove = new HashSet<Node>(prevRoots);
            toRemove.removeAll(curRoots);
            // ^^^ toRemove now contains only roots that are used no more
            for (Map.Entry<Node, ExplorerTab> me: rootsToTCs.entrySet()) {
                Node r = me.getKey();
                if (toRemove.contains(r)) {
                    // close top component asociated with this root context
                    // on all workspaces
                    closeEverywhere(me.getValue());
                }
            }
        } else {
            // initialize previous roots list
            prevRoots();
        }

        // create and open top components for newly added roots
        List workspaces = whereOpened(
                              rootsToTCs().values().toArray(new ExplorerTab[0])
                          );
        for (Iterator iter = curRoots.iterator(); iter.hasNext(); ) {
            Node r = (Node)iter.next();
            ExplorerTab tc = getRootPanel(r);
            if (tc == null) {
                // newly added root -> create new TC and open it on every
                // workspace where some top compoents from main explorer
                // are already opened
                tc = createTC(r, false);
                
                for (Iterator iter2 = workspaces.iterator(); iter2.hasNext(); ) {
                    tc.open((org.openide.windows.Workspace)iter2.next());
                }
            }
        }

        // save roots for use during future changes
        prevRoots = curRoots;
    }

    /** Helper method - closes given top component on all workspaces
    * where it is opened */
    @SuppressWarnings("deprecation")
    private static void closeEverywhere (TopComponent tc) {
        org.openide.windows.Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
        for (int i = 0; i < workspaces.length; i++) {
            if (tc.isOpened(workspaces[i])) {
                tc.close(workspaces[i]);
            }
        }
    }

    /** Utility method - returns list of workspaces where at least one from
    * given list of top components is opened. */
    @SuppressWarnings("deprecation")
    private static List<org.openide.windows.Workspace> whereOpened (TopComponent[] tcs) {
        org.openide.windows.Workspace[] workspaces = WindowManager.getDefault().getWorkspaces();
        ArrayList<org.openide.windows.Workspace> result = new ArrayList<org.openide.windows.Workspace>(workspaces.length);
        for (int i = 0; i < workspaces.length; i++) {
            for (int j = 0; j < tcs.length; j++) {
                if (tcs[j].isOpened(workspaces[i])) {
                    result.add(workspaces[i]);
                    break;
                }
            }
        }
        return result;
    }

    //Temporary solution for bugfix #9352. There is currently
    //no way how to select given tab other than focused in split container.
    //It requires better solution.
    //Method changed from private to public so it can be used in DefaultCreator.
    
    /** @return List of "root" nodes which has following structure:<br>
    * First goes repository, than root nodes added by modules and at last
    * runtime root node */
    public static List<Node> getRoots () {
        NbPlaces places = NbPlaces.getDefault();
        // build the list of roots
        LinkedList<Node> result = new LinkedList<Node>();
  
        //repository goes first
/*         
        #47032:  Netbeans hangs for 30 seconds during startup - so commented out
        Moreover there isn't any ExlorerTab dedicated to show this repository root. 
        result.add(RepositoryNodeFactory.getDefault().repository(DataFilter.ALL));
*/
        
        // roots added by modules (javadoc etc...)
        result.addAll(Arrays.asList(places.roots()));
        // runtime
        result.add(places.environment());

        return result;
    }

    /** Creates a top component dedicated to exploration of
    * specified node, which will serve as root context */
    private ExplorerTab createTC (Node rc, boolean deserialize) {
        // switch according to the type of the root context
        MainTab panel = null;
        NbPlaces places = NbPlaces.getDefault();

        if (rc.equals(places.environment())) {
            // default tabs
            if (deserialize) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("runtime"); // NOI18N
                if (tc != null) {
                    if (tc instanceof MainTab) {
                        panel = (MainTab) tc;
                    } else {
                        //Incorrect settings file?
                        IllegalStateException exc = new IllegalStateException
                        ("Incorrect settings file. Unexpected class returned." // NOI18N
                        + " Expected:" + MainTab.class.getName() // NOI18N
                        + " Returned:" + tc.getClass().getName()); // NOI18N
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                        panel = MainTab.getDefaultMainTab();
                    }
                } else {
                    panel = MainTab.getDefaultMainTab();
                }
            } else {
                panel = MainTab.getDefaultMainTab();
            }
            panel.setRootContext(rc, false);
        } else {
            // tabs added by modules
            //We cannot use findTopComponent here because we do not know unique
            //TC ID ie. proper deserialization of such TC will not work.
            panel = NbMainExplorer.findModuleTab(rc, null);
            panel.setRootContext(rc);
        }
        
        
        rootsToTCs().put(rc, panel);
        return panel;
    }

    /** Safe accessor for root context - top component map. */
    private Map<Node,ExplorerTab> rootsToTCs () {
        if (rootsToTCs == null) {
            rootsToTCs = new HashMap<Node,ExplorerTab>(7);
        }
        return rootsToTCs;
    }

    /** Safe accessor for list of previous root nodes */
    private List<Node> prevRoots () {
        if (prevRoots == null) {
            prevRoots = new LinkedList<Node>();
        }
        return prevRoots;
    }

    /** Deserialize this top component, sets as default.
    * Provided provided here only for backward compatibility
    * with older serialization protocol */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        //System.out.println("READING old main explorer..."); // NOI18N
        // read explorer panels (and managers)
        int cnt = in.readInt ();
        for (int i = 0; i < cnt; i++) {
            in.readObject();
        }
        in.readObject();
        // read property sheet switcher state...
        in.readBoolean ();
        in.readBoolean ();
        in.readInt();
        in.readInt();
    }

    //Temporary solution for bugfix #9352. There is currently
    //no way how to select given tab other than focused in split container.
    //It requires better solution.
    //Method changed from package to public so it can be used in DefaultCreator.
    
    /** Finds the right panel for given node.
    * @return the panel or null if no such panel exists
    */
    public final ExplorerTab getRootPanel (Node root) {
        return rootsToTCs().get(root);
    }


    // -------------------------------------------------------------------------
    // Static methods

    /** Static method to obtains the shared instance of NbMainExplorer
    * @return the shared instance of NbMainExplorer
    */
    public static NbMainExplorer getExplorer () {
        if (explorer == null) {
            explorer = new NbMainExplorer ();
        }
        return explorer;
    }

    /** @return The mode for main explorer on given workspace.
    * Creates explorer mode if no such mode exists on given workspace */
    @SuppressWarnings("deprecation")
    private static Mode explorerMode (org.openide.windows.Workspace workspace) {
        Mode result = workspace.findMode("explorer"); // NOI18N
        if (result == null) {
            // create explorer mode on current workspace
            String displayName = NbBundle.getBundle(NbMainExplorer.class).
                                 getString("CTL_ExplorerTitle");
            result = workspace.createMode(
                         "explorer", // NOI18N
                         displayName,
                         NbMainExplorer.class.getResource(
                             "/org/netbeans/core/resources/frames/explorer.gif" // NOI18N
                         )
                     );
        }
        return result;
    }

    /** Shared instance of NbMainExplorer */
    private static NbMainExplorer explorer;


    /** Common explorer top component which composites bean tree view
    * to view given context. */
    public static class ExplorerTab extends org.netbeans.beaninfo.ExplorerPanel
        implements /*DeferredPerformer.DeferredCommand,*/ TopComponent.Cloneable {
        static final long serialVersionUID =-8202452314155464024L;
        /** composited view */
        protected TreeView view;
        /** listeners to the root context and IDE settings */
        private PropertyChangeListener weakRcL, weakIdeL;
        private NodeListener weakNRcL;
        private IDESettings ideSettings;

        private NodeListener rcListener;
        /** validity flag */
        private boolean valid = true;
        private boolean rootVis = true;
        
        /** Used by ModuleTab to set persistence type according
         * root context node persistence ability. */
        protected int persistenceType = TopComponent.PERSISTENCE_ALWAYS;
        
        public ExplorerTab () {
            super();
            // complete initialization of composited explorer actions
            ideSettings = IDESettings.findObject(IDESettings.class, true);
            
            getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), ideSettings.getConfirmDelete ())); 
            
            // attach listener to the changes of IDE settings
            weakIdeL = WeakListeners.propertyChange(rcListener(), ideSettings);
            ideSettings.addPropertyChangeListener(weakIdeL);
        }
        
        /** Overriden to explicitely set persistence type of ExplorerTab
         * to PERSISTENCE_ALWAYS 
         */
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ALWAYS;
        }

        /** Initialize visual content of component */
        protected void componentShowing () {
            super.componentShowing ();
            
            if (view == null) {
                view = initGui ();
                view.setRootVisible(rootVis);
                
                view.getAccessibleContext().setAccessibleName(NbBundle.getBundle(NbMainExplorer.class).getString("ACSN_ExplorerBeanTree"));
                view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(NbMainExplorer.class).getString("ACSD_ExplorerBeanTree"));
            }
        }

        /** Performs superclass addNotify code, then delegates to
         * componentShowing if component is used outside window system.
         * Needed for proper initialization.
         */
        public void addNotify () {
            super.addNotify();
            if (WindowManager.getDefault().findMode(this) != null) {
                return;
            }
            componentShowing();
        }
        
        /** Transfer focus to view. */
        @SuppressWarnings("deprecation") public void requestFocus () {
            super.requestFocus();
            if (view != null) {
                view.requestFocus();
            }
        }
        
        /** Transfer focus to view. */
        @SuppressWarnings("deprecation") public boolean requestFocusInWindow () {
            super.requestFocusInWindow();
            if (view != null) {
                return view.requestFocusInWindow();
            } else {
                return false;
            }
        }
        
        /** Initializes gui of this component. Subclasses can override
        * this method to install their own gui.
        * @return Tree view that will serve as main view for this explorer.
        */
        protected TreeView initGui () {
            TreeView view = new BeanTreeView();
            view.setDragSource (true);
            setLayout(new BorderLayout());
            add (view);
            return view;
        }

        /** Ensures that component is valid before opening */
        @SuppressWarnings("deprecation") public void open (org.openide.windows.Workspace workspace) {
            setValidRootContext();
            
            super.open(workspace);
        }

        /** Sets new root context to view. Name, icon, tooltip
        * of this top component will be updated properly */
        public void setRootContext (Node rc) {
            Node oldRC = getExplorerManager().getRootContext();
            // remove old listener, if possible
            if (weakRcL != null) {
                oldRC.removePropertyChangeListener(weakRcL);
            }
            if (weakNRcL != null) {
                oldRC.removeNodeListener(weakNRcL);
            }
            getExplorerManager().setRootContext(rc);
            initializeWithRootContext(rc);
        }
        
        public void setRootContext(Node rc, boolean rootVisible) {
            rootVis = rootVisible;
            if (view != null) {
                view.setRootVisible(rootVisible);
            }
            setRootContext(rc);
        }

        // #16375. Not to try to serialize explored nodes which aren't
        // serializable (getHandle returns null).
        /** Adjusts this component persistence according
         * root context node persistence ability. */
        public void adjustComponentPersistence() {
            Node.Handle handle = getExplorerManager().getRootContext().getHandle();
            if(handle == null) {
                // Not persistent.
                persistenceType = TopComponent.PERSISTENCE_NEVER;
            } else {
                // Persistent.
                persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED;
            }
        }

        public Node getRootContext () {
            return getExplorerManager().getRootContext();
        }
        
        /** Deserialization of ExploreTab, if subclass overwrites this method it
            MUST call scheduleValidation() */
        public Object readResolve() throws java.io.ObjectStreamException {
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            //Bugfix #17622, call of scheduleValidation() moved from
            //readExternal().
            scheduleValidation();
            return this;
        }

        private void setValidRootContext() {
            if (!valid) {
                valid = true;
                validateRootContext();
            }
        }

        /** Validates root context of this top component after deserialization.
        * It is guaranteed that this method is called at a time when
        * getExplorerManager().getRootContext() call will return valid result.
        * Subclasses can override this method and peform further validation
        * or even set new root context instead of deserialized one.<br>
        * Default implementation just initializes top component with standard
        * deserialized root context. */
        protected void validateRootContext () {
            initializeWithRootContext(getExplorerManager().getRootContext());
        }

        // Bugfix #5891 04 Sep 2001 by Jiri Rechtacek
        // the title is derived from the root context
        // it isn't changed by a selected node in the tree
        /** Called when the explored context changes.
        * Overriden - we don't want title to change in this style.
        */
        protected void updateTitle () {
            // set name by the root context
            setName(getExplorerManager ().getRootContext().getDisplayName());
        }

        private NodeListener rcListener () {
            if (rcListener == null) {
                rcListener = new RootContextListener();
            }
            return rcListener;
        }

        /** Initialize this top component properly with information
        * obtained from specified root context node */
        private void initializeWithRootContext (Node rc) {
            // update TC's attributes
            setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
            setToolTipText(rc.getShortDescription());
            // bugfix #15136
            setName(rc.getDisplayName());
            updateTitle();

            if (weakRcL == null) {
                weakRcL = WeakListeners.propertyChange(rcListener(), rc);
            }
            else {
                rc.removePropertyChangeListener(weakRcL);
            }
            rc.addPropertyChangeListener(weakRcL);
            
            if (weakNRcL == null) {
                weakNRcL = org.openide.nodes.NodeOp.weakNodeListener (rcListener(), rc);
            }
            else {
                rc.removeNodeListener(weakNRcL);
            }
            rc.addNodeListener(weakNRcL);
        }
        
        // put a request for later validation
        // we must do this here, because of ExplorerManager's deserialization.
        // Root context of ExplorerManager is validated AFTER all other
        // deserialization, so we must wait for it
        protected final void scheduleValidation() {
            valid = false;
            setValidRootContext();
        }
        
        /* Updated accessible name of the tree view */
        public void setName(String name) {
            super.setName(name);
            if (view != null) {
                view.getAccessibleContext().setAccessibleName(name);
            }
        }
        
        /* Updated accessible description of the tree view */
        public void setToolTipText(String text) {
            super.setToolTipText(text);
            if (view != null) {
                view.getAccessibleContext().setAccessibleDescription(text);
            }
        }

        public TopComponent cloneComponent() {
            ExplorerTab nue = new ExplorerTab();
            nue.getExplorerManager().setRootContext(getExplorerManager().getRootContext());
            try {
                nue.getExplorerManager().setSelectedNodes(getExplorerManager().getSelectedNodes());
            } catch (PropertyVetoException pve) {
                ErrorManager.getDefault().notify(pve);
            }
            return nue;
        }
        
        /** Multi - purpose listener, listens to: <br>
        * 1) Changes of name, icon, short description of root context.
        * 2) Changes of IDE settings, namely delete confirmation settings */
        private final class RootContextListener extends Object implements NodeListener {
            
            RootContextListener() {}
            
            public void propertyChange (PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                Object source = evt.getSource();
                if (source instanceof IDESettings) {
                    // possible change in confirm delete settings
                    getActionMap().put("delete", ExplorerUtils.actionDelete(getExplorerManager(), ((IDESettings)source).getConfirmDelete())); 
                    return;
                }
                // root context node change
                final Node n = (Node)source;
                if (Node.PROP_DISPLAY_NAME.equals(propName) ||
                        Node.PROP_NAME.equals(propName)) {
                    // Fix #39275 start - posted to awt thread.
                    Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                setName(n.getDisplayName());
                            }
                        });
                    // fix #39275 end
                } else if (Node.PROP_ICON.equals(propName)) {
                    // Fix #39275 start - posted to awt thread.
                    Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
                            }
                        });
                    // fix #39275 end
                } else if (Node.PROP_SHORT_DESCRIPTION.equals(propName)) {
                    setToolTipText(n.getShortDescription());
                }
            }
            
            @SuppressWarnings("deprecation") public void nodeDestroyed(org.openide.nodes.NodeEvent nodeEvent) {
                ExplorerTab.this.setCloseOperation(TopComponent.CLOSE_EACH);
                ExplorerTab.this.close();
            }            
            
            public void childrenRemoved(org.openide.nodes.NodeMemberEvent e) {}
            public void childrenReordered(org.openide.nodes.NodeReorderEvent e) {}
            public void childrenAdded(org.openide.nodes.NodeMemberEvent e) {}
            
        } // end of RootContextListener inner class

    } // end of ExplorerTab inner class

    /** Tab of main explorer. Tries to dock itself to main explorer mode
    * before opening, if it's not docked already.
    * Also deserialization is enhanced in contrast to superclass */
    public static class MainTab extends ExplorerTab {
        static final long serialVersionUID =4233454980309064344L;

        /** Holds main tab which was last activated.
        * Used during decision which tab should receive focus
        * when opening all tabs at once using NbMainExplorer.openRoots()
        */
        private static MainTab lastActivated;
        
        private static MainTab DEFAULT;

        public static synchronized MainTab getDefaultMainTab() {
            if (DEFAULT == null) {
                DEFAULT = new MainTab();
                // put a request for later validation
                // we must do this here, because of ExplorerManager's deserialization.
                // Root context of ExplorerManager is validated AFTER all other
                // deserialization, so we must wait for it
                DEFAULT.scheduleValidation();
            }
            
            return DEFAULT;
        }
        
        /** Creator/accessor method of Runtime tab singleton. Instance is properly
         * deserialized by winsys.
         */
        public static MainTab findEnvironmentTab () {
            return (MainTab)getExplorer().createTC(
                NbPlaces.getDefault().environment(), true
            );
        }
        
        /** Creator/accessor method used ONLY by winsys for first time instantiation
         * of Runtime tab. Use <code>findEnvironmentTab</code> to properly deserialize
         * singleton instance.
         */
        public static MainTab createEnvironmentTab () {
            return (MainTab)getExplorer().createTC(
            NbPlaces.getDefault().environment(), false
            );
        }
        
        /** Overriden to explicitely set persistence type of MainTab
         * to PERSISTENCE_ALWAYS */
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_ALWAYS;
        }
        
        protected String preferredID () {
            return "runtime"; //NOI18N
        }
        
        public HelpCtx getHelpCtx () {
            return ExplorerUtils.getHelpCtx (getExplorerManager ().getSelectedNodes (),
                    new HelpCtx (EnvironmentNode.class));
	}

        /** Deserialization of RepositoryTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            if (DEFAULT == null) {
                DEFAULT = this;
            }
            getDefaultMainTab().scheduleValidation();
            return getDefaultMainTab();
        }
        
        @SuppressWarnings("deprecation") public void open (org.openide.windows.Workspace workspace) {
            org.openide.windows.Workspace realWorkspace = (workspace == null)
                                      ? WindowManager.getDefault().getCurrentWorkspace()
                                      : workspace;
            Mode ourMode = realWorkspace.findMode(this);
            if (ourMode == null) {
                explorerMode(realWorkspace).dockInto(this);
            }
            super.open(workspace);
        }

        /** Called when the explored context changes.
        * Overriden - we don't want title to chnage in this style.
        */
        protected void updateTitle () {
            // empty to keep the title unchanged
        }

        /** Overrides superclass' version, remembers last activated
        * main tab */
        protected void componentActivated () {
            super.componentActivated();
            lastActivated = this;
        }

        /** Registers root context in main explorer in addition to superclass'
        * version */
        protected void validateRootContext () {
            super.validateRootContext();
            registerRootContext(getExplorerManager().getRootContext());
        }

        /* Add given root context and this top component
        * to the map of main explorer's top components and nodes */
        protected void registerRootContext (Node rc) {
            NbMainExplorer explorer = NbMainExplorer.getExplorer();
            explorer.prevRoots().add(rc);
            explorer.rootsToTCs().put(rc, this);
        }

    } // end of MainTab inner class

    /** Special class for tabs added by modules to the main explorer */
    public static class ModuleTab extends MainTab {
        static final long serialVersionUID =8089827754534653731L;
        
        public ModuleTab() {
//	    System.out.println("NbMainExplorer.ModuleTab");
        }
                
        
        public void setRootContext(Node root) {
            super.setRootContext(root);
            adjustComponentPersistence();
        }
        
        /** Overriden to explicitely set persistence type of ModuleTab
         * to selected type */
        public int getPersistenceType() {
            return persistenceType;
        }
        
        /** Throws deserialized root context and sets proper node found
        * in roots set as new root context for this top component.
        * The reason for such construction is to keep the uniquennes of
        * root context node after deserialization. */
        protected void validateRootContext () {
            // find proper node
            Class nodeClass = getExplorerManager().getRootContext().getClass();
            Node[] roots = NbPlaces.getDefault().roots();
            for (int i = 0; i < roots.length; i++) {
                if (nodeClass.equals(roots[i].getClass())) {
                    setRootContext(roots[i]);
                    registerRootContext(roots[i]);
                    break;
                }
            }
        }
        
        /** Deserialization of ModuleTab */
        public Object readResolve() throws java.io.ObjectStreamException {
            Node root = getExplorerManager().getRootContext();
            
            ModuleTab tc = NbMainExplorer.findModuleTab(root, this);
            if(tc == null) {
                throw new java.io.InvalidObjectException(
                    "Cannot deserialize ModuleTab for node " + root); // NOI18N
            }
            
            tc.scheduleValidation();
            return tc;
        }

    } // end of ModuleTab inner class

    /** Listener on roots, listens to changes of roots content */
    private static final class RootsListener extends Object implements ChangeListener {
        
        RootsListener() {}
        
        public void stateChanged(ChangeEvent e) {
            NbMainExplorer.getExplorer().doOpen(null);
        }
    } // end of RootsListener inner class

    public static void main (String[] args) throws Exception {
        NbMainExplorer e = new NbMainExplorer ();
        e.open ();
    }
}
