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

package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;

public class ConfigurationMakefileWriter {
    private MakeConfigurationDescriptor projectDescriptor;
    
    public ConfigurationMakefileWriter(MakeConfigurationDescriptor projectDescriptor) {
        this.projectDescriptor = projectDescriptor;
    }
    
    public void write() {
	cleanup();
        writeMakefileImpl();
        Configuration[] confs = projectDescriptor.getConfs().getConfs();
        for (int i = 0; i < confs.length; i++)
            writeMakefileConf((MakeConfiguration)confs[i]);
    }

    private void cleanup() {
	// Remove all Makefile-* files
	File folder = new File(projectDescriptor.getBaseDir() + '/' + "nbproject"); // UNIX path // NOI18N
	File[] children = folder.listFiles();
	for (int i = 0; i < children.length; i++) {
	    if (children[i].getName().startsWith("Makefile-")) { // NOI18N
		children[i].delete();
	    }
	}
    }
    
    private void writeMakefileImpl() {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            URL url = new URL("nbresloc:/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile-impl.mk"); // NOI18N
            is = url.openStream();
            String outputFileName = projectDescriptor.getBaseDir() + '/' + "nbproject" + '/' + MakeConfiguration.MAKEFILE_IMPL; // UNIX path // NOI18N
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }
        if (is == null || os == null) {
            // FIXUP: ERROR
            return;
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        
        // Project name
        String projectName = IpeUtils.getBaseName(projectDescriptor.getBaseDir());
        
        // Configurations
        StringBuilder configurations = new StringBuilder();
        for (int i = 0; i < projectDescriptor.getConfs().getConfs().length; i++) {
            configurations.append(projectDescriptor.getConfs().getConfs()[i].getName());
            configurations.append(" "); // NOI18N
        }
        
        try {
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                if (line.indexOf("<PN>") >= 0) { // NOI18N
                    line = line.replaceFirst("<PN>", projectName); // NOI18N
                } else if (line.indexOf("<CNS>") >= 0) { // NOI18N
                    line = line.replaceFirst("<CNS>", configurations.toString()); // NOI18N
                } else if (line.indexOf("<CN>") >= 0) { // NOI18N
                    line = line.replaceFirst("<CN>", projectDescriptor.getConfs().getConf(0).getName()); // NOI18N
                }
                bw.write(line + "\n"); // NOI18N
            }
            br.close();
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }
        
    }
    
    private void writeMakefileConf(MakeConfiguration conf) {
        String outputFileName = projectDescriptor.getBaseDir() + '/' + "nbproject" + '/' + "Makefile-" + conf.getName() + ".mk"; // UNIX path // NOI18N
        
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            writePrelude(conf, bw);
            writeBuildTarget(conf, bw);
            writeCleanTarget(conf, bw);
            writeDependencyChecking(conf, bw);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // FIXUP
        }
    }
    
    private void writePrelude(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        CCCCompilerConfiguration cCompilerConfiguration = conf.getCCompilerConfiguration();
        CCCCompilerConfiguration ccCompilerConfiguration = conf.getCCCompilerConfiguration();
        FortranCompilerConfiguration fortranCompilerConfiguration = conf.getFortranCompilerConfiguration();
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()); // GRP - 
        BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
        BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        BasicCompiler fortranCompiler = (BasicCompiler)compilerSet.getTool(Tool.FortranCompiler);
        String cCompilerName = ""; // NOI18N
        String ccCompilerName = ""; // NOI18N
        String fortranCompilerName = ""; // NOI18N
        if (cCompiler != null) {
            if (cCompilerConfiguration.getTool().getModified())
                cCompilerName = cCompilerConfiguration.getTool().getValue();
            else
                cCompilerName = cCompiler.getName();
        }
        if (ccCompiler != null) {
            if (ccCompilerConfiguration.getTool().getModified())
                ccCompilerName = ccCompilerConfiguration.getTool().getValue();
            else {
                ccCompilerName = ccCompiler.getName();
            }
        }
        if (fortranCompiler != null) {
            if (fortranCompilerConfiguration.getTool().getModified())
                fortranCompilerName = fortranCompilerConfiguration.getTool().getValue();
            else
                fortranCompilerName = fortranCompiler.getName();
        }
        
        bw.write("#\n"); // NOI18N
        bw.write("# Gererated Makefile - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Edit the Makefile in the project folder instead (../Makefile). Each target\n"); // NOI18N
        bw.write("# has a -pre and a -post target defined where you can add customized code.\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# This makefile implements configuration specific macros and targets.\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Environment\n"); // NOI18N
        bw.write("MKDIR=mkdir\n"); // NOI18N
        bw.write("CP=cp\n"); // NOI18N
        bw.write("CCADMIN=CCadmin\n"); // NOI18N
        bw.write("RANLIB=ranlib\n"); // NOI18N
        bw.write("CC=" + cCompilerName + "\n"); // NOI18N
        bw.write("CCC=" + ccCompilerName + "\n"); // NOI18N
        bw.write("CXX=" + ccCompilerName + "\n"); // NOI18N
        bw.write("FC=" + fortranCompilerName + "\n"); // NOI18N
	if (conf.getArchiverConfiguration().getTool().getModified())
	    bw.write("AR=" + conf.getArchiverConfiguration().getTool().getValue() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Include project Makefile\n"); // NOI18N
        bw.write("include " + projectDescriptor.getProjectMakefileName() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Object Directory\n"); // NOI18N
        bw.write(MakeConfiguration.OBJECTDIR_MACRO_NAME + "=" + getObjectDir(conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Object Files\n"); // NOI18N
        bw.write("OBJECTFILES=" + getObjectFiles(projectDescriptor, conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        if (cCompiler != null) {
            bw.write("# C Compiler Flags\n"); // NOI18N
            bw.write("CFLAGS=" +conf.getCCompilerConfiguration().getCFlags(cCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (ccCompiler != null) {
            bw.write("# CC Compiler Flags\n"); // NOI18N
            bw.write("CCFLAGS=" +conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("CXXFLAGS=" +conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (fortranCompiler != null) {
            bw.write("# Fortran Compiler Flags\n"); // NOI18N
            bw.write("FFLAGS=" +conf.getFortranCompilerConfiguration().getFFlags(fortranCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        bw.write("# Link Libraries and Options\n"); // NOI18N
        bw.write("LDLIBSOPTIONS=" + conf.getLinkerConfiguration().getLibraryItems() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
    }
    
    private void writeBuildTarget(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        String output = getOutput(conf);
        bw.write("# Build Targets\n"); // NOI18N
        if (conf.isCompileConfiguration()) {
            bw.write(".build-conf: " + "${BUILD_SUBPROJECTS} " + output + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
            if (hasSubprojects(conf)) {
                bw.write(output + ": " + "${BUILD_SUBPROJECTS}" + "\n"); // NOI18N
                bw.write("\n"); // NOI18N
            }
            if (conf.isLinkerConfiguration())
                writeLinkTarget(conf, bw, output);
            if (conf.isArchiverConfiguration())
                writeArchiveTarget(conf, bw, output);
            if (conf.isCompileConfiguration())
                writeCompileTargets(conf, bw);
        }
        else if (conf.isMakefileConfiguration()) {
            bw.write(".build-conf: " + "${BUILD_SUBPROJECTS} " + "\n"); // NOI18N
            writeMakefileTargets(conf, bw);
        }
        writeSubProjectBuildTargets(conf, bw);
        bw.write("\n"); // NOI18N
    }
    
    private void writeLinkTarget(MakeConfiguration conf, BufferedWriter bw, String output) throws IOException {
        LinkerConfiguration linkerConfiguration = conf.getLinkerConfiguration();
        String command = ""; // NOI18N
	if (linkerConfiguration.getTool().getModified())
	    command += linkerConfiguration.getTool().getValue() + " "; // NOI18N
//	else if (conf.isDynamicLibraryConfiguration()) 
//	    command += "${CCC}" + " "; // NOI18N
	else if (conf.hasCPPFiles(projectDescriptor))
	    command += "${LINK.cc}" + " "; // NOI18N
	else if (conf.hasFortranFiles(projectDescriptor))
	    command += "${LINK.f}" + " "; // NOI18N
	else
	    command += "${LINK.c}" + " "; // NOI18N
        command += linkerConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        command += "${LDLIBSOPTIONS}" + " "; // NOI18N
	String[] additionalDependencies = linkerConfiguration.getAdditionalDependencies().getValues();
	for (int i = 0; i < additionalDependencies.length; i++) {
	    bw.write(output + ": " + additionalDependencies[i] + "\n\n"); // NOI18N
	}
        bw.write(output + ": " + "${OBJECTFILES}" + "\n"); // NOI18N
        String folders = IpeUtils.getDirName(output);
        if (folders != null)
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        bw.write("\t" + command + "\n"); // NOI18N
    }
    
    private void writeArchiveTarget(MakeConfiguration conf, BufferedWriter bw, String output) throws IOException {
        ArchiverConfiguration archiverConfiguration = conf.getArchiverConfiguration();
        String command = "${AR}" + " "; // NOI18N
        command += archiverConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        bw.write(output + ": " + "${OBJECTFILES}" + "\n"); // NOI18N
        String folders = IpeUtils.getDirName(output);
        if (folders != null)
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        bw.write("\t" + "${RM}" + " " + output + "\n"); // NOI18N
        bw.write("\t" + command + "\n"); // NOI18N
        if (archiverConfiguration.getRunRanlib().getValue())
            bw.write("\t" + archiverConfiguration.getRunRanlib().getOption() + " " + output + "\n"); // NOI18N
    }
    
    private void writeCompileTargets(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        Item[] items = projectDescriptor.getProjectItems();
        if (conf.isCompileConfiguration()) {
            String target = null;
            String folders = null;
            String file = null;
            String command = null;
            String comment = null;
	    String additionalDep = null;
            for (int i = 0; i < items.length; i++) {
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue())
                    continue;
                CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue());
                file = IpeUtils.escapeOddCharacters(compilerSet.normalizeDriveLetter(items[i].getPath()));
                command = ""; // NOI18N
                comment = null;
		additionalDep = null;
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i].getPath(true), conf, false);
                    if (compiler != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler) + fromLinker + " "; // NOI18N
                        command += "-o " + target + " "; // NOI18N
                        command += IpeUtils.escapeOddCharacters(items[i].getPath(true));
                    }
                    additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                } else if (itemConfiguration.getTool() == Tool.CustomTool) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration.getModified()) {
                        target = customToolConfiguration.getOutputs().getValue(" + "); // NOI18N
                        command = customToolConfiguration.getCommandLine().getValue();
                        comment = customToolConfiguration.getDescription().getValue();
			additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                    } else {
                        continue;
                    }
                } else {
                    assert false;
                }
                folders = IpeUtils.getDirName(target);
                bw.write("\n"); // NOI18N
		if (additionalDep != null)
		    bw.write(target + ": " + file + " " + additionalDep + "\n"); // NOI18N
		else
		    bw.write(target + ": " + file + "\n"); // NOI18N
                if (folders != null)
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                if (comment != null)
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                bw.write("\t" + command + "\n"); // NOI18N
            }
        }
    }
    
    private void writeMakefileTargets(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
        String target = makefileConfiguration.getOutput().getValue();
        String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
        String command = makefileConfiguration.getBuildCommand().getValue();
        //bw.write(target + ":" + "\n"); // NOI18N
        bw.write("\tcd " + IpeUtils.escapeOddCharacters(cwd) + " && " + command + "\n"); // NOI18N
    }
    
    private void writeSubProjectBuildTargets(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".build-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration = null;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();
            
            LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
            for (int j = 0; j < libraryItems.length; j++) {
                if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild())
                        continue;
                    bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
                }
            }
        }
        
        LibraryItem.ProjectItem[] projectItems = conf.getRequiredProjectsConfiguration().getRequiredProjectItemsAsArray();
        for (int i = 0; i < projectItems.length; i++) {
            MakeArtifact makeArtifact = projectItems[i].getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild())
                continue;
            bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
        }
    }
    
    private void writeSubProjectCleanTargets(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".clean-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration = null;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();
            
            LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
            for (int j = 0; j < libraryItems.length; j++) {
                if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild())
                        continue;
                    bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
                }
            }
        }
        
        LibraryItem.ProjectItem[] projectItems = conf.getRequiredProjectsConfiguration().getRequiredProjectItemsAsArray();
        for (int i = 0; i < projectItems.length; i++) {
            MakeArtifact makeArtifact = projectItems[i].getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild())
                continue;
            bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
        }
    }
    
    private void writeCleanTarget(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        bw.write("# Clean Targets\n"); // NOI18N
	if (hasSubprojects(conf))
	    bw.write(".clean-conf: ${CLEAN_SUBPROJECTS}\n"); // NOI18N
	else
	    bw.write(".clean-conf:\n"); // NOI18N
        if (conf.isCompileConfiguration()) {
            bw.write("\t${RM} -r " + MakeConfiguration.BUILD_FOLDER + '/' + conf.getName() + "\n"); // UNIX path // NOI18N
            bw.write("\t${RM} " + getOutput(conf) + "\n"); // NOI18N
            if (CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()).isSunCompiler() &&
                    conf.hasCPPFiles(projectDescriptor))
		bw.write("\t${CCADMIN} -clean" + "\n"); // NOI18N
            if (conf.hasFortranFiles(projectDescriptor))
		bw.write("\t${RM} *.mod" + "\n"); // NOI18N
            
            // Also clean output from custom tool
            Item[] items = projectDescriptor.getProjectItems();
            for (int i = 0; i < items.length; i++) {
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue())
                    continue;
                if (itemConfiguration.getTool() == Tool.CustomTool && itemConfiguration.getCustomToolConfiguration().getModified()) {
                    bw.write("\t${RM} " + itemConfiguration.getCustomToolConfiguration().getOutputs().getValue() + "\n"); // NOI18N
                }
            }
        } else if (conf.isMakefileConfiguration()) {
            MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
            String target = makefileConfiguration.getOutput().getValue();
            String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
            String command = makefileConfiguration.getCleanCommand().getValue();
            
            bw.write("\tcd " + IpeUtils.escapeOddCharacters(cwd) + " && " + command + "\n"); // NOI18N
        }
        
        writeSubProjectCleanTargets(conf, bw);
    }
    
    private void writeDependencyChecking(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        if (conf.getDependencyChecking().getValue()) {
            bw.write("\n"); // NOI18N
            bw.write("# Enable dependency checking\n"); // NOI18N
            bw.write(".KEEP_STATE:\n"); // NOI18N
            bw.write(".KEEP_STATE_FILE:.make.state.${CONF}\n"); // NOI18N
        }
    }
    
    
    private String getOutput(MakeConfiguration conf) {
        if (conf.isLinkerConfiguration()) {
            String output = conf.getLinkerConfiguration().getOutputValue();
            if (conf.isApplicationConfiguration() && conf.getPlatform().getValue() == Platform.PLATFORM_WINDOWS)
                output += ".exe"; // NOI18N
            return output;
        }
        else if (conf.isArchiverConfiguration())
            return conf.getArchiverConfiguration().getOutputValue();
        else if (conf.isMakefileConfiguration())
            return conf.getMakefileConfiguration().getOutput().getValue();
        assert false;
        return null;
    }
    
    public static String getObjectDir(MakeConfiguration conf) {
        return MakeConfiguration.BUILD_FOLDER + '/' + conf.getName() + '/' + conf.getVariant(); // UNIX path
    }
    
    private String getObjectFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        Item[] items = projectDescriptor.getProjectItems();
        StringBuilder linkObjects = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (int x = 0; x < items.length; x++) {
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[x].getPath()));
                //String commandLine = ""; // NOI18N
                if (itemConfiguration.getExcluded().getValue())
                    continue;
                if (!itemConfiguration.isCompilerToolConfiguration())
                    continue;
                BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                linkObjects.append(" \\\n\t"); // NOI18N
                linkObjects.append(compilerConfiguration.getOutputFile(items[x].getPath(true), conf, false));
            }
        }
        return linkObjects.toString();
    }
    
    private boolean hasSubprojects(MakeConfiguration conf) {
        LibrariesConfiguration librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();
        LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
        for (int j = 0; j < libraryItems.length; j++) {
            if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
                return true;
            }
        }
	return false;
    }
        }
