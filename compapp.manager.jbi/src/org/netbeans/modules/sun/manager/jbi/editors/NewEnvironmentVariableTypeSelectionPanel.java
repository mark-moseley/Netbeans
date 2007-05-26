/*
 * NewEnvironmentVariableTypeSelectionPanel.java
 *
 * Created on May 25, 2007, 4:56 PM
 */

package org.netbeans.modules.sun.manager.jbi.editors;

/**
 *
 * @author  jqian
 */
public class NewEnvironmentVariableTypeSelectionPanel extends javax.swing.JPanel {
    
    /** Creates new form NewEnvironmentVariableTypeSelectionPanel */
    public NewEnvironmentVariableTypeSelectionPanel() {
        initComponents();
    }
    
    public String getTypeChoice() {
        return buttonGroup1.getSelection().getActionCommand();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        stringRB = new javax.swing.JRadioButton();
        numberRB = new javax.swing.JRadioButton();
        booleanRB = new javax.swing.JRadioButton();
        passwordRB = new javax.swing.JRadioButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(NewEnvironmentVariableTypeSelectionPanel.class, "jLabel1.text_1")); // NOI18N

        buttonGroup1.add(stringRB);
        stringRB.setSelected(true);
        stringRB.setText(org.openide.util.NbBundle.getMessage(NewEnvironmentVariableTypeSelectionPanel.class, "jRadioButton1.text_1")); // NOI18N
        stringRB.setActionCommand("String"); // NOI18N
        stringRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stringRB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(numberRB);
        numberRB.setText(org.openide.util.NbBundle.getMessage(NewEnvironmentVariableTypeSelectionPanel.class, "jRadioButton2.text_1")); // NOI18N
        numberRB.setActionCommand("Number"); // NOI18N
        numberRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        numberRB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(booleanRB);
        booleanRB.setText(org.openide.util.NbBundle.getMessage(NewEnvironmentVariableTypeSelectionPanel.class, "jRadioButton3.text_1")); // NOI18N
        booleanRB.setActionCommand("Boolean"); // NOI18N
        booleanRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        booleanRB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(passwordRB);
        passwordRB.setText(org.openide.util.NbBundle.getMessage(NewEnvironmentVariableTypeSelectionPanel.class, "jRadioButton4.text_1")); // NOI18N
        passwordRB.setActionCommand("Password"); // NOI18N
        passwordRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passwordRB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(numberRB)
                            .add(stringRB)
                            .add(booleanRB)
                            .add(passwordRB)))
                    .add(jLabel1))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(stringRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(numberRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(booleanRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordRB)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton booleanRB;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton numberRB;
    private javax.swing.JRadioButton passwordRB;
    private javax.swing.JRadioButton stringRB;
    // End of variables declaration//GEN-END:variables
    
}
