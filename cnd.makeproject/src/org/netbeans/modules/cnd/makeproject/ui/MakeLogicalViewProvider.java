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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingFolderItemsAction;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingItemAction;
import org.netbeans.modules.cnd.makeproject.api.actions.NewFolderAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.ui.BrokenIncludes;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProviders;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openidex.search.SearchInfo;

/**
 * Support for creating logical views.
 */
public class MakeLogicalViewProvider implements LogicalViewProvider {

    private final MakeProject project;
    private final SubprojectProvider spp;
    private static final Boolean ASYNC_ROOT_NODE = Boolean.getBoolean("cnd.async.root");// NOI18N
    private static final Logger log = Logger.getLogger("cnd.async.root");// NOI18N
    private static final MessageFormat ITEM_VIEW_FLAVOR = new MessageFormat("application/x-org-netbeans-modules-cnd-makeproject-uidnd; class=org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider$ViewItemNode; mask={0}"); // NOI18N
    static final String PRIMARY_TYPE = "application"; // NOI18N
    static final String SUBTYPE = "x-org-netbeans-modules-cnd-makeproject-uidnd"; // NOI18N
    static final String MASK = "mask"; // NOI18N
    static StandardNodeAction renameAction = null;
    static StandardNodeAction deleteAction = null;

    public MakeLogicalViewProvider(MakeProject project, SubprojectProvider spp) {
        this.project = project;
        assert project != null;
        this.spp = spp;
        assert spp != null;
    }

    public Node createLogicalView() {
        MakeConfigurationDescriptor configurationDescriptor = getMakeConfigurationDescriptor();
        if (ASYNC_ROOT_NODE) {
            log.fine("creating async root node in EDT? " + SwingUtilities.isEventDispatchThread());// NOI18N
            return new MakeLogicalViewRootNode(configurationDescriptor.getLogicalFolders());
        } else {
            if (configurationDescriptor == null || configurationDescriptor.getState() == State.BROKEN || configurationDescriptor.getConfs().size() == 0) {
                return new MakeLogicalViewRootNodeBroken();
            } else {
                return new MakeLogicalViewRootNode(configurationDescriptor.getLogicalFolders());
            }
        }
    }
    private boolean findPathMode = false;

    private boolean isFindPathMode() {
        return findPathMode;
    }

