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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.ruby.rubyproject;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.NbUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.api.gsf.EditorAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileUtil;

/**
 * Run the current focused test or spec (test under caret)
 *
 * @author Tor Norbye
 */
public class RunFocusedTest extends AbstractAction implements EditorAction {

    public RunFocusedTest() {
        super(NbBundle.getMessage(RunFocusedTest.class, "run-focused-spec")); 
        putValue("PopupMenuText", NbBundle.getBundle(RunFocusedTest.class).getString("popup-run-focused-spec")); // NOI18N
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        runTest(target, false);
    }

    public String getActionName() {
        return "run-focused-spec";
    }

    public Class getShortDescriptionBundleClass() {
        return RunFocusedTest.class;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void actionPerformed(ActionEvent ev) {
        JTextComponent pane = NbUtilities.getOpenPane();

        if (pane != null) {
            runTest(pane, false);
        }
    }

    static void runTest(JTextComponent target, boolean debug) {
        if (target.getCaret() == null) {
            return;
        }

        FileObject file = NbUtilities.findFileObject(target);

        if (file != null) {
            Project project = FileOwnerQuery.getOwner(file);
            if (project != null) {
                int offset = target.getCaret().getDot();
                BaseDocument doc = (BaseDocument)target.getDocument();

                FileLocator locator = project.getLookup().lookup(FileLocator.class);

                try {
                    RSpecSupport rspec = new RSpecSupport(project);
                    if (rspec.isRSpecInstalled() && rspec.isSpecFile(file)) {
                        int line = Utilities.getLineOffset(doc, offset);
                        if (line >= 0) {
                            // TODO - compute line number of surrounding "spec" ? Or can spec find it on its own?

                            // Save all files first - this spec file could be accessing other files being tested
                            LifecycleManager.getDefault().saveAll();

                            // Line+1: spec seems to be 1-based rather than 0-based (first line is 1)
                            rspec.runRSpec(null, file, line+1, file.getName(), locator, true, debug);
                                return;
                        }
                    } else {
                        // Regular Test::Unit? Find the test surrounding the caret.
                        // "ruby my_test.rb -n test_this"
                        String testName = AstUtilities.getMethodName(file, offset);
                        if (testName != null) {
                            // No validation that the method is a test or the parentclass
                            // is Test::Unit -- make this work with possibly other useful
                            // single-method execution purposes as well. See
                            // http://www.nabble.com/Should-be-Ruby-from-bits.netbeans.org-preferred--tf4607093s27020.html#a13185222
                            // && testName.startsWith("test") { // NOI18N
                            
                            // Save all files first - this spec file could be accessing other files being tested
                            LifecycleManager.getDefault().saveAll();

                            runTest(project, null, file, testName, file.getName(), locator, true, debug);
                                return;
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        }
    }
    
    /**
     * Run rspec on the given specfile.
     * (If you pass null as the directory, the project directory will be used, and if not set,
     * the directory containing the spec file.)
     * @param lineNumber if not -1, run the spec at the given line
     * @param warn If true, produce popups if Ruby or RSpec are not configured
     *  correctly.
     */
    private static void runTest(Project project, File pwd, FileObject target, String testName, String displayName,
        FileLocator fileLocator, boolean warn, boolean debug, String... parameters) {
        FileObject projectDir = null;
        if (project != null) {
            projectDir = project.getProjectDirectory();
        }
        if (pwd == null) {
            FileObject pfo = (projectDir != null) ? projectDir : target.getParent();
            pwd = FileUtil.toFile(pfo);
        }
        
        RubyPlatform platform = RubyPlatform.platformFor(project);

        if (!platform.isValidRuby(warn)) {
            return;
        }

        List<String> additionalArgs = new ArrayList<String>();

        additionalArgs.add("-n");
        additionalArgs.add(testName);

        if ((parameters != null) && (parameters.length > 0)) {
            for (String parameter : parameters) {
                additionalArgs.add(parameter);
            }
        }

        String targetPath =  FileUtil.toFile(target).getAbsolutePath();
        ExecutionDescriptor desc = null;
        String charsetName = null;
        if (project != null) {
            PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
            if (evaluator != null) {
                charsetName = evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING);
            }

            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider instanceof ScriptDescProvider) { // Lookup ScriptDescProvider directly?
                ScriptDescProvider descProvider = (ScriptDescProvider)provider;
                OutputRecognizer[] extraRecognizers = new OutputRecognizer[] { new TestNotifier(true, true) };
                desc = descProvider.getScriptDescriptor(pwd, target, targetPath, displayName, project.getLookup(), debug, extraRecognizers);
                
                // Override args
                desc. additionalArgs(additionalArgs.toArray(
                            new String[additionalArgs.size()])); // NOI18N
            }
        } else {
            desc = new ExecutionDescriptor(platform, displayName, pwd, targetPath);

            desc.additionalArgs(additionalArgs.toArray(
                    new String[additionalArgs.size()])); // NOI18N
            desc.debug(debug);
            desc.allowInput();
            desc.fileLocator(fileLocator);
            desc.addStandardRecognizers();
            desc.addOutputRecognizer(new TestNotifier(true, true));
        }
        new RubyExecution(desc, charsetName).run();
    }
}
