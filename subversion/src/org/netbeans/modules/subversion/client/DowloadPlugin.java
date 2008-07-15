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

package org.netbeans.modules.subversion.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class DowloadPlugin implements ActionListener {

    private DownloadPanel panel;
    private JButton ok;
    private JButton cancel;
    private UpdateElement updateElement;

    public DowloadPlugin() {
        panel = new DownloadPanel();
        ok = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_OK"));
        cancel = new JButton(NbBundle.getMessage(SvnClientExceptionHandler.class, "CTL_Action_Cancel"));
        ok.setEnabled(false);
        panel.licensePanel.setVisible(false);
        panel.acceptCheckBox.addActionListener(this);
    }

    boolean show() {
        download();
        NotifyDescriptor descriptor = new NotifyDescriptor (
                panel,
                "Dowload me !!!",
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.DEFAULT_OPTION,
                new Object [] { ok, cancel },
                ok);
        boolean ret = DialogDisplayer.getDefault().notify(descriptor) == ok;
        if(!ret) {
            updateElement = null;
        }
        return ret;
    }

    private void download() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProgressHandle ph = ProgressHandleFactory.createHandle("Looking for java svn bindings...");
                panel.progressLabel.setText("Looking for java svn bindings...");
                panel.progressBarPanel.add(ProgressHandleFactory.createProgressComponent(ph), BorderLayout.CENTER);
                panel.repaint();
                ph.start();
                try {
                    List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
                    for (UpdateUnit u : units) {
                        if(u.getCodeName().equals(SvnClientFactory.JAVAHL_MODULE_CODE_NAME)) {

                            List<UpdateElement> elements = u.getAvailableUpdates();
                            if(elements.size() == 0) {
                                panel.progressBarPanel.setVisible(false);
                                panel.progressLabel.setText("Subversion Java Bindings seem to be already installed!");
                                panel.repaint();
                                return;
                            } else {
                                updateElement = u.getAvailableUpdates().get(0);
                                break;
                            }
                        }
                    }
                } finally {
                    ph.finish();
                }
                if(updateElement == null) {
                    panel.progressBarPanel.setVisible(false);
                    panel.progressLabel.setText("Subversion Java Bindings not found!");
                    panel.repaint();
                    return;
                }
                panel.licensePanel.setVisible(true);
                panel.licenseTextPane.setText(updateElement.getLicence());
                panel.progressPanel.setVisible(false);
                panel.repaint();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.acceptCheckBox) {
            ok.setEnabled(panel.acceptCheckBox.isSelected());
        } 
    }

    public UpdateElement getUpdateElement() {
        return updateElement;
    }

}
