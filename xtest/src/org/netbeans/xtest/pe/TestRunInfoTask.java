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

/*
 * TestRunInfoTask.java
 *
 * Created on November 13, 2001, 6:56 PM
 */

package org.netbeans.xtest.pe;


import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;
import org.w3c.dom.*;
import org.netbeans.xtest.util.SerializeDOM;

/**
 *
 * @author  mb115822
 * @version
 */
public class TestRunInfoTask extends Task{

    /** Creates new TestRunInfoTask */
    public TestRunInfoTask() {
    }

    private File outfile;
    private String config;
    private String name;
    private ModuleError error;

    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    public void setConfig(String config) {
        this.config = config;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setModuleError(ModuleError error) {
        this.error = error;
    }
    
    public TestRun getTestRunInfo() {
        TestRun tr = new TestRun();
        tr.xmlat_config = config;
        tr.xmlat_name = name;
        tr.xmlat_timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
        return tr;
    }

    public void execute () throws BuildException {
        log("Generating test run info xml");
        TestRun tr;
        try {
            Document doc = SerializeDOM.parseFile(this.outfile);
            tr = (TestRun)XMLBean.getXMLBean(doc);
            //System.out.println("TestRun already created");
            // testrun already created - return
            if (error != null) {
                tr.addModuleError(error);
                log("Test run info was updated with module error.");
            }
            else {
                log("Test run info already exists - skipping");
                return;
            }
        } catch (Exception e) {
            //System.out.println("TestRun not found, have to crete a new one");
            tr = getTestRunInfo();
        }
        //System.err.println("TR:"+tr);
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(tr.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save testrun:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:"+e);
            e.printStackTrace(System.err);           
        }
    }
}
