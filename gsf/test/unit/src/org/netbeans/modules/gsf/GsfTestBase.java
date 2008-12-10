/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.gsf;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.gsf.api.Rule.AstRule;
import org.netbeans.modules.gsf.api.Rule.ErrorRule;
import org.netbeans.modules.gsf.api.Rule.SelectionRule;
import org.netbeans.modules.gsf.api.Rule.UserConfigurableRule;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.gsfret.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.gsfret.hints.infrastructure.HintsSettings;
import org.netbeans.napi.gsfret.source.Source;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.ElementChange;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.spi.DefaultError;
import org.netbeans.modules.gsfret.hints.infrastructure.Pair;

/**
 * @todo Add stress tests: Test every single position in an occurrences marker,
 *   test every single position for a declaration finder, etc.
 * @author Tor Norbye
 */
public abstract class GsfTestBase extends NbTestCase {

    public GsfTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        TestSourceModelFactory.currentTest = this;
        
        super.setUp();
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    protected void initializeRegistry() {
        DefaultLanguageConfig defaultLanguage = getPreferredLanguage();
        if (defaultLanguage == null) {
            fail("If you don't implement getPreferredLanguage(), you must override initializeRegistry!");
            return;
        }
        LanguageRegistry registry = LanguageRegistry.getInstance();
        if (!LanguageRegistry.getInstance().isSupported(getPreferredMimeType())) {
            List<Action> actions = Collections.emptyList();
            org.netbeans.modules.gsf.Language dl = new org.netbeans.modules.gsf.Language("unknown", getPreferredMimeType(), actions, 
                    defaultLanguage, getParser(), getCodeCompleter(),
                    getRenameHandler(), defaultLanguage.getDeclarationFinder(),
                    defaultLanguage.getFormatter(), getKeystrokeHandler(), 
                    getIndexer(), getStructureScanner(), null, 
                    defaultLanguage.isUsingCustomEditorKit());
            List<org.netbeans.modules.gsf.Language> languages = new ArrayList<org.netbeans.modules.gsf.Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
    }

    public static File getXTestJsCluster() {
        String destDir = System.getProperty("xtest.js.home");
        if (destDir == null) {
            throw new RuntimeException("xtest.js.home property has to be set when running within binary distribution");
        }
        return new File(destDir);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected FileObject touch(final String dir, final String path) throws IOException {
        return touch(new File(dir), path);
    }

    protected FileObject touch(final File dir, final String path) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return touch(dirFO, path);
    }
    
    protected FileObject touch(final FileObject dir, final String path) throws IOException {
        return FileUtil.createData(dir, path);
    }

    public static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }

    /** Return the offset of the given position, indicated by ^ in the line fragment
     * from the fuller text
     */
    public static int getCaretOffset(String text, String caretLine) {
        int caretDelta = caretLine.indexOf('^');
        assertTrue("No ^ marker for the caret in the text", caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue("No occurrence of caretLine " + caretLine + " in text " + text, lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        return caretOffset;
    }

    
    /** Copy-pasted from APISupport. */
    protected static String slurp(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }
    
    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    protected String readFile(final FileObject fo) {
        return read(fo);
    }
    
    public static String read(final FileObject fo) {
        try {
            final StringBuilder sb = new StringBuilder(5000);
            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {

                    if (fo == null) {
                        return;
                    }

                    InputStream is = fo.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        sb.append(line);
                        sb.append('\n');
                    }
                }
            });

            if (sb.length() > 0) {
                return sb.toString();
            } else {
                return null;
            }
        }
        catch (IOException ioe){
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
    }

    public BaseDocument getDocument(String s, final String mimeType, final Language language) {
        try {
            BaseDocument doc = new BaseDocument(true, mimeType) {
                @Override
                public boolean isIdentifierPart(char ch) {
                    if (mimeType != null) {
                        org.netbeans.modules.gsf.Language l = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                        if (l != null) {
                            GsfLanguage gsfLanguage = l.getGsfLanguage();
                            if (gsfLanguage != null) {
                                return gsfLanguage.isIdentifierChar(ch);
                            }
                        }
                    }

                    return super.isIdentifierPart(ch);
                }
            };

            //doc.putProperty("mimeType", mimeType);
            doc.putProperty(org.netbeans.api.lexer.Language.class, language);

            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }
    
    public BaseDocument getDocument(String s, String mimeType) {
        Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
        assertNotNull(language);
        
        return getDocument(s, mimeType, language);
    }
    
    public static BaseDocument createDocument(String s) {
        try {
            BaseDocument doc = new BaseDocument(null, false);
            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    protected BaseDocument getDocument(String s) {
        String mimeType = getPreferredMimeType();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredMimeType()", mimeType);

        GsfLanguage language = getPreferredLanguage();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredLanguage()", language);
        
        return getDocument(s, mimeType, language.getLexerLanguage());
    }
    
    protected BaseDocument getDocument(FileObject fo) {
        try {
//             DataObject dobj = DataObject.find(fo);
//             assertNotNull(dobj);
//
//             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//             assertNotNull(ec);
//
//             return (BaseDocument)ec.openDocument();
            BaseDocument doc = getDocument(readFile(fo));
            try {
                DataObject dobj = DataObject.find(fo);
                doc.putProperty(Document.StreamDescriptionProperty, dobj);
            } catch (DataObjectNotFoundException dnfe) {
                fail(dnfe.toString());
            }

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public static String readFile(File f) throws Exception {
        FileReader r = new FileReader(f);
        int fileLen = (int)f.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }

    protected File getDataSourceDir() {
        // Check whether token dump file exists
        // Try to remove "/build/" from the dump file name if it exists.
        // Otherwise give a warning.
        File inputFile = getDataDir();
        String inputFilePath = inputFile.getAbsolutePath();
        boolean replaced = false;
        if (inputFilePath.indexOf(pathJoin("build", "test")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("build", "test"), pathJoin("test"));
            replaced = true;
        }
        if (!replaced && inputFilePath.indexOf(pathJoin("test", "work", "sys")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("test", "work", "sys"), pathJoin("test", "unit"));
            replaced = true;
        }
        if (!replaced) {
            System.err.println("Warning: Attempt to use dump file " +
                    "from sources instead of the generated test files failed.\n" +
                    "Patterns '/build/test/' or '/test/work/sys/' not found in " + inputFilePath
            );
        }
        inputFile = new File(inputFilePath);
        assertTrue(inputFile.exists());
        
        return inputFile;
    }
    
    private static String pathJoin(String... chunks) {
        StringBuilder result = new StringBuilder(File.separator);
        for (String chunk : chunks) {
            result.append(chunk).append(File.separatorChar);            
        }
        return result.toString();
    }
    
    protected File getDataFile(String relFilePath) {
        File inputFile = new File(getDataSourceDir(), relFilePath);
        return inputFile;
    }
    
    protected boolean failOnMissingGoldenFile() {
        return true;
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext) throws Exception {
        assertDescriptionMatches(relFilePath, description, includeTestName, ext, true);
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext, boolean checkFileExistence) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (checkFileExistence && !rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            if (failOnMissingGoldenFile()) {
                NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
            }
            return;
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        assertEquals(expected.trim(), description.trim());
    }

    protected void assertDescriptionMatches(FileObject fileObject, 
            String description, boolean includeTestName, String ext) throws Exception {
        File goldenFile = getDataFile("testfiles/" + fileObject.getName() + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        assertEquals("Not matching goldenfile: " + FileUtil.getFileDisplayName(fileObject), expected.trim(), description.trim());
    }
    
    protected void assertFileContentsMatches(String relFilePath, String description, boolean includeTestName, String ext) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);
        assertEquals(expected.trim(), description.trim());
    }

    public void assertEquals(Collection<String> s1, Collection<String> s2) {
        List<String> l1 = new ArrayList<String>();
        l1.addAll(s1);
        Collections.sort(l1);
        List<String> l2 = new ArrayList<String>();
        l2.addAll(s2);
        Collections.sort(l2);
        
        assertEquals(l1.toString(), l2.toString());
    }
    
    protected void createFilesFromDesc(FileObject folder, String descFile) throws Exception {
        File taskFile = new File(getDataDir(), descFile);
        assertTrue(taskFile.exists());
        BufferedReader br = new BufferedReader(new FileReader(taskFile));
        while (true) {
            String line = br.readLine();
            if (line == null || line.trim().length() == 0) {
                break;
            }
            
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length()-1);
            }

            String path = line;
            if (path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
                FileObject f = FileUtil.createFolder(folder, path);
                assertNotNull(f);
            } else {
                FileObject f = FileUtil.createData(folder, path);
                assertNotNull(f);
            }
        }
    }

   public static void createFiles(File baseDir, String... paths) throws IOException {
        assertNotNull(baseDir);
        for (String path : paths) {
            FileObject baseDirFO = FileUtil.toFileObject(baseDir);
            assertNotNull(baseDirFO);
            assertNotNull(FileUtil.createData(baseDirFO, path));
        }
    }

    public static void createFile(FileObject dir, String relative, String contents) throws IOException {
        FileObject datafile = FileUtil.createData(dir, relative);
        OutputStream os = datafile.getOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(contents);
        writer.close();
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Parsing Info Based Tests
    ////////////////////////////////////////////////////////////////////////////
    protected Parser getParser() {
        Parser parser = getPreferredLanguage().getParser();
        assertNotNull("You must override getParser(), either from your GsfLanguage or your test class", parser);
        return parser;
    }
    
    protected void validateParserResult(@CheckForNull ParserResult result) {
        // Clients can do checks to make sure everything is okay here. 
    }

    protected List<URL> getExtraCpUrls() {
        return null;
    }
    
    private GsfTestCompilationInfo getInfo(FileObject fo, BaseDocument doc, String source) throws Exception {
        return new GsfTestCompilationInfo(this, fo, doc, source);
    }
    
    protected DefaultLanguageConfig getPreferredLanguage() {
        return null;
    }
    
    protected String getPreferredMimeType() {
        return null;
    }
    
    public GsfTestCompilationInfo getInfo(String file) throws Exception {
        FileObject fileObject = getTestFile(file);

        return getInfo(fileObject);
    }
    
    public GsfTestCompilationInfo getInfo(FileObject fileObject) throws Exception {
        String text = readFile(fileObject);
        if (text == null) {
            text = "";
        }

        String mimeType = getPreferredMimeType();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredMimeType()", mimeType);

        GsfLanguage language = getPreferredLanguage();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredLanguage()", language);
        
        BaseDocument doc = getDocument(text, mimeType, language.getLexerLanguage());

        return getInfo(fileObject, doc, text);
    }
    
    public GsfTestCompilationInfo getInfoForText(String text, String tempName) throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject testFO = workDir.getFileObject(tempName);
        if (testFO != null) {
            testFO.delete();
        }
        testFO = workDir.createData(tempName);
        FileObject fileObject = copyStringToFileObject(testFO, text);
        return getInfo(fileObject);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Parser tests
    ////////////////////////////////////////////////////////////////////////////
    protected void checkErrors(String relFilePath) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        String text = info.getText();
        assertNotNull(text);

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Error> diagnostics = pr.getDiagnostics();
        String annotatedSource = annotateErrors(text, diagnostics);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".errors");
    }

    protected String annotateErrors(String text, List<Error> errors) {
        List<String> descs = new ArrayList<String>();
        for (Error error : errors) {
            StringBuilder desc = new StringBuilder();
            if (error.getKey() != null) {
                desc.append("[");
                desc.append(error.getKey());
                desc.append("] ");
            }
            desc.append(error.getStartPosition());
            desc.append("-");
            desc.append(error.getEndPosition());
            desc.append(":");
            desc.append(error.getDisplayName());
            if (error.getDescription() != null) {
                desc.append(" ; " );
                desc.append(error.getDescription());
            }
            descs.add(desc.toString());
        }
        Collections.sort(descs);
        StringBuilder summary = new StringBuilder();
        for (String desc : descs) {
            summary.append(desc);
            summary.append("\n");
        }

        return summary.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Keystroke completion tests
    ////////////////////////////////////////////////////////////////////////////
    protected KeystrokeHandler getKeystrokeHandler() {
        KeystrokeHandler handler = getPreferredLanguage().getKeystrokeHandler();
        assertNotNull("You must override getKeystrokeHandler, either from your GsfLanguage or your test class", handler);
        return handler;
    }

    // Also requires getFormatter(IndentPref) defined below under the formatting tests
    
    protected void assertMatches(String original) throws BadLocationException {
        KeystrokeHandler bc = getKeystrokeHandler();
        int caretPos = original.indexOf('^');
        
        original = original.substring(0, caretPos) + original.substring(caretPos+1);
        int matchingCaretPos = original.indexOf('^');
        assertTrue(caretPos < matchingCaretPos);
        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        OffsetRange range = bc.findMatching(doc, caretPos);
        
        assertNotSame("Didn't find matching token for " + /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos, 
                OffsetRange.NONE, range);
        assertEquals("forward match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                /*LexUtilities.getToken(doc, matchingCaretPos).text().toString()*/ " position " + matchingCaretPos, 
                matchingCaretPos, range.getStart());
        
        // Perform reverse match
        range = bc.findMatching(doc, matchingCaretPos);
        
        assertNotSame(OffsetRange.NONE, range);
        assertEquals("reverse match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " + 
                /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos, 
                caretPos, range.getStart());
    }
    
    // Copied from LexUtilities
    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            int start = Utilities.getRowStart(doc, offset);
            int end;

            if (Utilities.isRowWhite(doc, start)) {
                end = Utilities.getRowEnd(doc, offset);
            } else {
                end = Utilities.getRowFirstNonWhite(doc, start);
            }

            int indent = Utilities.getVisualColumn(doc, end);

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);
        }

