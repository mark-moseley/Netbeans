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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jdbcwizard.builder.wsdl;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
// <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.File;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBConnectionDefinition;

/**
 * @author Administrator
 */
public class GenerateWSDL extends Task {

    private String mSrcDirectoryLocation;

    private String mBuildDirectoryLocation;

    private String mWSDLFileName;

    private DBTable mTable;

    private String mDBType;

    private String mJNDIName;
    
    private DBConnectionDefinition dbinfo;

    public GenerateWSDL() {

    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getSrcDirectoryLocation() {
        return this.mSrcDirectoryLocation;
    }

    /**
     * @param srcDirectoryLocation The srcDirectoryLocation to set.
     */
    public void setSrcDirectoryLocation(final String srcDirectoryLocation) {
        this.mSrcDirectoryLocation = srcDirectoryLocation;
    }

    /**
     * @return Returns the srcDirectoryLocation.
     */
    public String getBuildDirectoryLocation() {
        return this.mBuildDirectoryLocation;
    }

    /**
     * @param buildDirectoryLocation The buildDirectoryLocation to set.
     */
    public void setBuildDirectoryLocation(final String buildDirectoryLocation) {
        this.mBuildDirectoryLocation = buildDirectoryLocation;
    }

    /**
     * get wsdl file name
     * 
     * @return
     */
    public String getWSDLFileName() {
        return this.mWSDLFileName;
    }

    /**
     * set wsdl file name
     * 
     * @param wsdlFileName
     */
    public void setWSDLFileName(final String wsdlFileName) {
        this.mWSDLFileName = wsdlFileName;
    }

    /**
     * @param table
     */
    public void setDBTable(final DBTable table) {
        this.mTable = table;
    }

    /**
     * @param dbtype
     */
    public void setDBType(final String dbtype) {
        this.mDBType = dbtype;
    }

    /**
     * @param schemaName
     */
    public void setSchemaName(final String schemaName) {
    }

    /**
     * @param jndiName
     */
    public void setJNDIName(final String jndiName) {
        this.mJNDIName = jndiName;
    }
    
    public void setDBInfo(DBConnectionDefinition dbinfo){
        this.dbinfo = dbinfo;
    }
    /**
     * generate wsdl
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        final File srcDir = new File(this.mSrcDirectoryLocation);
        if (!srcDir.exists()) {
            throw new BuildException("Directory " + this.mSrcDirectoryLocation + " does not exit.");
        }
        try {
            final String srcDirPath = srcDir.getAbsolutePath();
            // pass dbtable object
            // get the xsd file name and xsd top element name
            final WSDLGenerator wsdlgen = new WSDLGenerator(this.mTable, this.mWSDLFileName, srcDirPath, this.mDBType, this.mJNDIName);
            wsdlgen.setTopEleName();
            wsdlgen.setXSDName();
            wsdlgen.setDBInfo(this.dbinfo);
            wsdlgen.generateWSDL();
        } catch (final Exception e) {
            throw new BuildException(e.getMessage());
        }
    }

    public static void main(final String[] args) {
        // GenerateWSDL tsk = new GenerateWSDL();
        // tsk.setSrcDirectoryLocation("c:/temp");
        // tsk.execute();
    }
}