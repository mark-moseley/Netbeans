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
package org.netbeans.modules.visualweb.propertyeditors;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;


/**
 * A custom property editor for selecting CSS style classes from among a list of
 * all available classes.
 *
 * @author gjmurphy
 */
public class StyleClassPropertyPanel extends PropertyPanelBase {

    static final String SELECT_ACTION = "select"; //NOI18N
    static final String DESELECT_ACTION = "deselect"; //NOI18N
    static final String SELECT_ALL_ACTION = "select_all"; //NOI18N
    static final String DESELECT_ALL_ACTION = "deselect_all"; //NOI18N

    StyleClassPropertyEditor editor;

    DefaultListModel availableListModel;
    DefaultListModel selectedListModel;

    public StyleClassPropertyPanel(StyleClassPropertyEditor editor) {
        super(editor);
        assert(editor != null);
        this.editor = editor;
        // Initialize list models
        this.selectedListModel = new DefaultListModel();
        String[] selectedStyleClasses = editor.getStyleClasses();
        Set selectStyleClassesSet = new HashSet();
        for (int i = 0; i < selectedStyleClasses.length; i++) {
            this.selectedListModel.addElement(selectedStyleClasses[i]);
            selectStyleClassesSet.add(selectedStyleClasses[i]);
        }
        this.availableListModel = new DefaultListModel();
        String[] availableStyleClasses = editor.getAvailableStyleClasses();
        for (int i = 0; i < availableStyleClasses.length; i++) {
            if (!selectStyleClassesSet.contains(availableStyleClasses[i]))
                this.availableListModel.addElement(availableStyleClasses[i]);
        }
        // Initialize UI components
        initComponents();
        this.availableList.addKeyListener(new ListKeyListener(this.availableList));
    }

    public Object getPropertyValue() {
        if (this.selectedListModel.size() == 0)
            return null;
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.selectedListModel.get(0));
        for (int i = 1; i < this.selectedListModel.size(); i++) {
            buffer.append(" ");
            buffer.append(this.selectedListModel.get(i));
        }
        return buffer.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        titleLabel = new javax.swing.JLabel();
        availableListScrollPane = new javax.swing.JScrollPane();
        availableList = new JList(this.availableListModel);
        selectButtonPanel = new javax.swing.JPanel();
        selectButton = new javax.swing.JButton();
        deselectButton = new javax.swing.JButton();
        selectAllButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();
        selectedListScrollPane = new javax.swing.JScrollPane();
        selectedList = new JList(this.selectedListModel);
        availableLabel = new javax.swing.JLabel();
        selectedLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/propertyeditors/Bundle"); // NOI18N
        titleLabel.setText(bundle.getString("StyleClassPropertyEditor.title")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 6, 11);
        add(titleLabel, gridBagConstraints);

        availableListScrollPane.setViewportView(availableList);
        availableList.getAccessibleContext().setAccessibleName(bundle.getString("StyleClassPropertyEditor.availableList.AccessibleName")); // NOI18N
        availableList.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.availableList.AccessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(availableListScrollPane, gridBagConstraints);

        selectButtonPanel.setLayout(new javax.swing.BoxLayout(selectButtonPanel, javax.swing.BoxLayout.Y_AXIS));

