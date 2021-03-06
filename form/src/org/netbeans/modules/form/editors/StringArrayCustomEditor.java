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

package org.netbeans.modules.form.editors;

import java.util.Vector;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;

import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;


/** A custom editor for array of Strings.
*
* @author  Ian Formanek
*/
public class StringArrayCustomEditor extends javax.swing.JPanel {

    // the bundle to use
    private ResourceBundle bundle = NbBundle.getBundle (
                                       StringArrayCustomEditor.class);

    private Vector<String> itemsVector;
    private StringArrayCustomizable editor;

    private final static int DEFAULT_WIDTH = 400;

    static final long serialVersionUID =-4347656479280614636L;

    /** Initializes the Form */
    public StringArrayCustomEditor(StringArrayCustomizable sac) {
        editor = sac;
        itemsVector = new Vector<String>();
        String[] array = editor.getStringArray ();
        if (array != null)
            for (int i = 0; i < array.length; i++)
                itemsVector.addElement (array[i]);
        initComponents ();
        itemList.setCellRenderer (new EmptyStringListCellRenderer ());
        itemList.setListData (itemsVector);
        itemList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

        setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(16, 8, 8, 0)));
        buttonsPanel.setBorder (new javax.swing.border.EmptyBorder (new java.awt.Insets(0, 5, 5, 5)));

        Mnemonics.setLocalizedText(itemLabel, bundle.getString("CTL_Item")); // NOI18N
        Mnemonics.setLocalizedText(itemListLabel, bundle.getString("CTL_ItemList")); // NOI18N
        Mnemonics.setLocalizedText(addButton, bundle.getString("CTL_Add_StringArrayCustomEditor")); // NOI18N
        Mnemonics.setLocalizedText(changeButton, bundle.getString("CTL_Change_StringArrayCustomEditor")); // NOI18N
        Mnemonics.setLocalizedText(removeButton, bundle.getString("CTL_Remove")); // NOI18N
        Mnemonics.setLocalizedText(moveUpButton, bundle.getString("CTL_MoveUp")); // NOI18N
        Mnemonics.setLocalizedText(moveDownButton, bundle.getString("CTL_MoveDown")); // NOI18N

        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_StringArrayCustomEditor")); // NOI18N
        itemField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Item")); // NOI18N
        itemList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_ItemList")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Add_StringArrayCustomEditor")); // NOI18N
        changeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Change_StringArrayCustomEditor")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Remove")); // NOI18N
        moveUpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_MoveUp")); // NOI18N
        moveDownButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_MoveDown")); // NOI18N

        updateButtons ();
    }

    @Override
    public java.awt.Dimension getPreferredSize () {
        // ensure minimum width
        java.awt.Dimension sup = super.getPreferredSize ();
        return new java.awt.Dimension (Math.max (sup.width, DEFAULT_WIDTH), sup.height);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        itemField.requestFocusInWindow();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        editPanel = new javax.swing.JPanel();
        itemListScroll = new javax.swing.JScrollPane();
        itemList = new javax.swing.JList();
        itemLabel = new javax.swing.JLabel();
        itemField = new javax.swing.JTextField();
        itemListLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        changeButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        paddingPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        editPanel.setLayout(new java.awt.GridBagLayout());

        itemList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                itemListValueChanged(evt);
            }
        });
        itemListScroll.setViewportView(itemList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editPanel.add(itemListScroll, gridBagConstraints);

        itemLabel.setText("item");
        itemLabel.setLabelFor(itemField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 12);
        editPanel.add(itemLabel, gridBagConstraints);

        itemField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        editPanel.add(itemField, gridBagConstraints);

        itemListLabel.setText("jLabel1");
        itemListLabel.setLabelFor(itemList);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        editPanel.add(itemListLabel, gridBagConstraints);

        add(editPanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        buttonsPanel.add(addButton, gridBagConstraints);

        changeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        buttonsPanel.add(changeButton, gridBagConstraints);

        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        buttonsPanel.add(removeButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        buttonsPanel.add(jSeparator1, gridBagConstraints);

        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        buttonsPanel.add(moveUpButton, gridBagConstraints);

        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        buttonsPanel.add(moveDownButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        buttonsPanel.add(paddingPanel, gridBagConstraints);

        add(buttonsPanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

private void itemFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemFieldActionPerformed
        if (itemList.getSelectedIndex() >= 0) {
            changeButtonActionPerformed(evt);
        } else {
            addButtonActionPerformed(evt);
        }
    }//GEN-LAST:event_itemFieldActionPerformed

    private void changeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (itemField.getText (), sel);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_changeButtonActionPerformed

    private void moveDownButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = itemsVector.elementAt (sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (s, sel + 1);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel + 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        int sel = itemList.getSelectedIndex ();
        String s = itemsVector.elementAt (sel);
        itemsVector.removeElementAt (sel);
        itemsVector.insertElementAt (s, sel - 1);
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (sel - 1);
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        int currentIndex = itemList.getSelectedIndex ();
        itemsVector.removeElementAt (currentIndex);
        itemList.setListData (itemsVector);

        // set new selection
        if (itemsVector.size () != 0) {
            if (currentIndex >= itemsVector.size ())
                currentIndex = itemsVector.size () - 1;
            itemList.setSelectedIndex (currentIndex);
        }

        itemList.repaint ();

        updateValue ();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void itemListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_itemListValueChanged
        // Add your handling code here:
        updateButtons ();
        int sel = itemList.getSelectedIndex ();
        if (sel != -1) {
            itemField.setText (itemsVector.elementAt (sel));
        }
    }//GEN-LAST:event_itemListValueChanged

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        itemsVector.addElement (itemField.getText ());
        itemList.setListData (itemsVector);
        itemList.setSelectedIndex (-1);
        itemField.setSelectionStart(0);
        itemField.setSelectionEnd(itemField.getText().length());
        itemList.repaint ();
        updateValue ();
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtons () {
        int sel = itemList.getSelectedIndex ();
        if (sel == -1) {
            removeButton.setEnabled (false);
            moveUpButton.setEnabled (false);
            moveDownButton.setEnabled (false);
            changeButton.setEnabled (false);
        } else {
            removeButton.setEnabled (true);
            moveUpButton.setEnabled (sel != 0);
            moveDownButton.setEnabled (sel != itemsVector.size () - 1);
            changeButton.setEnabled (true);
        }
    }

    private void updateValue () {
        String [] value = new String [itemsVector.size()];
        itemsVector.copyInto (value);
        editor.setStringArray (value);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton changeButton;
    private javax.swing.JPanel editPanel;
    private javax.swing.JTextField itemField;
    private javax.swing.JLabel itemLabel;
    private javax.swing.JList itemList;
    private javax.swing.JLabel itemListLabel;
    private javax.swing.JScrollPane itemListScroll;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    static class EmptyStringListCellRenderer extends JLabel implements ListCellRenderer {

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        static final long serialVersionUID =487512296465844339L;
        /** Creates a new NodeListCellRenderer */
        public EmptyStringListCellRenderer () {
            setOpaque (true);
            setBorder (noFocusBorder);
        }

        /** This is the only method defined by ListCellRenderer.  We just
        * reconfigure the Jlabel each time we're called.
        */
        public java.awt.Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof String)) return this;
            String text = (String)value;
            if ("".equals (text)) text = NbBundle.getMessage (EmptyStringListCellRenderer.class, "CTL_Empty"); // NOI18N

            setText(text);
            if (isSelected){
                setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}
