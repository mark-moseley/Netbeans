/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.*;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.explorer.view.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

/**
 * Component that displays an explorer that displays only certain
 * nodes. Similar to the node selector (retrieved from the TopManager)
 * but arranged a bit differently, plus allows the user to set the
 * currently selected node.
 * @author Joe Warzecha
 */
public class DataObjectListView extends DataObjectPanel implements PropertyChangeListener {
    
    final static int DEFAULT_INSET = 10;
    
    private JFileChooser chooser;
    
    private File rootFile;
    
    public DataObjectListView (PropertyEditorSupport my) {
        super(my);
    }
    
    public void addNotify() {
        completeInitialization();
        super.addNotify();
    }
    
    /** Called from addNotify. */
    private void completeInitialization() {
        if (insets != null) {
            setBorder(new EmptyBorder(insets));
        } else {
            setBorder(new EmptyBorder(12, 12, 0, 11));
        }
        setLayout(new BorderLayout(0, 2));
        
        //TODO Is it possible to set any label for JFileChooser?
        /*if (subTitle != null) {
            JLabel l = new JLabel(subTitle);
            l.setLabelFor(reposTree);
            add(l, BorderLayout.NORTH);
        }*/
        
        if (rootNode == null) {
            if (dataFilter != null) {
                if (folderFilter != null) {
                    DataFilter dFilter = new DataFilter() {
                        public boolean acceptDataObject(DataObject obj) {
                            if (folderFilter.acceptDataObject(obj)) {
                                return true;
                            }
                            return dataFilter.acceptDataObject(obj);
                        }
                    };
                    rootNode = RepositoryNodeFactory.getDefault().repository(dFilter);
                } else {
                    rootNode = RepositoryNodeFactory.getDefault().repository(dataFilter);
                }
            } else {
                if (folderFilter != null) {
                    rootNode = RepositoryNodeFactory.getDefault().repository(folderFilter);
                } else {
                    rootNode = RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);
                }
            }
        }

        if (nodeFilter != null) {
            FilteredChildren children = 
                new FilteredChildren(rootNode, nodeFilter, dataFilter);
            FilterNode n = new FilterNode(rootNode, children);
            rootNode = n;
        }
        
        Node rNode = rootNode;
        if (rootObject != null) {
            Node n = findNodeForObj(rootNode, rootObject);
            if (n != null) {
                NodeAcceptor naccep = nodeFilter;
                if (naccep == null) {
                    naccep = new NodeAcceptor() {
                        public boolean acceptNodes(Node [] nodes) {
                            return false;
                        }
                    };
                }
                FilteredChildren children =
                    new FilteredChildren(n, naccep, dataFilter);
                FilterNode filtNode = new FilterNode(n, children);
                rNode = filtNode;
            }
        }
        
        rootFile = new NodeFile(getFileName(rNode), rNode);
        
        //Create instance AFTER root file is set!!!
        chooser = new NodeFileChooser(rootFile, new NodeFileSystemView());
        FileEditor.hackFileChooser(chooser);
        //We must initialize it after JFileChooser is created
        chooser.getAccessibleContext().setAccessibleName
        (NbBundle.getBundle (DataObjectPanel.class).getString ( "ACSN_DataObjectPanel" ));
        setDescription( NbBundle.getBundle (DataObjectPanel.class).getString ( "ACSD_DataObjectPanel" ) );
        
        chooser.setFileView(new NodeFileView());
        
        chooser.setControlButtonsAreShown(false);
        chooser.setMultiSelectionEnabled(multiSelection);
        chooser.addPropertyChangeListener(this);
        
        //TODO set initial selection
        
        add(chooser, BorderLayout.CENTER);

