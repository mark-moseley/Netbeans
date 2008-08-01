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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.actions.GoToSourceSupport;

/**
 *
 * @author  Karol Harezlak
 */
class GoToSourceCPE extends javax.swing.JPanel {

    private WeakReference<DesignComponent> component;

    /** Creates new form GoToSourceCPE */
    public GoToSourceCPE(WeakReference<DesignComponent> component) {
        this.component = component;
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        goToSourceButton = new javax.swing.JButton();
        noteLabel = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(350, 80));
        setPreferredSize(new java.awt.Dimension(350, 80));

        org.openide.awt.Mnemonics.setLocalizedText(goToSourceButton, org.openide.util.NbBundle.getMessage(GoToSourceCPE.class, "LBL_GoToSourceButtonText")); // NOI18N
        goToSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToSourceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, org.openide.util.NbBundle.getMessage(GoToSourceCPE.class, "LBL_GoToSourceNote")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(noteLabel)
                    .add(goToSourceButton))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(goToSourceButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(noteLabel)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        goToSourceButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GoToSourceCPE.class, "ACSN_GoToSource")); // NOI18N
        goToSourceButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToSourceCPE.class, "ACSD_GoToSource")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void goToSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToSourceButtonActionPerformed
        GoToSourceSupport.goToSourceOfComponent(component.get());
}//GEN-LAST:event_goToSourceButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton goToSourceButton;
    private javax.swing.JLabel noteLabel;
    // End of variables declaration//GEN-END:variables
    
}
