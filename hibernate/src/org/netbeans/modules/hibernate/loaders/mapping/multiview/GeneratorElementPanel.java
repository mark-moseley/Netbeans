/*
 * GeneratorElementPanel.java
 *
 * Created on February 6, 2008, 11:58 AM
 */

package org.netbeans.modules.hibernate.loaders.mapping.multiview;

/**
 *
 * @author  dc151887
 */
public class GeneratorElementPanel extends javax.swing.JPanel {
    
    /** Creates new form GeneratorElementPanel */
    public GeneratorElementPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        paramLabel = new javax.swing.JLabel();
        paramPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        classLabel.setText(org.openide.util.NbBundle.getMessage(GeneratorElementPanel.class, "GeneratorElementPanel.classLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(classLabel, gridBagConstraints);

        classTextField.setText(org.openide.util.NbBundle.getMessage(GeneratorElementPanel.class, "GeneratorElementPanel.classTextField.text")); // NOI18N
        classTextField.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(classTextField, gridBagConstraints);

        paramLabel.setText(org.openide.util.NbBundle.getMessage(GeneratorElementPanel.class, "GeneratorElementPanel.paramLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(paramLabel, gridBagConstraints);

        paramPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 0, 0);
        add(paramPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JPanel paramPanel;
    // End of variables declaration//GEN-END:variables
    
}
