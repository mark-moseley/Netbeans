/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uihandler;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author  jarda
 */
public class SubmitPanel extends javax.swing.JPanel 
implements ExplorerManager.Provider {
    
    /** Creates new form SubmitPanel */
    public SubmitPanel() {
        manager = new ExplorerManager();
        
        initComponents();
        view.setProperties(new Node.Property[] { 
            UINode.createPropertyDate(null),
            UINode.createPropertyLogger(null),
            UINode.createPropertyMessage(null),
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        view = new org.openide.explorer.view.TreeTableView();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        propertySheetView1 = new org.openide.explorer.propertysheet.PropertySheetView();

        jTextArea1.setBackground(getBackground());
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(SubmitPanel.class, "CTL_SummaryDescription")); // NOI18N
        jTextArea1.setEnabled(false);
        jScrollPane1.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 891, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(view, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(propertySheetView1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 265, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(view, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, propertySheetView1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private org.openide.explorer.propertysheet.PropertySheetView propertySheetView1;
    private org.openide.explorer.view.TreeTableView view;
    // End of variables declaration//GEN-END:variables

    private ExplorerManager manager;
    
}
