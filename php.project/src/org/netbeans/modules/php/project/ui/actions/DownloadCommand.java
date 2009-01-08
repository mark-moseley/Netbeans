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

package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.TransferInfo;
import org.netbeans.modules.php.project.connections.ui.TransferFilter;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Download files from remote connection.
 * @author Tomas Mysik
 */
public class DownloadCommand extends RemoteCommand implements Displayable {
    public static final String ID = "download"; // NOI18N
    public static final String DISPLAY_NAME = NbBundle.getMessage(DownloadCommand.class, "LBL_DownloadCommand");

    public DownloadCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    protected Runnable getContextRunnable(final Lookup context) {
        return new Runnable() {
            public void run() {
                invokeActionImpl(context);
            }
        };
    }

    private void invokeActionImpl(Lookup context) {
        FileObject[] selectedFiles = CommandUtils.filesForSelectedNodes();
        assert selectedFiles.length > 0 : "At least one node must be selected for Download action";

        FileObject[] sources = Utils.getSourceObjects(getProject());

        InputOutput remoteLog = getRemoteLog(getRemoteConfiguration().getDisplayName());
        RemoteClient remoteClient = getRemoteClient(remoteLog);
        String progressTitle = NbBundle.getMessage(UploadCommand.class, "MSG_DownloadingFiles", getProject().getName());
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
        TransferInfo transferInfo = null;
        try {
            progressHandle.start();
            Set<TransferFile> forDownload = remoteClient.prepareDownload(sources[0], selectedFiles);
            // avoid timeout errors
            remoteClient.disconnect();

            forDownload = TransferFilter.showDownloadDialog(forDownload);
            if (forDownload.size() == 0) {
                return;
            }

            if (forDownload.size() > 0) {
                progressHandle.finish();
                progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
                progressHandle.start();
                transferInfo = remoteClient.download(sources[0], forDownload);
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(UploadCommand.class, "MSG_DownloadFinished", getProject().getName()));
            }
        } catch (RemoteException ex) {
            processRemoteException(ex);
        } finally {
            try {
                remoteClient.disconnect();
            } catch (RemoteException ex) {
                processRemoteException(ex);
            }
            if (transferInfo != null) {
                processTransferInfo(transferInfo, remoteLog);
            }
            progressHandle.finish();
        }
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
