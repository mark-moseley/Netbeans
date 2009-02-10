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

package org.netbeans.modules.php.project.ui.actions.support;

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.actions.UploadCommand;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Action implementation for REMOTE configuration.
 * It means uploading, running and debugging web pages on a remote web server.
 * @author Tomas Mysik
 */
public class ConfigActionRemote extends ConfigActionLocal {

    protected ConfigActionRemote(PhpProject project) {
        super(project);
    }

    @Override
    public boolean isValid(boolean indexFileNeeded) {
        boolean valid = super.isValid(indexFileNeeded);
        if (!valid) {
            return false;
        }
        String remoteConnection = ProjectPropertiesSupport.getRemoteConnection(project);
        if (remoteConnection == null || RemoteConnections.get().remoteConfigurationForName(remoteConnection) == null) {
            valid = false;
        } else if (RunAsValidator.validateUploadDirectory(ProjectPropertiesSupport.getRemoteDirectory(project), true) != null) {
            valid = false;
        }
        if (!valid) {
            showCustomizer();
        }
        return valid;
    }

    @Override
    public void runProject() {
        eventuallyUploadFiles();
        super.runProject();
    }

    @Override
    public void debugProject() {
        eventuallyUploadFiles();
        super.debugProject();
    }

    @Override
    protected void preShowUrl(Lookup context) {
        eventuallyUploadFiles(CommandUtils.filesForSelectedNodes());
    }

    private void eventuallyUploadFiles() {
        eventuallyUploadFiles((FileObject[]) null);
    }

    private void eventuallyUploadFiles(FileObject... preselectedFiles) {
        UploadCommand uploadCommand = (UploadCommand) CommandUtils.getCommand(project, UploadCommand.ID);
        if (!uploadCommand.isActionEnabled(null)) {
            return;
        }

        PhpProjectProperties.UploadFiles uploadFiles = ProjectPropertiesSupport.getRemoteUpload(project);
        assert uploadFiles != null;

        if (PhpProjectProperties.UploadFiles.ON_RUN.equals(uploadFiles)) {
            uploadCommand.uploadFiles(new FileObject[] {ProjectPropertiesSupport.getSourcesDirectory(project)}, preselectedFiles);
        }
    }
}
