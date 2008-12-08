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
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.packaging.DummyPackager;

public class ConfigurationMakefileWriter {

    private MakeConfigurationDescriptor projectDescriptor;

    public ConfigurationMakefileWriter(MakeConfigurationDescriptor projectDescriptor) {
        this.projectDescriptor = projectDescriptor;
    }

    public void write() {
        cleanup();
        writeMakefileImpl();
        Configuration[] confs = projectDescriptor.getConfs().getConfs();
        for (int i = 0; i < confs.length; i++) {
            writeMakefileConf((MakeConfiguration) confs[i]);
            writePackagingScript((MakeConfiguration) confs[i]);
        }
    }

    private void cleanup() {
        // Remove all Makefile-* files
        File folder = new File(projectDescriptor.getBaseDir() + '/' + "nbproject"); // UNIX path // NOI18N
        File[] children = folder.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().startsWith("Makefile-")) { // NOI18N
                children[i].delete();
            }
            if (children[i].getName().startsWith("Package-")) { // NOI18N
                children[i].delete();
            }
        }
    }

    private void writeMakefileImpl() {
        String resource = "/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile-impl.mk"; // NOI18N
        InputStream is = null;
        FileOutputStream os = null;
        try {
            URL url = new URL("nbresloc:" + resource); // NOI18N
            is = url.openStream();
        } catch (Exception e) {
            is = MakeConfigurationDescriptor.class.getResourceAsStream(resource);
        }

        String outputFileName = projectDescriptor.getBaseDir() + '/' + "nbproject" + '/' + MakeConfiguration.MAKEFILE_IMPL; // UNIX path // NOI18N
        try {
            os = new FileOutputStream(outputFileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
                if (line == null) {
                    break;
                }
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
            if (conf.isQmakeConfiguration()) {
                writeQmakeProjectFile(conf);
            }
        } catch (IOException e) {
            // FIXUP
        }
    }

    protected void writeQmakeProjectFile(MakeConfiguration conf) throws IOException {
        String qtProFileName = projectDescriptor.getBaseDir() + "/Qt-" + conf.getName() + ".pro"; // NOI18N
        FileOutputStream os = new FileOutputStream(qtProFileName);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

        bw.write("TEMPLATE = "); // NOI18N
        bw.write(conf.getQmakeConfiguration().getTemplate().getValue());
        bw.write('\n'); // NOI18N
        bw.write("TARGET = "); // NOI18N
        bw.write(conf.expandMacros(getOutput(conf)));
        bw.write('\n'); // NOI18N
        bw.write("CONFIG += "); // NOI18N
        bw.write(conf.getQmakeConfiguration().getConfig().getValue());
        bw.write('\n'); // NOI18N
        bw.write("SOURCES +="); // NOI18N

        Item[] items = projectDescriptor.getProjectItems();
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (!item.hasHeaderOrSourceExtension(true, true) || item.hasHeaderOrSourceExtension(false, false)) {
                continue;
            }
            ItemConfiguration itemConf = item.getItemConfiguration(conf);
            if (itemConf.getExcluded().getValue()) {
                continue;
            }
            bw.write(' '); // NOI18N
            bw.write(item.getPath());
        }
        bw.write('\n'); // NOI18N

        bw.write("HEADERS +="); // NOI18N
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (!item.hasHeaderOrSourceExtension(false, false)) {
                continue;
            }
            ItemConfiguration itemConf = item.getItemConfiguration(conf);
            if (itemConf.getExcluded().getValue()) {
                continue;
            }
            bw.write(' '); // NOI18N
            bw.write(item.getPath());
        }
        bw.write('\n'); // NOI18N

        bw.write("FORMS +="); // NOI18N
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (!item.getAbsPath().endsWith(".ui")) { // NOI18N
                continue;
            }
            ItemConfiguration itemConf = item.getItemConfiguration(conf);
            if (itemConf.getExcluded().getValue()) {
                continue;
            }
            bw.write(' '); // NOI18N
            bw.write(item.getPath());
        }
        bw.write('\n'); // NOI18N

        CompilerSet cs = conf.getCompilerSet().getCompilerSet();

        bw.write("DEFINES += "); // NOI18N
        bw.write(conf.getCCompilerConfiguration().getPreprocessorConfiguration().getOption(cs, "")); // NOI18N
        bw.write(' '); // NOI18N
        bw.write(conf.getCCCompilerConfiguration().getPreprocessorConfiguration().getOption(cs, "")); // NOI18N
        bw.write('\n'); // NOI18N

        bw.write("INCLUDEPATH += "); // NOI18N
        bw.write(conf.getCCompilerConfiguration().getIncludeDirectories().getOption(cs, "")); // NOI18N
        bw.write(' '); // NOI18N
        bw.write(conf.getCCCompilerConfiguration().getIncludeDirectories().getOption(cs, "")); // NOI18N
        bw.write('\n'); // NOI18N

        bw.write("LIBS += "); // NOI18N
        bw.write(conf.getLinkerConfiguration().getLibrariesConfiguration().getOption(cs, "")); // NOI18N
        bw.write('\n'); // NOI18N

        bw.close();
    }

    protected void writePrelude(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        CCCCompilerConfiguration cCompilerConfiguration = conf.getCCompilerConfiguration();
        CCCCompilerConfiguration ccCompilerConfiguration = conf.getCCCompilerConfiguration();
        FortranCompilerConfiguration fortranCompilerConfiguration = conf.getFortranCompilerConfiguration();
        AssemblerConfiguration assemblerConfiguration = conf.getAssemblerConfiguration();
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return;
        }
        BasicCompiler cCompiler = (BasicCompiler) compilerSet.getTool(Tool.CCompiler);
        BasicCompiler ccCompiler = (BasicCompiler) compilerSet.getTool(Tool.CCCompiler);
        BasicCompiler fortranCompiler = (BasicCompiler) compilerSet.getTool(Tool.FortranCompiler);
        BasicCompiler assemblerCompiler = (BasicCompiler) compilerSet.getTool(Tool.Assembler);
        String cCompilerName = ""; // NOI18N
        String ccCompilerName = ""; // NOI18N
        String fortranCompilerName = ""; // NOI18N
        String assemblerName = ""; // NOI18N
        if (cCompiler != null) {
            if (cCompilerConfiguration.getTool().getModified()) {
                cCompilerName = cCompilerConfiguration.getTool().getValue();
            } else {
                cCompilerName = cCompiler.getName();
            }
        }
        if (ccCompiler != null) {
            if (ccCompilerConfiguration.getTool().getModified()) {
                ccCompilerName = ccCompilerConfiguration.getTool().getValue();
            } else {
                ccCompilerName = ccCompiler.getName();
            }
        }
        if (fortranCompiler != null) {
            if (fortranCompilerConfiguration.getTool().getModified()) {
                fortranCompilerName = fortranCompilerConfiguration.getTool().getValue();
            } else {
                fortranCompilerName = fortranCompiler.getName();
            }
        }
        if (assemblerCompiler != null) {
            if (assemblerConfiguration.getTool().getModified()) {
                assemblerName = assemblerConfiguration.getTool().getValue();
            } else {
                assemblerName = assemblerCompiler.getName();
            }
        }

        bw.write("#\n"); // NOI18N
        bw.write("# Generated Makefile - do not edit!\n"); // NOI18N
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
        bw.write("AS=" + assemblerName + "\n"); // NOI18N
        if (conf.getArchiverConfiguration().getTool().getModified()) {
            bw.write("AR=" + conf.getArchiverConfiguration().getTool().getValue() + "\n"); // NOI18N
        }
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
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
            bw.write("CFLAGS=" + conf.getCCompilerConfiguration().getCFlags(cCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (ccCompiler != null) {
            bw.write("# CC Compiler Flags\n"); // NOI18N
            bw.write("CCFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("CXXFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (fortranCompiler != null) {
            bw.write("# Fortran Compiler Flags\n"); // NOI18N
            bw.write("FFLAGS=" + conf.getFortranCompilerConfiguration().getFFlags(fortranCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (assemblerCompiler != null) {
            bw.write("# Assembler Flags\n"); // NOI18N
            bw.write("ASFLAGS=" + conf.getAssemblerConfiguration().getAsFlags(assemblerCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        bw.write("# Link Libraries and Options\n"); // NOI18N
        bw.write("LDLIBSOPTIONS=" + conf.getLinkerConfiguration().getLibraryItems() + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (conf.isQmakeConfiguration()) {
            bw.write("Qt-${CONF}.mk: Qt-${CONF}.pro\n"); // NOI18N
            bw.write("\tqmake -o Qt-${CONF}.mk Qt-${CONF}.pro\n"); // NOI18N
            bw.write('\n'); // NOI18N
        }
    }

    protected void writeBuildTarget(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        String output = getOutput(conf);
        bw.write("# Build Targets\n"); // NOI18N
        if (conf.isCompileConfiguration()) {
            bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
            bw.write("\t${MAKE} " + MakeOptions.getInstance().getMakeOptions() // NOI18N
                    + " -f nbproject/Makefile-" + conf.getName() + ".mk " // NOI18N
                    + output + "\n\n"); // NOI18N
            if (conf.isLinkerConfiguration()) {
                writeLinkTarget(conf, bw, output);
            }
            if (conf.isArchiverConfiguration()) {
                writeArchiveTarget(conf, bw, output);
            }
            if (conf.isCompileConfiguration()) {
                writeCompileTargets(conf, bw);
            }
        } else if (conf.isMakefileConfiguration()) {
            bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
            writeMakefileTargets(conf, bw);
        } else if (conf.isQmakeConfiguration()) {
            bw.write(".build-conf: ${BUILD_SUBPROJECTS} Qt-${CONF}.mk\n"); // NOI18N
            bw.write("\t${MAKE} -f Qt-${CONF}.mk\n"); // NOI18N
        }
        writeSubProjectBuildTargets(conf, bw);
        bw.write("\n"); // NOI18N
    }

    protected void writeLinkTarget(MakeConfiguration conf, BufferedWriter bw, String output) throws IOException {
        LinkerConfiguration linkerConfiguration = conf.getLinkerConfiguration();
        String command = ""; // NOI18N
        if (linkerConfiguration.getTool().getModified()) {
            command += linkerConfiguration.getTool().getValue() + " "; // NOI18N
        }//	else if (conf.isDynamicLibraryConfiguration())
        //	    command += "${CCC}" + " "; // NOI18N
        else if (conf.hasCPPFiles(projectDescriptor)) {
            command += "${LINK.cc}" + " "; // NOI18N
        } else if (conf.hasFortranFiles(projectDescriptor)) {
            command += "${LINK.f}" + " "; // NOI18N
        } else {
            command += "${LINK.c}" + " "; // NOI18N
        }
        command += linkerConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        command += "${LDLIBSOPTIONS}" + " "; // NOI18N
        String[] additionalDependencies = linkerConfiguration.getAdditionalDependencies().getValues();
        for (int i = 0; i < additionalDependencies.length; i++) {
            bw.write(output + ": " + additionalDependencies[i] + "\n\n"); // NOI18N
        }
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        LibraryItem[] libs = linkerConfiguration.getLibrariesConfiguration().getLibraryItemsAsArray();
        for (LibraryItem lib : libs) {
            String libPath = lib.getPath();
            if (libPath != null && libPath.length() > 0) {
                bw.write(output + ": " + IpeUtils.escapeOddCharacters(cs.normalizeDriveLetter(libPath)) + "\n\n"); // NOI18N
            }
        }
        bw.write(output + ": ${OBJECTFILES}\n"); // NOI18N
        String folders = IpeUtils.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + command + "\n"); // NOI18N
    }

    private void writeArchiveTarget(MakeConfiguration conf, BufferedWriter bw, String output) throws IOException {
        ArchiverConfiguration archiverConfiguration = conf.getArchiverConfiguration();
        String command = "${AR}" + " "; // NOI18N
        command += archiverConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        bw.write(output + ": " + "${OBJECTFILES}" + "\n"); // NOI18N
        String folders = IpeUtils.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + "${RM}" + " " + output + "\n"); // NOI18N
        bw.write("\t" + command + "\n"); // NOI18N
        if (archiverConfiguration.getRunRanlib().getValue()) {
            bw.write("\t" + archiverConfiguration.getRunRanlib().getOption() + " " + output + "\n"); // NOI18N
        }
    }

    protected void writeCompileTargets(MakeConfiguration conf, BufferedWriter bw) throws IOException {
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
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                file = IpeUtils.escapeOddCharacters(compilerSet.normalizeDriveLetter(items[i].getPath()));
                command = ""; // NOI18N
                comment = null;
                additionalDep = null;
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    BasicCompiler compiler = (BasicCompiler) compilerSet.getTool(itemConfiguration.getTool());
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i], conf, false);
                    if (compiler != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler) + fromLinker + " "; // NOI18N
                        if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                            command = "${RM} $@.d\n\t" + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                        }
                        if (items[i].hasHeaderOrSourceExtension(false, false)) {
                            String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                            if (flags == null) {
                                command = "# command to precompile header "; // NOI18N
                                comment = "Current compiler does not support header precompilation"; // NOI18N
                            } else {
                                command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                            }
                        } else {
                            command += compiler.getDescriptor().getOutputObjectFileFlags() + target + " "; // NOI18N
                        }
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
                if (additionalDep != null) {
                    bw.write(target + ": " + file + " " + additionalDep + "\n"); // NOI18N
                } else {
                    bw.write(target + ": " + file + "\n"); // NOI18N
                }
                if (folders != null) {
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                }
                if (comment != null) {
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                }
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
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) libraryItems[j];
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
                }
            }
        }

        LibraryItem.ProjectItem[] projectItems = conf.getRequiredProjectsConfiguration().getRequiredProjectItemsAsArray();
        for (int i = 0; i < projectItems.length; i++) {
            MakeArtifact makeArtifact = projectItems[i].getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
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
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) libraryItems[j];
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
                }
            }
        }

        LibraryItem.ProjectItem[] projectItems = conf.getRequiredProjectsConfiguration().getRequiredProjectItemsAsArray();
        for (int i = 0; i < projectItems.length; i++) {
            MakeArtifact makeArtifact = projectItems[i].getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
            bw.write("\tcd " + IpeUtils.escapeOddCharacters(location) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
        }
    }

    private void writeCleanTarget(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        bw.write("# Clean Targets\n"); // NOI18N
        if (hasSubprojects(conf)) {
            bw.write(".clean-conf: ${CLEAN_SUBPROJECTS}"); // NOI18N
        } else {
            bw.write(".clean-conf:"); // NOI18N
        }
        if (conf.isQmakeConfiguration()) {
            bw.write(" Qt-" + conf.getName() + ".mk"); // NOI18N
        }
        bw.write('\n'); // NOI18N
        if (conf.isCompileConfiguration()) {
            bw.write("\t${RM} -r " + MakeConfiguration.BUILD_FOLDER + '/' + conf.getName() + "\n"); // UNIX path // NOI18N
            bw.write("\t${RM} " + getOutput(conf) + "\n"); // NOI18N
            if (conf.getCompilerSet().getCompilerSet() != null &&
                    conf.getCompilerSet().getCompilerSet().isSunCompiler() &&
                    conf.hasCPPFiles(projectDescriptor)) {
                bw.write("\t${CCADMIN} -clean" + "\n"); // NOI18N
            }
            if (conf.hasFortranFiles(projectDescriptor)) {
                bw.write("\t${RM} *.mod" + "\n"); // NOI18N
            }

            // Also clean output from custom tool
            Item[] items = projectDescriptor.getProjectItems();
            for (int i = 0; i < items.length; i++) {
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
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
        } else if (conf.isQmakeConfiguration()) {
            bw.write("\t$(MAKE) -f Qt-" + conf.getName() + ".mk\n"); // NOI18N
        }

        writeSubProjectCleanTargets(conf, bw);
    }

    private void writeDependencyChecking(MakeConfiguration conf, BufferedWriter bw) throws IOException {
        if (conf.getDependencyChecking().getValue() && !conf.isMakefileConfiguration() && !conf.isQmakeConfiguration()) {
            bw.write("\n"); // NOI18N
            bw.write("# Enable dependency checking\n"); // NOI18N
            bw.write(".dep.inc: .depcheck-impl\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("include .dep.inc\n"); // NOI18N
        }
    }

    private String getOutput(MakeConfiguration conf) {
        if (conf.isLinkerConfiguration()) {
            String output = conf.getLinkerConfiguration().getOutputValue();
            if (conf.isApplicationConfiguration() && conf.getPlatform().getValue() == Platform.PLATFORM_WINDOWS) {
                output += ".exe"; // NOI18N
            }
            return output;
        } else if (conf.isArchiverConfiguration()) {
            return conf.getArchiverConfiguration().getOutputValue();
        } else if (conf.isMakefileConfiguration()) {
            return conf.getMakefileConfiguration().getOutput().getValue();
        } else if (conf.isQmakeConfiguration()) {
            return conf.getLinkerConfiguration().getOutputValue();
        }
        assert false;
        return null;
    }

    public static String getObjectDir(MakeConfiguration conf) {
        return MakeConfiguration.BUILD_FOLDER + '/' + conf.getName() + '/' + "${PLATFORM}"; // UNIX path // NOI18N
    }

    private String getObjectFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        Item[] items = projectDescriptor.getProjectItems();
        StringBuilder linkObjects = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (int x = 0; x < items.length; x++) {
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[x].getPath()));
                //String commandLine = ""; // NOI18N
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (!itemConfiguration.isCompilerToolConfiguration()) {
                    continue;
                }
                if (items[x].hasHeaderOrSourceExtension(false, false)) {
                    continue;
                }
                BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                linkObjects.append(" \\\n\t"); // NOI18N
                linkObjects.append(compilerConfiguration.getOutputFile(items[x], conf, false));
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

    private void writePackagingScript(MakeConfiguration conf) {
        String outputFileName = projectDescriptor.getBaseDir() + '/' + "nbproject" + '/' + "Package-" + conf.getName() + ".bash"; // UNIX path // NOI18N

        if (conf.getPackagingConfiguration().getFiles().getValue().size() == 0) {
            // Nothing to do
            return;
        }

        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue());
        if (packager == null || packager instanceof DummyPackager) {
            return;
        }

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outputFileName);
        } catch (Exception e) {
            // FIXUP
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            writePackagingScriptBody(bw, conf);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // FIXUP
        }
    }

    private void writePackagingScriptBody(BufferedWriter bw, MakeConfiguration conf) throws IOException {
        String tmpDirName = "tmp-packaging"; // NOI18N
        String tmpdir = getObjectDir(conf) + "/" + tmpDirName; // NOI18N
        PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
        String output = packagingConfiguration.getOutputValue();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());

        bw.write("#!/bin/bash"); // NOI18N
        if (conf.getPackagingConfiguration().getVerbose().getValue()) {
            bw.write(" -x"); // NOI18N
        }
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("TOP=" + "`pwd`" + "\n"); // NOI18N
        bw.write("PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
        bw.write("TMPDIR=" + tmpdir + "\n"); // NOI18N
        bw.write("TMPDIRNAME=" + tmpDirName + "\n"); // NOI18N
        String projectOutput = conf.getOutputValue();
        if (projectOutput == null || projectOutput.length() == 0) {
            projectOutput = "MissingOutputInProject"; // NOI18N
        }
        bw.write("OUTPUT_PATH=" + projectOutput + "\n"); // NOI18N
        bw.write("OUTPUT_BASENAME=" + IpeUtils.getBaseName(projectOutput) + "\n"); // NOI18N
        bw.write("PACKAGE_TOP_DIR=" + (packagingConfiguration.getTopDirValue().length() > 0 ? packagingConfiguration.getTopDirValue() + "/" : "") + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Functions\n"); // NOI18N
        bw.write("function checkReturnCode\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    rc=$?\n"); // NOI18N
        bw.write("    if [ $rc != 0 ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        exit $rc\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function makeDirectory\n"); // NOI18N
        bw.write("# $1 directory path\n"); // NOI18N
        bw.write("# $2 permission (optional)\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    mkdir -p \"$1\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$2\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("      chmod $2 \"$1\"\n"); // NOI18N
        bw.write("      checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function copyFileToTmpDir\n"); // NOI18N
        bw.write("# $1 from-file path\n"); // NOI18N
        bw.write("# $2 to-file path\n"); // NOI18N
        bw.write("# $3 permission\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    cp \"$1\" \"$2\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$3\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        chmod $3 \"$2\"\n"); // NOI18N
        bw.write("        checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N

        bw.write("\n"); // NOI18N
        bw.write("# Setup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N

        if (packager.isOutputAFolder()) {
            bw.write("mkdir -p " + output + "\n"); // NOI18N
        } else {
            bw.write("mkdir -p " + IpeUtils.getDirName(output) + "\n"); // NOI18N
        }
        bw.write("rm -rf ${TMPDIR}\n"); // NOI18N
        bw.write("mkdir -p ${TMPDIR}\n"); // NOI18N
        bw.write("\n"); // NOI18N

        packager.getShellFileWriter().writeShellScript(bw, conf, packagingConfiguration);

        bw.write("# Cleanup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N
        bw.write("rm -rf ${TMPDIR}\n"); // NOI18N
    }
}
