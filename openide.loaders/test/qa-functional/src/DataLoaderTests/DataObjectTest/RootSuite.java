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

package DataLoaderTests.DataObjectTest;

import junit.framework.*;
import org.netbeans.junit.*;

public class RootSuite extends NbTestCase {

    public RootSuite(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private DataObjectTest T = null;

    protected void setUp(){
        T = new DataObjectTest("Dummy");
        //now setting workdir - this class will write nothing into the logs, only the utility class should,
        //however into location for this class        
        T.work = Manager.getWorkDirPath()+
        java.io.File.separator + this.getClass().getName().replace('.',java.io.File.separatorChar)+
        java.io.File.separator + getName();
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite("RootSuite");

/*        suite.addTest(DataLoaderTests.DataObjectTest.others.AWTFormObject.AWTFormObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.ClassObject.ClassObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.HTMLObject.HTMLObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.ImageObject.ImageObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.JSPObject.JSPObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.JavaSourceObject.JavaSourceObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.Package.Package_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.SecurityJApplet.SecurityJApplet_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.SwingFormObject.SwingFormObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.TextualObject.TextualObject_others.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.others.URLObject.URLObject_others.suite());
*/
/*        suite.addTest(DataLoaderTests.DataObjectTest.modify.AWTFormObject.AWTFormObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.ClassObject.ClassObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.HTMLObject.HTMLObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.ImageObject.ImageObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.JSPObject.JSPObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.JavaSourceObject.JavaSourceObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.Package.Package_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.SecurityJApplet.SecurityJApplet_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.SwingFormObject.SwingFormObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.TextualObject.TextualObject_modify.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.modify.URLObject.URLObject_modify.suite());
*/
/*        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.AWTFormObject.AWTFormObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.ClassObject.ClassObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.HTMLObject.HTMLObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.ImageObject.ImageObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.JSPObject.JSPObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.JavaSourceObject.JavaSourceObject_manipulation.suite());
        //Package? - two dialogs pop up informing about package change
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.Package.Package_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.SecurityJApplet.SecurityJApplet_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.SwingFormObject.SwingFormObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.TextualObject.TextualObject_manipulation.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.manipulation.URLObject.URLObject_manipulation.suite());
*/
/*        suite.addTest(DataLoaderTests.DataObjectTest.validity.AWTFormObject.AWTFormObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.ClassObject.ClassObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.HTMLObject.HTMLObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.ImageObject.ImageObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.JSPObject.JSPObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.JavaSourceObject.JavaSourceObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.Package.Package_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.SecurityJApplet.SecurityJApplet_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.SwingFormObject.SwingFormObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.TextualObject.TextualObject_validity.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.validity.URLObject.URLObject_validity.suite());
*/
/*        suite.addTest(DataLoaderTests.DataObjectTest.delegate.AWTFormObject.AWTFormObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.ClassObject.ClassObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.HTMLObject.HTMLObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.ImageObject.ImageObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.JSPObject.JSPObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.JavaSourceObject.JavaSourceObject_delegate.suite());
        //Package? - two dialogs are thrown informing about change in package
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.Package.Package_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.SecurityJApplet.SecurityJApplet_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.SwingFormObject.SwingFormObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.TextualObject.TextualObject_delegate.suite());
        suite.addTest(DataLoaderTests.DataObjectTest.delegate.URLObject.URLObject_delegate.suite());
*/        
        return suite;
    }
    
}
