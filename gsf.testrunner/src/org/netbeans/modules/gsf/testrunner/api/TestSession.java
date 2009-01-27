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
package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.DefaultTestRunnerNodeFactory;
import org.openide.util.Parameters;

/**
 * Represents a test session, i.e. a single run of tests (e.g. all the tests
 * in the project, a single suite, a single test case etc).
 *
 * @author Erno Mononen
 */
public class TestSession {

    public enum SessionType {
        TEST,
        DEBUG
    }
    /**
     * Counter for failures/errors.
     */
    private long failuresCount = 0;
    
    private final FileLocator fileLocator;
    private final SessionType sessionType;
    /**
     * The name of this session. Will be used as the display 
     * name for the output tab.
     */
    private final String name;
    private final SessionResult result;
    /**
     * The project where this session is invoked.
     */
    private final Project project;
    /**
     * The suites that were executed.
     */
    private final List<TestSuite> testSuites = new ArrayList<TestSuite>();
    /**
     * Holds output for testcases. Since a testcase is created only after 
     * a test finishes, the output of that testcase needs to be associated 
     * with it after it has been created.
     */
    private final List<String> output = new ArrayList<String>();
    /*
     * The message to display when this session is starting.
     */
    private String startingMsg;

    private final TestRunnerNodeFactory nodeFactory;
    /**
     * Handles re-running of this session's execution.
     */
    private RerunHandler rerunHandler;

    /**
     * Constructs a new session.
     * 
     * @param name the name for the session.
     * @param project the project where the session is invoked.
     * @param sessionType the type of the session.
     */
    public TestSession(String name, Project project, SessionType sessionType) {
        this(name, project, sessionType, new DefaultTestRunnerNodeFactory());
    }

    /**
     * Constructs a new session.
     *
     * @param name the name for the session.
     * @param project the project where the session is invoked.
     * @param sessionType the type of the session.
     * @param nodeFactory the node factory for this session.
     */
    public TestSession(String name, Project project, SessionType sessionType, TestRunnerNodeFactory nodeFactory) {
        Parameters.notNull("name", name);
        Parameters.notNull("project", project);
        Parameters.notNull("nodeFactory", nodeFactory);
        this.name = name;
        this.project = project;
        this.fileLocator = project.getLookup().lookup(FileLocator.class);
        this.sessionType = sessionType;
        this.result = new SessionResult();
        this.nodeFactory = nodeFactory;
    }

    public TestRunnerNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /**
     * @return the handler for this session or <code>null</code>.
     */
    public RerunHandler getRerunHandler() {
        return rerunHandler;
    }

    /**
     * Sets the rerun handler for this session.
     * @param rerunHandler
     */
    public void setRerunHandler(RerunHandler rerunHandler) {
        Parameters.notNull("rerunHandler", rerunHandler);
        this.rerunHandler = rerunHandler;
    }

    /**
     * @see #startingMsg
     */
    public void setStartingMsg(String startingMsg) {
        this.startingMsg = startingMsg;
    }

    /**
     * @see #startingMsg
     */
    public String getStartingMsg() {
        return startingMsg;
    }

    /**
     * @return the project where this session for invoked.
     */
    public Project getProject() {
        return project;
    }

    /**
     * @return the currently running test case or <code>null</code>
     * if there is no test case running.
     */
    public Testcase getCurrentTestCase() {
        if (getCurrentSuite() == null) {
            return null;
        }
        List<Testcase> testcases = getCurrentSuite().getTestcases();
        return testcases.isEmpty() ? null : testcases.get(testcases.size() - 1);
     }

    private List<Testcase> getAllTestCases() {
        List<Testcase> all = new ArrayList<Testcase>();
        for (TestSuite suite : testSuites) {
            all.addAll(suite.getTestcases());
        }
        return all;
    }

    /**
     * Adds the given suite to this session. The lastly added
     * suite is considered as the currently running one (see {@link #getCurrentSuite() }.
     * 
     * @param suite the suite to add.
     */
    public void addSuite(TestSuite suite) {
        Parameters.notNull("suite", suite);
        if (!output.isEmpty() && getCurrentSuite() != null) {
            Testcase testcase = getCurrentSuite().getLastTestCase();
            if (testcase != null) {
                testcase.addOutputLines(output);
                output.clear();
            }
        }
        testSuites.add(suite);
    }

    /**
     * Adds the given line as output of the current testcase.
     * @param line
     */
    public void addOutput(String line) {
        output.add(line);
    }

    /**
     * Add a test case to the currently running test suite.
     * 
     * @param testCase the test case to add.
     */
    public void addTestCase(Testcase testCase) {
        assert !testSuites.isEmpty() : "No suites running";
        //XXX: is this really needed?
        if (testCase.getClassName() != null) {
            for (Testcase each : getAllTestCases()) {
                if (testCase.getClassName().equals(each.getClassName())
                        && testCase.getName().equals(each.getName())) {
                    return;
                }
            }
        }
        // add pending output to the newly created testcase
        testCase.addOutputLines(output);
        output.clear();
        getCurrentSuite().addTestcase(testCase);
    }

    /**
     * @return the suite that is currently running or <code>null</code> if 
     * no suite is running.
     */
    public TestSuite getCurrentSuite() {
        return testSuites.isEmpty() ? null : testSuites.get(testSuites.size() -1);
    }

    /**
     * Builds a report for the suite of this session.
     * 
     * @return
     */
    public Report getReport(long timeInMillis) {
        Report report = new Report(getCurrentSuite().getName(), project);
        report.setElapsedTimeMillis(timeInMillis);
        for (Testcase testcase : getCurrentSuite().getTestcases()) {
            report.reportTest(testcase);
            report.setTotalTests(report.getTotalTests() + 1);
            if (testcase.getStatus() == Status.ERROR) {
                report.setErrors(report.getErrors() + 1);
            } else if (testcase.getStatus() == Status.FAILED) {
                report.setFailures(report.getFailures() + 1);
            } else if (testcase.getStatus() == Status.PENDING) {
                report.setPending(report.getPending() + 1);
            }
        }
        addReportToSessionResult(report);
        return report;
    }

    private void addReportToSessionResult(Report report) {
        result.elapsedTime(report.getElapsedTimeMillis());
        result.failed(report.getFailures());
        result.passed(report.getDetectedPassedTests());
        result.pending(report.getPending());
        result.errors(report.getErrors());
    }

    /**
     * @return the type of this session.
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    public synchronized long incrementFailuresCount() {
        return ++failuresCount;
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }

    /**
     * @return the name of this session.
     * @see #name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the complete results for this session.
     */
    public SessionResult getSessionResult() {
        return result;
    }

    /**
     * The results for the whole session, i.e. the cumulative result 
     * of all reports that were generated for the session.
     */
    public static final class SessionResult {

        private int passed;
        private int failed;
        private int errors;
        private int pending;
        private long elapsedTime;
        
        private int failed(int failedCount) {
            return failed += failedCount;
        }

        private int errors(int errorCount) {
            return errors += errorCount;
        }

        private int passed(int passedCount) {
            return passed += passedCount;
        }

        private int pending(int pendingCount) {
            return pending += pendingCount;
        }

        private long elapsedTime(long time) {
            return elapsedTime += time;
        }

        public int getErrors() {
            return errors;
        }

        public int getFailed() {
            return failed;
        }

        public int getPassed() {
            return passed;
        }

        public int getPending() {
            return pending;
        }

        public int getTotal() {
            return getPassed() + getFailed() + getErrors() + getPending();
        }

        public long getElapsedTime() {
            return elapsedTime;
        }
    }
}
