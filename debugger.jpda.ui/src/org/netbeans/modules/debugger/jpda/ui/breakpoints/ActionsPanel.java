/*
 * LineBreakpointPanel.java
 *
 * Created on 24. b�ezen 2004, 16:18
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;

/**
 *
 * @author  jj97931
 */
public class ActionsPanel extends javax.swing.JPanel {
    
    private JPDABreakpoint  breakpoint;
    
    /** Creates new form LineBreakpointPanel */
    public ActionsPanel (JPDABreakpoint b) {
        breakpoint = b;
        initComponents ();
        
        cbSuspend.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_CB_Actions_Panel_Suspend_None"));
        cbSuspend.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_CB_Actions_Panel_Suspend_Current"));
        cbSuspend.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_CB_Actions_Panel_Suspend_All"));
        switch (b.getSuspend ()) {
            case JPDABreakpoint.SUSPEND_NONE:
                cbSuspend.setSelectedIndex (0);
                break;
            case JPDABreakpoint.SUSPEND_EVENT_THREAD:
                cbSuspend.setSelectedIndex (1);
                break;
            case JPDABreakpoint.SUSPEND_ALL:
                cbSuspend.setSelectedIndex (2);
                break;
        }
        if (b.getPrintText () != null)
            tfPrintText.setText (b.getPrintText ());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        tfPrintText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbSuspend = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Actions_Panel_BorderTitle")));
        tfPrintText.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_TF_Actions_Panel_Print_Text"));
        tfPrintText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfPrintTextActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfPrintText, gridBagConstraints);
        tfPrintText.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ACSD_TF_Actions_Panel_Print_Text"));

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_L_Actions_Panel_Suspend").charAt(0));
        jLabel1.setLabelFor(cbSuspend);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Actions_Panel_Suspend"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ASCD_L_Actions_Panel_Suspend"));

        cbSuspend.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("TTT_CB_Actions_Panel_Suspend"));
        cbSuspend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSuspendActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(cbSuspend, gridBagConstraints);
        cbSuspend.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("ASCD_CB_Actions_Panel_Suspend"));

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("MN_L_Actions_Panel_Print_Text").charAt(0));
        jLabel2.setLabelFor(tfPrintText);
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("L_Actions_Panel_Print_Text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel2, gridBagConstraints);

    }//GEN-END:initComponents

    private void tfPrintTextActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_tfPrintTextActionPerformed
    {//GEN-HEADEREND:event_tfPrintTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tfPrintTextActionPerformed

    private void cbSuspendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbSuspendActionPerformed
    {//GEN-HEADEREND:event_cbSuspendActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbSuspendActionPerformed
    
    /**
     * Called when "Ok" button is pressed.
     */
    public void ok () {
        String printText = tfPrintText.getText ();
        if (printText.trim ().length () > 0)
            breakpoint.setPrintText (printText.trim ());
        else
            breakpoint.setPrintText (null);
        
        switch (cbSuspend.getSelectedIndex ()) {
            case 0:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_NONE);
                break;
            case 1:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_EVENT_THREAD);
                break;
            case 2:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_ALL);
                break;
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSuspend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tfPrintText;
    // End of variables declaration//GEN-END:variables
    
}
