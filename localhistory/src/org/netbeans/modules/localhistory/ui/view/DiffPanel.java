/*
 * DiffPanel.java
 *
 * Created on February 8, 2007, 2:24 PM
 */

package org.netbeans.modules.localhistory.ui.view;

/**
 *
 * @author  tomas
 */
public class DiffPanel extends javax.swing.JPanel {
    
    /** Creates new form DiffPanel */
    public DiffPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        diffPanel.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setBorderPainted(false);

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 1));
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/localhistory/resources/icons/diff-next.png")));
        nextButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.nextButton.text")); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.nextButton.toolTipText")); // NOI18N
        nextButton.setAlignmentX(0.5F);
        nextButton.setBorder(null);
        nextButton.setBorderPainted(false);
        nextButton.setEnabled(false);
        jToolBar1.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/localhistory/resources/icons/diff-prev.png")));
        prevButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.prevButton.text")); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.prevButton.toolTipText")); // NOI18N
        prevButton.setAlignmentX(0.5F);
        prevButton.setBorder(null);
        prevButton.setBorderPainted(false);
        prevButton.setEnabled(false);
        jToolBar1.add(prevButton);

        jPanel4.setMaximumSize(new java.awt.Dimension(32767, 1));
        jPanel4.setPreferredSize(new java.awt.Dimension(100, 1));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(diffPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 749, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(diffPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JPanel diffPanel = new javax.swing.JPanel();
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JToolBar jToolBar1;
    final javax.swing.JButton nextButton = new javax.swing.JButton();
    final javax.swing.JButton prevButton = new javax.swing.JButton();
    // End of variables declaration//GEN-END:variables
    
}
