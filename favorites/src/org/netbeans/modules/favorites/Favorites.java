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

package org.netbeans.modules.favorites;

import java.io.File;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataShadow;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author  Jaroslav Tulach
 */
final class Favorites extends FilterNode {
    /** default node */
    private static Node node;
    /** node that represents root of filesystems */
    private static Node root;

    /** Creates new ProjectRootFilterNode. */
    private Favorites(Node node) {
        super(node, new Chldrn (node));
    }
    
    public static org.openide.loaders.DataFolder getFolder () {
        try {
            org.openide.filesystems.FileObject fo = org.openide.filesystems.FileUtil.createFolder (
                org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot(), 
                "Favorites" // NOI18N
            );
            org.openide.loaders.DataFolder folder = org.openide.loaders.DataFolder.findFolder(fo);
            return folder;
        } catch (java.io.IOException ex) {
            org.openide.ErrorManager.getDefault().notify (ex);
            return org.openide.loaders.DataFolder.findFolder (
                org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot()
            );
        }
        
    }
    
    /** Getter for default filter node.
     */
    public static synchronized Node getNode () {
        if (node == null) {
            node = new Favorites (getFolder().getNodeDelegate ());
        }
        return node;
    }
    
    /** Getter root node.
     */
    public static synchronized Node getRoot () {
        if (root == null) {
            root = org.openide.loaders.RepositoryNodeFactory.getDefault().repository (
                org.openide.loaders.DataFilter.ALL
            );
        }
        return root;
    }
    
    /** Get name of home directory. Used from layer.
     */
    public static java.net.URL getHome () 
    throws org.openide.filesystems.FileStateInvalidException, java.net.MalformedURLException {
        ensureShadowsWork (null);
        
        String s = System.getProperty("user.home"); // NOI18N
        
        File home = new File (s);
        home = FileUtil.normalizeFile (home);
        
        return home.toURI ().toURL ();
    }
    
    static void ensureShadowsWork (FileObject fo) throws org.openide.filesystems.FileStateInvalidException {
        /*
        if (fo == null) {
            File r = new File (System.getProperty("user.home"));
            fo = FileUtil.fromFile (r)[0];
        }
        
        // make sure the filesystem is in repository otherwise
        // the shadows will not work, workaround for issue 42690
        org.openide.filesystems.Repository.getDefault().addFileSystem(fo.getFileSystem());
         */
    }

    /** Finds file for a given node 
     */
    static File fileForNode (Node n) {
        DataObject obj = (DataObject)n.getCookie (DataObject.class);
        if (obj == null) return null;
        
        return org.openide.filesystems.FileUtil.toFile (
            obj.getPrimaryFile()
        );
    }

    public Handle getHandle () {
        return new RootHandle ();
    }

    private static class RootHandle implements Node.Handle {
        static final long serialVersionUID = 1907300072945111595L;

        /** Return a node for the current project.
        */
        public Node getNode () {
            return Favorites.getNode ();
        }
    }

    private static class Chldrn extends FilterNode.Children {
        /** Creates new Chldrn. */
        public Chldrn (Node node) {
            super (node);
        }
        
        protected Node[] createNodes(Object key) {
            // strange equals statement due to
            // bug #28198
            boolean e = Favorites.getRoot ().equals (key)
                || key.equals (Favorites.getRoot ());
            
            if (e) {
                // list all roots
                File[] roots = File.listRoots();
                java.util.ArrayList list = new java.util.ArrayList ();
                for (int i = 0; i < roots.length; i++) {
                    FileObject r = org.openide.filesystems.FileUtil.toFileObject (roots[i]);
                    try {
                        DataObject obj = DataObject.find (r);
                        list.add (
                            new ProjectFilterNode (obj.getNodeDelegate(), new Chldrn (obj.getNodeDelegate ()))
                        );
                    } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                        org.openide.ErrorManager.getDefault ().notify (ex);
                    }
                }
                return (Node[])list.toArray(new Node[0]);
            }
            
            Node node = (Node)key;
            return new Node[] { new ProjectFilterNode (
                node,
                (node.isLeaf ()) ? org.openide.nodes.Children.LEAF : new Chldrn (node)
            )};
        }
        
    }

    /** This FilterNode is sensitive to 'Delete Original Files' property of {@link ProjectOption}.
     * When this property is true then original DataObjects pointed to by links under the project's node
     * are deleted as the Delete is performed on the link's node.
     */
    private static class ProjectFilterNode extends FilterNode {

        /** Creates new ProjectFilterNode. */
        public ProjectFilterNode (Node node, org.openide.nodes.Children children) {
            super (node, children);
        }
        
        public String getDisplayName () {
            String s = super.getDisplayName ();
            for (;;) {
                int indx = s.indexOf("(->)"); // NOI18N
                if (indx == -1) return s;
                
                s = s.substring(0, indx) + s.substring (indx + 4);
            }
        }

        public boolean canDestroy () {
            boolean canDestroy = super.canDestroy ();
            DataShadow link = (DataShadow) getCookie (DataShadow.class);

            // if the DO of this node can be destroyed and the original DO should be destroyed too
            // ask the original if it's allowed to delete it
            if (canDestroy && isDeleteOriginal (link)) {
                canDestroy = link.getOriginal ().isDeleteAllowed ();
            }

            return canDestroy;
        }

        public void destroy () throws java.io.IOException {
            if (canDestroy ()) {
                DataShadow link = (DataShadow) getCookie (DataShadow.class);
                DataObject original = isDeleteOriginal (link) ? link.getOriginal () : null;

                super.destroy ();

                if (original != null) {
                    original.delete ();
                }
            }
        }

        private boolean isDeleteOriginal (DataShadow link) {
            return false;
        }
        
        public javax.swing.Action[] getActions(boolean context) {
            javax.swing.Action[] arr;
            arr = super.getActions(context);
            
            boolean added = false;
            java.util.ArrayList newArr = new java.util.ArrayList (arr.length + 3);
            for (int i = 0; i < arr.length; i++) {
                if (!added && (
                    (arr[i] instanceof org.openide.actions.CopyAction)  
                    ||
                    (arr[i] instanceof org.openide.actions.CutAction)  
                )) {
                    added = true;
                    newArr.add (Actions.add ());
                    newArr.add (Actions.remove ());
                    newArr.add (null);
                }
                newArr.add (arr[i]);
            }
            
            return (javax.swing.Action[])newArr.toArray (arr);
        }
        
    }
}