        if (formatter != null) {
            configureIndenters(doc, formatter, null, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, DefaultEditorKit.defaultKeyTypedAction, ""+insertText);

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void deleteChar(String original, String expected) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();

        if (formatter != null) {
            configureIndenters(doc, formatter, null, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, DefaultEditorKit.deletePrevCharAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }
    
    protected void deleteWord(String original, String expected) throws Exception {
        String source = original;
        String reformatted = expected;
        Formatter formatter = getFormatter(null);

        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, null, true);
        }

        setupDocumentIndentation(doc, null);

        runKitAction(ta, BaseKit.removePreviousWordAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }

    }
    
    protected FileObject getTestFileObject() {
        return null;
    }

    protected void assertLogicalRange(String source, boolean up, String expected) throws Exception {
        KeystrokeHandler completer = getKeystrokeHandler();
        assertNotNull("getKeystrokeHandler() must be implemented!", completer);

        String BEGIN = "%<%"; // NOI18N
        String END = "%>%"; // NOI18N
        int sourceStartPos = source.indexOf(BEGIN);
        if (sourceStartPos != -1) {
            source = source.substring(0, sourceStartPos) + source.substring(sourceStartPos+BEGIN.length());
        }
        
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);

        int sourceEndPos = source.indexOf(END);
        if (sourceEndPos != -1) {
            source = source.substring(0, sourceEndPos) + source.substring(sourceEndPos+END.length());
        }
        
        int expectedStartPos = expected.indexOf(BEGIN);
        if (expectedStartPos != -1) {
            expected = expected.substring(0, expectedStartPos) + expected.substring(expectedStartPos+BEGIN.length());
        }

        int expectedCaretPos = expected.indexOf('^');
        expected = expected.substring(0, expectedCaretPos) + expected.substring(expectedCaretPos+1);
        
        int expectedEndPos = expected.indexOf(END);
        if (expectedEndPos != -1) {
            expected = expected.substring(0, expectedEndPos) + expected.substring(expectedEndPos+END.length());
        }

        assertEquals("Only range markers should differ", source,expected);

        OffsetRange selected = null;
        
        BaseDocument doc = getDocument(source);
        FileObject fileObject = getTestFileObject();
        CompilationInfo info = getInfo(fileObject, doc, source);
        assertNotNull("To run this test you must have implemented getInfo(FileObject,DocumentString)!", info);
        
        List<OffsetRange> ranges = completer.findLogicalRanges(info, caretPos);
        OffsetRange expectedRange;
        if (expectedStartPos != -1) {
            expectedRange = new OffsetRange(expectedStartPos, expectedEndPos);
        } else {
            expectedRange = new OffsetRange(expectedCaretPos, expectedCaretPos);
        }

