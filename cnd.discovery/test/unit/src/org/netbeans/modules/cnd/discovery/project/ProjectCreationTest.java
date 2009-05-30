/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProjectCreationTest extends MakeProjectBase {
    private static final boolean TRACE = true;

    public ProjectCreationTest() {
        super("ProjectCreationTest");
        if (TRACE) {
            System.setProperty("cnd.discovery.trace.projectimport", "true"); // NOI18N
        }

    }

    public void testPkgConfig(){
        performTestProject("http://pkgconfig.freedesktop.org/releases/pkgconfig-0.18.tar.gz");
    }

    public void testLiteSql(){
        if (Utilities.isWindows()) {
            // make does not work on windows
            // it start XWin and all hangs
            // do anybody know how to make litesql on windows?
            return;
        }
        performTestProject("http://www.mirrorservice.org/sites/download.sourceforge.net/pub/sourceforge/l/li/litesql/litesql-0.3.3.tar.gz");
    }

    @Override
    void perform(CsmProject csmProject) {
        if (TRACE) {
            System.err.println("Model content:");
        }
        for (CsmFile file : csmProject.getAllFiles()) {
            if (TRACE) {
                System.err.println("\t"+file.getAbsolutePath());
            }
            for(CsmInclude include : file.getIncludes()){
                assertTrue("Not resolved include directive "+include.getIncludeName()+" in file "+file.getAbsolutePath(), include.getIncludeFile() != null);
            }
        }
    }
}
