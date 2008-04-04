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

package org.netbeans.modules.php.project.ui.options;

import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.classpath.GlobalIncludePathSupport;
import org.netbeans.modules.php.project.ui.IncludePathUiSupport;
import org.openide.util.ChangeSupport;

/**
 * @author  Tomas Mysik
 */
public class PhpOptionsPanel extends JPanel {
    private static final long serialVersionUID = 1092136352492432078L;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpOptionsPanel() {
        initComponents();
        errorLabel.setForeground(Color.RED);

        initPhpGlobalIncludePath();

        // listeners
        debuggerPortTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                fireChange();
            }
        });
    }

    private void initPhpGlobalIncludePath() {
        DefaultListModel listModel = IncludePathUiSupport.createListModel(
                GlobalIncludePathSupport.getInstance().itemsIterator());
        includePathList.setModel(listModel);
        includePathList.setCellRenderer(new IncludePathUiSupport.ClassPathListCellRenderer());
        IncludePathUiSupport.EditMediator.register(includePathList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel());
    }

    public String getPhpInterpreter() {
        return phpInterpreterTextField.getText();
    }

    public void setPhpInterpreter(String phpInterpreter) {
        phpInterpreterTextField.setText(phpInterpreter);
    }

    public boolean isOpenResultInOutputWindow() {
        return outputWindowCheckBox.isSelected();
    }

    public void setOpenResultInOutputWindow(boolean openResultInOutputWindow) {
        outputWindowCheckBox.setSelected(openResultInOutputWindow);
    }

    public boolean isOpenResultInBrowser() {
        return webBrowserCheckBox.isSelected();
    }

    public void setOpenResultInBrowser(boolean openResultInBrowser) {
        webBrowserCheckBox.setSelected(openResultInBrowser);
    }

    public boolean isOpenResultInEditor() {
        return editorCheckBox.isSelected();
    }

    public void setOpenResultInEditor(boolean openResultInEditor) {
        editorCheckBox.setSelected(openResultInEditor);
    }

    public Integer getDebuggerPort() {
        Integer port = null;
        try {
            port = Integer.parseInt(debuggerPortTextField.getText());
        } catch (NumberFormatException exc) {
            // ignored
        }
        return port;
    }

    public void setDebuggerPort(int debuggerPort) {
        debuggerPortTextField.setText(String.valueOf(debuggerPort));
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return stopAtTheFirstLineCheckBox.isSelected();
    }

    public void setDebuggerStoppedAtTheFirstLine(boolean debuggerStoppedAtTheFirstLine) {
        stopAtTheFirstLineCheckBox.setSelected(debuggerStoppedAtTheFirstLine);
    }

    public String getPhpGlobalIncludePath() {
        String[] paths = GlobalIncludePathSupport.getInstance().encodeToStrings(
                IncludePathUiSupport.getIterator((DefaultListModel) includePathList.getModel()));
        StringBuilder path = new StringBuilder();
        for (String s : paths) {
            path.append(s);
        }
        return path.toString();
    }

    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commandLineLabel = new javax.swing.JLabel();
        phpInterpreterLabel = new javax.swing.JLabel();
        phpInterpreterTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        openResultInLabel = new javax.swing.JLabel();
        outputWindowCheckBox = new javax.swing.JCheckBox();
        webBrowserCheckBox = new javax.swing.JCheckBox();
        editorCheckBox = new javax.swing.JCheckBox();
        debuggingLabel = new javax.swing.JLabel();
        debuggerPortLabel = new javax.swing.JLabel();
        debuggerPortTextField = new javax.swing.JTextField();
        stopAtTheFirstLineCheckBox = new javax.swing.JCheckBox();
        globalIncludePathLabel = new javax.swing.JLabel();
        includePathScrollPane = new javax.swing.JScrollPane();
        includePathList = new javax.swing.JList();
        addFolderButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(commandLineLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_CommandLine")); // NOI18N

        phpInterpreterLabel.setLabelFor(phpInterpreterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(phpInterpreterLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreter")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Browse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Search")); // NOI18N

        openResultInLabel.setLabelFor(outputWindowCheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(openResultInLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OpenResultIn")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(outputWindowCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OutputWindow")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(webBrowserCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_WebBrowser")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(editorCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Editor")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debuggingLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Debugging")); // NOI18N

        debuggerPortLabel.setLabelFor(debuggerPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(debuggerPortLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_DebuggerPort")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stopAtTheFirstLineCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_StopAtTheFirstLine")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalIncludePathLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_GlobalIncludePath")); // NOI18N

        includePathScrollPane.setViewportView(includePathList);

        org.openide.awt.Mnemonics.setLocalizedText(addFolderButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_AddFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveDown")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " "); // NOI18N

        jLabel1.setLabelFor(includePathList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_UseTheFollowingPathByDefault")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, debuggingLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(stopAtTheFirstLineCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(debuggerPortLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(debuggerPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(globalIncludePathLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(365, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(includePathScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                            .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(moveDownButton)
                            .add(moveUpButton)
                            .add(removeButton)
                            .add(addFolderButton)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, commandLineLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(phpInterpreterLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(phpInterpreterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(openResultInLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(webBrowserCheckBox)
                                    .add(outputWindowCheckBox)
                                    .add(editorCheckBox))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchButton)))
                .add(0, 0, 0))
        );

        layout.linkSize(new java.awt.Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {browseButton, searchButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(commandLineLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phpInterpreterLabel)
                    .add(searchButton)
                    .add(browseButton)
                    .add(phpInterpreterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(openResultInLabel)
                    .add(outputWindowCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webBrowserCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(editorCheckBox)
                .add(18, 18, 18)
                .add(debuggingLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debuggerPortLabel)
                    .add(debuggerPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stopAtTheFirstLineCheckBox)
                .add(18, 18, 18)
                .add(globalIncludePathLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addFolderButton)
                        .add(18, 18, 18)
                        .add(removeButton)
                        .add(18, 18, 18)
                        .add(moveUpButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveDownButton))
                    .add(includePathScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel commandLineLabel;
    private javax.swing.JLabel debuggerPortLabel;
    private javax.swing.JTextField debuggerPortTextField;
    private javax.swing.JLabel debuggingLabel;
    private javax.swing.JCheckBox editorCheckBox;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel globalIncludePathLabel;
    private javax.swing.JList includePathList;
    private javax.swing.JScrollPane includePathScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JLabel openResultInLabel;
    private javax.swing.JCheckBox outputWindowCheckBox;
    private javax.swing.JLabel phpInterpreterLabel;
    private javax.swing.JTextField phpInterpreterTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JCheckBox stopAtTheFirstLineCheckBox;
    private javax.swing.JCheckBox webBrowserCheckBox;
    // End of variables declaration//GEN-END:variables

}
