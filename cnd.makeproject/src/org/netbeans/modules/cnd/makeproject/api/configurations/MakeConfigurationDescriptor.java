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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationXMLWriter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.wizards.FolderEntry;
import org.netbeans.modules.cnd.ui.options.ToolsPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MakeConfigurationDescriptor extends ConfigurationDescriptor implements ChangeListener {

    public static final String EXTERNAL_FILES_FOLDER = "ExternalFiles"; // NOI18N
    public static final String SOURCE_FILES_FOLDER = "SourceFiles"; // NOI18N
    public static final String HEADER_FILES_FOLDER = "HeaderFiles"; // NOI18N
    public static final String RESOURCE_FILES_FOLDER = "ResourceFiles"; // NOI18N
    public static final String ICONBASE = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject"; // NOI18N
    public static final String ICON = "org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif"; // NOI18N
    public static final Icon MAKEFILE_ICON = new ImageIcon(Utilities.loadImage(ICON)); // NOI18N
    private Project project = null;
    private String baseDir;
    private boolean modified = false;
    private Folder externalFileItems = null;
    private Folder rootFolder = null;
    private HashMap projectItems = null;
    private List<String> sourceRoots = null;
    private Set<ChangeListener> projectItemsChangeListeners = new HashSet<ChangeListener>();
    private NativeProject nativeProject = null;
    public static String DEFAULT_PROJECT_MAKFILE_NAME = "Makefile"; // NOI18N
    private String projectMakefileName = DEFAULT_PROJECT_MAKFILE_NAME;
    private String sourceEncoding = null;

    public MakeConfigurationDescriptor(String baseDir) {
        super();
        this.baseDir = baseDir;
        rootFolder = new Folder(this, null, "root", "root", true); // NOI18N
        projectItems = new HashMap();
        sourceRoots = new ArrayList<String>();
        setModified(true);
        ToolsPanel.addCompilerSetModifiedListener(this);
        sourceEncoding = FileEncodingQuery.getDefaultEncoding().name();
    }

    /*
     * Called when project is being closed
     */
    public void closed() {
        ToolsPanel.removeCompilerSetModifiedListener(this);
    }

    public static MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project project) {
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = (MakeConfigurationDescriptor) pdp.getConfigurationDescriptor();
            return makeConfigurationDescriptor;
        } else {
            return null;
        }
    }

    /*
     * One of the compiler sets have changed.
     * Mark project modified. This will trigger all makefiles to be regenerated.
     */
    public void stateChanged(ChangeEvent e) {
        setModified();
    }

    public Project getProject() {
        if (project == null) {
            String location = FilePathAdaptor.mapToLocal(getBaseDir()); // PC path
            try {
                // convert base path into file object
                // we can't use canonical path here, because descriptor created with path like
                // /set/ide/mars/... will be changed by canonization into
                // /net/endif/export/home1/deimos/dev/...
                // and using the canonical path based FileObject in the ProjectManager.getDefault().findProject(fo);
                // will cause creating new MakeProject project
                // because there are no opened /net/endif/export/home1/deimos/dev/... project in system
                // there is only /set/ide/mars/... project in system
                //
                // in fact ProjectManager should solve such problems in more general way
                // because even for java it's possible to open the same project from two different
                // locations /set/ide/mars/... and /net/endif/export/home1/deimos/dev/...
                FileObject fo = FileUtil.toFileObject(new File(location));
                project = ProjectManager.getDefault().findProject(fo);
            } catch (Exception e) {
                // Should not happen
                System.err.println("Cannot find project in '" + location + "' " + e); // FIXUP // NOI18N
            }
        }
        return project;
    }

    public void init(Configuration def) {
        super.init(new Configuration[]{def}, 0);
        setModified(true);
    }

    public void initLogicalFolders(Iterator sourceFileFolders, boolean createLogicalFolders, Iterator importantItems) {
        if (createLogicalFolders) {
            rootFolder.addNewFolder(SOURCE_FILES_FOLDER, getString("SourceFilesTxt"), true);
            rootFolder.addNewFolder(HEADER_FILES_FOLDER, getString("HeaderFilesTxt"), true);
            rootFolder.addNewFolder(RESOURCE_FILES_FOLDER, getString("ResourceFilesTxt"), true);
        }
        externalFileItems = rootFolder.addNewFolder(EXTERNAL_FILES_FOLDER, getString("ImportantFilesTxt"), false);
//        if (sourceFileFolders != null)
//            setExternalFileItems(sourceFileFolders); // From makefile wrapper wizard
        externalFileItems.addItem(new Item(getProjectMakefileName())); // NOI18N
        if (importantItems != null) {
            while (importantItems.hasNext()) {
                externalFileItems.addItem(new Item((String) importantItems.next()));
            }
        }
        addSourceFilesFromFolders(sourceFileFolders, false, false);
        setModified(true);
    }

    public String getProjectMakefileName() {
        return projectMakefileName;
    }

    public void setProjectMakefileName(String projectMakefileName) {
        this.projectMakefileName = projectMakefileName;
    }

    /**
     * @deprecated. Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    public void addProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.add(cl);
        }
    }

    /**
     * @deprecated. Use org.netbeans.modules.cnd.api.project.NativeProject interface instead.
     */
    public void removeProjectItemsChangeListener(ChangeListener cl) {
        synchronized (projectItemsChangeListeners) {
            projectItemsChangeListeners.remove(cl);
        }
    }

    public void fireProjectItemsChangeEvent(Item item, int action) {
        Iterator it;

        synchronized (projectItemsChangeListeners) {
            it = new HashSet(projectItemsChangeListeners).iterator();
        }
        ChangeEvent ev = new ProjectItemChangeEvent(this, item, action);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public Set<ChangeListener> getProjectItemsChangeListeners() {
        return projectItemsChangeListeners;
    }

    public void setProjectItemsChangeListeners(Set<ChangeListener> projectItemsChangeListeners) {
        this.projectItemsChangeListeners = projectItemsChangeListeners;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public HashMap getProjectItemsMap() {
        return projectItems;
    }

    public void setProjectItemsMap(HashMap projectItems) {
        this.projectItems = projectItems;
    }

    public void init(Configuration[] confs) {
        super.init(confs, 0);
    }

    public Icon getIcon() {
        return MAKEFILE_ICON;
    }

    public Configuration defaultConf(String name, int type) {
        MakeConfiguration c = new MakeConfiguration(this, name, type);
        Item[] items = getProjectItems();
        for (int i = 0; i < items.length; i++) {
            c.addAuxObject(new ItemConfiguration(c, items[i]));
        }
        return c;
    }

    // External File Items
    public void setExternalFileItems(Vector items) {
        externalFileItems.reset();
        for (Enumeration e = items.elements(); e.hasMoreElements();) {
            externalFileItems.addItem(new Item((String) e.nextElement()));
        }
    }

    public void setExternalFileItems(Folder folder) {
        externalFileItems = folder;
    }

    public Folder getExternalFileItems() {
        return externalFileItems;
    }

    public Item[] getExternalFileItemsAsArray() {
        return externalFileItems.getItemsAsArray();
    }

    public Folder getExternalItemFolder() {
        return externalFileItems;
    }

    // Logical Folders
    public Folder getLogicalFolders() {
        return rootFolder;
    }

    public void setLogicalFolders(Folder logicalFolders) {
        this.rootFolder = logicalFolders;
    }

    // Project Files
    public Item[] getProjectItems() {
        Collection collection = projectItems.values();
        return (Item[]) collection.toArray(new Item[collection.size()]);
    }

    public Item findItemByFile(File file) {
        Collection coll = projectItems.values();
        Iterator it = coll.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            File itemFile = item.getCanonicalFile();
            if (itemFile == file || itemFile.getPath().equals(file.getPath())) {
                return item;
            }
        }
        return null;
    }

    public Item findProjectItemByPath(String path) {
        // Try first as-is
        path = FilePathAdaptor.normalize(path);
        Item item = (Item) projectItems.get(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (IpeUtils.isPathAbsolute(path)) {
                newPath = IpeUtils.toRelativePath(getBaseDir(), FilePathAdaptor.naturalize(path));
            } else {
                newPath = IpeUtils.toAbsolutePath(getBaseDir(), path);
            }
            newPath = FilePathAdaptor.normalize(newPath);
            item = (Item) projectItems.get(newPath);
        }
        return item;
    }

    public Item findExternalItemByPath(String path) {
        // Try first as-is
        Item item = (Item) externalFileItems.findItemByPath(path);
        if (item == null) {
            // Then try absolute if relative or relative if absolute
            String newPath;
            if (IpeUtils.isPathAbsolute(path)) {
                newPath = IpeUtils.toRelativePath(getBaseDir(), path);
            } else {
                newPath = IpeUtils.toAbsolutePath(getBaseDir(), path);
            }
            item = (Item) projectItems.get(FilePathAdaptor.normalize(newPath));
        }
        return item;
    }

    public Folder findFolderByPath(String path) {
        return getLogicalFolders().findFolderByPath(path);
    }

    public void addProjectItem(Item item) {
        projectItems.put(item.getPath(), item);
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_ADDED);
        //getNativeProject().fireFileAdded(item);
        setModified(true);
    }

    public void fireFilesAdded(List<NativeFileItem> fileItems) {
        getNativeProject().fireFilesAdded(fileItems);
    }

    public void removeProjectItem(Item item) {
        projectItems.remove(item.getPath());
        fireProjectItemsChangeEvent(item, ProjectItemChangeEvent.ITEM_REMOVED);
        //getNativeProject().fireFileRemoved(item);
        setModified(true);
    }

    public void fireFilesRemoved(List<NativeFileItem> fileItems) {
        if (getNativeProject() != null) {
            getNativeProject().fireFilesRemoved(fileItems);
        }
    }

    public void fireFileRenamed(String oldPath, NativeFileItem newFileItem) {
        getNativeProject().fireFileRenamed(oldPath, newFileItem);
    }

    public void checkForChangedItems(Project project, Folder folder, Item item) {
        getNativeProject().checkForChangedItems(folder, item);
        MakeLogicalViewProvider.checkForChangedItems(project, folder, item);
    }

    public void copyFromProjectDescriptor(ConfigurationDescriptor copyProjectDescriptor) {
        MakeConfigurationDescriptor copyExtProjectDescriptor = (MakeConfigurationDescriptor) copyProjectDescriptor;
        setConfs(copyExtProjectDescriptor.getConfs());
        setBaseDir(copyProjectDescriptor.getBaseDir());
        setProjectMakefileName(copyExtProjectDescriptor.getProjectMakefileName());
        setExternalFileItems(copyExtProjectDescriptor.getExternalFileItems());
        setLogicalFolders(copyExtProjectDescriptor.getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) copyProjectDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) copyProjectDescriptor).getSourceRootsRaw());
        setSourceEncoding(((MakeConfigurationDescriptor) copyProjectDescriptor).getSourceEncoding());
    }

    public void assign(ConfigurationDescriptor clonedConfigurationDescriptor) {
        Configuration[] clonedConfs = clonedConfigurationDescriptor.getConfs().getConfs();
        Configuration[] newConfs = new Configuration[clonedConfs.length];
        setBaseDir(clonedConfigurationDescriptor.getBaseDir());

        for (int i = 0; i < clonedConfs.length; i++) {
            if (clonedConfs[i].getCloneOf() != null) {
                clonedConfs[i].getCloneOf().assign(clonedConfs[i]);
                newConfs[i] = clonedConfs[i].getCloneOf();
            } else {
                newConfs[i] = clonedConfs[i];
            }
        }
        init(newConfs, clonedConfigurationDescriptor.getConfs().getActiveAsIndex());
        setProjectMakefileName(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectMakefileName());
        setExternalFileItems(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getExternalFileItems());
        setLogicalFolders(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getLogicalFolders());
        setProjectItemsMap(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsMap());
        setProjectItemsChangeListeners(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getProjectItemsChangeListeners());
        setSourceRoots(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getSourceRootsRaw());
        setSourceEncoding(((MakeConfigurationDescriptor) clonedConfigurationDescriptor).getSourceEncoding());
    }

    public ConfigurationDescriptor cloneProjectDescriptor() {
        MakeConfigurationDescriptor clone = new MakeConfigurationDescriptor(getBaseDir());
        super.cloneProjectDescriptor(clone);
        clone.setProjectMakefileName(getProjectMakefileName());
        clone.setExternalFileItems(getExternalFileItems());
        clone.setLogicalFolders(getLogicalFolders());
        clone.setProjectItemsMap(getProjectItemsMap());
        clone.setProjectItemsChangeListeners(getProjectItemsChangeListeners());
        clone.setSourceRoots(getSourceRootsRaw());
        clone.setSourceEncoding(getSourceEncoding());
        return clone;
    }

    public boolean getModified() {
        return modified;
    }

    public void setModified() {
        setModified(true);
    }

    public void setModified(boolean modified) {
        //System.out.println("setModified - " + modified);
        this.modified = modified;
        if (modified && getConfs() != null) {
            Configuration[] confs = getConfs().getConfs();
            for (int i = 0; i < confs.length; i++) {
                ((MakeConfiguration) confs[i]).setRequiredLanguagesDirty(true);
            }
        }
    }

    public void refreshRequiredLanguages() {
        if (getConfs() != null) {
            Configuration[] confs = getConfs().getConfs();
            for (int i = 0; i < confs.length; i++) {
                ((MakeConfiguration) confs[i]).reCountLanguages(this);
            }
        }
    }

    public boolean save() {
        return save(null);
    }

    public boolean save(final String extraMessage) {
        SaveRunnable saveRunnable = new SaveRunnable(extraMessage);
        RequestProcessor.Task task = RequestProcessor.getDefault().post(saveRunnable);
        task.waitFinished();
        return saveRunnable.ret;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    private class SaveRunnable implements Runnable {

        public boolean ret = false;
        private String extraMessage;

        public SaveRunnable(String extraMessage) {
            this.extraMessage = extraMessage;
        }

        public void run() {
            ret = saveWorker(extraMessage);
        }
    }

    private boolean saveWorker(String extraMessage) {
        // First check all configurations aux objects if they have changed
        Configuration[] configurations = getConfs().getConfs();
        for (int i = 0; i < configurations.length; i++) {
            Configuration conf = configurations[i];
            ConfigurationAuxObject[] auxObjects = conf.getAuxObjects();
            for (int j = 0; j < auxObjects.length; j++) {
                if (auxObjects[j].hasChanged()) {
                    setModified(true);
                }
                auxObjects[j].clearChanged();
            }
        }

        if (!getModified()) {
            return true;
        }

        // Check metadata files are writable
        Vector metadataFiles = new Vector();
        Vector notOkFiles = new Vector();
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "project.xml"); // NOI18N
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "configurations.xml"); // NOI18N
        metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "Makefile-impl.mk"); // NOI18N
        Configuration[] confs = getConfs().getConfs();
        for (int i = 0; i < confs.length; i++) {
            metadataFiles.add(getBaseDir() + File.separator + "nbproject" + File.separator + "Makefile-" + confs[i].getName() + ".mk"); // NOI18N
        } // NOI18N
        boolean allOk = true;
        for (int i = 0; i < metadataFiles.size(); i++) {
            File file = new File((String) metadataFiles.elementAt(i));
            if (!file.exists()) {
                continue;
            }
            if (!file.canWrite()) {
                allOk = false;
                notOkFiles.add(metadataFiles.elementAt(i));
            }
        }
        if (!allOk) {
            String projectName = IpeUtils.getBaseName(getBaseDir());
            String text = getString("CannotSaveTxt", projectName);
            for (int i = 0; i < notOkFiles.size(); i++) {
                text += "\n" + notOkFiles.elementAt(i); // NOI18N
            }
            if (extraMessage != null) {
                text += "\n\n" + extraMessage; // NOI18N
            }
            NotifyDescriptor d = new NotifyDescriptor.Message(text, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return allOk;
        }

        // ALl OK
        FileObject fo = null;
        fo = FileUtil.toFileObject(new File(getBaseDir()));
        if (fo != null) {
            new ConfigurationXMLWriter(fo, this).write();
            new ConfigurationMakefileWriter(this).write();
            ConfigurationProjectXMLWriter();
        }

        // Clear flag
        setModified(false);

        return allOk;
    }

    private void ConfigurationProjectXMLWriter() {
        // And save the project
        try {
            AntProjectHelper helper = ((MakeProject) getProject()).getAntProjectHelper();
            Element data = helper.getPrimaryConfigurationData(true);
            Document doc = data.getOwnerDocument();

            // Remove old node
            NodeList nodeList = data.getElementsByTagName("make-dep-projects"); // NOI18N
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    data.removeChild(node);
                }
            }
            // Create new node
            Element element = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectType.MAKE_DEP_PROJECTS);
            Set<String> subprojectLocations = getSubprojectLocations();
            for (String loc : subprojectLocations) {
                Node n1;
                n1 = doc.createElement(MakeProjectType.MAKE_DEP_PROJECT);
                n1.appendChild(doc.createTextNode(loc));
                element.appendChild(n1);
            }
            data.appendChild(element);
            helper.putPrimaryConfigurationData(data, true);
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    /**
     * Returns project locations (rel or abs) or all subprojects in all configurations.
     */
    public Set<String> getSubprojectLocations() {
        Set subProjects = new HashSet();

        Configuration[] confs = getConfs().getConfs();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
            LibrariesConfiguration librariesConfiguration = null;

            if (((MakeConfiguration) confs[i]).isLinkerConfiguration()) {
                librariesConfiguration = makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration();
                LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
                for (int j = 0; j < libraryItems.length; j++) {
                    if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                        LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) libraryItems[j];
                        subProjects.add(projectItem.getMakeArtifact().getProjectLocation());
                    }
                }
            }

            LibraryItem.ProjectItem[] projectItems = makeConfiguration.getRequiredProjectsConfiguration().getRequiredProjectItemsAsArray();
            for (int j = 0; j < projectItems.length; j++) {
                subProjects.add(projectItems[j].getMakeArtifact().getProjectLocation());
            }
        }

        return subProjects;
    }

    /*
     * Add a new root.
     * Don't add if root inside project
     * Don't add if root is subdir of existing root
     */
    private void addSourceRoot(String path) {
        String absPath = IpeUtils.toAbsolutePath(getBaseDir(), path);
        String canonicalPath = null;
        try {
            canonicalPath = new File(absPath).getCanonicalPath();
        } catch (IOException ioe) {
            canonicalPath = null;
        }
        String relPath = FilePathAdaptor.normalize(IpeUtils.toRelativePath(getBaseDir(), path));
        boolean addPath = true;
        ArrayList<String> toBeRemoved = new ArrayList<String>();

        if (IpeUtils.isPathAbsolute(relPath) || relPath.startsWith("..")) { // NOI18N
            synchronized (sourceRoots) {
                if (canonicalPath != null) {
                    int canonicalPathLength = canonicalPath.length();
                    for (String sourceRoot : sourceRoots) {
                        String absSourceRoot = IpeUtils.toAbsolutePath(getBaseDir(), sourceRoot);
                        String canonicalSourceRoot = null;
                        try {
                            canonicalSourceRoot = new File(absSourceRoot).getCanonicalPath();
                        } catch (IOException ioe) {
                            canonicalSourceRoot = null;
                        }
                        if (canonicalSourceRoot != null) {
                            int canonicalSourceRootLength = canonicalSourceRoot.length();
                            if (canonicalSourceRoot.equals(canonicalPath)) {
                                // Identical - don't add
                                addPath = false;
                                break;
                            }
                            if (canonicalSourceRoot.startsWith(canonicalPath) && canonicalSourceRoot.charAt(canonicalPathLength) == File.separatorChar) {
                                // Existing root sub dir of new path - remove existing path
                                toBeRemoved.add(sourceRoot);
                                continue;
                            }
                            if (canonicalPath.startsWith(canonicalSourceRoot) && canonicalPath.charAt(canonicalSourceRootLength) == File.separatorChar) {
                                // Sub dir of existing root - don't add
                                addPath = false;
                                break;
                            }
                        }
                    }
                }
                if (toBeRemoved.size() > 0) {
                    for (String toRemove : toBeRemoved) {
                        sourceRoots.remove(toRemove);
                    }
                }
                if (addPath) {
                    sourceRoots.add(relPath);
                    setModified();
                }
            }
        }
    }

    /*
     * Return real list
     */
    public List<String> getSourceRootsRaw() {
        synchronized (sourceRoots) {
            return sourceRoots;
        }
    }

    public void setSourceRoots(List list) {
        synchronized (sourceRoots) {
            sourceRoots = list;
        }
    }

    public void setSourceRootsList(List<String> list) {
        synchronized (sourceRoots) {
            sourceRoots.clear();
            for (String l : list) {
                addSourceRoot(l);
            }
        }
        MakeSources makeSources = (MakeSources) getProject().getLookup().lookup(MakeSources.class);
        if (makeSources != null) {
            makeSources.sourceRootsChanged();
        }
    }

    /*
     * return copy
     */
    public List<String> getSourceRoots() {
        List<String> copy;
        synchronized (sourceRoots) {
            copy = new ArrayList<String>(sourceRoots);
        }
        return copy;
    }

    /*
     * return copy and convert to absolute
     */
    public List<String> getAbsoluteSourceRoots() {
        List<String> copy = new ArrayList<String>();
        synchronized (sourceRoots) {
            for (String sr : sourceRoots) {
                copy.add(IpeUtils.toAbsolutePath(baseDir, sr));
            }
        }
        return copy;
    }
    /*
     * return copy and convert to absolute
     */

    public String[] getSourceRootsAsArray() {
        synchronized (sourceRoots) {
            String[] copy = new String[sourceRoots.size()];
            int index = 0;
            for (String sr : sourceRoots) {
                copy[index++] = sr;
            }
            return copy;
        }
    }

    private NativeProjectProvider getNativeProject() {
        if (nativeProject == null) {
            FileObject fo = FileUtil.toFileObject(new File(baseDir));
            try {
                Project project = ProjectManager.getDefault().findProject(fo);
                nativeProject = (NativeProject) project.getLookup().lookup(NativeProject.class);
            } catch (Exception e) {
                // This may be ok. The project could have been removed ....
                System.err.println("getNativeProject " + e);
            }

        }
        return (NativeProjectProvider) nativeProject;
    }

    public static class ProjectItemChangeEvent extends ChangeEvent {

        public static final int ITEM_ADDED = 0;
        public static final int ITEM_REMOVED = 1;
        private Item item;
        private int action;

        public ProjectItemChangeEvent(Object src, Item item, int action) {
            super(src);
            this.item = item;
            this.action = action;
        }

        public Item getItem() {
            return item;
        }

        public int getAction() {
            return action;
        }
    }

    public void addSourceFilesFromFolders(Iterator sourceFileFolders, boolean acrynchron, boolean notify) {
        addSourceFilesFromFolders(rootFolder, sourceFileFolders, acrynchron, notify);
    }

    public void addSourceFilesFromFolders(Folder folder, Iterator sourceFileFoldersIterator, boolean acrynchron, boolean notify) {
        if (sourceFileFoldersIterator == null) {
            return;
        }
        if (acrynchron) {
            new AddFilesThread(sourceFileFoldersIterator, folder, notify).start();
        } else {
            while (sourceFileFoldersIterator.hasNext()) {
                ArrayList filesAdded = new ArrayList();
                FolderEntry folderEntry = (FolderEntry) sourceFileFoldersIterator.next();
                Folder top = folder.findFolderByName(folderEntry.getFile().getName());
                if (top == null) {
                    top = new Folder(folder.getConfigurationDescriptor(), folder, folderEntry.getFile().getName(), folderEntry.getFile().getName(), true);
                    folder.addFolder(top);
                }
                addFiles(top, folderEntry.getFile(), folderEntry.isAddSubfoldersSelected(), folderEntry.getFileFilter(), null, filesAdded, notify);
                getNativeProject().fireFilesAdded(filesAdded);

                addSourceRoot(folderEntry.getFile().getPath());
            }
            if (notify) {
                // Notify that list has changed
                MakeSources makeSources = (MakeSources) getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
            }
        }
    }

    class AddFilesThread extends Thread {

        Iterator iterator;
        Folder folder;
        private ProgressHandle handle;
        private boolean notify;

        AddFilesThread(Iterator iterator, Folder folder, boolean notify) {
            this.iterator = iterator;
            this.folder = folder;
            this.notify = notify;
            handle = ProgressHandleFactory.createHandle(getString("AddingFilesTxt"));
        }

        public void run() {
            ArrayList filesAdded = new ArrayList();
            try {
                handle.setInitialDelay(500);
                handle.start();
                while (iterator.hasNext()) {
                    FolderEntry folderEntry = (FolderEntry) iterator.next();
                    Folder top = folder.findFolderByName(folderEntry.getFile().getName());
                    if (top == null) {
                        top = new Folder(folder.getConfigurationDescriptor(), folder, folderEntry.getFile().getName(), folderEntry.getFile().getName(), true);
                        folder.addFolder(top);
                    }
                    addFiles(top, folderEntry.getFile(), folderEntry.isAddSubfoldersSelected(), FolderEntry.getFileFilter(), handle, filesAdded, true);
                    addSourceRoot(folderEntry.getFile().getPath());
                }
            } finally {
                handle.finish();
            }
            getNativeProject().fireFilesAdded(filesAdded);
            if (notify) {
                // Notify that list has changed
                MakeSources makeSources = (MakeSources) getProject().getLookup().lookup(MakeSources.class);
                if (makeSources != null) {
                    makeSources.sourceRootsChanged();
                }
            }
        }
    }

    private void addFiles(Folder folder, File dir, boolean addSubFolders, FileFilter filter, ProgressHandle handle, ArrayList filesAdded, boolean notify) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (!filter.accept(files[i])) {
                continue;
            }
            if (files[i].isDirectory()) {
                // FIXUP: is this the best way to deal with files under version control?
                // Unfortunately the SCCS directory contains data files with the same
                // suffixes as the the source files, and a simple file filter based on
                // a file's suffix cannot see the difference between the source file and
                // the data file. Only the source file should be added.
                if (files[i].getName().equals("SCCS")) // NOI18N
                {
                    continue;
                }
                if (files[i].getName().equals(".hg")) // NOI18N
                {
                    continue;
                }
                if (files[i].getName().equals("CVS")) // NOI18N
                {
                    continue;
                }
                Folder dirfolder = folder;
                if (addSubFolders) {
                    dirfolder = folder.findFolderByName(files[i].getName());
                    if (dirfolder == null) {
                        dirfolder = folder.addNewFolder(files[i].getName(), files[i].getName(), true);
                    }
                }
                addFiles(dirfolder, files[i], addSubFolders, filter, handle, filesAdded, notify);
                if (dirfolder.size() == 0) {
                    folder.removeFolder(dirfolder);
                }
            } else {
                String filePath = IpeUtils.toRelativePath(baseDir, files[i].getPath());
                Item item = new Item(FilePathAdaptor.normalize(filePath));
                if (folder.addItem(item, notify) != null) {
                    filesAdded.add(item);
                }
                if (handle != null) {
                    handle.progress(filePath);
                }
            }
        }
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s);
    }

    private static String getString(String s, String a1) {
        return NbBundle.getMessage(MakeConfigurationDescriptor.class, s, a1);
    }
}
