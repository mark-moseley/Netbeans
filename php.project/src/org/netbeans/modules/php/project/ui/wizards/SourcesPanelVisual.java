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

package org.netbeans.modules.php.project.ui.wizards;

import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.ChangeSupport;

/**
 * @author Tomas Mysik
 */
public class SourcesPanelVisual extends JPanel implements DocumentListener, ChangeListener {

    private static final long serialVersionUID = -358263102348820543L;

    public static final String URL_REGEXP = "^https?://[^/?# ]+(:\\d+)?/[^?#]*(\\?[^#]*)?(#\\w*)?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

    private final WebFolderNameProvider webFolderNameProvider;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private MutableComboBoxModel localServerComboBoxModel;
    private final LocalServer.ComboBoxEditor localServerComboBoxEditor = new LocalServer.ComboBoxEditor(this);


    /** Creates new form SourcesPanelVisual */
    public SourcesPanelVisual(WebFolderNameProvider webFolderNameProvider) {
        this.webFolderNameProvider = webFolderNameProvider;
        initComponents();
        init();
    }

    private void init() {
        localServerComboBoxModel = new LocalServer.ComboBoxModel(ConfigureProjectPanel.DEFAULT_LOCAL_SERVER);

        localServerComboBox.setModel(localServerComboBoxModel);
        localServerComboBox.setRenderer(new LocalServer.ComboBoxRenderer());
        localServerComboBox.setEditor(localServerComboBoxEditor);

        urlTextField.getDocument().addDocumentListener(this);
    }

    void addSourcesListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeSourcesListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourcesLabel = new javax.swing.JLabel();
        localServerComboBox = new javax.swing.JComboBox();
        locateButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        localServerLabel = new javax.swing.JLabel();
        urlInfoLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(sourcesLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_Sources")); // NOI18N

        localServerComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(locateButton, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_LocateLocalServer")); // NOI18N
        locateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_BrowseLocalServer")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(localServerLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "TXT_LocalServer")); // NOI18N
        localServerLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(urlInfoLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "TXT_Url")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(SourcesPanelVisual.class, "LBL_Url")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(sourcesLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(localServerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(localServerComboBox, 0, 319, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(locateButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(browseButton))))
                    .add(urlInfoLabel)
                    .add(layout.createSequentialGroup()
                        .add(urlLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sourcesLabel)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(locateButton)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localServerLabel)
                .add(18, 18, 18)
                .add(urlInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        Utils.browseLocalServerAction(this, localServerComboBox, localServerComboBoxModel,
                webFolderNameProvider.getWebFolderName());
    }//GEN-LAST:event_browseButtonActionPerformed

    private void locateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locateButtonActionPerformed
        Utils.locateLocalServerAction();
    }//GEN-LAST:event_locateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel localServerLabel;
    private javax.swing.JButton locateButton;
    private javax.swing.JLabel sourcesLabel;
    private javax.swing.JLabel urlInfoLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    LocalServer getSourcesLocation() {
        return (LocalServer) localServerComboBox.getSelectedItem();
    }

    MutableComboBoxModel getLocalServerModel() {
        return localServerComboBoxModel;
    }

    void setLocalServerModel(MutableComboBoxModel localServers) {
        localServerComboBoxModel = localServers;
        localServerComboBox.setModel(localServerComboBoxModel);
    }

    void selectSourcesLocation(LocalServer localServer) {
        localServerComboBox.setSelectedItem(localServer);
    }

    String getUrl() {
        return urlTextField.getText().trim();
    }

    void setUrl(String url) {
        urlTextField.setText(url);
    }

    // listeners
    public void insertUpdate(DocumentEvent e) {
        processUpdate();
    }

    public void removeUpdate(DocumentEvent e) {
        processUpdate();
    }

    public void changedUpdate(DocumentEvent e) {
        processUpdate();
    }

    private void processUpdate() {
        changeSupport.fireChange();
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
