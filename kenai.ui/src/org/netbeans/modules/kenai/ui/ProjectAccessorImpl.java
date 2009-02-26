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

package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiProjectFeature;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.netbeans.modules.kenai.ui.spi.LoginHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=ProjectAccessor.class)
public class ProjectAccessorImpl extends ProjectAccessor {

    private Kenai kenai = Kenai.getDefault();

    @Override
    public List<ProjectHandle> getMemberProjects(LoginHandle login) {
        try {
            LinkedList<ProjectHandle> l = new LinkedList<ProjectHandle>();
            for (KenaiProject prj : kenai.getMyProjects()) {
                l.add(new ProjectHandleImpl(prj));
            }
            return l;
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public ProjectHandle getNonMemberProject(String projectId) {
        try {
            return new ProjectHandleImpl(kenai.getProject(projectId));
        } catch (KenaiException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ActionListener getOpenNonMemberProjectAction() {
        return new OpenKenaiProjectAction();
    }

    @Override
    public ActionListener getDetailsAction(final ProjectHandle project) {
        return new URLDisplayerAction(org.openide.util.NbBundle.getMessage(ProjectAccessorImpl.class, "CTL_EditProject"), ((ProjectHandleImpl) project).getKenaiProject().getWebLocation());
    }

    @Override
    public ActionListener getDefaultAction(ProjectHandle project) {
        return getDetailsAction(project);
    }

    @Override
    public Action[] getPopupActions(ProjectHandle project) {
        Action[] actions = new Action[]{
            (Action) getDetailsAction(project),
            new RemoveProjectAction(project),
            new RefreshProjectAction(project)
        };
        return actions;
    }

    @Override
    public ActionListener getOpenWikiAction(ProjectHandle project) {
        KenaiProjectFeature[] wiki = ((ProjectHandleImpl)project).getKenaiProject().getFeatures(KenaiFeature.WIKI);
        if (wiki.length==1) {
            return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
        }
        return null;
    }

    @Override
    public ActionListener getOpenDownloadsAction(ProjectHandle project) {
        KenaiProjectFeature[] wiki = ((ProjectHandleImpl)project).getKenaiProject().getFeatures(KenaiFeature.DOWNLOADS);
        if (wiki.length==1) {
            return new URLDisplayerAction(wiki[0].getDisplayName(), wiki[0].getWebLocation());
        }
        return null;
    }
}
