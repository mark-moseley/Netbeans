/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Dialog;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
 * Component for visual editing of Map of macroiations. When you enter new
 * macroiation with the already used macro, it will replace the existing one.
 * macroiations with empty expanded form are perfectly valid, but macroiations
 * with empty macro field are simply ignored.
 *
 * @author  David Konecny
 */

public class MacrosEditorPanel extends javax.swing.JPanel {

    PairStringModel model;

    // The master we talk to about changes in map
    private MacrosEditor editor;

    /** Creates new form MacrosEditorPanel */
    public MacrosEditorPanel(MacrosEditor editor) {
        this.editor = editor;
        model = new PairStringModel();
        initComponents ();
        getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP")); // NOI18N
        macrosTable.getAccessibleContext().setAccessibleName(getBundleString("ACSN_MEP_Table")); // NOI18N
        macrosTable.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Table")); // NOI18N
        addButton.setMnemonic(getBundleString("MEP_Add_Mnemonic").charAt(0)); // NOI18N
        editButton.setMnemonic(getBundleString("MEP_Edit_Mnemonic").charAt(0)); // NOI18N
        removeButton.setMnemonic(getBundleString("MEP_Remove_Mnemonic").charAt(0)); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Add")); // NOI18N
        editButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Edit")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_MEP_Remove")); // NOI18N
    }

    private String getBundleString(String s) {
        return NbBundle.getMessage(MacrosEditorPanel.class, s);
    }        
    
    /**
     * Fill in editor with initial values
     */
    public void setValue( Map m ) {
        HashMap hm;
        if (m != null)
            hm = new HashMap(m);
        else
            hm = new HashMap();
        if (hm.containsKey(null)) {
            hm.remove(null);
        }
        // Our model is the one and only holding data
        model.setData( new TreeMap( hm ) );
        // select first item, just to have something selected
        if( model.getRowCount() > 0 ) macrosTable.setRowSelectionInterval( 0, 0 );
    }

    /**
     * Take the result of users modifications
     */
    public Map getValue() {
        return model.getData();
    }

    /**
     * Tell the editor (and in round the system), that user've changed
     * macros mapping.
     */
    private void notifyEditor() {
        if( editor != null ) editor.customEditorChange();
    }


    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        macrosPane = new javax.swing.JScrollPane();
        macrosTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
        macrosTable.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        macrosTable.setModel(model);
        macrosTable.setShowVerticalLines(false);
        macrosTable.setShowHorizontalLines(false);
        macrosTable.setSelectionMode( DefaultListSelectionModel.SINGLE_SELECTION );
        // Set the width of columns to 30% and 70%
        TableColumnModel col = macrosTable.getColumnModel();
        col.getColumn( 0 ).setMaxWidth( 3000 );
        col.getColumn( 0 ).setPreferredWidth( 30 );
        col.getColumn( 1 ).setMaxWidth( 7000 );
        col.getColumn( 1 ).setPreferredWidth( 70 );
        macrosPane.setViewportView(macrosTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(macrosPane, gridBagConstraints);

        addButton.setText(getBundleString( "MEP_Add" ));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(addButton, gridBagConstraints);

        editButton.setText(getBundleString( "MEP_Edit" ));
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(editButton, gridBagConstraints);

        removeButton.setText(getBundleString( "MEP_Remove" ));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(removeButton, gridBagConstraints);

    }//GEN-END:initComponents

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String[] macro = getMacro( null );
        // If user canceled entering, do noting
        if( macro == null ) return;
        int index = model.putPair( macro );  // can silently replace existing mapping
        macrosTable.setRowSelectionInterval( index, index );
        notifyEditor();
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        int index = macrosTable.getSelectedRow();
        if( index != -1 ) {  // is something selected?
            String[] pair = model.getPair( index );
            pair = getMacro( pair );
            if( pair != null ) {
                model.removePair( index );
                index = model.putPair( pair );
                macrosTable.setRowSelectionInterval( index, index );
                notifyEditor();
            }
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = macrosTable.getSelectedRow();
        if( index != -1 ) { // is something selected?
            model.removePair( index );
            if( index >= model.getRowCount() ) index--;
            if( index >= 0 ) macrosTable.setRowSelectionInterval( index, index );
            notifyEditor();
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    /**
     * Creates a dialog asking user for pair of Strings.
     * @param macro value to be preset in dialog, or <CODE>null</CODE>
     * @return String[2] filled with {macro, expand}
     * or <CODE>null</CODE> if canceled.
     */
    private String[] getMacro( String[] macro ) {
        MacroInputPanel input = new MacroInputPanel();
        // set HELP_ID of parent 
        HelpCtx.setHelpIDString( input, (HelpCtx.findHelp(this) != null ? HelpCtx.findHelp(this).getHelpID() : null) );
        if( macro != null ) input.setMacro( macro ); // preset value

        DialogDescriptor dd = new DialogDescriptor ( input, getBundleString( "MEP_EnterMacro" ) ); // NOI18N
        Dialog dial = org.openide.DialogDisplayer.getDefault().createDialog(dd);
        input.requestFocus();  // Place caret in it, hopefully
        dial.show(); // let the user tell us their wish

        if( dd.getValue() == DialogDescriptor.OK_OPTION ) {
            String[] retVal = input.getMacro();
            if( ! "".equals( retVal[0] )  ) return retVal;  // NOI18N don't allow empty macro
        }
        return null; // cancel or empty
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTable macrosTable;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane macrosPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    /**
     * TableModel of sorted map of string pairs, provides additional functions
     * for setting, getting and modifying it's content.
     */
    private class PairStringModel extends javax.swing.table.AbstractTableModel {

        String[] columns = { getBundleString( "MEP_MacroTitle" ),     // NOI18N
                             getBundleString( "MEP_ExpandTitle" ) };   // NOI18N

        TreeMap data;
        String[] keys;

        public PairStringModel() {
            data = new TreeMap();
            keys = new String[0];
        }

        public void setData( TreeMap data ) {
            this.data = data;
            updateKeys();
        }

        private void updateKeys() {
            keys = (String[])data.keySet().toArray( new String[0] );
            fireTableDataChanged(); // we make general changes to table, invalidate whole
        }

        public TreeMap getData() {
            return data;
        }

        public int getRowCount() {
            return keys.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            if( column == 0 ) return keys[row];
            else return data.get( keys[row] );
        }

        public int putPair( String[] pair ) {
            data.put( pair[0], pair[1] );
            updateKeys();
            return Arrays.binarySearch( keys, pair[0] );  // it should always find
        }

        public void removePair( int row ) {
            data.remove( getValueAt( row, 0 ) );
            updateKeys();
        }

        public String[] getPair( int row ) {
            String key = (String)getValueAt( row, 0 );
            String[] retVal = { key, (String)data.get( key ) };
            return retVal;
        }
    }

}
