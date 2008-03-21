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

package org.netbeans.modules.php.project.ui.wizards;

import org.netbeans.modules.php.project.ui.WebFolderNameProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureServerPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    static final String COPY_FILES = "copyFiles"; // NOI18N
    static final String COPY_TARGET = "copyTarget"; // NOI18N
    static final String COPY_TARGETS = "copyTargets"; // NOI18N
    static final String SERVER_IS_VALID = "serverIsValid"; // NOI18N

    private final WebFolderNameProvider webFolderNameProvider;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final String[] steps;
    private CopyFilesVisual copyFilesVisual = null;
    private WizardDescriptor descriptor;

    public ConfigureServerPanel(String[] steps, WebFolderNameProvider webFolderNameProvider) {
        this.webFolderNameProvider = webFolderNameProvider;
        this.steps = steps;
    }

    public Component getComponent() {
        if (copyFilesVisual == null) {
            copyFilesVisual = new CopyFilesVisual(webFolderNameProvider);
        }
        return copyFilesVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureServerPanel.class.getName());
    }

    public void readSettings(Object settings) {
        getComponent();
        descriptor = (WizardDescriptor) settings;

        unregisterListeners();

        // copying enabled?
        copyFilesVisual.setState(isProjectFolder());

        Boolean copyFiles = isCopyFiles();
        if (copyFiles != null) {
            copyFilesVisual.setCopyFiles(copyFiles);
        }

        MutableComboBoxModel localServers = getLocalServers();
        if (localServers != null) {
            copyFilesVisual.setLocalServerModel(localServers);
        }
        LocalServer wwwFolder = getLocalServer();
        if (wwwFolder != null) {
            copyFilesVisual.selectLocalServer(wwwFolder);
        }

        registerListeners();
        fireChangeEvent();
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;

        d.putProperty(COPY_FILES, copyFilesVisual.isCopyFiles());
        d.putProperty(COPY_TARGET, copyFilesVisual.getLocalServer());
        d.putProperty(COPY_TARGETS, copyFilesVisual.getLocalServerModel());
    }

    public boolean isValid() {
        getComponent();
        String error = validateServerLocation();
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            descriptor.putProperty(SERVER_IS_VALID, false);
            return false;
        }
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
        descriptor.putProperty(SERVER_IS_VALID, true);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return true;
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    String[] getSteps() {
        return steps;
    }

    private boolean isProjectFolder() {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.WWW_FOLDER);
        return ConfigureProjectPanel.isProjectFolder(localServer);
    }

    private Boolean isCopyFiles() {
        return (Boolean) descriptor.getProperty(COPY_FILES);
    }

    private LocalServer getLocalServer() {
        return (LocalServer) descriptor.getProperty(COPY_TARGET);
    }

    private MutableComboBoxModel getLocalServers() {
        return (MutableComboBoxModel) descriptor.getProperty(COPY_TARGETS);
    }

    private String validateServerLocation() {
        if (!copyFilesVisual.isCopyFiles()) {
            return null;
        }

        String sourcesLocation = copyFilesVisual.getLocalServer().getSrcRoot();
        if (sourcesLocation == null
                || !Utils.isValidFileName(new File(sourcesLocation).getName())) {
            return NbBundle.getMessage(ConfigureServerPanel.class, "MSG_IllegalFolderName");
        }

        return Utils.validateProjectDirectory(sourcesLocation, "Folder", false); // NOI18N
    }

    private void registerListeners() {
        copyFilesVisual.addChangeListener(this);
    }

    private void unregisterListeners() {
        copyFilesVisual.removeChangeListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
}
