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

package org.netbeans.modules.palette.ui;

import java.awt.Dialog;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.beans.*;
import org.netbeans.spi.palette.PaletteActions;
import org.openide.awt.Mnemonics;

import org.openide.util.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.*;
import org.openide.nodes.*;
import org.netbeans.modules.palette.*;


/**
 * This class provides the UI for managing palette content (adding components
 * etc). Shown to the user as "Palette Customizer" window.
 *
 * @author Tomas Pavek, S. Aubrecht
 */

public class Customizer extends JPanel implements ExplorerManager.Provider,
                                                      Lookup.Provider
{
    private ExplorerManager explorerManager;
    private Lookup lookup;
    
    private Node root;
    private Settings settings;
    
    private JButton[] customButtons;

    // ------------

    /**
     * Opens the manager window.
     *
     * @param paletteRoot Palette root node.
     */
    public static void show( Node paletteRoot, Settings settings ) {
        JButton closeButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(
            closeButton, Utils.getBundleString("CTL_Close_Button")); // NOI18N
        closeButton.getAccessibleContext().setAccessibleDescription( Utils.getBundleString("ACSD_Close") );
        DialogDescriptor dd = new DialogDescriptor(
            new Customizer( paletteRoot, settings ),
            Utils.getBundleString("CTL_Customizer_Title"), // NOI18N
            false,
            new Object[] { closeButton },
            closeButton,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }

    /** Creates new Customizer */
    public Customizer( Node paletteRoot, Settings settings ) {
        this.root = paletteRoot;
        this.settings = settings;
        explorerManager = new ExplorerManager();

        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, true)); // NOI18N

        lookup = ExplorerUtils.createLookup(explorerManager, map);

        explorerManager.setRootContext(paletteRoot);

        initComponents();

        CheckTreeView treeView = new CheckTreeView( settings );
        treeView.getAccessibleContext().setAccessibleName(
            Utils.getBundleString("ACSN_PaletteContentsTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            Utils.getBundleString("ACSD_PaletteContentsTree")); // NOI18N
        infoLabel.setLabelFor( treeView );
        treePanel.add(treeView, java.awt.BorderLayout.CENTER);
        captionLabel.setLabelFor(treeView);

        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName()))
                    updateInfoLabel(explorerManager.getSelectedNodes());
            }
        });
    }

    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(explorerManager, true);
    }

    public void removeNotify() {
        ExplorerUtils.activateActions(explorerManager, false);
        super.removeNotify();
    }

    // ExplorerManager.Provider
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Lookup.Provider from TopComponent
    public Lookup getLookup() {
        return lookup;
    }

    // -------

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        captionLabel = new javax.swing.JLabel();
        treePanel = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        newCategoryButton = new javax.swing.JButton();
        customActionsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_PaletteCustomizer"));
        org.openide.awt.Mnemonics.setLocalizedText(captionLabel, Utils.getBundleString("CTL_Caption"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        add(captionLabel, gridBagConstraints);

        treePanel.setLayout(new java.awt.BorderLayout());

        treePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        treePanel.setPreferredSize(new java.awt.Dimension(288, 336));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(treePanel, gridBagConstraints);

        infoLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(infoLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, Utils.getBundleString("CTL_MoveUp_Button"));
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(28, 12, 0, 10);
        add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_MoveUp"));

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, Utils.getBundleString("CTL_MoveDown_Button"));
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(moveDownButton, gridBagConstraints);
        moveDownButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_MoveDown"));

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, Utils.getBundleString("CTL_Remove_Button"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_Remove"));

        org.openide.awt.Mnemonics.setLocalizedText(newCategoryButton, Utils.getBundleString("CTL_NewCategory_Button"));
        newCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCategoryButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
        add(newCategoryButton, gridBagConstraints);
        newCategoryButton.getAccessibleContext().setAccessibleDescription(Utils.getBundleString("ACSD_NewCategory"));

        customActionsPanel.setLayout(new java.awt.GridBagLayout());

        createCustomButtons();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(customActionsPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Node[] selected = explorerManager.getSelectedNodes();
        if (selected.length == 0)
            return;

        if( selected.length == 1 && !selected[0].canDestroy() )
            return;
        
        // first user confirmation...
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
            Utils.getBundleString("MSG_ConfirmPaletteDelete"), // NOI18N
            Utils.getBundleString("CTL_ConfirmDeleteTitle"), // NOI18N
            NotifyDescriptor.YES_NO_OPTION);

        if (NotifyDescriptor.YES_OPTION.equals(
                    DialogDisplayer.getDefault().notify(desc)))
        {
            try {
                for (int i=0; i < selected.length; i++) {
                    if( selected[i].canDestroy() )
                        selected[i].destroy();
                }
            }
            catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        moveNode(false);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        moveNode(true);
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void newCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCategoryButtonActionPerformed
        new Utils.NewCategoryAction( root ).actionPerformed( evt );
    }//GEN-LAST:event_newCategoryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel captionLabel;
    private javax.swing.JPanel customActionsPanel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newCategoryButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables

    private void moveNode(boolean up) {
        final Node[] selected = explorerManager.getSelectedNodes();
        if (selected.length != 1)
            return;

        Node node = selected[0];
        Node parent = node.getParentNode();
        if (parent == null)
            return;

        Index indexCookie = (Index) parent.getCookie(Index.class);
        if (indexCookie == null)
            return;

        int index = movePossible(node, parent, up);
        if (index != -1) {
            if (up)
                indexCookie.moveUp(index);
            else 
                indexCookie.moveDown(index);
        }
    }

    private static int movePossible(Node node, Node parentNode, boolean up) {
        if (parentNode == null)
            return -1;

        Node[] nodes = parentNode.getChildren().getNodes();
        for (int i=0; i < nodes.length; i++)
            if (nodes[i].getName().equals(node.getName()))
                return (up && i > 0) || (!up && i+1 < nodes.length) ? i : -1;

        return -1;
    }

    private void updateInfoLabel(org.openide.nodes.Node[] nodes) {
        String text = " "; // NOI18N
        if (nodes.length == 1) {
            Item item = (Item) nodes[0].getCookie(Item.class);
            if (item != null)
                text = item.getShortDescription(); //TODO revisit PaletteSupport.getItemComponentDescription(item);
        }
        infoLabel.setText(text);
    }
    
    private void createCustomButtons() {
        PaletteActions customActions = (PaletteActions)root.getLookup().lookup( PaletteActions.class );
        if( null == customActions )
            return;
        
        Action[] actions = customActions.getImportActions();
        if( null == actions || actions.length == 0 )
            return;
        
        customButtons = new JButton[actions.length];
        for( int i=0; i<actions.length; i++ ) {
            customButtons[i] = new JButton( actions[i] );
            if( null != actions[i].getValue( Action.NAME ) )
                Mnemonics.setLocalizedText( customButtons[i], actions[i].getValue( Action.NAME ).toString() );
            if( null != actions[i].getValue( Action.LONG_DESCRIPTION ) )
                customButtons[i].getAccessibleContext().setAccessibleDescription( actions[i].getValue( Action.LONG_DESCRIPTION ).toString() );
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = i;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 10);
            customActionsPanel.add( customButtons[i], gridBagConstraints);
        }
    }
    
    
    private static class CheckTreeView extends BeanTreeView {
        /** Creates a new instance of CheckTreeView */
        public CheckTreeView( Settings settings ) {
            if( settings instanceof DefaultSettings ) {
                CheckListener l = new CheckListener( (DefaultSettings)settings );
                tree.addMouseListener( l );
                tree.addKeyListener( l );

                CheckRenderer check = new CheckRenderer( (DefaultSettings)settings );
                tree.setCellRenderer( check );
            }
        }
    }
}
