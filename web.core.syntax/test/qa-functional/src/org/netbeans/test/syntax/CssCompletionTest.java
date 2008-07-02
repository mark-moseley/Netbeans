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
package org.netbeans.test.syntax;

import java.io.File;
import org.netbeans.test.web.FileObjectFilter;
import org.openide.filesystems.FileObject;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.web.RecurrentSuiteFactory;

/**
 *
 * @author Jindrich Sedek
 */
public class CssCompletionTest extends CompletionTest {

    /** Creates a new instance of CompletionTesJ2EE */
    public CssCompletionTest() {}

    public CssCompletionTest(String name, FileObject testFileObj) {
        super(name, testFileObj);
        debug = false;
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        addServerTests(conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fo) {
                    String ext = fo.getExt();
                    String name = fo.getName();
                    return (name.startsWith("test") || name.startsWith("Test")) && (ext.equals("css"));
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(CssCompletionTest.class,
                    new CssCompletionTest().getProjectsDir(), filter));
        }
    }

    @Override
    public void runTest() throws Exception {
        test(testFileObj, "/**CC", "*/", false);
    }

    @Override
    public File getProjectsDir() {
        File datadir = new CssCompletionTest(null, null).getDataDir();
        return new File(datadir, "CSSCompletionTestProjects");
    }


}
