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

package prepare;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.Operator;

import org.netbeans.junit.NbTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened NB plug-in.
 * Open 3 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author Marian.Mirilovic@sun.com
 */
public class PrepareIDEForPluginComplexMeasurements extends PrepareIDEForComplexMeasurements {
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForPluginComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForComplexMeasurements("closeWelcome"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeAllDocuments"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForPluginComplexMeasurements("openFiles"));
        suite.addTest(new PrepareIDEForComplexMeasurements("saveStatus"));
        return suite;
    }

    
    /**
     * Open 3 selected files from jEdit project.
     */
    public void openFiles(){
        
        try {
            String[][] files_path = {
                {"org.myorg.systemproperties","AllPropsChildren.java"},
                {"org.myorg.systemproperties","AllPropsNode.java"},
                {"org.myorg.systemproperties","OnePropNode.java"},
                {"org.myorg.systemproperties","PropertiesNotifier.java"},
                {"org.myorg.systemproperties","RefreshPropsAction.java"}
            };
            
            Node[] openFileNodes = new Node[files_path.length];
            Node node;
            
            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("SystemProperties");
            projectNode.expand();
            
            SourcePackagesNode sourceNode = new SourcePackagesNode(projectNode);
            sourceNode.expand();
            
            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            sourceNode.setComparator(comparator);
            
            for(int i=0; i<files_path.length; i++) {
                node = new Node(sourceNode,files_path[i][0]);
                node.expand();
                
                openFileNodes[i] = new Node(node,files_path[i][1]);
                
                //try to avoid issue 56825
                openFileNodes[i].select();
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }
            
            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes);
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes);
                err.println("issue 56825 : Success");
            }
            
            
            // check whether files are opened in editor
            for(int i=0; i<files_path.length; i++) {
                new EditorOperator(files_path[i][1]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
            
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
}