    public org.openide.nodes.Node findPath(Node root, Object target) {
        Node returnNode = null;
        Project rootProject = root.getLookup().lookup(Project.class);
        if (rootProject == null) {
            return null;
        }

        if (target instanceof DataObject) {
            target = ((DataObject) target).getPrimaryFile();
        }

        if (!(target instanceof FileObject)) {
            return null;
        }

        // FIXUP: this doesn't work with file groups (jl: is this still true?)
        File file = FileUtil.toFile((FileObject) target);
        if (!gotMakeConfigurationDescriptor() || file == null) {
            // IZ 111884 NPE while creating a web project
            return null;
        }
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        Item item = makeConfigurationDescriptor.findProjectItemByPath(file.getAbsolutePath());

        if (item == null) {
            item = makeConfigurationDescriptor.findExternalItemByPath(file.getAbsolutePath());
            if (item == null) {
                // try to find any item
                item = makeConfigurationDescriptor.findItemByFile(file);
                if (item == null) {
                    //not found:
                    return null;
                }
            }
        }

        try {
            findPathMode = true;
            // FIXUP: assume nde node is last node in current folder. Is this always true?
            // Find the node and return it
            Node folderNode = findFolderNode(root, item.getFolder());
            if (folderNode != null) {
                Node[] nodes = folderNode.getChildren().getNodes(true);
                int index = 0;
                for (index = 0; index < nodes.length; index++) {
                    Item nodeItem = (Item) nodes[index].getValue("Item"); // NOI18N
                    if (nodeItem == item) {
                        break;
                    }
                }
                if (nodes.length > 0 && index < nodes.length) {
                    returnNode = nodes[index];
                }
            /*
            if (nodes.length > 0)
            returnNode = nodes[nodes.length -1];
             */
            }
        } finally {
            findPathMode = false;
        }
        return returnNode;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'folder'
     */
    private static Node findFolderNode(Node root, Folder folder) {
        if (root.getValue("Folder") == folder) // NOI18N
        {
            return root;
        }
        Folder parent = folder.getParent();

        if (parent == null) {
            return root;
        }

        Node parentNode = findFolderNode(root, parent);

        if (parentNode == null) {
            return null;
        }

        Node[] nodes = parentNode.getChildren().getNodes(true);
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getValue("Folder") == folder) // NOI18N
            {
                return nodes[i];
            }
        }
        return null;
    }

    /*
     * Recursive method to find the node in the tree with root 'root'
     * that is representing 'item'
     */
    private static Node findItemNode(Node root, Item item) {
        Node parentNode = findFolderNode(root, item.getFolder());
        if (parentNode != null) {
            Node[] nodes = parentNode.getChildren().getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getValue("Item") == item) // NOI18N
                {
                    return nodes[i];
                }
            }
        }
        return null;
    }

    /**
     * HACK: set the folder node visible in the project explorer
     * See IZ7551
     */
    public static void setVisible(Project project, Folder folder) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        Node projectRoot = findProjectNode(rootNode, project);

        if (projectRoot == null) {
            return;
        }

        Node folderNode = findFolderNode(projectRoot, folder);
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(new Node[]{folderNode});
        } catch (Exception e) {
            // skip
        }
    }

    /**
     * HACK: set the folder node visible in the project explorer
     * See IZ7551
     */
    public static void setVisible(Project project, Item item) {
        setVisible(project, new Item[]{item});
    }

    public static void setVisible(Project project, Item[] items) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < items.length; i++) {
            Node root = findProjectNode(rootNode, project);

            if (root != null) {
                nodes.add(findItemNode(root, items[i]));
            }
        }
        try {
            ProjectTabBridge.getInstance().getExplorerManager().setSelectedNodes(nodes.toArray(new Node[0]));
        } catch (Exception e) {
            // skip
        }
    }

    public static void checkForChangedName(final Project project) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                Node root = findProjectNode(rootNode, project);
                if (root != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(project);
                    if (pi != null) { // node will check whether it equals...
                        root.setDisplayName(pi.getDisplayName());
                    }
                }
            }
        });
    }

    public static void checkForChangedItems(final Project project, final Folder folder, final Item item) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (item == null) {
                    checkForChangedItems(project);
                    return;
                }
                Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
                Node root = findProjectNode(rootNode, project);
                if (root != null) {
                    Node node = findItemNode(root, item);
                    if (node instanceof FilterNode) {
                        Object o = node.getLookup().lookup(ViewItemNode.class);
                        if (o != null) {
                            ((ChangeListener) o).stateChanged(null);
                        }
                    }
                }
            }
        });
    }

    private static void checkForChangedItems(Project project) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        checkForChangedItems(findProjectNode(rootNode, project));
    }

    private static void checkForChangedItems(Node root) {
        if (root != null) {
            for (Node node : root.getChildren().getNodes(true)) {
                checkForChangedItems(node);
                if (node instanceof FilterNode) {
                    Object o = node.getLookup().lookup(ViewItemNode.class);
                    if (o != null) {
                        ((ChangeListener) o).stateChanged(null);
                    }
                }
            }
        }
    }

    public static void refreshBrokenItems(final Project project) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                refreshBrokenItemsImpl(project);
            }
        });
    }

    private static void refreshBrokenItemsImpl(Project project) {
        Node rootNode = ProjectTabBridge.getInstance().getExplorerManager().getRootContext();
        refreshBrokenItemsImpl(findProjectNode(rootNode, project));
    }

    private static void refreshBrokenItemsImpl(Node root) {
        if (root != null) {
            if (root.isLeaf()) {
                Object o = root.getLookup().lookup(BrokenViewItemNode.class);
                if (o != null) {
                    ((BrokenViewItemNode) o).refresh();
                }
            } else {
                for (Node node : root.getChildren().getNodes(true)) {
                    refreshBrokenItemsImpl(node);
                }
            }
        }
    }

    private static Node findProjectNode(Node root, Project p) {
        Node[] n = root.getChildren().getNodes(true);
        Template<Project> t = new Template<Project>(null, null, p);

        for (int cntr = 0; cntr < n.length; cntr++) {
            if (n[cntr].getLookup().lookupItem(t) != null) {
                return n[cntr];
            }
        }

        return null;
    }

    // Private innerclasses ----------------------------------------------------
    public static boolean hasBrokenLinks() {
        return false;
    }
    private static final String brokenProjectBadgePath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif"; // NOI18N
    private static final String brokenIncludeImgPath = "org/netbeans/modules/cnd/makeproject/ui/resources/brokenIncludeBadge.png"; // NOI18N
    private static final Image brokenProjectBadge = loadToolTipImage(brokenProjectBadgePath, "BrokenProjectTxt"); // NOI18N
    private static final Image brokenIncludeBadge = loadToolTipImage(brokenIncludeImgPath, "BrokenIncludeTxt"); // NOI18N

    private static Image loadToolTipImage(String imgResouce, String textResource) {
        Image img = ImageUtilities.loadImage(imgResouce);
        img = ImageUtilities.assignToolTipToImage(img,
                "<img src=\"" + MakeLogicalViewRootNode.class.getClassLoader().getResource(imgResouce) + "\">&nbsp;" // NOI18N
                + NbBundle.getMessage(MakeLogicalViewRootNode.class, textResource));
        return img;
    }

    private static Node getWaitNode() {
        return new LoadingNode();
    }

    private String getShortDescription() {
        String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
        DevelopmentHostConfiguration devHost = project.getDevelopmentHostConfiguration();
        if (devHost == null || devHost.isLocalhost()) {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node", prjDirDispName); // NOI18N
        } else {
            return NbBundle.getMessage(MakeLogicalViewProvider.class,
                    "HINT_project_root_node_on_host", prjDirDispName, devHost.getDisplayName(true)); // NOI18N
        }
    }

    private static class LoadingNode extends AbstractNode {

        public LoadingNode() {
            super(Children.LEAF);
            setName("dummy"); // NOI18N
            setDisplayName(NbBundle.getMessage(MakeLogicalViewProvider.class, "Tree_Loading")); // NOI18N
        }

        @Override
        public Image getIcon(int param) {
            //System.err.println("get icon asked");
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/waitNode.gif"); // NOI18N
        }
    }

    /** Filter node containin additional features for the Make physical
     */
    private final class MakeLogicalViewRootNode extends AnnotatedNode implements ChangeListener, LookupListener {

        private boolean brokenLinks;
        private boolean brokenIncludes;
        private Folder folder;
        private final Lookup.Result<BrokenIncludes> brokenIncludesResult;

        public MakeLogicalViewRootNode(Folder folder) {
            super(new LogicalViewChildren(folder), Lookups.fixed(new Object[]{
                        folder,
                        project,
                        new FolderSearchInfo(folder),}));
            this.folder = folder;
            setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
            setName(ProjectUtils.getInformation(project).getDisplayName());

            brokenIncludesResult = Lookup.getDefault().lookup(new Lookup.Template<BrokenIncludes>(BrokenIncludes.class));
            brokenIncludesResult.addLookupListener(this);
            resultChanged(null);

            brokenLinks = hasBrokenLinks();
            brokenIncludes = hasBrokenIncludes(project);
            // Handle annotations
            setForceAnnotation(true);
            updateAnnotationFiles();
        }

        public Folder getFolder() {
            return folder;
        }

        private void updateAnnotationFiles() {
            HashSet<FileObject> set = new HashSet<FileObject>();
            // Add project directory
            FileObject fo = project.getProjectDirectory();
            if (fo == null || !fo.isValid()) {
                // See IZ 125880
                Logger.getLogger("cnd.makeproject").warning("project.getProjectDirectory() == null - " + project);
            }
            set.add(project.getProjectDirectory());
            if (!gotMakeConfigurationDescriptor()) {
                return;
            }
            // Add buildfolder from makefile projects to sources. See IZ 90190.
            MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
            if (makeConfigurationDescriptor == null) {
                return;
            }
            Configurations confs = makeConfigurationDescriptor.getConfs();
            if (confs == null) {
                return;
            }
            for (Configuration conf : confs.getConfs()) {
                MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
                if (makeConfiguration.isMakefileConfiguration()) {
                    MakefileConfiguration makefileConfiguration = makeConfiguration.getMakefileConfiguration();
                    String path = makefileConfiguration.getAbsBuildCommandWorkingDir();
                    File file = new File(path);
                    if (file.exists()) {
                        try {
                            set.add(FileUtil.toFileObject(file.getCanonicalFile()));
                        } catch (IOException ioe) {
                        }
                    }
                }
            }
            setFiles(set);
            List<Folder> allFolders = new ArrayList<Folder>();
            allFolders.add(folder);
            allFolders.addAll(folder.getAllFolders(true));
            Iterator iter = allFolders.iterator();
            while (iter.hasNext()) {
                ((Folder) iter.next()).addChangeListener(this);
            }
        }

        @Override
        public String getShortDescription() {
            return MakeLogicalViewProvider.this.getShortDescription();
        }

        class VisualUpdater implements Runnable {
            public void run() {
                fireIconChange();
                fireOpenedIconChange();
                fireDisplayNameChange(null, null);
            }
        }

        /*
         * Something in the folder has changed
         **/
        public void stateChanged(ChangeEvent e) {
            brokenLinks = hasBrokenLinks();
            brokenIncludes = hasBrokenIncludes(project);
            updateAnnotationFiles();
            EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // MakeLogicalViewRootNode
//            fireOpenedIconChange();
//            fireDisplayNameChange(null, null);
        }

        @Override
        public Object getValue(String valstring) {
            if (valstring == null) {
                return super.getValue(null);
            }
            if (valstring.equals("Folder")) // NOI18N
            {
                return folder;
            } else if (valstring.equals("Project")) // NOI18N
            {
                return project;
            } else if (valstring.equals("This")) // NOI18N
            {
                return this;
            }
            return super.getValue(valstring);
        }

        @Override
        public Image getIcon(int type) {
            return mergeBadge(annotateIcon(super.getIcon(type), type));
        }

        private Image mergeBadge(Image original) {
            if (brokenLinks) {
                return ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0);
            } else if (brokenIncludes) {
                return ImageUtilities.mergeImages(original, brokenIncludeBadge, 8, 0);
            }
            return original;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return mergeBadge(annotateIcon(super.getOpenedIcon(type), type));
        }

        @Override
        public Action[] getActions(boolean context) {
            MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();

            // TODO: not clear if we need to call the following method at all
            // but we need to remove remembering the output to prevent memory leak;
            // I think it could be removed
            if (descriptor != null) {
                descriptor.getLogicalFolders();
            }

            List<Action> actions = new ArrayList<Action>();
            // Add standard actions
            Action[] standardActions;
            MakeConfiguration active = (descriptor == null) ? null : (MakeConfiguration) descriptor.getConfs().getActive();
            if (descriptor == null || active == null || active.isMakefileConfiguration()) { // FIXUP: need better check
                standardActions = getAdditionalDiskFolderActions();
            } else {
                standardActions = getAdditionalLogicalFolderActions();
            }
            for (int i = 0; i < standardActions.length; i++) {
                actions.add(standardActions[i]);
            }
            actions.add(null);
            //actions.add(new CodeAssistanceAction());
            actions.addAll(Utilities.actionsForPath("NativeProjects/Menu")); // NOI18N
            actions.add(null);
            actions.add(SystemAction.get(org.openide.actions.FindAction.class));
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
            // Add remaining actions
            actions.add(null);
            //actions.add(SystemAction.get(ToolsAction.class));
            //actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public PasteType getDropType(Transferable transferable, int action, int index) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    return super.getDropType(transferable, action, index);
                }
            }
            return null;
        }

        @Override
        protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    try {
                        ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                        int type = new Integer(flavors[i].getParameter(MASK)).intValue();
                        list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type));
                    } catch (Exception e) {
                    }
                }
            }
            super.createPasteTypes(transferable, list);
        }

        // Private methods -------------------------------------------------
        private Action[] getAdditionalLogicalFolderActions() {

            ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);

            return new Action[]{
                        CommonProjectActions.newFileAction(),
                        null,
                        SystemAction.get(AddExistingItemAction.class),
                        SystemAction.get(AddExistingFolderItemsAction.class),
                        SystemAction.get(NewFolderAction.class),
                        new AddExternalItemAction(project),
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                        //                        ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, bundle.getString("LBL_BuildPackagesAction_Name"), null), // NOI18N
                        new RemoteDevelopmentAction(project),
                        new SetConfigurationAction(project),
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null), // NOI18N
                        //new DebugMenuAction(project, helper),
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null),
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString("LBL_DebugAction_Step_Name"), null),
                        null,
                        CommonProjectActions.setAsMainProjectAction(),
                        CommonProjectActions.openSubprojectsAction(),
                        CommonProjectActions.closeProjectAction(),
                        null,
                        CommonProjectActions.renameProjectAction(),
                        CommonProjectActions.moveProjectAction(),
                        CommonProjectActions.copyProjectAction(),
                        CommonProjectActions.deleteProjectAction(),
                        null,};
        }

        private Action[] getAdditionalDiskFolderActions() {

            ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);

            return new Action[]{
                        CommonProjectActions.newFileAction(),
                        null,
                        new AddExternalItemAction(project),
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null), // NOI18N
                        //                        ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BATCH_BUILD, bundle.getString("LBL_BatchBuildAction_Name"), null), // NOI18N
                        ProjectSensitiveActions.projectCommandAction(MakeActionProvider.COMMAND_BUILD_PACKAGE, bundle.getString("LBL_BuildPackagesAction_Name"), null), // NOI18N
                        new RemoteDevelopmentAction(project),
                        new SetConfigurationAction(project),
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null), // NOI18N
                        //new DebugMenuAction(project, helper),
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null),
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG_STEP_INTO, bundle.getString("LBL_DebugAction_Step_Name"), null),
                        null,
                        CommonProjectActions.setAsMainProjectAction(),
                        CommonProjectActions.openSubprojectsAction(),
                        CommonProjectActions.closeProjectAction(),
                        null,
                        CommonProjectActions.renameProjectAction(),
                        CommonProjectActions.moveProjectAction(),
                        CommonProjectActions.copyProjectAction(),
                        CommonProjectActions.deleteProjectAction(),
                        null,};
        }

        public void resultChanged(LookupEvent ev) {
            for (BrokenIncludes elem : brokenIncludesResult.allInstances()) {
                elem.addChangeListener(this);
            }
        }

        private boolean hasBrokenIncludes(Project project) {
            BrokenIncludes provider = Lookup.getDefault().lookup(BrokenIncludes.class);
            if (provider != null) {
                NativeProject id = project.getLookup().lookup(NativeProject.class);
                if (id != null) {
                    return provider.isBroken(id);
                }
            }
            return false;
        }
    }

    private final class MakeLogicalViewRootNodeBroken extends AbstractNode {

        public MakeLogicalViewRootNodeBroken() {
            super(Children.LEAF, Lookups.fixed(new Object[]{project}));
            setIconBaseWithExtension(MakeConfigurationDescriptor.ICON);
            setName(ProjectUtils.getInformation(project).getDisplayName());
        }

        @Override
        public Image getIcon(int type) {
            Image original = super.getIcon(type);
            return ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0);
        }

        @Override
        public Image getOpenedIcon(int type) {
            Image original = super.getOpenedIcon(type);
            return ImageUtilities.mergeImages(original, brokenProjectBadge, 8, 0);
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.closeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public String getShortDescription() {
            return MakeLogicalViewProvider.this.getShortDescription();
        }
    }

    private class LogicalViewChildren extends BaseMakeViewChildren {

        public LogicalViewChildren(Folder folder) {
            super(folder);
        }

        protected Node[] createNodes(Object key) {
            Node node = null;
            if (key instanceof LoadingNode) {
                //System.err.println("LogicalViewChildren: return wait node");
                node = (Node) key;
            } else if (key instanceof Node) {
                node = (Node) key;
            } else if (key instanceof Folder) {
                Folder folder = (Folder) key;
                if (folder.isProjectFiles()) {
                    //FileObject srcFileObject = project.getProjectDirectory().getFileObject("src");
                    FileObject srcFileObject = project.getProjectDirectory();
                    DataObject srcDataObject = null;
                    try {
                        if (srcFileObject.isValid()) {
                            srcDataObject = DataObject.find(srcFileObject);
                        }
                    } catch (DataObjectNotFoundException e) {
                        // Do not throw Exception.
                        // It is normal use case when folder can be deleted at build time.
                        //throw new AssertionError(e);
                    }
                    if (srcDataObject != null) {
                        node = new LogicalFolderNode(((DataFolder) srcDataObject).getNodeDelegate(), folder);
                    } else {
                        // Fix me. Create Broken Folder
                        //node = new BrokenViewFolderNode(this, getFolder(), folder);
                    }
                } else {
                    node = new ExternalFilesNode(folder);
                }
            } else if (key instanceof Item) {
                Item item = (Item) key;
                DataObject fileDO = item.getDataObject();
                if (fileDO != null) {
                    node = new ViewItemNode(this, getFolder(), item, fileDO);
                } else {
                    node = new BrokenViewItemNode(this, getFolder(), item);
                }
            } else if (key instanceof AbstractNode) {
                node = (AbstractNode) key;
            }
            if (node == null) {
                return new Node[]{};
            }
            return new Node[]{node};
        }

        protected Collection<Object> getKeys() {
            Collection<Object> collection;
            if (getFolder().isDiskFolder()) {
                // Search disk folder for C/C++ files and add them to the view (not the project!).
                ArrayList<Object> collection2 = new ArrayList<Object>(getFolder().getElements());
                String absPath = IpeUtils.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), getFolder().getRootPath());
                File folderFile = new File(absPath);
                if (folderFile.isDirectory() && folderFile.exists()) {
                    File[] children = folderFile.listFiles();
                    for (File child : children) {
                        if (!child.isFile()) {
                            // it's a folder
                            continue;
                        }
                        if (getFolder().findItemByName(child.getName()) != null) {
                            // Already there
                            continue;
                        }
                        if (!VisibilityQuery.getDefault().isVisible(child)) {
                            // not visible
                            continue;
                        }
                        // Add file to the view
                        Item item = new Item(child.getAbsolutePath());
                        Folder.insertItemElementInList(collection2, item);
                    }
                }
                collection = collection2;
            } else {
                collection = getFolder().getElements();
            }

            switch (getFolder().getConfigurationDescriptor().getState()) {
                case READING:
                    if (collection.size() == 0) {
                        collection = Collections.singletonList((Object) new LoadingNode());
                    }
                    break;
                case BROKEN:
                // TODO show broken node
            }
            if ("root".equals(getFolder().getName())) { // NOI18N
                LogicalViewNodeProvider[] providers = LogicalViewNodeProviders.getInstance().getProvidersAsArray();
                if (providers.length > 0) {
                    for (int i = 0; i < providers.length; i++) {
                        AbstractNode node = providers[i].getLogicalViewNode(project);
                        if (node != null) {
                            collection.add(node);
                        }
                    }
                }
            }

            return collection;
        }
    }

    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor) pdp.getConfigurationDescriptor();
        return makeConfigurationDescriptor;
    }

    private boolean gotMakeConfigurationDescriptor() {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        return pdp.gotDescriptor();
    }

    private class LogicalFolderNode extends AnnotatedNode implements ChangeListener {

        private Folder folder;

        public LogicalFolderNode(Node folderNode, Folder folder) {
            super(new LogicalViewChildren(folder), Lookups.fixed(new Object[]{
                        folder,
                        project,
                        new FolderSearchInfo(folder),}));
            this.folder = folder;
            setForceAnnotation(true);
            updateAnnotationFiles();
        }

        private void updateAnnotationFiles() {
            RequestProcessor.getDefault().post(new UpdateAnnotationFilesTHread(this));
        }

        class UpdateAnnotationFilesTHread extends Thread {

            LogicalFolderNode logicalFolderNode;

            UpdateAnnotationFilesTHread(LogicalFolderNode logicalFolderNode) {
                this.logicalFolderNode = logicalFolderNode;
            }

            @Override
            public void run() {
                setFiles(new HashSet<FileObject>() /*Collections.EMPTY_SET*/ /*folder.getAllItemsAsFileObjectSet(true)*/); // See IZ 100394 for details
                List<Folder> allFolders = new ArrayList<Folder>();
                allFolders.add(folder);
                allFolders.addAll(folder.getAllFolders(true));
                Iterator iter = allFolders.iterator();
                while (iter.hasNext()) {
                    ((Folder) iter.next()).addChangeListener(logicalFolderNode);
                }
            }
        }


        class VisualUpdater implements Runnable {
            public void run() {
                fireIconChange();
                fireOpenedIconChange();
            }
        }
        /*
         * Something in the folder has changed
         **/
        public void stateChanged(ChangeEvent e) {
            updateAnnotationFiles();
            EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // LogicalFolderNode
//            fireOpenedIconChange();
        }

        public Folder getFolder() {
            return folder;
        }

        @Override
        public Object getValue(String valstring) {
            if (valstring == null) {
                return super.getValue(null);
            }
            if (valstring.equals("Folder")) // NOI18N
            {
                return folder;
            } else if (valstring.equals("Project")) // NOI18N
            {
                return project;
            } else if (valstring.equals("This")) // NOI18N
            {
                return this;
            }
            return super.getValue(valstring);
        }

        @Override
        public Image getIcon(int type) {
            if (folder.isDiskFolder()) {
                return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"), type); // NOI18N
            } else {
                return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolder.gif"), type); // NOI18N
            }
        }

        @Override
        public Image getOpenedIcon(int type) {
            if (folder.isDiskFolder()) {
                return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"), type); // NOI18N
            } else {
                return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolderOpened.gif"), type); // NOI18N
            }
        }

        @Override
        public String getName() {
            return folder.getDisplayName();
        }

        @Override
        public String getDisplayName() {
            return annotateName(folder.getDisplayName());
        }

        @Override
        public void setName(String newName) {
            String oldName = folder.getDisplayName();
            if (folder.isDiskFolder()) {
                String rootPath = folder.getRootPath();
                String AbsRootPath = IpeUtils.toAbsolutePath(folder.getConfigurationDescriptor().getBaseDir(), rootPath);
                File file = new File(AbsRootPath);
                if (!file.isDirectory() || !file.exists()) {
                    return;
                }
                FileObject fo = FileUtil.toFileObject(file);
                try {
                    fo.rename(fo.lock(), newName, null);
                } catch (IOException ioe) {
                }
                return;
            }
            if (folder.getParent() != null && folder.getParent().findFolderByDisplayName(newName) != null) {
                String msg = NbBundle.getMessage(MakeLogicalViewProvider.class, "CANNOT_RENAME", oldName, newName); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                return;
            }
            folder.setDisplayName(newName);
            fireDisplayNameChange(oldName, newName);
        }

//        @Override
//        public void setDisplayName(String newName) {
//            setDisplayName(newName);
//        }
        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public boolean canCut() {
            return false; // FIXUP
        }

        @Override
        public boolean canCopy() {
            return false; // FIXUP
        }

        @Override
        public void destroy() throws IOException {
            if (!getFolder().isDiskFolder()) {
                return;
            }
            String absPath = IpeUtils.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), getFolder().getRootPath());
            File folderFile = new File(absPath);
            if (!folderFile.isDirectory() || !folderFile.exists()) {
                return;
            }
            FileObject folderFileObject = FileUtil.toFileObject(folderFile);
            folderFileObject.delete();
            super.destroy();
        }

        @Override
        public PasteType getDropType(Transferable transferable, int action, int index) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    return super.getDropType(transferable, action, index);
                }
            }
            return null;
        }

        @Override
        protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].getSubType().equals(SUBTYPE)) {
                    try {
                        ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                        int type = new Integer(flavors[i].getParameter(MASK)).intValue();
                        list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type));
                    } catch (Exception e) {
                    }
                }
            }
            super.createPasteTypes(transferable, list);
        }

        public void newLogicalFolder() {
        }

        @Override
        public Action[] getActions(boolean context) {
            if (folder.isDiskFolder()) {
                return new Action[]{
                            CommonProjectActions.newFileAction(),
                            SystemAction.get(org.openide.actions.FindAction.class),
                            null,
                            SystemAction.get(CutAction.class),
                            SystemAction.get(CopyAction.class),
                            SystemAction.get(PasteAction.class),
                            null,
                            //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                            //                        null,
                            SystemAction.get(DeleteAction.class),
                            createRenameAction(),
                            null,
                            SystemAction.get(PropertiesFolderAction.class),};
            } else {
                return new Action[]{
                            CommonProjectActions.newFileAction(),
                            SystemAction.get(NewFolderAction.class),
                            SystemAction.get(AddExistingItemAction.class),
                            SystemAction.get(AddExistingFolderItemsAction.class),
                            SystemAction.get(org.openide.actions.FindAction.class),
                            null,
                            //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                            //                        null,
                            SystemAction.get(CutAction.class),
                            SystemAction.get(CopyAction.class),
                            SystemAction.get(PasteAction.class),
                            null,
                            SystemAction.get(RemoveFolderAction.class),
                            //                SystemAction.get(RenameAction.class),
                            createRenameAction(),
                            null,
                            SystemAction.get(PropertiesFolderAction.class),};
            }
        }
    }

    private class ViewItemPasteType extends PasteType {

        private final Folder toFolder;
        private final ViewItemNode viewItemNode;
        private final int type;

        public ViewItemPasteType(Folder toFolder, ViewItemNode viewItemNode, int type) {
            this.toFolder = toFolder;
            this.viewItemNode = viewItemNode;
            this.type = type;
        }

        private void copyItemConfigurations(ItemConfiguration[] newConfigurations, ItemConfiguration[] oldConfigurations) {
            // Only allowing copying configurations within same project
            assert newConfigurations.length == oldConfigurations.length;
            for (int i = 0; i < newConfigurations.length; i++) {
                newConfigurations[i].assignValues(oldConfigurations[i]);
            }
        }

        public Transferable paste() throws IOException {
            if (!gotMakeConfigurationDescriptor() || !(getMakeConfigurationDescriptor().okToChange())) {
                return null;
            }
            Item item = viewItemNode.getItem();
            ItemConfiguration[] oldConfigurations = item.getItemConfigurations();
            if (oldConfigurations.length == 0) {
                // Item may have been removed or renamed inbetween copy and paste
                return null;
            }
            if (type == DnDConstants.ACTION_MOVE) {
                // Drag&Drop, Cut&Paste
                if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                    // Move within same project
                    if (toFolder.isDiskFolder()) {
                        FileObject itemFO = item.getFileObject();
                        String toFolderPath = IpeUtils.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                        FileObject toFolderFO = FileUtil.toFileObject(new File(toFolderPath));
                        String newName = IpeUtils.createUniqueFileName(toFolderPath, itemFO.getName(), itemFO.getExt());
                        FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);

                        File movedFileFile = FileUtil.toFile(movedFileFO);
                        String itemPath = movedFileFile.getPath();
                        itemPath = FilePathAdaptor.mapToRemote(itemPath);
                        itemPath = IpeUtils.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                        itemPath = FilePathAdaptor.normalize(itemPath);
                        Item movedItem = toFolder.findItemByPath(itemPath);
                        if (movedItem != null) {
                            copyItemConfigurations(movedItem.getItemConfigurations(), oldConfigurations);
                        }
                    } else {
                        if (viewItemNode.getFolder().removeItem(item)) {
                            toFolder.addItem(item);
                            copyItemConfigurations(item.getItemConfigurations(), oldConfigurations);
                        }
                    }
                } else {
                    if (toFolder.isDiskFolder()) {
                        FileObject itemFO = item.getFileObject();
                        String toFolderPath = IpeUtils.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                        FileObject toFolderFO = FileUtil.toFileObject(new File(toFolderPath));
                        String newName = IpeUtils.createUniqueFileName(toFolderPath, itemFO.getName(), itemFO.getExt());
                        FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);
                    } else if (IpeUtils.isPathAbsolute(item.getPath())) {
                        if (viewItemNode.getFolder().removeItem(item)) {
                            toFolder.addItem(item);
                        }
                    } else if (item.getPath().startsWith("..")) { // NOI18N
                        String originalFilePath = FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath();
                        String newFilePath = FileUtil.toFile(toFolder.getProject().getProjectDirectory()).getPath();
                        String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                        fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
                        String newPath = fromNewToOriginal + item.getPath();
                        newPath = IpeUtils.trimDotDot(newPath);
                        if (viewItemNode.getFolder().removeItemAction(item)) {
                            toFolder.addItemAction(new Item(FilePathAdaptor.normalize(newPath)));
                        }
                    } else {
                        Project toProject = toFolder.getProject();
                        FileObject fo = item.getFileObject();
                        FileObject copy = fo.copy(toProject.getProjectDirectory(), fo.getName(), fo.getExt());
                        String newPath = IpeUtils.toRelativePath(FileUtil.toFile(toProject.getProjectDirectory()).getPath(), FileUtil.toFile(copy).getPath());
                        if (viewItemNode.getFolder().removeItemAction(item)) {
                            fo.delete();
                            toFolder.addItemAction(new Item(FilePathAdaptor.normalize(newPath)));
                        }
                    }
                }
            } else if (type == DnDConstants.ACTION_COPY || type == DnDConstants.ACTION_NONE) {
                // Copy&Paste
                if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                    if ((IpeUtils.isPathAbsolute(item.getPath()) || item.getPath().startsWith("..")) && !toFolder.isDiskFolder()) { // NOI18N
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        FileObject fo = FileUtil.toFileObject(item.getCanonicalFile());
                        String ext = fo.getExt();
                        if (toFolder.isDiskFolder()) {
                            String toFolderPath = IpeUtils.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                            FileObject toFolderFO = FileUtil.toFileObject(new File(toFolderPath));
                            String newName = IpeUtils.createUniqueFileName(toFolderPath, fo.getName(), ext);
                            FileObject copiedFileObject = fo.copy(toFolderFO, newName, ext);

                            File copiedFileFile = FileUtil.toFile(copiedFileObject);
                            String itemPath = copiedFileFile.getPath();
                            itemPath = FilePathAdaptor.mapToRemote(itemPath);
                            itemPath = IpeUtils.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                            itemPath = FilePathAdaptor.normalize(itemPath);
                            Item copiedItemItem = toFolder.findItemByPath(itemPath);
                            if (copiedItemItem != null) {
                                copyItemConfigurations(copiedItemItem.getItemConfigurations(), oldConfigurations);
                            }
                        } else {
                            String parent = FileUtil.toFile(fo.getParent()).getPath();
                            String newName = IpeUtils.createUniqueFileName(parent, fo.getName(), ext);
                            fo.copy(fo.getParent(), newName, ext);
                            String newPath = parent + "/" + newName; // NOI18N
                            if (ext.length() > 0) {
                                newPath = newPath + "." + ext; // NOI18N
                            }
                            newPath = IpeUtils.toRelativePath(FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath(), newPath);
                            Item newItem = new Item(FilePathAdaptor.normalize(newPath));
                            toFolder.addItemAction(newItem);
                            copyItemConfigurations(newItem.getItemConfigurations(), oldConfigurations);
                        }
                    }
                } else {
                    if (toFolder.isDiskFolder()) {
                        FileObject fo = FileUtil.toFileObject(item.getCanonicalFile());
                        String ext = fo.getExt();
                        String toFolderPath = IpeUtils.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                        FileObject toFolderFO = FileUtil.toFileObject(new File(toFolderPath));
                        String newName = IpeUtils.createUniqueFileName(toFolderPath, fo.getName(), ext);
                        fo.copy(toFolderFO, newName, ext);
                    } else if (IpeUtils.isPathAbsolute(item.getPath())) {
                        toFolder.addItem(new Item(item.getPath()));
                    } else if (item.getPath().startsWith("..")) { // NOI18N
                        String originalFilePath = FileUtil.toFile(viewItemNode.getFolder().getProject().getProjectDirectory()).getPath();
                        String newFilePath = FileUtil.toFile(toFolder.getProject().getProjectDirectory()).getPath();
                        String fromNewToOriginal = IpeUtils.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                        fromNewToOriginal = FilePathAdaptor.normalize(fromNewToOriginal);
                        String newPath = fromNewToOriginal + item.getPath();
                        newPath = IpeUtils.trimDotDot(newPath);
                        toFolder.addItemAction(new Item(FilePathAdaptor.normalize(newPath)));
                    } else {
                        Project toProject = toFolder.getProject();
                        String parent = FileUtil.toFile(toProject.getProjectDirectory()).getPath();
                        FileObject fo = item.getFileObject();
                        String ext = fo.getExt();
                        String newName = IpeUtils.createUniqueFileName(parent, fo.getName(), ext);
                        fo.copy(toProject.getProjectDirectory(), newName, ext);
                        String newPath = newName;
                        if (ext.length() > 0) {
                            newPath = newPath + "." + ext; // NOI18N
                        }
                        toFolder.addItemAction(new Item(FilePathAdaptor.normalize(newPath))); // NOI18N
                    }
                }
            }
            return null;
        }
    }

    private final class ExternalFilesNode extends AbstractNode {

        private Folder folder;

        public ExternalFilesNode(Folder folder) {
            super(new ExternalFilesChildren(project, folder), Lookups.fixed(new Object[]{project, new FolderSearchInfo(folder)}));
            setName(folder.getName());
            setDisplayName(folder.getDisplayName());
            setShortDescription(NbBundle.getBundle(getClass()).getString("ONLY_REFERENCE_TXT"));
            this.folder = folder;
        }

        @Override
        public Object getValue(String valstring) {
            if (valstring == null) {
                return super.getValue(null);
            }
            if (valstring.equals("Folder")) // NOI18N
            {
                return folder;
            } else if (valstring.equals("Project")) // NOI18N
            {
                return project;
            } else if (valstring.equals("This")) // NOI18N
            {
                return this;
            }
            return super.getValue(valstring);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/importantFolder.gif"); // NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/importantFolderOpened.gif"); // NOI18N
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                        new AddExternalItemAction(project),
                        null,
                        SystemAction.get(org.openide.actions.FindAction.class),};
        }

        @Override
        public boolean canRename() {
            return false;
        }
    }
    private static final int WAIT_DELAY = 50;

    private abstract class BaseMakeViewChildren extends Children.Keys<Object>
            implements ChangeListener, RefreshableItemsContainer {

        private final Folder folder;

        public BaseMakeViewChildren(Folder folder) {
            this.folder = folder;
        }

        @Override
        protected void addNotify() {
            if (isFindPathMode()) {
                //System.err.println("BaseMakeViewChildren: FindPathMode " + (SwingUtilities.isEventDispatchThread() ? "UI":"regular") + " thread");
                // no wait node for direct search
                super.addNotify();
                folder.addChangeListener(this);
                setKeys(getKeys());
            } else {
                //System.err.println("BaseMakeViewChildren: create wait node " + (SwingUtilities.isEventDispatchThread() ? "UI":"regular") + " thread");
                if (SwingUtilities.isEventDispatchThread()) {
                    super.addNotify();
                    setKeys(new Object[]{getWaitNode()});
                    folder.addChangeListener(this);
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            // between posting this task and running it can be become deleted (see iz #142240)
                            // TODO: fix workflow instead?
                            if (project.getProjectDirectory() != null && project.getProjectDirectory().isValid()) {
                                //System.err.println("ExternalFilesChildren: setting real nodes");
                                setKeys(getKeys());
                            }
                        }
                    }, WAIT_DELAY);
                } else {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            addNotify();
                        }
                    });
                }
            }
        }

        public void refreshItem(Item item) {
            refreshKey(item);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            folder.removeChangeListener(this);
            super.removeNotify();
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() instanceof Item) {
                // update single item (it may be broken)
                Item[] items = getFolder().getItemsAsArray();
                for (int i = 0; i < items.length; i++) {
                    if (e.getSource() == items[i]) {
                        refreshItem(items[i]);
                    }
                }
            } else {
                // update folder. Items may have been added or deleted
                setKeys(getKeys());
            }
        }

        abstract protected Collection<Object> getKeys();

        public Folder getFolder() {
            return folder;
        }
    }

    private class ExternalFilesChildren extends BaseMakeViewChildren {

        public ExternalFilesChildren(Project project, Folder folder) {
            super(folder);
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof LoadingNode) {
                return new Node[]{(Node) key};
            }
            if (!(key instanceof Item)) {
                System.err.println("wrong item in external files folder " + key); // NOI18N
                return null;
            }
            Item item = (Item) key;
            DataObject fileDO = item.getDataObject();
            Node node;
            if (fileDO != null) {
                node = new ViewItemNode(this, getFolder(), item, fileDO);
            } else {
                node = new BrokenViewItemNode(this, getFolder(), item);
            }
            return new Node[]{node};
        }

        protected Collection<Object> getKeys() {
            return getFolder().getElements();
        }
    }

    private class ViewItemNode extends FilterNode implements ChangeListener {

        RefreshableItemsContainer childrenKeys;
        private Folder folder;
        private Item item;

        public ViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item, DataObject dataObject) {
            super(dataObject.getNodeDelegate());//, null, Lookups.fixed(item));
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            File file = item.getCanonicalFile();
            setShortDescription(file.getPath());
        }

        public Folder getFolder() {
            return folder;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public boolean canCut() {
            return true;
        }

        @Override
        public boolean canCopy() {
            return true;
        }

        @Override
        public Transferable clipboardCopy() throws IOException {
            return addViewItemTransferable(super.clipboardCopy(), DnDConstants.ACTION_COPY);
        }

        @Override
        public Transferable clipboardCut() throws IOException {
            return addViewItemTransferable(super.clipboardCut(), DnDConstants.ACTION_MOVE);
        }

        @Override
        public Transferable drag() throws IOException {
            return addViewItemTransferable(super.drag(), DnDConstants.ACTION_NONE);
        }

        private ExTransferable addViewItemTransferable(Transferable t, int operation) {
            try {
                ExTransferable extT = ExTransferable.create(t);
                ViewItemTransferable viewItem = new ViewItemTransferable(this, operation);
                extT.put(viewItem);
                return extT;
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        // The node will be removed when the Item gets notification that the file has been destroyed.
        // No need to do it here.

        @Override
        public void destroy() throws IOException {
//            File file = new File(item.getAbsPath());
//            if (file.exists())
//                file.delete();
            folder.removeItemAction(item);
            super.destroy();
        }

        @Override
        public Object getValue(String valstring) {
            if (valstring == null) {
                return super.getValue(null);
            }
            if (valstring.equals("Folder")) // NOI18N
            {
                return getFolder();
            } else if (valstring.equals("Project")) // NOI18N
            {
                return project;
            } else if (valstring.equals("Item")) // NOI18N
            {
                return getItem();
            } else if (valstring.equals("This")) // NOI18N
            {
                return this;
            }
            return super.getValue(valstring);
        }

        @Override
        public Action[] getActions(boolean context) {
            // Replace DeleteAction with Remove Action
            // Replace PropertyAction with customizeProjectAction
            Action[] oldActions = super.getActions(false);
            List<Action> newActions = new ArrayList<Action>();
            if (getItem().getFolder() == null) {
                return oldActions;
            }
            else if (getItem().getFolder().isDiskFolder()) {
                for (int i = 0; i < oldActions.length; i++) {
                    if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                        newActions.add(oldActions[i]);
                        newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                    } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                        newActions.add(createRenameAction());
                    } else if (oldActions[i] != null && oldActions[i] instanceof DeleteAction) {
                        newActions.add(createDeleteAction());
                    } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                        newActions.add(SystemAction.get(PropertiesItemAction.class));
                    } else {
                        newActions.add(oldActions[i]);
                    }
                }
                return newActions.toArray(new Action[newActions.size()]);
            } else {
                for (int i = 0; i < oldActions.length; i++) {
                    if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.OpenAction) {
                        newActions.add(oldActions[i]);
                        newActions.add(null);
//                        newActions.add(new RefreshItemAction(childrenKeys, null, getItem()));
//                        newActions.add(null);
                    } else if (oldActions[i] != null && oldActions[i] instanceof PasteAction) {
                        newActions.add(oldActions[i]);
                        newActions.add(SystemAction.get(CompileSingleAction.class));
                    } else if (oldActions[i] != null && oldActions[i] instanceof RenameAction) {
                        newActions.add(createRenameAction());
                    } else if (oldActions[i] != null && oldActions[i] instanceof org.openide.actions.PropertiesAction && getFolder().isProjectFiles()) {
                        newActions.add(SystemAction.get(PropertiesItemAction.class));
                    } else if (oldActions[i] != null && oldActions[i] instanceof DeleteAction) {
                        newActions.add(SystemAction.get(RemoveItemAction.class));
                        newActions.add(createDeleteAction());
                    } else {
                        newActions.add(oldActions[i]);
                    }
                }
                return newActions.toArray(new Action[newActions.size()]);
            }
        }

        @Override
        public Image getIcon(int type) {
            Image image = super.getIcon(type);
            if (isExcluded() && (image instanceof BufferedImage)) {
                ColorSpace gray_space = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                ColorConvertOp convert_to_gray_op = new ColorConvertOp(gray_space, null);
                image = convert_to_gray_op.filter((BufferedImage) image, null);
            }
            return image;
        }

        @Override
        public String getHtmlDisplayName() {
            if (isExcluded()) {
                String baseName = super.getHtmlDisplayName();
                if (baseName != null && baseName.toLowerCase().contains("color=")) { // NOI18N
                    // decorating node already has color, leave it
                    return baseName;
                } else {
                    // add own "disabled" color
                    baseName = baseName != null ? baseName : getDisplayName();
                    return "<font color='!controlShadow'>" + baseName; // NOI18N
                }
            }
            return super.getHtmlDisplayName();
        }

        private boolean isExcluded() {
            if (item == null || item.getFolder() == null || item.getFolder().getConfigurationDescriptor() == null || item.getFolder().getConfigurationDescriptor().getConfs() == null) {
                return false;
            }
            MakeConfiguration makeConfiguration = (MakeConfiguration) item.getFolder().getConfigurationDescriptor().getConfs().getActive();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                return false;
            }
            BooleanConfiguration excl = itemConfiguration.getExcluded();
            return excl.getValue();
        }

        class VisualUpdater implements Runnable {
            public void run() {
                fireIconChange();
                fireOpenedIconChange();
                String displayName = getDisplayName();
                fireDisplayNameChange(displayName, "");
                fireDisplayNameChange("", displayName);
            }
        }

        public void stateChanged(ChangeEvent e) {
//            String displayName = getDisplayName();
//            fireDisplayNameChange(displayName, "");
//            fireDisplayNameChange("", displayName);
            EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // ViewItemNode
//            fireOpenedIconChange();
        }
    }

    private static class ViewItemTransferable extends ExTransferable.Single {

        private ViewItemNode node;

        public ViewItemTransferable(ViewItemNode node, int operation) throws ClassNotFoundException {
            super(new DataFlavor(ITEM_VIEW_FLAVOR.format(new Object[]{Integer.valueOf(operation)}), null, MakeLogicalViewProvider.class.getClassLoader()));
            this.node = node;
        }

        protected Object getData() throws IOException, UnsupportedFlavorException {
            return this.node;
        }
    }

    private final class BrokenViewItemNode extends AbstractNode {

        private boolean broken;
        private RefreshableItemsContainer childrenKeys;
        private Folder folder;
        private Item item;

        public BrokenViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item) {
            super(Children.LEAF);
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            File file = item.getCanonicalFile();
            setName(file.getPath());
            setDisplayName(file.getName());
            setShortDescription(NbBundle.getMessage(getClass(), "BrokenTxt", file.getPath())); // NOI18N
            broken = true;
        }

        @Override
        public Image getIcon(int type) {
            //Image original = Utilities.loadImage("org/openide/loaders/instanceObject.gif"); // NOI18N
            //Image original = Utilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); // NOI18N
            Image original;
            int tool = item.getDefaultTool();
            if (tool == Tool.CCompiler) {
                original = ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/CSrcIcon.gif"); // NOI18N
            } else if (tool == Tool.CCCompiler) {
                original = ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); // NOI18N
            } else if (tool == Tool.FortranCompiler) {
                original = ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/FortranSrcIcon.gif"); // NOI18N
            } else {
                original = ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/unknown.gif"); // NOI18N
            }
            return broken ? ImageUtilities.mergeImages(original, brokenProjectBadge, 11, 0) : original;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                        SystemAction.get(RemoveItemAction.class),
                        new RefreshItemAction(childrenKeys, null, item),
                        null,
                        SystemAction.get(PropertiesItemAction.class),};
        }

        public void refresh() {
            childrenKeys.refreshItem(item);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public Object getValue(String valstring) {
            if (valstring == null) {
                return super.getValue(null);
            }
            if (valstring.equals("Folder")) // NOI18N
            {
                return folder;
            } else if (valstring.equals("Project")) // NOI18N
            {
                return project;
            } else if (valstring.equals("Item")) // NOI18N
            {
                return item;
            } else if (valstring.equals("This")) // NOI18N
            {
                return this;
            }
            return super.getValue(valstring);
        }
    }

    private interface RefreshableItemsContainer {

        void refreshItem(Item item);
    }

    private static class RefreshItemAction extends AbstractAction {

        private RefreshableItemsContainer childrenKeys;
        private Folder folder;
        private Item item;

        public RefreshItemAction(RefreshableItemsContainer childrenKeys, Folder folder, Item item) {
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            putValue(NAME, NbBundle.getBundle(getClass()).getString("CTL_Refresh")); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            if (item != null) {
                childrenKeys.refreshItem(item);
            } else {
                Item[] items = folder.getItemsAsArray();
                for (int i = 0; i < items.length; i++) {
                    childrenKeys.refreshItem(items[i]);
                }
            }
        }
    }

    private static class FolderSearchInfo implements SearchInfo {

        Folder folder;

        FolderSearchInfo(Folder folder) {
            this.folder = folder;
        }

        public boolean canSearch() {
            return true;
        }

        public Iterator<DataObject> objectsToSearch() {
            return folder.getAllItemsAsDataObjectSet(false, "text/").iterator(); // NOI18N
        }
    }

    // this class should be static, because successors are shared classes
    // and accessing MakeLogicalViewProvider.this would use wrong one!
    private static class StandardNodeAction extends NodeAction {

        SystemAction systemAction;

        public StandardNodeAction(SystemAction systemAction) {
            this.systemAction = systemAction;
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes.length > 0) {
                Folder folder = activatedNodes[0].getLookup().lookup(Folder.class);
                if (folder == null) {
                    // TODO: looking up for views is not a good technique, this should
                    // be changed one day all other this class
                    ViewItemNode vin = activatedNodes[0].getLookup().lookup(ViewItemNode.class);
                    if (vin != null) {
                        folder = vin.getFolder();
                    }
                }

                if (folder != null) {
                    MakeConfigurationDescriptor mcd = (MakeConfigurationDescriptor) folder.getConfigurationDescriptor();
                    if (mcd != null && !mcd.okToChange()) {
                        return;
                    }
                }
            }
            InstanceContent ic = new InstanceContent();
            for (int i = 0; i < activatedNodes.length; i++) {
                ic.add(activatedNodes[i]);
            }
            Lookup actionContext = new AbstractLookup(ic);
            final Action a;
            if (systemAction instanceof NodeAction) {
                a = ((NodeAction) systemAction).createContextAwareInstance(actionContext);
            } else if (systemAction instanceof CallbackSystemAction) {
                a = ((CallbackSystemAction) systemAction).createContextAwareInstance(actionContext);
            } else {
                a = null;
                assert false;
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    a.actionPerformed(new ActionEvent(this, 0, null));
                }
            });
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return systemAction.getHelpCtx();
        }

        @Override
        public String getName() {
            return systemAction.getName();
        }
    }

    private static class RenameNodeAction extends StandardNodeAction {

        public RenameNodeAction() {
            super(SystemAction.get(RenameAction.class));
        }
    }

    private static class DeleteNodeAction extends StandardNodeAction {

        public DeleteNodeAction() {
            super(SystemAction.get(DeleteAction.class));
        }
    }

    private StandardNodeAction createRenameAction() {
        if (renameAction == null) {
            renameAction = new RenameNodeAction();
        }
        return renameAction;
    }

    private StandardNodeAction createDeleteAction() {
        if (deleteAction == null) {
            deleteAction = new DeleteNodeAction();
        }
        return deleteAction;
    }
}
