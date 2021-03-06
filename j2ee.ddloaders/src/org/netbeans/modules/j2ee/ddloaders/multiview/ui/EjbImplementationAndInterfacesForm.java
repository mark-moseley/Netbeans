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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ddloaders.multiview.ui;

import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesForm extends SectionNodeInnerPanel {

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesForm(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        beanClassTextField = new javax.swing.JTextField();
        localComponentTextField = new javax.swing.JTextField();
        localHomeTextField = new javax.swing.JTextField();
        remoteComponentTextField = new javax.swing.JTextField();
        remoteHomeTextField = new javax.swing.JTextField();
        spacerLabel = new javax.swing.JLabel();
        beanClassLinkButton = new javax.swing.JButton();
        localComponentLinkButton = new javax.swing.JButton();
        localHomeLinkButton = new javax.swing.JButton();
        remoteComponentLinkButton = new javax.swing.JButton();
        remoteHomeLinkButton = new javax.swing.JButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_BeanClass")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_LocalInterface")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_Component")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_Home")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_RemoteInterface")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_Component")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_Home")); // NOI18N

        beanClassTextField.setColumns(35);

        localComponentTextField.setColumns(35);

        localHomeTextField.setColumns(35);

        remoteComponentTextField.setColumns(35);

        remoteHomeTextField.setColumns(35);

        spacerLabel.setText(" ");

        org.openide.awt.Mnemonics.setLocalizedText(beanClassLinkButton, org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_GoToSource")); // NOI18N
        beanClassLinkButton.setBorderPainted(false);
        beanClassLinkButton.setContentAreaFilled(false);
        beanClassLinkButton.setFocusPainted(false);
        beanClassLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(localComponentLinkButton, org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_GoToSource")); // NOI18N
        localComponentLinkButton.setBorderPainted(false);
        localComponentLinkButton.setContentAreaFilled(false);
        localComponentLinkButton.setFocusPainted(false);
        localComponentLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(localHomeLinkButton, org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_GoToSource")); // NOI18N
        localHomeLinkButton.setBorderPainted(false);
        localHomeLinkButton.setContentAreaFilled(false);
        localHomeLinkButton.setFocusPainted(false);
        localHomeLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(remoteComponentLinkButton, org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_GoToSource")); // NOI18N
        remoteComponentLinkButton.setBorderPainted(false);
        remoteComponentLinkButton.setContentAreaFilled(false);
        remoteComponentLinkButton.setFocusPainted(false);
        remoteComponentLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        org.openide.awt.Mnemonics.setLocalizedText(remoteHomeLinkButton, org.openide.util.NbBundle.getMessage(EjbImplementationAndInterfacesForm.class, "LBL_GoToSource")); // NOI18N
        remoteHomeLinkButton.setBorderPainted(false);
        remoteHomeLinkButton.setContentAreaFilled(false);
        remoteHomeLinkButton.setFocusPainted(false);
        remoteHomeLinkButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(spacerLabel)
                .add(5, 5, 5)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(beanClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(beanClassLinkButton))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(36, 36, 36))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(localComponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(localComponentLinkButton))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(localHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(localHomeLinkButton))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel5)
                .add(36, 36, 36))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(remoteComponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(remoteComponentLinkButton))
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(remoteHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(remoteHomeLinkButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spacerLabel)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jLabel1))
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(beanClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(beanClassLinkButton)))
                .add(7, 7, 7)
                .add(jLabel2)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel3))
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(localComponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(localComponentLinkButton))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel4))
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(localHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(localHomeLinkButton))
                .add(7, 7, 7)
                .add(jLabel5)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel6))
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(remoteComponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(remoteComponentLinkButton))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel7))
                    .add(layout.createSequentialGroup()
                        .add(3, 3, 3)
                        .add(remoteHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(remoteHomeLinkButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton beanClassLinkButton;
    private javax.swing.JTextField beanClassTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JButton localComponentLinkButton;
    private javax.swing.JTextField localComponentTextField;
    private javax.swing.JButton localHomeLinkButton;
    private javax.swing.JTextField localHomeTextField;
    private javax.swing.JButton remoteComponentLinkButton;
    private javax.swing.JTextField remoteComponentTextField;
    private javax.swing.JButton remoteHomeLinkButton;
    private javax.swing.JTextField remoteHomeTextField;
    private javax.swing.JLabel spacerLabel;
    // End of variables declaration//GEN-END:variables

    public JTextField getBeanClassTextField() {
        return beanClassTextField;
    }

    public JTextField getLocalComponentTextField() {
        return localComponentTextField;
    }

    public JTextField getLocalHomeTextField() {
        return localHomeTextField;
    }

    public JTextField getRemoteComponentTextField() {
        return remoteComponentTextField;
    }

    public JTextField getRemoteHomeTextField() {
        return remoteHomeTextField;
    }

    public JComponent getErrorComponent(String errorId) {
        return null;
    }

    public void setValue(JComponent source, Object value) {

    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {

    }

    public JButton getBeanClassLinkButton() {
        return beanClassLinkButton;
    }

    public JButton getLocalComponentLinkButton() {
        return localComponentLinkButton;
    }

    public JButton getLocalHomeLinkButton() {
        return localHomeLinkButton;
    }

    public JButton getRemoteComponentLinkButton() {
        return remoteComponentLinkButton;
    }

    public JButton getRemoteHomeLinkButton() {
        return remoteHomeLinkButton;
    }
}
