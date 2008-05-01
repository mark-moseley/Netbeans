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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.php.project;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.ui.actions.DebugLocalCommand;
import org.netbeans.modules.php.project.ui.actions.DebugSingleCommand;
import org.netbeans.modules.php.project.ui.actions.RunLocalCommand;
import org.netbeans.modules.php.project.ui.actions.RunSingleCommand;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FileSystemAction;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ads, Tomas Mysik
 */
class PhpLogicalViewProvider implements LogicalViewProvider {
    private static final Logger LOGGER = Logger.getLogger(PhpLogicalViewProvider.class.getName());
    static final Image PACKAGE_BADGE = Utilities.loadImage(
            "org/netbeans/modules/php/project/ui/resources/packageBadge.gif"); // NOI18N

    final PhpProject project;

    PhpLogicalViewProvider(PhpProject project) {
        this.project = project;
    }

    public Node createLogicalView() {
        return new PhpLogicalViewRootNode();
    }

    public Node findPath(Node root, Object target) {
        Project p = root.getLookup().lookup(Project.class);
        if (p == null) {
            return null;
        }
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                DataObject d = node.getLookup().lookup(DataObject.class);
                if (d == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject kidFO = d.getPrimaryFile();
                FileObject targetFO = null;
                if (target instanceof DataObject) {
                    targetFO = ((DataObject) target).getPrimaryFile();
                } else {
                    targetFO = (FileObject) target;
                }
                Project owner = FileOwnerQuery.getOwner(targetFO);
                if (!p.equals(owner)) {
                    return null; // Don't waste time if project does not own the fileobject
                }
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);

                    // first path without extension (more common case)
                    String[] path = relPath.split("/"); // NOI18N
                    path[path.length - 1] = targetFO.getName();

                    // first try to find the file without extension (more common case)
                    Node found = findNode(node, path);
                    if (found == null) {
                        // file not found, try to search for the name with the extension
                        path[path.length - 1] = targetFO.getNameExt();
                        found = findNode(node, path);
                    }
                    if (found == null) {
                        return null;
                    }
                    if (hasObject(found, target)) {
                        return found;
                    }
                    Node parent = found.getParentNode();
                    Children kids = parent.getChildren();
                    children = kids.getNodes();
                    for (Node child : children) {
                        if (hasObject(child, target)) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Node findNode(Node start, String[] path) {
        Node found = null;
        try {
            found = NodeOp.findPath(start, path);
        } catch (NodeNotFoundException ex) {
            // ignored
        }
        return found;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        DataObject dataObject = node.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            if (dataObject.equals(obj)) {
                return true;
            }
            FileObject fileObject = ((DataObject) obj).getPrimaryFile();
            return hasObject(node, fileObject);
        } else if (obj instanceof FileObject) {
            FileObject fileObject = dataObject.getPrimaryFile();
            return obj.equals(fileObject);
        } else {
            return false;
        }
    }

    private final class PhpLogicalViewRootNode extends AbstractNode {
        private PhpLogicalViewRootNode() {
            super(new LogicalViewChildren(), Lookups.singleton(project));
            setIconBaseWithExtension("org/netbeans/modules/php/project/ui/resources/phpProject.png"); // NOI18N
            setName(ProjectUtils.getInformation(project).getDisplayName());
        }

        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(PhpLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
        }

        @Override
        public Action[] getActions(boolean context) {
            PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
            assert provider != null;
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(provider.getAction(ActionProvider.COMMAND_RUN));
            actions.add(provider.getAction(ActionProvider.COMMAND_DEBUG));
            actions.add(null);
            actions.add(CommonProjectActions.setProjectConfigurationAction());
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            actions.add(CommonProjectActions.openSubprojectsAction());
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));

            // honor 57874 contact
            Collection<? extends Object> res = Lookups.forPath("Projects/Actions").lookupAll(Object.class); // NOI18N
            if (!res.isEmpty()) {
                actions.add(null);
                for (Object next : res) {
                    if (next instanceof Action) {
                        actions.add((Action) next);
                    } else if (next instanceof JSeparator) {
                        actions.add(null);
                    }
                }
            }
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(PhpLogicalViewProvider.class);
        }
    }

    private class LogicalViewChildren extends Children.Keys<SourceGroup> implements ChangeListener,
            PropertyChangeListener {

        private ChangeListener sourcesListener;
        private java.util.Map<SourceGroup, PropertyChangeListener> groupsListeners;
        //private HashMap<FileSystem, FileStatusListener> fileSystemListeners;

        @Override
        protected void addNotify() {
            super.addNotify();
            createNodes();
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<SourceGroup>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(SourceGroup key) {
            Node node = null;
            if (key != null) {
                DataFolder folder = getFolder(key.getRootFolder());
                if (folder != null) {
                    /* no need to use sourceGroup.getDisplayName() while we have only one sourceRoot.
                     * Now it contains not good-looking label.
                     * We put label there in PhpSources.configureSources()
                     */
                    //node = new SrcNode(folder, sourceGroup.getDisplayName());
                    node = new SrcNode(folder);
                }
            }
            return node == null ? new Node[]{} : new Node[]{node};
        }

        public void stateChanged(ChangeEvent e) {
            // #132877 - discussed with tomas zezula
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createNodes();
                }
            });
        }

        /*
         * source group change
         */
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (PhpProjectProperties.SRC_DIR.equals(property)) {
                // #132877 - discussed with tomas zezula
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createNodes();
                    }
                });
            }
        }

        void createNodes() {
            // update Sources listeners
            Sources sources = ProjectUtils.getSources(project);
            updateSourceListeners(sources);

            // parse SG
            // update SG listeners
            // XXX check if this is necessary
            final SourceGroup[] sourceGroups = Utils.getSourceGroups(project);
            updateSourceGroupsListeners(sourceGroups);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);

            List<SourceGroup> keysList = new ArrayList<SourceGroup>(groups.length);
            //Set<FileObject> roots = new HashSet<FileObject>();
            FileObject fileObject = null;
            for (int i = 0; i < groups.length; i++) {
                fileObject = groups[i].getRootFolder();
                DataFolder srcDir = getFolder(fileObject);

                if (srcDir != null) {
                    keysList.add(groups[i]);
                }
                //roots.add(fileObject);
            }
            if (keysList.size() > 0) {
                setKeys(keysList);
            }
            // Seems that we do not need to implement FileStatusListener
            // to listen to source groups root folders changes.
            // look at RubyLogicalViewRootNode for example.
            //updateSourceRootsListeners(roots);
        }

        private void updateSourceListeners(Sources sources) {
            if (sourcesListener == null) {
                sourcesListener = WeakListeners.change(this, sources);
                sources.addChangeListener(sourcesListener);
            }
        }

        private void updateSourceGroupsListeners(SourceGroup[] sourceGroups) {
            if (groupsListeners != null) {
                Iterator<SourceGroup> it = groupsListeners.keySet().iterator();
                while (it.hasNext()) {
                    SourceGroup group = it.next();
                    PropertyChangeListener pcl = groupsListeners.get(group);
                    group.removePropertyChangeListener(pcl);
                }
            }
            groupsListeners = new HashMap<SourceGroup, PropertyChangeListener>();
            for (SourceGroup group : sourceGroups) {
                PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
                groupsListeners.put(group, pcl);
                group.addPropertyChangeListener(pcl);
            }
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return null;
        }

    }

    private class SrcNode extends FilterNode {

        /**
         * creates source root node based on specified DataFolder.
         * Name is taken from bundle by 'LBL_PhpFiles' key.
         * <br/>
         * TODO : if we support several source roots, remove this constructor
         */
        SrcNode(DataFolder folder) {
            this(folder, NbBundle.getMessage(PhpLogicalViewProvider.class, "LBL_PhpFiles"));
        }

        /**
         * creates source root node based on specified DataFolder.
         * Uses specified name.
         */
        SrcNode(DataFolder folder, String name) {
            this(new FilterNode(folder.getNodeDelegate(), folder.createNodeChildren(new PhpSourcesFilter())), name);
        }

        private SrcNode(FilterNode node, String name) {
            super(node, new FolderChildren(node));
            disableDelegation(DELEGATE_GET_DISPLAY_NAME
                    | DELEGATE_SET_DISPLAY_NAME
                    | DELEGATE_GET_SHORT_DESCRIPTION
                    | DELEGATE_GET_ACTIONS);
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int type) {
            return Utilities.mergeImages(super.getIcon(type), PACKAGE_BADGE, 7, 7);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return Utilities.mergeImages(super.getOpenedIcon(type), PACKAGE_BADGE, 7, 7);
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] actions = new Action[] {
                CommonProjectActions.newFileAction(),
                null,
                SystemAction.get(FileSystemAction.class),
                null,
                SystemAction.get(FindAction.class),
                null,
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                null,
                CommonProjectActions.customizeProjectAction()
            };
            return actions;
        }
    }

    /**
     * Children for node that represents folder (SrcNode or PackageNode)
     */
    private class FolderChildren extends FilterNode.Children {

        FolderChildren(final Node originalNode) {
            super(originalNode);
        }

        @Override
        protected Node[] createNodes(Node key) {
            return super.createNodes(key);
        }


        @Override
        protected Node copyNode(final Node originalNode) {
            DataObject dobj = originalNode.getLookup().lookup(DataObject.class);
            return (dobj instanceof DataFolder)
                    ? new PackageNode(originalNode)
                    : new ObjectNode(originalNode);
        }
    }

    private final class PackageNode extends FilterNode {

        public PackageNode(final Node originalNode) {
            super(originalNode, new FolderChildren(originalNode));
        }

        @Override
        public Action[] getActions(boolean context) {
            return getOriginal().getActions(context);
        }
    }


    private final class ObjectNode extends FilterNode {
        public ObjectNode(final Node originalNode) {
            super(originalNode);
        }

        @Override
        public Action[] getActions(boolean context) {
            PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
            assert provider != null;
            List<Action> actions = new ArrayList<Action>();
            actions.addAll(Arrays.asList(getOriginal().getActions(context)));
            Action[] toAdd = new Action[] {
                null,
                provider.getAction(RunSingleCommand.ID),
                provider.getAction(DebugSingleCommand.ID),
                null,
                provider.getAction(RunLocalCommand.ID),
                provider.getAction(DebugLocalCommand.ID)
            };
            int idx = actions.indexOf(SystemAction.get(PasteAction.class));
            for (int i = 0; i < toAdd.length; i++) {
                if (idx >= 0 && idx + toAdd.length < actions.size()) {
                    //put on the proper place after paste
                    actions.add(idx + i + 1, toAdd[i]);
                } else {
                    //else put at the tail
                    actions.add(toAdd[i]);
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }
    }

    private class PhpSourcesFilter implements DataFilter {
        private static final long serialVersionUID = -7439706583318056955L;
        private final File projectXml = project.getHelper().resolveFile(AntProjectHelper.PROJECT_XML_PATH);

        public boolean acceptDataObject(DataObject object) {
                return isNotProjectFile(object) && VisibilityQuery.getDefault().isVisible(object.getPrimaryFile());
        }

        private boolean isNotProjectFile(DataObject object) {
            try {
                if (projectXml != null) {
                    File nbProject = projectXml.getParentFile().getCanonicalFile();
                    File f = FileUtil.toFile(object.getPrimaryFile()).getCanonicalFile();
                    return nbProject != null && !nbProject.equals(f);
                } else {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
    }
}
