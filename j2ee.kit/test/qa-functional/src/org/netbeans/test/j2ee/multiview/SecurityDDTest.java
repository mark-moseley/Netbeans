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
 *
 */

package org.netbeans.test.j2ee.multiview;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
//import org.netbeans.modules.j2ee.dd.impl.web.model_2_5.SecurityConstraint;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.ErrorPagesTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRoleTableModel;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.CommonBean;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Node;
/**
 *
 * @author kolard
 */
public class SecurityDDTest extends JellyTestCase {
    
    private static DDTestUtils utils;
    
    /** Creates a new instance of SecurityDDTest */
    public SecurityDDTest(String testName) {
        super(testName);
    }
    
        protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    private static Project project;
    private static FileObject ddFo;
    private static WebApp webapp;
    private static DDDataObject ddObj;
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SecurityDDTest("testOpenProject"));
        suite.addTest(new SecurityDDTest("testExistingLoginConfiguration"));
        suite.addTest(new SecurityDDTest("testExistingSecurityRoles"));
        suite.addTest(new SecurityDDTest("testExistingSecurityConstraint"));
        suite.addTest(new SecurityDDTest("testAddSecurityRole"));
        suite.addTest(new SecurityDDTest("testEditSecurityRole"));
        suite.addTest(new SecurityDDTest("testDelSecurityRole"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run only selected test case
        TestRunner.run(suite());
    }
    
    
    public void testOpenProject() throws Exception{
        File projectDir = new File(getDataDir(), "projects/TestWebApp");
        Project project = (Project)J2eeProjectSupport.openProject(projectDir);
        assertNotNull("Project is null.", project);
        WebProject webproj = (WebProject)project;
        assertNotNull("Project is not webproject",webproj);
        ddFo = webproj.getAPIWebModule().getDeploymentDescriptor();
        assertNotNull("Can't get deploy descriptor file object",ddFo);
        webapp = DDProvider.getDefault().getDDRoot(ddFo);
        ddObj = (DDDataObject)DataObject.find(ddFo);
        assertNotNull("Multiview is null",ddObj);
        ddObj.openView(5);//lets open security view
        utils = new DDTestUtils(ddObj,this);
        Utils.waitForAWTDispatchThread();
    }
    public void testExistingLoginConfiguration() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("login_config");
        Component[] comp = panel.getComponents();
        /*
        for(int i=0;i<comp.length;i++)
        {
            System.err.println("comp:" + (comp[i]));
        }
        */
        assertEquals("Login authentication isn't set to form","Form",((JRadioButton)comp[12]).getText());
        assertEquals("Undefined login page","/login.jsp",webapp.getSingleLoginConfig().getFormLoginConfig().getFormLoginPage());
        assertEquals("Undefined error page","/loginError.jsp",webapp.getSingleLoginConfig().getFormLoginConfig().getFormErrorPage());
        
    }
    
    public void testExistingSecurityRoles() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        assertEquals("Wrong number of roles",2,model.getRowCount());
        assertEquals("Wrong role name","admin",model.getValueAt(0,0));
        assertEquals("Wrong role description","administrator",model.getValueAt(0,1));
        assertEquals("Wrong role name","user",model.getValueAt(1,0));
        assertEquals("Wrong role description","testuser",model.getValueAt(1,1));
         
    }

     public void testAddSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.addRow(new Object[]{"user1","user1desc"});
        ddObj.modelUpdatedFromUI();
        utils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not added",3,model.getRowCount());
        ((Component)comp[0]).requestFocus();
        new StepIterator() {
            public boolean step() throws Exception {
                return utils.contains(".*<security-role>\\s*<description>user1desc</description>\\s*<role-name>user1</role-name>\\s*</security-role>.*");
            }            
            public void finalCheck() {
                utils.checkInDDXML(".*<security-role>\\s*<description>user1desc</description>\\s*<role-name>user1</role-name>\\s*</security-role>.*");
            }
        };         
    }
     public void testEditSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.editRow(2,new Object[]{"user2","user2desc"});
        ddObj.modelUpdatedFromUI();
        utils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not changed","user2",model.getValueAt(2,0));
        assertEquals("Role description not changed","user2desc",model.getValueAt(2,1));
        ((Component)comp[0]).requestFocus();
        new StepIterator() {
            public boolean step() throws Exception {
                return utils.contains(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");
            }            
            public void finalCheck() {
                utils.checkInDDXML(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");
            }
        };         
    }

     public void testDelSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.removeRow(2);
        ddObj.modelUpdatedFromUI();
        utils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not deleted",2,model.getRowCount());
        utils.checkNotInDDXML(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");
        
    }
    
    public void testExistingSecurityConstraint() throws Exception {
        //empty yet...
        //SecurityConstraint constraint = webapp.getSecurityConstraint(0);
        //org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRoleTablePanel pan = new org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRoleTablePanel();            
    }
}
