/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor.completion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.Boolean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.framework.Assert;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.completion.CompletionImpl;
import org.netbeans.modules.editor.completion.CompletionItemComparator;
import org.netbeans.modules.editor.completion.CompletionResultSetImpl;
import org.netbeans.modules.editor.java.JavaCompletionProvider;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaDataObject.JavaEditorSupport;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Dusan Balek
 */
public class CompletionTestBase extends NbTestCase {
    
    static {
        JavaCompletionProviderBasicTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
    }

    static final int FINISH_OUTTIME = 5 * 60 * 1000;
    
    public static class Lkp extends ProxyLookup {
        
        private static Lkp DEFAULT;
        
        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
        }
        
        public static void initLookups(Object[] objs) throws Exception {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup [] {
                Lookups.fixed(objs),
                Lookups.metaInfServices(l),
                Lookups.singleton(l)
            });
        }
    }
    
    public CompletionTestBase(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(new URL[] {
            JavaCompletionProviderBasicTest.class.getResource("/org/netbeans/modules/java/editor/resources/layer.xml"),
            JavaCompletionProviderBasicTest.class.getResource("/org/netbeans/modules/defaults/mf-layer.xml")
        });
        Repository repository = new Repository(system);
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
        final ClassPath sourcePath = ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
        final ClassIndexManager mgr  = ClassIndexManager.getDefault();
        for (ClassPath.Entry entry : sourcePath.entries()) {
            mgr.createUsagesQuery(entry.getURL(), true);
        }
        final ClassPath bootPath = createClassPath(System.getProperty("sun.boot.class.path"));
        for (ClassPath.Entry entry : bootPath.entries()) {
            URL url = entry.getURL();
            ClassIndexImpl cii = mgr.createUsagesQuery(url, false);
            if ("jar".equals(url.getProtocol())) {
                url = FileUtil.getArchiveFile(url);
            }
            File root = new File (URI.create(url.toExternalForm()));
            cii.getBinaryAnalyser().analyse(root, null);
            
        }                
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type == ClassPath.SOURCE)
                    return sourcePath;
                    if (type == ClassPath.COMPILE)
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type == ClassPath.BOOT)
                        return bootPath;
                    return null;
            }
        };                               
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        Lkp.initLookups(new Object[] {repository, loader, cpp});
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        Utilities.setCaseSensitive(true);
    }
    
    protected void tearDown() throws Exception {
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String goldenFileName) throws Exception {
        File testSource = new File(getWorkDir(), "test/Test.java");
        testSource.getParentFile().mkdirs();
        copyToWorkDir(new File(getDataDir(), "org/netbeans/modules/java/editor/completion/data/" + source + ".java"), testSource);
        FileObject testSourceFO = FileUtil.toFileObject(testSource);
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        doc.putProperty(LanguageDescription.class, JavaTokenId.language());
        JEditorPane editor = new JEditorPane();
        editor.setDocument(doc);
        int textToInsertLength = textToInsert != null ? textToInsert.length() : 0;
        if (textToInsertLength > 0)
            doc.insertString(caretPos, textToInsert, null);
        editor.setCaretPosition(caretPos + textToInsertLength);
        
        JavaCompletionProvider provider = new JavaCompletionProvider();
        int queryType = CompletionProvider.COMPLETION_QUERY_TYPE;
        final CompletionTask task = provider.createTask(queryType, editor);
        CompletionImpl ci = CompletionImpl.get();
        final CompletionResultSetImpl result = ci.createTestResultSet(task, queryType);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                task.query(result.getResultSet());
            }
        });
        long start = System.currentTimeMillis();
        while(!result.isFinished() && start + FINISH_OUTTIME > System.currentTimeMillis())
            Thread.sleep(100);
        if (!result.isFinished())
            fail("Result not finished in given time (" + FINISH_OUTTIME + ")");
        List items = result.getItems();
        Collections.sort(items, CompletionItemComparator.BY_PRIORITY);
        
        File output = new File(getWorkDir(), getName() + ".out");
        Writer out = new FileWriter(output);
        for (Object item : items) {
            out.write(item.toString());
            out.write("\n");
        }
        out.close();
        
        File goldenFile = new File(getDataDir(), "/goldenfiles/org/netbeans/modules/java/editor/completion/JavaCompletionProviderTest/" + goldenFileName);
        File diffFile = new File(getWorkDir(), getName() + ".diff");
        
        assertFile(output, goldenFile, diffFile);
        
        JavaEditorSupport s = (JavaEditorSupport) ec;
        
        SourceUtilsTestUtil2.ignoreCompileRequests();
        
        s.close(false);
    }
    
    private void copyToWorkDir(File resource, File toFile) throws IOException {
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }
    
    private static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List/*<PathResourceImplementation>*/ list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url!=null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }
    
    // XXX this method could probably be removed... use standard FileUtil stuff
    private static URL getRootURL  (File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            } else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl+"/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }
    
    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }    
}
