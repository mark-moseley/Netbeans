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

package gui.action;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class OpenBeanFiles  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String beanName;
    private String beanFileName;
    private String beanPath;
    /** Node to be opened/edited */
    public static Node openNode ;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    /** Creates a new instance of OpenBeanFiles */
    public OpenBeanFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        WAIT_AFTER_PREPARE=3000;
    }
    
    public OpenBeanFiles(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        WAIT_AFTER_PREPARE=3000;        
    }
    
    public void testApplicationBean() {
        beanName = "ApplicationBean1.java"; //NOI18N
        beanFileName = "ApplicationBean1.java";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void testRequestBean() {
        beanName = "RequestBean1.java"; //NOI18N
        beanFileName = "RequestBean1.java";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void testSessionBean() {
        beanName = "SessionBean1.java"; //NOI18N
        beanFileName = "SessionBean1.java";
        setJavaEditorCaretFilteringOn();
        doMeasurement();
    }
    
    public void initialize() {
        log("::initialize");
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }
    
    public void prepare() {
        log("::prepare");
        beanPath = "Source Packages"+"|"+"visualwebproject"+"|"+beanName;
        log("Bean Path = "+beanPath);
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        try {
            this.openNode = new Node(projectRoot, beanPath);
            this.openNode.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select bean node"); 
        }
        
        if (this.openNode == null) {
            throw new Error("Cannot find node "+beanPath);
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open() {
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node "+beanPath);
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item Open on node "+beanPath);
        }
        log("------------------------- after open ------------");
        
        return new EditorOperator(beanFileName);
    }
    
    public void close() {
        log(":: close");
        if(testedComponentOperator != null)
        {
            ((EditorOperator)testedComponentOperator).close();
            testedComponentOperator = null;
        }
        
    }
    
    protected void shutdown(){
        log(":: shutdown");
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
}
