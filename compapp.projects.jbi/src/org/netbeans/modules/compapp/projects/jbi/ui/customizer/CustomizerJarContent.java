/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** 
 * Customizer for WAR packaging.
 */
public class CustomizerJarContent extends JPanel 
        implements JbiJarCustomizer.Panel, HelpCtx.Provider {

    private Dialog dialog;
    JbiProjectProperties jbiProperties;
    private VisualPropertySupport vps;
    private VisualArchiveIncludesSupport vas;
    private ActionListener actionListener;
    
    /** Creates new form CustomizerCompile */
    public CustomizerJarContent(JbiProjectProperties jbiProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeWAR_A11YDesc")); // NOI18N

        this.jbiProperties = jbiProperties;
        vps = new VisualPropertySupport(jbiProperties);
        vas = new VisualArchiveIncludesSupport(jbiProperties,
                                            jTableTargetComponents,
                                            jTableDeploymentArtifacts,
                                            jButtonCheckServer,
                                            jButtonAddProject,
                                            jButtonRemoveProject);
    }

    public void initValues() {
        //vps.register(jTextFieldFileName, JbiProjectProperties.DIST_JAR);
        jTextFieldBuildArtifactName.setDocument(jbiProperties.DIST_JAR_MODEL);
        vas.initTableValues();
        vps.register(vas, JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelBuildArtifactName = new javax.swing.JLabel();
        jTextFieldBuildArtifactName = new javax.swing.JTextField();
        jLabelTargetComponents = new javax.swing.JLabel();
        jScrollPaneTargetComponents = new javax.swing.JScrollPane();
        jTableTargetComponents = new javax.swing.JTable();
        jButtonCheckServer = new javax.swing.JButton();
        jLabelDeploymentArtifacts = new javax.swing.JLabel();
        jScrollPaneDeploymentArtifacts = new javax.swing.JScrollPane();
        jTableDeploymentArtifacts = new javax.swing.JTable();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemoveProject = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelBuildArtifactName.setLabelFor(jTextFieldBuildArtifactName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelBuildArtifactName, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_FileName_JLabel")); // NOI18N

        jTextFieldBuildArtifactName.setEditable(false);

        jLabelTargetComponents.setLabelFor(jTableTargetComponents);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTargetComponents, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_Content_JLabel")); // NOI18N

        jTableTargetComponents.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"engine", "BPEL Service Engine"},
                {"binding", "HTTP SOAP Binding Component"}
            },
            new String [] {
                "Type", "Component ID"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneTargetComponents.setViewportView(jTableTargetComponents);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonCheckServer, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_CheckComponentsOnTargetServer_JButton")); // NOI18N

        jLabelDeploymentArtifacts.setLabelFor(jTableDeploymentArtifacts);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelDeploymentArtifacts, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_AddContent_JLabel")); // NOI18N

        jTableDeploymentArtifacts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPaneDeploymentArtifacts.setViewportView(jTableDeploymentArtifacts);
        jTableDeploymentArtifacts.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_AACH_ProjectJarFiles_JLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_AddProject_JButton")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveProject, org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "LBL_CustomizeWAR_Remove_JButton")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelDeploymentArtifacts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPaneTargetComponents, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPaneDeploymentArtifacts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jButtonAddProject)
                                .add(jButtonRemoveProject))
                            .add(jButtonCheckServer)))
                    .add(layout.createSequentialGroup()
                        .add(jLabelBuildArtifactName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextFieldBuildArtifactName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE))
                    .add(jLabelTargetComponents))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jButtonAddProject, jButtonCheckServer, jButtonRemoveProject}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelBuildArtifactName)
                    .add(jTextFieldBuildArtifactName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(jLabelTargetComponents)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButtonCheckServer)
                    .add(jScrollPaneTargetComponents, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                .add(11, 11, 11)
                .add(jLabelDeploymentArtifacts)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPaneDeploymentArtifacts, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jButtonAddProject)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRemoveProject)))
                .addContainerGap())
        );

        jLabelBuildArtifactName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "ACS_SERVICE_ASSEMBLY_BUILD_ARTIFACT")); // NOI18N
        jTextFieldBuildArtifactName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJarContent.class).getString("ACS_CustomizeWAR_FileName_A11YDesc")); // NOI18N
        jLabelTargetComponents.getAccessibleContext().setAccessibleDescription("List of Target JBI Components");
        jButtonCheckServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeWAR_AddFilter_A11YDesc")); // NOI18N
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeWAR_AddProject_A11YDesc")); // NOI18N
        jButtonRemoveProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJarContent.class, "ACS_CustomizeWAR_AdditionalRemove_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonCheckServer;
    private javax.swing.JButton jButtonRemoveProject;
    private javax.swing.JLabel jLabelBuildArtifactName;
    private javax.swing.JLabel jLabelDeploymentArtifacts;
    private javax.swing.JLabel jLabelTargetComponents;
    private javax.swing.JScrollPane jScrollPaneDeploymentArtifacts;
    private javax.swing.JScrollPane jScrollPaneTargetComponents;
    private javax.swing.JTable jTableDeploymentArtifacts;
    private javax.swing.JTable jTableTargetComponents;
    private javax.swing.JTextField jTextFieldBuildArtifactName;
    // End of variables declaration//GEN-END:variables

    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerJarContent.class);
    }    
}
