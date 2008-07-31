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
package org.netbeans.modules.ruby.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FilteredOutput;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.RecognizedOutput;
import org.netbeans.modules.ruby.testrunner.TestUnitRunner;
import org.openide.util.NbBundle;

/**
 * An output recognizer for parsing output of the test/unit runner script, 
 * <code>nb_test_mediator.rb</code>. Updates the test result UI.
 *
 * @author Erno Mononen
 */
public class TestUnitHandlerFactory {

    private static final Logger LOGGER = Logger.getLogger(TestUnitHandlerFactory.class.getName());
    
    public static List<TestRecognizerHandler> getHandlers() {
        List<TestRecognizerHandler> result = new ArrayList<TestRecognizerHandler>();
        result.add(new SuiteStartingHandler());
        result.add(new SuiteStartedHandler());
        result.add(new SuiteFinishedHandler());
        result.add(new SuiteErrorOutputHandler());
        result.add(new TestStartedHandler());
        result.add(new TestFailedHandler());
        result.add(new TestErrorHandler());
        result.add(new TestFinishedHandler());
        result.add(new TestLoggerHandler());
        result.add(new TestMiscHandler());
        result.add(new SuiteMiscHandler());
        return result;
    }

    private static String errorMsg(long failureCount) {
        return NbBundle.getMessage(TestUnitHandlerFactory.class, "MSG_Error", failureCount);
    }
    
    private static String failureMsg(long failureCount) {
        return NbBundle.getMessage(TestUnitHandlerFactory.class, "MSG_Failure", failureCount);
    }

    static String[] getStackTrace(String message, String stackTrace) {
        List<String> stackTraceList = new ArrayList<String>();
        stackTraceList.add(message);
        for (String location : stackTrace.split("%BR%")) { //NOI18N
            if (!location.contains(TestUnitRunner.MEDIATOR_SCRIPT_NAME) && !location.contains(TestUnitRunner.RUNNER_SCRIPT_NAME)) { //NOI18N
                stackTraceList.add(location);
            }
        }
        return stackTraceList.toArray(new String[stackTraceList.size()]);
    }

    static class TestFailedHandler extends TestRecognizerHandler {

        public TestFailedHandler() {
            super("%TEST_FAILED%\\stime=(.+)\\stestname=(.+)\\((.+)\\)\\smessage=(.*)\\slocation=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.name = matcher.group(2);
            testcase.className = matcher.group(3);
            testcase.trouble = new Report.Trouble(false);
            String message = matcher.group(4);
            String location = matcher.group(5);
            testcase.trouble.stackTrace = getStackTrace(message, location);
            session.addTestCase(testcase);
            manager.displayOutput(session, failureMsg(session.incrementFailuresCount()), false);
            manager.displayOutput(session, testcase.name + "(" + testcase.className + "):", false); //NOI18N
            for (String line : testcase.trouble.stackTrace) {
                manager.displayOutput(session, line, false);
            }
            manager.displayOutput(session, "", false);
        }

        @Override
        RecognizedOutput getRecognizedOutput() {
            return new FilteredOutput(matcher.group(4));
        }
    }

    static class TestErrorHandler extends TestRecognizerHandler {

        public TestErrorHandler() {
            super("%TEST_ERROR%\\stime=(.+)\\stestname=(.+)\\((.+)\\)\\smessage=(.*)\\slocation=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.className = matcher.group(3);
            testcase.name = matcher.group(2);
            testcase.trouble = new Report.Trouble(true);
            testcase.trouble.stackTrace = getStackTrace(matcher.group(4), matcher.group(5));
            session.addTestCase(testcase);
            manager.displayOutput(session, errorMsg(session.incrementFailuresCount()), false);
            manager.displayOutput(session, testcase.name + "(" + testcase.className + "):", false); //NOI18N
            for (String line : testcase.trouble.stackTrace) {
                manager.displayOutput(session, line, true);
            }
            manager.displayOutput(session, "", false);
        }

        @Override
        RecognizedOutput getRecognizedOutput() {
            return new FilteredOutput(matcher.group(4));
        }
    }

    static class TestStartedHandler extends TestRecognizerHandler {

        public TestStartedHandler() {
            super("%TEST_STARTED%\\s([\\w]+)\\((.+)\\)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class TestFinishedHandler extends TestRecognizerHandler {

        public TestFinishedHandler() {
            super("%TEST_FINISHED%\\stime=(.+)\\s([\\w]+)\\((.+)\\)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Report.Testcase testcase = new Report.Testcase();
            testcase.timeMillis = toMillis(matcher.group(1));
            testcase.className = matcher.group(3);
            testcase.name = matcher.group(2);
            session.addTestCase(testcase);
        }
    }

    /**
     * Captures the rest of %TEST_* patterns that are not handled
     * otherwise (yet). 
     */
    static class TestMiscHandler extends TestRecognizerHandler {

        public TestMiscHandler() {
            super("%TEST_.*"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SuiteFinishedHandler extends TestRecognizerHandler {

        public SuiteFinishedHandler() {
            super("%SUITE_FINISHED%\\stime=(.+)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            String timeInSeconds = matcher.group(1);
            Double elapsedTimeMillis = Double.parseDouble(timeInSeconds) * 1000;
            Report result = session.getReport();
            result.elapsedTimeMillis = elapsedTimeMillis.intValue();
            manager.displayReport(session, result);
        }
    }

    static class SuiteStartedHandler extends TestRecognizerHandler {

        public SuiteStartedHandler() {
            super("%SUITE_STARTED%\\s.*"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    static class SuiteErrorOutputHandler extends TestRecognizerHandler {

        public SuiteErrorOutputHandler() {
            super("%SUITE_ERROR_OUTPUT%\\serror=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            manager.displayOutput(session, matcher.group(1), true);
            manager.displayOutput(session, "", false);
        }

        @Override
        RecognizedOutput getRecognizedOutput() {
            return new FilteredOutput(matcher.group(1));
        }
        
    }

    static class SuiteStartingHandler extends TestRecognizerHandler {

        private boolean firstSuite = true;
        
        public SuiteStartingHandler() {
            super("%SUITE_STARTING%\\s(.+)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            if (firstSuite) {
                firstSuite = false;
                manager.testStarted(session);
            }
            String suiteName = matcher.group(1);
            session.setSuiteName(suiteName);
            manager.displaySuiteRunning(session, suiteName);
        }
    }

    /**
     * Captures the rest of %SUITE_* patterns that are not handled
     * otherwise (yet). 
     */
    static class SuiteMiscHandler extends TestRecognizerHandler {

        public SuiteMiscHandler() {
            super("%SUITE_.*"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
        }
    }

    /**
     * Captures output meant for logging.
     */
    static class TestLoggerHandler extends TestRecognizerHandler {

        public TestLoggerHandler() {
            super("%TEST_LOGGER%\\slevel=(.+)\\smsg=(.*)"); //NOI18N
        }

        @Override
        void updateUI( Manager manager, TestSession session) {
            Level level = Level.parse(matcher.group(1));
            if (LOGGER.isLoggable(level))
                LOGGER.log(level, matcher.group(2));
        }
    }
}
