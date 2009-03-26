/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ConfirmationPanel.java
 *
 * Created on 26-Mar-2009, 18:07:26
 */

package org.netbeans.modules.maven.jaxws;

/**
 *
 * @author mkuchtiak
 */
public class ConfirmationPanel extends javax.swing.JPanel {

    /** Creates new form ConfirmationPanel */
    public ConfirmationPanel(String text) {
        initComponents();
        textLabel.setText(text);
    }
    
    /** Creates new form ConfirmationPanel */
    public ConfirmationPanel() {
        initComponents();
    }

    boolean notAskAgain() {
        return notAskAgain.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textLabel = new javax.swing.JLabel();
        notAskAgain = new javax.swing.JCheckBox();

        textLabel.setText(org.openide.util.NbBundle.getMessage(ConfirmationPanel.class, "MSG_GenerateDDEntries")); // NOI18N

        notAskAgain.setText(org.openide.util.NbBundle.getMessage(ConfirmationPanel.class, "ConfirmationPanel.dontAskAgain.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(notAskAgain)
                    .add(textLabel))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(textLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 54, Short.MAX_VALUE)
                .add(notAskAgain)
                .add(29, 29, 29))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox notAskAgain;
    private javax.swing.JLabel textLabel;
    // End of variables declaration//GEN-END:variables

}
