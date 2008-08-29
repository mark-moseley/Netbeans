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

package org.netbeans.modules.j2ee.common.project.ui;

import java.text.MessageFormat;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public final class DeployOnSaveUtils {

    private DeployOnSaveUtils() {
        super();
    }

    public static boolean showBuildActionWarning(Project project, CustomizerPresenter presenter) {
        String text = NbBundle.getMessage(DeployOnSaveUtils.class, "LBL_ProjectBuiltAutomatically");
        String projectProperties = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_ProjectProperties");
        String cleanAndBuild = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_CleanAndBuild");
        String ok = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_OK");
        String titleFormat = NbBundle.getMessage(DeployOnSaveUtils.class, "TITLE_BuildProjectWarning");
        String title = MessageFormat.format(titleFormat, ProjectUtils.getInformation(project).getDisplayName());
        DialogDescriptor dd = new DialogDescriptor(text,
                title,
                true,
                new Object[]{projectProperties, cleanAndBuild, ok},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);

        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == projectProperties) {
            if (presenter != null) {
                presenter.showCustomizer("Run"); // NOI18N
            }
            return false;
        }

        if (result == cleanAndBuild) {
            return true;
        } else {
            return false;
        }
    }

    public interface CustomizerPresenter {
        void showCustomizer(String category);
    }
}
