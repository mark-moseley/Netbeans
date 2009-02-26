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
package org.netbeans.modules.cnd.gizmo;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = SourceFileInfoProvider.class)
public final class CodeModelSourceFileInfoProvider implements SourceFileInfoProvider {

    public SourceFileInfo fileName(String functionName) throws SourceFileInfoCannotBeProvided {
        //get project current name
        Project prj = org.netbeans.api.project.ui.OpenProjects.getDefault().getMainProject();
        if (prj.getLookup().lookup(NativeProject.class) == null) {
            throw new SourceFileInfoCannotBeProvided();
        }
        CsmProject csmProject = CsmModelAccessor.getModel().getProject(prj);
        if (csmProject == null) {
            throw new SourceFileInfoCannotBeProvided();
        }
        CsmDeclaration csmDeclaration = csmProject.findDeclaration(functionName);
        if (csmDeclaration == null) {
            Collection<CsmProject> libraries = csmProject.getLibraries();
            Iterator<CsmProject> iterator = libraries.iterator();
            for (CsmProject library : libraries) {
                csmDeclaration = library.findDeclaration(functionName);
                if (csmDeclaration != null) {
                    break;
                }
            }
        }
        if (csmDeclaration == null) {
            throw new SourceFileInfoCannotBeProvided();
        }
        if (!CsmKindUtilities.isOffsetableDeclaration(csmDeclaration)) {
            //do not know how to deal with this
            throw new SourceFileInfoCannotBeProvided();
        }
        return new SourceFileInfoProvider.SourceFileInfo(((CsmOffsetable) csmDeclaration).getContainingFile().getAbsolutePath().toString(), ((CsmOffsetable) csmDeclaration).getStartOffset());
    }
}
