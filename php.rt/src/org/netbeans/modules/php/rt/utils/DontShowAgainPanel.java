/*
 * DontShowAgainPanel.java
 *
 * Created on 15 Август 2007 г., 14:54
 */

package org.netbeans.modules.php.rt.utils;

import java.awt.Color;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  avk
 */
public class DontShowAgainPanel extends javax.swing.JPanel {

    /** Creates new form DontShowAgainPanel with specified message and deselected checkbox */
    public DontShowAgainPanel(String message) {
        this(message, false);
    }

    /** Creates new form DontShowAgainPanel with specified message and specified checkbox selection */
    public DontShowAgainPanel(String message, boolean dontAskAgain) {
        initComponents();
        initValues(message, dontAskAgain);
    }

    private void initValues(String message, boolean dontAskAgain) {
        setMessage(message);
        dontShowCheckbox.setSelected(dontAskAgain);
    }

    public String getMessage() {
        return myMessagePanel.getText();
    }
    
    public boolean getDontShowAgain(){
        return dontShowCheckbox.isSelected();
    }

    public void setMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.BLACK);
    }

    public void setErrorMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.RED);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myMessageContainer = new javax.swing.JPanel();
        myMessagePanel = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        dontShowCheckbox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        myMessageContainer.setLayout(new java.awt.BorderLayout());

        myMessagePanel.setEditable(false);
        myMessagePanel.setText(null);
        myMessagePanel.setFocusable(false);
        myMessagePanel.setMargin(new java.awt.Insets(10, 10, 10, 10));
        myMessagePanel.setOpaque(false);
        myMessagePanel.setPreferredSize(new java.awt.Dimension(120, 120));
        myMessageContainer.add(myMessagePanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(myMessageContainer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(dontShowCheckbox, org.openide.util.NbBundle.getMessage(DontShowAgainPanel.class, "LBL_Do_Not_Ask_Me_Again")); // NOI18N
        dontShowCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dontShowCheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dontShowCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontShowCheckboxActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DontShowAgainPanel.class, "DontShowAgainPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(dontShowCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(321, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                        .addContainerGap(116, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(dontShowCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void dontShowCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontShowCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dontShowCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dontShowCheckbox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel myMessageContainer;
    private javax.swing.JTextPane myMessagePanel;
    // End of variables declaration//GEN-END:variables


    /**
     * Creates and shows dialod with DontShowAgainPanel(String) JPanel.
     * Dialog will have specified title, display specified message, 
     * will suggest to remember selection by checking "Don't show again" checkbox,
     * And will suggest DialogDescriptor.YES_NO_OPTION options.
     * 
     * @param title String title for dialog
     * @param msg String message to show in dialog
     * @param dontShowAgain Array of boolean value. 
     *      If it is not null and not empty, selection state of 'Don't show again'
     *      checkbox will be stored in dontShowAgain[0]
     * @returns If user has confirmed. true if 'YES' button was selected. false otherwise.
     */
    public static boolean showDialog(String title, String msg, boolean[] dontShowAgain) {
        boolean confirm = false;

        DontShowAgainPanel panel = new DontShowAgainPanel(msg);
        DialogDescriptor dialog = new DialogDescriptor(
                panel, 
                title, 
                true, 
                DialogDescriptor.YES_NO_OPTION, 
                NotifyDescriptor.YES_OPTION, 
                null);

        confirm = (DialogDisplayer.getDefault().notify(dialog) == NotifyDescriptor.YES_OPTION);
        // just check that array has cell to store result
        if (dontShowAgain.length > 0) {
            dontShowAgain[0] = panel.getDontShowAgain();
        }
        return confirm;
    }

}
