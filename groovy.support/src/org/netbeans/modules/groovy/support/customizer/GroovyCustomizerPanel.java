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

package org.netbeans.modules.groovy.support.customizer;

import org.netbeans.modules.groovy.support.GroovyProjectExtender;

/**
 *
 * @author Martin Adamek
 */
public class GroovyCustomizerPanel extends javax.swing.JPanel {

    public GroovyCustomizerPanel(GroovyProjectExtender extender) {
        initComponents();
        if (extender == null || !extender.isGroovyEnabled()) {
            enableGroovyCheckBox.setSelected(false);
        } else {
            enableGroovyCheckBox.setSelected(true);
        }
    }

    public boolean isEnablingCheckboxSelected() {
        return enableGroovyCheckBox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enableGroovyCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(enableGroovyCheckBox, org.openide.util.NbBundle.getMessage(GroovyCustomizerPanel.class, "GroovyCustomizerPanel.enableGroovyCheckBox.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GroovyCustomizerPanel.class, "GroovyCustomizerPanel.jLabel1.text")); // NOI18N
        jLabel1.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(enableGroovyCheckBox)
                .add(25, 25, 25))
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(enableGroovyCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        enableGroovyCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GroovyCustomizerPanel.class, "GroovyCustomizerPanel.enableGroovyCheckBox.accessibleName")); // NOI18N
        enableGroovyCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GroovyCustomizerPanel.class, "GroovyCustomizerPanel.enableGroovyCheckBox.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox enableGroovyCheckBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}
