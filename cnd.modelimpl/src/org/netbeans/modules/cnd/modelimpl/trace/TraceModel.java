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
package org.netbeans.modules.cnd.modelimpl.trace;

import java.text.NumberFormat;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import java.io.*;
import java.util.*;
import java.util.List;

import antlr.*;
import antlr.collections.*;

import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.editor.parser.FoldingParser;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.util.Lookup;

/**
 * Tracer for model
 * @author Vladimir Kvasihn
 */
public class TraceModel extends TraceModelBase {

    private static class TestResult {

        private long time;
        private long lineCount;

        public TestResult() {
            this(0);
        }

        public TestResult(long time) {
            this.setTime(time);
        }

        public TestResult(long time, long lineCount) {
            this.setTime(time);
            this.setLineCount(lineCount);
        }

        public String getLPS() {
            if (time == 0 || lineCount <= 0) {
                return "N/A";  // NOI18N
            } else {
                return "" + (lineCount * 1000 / time);
            }
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getLineCount() {
            return (lineCount < 0) ? 0 : lineCount;
        }

        public boolean isLineCountValid() {
            return lineCount >= 0;
        }

        public void setLineCount(long lineCount) {
            this.lineCount = lineCount;
        }

        public void accumulate(TestResult toAdd) {
            time += toAdd.time;
            if (isLineCountValid()) {
                if (toAdd.isLineCountValid()) {
                    lineCount += toAdd.getLineCount();
                } else {
                    //		    lineCount = -1;
                }
            }
        }
    }
    private static final int APT_REPEAT_TEST = Integer.getInteger("apt.repeat.test", 3).intValue(); // NOI18N

    public static void main(String[] args) {
        new TraceModel().test(args);
        APTDriver.getInstance().close();
    //System.out.println("" + org.netbeans.modules.cnd.apt.utils.APTIncludeUtils.getHitRate());
    }
    private Cache cache;
    private static CsmTracer tracer = new CsmTracer(false);
    private boolean showAstWindow = false;
    private boolean dumpAst = false;
    private boolean dumpModel = false;
    private boolean dumpLib = false;
    private boolean dumpFileOnly = false;
    private boolean showTime = false;
    //private boolean showErrorCount = false;
    private boolean writeAst = false;
    private boolean readAst = false;
    private boolean useZip = false;
    private boolean testLibProject = false;
    private boolean deep = true;
    private boolean showMemoryUsage = false;
    private boolean testUniqueName = false;
    private boolean testAPT = false;
    private boolean testAPTLexer = false;
    private boolean testAPTDriver = false;
    private boolean testAPTWalkerVisit = false;
    private boolean testAPTWalkerGetStream = false;
    private boolean testAPTWalkerGetExpandedStream = false;
    private boolean testAPTWalkerGetFilteredStream = false;
    private boolean testAPTParser = false;
    private boolean breakAfterAPT = false;
    private boolean stopBeforeAll = false;
    private boolean stopAfterAll = false;
    private boolean printTokens = false;
    private boolean dumpModelAfterCleaningCache = false; // --clean4dump
    private boolean dumpTemplateParameters = false; // --tparm
    private int repeatCount = 1; // --repeat
    private boolean dumpStatistics = false;
    private static final int DEFAULT_TRACEMODEL_STATISTICS_LEVEL = 1;
    private String dumpFile = null;
    private String dumpDir = null;
    private static final String statPostfix = ".stat"; // NOI18N

    // Cache options
    private boolean enableCache = false;
    private boolean testCache = false;

    // Callback options
    private boolean dumpPPState = false;

    public void setDumpModel(boolean dumpModel) {
        this.dumpModel = dumpModel;
    }

    public void setDumpPPState(boolean dumpPPState) {
        this.dumpPPState = dumpPPState;
    }
    private boolean listFilesAtEnd = false;
    private boolean testRawPerformance = false;
    private boolean printUserFileList = false;
    private boolean quiet = false;
    private boolean memBySize = false;
    private boolean doCleanRepository = Boolean.getBoolean("cnd.clean.repository");
    private boolean testFolding = false;
    private Map<String, Long> cacheTimes = new HashMap<String, Long>();
    private int lap = 0;
    private final Map<CsmFile, APTPreprocHandler> states = new HashMap<CsmFile, APTPreprocHandler>();
    FileImpl.Hook hook = new FileImpl.Hook() {

        public void parsingFinished(CsmFile file, APTPreprocHandler preprocHandler) {
            states.put(file, preprocHandler);
        }
    };

    public TraceModel() {
        FileImpl.setHook(hook);
    }