        if (sourceStartPos != -1) {
            assertTrue(sourceEndPos != -1);
            selected = new OffsetRange(sourceStartPos, sourceEndPos);            

            for (int i = 0; i < ranges.size(); i++) {
                if (ranges.get(i).equals(selected)) {
                    if (up) {
                        assertTrue(i < ranges.size()-1);
                        OffsetRange was = ranges.get(i+1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    } else {
                        if (i == 0) {
                            assertEquals(caretPos, expectedCaretPos);
                            return;
                        }
                        OffsetRange was = ranges.get(i-1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    }
                }
            }
            fail("Selection range " + selected + " is not in the range; ranges=" + ranges);
        } else {
            assertTrue(ranges.size() > 0);
            OffsetRange was = ranges.get(0);
            assertEquals("Wrong selection: expected \"" + 
                    expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                    source.substring(was.getStart(), was.getEnd()) + "\"",
                    expectedRange, was);
            return;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Mark Occurrences Tests
    ////////////////////////////////////////////////////////////////////////////
    protected OccurrencesFinder getOccurrencesFinder() {
        OccurrencesFinder handler = getPreferredLanguage().getOccurrencesFinder();
        assertNotNull("You must override getOccurrencesFinder, either from your GsfLanguage or your test class", handler);
        return handler;
    }
    
    /** Test the occurrences to make sure they equal the golden file.
     * If the symmetric parameter is set, this test will also ensure that asking for
     * occurrences on ANY of the matches produced by the original caret position will
     * produce the exact same map. This is obviously not appropriate for things like
     * occurrences on the exit points.
     */
    protected void checkOccurrences(String relFilePath, String caretLine, boolean symmetric) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        OccurrencesFinder finder = getOccurrencesFinder();
        assertNotNull("getOccurrencesFinder must be implemented", finder);
        finder.setCaretPosition(caretOffset);
        finder.run(info);
        Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();
        if (occurrences == null) {
            occurrences = Collections.emptyMap();
        }

        String annotatedSource = annotateFinderResult((BaseDocument)info.getDocument(), occurrences, caretOffset);

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".occurrences");
        
        if (symmetric) {
            // Extra check: Ensure that occurrences are symmetric: Placing the caret on ANY of the occurrences
            // should produce the same set!!
            for (OffsetRange range : occurrences.keySet()) {
                int midPoint = range.getStart() + range.getLength() / 2;
                finder.setCaretPosition(midPoint);
                finder.run(info);
                Map<OffsetRange, ColoringAttributes> alternates = finder.getOccurrences();
                assertEquals("Marks differ between caret positions - failed at " + midPoint, occurrences, alternates);
            }
        }
    }

    private String annotateFinderResult(BaseDocument doc, Map<OffsetRange, ColoringAttributes> highlights, int caretOffset) throws Exception {
        Set<OffsetRange> ranges = highlights.keySet();
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        sb.append("|>");
                        OffsetRange range = starts.get(i);
                        ColoringAttributes ca = highlights.get(range);
                        if (ca != null) {
                            sb.append(ca.name());
                            sb.append(':');
                        }
                    }
                    if (ends.containsKey(i)) {
                        sb.append("<|");
                    }
                    sb.append(text.charAt(i));
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Semantic Highlighting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected SemanticAnalyzer getSemanticAnalyzer() {
        SemanticAnalyzer handler = getPreferredLanguage().getSemanticAnalyzer();
        assertNotNull("You must override getSemanticAnalyzer, either from your GsfLanguage or your test class", handler);
        return handler;
    }
    
    protected void checkSemantic(String relFilePath, String caretLine) throws Exception {
        SemanticAnalyzer analyzer = getSemanticAnalyzer();
        assertNotNull("getSemanticAnalyzer must be implemented", analyzer);
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);
            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        analyzer.run(info);
        Map<OffsetRange, Set<ColoringAttributes>> highlights = analyzer.getHighlights();

        if (highlights == null) {
            highlights = Collections.emptyMap();
        }
        checkNoOverlaps(highlights.keySet(), info.getDocument());
        
        String annotatedSource = annotateSemanticResults(info.getDocument(), highlights);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".semantic");
    }
    
    private void checkNoOverlaps(Set<OffsetRange> ranges, Document doc) throws BadLocationException {
        // Make sure there are no overlapping ranges (but containment is allowed)
        List<OffsetRange> sortedRanges = new ArrayList<OffsetRange>(ranges);
        Collections.sort(sortedRanges);
        OffsetRange prevRange = OffsetRange.NONE;
        for (OffsetRange range : sortedRanges) {
            if (range.getStart() < prevRange.getEnd() && range.getEnd() > prevRange.getEnd()) {
                fail("OffsetRanges should be non-overlapping! " + prevRange + 
                        "(" + doc.getText(prevRange.getStart(), prevRange.getLength()) + ") and " + range + 
                        "(" + doc.getText(range.getStart(), range.getLength()) + ")");
            }
            prevRange = range;
        }
    }

    private String annotateSemanticResults(Document doc, Map<OffsetRange, Set<ColoringAttributes>> highlights) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : highlights.keySet()) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
                OffsetRange range = starts.get(i);
                Set<ColoringAttributes> cas = highlights.get(range);
                if (cas != null) {
                    // Sort to ensure stable unit test golden files
                    List<String> attrs = new ArrayList<String>(cas.size());
                    for (ColoringAttributes c : cas) {
                        attrs.add(c.name());
                    }
                    Collections.sort(attrs);
                    boolean first = true;
                    for (String name : attrs) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(",");
                        }
                        sb.append(name);
                    }
                    sb.append(':');
                }
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    protected void checkSemantic(String relFilePath) throws Exception {
        checkSemantic(relFilePath, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Rename Handling Tests
    ////////////////////////////////////////////////////////////////////////////
    protected InstantRenamer getRenameHandler() {
        InstantRenamer handler = getPreferredLanguage().getInstantRenamer();
        assertNotNull("You must override getRenameHandler, either from your GsfLanguage's getInstantRenamer or your test class", handler);
        return handler;
    }

    protected void checkRenameSections(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        InstantRenamer handler = getRenameHandler();
        assertNotNull("getRenameHandler must be implemented", handler);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(info.getText(), caretLine);
        }

        String annotatedSource;
        String[] desc = new String[1];
        if (handler.isRenameAllowed(info, caretOffset, desc)) {
            Set<OffsetRange> renameRegions = handler.getRenameRegions(info, caretOffset);
            annotatedSource = annotateRenameRegions(info.getDocument(), renameRegions);
        } else {
            annotatedSource = "Refactoring not allowed here\n";
            if (desc[0] != null) {
                annotatedSource += desc[0] + "\n";
            }
        }

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".rename");
    }

    private String annotateRenameRegions(Document doc, Set<OffsetRange> ranges) throws Exception {
        if (ranges.size() == 0) {
            return "Requires Interactive Refactoring\n";
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }
        // Only print lines with result
        String[] lines = sb.toString().split("\n");
        sb = new StringBuilder();
        int lineno = 1;
        for (String line : lines) {
            if (line.indexOf("|>") != -1) {
                sb.append(Integer.toString(lineno));
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
            }
            lineno++;
        }
        
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Indexing Tests
    ////////////////////////////////////////////////////////////////////////////
    public Indexer getIndexer() {
        Indexer handler = getPreferredLanguage().getIndexer();
        assertNotNull("You must override getIndexer, either from your GsfLanguage or your test class", handler);
        return handler;
    }
    
    protected List<IndexDocument> indexFile(String relFilePath) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        ParserResult rpr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(rpr);

        Indexer indexer = getIndexer();
        assertNotNull("getIndexer must be implemented", indexer);
        IndexDocumentFactory factory = new IndexDocumentFactoryImpl(/*info.getIndex(info.getPreferredMimeType())*/);
        List<IndexDocument> result = indexer.index(rpr, factory);
        if (result == null) {
            // GSF also makes this conversion
            result = Collections.emptyList();
        }
        
        return result;
    }
    
    protected void checkIndexer(String relFilePath) throws Exception {
        File jsFile = new File(getDataDir(), relFilePath);
        String fileUrl = jsFile.toURI().toURL().toExternalForm();
        String localUrl = fileUrl;
        int index = localUrl.lastIndexOf('/');
        if (index != -1) {
            localUrl = localUrl.substring(0, index);
        }
        
        List<IndexDocument> result = indexFile(relFilePath);
        String annotatedSource = prettyPrint(result, localUrl);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    
    protected void checkIsIndexable(String relFilePath, boolean isIndexable) throws Exception {
        Indexer indexer = getIndexer();
        assertNotNull("getIndexer must be implemented", indexer);
        FileObject fo = getTestFile(relFilePath);
        assertNotNull(fo);
        ParserFile file = new DefaultParserFile(fo, null, false);
        
        assertEquals(isIndexable, indexer.isIndexable(file));
    }
    
    private String sortCommaList(String s) {
        String[] items = s.split(",");
        Arrays.sort(items);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(item);
        }

        return sb.toString();
    }
    
    protected String prettyPrintValue(String key, String value) {
        return value;
    }
    
    private String prettyPrint(List<IndexDocument> documents, String localUrl) throws IOException {
        List<String> nonEmptyDocuments = new ArrayList<String>();
        List<String> emptyDocuments = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();

        for (IndexDocument d : documents) {
            IndexDocumentImpl doc = (IndexDocumentImpl)d;
        
            sb = new StringBuilder();
            
            if (doc.overrideUrl != null) {
                sb.append("Override URL: ");
                sb.append(doc.overrideUrl);
                sb.append("\n");
            }
                            
            sb.append("Searchable Keys:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();

            List<String> keys = doc.indexedKeys;
            List<String> values = doc.indexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = values.get(i);
                strings.add(key + " : " + prettyPrintValue(key, value));
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Not Searchable Keys:");
            sb.append("\n");
            strings = new ArrayList<String>();
            keys = doc.unindexedKeys;
            values = doc.unindexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = prettyPrintValue(key, values.get(i));
                if (value.indexOf(',') != -1) {
                    value = sortCommaList(value);
                }
                strings.add(key + " : " + value);
            }

            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            String s = sb.toString();
            if (doc.indexedKeys.size() == 0 && doc.unindexedKeys.size() == 0) {
                emptyDocuments.add(s);
            } else {
                nonEmptyDocuments.add(s);
            }
        }

        Collections.sort(emptyDocuments);
        Collections.sort(nonEmptyDocuments);
        sb = new StringBuilder();
        int documentNumber = 0;
        for (String s : emptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }

        for (String s : nonEmptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }


        return sb.toString().replace(localUrl, "<TESTURL>");
    }
        
        
    public class IndexDocumentImpl implements IndexDocument {
        public List<String> indexedKeys = new ArrayList<String>();
        public List<String> indexedValues = new ArrayList<String>();
        public List<String> unindexedKeys = new ArrayList<String>();
        public List<String> unindexedValues = new ArrayList<String>();

        public String overrideUrl;

        private IndexDocumentImpl(String overrideUrl) {
            this.overrideUrl = overrideUrl;
        }

        public void addPair(String key, String value, boolean indexed) {
            if (indexed) {
                indexedKeys.add(key);
                indexedValues.add(value);
            } else {
                unindexedKeys.add(key);
                unindexedValues.add(value);
            }
        }
    }

    private class IndexDocumentFactoryImpl implements IndexDocumentFactory {
        private IndexDocumentFactoryImpl() {
        }

        public IndexDocument createDocument(int initialPairs) {
            return new IndexDocumentImpl(null);
        }

        public IndexDocument createDocument(int initialPairs, String overrideUrl) {
            return new IndexDocumentImpl(overrideUrl);
        }
    }

    protected List<SearchResult> createTestMaps(FileObject testFile) {
        List<SearchResult> maps = new ArrayList<SearchResult>();

        String dump = readFile(testFile);
        String[] lines = dump.split("\n");

        for (int lineno = 0; lineno < lines.length; lineno++) {
            // Skip empty lines
            String line = lines[lineno].trim();
            if (line.length() == 0) {
                continue;
            }

            Map<String,List<String>> values = new HashMap<String, List<String>>();
            String url = null;
            while (lineno < lines.length) {
                line = lines[lineno];
                int equal = line.indexOf('=');
                if (equal == -1) {
                    break;
                }
                String key = line.substring(0, equal);
                List<String> list = values.get(key);
                if (list == null) {
                    list = new ArrayList<String>();
                    values.put(key, list);
                }
                String value = line.substring(equal+1);

                list.add(value);
                lineno++;

                if ("filename".equals(key)) {
                    url = value;
                }
            }

            if (url != null) {
                TestSearchResult result = new TestSearchResult(url, values);
                maps.add(result);
            }
        }

        return maps;
    }

    protected List<SearchResult> createTestMapsFromIndexFile(FileObject testFile) throws MalformedURLException {
        List<SearchResult> maps = new ArrayList<SearchResult>();

        String dump = readFile(testFile);
        if (dump == null) {
            return Collections.emptyList();
        }
        String[] lines = dump.split("\n");

        String name = testFile.getNameExt();
        assertTrue(name.endsWith(".indexed"));
        name = name.substring(0, name.length()-".indexed".length());
        FileObject source = testFile.getParent().getFileObject(name);
        assertNotNull(source);

        for (int lineno = 0; lineno < lines.length; lineno++) {
            // Skip empty lines
            String line = lines[lineno].trim();
            if (line.length() == 0 || !line.startsWith("Document ")) {
                continue;
            }
            lineno++; // Skip Document

            Map<String,List<String>> values = new HashMap<String, List<String>>();
            while (lineno < lines.length) {
                line = lines[lineno];
                if (line.startsWith("Document ")) {
                    lineno--;
                    break;
                }
                if (line.trim().length() == 0 || line.startsWith("Searchable Keys") || line.startsWith("Not Searchable Keys")) {
                    lineno++;
                    continue;
                }
                String SEPARATOR = " : ";

                int equal = line.indexOf(SEPARATOR);
                if (equal == -1) {
                    break;
                }
                String key = line.substring(0, equal).trim();
                List<String> list = values.get(key);
                if (list == null) {
                    list = new ArrayList<String>();
                    values.put(key, list);
                }
                String value = line.substring(equal+SEPARATOR.length()).trim();

                // HACK: Pretty printer in Ruby indexed files are missing a semicolon
                if ("method".equals(key) || "field".equals(key)) {
                    int semi = value.indexOf(';');
                    int pipe = value.indexOf('|');
                    if (pipe != -1 && semi < pipe) {
                        value = value.substring(0, pipe) + ";" + value.substring(pipe);
                    }
                }

                list.add(value);
                lineno++;
            }

            String url = FileUtil.toFile(source).toURI().toURL().toExternalForm();
            TestSearchResult result = new TestSearchResult(url, values);
            result.setValue("filename", url);
            maps.add(result);
        }

        int documentCount = 0;
        for (String line : lines) {
            if (line.startsWith("Document ")) {
                documentCount++;
            }
        }
        assertEquals(documentCount, maps.size());

        return maps;
    }

    protected class TestSearchResult implements SearchResult {
        private final String url;
        private final Map<String,List<String>> values;

        public TestSearchResult(String url, Map<String,List<String>> values) {
            this.url = url;
            this.values = values;
        }

        public void setValue(String key, String value) {
            values.put(key, Collections.singletonList(value));
        }

        public String getPersistentUrl() {
            return url;
        }

        public String getValue(String key) {
            if (values.containsKey(key)) {
                return values.get(key).get(0);
            }
            return null;
        }

        public String[] getValues(String key) {
            if (values.containsKey(key)) {
                List<String> v = values.get(key);
                return v.toArray(new String[v.size()]);
            }
            return null;
        }

        public Object getIndex() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getDocument() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getIndexReader() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getSegment() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getDocumentNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            return "SearchMap(" + url + ", " + values + ")";
        }
    };

    ////////////////////////////////////////////////////////////////////////////
    // Structure Analyzer Tests
    ////////////////////////////////////////////////////////////////////////////
    public StructureScanner getStructureScanner() {
        StructureScanner handler = getPreferredLanguage().getStructureScanner();
        assertNotNull("You must override getStructureScanner, either from your GsfLanguage or your test class", handler);
        return handler;
    }
    
    protected void checkStructure(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        StructureScanner analyzer = getStructureScanner();
        assertNotNull("getStructureScanner must be implemented", analyzer);
        
        HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append("ESCAPED{");
                sb.append(text, fromInclusive, toExclusive);
                sb.append("}");
            }
            
            @Override
            public void name(ElementKind kind, boolean start) {
                if (start) {
                    sb.append(kind);
                }
            }

            @Override
            public void active(boolean start) {
                if (start) {
                    sb.append("ACTIVE{");
                } else {
                    sb.append("}");
                }
            }
            
            @Override
            public void parameters(boolean start) {
                if (start) {
                    sb.append("PARAMETERS{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void type(boolean start) {
                if (start) {
                    sb.append("TYPE{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void deprecated(boolean start) {
                if (start) {
                    sb.append("DEPRECATED{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public String getText() {
                return sb.toString();
            }

            @Override
            public void emphasis(boolean start) {
            }
        };
        List<? extends StructureItem> structure = analyzer.scan(info);
        
        String annotatedSource = annotateStructure(info.getDocument(), structure, formatter);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".structure");
    }
    
    protected void checkFolds(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        StructureScanner analyzer = getStructureScanner();
        assertNotNull("getStructureScanner must be implemented", analyzer);

        Map<String,List<OffsetRange>> foldsMap = analyzer.folds(info);
        
        // Write folding structure
        String source = info.getText();
        List<Integer> begins = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        
        begins.add(0);
        
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                ends.add(i);
                if (i < source.length()) {
                    begins.add(i+1);
                }
            }
        }
        
        ends.add(source.length());

        assertEquals(begins.size(), ends.size());
        List<Character> margin = new ArrayList<Character>(begins.size());
        for (int i = 0; i < begins.size(); i++) {
            margin.add(' ');
        }

        List<String> typeList = new ArrayList<String>(foldsMap.keySet());
        Collections.sort(typeList);
        for (String type : typeList) {
            List<OffsetRange> ranges = foldsMap.get(type);
            for (OffsetRange range : ranges) {
                int beginIndex = Collections.binarySearch(begins, range.getStart());
                if (beginIndex < 0) {
                    beginIndex = -(beginIndex+2);
                }
                int endIndex = Collections.binarySearch(ends, range.getEnd());
                if (endIndex < 0) {
                    endIndex = -(endIndex+2);
                }
                for (int i = beginIndex; i <= endIndex; i++) {
                    char c = margin.get(i);
                    if (i == beginIndex) {
                        c = '+';
                    } else if (c != '+') {
                        if (i == endIndex) {
                            c = '-';
                        } else {
                            c = '|';
                        }
                    }
                    margin.set(i, c);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder(3000);
        for (int i = 0; i < begins.size(); i++) {
            sb.append(margin.get(i));
            sb.append(' ');
            for (int j = begins.get(i), max = ends.get(i); j < max; j++) {
                sb.append(source.charAt(j));
            }
            sb.append('\n');
        }
        String annotatedSource = sb.toString();
        
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".folds");
    }

    private void annotateStructureItem(int indent, StringBuilder sb, Document document, List<? extends StructureItem> structure, HtmlFormatter formatter) {
        for (StructureItem element : structure) {
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append(element.getName());
            sb.append(":");
            sb.append(element.getKind());
            sb.append(":");
            sb.append(element.getModifiers());
            sb.append(":");
            formatter.reset();
            sb.append(element.getHtml(formatter));
            sb.append(":");
            sb.append("\n");
            List<? extends StructureItem> children = element.getNestedItems();
            if (children != null && children.size() > 0) {
                List<? extends StructureItem> c = new ArrayList<StructureItem>(children);
                // Sort children to make tests more stable
                Collections.sort(c, new Comparator<StructureItem>() {
                    public int compare(StructureItem s1, StructureItem s2) {
                        String s1Name = s1.getName();
                        String s2Name = s2.getName();
                        if (s1Name == null || s2Name == null) {
                            if (s1Name == (Object)s2Name) { // Object Cast: avoid String==String semantic warning
                                return 0;
                            } else if (s1Name == null) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            return s1Name.compareTo(s2Name);
                        }
                    }
                    
                });
                
                annotateStructureItem(indent+1, sb, document, c, formatter);
            }
        }
    }

    private String annotateStructure(Document document, List<? extends StructureItem> structure, HtmlFormatter formatter) {
        StringBuilder sb = new StringBuilder();
        annotateStructureItem(0, sb, document, structure, formatter);
        
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Formatting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected Formatter getFormatter(IndentPrefs preferences) {
        Formatter formatter = getPreferredLanguage().getFormatter();
        assertNotNull("You must override getFormatter, either from your GsfLanguage or your test class", formatter);
        return formatter;
    }

    public class IndentPrefs {

        private final int hanging;

        private final int indent;

        public IndentPrefs(int indent, int hanging) {
            super();
            this.indent = indent;
            this.hanging = hanging;
        }

        public int getIndentation() {
            return indent;
        }

        public int getHangingIndentation() {
            return hanging;
        }
    }

    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, boolean indentOnly) throws BadLocationException {
        configureIndenters(document, formatter, compilationInfo, indentOnly, getPreferredMimeType());
    }

    protected void configureIndenters(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, boolean indentOnly, String mimeType) throws BadLocationException {
        ReformatTask.Factory reformatFactory = new ReformatTask.Factory() {
            public ReformatTask createTask(Context context) {
                final Context ctx = context;
                return new ReformatTask() {
                    public void reformat() throws BadLocationException {
                        if (formatter != null) {
                            formatter.reformat(ctx, compilationInfo);
                        }
                    }

                    public ExtraLock reformatLock() {
                        return null;
                    }
                };
            }
        };
        IndentTask.Factory indentFactory = new IndentTask.Factory() {
            public IndentTask createTask(Context context) {
                final Context ctx = context;
                return new IndentTask() {
                    public void reindent() throws BadLocationException {
                        if (formatter != null) {
                            formatter.reindent(ctx);
                        }
                    }

                    public ExtraLock indentLock() {
                        return null;
                    }
                };
            }

        };

        MockServices.setServices(MockMimeLookup.class);
        if (indentOnly) {
            MockMimeLookup.setInstances(MimePath.parse(mimeType), indentFactory);
        } else {
            MockMimeLookup.setInstances(MimePath.parse(mimeType), reformatFactory, indentFactory);
        }
    }

    @SuppressWarnings("deprecation") // For document.getFormatter() -- but annotation only seems to work at the method level
    protected void format(final BaseDocument document, final Formatter formatter,
            final CompilationInfo compilationInfo, int startPos, int endPos, boolean indentOnly) throws BadLocationException {
        //assertTrue(SwingUtilities.isEventDispatchThread());
        configureIndenters(document, formatter, compilationInfo, indentOnly);

//        formatter.reformat(doc, startPos, endPos, getInfoForText(source, "unittestdata"));
        final org.netbeans.editor.Formatter f = document.getFormatter();
        try {
            f.reformatLock();
            int reformattedLen = f.reformat(document, 
                    Math.min(document.getLength(), startPos),
                    Math.min(document.getLength(), endPos));
        } finally {
            f.reformatUnlock();
        }
    }

    public void format(String source, String reformatted, IndentPrefs preferences) throws Exception {
        final Formatter formatter = getFormatter(preferences);
        assertNotNull("getFormatter must be implemented", formatter);

        String BEGIN = "%<%"; // NOI18N
        int startPos = source.indexOf(BEGIN);
        if (startPos != -1) {
            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }

        String END = "%>%"; // NOI18N
        int endPos = source.indexOf(END);
        if (endPos != -1) {
            source = source.substring(0, endPos) + source.substring(endPos+END.length());
        }

        BaseDocument doc = getDocument(source);

        if (endPos == -1) {
            endPos = doc.getLength();
        }

        setupDocumentIndentation(doc, preferences);
        final CompilationInfo compilationInfo = getInfoForText(source, "unittestdata");
        format(doc, formatter, compilationInfo, startPos, endPos, false);
        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }


    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        //String before = doc.getText(0, doc.getLength());
        
        Formatter formatter = getFormatter(preferences);
        assertNotNull("getFormatter must be implemented", formatter);
        setupDocumentIndentation(doc, preferences);

        format(doc, formatter, getInfo(fo), 0, doc.getLength(), false);
        String after = doc.getText(0, doc.getLength());
        
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected BaseKit getEditorKit(String mimeType) {
        org.netbeans.modules.gsf.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
        assertNotNull(language);
        if (!language.useCustomEditorKit()) {
            GsfEditorKitFactory factory = new GsfEditorKitFactory(language);
            return factory.kit();
        }
        fail("Must override getEditorKit() for useCustomEditorKit languages");
        return null;
    }

    protected void toggleComment(String text, String expected) throws Exception {
        JEditorPane pane = getPane(text);

        runKitAction(pane, "toggle-comment", "");

        String toggled = pane.getText();
        assertEquals(expected, toggled);
    }

    protected JEditorPane getPane(String text) throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            fail("You must run this test from the event dispatch thread! To do that, add @Override protected boolean runInEQ() { return true } from your testcase!");
        }
        String BEGIN = "$start$"; // NOI18N
        String END = "$end$"; // NOI18N
        int sourceStartPos = text.indexOf(BEGIN);
        int caretPos = -1;
        int sourceEndPos = -1;
        if (sourceStartPos != -1) {
            text = text.substring(0, sourceStartPos) + text.substring(sourceStartPos+BEGIN.length());
            sourceEndPos = text.indexOf(END);
            assertTrue(sourceEndPos != -1);
            text = text.substring(0, sourceEndPos) + text.substring(sourceEndPos+END.length());
        } else {
            caretPos = text.indexOf('^');
            if (caretPos != -1) {
                text = text.substring(0, caretPos) + text.substring(caretPos+1);
            }
        }

        JEditorPane pane = new JEditorPane();
        pane.setContentType(getPreferredMimeType());
        pane.setEditorKit(getEditorKit(getPreferredMimeType()));
        pane.setText(text);

        BaseDocument bdoc = (BaseDocument)pane.getDocument();

        bdoc.putProperty(org.netbeans.api.lexer.Language.class, getPreferredLanguage().getLexerLanguage());
        bdoc.putProperty("mimeType", getPreferredMimeType());

        //bdoc.insertString(0, text, null);
        if (sourceStartPos != -1) {
            assertTrue(sourceEndPos != -1);
            pane.setSelectionStart(sourceStartPos);
            pane.setSelectionEnd(sourceEndPos);
        } else if (caretPos != -1) {
            pane.getCaret().setDot(caretPos);
        }
        pane.getCaret().setSelectionVisible(true);

        return pane;
    }

    protected void runKitAction(JEditorPane jt, String actionName, String cmd) {
        BaseKit kit = (BaseKit)jt.getEditorKit();
        Action a = kit.getActionByName(actionName);
        assertNotNull(a);
        a.actionPerformed(new ActionEvent(jt, 0, cmd));
    }
    
    protected void setupDocumentIndentation(BaseDocument doc, IndentPrefs preferences) {
        // Enforce indentprefs
        if (preferences != null) {
            assertEquals("Hanging indentation not yet supported; must be exposed through options", preferences.getIndentation(), preferences.getHangingIndentation());
            Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, preferences.getIndentation());
        } else {
            int preferred = 4;
            if (getPreferredLanguage().hasFormatter()) {
                preferred = getPreferredLanguage().getFormatter().indentSize();
            }
            Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, preferred);
        }
    }

    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        int sourcePos = source.indexOf('^');     
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);
        Formatter formatter = getFormatter(null);

        int reformattedPos = reformatted.indexOf('^');        
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);
        
        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, null, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    protected void insertBreak(String original, String expected) throws Exception {
        insertNewline(original, expected, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Code Completion Tests
    ////////////////////////////////////////////////////////////////////////////
    protected CodeCompletionHandler getCodeCompleter() {
        CodeCompletionHandler handler = getPreferredLanguage().getCompletionHandler();
        assertNotNull("You must override getCompletionHandler, either from your GsfLanguage or your test class", handler);
        return handler;
    }
    
    private String getSourceLine(String s, int offset) {
        int begin = offset;
        if (begin > 0) {
            begin = s.lastIndexOf('\n', offset-1);
            if (begin == -1) {
                begin = 0;
            } else if (begin < s.length()) {
                begin++;
            }
        }
        if (s.length() == 0) {
            return s;
        }
        s.charAt(offset);
        int end = s.indexOf('\n', offset);
        if (end == -1) {
            end = s.length();
        }
        return (s.substring(begin, offset)+"|"+s.substring(offset, end)).trim();
    }

    protected String getSourceWindow(String s, int offset) {
        int prevLineBegin;
        int nextLineEnd;
        int begin = offset;
        if (offset > 0) {
            begin = s.lastIndexOf('\n', offset);
            if (begin == -1) {
                begin = 0;
                prevLineBegin = 0;
            } else if (begin > 0) {
                prevLineBegin = s.lastIndexOf('\n', begin-1);
                if (prevLineBegin == -1) {
                    prevLineBegin = 0;
                } else if (prevLineBegin < s.length()) {
                    prevLineBegin++;
                }
            } else{
                prevLineBegin = 0;
            }
        } else {
            prevLineBegin = 0;
        }
        int end = s.indexOf('\n', offset);
        if (end == -1) {
            end = s.length();
            nextLineEnd = end;
        } else if (end < s.length()) {
            nextLineEnd = s.indexOf('\n', end+1);
            if (nextLineEnd == -1) {
                s.length();
            }
        } else {
            nextLineEnd = end;
        }
        return s.substring(prevLineBegin, offset)+"|"+s.substring(offset, nextLineEnd);
    }

    protected boolean skipRhs() {
        return false;
    }

    private String describeCompletion(String caretLine, String text, int caretOffset, NameKind kind, QueryType type, List<CompletionProposal> proposals, 
            boolean includeModifiers, boolean[] deprecatedHolder, final HtmlFormatter formatter) {
        assertTrue(deprecatedHolder != null && deprecatedHolder.length == 1);
        StringBuilder sb = new StringBuilder();
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", NameKind=" + kind + ")");
        sb.append("\n");

        // Sort to make test more stable
        Collections.sort(proposals, new Comparator<CompletionProposal>() {

            public int compare(CompletionProposal p1, CompletionProposal p2) {
                // Smart items first
                if (p1.isSmart() != p2.isSmart()) {
                    return p1.isSmart() ? -1 : 1;
                }

                if (p1.getKind() != p2.getKind()) {
                    return p1.getKind().compareTo(p2.getKind());
                }
                
                formatter.reset();
                String p1L = p1.getLhsHtml(formatter);
                formatter.reset();
                String p2L = p2.getLhsHtml(formatter);
                
                if (!p1L.equals(p2L)) {
                    return p1L.compareTo(p2L);
                }

                if (!skipRhs()) {
                    formatter.reset();
                    String p1Rhs = p1.getRhsHtml(formatter);
                    formatter.reset();
                    String p2Rhs = p2.getRhsHtml(formatter);
                    if (p1Rhs == null) {
                        p1Rhs = "";
                    }
                    if (p2Rhs == null) {
                        p2Rhs = "";
                    }
                    if (!p1Rhs.equals(p2Rhs)) {
                        return p1Rhs.compareTo(p2Rhs);
                    }
                }

                // Yuck - tostring comparison of sets!!
                if (!p1.getModifiers().toString().equals(p2.getModifiers().toString())) {
                    return p1.getModifiers().toString().compareTo(p2.getModifiers().toString());
                }
                
                return 0;
            }
        });
        
        boolean isSmart = true;
        for (CompletionProposal proposal : proposals) {
            if (isSmart && !proposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }

            deprecatedHolder[0] = false;
            formatter.reset();
            proposal.getLhsHtml(formatter); // Side effect to deprecatedHolder used
            boolean strike = includeModifiers && deprecatedHolder[0];
            
            String n = proposal.getKind().toString();
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }
            
//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

            sb.append(" ");
            
            formatter.reset();
            n = proposal.getLhsHtml(formatter);
            int MAX_LHS = 30;
            if (strike) {
                MAX_LHS -= 6; // Account for the --- --- strikethroughs
                sb.append("---");
            }
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            if (strike) {
                sb.append("---");
            }
            
            sb.append("  ");

            assertNotNull("Return Collections.emptySet() instead from getModifiers!", proposal.getModifiers());
            if (proposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = proposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");
            
            if (!skipRhs()) {
                formatter.reset();
                sb.append(proposal.getRhsHtml(formatter));
            }
            sb.append("\n");
            
            isSmart = proposal.isSmart();
        }
        
        return sb.toString();
    }

    private static org.netbeans.modules.gsf.Language getCompletableLanguage(Document doc, int offset) {
        BaseDocument baseDoc = (BaseDocument)doc;
        List<org.netbeans.modules.gsf.Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages(baseDoc, offset);
        for (org.netbeans.modules.gsf.Language l : list) {
            if (l.getCompletionProvider() != null) {
                return l;
            }
        }

        return null;
    }
    
    public void checkCompletion(String file, String caretLine, boolean includeModifiers) throws Exception {
        initializeClassPaths();

        // TODO call TestCompilationInfo.setCaretOffset!        
        final QueryType type = QueryType.COMPLETION;
        final boolean caseSensitive = true;
        final NameKind kind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

        final GsfTestCompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);
            ci.setCaretOffset(caretOffset);
        }

        //String targetMime = ci.getPreferredMimeType();
        String targetMime = getCompletableLanguage(ci.getDocument(), caretOffset).getMimeType();

        ParserResult pr = ci.getEmbeddedResult(targetMime, 0);
        assertNotNull(pr);
        
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", cc);
        final boolean deprecatedHolder[] = new boolean[1];
        
        final HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append(text, fromInclusive, toExclusive);
            }
            
            @Override
            public void emphasis(boolean start) {
            }

            @Override
            public void active(boolean start) {
            }

            @Override
            public void name(ElementKind kind, boolean start) {
            }

            @Override
            public void parameters(boolean start) {
            }

            @Override
            public void type(boolean start) {
            }

            @Override
            public void deprecated(boolean start) {
                deprecatedHolder[0] = true;
            }

            @Override
            public String getText() {
                return sb.toString();
            }
        };
        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        if (js != null) {
            assertNotNull(js);
            //ci.getIndex();
            //index.setDirty(js);
            js.testUpdateIndex();
        }
        
        final int finalCaretOffset = caretOffset;
        final String finalPrefix = prefix;
        CodeCompletionContext context = new CodeCompletionContext() {

            @Override
            public int getCaretOffset() {
                return finalCaretOffset;
            }

            @Override
            public CompilationInfo getInfo() {
                return ci;
            }

            @Override
            public String getPrefix() {
                return finalPrefix;
            }

            @Override
            public NameKind getNameKind() {
                return kind;
            }

            @Override
            public QueryType getQueryType() {
                return type;
            }

            @Override
            public boolean isCaseSensitive() {
                return caseSensitive;
            }
        };
        
        CodeCompletionResult completionResult = cc.complete(context);
        List<CompletionProposal> proposals = completionResult.getItems();
        
        String described = describeCompletion(caretLine, text, caretOffset, kind, type, proposals, includeModifiers, deprecatedHolder, formatter);
        assertDescriptionMatches(file, described, true, ".completion");
    }

    public void checkCompletionDocumentation(String file, String caretLine, boolean includeModifiers, String itemPrefix) throws Exception {
        initializeClassPaths();

        // TODO call TestCompilationInfo.setCaretOffset!        
        final QueryType type = QueryType.COMPLETION;
        final boolean caseSensitive = true;
        final NameKind kind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

        final GsfTestCompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);
            ci.setCaretOffset(caretOffset);
        }

        ParserResult pr = ci.getEmbeddedResult(ci.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", cc);
        final boolean deprecatedHolder[] = new boolean[1];
        
        final HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append(text, fromInclusive, toExclusive);
            }
            
            @Override
            public void emphasis(boolean start) {
            }

            @Override
            public void active(boolean start) {
            }

            @Override
            public void name(ElementKind kind, boolean start) {
            }

            @Override
            public void parameters(boolean start) {
            }

            @Override
            public void type(boolean start) {
            }

            @Override
            public void deprecated(boolean start) {
                deprecatedHolder[0] = true;
            }

            @Override
            public String getText() {
                return sb.toString();
            }
        };
        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        if (js != null) {
            assertNotNull(js);
            //ci.getIndex();
            //index.setDirty(js);
            js.testUpdateIndex();
        }
        
        final int finalCaretOffset = caretOffset;
        final String finalPrefix = prefix;
        CodeCompletionContext context = new CodeCompletionContext() {

            @Override
            public int getCaretOffset() {
                return finalCaretOffset;
            }

            @Override
            public CompilationInfo getInfo() {
                return ci;
            }

            @Override
            public String getPrefix() {
                return finalPrefix;
            }

            @Override
            public NameKind getNameKind() {
                return kind;
            }

            @Override
            public QueryType getQueryType() {
                return type;
            }

            @Override
            public boolean isCaseSensitive() {
                return caseSensitive;
            }
        };
        
        CodeCompletionResult completionResult = cc.complete(context);
        List<CompletionProposal> proposals = completionResult.getItems();

        CompletionProposal match = null;
        for (CompletionProposal proposal : proposals) {
            if (proposal.getName().startsWith(itemPrefix)) {
                match = proposal;
                break;
            }
        }
        assertNotNull(match);
        assertNotNull(match.getElement());

        // Get documentation
        String documentation = cc.document(ci, match.getElement());
        
        String described = describeCompletionDoc(text, caretOffset, kind, type, match, documentation, includeModifiers, deprecatedHolder, formatter);
        assertDescriptionMatches(file, described, true, ".html");
    }
    
    private String describeCompletionDoc(String text, int caretOffset, NameKind kind, QueryType type, 
             CompletionProposal proposal, String documentation,
            boolean includeModifiers, boolean[] deprecatedHolder, final HtmlFormatter formatter) {
        assertTrue(deprecatedHolder != null && deprecatedHolder.length == 1);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>\n");
        sb.append("<pre>");
        sb.append("Code completion result for source line:\n");
        String sourceLine = getSourceLine(text, caretOffset);
        if (sourceLine.length() == 1) {
            sourceLine = getSourceWindow(text, caretOffset);
        }
        sb.append(sourceLine);
        sb.append("\n(QueryType=" + type + ", NameKind=" + kind + ")");
        sb.append("\n");

        boolean isSmart = true;
        if (isSmart && !proposal.isSmart()) {
            sb.append("------------------------------------\n");
            isSmart = false;
        }

        deprecatedHolder[0] = false;
        formatter.reset();
        proposal.getLhsHtml(formatter); // Side effect to deprecatedHolder used
        boolean strike = includeModifiers && deprecatedHolder[0];

        String n = proposal.getKind().toString();
        int MAX_KIND = 10;
        if (n.length() > MAX_KIND) {
            sb.append(n.substring(0, MAX_KIND));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_KIND; i++) {
                sb.append(" ");
            }
        }

