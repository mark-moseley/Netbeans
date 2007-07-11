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

package org.netbeans.modules.tasklist.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;
import org.netbeans.modules.tasklist.ui.checklist.CheckList;

/**
 *
 * @author  sa154850
 */
final class TypesPanel extends JPanel {
    
    private CheckList lstTypes;
    private List<? extends ScannerDescriptor> providers;
    private boolean[] providerState;
    
    private TypesFilter filter;
    
    public TypesPanel( TypesFilter filter ) {
        this.filter = filter;
        init();
    }
    
    public boolean isValueValid() {
        boolean atLeastOneTypeSelected = false;
        for( int i=0; i<providerState.length; i++ ) {
            if( providerState[i] ) {
                atLeastOneTypeSelected = true;
                break;
            }
        }
        return atLeastOneTypeSelected && checkVisibleLimit();
    }

    private void init() {
        initComponents();
        
        providers = ScannerDescriptor.getDescriptors();
        providerState = new boolean[providers.size()];
        String[] names = new String[providers.size()];
        for( int i=0; i<names.length; i++ ) {
            names[i] = providers.get( i ).getDisplayName();
        }
        lstTypes = new CheckList( providerState, names );
        lstTypes.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        lstTypes.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                int selIndex = lstTypes.getSelectedIndex();
                boolean enableOptions = false;
                if( selIndex >= 0 ) {
                    ScannerDescriptor tp = providers.get( selIndex );
                    enableOptions = null != tp.getOptionsPath();
                }
                btnOptions.setEnabled( enableOptions );
            }
        });
        lstTypes.getModel().addListDataListener( new ListDataListener() {
            public void intervalAdded(ListDataEvent arg0) {
            }
            public void intervalRemoved(ListDataEvent arg0) {
            }

            public void contentsChanged(ListDataEvent arg0) {
                putClientProperty( FilterCondition.PROP_VALUE_VALID, new Boolean( isValueValid() ) );
            }
        });
        
        btnOptions.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                int selIndex = lstTypes.getSelectedIndex();
                if( selIndex >= 0 ) {
                    ScannerDescriptor tp = providers.get( selIndex );
                    if( null != tp.getOptionsPath() ) {
                        //TODO implement navigation into sub-panels in the options window
                        OptionsDisplayer.getDefault().open( tp.getOptionsPath() );
                    }
                }
            }
        });
        scrollTypes.setViewportView( lstTypes );
        
        showFilter( filter );
    }
    
    private void showFilter( TypesFilter filter ) {
        for( int i=0; i<providerState.length; i++ ) {
            ScannerDescriptor tp = providers.get( i );
            providerState[i] = null != filter && filter.isEnabled( tp.getType() );
        }
        txtVisibleLimit.setText( null == filter ? "" : String.valueOf(filter.getTaskCountLimit()) ); //NOI18N
        lstTypes.setEnabled( null != filter );
        txtVisibleLimit.setEnabled( null != filter );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblVisibleLimit = new javax.swing.JLabel();
        txtVisibleLimit = new javax.swing.JTextField();
        btnOptions = new javax.swing.JButton();
        scrollTypes = new javax.swing.JScrollPane();

        setOpaque(false);

        lblVisibleLimit.setLabelFor(txtVisibleLimit);
        org.openide.awt.Mnemonics.setLocalizedText(lblVisibleLimit, org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.lblVisibleLimit.text")); // NOI18N

        txtVisibleLimit.setText(org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.txtVisibleLimit.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnOptions, org.openide.util.NbBundle.getMessage(TypesPanel.class, "TypesPanel.btnOptions.text")); // NOI18N
        btnOptions.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(scrollTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnOptions))
                    .add(layout.createSequentialGroup()
                        .add(lblVisibleLimit)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtVisibleLimit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(btnOptions)
                    .add(layout.createSequentialGroup()
                        .add(scrollTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblVisibleLimit)
                            .add(txtVisibleLimit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOptions;
    private javax.swing.JLabel lblVisibleLimit;
    private javax.swing.JScrollPane scrollTypes;
    private javax.swing.JTextField txtVisibleLimit;
    // End of variables declaration//GEN-END:variables
    
    public TypesFilter getFilter() {
        if (filter != null) {
            for( int i=0; i<providerState.length; i++ ) {
                ScannerDescriptor tp = providers.get( i );
                filter.setEnabled( tp.getType(),  providerState[i] );
            }
            filter.setTaskCountLimit( getVisibleLimit() );
        }
        return filter;
    }

    private int getVisibleLimit() {
        int limit = null == filter ? 100 : filter.getTaskCountLimit();
        try {
            String strLimit = txtVisibleLimit.getText();
            int tmp = Integer.parseInt( strLimit );
            if( tmp > 0 )
                limit = tmp;
        } catch( NumberFormatException nfE ) {
            //ignore
        }
        return limit;
    }
    
    private boolean checkVisibleLimit() {
        try {
            String strLimit = txtVisibleLimit.getText();
            int limit = Integer.parseInt( strLimit );
            return limit > 0;
        } catch( NumberFormatException nfE ) {
            return false;
        }
    }
}