    @Override
    protected ProcessFlagResult processFlag(char flag, String argRest) {
        ProcessFlagResult result = super.processFlag(flag, argRest);
        if (result != ProcessFlagResult.NONE_PROCESSED) {
            return result;
        }
        // it's easier to set the most "popular" return value here and NONE_... in default case
        result = ProcessFlagResult.CHAR_PROCESSED;
        switch (flag) {
            case 'n':
                deep = false;
                break;
            case 'e':
                System.setErr(System.out);
                break;
            case 'w':
                showAstWindow = true;
                break;
            case 'a':
                dumpAst = true;
                break;
            case 'm':
                dumpModel = true;
                dumpFileOnly = false;
                break; // -m overrides -f
            case 'M':
                showMemoryUsage = true;
                break;
            case 'u':
                testUniqueName = true;
                break;
            case 'f':
                if (!dumpModel) { // do not ovverride -m
                    dumpModel = true;
                    dumpFileOnly = true;
                }
                break;
            case 't':
                showTime = true;
                break;
            //            case 'L':   testLexer = true; break;
            //case 'c':   showErrorCount = true; break;
            case 'W':
                writeAst = true;
                break;
            case 'R':
                readAst = true;
                break;
            case 'Z':
                useZip = true;
                break;
            case 'C':
                enableCache = true;
                break;
            case 'l':
                testLibProject = true;
                break;
            case 'c':
                testCache = true;
                break;
            case 'p':
                dumpPPState = true;
                break;
            // "-SDir" defines dump directory for per file statistics
            case 'S':
                dumpStatistics = true;
                if (argRest.length() > 0) {
                    // dump directory for per file statistics
                    File perFileDumpDir = new File(argRest);
                    perFileDumpDir.mkdirs();
                    if (!perFileDumpDir.isDirectory()) {
                        print("Parameter -S" + argRest + " does not specify valid directory"); // NOI18N
                    } else {
                        this.dumpDir = perFileDumpDir.getAbsolutePath();
                    }
                    result = ProcessFlagResult.ALL_PROCESSED;
                }
                break;
            // "-sFileName" defines global statistics dump file
            case 's':
                dumpStatistics = true;
                if (argRest.length() > 0) {
                    File globalDumpFile = new File(argRest);
                    if (globalDumpFile.exists()) {
                        globalDumpFile.delete();
                    }
                    try {
                        if (globalDumpFile.getParentFile() != null) {
                            globalDumpFile.getParentFile().mkdirs();
                            globalDumpFile.createNewFile();
                            this.dumpFile = globalDumpFile.getAbsolutePath();
                            result = ProcessFlagResult.ALL_PROCESSED;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case 'A':
                testAPT = true;
                testAPTWalkerVisit = true;
                testAPTWalkerGetStream = true;
                testAPTWalkerGetExpandedStream = true;
                testAPTWalkerGetFilteredStream = true;
                testAPTLexer = true;
                breakAfterAPT = true;
                testAPTDriver = true;
                break;
            //            case 'b':   testAPTPlainLexer = true; testAPT = true; breakAfterAPT = true; break;
            case 'B':
                testAPTLexer = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'o':
                printTokens = true;
                break;
            case 'v':
                testAPTWalkerVisit = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'g':
                testAPTWalkerGetStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'G':
                testAPTWalkerGetExpandedStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'F':
                testAPTWalkerGetFilteredStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'd':
                testAPTDriver = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'h':
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'H':
                testAPTParser = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'O':
                stopBeforeAll = true;
                stopAfterAll = true;
                break;
            case 'q':
                quiet = true;
                break;
            default:
                result = ProcessFlagResult.NONE_PROCESSED;
        }
        return result;
    }

    @Override
    protected boolean processFlag(String flag) {
        if (super.processFlag(flag)) {
            return true;
        } else if ("dumplib".equals(flag)) { // NOI18N
            dumpLib = true;
        } else if ("listfiles".equals(flag)) { // NOI18N
            listFilesAtEnd = true;
        } else if ("raw".equals(flag)) { // NOI18N
            testRawPerformance = true;
        //TraceFlags.DO_NOT_RENDER = true;
        } else if ("listfiles".equals(flag)) { // NOI18N
            printUserFileList = true;
        } else if ("mbs".equals(flag)) { // NOI18N
            memBySize = true;
        } else if ("cleanrepository".equals(flag)) { // NOI18N
            doCleanRepository = true;
        } else if ("folding".equals(flag)) { // NOI18N
            testFolding = true;
        } else if ("clean4dump".equals(flag)) { // NOI18N
            dumpModelAfterCleaningCache = true;
        } else if ("tparm".equals(flag)) { // NOI18N
            dumpTemplateParameters = true;
        } else if ("repeat".equals(flag) || flag.startsWith("repeat:")) { // NOI18N
            int len = "repeat".length(); // NOI18N
            if (flag.length() == len) {
                repeatCount = 2;
            } else {
                repeatCount = Integer.parseInt(flag.substring(len + 1));
            }
        } else {
            return false;
        }
        return true;
    }

    private void test(String[] args) {
        try {
            processArguments(args);
            doTest();
        } catch (Error thr) {
            System.err.printf("\n");
            DiagnosticExceptoins.register(thr);
            return;
        } finally {
            getModel().shutdown();
        }
    }

    /*package*/
    void doTest() {
        if (repeatCount > 1) {
            for (int i = 0; i < repeatCount; i++) {
                print("\n\n==================== Pass " + i + "====================\n"); // NOI18N
                doTest2();
                resetProject();
            //sleep(2000, "Waiting for repository to shutdown");
            }
        } else {
            doTest2();
        }
    }

    /*package*/
    void doTest2() {
        if (stopBeforeAll) {
            waitAnyKey();
        }
        if (writeAst || readAst) {
            try {
                cache = new Cache(useZip);
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
                return;
            }
        }

        if (writeAst && readAst) {
            print("Impossible options combination: both writing and reading AST\n"); // NOI18N
            return;
        }
        if (useZip && !(writeAst || readAst)) {
            print("Impossible options combination: using ZIP format, but neither writing nor reading AST. Ignoring ZIP format.\n"); // NOI18N
            useZip = false;
        }

        if (testCache) {
            //print("Test cache mode ON." + '\n');
            enableCache = true;
        } else if (dumpStatistics) {
            if (dumpFile == null && dumpDir == null) {
                print("Turning OFF statistics as neither global file nor directory is specified"); // NOI18N
                dumpStatistics = false;
            } else {
                print("Dumping Statistics is ON"); // NOI18N
                if (Diagnostic.getStatisticsLevel() == 0) {
                    // need to set the default level
                    Diagnostic.setStatisticsLevel(DEFAULT_TRACEMODEL_STATISTICS_LEVEL);
                }
                if (dumpFile != null) {
                    print("Global Dump file is " + dumpFile); // NOI18N
                }
                if (dumpDir != null) {
                    print("Dump directory for per file statistics is " + dumpDir); // NOI18N
                }
            }
        }

        if (testLibProject) {
            testLibProject();
        }

        if (printUserFileList) {
            print("Processing files:\n"); // NOI18N
            for (NativeFileItem file : getFileItems()) {
                print(file.getFile().getAbsolutePath() + ' ', false);
            }
            print("");
        }

        long memUsed = 0;
        if (showMemoryUsage) {
            memUsed = usedMemory();
        }

        long t = System.currentTimeMillis();
        TestResult total = test();
        total.time = System.currentTimeMillis() - t;

        if (testRawPerformance) {
            print("Take one finished."); // NOI18N
            print("Total parsing time " + total.time + " ms"); // NOI18N
            calculateAverageLPS(total, true);
            print("Lines count " + total.lineCount); // NOI18N
            print("Average LPS " + total.getLPS()); // NOI18N
            if (showMemoryUsage) {
                showMemoryUsage(memUsed);
            }

            //	    for (int i = 0; i < 100; i++) {
            //		initProject();
            //		test();
            //		showMemoryUsage(memUsed);
            //	    }
            print("\nTesting raw performance: parsing project, take two\n"); // NOI18N
            resetProject();
            if (stopBeforeAll) {
                waitAnyKey();
            }
            t = System.currentTimeMillis();
            total = test();
            total.time = System.currentTimeMillis() - t;
        }

        /* this unnecessary since we call waitProjectParsed() for each file
        if( showTime ) {
        print("Waiting for the rest of the parser queue to be parsed");
        }
        waitProjectParsed();
         */

        if (dumpLib) {
            for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = (CsmProject) it.next();
                tracer.dumpModel(lib);
            }
        }

        if (isShowTime()) {

            int maxLen = 0;
            for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                if (CPPParserEx.guessingNames[i] != null) {
                    int len = CPPParserEx.guessingNames[i].length();
                    if (len > maxLen) {
                        maxLen = len;
                    }
                }
            }

            boolean printGuessStat = false;
            // check if we had the statistics
            for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                if (CPPParserEx.guessingCount[i] != 0) {
                    printGuessStat = true;
                    break;
                }
            }
            if (listFilesAtEnd) {
                print("\n========== User project files =========="); // NOI18N
                List<CharSequence> l = new ArrayList<CharSequence>(getProject().getAllFiles().size());
                for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
                    CsmFile file = (CsmFile) it.next();
                    l.add(file.getAbsolutePath());
                }
                Collections.sort(l, CharSequenceKey.Comparator);
                for (Iterator it = l.iterator(); it.hasNext();) {
                    print((String) it.next());
                }
                print("\n========== Library files =========="); // NOI18N
                l = new ArrayList<CharSequence>();
                for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
                    ProjectBase lib = (ProjectBase) it1.next();
                    for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
                        CsmFile file = (CsmFile) it2.next();
                        l.add(file.getAbsolutePath());
                    }
                }
                Collections.sort(l, CharSequenceKey.Comparator);
                for (Iterator it = l.iterator(); it.hasNext();) {
                    print((String) it.next());
                }
            }
            if (printGuessStat) {
                print("\nGuessing statistics:"); // NOI18N
                print(
                        "Id" // NOI18N
                        + "\t" + padR("Rule:Line", maxLen) // NOI18N
                        + "\tTime" // NOI18N
                        + "\tCount" // NOI18N
                        + "\tFail" // NOI18N
                        //+ "\tTime in failures"
                        + "\tSuccess, %"); // NOI18N
                long guessingTime = 0;
                for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                    guessingTime += CPPParserEx.guessingTimes[i];
                    //double sps = (CPPParserEx.guessingTimes[i] !=0) ? ((double)CPPParserEx.guessingCount[i])/CPPParserEx.guessingTimes[i] : 0;
                    double usa = 0;
                    if (CPPParserEx.guessingCount[i] != 0) {
                        usa = (1 - ((double) CPPParserEx.guessingFailures[i]) / CPPParserEx.guessingCount[i]) * 100;
                    }
                    print("" + i + "\t" + padR(CPPParserEx.guessingNames[i], maxLen) // NOI18N
                            + "\t" + CPPParserEx.guessingTimes[i] // NOI18N
                            + "\t" + CPPParserEx.guessingCount[i] // NOI18N
                            + "\t" + CPPParserEx.guessingFailures[i] // NOI18N
                            //+ "\t" + (int)sps
                            + "\t" + (int) usa); // NOI18N
                }