        selectButton.setFont(selectButton.getFont().deriveFont(Font.BOLD));
        selectButton.setText(">");
        selectButton.setActionCommand(SELECT_ACTION);
        selectButton.setAlignmentY(0.0F);
        selectButton.setMaximumSize(new java.awt.Dimension(45, 23));
        selectButton.setMinimumSize(new java.awt.Dimension(45, 23));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });
        selectButtonPanel.add(selectButton);
        selectButton.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.addButton.AccessibleDescription")); // NOI18N

        deselectButton.setFont(deselectButton.getFont().deriveFont(Font.BOLD));
        deselectButton.setText("<");
        deselectButton.setActionCommand(DESELECT_ACTION);
        deselectButton.setMaximumSize(new java.awt.Dimension(45, 23));
        deselectButton.setMinimumSize(new java.awt.Dimension(45, 23));
        selectButtonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 5)));
        deselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });
        selectButtonPanel.add(deselectButton);
        deselectButton.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.removeButton.AccessibleDescription")); // NOI18N

        selectAllButton.setFont(selectAllButton.getFont().deriveFont(Font.BOLD));
        selectAllButton.setText(">>");
        selectAllButton.setActionCommand(SELECT_ALL_ACTION);
        selectButtonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 5)));
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });
        selectButtonPanel.add(selectAllButton);
        selectAllButton.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.addAllButton.AccessibleDescription")); // NOI18N

        deselectAllButton.setFont(deselectAllButton.getFont().deriveFont(Font.BOLD));
        deselectAllButton.setText("<<");
        deselectAllButton.setActionCommand(DESELECT_ALL_ACTION);
        selectButtonPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 5)));
        deselectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });
        selectButtonPanel.add(deselectAllButton);
        deselectAllButton.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.removeAllButton.AccessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(selectButtonPanel, gridBagConstraints);

        selectedListScrollPane.setViewportView(selectedList);
        selectedList.getAccessibleContext().setAccessibleName(bundle.getString("StyleClassPropertyEditor.selectedList.AccessibleName")); // NOI18N
        selectedList.getAccessibleContext().setAccessibleDescription(bundle.getString("StyleClassPropertyEditor.selectedList.AccessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 9);
        add(selectedListScrollPane, gridBagConstraints);

        availableLabel.setLabelFor(availableList);
        org.openide.awt.Mnemonics.setLocalizedText(availableLabel, org.openide.util.NbBundle.getMessage(StyleClassPropertyPanel.class, "StyleClassPropertyEditor.availableStyleClasses")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        add(availableLabel, gridBagConstraints);

        selectedLabel.setLabelFor(selectedList);
        org.openide.awt.Mnemonics.setLocalizedText(selectedLabel, org.openide.util.NbBundle.getMessage(StyleClassPropertyPanel.class, "StyleClassPropertyEditor.selectedStyleClasses")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(selectedLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void handleButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handleButtonAction
        String action = evt.getActionCommand();
        if (action.equals(SELECT_ACTION)) {
            int[] selectedIndices = this.availableList.getSelectedIndices();
            for (int i = selectedIndices.length - 1; i >= 0; i--)
                this.selectedListModel.addElement(this.availableListModel.remove(selectedIndices[i]));
        } else if (action.equals(DESELECT_ACTION)) {
            int[] selectedIndices = this.selectedList.getSelectedIndices();
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                String styleClass = (String) this.selectedListModel.remove(selectedIndices[i]);
                this.availableListModel.add(-1 - Arrays.binarySearch(this.availableListModel.toArray(), styleClass), styleClass);
            }
        } else if (action.equals(SELECT_ALL_ACTION)) {
            for (int i = 0; i < this.availableListModel.size(); i++)
                this.selectedListModel.addElement(this.availableListModel.get(i));
            this.availableListModel.removeAllElements();
        } else if (action.equals(DESELECT_ALL_ACTION)) {
            for (int i = 0; i < this.selectedListModel.size(); i++) {
                String styleClass = (String) this.selectedListModel.get(i);
                this.availableListModel.add(-1 - Arrays.binarySearch(this.availableListModel.toArray(), styleClass), styleClass);
            }
            this.selectedListModel.removeAllElements();
        }
    }//GEN-LAST:event_handleButtonAction
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availableLabel;
    private javax.swing.JList availableList;
    private javax.swing.JScrollPane availableListScrollPane;
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JButton deselectButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectButton;
    private javax.swing.JPanel selectButtonPanel;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JList selectedList;
    private javax.swing.JScrollPane selectedListScrollPane;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * A listener that implements key-based navigation through a JList.
     */
    class ListKeyListener implements KeyListener {
        
        // Delay in milliseconds before keystroke buffer will be erased
        static final long BUFFER_ERASE_DELAY = 350;
        
        JList list;
        StringBuffer keyStrokeBuffer;
        Timer keyStrokeTimer;
        TimerTask bufferEraseTask;
        
        ListKeyListener(JList list) {
            this.list = list;
            keyStrokeBuffer = new StringBuffer(16);
            keyStrokeTimer = new Timer();
        }
        
        public void keyTyped(KeyEvent event) {
            char c = event.getKeyChar();
            if (!Character.isISOControl(c)) {
                synchronized(keyStrokeBuffer) {
                    if (bufferEraseTask != null)
                        bufferEraseTask.cancel();
                    keyStrokeBuffer.append(c);
                    bufferEraseTask = new TimerTask() {
                        public void run() {
                            keyStrokeBuffer.setLength(0);
                        }
                    };
                    repositionSelectedItem(keyStrokeBuffer.toString());
                    keyStrokeTimer.schedule(bufferEraseTask, BUFFER_ERASE_DELAY);
                }
            }
        }
        
        private void repositionSelectedItem(String prefix) {
            int i = this.list.getSelectedIndex() - 1;
            ListModel listModel = this.list.getModel();
            // If an item is already selected, try to find the next item that
            // matches the current set of typed keys
            if (i >= 0) {
                for (int j = i + 1; j < listModel.getSize(); j++) {
                    if (((String) listModel.getElementAt(j)).regionMatches(true, 0, prefix, 0, prefix.length())) {
                        this.list.setSelectedIndex(j);
                        this.list.ensureIndexIsVisible(j);
                        return;
                    }
                }
            }
            // Either no item was selected, or an item was selected but no
            // subsequent matching item was found: so, start search for a match
            // from the beginning of the list.
            for (int j = 0; j < listModel.getSize(); j++) {
                if (((String) listModel.getElementAt(j)).regionMatches(true, 0, prefix, 0, prefix.length())) {
                    this.list.setSelectedIndex(j);
                    this.list.ensureIndexIsVisible(j);
                    return;
                }
            }
            // No matches were found anywhere in the list: leave current selection,
            // if any, alone.
            return;
        }
        
        public void keyReleased(KeyEvent event) {
        }
        
        public void keyPressed(KeyEvent event) {
        }
        
    }
}
