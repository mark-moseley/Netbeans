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

import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class CCompilerConfiguration extends CCCCompilerConfiguration implements AllOptionsProvider {
    // Constructors
    public CCompilerConfiguration(String baseDir, CCompilerConfiguration master) {
        super(baseDir, master);
    }
    
    // Clone and assign
    public void assign(CCompilerConfiguration conf) {
        // From XCompiler
        super.assign(conf);
    }
    
    @Override
    public Object clone() {
        CCompilerConfiguration clone = new CCompilerConfiguration(getBaseDir(), (CCompilerConfiguration)getMaster());
        // BasicCompilerConfiguration
        clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
        clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
        clone.setMTLevel((IntConfiguration)getMTLevel().clone());
        clone.setSixtyfourBits((IntConfiguration)getSixtyfourBits().clone());
        clone.setStrip((BooleanConfiguration)getStrip().clone());
        clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
        clone.setTool((StringConfiguration)getTool().clone());
        clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
        // From CCCCompiler
        clone.setMTLevel((IntConfiguration)getMTLevel().clone());
        clone.setLibraryLevel((IntConfiguration)getLibraryLevel().clone());
        clone.setStandardsEvolution((IntConfiguration)getStandardsEvolution().clone());
        clone.setLanguageExt((IntConfiguration)getLanguageExt().clone());
        clone.setIncludeDirectories((VectorConfiguration)getIncludeDirectories().clone());
        clone.setInheritIncludes((BooleanConfiguration)getInheritIncludes().clone());
        clone.setPreprocessorConfiguration((VectorConfiguration)getPreprocessorConfiguration().clone());
        clone.setInheritPreprocessor((BooleanConfiguration)getInheritPreprocessor().clone());
        return clone;
    }
    
    // Interface OptionsProvider
    @Override
    public String getOptions(BasicCompiler compiler) {
        String options = "$(COMPILE.c) "; // NOI18N
        options += getAllOptions2(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCFlagsBasic(BasicCompiler compiler) {
        CCCCompiler cccCompiler = (CCCCompiler)compiler;
        String options = ""; // NOI18N
        options += cccCompiler.getMTLevelOptions(getMTLevel().getValue()) + " "; // NOI18N
        options += cccCompiler.getStandardsEvolutionOptions(getStandardsEvolution().getValue()) + " "; // NOI18N
        options += cccCompiler.getLanguageExtOptions(getLanguageExt().getValue()) + " "; // NOI18N
        //options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
        if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST)
            options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getCFlags(BasicCompiler compiler) {
        String options = getCFlagsBasic(compiler) + " "; // NOI18N
        options += getCommandLineConfiguration().getValue() + " "; // NOI18N
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getAllOptions(BasicCompiler compiler) {
        CCompilerConfiguration master;
        
        StringBuilder options = new StringBuilder();
        options.append(getCFlagsBasic(compiler));
        options.append(" "); // NOI18N
        master = (CCompilerConfiguration)getMaster();
        while (master != null) {
            options.append(master.getCommandLineConfiguration().getValue());
            options.append(" "); // NOI18N
            master = (CCompilerConfiguration)master.getMaster();
        }
        options.append(getAllOptions2(compiler));
        options.append(" "); // NOI18N
        return CppUtils.reformatWhitespaces(options.toString());
    }
    
    public String getAllOptions2(BasicCompiler compiler) {
        CCompilerConfiguration master;
        
        String options = ""; // NOI18N
        if (getDevelopmentMode().getValue() != DEVELOPMENT_MODE_TEST)
            options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
        options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
        options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
        options += getPreprocessorOptions();
        options += getIncludeDirectoriesOptions();
        return CppUtils.reformatWhitespaces(options);
    }
    
    public String getPreprocessorOptions() {
        CCompilerConfiguration master = (CCompilerConfiguration)getMaster();
        StringBuilder options = new StringBuilder(getPreprocessorConfiguration().getOption("-D") + " ") ; // NOI18N
        while (master != null && getInheritPreprocessor().getValue()) {
            options.append(master.getPreprocessorConfiguration().getOption("-D") + " "); // NOI18N
            if (master.getInheritPreprocessor().getValue())
                master = (CCompilerConfiguration)master.getMaster();
            else
                master = null;
        }
        return options.toString();
    }
    
    public String getIncludeDirectoriesOptions() {
        CCompilerConfiguration master = (CCompilerConfiguration)getMaster();
        StringBuilder options = new StringBuilder(getIncludeDirectories().getOption("-I") + " "); // NOI18N
        while (master != null && getInheritIncludes().getValue()) {
            options.append(master.getIncludeDirectories().getOption("-I") + " "); // NOI18N
            if (master.getInheritIncludes().getValue())
                master = (CCompilerConfiguration)master.getMaster();
            else
                master = null;
        }
        return options.toString();
    } 
    
    // Sheet
    public Sheet getGeneralSheet(MakeConfiguration conf, Folder folder) {
        Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue());
        BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
        
        sheet.put(getSet());
        if (conf.isCompileConfiguration() && folder == null) {
            sheet.put(getBasicSet());
            if (compilerSet.isSunCompiler()) { // FIXUP: should be moved to SunCCompiler
                Sheet.Set set2 = new Sheet.Set();
                set2.setName("OtherOptions"); // NOI18N
                set2.setDisplayName(getString("OtherOptionsTxt"));
                set2.setShortDescription(getString("OtherOptionsHint"));
                set2.put(new IntNodeProp(getMTLevel(), getMaster() != null ? false : true, "MultithreadingLevel", getString("MultithreadingLevelTxt"), getString("MultithreadingLevelHint"))); // NOI18N
                set2.put(new IntNodeProp(getStandardsEvolution(), getMaster() != null ? false : true, "StandardsEvolution", getString("StandardsEvolutionTxt"), getString("StandardsEvolutionHint"))); // NOI18N
                set2.put(new IntNodeProp(getLanguageExt(), getMaster() != null ? false : true, "LanguageExtensions", getString("LanguageExtensionsTxt"), getString("LanguageExtensionsHint"))); // NOI18N
                sheet.put(set2);
            }
            if (getMaster() != null)
                sheet.put(getInputSet());
            Sheet.Set set4 = new Sheet.Set();
            set4.setName("Tool"); // NOI18N
            set4.setDisplayName(getString("ToolTxt1"));
            set4.setShortDescription(getString("ToolHint1"));
            set4.put(new StringNodeProp(getTool(), cCompiler.getName(), "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
            sheet.put(set4);
        }
        
        return sheet;
    }
    
    public Sheet getCommandLineSheet(Configuration conf) {
        Sheet sheet = new Sheet();
        String[] texts = new String[] {getString("AdditionalOptionsTxt1"), getString("AdditionalOptionsHint"), getString("AdditionalOptionsTxt2"), getString("AllOptionsTxt")};
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)conf).getCompilerSet().getValue());
        BasicCompiler cCompiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
        
        Sheet.Set set2 = new Sheet.Set();
        set2.setName("CommandLine"); // NOI18N
        set2.setDisplayName(getString("CommandLineTxt"));
        set2.setShortDescription(getString("CommandLineHint"));
        set2.put(new OptionsNodeProp(getCommandLineConfiguration(), null, this, cCompiler, null, texts));
        sheet.put(set2);
        
        return sheet;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CCompilerConfiguration.class, s);
    }
}
