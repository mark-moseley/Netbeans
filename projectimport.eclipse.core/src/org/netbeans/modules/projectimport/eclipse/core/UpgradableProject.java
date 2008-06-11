/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;

/**
 *
 */
final public class UpgradableProject {

    private Project project;
    private EclipseProjectReference reference;
    private boolean initialized;
    
    public UpgradableProject(Project project) {
        this.project = project;
    }
    
    public boolean isEclipseProjectReachable() {
        if (!isUpgradable()) {
            return false;
        }
        return getEclipse().isEclipseProjectReachable();
    }
    
    public boolean isUpgradable() {
        return getEclipse() != null;
    }
    
    public boolean isUpToDate(boolean deepTest) {
        assert isUpgradable() && isEclipseProjectReachable();
        return getEclipse().isUpToDate(deepTest);
    }
    
    public void update(final List<String> importProblems) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new ExceptionAction<Void>() {

                public Void run() throws Exception {
                    getEclipse().update(importProblems);
                    return null;
                }
            });
        } catch (MutexException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex.getCause());
            throw ioe;
        }
    }
    
    private EclipseProjectReference getEclipse() {
        if (!initialized) {
            reference = EclipseProjectReference.read(project);
            // cache data only if project is reachable
            initialized = reference.isEclipseProjectReachable();
        }
        return reference;
    }
    
    /**
     * Shows UI to resolve location of eclipse project and workspace and returns 
     * true if reference was succesfully resolved.
     */
    public boolean updateBrokenEclipseReference() {
        return UpdateEclipseReferencePanel.showEclipseReferenceResolver(getEclipse());
    }

    File getEclipseProjectFolder() {
        return getEclipse().getFallbackEclipseProjectLocation();
    }
}
