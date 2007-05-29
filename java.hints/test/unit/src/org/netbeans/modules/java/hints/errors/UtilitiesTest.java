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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class UtilitiesTest extends NbTestCase {
    
    public UtilitiesTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testNameGuess1() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {toString();}}", 54, "toString");
    }

    public void testNameGuess2() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getX();} public int getX() {return 0;}}", 54, "x");
    }
    
    public void testNameGuess3() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getData();} public int getData() {return 0;}}", 54, "data");
    }
    
    public void testNameGuess4() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getProcessedData();} public int getProcessedData() {return 0;}}", 54, "processedData");
    }
    
    public void testNameGuess5() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {isEnabled();} public boolean isEnabled() {return true;}}", 54, "enabled");
    }
    
    public void testNameGuess6() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {get();} public int get() {return 0;}}", 52, "get");
    }
    
    public void testNameGuessKeyword() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getDo();} public int getDo() {return 0;}}", 52, "aDo");
    }
    
    public void testNameGuessKeywordTypeMirror() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getDo();} public int getDo() {return 0;}}", 52, "aDo");
    }
    
    protected void prepareTest(String code) throws Exception {
        clearWorkDir();
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    
    private void performNameGuessTest(String code, int position, String desiredName) throws Exception {
        prepareTest(code);
        
        TreePath tp = info.getTreeUtilities().pathFor(position);
        
        String name = Utilities.guessName(info, tp);
        
        assertEquals(desiredName, name);
    }
    
}
