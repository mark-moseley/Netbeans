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

package validation;
import java.awt.Point;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;

/**
 */
public class SampleModuleValidation extends JellyTestCase {
    
    /** Need to be defined because of JUnit */
    public SampleModuleValidation(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SampleModuleValidation("testT1"));
        suite.addTest(new SampleModuleValidation("testT2"));
        suite.addTest(new SampleModuleValidation("testT3"));
        suite.addTest(new SampleModuleValidation("testT4"));
        suite.addTest(new SampleModuleValidation("testT5"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        //junit.textui.TestRunner.run(suite());
        // run only selected test case
        junit.textui.TestRunner.run(new SampleModuleValidation("testT3"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    public void tearDown() {
    }
    
    public void testT1() {
        JFrameOperator frame = new JFrameOperator();
        Point p = frame.getLocation();
        frame.setLocation(p.x+35, p.y+35);
    }
    
    public void testT2() {
        JFrameOperator frame = new JFrameOperator();
        Point p = frame.getLocation();
        frame.setLocation(p.x-35, p.y-35);
    }
    
    public void testT3() throws Exception {
        new ActionNoBlock("Help|About", null).perform();
        Thread.sleep(1000);
        new NbDialogOperator("About").close();
    }
    
    
    public void testT4() {
        TopComponentOperator tco = new TopComponentOperator("Projects");
        //String name = ((org.openide.windows.TopComponent)tco.getSource()).getDisplayName();
        //System.out.println("NAME="+name);
    }
    
    public void testT5() {
        throw new JemmyException("Failed because of ...");
    }
    
}