                print("\nTotal guessing time: " + guessingTime + "ms " + "(" + ((total.getTime() != 0) ? guessingTime * 100 / total.getTime() : -1) + "% of total parse time)"); // NOI18N
            }
        }
        if (isShowTime() || testRawPerformance) {
            print("Total parsing time: " + total.getTime() + "ms"); // NOI18N
            //print("Average LPS: " + total.getLPS());
            calculateAverageLPS(total, !testRawPerformance);
            print("Lines count " + total.lineCount); // NOI18N
            String text = testRawPerformance ? "Raw performance (average LPS): " : "Average LPS: "; // NOI18N
            print(text + total.getLPS());
            int userFiles = countUserFiles();
            int systemHeaders = countSystemHeaders();
            print("" + userFiles + " user files"); // NOI18N
            print("" + systemHeaders + " system headers"); // NOI18N
        }
        if (showMemoryUsage) {
            showMemoryUsage(memUsed);
        }
        if (isShowTime() || showMemoryUsage || dumpModel || dumpFileOnly || dumpPPState) {
            print("\n"); // NOI18N
        }
        if (dumpStatistics) {
            if (this.dumpFile != null) {
                try {
                    Diagnostic.dumpUnresolvedStatistics(this.dumpFile, true);
                } catch (FileNotFoundException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
        }

        if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
            List restoredFiles = ProjectBase.testGetRestoredFiles();
            if (restoredFiles != null) {
                System.err.println("the number of restored files " + restoredFiles.size());
                for (int i = 0; i < restoredFiles.size(); i++) {
                    System.err.println("#" + i + ":" + restoredFiles.get(i));
                }
            }
        }

        if (dumpModelAfterCleaningCache) {
            anyKey("Press any key to clean repository:"); // NOI18N
            RepositoryAccessor.getRepository().debugClear();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            anyKey("Press any key to dump model:"); // NOI18N
            if (!dumpFileOnly) {
                tracer.dumpModel(getProject());
            }
        }
        if (stopAfterAll) {
            System.gc();
            anyKey("Press any key to finish:"); // NOI18N
        }
    }

    private void anyKey(String message) {
        System.err.println(message);
        try {
            System.in.read();
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }

    private void showMemoryUsage(long memUsed) {
        long newMemUsed = usedMemory();
        long memDelta = newMemUsed - memUsed;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(6);
        print("Amount of memory used" + getLap() + ": " + nf.format((memDelta) / 1024) + " Kb"); // NOI18N
        if (memBySize) {
            TestResult rInc = new TestResult();
            TestResult rExc = new TestResult();
            calculateAverageLPS(rInc, true);
            calculateAverageLPS(rExc, false);
            print("User code lines:  " + rExc.lineCount); // NOI18N
            print("Total lines (including all headers):  " + rInc.lineCount); // NOI18N
            print("Memory usage per (user) line " + getLap() + '\t' + nf.format(memDelta / rExc.lineCount) + " bytes per line"); // NOI18N
            print("Memory usage per (total) line" + getLap() + '\t' + nf.format(memDelta / rInc.lineCount) + " bytes per line"); // NOI18N
        }
    }

    private void waitAnyKey() {
        System.out.println("Press any key to continue:"); // NOI18N
        try {
            System.in.read();
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }

    private TestResult test() {
        lap++;
        TestResult total = new TestResult();
        //for (int i = 0; i < fileList.size(); i++) {
        for (NativeFileItem item : getFileItems()) {
            try {
                if (!testFolding) {
                    TestResult res = test(item);
                    total.accumulate(res);
                } else {
                    testFolding(item.getFile());
                }
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
            }
        }

        return total;
    }

    private String getLap() {
        return " (lap " + lap + ") "; // NOI18N
    }

    private String padR(String s, int len) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= len) {
            return s;
        } else {
            StringBuilder sb = new StringBuilder(s);
            sb.setLength(len);
            for (int i = s.length(); i < len; i++) {
                sb.setCharAt(i, ' ');
            }
            return sb.toString();
        }
    }

