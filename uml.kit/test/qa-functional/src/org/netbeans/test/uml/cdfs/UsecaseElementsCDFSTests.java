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



package org.netbeans.test.uml.cdfs;

import org.netbeans.test.uml.cdfs.utils.CDFSUtil;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class UsecaseElementsCDFSTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    
    public UsecaseElementsCDFSTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(UsecaseElementsCDFSTests.class);
        return suite;
    }
    
    
    /******************const section*************************/
    private String PROJECT_NAME = "CDFS_uml";
    private String EXCEPTION_DLG = "Exception";
    /********************************************************/
    
    private static boolean isNotInitialized = true;
    CDFSUtil util = new CDFSUtil(PROJECT_NAME);
    
    
    
    public void testCDFSUsecase(){
        final String PATH_TO_OBJ = "Model|AnotherCase";
        final String OBJ_NAME = "AnotherCase";
        final String DIA_NAME = "Usd_Uscse";
        final String DIA_PATH = "Model|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.USECASE_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSUsecase verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCDFSArtifact verification failed");
            }
             */
        
    }
    
    
    protected void setUp() {
        if (isNotInitialized){
            Project.openProject(CDFSUtil.CDFS_XTEST_PROJECT_DIR+"/"+PROJECT_NAME);
            isNotInitialized=false;
        }
    }
    
    
    public void tearDown() {
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.tearDown();        
        new EventTool().waitNoEvent(1000);
        try {
            DiagramOperator d=new DiagramOperator("Usd");
            d.closeAllDocuments();
        } catch (TimeoutExpiredException tee){}
        
        new EventTool().waitNoEvent(1000);
    }

    
}