//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

        sb.append(" ");

        formatter.reset();
        n = proposal.getLhsHtml(formatter);
        int MAX_LHS = 30;
        if (strike) {
            MAX_LHS -= 6; // Account for the --- --- strikethroughs
            sb.append("---");
        }
        if (n.length() > MAX_LHS) {
            sb.append(n.substring(0, MAX_LHS));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_LHS; i++) {
                sb.append(" ");
            }
        }

        if (strike) {
            sb.append("---");
        }

        sb.append("  ");

        assertNotNull("Return Collections.emptySet() instead from getModifiers!", proposal.getModifiers());
        if (proposal.getModifiers().isEmpty()) {
            n = "";
        } else {
            n = proposal.getModifiers().toString();
        }
        int MAX_MOD = 9;
        if (n.length() > MAX_MOD) {
            sb.append(n.substring(0, MAX_MOD));
        } else {
            sb.append(n);
            for (int i = n.length(); i < MAX_MOD; i++) {
                sb.append(" ");
            }
        }

        sb.append("  ");

        formatter.reset();
        sb.append(proposal.getRhsHtml(formatter));
        sb.append("\n");

        isSmart = proposal.isSmart();
        sb.append("</pre>");
        sb.append("<h2>Documentation:</h2>");
        sb.append(documentation);

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }
    
    protected void assertAutoQuery(QueryType queryType, String source, String typedText) {
        CodeCompletionHandler completer = getCodeCompleter();
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);
        
        BaseDocument doc = getDocument(source);
        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(caretPos);
        
        QueryType qt = completer.getAutoQuery(ta, typedText);
        assertEquals(queryType, qt);
    }
    
    protected void checkCall(GsfTestCompilationInfo info, int caretOffset, String param, boolean expectSuccess) {
    }

    protected void initializeClassPaths() {
        initializeRegistry();
        // Force classpath initialization
        LanguageRegistry.getInstance().getLibraryUrls();
        org.netbeans.modules.gsf.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());
        org.netbeans.modules.gsfret.source.usages.ClassIndexManager.get(language).getBootIndices();
    }
    
    public void checkComputeMethodCall(String file, String caretLine, String param, boolean expectSuccess) throws Exception {
        initializeClassPaths();
        
        QueryType type = QueryType.COMPLETION;
        //boolean caseSensitive = true;

        GsfTestCompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);
        
        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);
            ci.setCaretOffset(caretOffset);
        }
        
        ParserResult pr = ci.getEmbeddedResult(ci.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getCodeCompleter must be implemented", cc);

        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        
        checkCall(ci, caretOffset, param, expectSuccess);
    }
    
    public void checkPrefix(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        BaseDocument doc = (BaseDocument)info.getDocument();
        StringBuilder sb = new StringBuilder();

        CodeCompletionHandler completer = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", completer);

        int index = 0;
        while (index < doc.getLength()) {
            int lineStart = index;
            int lineEnd = Utilities.getRowEnd(doc, index);
            if (lineEnd == -1) {
                break;
            }
            if (Utilities.getRowFirstNonWhite(doc, index) != -1) {
                String line = doc.getText(lineStart, lineEnd-lineStart);
                for (int i = lineStart; i <= lineEnd; i++) {
                    String prefix = completer.getPrefix(info, i, true); // line.charAt(i)
                    if (prefix == null) {
                        continue;
                    }
                    String wholePrefix = completer.getPrefix(info, i, false);
                    assertNotNull(wholePrefix);

                    sb.append(line +"\n");
                    //sb.append("Offset ");
                    //sb.append(Integer.toString(i));
                    //sb.append(" : \"");
                    for (int j = lineStart; j < i; j++) {
                        sb.append(' ');
                    }
                    sb.append('^');
                    sb.append(prefix.length() > 0 ? prefix : "\"\"");
                    sb.append(",");
                    sb.append(wholePrefix.length() > 0 ? wholePrefix : "\"\"");
                    sb.append("\n");
                }
            }
            
            index = lineEnd+1;
        }

        String annotatedSource = sb.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".prefixes");
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Ast Offsets Test
    ////////////////////////////////////////////////////////////////////////////
    protected String describeNode(CompilationInfo info, Object node, boolean includePath) throws Exception {
        // Override in your test
        return null;
    }
    
    protected void initializeNodes(CompilationInfo info, ParserResult result, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        // Override in your test
    }
    
    protected void checkOffsets(String relFilePath) throws Exception {
        checkOffsets(relFilePath, null);
    }
    
    protected void checkOffsets(String relFilePath, String caretLine) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);

            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Object> validNodes = new ArrayList<Object>();
        List<Object> invalidNodes = new ArrayList<Object>();
        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
        initializeNodes(info, info.getEmbeddedResult(getPreferredMimeType(), caretOffset),
                validNodes, positions, invalidNodes);

        String annotatedSource = annotateOffsets(validNodes, positions, invalidNodes, info);
        assertDescriptionMatches(relFilePath, annotatedSource, false, getOffsetTestGoldenSuffix());
    }

    /** Suffix to use for golden files for offset tests */
    protected String getOffsetTestGoldenSuffix() {
        return ".offsets";
    }
    
    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateOffsets(List<Object> validNodes, Map<Object,OffsetRange> positions,
            List<Object> invalidNodes, CompilationInfo info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        BaseDocument doc = (BaseDocument) info.getDocument();
        String text = doc.getText(0, doc.getLength());

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }
                
        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };
        
        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);
    
        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }
        
        // Sort nodes
        for (List<Object> list : starts.values()) {
            Collections.sort(list, FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            Collections.sort(list, BACKWARDS_COMPARATOR);
        }
        
        // Include 0-0 nodes first
        List<String> missing = new ArrayList<String>();
        for (Object n : invalidNodes) {
            String desc = describeNode(info, n, true);
            assertNotNull("You must implement describeNode()", desc);
            
            missing.add("Missing position for node " + desc);
        }
        Collections.sort(missing);
        for (String s : missing) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("\n");
        
        for (int i = 0; i < text.length(); i++) {
            List<Object> deferred = null;
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                List<Object> sts = starts.get(i);
                for (Object n : ns) {
                    if (sts != null && sts.contains(n)) {
                        if (deferred == null) {
                            deferred = new ArrayList<Object>();
                        }
                        deferred.add(n);
                    } else {
                        sb.append("</");
                        String desc = describeNode(info, n, false);
                        assertNotNull(desc);
                        sb.append(desc);
                        sb.append(">");
                    }
                }
            }
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                List<Object> ets = ends.get(i);
                for (Object n : ns) {
                    if (ets != null && ets.contains(n)) {
                        if (deferred == null) {
                            deferred = new ArrayList<Object>();
                        } else if (deferred.get(deferred.size()-1) != n) {
                            deferred.add(n);
                        }
                    } else {
                        sb.append("<");
                        String desc = describeNode(info, n, false);
                        assertNotNull(desc);
                        sb.append(desc);
                        sb.append(">");
                    }
                }
            }
            if (deferred != null) {
                for (Object n : deferred) {
                    sb.append("<");
                    String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append("/>");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }


    
    ////////////////////////////////////////////////////////////////////////////
    // Incremental Parsing and Offsets
    ////////////////////////////////////////////////////////////////////////////
    protected void verifyIncremental(ParserResult result, EditHistory history, ParserResult oldResult) {
        // Your module should check that the parser results are really okay and incremental here
    }

    public class TestDocumentEvent implements DocumentEvent {
        private int offset;
        private int length;

        public TestDocumentEvent(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }

        public int getOffset() {
            return offset;
        }
        public int getLength() {
            return length;
        }
        public Document getDocument() {
            return null;
        }
        public EventType getType() {
            return null;
        }
        public ElementChange getChange(Element elem) {
            return null;
        }
    }


    public static final String INSERT = "insert:"; // NOI18N
    public static final String REMOVE = "remove:"; // NOI18N

    public class IncrementalParse {
        public ParserResult oldParserResult;
        public GsfTestCompilationInfo info;
        public ParserResult newParserResult;
        public ParserResult fullParseResult;
        public EditHistory history;
        public String initialSource;
        public String modifiedSource;

        public IncrementalParse(ParserResult oldParserResult, GsfTestCompilationInfo info, ParserResult newParserResult,
                EditHistory history,
                String initialSource, String modifiedSource,
                ParserResult fullParseResult
                ) {
            this.oldParserResult = oldParserResult;
            this.info = info;
            this.newParserResult = newParserResult;
            this.history = history;
            this.initialSource = initialSource;
            this.modifiedSource = modifiedSource;
            this.fullParseResult = fullParseResult;
        }
    }

    protected final Pair<EditHistory,String> getEditHistory(String initialText, String... edits) {
        return getEditHistory(initialText, new EditHistory(), edits);
    }

    protected final Pair<EditHistory,String> getEditHistory(String initialText, EditHistory history, String... edits) {
        assertNotNull("Must provide a list of edits", edits);
        assertTrue("Should be an even number of edit events: pairs of caret, insert/remove", edits.length % 2 == 0);

        String modifiedText = initialText;
        for (int i = 0, n = edits.length; i < n; i += 2) {
            String caretLine = edits[i];
            String event = edits[i+1];
            int caretOffset = getCaretOffset(modifiedText, caretLine);

            assertTrue(event + " must start with " + INSERT + " or " + REMOVE,
                    event.startsWith(INSERT) || event.startsWith(REMOVE));
            if (event.startsWith(INSERT)) {
                event = event.substring(INSERT.length());
                history.insertUpdate(new TestDocumentEvent(caretOffset, event.length()));
                modifiedText = modifiedText.substring(0, caretOffset) + event + modifiedText.substring(caretOffset);
            } else {
                assertTrue(event.startsWith(REMOVE));
                event = event.substring(REMOVE.length());
                assertTrue(modifiedText.regionMatches(caretOffset, event, 0, event.length()));
                history.removeUpdate(new TestDocumentEvent(caretOffset, event.length()));
                modifiedText = modifiedText.substring(0, caretOffset) + modifiedText.substring(caretOffset+event.length());
            }
        }

        return new Pair<EditHistory,String>(history, modifiedText);
    }

    protected final Pair<EditHistory,String> getEditHistory(BaseDocument doc, final EditHistory history, String... edits) throws BadLocationException {
        assertNotNull("Must provide a list of edits", edits);
        assertTrue("Should be an even number of edit events: pairs of caret, insert/remove", edits.length % 2 == 0);

        String initialText = doc.getText(0, doc.getLength());
        doc.addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                history.insertUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                history.removeUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });

        TokenHierarchy th = TokenHierarchy.get((Document) doc);
        th.addTokenHierarchyListener(new TokenHierarchyListener() {
                public void tokenHierarchyChanged(TokenHierarchyEvent e) {
                    // I'm getting empty token change events from tests...
                    // Figure out why this happens
                    // assert e.type() != TokenHierarchyEventType.MODIFICATION || e.tokenChange().addedTokenCount() > 0 || e.tokenChange().removedTokenCount() > 0;
                    history.tokenHierarchyChanged(e);
                }
        });


        // Attempt to activate them token hierarchy, one of my attempts to get TokenHierarchyEvents fired
        //// doc.writeLock();
        //try {
        //    MutableTextInput input = (MutableTextInput)doc.getProperty(MutableTextInput.class);
        //    assertNotNull(input);
        //    input.tokenHierarchyControl().setActive(true);
        //} finally {
        //    // doc.writeUnlock();
        //}

        String modifiedText = initialText;
        for (int i = 0, n = edits.length; i < n; i += 2) {
            String caretLine = edits[i];
            String event = edits[i+1];
            int caretOffset = getCaretOffset(modifiedText, caretLine);

            assertTrue(event + " must start with " + INSERT + " or " + REMOVE,
                    event.startsWith(INSERT) || event.startsWith(REMOVE));
            if (event.startsWith(INSERT)) {
                event = event.substring(INSERT.length());
                //assertTrue(th.isActive());
                doc.insertString(caretOffset, event, null);
                modifiedText = modifiedText.substring(0, caretOffset) + event + modifiedText.substring(caretOffset);
            } else {
                assertTrue(event.startsWith(REMOVE));
                event = event.substring(REMOVE.length());
                assertTrue(modifiedText.regionMatches(caretOffset, event, 0, event.length()));
                doc.remove(caretOffset, event.length());
                modifiedText = modifiedText.substring(0, caretOffset) + modifiedText.substring(caretOffset+event.length());
            }
        }

        assertEquals(modifiedText, doc.getText(0, doc.getLength()));
        // Make sure the hierarchy is activated - this happens when we obtain a token sequence
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();
        for (int i = 0; i < 10; i++) {
            if (!ts.moveNext()) {
                break;
            }
        }

        return new Pair<EditHistory,String>(history, modifiedText);
    }

    /**
     * Produce an incremental parser result for the given test file with the given
     * series of edits. An edit is a pair of caret position string (with ^ representing
     * the caret) and a corresponding insert or delete (with insert:string or remove:string)
     * as the value.
     */
    protected final IncrementalParse getIncrementalResult(String relFilePath, double speedupExpectation, String... edits) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);

        // Obtain the initial parse result
        ParserResult initialResult = info.getEmbeddedResult(getPreferredMimeType(), 0);
        assertNotNull(initialResult);

        // Apply edits
        String initialText = info.getText();
        assertNotNull(initialText);
        Pair<EditHistory,String> pair = getEditHistory(initialText, edits);
        EditHistory history = pair.getA();
        String modifiedText = pair.getB();

        info.setText(modifiedText);
        info.setEditHistory(history);
        info.setPreviousResult(initialResult);

        // Attempt to avoid garbage collection during timing
        System.gc();
        System.gc();
        System.gc();
        long incrementalStartTime = System.nanoTime();
        int caretOffset = history.getStart();
        if (history.getSizeDelta() > 0) {
            caretOffset += history.getSizeDelta();
        }
        info.setCaretOffset(caretOffset);
        ParserResult incrementalResult = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(incrementalResult);
        long incrementalEndTime = System.nanoTime();
        verifyIncremental(incrementalResult, history, initialResult);

        info.setEditHistory(null);
        info.setPreviousResult(null);
        info.setText(modifiedText);

        System.gc();
        System.gc();
        System.gc();
        long fullParseStartTime = System.nanoTime();
        ParserResult fullParseResult = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        long fullParseEndTime = System.nanoTime();
        assertNotNull(fullParseResult);

        if (speedupExpectation > 0.0) {
            long incrementalParseTime = incrementalEndTime-incrementalStartTime;
            long fullParseTime = fullParseEndTime-fullParseStartTime;
            // Figure out how to ensure garbage collections etc. make a fair run.
            assertTrue("Incremental parsing time (" + incrementalParseTime + " ns) should be less than full parse time (" + fullParseTime + " ns); speedup was " +
                    ((double)fullParseTime)/incrementalParseTime,
                    ((double)incrementalParseTime)*speedupExpectation < fullParseTime);
        }

        assertEquals(incrementalResult.getDiagnostics().toString(), fullParseResult.getDiagnostics().size(), incrementalResult.getDiagnostics().size());

        return new IncrementalParse(initialResult, info, incrementalResult, history, initialText, modifiedText, fullParseResult);
    }

    /**
     * Check incremental parsing
     * @param relFilePath Path to test file to be parsed
     * @param speedupExpectation The speed up we're expecting for incremental processing
     *   over normal full-file analysis. E.g. 1.0d means we want to ensure that incremental
     *   parsing is at least as fast as normal parsing. For small files there may be extra
     *   overhead; you can pass 0.0d to turn off this check (but the test runs to ensure
     *   that things are working okay.)
     * @param edits A list of edits to perform.
     */
    protected final void checkIncremental(String relFilePath, double speedupExpectation, String... edits) throws Exception {
        IncrementalParse parse = getIncrementalResult(relFilePath, speedupExpectation, edits);

        ParserResult incrementalResult = parse.newParserResult;
        ParserResult fullParseResult = parse.fullParseResult;
        CompilationInfo info = parse.info;

        BaseDocument doc = (BaseDocument)info.getDocument();
        assertEquals("Parse trees must equal", doc, fullParseResult,incrementalResult);
        
//        List<Object> incrValidNodes = new ArrayList<Object>();
//        List<Object> incrInvalidNodes = new ArrayList<Object>();
//        Map<Object,OffsetRange> incrPositions = new HashMap<Object,OffsetRange>();
//        initializeNodes(info, incrementalResult, incrValidNodes, incrPositions, incrInvalidNodes);
//
//        String incrementalAnnotatedSource = annotateOffsets(incrValidNodes, incrPositions, incrInvalidNodes, info);
//
//        // Now make sure we get an identical linearization of the non-incremental result
//        List<Object> validNodes = new ArrayList<Object>();
//        List<Object> invalidNodes = new ArrayList<Object>();
//        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
//        initializeNodes(info, fullParseResult, validNodes, positions, invalidNodes);
//
//        String fullParseAnnotatedSource = annotateOffsets(validNodes, positions, invalidNodes, info);
//
//        assertEquals(fullParseAnnotatedSource, incrementalAnnotatedSource);
    }

    protected void assertEquals(String message, BaseDocument doc, ParserResult expected, ParserResult actual) throws Exception {
        fail("You must override assertEquals(ParserResult,ParserResult)");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Type Test
    ////////////////////////////////////////////////////////////////////////////
    protected void initializeTypeNodes(CompilationInfo info, List<Object> nodes,
            Map<Object,OffsetRange> positions, Map<Object,String> types) throws Exception {
        // Override in your test
        // Associate type descriptions with a bunch of nodes.
        // For every node that has an associated type, add position and description information about it.
        // This will then be used to generate type hints in the source
    }
    
    protected void checkTypes(String relFilePath) throws Exception {
        checkTypes(relFilePath, null);
    }
    
    protected void checkTypes(String relFilePath, String caretLine) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            caretOffset = getCaretOffset(text, caretLine);
            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Object> nodes = new ArrayList<Object>();
        Map<Object,String> types = new HashMap<Object,String>();
        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
        initializeTypeNodes(info, nodes, positions, types);

        BaseDocument doc = (BaseDocument) info.getDocument();
        String annotatedSource = annotateTypes(nodes, positions, types, info);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".types");
    }
    
    
    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateTypes(List<Object> validNodes, Map<Object,OffsetRange> positions,
            Map<Object,String> types, CompilationInfo info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        BaseDocument doc = (BaseDocument) info.getDocument();
        String text = doc.getText(0, doc.getLength());

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }
                
        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };
        
        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);
    
        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }
        
        // Sort nodes
        for (List<Object> list : starts.values()) {
            Collections.sort(list, FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            Collections.sort(list, BACKWARDS_COMPARATOR);
        }
        
        // TODO - include information here about nodes without correct positions
        
        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                for (Object n : ns) {
                    sb.append("<");
                    String desc = types.get(n);
                    //String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                for (Object n : ns) {
                    sb.append("</");
                    //String desc = describeNode(info, n, false);
                    String desc = types.get(n);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Hints / Quickfix Tests
    ////////////////////////////////////////////////////////////////////////////
    protected HintsProvider getHintsProvider() {
        HintsProvider provider = getPreferredLanguage().getHintsProvider();
        assertNotNull("You must override getHintsProvider, either from your GsfLanguage or your test class", provider);
        return provider;
    }

    protected GsfHintsManager getHintsManager(org.netbeans.modules.gsf.Language language) {
        return new GsfHintsManager(getPreferredMimeType(), getHintsProvider(), language);
    }
    
    protected String annotateHints(BaseDocument doc, List<Hint> result, int caretOffset) throws Exception {
        Map<OffsetRange, List<Hint>> posToDesc = new HashMap<OffsetRange, List<Hint>>();
        Set<OffsetRange> ranges = new HashSet<OffsetRange>();
        for (Hint desc : result) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            List<Hint> l = posToDesc.get(range);
            if (l == null) {
                l = new ArrayList<Hint>();
                posToDesc.put(range, l);
            }
            l.add(desc);
            ranges.add(range);
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                List<Hint> descsOnLine = null;
                int underlineStart = -1;
                int underlineEnd = -1;
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        if (descsOnLine == null) {
                            descsOnLine = new ArrayList<Hint>();
                        }
                        underlineStart = i-lineStart;
                        OffsetRange range = starts.get(i);
                        if (posToDesc.get(range) != null) {
                            for (Hint desc : posToDesc.get(range)) {
                                descsOnLine.add(desc);
                            }
                        }
                    }
                    if (ends.containsKey(i)) {
                        underlineEnd = i-lineStart;
                    }
                    sb.append(text.charAt(i));
                }
                if (underlineStart != -1) {
                    for (int i = 0; i < underlineStart; i++) {
                        sb.append(" ");
                    }
                    for (int i = underlineStart; i < underlineEnd; i++) {
                        sb.append("-");
                    }
                    sb.append("\n");
                }
                if (descsOnLine != null) {
                    Collections.sort(descsOnLine, new Comparator<Hint>() {
                        public int compare(Hint arg0, Hint arg1) {
                            return arg0.getDescription().compareTo(arg1.getDescription());
                        }
                    });
                    for (Hint desc : descsOnLine) {
                        sb.append("HINT:");
                        sb.append(desc.getDescription());
                        sb.append("\n");
                        List<HintFix> list = desc.getFixes();
                        if (list != null) {
                            for (HintFix fix : list) {
                                sb.append("FIX:");
                                sb.append(fix.getDescription());
                                sb.append("\n");
                            }
                        }
                    }
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }
 
    protected boolean parseErrorsOk;

    protected boolean checkAllHintOffsets() {
        return true;
    }

    protected void customizeHintError(Error error, int start) {
        // Optionally override
    }

    private enum ChangeOffsetType { NONE, OVERLAP, OUTSIDE };

    private void customizeHintInfo(GsfTestCompilationInfo info, ParserResult result, ChangeOffsetType changeOffsetType) {
        if (changeOffsetType == ChangeOffsetType.NONE) {
            return;
        }
        if (info == null || result == null) {
            return;
        }
        // Test offset handling to make sure we can handle bogus node positions

        Document doc = info.getDocument();
        int docLength = doc.getLength();
        // Replace errors with offsets
        List<Error> errors = new ArrayList<Error>();
        List<Error> oldErrors = result.getDiagnostics();
        for (Error error : oldErrors) {
            int start = error.getStartPosition();
            int end = error.getEndPosition();

            // Modify document position to be off
            int length = end-start;
            if (changeOffsetType == ChangeOffsetType.OUTSIDE) {
                start = docLength+1;
            } else {
                start = docLength-1;
            }
            end = start+length;
            if (end <= docLength) {
                end = docLength+1;
            }

            DefaultError newError = new DefaultError(error.getKey(), error.getDisplayName(), error.getDescription(), error.getFile(), start,
                    end, error.getSeverity());
            newError.setParameters(error.getParameters());
            customizeHintError(error, start);
            errors.add(newError);
        }
        oldErrors.clear();
        oldErrors.addAll(errors);
    }
    
    protected ComputedHints getHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints hints = computeHints(test, hint, relFilePath, fileObject, caretLine, ChangeOffsetType.NONE);

        if (checkAllHintOffsets()) {
            // Run alternate hint computation AFTER the real computation above since we will destroy the document...
            Logger.global.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        record.getThrown().printStackTrace(new PrintWriter(sw));
                        fail("Encountered error: " + sw.toString());
                    }
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() throws SecurityException {
                }

            });
            for (ChangeOffsetType type : new ChangeOffsetType[] { ChangeOffsetType.OUTSIDE, ChangeOffsetType.OVERLAP }) {
                computeHints(test, hint, relFilePath, fileObject, caretLine, type);
            }
        }

        return hints;
    }

    protected ComputedHints computeHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine, ChangeOffsetType changeOffsetType) throws Exception {
        assertTrue(relFilePath == null || fileObject == null);
        UserConfigurableRule ucr = null;
        if (hint instanceof UserConfigurableRule) {
            ucr = (UserConfigurableRule)hint;
        }

        initializeRegistry();
        org.netbeans.modules.gsf.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());

        HintsProvider provider = getHintsProvider();
        GsfHintsManager manager = getHintsManager(language);

        // Make sure the hint is enabled
        if (ucr != null && !HintsSettings.isEnabled(manager, ucr)) {
            Preferences p = HintsSettings.getPreferences(manager, ucr, HintsSettings.getCurrentProfileId());
            HintsSettings.setEnabled(p, true);
        }
        
        GsfTestCompilationInfo info = fileObject != null ? getInfo(fileObject) : getInfo(relFilePath);
        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);

        if (changeOffsetType != ChangeOffsetType.NONE) {
            customizeHintInfo(info, pr, changeOffsetType);

            // Also: Delete te contents from the document!!!
            // This ensures that the node offsets will be out of date by the time the rules run
            Document doc = info.getDocument();
            doc.remove(0, doc.getLength());
        }

        if (pr == null /*|| pr.hasErrors()*/ && !(hint instanceof ErrorRule)) { // only expect testcase source errors in error tests
            if (parseErrorsOk) {
                List<Hint> result = new ArrayList<Hint>();
                int caretOffset = 0;
                return new ComputedHints(info, result, caretOffset);
            }
            assertTrue("Unexpected parse error in test case " + 
                    FileUtil.getFileDisplayName(info.getFileObject()) + "\nErrors = " + 
                    info.getErrors(), pr != null && !pr.hasErrors());
        }

        String text = info.getText();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue("NOT FOUND: " + info.getFileObject().getName() + ":" + caretLine, lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        List<Hint> hints = new ArrayList<Hint>();
        if (hint instanceof ErrorRule) {
            RuleContext context = manager.createRuleContext(info, language, -1, -1, -1);
            // It's an error!
            // Create a hint registry which contains ONLY our hint (so other registered
            // hints don't interfere with the test)
            Map<Object, List<? extends ErrorRule>> testHints = new HashMap<Object, List<? extends ErrorRule>>();
            if (hint.appliesTo(context)) {
                ErrorRule ErrorRule = (ErrorRule)hint;
                for (Object key : ErrorRule.getCodes()) {
                    testHints.put(key, Collections.singletonList(ErrorRule));
                }
            }
            manager.setTestingRules(testHints, null, null, null);
            provider.computeErrors(manager, context, hints, new ArrayList<Error>());
        } else if (hint instanceof SelectionRule) {
            SelectionRule rule = (SelectionRule)hint;
            List<SelectionRule> testHints = new ArrayList<SelectionRule>();
            testHints.add(rule);
            
            manager.setTestingRules(null, null, null, testHints);
            
            if (caretLine != null) {
                int start = text.indexOf(caretLine);
                int end = start+caretLine.length();
                RuleContext context = manager.createRuleContext(info, language, -1, start, end);
                provider.computeSelectionHints(manager, context, hints, start, end);
            }
        } else {
            assertTrue(hint instanceof AstRule && ucr != null);
            AstRule AstRule = (AstRule)hint;
            // Create a hint registry which contains ONLY our hint (so other registered
            // hints don't interfere with the test)
            Map<Object, List<? extends AstRule>> testHints = new HashMap<Object, List<? extends AstRule>>();
            RuleContext context = manager.createRuleContext(info, language, caretOffset, -1, -1);
            if (hint.appliesTo(context)) {
                for (Object nodeId : AstRule.getKinds()) {
                    testHints.put(nodeId, Collections.singletonList(AstRule));
                }
            }
            if (HintsSettings.getSeverity(manager, ucr) == HintSeverity.CURRENT_LINE_WARNING) {
                manager.setTestingRules(null, Collections.EMPTY_MAP, testHints, null);
                provider.computeSuggestions(manager, context, hints, caretOffset);
            } else {
                manager.setTestingRules(null, testHints, null, null);
                context.caretOffset = -1;
                provider.computeHints(manager, context, hints);
            }
        }

        return new ComputedHints(info, hints, caretOffset);
    }
    
    protected void checkHints(NbTestCase test, Rule hint, String relFilePath, String caretLine) throws Exception {
        findHints(test, hint, relFilePath, null, caretLine);
    }
    
    protected void checkHints(Rule hint, String relFilePath,
            String selStartLine, String selEndLine) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;
        
        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;
        
        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";
        
        checkHints(this, hint, relFilePath, caretLine);
    }

    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, FileObject fileObject, String caretLine) throws Exception {
        findHints(test, hint, null, fileObject, caretLine);
    }
    
    protected String getGoldenFileSuffix() {
        return "";
    }
    
    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, fileObject, caretLine);
        CompilationInfo info = r.info;
        List<Hint> result = r.hints;
        int caretOffset = r.caretOffset;

        String annotatedSource = annotateHints((BaseDocument)info.getDocument(), result, caretOffset);

        if (fileObject != null) {
            assertDescriptionMatches(fileObject, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        } else {
            assertDescriptionMatches(relFilePath, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        }
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String selStartLine, String selEndLine, String fixDesc) throws Exception {
        applyHint(test, hint, relFilePath, selStartLine, selEndLine, fixDesc, false);
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String selStartLine, String selEndLine, String fixDesc, boolean hasEditRegions) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;
        
        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;
        
        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";
        
        applyHint(test, hint, relFilePath, caretLine, fixDesc, hasEditRegions);
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String caretLine, String fixDesc) throws Exception {
        applyHint(test, hint, relFilePath, caretLine, fixDesc, false);
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String caretLine, String fixDesc, boolean hasEditRegions) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, null, caretLine);
        CompilationInfo info = r.info;
        
        HintFix fix = findApplicableFix(r, fixDesc);
        assertNotNull(fix);

        Set<OffsetRange> annotateRegions = null;
        int annotateCaret = -1;

        if (hasEditRegions) {
            TestEditRegionsImpl.lastRegions = null;
            fix.implement();
            assertNotNull(TestEditRegionsImpl.lastRegions);
            assertTrue(TestEditRegionsImpl.lastCaretOffset != -1);

            annotateRegions = TestEditRegionsImpl.lastRegions;
            annotateCaret = TestEditRegionsImpl.lastCaretOffset;
        } else if (fix.isInteractive() && fix instanceof PreviewableFix) {
            PreviewableFix preview = (PreviewableFix)fix;
            assertTrue(preview.canPreview());
            EditList editList = preview.getEditList();
            editList.applyToDocument((BaseDocument) info.getDocument());
        } else {
            fix.implement();
        }
        
        Document doc = info.getDocument();

        String fixed = doc.getText(0, doc.getLength());

        if (annotateRegions != null) {
            Set<Integer> begins = new HashSet<Integer>();
            Set<Integer> ends = new HashSet<Integer>();

            for (OffsetRange range : annotateRegions) {
                begins.add(range.getStart());
                ends.add(range.getEnd());
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fixed.length(); i++) {
                if (ends.contains(i)) {
                    sb.append("<|"); // NOI18N
                }
                if (begins.contains(i)) {
                    sb.append("|>"); // NOI18N
                }
                if (i == annotateCaret) {
                    sb.append("^"); // NOI18N
                }
                sb.append(fixed.charAt(i));
            }

            fixed = sb.toString();
        }

        assertDescriptionMatches(relFilePath, fixed, true, ".fixed");
    }
    
