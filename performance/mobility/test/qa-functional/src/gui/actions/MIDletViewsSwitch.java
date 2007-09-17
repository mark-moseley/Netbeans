/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package gui.actions;

import gui.MPUtilities;
import gui.window.MIDletEditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.performance.test.guitracker.ActionTracker;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MIDletViewsSwitch extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    public String fromView;
    public String toView;
    
    private String targetProject;
    private String midletName;
    private Node openNode;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    private MIDletEditorOperator targetMIDletEditor;
    private static ActionTracker tr;
    
    public MIDletViewsSwitch(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_CLOSE = 5000;
    }
    public MIDletViewsSwitch(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 10000;
        WAIT_AFTER_CLOSE = 5000;        
    }
    public void initialize() {
        log(":: initialize");
        tr = ActionTracker.getInstance();
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java"; 
        
        String documentPath = MPUtilities.SOURCE_PACKAGES+"|"+"allComponents"+"|"+midletName;
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(targetProject), documentPath);
        
        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select(); 
        
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        log("------------------------- after popup invocation ------------");
        popup.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout", 90000);
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        } 
        targetMIDletEditor = MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }
    public void prepare() {
        log(":: prepare");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:PREPARE");
        targetMIDletEditor.switchToViewByName(fromView);
        
    }

    public ComponentOperator open() {
        log(":: open");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:OPEN");
        targetMIDletEditor.switchToViewByName(toView);
        return null;
    }
    public void close() {
        log(":: close");
        tr.add(ActionTracker.TRACK_TRACE_MESSAGE, "TEST:CLOSE");
    }
    public void shutdown() {
        log(":: shutdown");
        targetMIDletEditor.closeDiscard();
    }
    public void testFlowToDesignSwitch() {
        fromView = "Flow";
        toView = "Screen";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void testDesignToFlowSwitch() {
        fromView = "Screen";
        toView = "Flow";        
        setJavaEditorCaretFilteringOn();
        doMeasurement();        
    }
    
    public void testFlowToSourceSwitch() {
        fromView = "Flow";
        toView = "Source";        
        setJavaEditorCaretFilteringOn();
        doMeasurement();        
    }
    
    public void testSourceToFlowSwitch() {
        fromView = "Source";
        toView = "Flow";        
        setJavaEditorCaretFilteringOn();
        doMeasurement();        
        
    }
}