        if (multiSelection) {
            DataObject [] dObjArr = getDataObjects();
            if ((dataFilter != null) && (dObjArr != null)) {
                boolean b = false;
                for (int i = 0; i < dObjArr.length; i++) {
                    if (dataFilter.acceptDataObject(dObjArr[i])) {
                        b = true;
                        break;
                    }
                }
                setOkButtonEnabled(b);
            } else {
                setOkButtonEnabled(dObjArr != null);
            }
        } else {
            if ((dataFilter != null) && (getDataObject() != null)) {
                setOkButtonEnabled(
                    dataFilter.acceptDataObject(getDataObject())); 
            } else {
                setOkButtonEnabled(getDataObject() != null);
            }
        }
    }
    
    /**
     * Tries to retrieve unique file name from Node -> DataObject -> PrimaryFile
     * if possible. Used to set file name for NodeFile representing node in JFileChooser.
     */
    private static String getFileName (Node n) {
        DataObject dObj = (DataObject) n.getCookie(DataObject.class);
        if (dObj != null) {
            FileObject pf = dObj.getPrimaryFile();
            if (pf.isRoot()) {
                return n.getDisplayName();
            } else {
                return pf.getNameExt();
            }
        } else {
            return n.getDisplayName();
        }
    }
    
    /**
     * Sets description of the panel.
     *
     * @param desc Desciption of the panel.
     */
    public void setDescription(String desc) {
        getAccessibleContext().setAccessibleDescription(desc);
        //reposTree.getAccessibleContext().setAccessibleDescription(desc);
        chooser.getAccessibleContext().setAccessibleDescription(desc);
    }
    
    /** Finds node by path from root node. It can return null if node is not found.
     */
    private Node findNode (String path) {
        //Find node corresponding to given path
        Node n = rootNode;
        String p = path;
        String fileName;
        int ind = p.indexOf('/');
        if (ind != -1) {
            fileName = p.substring(0, ind);
            p = p.substring(ind + 1);
        } else {
            fileName = p;
        }
        fileName = fileName.replace('#','/');
        
        //Root node must correspond to root file
        /*if (!fileName.equals(n.getDisplayName())) {
            System.out.println("########### ERROR folder name and node display name does not match #########");
            System.out.println("fileName:" + fileName
            + " nodeName:" + n.getDisplayName());
        }*/

        while (ind != -1) {
            Node [] nodes = n.getChildren().getNodes(true);
            ind = p.indexOf('/');
            if (ind != -1) {
                fileName = p.substring(0, ind);
                p = p.substring(ind + 1);
            } else {
                fileName = p;
            }
            fileName = fileName.replace('#','/');
            //Find node with the same name
            for (int i = 0; i < nodes.length; i++) {
                if (fileName.equals(getFileName(nodes[i]))) {
                    n = nodes[i];
                    break;
                }
            }
        }
        //Check if node path corresponds to parameter path.
        /*Node backNode = n;
        StringBuffer nodePath = new StringBuffer(100);
        nodePath.append(backNode.getDisplayName().replace('/','#'));
        backNode = backNode.getParentNode();
        while (backNode != null) {
            nodePath.insert(0, "/");
            nodePath.insert(0, backNode.getDisplayName().replace('/','#'));
            backNode = backNode.getParentNode();
        }
        if (!path.equals(nodePath.toString())) {
            System.out.println("#######################################");
            System.out.println("ERROR PATH IS NOT EQUAL TO NODE PATH");
            System.out.println("#######################################");
        }*/
        
        //Check if node was found
        if (!fileName.equals(getFileName(n))) {
            return null;
        }
        return n;
    }
    
    /** Creates node by path from root node. It either returns existing node as
     * findNode does or creates instance of FakeNode. Created node is NOT added
     * to node hierarchy.
     */
    private Node createNode (String path) {
        //Find node corresponding to given path
        Node n = rootNode;
        Node parent = null;
        String p = path;
        String fileName;
        int ind = p.indexOf('/');
        if (ind != -1) {
            fileName = p.substring(0, ind);
            p = p.substring(ind + 1);
        } else {
            fileName = p;
        }
        fileName = fileName.replace('#','/');
        
        //Root node must correspond to root file
        /*if (!fileName.equals(n.getDisplayName())) {
            System.out.println("########### ERROR folder name and node display name does not match #########");
            System.out.println("fileName:" + fileName
            + " nodeName:" + n.getDisplayName());
        }*/

        while (ind != -1) {
            Node [] nodes = n.getChildren().getNodes(true);
            parent = n;
            ind = p.indexOf('/');
            if (ind != -1) {
                fileName = p.substring(0, ind);
                p = p.substring(ind + 1);
            } else {
                fileName = p;
            }
            fileName = fileName.replace('#','/');
            //Find node with the same name
            for (int i = 0; i < nodes.length; i++) {
                if (fileName.equals(getFileName(nodes[i]))) {
                    n = nodes[i];
                    break;
                }
            }
        }
        
        if (!fileName.equals(getFileName(n))) {
            //Create new node
            n = new FakeNode(Children.LEAF);
            n.setDisplayName(fileName.replace('#','/'));
        }
        
        /*if (parent != null) {
            System.out.println("ADD NODE TO PARENT");
            parent.getChildren().add(new Node [] { n });
        }*/
        
        //Check if node path corresponds to parameter path.
        /*Node backNode = n;
        StringBuffer nodePath = new StringBuffer(100);
        nodePath.append(backNode.getDisplayName().replace('/','#'));
        backNode = backNode.getParentNode();
        while (backNode != null) {
            nodePath.insert(0, "/");
            nodePath.insert(0, backNode.getDisplayName().replace('/','#'));
            backNode = backNode.getParentNode();
        }
        if (!path.equals(nodePath.toString())) {
            System.out.println("#######################################");
            System.out.println("ERROR PATH IS NOT EQUAL TO NODE PATH");
            System.out.println("#######################################");
        }*/
        return n;
    }
    
    /**
     * Return the currently selected DataObject. 
     * @return The currently selected DataObject or null if there is no node seleted
     */
    public DataObject getDataObject() {
        DataObject retValue = null;
        if (!multiSelection) {
            File f = chooser.getSelectedFile();
            if (f instanceof NodeFile) {
                Node n = ((NodeFile) f).getNode();
                if (n != null) {
                    retValue = (DataObject) n.getCookie(DataObject.class);
                }
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node getNode() {
        Node retValue = null;
        if (!multiSelection) {
            File f = chooser.getSelectedFile();
            if (f instanceof NodeFile) {
                retValue = ((NodeFile) f).getNode();
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected DataObject. 
     * @return The currently selected DataObject or null if there is no node seleted
     */
    public DataObject [] getDataObjects () {
        DataObject [] retValue = null;
        if (multiSelection) {
            File [] f = chooser.getSelectedFiles();
            retValue = new DataObject [f.length];
            for (int i = 0; i < f.length; i++) {
                if (f[i] instanceof NodeFile) {
                    Node n = ((NodeFile) f[i]).getNode();
                    if (n != null) {
                        retValue[i] = (DataObject) n.getCookie(DataObject.class);
                    }
                }
            }
        }
        return retValue;
    }
    
    /**
     * Return the currently selected Node. 
     * @return The currently selected Node or null if there is no node seleted
     */
    public Node [] getNodes () {
        Node [] retValue = null;
        if (multiSelection) {
            File [] f = chooser.getSelectedFiles();
            retValue = new Node [f.length];
            for (int i = 0; i < f.length; i++) {
                if (f[i] instanceof NodeFile) {
                    retValue[i] = ((NodeFile) f[i]).getNode();
                }
            }
        }
        return retValue;
    }
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        if (multiSelection) {
            return getDataObjects();
        } else {
            return getDataObject();
        }
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName()) ||
            JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            File[] selFiles = (File[]) chooser.getSelectedFiles();
            if (selFiles == null) {
                return;
            }

            if ((selFiles.length == 0) && (chooser.getSelectedFile() != null)) {
                selFiles = new File[] { chooser.getSelectedFile() };
            }
                        
            Node [] nodes = new Node [selFiles.length];
            for (int i = 0; i < selFiles.length; i++) {
                if (selFiles[i] instanceof NodeFile) {
                    //Get node directly
                    nodes[i] = ((NodeFile) selFiles[i]).getNode();
                } else {
                    //Try to find node by path
                    nodes[i] = findNode(selFiles[i].getPath());
                }
            }
            
            ArrayList dObjList = new ArrayList(selFiles.length);
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    DataObject dObj = (DataObject) nodes[i].getCookie(DataObject.class);
                    if (dObj != null) {
                        if (dataFilter != null) {
                            if (dataFilter.acceptDataObject(dObj)) {
                                dObjList.add(dObj);
                            }
                        } else {
                            dObjList.add(dObj);
                        }
                    }
                }
            }
            
            DataObject [] dObjArray = (DataObject []) dObjList.toArray(new DataObject[dObjList.size()]);
            
            boolean enableOK = false;
            if (dObjArray.length > 0) {
                enableOK = true;
            } else {
                enableOK = false;
            }
            if (enableOK) {
                if (multiSelection) {
                    myEditor.setValue(dObjArray);
                } else {
                    myEditor.setValue(dObjArray[0]);
                }
            }
            setOkButtonEnabled(enableOK);
        } else if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
        }
    }
    
    /** Fake node used to create NodeFile for nonexisting node. JFileChooser calls
     * FileSystemView.createFileObject() when renaming existing file.
     */
    private static class FakeNode extends AbstractNode {
        
        public FakeNode (Children children) {
            super(children);
        }
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFile extends File {
        private Node n;
        
        NodeFile (String path, Node n) {
            super(path);
            this.n = n;
        }
        
        NodeFile (File parent, String child, Node n) {
            super(parent,child);
            this.n = n;
        }
        
        public boolean canRead() {
            return true;
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public boolean renameTo (File dest) {
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                try {
                    dObj.rename(dest.getName());
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        
        public File[] listFiles () {
            Node [] nodes = n.getChildren().getNodes(true);
            NodeFile [] files = new NodeFile[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                String name = getFileName(nodes[i]);
                name = name.replace('/','#');
                files[i] = new NodeFile(getPath() + "/" + name, nodes[i]); // NOI18N
            }
            return files;
        }
        
        public String getName () {
            if (n != null) {
                return n.getDisplayName();
            } else {
                return super.getName();
            }
        }
        
        public String getParent () {
            String p = super.getParent();
            return p;
        }
        
        public File getParentFile () {
            String p = this.getParent();
            if (p == null) {
                return null;
            }
            if (n == null) {
                return null;
            }
            Node parent = n.getParentNode();
            if (parent == null) {
                return null;
            }
            return new NodeFile(p, parent);
        }
        
        public boolean exists () {
            Node n = findNode(getPath());
            if (n != null) {
                if (n instanceof FakeNode) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        
        public boolean isAbsolute () {
            String p = getPath();
            int ind = p.indexOf('/');
            if (ind != -1) {
                //Get root of path
                p = p.substring(0, ind);
            }
            p = p.replace('#','/');
            if (p.equals(getFileName(rootNode))) {
                return true;
            } else {
                return false;
            }
        }
        
        public boolean isDirectory () {
            if (n == null) {
                return false;
            }
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                if (dObj instanceof DataFolder) {
                    return true;
                } else {
                    return false;
                }
            } else {
                //Always root??
                return true;
            }
        }
        
        public boolean isFile () {
            if (n == null) {
                return true;
            }
            DataObject dObj = (DataObject) n.getCookie(DataObject.class);
            if (dObj != null) {
                if (dObj instanceof DataFolder) {
                    return false;
                } else {
                    return true;
                }
            } else {
                //Always root??
                return false;
            }
        }
        
        public Icon getIcon () {
            Icon icon = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            return icon;
        }
        
        public String getAbsolutePath() {
            return getPath();
        }
        
        public File getAbsoluteFile() {
            return new NodeFile(getAbsolutePath(), n);
        }
        
        public String getCanonicalPath() throws IOException {
            return getPath();
        }
        
        public File getCanonicalFile() throws IOException {
            return new NodeFile(getCanonicalPath(), n);
        }
        
        public Node getNode () {
            return n;
        }
        
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFileView extends FileView {
        
        NodeFileView () {
            super();
        }
        
        public String getName (File f) {
            if (f instanceof NodeFile) {
                return f.getName();
            } else {
                //Try to locate corresponding node by path
                Node n = findNode(f.getPath());
                if (n != null) {
                    return n.getDisplayName();
                } else {
                    return null;
                }
            }
        }
        
        public Icon getIcon (File f) {
            if (f instanceof NodeFile) {
                return ((NodeFile) f).getIcon();
            } else {
                //Try to locate corresponding node by path
                Node n = findNode(f.getPath());
                if (n != null) {
                    Icon icon = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
                    return icon;
                } else {
                    return null;
                }
            }
        }
    }
    
    /** Used by JFileChooser to display File instances from our fake
     * file system representing node hierarchy.
     */
    private class NodeFileSystemView extends FileSystemView {
        private final String newFolderString =
                UIManager.getString("FileChooser.other.newFolder"); // NOI18N
        
        NodeFileSystemView () {
            super();
        }
        
        /**
         * Determines if the given file is a root partition or drive.
         */
        public boolean isRoot(File f) {
            return rootFile.equals(f);
        }
        
        /** Creates a new folder with a default folder name.
         *
         */
        public File createNewFolder(File containingDir) throws IOException {
            String path = containingDir.getPath() + "/" + newFolderString; // NOI18N
            Node n = findNode(path);
            if (n != null) {
                NodeFile folder = new NodeFile(path, n);
                return folder;
            } else {
                Node parent = findNode(containingDir.getPath());
                if (parent == null) {
                    return null;
                }
                DataObject dObj = (DataObject) parent.getCookie(DataObject.class);
                if (dObj != null) {
                    if (dObj instanceof DataFolder) {
                        DataFolder.create((DataFolder) dObj, newFolderString);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
                n = createNode(path);
                NodeFile folder = new NodeFile(path, n);
                return folder;
            }
        }
        
        public File createFileObject(File dir, String filename) {
            filename = filename.replace('/','#');
            //Find node corresponding to given path
            String path = dir.getPath() + "/" + filename; // NOI18N
            Node n = findNode(path);
            if (n == null) {
                n = createNode(path);
            }
            NodeFile file = new NodeFile(path, n);
            return file;
        }

        /**
         * Returns a File object constructed from the given path string.
         */
        public File createFileObject(String path) {
            //Find node corresponding to given path
            Node n = findNode(path);
            if (n == null) {
                n = createNode(path);
            }
            NodeFile file = new NodeFile(path, n);
            return file;
        }
        
        /**
         * Returns whether a file is hidden or not.
         */
        public boolean isHiddenFile(File f) {
            return false;
        }
        
        /**
         * Returns all root partitians on this system. For example, on Windows,
         * this would be the A: through Z: drives.
         */
        public File[] getRoots() {
            return new NodeFile [] { (NodeFile) rootFile };
        }
        
        public File getHomeDirectory() {
            return rootFile;
        }
        
        public File[] getFiles (File dir, boolean useFileHiding) {
            if (dir instanceof NodeFile) {
                return dir.listFiles();
            } else {
                return super.getFiles(dir, useFileHiding);
            }
        }
        
        public File getParentDirectory (File dir) {
            if (dir != null) {
                File f = createFileObject(dir.getPath());
                File parent = f.getParentFile();
                return parent;
            }
            return null;
        }
        
        public String getSystemDisplayName (File f) {
            return f.getName();
        }
        
    }
    
    private class NodeL implements NodeListener {
        
        /** Fired when a set of new children is added.
         * @param ev event describing the action
         *
         */
        public void childrenAdded(NodeMemberEvent ev) {
        }
        
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         *
         */
        public void childrenRemoved(NodeMemberEvent ev) {
        }
        
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         *
         */
        public void childrenReordered(NodeReorderEvent ev) {
        }
        
        /** Fired when the node is deleted.
         * @param ev event describing the node
         *
         */
        public void nodeDestroyed(NodeEvent ev) {
        }
        
        /** This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         *
         */
        public void propertyChange(PropertyChangeEvent evt) {
        }
        
    }
    
    /** Extended JFileChooser. We have to overwrite some methods because
     * UI implementation creates its own instances of java.io.File -> it causes
     * trouble with our fake filesystem for example java.io.File.exists() returns
     * false when our fake path is provided. */
    public class NodeFileChooser extends JFileChooser {
        
        NodeFileChooser (File currentDirectory, FileSystemView fsv) {
            super(currentDirectory, fsv);
        }
        
        public void setCurrentDirectory (File dir) {
            if ((DataObjectListView.this != null) && (dir != null) && !(dir instanceof NodeFile)) {
                Node n = findNode(dir.getPath());
                dir = new NodeFile(dir.getPath(), n);
            }
            super.setCurrentDirectory(dir);
        }
        
    }
}
