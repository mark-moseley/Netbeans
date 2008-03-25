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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.ui.CopyFilesVisual;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.WebFolderNameProvider;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureServerPanelVisual extends JPanel {
    private static final long serialVersionUID = 186471932981722630L;

    private final CopyFilesVisual copyFilesVisual;

    /** Creates new form ConfigureServerPanelVisual */
    public ConfigureServerPanelVisual(ConfigureServerPanel wizardPanel, WebFolderNameProvider webFolderNameProvider) {

        // Provide a name in the title bar.
        setName(NbBundle.getMessage(ConfigureServerPanelVisual.class, "LBL_ProjectServer"));
        putClientProperty("WizardPanel_contentSelectedIndex", 1); // NOI18N
        // Step name (actually the whole list for reference).
        putClientProperty("WizardPanel_contentData", wizardPanel.getSteps()); // NOI18N

        initComponents();

        copyFilesVisual = new CopyFilesVisual(webFolderNameProvider);
        copyFilesPanel.add(BorderLayout.NORTH, copyFilesVisual);
    }

    public void addServerListener(ChangeListener listener) {
        copyFilesVisual.addChangeListener(listener);
    }

    public void removeServerListener(ChangeListener listener) {
        copyFilesVisual.removeChangeListener(listener);
    }


    public boolean isCopyFiles() {
        return copyFilesVisual.isCopyFiles();
    }

    public void setCopyFiles(boolean copyFiles) {
        copyFilesVisual.setCopyFiles(copyFiles);
    }

    public LocalServer getLocalServer() {
        return copyFilesVisual.getLocalServer();
    }

    public MutableComboBoxModel getLocalServerModel() {
        return copyFilesVisual.getLocalServerModel();
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
        copyFilesVisual.setLocalServerModel(localServers);
    }

    public void selectLocalServer(LocalServer localServer) {
        copyFilesVisual.selectLocalServer(localServer);
    }

    public void setState(boolean enabled) {
        copyFilesVisual.setState(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        copyFilesPanel = new javax.swing.JPanel();

        copyFilesPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(copyFilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel copyFilesPanel;
    // End of variables declaration//GEN-END:variables

}