//    private void sleep(int timeout, String message) {
//	System.err.printf("Sleeping: %s\n", message);
//	sleep(timeout);
//	System.err.printf("Awoke (%s)\n", message);
//    }
    private void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }
    private APTSystemStorage sysAPTData = APTSystemStorage.getDefault();

    private APTIncludeHandler getIncludeHandler(File file) {
        List<String> sysIncludes = sysAPTData.getIncludes("TraceModelSysIncludes", getSystemIncludes()); // NOI18N
        List<String> qInc = getQuoteIncludePaths();
        if (isPathsRelCurFile()) {
            qInc = new ArrayList<String>(getQuoteIncludePaths().size());
            for (Iterator it = getQuoteIncludePaths().iterator(); it.hasNext();) {
                String path = (String) it.next();
                if (!(new File(path).isAbsolute())) {
                    File dirFile = file.getParentFile();
                    File pathFile = new File(dirFile, path);
                    path = pathFile.getAbsolutePath();
                }
                qInc.add(path);
            }
        }
        StartEntry startEntry = new StartEntry(file.getAbsolutePath(), RepositoryUtils.UIDtoKey(getProject().getUID()));
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludes, qInc);
    }

    private APTMacroMap getMacroMap(File file) {
        //print("SystemIncludePaths: " + systemIncludePaths.toString() + "\n");
        //print("QuoteIncludePaths: " + quoteIncludePaths.toString() + "\n");
        APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMap(file), getMacros());
        return map;
    }

