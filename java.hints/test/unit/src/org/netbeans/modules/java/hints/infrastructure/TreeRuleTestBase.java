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

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * @author Jan Lahoda
 */
public abstract class TreeRuleTestBase extends NbTestCase {
    protected final Logger LOG;
    
    public TreeRuleTestBase(String name) {
        super(name);
        LOG = Logger.getLogger("test." + name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        workFO.refresh();
        
        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, extraClassPath());
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    
    protected abstract List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path);
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        return computeErrors(info, path);
    }
    
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.toString();
    }
    
    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        int[] offset = new int[1];
        
        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, offset);
        
        performAnalysisTest(fileName, code, offset[0], golden);
    }
    
    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<ErrorDescription> errors = computeErrors(info, path, pos);
        List<String> errorsNames = new LinkedList<String>();
        
        errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();
        
        for (ErrorDescription e : errors) {
            errorsNames.add(e.toString());
        }
        
        assertTrue(errorsNames.toString(), Arrays.equals(golden, errorsNames.toArray(new String[0])));
    }
    
    protected String performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        int[] offset = new int[1];

        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, offset);
        
        return performFixTest(fileName, code, offset[0], errorDescriptionToString, fixDebugString, golden);
    }
    
    protected String performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        return performFixTest(fileName, code, pos, errorDescriptionToString, fixDebugString, fileName, golden);
    }
    
    protected String performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String goldenFileName, String golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<ErrorDescription> errors = computeErrors(info, path, pos);
        
        ErrorDescription toFix = null;
        
        for (ErrorDescription d : errors) {
            if (errorDescriptionToString.equals(d.toString())) {
                toFix = d;
                break;
            }
        }
        
        assertNotNull("Error: \"" + errorDescriptionToString + "\" not found. All ErrorDescriptions: " + errors.toString(), toFix);
        
        assertTrue("Must be computed", toFix.getFixes().isComputed());
        
        List<Fix> fixes = toFix.getFixes().getFixes();
        List<String> fixNames = new LinkedList<String>();
        Fix toApply = null;
        
        for (Fix f : fixes) {
            if (fixDebugString.equals(toDebugString(info, f))) {
                toApply = f;
            }
            
            fixNames.add(toDebugString(info, f));
        }
        
        assertNotNull("Cannot find fix to invoke: " + fixNames.toString(), toApply);
        
        toApply.implement();
        
        FileObject toCheck = sourceRoot.getFileObject(goldenFileName);
        
        assertNotNull(toCheck);
        
        DataObject toCheckDO = DataObject.find(toCheck);
        EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
        Document toCheckDocument = ec.openDocument();
        
        String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());
        
        //ignore whitespaces:
        realCode = realCode.replaceAll("[ \t\n]+", " ");

        if (golden != null) {
            assertEquals(golden, realCode);
        }
        
        LifecycleManager.getDefault().saveAll();

        return realCode;
    }
    
    protected FileObject[] extraClassPath() {
        return new FileObject[0];
    }

    // common tests to check nothing is reported
    public void testIssue105979() throws Exception {
        String before = "package test; class Test {" +
            "  return b;" +
            "}\n";
        
        for (int i = 0; i < before.length(); i++) {
            LOG.info("testing position " + i + " at " + before.charAt(i));
            clearWorkDir();
            performAnalysisTest("test/Test.java", before, i);
        }
    }
    public void testIssue108246() throws Exception {
        
        String before = "package test; class Test {" +
            "  Integer ii = new Integer(0);" +
            "  String s = ii.toString();" +
            "\n}\n";
        
        for (int i = 0; i < before.length(); i++) {
            LOG.info("testing position " + i + " at " + before.charAt(i));
            clearWorkDir();
            performAnalysisTest("test/Test.java", before, i);
        }
    }

    public void testNoHintsForSimpleInitialize() throws Exception {
        
        String before = "package test; class Test {" +
            " { java.lang.System.out.println(); } " +
            "}\n";
        
        for (int i = 0; i < before.length(); i++) {
            LOG.info("testing position " + i + " at " + before.charAt(i));
            clearWorkDir();
            performAnalysisTest("test/Test.java", before, i);
        }
    }
    
    public void testIssue113933() throws Exception {
        
        String before = "package test; class Test {" +
            "  public void test() {" +
            "  super.A();" +
            "\n}\n}\n";
        
        for (int i = 0; i < before.length(); i++) {
            LOG.info("testing position " + i + " at " + before.charAt(i));
            clearWorkDir();
            performAnalysisTest("test/Test.java", before, i);
        }
    }
}
