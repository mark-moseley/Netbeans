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

package org.netbeans.modules.editor.options;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.actions.CustomizeBeanAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.Repository;
import org.openide.util.WeakListener;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;


/** Node representing the Editor Settings main node.
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */

public class AllOptionsNode extends FilterNode {
    
    private static final String HELP_ID = "editing.global"; // !!! NOI18N
    
    /** Creates new AllOptionsNode as BeanNode with Children.Array */
    public AllOptionsNode() throws IntrospectionException {
        super(new BeanNode(AllOptionsFolder.getDefault()), new EditorSubnodes());
        NbEditorSettingsInitializer.init();
    }
    
    /** Gets display name of all options node from bundle */
    public String getDisplayName(){
        return NbBundle.getMessage(AllOptionsNode.class, "OPTIONS_all"); //NOI18N
    }

    // #7925
    public boolean canDestroy() {
        return false;
    }        
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
    // #28678 Exclude Customize Bean action.
    /** Overrides superclass method, excludes the CustomizeBeanAction from the node. */
    public SystemAction[] getActions() {
        SystemAction[] as = super.getActions();
        List actions = java.util.Arrays.asList(as);
        SystemAction customizeBean = SystemAction.get(CustomizeBeanAction.class);
        if(actions.contains(customizeBean)) {
            actions = new ArrayList(actions); // to be mutable
            actions.remove(customizeBean);
            return (SystemAction[])actions.toArray(new SystemAction[0]);
        } else {
            return as;
        }
    }
    
    /** Class representing subnodes of Editor Settings node.*/
    private static class EditorSubnodes extends Children.Keys {

        /** Listens to changes on the Modules folder */
        private FileChangeListener moduleRegListener;
        
        /** Constructor.*/
        EditorSubnodes() {
            super();
        }        
        
        private void mySetKeys() {
            setKeys(AllOptionsFolder.getDefault().getInstalledOptions());
        }
        
        /** Called to notify that the children has lost all of its references to
         * its nodes associated to keys and that the keys could be cleared without
         * affecting any nodes (because nobody listens to that nodes). 
         * Overrides superclass method. */
        protected void removeNotify () {
            setKeys(new ArrayList());
        }
        
        /** Called to notify that the children has been asked for children
         * after and that they should set its keys. Overrides superclass method. */
        protected void addNotify() {
            mySetKeys();
            
            // listener
            if(moduleRegListener == null) {
                moduleRegListener = new FileChangeAdapter() {
                    public void fileChanged(FileEvent fe){
                        mySetKeys();
                    }
                };
                
                FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N
                
                if (moduleRegistry !=null){ //NOI18N
                    moduleRegistry.addFileChangeListener(
                    WeakListener.fileChange(moduleRegListener, moduleRegistry ));
                }
            }
        }
       
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            if(key == null)
                return null;

            if(!(key instanceof Class))
                return null;            
            
            BaseOptions baseOptions
            = (BaseOptions)BaseOptions.findObject((Class)key, true);
            
            if (baseOptions == null) return null;
            
            return new Node[] {baseOptions.getMimeNode()/* #18678 */.cloneNode()};
        }
        
    }
    
}
