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
package org.netbeans.modules.options.export;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Panel for import confirmation.
 * 
 * @author Jiri Skrivanek
 */
public class ImportConfirmationPanel extends javax.swing.JPanel {

    private boolean confirmed = false;

    /** Creates new form ImportConfirmationPanel */
    public ImportConfirmationPanel() {
        initComponents();
        Mnemonics.setLocalizedText(cbRestart, NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.cbRestart.text"));
        String message = NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.lblMessage.text"); // NOI18N
        lblMessage.setText("<html>" + message + "</html>");  //NOI18N
    }

    /** Opens confirmation dialog. */
    void showConfirmation() {
        DialogDescriptor dd = new DialogDescriptor(
                this,
                NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.title"),
                true,
                DialogDescriptor.YES_NO_OPTION,
                DialogDescriptor.YES_OPTION,
                null);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            confirmed = true;
        } else {
            confirmed = false;
        }
    }

    /** Returns true if user click OK. */
    boolean confirmed() {
        return confirmed;
    }

    /** Returns true if the restart now check box is selected. */
    boolean restartNow() {
        return cbRestart.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbRestart = new javax.swing.JCheckBox();
        lblMessage = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(332, 90));
        setLayout(new java.awt.BorderLayout());

        cbRestart.setSelected(true);
        cbRestart.setText(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.cbRestart.text")); // NOI18N
        add(cbRestart, java.awt.BorderLayout.SOUTH);
        cbRestart.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.cbRestart.AN")); // NOI18N
        cbRestart.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.cbRestart.AD")); // NOI18N

        lblMessage.setText(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.lblMessage.text")); // NOI18N
        add(lblMessage, java.awt.BorderLayout.NORTH);
        lblMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.lblMessage.text")); // NOI18N
        lblMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.lblMessage.AD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.AN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportConfirmationPanel.class, "ImportConfirmationPanel.AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbRestart;
    private javax.swing.JLabel lblMessage;
    // End of variables declaration//GEN-END:variables

}
