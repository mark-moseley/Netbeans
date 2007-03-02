/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.propertyeditors.domains.Domain;
import com.sun.rave.propertyeditors.domains.EditableDomain;
import com.sun.rave.propertyeditors.domains.Element;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.DefaultListModel;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * A custom property editor panel for selecting one element of a domain of
 * elements. The panel presents the elements in a scrollable listbox, and
 * emulates the keystroke look-ahead functionality found in MS Windows file
 * explorer.
 *
 * @author gjmurphy
 */
public class SelectOneDomainPanel extends PropertyPanelBase {

    static ResourceBundle bundle =
            ResourceBundle.getBundle(SelectOneDomainPanel.class.getPackage().getName() + ".Bundle"); //NOI18N

    // The domain from which this panel draws its elements
    protected Domain domain;
    // The element selected by the user
    protected Element element;

    private DefaultListModel listModel;

    /**
     * Creates a new form SelectOneDomainPanel, with elements drawn from the
     * specified property editor's domain.
     */
    public SelectOneDomainPanel(SelectOneDomainEditor propertyEditor) {
        super(propertyEditor);
        domain = propertyEditor.getDomain();
        element = propertyEditor.getElement();
        assert domain != null;
        // Populate a list model with domain elements
        this.listModel = new DefaultListModel();
        Element[] elements = domain.getElements();
        for (int i = 0; i < elements.length; i++)
            listModel.addElement(elements[i].getLabel());
        // Initialize the user interface
        initComponents();
        // Show editing buttons only when domain is editable
        if (domain instanceof EditableDomain)
            this.buttonPanel.setVisible(true);
        else
            this.buttonPanel.setVisible(false);
        // If there is a property value already, highlight the element's label
        // in the list widget
        if (element != null) {
            int i = 0;
            while (i < elements.length) {
                if (element.equals(elements[i]))
                    break;
                i++;
            }
            if (i < elements.length) {
                this.domainList.setSelectedIndex(i);
                this.domainList.ensureIndexIsVisible(i);
            }
        }
        // If no property value already, do not enable edit and delete buttons
        else {
            this.editButton.setEnabled(false);
            this.deleteButton.setEnabled(false);
        }
        // Initialize list event listeners
        ListEventListener eventListener = new ListEventListener(domainList);
        this.domainList.addListSelectionListener(eventListener);
        this.domainList.addKeyListener(eventListener);
    }

    public Object getPropertyValue() throws IllegalStateException {
        if (this.element == null)
            return null;
        return this.element.getValue();
    }

    /**
     * Called when a new list item has been selected.
     */
    public void valueChanged(ListSelectionEvent event) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        String domainDisplayName = this.domain.getDisplayName();
        String listLabelString;
        if (domainDisplayName == null)
        listLabelString = bundle.getString("SelectOneDomainPanel.list.label.default");
        else
        listLabelString = MessageFormat.format(bundle.getString("SelectOneDomainPanel.list.label"),
            new String[]{domainDisplayName});
        listLabel = new javax.swing.JLabel();
        domainScrollPane = new javax.swing.JScrollPane();
        domainList = new JList(this.listModel);
        buttonPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        listLabel.setLabelFor(domainList);
        listLabel.setText(listLabelString);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 11);
        add(listLabel, gridBagConstraints);

        domainList.setMinimumSize(new java.awt.Dimension(64, 64));
        domainList.setVisibleRowCount(getLabelRowCount());
        domainScrollPane.setViewportView(domainList);
        domainList.getAccessibleContext().setAccessibleName(bundle.getString("SelectOneDomainPanel.list.accessibleName"));
        domainList.getAccessibleContext().setAccessibleDescription(bundle.getString("SelectOneDomainPanel.list.accessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 11);
        add(domainScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 5));

        newButton.setMnemonic(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.new.mnemonic").charAt(0));
        newButton.setText(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.new"));
        newButton.setActionCommand("new");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });

        buttonPanel.add(newButton);
        newButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.new.accessibleName"));
        newButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.new.accessibleDescription"));

        editButton.setMnemonic(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.edit.mnemonic").charAt(0));
        editButton.setText(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.edit"));
        editButton.setActionCommand("edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });

        buttonPanel.add(editButton);
        editButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.edit.accessibleName"));
        editButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.edit.accessibleDescription"));

        deleteButton.setMnemonic(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.delete.mnemonic").charAt(0));
        deleteButton.setText(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.delete"));
        deleteButton.setActionCommand("delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleButtonAction(evt);
            }
        });

        buttonPanel.add(deleteButton);
        deleteButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.delete.accessibleName"));
        deleteButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle").getString("SelectOneDomainPanel.button.delete.accessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
        add(buttonPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void handleButtonAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handleButtonAction
        String action = evt.getActionCommand();
        EditableDomain editableDomain = (EditableDomain) this.domain;
        if (action.equals("new")) {
            Element newElement = (new EditElementDialog(editableDomain)).showDialog(this);
            if (newElement != null) {
                editableDomain.addElement(newElement);
                this.listModel.addElement(newElement.getLabel());
                this.domainList.setSelectedIndex(editableDomain.getSize() - 1);
            }
        } else if (action.equals("edit")) {
            int index = this.domainList.getSelectedIndex();
            if (index >= 0 && index < domain.getSize()) {
                Element element = editableDomain.getElementAt(index);
                Element newElement = (new EditElementDialog(element, editableDomain)).showDialog(this);
                if (newElement != null && !(newElement.equals(element))) {
                    editableDomain.setElementAt(index, newElement);
                    this.listModel.set(index, newElement.getLabel());
                }
            }
        } else if (action.equals("delete")) {
            int index = this.domainList.getSelectedIndex();
            if (index >= 0 && index < domain.getSize()) {
                editableDomain.removeElementAt(index);
                this.listModel.remove(index);
                if (index == this.listModel.getSize())
                    index--;
                this.domainList.setSelectedIndex(index);
            }
        }
    }//GEN-LAST:event_handleButtonAction
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JList domainList;
    private javax.swing.JScrollPane domainScrollPane;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel listLabel;
    private javax.swing.JButton newButton;
    // End of variables declaration//GEN-END:variables
    
    private int getLabelColumnCount() {
        int c = 8;
        Element[] elements = this.domain.getElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getLabel().length() > c)
                c = elements[i].getLabel().length();
        }
        return c;
    }
    
    private int getLabelRowCount() {
        return this.domain.getSize() < 16 ? this.domain.getSize() : 16;
    }
    
    
    class ListEventListener implements KeyListener, ListSelectionListener {
        
        // Delay in milliseconds before keystroke buffer will be erased
        static final long BUFFER_ERASE_DELAY = 350;
        
        JList list;
        StringBuffer keyStrokeBuffer;
        Timer keyStrokeTimer;
        TimerTask bufferEraseTask;
        
        ListEventListener(JList list) {
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
            // No matchs were found anywhere in the list: leave current selection,
            // if any, alone.
            return;
        }
        
        public void keyReleased(KeyEvent event) {
        }
        
        public void keyPressed(KeyEvent event) {
        }
        
        /**
         * User selected a list item by clicking on it. Set the current element
         * to be that with the selected label.
         */
        public void valueChanged(ListSelectionEvent event) {
            int index = ((JList) event.getSource()).getSelectedIndex();
            SelectOneDomainPanel.this.element =
                    SelectOneDomainPanel.this.domain.getElementAt(index);
            SelectOneDomainPanel.this.editButton.setEnabled(true);
            SelectOneDomainPanel.this.deleteButton.setEnabled(true);
        }
        
    }
}
