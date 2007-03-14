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

package org.netbeans.test.editor.highlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import junit.textui.TestRunner;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.test.editor.LineDiff;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * Tests for editor highlighting
 * @author Jiri Prox
 */
public class SyntaxHighlightTest extends EditorTestCase{
    
    /** Creates a new instance of SyntaxHighlightTest */
    public SyntaxHighlightTest(String name) {
	super(name);
	curPackage = getClass().getPackage().getName();
    }
    
    private boolean generateGoldenFiles = true;
    
    private String curPackage;
    
    private String testClass;
    
    protected EditorOperator oper;
    
    
    public static NbTestSuite suite() {
	NbTestSuite suite = new NbTestSuite(SyntaxHighlightTest.class);
	return suite;
    }
    
    public File getGoldenFile() {
	String fileName = "goldenfiles/"+curPackage.replace('.', '/')+ "/" + testClass + ".pass";
	File f = new java.io.File(getDataDir(),fileName);
	if(!f.exists()) fail("Golden file "+f.getAbsolutePath()+ " does not exist");
	return f;
    }
    
    public File getNewGoldenFile() {
	String fileName = "data/goldenfiles/"+curPackage.replace('.', '/')+ "/" + testClass + ".pass";
	File f = new File(getDataDir().getParentFile().getParentFile().getParentFile(),fileName);
	f.getParentFile().mkdirs();
	return f;
    }
    
    public void compareGoldenFile() throws IOException {
	File fGolden = null;
	if(!generateGoldenFiles) {
	    fGolden = getGoldenFile();
	} else {
	    fGolden = getNewGoldenFile();
	}
	String refFileName = getName()+".ref";
	String diffFileName = getName()+".diff";
	File fRef = new File(getWorkDir(),refFileName);
	//FileWriter fw = new FileWriter(fRef);
	//fw.write(oper.getText());
	//fw.close();
	LineDiff diff = new LineDiff(false);
	if(!generateGoldenFiles) {
	    File fDiff = new File(getWorkDir(),diffFileName);
	    if(diff.diff(fGolden, fRef, fDiff)) fail("Golden files differ");
	} else {
	    FileWriter fwgolden = new FileWriter(fGolden);
	    BufferedReader br = new BufferedReader(new FileReader(fRef));
	    String line;
	    while((line=br.readLine())!=null) {
		fwgolden.write(line+"\n");
	    }
	    fwgolden.close();
	    fail("Golden file generated");
	}
    }
    
    /**
     * Check default settings 
     */ 
    public void testColor() throws DataObjectNotFoundException, IOException, InterruptedException, InvocationTargetException, BadLocationException {
	checkCurrentColorSettings();
    }
    
    /**
     * Change java comment settings and verify changes took effect
     * @throws java.io.IOException problems with accessing goldenfile
     * This does not work properly due to:
     * http://www.netbeans.org/issues/show_bug.cgi?id=93969
     */ 
    public void testCommentColor() throws IOException {
	OptionsOperator odop = OptionsOperator.invoke();
	odop.selectFontAndColors();
	JTabbedPaneOperator jtpo = new JTabbedPaneOperator(odop, "Syntax");
	jtpo.selectPage(0);
	JListOperator jlo = new JListOperator(jtpo, 0);
	jlo.selectItem(2); //select 'Comment'
	JComboBoxOperator jcbo = new JComboBoxOperator(jtpo, 1); //change FG
	jcbo.selectItem(2); //select 'cyan'
	JButtonOperator okOperator = new JButtonOperator(odop, "OK");
	okOperator.push(); //confirm OD
	new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
	checkCurrentColorSettings();
    }
    
    /**
     * Creates/compares goldenfile with all current highlighting settings
     * The name of the testedfile & goldenfile depends on test method
     * from which this method is called from
     */ 
    private void checkCurrentColorSettings() throws DataObjectNotFoundException, IOException {
	String path  = "/projects/editor_test/src/"+curPackage.replace('.','/')+"/"+testClass+".java";
	//System.out.println(path);
	File testFile = new File(getDataDir(),path);
	FileObject fo = FileUtil.toFileObject(testFile);
	DataObject d = DataObject.find(fo);
	final EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
	ec.open();
	StyledDocument doc = ec.openDocument();
	SyntaxHighlighting layer = new SyntaxHighlighting(doc);
	HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
	while(hs.moveNext()) {
	    AttributeSet as  = hs.getAttributes();
	    Enumeration en = as.getAttributeNames();//produces elements in random order!
	    getRef().println(hs.getStartOffset()+ " "+hs.getEndOffset()  /* +" "+doc.getText(hs.getStartOffset(),hs.getEndOffset()-hs.getStartOffset()) */);
	    //            getRef().println(as);
	    ArrayList<String> tmpEnumContent = new ArrayList<String>();
	    while(en.hasMoreElements()) {
		Object s = en.nextElement();
		tmpEnumContent.add("    "+s+" "+as.getAttribute(s));
	    }
	    Collections.sort(tmpEnumContent); //sort the output
	    Iterator<String> it = tmpEnumContent.iterator();
	    while(it.hasNext()) {
		String s = it.next();
		getRef().println(s);
	    }
	}
    }
    
    protected void setUp() throws Exception {
	super.setUp();
	openDefaultProject();
	//sets the testClass name to current test name (ie. for proper goldenfile)
	testClass = getName();
	openSourceFile(curPackage, testClass);
	oper =  new EditorOperator(testClass);
    }
    
    protected void tearDown() throws Exception {
	compareGoldenFile();
	super.tearDown();
    }
    
    public static void main(String[] args) {
	TestRunner.run(new NbTestSuite(SyntaxHighlightTest.class));
    }
    
}
