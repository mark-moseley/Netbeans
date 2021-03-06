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

package org.netbeans.modules.websvc.core.webservices.ui;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class DeleteWsDialog extends javax.swing.JPanel {

    public static final String DELETE_NOTHING = "deleteNothing"; //NOI18N
    public static final String DELETE_ALL = "deleteALL"; //NOI18N
    public static final String DELETE_WS = "deleteWebService"; //NOI18N
    public static final String DELETE_PACKAGE = "deletePackage"; //NOI18N
    public static final String DELETE_WSDL = "deleteWsdl"; //NOI18N
    
    private String wsName, packageName, wsdlName;
    
    private DeleteWsDialog(String wsName, String packageName, String wsdlName) {
        this.wsName = wsName;
        this.packageName=packageName;
        this.wsdlName=wsdlName;
        initComponents();
        // display the delete_wsdl checkbox only if wsdl exists
        if (wsdlName==null) deleteWsdlCheckBox.setVisible(false);
    }

    public static String open(String wsName, String packageName, String wsdlName) {
        String title = NbBundle.getMessage(DeleteWsDialog.class, "MSG_ConfirmDeleteObjectTitle");
        DeleteWsDialog delDialog = new DeleteWsDialog(wsName, packageName, wsdlName);
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(delDialog, title, NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.CLOSED_OPTION)) {
            return DELETE_NOTHING;
        } else if (result.equals(NotifyDescriptor.NO_OPTION)) {
            return DELETE_NOTHING;
        } else if (delDialog.deletePackage() && delDialog.deleteWsdl()) {
            return DELETE_ALL;
        } else if (delDialog.deletePackage()) {
            return DELETE_PACKAGE;
        } else if (delDialog.deleteWsdl()) {
            return DELETE_WSDL;
        } else return DELETE_WS;
        
    }
    
    private boolean deletePackage() {
        return deletePackageCheckBox.isSelected();
    }
    
    private boolean deleteWsdl() {
        if (wsdlName==null) return false;
        else return deleteWsdlCheckBox.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        deletePackageCheckBox = new javax.swing.JCheckBox();
        deleteWsdlCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_ConfirmDeleteObject", new Object[] {wsName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deletePackageCheckBox, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_DeletePackage", new Object[] {packageName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(deletePackageCheckBox, gridBagConstraints);

        deleteWsdlCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(deleteWsdlCheckBox, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_DeleteWsdl", new Object[] {wsdlName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(deleteWsdlCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deletePackageCheckBox;
    private javax.swing.JCheckBox deleteWsdlCheckBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
