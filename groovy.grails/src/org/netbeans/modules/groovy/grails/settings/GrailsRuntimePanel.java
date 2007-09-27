package org.netbeans.modules.groovy.grails.settings;

import java.awt.Cursor;
import org.netbeans.modules.groovy.grails.settings.Settings;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;

final class GrailsRuntimePanel extends javax.swing.JPanel {

    private final GrailsRuntimeOptionsPanelController controller;
    private final Settings settings;

    GrailsRuntimePanel(GrailsRuntimeOptionsPanelController controller) {
        this.controller = controller;
        this.settings = Settings.getInstance();
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grailsHomeLocation = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        chooseDir = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        linkLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "GRAILS_HOME:");

        org.openide.awt.Mnemonics.setLocalizedText(chooseDir, "choose");
        chooseDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDirActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GrailsRuntimePanel.class, "SupportPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(linkLabel, "<html><a href=\"http://www.grails.org\">http://www.grails.org</a></html>"); // NOI18N
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                linkLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                linkLabelMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                linkLabelMousePressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Port");

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, "SSL");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Hostname");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(linkLabel))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, grailsHomeLocation))
                        .add(28, 28, 28)
                        .add(chooseDir))
                    .add(layout.createSequentialGroup()
                        .add(93, 93, 93)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jTextField2))
                            .add(layout.createSequentialGroup()
                                .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jCheckBox1)
                        .add(105, 105, 105)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(chooseDir)
                    .add(grailsHomeLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(linkLabel))
                .add(18, 18, 18)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(138, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chooseDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDirActionPerformed
        
            JFileChooser chooser = new JFileChooser (grailsHomeLocation.getText ());
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            int r = chooser.showDialog (
                SwingUtilities.getWindowAncestor (this),"Select_Directory");
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile ();
                if (!new File (new File (file, "bin"), "grails").isFile ()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        "Not_grails_home",
                        NotifyDescriptor.Message.WARNING_MESSAGE
                    ));
                    return;
                }
                grailsHomeLocation.setText (file.getAbsolutePath ());

            }
        
        
    }//GEN-LAST:event_chooseDirActionPerformed

    private void linkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_linkLabelMouseEntered

    private void linkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_linkLabelMouseExited

    private void linkLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.grails.org")); // NOI18N
        } catch (MalformedURLException murle) {
            Exceptions.printStackTrace(murle);
        }
    }//GEN-LAST:event_linkLabelMousePressed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    void load() {

        grailsHomeLocation.setText(settings.getGrailsBase());
    }

    void store() {
        
        settings.setGrailsBase(grailsHomeLocation.getText());
        
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseDir;
    private javax.swing.JTextField grailsHomeLocation;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JLabel linkLabel;
    // End of variables declaration//GEN-END:variables

}
