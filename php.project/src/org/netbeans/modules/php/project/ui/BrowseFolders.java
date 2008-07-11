/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.php.project.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.SourceGroup;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;

/**
 * @author  phrebejk, mkuchtiak, Tomas Mysik
 */
public class BrowseFolders extends JPanel implements ExplorerManager.Provider {
    private static final long serialVersionUID = -180677991433020051L;

    static final Comparator<FileObject> FILE_OBJECT_COMAPARTOR = new BrowseFolders.FileObjectComparator();
    private static final JScrollPane SAMPLE_SCROLL_PANE = new JScrollPane();

    private final ExplorerManager manager;
    private final SourceGroup[] folders;
    private final Class target;
    private final BeanTreeView btv;

    public BrowseFolders(SourceGroup[] folders, Class target, String preselectedFileName) {
        initComponents();
        String description = target == DataFolder.class ? "ACSD_BrowseFolders" : "ACSD_BrowseFiles"; // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, description));

        this.folders = folders;
        this.target = target;
        manager = new ExplorerManager();
        AbstractNode rootNode = new AbstractNode(new SourceGroupsChildren(folders));
        manager.setRootContext(rootNode);

        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible(false);
        btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        btv.setBorder(SAMPLE_SCROLL_PANE.getBorder());
        expandSelection(preselectedFileName);
        folderPanel.add(btv, BorderLayout.CENTER);
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void expandSelection(String preselectedFileName) {
        Node root = manager.getRootContext();
        Children ch = root.getChildren();
        if (ch == Children.LEAF) {
            return;
        }
        Node[] nodes = ch.getNodes(true);
        Node sel = null;

        if (preselectedFileName != null && preselectedFileName.length() > 0) {
             // Try to find the node
             for (Node node : nodes) {
                try {
                    sel = NodeOp.findPath(node, preselectedFileName.split("/")); // NOI18N
                    break;
                } catch (NodeNotFoundException e) {
                    // Will select the first node
                }
             }
        }

        if (sel == null) {
            // Node not found => expand first level
            btv.expandNode(root);
            for (Node node : nodes) {
                btv.expandNode(node);
                if (sel == null) {
                    sel = node;
                }
            }
        }

        if (sel != null) {
            // Select the node
            try {
                manager.setSelectedNodes(new Node[] {sel});
            } catch (PropertyVetoException e) {
                // No selection for some reason
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        folderPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BrowseFolders.class, "LBL_Folders")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel1, gridBagConstraints);

        folderPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(folderPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel folderPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public static FileObject showDialog(SourceGroup[] folders, Class target, String preselectedFileName) {
        BrowseFolders bf = new BrowseFolders(folders, target, preselectedFileName);

        String title = target == DataFolder.class ? "LBL_SelectFolder" : "LBL_SelectFile"; // NOI18N
        JButton selectButton = new JButton(NbBundle.getMessage(BrowseFolders.class, title));
        String description = target == DataFolder.class ? "ACSD_SelectFolder" : "ACSD_SelectFile"; // NOI18N
        selectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, description));
        JButton cancelButton = new JButton(NbBundle.getMessage(BrowseFolders.class, "LBL_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrowseFolders.class, "ACSD_Cancel"));
        JButton[] options = new JButton[] {selectButton, cancelButton};

        OptionsListener optionsListener = new OptionsListener(bf, target);

        options[0].setActionCommand(OptionsListener.COMMAND_SELECT);
        options[0].addActionListener(optionsListener);
        options[1].setActionCommand(OptionsListener.COMMAND_CANCEL);
        options[1].addActionListener(optionsListener);

        title = target == DataFolder.class ? "LBL_BrowseFolders" : "LBL_BrowseFiles"; // NOI18N
        DialogDescriptor dialogDescriptor = new DialogDescriptor(bf, NbBundle.getMessage(BrowseFolders.class, title), true, options,
                options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);

        dialogDescriptor.setClosingOptions(new Object[] {options[0], options[1]});

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);

        return optionsListener.getResult();
    }

    public static FileObject showDialog(FileObject[] folders, Class target, String preselectedFileName) {
        SourceGroup[] groups = new SourceGroup[folders.length];
        int i = 0;
        for (FileObject fo : folders) {
            groups[i++] = new FOSourceGroup(fo);
        }
        return showDialog(groups, target, preselectedFileName);
    }

    /** Children to be used to show FileObjects from given SourceGroups
     */
    private final class SourceGroupsChildren extends Children.Keys<SourceGroupsChildren.Key> {

        private final SourceGroup[] groups;
        private final SourceGroup group;
        private final FileObject fo;

        public SourceGroupsChildren(SourceGroup[] groups) {
            this.groups = groups;
            fo = null;
            group = null;
        }

        public SourceGroupsChildren(FileObject fo, SourceGroup group) {
            this.fo = fo;
            this.group = group;
            groups = null;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(getKeys());
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<Key>emptySet());
            super.removeNotify();
        }

        protected Node[] createNodes(Key key) {
            FileObject fObj = key.folder;
            SourceGroup grp = key.group;
            boolean isFile = !fObj.isFolder();

            try {
                DataObject dobj = DataObject.find(fObj);
                FilterNode fn = new FilterNode(dobj.getNodeDelegate(), isFile ? Children.LEAF : new SourceGroupsChildren(fObj, grp));
                return new Node[] {fn};
            } catch (DataObjectNotFoundException e) {
                return null;
            }
        }

        private Collection<Key> getKeys() {
            List<Key> keys = null;
            if (groups != null) {
                // change because of incorrect labels
                keys = new ArrayList<Key>(groups.length);
                for (SourceGroup sg : groups) {
                    keys.add(new Key(sg.getRootFolder(), sg));
                }
            } else {
                FileObject[] children = fo.getChildren();
                Arrays.sort(children, FILE_OBJECT_COMAPARTOR);
                keys = new ArrayList<Key>(children.length);
                if (BrowseFolders.this.target == org.openide.loaders.DataFolder.class) {
                    for (FileObject file : children) {
                        if (file.isFolder() && group.contains(file)) {
                            keys.add(new Key(file, group));
                        }
                    }
                } else {
                    List<Key> dirs = new ArrayList<Key>(children.length);
                    List<Key> files = new ArrayList<Key>(children.length);
                    for (FileObject file : children) {
                        if (!group.contains(file)) {
                            continue;
                        }
                        if (file.isFolder()) {
                            dirs.add(new Key(file, group));
                        } else {
                            files.add(new Key(file, group));
                        }
                    }
                    keys.addAll(dirs);
                    keys.addAll(files);
                }
            }
            return keys;
        }

        private final class Key {
            final FileObject folder;
            final SourceGroup group;

            Key(FileObject folder, SourceGroup group) {
                this.folder = folder;
                this.group = group;
            }
        }
    }

    private static class FileObjectComparator implements Comparator<FileObject> {
        public int compare(FileObject fo1, FileObject fo2) {
            return fo1.getName().compareTo(fo2.getName());
        }
    }

    private static final class OptionsListener implements ActionListener {
        public static final String COMMAND_SELECT = "SELECT"; // NOI18N
        public static final String COMMAND_CANCEL = "CANCEL"; // NOI18N

        private final BrowseFolders browsePanel;
        private final Class target;

        private FileObject result;

        public OptionsListener(BrowseFolders browsePanel, Class target) {
            this.browsePanel = browsePanel;
            this.target = target;
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (COMMAND_SELECT.equals(command)) {
                Node[] selection = browsePanel.getExplorerManager().getSelectedNodes();

                if (selection != null && selection.length > 0) {
                    DataObject dobj = selection[0].getLookup().lookup(DataObject.class);
                    if (dobj != null && dobj.getClass().isAssignableFrom(target)) {
                        result = dobj.getPrimaryFile();
                    }
                    /*if (dobj != null) {
                        FileObject fo = dobj.getPrimaryFile();
                        if (fo.isFolder()) {
                            result = fo;
                        }
                    }*/
                }
            }
        }

        public FileObject getResult() {
            return result;
        }
    }

    private static final class FOSourceGroup implements SourceGroup {
        private final FileObject fo;

        public FOSourceGroup(FileObject fo) {
            assert fo.isFolder() : "Directory must be provided";
            this.fo = fo;
        }

        public FileObject getRootFolder() {
            return fo;
        }

        public String getName() {
            return fo.getNameExt();
        }

        public String getDisplayName() {
            return fo.getNameExt();
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return FileUtil.isParentOf(fo, file);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
