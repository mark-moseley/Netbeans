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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.editor.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class DelegateMethodGeneratorTest extends NbTestCase {
    
    public DelegateMethodGeneratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

//    public void testFindUsableFields() throws Exception {
//        prepareTest("FindUsableFieldsTest");
//
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
//        TypeElement clazz = info.getElements().getTypeElement("test.FindUsableFieldsTest");
//
//        Map<? extends TypeElement, ? extends List<? extends VariableElement>> class2Variables = DelegatePanel.findUsableFields(info, clazz);
//
//        assertEquals(class2Variables.toString(), 1, class2Variables.size());
//
//        List<? extends VariableElement> variables = class2Variables.get(clazz);
//
//        assertEquals(2, variables.size());
//        assertTrue("s".contentEquals(variables.get(0).getSimpleName()));
//        assertTrue("l".contentEquals(variables.get(1).getSimpleName()));
//    }
//
//    public void testMethodProposals1() throws Exception {
//        prepareTest("MethodProposals");
//        
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
//        TypeElement clazz = info.getElements().getTypeElement("test.MethodProposals");
//        VariableElement variable = null;
//
//        for (VariableElement v : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
//            if ("a".contentEquals(v.getSimpleName())) {
//                variable = v;
//            }
//        }
//
//        assertNotNull(variable);
//
//        compareMethodProposals(DelegatePanel.getMethodProposals(info, clazz, variable));
//    }
//
//    public void testMethodProposals2() throws Exception {
//        prepareTest("MethodProposals");
//        
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
//        TypeElement clazz = info.getElements().getTypeElement("test.MethodProposals");
//        VariableElement variable = null;
//
//        for (VariableElement v : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
//            if ("l".contentEquals(v.getSimpleName())) {
//                variable = v;
//            }
//        }
//
//        assertNotNull(variable);
//
//        compareMethodProposals(DelegatePanel.getMethodProposals(info, clazz, variable));
//    }
//
//    public void testMethodProposals3() throws Exception {
//        prepareTest("MethodProposals");
//        
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
//        TypeElement clazz = info.getElements().getTypeElement("test.MethodProposals");
//        VariableElement variable = null;
//
//        for (VariableElement v : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
//            if ("s".contentEquals(v.getSimpleName())) {
//                variable = v;
//            }
//        }
//
//        assertNotNull(variable);
//
//        compareMethodProposals(DelegatePanel.getMethodProposals(info, clazz, variable));
//    }
//
//    private void compareMethodProposals(List<TypedExecutableElementWrapper> proposals) {
//        List<String> result = new ArrayList<String>();
//        
//        for (TypedExecutableElementWrapper ee : proposals) {
//            result.add(ee.toString());
//        }
//        
//        Collections.sort(result);
//        
//        for (String s : result) {
//            ref(s);
//        }
//        
//        compareReferenceFiles();
//    }

    private FileObject testSourceFO;
    private JavaSource source;
    
    private void copyToWorkDir(File resource, File toFile) throws IOException {
        //TODO: finally:
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        
        int read;
        
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        
        outs.close();
        
        is.close();
    }
    
    private void prepareTest(String fileName) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject cache   = scratch.createFolder("cache");
        
        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");
        
        testSource.getParentFile().mkdirs();
        
        File dataFolder = new File(getDataDir(), "org/netbeans/modules/java/editor/codegen/data/");
        
        for (File f : dataFolder.listFiles()) {
            copyToWorkDir(f, new File(wd, "test/" + f.getName()));
        }
        
        testSourceFO = FileUtil.toFileObject(testSource);
        
        assertNotNull(testSourceFO);
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();
        
        SourceUtilsTestUtil.prepareTest(FileUtil.toFileObject(dataFolder), FileUtil.toFileObject(testBuildTo), cache);
        SourceUtilsTestUtil.compileRecursively(FileUtil.toFileObject(dataFolder));
        
        source = JavaSource.forFileObject(testSourceFO);
    }
}
