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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.php.project.connections.ConfigManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Radek Matous, Tomas Mysik
 */
public class RunAsScript extends RunAsPanel.InsidePanel {
    private static final long serialVersionUID = -5593489817914071L;
    private final String displayName;
    final ChangeSupport changeSupport = new ChangeSupport(this);

    public RunAsScript(ConfigManager manager) {
        this(manager, NbBundle.getMessage(RunAsScript.class, "LBL_ConfigScript"));
    }

    private RunAsScript(ConfigManager manager, String displayName) {
        super(manager);
        this.displayName = displayName;

        initComponents();

        addListeners();
    }

    private void addListeners() {
        interpreterTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        });
        runAsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });
    }

    public void loadPhpInterpreter() {
        interpreterTextField.setText(PhpOptions.getInstance().getPhpInterpreter());
    }

    @Override
    protected RunAsType getRunAsType() {
        return PhpProjectProperties.RunAsType.SCRIPT;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    public JComboBox getRunAsCombo() {
        return runAsCombo;
    }

    protected void loadFields() {
        loadPhpInterpreter();
    }

    protected void validateFields() {
        // nothing needed in this panel
    }

    public void addRunAsScriptListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsScriptListener(ChangeListener listener) {
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

        interpreterLabel = new javax.swing.JLabel();
        interpreterTextField = new javax.swing.JTextField();
        runAsLabel = new javax.swing.JLabel();
        runAsCombo = new javax.swing.JComboBox();
        configureButton = new javax.swing.JButton();

        interpreterLabel.setLabelFor(interpreterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(interpreterLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_PhpInterpreter")); // NOI18N

        interpreterTextField.setEditable(false);

        runAsLabel.setLabelFor(runAsCombo);
        org.openide.awt.Mnemonics.setLocalizedText(runAsLabel, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_RunAs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(RunAsScript.class, "LBL_Configure")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(runAsLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(interpreterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, runAsCombo, 0, 222, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(interpreterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(configureButton)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(runAsLabel)
                    .add(runAsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(interpreterLabel)
                    .add(interpreterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(configureButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open("Advanced/PHP");
    }//GEN-LAST:event_configureButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton configureButton;
    private javax.swing.JLabel interpreterLabel;
    private javax.swing.JTextField interpreterTextField;
    private javax.swing.JComboBox runAsCombo;
    private javax.swing.JLabel runAsLabel;
    // End of variables declaration//GEN-END:variables

    public String getPhpInterpreter() {
        return interpreterTextField.getText().trim();
    }
}
