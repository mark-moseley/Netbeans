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
package org.netbeans.modules.web.client.tools.impl.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Customizer panel for the Debug category in the project properties dialog.
 * 
 * @author  Quy Nguyen <quynguyen@netbeans.org>
 */
public final class CustomizerDebug extends JPanel implements ActionListener {
    private final ProjectCustomizer.Category category;
    private final Project project;
    private final String debugServerMsg;
    private final String debugClientMsg;
    
    /** Creates new form CustomizerDebug */
    public CustomizerDebug(ProjectCustomizer.Category category, final Project project,
            String debugServerMsg, String debugClientMsg) {
        this.category = category;
        this.debugServerMsg = debugServerMsg;
        this.debugClientMsg = debugClientMsg;
        this.project = project;
        
        initComponents();
        
        boolean serverDebug = WebClientToolsProjectUtils.getServerDebugProperty(project);
        boolean clientDebug = WebClientToolsProjectUtils.getClientDebugProperty(project);
        
        this.debugServerJCheckBox.setSelected(serverDebug);
        this.debugClientJCheckBox.setSelected(clientDebug);
                
        firefoxRadioButton.setSelected(Utilities.isWindows() || WebClientToolsProjectUtils.isFirefox(project));
        internetExplorerRadioButton.setSelected((!Utilities.isWindows()) && WebClientToolsProjectUtils.isInternetExplorer(project));

        adjustBrowserRadioButtons();
        debugClientJCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                adjustBrowserRadioButtons();
            }
        });
        
        this.category.setStoreListener(this);
        validateCheckBoxes();
    }

    private void adjustBrowserRadioButtons() {
        firefoxRadioButton.setEnabled(debugClientJCheckBox.isSelected());
        internetExplorerRadioButton.setEnabled(Utilities.isWindows() && debugClientJCheckBox.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserButtonGroup = new javax.swing.ButtonGroup();
        debugServerJCheckBox = new javax.swing.JCheckBox();
        debugClientJCheckBox = new javax.swing.JCheckBox();
        firefoxRadioButton = new javax.swing.JRadioButton();
        internetExplorerRadioButton = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(debugServerJCheckBox, debugServerMsg);
        debugServerJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugServerActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(debugClientJCheckBox, debugClientMsg);
        debugClientJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugClientActionPerformed(evt);
            }
        });

        browserButtonGroup.add(firefoxRadioButton);
        firefoxRadioButton.setSelected(true);
        firefoxRadioButton.setText(org.openide.util.NbBundle.getMessage(CustomizerDebug.class, "CustomizerDebug.firefoxRadioButton.text")); // NOI18N

        browserButtonGroup.add(internetExplorerRadioButton);
        internetExplorerRadioButton.setText(org.openide.util.NbBundle.getMessage(CustomizerDebug.class, "CustomizerDebug.internetExplorerRadioButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(debugClientJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
            .add(debugServerJCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(internetExplorerRadioButton)
                    .add(firefoxRadioButton))
                .add(318, 318, 318))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(debugServerJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(debugClientJCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(firefoxRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(internetExplorerRadioButton)
                .add(397, 397, 397))
        );

        debugServerJCheckBox.getAccessibleContext().setAccessibleDescription("null");
        debugClientJCheckBox.getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents

private void debugServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugServerActionPerformed
//GEN-LAST:event_debugServerActionPerformed
    validateCheckBoxes();
}                                           

private void debugClientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugClientActionPerformed
//GEN-LAST:event_debugClientActionPerformed
    validateCheckBoxes();
}                                           

    private void validateCheckBoxes() {
        if (!debugClientJCheckBox.isSelected() && !debugServerJCheckBox.isSelected()) {
            category.setErrorMessage(NbBundle.getMessage(CustomizerDebug.class, "LBL_CustomizeDebug_NoDebug_Error")); // NOI18N
            category.setValid(false);
        } else {
            category.setErrorMessage(null);
            category.setValid(true);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup browserButtonGroup;
    private javax.swing.JCheckBox debugClientJCheckBox;
    private javax.swing.JCheckBox debugServerJCheckBox;
    private javax.swing.JRadioButton firefoxRadioButton;
    private javax.swing.JRadioButton internetExplorerRadioButton;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        WebClientToolsProjectUtils.setProjectProperties(project, debugServerJCheckBox.isSelected(), debugClientJCheckBox.isSelected(),
                (internetExplorerRadioButton.isSelected() ?
                    WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER : WebClientToolsProjectUtils.Browser.FIREFOX));
    }

}
