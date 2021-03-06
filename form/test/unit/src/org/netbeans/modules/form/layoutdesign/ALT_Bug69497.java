package org.netbeans.modules.form.layoutdesign;

import java.util.Calendar;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

public class ALT_Bug69497 extends javax.swing.JFrame {

    /** Creates new form BondCalculator */
    public ALT_Bug69497() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        myJPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        purchaseDateField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        purchasePriceField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        maturityDateField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        couponField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        callDateField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        callPriceField = new javax.swing.JTextField();

        setTitle("Bond Calculator (alpha)");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jLabel1.setText("Purchase Date");
        jLabel1.setToolTipText("Purchase date of bond (MM/DD/YY)");

        purchaseDateField.setColumns(10);

        jLabel3.setText("Purchase Price");
        jLabel3.setToolTipText("Purchase price of bond (e.g., 100)");

        purchasePriceField.setColumns(10);

        jLabel5.setText("Maturity Date");
        jLabel5.setToolTipText("Maturity date of bond (MM/DD/YY)");

        maturityDateField.setColumns(10);

        jLabel2.setText("Coupon");
        jLabel2.setToolTipText("Annual interest (e.g., 6.5)");

        couponField.setColumns(10);

        jLabel7.setText("Call Date");
        jLabel7.setToolTipText("Call date of bond (MM/DD/YY)");

        callDateField.setColumns(10);

        jLabel6.setText("Call Price");
        jLabel6.setToolTipText("Call price (e.g., 100)");

        callPriceField.setColumns(10);

        org.jdesktop.layout.GroupLayout myJPanelLayout = new org.jdesktop.layout.GroupLayout(myJPanel);
        myJPanel.setLayout(myJPanelLayout);
        myJPanelLayout.setHorizontalGroup(
            myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(callPriceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(callDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maturityDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(couponField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(purchasePriceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(purchaseDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        myJPanelLayout.setVerticalGroup(
            myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(purchaseDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(purchasePriceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(36, 36, 36))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myJPanelLayout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(maturityDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel2))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1)))
                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myJPanelLayout.createSequentialGroup()
                        .add(couponField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(9, 9, 9)
                        .add(callDateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(2, 2, 2)
                .add(myJPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(callPriceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        getContentPane().add(myJPanel, java.awt.BorderLayout.NORTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                appFrame = new ALT_Bug69497();
                appFrame.setVisible(true);
            }
        });
    }

    private static javax.swing.JFrame appFrame;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField callDateField;
    private javax.swing.JTextField callPriceField;
    private javax.swing.JTextField couponField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField maturityDateField;
    private javax.swing.JPanel myJPanel;
    private javax.swing.JTextField purchaseDateField;
    private javax.swing.JTextField purchasePriceField;
    // End of variables declaration//GEN-END:variables

}
