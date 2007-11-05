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
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.Utils;
import org.netbeans.modules.versioning.FlatFolder;
import org.netbeans.modules.versioning.VersioningManager;
import org.netbeans.modules.versioning.Accessor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;

/**
 * This encapsulates a context, typically set of selected files or nodes. Context is passed to VCSAnnotators when
 * asked for actions available on a given context or to annotate a name (label) representing a context.
 * 
 * @author Maros Sandor
 */
public final class VCSContext {
    
    /**
     * VCSContext that contains no files.
     */
    public static final VCSContext EMPTY = new VCSContext(null, emptySet(), emptySet() );

    /**
     * Caching of current context for performance reasons, also see #72006.
     */
    private static Reference<VCSContext>  contextCached = new WeakReference<VCSContext>(null);    
    private static Reference<Node[]> contextNodesCached = new WeakReference<Node []>(null); 

    private final Lookup    elements;
    
    private final Set<File> rootFiles;
    private final Set<File> exclusions;

    private Set<File>       computedFilesCached;
    private FileFilter      fileFilterCached;

    static {
        Accessor.VCSContextAccessor = new AccessorImpl();
    }
    
    /**
     * Constructs a VCSContext out of a set of files. These files are later available via getRootFiles().
     * 
     * @param rootFiles set of Files
     * @return VCSContext a context representing supplied set of Files
     */ 
    static VCSContext forFiles(Set<File> rootFiles) {
        return new VCSContext(null, rootFiles, emptySet());
    }

    /**
     * Initializes the context from array of nodes (typically currently activated nodes).
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become root files of this context and
     * exclusions list is constructed using sourceRoot.contains() queries.
     * 
     * Nodes' lookups are examined in the following way (the first applied rule wins):
     * - if there's a File, the File is added to set of root files
     * - if there's a Project, project's source roots of type Sources.TYPE_GENERIC are added to set of root files and
     *   all direct children that do not belong to the project (sg.contains() == false) are added to set of exclusions
     * - if there's a FileObject, it is added to set of root files
     * - if there's a DataObject, all dao.files() are added to set of root files 
     * 
     * @param nodes array of Nodes
     * @return VCSContext containing nodes and corresponding files they represent
     */
    public synchronized static VCSContext forNodes(Node[] nodes) {
        if (Arrays.equals(contextNodesCached.get(), nodes)) {
            VCSContext ctx = contextCached.get();
            if (ctx != null) return ctx;
        }
        Set<File> files = new HashSet<File>(nodes.length);
        Set<File> rootFiles = new HashSet<File>(nodes.length);
        Set<File> rootFileExclusions = new HashSet<File>(5);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            File aFile = node.getLookup().lookup(File.class);
            if (aFile != null) {
                files.add(aFile);
                rootFiles.add(aFile);
                continue;
            }
            Project project =  node.getLookup().lookup(Project.class);
            if (project != null) {
                addProjectFiles(rootFiles, rootFileExclusions, project);
                continue;
            }
            addFileObjects(node, files, rootFiles);
        }
        
