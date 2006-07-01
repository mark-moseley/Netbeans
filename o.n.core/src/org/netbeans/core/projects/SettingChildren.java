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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.beaninfo.editors.ListImageEditor;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.openide.actions.ToolsAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/** Filters nodes under the session node (displayed in Options dialog), adds special
 * properties to Nodes of particular settings to show/edit positions wher the
 * setting is defined on DefaultFileSystem.
 *
 * @author  Vitezslav Stejskal
 */
public final class SettingChildren extends FilterNode.Children {

    /** Name of Node.Property showing status of Session layer according to the setting */
    public static final String PROP_LAYER_SESSION = "Session-Layer"; // NOI18N
    /** Name of Node.Property showing status of Modules layer according to the setting */
    public static final String PROP_LAYER_MODULES = "Modules-Layer"; // NOI18N

    public SettingChildren (Node original) {
        super (original);
    }

    protected Node copyNode (Node node) {
        boolean filter = false;
        try {
            DataObject d = (DataObject) node.getCookie (DataObject.class);
            if (d != null) {
                InstanceCookie.Of inst = (InstanceCookie.Of)d.getCookie(InstanceCookie.Of.class);
                if (inst != null && (inst.instanceOf(Node.class) || inst.instanceOf(Node.Handle.class))) {
                    // This is just a node, not a real setting. E.g. ModuleNode, LoaderPoolNode. As such,
                    // it itself should not display any origin information, it would make no sense. However
                    // its children might have a legitimate DataObject cookie from the SFS.
                    d = null;
                }
            }
            DataFolder folder = (DataFolder) node.getCookie (DataFolder.class);
            FileSystem fs = d == null || folder != null ? null : d.getPrimaryFile ().getFileSystem ();
            filter = fs == null ? false : fs.equals (Repository.getDefault ().getDefaultFileSystem ());
        } catch (FileStateInvalidException e) {
            // ignore
        }

        return filter ? new SettingFilterNode (node) : 
            node.isLeaf() ? node.cloneNode() : new TrivialFilterNode(node);
    }
    
    private static Action[] removeActions(Action[] allActions, Action[] toDeleteActions) {
        Action[] retVal = allActions;
        List<Action> actions = new ArrayList<Action>(Arrays.asList(allActions)); // to be mutable 
        for (int i = 0; i < toDeleteActions.length; i++) {
            Action a = toDeleteActions[i];
            if (actions.contains(a)) {
                actions.remove(a);
                retVal = actions.toArray(new Action[0]);
            }                
        }            
        return retVal;
    }
    
    private static final class TrivialFilterNode extends FilterNode {
        public TrivialFilterNode(Node n) {
            super(n, new SettingChildren(n));
        }
        // #17920: Index cookie works only when equality works
        public boolean equals(Object o) {
            return this == o || getOriginal().equals(o) || (o != null && o.equals(getOriginal()));
        }
        public int hashCode() {
            return getOriginal().hashCode();
        }        
        public Action[] getActions(boolean context) {            
            return removeActions(super.getActions(context), new Action[] {SystemAction.get(ToolsAction.class)});
        } 
        public String getHtmlDisplayName() {
            return null;
        }
    }

    /** Property allowing display/manipulation of setting status for one specific layer. */
    public static class FileStateProperty extends PropertySupport {
        static final int ACTION_DEFINE = 1;
        static final int ACTION_REVERT = 2;
        static final int ACTION_DELETE = 3;
        
        private FileObject primaryFile = null;
        private int layer;

        public FileStateProperty (String name) {
            this (null, 0, name, true);
        }

        public FileStateProperty (FileObject primaryFile, int layer, String name, boolean readonly) {
            super (name, Integer.class,
                NbBundle.getMessage (FileStateProperty.class, "LBL_FSP_" + name), // NOI18N
                NbBundle.getMessage (FileStateProperty.class, "LBL_FSP_Desc_" + name), // NOI18N
                true, !readonly);
            
            this.primaryFile = primaryFile;
            this.layer = layer;

            setValue (ListImageEditor.PROP_VALUES, new Integer [] {
                new Integer (FileStateManager.FSTATE_DEFINED),
                new Integer (FileStateManager.FSTATE_IGNORED),
                new Integer (FileStateManager.FSTATE_INHERITED),
                new Integer (FileStateManager.FSTATE_UNDEFINED)
            });

            setValue (ListImageEditor.PROP_IMAGES, new Image [] {
                Utilities.loadImage ("org/netbeans/core/resources/setting-defined.gif"), // NOI18N
                Utilities.loadImage ("org/netbeans/core/resources/setting-ignored.gif"), // NOI18N
                Utilities.loadImage ("org/netbeans/core/resources/setting-inherited.gif"), // NOI18N
                Utilities.loadImage ("org/openide/resources/actions/empty.gif") // NOI18N
            });
        }

        public boolean canWrite () {
            if (!super.canWrite ())
                return false;
            
            Integer val = null;
            try {
                val = (Integer) getValue ();
            } catch (Exception e) {
                // ignore it, will be handled later
            }
            
            return val != null &&
                val.intValue () != FileStateManager.FSTATE_DEFINED &&
                (layer != FileStateManager.LAYER_MODULES || val.intValue () != FileStateManager.FSTATE_UNDEFINED);
        }

