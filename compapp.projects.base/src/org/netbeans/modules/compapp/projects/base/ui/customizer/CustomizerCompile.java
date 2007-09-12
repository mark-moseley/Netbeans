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


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import org.openide.util.NbBundle;

public class CustomizerCompile extends javax.swing.JPanel implements IcanproCustomizer.Panel {

    private VisualPropertySupport vps;
    private VisualClasspathSupport vcs;

    /** Creates new form CustomizerCompile */
    public CustomizerCompile(IcanproProjectProperties webProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_A11YDesc")); //NOI18N

        vps = new VisualPropertySupport(webProperties);
        vcs = new VisualClasspathSupport(
            webProperties.getProject(),
            jProjList,
            jButtonAddProject,
            jButtonRemove,
            jButtonMoveUp,
            jButtonMoveDown);
    }

    public void initValues() {
        vps.register(vcs, IcanproProjectProperties.JAVAC_CLASSPATH);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabelClasspath = new javax.swing.JLabel();
        jScrollClasspath = new javax.swing.JScrollPane();
        jProjList = new javax.swing.JList();
        jButtonAddProject = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();
        jButtonMoveUp = new javax.swing.JButton();
        jButtonMoveDown = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        jLabelClasspath.setLabelFor(jProjList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelClasspath, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Classpath_JLabel"));

        jScrollClasspath.setPreferredSize(new java.awt.Dimension(258, 130));
        jScrollClasspath.setViewportView(jProjList);
        jProjList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_Jprojlist_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddProject, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Classpath_AddProject_JButton"));
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_AddProject_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemove, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Classpath_Remove_JButton"));
        jButtonRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_Remove_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUp, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Classpath_MoveUp_JButton"));
        jButtonMoveUp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_Up_A11YDesc"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDown, org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Classpath_MoveDown_JButton"));
        jButtonMoveDown.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerCompile.class, "ACS_CustomizeCompile_Down_A11YDesc"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollClasspath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jButtonAddProject)
                            .add(jButtonRemove)
                            .add(jButtonMoveUp)
                            .add(jButtonMoveDown))
                        .add(20, 20, 20))
                    .add(layout.createSequentialGroup()
                        .add(jLabelClasspath)
                        .addContainerGap(306, Short.MAX_VALUE))))
        );

        layout.linkSize(new java.awt.Component[] {jButtonAddProject, jButtonMoveDown, jButtonMoveUp, jButtonRemove}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelClasspath)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButtonAddProject)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonMoveUp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonMoveDown))
                    .add(jScrollClasspath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonMoveDown;
    private javax.swing.JButton jButtonMoveUp;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JLabel jLabelClasspath;
    private javax.swing.JList jProjList;
    private javax.swing.JScrollPane jScrollClasspath;
    // End of variables declaration//GEN-END:variables

}
