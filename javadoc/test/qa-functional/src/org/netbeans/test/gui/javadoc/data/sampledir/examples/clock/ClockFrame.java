/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package examples.clock;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

public class ClockFrame extends javax.swing.JFrame {

    /** Initializes the Form */
    public ClockFrame() {
        initComponents ();
        pack ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        tmrSeconds = new org.netbeans.examples.lib.timerbean.Timer();
        jlblCurrentTime = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jlblNewTime = new javax.swing.JLabel();
        jtfNewTime = new javax.swing.JTextField();
        jbtnNewTime = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jlblNewFormat = new javax.swing.JLabel();
        jtfNewTimeFormat = new javax.swing.JTextField();
        jbtnNewTimeFormat = new javax.swing.JButton();

        tmrSeconds.addTimerListener(new org.netbeans.examples.lib.timerbean.TimerListener() {
            public void onTime(java.awt.event.ActionEvent evt) {
                tmrSecondsOnTime(evt);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jlblCurrentTime.setText("00:00:00");
        jlblCurrentTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblCurrentTime.setFont(new java.awt.Font("Dialog", 1, 36));
        getContentPane().add(jlblCurrentTime, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridLayout(1, 0));

        jlblNewTime.setText("New Time");
        jlblNewTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel1.add(jlblNewTime);

        jtfNewTime.setText("00:00:00");
        jPanel1.add(jtfNewTime);

        jbtnNewTime.setText("Set New Time");
        jbtnNewTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnSetNewTimeClicked(evt);
            }
        });

        jPanel1.add(jbtnNewTime);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jlblNewFormat.setText("Time Format");
        jlblNewFormat.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jlblNewFormat);

        jtfNewTimeFormat.setText("hh:mm:ss");
        jPanel2.add(jtfNewTimeFormat);

        jbtnNewTimeFormat.setText("Set New Time Format");
        jbtnNewTimeFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnNewTimeFormatActionClicked(evt);
            }
        });

        jPanel2.add(jbtnNewTimeFormat);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void jbtnNewTimeFormatActionClicked (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnNewTimeFormatActionClicked
        String timeFormat = jtfNewTimeFormat.getText();
        formatter = new SimpleDateFormat(timeFormat);
    }//GEN-LAST:event_jbtnNewTimeFormatActionClicked

    private void jbtnSetNewTimeClicked (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnSetNewTimeClicked
        try {
            String timeStr = jtfNewTime.getText();
            gCal.setTime(formatter.parse(timeStr));
        } catch (java.text.ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format", "I don't understand your date format.", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jbtnSetNewTimeClicked

    private void tmrSecondsOnTime (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tmrSecondsOnTime
        gCal.add(Calendar.SECOND,1);
        String timeTxt = formatter.format(gCal.getTime());
        if (jlblCurrentTime != null)
            jlblCurrentTime.setText(timeTxt);
    }//GEN-LAST:event_tmrSecondsOnTime

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jbtnNewTime;
    private javax.swing.JButton jbtnNewTimeFormat;
    private javax.swing.JTextField jtfNewTime;
    private javax.swing.JLabel jlblNewTime;
    private javax.swing.JLabel jlblNewFormat;
    private javax.swing.JLabel jlblCurrentTime;
    private org.netbeans.examples.lib.timerbean.Timer tmrSeconds;
    private javax.swing.JTextField jtfNewTimeFormat;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    private GregorianCalendar gCal = new GregorianCalendar();
    private String timeFormat = "hh:mm:ss";
    private SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);


    public static void main(java.lang.String[] args) {
        new ClockFrame ().show ();
    }

}