        VCSContext ctx = new VCSContext(nodes, rootFiles, rootFileExclusions);
        contextCached = new WeakReference<VCSContext>(ctx);
        contextNodesCached = new WeakReference<Node []>(nodes);
        return ctx;
    }
        
    /**
     * Returns the smallest possible set of all files that lie under Root files and are NOT 
     * under some Excluded file. 
     * Technically, for every file in the returned set all of the following is true:
     * 
     * - the file itself or at least one of its ancestors is a root file/folder
     * - neither the file itself nor any of its ancestors is an exluded file/folder
     * - the file passed through the supplied FileFilter
     *  
     * @param filter custom file filter
     * @return filtered set of files that must pass through the filter
     */
    public synchronized Set<File> computeFiles(FileFilter filter) {
        if (computedFilesCached == null || filter != fileFilterCached) {
            computedFilesCached = substract(rootFiles, exclusions, filter);
            fileFilterCached = filter;
        }
        return computedFilesCached;
    }
    
    /**
     * Retrieves elements that make up this VCS context. The returned lookup may be empty
     * or may contain any number of the following elements:
     * - instances of Node that were originally used to construct this context object
     *
     * @return Lookup lookup of this VCSContext
     */ 
    public Lookup getElements() {
        return elements;
    }

    /**
     * Retrieves set of files/folders that represent this context.
     * 
     * @return Set<File> set of Files this context represents
     */ 
    public Set<File> getRootFiles() {
        return rootFiles;
    }

    /**
     * Retrieves set of files/folders that are excluded from this context. Exclusions are files or folders that
     * are descendants of a root folder and should NOT be a part of a versioning operation. For example, an CVS/Update command
     * run on a project that contains a subproject should not touch any files in the subproject. Therefore the VCSContext for
     * the action would contain one root file (the project's root) and one exclusion (subproject root).
     * 
     * @return Set<File> set of files and folders that are not part of (are excluded from) this context. 
     * All their descendands are excluded too.
     */ 
    public Set<File> getExclusions() {
        return exclusions;
    }

    /**
     * Determines whether the supplied File is contained in this context. In other words, the file must be either a root file/folder
     * or be a descendant of a root folder and also must NOT be an excluded file/folder or be a descendant of an excluded folder. 
     * 
     * @param file a File to test
     * @return true if this context contains the supplied file, false otherwise 
     */ 
    public boolean contains(File file) {
        outter : for (File root : rootFiles) {
            if (Utils.isAncestorOrEqual(root, file)) {
                for (File excluded : exclusions) {
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
        
    private static void addProjectFiles(Collection<File> rootFiles, Collection<File> rootFilesExclusions, Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        List<File> unversionedFiles = new ArrayList<File>(sourceGroups.length);
        Set<VersioningSystem> projectOwners = new HashSet<VersioningSystem>(2);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            VersioningSystem owner = VersioningManager.getInstance().getOwner(rootFile);
            if (owner == null) {
                unversionedFiles.add(rootFile);
            } else {
                projectOwners.add(owner);
            }
            rootFiles.add(rootFile);
            FileObject [] rootChildren = srcRootFo.getChildren();
            for (int i = 0; i < rootChildren.length; i++) {
                FileObject rootChildFo = rootChildren[i];
                File child = FileUtil.toFile(rootChildFo);
                // TODO: #60516 deep scan is required here but not performed due to performace reasons 
                if (child != null && !sourceGroup.contains(rootChildFo) && SharabilityQuery.getSharability(child) != SharabilityQuery.NOT_SHARABLE) {
                    rootFilesExclusions.add(child);
                }
            }
        }
        if (projectOwners.size() == 1 && projectOwners.iterator().next() != null) {
            rootFiles.removeAll(unversionedFiles);
            outter: for (Iterator<File> i = rootFilesExclusions.iterator(); i.hasNext(); ) {
                File exclusion = i.next();
                for (File rootFile : rootFiles) {
                    if (Utils.isAncestorOrEqual(rootFile, exclusion)) {
                        continue outter;
                    }
                }
                i.remove();
            }
        }
    }
    
    private static void addFileObjects(Node node, Set<File> files, Set<File> rootFiles) {
        Collection<? extends NonRecursiveFolder> folders = node.getLookup().lookup(new Lookup.Template<NonRecursiveFolder>(NonRecursiveFolder.class)).allInstances();
        List<File> nodeFiles = new ArrayList<File>();
        if (folders.size() > 0) {
            for (Iterator j = folders.iterator(); j.hasNext();) {
                NonRecursiveFolder nonRecursiveFolder = (NonRecursiveFolder) j.next();
                File file = FileUtil.toFile(nonRecursiveFolder.getFolder());
                if (file != null) {
                    nodeFiles.add(new FlatFolder(file.getAbsolutePath()));
                }
            }
        } else {
            Collection<? extends FileObject> fileObjects = node.getLookup().lookup(new Lookup.Template<FileObject>(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                nodeFiles.addAll(toFileCollection(fileObjects));
            } else {
                DataObject dataObject = node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    Collection<File> doFiles = toFileCollection(dataObject.files());
                    nodeFiles.addAll(doFiles);
                }
            }
        }
        files.addAll(nodeFiles);
        rootFiles.addAll(nodeFiles);
    }
    
    private static Collection<File> toFileCollection(Collection<? extends FileObject> fileObjects) {
        Set<File> files = new HashSet<File>(fileObjects.size()*4/3+1);
        for (FileObject fo : fileObjects) {
            files.add(FileUtil.toFile(fo));
        }
        files.remove(null);
        return files;
    }    

    private VCSContext(Node [] nodes, Set<File> rootFiles, Set<File> exclusions) {
        this.elements = nodes != null ? Lookups.fixed(nodes) : Lookups.fixed(new Node[0]);
        Set<File> tempRootFiles = new HashSet<File>(rootFiles);
        Set<File> tempExclusions = new HashSet<File>(exclusions);
        while (normalize(tempRootFiles, tempExclusions));
        this.rootFiles = Collections.unmodifiableSet(tempRootFiles);
        this.exclusions = Collections.unmodifiableSet(tempExclusions);
    }

    private boolean normalize(Set<File> rootFiles, Set<File> exclusions) {
        for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            for (Iterator j = exclusions.iterator(); j.hasNext();) {
                File exclusion = (File) j.next();
                if (Utils.isAncestorOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusions, exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }
    
    private void exclusionRemoved(Set<File> exclusions, File exclusion, File root) {
        File [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            File child = exclusionChildren[i];
            if (!Utils.isAncestorOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    private static Set<File> substract(Set<File> roots, Set<File> exclusions, FileFilter filter) {
        Set<File> files = new HashSet<File>(roots);
        for (File exclusion : exclusions) {
            assert exclusion != null;
            for (;;) {
                addSiblings(files, exclusion, filter);
                exclusion = exclusion.getParentFile();
                files.remove(exclusion);
                if (roots.contains(exclusion)) break;
            }
        }
        files.removeAll(exclusions);
        return files;
    }

    private static void addSiblings(Set<File> files, File exclusion, FileFilter filter) {
        if (exclusion.getParentFile() == null) return;  // roots have no siblings
        File [] siblings = exclusion.getParentFile().listFiles();
        for (File sibling : siblings) {
            if (filter.accept(sibling)) files.add(sibling);
        }
        files.remove(exclusion);
    }

    private static final Set<File> emptySet() {
        return Collections.emptySet();
    }

    private void removeDuplicates(Set<File> files) {
        List<File> newFiles = new ArrayList<File>();
        outter: for (Iterator<File> i = files.iterator(); i.hasNext();) {
            File file = i.next();
            for (Iterator<File> j = newFiles.iterator(); j.hasNext();) {
                File includedFile = j.next();
                if (Utils.isAncestorOrEqual(includedFile, file) && (file.isFile() || !VersioningSupport.isFlat(includedFile))) continue outter;
                if (Utils.isAncestorOrEqual(file, includedFile) && (includedFile.isFile() || !VersioningSupport.isFlat(file))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
}
