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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** JUnit test suite with Jemmy support
 *
 * @author pj97932
 * @version 1.0
 */
public class FastScanTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FastScanTest(String testName) {
        super(testName);
    }
    
    public void testPage1() throws Exception {
        doFastScanTest("jspparser-data/wmroot", "subdir/Page1.jsp", new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"));
        
    }
    
    public void testXMLFromExamples1() throws Exception {
        doFastScanTest("project3/web", "xml/xml.jsp", new JspParserAPI.JspOpenInfo(true, "UTF-8"));
    }
    
    public void testXMLFromExamples2() throws Exception {
        doFastScanTest("project3/web", "jsp2/jspx/basic.jspx", new JspParserAPI.JspOpenInfo(true, "UTF-8"));
    }
    
    public void doFastScanTest(String wmRootPath, String path, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        try{
            FileObject wmRoot = TestUtil.getFileInWorkDir(wmRootPath, this);
            StringTokenizer st = new StringTokenizer(path, "/");
            FileObject tempFile = wmRoot;
            String ss;
            while (st.hasMoreTokens()) {
                tempFile = tempFile.getFileObject(st.nextToken());
            }
            parseIt(wmRoot, tempFile, correctInfo);
        }catch(RuntimeException e){
            e.printStackTrace();
            e.printStackTrace(getRef());
            fail("Initialization of test failed! ->" + e);
        }
    }
    
    private void parseIt(FileObject root, FileObject jspFile, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        log("calling parseIt, root: " + root + "  file: " + jspFile);
        JspParserAPI api = JspParserFactory.getJspParser();
        JspParserAPI.JspOpenInfo info = api.getJspOpenInfo(jspFile, TestUtil.getWebModule(jspFile), false);
        log("file: " + jspFile + "   enc: " + info.getEncoding() + "   isXML: " + info.isXmlSyntax());
        assertEquals(correctInfo, info);
    }
    
}
