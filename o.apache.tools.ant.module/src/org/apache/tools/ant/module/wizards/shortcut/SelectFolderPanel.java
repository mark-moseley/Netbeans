/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module.wizards.shortcut;

import java.awt.Component;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.loaders.DataFolder;
import org.apache.tools.ant.module.AntModule;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;

/**
 * Wizard panel that lets you select a menu or toolbar folder and a display name
 * for the menu or toolbar item.
 */
final class SelectFolderPanel extends JPanel implements DocumentListener {

    private final String prop;
    private final boolean stripAmps;
    private final SelectFolderWizardPanel wiz;
    private final DataFolder top;
    
    /** Create the wizard panel component and set up some basic properties. */
    public SelectFolderPanel(SelectFolderWizardPanel wiz, String name, String hint, String displayNameLabelText, DataFolder top, boolean stripAmps, String prop) {
        this.wiz = wiz;
        initComponents ();
        // Provide a name in the title bar.
        setName (name);
        hintsArea.setText (hint);
        initAccessibility (hint);                
        displayNameLabel.setText(displayNameLabelText);
        this.prop = prop;
        this.top = top;
        this.stripAmps = stripAmps;
        DefaultListModel model = new DefaultListModel();
        DataObject[] folders = findFolders(top);
        for (int i = 0; i < folders.length; i++) {
            model.addElement(folders[i]);
        }
        folderList.setModel(model);
        folderList.setCellRenderer(new CellRenderer());
        displayNameField.getDocument().addDocumentListener(this);
    }
    
    ListModel getModel() {
        return folderList.getModel();
    }
    
    private String getDisplayName(DataFolder folder) {
        String name = folder.getNodeDelegate().getDisplayName();
        if (stripAmps) {
            // XXX use o.o.a.Mnemonics instead
            int idx = name.indexOf('&');
            if (idx != -1) {
                name = name.substring(0, idx) + name.substring(idx + 1);
            }
        }
        return name;
    }
    
    String getNestedDisplayName(DataFolder folder) {
        DataFolder f = folder;
        StringBuffer b = new StringBuffer();
        while (f != top) {
            if (b.length() > 0) {
                b.insert(0, " \u2192 "); // XXX I18N? just a right-arrow
            }
            b.insert(0, getDisplayName(f));
            f = f.getFolder();
        }
        return b.toString();
    }
    
    private DataFolder getFolder() {
        return (DataFolder)folderList.getSelectedValue();
    }
    
    private void setFolder(DataFolder f) {
        folderList.setSelectedValue(f, true);
    }
    
    private static DataFolder[] findFolders(DataFolder top) {
        List/*<DataFolder>*/ folders = new ArrayList();
        // Needs to be DFS, so children(true) is no good
        visit(folders, top);
        folders.remove(0);
        return (DataFolder[])folders.toArray(new DataFolder[folders.size()]);
    }
    
    private static void visit(List/*<DataFolder>*/ folders, DataFolder f) {
        folders.add(f);
        DataObject[] kids = f.getChildren();
        for (int i = 0; i < kids.length; i++) {
            if (kids[i] instanceof DataFolder) {
                visit(folders, (DataFolder)kids[i]);
            }
        }
    }
    
    private final class CellRenderer extends DefaultListCellRenderer {
        
