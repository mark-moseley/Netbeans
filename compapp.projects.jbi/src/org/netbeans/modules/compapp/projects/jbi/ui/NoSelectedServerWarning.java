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


package org.netbeans.modules.compapp.projects.jbi.ui;

import java.awt.Component;
import javax.swing.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

/** Show a warning that no server is set and allows choose it.
 *
 * @author  Pavel Buzek
 */
public class NoSelectedServerWarning extends JPanel {

    public NoSelectedServerWarning (String[] serverIDs) {
        initComponents();
        // add MainClassChooser
        jList1.setModel(new ServerListModel (serverIDs));
        jList1.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(new ServersRenderer ());
        jList1.setSelectedIndex(0);
    }

    /** Returns the selected server instance Id or null if no instance was selected.
     *
     * @return server instance ID or null if no instance is selected
     */
    public String getSelectedInstance () {
        if (jList1.getSelectedIndex () == -1) {
            return null;
        } else {
            return (String)jList1.getSelectedValue ();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_NoSelectedServerWarning_jLabel1")); // NOI18N

        jLabel2.setLabelFor(jList1);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NoSelectedServerWarning.class, "LBL_NoSelectedServerWarning_jLabel2")); // NOI18N

        jScrollPane2.setMinimumSize(new java.awt.Dimension(100, 200));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(NoSelectedServerWarning.class).getString("ACS_NoSelectedServerWarning_jLabel2_A11YDesc")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(11, 11, 11)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(31, 31, 31))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables


    private static final class ServerListModel extends AbstractListModel {

        private String instances [];

        public ServerListModel (String[] serverIDs) {
            this.instances = serverIDs;
        }

        public synchronized int getSize() {
            return instances.length;
        }

        public synchronized Object getElementAt (int index) {
            if (index >= 0 && index < instances.length) {
                return instances [index];
            }
            else {
                return null;
            }
        }

    }

    private static final class ServersRenderer extends JLabel implements ListCellRenderer {
        ServersRenderer () {
            setOpaque (true);
        }

        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof String) {
                String id = (String) value;
                setText (Deployment.getDefault ().getServerInstanceDisplayName (id));
//                setIcon (ProjectUtils.getInformation (prj).getIcon ());
            } else {
                setText (value.toString ());
                setIcon (null);
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                //setBorder (BorderFactory.createLineBorder (Color.BLACK));
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                //setBorder (null);
            }
            return this;
        }
    }

}
