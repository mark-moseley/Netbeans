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

import org.netbeans.modules.cnd.makeproject.configurations.ui.OptionsNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class FortranCompilerConfiguration extends BasicCompilerConfiguration implements AllOptionsProvider {
    // Constructors
    public FortranCompilerConfiguration(String baseDir, FortranCompilerConfiguration master) {
	super(baseDir, master);
    }
    
    // Clone and assign
    public void assign(FortranCompilerConfiguration conf) {
	// From XCompiler
	super.assign(conf);
    }

    public Object clone() {
	FortranCompilerConfiguration clone = new FortranCompilerConfiguration(getBaseDir(), (FortranCompilerConfiguration)getMaster());
	// BasicCompilerConfiguration
	clone.setDevelopmentMode((IntConfiguration)getDevelopmentMode().clone());
	clone.setWarningLevel((IntConfiguration)getWarningLevel().clone());
	clone.setSixtyfourBits((IntConfiguration)getSixtyfourBits().clone());
	clone.setStrip((BooleanConfiguration)getStrip().clone());
	clone.setAdditionalDependencies((StringConfiguration)getAdditionalDependencies().clone());
	clone.setTool((StringConfiguration)getTool().clone());
	clone.setCommandLineConfiguration((OptionsConfiguration)getCommandLineConfiguration().clone());
	return clone;
    }

    // Interface OptionsProvider
    public String getOptions(BasicCompiler compiler) {
	String options = "$(COMPILE.f) "; // NOI18N
	options += getAllOptions2(compiler) + " "; // NOI18N
	options += getCommandLineConfiguration().getValue() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getFFlagsBasic(BasicCompiler compiler) {
	String options = ""; // NOI18N
	options += compiler.getStripOption(getStrip().getValue()) + " "; // NOI18N
	options += compiler.getSixtyfourBitsOption(getSixtyfourBits().getValue()) + " "; // NOI18N
	if (getDevelopmentMode().getValue() == DEVELOPMENT_MODE_TEST)
	    options += compiler.getDevelopmentModeOptions(DEVELOPMENT_MODE_TEST);
	return CppUtils.reformatWhitespaces(options);
    }

    public String getFFlags(BasicCompiler compiler) {
	String options = getFFlagsBasic(compiler) + " "; // NOI18N
	options += getCommandLineConfiguration().getValue() + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getAllOptions(BasicCompiler compiler) {
	CCompilerConfiguration master = (CCompilerConfiguration)getMaster();

	String options = ""; // NOI18N
	options += getFFlagsBasic(compiler) + " "; // NOI18N
	if (master != null)
	    options += master.getCommandLineConfiguration().getValue() + " "; // NOI18N
	options += getAllOptions2(compiler) + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    public String getAllOptions2(BasicCompiler compiler) {
	FortranCompilerConfiguration master = (FortranCompilerConfiguration)getMaster();

	String options = ""; // NOI18N
        options += compiler.getDevelopmentModeOptions(getDevelopmentMode().getValue()) + " "; // NOI18N
	options += compiler.getWarningLevelOptions(getWarningLevel().getValue()) + " "; // NOI18N
	return CppUtils.reformatWhitespaces(options);
    }

    // Sheet
    public Sheet getGeneralSheet(MakeConfiguration conf) {
	Sheet sheet = new Sheet();
        CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue());
        BasicCompiler fortranCompiler = (BasicCompiler)compilerSet.getTool(Tool.FortranCompiler);
        
	sheet.put(getBasicSet());
	if (getMaster() != null)
	    sheet.put(getInputSet());
	Sheet.Set set4 = new Sheet.Set();
        set4.setName("Tool"); // NOI18N
        set4.setDisplayName(getString("ToolTxt1"));
        set4.setShortDescription(getString("ToolHint1"));
        set4.put(new StringNodeProp(getTool(), fortranCompiler.getName(), "Tool", getString("ToolTxt2"), getString("ToolHint2"))); // NOI18N
	sheet.put(set4);

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
        return NbBundle.getMessage(FortranCompilerConfiguration.class, s);
    }
}
