/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.loaders.CCFSrcLoader;
import org.netbeans.modules.cnd.loaders.CCSrcObject;
import org.netbeans.modules.cnd.loaders.CSrcObject;
import org.netbeans.modules.cnd.loaders.FortranSrcObject;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class Item implements NativeFileItem {
    private String path;
    private String sortName;
    private Folder folder;
    private File file = null;
    
    public Item(String path) {
        this.path = path;
        sortName = IpeUtils.getBaseName(path).toLowerCase();
        int i = sortName.lastIndexOf("."); // NOI18N
        if (i > 0)
            sortName = sortName.substring(0, i);
        folder = null;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    public String getPath(boolean norm) {
        String pat = "./"; // UNIX path  // NOI18N
        if (norm && getPath().startsWith(pat))
            return getPath().substring(2);
        else
            return getPath();
    }
    
    public String getAbsPath() {
        String retPath = null;
        if (IpeUtils.isPathAbsolute(getPath()))  {// UNIX path
            retPath = getPath();
            retPath = FilePathAdaptor.mapToLocal(retPath);
        } else if (getFolder() != null) {
            retPath = getFolder().getConfigurationDescriptor().getBaseDir() + '/' + getPath(); // UNIX path
        }
        return retPath;
    }
    
    public void setFolder(Folder folder) {
        this.folder = folder;
    }
    
    public Folder getFolder() {
        return folder;
    }
    
    public File getFile() {
        if (file == null) {
            try {
                file = new File(getAbsPath()).getCanonicalFile();
            } catch (IOException ioe) {
                file = new File(getAbsPath());
            }
        }
        return file;
    }
    
    public FileObject getFileObject() {
        File file = getFile();
        FileObject fo = null;
        try {
            fo = FileUtil.toFileObject(file.getCanonicalFile());
        } catch (IOException e) {
        }
        return fo;
    }
    
    public DataObject getDataObject() {
        DataObject dataObject = null;
        FileObject fo = getFileObject();
        if (fo != null) {
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // should not happen
                ErrorManager.getDefault().notify(e);
            }
        }
        return dataObject;
    }
    
    public int getDefaultTool() {
        DataObject dataObject = getDataObject();
        int tool = Tool.CustomTool;
        if (dataObject == null) {
            String suffix = null;
            int i = path.lastIndexOf("."); // NOI18N
            if (i >= 0)
                suffix = path.substring(i+1);
            if (suffix != null) {
                if (amongSuffixes(suffix, CCFSrcLoader.getCppExtensions()))
                    tool = Tool.CCCompiler;
                else if (amongSuffixes(suffix, CCFSrcLoader.getCExtensions()))
                    tool = Tool.CCompiler;
                else if (MakeOptions.getInstance().getFortran() && amongSuffixes(suffix, CCFSrcLoader.getFortranExtensions()))
                    tool = Tool.FortranCompiler;
                else
                    tool = Tool.CustomTool;
            } else {
                tool = Tool.CustomTool;
            }
        } else if (dataObject instanceof CSrcObject)
            tool = Tool.CCompiler;
        else if (dataObject instanceof HDataObject)
            tool = Tool.CustomTool;
        else if (dataObject instanceof CCSrcObject)
            tool = Tool.CCCompiler;
        else if (MakeOptions.getInstance().getFortran() && dataObject instanceof FortranSrcObject)
            tool = Tool.FortranCompiler;
        else
            tool = Tool.CustomTool;
        return tool;
    }
    
    private boolean amongSuffixes(String suffix, String[] suffixes) {
        for (int i = 0; i < suffixes.length; i++) {
            if (suffixes[i].equals(suffix))
                return true;
        }
        return false;
    }
    
    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return (MakeConfigurationDescriptor)getFolder().getConfigurationDescriptor();
    }
    
    private MakeConfiguration getMakeConfiguration() {
        return (MakeConfiguration)getMakeConfigurationDescriptor().getConfs().getActive();
    }
    
    public NativeProject getNativeProject() {
        Project project = getFolder().getProject();
        return (NativeProject)project.getLookup().lookup(NativeProject.class);
    }
    
    public List getSystemIncludePaths() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            vec.addAll(compiler.getSystemIncludeDirectories(platform));
        }
        return vec;
    }
    
    public List getUserIncludePaths() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            Vector vec2 = new Vector();
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            if (cccCompilerConfiguration.getMaster() != null && cccCompilerConfiguration.getInheritIncludes().getValue())
                vec2.addAll(((CCCCompilerConfiguration)cccCompilerConfiguration.getMaster()).getIncludeDirectories().getValue());
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator iter = vec2.iterator();
            while (iter.hasNext()) {
                vec.add(IpeUtils.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), (String)iter.next()));
        }
        }
        return vec;
    }
    
    public List getSystemMacroDefinitions() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get macro definitions from compiler
            vec.addAll(compiler.getSystemPreprocessorSymbols(platform));
        }
        return vec;
    }
    
    public List getUserMacroDefinitions() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // get macro definitions from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            if (cccCompilerConfiguration.getMaster() != null && cccCompilerConfiguration.getInheritPreprocessor().getValue())
                vec.addAll(((CCCCompilerConfiguration)cccCompilerConfiguration.getMaster()).getPreprocessorConfiguration().getValuesAsVector());
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValuesAsVector());
        }
        return vec;
    }
}
