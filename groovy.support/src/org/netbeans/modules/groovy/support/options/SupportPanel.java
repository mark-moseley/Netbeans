/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.support.options;

import java.awt.Cursor;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Groovy settings
 *
 * @author Martin Adamek
 */
final class SupportPanel extends javax.swing.JPanel {

    private final SupportOptionsPanelController controller;

    SupportPanel(SupportOptionsPanelController controller) {
        this.controller = controller;
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

        homeLabel = new javax.swing.JLabel();
        groovyHomeTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        linkLabel = new javax.swing.JLabel();
        chooseHomeButton = new javax.swing.JButton();
        docLabel = new javax.swing.JLabel();
        groovyDocTextField = new javax.swing.JTextField();
        chooseDocButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(homeLabel, org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.homeLabel.text")); // NOI18N

        groovyHomeTextField.setText(org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.groovyHomeTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(linkLabel, "<html><a href=\"http://groovy.codehaus.org\">http://groovy.codehaus.org</a></html>"); // NOI18N
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                linkLabelMousePressed(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                linkLabelMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                linkLabelMouseEntered(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chooseHomeButton, org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.chooseHomeButton.text")); // NOI18N
        chooseHomeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseHomeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(docLabel, org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.docLabel.text")); // NOI18N

        groovyDocTextField.setText(org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.groovyDocTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chooseDocButton, org.openide.util.NbBundle.getMessage(SupportPanel.class, "SupportPanel.chooseDocButton.text")); // NOI18N
        chooseDocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseDocButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(homeLabel)
                    .add(docLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(linkLabel)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, groovyDocTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                            .add(groovyHomeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chooseDocButton)
                            .add(chooseHomeButton))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(homeLabel)
                    .add(chooseHomeButton)
                    .add(groovyHomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(docLabel)
                    .add(groovyDocTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chooseDocButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(linkLabel)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void linkLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMousePressed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://groovy.codehaus.org")); // NOI18N
        } catch (MalformedURLException murle) {
            Exceptions.printStackTrace(murle);
        }
    }//GEN-LAST:event_linkLabelMousePressed

    private void linkLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_linkLabelMouseEntered

    private void linkLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_linkLabelMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_linkLabelMouseExited

    private void chooseHomeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseHomeButtonActionPerformed
        JFileChooser chooser = new JFileChooser(groovyHomeTextField.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = chooser.showDialog(
            SwingUtilities.getWindowAncestor (this), NbBundle.getMessage(SupportPanel.class, "LBL_Select_Directory"));
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile ();
            if (!new File (new File (file, "bin"), "groovy").isFile ()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(SupportPanel.class, "LBL_Not_groovy_home"),
                    NotifyDescriptor.Message.WARNING_MESSAGE
                ));
                return;
            }
            groovyHomeTextField.setText(file.getAbsolutePath());

        }
}//GEN-LAST:event_chooseHomeButtonActionPerformed

private void chooseDocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseDocButtonActionPerformed
        JFileChooser chooser = new JFileChooser(groovyDocTextField.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int r = chooser.showDialog(
            SwingUtilities.getWindowAncestor (this), NbBundle.getMessage(SupportPanel.class, "LBL_Select_Directory"));
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile ();
            if (!new File (new File (file, "groovy-jdk"), "index.html").isFile ()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(SupportPanel.class, "LBL_Not_groovy_doc"),
                    NotifyDescriptor.Message.WARNING_MESSAGE
                ));
                return;
            }
            groovyDocTextField.setText(file.getAbsolutePath());

        }
}//GEN-LAST:event_chooseDocButtonActionPerformed

    void load() {
        GroovySettings groovyOption = new GroovySettings();
        groovyHomeTextField.setText(groovyOption.getGroovyHome());
        groovyDocTextField.setText(groovyOption.getGroovyDoc());
    }

    void store() {
        GroovySettings groovyOption = new GroovySettings();
        groovyOption.setGroovyHome(groovyHomeTextField.getText().trim());
        groovyOption.setGroovyDoc(groovyDocTextField.getText().trim());
    }

    boolean valid() {
        String groovyHome = groovyHomeTextField.getText().trim();
        String groovyDoc = groovyDocTextField.getText().trim();
        return (!"".equals(groovyHome) && !"".equals(groovyDoc)); // NOI18N
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseDocButton;
    private javax.swing.JButton chooseHomeButton;
    private javax.swing.JLabel docLabel;
    private javax.swing.JTextField groovyDocTextField;
    private javax.swing.JTextField groovyHomeTextField;
    private javax.swing.JLabel homeLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel linkLabel;
    // End of variables declaration//GEN-END:variables

}
