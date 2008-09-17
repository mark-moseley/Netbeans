/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * ShowSQLDialog.java
 *
 * Created on Jun 26, 2008, 1:24:53 AM
 */
package org.netbeans.modules.db.dataview.output;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ahimanikya Satapathy
 */
class ShowSQLDialog extends javax.swing.JDialog {

    /** Creates new form ShowSQLDialog */
    public ShowSQLDialog() {
        super(WindowManager.getDefault().getMainWindow(), true);
        initComponents();

        //jButton1.setFont(new java.awt.Font("Tahoma", 0, 16));
        //jButton1.setText("TESTING");
        Font font = jButton1.getFont();
        FontMetrics metrics = getFontMetrics(font);
        int width = metrics.stringWidth(jButton1.getText());
        int height = metrics.getHeight();        
        jButton1.setSize(width*2,height+height); 

        jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "showsql.editorpane.accessibleName"));
        jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "ShowSQLDialog.jEditorPane1.AccessibleContext.accessibleDescription"));

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "ShowSQLDialog.title"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "ShowSQLDialog.AccessibleContext.accessibleDescription"));

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE"); // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction); // NOI18N  
    }

    public void setText(String sqlScript) {
        jEditorPane1.setText(sqlScript);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "ShowSQLDialog.title")); // NOI18N

        jEditorPane1.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-sql"));
        jScrollPane1.setViewportView(jEditorPane1);

        jButton1.setFont(jButton1.getFont());
        jButton1.setText(org.openide.util.NbBundle.getMessage(ShowSQLDialog.class, "ShowSQLDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();                                        
    }                                        
    // Variables declaration - do not modify                                             
    private javax.swing.JButton jButton1;//GEN-HEADEREND:event_jButton1ActionPerformed
    private javax.swing.JEditorPane jEditorPane1;//GEN-LAST:event_jButton1ActionPerformed
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration                   
}
