/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.codecoverage.PhpUnitCoverageLogParser;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action implementation for TEST configuration.
 * It means running and debugging tests.
 * @author Tomas Mysik
 */
class ConfigActionTest extends ConfigAction {
    private static final String CWD = "."; // NOI18N
    final PhpCoverageProvider coverageProvider;

    protected ConfigActionTest(PhpProject project) {
        super(project);
        coverageProvider = project.getLookup().lookup(PhpCoverageProvider.class);
        assert coverageProvider != null;
    }

    protected FileObject getTestDirectory(boolean showCustomizer) {
        return ProjectPropertiesSupport.getTestDirectory(project, showCustomizer);
    }

    protected boolean isCoverageEnabled() {
        return coverageProvider.isEnabled();
    }

    @Override
    public boolean isValid(boolean indexFileNeeded) {
        throw new IllegalStateException("Validation is not needed for tests");
    }

    @Override
    public boolean isDebugProjectEnabled() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public boolean isRunFileEnabled(Lookup context) {
        FileObject rootFolder = getTestDirectory(false);
        assert rootFolder != null : "Test directory not found but isRunFileEnabled() for a test file called?!";
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, rootFolder);
        return file != null && CommandUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(context);
    }

    @Override
    public void runProject() {
        // first, let user select test directory
        FileObject testDirectory = getTestDirectory(true);
        if (testDirectory == null) {
            return;
        }
        PhpUnit phpUnit = CommandUtils.getPhpUnit(false);
        if (phpUnit == null || !phpUnit.supportedVersionFound()) {
            int[] version = phpUnit.getVersion();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ConfigActionTest.class, "MSG_OldPhpUnit", PhpUnit.getVersions(version)),
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        run(getPhpUnitInfo(null));
    }

    @Override
    public void debugProject() {
        throw new IllegalStateException("Debug project tests action is not supported");
    }

    @Override
    public void runFile(Lookup context) {
        run(getPhpUnitInfo(context));
    }

    @Override
    public void debugFile(Lookup context) {
        debug(getPhpUnitInfo(context));
    }

    void run(PhpUnitInfo info) {
        if (info == null) {
            return;
        }

        new RunScript(new RunScriptProvider(info)).run();
    }

    void debug(PhpUnitInfo info) {
        if (info == null) {
            return;
        }

        new DebugScript(new DebugScriptProvider(info)).run();
    }

    private PhpUnitInfo getPhpUnitInfo(Lookup context) {
        PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return null;
        }
        FileObject testDirectory = getTestDirectory(true);
        if (testDirectory == null) {
            return null;
        }
        if (context == null) {
            return getProjectPhpUnitInfo(testDirectory);
        }
        return getFilePhpUnitInfo(testDirectory, context);
    }

    private PhpUnitInfo getProjectPhpUnitInfo(FileObject testDirectory) {
        return new PhpUnitInfo(testDirectory, null, CWD);
    }

    private PhpUnitInfo getFilePhpUnitInfo(FileObject testDirectory, Lookup context) {
        FileObject fileObj = CommandUtils.fileForContextOrSelectedNodes(context, testDirectory);
        assert fileObj != null : "Fileobject not found for context: " + context;
        if (!fileObj.isValid()) {
            return null;
        }
        return new PhpUnitInfo(fileObj.getParent(), fileObj, fileObj.getName());
    }

    private static class PhpUnitInfo {
        public final FileObject workingDirectory;
        public final FileObject startFile;
        public final String testName;

        public PhpUnitInfo(FileObject workingDirectory, FileObject startFile, String testName) {
            this.workingDirectory = workingDirectory;
            this.startFile = startFile;
            this.testName = testName;
        }
    }

    private class RunScriptProvider implements RunScript.Provider {
        protected final PhpUnitInfo info;
        protected final PhpUnit phpUnit;
        protected final UnitTestRunner testRunner;
        protected final RerunUnitTestHandler rerunUnitTestHandler;

        public RunScriptProvider(PhpUnitInfo info) {
            assert info != null;

            this.info = info;
            rerunUnitTestHandler = getRerunUnitTestHandler();
            testRunner = getTestRunner();
            phpUnit = CommandUtils.getPhpUnit(false);
            assert phpUnit != null;
        }

        public ExecutionDescriptor getDescriptor() throws IOException {
            ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                    .optionsPath(PHPOptionsCategory.PATH_IN_LAYER)
                    .frontWindow(!phpUnit.supportedVersionFound())
                    .showProgress(true);
            if (phpUnit.supportedVersionFound()) {
                executionDescriptor = executionDescriptor
                        .preExecution(new Runnable() {
                            public void run() {
                                rerunUnitTestHandler.disable();
                                testRunner.start();
                            }
                        })
                        .postExecution(new Runnable() {
                            public void run() {
                                testRunner.showResults();
                                rerunUnitTestHandler.enable();
                                handleCodeCoverage();
                            }
                        });
            } else {
                executionDescriptor = executionDescriptor
                        .outProcessorFactory(new OutputProcessorFactory(phpUnit));
            }
            return executionDescriptor;
        }

        public ExternalProcessBuilder getProcessBuilder() {
            ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getProgram())
                    .workingDirectory(FileUtil.toFile(info.workingDirectory));
            for (String param : phpUnit.getParameters()) {
                externalProcessBuilder = externalProcessBuilder.addArgument(param);
            }
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_XML_LOG)
                    .addArgument(PhpUnit.XML_LOG.getAbsolutePath());
            if (isCoverageEnabled()) {
                externalProcessBuilder = externalProcessBuilder
                        .addArgument(PhpUnit.PARAM_COVERAGE_LOG)
                        .addArgument(PhpUnit.COVERAGE_LOG.getAbsolutePath());
            }
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(info.testName);
            return externalProcessBuilder;
        }

        public String getOutputTabTitle() {
            String title = null;
            if (info.testName == CWD) {
                title = NbBundle.getMessage(ConfigActionTest.class, "LBL_UnitTestsForTestSourcesSuffix");
            } else {
                title = info.testName;
            }
            return String.format("%s - %s", phpUnit.getProgram(), title);
        }

        public boolean isValid() {
            return phpUnit.isValid() && info.workingDirectory != null && info.testName != null;
        }

        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RerunUnitTestHandler(info);
        }

        protected UnitTestRunner getTestRunner() {
            return new UnitTestRunner(project, TestSession.SessionType.TEST, rerunUnitTestHandler);
        }

        void handleCodeCoverage() {
            if (!isCoverageEnabled()
                    || info.testName != CWD) {
                // XXX not enabled or just one test case (could be handled later)
                return;
            }

            CoverageVO coverage = new CoverageVO();
            try {
                PhpUnitCoverageLogParser.parse(new BufferedReader(new FileReader(PhpUnit.COVERAGE_LOG)), coverage);
            } catch (FileNotFoundException ex) {
                LOGGER.warning(String.format("In order to show code coverage, file %s must exist."
                        + "Report this issue please in http://www.netbeans.org/issues/.", PhpUnit.COVERAGE_LOG));
                return;
            }
            coverageProvider.setCoverage(coverage);
        }
    }

    private final class DebugScriptProvider extends RunScriptProvider implements DebugScript.Provider {
        public DebugScriptProvider(PhpUnitInfo info) {
            super(info);
        }

        public PhpProject getProject() {
            assert info.startFile != null : "Only particular test files can be debugged";
            return project;
        }

        public FileObject getStartFile() {
            assert info.startFile != null : "Only particular test files can be debugged";
            return info.startFile;
        }

        @Override
        protected RerunUnitTestHandler getRerunUnitTestHandler() {
            return new RedebugUnitTestHandler(info);
        }

        @Override
        protected UnitTestRunner getTestRunner() {
            assert rerunUnitTestHandler instanceof RedebugUnitTestHandler;
            return new UnitTestRunner(project, TestSession.SessionType.DEBUG, rerunUnitTestHandler);
        }
    }

    private class RerunUnitTestHandler implements RerunHandler {
        protected final PhpUnitInfo info;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private volatile boolean enabled = false;

        public RerunUnitTestHandler(PhpUnitInfo info) {
            assert info != null;
            this.info = info;
        }

        public void rerun() {
            run(info);
        }

        public boolean enabled() {
            return enabled;
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        void enable() {
            if (!enabled) {
                enabled = true;
                changeSupport.fireChange();
            }
        }

        void disable() {
            if (enabled) {
                enabled = false;
                changeSupport.fireChange();
            }
        }
    }

    private class RedebugUnitTestHandler extends RerunUnitTestHandler {
        public RedebugUnitTestHandler(PhpUnitInfo info) {
            super(info);
        }

        @Override
        public void rerun() {
            debug(info);
        }
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        private final PhpUnit phpUnit;

        public OutputProcessorFactory(PhpUnit phpUnit) {
            this.phpUnit = phpUnit;
        }

        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return new InputProcessor() {
                public void processInput(char[] chars) throws IOException {
                    defaultProcessor.processInput(chars);
                }
                public void reset() throws IOException {
                    defaultProcessor.reset();
                }
                public void close() throws IOException {
                    String msg = NbBundle.getMessage(ConfigActionTest.class, "MSG_OldPhpUnit", PhpUnit.getVersions(phpUnit.getVersion()));
                    char[] separator = new char[msg.length()];
                    Arrays.fill(separator, '='); // NOI18N
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(msg.toCharArray());
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.processInput(separator);
                    defaultProcessor.processInput("\n".toCharArray()); // NOI18N
                    defaultProcessor.close();
                }
            };
        }
    }
}
