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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ui.wizards.FolderList;


/**
 *
 * @author  tom
 */
public class PanelSourceFoldersTest extends NbTestCase {

    public PanelSourceFoldersTest (java.lang.String testName) {
        super(testName);
    }

    public void testCheckValidity () throws Exception {

        File root = getWorkDir();
        File projectDir = new File (root, "project");
        File test = new File (root,  "tests");
        test.mkdir();
        File src = new File (root, "src");
        src.mkdir();
        File badSrcDir = new File (root, "badSrc");
        File badSrcDir2 = new File (test, "src");
        badSrcDir2.mkdir();
        File badProjectDir = new File (root, "badPrjDir");
        badProjectDir.mkdir();
        badProjectDir.setReadOnly();
        
        assertNotNull("Empty name", PanelProjectLocationExtSrc.checkValidity ("",projectDir.getAbsolutePath(),"build.xml"));
        assertNotNull("Read Only WorkDir", PanelProjectLocationExtSrc.checkValidity ("",badProjectDir.getAbsolutePath(),"build.xml"));
        assertNotNull("Non Existent Sources", PanelSourceFolders.checkValidity (projectDir, new File[] {badSrcDir} , new File[] {test}));
        assertFalse("Sources == Tests",  FolderList.isValidRoot (src, new File[] {src},projectDir));
        assertFalse("Tests under Sources", FolderList.isValidRoot (new File (src, "Tests"),new File[] {src},projectDir));
        assertFalse("Sources under Tests", FolderList.isValidRoot (badSrcDir2, new File[] {test},projectDir));
        assertNull ("Valid data", PanelSourceFolders.checkValidity (projectDir, new File[]{src}, new File[]{test}));
    }
}