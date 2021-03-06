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
  *
 * Portions Copyrighted 2008 Craig MacKay.
*/

package org.netbeans.modules.spring.beans.wizards;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle;

public final class SpringXMLConfigNamespacesVisual extends JPanel {

    /** Creates new form NewBeansContextVisualPanel1 */
    public SpringXMLConfigNamespacesVisual() {
        initComponents();
        
        TableColumn col1 = includesTable.getColumnModel().getColumn(0);
        col1.setMaxWidth(60);
        includesTable.revalidate();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SpringXMLConfigNamespacesVisual.class, "LBL_Namespaces_Include_Step");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        includesTable = new javax.swing.JTable();

        includesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, "aop - http://www.springframework.org/schema/aop/spring-aop-2.5.xsd"},
                {null, "context - http://www.springframework.org/schema/context/spring-context-2.5.xsd"},
                {null, "flow - http://www.springframework.org/schema/webflow-config/spring-webflow-config-1.0.xsd"},
                {null, "jms - http://www.springframework.org/schema/jms/spring-jms-2.5.xsd"},
                {null, "jee - http://www.springframework.org/schema/jee/spring-jee-2.5.xsd"},
                {null, "lang - http://www.springframework.org/schema/lang/spring-lang-2.5.xsd"},
                {null, "osgi - http://www.springframework.org/schema/osgi/spring-osgi.xsd"},
                {null, "tx - http://www.springframework.org/schema/tx/spring-tx-2.5.xsd"},
                {null, "util - http://www.springframework.org/schema/util/spring-util-2.5.xsd"},
                {null, "p - http://www.springframework.org/schema/p"}
            },
            new String [] {
                "Include", "Namespace"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        includesTable.setShowGrid(false);
        includesTable.setDragEnabled(false);
        includesTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jScrollPane1.setViewportView(includesTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable includesTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    public String[] getIncludedNamespaces() {
        List<String> incs = new ArrayList<String>();
        TableModel model = includesTable.getModel();
        
        for(int i = 0; i < model.getRowCount(); i++) {
            Boolean selected = (Boolean) model.getValueAt(i, 0);
            if(selected != null && selected == Boolean.TRUE) {
                String namespace = (String) model.getValueAt(i, 1);
                incs.add(namespace);
            }
        }
        return incs.toArray(new String[0]);
    }
}

