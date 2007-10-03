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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import org.netbeans.modules.j2ee.ejbcore.ui.EJBPreferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class DeleteEJBDialog extends javax.swing.JPanel {

    public static final String DELETE_NOTHING = "deleteNothing";
    public static final String DELETE_ONLY_DD = "deleteOnlyDD";
    public static final String DELETE_ALL = "deleteAll";

    private final String ejbDisplayName;
    private final EJBPreferences ejbPreferences;
    
    private DeleteEJBDialog(String ejbDisplayName) {
        this.ejbDisplayName = ejbDisplayName;
        initComponents();
        ejbPreferences = new EJBPreferences();
        deleteClassesCheckBox.setSelected(ejbPreferences.isAgreedDeleteEJBGeneratedSources());
    }

    public static String open(String ejbDisplayName) {
        String title = NbBundle.getMessage(DeleteEJBDialog.class, "MSG_ConfirmDeleteObjectTitle");
        DeleteEJBDialog delDialog = new DeleteEJBDialog(ejbDisplayName);
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(delDialog, title, NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.CLOSED_OPTION)) {
            return DELETE_NOTHING;
        } else if (result.equals(NotifyDescriptor.NO_OPTION)) {
            return DELETE_NOTHING;
        } else if (delDialog.deleteClassesCheckBox.isSelected()) {
            return DELETE_ALL;
        } else {
            return DELETE_ONLY_DD;
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        deleteClassesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DeleteEJBDialog.class, "MSG_ConfirmDeleteObject", new Object[] {ejbDisplayName}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jLabel1, gridBagConstraints);

        deleteClassesCheckBox.setText(org.openide.util.NbBundle.getBundle(DeleteEJBDialog.class).getString("MSG_DeleteClasses"));
        deleteClassesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteClassesCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        add(deleteClassesCheckBox, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void deleteClassesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteClassesCheckBoxActionPerformed
        ejbPreferences.setAgreedDeleteEJBGeneratedSources(deleteClassesCheckBox.isSelected());
    }//GEN-LAST:event_deleteClassesCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deleteClassesCheckBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
