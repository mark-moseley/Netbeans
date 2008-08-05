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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import javax.swing.JPanel;

/**
 *
 * @author  thp
 */
public class PackagingInfoOuterPanel extends javax.swing.JPanel {

    /** Creates new form PackagingInfo2Panel */
    public PackagingInfoOuterPanel(PackagingInfoPanel innerPanel) {
        java.awt.GridBagConstraints gridBagConstraints;
        
        initComponents();
        
        remove(packagingHeaderOuterPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(innerPanel, gridBagConstraints);
        
        innerPanel.setDocArea(docTextArea);
        innerPanel.refresh();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        packagingHeaderOuterPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        docTextArea = new javax.swing.JTextArea();
        docTextArea.setBackground(getBackground());

        setLayout(new java.awt.GridBagLayout());

        org.jdesktop.layout.GroupLayout packagingHeaderOuterPanelLayout = new org.jdesktop.layout.GroupLayout(packagingHeaderOuterPanel);
        packagingHeaderOuterPanel.setLayout(packagingHeaderOuterPanelLayout);
        packagingHeaderOuterPanelLayout.setHorizontalGroup(
            packagingHeaderOuterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 392, Short.MAX_VALUE)
        );
        packagingHeaderOuterPanelLayout.setVerticalGroup(
            packagingHeaderOuterPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 203, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(packagingHeaderOuterPanel, gridBagConstraints);

        scrollPane.setBorder(null);

        docTextArea.setColumns(20);
        docTextArea.setEditable(false);
        docTextArea.setLineWrap(true);
        docTextArea.setRows(5);
        docTextArea.setWrapStyleWord(true);
        scrollPane.setViewportView(docTextArea);
        docTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingInfoOuterPanel.class, "PackagingInfoOuterPanel.docTextArea.AccessibleContext.accessibleName")); // NOI18N
        docTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingInfoOuterPanel.class, "PackagingInfoOuterPanel.docTextArea.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 4);
        add(scrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea docTextArea;
    private javax.swing.JPanel packagingHeaderOuterPanel;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
