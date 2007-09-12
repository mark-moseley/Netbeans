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

package org.netbeans.modules.bpel.project;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelSourcesRegistryHelper {
    private final BpelproProject myProject;
    private final List<String> myRegisteredSources =
            new LinkedList<String>();
    private final BpelSourcesRegistry mySourcesRegistry;
    
    /** Creates a new instance of BpelSourcesRegistryHelper */
    public BpelSourcesRegistryHelper(BpelproProject project) {
        myProject = project;
        mySourcesRegistry = (BpelSourcesRegistry)Lookup.
                getDefault().lookup(BpelSourcesRegistry.class);
    }
    
    public synchronized void register() {
        if (mySourcesRegistry == null) {
            return;
        }
        Sources sources = ProjectUtils.getSources(myProject);
        SourceGroup sgs [] = sources.getSourceGroups(IcanproProject.SOURCES_TYPE_ICANPRO);
        for (int i = 0; i < sgs.length; i++) {
            FileObject fo = sgs[i].getRootFolder();
            if (fo == null) {
                continue;
            }
            
            File file = FileUtil.toFile(fo);
            if (file == null) {
                continue;
            }
            
            String path = file.getPath();
            if (mySourcesRegistry.addSourceRoot(path)) {
                myRegisteredSources.add(path);
            }
        }
    }
    
    public synchronized void unregister() {
        if (mySourcesRegistry == null) {
            return;
        }
        for (String path : myRegisteredSources) {
            mySourcesRegistry.removeSourceRoot(path);
        }
        myRegisteredSources.clear();
    }
}
