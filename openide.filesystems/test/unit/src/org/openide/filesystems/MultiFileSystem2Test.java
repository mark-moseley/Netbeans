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

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;
//import org.openide.filesystems.hidden.*;

import java.io.*;
import java.beans.PropertyVetoException;
/**
 *
 * @author  rm111737
 * @version
 */
public class MultiFileSystem2Test extends FileSystemFactoryHid {

    /** Creates new MultiFileSystemTest */
    public MultiFileSystem2Test(Test test) {
        super(test);
    }

    public static void main(String args[]) throws  Exception{
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);                        
        suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(FileSystemTestHid.class);                        
        suite.addTestSuite(FileObjectTestHid.class);        
        suite.addTestSuite(MultiFileObjectTestHid.class);
        /*failing tests*/        
        suite.addTestSuite(URLMapperTestHidden.class);        
        suite.addTestSuite(URLMapperTestInternalHidden.class);                        
        
        return new MultiFileSystem2Test (suite);
    }
    

    /**
     * 
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName" */
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException {        
            FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs2"+testName, new String[] {});
            FileSystem xfs = TestUtilHid.createXMLFileSystem(testName, resources);
            FileSystem mfs = new MultiFileSystem (new FileSystem[] {lfs,xfs});
            try {
                mfs.setSystemName("mfs2test");
            } catch (PropertyVetoException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        return new FileSystem[] {mfs,lfs,xfs};
    }
    
    protected void destroyFileSystem (String testName) throws IOException {}            
}
