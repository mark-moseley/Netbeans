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

package gui.menu;

import org.netbeans.performance.test.guitracker.ActionTracker;

import java.awt.event.KeyEvent;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on Source Editor pane.
 *
 * @author  mmirilovic@netbeans.org
 */
public class SourceEditorPopupMenu extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String stringToInvokePopup;
    private static boolean setCaretPositionAfterString;
    private static EditorOperator editor;
    private static String fileName;
    
    /** Creates a new instance of SourceEditorPopupMenu */
    public SourceEditorPopupMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    /** Creates a new instance of SourceEditorPopupMenu */
    public SourceEditorPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SourceEditorPopupMenu("testPopupInTxt"));
        suite.addTest(new SourceEditorPopupMenu("testPopupInXml"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnMethod"));
        suite.addTest(new SourceEditorPopupMenu("testPopupOnClassName"));
        return suite;
    }
    
    public void testPopupInTxt(){
        fileName = "textfile.txt";
        stringToInvokePopup = "***********";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupInXml(){
        fileName = "xmlfile.xml";
        stringToInvokePopup = "<root";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupOnMethod(){
        fileName = "Main.java";
        stringToInvokePopup = "javax.swing.JPa";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    public void testPopupOnClassName(){
        fileName = "Main.java";
        stringToInvokePopup = "class Mai";
        setCaretPositionAfterString = false;
        
        doMeasurement();
    }
    
    
    public void initialize(){
        Node fileNode = new Node(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"), gui.Utilities.SOURCE_PACKAGES + "|org.netbeans.test.performance|" + fileName);
        
        if (fileName.endsWith("xml")) {
            new EditAction().performAPI(fileNode);
        }
        else {
            new OpenAction().performAPI(fileNode);
        }
        editor = new EditorOperator(fileName);
        waitNoEvent(2000);  // annotations, folds, toolbars, ...
    }
    
    public void prepare(){
        editor.setCaretPosition(stringToInvokePopup,setCaretPositionAfterString);
    }
    
    public ComponentOperator open(){
        editor.pushKey(KeyEvent.VK_F10, KeyEvent.SHIFT_MASK);
        return new JPopupMenuOperator();
    }
    
    public void shutdown(){
        editor.closeDiscardAll();
    }
    
}
