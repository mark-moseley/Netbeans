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
package org.netbeans.modules.editor.macros.storage.ui;

import java.awt.Component;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class MacrosPanel extends JPanel {

    private final MacrosModel model = MacrosModel.get();
    private final Lookup lookup;
    
    /** 
     * Creates new form MacrosPanel.
     */
    public MacrosPanel(Lookup lookup) {
        this.lookup = lookup;
        
        initComponents();

        setName(loc("Macro_Tab")); //NOI18N

        // 1) init components
        tMacros.getAccessibleContext().setAccessibleName(loc("AN_Macros_Table")); //NOI18N
        tMacros.getAccessibleContext().setAccessibleDescription(loc("AD_Macros_Table")); //NOI18N
        epMacroCode.getAccessibleContext().setAccessibleName(loc("AN_Macro")); //NOI18N
        epMacroCode.getAccessibleContext().setAccessibleDescription(loc("AD_Macro")); //NOI18N
        bRemove.setEnabled(false);
        bSetShortcut.setEnabled(false);
        loc(bNew, "New_Macro"); //NOI18N
        loc(bRemove, "Remove_Macro"); //NOI18N
        loc(bSetShortcut, "Shortcut"); //NOI18N
        tMacros.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tMacros.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                tMacrosValueChanged(evt);
            }
        });
        tMacros.getTableHeader().setReorderingAllowed(false);
        tMacros.setModel(model.getTableModel());
        tMacros.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                tMacrosTableChanged(evt);
            }
        });
        
        epMacroCode.setEnabled(false);
        epMacroCode.setEditorKit(JEditorPane.createEditorKitForContentType("text/plain")); //NOI18N
        epMacroCode.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                epMacroCodeDocumentChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                epMacroCodeDocumentChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                // ignore
            }
        });
        
        loc(lMacros, "Macro_List"); //NOI18N
        lMacros.setLabelFor(tMacros);
        loc(lMacroCode, "Macro_Code"); //NOI18N
        lMacroCode.setLabelFor(epMacroCode);
    }

    public MacrosModel getModel() {
        return model;
    }
    
    public void forceAddMacro(String code) {
        MacrosModel.Macro macro = addMacro();
        if (macro != null) {
            macro.setCode(code);
        }
    }

    // UI form .................................................................
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lMacros = new javax.swing.JLabel();
        spMacros = new javax.swing.JScrollPane();
        tMacros = new javax.swing.JTable();
        bNew = new javax.swing.JButton();
        bSetShortcut = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        lMacroCode = new javax.swing.JLabel();
        sMacroCode = new javax.swing.JScrollPane();
        epMacroCode = new javax.swing.JEditorPane();

        lMacros.setText("Macros:");

        tMacros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        spMacros.setViewportView(tMacros);

        bNew.setText("New");
        bNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bNewActionPerformed(evt);
            }
        });

        bSetShortcut.setText("Set Shortcut...");
        bSetShortcut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSetShortcutActionPerformed(evt);
            }
        });

        bRemove.setText("Remove");
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });

        lMacroCode.setText("Macro Code:");

        sMacroCode.setViewportView(epMacroCode);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lMacros)
                    .add(lMacroCode)
                    .add(spMacros, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                    .add(sMacroCode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(bNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .add(bSetShortcut, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, bRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lMacros)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(bNew)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bSetShortcut)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bRemove)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(spMacros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lMacroCode)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sMacroCode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MacrosPanel.class, "AN_MacrosPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MacrosPanel.class, "AD_MacrosPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void bNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNewActionPerformed
        // TODO add your handling code here:
        addMacro();
    }//GEN-LAST:event_bNewActionPerformed

    private void bSetShortcutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSetShortcutActionPerformed
        // TODO add your handling code here:
        ShortcutsFinder shortcutsFinder = lookup.lookup(ShortcutsFinder.class);
        assert shortcutsFinder != null : "Can't find ShortcutsFinder"; //NOI18N
        
        String shortcut = shortcutsFinder.showShortcutsDialog();
        if (shortcut != null) {
            MacrosModel.Macro macro = model.getMacroByIndex(tMacros.getSelectedRow());
            macro.setShortcut(shortcut);
            shortcutsFinder.setShortcuts(macro, Collections.singleton(shortcut));
//                StorageSupport.keyStrokesToString(Arrays.asList(StorageSupport.stringToKeyStrokes(shortcut, true)), false)));
        }
    }//GEN-LAST:event_bSetShortcutActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        // TODO add your handling code here:
        model.deleteMacro(tMacros.getSelectedRow());
    }//GEN-LAST:event_bRemoveActionPerformed

    private void tMacrosValueChanged(ListSelectionEvent evt) {
        int index = tMacros.getSelectedRow();
    
        if (index < 0 || index >= tMacros.getRowCount()) {
            epMacroCode.setText(""); //NOI18N
            epMacroCode.setEnabled(false);
            bRemove.setEnabled(false);
            bSetShortcut.setEnabled(false);
        } else {
            epMacroCode.setText(model.getMacroByIndex(index).getCode()); //NOI18N
            epMacroCode.getCaret().setDot(0);
            epMacroCode.setEnabled(true);
            epMacroCode.requestFocusInWindow();
            bRemove.setEnabled(true);
            bSetShortcut.setEnabled(true);
        }
    }
    
    private void tMacrosTableChanged(final TableModelEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (evt.getType() == TableModelEvent.INSERT) {
                    tMacros.getSelectionModel().setSelectionInterval(evt.getFirstRow(), evt.getFirstRow());
                } else if (evt.getType() == TableModelEvent.DELETE) {
                    // try the next row after the deleted one
                    int tableRow = evt.getLastRow();
                    if (tableRow < tMacros.getModel().getRowCount()) {
                        tMacros.getSelectionModel().setSelectionInterval(tableRow, tableRow);
                    } else {
                        // try the previous row
                        tableRow = evt.getFirstRow() - 1;
                        if (tableRow >= 0) {
                            tMacros.getSelectionModel().setSelectionInterval(tableRow, tableRow);
                        } else {
                            tMacros.getSelectionModel().clearSelection();
                        }
                    }
                }
            }
        });
    }
    
    private void epMacroCodeDocumentChanged() {
        int index = tMacros.getSelectedRow();
        if (index >= 0) {
            model.getMacroByIndex(index).setCode(epMacroCode.getText());
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNew;
    private javax.swing.JButton bRemove;
    private javax.swing.JButton bSetShortcut;
    private javax.swing.JEditorPane epMacroCode;
    private javax.swing.JLabel lMacroCode;
    private javax.swing.JLabel lMacros;
    private javax.swing.JScrollPane sMacroCode;
    private javax.swing.JScrollPane spMacros;
    private javax.swing.JTable tMacros;
    // End of variables declaration//GEN-END:variables
    
    private static String loc(String key) {
        return NbBundle.getMessage(MacrosPanel.class, key);
    }

    private static void loc(Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext().setAccessibleName(loc("AN_" + key)); //NOI18N
            c.getAccessibleContext().setAccessibleDescription(loc("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton) c, loc("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText((JLabel) c, loc("CTL_" + key)); //NOI18N
        }
    }

    private MacrosModel.Macro addMacro() {
        InputLine descriptor = new InputLine(loc("CTL_Enter_macro_name"), loc("CTL_New_macro_dialog_title")); //NOI18N
        if (DialogDisplayer.getDefault().notify(descriptor) == InputLine.OK_OPTION) {
            String macroName = descriptor.getInputText().trim();
            String err = model.validateMacroName(macroName);
            if (err == null) {
                return model.createMacro(MimePath.EMPTY, macroName);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(err, NotifyDescriptor.ERROR_MESSAGE));
            }
        }
        return null;
    }
}
