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
package org.netbeans.modules.maven.hints.errors;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.hints.ui.SearchDependencyUI;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anuradha
 */
public class DependencyByClassNameAction extends AbstractAction implements ContextAwareAction {

    private Project mavenProject;

    public DependencyByClassNameAction() {

        putValue(Action.NAME, NbBundle.getMessage(DependencyByClassNameAction.class,
                "LBL_Find_Add_Dependency_By_ClassName"));

    }

    public DependencyByClassNameAction(Project mavenProject) {
        this();
        this.mavenProject = mavenProject;
    }

    public void actionPerformed(ActionEvent e) {
        if (mavenProject != null) {
            NBVersionInfo nbvi = null;
            SearchDependencyUI dependencyUI = new SearchDependencyUI("", mavenProject);

            DialogDescriptor dd = new DialogDescriptor(dependencyUI,
                    org.openide.util.NbBundle.getMessage(SearchClassDependencyInRepo.class, "LBL_Search_Repo"));
            dd.setClosingOptions(new Object[]{
                        dependencyUI.getAddButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            dd.setOptions(new Object[]{
                        dependencyUI.getAddButton(),
                        DialogDescriptor.CANCEL_OPTION
                    });
            Object ret = DialogDisplayer.getDefault().notify(dd);
            if (dependencyUI.getAddButton() == ret) {
                nbvi = dependencyUI.getSelectedVersion();
            }

            if (nbvi != null) {
                ModelUtils.addDependency(mavenProject.getProjectDirectory().getFileObject("pom.xml"), nbvi.getGroupId(), nbvi.getArtifactId(),
                        nbvi.getVersion(), nbvi.getType(), null, null, true);//NOI18N

                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        mavenProject.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
                    }
                });
            }
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        mavenProject = actionContext.lookup(Project.class);
        return new DependencyByClassNameAction(mavenProject);
    }
}
