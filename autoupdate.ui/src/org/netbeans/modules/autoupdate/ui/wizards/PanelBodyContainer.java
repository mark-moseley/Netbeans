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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui.wizards;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author  Jiri Rechtacek
 */
public class PanelBodyContainer extends javax.swing.JPanel {
    private String head = null;
    private String message = null;
    private JScrollPane customPanel;
    private JPanel bodyPanel = null;
    
    /** Creates new form InstallPanelContainer */
    public PanelBodyContainer (String heading, String msg, JPanel bodyPanel) {
        head = heading;
        message = msg;
        this.bodyPanel = bodyPanel;
        customPanel = new JScrollPane ();
        customPanel.setBorder (null);
        initComponents ();
        pBodyPanel.add (customPanel, BorderLayout.CENTER);
        writeToHeader (head, message);
        customPanel.setViewportView (bodyPanel);
        customPanel.getVerticalScrollBar ().setUnitIncrement (10);
        customPanel.getHorizontalScrollBar ().setUnitIncrement (10);
    }
    
    @Override
    public void addNotify () {
        super.addNotify();
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                bodyPanel.scrollRectToVisible (new Rectangle (0, 0, 10, 10));
            }
        });
    }
    
    public void setHeadAndContent (String heading, String content) {
        writeToHeader (heading, content);
    }
    
    private void writeToHeader (String heading, String msg) {
        tpPanelHeader.setText ("<b>" + heading + "</b> <br>" + msg); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        pBodyPanel = new javax.swing.JPanel();
        spPanelHeader = new javax.swing.JScrollPane();
        tpPanelHeader = new javax.swing.JTextPane();

        pBodyPanel.setLayout(new java.awt.BorderLayout());

        tpPanelHeader.setBackground(java.awt.SystemColor.controlLtHighlight);
        tpPanelHeader.setContentType(org.openide.util.NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer.tpPanelHeader.contentType")); // NOI18N
        tpPanelHeader.setEditable(false);
        spPanelHeader.setViewportView(tpPanelHeader);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pBodyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, spPanelHeader, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(spPanelHeader, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pBodyPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pBodyPanel;
    private javax.swing.JScrollPane spPanelHeader;
    private javax.swing.JTextPane tpPanelHeader;
    // End of variables declaration//GEN-END:variables
    
}
