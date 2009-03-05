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

package org.netbeans.modules.php.editor.nav;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public abstract class TestBase extends CslTestBase {
    
    public TestBase(String testName) {
        super(testName);
    }            

    private static final String FOLDER = "GsfPlugins";

    protected String preaperTestFile(String filePath) throws IOException {
        String retval = TestUtilities.copyFileToString(new File(getDataDir(), filePath));
        return retval;
    }

    protected String prepareTestFile(String filePath, String... texts) throws IOException {
        String retval = preaperTestFile(filePath);
        assert texts != null && texts.length%2 == 0;
        for (int i = 0; i+1 < texts.length; i++) {
            String originalText = texts[i];
            String replacement = texts[++i];
            retval = retval.replace(originalText, replacement);
        }
        return retval;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PHPIndex.setClusterUrl("file:/bogus"); // No translation
        FileObject f = FileUtil.getConfigFile(FOLDER + "/text/html");
        
        if (f != null) {
            f.delete();
        }
        
        FileUtil.setMIMEType("php", PHPLanguage.PHP_MIME_TYPE);
        Logger.global.setFilter(new Filter() {
            public boolean isLoggable(LogRecord record) {
                Throwable t = record.getThrown();
                
                if (t == null) {
                    return true;
                }
                
                for (StackTraceElement e : t.getStackTrace()) {
                    if (   "org.netbeans.modules.php.editor.index.GsfUtilities".equals(e.getClassName())
                        && "getBaseDocument".equals(e.getMethodName())
                        && t instanceof ClassNotFoundException) {
                        return false;
                    }
                }
                return false;
            }
        });
    }
    
    protected static String computeFileName(int index) {
        return "test" + (index == (-1) ? "" : (char) ('a' + index)) + ".php";
    }
    
    protected void performTest(String[] code, final UserTask task) throws Exception {
        clearWorkDir();
        FileUtil.refreshAll();

        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject folder = workDir.createFolder("src");
        int index = -1;
//
        for (String c : code) {
            FileObject f = FileUtil.createData(folder, computeFileName(index));
            TestUtilities.copyStringToFile(f, c);
            index++;
        }
//
        final FileObject test = folder.getFileObject("test.php");

        Source testSource = getTestSource(test);
        Future<Void> parseWhenScanFinished = ParserManager.parseWhenScanFinished(Collections.singleton(testSource), task);
        parseWhenScanFinished.get();
    }

    private static Document openDocument(FileObject fileObject) throws Exception {
        DataObject dobj = DataObject.find(fileObject);

        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        return ec.openDocument();
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new PHPLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return PHPLanguage.PHP_MIME_TYPE;
    }

    @Override
    protected final Map<String, ClassPath> createClassPathsForTest() {
        FileObject[] srcFolders = createSourceClassPathsForTest();
        return srcFolders != null ? Collections.singletonMap(
            PhpSourcePath.SOURCE_CP,
            ClassPathSupport.createClassPath(srcFolders)
        ) : null;
    }


    protected FileObject[] createSourceClassPathsForTest() {
        return null;
    }

    protected final FileObject[] createSourceClassPathsForTest(FileObject base, String relativePath) {
        try {
            return new FileObject[]{toFileObject(base, relativePath, true)};
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    protected final FileObject workDirToFileObject() throws IOException {
        FileObject workDir = null;
        assert getWorkDir().exists();
        workDir = FileUtil.toFileObject(getWorkDir());
        return workDir;
    }
    
    protected final FileObject toFileObject(FileObject base, String relativePath, boolean isFolder) throws IOException {
        FileObject retval = null;
        if (isFolder) {
            retval = FileUtil.createFolder(base, relativePath);
        } else {
            retval = FileUtil.createData(base, relativePath);
        }
        return retval;
    }

}
