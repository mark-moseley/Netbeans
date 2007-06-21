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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jaroslav Tulach
 */
public class HideFieldByVarTest extends TreeRuleTestBase {
    
    public HideFieldByVarTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }
    
    
    public void testDoesNotHideItself() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int va";
        String after = "lue = -1;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    public void testLocaVarAgainsInstanceVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "  private int compute() {" +
            "    int va";
        String after = "lue = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:82-0:87:verifier:Local variable hides a field"
        );
    }
    public void testLocaVarInStaticMethod() throws Exception {
        String text = "package test; class Test {" +
            "  protected  int value;" +
            "  private static int compute() {" +
            "    int value = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        for (int i = 0; i < text.length(); i++) {
            clearWorkDir();
            performAnalysisTest("test/Test.java", "// index: " + i + "\n" + text, i);
        }
    }
    public void testLocaVarAgainsInhVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private int compute() {" +
            "    int va";
        String after = "lue = -1;" +
            "    return 10;" +
            "  }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:109-0:114:verifier:Local variable hides a field"
        );
    }
    public void testParamIsOkAgainstInhVar() throws Exception {
        String before = "package test; class Test {" +
            "  protected  int value;" +
            "}" +
            "class Test2 extends Test {" +
            "  private void compute(int val";
        String after =         "ue) {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new HideFieldByVar().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
