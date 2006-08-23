/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.openide.util.Utilities;

public class StdLibPanel extends javax.swing.JPanel {

    private MyListCellRenderer myListCellRenderer = new MyListCellRenderer();

    /** Creates new form StdLibPanel */
    public StdLibPanel(LibraryItem.StdLibItem[] stdLibs) {
        initComponents();
	libraryList = new JList(stdLibs);
	libraryList.setCellRenderer(myListCellRenderer);
        scrollPane.setViewportView(libraryList);
	setPreferredSize(new java.awt.Dimension(300, 300));
	manageLibrariesButton.setEnabled(false);
    }

    private final class MyListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    LibraryItem libraryItem = (LibraryItem)value;
	    label.setIcon(new ImageIcon(Utilities.loadImage(libraryItem.getIconName())));
	    label.setToolTipText(libraryItem.getToolTip());
            return label;
        }
    }   

    public LibraryItem.StdLibItem[] getSelectedStdLibs() {
	Object[] selectedValues = libraryList.getSelectedValues();
	LibraryItem.StdLibItem[] selectedLibs = new LibraryItem.StdLibItem[selectedValues.length];
	for (int i = 0; i < selectedValues.length; i++)
	    selectedLibs[i] = (LibraryItem.StdLibItem)selectedValues[i];
	return selectedLibs;
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        label = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        libraryList = new javax.swing.JList();
        manageLibrariesButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        label.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("STANDARD_LIBRARIES_MN").charAt(0));
        label.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("STANDARD_LIBRARIES_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(label, gridBagConstraints);

        scrollPane.setViewportView(libraryList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 12);
        add(scrollPane, gridBagConstraints);

        manageLibrariesButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("MANAGE_LIBRARIES_MN").charAt(0));
        manageLibrariesButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("MANAGE_LIBRARIES_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 12);
        add(manageLibrariesButton, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JList libraryList;
    private javax.swing.JButton manageLibrariesButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
}
