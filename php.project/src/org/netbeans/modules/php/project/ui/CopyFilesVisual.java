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

package org.netbeans.modules.php.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CopyFilesVisual extends JPanel {
    private static final long serialVersionUID = 16907251064819776L;

    final LocalServerController localServerController;
    final ChangeSupport changeSupport = new ChangeSupport(this);

    public CopyFilesVisual(SourcesFolderProvider sourcesFolderProvider, LocalServer... defaultLocalServers) {
        initComponents();

        localServerController = LocalServerController.create(copyFilesComboBox, copyFilesButton, sourcesFolderProvider,
                new BrowseCopyFiles(), NbBundle.getMessage(CopyFilesVisual.class, "LBL_SelectFolderLocation"), defaultLocalServers);
        // set default, disabled state
        localServerController.setEnabled(false);

        copyFilesCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyFilesCheckBoxChanged();
                changeSupport.fireChange();
            }
        });
        localServerController.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    void copyFilesCheckBoxChanged() {
        boolean selected = copyFilesCheckBox.isSelected();
        localServerLabel.setEnabled(selected);
        localServerController.setEnabled(selected);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public boolean isCopyFiles() {
        return copyFilesCheckBox.isSelected();
    }

    public void setCopyFiles(boolean copyFiles) {
        copyFilesCheckBox.setSelected(copyFiles);
        copyFilesCheckBoxChanged();
    }

    public LocalServer getLocalServer() {
        return localServerController.getLocalServer();
    }

    public MutableComboBoxModel getLocalServerModel() {
        return localServerController.getLocalServerModel();
    }

    public void setLocalServerModel(MutableComboBoxModel localServers) {
        localServerController.setLocalServerModel(localServers);
    }

    public void selectLocalServer(LocalServer localServer) {
        localServerController.selectLocalServer(localServer);
    }

    // to enable/disable components
    public void setState(boolean enabled) {
        copyFilesCheckBox.setEnabled(enabled);
    }

    public boolean getState() {
        return copyFilesCheckBox.isEnabled();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        copyFilesCheckBox = new JCheckBox();
        localServerLabel = new JLabel();
        copyFilesComboBox = new JComboBox();
        copyFilesButton = new JButton();

        setFocusTraversalPolicy(null);


        Mnemonics.setLocalizedText(copyFilesCheckBox, NbBundle.getMessage(CopyFilesVisual.class, "LBL_CopyFiles")); // NOI18N
        localServerLabel.setLabelFor(copyFilesComboBox);
        Mnemonics.setLocalizedText(localServerLabel, NbBundle.getMessage(CopyFilesVisual.class, "LBL_CopyFileToFolder")); // NOI18N
        localServerLabel.setEnabled(false);

        copyFilesComboBox.setEditable(true);
        copyFilesComboBox.setEnabled(false);

        Mnemonics.setLocalizedText(copyFilesButton, NbBundle.getMessage(CopyFilesVisual.class, "LBL_Browse")); // NOI18N
        copyFilesButton.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(copyFilesCheckBox)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(localServerLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(copyFilesComboBox, 0, 168, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(copyFilesButton))

        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(copyFilesCheckBox)
                .add(9, 9, 9)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(copyFilesButton)
                    .add(copyFilesComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(localServerLabel)))

        );

        copyFilesCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesCheckBox.AccessibleContext.accessibleName")); // NOI18N
        copyFilesCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        localServerLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.localServerLabel.AccessibleContext.accessibleName")); // NOI18N
        localServerLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.localServerLabel.AccessibleContext.accessibleDescription")); // NOI18N
        copyFilesComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesComboBox.AccessibleContext.accessibleName")); // NOI18N
        copyFilesComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        copyFilesButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesButton.AccessibleContext.accessibleName")); // NOI18N
        copyFilesButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.copyFilesButton.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CopyFilesVisual.class, "CopyFilesVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton copyFilesButton;
    private JCheckBox copyFilesCheckBox;
    private JComboBox copyFilesComboBox;
    private JLabel localServerLabel;
    // End of variables declaration//GEN-END:variables


    private static class BrowseCopyFiles implements LocalServerController.BrowseHandler {
        public File getCurrentDirectory() {
            return LastUsedFolders.getCopyFiles();
        }
        public void locationChanged(File location) {
            LastUsedFolders.setCopyFiles(location);
        }
    }
}
