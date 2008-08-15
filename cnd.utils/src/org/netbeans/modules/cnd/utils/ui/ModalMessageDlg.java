/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ModalMessageDlg extends javax.swing.JPanel {

    /** Creates new form ModalMessageDlg */
    public ModalMessageDlg() {
        initComponents();
    }

    /**
     * allows to display modal dialog with title and message for the period of
     * long task run
     * @param parent parent frame or dialog
     * @param workTask non EDT task to run
     * @param postEDTTask EDT task to run after closing modal dialog (can be null)
     * @param title title of dialog
     * @param message message in dialog
     * @return
     */
    public static Task runLongTask(Dialog parent, final Runnable workTask, final Runnable postEDTTask, String title, String message) {
        return runLongTaskImpl(parent, workTask, postEDTTask, title, message);
    }
    public static Task runLongTask(Frame parent, final Runnable workTask, final Runnable postEDTTask, String title, String message) {
        return runLongTaskImpl(parent, workTask, postEDTTask, title, message);
    }
    private static Task runLongTaskImpl(Window parent, final Runnable workTask, final Runnable postEDTTask, String title, String message) {
        final JDialog dialog;
        if (parent instanceof Frame) {
            dialog = new JDialog((Frame)parent, title, true);           
        } else {
            assert (parent instanceof Dialog);
            dialog = new JDialog((Dialog)parent, title, true);
        }
        final ModalMessageDlg panel = new ModalMessageDlg();

        dialog.getContentPane().add(panel);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); //make sure the dialog is not closed during the project open
        dialog.pack();

        Rectangle bounds = parent.getBounds();

        int middleX = bounds.x + bounds.width / 2;
        int middleY = bounds.y + bounds.height / 2;

        Dimension size = dialog.getPreferredSize();

        dialog.setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
        panel.setMessage(message);
        Task post = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    workTask.run();
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // hide dialog and run action if successfully connected
                            dialog.setVisible(false);
                            dialog.dispose();
                            if (postEDTTask != null) {
                                postEDTTask.run();
                            }
                        }
                    });
                }
            }
        });
        dialog.setVisible(true);
        return post;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblMessage = new javax.swing.JLabel();

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .add(lblMessage)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblMessage;
    // End of variables declaration//GEN-END:variables

    public void setMessage(String msg) {
        lblMessage.setText(msg);
    }
}