//    private APTPreprocHandler getPreprocHandler(File file) {
//	APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(file), getIncludeHandler(file), !file.getPath().endsWith(".h")); // NOI18N
//	return preprocHandler;
//    }
    private APTMacroMap getSysMap(File file) {
        APTMacroMap map = sysAPTData.getMacroMap("TraceModelSysMacros", getSysMacros()); // NOI18N
        return map;
    }

//    
//    @Override
//    protected List<String> getSystemIncludes() {
//	List<String> result = super.getSystemIncludes();
//	if( result.isEmpty() && ! dumpPPState ) {
//	    // NB: want any fake value but not for suite.sh which is run with dumpPPState
//	    result.add("/usr/non-exists"); // NOI18N
//	}
//	return result;
//    }
//
//    @Override
//    protected List<String> getSysMacros() {
//	List<String> result = super.getSysMacros();
//	if( result.isEmpty() && ! dumpPPState ) {
//	    // NB: want any fake value but not for suite.sh which is run with dumpPPState
//	    result.add("NO_DEFAULT_DEFINED_SYSTEM_MACROS"); // NOI18N
//	}
//	return result;
//    }
    private long testAPTLexer(File file, boolean printTokens) throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        print("Testing APT lexer:"); // NOI18N
        long time = System.currentTimeMillis();
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);
            reader = new InputStreamReader(stream, FileEncodingQuery.getDefaultEncoding());
            TokenStream ts = APTTokenStreamBuilder.buildTokenStream(file.getAbsolutePath(), reader);
            for (Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken()) {
                if (printTokens) {
                    print("" + t);
                }
            }
            time = System.currentTimeMillis() - time;
            if (isShowTime()) {
                print("APT Lexing " + file.getName() + " took " + time + " ms"); // NOI18N
            }
            return time;
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
    }

    private long testAPTWalkerVisit(APTFile apt, FileBuffer buffer) throws TokenStreamException, IOException {
        File file = buffer.getFile();
        boolean cleanAPT = apt == null;
        long time = System.currentTimeMillis();
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
            apt = APTDriver.getInstance().findAPTLight(buffer);
        }
        APTPreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(file), getIncludeHandler(file), true);
        APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
        walker.visit();
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Visiting APT " + (cleanAPT ? "with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
        }

        //        time = System.currentTimeMillis();
        //        if (cleanAPT) {
        //            invalidateAPT(file);
        //            time = System.currentTimeMillis();
        //            apt = APTDriver.getInstance().findAPT(file);
        //        }
        //        walker = new APTWalkerTest(apt, getMacroMap(file), getIncludeHandler(file));
        //        walker.nonRecurseVisit();
        //        time = System.currentTimeMillis() - time;
        //
        //        if( showTime ) {
        //            print("Non recursive visiting APT "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms");
        //        }
        return time;
    }

    private long testAPTWalkerGetStream(APTFile apt, FileBuffer buffer, boolean expand, boolean filter, boolean printTokens) throws TokenStreamException, IOException {
        File file = buffer.getFile();
        boolean cleanAPT = apt == null;
        long time = System.currentTimeMillis();
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
            apt = APTDriver.getInstance().findAPT(buffer);
        }
        APTMacroMap macroMap = getMacroMap(file);
        APTPreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(macroMap, getIncludeHandler(file), true);
        APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
        TokenStream ts = walker.getTokenStream();
        if (expand) {
            ts = new APTMacroExpandedStream(ts, macroMap);
        }
        if (filter) {
            ts = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP).getFilteredStream(new APTCommentsFilter(ts));
        }
        int lastLine = -1;
        for (Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken()) {
            if (printTokens) {
                print(" " + t.getText(), t.getLine() != lastLine); // NOI18N
            }
            lastLine = t.getLine();
        }
        if (printTokens && lastLine > 0) {
            print("", true);
        }
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Getting" + (expand ? " expanded" : "") + (filter ? " filtered" : "") + " APT token stream " + (cleanAPT ? "with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
        }
        return time;
    }

    private long testAPTParser(NativeFileItem item, boolean cleanAPT) throws IOException, RecognitionException, TokenStreamException {
        FileBuffer buffer = new FileBufferFile(item.getFile());
        print("Testing APT Parser"); // NOI18N
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        File file = buffer.getFile();
        long time = System.currentTimeMillis();
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
        }
        FileImpl fileImpl = null;
        fileImpl = (FileImpl) getProject().testAPTParseFile(item);
        getProject().waitParse();
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Parsing" + (cleanAPT ? " with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
        }
        return time;
    }

    private void testAPT(NativeFileItem item) throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        File file = item.getFile();
        FileBuffer buffer = new FileBufferFile(file);
        print("Testing APT: " + file.getName()); // NOI18N
        long minLexer = Long.MAX_VALUE;
        long maxLexer = Long.MIN_VALUE;
        long minAPTLexer = Long.MAX_VALUE;
        long maxAPTLexer = Long.MIN_VALUE;
        if (testAPTLexer) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTLexer(file, i == -1 ? printTokens : false);
                minAPTLexer = Math.min(minAPTLexer, val);
                maxAPTLexer = Math.max(maxAPTLexer, val);
            }
        }
        APTFile apt = null;
        minDriver = Long.MAX_VALUE;
        maxDriver = Long.MIN_VALUE;
        if (testAPTDriver) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                invalidateAPT(buffer);
                apt = testAPTDriver(buffer, i == -1 ? true : false);
            }
        }
        boolean cleanAPT = minDriver == Long.MAX_VALUE;

        long minVisit = Long.MAX_VALUE;
        long maxVisit = Long.MIN_VALUE;
        if (testAPTWalkerVisit) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerVisit(apt, buffer);
                minVisit = Math.min(minVisit, val);
                maxVisit = Math.max(maxVisit, val);
            }
        }
        long minGetTS = Long.MAX_VALUE;
        long maxGetTS = Long.MIN_VALUE;
        if (testAPTWalkerGetStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, false, false, i == -1 ? printTokens : false);
                minGetTS = Math.min(minGetTS, val);
                maxGetTS = Math.max(maxGetTS, val);
            }
        }
        long minGetExpandedTS = Long.MAX_VALUE;
        long maxGetExpandedTS = Long.MIN_VALUE;
        if (testAPTWalkerGetExpandedStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, false, i == -1 ? printTokens : false);
                minGetExpandedTS = Math.min(minGetExpandedTS, val);
                maxGetExpandedTS = Math.max(maxGetExpandedTS, val);
            }
        }
        long minGetFilteredTS = Long.MAX_VALUE;
        long maxGetFilteredTS = Long.MIN_VALUE;
        if (testAPTWalkerGetFilteredStream) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, true, i == -1 ? printTokens : false);
                minGetFilteredTS = Math.min(minGetFilteredTS, val);
                maxGetFilteredTS = Math.max(maxGetFilteredTS, val);
            }
        }
        long minParsing = Long.MAX_VALUE;
        long maxParsing = Long.MIN_VALUE;
        long minAPTParsing = Long.MAX_VALUE;
        long maxAPTParsing = Long.MIN_VALUE;
        if (testAPTParser) {
            for (int i = -1; i < APT_REPEAT_TEST; i++) {
                long val = testAPTParser(item, cleanAPT);
                minAPTParsing = Math.min(minAPTParsing, val);
                maxAPTParsing = Math.max(maxAPTParsing, val);
            }
        }
        if (isShowTime()) {
            print("APT BEST/WORST results for " + file.getAbsolutePath()); // NOI18N
            if (minLexer != Long.MAX_VALUE) {
                print(minLexer + " ms BEST Plain lexer"); // NOI18N
                print(maxLexer + " ms WORST Plain lexer"); // NOI18N
            }
            if (minAPTLexer != Long.MAX_VALUE) {
                print(minAPTLexer + " ms BEST APT lexer"); // NOI18N
                print(maxAPTLexer + " ms WORST APT lexer"); // NOI18N
            }
            if (minDriver != Long.MAX_VALUE) {
                print(minDriver + " ms BEST Building APT:"); // NOI18N
                print(maxDriver + " ms WORST Building APT:"); // NOI18N
            }

            if (minVisit != Long.MAX_VALUE) {
                print(minVisit + " ms BEST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxVisit + " ms WORST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetTS != Long.MAX_VALUE) {
                print(minGetTS + " ms BEST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetTS + " ms WORST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetExpandedTS != Long.MAX_VALUE) {
                print(minGetExpandedTS + " ms BEST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetExpandedTS + " ms WORST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetFilteredTS != Long.MAX_VALUE) {
                print(minGetFilteredTS + " ms BEST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetFilteredTS + " ms WORST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minParsing != Long.MAX_VALUE) {
                print(minParsing + " ms BEST Plaing Parsing"); // NOI18N
                print(maxParsing + " ms WORST Plaing Parsing"); // NOI18N
            }
            if (minAPTParsing != Long.MAX_VALUE) {
                print(minAPTParsing + " ms BEST APT parsing" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxAPTParsing + " ms WORST APT parsing" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
        }
    }
    private static String firstFile = null;

    private void invalidateAPT(final FileBuffer buffer) {
        File file = buffer.getFile();
        if (firstFile == null || firstFile.equalsIgnoreCase(file.getAbsolutePath())) {
            firstFile = file.getAbsolutePath();
            APTDriver.getInstance().invalidateAll();
            getProject().debugInvalidateFiles();
        } else {
            APTDriver.getInstance().invalidateAPT(buffer);
        }
    }
    long minDriver = Long.MAX_VALUE;
    long maxDriver = Long.MIN_VALUE;

    private APTFile testAPTDriver(final FileBuffer buffer, boolean buildXML) throws IOException, FileNotFoundException {
        File file = buffer.getFile();
        long oldMem = usedMemory();
        long time = System.currentTimeMillis();
        APTFile apt = APTDriver.getInstance().findAPT(buffer);
        time = System.currentTimeMillis() - time;
        long newMem = usedMemory();
        if (isShowTime()) {
            minDriver = Math.min(minDriver, time);
            maxDriver = Math.max(maxDriver, time);
            print("Building APT for " + file.getName() + "\n SIZE OF FILE:" + file.length() / 1024 + "Kb\n TIME: took " + time + " ms\n MEMORY: changed from " + (oldMem) / (1024) + " to " + newMem / (1024) + "[" + (newMem - oldMem) / 1024 + "]Kb"); // NOI18N
        }

        //        System.out.println("apt tree: \n" + APTTraceUtils.toStringList(apt));
        if (buildXML) {
            File outDir = new File("/tmp/aptout/"); // NOI18N
            outDir.mkdirs();
            File outFile = new File(outDir, file.getName() + ".xml"); // NOI18N
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            Writer out = new BufferedWriter(new FileWriter(outFile));
            APTTraceUtils.xmlSerialize(apt, out);
            out.flush();
            APT light = APTBuilder.buildAPTLight(apt);
            File outFileLW = new File(outDir, file.getName() + "_lw.xml"); // NOI18N
            if (outFileLW.exists()) {
                outFileLW.delete();
            }
            outFileLW.createNewFile();
            Writer outLW = new BufferedWriter(new FileWriter(outFileLW));
            APTTraceUtils.xmlSerialize(light, outLW);
            outLW.flush();
        }
        return apt;
    }

    private long usedMemory() {
        System.gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /*package*/
    void test(String[] args, PrintStream out, PrintStream err) throws Exception {
        tracer.setPrintStream(out);
        processArguments(args);
        doTest();
        initDataObjects();
    }

    private TestResult test(NativeFileItem item)
            throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {

        TestResult result = new TestResult();

        if (testAPT) {
            testAPT(item);
            if (breakAfterAPT) {
                return new TestResult();
            }
        }

        AST ast = null;

        if (dumpStatistics) {
            Diagnostic.initFileStatistics(item.getFile().getAbsolutePath());
        }

        long time = System.currentTimeMillis();

        if (readAst) {
            long t2 = System.currentTimeMillis();
            ast = cache.readAst(item.getFile());
            t2 = System.currentTimeMillis() - t2;
            print("AST read; time: " + t2 + " ms"); // NOI18N
        }

        AST tree = null;
        int errCount = 0;

        FileImpl fileImpl = (FileImpl) getProject().testAPTParseFile(item);
        waitProjectParsed(getProject(), false);
        if (dumpAst || writeAst || showAstWindow) {
            tree = fileImpl.debugParse();
        }
        errCount = fileImpl.getErrorCount();
        if (dumpPPState) {
            sleep(100); // so that we don't run ahead of fileParsingFinished event
            APTPreprocHandler preprocHandler = states.get(fileImpl);
            assert preprocHandler != null;
            dumpMacroMap(preprocHandler.getMacroMap());
        }
        time = System.currentTimeMillis() - time;
        if (isShowTime()) {
            result.setTime(time);
            result.setLineCount(countLines(fileImpl));
            if (!quiet) {
                print("Processing " + item.getFile().getName() + " took " + time + " ms; LPS=" + result.getLPS() + "; error count: " + errCount); // NOI18N
            }
        }

        if (dumpStatistics) {
            if (this.dumpDir != null) {
                String postfix = statPostfix;
                if (Diagnostic.getStatisticsLevel() > 1) {
                    postfix += "." + Diagnostic.getStatisticsLevel(); // NOI18N
                }
                String name = item.getFile().getName() + postfix;
                String theDumpFile = new File(this.dumpDir, name).getAbsolutePath();
                Diagnostic.dumpFileStatistics(theDumpFile);
            }
            if (this.dumpFile != null) {
                Diagnostic.dumpFileStatistics(this.dumpFile, true);
            }
        }

        if (testCache) {
            cacheTimes.put(item.getFile().getName(), Long.valueOf(time));
        }
        if (dumpAst) {
            System.out.println("AST DUMP for file " + item.getFile().getName()); // NOI18N
            dumpAst(tree);
        }

        if (writeAst && tree != null) {
            long t2 = System.currentTimeMillis();
            t2 = System.currentTimeMillis() - t2;
            cache.writeAst(tree, item.getFile());
            print("AST stored; time: " + t2 + " ms"); // NOI18N
        }

        if (doCleanRepository) {
            CsmProject prj = fileImpl.getProject();
            String absPath = fileImpl.getAbsolutePath();
            fileImpl = null;
            ParserThreadManager.instance().waitEmptyProjectQueue((ProjectBase) prj);
            sleep(100);
            RepositoryAccessor.getRepository().debugClear();
            fileImpl = (FileImpl) prj.findFile(absPath);
        }

        if (dumpModel) {
            if (fileImpl != null) {
                tracer.setDeep(deep);
                tracer.setDumpTemplateParameters(dumpTemplateParameters);
                tracer.setTestUniqueName(testUniqueName);
                tracer.dumpModel(fileImpl);
                if (!dumpFileOnly) {
                    tracer.dumpModel(getProject());
                }
            } else {
                print("FileImpl is null - not possible to dump File Model"); // NOI18N
            }
        }

        if (showAstWindow) {
            test(tree, item.getFile().getName());
        }

        return result;
    }

    private boolean hasNonEmptyIncludes(CsmFile fileImpl) {
        for (Iterator<CsmInclude> it = fileImpl.getIncludes().iterator(); it.hasNext();) {
            CsmInclude inc = it.next();
            if (inc.getIncludeFile() != null) {
                return true;
            }
        }
        return false;
    }

    private long countLines(CsmFile fileImpl) {
        return countLines(fileImpl, false);
    }

    private long countLines(CsmFile fileImpl, boolean allowResolvedIncludes) {
        if (fileImpl == null) {
            return -1;
        }
        if (!allowResolvedIncludes && hasNonEmptyIncludes(fileImpl)) {
            //! fileImpl.getIncludes().isEmpty() ) {
            return -1;
        }
        CharSequence text = fileImpl.getText();
        long cnt = 0;
        for (int pos = 0; pos < text.length(); pos++) {
            if (text.charAt(pos) == '\n') {
                cnt++;
            }
        }
        return cnt;
    }

    private void test(AST tree, String label) {

        //	    System.out.println("LIST:");
        //	    System.out.println(tree.toStringList());
        //
        //	    System.out.println("DUMP:");
        //	    DumpASTVisitor visitor = new DumpASTVisitor();
        //	    visitor.visit(tree);
        ASTFrameEx frame = new ASTFrameEx(label, tree);
        frame.setVisible(true);
    }

//    private boolean isDummyUnresolved(CsmDeclaration decl) {
//	return decl == null || decl instanceof Unresolved.UnresolvedClass;
//    }
    public static void dumpAst(AST ast) {
        ASTVisitor visitor = new ASTVisitor() {

            public void visit(AST node) {
                for (AST node2 = node; node2 != null; node2 = node2.getNextSibling()) {
                    String ofStr = (node2 instanceof CsmAST) ? (" offset=" + ((CsmAST) node2).getOffset() + " file = " + ((CsmAST) node2).getFilename()) : ""; // NOI18N
                    print("" + node2.getText() + " [" + node2.getType() + "] " + node2.getLine() + ':' + node2.getColumn() + ofStr); // NOI18N
                    if (node2.getFirstChild() != null) {
                        indent();
                        visit(node2.getFirstChild());
                        unindent();
                    }
                }
            }
        };
        visitor.visit(ast);
    }

    private void dumpMacroMap(APTMacroMap macroMap) {
        tracer.print("State of macro map:"); // NOI18N
        tracer.print(macroMap == null ? "empty macro map" : macroMap.toString()); // NOI18N
    }

    private void testLibProject() {
        LibProjectImpl libProject = LibProjectImpl.createInstance(getModel(), "/usr/include"); // NOI18N
        getModel().addProject(libProject);
        tracer.dumpModel(libProject);
    }

    private static void print(String s) {
        tracer.print(s);
    }

    private void print(String s, boolean newLine) {
        tracer.print(s, newLine);
    }

    private static void indent() {
        tracer.indent();
    }

    private static void unindent() {
        tracer.unindent();
    }

    private int countUserFiles() {
        return getProject().getAllFiles().size();
    }

    private int countSystemHeaders() {
        int cnt = 0;
        Set processedProjects = new HashSet();
        for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
            cnt += countFiles((ProjectBase) it.next(), processedProjects);
        }
        return cnt;
    }

    private int countFiles(ProjectBase prj, Collection processedProjects) {
        if (processedProjects.contains(prj)) {
            return 0; // already counted
        }
        int cnt = prj.getAllFiles().size();
        for (Iterator it = prj.getLibraries().iterator(); it.hasNext();) {
            cnt += countFiles((ProjectBase) it.next(), processedProjects);
        }
        return cnt;
    }

    private void calculateAverageLPS(TestResult total, boolean includeLibs) {
        total.lineCount = 0;
        for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
            CsmFile file = (CsmFile) it.next();
            total.lineCount += countLines(file, true);
        }
        if (includeLibs) {
            for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
                ProjectBase lib = (ProjectBase) it1.next();
                for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
                    CsmFile file = (CsmFile) it2.next();
                    total.lineCount += countLines(file, true);
                }
            }
        }
    }

    private void testFolding(File file) {
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
        if (is == null) {
            return;
        }
        Reader reader = new InputStreamReader(is);
        reader = new BufferedReader(reader);
        FoldingParser p = Lookup.getDefault().lookup(FoldingParser.class);
        if (p != null) {
            List<CppFoldRecord> folds = p.parse(file.getAbsolutePath(), reader);
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            printFolds(file.getAbsolutePath(), folds);
        } else {
            System.out.println("No Folding Provider"); // NOI18N
        }
    }

    private void printFolds(String file, List<CppFoldRecord> folds) {
        Collections.sort(folds, FOLD_COMPARATOR);
        System.out.println("Foldings of the file " + file); // NOI18N
        for (Iterator it = folds.iterator(); it.hasNext();) {
            CppFoldRecord fold = (CppFoldRecord) it.next();
            System.out.println(fold);
        }
    }
    private static Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {

        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
            int start1 = o1.getStartLine();
            int start2 = o2.getStartLine();
            if (start1 == start2) {
                return o1.getStartOffset() - o2.getStartOffset();
            } else {
                return start1 - start2;
            }
        }
    };

    boolean isShowTime() {
        return showTime;
    }

    private List<NativeFileItem> getFileItems() {
        List<NativeFileItem> result = new ArrayList<NativeFileItem>();

        Object platformProject = getProject().getPlatformProject();
        if (platformProject instanceof NativeProject) {
            NativeProject nativeProject = (NativeProject) platformProject;
            if (nativeProject != null) {
                result.addAll(nativeProject.getAllFiles());
// these are all files specified in command line; it does not make sense to filter them
//                for(NativeFileItem item : nativeProject.getAllFiles()){
//                    if (!item.isExcluded()) {
//                        switch(item.getLanguage()){
//                            case C:
//                            case CPP:
//                            case C_HEADER:
//                                result.add(item);
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
            }
        }
        return result;
    }
}

