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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class ConvertImportTest extends NbTestCase {
    private File testFile;
    public ConvertImportTest(java.lang.String testName) {
        super(testName);
    }

    
    public void testConvertImport() throws IOException {
       String xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       String xmlOut =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../templates/xtest-unit.xml\"/>\n" +
           "</project>";

       String xmlOutPrefix =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"${test.dist.dir}/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       
       createFile(xml);
       
       ConvertImport convert = new ConvertImport();
       convert.setFile(testFile); 
       convert.setOldName("templates/xtest-unit.xml");
       convert.setNewPath("../templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOut);
       
       createFile(xml);
       convert.setPropertyPrefixName("test.dist.dir");
       convert.setNewPath("templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOutPrefix); 
 
        xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<!-- <import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n-->" +
           "</project>";
       createFile(xml);
       convert.execute();
       assertNewXml(xml);

       xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<!-- file -->" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       xmlOut =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<!-- file -->" +               
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"${test.dist.dir}/xx\"/>\n" +
           "</project>";
       createFile(xml);
       convert.setNewPath("xx");
       convert.execute();
       assertNewXml(xmlOut);
       
    }

    private File createFile(String xml) throws IOException {
       testFile = new File(getWorkDir(),"testFile.xml");
       PrintStream ps = new PrintStream(testFile);
       ps.print(xml);
       ps.close();
       return testFile;
    }

    private void assertNewXml(String xmlOut) throws IOException {
        File file = new File(getWorkDir(),"ref.xml");
        PrintStream ps = new PrintStream(file);
        ps.print(xmlOut);
        assertFile(testFile,file);
    }
}
