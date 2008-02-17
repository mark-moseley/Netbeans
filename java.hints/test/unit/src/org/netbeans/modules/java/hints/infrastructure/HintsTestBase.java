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
package org.netbeans.modules.java.hints.infrastructure;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.gen.WhitespaceIgnoringDiff;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.JavaDataObject.JavaEditorSupport;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Jan Lahoda
 */
public class HintsTestBase extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public HintsTestBase(String name) {
        super(name);
        
    }
    
    private FileObject packageRoot;
    private FileObject testSource;
    private JavaSource js;
    protected CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    private CancellableTask<CompilationInfo> task;
    
    protected void setUp() throws Exception {
        doSetUp(layer());
    }
    
    protected void doSetUp(String resource) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml", resource}, new Object[] {
            JavaDataLoader.class,
            new MimeDataProvider() {
                public Lookup getLookup(MimePath mimePath) {
                    return Lookups.fixed(new Object[] {
                        new JavaKit(),
                    });
                }
            },
            new LanguageProvider() {
                public Language<?> findLanguage(String mimePath) {
                    return JavaTokenId.language();
                }
                
                public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
                        LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    return null;
                }
            }
        });
        
        if (cache == null) {
            cache = FileUtil.normalizeFile(TestUtil.createWorkFolder());
            cacheFO = FileUtil.toFileObject(cache);
            
            cache.deleteOnExit();
            
            IndexUtil.setCacheFolder(cache);
            
            if (createCaches()) {
                for (URL u : SourceUtilsTestUtil.getBootClassPath()) {
                    FileObject file = URLMapper.findFileObject(u);
                    
                    if (file == null)
                        continue;
                    
                    file = FileUtil.getArchiveFile(file);
                    
                    if (file == null)
                        continue;
                    
                    File jioFile = FileUtil.toFile(file);
                    
                    if (jioFile == null)
                        continue;
                    
                    final ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(u, false);
                    ProgressHandle handle = ProgressHandleFactory.createHandle("cache creation");
                    BinaryAnalyser ba = ci.getBinaryAnalyser();
                    
                    ba.start(u, handle, new AtomicBoolean(false), new AtomicBoolean(false));
                    ba.finish();
                }
            }
        }
    }
    
    protected boolean createCaches() {
        return true;
    }
    
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/";
    }
    
    protected String layer() {
        return "org/netbeans/modules/java/hints/resources/layer.xml";
    }
    
    protected void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        
        packageRoot = FileUtil.createFolder(sourceRoot, testDataExtension());
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = testDataExtension();
        File   testPackageFile = new File(getDataDir(), testPackagePath);
        
        String[] names = testPackageFile.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(".java"))
                    return true;
                
                return false;
            }
        });
        
        String[] files = new String[names.length];
        
        for (int cntr = 0; cntr < files.length; cntr++) {
            files[cntr] = testPackagePath + names[cntr];
        }
        
        TestUtil.copyFiles(getDataDir(), FileUtil.toFile(sourceRoot), files);
        
        packageRoot.refresh();
        
        capitalizedName = capitalizedName.substring(capitalizedName.lastIndexOf('.') + 1);
        
        testSource = packageRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
        
        task = new LazyHintComputationFactory().createTask(testSource);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;

        FileUtil.refreshFor(File.listRoots());
        
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
//    private int getOffset(Document doc, int linenumber, int column) {
//        return NbDocument.findLineOffset((StyledDocument) doc, linenumber - 1) + column - 1;
//    }
    
    private List<Fix> getFixes(ErrorDescription d) throws Exception {
        LazyFixList f = d.getFixes();
        
        f.getFixes();
        
        task.run(info);
        
        return f.getFixes();
    }
    
    //XXX: copied from org.netbeans.modules.editor.hints.borrowed.ListCompletionView, would be nice to have this on one place only:
        private List<Fix> sortFixes(Collection<Fix> fixes) {
            fixes = new LinkedHashSet<Fix>(fixes);
            
            List<EnhancedFix> sortableFixes = new ArrayList<EnhancedFix>();
            List<Fix> other = new LinkedList<Fix>();
            
            for (Fix f : fixes) {
                if (f instanceof EnhancedFix) {
                    sortableFixes.add((EnhancedFix) f);
                } else {
                    other.add(f);
                }
            }
            
            Collections.sort(sortableFixes, new FixComparator());
            
            List<Fix> result = new ArrayList<Fix>();
            
            result.addAll(sortableFixes);
            result.addAll(other);
            
            return result;
        }

        private static final class FixComparator implements Comparator<EnhancedFix> {

            public int compare(EnhancedFix o1, EnhancedFix o2) {
                return compareText(o1.getSortText(), o2.getSortText());
            }
            
        }
        
        private static int compareText(CharSequence text1, CharSequence text2) {
            int len = Math.min(text1.length(), text2.length());
            for (int i = 0; i < len; i++) {
                char ch1 = text1.charAt(i);
                char ch2 = text2.charAt(i);
                if (ch1 != ch2) {
                    return ch1 - ch2;
                }
            }
            return text1.length() - text2.length();
        }
    
    private int getStartLine(ErrorDescription d) throws IOException {
        return d.getRange().getBegin().getLine();
    }
    
    protected void performHintsPresentCheck(String className, int line, int column, boolean present) throws Exception {
        prepareTest(className);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        Document doc = ec.openDocument();
        
        List<ErrorDescription> errors = new ErrorHintsProvider(testSource).computeErrors(info, doc);
        List<Fix> fixes = new ArrayList<Fix>();
        
        for (ErrorDescription d : errors) {
            if (getStartLine(d) + 1 == line)
                fixes.addAll(getFixes(d));
        }
        
        if (present) {
            assertTrue(fixes != null && !fixes.isEmpty());
        } else {
            assertTrue(fixes == null || fixes.isEmpty());
        }
    }
    
    protected void performTestDoNotPerform(String className, int line, int column) throws Exception {
        prepareTest(className);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        Document doc = ec.openDocument();
        
        List<ErrorDescription> errors = new ErrorHintsProvider(testSource).computeErrors(info, doc);
        List<Fix> fixes = new ArrayList<Fix>();
        
        for (ErrorDescription d : errors) {
            if (getStartLine(d) + 1 == line)
                fixes.addAll(getFixes(d));
        }
        
        fixes = sortFixes(fixes);
        
        File fixesDump = new File(getWorkDir(), getName() + "-hints.out");
        File diff   = new File(getWorkDir(), getName() + "-hints.diff");
        
        Writer hintsWriter = new FileWriter(fixesDump);
        
        for (Fix f : fixes) {
            hintsWriter.write(f.getText());
            hintsWriter.write("\n");
        }
        
        hintsWriter.close();
        
        File hintsGolden = getGoldenFile(getName() + "-hints.pass");
        
        assertFile(fixesDump, hintsGolden, diff);
    }
    
    protected  void performTest(String className, String performHint, int line, int column) throws Exception {
        performTest(className, className, performHint, line, column, true);
    }
    
    protected void performTest(String className, String modifiedClassName,
            String performHint, int line, int column, boolean checkHintList) throws Exception {
        prepareTest(className);
        DataObject od = DataObject.find(testSource);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        try {
            Document doc = ec.openDocument();
            
            List<ErrorDescription> errors = new ErrorHintsProvider(testSource).computeErrors(info, doc);
            List<Fix> fixes = new ArrayList<Fix>();
            
            for (ErrorDescription d : errors) {
                if (getStartLine(d) + 1 == line)
                    fixes.addAll(getFixes(d));
            }
            
            fixes = sortFixes(fixes);
        
            Fix toPerform = null;
            
            if (checkHintList) {
                File fixesDump = new File(getWorkDir(), getName() + "-hints.out");
                
                Writer hintsWriter = new FileWriter(fixesDump);
                
                for (Fix f : fixes) {
                    if (f.getText().indexOf(performHint) != (-1)) {
                        toPerform = f;
                    }
                    
                    hintsWriter.write(f.getText());
                    hintsWriter.write("\n");
                }
                
                hintsWriter.close();
                
                File hintsGolden = getGoldenFile(getName() + "-hints.pass");
                File diff   = new File(getWorkDir(), getName() + "-hints.diff");
                
                assertFile(fixesDump, hintsGolden, diff);
            } else {
                for (Fix f : fixes) {
                    if (f.getText().indexOf(performHint) != (-1)) {
                        toPerform = f;
                    }
                }
            }
            
            assertNotNull(toPerform);
            
            toPerform.implement();
            
            File dump   = new File(getWorkDir(), getName() + ".out");
            
            Writer writer = new FileWriter(dump);
            
            Document modifDoc;
            if (className.equals(modifiedClassName)) {
                modifDoc = doc;
            } else {
                FileObject modFile = packageRoot.getFileObject(modifiedClassName + ".java");
                od = DataObject.find(modFile);
                ec = od.getCookie(EditorCookie.class);
                modifDoc = ec.openDocument();
            }
            
            writer.write(modifDoc.getText(0, modifDoc.getLength()));
            
            writer.close();
            
            File golden = getGoldenFile();
            
            assertNotNull(golden);
            
            File diff   = new File(getWorkDir(), getName() + ".diff");
            
            assertFile(dump, golden, diff, new WhitespaceIgnoringDiff());
        } finally {
            SourceUtilsTestUtil2.ignoreCompileRequests();
            LifecycleManager.getDefault().saveAll();
        }
    }
    
}
