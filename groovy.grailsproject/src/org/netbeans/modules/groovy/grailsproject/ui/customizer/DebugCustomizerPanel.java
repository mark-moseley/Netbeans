/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * DebugCustomizerPanel.java
 *
 * Created on Jul 12, 2008, 11:15:15 AM
 */

package org.netbeans.modules.groovy.grailsproject.ui.customizer;

import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;

/**
 *
 * @author sc32560
 */
public class DebugCustomizerPanel extends javax.swing.JPanel {
    
    private GrailsProjectProperties uiProperties;

    private final boolean ieBrowserSupported;

    private final boolean ffBrowserSupported;

    /** Creates new form DebugCustomizerPanel */
    public DebugCustomizerPanel(GrailsProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        
        initComponents();
        
        ieBrowserSupported = WebClientToolsProjectUtils.isInternetExplorerSupported();
        ffBrowserSupported = WebClientToolsProjectUtils.isFirefoxSupported();
        
        String browserString = uiProperties.getDebugBrowser();
        WebClientToolsProjectUtils.Browser selectedBrowser = null;
        if (browserString == null) {
            browserString = (ffBrowserSupported || !ieBrowserSupported) ? WebClientToolsProjectUtils.Browser.FIREFOX.name() :
                WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER.name();
        }
        selectedBrowser = WebClientToolsProjectUtils.Browser.valueOf(browserString);
        
        firefoxRadioButton.setSelected(selectedBrowser == WebClientToolsProjectUtils.Browser.FIREFOX);
        internetExplorerRadioButton.setSelected(selectedBrowser == WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER);
        
        firefoxRadioButton.setEnabled(ffBrowserSupported);
        internetExplorerRadioButton.setEnabled(ieBrowserSupported);
        
        noSupportedBrowserLabel.setVisible(!ieBrowserSupported && !ffBrowserSupported);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserButtonGroup = new javax.swing.ButtonGroup();
        debugClientLabel = new javax.swing.JLabel();
        firefoxRadioButton = new javax.swing.JRadioButton();
        internetExplorerRadioButton = new javax.swing.JRadioButton();
        noSupportedBrowserLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(debugClientLabel, org.openide.util.NbBundle.getMessage(DebugCustomizerPanel.class, "DebugCustomizerPanel.debugClientLabel.text")); // NOI18N

        browserButtonGroup.add(firefoxRadioButton);
        firefoxRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(firefoxRadioButton, org.openide.util.NbBundle.getMessage(DebugCustomizerPanel.class, "DebugCustomizerPanel.firefoxRadioButton.text")); // NOI18N
        firefoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firefoxRadioButtonActionPerformed(evt);
            }
        });

        browserButtonGroup.add(internetExplorerRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(internetExplorerRadioButton, org.openide.util.NbBundle.getMessage(DebugCustomizerPanel.class, "DebugCustomizerPanel.internetExplorerRadioButton.text")); // NOI18N
        internetExplorerRadioButton.setToolTipText(org.openide.util.NbBundle.getMessage(DebugCustomizerPanel.class, "DebugCustomizerPanel.internetExplorerRadioButton.tooltip")); // NOI18N
        internetExplorerRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                internetExplorerRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(noSupportedBrowserLabel, org.openide.util.NbBundle.getMessage(DebugCustomizerPanel.class, "DebugCustomizerPanel.noSupportedBrowserLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(debugClientLabel)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(internetExplorerRadioButton)
                            .add(firefoxRadioButton)))
                    .add(noSupportedBrowserLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(debugClientLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(firefoxRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(internetExplorerRadioButton)
                .add(18, 18, 18)
                .add(noSupportedBrowserLabel)
                .addContainerGap(181, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void firefoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firefoxRadioButtonActionPerformed
        setBrowser();
    }//GEN-LAST:event_firefoxRadioButtonActionPerformed

    private void internetExplorerRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_internetExplorerRadioButtonActionPerformed
        setBrowser();
    }//GEN-LAST:event_internetExplorerRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup browserButtonGroup;
    private javax.swing.JLabel debugClientLabel;
    private javax.swing.JRadioButton firefoxRadioButton;
    private javax.swing.JRadioButton internetExplorerRadioButton;
    private javax.swing.JLabel noSupportedBrowserLabel;
    // End of variables declaration//GEN-END:variables

    private void setBrowser() {
        uiProperties.setDebugBrowser(internetExplorerRadioButton.isSelected() ?
                WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER.name() :
                WebClientToolsProjectUtils.Browser.FIREFOX.name());
    }

}
