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

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ExtensionList;
import org.openide.util.RequestProcessor;

final public class NativeProjectProvider implements NativeProject, PropertyChangeListener {
    private Project project;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private Set<NativeProjectItemsListener> listeners = new HashSet<NativeProjectItemsListener>();
    
    
    public NativeProjectProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }
    
    private void addMyListeners() {
        if (getMakeConfigurationDescriptor() != null)
            getMakeConfigurationDescriptor().getConfs().addPropertyChangeListener(this);
    }
    
    private void removeMyListeners() {
        if (getMakeConfigurationDescriptor() != null)
            getMakeConfigurationDescriptor().getConfs().removePropertyChangeListener(this);
    }
    
    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return (MakeConfigurationDescriptor)projectDescriptorProvider.getConfigurationDescriptor();
    }
    
    private MakeConfiguration getMakeConfiguration() {
	MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
	if (descriptor != null) {
	    Configurations confs = descriptor.getConfs();
	    if (confs != null) {
                return (MakeConfiguration)confs.getActive();
	    }
	}
	return null;
    }

    public Object getProject() {
        return this.project;
    }

    public List<String> getSourceRoots() {
	MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
	if (descriptor != null) {
            return descriptor.getAbsoluteSourceRoots();
//            List<String> res = new ArrayList<String>(1);
//            res.add(descriptor.getBaseDir());
//            return res;
	}
	return Collections.<String>emptyList();
    }
    
    public String getProjectRoot() {
        return FileUtil.toFile(project.getProjectDirectory()).getPath();
    }
    
    public String getProjectDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
    
