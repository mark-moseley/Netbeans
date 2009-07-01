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

package org.netbeans.performance.j2ee.startup;

import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;


/**
 * Prepare user directory for measurement of startup time of IDE with opened files.
 * Open 10 java files and shut down ide. 
 * Created user directory will be used to measure startup time of IDE with opened files. 
 *
 * @author Marian.Mirilovic@sun.com
 */
public class MeasureJ2EEStartupTimeOpenedFilesPrepare extends JellyTestCase {
    
    public static final String suiteName="J2EE Startup suite";        
    
    /** Define testcase
     * @param testName name of the testcase
     */    
    public MeasureJ2EEStartupTimeOpenedFilesPrepare(String testName) {
        super(testName);
    }

   
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  ########");
    }
    
    public void testOpenProjects() {
 /*       ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp/TestStartupApp-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp/TestStartupApp-war");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupEJB1");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupEJB2");
        ProjectSupport.waitScanFinished();
        //waitForScan();*/
    }
    
    private void waitForScan() {
        // "Scanning Project Classpaths"
        String titleScanning = Bundle.getString("org.netbeans.modules.javacore.Bundle", "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed();
    }  
    
    /** 
     * Open 10 selected files from jEdit project. 
     */
    public void openFiles(){
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(10000);
        
        String[][] files_path = { 
            {"TestStartupApp","Configuration Files|sun-application.xml"},
            {"TestStartupApp-EJB","Enterprise Beans|TestSessionSB"},
            {"TestStartupApp-EJB","Configuration Files|ejb-jar.xml"},
            {"TestStartupApp-EJB","Configuration Files|sun-ejb-jar.xml"},
            {"TestStartupApp-WAR","Web Pages|index.jsp"},
            {"TestStartupApp-WAR","Configuration Files|web.xml"}, 
            {"TestStartupApp-WAR","Configuration Files|sun-web.xml"},
            {"TestStartupApp-WAR","Source Packages|test|TestServlet.java"},
            {"TestStartupEJB1","Enterprise Beans|TestSession2SB"},
            {"TestStartupEJB1","Enterprise Beans|TestMessageMDB"},
            {"TestStartupEJB1","Enterprise Beans|TestEntityEB"},
//  uncomment when Web Services node becomes fixed
//            {"TestStartupEJB2","Web Services|TestWebService1"},
//            {"TestStartupEJB2","Web Services|TestWebService2"}
        };
        
        Node[] openFileNodes = new Node[files_path.length];
        
        for(int i=0; i<files_path.length; i++) {
                Node root = new ProjectsTabOperator().getProjectRootNode(files_path[i][0]);
                root.setComparator(new Operator.DefaultStringComparator(true, true));
                openFileNodes[i] = new Node(root, files_path[i][1]);
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder, 
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
        
    }
   


}
