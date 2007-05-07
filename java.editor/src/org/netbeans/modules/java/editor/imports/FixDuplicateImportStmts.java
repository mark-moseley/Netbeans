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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor.imports;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * JTable with custom renderer, so second column looks editable (JComboBox).
 * Second column also has CellEditor (also a JComboBox).
 *
 * @author  eakle, Martin Roskanin
 */
public class FixDuplicateImportStmts extends javax.swing.JPanel{
    private JComboBox[] combos;
    private JCheckBox checkUnusedImports;
    
    public FixDuplicateImportStmts() {
        initComponents();
    }
    
    public void initPanel(String[] simpleNames, String[][] choices, Icon[][] icons, String[] defaults, boolean removeUnusedImports) {
        initComponentsMore(simpleNames, choices, icons, defaults, removeUnusedImports);
        setAccessible();
    }
    
    private void initComponentsMore(String simpleNames[], String choices[][], Icon[][] icons, String defaults[], boolean removeUnusedImports) {
        contentPanel.setLayout( new GridBagLayout() );
        contentPanel.setBackground( UIManager.getColor("Table.background") ); //NOI18N
        jScrollPane1.setBorder( UIManager.getBorder("ScrollPane.border") ); //NOI18N
        
        if( choices.length > 0 ) {
        
            int row = 0;

            combos = new JComboBox[choices.length];

            Font monoSpaced = new Font( "Monospaced", Font.PLAIN, new JLabel().getFont().getSize() );
            FocusListener focusListener = new FocusListener() {
                public void focusGained(FocusEvent e) {
                    Component c = e.getComponent();
                    Rectangle r = c.getBounds();
                    contentPanel.scrollRectToVisible( r );
                }
                public void focusLost(FocusEvent arg0) {
                }
            };
            for (int i=0; i<choices.length; i++){
                combos[i] = new JComboBox(choices[i]);
                combos[i].setSelectedItem(defaults[i]);
                combos[i].getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_Combo_ACSD")); //NOI18N
                combos[i].getAccessibleContext().setAccessibleName(getBundleString("FixDupImportStmts_Combo_Name_ACSD")); //NOI18N
                combos[i].setOpaque(false);
                combos[i].setFont( monoSpaced );
                combos[i].addFocusListener( focusListener );
                combos[i].setEnabled( choices[i].length > 1 );
                combos[i].setRenderer( new DelegatingRenderer(combos[i].getRenderer(), choices[i], icons[i] ) );

                JLabel lblSimpleName = new JLabel( simpleNames[i] );
                lblSimpleName.setOpaque( false );
                lblSimpleName.setFont( monoSpaced );
                lblSimpleName.setLabelFor( combos[i] );

                contentPanel.add( lblSimpleName, new GridBagConstraints(0,row,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(3,5,2,5),0,0) );
                contentPanel.add( combos[i], new GridBagConstraints(1,row++,1,1,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(3,5,2,5),0,0) );
            }

            contentPanel.add( new JLabel(), new GridBagConstraints(2,row,2,1,0.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );

            jScrollPane1.setPreferredSize( new Dimension(460, getRowHeight() * Math.min(combos.length, 6) + 0 ) );
        } else {
            contentPanel.add( new JLabel(getBundleString("FixDupImportStmts_NothingToFix")), new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
        }
        
        // load localized text into widgets:
        lblTitle.setText(getBundleString("FixDupImportStmts_IntroLbl")); //NOI18N
        lblHeader.setText(getBundleString("FixDupImportStmts_Header")); //NOI18N
        
        checkUnusedImports = new JCheckBox(getBundleString("FixDupImportStmts_UnusedImports")); //NOI18N
        bottomPanel.add( checkUnusedImports, BorderLayout.WEST );
        checkUnusedImports.setEnabled(removeUnusedImports);
        checkUnusedImports.setSelected(removeUnusedImports);
    }
    
    private int getRowHeight() {
        return combos.length == 0 ? 0 :combos[0].getPreferredSize().height+6;
    }
    
    private static String getBundleString(String s) {
        return NbBundle.getMessage(FixDuplicateImportStmts.class, s);
    }
    
    
    private void setAccessible() {
        getAccessibleContext().setAccessibleDescription(getBundleString("FixDupImportStmts_IntroLbl")); // NOI18N
    }
    
    public String[] getSelections() {
        String[] res = new String[combos.length];
        for( int i=0; i<combos.length; i++ ) {
            res[i] = combos[i].getSelectedItem().toString();
        }
        return res;
    }
    
    public boolean getRemoveUnusedImports() {
        return checkUnusedImports.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        lblHeader = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(null);
        setLayout(new java.awt.GridBagLayout());

        lblTitle.setText("~Select the fully qualified name to use in the import statement.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(lblTitle, gridBagConstraints);

        jScrollPane1.setBorder(null);

        contentPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(contentPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        bottomPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(bottomPanel, gridBagConstraints);

        lblHeader.setText("~Import Statements:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(lblHeader, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblHeader;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables
    
    private static class DelegatingRenderer implements ListCellRenderer {
        private ListCellRenderer orig;
        private Icon[] icons;
        private String[] values;
        public DelegatingRenderer( ListCellRenderer orig, String[] values, Icon[] icons ) {
            this.orig = orig;
            this.icons = icons;
            this.values = values;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component res = orig.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if( res instanceof JLabel && null != icons ) {
                for( int i=0; i<values.length; i++ ) {
                    if( values[i].equals( value ) ) {
                        ((JLabel)res).setIcon( icons[i] );
                        break;
                    }
                }
            }
            return res;
        }
    }
}