        public Object getValue () throws IllegalAccessException, InvocationTargetException {
            FileStateManager fsm = FileStateManager.getDefault ();
            return new Integer (fsm.getFileState (primaryFile, layer));
        }

        public void setValue (Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            FileStateManager fsm = FileStateManager.getDefault ();
            int action = ((Integer) val).intValue ();
            
            try {
                switch (action) {
                    case ACTION_DEFINE:
                    case ACTION_REVERT:
                        boolean go = true;

                        for (int i = 0; i < layer; i++) {
                            int state = fsm.getFileState (primaryFile, i);
                            if (state == FileStateManager.FSTATE_DEFINED) {
                                // warn user, that above defined files will be removed

                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
                                    NbBundle.getMessage (SettingChildren.class, "MSG_ask_remove_above_defined_files"), // NOI18N
                                    NotifyDescriptor.YES_NO_OPTION);

                                Object answer = org.openide.DialogDisplayer.getDefault ().notify (nd);
                                if (answer.equals (NotifyDescriptor.NO_OPTION))
                                    go = false;

                                break;
                            }
                        }

                        if (go)
                            fsm.define (primaryFile, layer, action == ACTION_REVERT);

                        break;

                    case ACTION_DELETE:
                        fsm.delete (primaryFile, layer);
                        break;

                    default:
                        throw new IllegalArgumentException ("Required file state change isn't allowed. Action=" + action); // NOI18N
                }
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public PropertyEditor getPropertyEditor () {
            return new FileStateEditor ();
        }
        
        public String getShortDescription () {
            Integer val = null;
            String s = null;

            if (primaryFile != null) {
                try {
                    val = (Integer) getValue ();
                } catch (Exception e) {
                    // ignore it, will be handled later
                }

                switch (val == null ? -1 : val.intValue()) {
                    case FileStateManager.FSTATE_DEFINED:
                        s = NbBundle.getMessage (SettingChildren.class, "LBL_fstate_defined");
                        break;
                    case FileStateManager.FSTATE_IGNORED:
                        s = NbBundle.getMessage (SettingChildren.class, "LBL_fstate_ignored");
                        break;
                    case FileStateManager.FSTATE_INHERITED:
                        s = NbBundle.getMessage (SettingChildren.class, "LBL_fstate_inherited");
                        break;
                    case FileStateManager.FSTATE_UNDEFINED:
                        s = NbBundle.getMessage (SettingChildren.class, "LBL_fstate_undefined");
                        break;
                }
            }
            else {
                s = super.getShortDescription ();
            }
            return s == null || s.length () == 0 ? null : s;
        }
    }

    /** Filter node used for adding special status related properties to setting nodes. */
    private static final class SettingFilterNode extends FilterNode {
        private FSL weakL = null;
        
        public SettingFilterNode (Node original) {
            super (original);
	    // need to keep the values in this FilterNode, not delegates
	    disableDelegation(DELEGATE_SET_VALUE | DELEGATE_GET_VALUE);

            FileObject pf = ((DataObject) getCookie (DataObject.class)).getPrimaryFile ();
            weakL = new FSL (this);
            FileStateManager.getDefault ().addFileStatusListener (weakL, pf);

            specialProp (new FileStateProperty (pf, FileStateManager.LAYER_SESSION, PROP_LAYER_SESSION, false));
            specialProp (new FileStateProperty (pf, FileStateManager.LAYER_MODULES, PROP_LAYER_MODULES, false));
        }
        
        /* @return the display name of the original node
        */
        public String getDisplayName() {
            String retVal = null;
            DataObject dobj= (DataObject) getCookie (DataObject.class);
            if (dobj != null && dobj instanceof DataShadow) {
                DataShadow dsh = (DataShadow)dobj;
                Node origNode = dsh.getOriginal().getNodeDelegate();
                if (origNode != null) {
                    retVal = origNode.getDisplayName();
                }
            }                        
            return (retVal != null) ? retVal : super.getDisplayName();
        }
        
        /** Registers special property.
         */
        private void specialProp (Node.Property p) {
            setValue (p.getName (), p);
        }
     
        // #17920: Index cookie works only when equality works
        public boolean equals(Object o) {
            return this == o || getOriginal().equals(o) || (o != null && o.equals(getOriginal()));
        }
        public int hashCode() {
            return getOriginal().hashCode();
        }
        // #24766 Exclude Customize Bean action.
        /** Overrides superclass method, excludes the ToolsAction from the node. */
        public Action[] getActions(boolean context) {
            return removeActions(super.getActions(context), new Action[] {SystemAction.get(ToolsAction.class)});
        }

        private static class FSL implements FileStateManager.FileStatusListener {
            WeakReference<SettingFilterNode> node = null;
            public FSL (SettingFilterNode sfn) {
                node = new WeakReference<SettingFilterNode> (sfn);
            }
            public void fileStatusChanged (FileObject mfo) {
                SettingFilterNode n = node.get ();
                if (n == null) {
                    FileStateManager.getDefault ().removeFileStatusListener (this, null);
                    return;
                }
                
                n.firePropertyChange (PROP_LAYER_SESSION, null, null);
                n.firePropertyChange (PROP_LAYER_MODULES, null, null);
            }
        }
    }
}
