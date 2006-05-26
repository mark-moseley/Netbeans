/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 * Obtains events from a single session of an Ant <code>junit</code> task
 * and builds a {@link Report}.
 * The events are delivered by the {@link JUnitAntLogger}.
 *
 * @see  JUnitAntLogger
 * @see  Report
 * @author  Marian Petras
 */
final class JUnitOutputReader {
    
    private static final int MAX_REPORT_FILE_SIZE = 1 << 19;    //512 kBytes
    /** */
    private static final int UPDATE_DELAY = 300;    //milliseconds
    
    /** */
    private boolean testTargetStarted = false;
    /** */
    private boolean testTaskStarted = false;
    /**
     * did we already get statistics of tests/failures/errors for the current
     * report?
     */
    private boolean testsuiteStatsKnown = false;   //see issue #74979
    
    /** */
    private final AntSession session;
    /** */
    private final TaskType sessionType;
    /** */
    private final File antScript;
    /** */
    private final long timeOfSessionStart;
    
    /** */
    private RegexpUtils regexp = RegexpUtils.getInstance();
    
    /** */
    private Report topReport;
    /** */
    private Report report;
    /** */
    private Report.Testcase testcase;
    /** */
    private Report.Trouble trouble;
    /** */
    private String suiteName;
    
    /** */
    private List/*<String>*/ callstackBuffer;
    
    /** */
    private StringBuffer xmlOutputBuffer;
    
    /**
     * Are we reading standard output or standard error output?
     * This variable is used only when reading output from the test cases
     * (when {@link #outputBuffer} is non-<code>null</code>).
     * If <code>true</code>, standard output is being read,
     * if <code>false</code>, standard error output is being read.
     */
    private boolean readingOutputReport;
    /** */
    private boolean lastHeaderBrief;
    /** */
    private boolean waitingForIssueStatus;
    /** */
    private final Manager manager = Manager.getInstance();
    /** */
    private String classpath;
    /** */
    private ClassPath platformSources;
    
    
    /** Creates a new instance of JUnitOutputReader */
    JUnitOutputReader(final AntSession session,
                      final TaskType sessionType,
                      final long timeOfSessionStart) {
        this.session = session;
        this.sessionType = sessionType;
        this.antScript = session.getOriginatingScript();
        this.timeOfSessionStart = timeOfSessionStart;
    }
    
    /**
     */
    void verboseMessageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        
        /* Look for classpaths: */

        /* Code copied from JavaAntLogger */

        Matcher matcher;

