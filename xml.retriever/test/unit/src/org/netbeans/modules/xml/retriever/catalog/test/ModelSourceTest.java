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
 * ModelSourceTest.java
 * JUnit based test
 *
 * Created on January 22, 2007, 6:38 PM
 */

package org.netbeans.modules.xml.retriever.catalog.test;

import java.io.File;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class ModelSourceTest extends TestCase {
    
    public ModelSourceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testModelSource(){
        
                /*
                 * Step1: Copy
                 * <nb_src>/xml/retriever/test/unit/src/org/netbeans/modules/xml/retriever/catalog/test/TestCatalogModel.java
                 * to your unit test area.
                 */
        
        
        
        
        
        
        
        
        
        
        /*
         *Step 2: IMPORTANT NOTE: also make sure that all the required jars are actually set in the unit test class path.
         * This is done by placing or appending to the property (for more accurate list, copy and use from:
         <nb_src>/xml/retriever/nbproject/project.properties)
         
         test.unit.cp.extra=\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-retriever.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-xdm.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-xam.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-apache-xml-resolver.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor.jar:\
            ${netbeans.dest.dir}/platform6/lib/org-openide-modules.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor-util.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-text.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-core.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor-lib.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-projectapi.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-netbeans-modules-masterfs.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-windows.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-dialogs.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-awt.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-options.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-loaders.jar:\
            ${netbeans.dest.dir}/platform6/core/org-openide-filesystems.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-nodes.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-text.jar:\
            ${netbeans.dest.dir}/platform6/lib/org-openide-util.jar
         */
        
        
        //To create a model source use this code
        
        //ModelSource ms = TestCatalogModel.getDefault().createTestModelSource(FileObject, editable);
        
        /*Sample code*/
        File file = null;
        try {
            file = File.createTempFile("modelsource", "deleteme");
            file.deleteOnExit();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //create ur own file object here
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        ModelSource ms = null;
        try {
            ms = TestCatalogModel.getDefault().createTestModelSource(fo, true);
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
        
        System.out.println(ms.getLookup().lookup(FileObject.class));
        
    }
    
}
