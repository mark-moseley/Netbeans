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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui.options;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.compilers.CCCCompiler;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Panel used to manage predefined Include Paths and Macro Definitions of the compiler
 */
public class PredefinedPanel extends javax.swing.JPanel {

    private IncludesPanel includesPanel;
    private DefinitionsPanel definitionsPanel;
    private CCCCompiler compiler;
    private ParserSettingsPanel parserSettingsPanel;

    private boolean settingsReseted = false;

    /** Creates new form PredefinedPanel */
    public PredefinedPanel(CCCCompiler compiler, ParserSettingsPanel parserSettingsPanel) {
        initComponents();
        this.compiler = compiler;
        this.parserSettingsPanel = parserSettingsPanel;
        updatePanels();

        resetButton.getAccessibleContext().setAccessibleDescription(getString("RESET_BUTTON_AD"));

        setOpaque(false);
    }
    private static final int INSETS = 0;
    private static final double WEIGTH = 0.1;

    private void updatePanels() {
        List<String> includesList = compiler.getSystemIncludeDirectories();
//        String[] includesAr = (String[])includesList.toArray(new String[includesList.size()]);

        if (includesPanel != null) {
            includes.remove(includesPanel);
        }
        includes.add(includesPanel = new IncludesPanel(includesList));
        List<String> definesList = compiler.getSystemPreprocessorSymbols();
        Collections.sort(definesList, new Comparator<String>() {
            public int compare(String s1, String s2) {
                //return trim(s1).compareToIgnoreCase(trim(s2));
                return s1.compareToIgnoreCase(s2);
            }
// though useful in some situations (due to the absence of search),
// such sort order is counter-intuitive 
//            private String trim(String s) {
//                if (s == null) {
//                    return s;
//                } else {
//                    int start = 0;
//                    for (int i = 0; i < s.length(); i++) {
//                        if (s.charAt(i) == '_') {
//                            start++;
//                        }
//                    }
//                    return (start == 0) ? s : s.substring(start);
//                }
//            }
        });
//        String[] definesAr = (String[])definesList.toArray(new String[definesList.size()]);

        if (definitionsPanel != null) {
            macros.remove(definitionsPanel);
        }
        macros.add(definitionsPanel = new DefinitionsPanel(definesList));
    }

    public boolean save() {
        boolean wasChanges = settingsReseted;
        settingsReseted = false;
        Vector<String> includes = includesPanel.getListData();
        wasChanges |= compiler.setSystemIncludeDirectories(includes);
        Vector<String> definitions = definitionsPanel.getListData();
        wasChanges |= compiler.setSystemPreprocessorSymbols(definitions);
        return wasChanges;
    }

    public void update() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("update for PredefinedPanel " + compiler.getName());
        }
        updatePanels();
    }

    public void updateCompiler(CCCCompiler compiler) {
        this.compiler = compiler;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        includes = new javax.swing.JPanel();
        macros = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setOpaque(false);

        includes.setBackground(new java.awt.Color(255, 51, 51));
        includes.setOpaque(false);
        includes.setLayout(new java.awt.BorderLayout());

        macros.setBackground(new java.awt.Color(204, 204, 0));
        macros.setOpaque(false);
        macros.setLayout(new java.awt.BorderLayout());

        resetButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle").getString("RESET_BUTTON_MN").charAt(0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/options/Bundle"); // NOI18N
        resetButton.setText(bundle.getString("RESET_BUTTON_TXT")); // NOI18N
        resetButton.setOpaque(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("CODE_ASSISTANCE_COMMENT")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(includes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, macros, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(resetButton)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(includes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(macros, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resetButton)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        String txt = getString("RESET_QUESTION"); // NOI18N
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, getString("RESET_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION); // NOI18N
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            compiler.resetSystemIncludesAndDefines();
            updatePanels();
            validate();
            repaint();
            //parserSettingsPanel.fireFilesPropertiesChanged();
            parserSettingsPanel.setModified(true);
            settingsReseted = true;
        }
    }//GEN-LAST:event_resetButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel includes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel macros;
    private javax.swing.JButton resetButton;
    // End of variables declaration//GEN-END:variables

    private static class IncludesPanel extends ListEditorPanel<String> {

        public IncludesPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);

            if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                getDataPanel().setOpaque(false);
            }
        }

        @Override
        public String addAction() {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = System.getProperty("user.home"); // NOI18N
            }
            FileChooser fileChooser = new FileChooser(getString("SelectDirectoryTxt"), getString("SelectTxt"), JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("IncludeDirectoriesTxt");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("IncludeDirectoriesMn").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("AddButtonTxt");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("IAddButtonMn").charAt(0);
        }

        @Override
        public char getCopyButtonMnemonics() {
            return getString("ICopyButtonMn").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public String getRenameButtonText() {
            return getString("EditButtonTxt");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EditButtonMn").charAt(0);
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDir"), getString("EditDialogTitle")); // NOI18N
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            Vector<String> vector = getListData();
            Object[] arr = getListData().toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }

        public char getRemoveButtonMnemonics() {
            return getString("IRemoveButtonMn").charAt(0);
        }

        public char getUpButtonMnemonics() {
            return getString("IUpButtonMn").charAt(0);
        }

        public char getDownButtonMnemonics() {
            return getString("IDownButtonMn").charAt(0);
        }
    }

    private static class DefinitionsPanel extends ListEditorPanel<String> {

        public DefinitionsPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
            if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                getDataPanel().setOpaque(false);
            }
        }

        public String addAction() {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDef"), getString("AddDialogTitle"));
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            String def = notifyDescriptor.getInputText();
            if (def.length() != 0) {
                return def;
            } else {
                return null;
            }
        }

        @Override
        public String getListLabelText() {
            return getString("MacroDefinitionsTxt");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("MacroDefinitionsMn").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("AddButtonTxt");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("MAddButtonMn").charAt(0);
        }

        @Override
        public char getCopyButtonMnemonics() {
            return getString("MCopyButtonMn").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("MditButtonMn").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EditButtonTxt");
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDef"), getString("EditDialogTitle")); // NOI18N
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            Vector<String> vector = getListData();
            Object[] arr = getListData().toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }

        @Override
        public char getRemoveButtonMnemonics() {
            return getString("MRemoveButtonMn").charAt(0);
        }

        @Override
        public char getUpButtonMnemonics() {
            return getString("MUpButtonMn").charAt(0);
        }

        @Override
        public char getDownButtonMnemonics() {
            return getString("MDownButtonMn").charAt(0);
        }
    }

    private static String getString(String s) {
        return NbBundle.getMessage(PredefinedPanel.class, s);
    }

    boolean isChanged() {
        boolean isChanged = settingsReseted;
        if (this.includesPanel != null) {
            isChanged |= this.includesPanel.isChanged();
        }
        if (this.definitionsPanel != null) {
            isChanged |= this.definitionsPanel.isChanged();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isChanged for PredefinedPanel " + compiler.getName() + " is " + isChanged);
        }
        return isChanged;
    }

    boolean isDataValid() {
        boolean isDataValid = true;
        if (this.includesPanel != null) {
            isDataValid &= this.includesPanel.isDataValid();
        }
        if (this.definitionsPanel != null) {
            isDataValid &= this.definitionsPanel.isDataValid();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isDataValid for PredefinedPanel " + compiler.getName() + " is " + isDataValid);
        }
        return isDataValid;
    }

    void cancel() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("cancel for PredefinedPanel " + compiler.getName());
        }
    }
}
