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

package org.netbeans.core.windows.services;

import org.openide.util.WeakListeners;
import org.openide.util.WeakListeners;
import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.cookies.InstanceCookie;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;

import org.netbeans.core.NbPlaces;
import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.WindowManager;

/** The node for the toolbar folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Dafe Simonek
*/
public final class ToolbarFolderNode extends DataFolder.FolderNode implements PropertyChangeListener {

    /** Actions of this node when it is top level toolbar node */
    static SystemAction[] topStaticActions;

    private DataFolder folder;
    public static final ResourceBundle bundle = NbBundle.getBundle (ToolbarFolderNode.class);

    public ToolbarFolderNode () {
        this (NbPlaces.getDefault().toolbars ());
    }

    /** Constructs this node with given node to filter.
    */
    ToolbarFolderNode (DataFolder folder) {
        folder.super(new ToolbarFolderChildren(folder));
        this.folder = folder;
        //JST: it displays only Toolbar as name!    super.setDisplayName(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_name"));
        super.setShortDescription(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_hint"));
        super.setIconBase ("org/netbeans/core/resources/toolbars"); // NOI18N
        
        ToolbarPool.getDefault().addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(this, ToolbarPool.getDefault()));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ToolbarFolderNode.class);
    }

    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType () {
                       public String getName () {
                           return bundle.getString ("PROP_newToolbarName");
                       }
                       public void create () throws IOException {
                           newToolbar();
                       }
                   },
                   new NewType () {
                       public String getName () {
                           return bundle.getString ("PROP_newToolbarConfigName");
                       }
                       public void create () throws IOException {
                           newConfiguration();
                       }
                   }
               };
    }
    
    void newToolbar () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (bundle.getString ("PROP_newToolbarLabel"),
                                         bundle.getString ("PROP_newToolbarDialog"));
        il.setInputText (bundle.getString ("PROP_newToolbar"));

        Object ok = org.openide.DialogDisplayer.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                FileObject tbFO = folder.getPrimaryFile();
                try {
                    FileObject newFO = tbFO.getFileObject(s);
                    if (newFO == null) {
                        String lastName = getLastName();
                        newFO = tbFO.createFolder (s);
                        
                        // #13015. Set new item as last one.
                        if(lastName != null) {
                            tbFO.setAttribute(
                                lastName + "/" +newFO.getNameExt(),
                                Boolean.TRUE
                            );
                        }
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault ().notify (e);
                }
            }
        }
    }

    void newConfiguration () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (bundle.getString ("PROP_newToolbarConfigLabel"),
                                         bundle.getString ("PROP_newToolbarConfigDialog"));
        il.setInputText (bundle.getString ("PROP_newToolbarConfig"));

        Object ok = org.openide.DialogDisplayer.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                String lastName = getLastName();
                ToolbarConfiguration tc = new ToolbarConfiguration (s, s);
                try {
                    tc.writeDocument();

                    // #13015. Set new item as last one.
                    if(lastName != null) {
                        folder.getPrimaryFile().setAttribute(
                            // XXX hardcoded extension,
                            // see ToolbarConfiguration.EXT_XML
                            lastName + "/" + s + ".xml", // NOI18N
                            Boolean.TRUE
                        );
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault ().notify (e);
                }
            }
        }
    }

    /** Gets name of last child.
     * @return name of last menu or <code>null</code> if there is no one */
    private String getLastName() {
        String lastName = null;
        Node[] ch = getChildren().getNodes();
        if(ch.length > 0) {
            Node last = ch[ch.length - 1];
            DataObject d = (DataObject)last.getCookie(DataObject.class);
            if(d != null) {
                lastName = d.getPrimaryFile().getNameExt();
            }
        }
        
        return lastName;
    }

    /** Actions.
    * @return array of actions for this node
    */
    public SystemAction[] getActions () {
        if (topStaticActions == null)
            topStaticActions = new SystemAction [] {
                                   SystemAction.get (FileSystemAction.class),
                                   null,
                                   SystemAction.get(PasteAction.class),
                                   null,
                                   SystemAction.get(NewAction.class),
                                   null,
                                   SystemAction.get(ToolsAction.class),
                                   SystemAction.get(PropertiesAction.class),
                               };
        return topStaticActions;
    }

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        Sheet sheet = Sheet.createDefault();
        sheet.get(Sheet.PROPERTIES).put(
            new PropertySupport.ReadWrite(
                "configuration", // NOI18N
                String.class,
                bundle.getString("PROP_ToolbarConfiguration"),
                bundle.getString("HINT_ToolbarConfiguration")
            ) {
                public void setValue(Object conf) {
                    WindowManagerImpl.getInstance().setToolbarConfigName((String)conf);
                }

                public Object getValue() {
                    return ToolbarPool.getDefault().getConfiguration();
                }

                public java.beans.PropertyEditor getPropertyEditor() {
                    return new java.beans.PropertyEditorSupport() {
                        public String[] getTags() {
                            return ToolbarPool.getDefault().getConfigurations();
                        }
                    };
                }
            }
        );
        return sheet.toArray();
    }

    /** Supports index cookie in addition to standard support.
    *
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    *
    public Node.Cookie getCookie (Class type) {
        // no index for reordering toolbars, just for toolbar items
        if ((!isTopLevel ()) && Index.class.isAssignableFrom(type)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new ToolbarIndex(dataObj, (ToolbarFolderChildren)getChildren());
            }
        }
        return super.getCookie(type);
    }
    */

    public boolean canDestroy () {
        return false;
    }

    public boolean canCut () {
        return false;
    }

    public boolean canRename () {
        return false;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("configuration".equals(evt.getPropertyName())) { // NOI18N
            firePropertyChange("configuration", evt.getOldValue(), evt.getNewValue()); // NOI18N
        }
    }
    
    /** Children for the ToolbarFolderNode. Creates ToolbarFolderNodes or
    * ToolbarItemNodes as filter subnodes...
    */
    static final class ToolbarFolderChildren extends FilterNode.Children {

        /** @param or original node to take children from */
        public ToolbarFolderChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns ToolbarFolderNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return ToolbarFolderNode filter of the original node
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new ToolbarNode(df);
            }
            // nodes in the same folder as toolbar folders are not toolbar items!
            if (node.getCookie (org.openide.loaders.InstanceDataObject.class) != null) {
                return new ToolbarItemNode(node);
            } else {
                // #18650. If is ToolbarConfiguration, use special node,
                // which sets help ctx, and later also an icon for that.
                InstanceCookie ic = (InstanceCookie)node.getCookie(InstanceCookie.class);
                
                boolean isConfig = false;
                
                if(ic != null) {
                    if(ic instanceof InstanceCookie.Of) {
                        isConfig = ((InstanceCookie.Of)ic).instanceOf(ToolbarConfiguration.class);
                    } else {
                        try {
                            if(ic.instanceClass().equals(ToolbarConfiguration.class)) {
                                isConfig = true;
                            }
                        } catch(ClassNotFoundException cnfe) {
                        } catch(IOException ioe) {
                        }
                    }
                }
                
                if(isConfig) {
                    return new ToolbarConfigurationNode(node);
                } else {
                    return node.cloneNode();
                }
            }
        }
    }

    /** This class serves as index cookie implementation for the
    * ToolbarFolderNode object. Allows reordering of Toolbar items.
    *
    static final class ToolbarIndex extends DataFolder.Index {

        /** The children we are working with *
        ToolbarFolderChildren children;

        ToolbarIndex (final DataFolder df, final ToolbarFolderChildren children) {
            super(df);
            this.children = children;
        }

        /** Overrides DataFolder.Index.getNodesCount().
        * Returns count of the nodes from the asociated chidren.
        *
        public int getNodesCount () {
            return children.getNodesCount();
        }

        /** Overrides DataFolder.Index.getNodes().
        * Returns array of subnodes from asociated children.
        * @return array of subnodes
        *
        public Node[] getNodes () {
            return children.getNodes();
        }

    } // end of ToolbarIndex
    */
    
    /** Filter node for toolbar configuration which provides help
     * context and special icon (see bug #18650), also special actions. */
    private static class ToolbarConfigurationNode extends FilterNode {
        
        private ToolbarConfigurationNode(Node filter) {
            super(filter, Children.LEAF);
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(ToolbarConfiguration.class);
        }
        
        public Image getIcon(int type) {
            return org.openide.util.Utilities.loadImage(
                "org/netbeans/core/resources/toolbarConfiguration.gif"); // NOI18N
        }
        
        public SystemAction[] getActions () {
            return new SystemAction[] {
                SystemAction.get(FileSystemAction.class),
                null, 
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };
        }
        
    } // End of class ToolbarConfigurationNode.
    

    static final class ToolbarItemNode extends FilterNode {

        /** Actions which this node supports */
        static SystemAction[] staticActions;

        /** Constructs new filter node for Toolbar item */
        ToolbarItemNode (Node filter) {
            super(filter, Children.LEAF);
        }

        /** Make Move Up and Move Down actions be enabled. */
        public boolean equals (Object o) {
            if (o == null) return false;
            return this == o || getOriginal ().equals (o) || o.equals (getOriginal ());
        }

        /** Actions.
        * @return array of actions for this node
        */
        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction [] {
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            }
            return staticActions;
        }

        /** Disallows renaming.
        */
        public boolean canRename () {
            return false;
        }

        /** Creates properties for this node */
        public Node.PropertySet[] getPropertySets () {
            /*
            ResourceBundle bundle = NbBundle.getBundle(ToolbarFolderNode.class);
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_ToolbarItemName"),
                    bundle.getString("HINT_ToolbarItemName")
                )
            );
            return sheet.toArray();
             */
            return new Node.PropertySet[] { };
        }

    } // end of ToolbarItemNode

    /** Toolbar folder node.
     */
    private static class ToolbarNode extends DataFolder.FolderNode implements PropertyChangeListener  {

        /** Actions which this node supports */
        static SystemAction[] staticActions;

        private DataFolder folder;
        private static final ResourceBundle bundle = NbBundle.getBundle (ToolbarFolderNode.class);

        /** Toolbar folder node.
        */
        ToolbarNode(DataFolder folder) {
            folder.super(new ToolbarFolderChildren(folder));
            this.folder = folder;
            //JST: it displays only Toolbar as name!    super.setDisplayName(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_name"));
            super.setShortDescription(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_hint"));
            super.setIconBase ("org/netbeans/core/resources/toolbars"); // NOI18N

            ToolbarPool.getDefault().addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(this, ToolbarPool.getDefault()));
            attachConfigListener();
        }

        /** Support for new types that can be created in this node.
        * @return array of new type operations that are allowed
        */
        public NewType[] getNewTypes () {
            return new NewType[] {
                       new NewType () {
                           public String getName () {
                               return bundle.getString ("PROP_newToolbarSeparator");
                           }
                           public void create () throws IOException {
                               newToolbarSeparator();
                           }
                       }
                   };
        }
        
        public void setName (String name, boolean rename) {
            //Bugfix #19735: Rename toolbar too, not only node
            Toolbar tb = ToolbarPool.getDefault().findToolbar(getName());
            if (tb != null) {
                tb.setName(name);
            }
            super.setName(name, rename);
        }
            
        //Fixed bug #5610 Added support for adding new separator through popup menu
        void newToolbarSeparator () {
            try {
                InstanceDataObject instData = InstanceDataObject.find
                (folder,null,"javax.swing.JToolBar$Separator"); // NOI18N

                String lastName = getLastName();
                DataObject d;

                if (instData == null) {
                    d = InstanceDataObject.create
                        (folder,null,"javax.swing.JToolBar$Separator"); // NOI18N

                } else {
                    d = instData.copy(folder);
                }

                // #13015. Set new item as last one.
                if(lastName != null) {
                    folder.getPrimaryFile().setAttribute(
                        lastName + "/" + d.getPrimaryFile().getNameExt(), // NOI18N
                        Boolean.TRUE
                    );
                }
            } catch (java.io.IOException e) {
                ErrorManager.getDefault ().notify (e);
            }
        }
        //End

        /** Gets name of last child.
         * @return name of last menu or <code>null</code> if there is no one */
        private String getLastName() {
            String lastName = null;
            Node[] ch = getChildren().getNodes();
            if(ch.length > 0) {
                Node last = ch[ch.length - 1];
                DataObject d = (DataObject)last.getCookie(DataObject.class);
                if(d != null) {
                    lastName = d.getPrimaryFile().getNameExt();
                }
            }

            return lastName;
        }

        /** Actions.
        * @return array of actions for this node
        */
        public SystemAction[] getActions () {
            if (staticActions == null)
                staticActions = new SystemAction [] {
                                    SystemAction.get (FileSystemAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    SystemAction.get(RenameAction.class),
                                    null,
                                    SystemAction.get(NewAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            return staticActions;
        }

        /** Creates properties for this node */
        public Node.PropertySet[] getPropertySets () {
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ss = sheet.get(Sheet.PROPERTIES);
            ss.put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_ToolbarName"),
                    bundle.getString("HINT_ToolbarName")
                )
            );
            ss.put(
                new PropertySupport.ReadWrite(
                    "visible", // NOI18N
                    Boolean.class,
                    bundle.getString("PROP_ToolbarVisible"),
                    bundle.getString("HINT_ToolbarVisible")
                ) {
                    public void setValue(Object v) {
                        currentConfiguration().setToolbarVisible(ToolbarPool.getDefault().findToolbar(folder.getName()), ((Boolean)v).booleanValue());
                    }

                    public Object getValue() {
                        return currentConfiguration().isToolbarVisible(ToolbarPool.getDefault().findToolbar(folder.getName()))
                            ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            );
            return sheet.toArray();
        }

        // Configuration listener
        private PropertyChangeListener wlpc;

        public void propertyChange(PropertyChangeEvent evt) {
            if ("configuration".equals(evt.getPropertyName())) { // NOI18N
                ToolbarConfiguration tc = configuration((String)evt.getOldValue());
                if (tc != null && wlpc != null) {
                    tc.removePropertyChangeListener(wlpc);
                }
                attachConfigListener();
            } else if ("constraints".equals(evt.getPropertyName())) {  // NOI18N
                firePropertyChange("visible", evt.getOldValue(), evt.getNewValue()); // NOI18N
            }
        }

        private void attachConfigListener() {
            ToolbarConfiguration tc = currentConfiguration();
            if (tc != null) {
                tc.addPropertyChangeListener(wlpc = org.openide.util.WeakListeners.propertyChange(this, tc));
            }
        }

        /** Returns current ToolbarConfiguration */
        ToolbarConfiguration currentConfiguration() {
            String conf = ToolbarPool.getDefault().getConfiguration();
            return configuration(conf);
        }

        /** Returns ToolbarConfiguration found by its name or null if such config doesn't exist. */
        ToolbarConfiguration configuration(String conf) {
            DataObject[] obj = NbPlaces.getDefault().toolbars ().getChildren();
            for (int i = 0; i < obj.length; i++) {
                DataObject o = obj[i];
                org.openide.cookies.InstanceCookie ic = (org.openide.cookies.InstanceCookie)o.getCookie(org.openide.cookies.InstanceCookie.class);
                if (ic != null) {
                    try {
                        if (ToolbarConfiguration.class.isAssignableFrom(ic.instanceClass())) {
                            ToolbarConfiguration tc = (ToolbarConfiguration)ic.instanceCreate();
                            if (conf.equals(tc.getName()))
                                return tc;
                        }
                    } catch (java.io.IOException ex) {
                        ErrorManager.getDefault ().notify (ex);
                    } catch (ClassNotFoundException ex) {
                        ErrorManager.getDefault ().notify (ex);
                    }
                }
            }
            return null;
        }
    }
}