        public CellRenderer() {}
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DataFolder f = (DataFolder)value;
            String display = getNestedDisplayName(f);
            return super.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
        }
        
    }
    
    // --- VISUAL DESIGN OF PANEL ---
    
    public void requestFocus() {
        super.requestFocus();
        folderList.requestFocus();
    }
    
    private void initAccessibility(String hint) {
        this.getAccessibleContext().setAccessibleDescription(hint);    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        hintsArea = new javax.swing.JTextArea();
        folderScrollPane = new javax.swing.JScrollPane();
        folderList = new javax.swing.JList();
        displayNamePanel = new javax.swing.JPanel();
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout(0, 11));

        hintsArea.setEditable(false);
        hintsArea.setFont(javax.swing.UIManager.getFont ("Label.font"));
        hintsArea.setText("<hints>");
        hintsArea.setBackground(new java.awt.Color(204, 204, 204));
        hintsArea.setLineWrap(true);
        hintsArea.setForeground(new java.awt.Color(102, 102, 153));
        hintsArea.setWrapStyleWord(true);
        hintsArea.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        hintsArea.setOpaque(false);
        hintsArea.setEnabled(false);
        add(hintsArea, java.awt.BorderLayout.NORTH);

        folderList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        folderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                folderListValueChanged(evt);
            }
        });

        folderScrollPane.setViewportView(folderList);
        folderList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectFolderPanel.class, "ACSN_folderList"));
        folderList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectFolderPanel.class, "ACSD_folderList"));

        add(folderScrollPane, java.awt.BorderLayout.CENTER);

        displayNamePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        displayNameLabel.setText("<set display name>");
        displayNameLabel.setLabelFor(displayNameField);
        displayNamePanel.add(displayNameLabel);

        displayNameField.setColumns(30);
        displayNamePanel.add(displayNameField);

        add(displayNamePanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void folderListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_folderListValueChanged
        wiz.fireChangeEvent();
    }//GEN-LAST:event_folderListValueChanged

    public void insertUpdate(DocumentEvent e) {
        // From displayNameField.
        wiz.fireChangeEvent();
    }
    
    public void removeUpdate(DocumentEvent e) {
        // From displayNameField.
        wiz.fireChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        // ignore
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JPanel displayNamePanel;
    private javax.swing.JList folderList;
    private javax.swing.JScrollPane folderScrollPane;
    private javax.swing.JTextArea hintsArea;
    // End of variables declaration//GEN-END:variables

    public static class SelectFolderWizardPanel implements WizardDescriptor.Panel {

        private SelectFolderPanel panel;

        private String namePanel;
        private String hintPanel;
        private String displayNameLabelText;
        private DataFolder topPanel;
        private boolean stripAmpsPanel;
        private String propPanel;
        
        public SelectFolderWizardPanel(String name, String hint, String displayNameLabelText, DataFolder top, boolean stripAmps, String prop) {
            this.namePanel = name;
            this.hintPanel = hint;
            this.displayNameLabelText = displayNameLabelText;
            this.topPanel = top;
            this.stripAmpsPanel = stripAmps;
            this.propPanel = prop;
        }
        
        public Component getComponent () {
            return getPanel();
        }
        
        SelectFolderPanel getPanel() {
            if (panel == null) {
                panel = new SelectFolderPanel(this, namePanel, hintPanel, displayNameLabelText, topPanel, stripAmpsPanel, propPanel);
            }
            return panel;
        }

        public HelpCtx getHelp () {
            return new HelpCtx("ant.wizard.shortcut");
        }

        public boolean isValid () {
            return getPanel().getFolder() != null &&
                getPanel().displayNameField.getText().length() > 0;
        }

        private final Set listeners = new HashSet (1); // Set<ChangeListener>
        public final void addChangeListener (ChangeListener l) {
            synchronized (listeners) {
                listeners.add (l);
            }
        }
        public final void removeChangeListener (ChangeListener l) {
            synchronized (listeners) {
                listeners.remove (l);
            }
        }
        protected final void fireChangeEvent () {
            Iterator it;
            synchronized (listeners) {
                it = new HashSet (listeners).iterator ();
            }
            ChangeEvent ev = new ChangeEvent (this);
            while (it.hasNext ()) {
                ((ChangeListener) it.next ()).stateChanged (ev);
            }
        }

        public void readSettings (Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            getPanel().setFolder((DataFolder)wiz.getProperty(getPanel().prop));
            String dn = (String)wiz.getProperty(ShortcutWizard.PROP_DISPLAY_NAME);
            getPanel().displayNameField.setText(dn != null ? dn : ""); // NOI18N
        }
        
        public void storeSettings (Object settings) {
            DataFolder folder = getPanel().getFolder();
            WizardDescriptor wiz = (WizardDescriptor) settings;
            wiz.putProperty(getPanel().prop, folder);
            wiz.putProperty(ShortcutWizard.PROP_DISPLAY_NAME, getPanel().displayNameField.getText());
        }

    }
}