//    public void ensureRegistered(AstRule hint) throws Exception {
//        Map<Integer, List<AstRule>> hints = JsRulesManager.getInstance().getHints();
//        Set<Integer> kinds = hint.getKinds();
//        for (int nodeType : kinds) {
//            List<AstRule> rules = hints.get(nodeType);
//            assertNotNull(rules);
//            boolean found = false;
//            for (AstRule rule : rules) {
//                if (rule instanceof BlockVarReuse) {
//                    found  = true;
//                    break;
//                }
//            }
//            
//            assertTrue(found);
//        }
//    }

    private HintFix findApplicableFix(ComputedHints r, String text) {
        boolean substringMatch = true;
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
            substringMatch = false;
        }
        int caretOffset = r.caretOffset;
        for (Hint desc : r.hints) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            if (range.containsInclusive(caretOffset) || caretOffset == range.getEnd()+1) { // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                List<HintFix> list = desc.getFixes();
                assertNotNull(list);
                for (HintFix fix : list) {
                    if (text == null ||
                            (substringMatch && fix.getDescription().indexOf(text) != -1) ||
                            (!substringMatch && fix.getDescription().equals(text))) {
                        return fix;
                    }
                }
            }
        }
        
        return null;
    }
    
    protected static class ComputedHints {
        ComputedHints(CompilationInfo info, List<Hint> hints, int caretOffset) {
            this.info = info;
            this.hints = hints;
            this.caretOffset = caretOffset;
        }

        @Override
        public String toString() {
            return "ComputedHints(caret=" + caretOffset + ",info=" + info + ",hints=" + hints + ")";
        }

        public CompilationInfo info;
        public List<Hint> hints;
        public int caretOffset;
    }

    ////////////////////////////////////////////////////////////////////////////
    // DeclarationFinder
    ////////////////////////////////////////////////////////////////////////////
    protected DeclarationFinder getFinder() {
        DeclarationFinder finder = getPreferredLanguage().getDeclarationFinder();
        if (finder == null) {
            fail("You must override getFinder()");
        }

        return finder;
    }

    protected DeclarationLocation findDeclaration(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);
        int caretOffset = lineOffset + caretDelta;

        DeclarationFinder finder = getFinder();
        DeclarationLocation location = finder.findDeclaration(info, caretOffset);

        return location;
    }

    protected void checkDeclaration(String relFilePath, String caretLine, URL url) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            assertTrue("DeclarationLocation.NONE", false);
        }

        assertEquals(location.getUrl(), url);
    }

    protected void checkDeclaration(String relFilePath, String caretLine, String declarationLine) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            assertTrue("DeclarationLocation.NONE", false);
        }

        int caretDelta = declarationLine.indexOf('^');
        assertTrue(caretDelta != -1);
        declarationLine = declarationLine.substring(0, caretDelta) + declarationLine.substring(caretDelta + 1);
        String text = readFile(location.getFileObject());
        int lineOffset = text.indexOf(declarationLine);
        assertTrue(lineOffset != -1);
        int caretOffset = lineOffset + caretDelta;

        if (caretOffset != location.getOffset()) {
            fail("Offset mismatch (expected " + caretOffset + " vs. actual " + location.getOffset() + ": got " + getSourceWindow(text, location.getOffset()));
        }
        assertEquals(caretOffset, location.getOffset());
    }

    protected void checkDeclaration(String relFilePath, String caretLine, String file, int offset) throws Exception {
        DeclarationLocation location = findDeclaration(relFilePath, caretLine);
        if (location == DeclarationLocation.NONE) {
            // if we dont found a declaration, bail out.
            assertTrue("DeclarationLocation.NONE", false);
        }

        assertEquals(file, location.getFileObject() != null ? location.getFileObject().getNameExt() : "<none>");
        assertEquals(offset, location.getOffset());
    }
}
