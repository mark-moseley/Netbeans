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

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.LibrariesNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.VectorNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class LinkerConfiguration implements AllOptionsProvider {
    private MakeConfiguration makeConfiguration;

    private StringConfiguration output;
    private VectorConfiguration additionalLibs;
    private VectorConfiguration dynamicSearch;
    private BooleanConfiguration stripOption;
    private BooleanConfiguration picOption;
    private BooleanConfiguration norunpathOption;
    private BooleanConfiguration nameassignOption;
    private OptionsConfiguration commandLineConfiguration;
    private OptionsConfiguration additionalDependencies;
    private LibrariesConfiguration librariesConfiguration;
    private StringConfiguration tool;

    // Constructors
    public LinkerConfiguration(MakeConfiguration makeConfiguration) {
	this.makeConfiguration = makeConfiguration;
	output = new StringConfiguration(null, ""); // NOI18N
	additionalLibs = new VectorConfiguration(null);
	dynamicSearch = new VectorConfiguration(null);
	stripOption = new BooleanConfiguration(null, false, "", "-s"); // NOI18N
	picOption = new BooleanConfiguration(null, true, "", "-Kpic"); // NOI18N
	norunpathOption = new BooleanConfiguration(null, true, "", "-norunpath"); // NOI18N
	nameassignOption = new BooleanConfiguration(null, true);
	commandLineConfiguration = new OptionsConfiguration();
	additionalDependencies = new OptionsConfiguration();
	additionalDependencies.setPreDefined(getAdditionalDependenciesPredefined());
	librariesConfiguration = new LibrariesConfiguration();
	tool = new StringConfiguration(null, ""); // NOI18N
    }

    private String getAdditionalDependenciesPredefined() {
	String pd = "${BUILD_SUBPROJECTS} ${OBJECTFILES}"; // NOI18N
	return pd;
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration MakeConfiguration) {
	this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
	return makeConfiguration;
    }

    // Output
    public void setOutput(StringConfiguration output) {
	this.output = output;
    }
    public StringConfiguration getOutput() {
	return output;
    }

    // Additional Libraries
    public VectorConfiguration getAdditionalLibs() {
	return additionalLibs;
    }

    public void setAdditionalLibs(VectorConfiguration additionalLibs) {
	this.additionalLibs = additionalLibs;
    }

    // Dynamic Search
    public VectorConfiguration getDynamicSearch() {
	return dynamicSearch;
    }

    public void setDynamicSearch(VectorConfiguration dynamicSearch) {
	this.dynamicSearch = dynamicSearch;
    }

    // Strip
    public void setStripOption(BooleanConfiguration stripOption) {
	this.stripOption = stripOption;
    }
    public BooleanConfiguration getStripOption() {
	return stripOption;
    }

    // Kpic
    public void setPICOption(BooleanConfiguration picOption) {
	this.picOption = picOption;
    }
    public BooleanConfiguration getPICOption() {
	return picOption;
    }

    // Norunpath
    public void setNorunpathOption(BooleanConfiguration norunpathOption) {
	this.norunpathOption = norunpathOption;
    }
    public BooleanConfiguration getNorunpathOption() {
	return norunpathOption;
    }

    // Name Assign
    public void setNameassignOption(BooleanConfiguration nameassignOption) {
	this.nameassignOption = nameassignOption;
    }
    public BooleanConfiguration getNameassignOption() {
	return nameassignOption;
    }

    // CommandLine
    public OptionsConfiguration getCommandLineConfiguration() {
	return commandLineConfiguration;
    }
    public void setCommandLineConfiguration(OptionsConfiguration commandLineConfiguration) {
	this.commandLineConfiguration = commandLineConfiguration;
    }

    // Additional Dependencies
    public OptionsConfiguration getAdditionalDependencies() {
	return additionalDependencies;
    }
    public void setAdditionalDependencies(OptionsConfiguration additionalDependencies) {
	this.additionalDependencies = additionalDependencies;
    }

    // LibrariesConfiguration
    public LibrariesConfiguration getLibrariesConfiguration() {
	return librariesConfiguration;
    }
    public void setLibrariesConfiguration(LibrariesConfiguration librariesConfiguration) {
	this.librariesConfiguration = librariesConfiguration;
    }

    // Tool
    public void setTool(StringConfiguration tool) {
	this.tool = tool;
    }
    public StringConfiguration getTool() {
	return tool;
    }


    // Clone and assign
    public void assign(LinkerConfiguration conf) {
	// LinkerConfiguration
	setMakeConfiguration(conf.getMakeConfiguration());
	getOutput().assign(conf.getOutput());
	getAdditionalLibs().assign(conf.getAdditionalLibs());
	getDynamicSearch().assign(conf.getDynamicSearch());
	getCommandLineConfiguration().assign(conf.getCommandLineConfiguration());
	getAdditionalDependencies().assign(conf.getAdditionalDependencies());
	getStripOption().assign(conf.getStripOption());
	getPICOption().assign(conf.getPICOption());
	getNorunpathOption().assign(conf.getNorunpathOption());
	getNameassignOption().assign(conf.getNameassignOption());
	getLibrariesConfiguration().assign(conf.getLibrariesConfiguration());
	getTool().assign(conf.getTool());
    }

    public Object clone() {
	LinkerConfiguration clone = new LinkerConfiguration(getMakeConfiguration());
	// LinkerConfiguration
	clone.setOutput((StringConfiguration)getOutput().clone());
	clone.setAdditionalLibs((VectorConfiguration)getAdditionalLibs().clone());
	clone.setDynamicSearch((VectorConfiguration)getDynamicSearch().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	clone.setAdditionalDependencies((OptionsConfiguration)getAdditionalDependencies().clone());
	clone.setStripOption((BooleanConfiguration)getStripOption().clone());
	clone.setPICOption((BooleanConfiguration)getPICOption().clone());
	clone.setNorunpathOption((BooleanConfiguration)getNorunpathOption().clone());
	clone.setNameassignOption((BooleanConfiguration)getNameassignOption().clone());
	clone.setLibrariesConfiguration((LibrariesConfiguration)getLibrariesConfiguration().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	return clone;
    }

    public String getOptions() {
	String options = getCommandLineConfiguration().getValue() + " "; // NOI18N
	options += getBasicOptions() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getBasicOptions() {
	String options = ""; // NOI18N 
        CompilerSet cs = CompilerSetManager.getDefault().getCompilerSet(getMakeConfiguration().getCompilerSet().getValue());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB ) {
            String libName = getOutputValue();
            int sep = libName.lastIndexOf('/');
            if (sep >= 0 && libName.length() > 1)
                libName = libName.substring(sep+1);
            // FIXUP: should be move to Platform...
            if (cs.isSunCompiler())
                options += "-G "; // NOI18N
            else if (cs.isGnuCompiler() && (getMakeConfiguration().getPlatform().getValue() == Platform.PLATFORM_SOLARIS_INTEL || getMakeConfiguration().getPlatform().getValue() == Platform.PLATFORM_SOLARIS_SPARC)) {
                options += "-G "; // NOI18N
            }
            else if (cs.isGnuCompiler() && getMakeConfiguration().getPlatform().getValue() == Platform.PLATFORM_MACOSX) {
                options += "-dynamiclib -install_name " + libName + " "; // NOI18N
            }
            else if (cs.isGnuCompiler()) {
                if (cs.getCompilerFlavor() == CompilerSet.CompilerFlavor.Cygwin) {
                    // For gdb debugging. See IZ 113893 for details.
                    options += "-mno-cygwin "; // NOI18N
                }
                options += "-shared "; // NOI18N
            }
            else
                assert false;
        }
	options += getOutputOptions() + " "; // NOI18N
	options += getStripOption().getOption() + " "; // NOI18N
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            // FIXUP: should move to Platform
            if (getPICOption().getValue()) {
                options += getPICOption(cs);
            }
            if (cs.isSunCompiler()) {
                options += getNorunpathOption().getOption() + " "; // NOI18N
                options += getNameassignOption(getNameassignOption().getValue()) + " "; // NOI18N
            }
	}
	return CppUtils.reformatWhitespaces(options);
    }

    public String getPICOption(CompilerSet cs) {
        // FIXUP: should move to Platform
        String option = null;
        if (cs.isSunCompiler()) {
            option = "-Kpic "; // NOI18N
        } else if (cs.isGnuCompiler()) {
            option = "-fPIC "; // NOI18N
        }
        else {
            assert false;
        }
        return option;
    }
    
    public String getLibraryItems() {
        String libPrefix = "-L"; // NOI18N
        String dynSearchPrefix = ""; // NOI18N
        CompilerSet cs = CompilerSetManager.getDefault().getCompilerSet(getMakeConfiguration().getCompilerSet().getValue());
        if (cs.isSunCompiler()) {
            dynSearchPrefix = "-R"; // NOI18N
        } else if (cs.isGnuCompiler()) {
            dynSearchPrefix = "-Wl,-rpath "; // NOI18N
        } else {
            return "";
        }
	String options = ""; // NOI18N
	options += getAdditionalLibs().getOption(libPrefix) + " "; // NOI18N
	options += getDynamicSearch().getOption(dynSearchPrefix) + " "; // NOI18N
	options += getLibrariesConfiguration().getOptions(getMakeConfiguration()) + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Interface OptionsProvider
    public String getAllOptions(BasicCompiler compiler) {
	String options = getBasicOptions() + " "; // NOI18N
	options += getLibraryItems() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet(MakeConfigurationDescriptor configurationDescriptor, MakeConfiguration conf) {
	Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue());
        String linkDriver;
        if (conf.hasCPPFiles(configurationDescriptor)) {
            BasicCompiler ccCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            linkDriver = ccCompiler.getName();
        }
        else {
            BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
            linkDriver = cCompiler.getName();
        }
        
	Sheet.Set set1 = new Sheet.Set();
	set1.setName("General"); // NOI18N
	set1.setDisplayName(getString("GeneralTxt"));
	set1.setShortDescription(getString("GeneralHint"));
	set1.put(new OutputNodeProp(getOutput(), getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
	set1.put(new VectorNodeProp(getAdditionalLibs(), null, getMakeConfiguration().getBaseDir(), new String[] {"AdditionalLibraryDirectories", getString("AdditionalLibraryDirectoriesTxt"), getString("AdditionalLibraryDirectoriesHint")}, true, new HelpCtx("AddtlLibraryDirectories"))); // NOI18N
	set1.put(new VectorNodeProp(getDynamicSearch(), null, getMakeConfiguration().getBaseDir(), new String[] {"RuntimeSearchDirectories", getString("RuntimeSearchDirectoriesTxt"), getString("RuntimeSearchDirectoriesHint")}, false, new HelpCtx("RuntimeSearchDirectories"))); // NOI18N
	sheet.put(set1);
	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Options"); // NOI18N
	set2.setDisplayName(getString("OptionsTxt"));
	set2.setShortDescription(getString("OptionsHint"));
	set2.put(new BooleanNodeProp(getStripOption(), true, "StripSymbols", getString("StripSymbolsTxt"), getString("StripSymbolsHint"))); // NOI18N
	if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            set2.put(new BooleanNodeProp(getPICOption(), true, "PositionIndependantCode", getString("PositionIndependantCodeTxt"), getString("PositionIndependantCodeHint"))); // NOI18N
            if (compilerSet.isSunCompiler()) {
                set2.put(new BooleanNodeProp(getNorunpathOption(), true, "NoRunPath", getString("NoRunPathTxt"), getString("NoRunPathHint"))); // NOI18N
                set2.put(new BooleanNodeProp(getNameassignOption(), true, "AssignName", getString("AssignNameTxt"), getString("AssignNameHint"))); // NOI18N
            }
	}
	sheet.put(set2);
	Sheet.Set set3 = new Sheet.Set();
	String [] texts = new String[] {getString("AdditionalDependenciesTxt1"), getString("AdditionalDependenciesHint"), getString("AdditionalDependenciesTxt2"), getString("InheritedValuesTxt")};
	set3.setName("Input"); // NOI18N
	set3.setDisplayName(getString("InputTxt"));
	set3.setShortDescription(getString("InputHint"));
	set3.put(new OptionsNodeProp(getAdditionalDependencies(), null, new AdditionalDependenciesOptions(), null, ",", texts)); // NOI18N
	sheet.put(set3);
	Sheet.Set set4 = new Sheet.Set();
	set4.setName("Tool"); // NOI18N
	set4.setDisplayName(getString("ToolTxt1"));
	set4.setShortDescription(getString("ToolHint1"));
	set4.put(new StringNodeProp(getTool(), linkDriver, "Tool", getString("ToolTxt1"), getString("ToolHint1"))); // NOI18N
	sheet.put(set4);
	return sheet;
    }

    class AdditionalDependenciesOptions implements AllOptionsProvider {
	public String getAllOptions(BasicCompiler compiler) {
	    String options = ""; // NOI18N
	    options += additionalDependencies.getPreDefined();
	    return CppUtils.reformatWhitespaces(options);
	}
    }

    public Sheet getLibrariesSheet(Project project, MakeConfiguration conf) {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {getString("LibrariesTxt1"), getString("LibrariesHint"), getString("LibrariesTxt2"), getString("AllOptionsTxt2")};

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("Libraries"); // NOI18N
	set2.setDisplayName(getString("LibrariesTxt1"));
	set2.setShortDescription(getString("LibrariesHint"));
	set2.put(new LibrariesNodeProp(getLibrariesConfiguration(), project, conf, getMakeConfiguration().getBaseDir(), texts));
	sheet.put(set2);

	return sheet;
    }

    public Sheet getCommandLineSheet() {
	Sheet sheet = new Sheet();
	String[] texts = new String[] {getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")}; // NOI18N

	Sheet.Set set2 = new Sheet.Set();
	set2.setName("CommandLine"); // NOI18N
	set2.setDisplayName(getString("CommandLineTxt"));
	set2.setShortDescription(getString("CommandLineHint"));
	set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, null, null, texts));
	sheet.put(set2);

	return sheet;
    }

    private String getNameassignOption(boolean val) {
	if (val)
	    return "-h " + IpeUtils.getBaseName(getOutputValue()); // NOI18N
	else
	    return ""; // NOI18N
    }

    private String getOutputOptions() {
	return "-o " + getOutputValue() + " "; // NOI18N
    }

    public String getOutputValue() {
        if (getOutput().getModified())
            return getOutput().getValue();
        else
            return getOutputDefault();
    }

    private String getOutputDefault() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getPlatform().getValue());
            outputName = platform.getLibraryName(outputName);
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/" + outputName; // NOI18N 
    }
    
    /*
    private String getOutputDefault30() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB)
	    outputName = "lib" + outputName + ".so"; // NOI18N
        
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/" + outputName; // NOI18N 
    }
     **/
    
    public String getOutputDefault27() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB)
	    outputName = "lib" + outputName + ".so"; // NOI18N
        
	return MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + outputName; // NOI18N 
    }
    
    private class OutputNodeProp extends StringNodeProp {
        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }
        
        public void setValue(Object v) {
            if (IpeUtils.hasMakeSpecialCharacters((String)v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(LinkerConfiguration.class, s);
    }
}