//    public List<NativeFileItem> getAllSourceFiles() {
//        ArrayList list = new ArrayList();
//        if (getMakeConfigurationDescriptor() == null || getMakeConfiguration() == null)
//            return list;
//        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
//        for (int i = 0; i < items.length; i++) {
//            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
//            if (itemConfiguration != null && itemConfiguration.isCompilerToolConfiguration() && !itemConfiguration.getExcluded().getValue())
//                list.add(items[i]);
//        }
//        return list;
//    }
//    
//    public List<NativeFileItem> getAllHeaderFiles() {
//        ArrayList list = new ArrayList();
//        if (getMakeConfigurationDescriptor() == null || getMakeConfiguration() == null)
//            return list;
//        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
//        for (int i = 0; i < items.length; i++) {
//	    if (items[i].hasHeaderOrSourceExtension(true, true)) {
//		ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration());
//		if (itemConfiguration != null && !itemConfiguration.isCompilerToolConfiguration()){
//		    if (!itemConfiguration.getExcluded().getValue()){
//			list.add(items[i]);
//		    }
//		}		
//	    }
//        }
//        return list;
//    }

    public List<NativeFileItem> getAllFiles() {
        List<NativeFileItem> list = new ArrayList<NativeFileItem>();
        if (getMakeConfigurationDescriptor() == null || getMakeConfiguration() == null) {
            return list;
        }
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration());
            if (itemConfiguration != null){
                if (itemConfiguration.isCompilerToolConfiguration()){
                    list.add(items[i]);
                } else if (items[i].hasHeaderOrSourceExtension(true, true)){
                    list.add(items[i]);
                }
	    }
        }
        return list;
    }
    
    public List<NativeProject> getDependences(){
        List<NativeProject> list = new ArrayList<NativeProject>();
	MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            for (Object lib : makeConfiguration.getSubProjects()){
                Project prj = (Project)lib;
                NativeProject nativeProject = (NativeProject)prj.getLookup().lookup(NativeProject.class);
                if (nativeProject != null){
                    list.add(nativeProject);
                }
            }
        }
        return list;
    }
    
    public void addProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            if (listeners.size() == 0) {
                addMyListeners();
            }
            listeners.add(listener);
        }
    }
    
    public void removeProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.size() == 0) {
                removeMyListeners();
            }
        }
    }
    
    public void fireFilesAdded(List<NativeFileItem> nativeFileIetms) {
        //System.out.println("fireFileAdded ");
        ArrayList actualList = new ArrayList();
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        // Remove non C/C++ items
        Iterator iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = (NativeFileItem)iter.next();
            int tool = ((Item)nativeFileIetm).getDefaultTool();
            if (tool == Tool.CustomTool && !hlist.isRegistered(((Item)nativeFileIetm).getPath()))
                continue; // IZ 87407
            actualList.add(nativeFileIetm);
            //System.out.println("    " + ((Item)nativeFileIetm).getPath());
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                if (actualList.size() == 1)
                    listener.fileAdded((NativeFileItem)actualList.get(0));
                else
                    listener.filesAdded(actualList);
            }
        }
    }
    
    public void fireFilesRemoved(List<NativeFileItem> nativeFileIetms) {
        //System.out.println("fireFilesRemoved ");
        ArrayList actualList = new ArrayList();
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        // Remove non C/C++ items
        Iterator iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = (NativeFileItem)iter.next();
            ItemConfiguration itemConfiguration = ((Item)nativeFileIetm).getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(((Item)nativeFileIetm).getPath()));
            if (itemConfiguration == null)
                continue;
            if ((!itemConfiguration.isCompilerToolConfiguration() && !hlist.isRegistered(((Item)nativeFileIetm).getPath())) /*|| itemConfiguration.getExcluded().getValue()*/)
                continue; // IZ 87407
            actualList.add(nativeFileIetm);
            //System.out.println("    " + ((Item)nativeFileIetm).getPath());
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                if (actualList.size() == 1)
                    listener.fileRemoved((NativeFileItem)actualList.get(0));
                else
                    listener.filesRemoved(actualList);
            }
        }
    }

    public void fireFileRenamed(String oldPath, NativeFileItem newNativeFileIetm) {
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.fileRenamed(oldPath, newNativeFileIetm);
        }
    }

    public void fireFilePropertiesChanged(NativeFileItem nativeFileIetm) {
        //System.out.println("fireFilePropertiesChanged " + nativeFileIetm.getFile());
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filePropertiesChanged(nativeFileIetm);
        }
    }
    
    public void fireFilesPropertiesChanged(List<NativeFileItem> fileItems) {
        //System.out.println("fireFilesPropertiesChanged " + fileItems);
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged(fileItems);
        }
    }
    
    public void fireFilesPropertiesChanged() {
        //System.out.println("fireFilesPropertiesChanged ");
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged();
        }
    }
    
    public void fireProjectDeleted() {
        //System.out.println("fireProjectDeleted ");
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.projectDeleted(this);
        }
    }
    
    private List<NativeProjectItemsListener> getListenersCopy() {
        synchronized (listeners) {
	    return (listeners.size() == 0) ? Collections.EMPTY_LIST : new ArrayList<NativeProjectItemsListener>(listeners);
        }
    }
    
    public NativeFileItem findFileItem(File file) {
        MakeConfigurationDescriptor descr = getMakeConfigurationDescriptor();
        if (descr != null) {
            return (NativeFileItem)descr.findItemByFile(file);
        }
        return null;
    }
    
    private void checkConfigurationChanged(final Configuration oldConf, final Configuration newConf) {
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                checkConfigurationChangedWorker(oldConf, newConf);
            }
        });
    }
    
    private void checkConfigurationChangedWorker(Configuration oldConf, Configuration newConf) {
        MakeConfiguration oldMConf = (MakeConfiguration)oldConf;
        MakeConfiguration newMConf = (MakeConfiguration)newConf;
        List<NativeFileItem> list = new ArrayList<NativeFileItem>();
        List<NativeFileItem> added = new ArrayList<NativeFileItem>();
        List<NativeFileItem> deleted = new ArrayList<NativeFileItem>();
        
        synchronized (listeners) {
            if (listeners.size() == 0)
                return;
        }
        
        if (newConf == null) {
            // How can this happen?
            System.err.println("Nativeprojectprovider - checkConfigurationChanged - newConf is null!"); // NOI18N
            return;
        }
        
        if (!newConf.isDefault())
            return;
        
        if (oldConf == null) {
            // What else can we do?
            firePropertiesChanged(getMakeConfigurationDescriptor().getProjectItems(), true, true, true);
            return;
        }
        
        // Check compiler collection. Fire if different (IZ 131825)
        if (!oldMConf.getCompilerSet().getName().equals(newMConf.getCompilerSet().getName())) {
            fireFilesPropertiesChanged(); // firePropertiesChanged(getAllFiles(), true);
            return;
        }
        
        // Check all items
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        Project project = getMakeConfigurationDescriptor().getProject();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = items[i].getItemConfiguration(oldMConf); //ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = items[i].getItemConfiguration(newMConf); //ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (oldItemConf == null || newItemConf == null) {
                continue;
            }

            if ((newItemConf.getExcluded().getValue() ^ oldItemConf.getExcluded().getValue()) &&
                (newItemConf.getTool() == Tool.CCompiler ||
                 newItemConf.getTool() == Tool.CCCompiler ||
                 items[i].hasHeaderOrSourceExtension(true,true))) {
                if (newItemConf.getExcluded().getValue()) {
                    // excluded
                    deleted.add(items[i]);
                } else {
                    // included
                    added.add(items[i]);
                }
                MakeLogicalViewProvider.checkForChangedItems(project, null, items[i]);
            }
            
            if (newItemConf.getExcluded().getValue()){
                continue;
            }
            
            if (newItemConf.getTool() == Tool.CCompiler) {
                if (!oldItemConf.getCCompilerConfiguration().getPreprocessorOptions().equals(newItemConf.getCCompilerConfiguration().getPreprocessorOptions())) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions().equals(newItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions())) {
                    list.add(items[i]);
                    continue;
                }
            }
            if (newItemConf.getTool() == Tool.CCCompiler) {
                if (!oldItemConf.getCCCompilerConfiguration().getPreprocessorOptions().equals(newItemConf.getCCCompilerConfiguration().getPreprocessorOptions())) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions().equals(newItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions())) {
                    list.add(items[i]);
                    continue;
                }
            }
        }
        fireFilesRemoved(deleted);
        fireFilesAdded(added);
        firePropertiesChanged(list, true);
    }
    
    public void checkForChangedItems(final Folder folder, final Item item) {
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                checkForChangedItemsWorker(folder, item);
            }
        });
    }
    
    private void checkForChangedItemsWorker(Folder folder, Item item) {
        synchronized (listeners) {
            if (listeners.size() == 0)
                return;
        }
        
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        boolean cFiles = false;
        boolean ccFiles = false;
        boolean libsChanged = false;
        boolean projectChanged = false;
        VectorConfiguration cIncludeDirectories;
        BooleanConfiguration cInheritIncludes;
        VectorConfiguration cPpreprocessorOption;
        BooleanConfiguration cInheritMacros;
        VectorConfiguration ccIncludeDirectories;
        BooleanConfiguration ccInheritIncludes;
        VectorConfiguration ccPreprocessorOption;
        BooleanConfiguration ccInheritMacros;
        Item[] items;
        
        // Check first whether compiler set has changed
        if (makeConfiguration.getCompilerSet().getDirty()) {
            makeConfiguration.getCompilerSet().setDirty(false);
            items = getMakeConfigurationDescriptor().getProjectItems();
            firePropertiesChanged(items, true, true, true);
        }
        
        if (folder != null) {
            FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
            cIncludeDirectories = folderConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = folderConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cPpreprocessorOption = folderConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cInheritMacros = folderConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            ccIncludeDirectories = folderConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = folderConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccPreprocessorOption = folderConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = folderConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            items = folder.getAllItemsAsArray();
        } else if (item != null) {
            ItemConfiguration itemConfiguration = item.getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(item.getPath()));
            cIncludeDirectories = itemConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = itemConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cInheritMacros = itemConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            cPpreprocessorOption = itemConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            ccIncludeDirectories = itemConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = itemConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccPreprocessorOption = itemConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = itemConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            if (itemConfiguration.getExcluded().getDirty()) {
                itemConfiguration.getExcluded().setDirty(false);
                ArrayList<NativeFileItem> list = new ArrayList();
                list.add(item);
                if (itemConfiguration.getExcluded().getValue()){
                    fireFilesRemoved(list);
                } else {
                    fireFilesAdded(list);
                }
            }
            items = new Item[] {item};
        } else {
            libsChanged = makeConfiguration.getRequiredProjectsConfiguration().getDirty() ||
                          makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().getDirty();
            cIncludeDirectories = makeConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = makeConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cPpreprocessorOption = makeConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cInheritMacros = makeConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            ccIncludeDirectories = makeConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = makeConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccPreprocessorOption = makeConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = makeConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            items = getMakeConfigurationDescriptor().getProjectItems();
            projectChanged = true;
        }
        
        if (cIncludeDirectories.getDirty() || cPpreprocessorOption.getDirty() ||
            cInheritIncludes.getDirty() || cInheritMacros.getDirty()) {
            cFiles = true;
            cIncludeDirectories.setDirty(false);
            cPpreprocessorOption.setDirty(false);
            cInheritIncludes.setDirty(false);
            cInheritMacros.setDirty(false);
        }
        if (ccIncludeDirectories.getDirty() || ccPreprocessorOption.getDirty() ||
            ccInheritIncludes.getDirty() || ccInheritMacros.getDirty()) {
            ccFiles = true;
            ccIncludeDirectories.setDirty(false);
            ccPreprocessorOption.setDirty(false);
            ccInheritIncludes.setDirty(false);
            ccInheritMacros.setDirty(false);
        }
        if (libsChanged){
            makeConfiguration.getRequiredProjectsConfiguration().setDirty(false);
            makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().setDirty(false);
            cFiles = true;
            ccFiles = true;
        }
        if (cFiles || ccFiles)
            firePropertiesChanged(items, cFiles, ccFiles, projectChanged);
    }
    
    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged) {
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>();
        ArrayList<NativeFileItem> deleted = new ArrayList<NativeFileItem>();
        // Handle project and file level changes
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration.getExcluded().getValue()){
                deleted.add(items[i]);
                continue;
            }
            if ((cFiles && itemConfiguration.getTool() == Tool.CCompiler) ||
                (ccFiles && itemConfiguration.getTool() == Tool.CCCompiler) ||
                items[i].hasHeaderOrSourceExtension(cFiles, ccFiles)) {
                list.add(items[i]);
            }
        }
        if (deleted.size() > 0){
            fireFilesRemoved(deleted);
        }
        firePropertiesChanged(list, projectChanged);
    }
    
    private void firePropertiesChanged(List<NativeFileItem> list, boolean projectChanged) {
        if (list.size() > 1 || (projectChanged && list.size() == 1)) {
            fireFilesPropertiesChanged(list);
        } else if (list.size() == 1) {
            fireFilePropertiesChanged((NativeFileItem)list.get(0));
        } else {
            // nothing
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println("propertyChange " + evt.getPropertyName());
        if (evt.getPropertyName().equals(Configurations.PROP_ACTIVE_CONFIGURATION))
            checkConfigurationChanged((Configuration)evt.getOldValue(), (Configuration)evt.getNewValue());
    }
    
    /**
     * Returns a list <String> of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    public List<String> getSystemIncludePaths() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(makeConfiguration.getCompilerSet().getValue());
            BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            if (compiler != null) {
                vec.addAll(compiler.getSystemIncludeDirectories());
            }
        }
        return vec;
    }
    
    /**
     * Returns a list <String> of user defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    public List<String> getUserIncludePaths() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            ArrayList vec2 = new ArrayList();
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator iter = vec2.iterator();
            while (iter.hasNext()) {
                vec.add(IpeUtils.toAbsolutePath(makeConfiguration.getBaseDir(), (String)iter.next()));
            }
        }
        return vec;
    }
    
    /**
     * Returns a list <String> of compiler defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    public List<String> getSystemMacroDefinitions() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(makeConfiguration.getCompilerSet().getValue());
            BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            if (compiler != null) {
                vec.addAll(compiler.getSystemPreprocessorSymbols());
            }
        }
        return vec;
    }
    
    /**
     * Returns a list <String> of user defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    public List<String> getUserMacroDefinitions() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValue());
        }
        return vec;
    }
}
