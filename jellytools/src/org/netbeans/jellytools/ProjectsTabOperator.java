/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.actions.ProjectViewAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
//import org.openide.windows.TopComponent;

/** Operator handling Projects TopComponent.<p>
 * This operator is child of TopComponentOperator and thus it can be located
 * independently on Explorer (it can by found undocked or docked in other
 * NbFrame).<p>
 * Functionality related to Projects tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 * Example:<p>
 * <pre>
 *      ProjectsTabOperator tab = ProjectsTabOperator.invoke();
 *      // or when tab is already opened anywhere
 *      ProjectsTabOperator tab = new ProjectsTabOperator();
 *
 *      JTreeOperator tree = tab.tree();
 *      System.out.println(tab.getRootNode().getText());
 * </pre> */
public class ProjectsTabOperator extends TopComponentOperator {
    
    static final String PROJECT_CAPTION = Bundle.getStringTrimmed("org.netbeans.modules.projects.Bundle", "CTL_ProjectDesktop_name");
    private static final ProjectViewAction viewAction = new ProjectViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Projects TopComponent through all IDE. */    
    public ProjectsTabOperator() {
        this(null);
    }
    
    /** Search for Projects TopComponent inside given Container
     * @param contOper parent Container */    
    public ProjectsTabOperator(ContainerOperator contOper) {
        super(waitTopComponent(contOper, PROJECT_CAPTION, 0, new ProjectsTabSubchooser()));
        if(contOper != null) {
            copyEnvironment(contOper);
        }
    }

    /** invokes ProjectsTab and returns new instance of ProjectsTabOperator
     * @return new instance of ProjectsTabOperator */    
    public static ProjectsTabOperator invoke() {
        viewAction.perform();
        return new ProjectsTabOperator();
    }
    
    /** Getter for Projects JTreeOperator
     * @return JTreeOperator of Projects tree */    
    public JTreeOperator tree() {
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** getter for ProjectsRootNode
     * @return ProjectsRootNode */    
    public ProjectRootNode getRootNode() {
        return new ProjectRootNode(tree());
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.modules.projects.CurrentProjectNode$ProjectsTab
     * Used in constructor.
     */
    private static final class ProjectsTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("ProjectsTab");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.projects.CurrentProjectNode$ProjectsTab";
        }
    }
}
