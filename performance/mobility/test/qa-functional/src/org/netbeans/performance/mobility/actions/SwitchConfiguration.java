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
package org.netbeans.performance.mobility.actions;

import javax.swing.JComponent;
import org.netbeans.performance.mobility.MPUtilities;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class SwitchConfiguration extends PerformanceTestCase {

    private Node openNode;
    private ProjectRootNode projectNode;
    private String targetProject,  midletName;
    private WizardOperator propertiesWindow;
    private MIDletEditorOperator editor;
    private LoggingRepaintManager.RegionFilter filter;

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */
    public SwitchConfiguration(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SwitchConfiguration(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    @Override
    public void initialize() {
        log(":: initialize");
        targetProject = "MobileApplicationSwitchConfiguration";
        midletName = "Midlet.java";
        EditorOperator.closeDiscardAll();
    }

    public void prepare() {
        log(":: prepare");
        String documentPath = MPUtilities.SOURCE_PACKAGES + "|" + "switchit" + "|" + midletName;
        projectNode = new ProjectsTabOperator().getProjectRootNode(targetProject);
        openNode = new Node(projectNode, documentPath);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }

        new OpenAction().perform(openNode);
        editor = MIDletEditorOperator.findMIDletEditorOperator(midletName);

        projectNode.properties();
        propertiesWindow = new WizardOperator(targetProject);
        
        filter = new LoggingRepaintManager.RegionFilter() {
            boolean done = false;

            public boolean accept(JComponent comp) {
                if (done) { 
                    return false;
                }
                if (comp.getClass().getName().equals("org.netbeans.modules.editor.errorstripe.AnnotationView")) {
                    done = true;
                    return false;
                }
                return true;
            }

            public String getFilterName() {
                return "Filters out all Regions starting with org.netbeans.modules.editor.errorstripe.AnnotationView";
            }
        };
        repaintManager().addRegionFilter(filter);
    }

    public ComponentOperator open() {
        log(":: open");
        JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow);
        combo.selectItem(1); // NotDefaultConfiguration

        propertiesWindow.ok();
        return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    @Override
    public void close() {
        log(":: close");
        repaintManager().removeRegionFilter(filter);
        if (projectNode != null) {
            projectNode.properties();
            propertiesWindow = new WizardOperator(targetProject);

            // switch back to default config
            JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow, 0);
            combo.selectItem(0); //DefaultConfiguration

            propertiesWindow.ok();
        }
    }

    @Override
    public void shutdown() {
        log("::shutdown");
        if (editor != null) {
            editor.close();
        }
    }

//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new SwitchConfiguration("measureTime"));
//    }
}
