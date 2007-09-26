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

package org.netbeans.modules.apisupport.project.suite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * Lists modules in a suite.
 * @author Jesse Glick
 */
final class SuiteSubprojectProviderImpl implements SubprojectProvider {
    
    private Set<NbModuleProject> projects;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean reloadNeeded;
    
    public SuiteSubprojectProviderImpl(AntProjectHelper helper, PropertyEvaluator eval) {
        this.helper = helper;
        this.eval = eval;
        eval.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modules".equals(evt.getPropertyName())) { // NOI18N
                    SuiteSubprojectProviderImpl.this.reloadNeeded = true;
                    SuiteSubprojectProviderImpl.this.changeSupport.fireChange();
                }
            }
        });
    }
    
    public Set<NbModuleProject> getSubprojects() {
        if (projects == null || reloadNeeded) {
            projects = loadProjects();
            reloadNeeded = false;
        }
        return projects;
    }
    
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private Set<NbModuleProject> loadProjects() {
        Set<NbModuleProject> newProjects = new HashSet<NbModuleProject>();
        String modules = eval.getProperty("modules"); // NOI18N
        if (modules != null) {
            for (String piece : PropertyUtils.tokenizePath(modules)) {
                FileObject dir = helper.resolveFileObject(piece);
                if (dir != null) {
                    try {
                        Project subp = ProjectManager.getDefault().findProject(dir);
                        if (subp != null && subp instanceof NbModuleProject) {
                            newProjects.add((NbModuleProject) subp);
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(newProjects);
    }
    
}