        matcher = RegexpUtils.CLASSPATH_ARGS.matcher(msg);
        if (matcher.find()) {
            this.classpath = matcher.group(1);
        }
        // XXX should also probably clear classpath when taskFinished called
        matcher = RegexpUtils.JAVA_EXECUTABLE.matcher(msg);
        if (matcher.find()) {
            String executable = matcher.group(1);
            ClassPath platformSrcs = findPlatformSources(executable);
            if (platformSrcs != null) {
                this.platformSources = platformSrcs;
            }
        }
    }
    
    /**
     */
    void messageLogged(final AntEvent event) {
        final String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        
        //<editor-fold defaultstate="collapsed" desc="if (waitingForIssueStatus) ...">
        if (waitingForIssueStatus) {
            assert testcase != null;
            
            Matcher matcher = regexp.getTestcaseIssuePattern().matcher(msg);
            if (matcher.matches()) {
                boolean error = (matcher.group(1) == null);
            
                trouble = (testcase.trouble = new Report.Trouble(error));
                waitingForIssueStatus = false;
                return;
            } else {
                report.reportTest(testcase);
                waitingForIssueStatus = false;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (xmlOutputBuffer != null) ...">
        if (xmlOutputBuffer != null) {
            xmlOutputBuffer.append(msg).append('\n');
            if (msg.equals("</testsuite>")) {                           //NOI18N
                closePreviousReport();
            }
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (readingOutputReport) ...">
        if (readingOutputReport) {
            if (msg.startsWith(RegexpUtils.OUTPUT_DELIMITER_PREFIX)) {
                Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
                if (matcher.matches() && (matcher.group(1) == null)) {
                    readingOutputReport = false;
                }
            }
            return;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="if (trouble != null) ...">
        if (trouble != null) {
            Matcher matcher;
            if (trouble.exceptionClsName == null) {
                matcher = regexp.getTestcaseExceptionPattern().matcher(msg);
                if (matcher.matches()) {
                    trouble.exceptionClsName = matcher.group(1);
                    String exceptionMsg = matcher.group(2);
                    if (exceptionMsg != null) {
                        trouble.message = exceptionMsg;
                    }
                }
                return;     //ignore other texts until
                            //we get exception class name
            }
            String trimmed = RegexpUtils.specialTrim(msg);
            if (trimmed.length() == 0) {
                if (callstackBuffer != null) {
                    
                    /* finalize the trouble and clear buffers: */
                    trouble.stackTrace = (String[]) callstackBuffer.toArray(
                                            new String[callstackBuffer.size()]);
                    callstackBuffer = null;
                }
                report.reportTest(testcase);

                trouble = null;
                testcase = null;
                return;
            }
            if (trimmed.startsWith(RegexpUtils.CALLSTACK_LINE_PREFIX_CATCH)) {
                trimmed = trimmed.substring(
                              RegexpUtils.CALLSTACK_LINE_PREFIX_CATCH.length());
            }
            if (trimmed.startsWith(RegexpUtils.CALLSTACK_LINE_PREFIX)) {
                matcher = regexp.getCallstackLinePattern().matcher(msg);
                if (matcher.matches()) {
                    if (callstackBuffer == null) {
                        callstackBuffer = new ArrayList(8);
                    }
                    callstackBuffer.add(
                            trimmed.substring(
                                   RegexpUtils.CALLSTACK_LINE_PREFIX.length()));
                    setClasspathSourceRoots();
                    return;
                }
            }
            if ((callstackBuffer == null) && (trouble.message != null)) {
                trouble.message = trouble.message + '\n' + msg;
            }
            /* else: just ignore the text */
            return;
        }//</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="TESTCASE_PREFIX">
        if (msg.startsWith(RegexpUtils.TESTCASE_PREFIX)) {

            if (report == null) {
                return;
            }
            
            String header = msg.substring(RegexpUtils.TESTCASE_PREFIX.length());
            
            boolean success =
                lastHeaderBrief
                ? tryParseBriefHeader(header)
                    || !(lastHeaderBrief = !tryParsePlainHeader(header))
                : tryParsePlainHeader(header)
                    || (lastHeaderBrief = tryParseBriefHeader(header));
            if (success) {
                waitingForIssueStatus = !lastHeaderBrief;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="OUTPUT_DELIMITER_PREFIX">
        else if (msg.startsWith(RegexpUtils.OUTPUT_DELIMITER_PREFIX)) {

            if (report == null) {
                return;
            }
            
            Matcher matcher = regexp.getOutputDelimPattern().matcher(msg);
            if (matcher.matches()) {
                readingOutputReport = true;
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="XML_DECL_PREFIX">
        else if (msg.startsWith(RegexpUtils.XML_DECL_PREFIX)) {
            Matcher matcher = regexp.getXmlDeclPattern().matcher(msg.trim());
            if (matcher.matches()) {
                suiteStarted(null);
                
                xmlOutputBuffer = new StringBuffer(4096);
                xmlOutputBuffer.append(msg);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_PREFIX">
        else if (msg.startsWith(RegexpUtils.TESTSUITE_PREFIX)) {
            suiteName = msg.substring(RegexpUtils.TESTSUITE_PREFIX
                                             .length());
            if (regexp.getFullJavaIdPattern().matcher(suiteName).matches()){
                suiteStarted(suiteName);
                report.resultsDir = determineResultsDir(event);
            }
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TESTSUITE_STATS_PREFIX">
        else if (msg.startsWith(RegexpUtils.TESTSUITE_STATS_PREFIX)) {

            if (report == null) {
                return;
            }
            if (testsuiteStatsKnown) {
                return;                     //see issue #74979
            }
            
            Matcher matcher = regexp.getSuiteStatsPattern().matcher(msg);
            if (matcher.matches()) {
                assert report != null;
                
                try {
                    report.totalTests = Integer.parseInt(matcher.group(1));
                    report.failures = Integer.parseInt(matcher.group(2));
                    report.errors = Integer.parseInt(matcher.group(3));
                    report.elapsedTimeMillis
                            = regexp.parseTimeMillis(matcher.group(4));
                } catch (NumberFormatException ex) {
                    //if the string matches the pattern, this should not happen
                    assert false;
                }
            }
            testsuiteStatsKnown = true;
        }//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Test ... FAILED">
        else if ((suiteName != null)
                && msg.startsWith("Test ")                              //NOI18N
                && msg.endsWith(" FAILED")                              //NOI18N
                && msg.equals("Test " + suiteName + " FAILED")) {       //NOI18N
            suiteName = null;
            //PENDING - stop the timer (if any)?
            //PENDING - perform immediate update (if necessary)?
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="output">
        else {
            displayOutput(msg,
                          event.getLogLevel() == AntEvent.LOG_WARN);
        }
        //</editor-fold>
    }
    
    /**
     * Tries to determine test results directory.
     *
     * @param  event  Ant event serving as a source of information
     * @return  <code>File<code> object representing the results directory,
     *          or <code>null</code> if the results directory could not be
     *          determined
     */
    private static File determineResultsDir(final AntEvent event) {
        File resultsDir = null;
        String dirName = null;
        
        TaskStructure taskStruct = event.getTaskStructure();
        if (taskStruct != null) {
            final TaskStructure[] taskChildren = taskStruct.getChildren();
            if (taskChildren.length != 0) {
                for (int i = 0; i < taskChildren.length; i++) {
                    TaskStructure taskChild = taskChildren[i];
                    String taskChildName = taskChild.getName();
                    if (taskChildName.equals("batchtest")               //NOI18N
                            || taskChildName.equals("test")) {          //NOI18N
                        String dirAttr =taskChild.getAttribute("todir");//NOI18N
                        dirName = (dirAttr != null)
                                  ? event.evaluate(dirAttr)
                                  : ".";                                //NOI18N
                            /* default is the current directory (Ant manual) */
                        break;
                    }
                }
            }
        }
        
        if (dirName != null) {
            resultsDir = new File(dirName);
            if (!resultsDir.isAbsolute()) {
                resultsDir = new File(event.getProperty("basedir"),     //NOI18N
                                      dirName);
            }
            if (!resultsDir.exists() || !resultsDir.isDirectory()) {
                resultsDir = null;
            }
        } else {
            resultsDir = null;
        }
        
        return resultsDir;
    }
    
    /**
     */
    private Report createReport(final String suiteName) {
        Report report = new Report(suiteName);
        report.antScript = antScript;
        
        report.classpath = classpath;
        report.platformSources = platformSources;
        
        this.classpath = null;
        this.platformSources = null;
        
        return report;
    }
    
    /**
     */
    private ClassPath findPlatformSources(final String javaExecutable) {
        
        /* Copied from JavaAntLogger */
        
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault()
                                         .getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java");              //NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f.getAbsolutePath().startsWith(javaExecutable)) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }
    
    /**
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher
     * with -cp) and stores it in the current report.
     * <!-- copied from JavaAntLogger -->
     * <!-- XXX: move to class Report -->
     */
    private void setClasspathSourceRoots() {
        
        /* Copied from JavaAntLogger */
        
        if (report == null) {
            return;
        }
        
        if (report.classpathSourceRoots != null) {      //already set
            return;
        }
        
        if (report.classpath == null) {
            return;
        }
        
        Collection/*<FileObject>*/ sourceRoots = new LinkedHashSet();
        final StringTokenizer tok = new StringTokenizer(report.classpath,
                                                        File.pathSeparator);
        while (tok.hasMoreTokens()) {
            String binrootS = tok.nextToken();
            File f = FileUtil.normalizeFile(new File(binrootS));
            URL binroot;
            try {
                binroot = f.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            if (FileUtil.isArchiveFile(binroot)) {
                URL root = FileUtil.getArchiveRoot(binroot);
                if (root != null) {
                    binroot = root;
                }
            }
            FileObject[] someRoots = SourceForBinaryQuery
                                     .findSourceRoots(binroot).getRoots();
            sourceRoots.addAll(Arrays.asList((Object[]) someRoots));
        }

        if (report.platformSources != null) {
            sourceRoots.addAll(Arrays.asList(report.platformSources.getRoots()));
        } else {
            // no platform found. use default one:
            JavaPlatform platform = JavaPlatform.getDefault();
            // in unit tests the default platform may be null:
            if (platform != null) {
                sourceRoots.addAll(
                        Arrays.asList(platform.getSourceFolders().getRoots()));
            }
        }
        report.classpathSourceRoots = sourceRoots;
        
        /*
         * The following fields are no longer necessary
         * once the source classpath is defined:
         */
        report.classpath = null;
        report.platformSources = null;
    }
    
    /**
     */
    void testTargetStarted() {
        checkTestTargetStarted();
    }
    
    /**
     */
    void testTaskStarted() {
        checkTestTaskStarted();
    }
    
    /**
     */
    void buildFinished(final AntEvent event) {
        finishReport(event.getException());
        Manager.getInstance().sessionFinished(session,
                                              sessionType,
                                              testTaskStarted == false);
    }
    
    /**
     */
    private Report suiteStarted(final String suiteName) {
        closePreviousReport();
        report = createReport(suiteName);
                
        Manager.getInstance().displaySuiteRunning(session,
                                                  sessionType,
                                                  suiteName);
        return report;
    }
    
    /**
     */
    private void suiteFinished(final Report report) {
        Manager.getInstance().displayReport(session, sessionType, report);
    }
    
    /**
     */
    void finishReport(final Throwable exception) {
        if (waitingForIssueStatus) {
            assert testcase != null;
            
            report.reportTest(testcase);
        }
        closePreviousReport();
        
        //<editor-fold defaultstate="collapsed" desc="disabled code">
        //PENDING:
        /*
        int errStatus = ResultWindow.ERR_STATUS_OK;
        if (exception != null) {
            if (exception instanceof java.lang.ThreadDeath) {
                errStatus = ResultWindow.ERR_STATUS_INTERRUPTED;
            } else {
                errStatus = ResultWindow.ERR_STATUS_EXCEPTION;
            }
        }
         */
        
        /*
        //PENDING: final int status = errStatus;
        Mutex.EVENT.postWriteRequest(new Runnable() {
            public void run() {
                //PENDING:
                //ResultWindow resultView = ResultWindow.getInstance();
                //resultView.displayReport(topReport, status, antScript);
                
                final TopComponent resultWindow = ResultWindow.getDefault();
                resultWindow.open();
                resultWindow.requestActive();
            }
        });
         */
        //</editor-fold>
    }
    
    //------------------ UPDATE OF DISPLAY -------------------
    
    /**
     */
    private void displayOutput(final String text, final boolean error) {
        Manager.getInstance().displayOutput(session, sessionType, text, error);
    }
    
    //--------------------------------------------------------
    
    /**
     */
    private void checkTestTargetStarted() {
        if (!testTargetStarted) {
            testTargetStarted = true;
            Manager.getInstance().targetStarted(session, sessionType);
        }
    }
    
    /**
     */
    private void checkTestTaskStarted() {
        if (!testTaskStarted) {
            testTargetStarted = true;
            testTaskStarted = true;
            Manager.getInstance().testStarted(session, sessionType);
        }
    }
    
    /**
     */
    private boolean tryParsePlainHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderPlainPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            int timeMillis = regexp.parseTimeMillisNoNFE(matcher.group(2));
            
            testcase = new Report.Testcase();
            testcase.className = null;
            testcase.name = methodName;
            testcase.timeMillis = timeMillis;
            
            trouble = null;
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     */
    private boolean tryParseBriefHeader(String testcaseHeader) {
        final Matcher matcher = regexp.getTestcaseHeaderBriefPattern()
                                .matcher(testcaseHeader);
        if (matcher.matches()) {
            String methodName = matcher.group(1);
            String clsName = matcher.group(2);
            boolean error = (matcher.group(3) == null);

            testcase = new Report.Testcase();
            testcase.className = clsName;
            testcase.name = methodName;
            testcase.timeMillis = -1;

            trouble = (testcase.trouble = new Report.Trouble(error));
            
            return true;
        } else {
            return false;
        }
    }
    
    /**
     */
    private void closePreviousReport() {
        if (xmlOutputBuffer != null) {
            try {
                String xmlOutput = xmlOutputBuffer.toString();
                xmlOutputBuffer = null;     //allow GC before parsing XML
                Report xmlReport;
                xmlReport = XmlOutputParser.parseXmlOutput(
                                                new StringReader(xmlOutput));
                report.update(xmlReport);
            } catch (SAXException ex) {
                /* initialization of the parser failed, ignore the output */
            } catch (IOException ex) {
                assert false;           //should not happen (StringReader)
            }
        } else if ((report != null) && (report.resultsDir != null)) {
            /*
             * We have parsed the output but it seems that we also have
             * an XML report file available - let's use it:
             */
            
            File reportFile = new File(
                              report.resultsDir,
                              "TEST-" + report.suiteClassName + ".xml");//NOI18N
            if (reportFile.exists()
                    && reportFile.isFile() && reportFile.canRead()
                    && (reportFile.lastModified() >= timeOfSessionStart)) {
                final long fileSize = reportFile.length();
                if ((fileSize > 0l) && (fileSize <= MAX_REPORT_FILE_SIZE)) {
                    try {
                        Report fileReport;
                        fileReport = XmlOutputParser.parseXmlOutput(
                                new InputStreamReader(
                                        new FileInputStream(reportFile),
                                        "UTF-8"));                      //NOI18N
                        report.update(fileReport);
                    } catch (UnsupportedCharsetException ex) {
                        assert false;
                    } catch (SAXException ex) {
                        /* This exception has already been handled. */
                    } catch (IOException ex) {
                        /*
                         * Failed to read the report file - but we still have
                         * the report built from the Ant output.
                         */
                        int severity = ErrorManager.INFORMATIONAL;
                        ErrorManager errMgr = ErrorManager.getDefault();
                        if (errMgr.isLoggable(severity)) {
                            errMgr.notify(
                                    severity,
                                    errMgr.annotate(
     ex,
     "I/O exception while reading JUnit XML report file from JUnit: "));//NOI18N
                        }
                    }
                }
            }
        }
        if (report != null) {
            suiteFinished(report);
        }
        
        callstackBuffer = null;
        xmlOutputBuffer = null;
        readingOutputReport = false;
        testcase = null;
        trouble = null;
        report = null;
        testsuiteStatsKnown = false;
    }
    
}
