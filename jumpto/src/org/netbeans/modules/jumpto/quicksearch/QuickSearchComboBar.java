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

package org.netbeans.modules.jumpto.quicksearch;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.openide.util.Exceptions;

/**
 *
 * @author  Jan Becicka
 */
public class QuickSearchComboBar extends javax.swing.JPanel {
    
    Popup popup;
    QuickSearchPopup displayer = new QuickSearchPopup();
    
    /** Creates new form SilverLightComboBar */
    public QuickSearchComboBar() {
        initComponents();

        command.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent arg0) {
                processCommand(command.getText());
            }

            public void removeUpdate(DocumentEvent arg0) {
                processCommand(command.getText());
            }

            public void changedUpdate(DocumentEvent arg0) {
                processCommand(command.getText());
            }
        });
    }

    private void processCommand(String text) {
        try {

            if (popup == null && !"".equals(command.getText())) {
                final int caretOffset = command.getCaretPosition();
                Rectangle carretRectangle = command.modelToView(command.getCaretPosition());
                Point where = new Point(0, carretRectangle.height);
                SwingUtilities.convertPointToScreen(where, command);
                popup = PopupFactory.getSharedInstance().getPopup(command, displayer, where.x, where.y);
                popup.show();
                command.requestFocus();
            }
            displayer.update(text);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        command = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setBackground(command.getBackground());
        setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
        setName("Form"); // NOI18N
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        command.setName("command"); // NOI18N
        command.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commandFocusLost(evt);
            }
        });
        command.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                commandKeyPressed(evt);
            }
        });

        jLabel2.setBackground(command.getBackground());
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jumpto/resources/find.png"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.setOpaque(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jLabel2)
                .add(0, 0, 0)
                .add(command, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 555, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(command, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jLabel2)
        );
    }// </editor-fold>//GEN-END:initComponents

private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
//    if (popup!=null)
//        popup.hide();
//    popup = null;
}//GEN-LAST:event_formFocusLost

private void commandFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commandFocusLost
    if (popup!=null)
        popup.hide();
    popup = null;
}//GEN-LAST:event_commandFocusLost

private void commandKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commandKeyPressed
    if (evt.getKeyCode()==KeyEvent.VK_DOWN) {
        displayer.selectNext();
        evt.consume();
    } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
        displayer.selectPrev();
        evt.consume();
    } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        displayer.invoke();
        evt.consume();
        popup.hide();
        popup=null;
        command.setText("");
    } else if ((evt.getKeyCode()) == KeyEvent.VK_ESCAPE) {
    if (popup != null) {
        popup.hide();
    }
    popup = null;
    }

}//GEN-LAST:event_commandKeyPressed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField command;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables


    @Override
    public void requestFocus() {
        super.requestFocus();
        command.requestFocus();
    }
}
