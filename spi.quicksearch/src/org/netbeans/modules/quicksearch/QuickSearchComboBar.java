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

package org.netbeans.modules.quicksearch;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public class QuickSearchComboBar extends javax.swing.JPanel {

    QuickSearchPopup displayer = new QuickSearchPopup(this);
    WeakReference<TopComponent> caller;

    Color origForeground;
    private KeyStroke keyStroke;

    public QuickSearchComboBar(KeyStroke ks) {
        this();
        keyStroke = ks;
        setShowHint(true);
    }

    /** Creates new form SilverLightComboBar */
    public QuickSearchComboBar() {
        initComponents();

        command.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void removeUpdate(DocumentEvent arg0) {
                textChanged();
            }

            public void changedUpdate(DocumentEvent arg0) {
                textChanged();
            }

            private void textChanged () {
                if (command.isFocusOwner()) {
                    displayer.maybeEvaluate(command.getText());
                }
            }

        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        command = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 1));
        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setName("Form"); // NOI18N
        setOpaque(false);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setBackground(getTextBackground());
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(getShadowColor()));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/quicksearch/resources/find.png"))); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.jLabel2.toolTipText")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 1);
        jPanel1.add(jLabel2, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(2, 18));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        command.setColumns(15);
        command.setRows(1);
        command.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        command.setName("command"); // NOI18N
        command.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                commandFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                commandFocusLost(evt);
            }
        });
        command.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commandKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(command);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
        jPanel1.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
    displayer.setVisible(false);
}//GEN-LAST:event_formFocusLost

private void commandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commandKeyPressed
    if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
        displayer.selectNext();
        evt.consume();
    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
        displayer.selectPrev();
        evt.consume();
    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        evt.consume();
        invokeSelectedItem();
    } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
        returnFocus();
        displayer.clearModel();
    }
}//GEN-LAST:event_commandKeyPressed

    /** Actually invokes action selected in the results list */
    public void invokeSelectedItem () {
        // #137259: invoke only some results were found
        if (displayer.getList().getModel().getSize() > 0) {
            returnFocus();
            // #137342: run action later to let focus indeed be transferred
            // by previous returnFocus() call
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayer.invoke();
                    }
            });
        }
    }

    private void returnFocus () {
        displayer.setVisible(false);
        if (caller != null) {
            TopComponent tc = caller.get();
            if (tc != null) {
                tc.requestActive();
                tc.requestFocus();
            }
        }
    }


private void commandFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commandFocusLost
    displayer.setVisible(false);
    setShowHint(true);
}//GEN-LAST:event_commandFocusLost

private void commandFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commandFocusGained
    setShowHint(false);
}//GEN-LAST:event_commandFocusGained

    private void setShowHint (boolean showHint) {
        // remember orig color on first invocation
        if (origForeground == null) {
            origForeground = command.getForeground();
        }
        if (showHint) {
            command.setForeground(command.getDisabledTextColor());
            String sc = keyStroke == null ? "" : " (" + keyStroke.toString().replace(" pressed ", "+") + ")"; //NOI18N
            command.setText(NbBundle.getMessage(QuickSearchComboBar.class, "MSG_DiscoverabilityHint") + sc); //NOI18N
        } else {
            command.setForeground(origForeground);
            command.setText("");
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea command;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables


    @Override
    public void requestFocus() {
        caller = new WeakReference<TopComponent>(TopComponent.getRegistry().getActivated());
        super.requestFocus();
        command.requestFocus();
    }

    JTextArea getCommand() {
        return command;
    }

    static Color getShadowColor () {
        Color shadow = UIManager.getColor("controlShadow");
        return shadow != null ? shadow : Color.GRAY;
    }

    static Color getTextBackground () {
        Color textB = UIManager.getColor("TextPane.background");
        return textB != null ? textB : Color.WHITE;
    }

    static Color getResultBackground () {
        return getTextBackground();
    }

    static Color getCategoryTextColor () {
        Color shadow = UIManager.getColor("textInactiveText");
        return shadow != null ? shadow : Color.DARK_GRAY;
    }

}
