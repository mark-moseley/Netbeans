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
package org.netbeans.modules.java.editor.overridden;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**XXX: tests for multi source root is overridden annotations are missing!
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotationCreatorTest extends NbTestCase {
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    private static File cache;
    private static FileObject cacheFO;
    
    public IsOverriddenAnnotationCreatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        
        if (cache == null) {
            cache = TestUtil.createWorkFolder();
            cacheFO = FileUtil.toFileObject(cache);
            
            cache.deleteOnExit();
        }
    }
    
    private void prepareTest(String capitalizedName) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
//        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = FileUtil.createFolder(sourceRoot, "org/netbeans/modules/editor/java");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        String testPackagePath = "org/netbeans/modules/editor/java/";
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
        
        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        testSource = packageRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    //does not work as recursive lookup does not work:
//    public void testExtendsList() throws Exception {
//        doTest("TestExtendsList");
//    }
    
    public void testOverrides() throws Exception {
        doTest("TestOverrides");
    }
    
    //does not work as recursive lookup does not work:
//    public void testInterface() throws Exception {
//        doTest("TestInterface");
//    }
    
    public void testInterfaceImplOverride() throws Exception {
        doTest("TestInterfaceImplOverride");
    }
    
    //the "is overridden" part is currently disabled:
//    public void testInterfaceImpl() throws Exception {
//        doTest("TestInterfaceImpl");
//    }
//    
//    public void testInterface2() throws Exception {
//        doTest("TestInterface2");
//    }
    
    public void testHierarchy1() throws Exception {
        doTest("TestHierarchy1");
    }
    
    public void testHierarchy2() throws Exception {
        doTest("TestHierarchy2");
    }
    
    public void testBrokenSource() throws Exception {
        doTest("TestBrokenSource");
    }
    
    private void doTest(String name) throws Exception {
        prepareTest(name);
        
        DataObject testDO = DataObject.find(testSource);
        EditorCookie ec = testDO.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        StyledDocument doc = ec.openDocument();
        
        List<IsOverriddenAnnotation> annotations = new IsOverriddenAnnotationHandler(testSource).process(info, doc);
        List<String> result = new ArrayList<String>();
        
        for (IsOverriddenAnnotation annotation : annotations) {
            result.add(annotation.debugDump());
        }
        
        Collections.sort(result);
        
        for (String r : result) {
            ref(r);
        }
        
        compareReferenceFiles();
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    @SuppressWarnings("deprecation")
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
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
}
