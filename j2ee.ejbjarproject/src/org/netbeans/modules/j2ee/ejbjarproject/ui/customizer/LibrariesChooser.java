/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.openide.util.WeakListeners;

import java.util.Comparator;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
/**
 *
 * @author  tz97951
 */
public class LibrariesChooser extends javax.swing.JPanel {

    /** Creates new form LibrariesChooser */
    public LibrariesChooser() {
        initComponents();
        jList1.setPrototypeCellValue("0123456789012345678901234");      //NOI18N
        jList1.setModel(new LibrariesListModel());
        jList1.setCellRenderer(new LibraryRenderer());
    }



    public Library[] getSelectedLibraries () {
        Object[] selected = this.jList1.getSelectedValues();
        Library[] libraries = new Library[selected.length];
        System.arraycopy(selected,0,libraries,0,selected.length);
        return libraries;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        edit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("LBL_LibrariesChooser_LibList_LabelMnemonic").charAt(0));
        jLabel1.setLabelFor(jList1);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("CTL_InstalledLibraries"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(jList1);
        jList1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("ACS_LibrariesChooser_LibList_A11YDesc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        edit.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("LBL_LibrariesChooser_Edit_LabelMnemonic").charAt(0));
        edit.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("CTL_EditLibraries"));
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLibraries(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 12);
        add(edit, gridBagConstraints);
        edit.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("ACS_LibrariesChooser_Edit_A11YDesc"));

    }//GEN-END:initComponents

    private void editLibraries(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibraries
        LibrariesCustomizer.showCustomizer((Library)this.jList1.getSelectedValue());
    }//GEN-LAST:event_editLibraries


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton edit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables



    private static final class LibrariesListModel extends AbstractListModel implements PropertyChangeListener {

        private Library[] cache;

        public LibrariesListModel () {
            LibraryManager manager = LibraryManager.getDefault();
            manager.addPropertyChangeListener((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,
                    this, manager));
        }

        public synchronized int getSize() {
            if (this.cache == null) {
                this.cache = this.getLibraries();
            }
            return this.cache.length;
        }

        public synchronized Object getElementAt(int index) {
            if (this.cache == null) {
                this.cache = this.getLibraries();
            }
            assert index >= 0 && index < this.cache.length;
            return this.cache[index];
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            this.cache = null;
        }

        private Library[] getLibraries () {
            Library[] libs = LibraryManager.getDefault().getLibraries();
            Arrays.sort(libs, new Comparator () {
                public int compare (Object o1, Object o2) {
                    assert (o1 instanceof Library) && (o2 instanceof Library);
                    String name1 = ((Library)o1).getDisplayName();
                    String name2 = ((Library)o2).getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                }
            });
            return libs;
        }
    }


    private static final class LibraryRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String displayName;
            if (value instanceof Library) {
                displayName = ((Library)value).getDisplayName();
            }
            else if (value instanceof String) {
                displayName = (String) value;
            }
            else {
                displayName = value.toString();
            }
            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }

}
