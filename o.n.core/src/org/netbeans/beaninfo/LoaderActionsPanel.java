/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.awt.Component;
import java.beans.*;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.awt.Actions;
import org.openide.cookies.InstanceCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.core.ActionsPoolNode;

/** Custom editor for data loader actions list.
 *
 * @author Jesse Glick
 */
public class LoaderActionsPanel extends javax.swing.JPanel implements EnhancedCustomPropertyEditor, ListCellRenderer {

    private DefaultListModel model;
    private ExplorerManager mgr;

    /** Creates new form LoaderActionsPanel */
    public LoaderActionsPanel (PropertyEditor pe) {
        initComponents ();
        model = new DefaultListModel ();
        SystemAction[] actions = (SystemAction[]) pe.getValue ();
        if (actions == null) actions = new SystemAction[] { };
        for (int i = 0; i < actions.length; i++)
            model.addElement (actions[i]);
        list.setModel (model);
        mgr = explorerPanel.getExplorerManager ();
        mgr.setRootContext (new ActionsPoolNode ());
        mgr.addPropertyChangeListener (new PropertyChangeListener () {
                                           public void propertyChange (PropertyChangeEvent ev) {
                                               if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                   SystemAction action = findAction (mgr.getSelectedNodes ());
                                                   addButton.setEnabled (action != null);
                                               }
                                           }
                                       });
        HelpCtx.setHelpIDString (this, LoaderActionsPanel.class.getName ());
        // Form Editor does not permit you to set this, because
        // it has both int and double params:
        splitPane.setDividerLocation (300);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        splitPane = new javax.swing.JSplitPane ();
        explorerPanel = new org.openide.explorer.ExplorerPanel ();
        beanTreeView2 = new org.openide.explorer.view.BeanTreeView ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        list = new javax.swing.JList ();
        jPanel2 = new javax.swing.JPanel ();
        jPanel1 = new javax.swing.JPanel ();
        addButton = new javax.swing.JButton ();
        separatorButton = new javax.swing.JButton ();
        removeButton = new javax.swing.JButton ();
        upButton = new javax.swing.JButton ();
        downButton = new javax.swing.JButton ();
        setLayout (new java.awt.BorderLayout ());
        setPreferredSize (new java.awt.Dimension(600, 500));

        splitPane.setDividerSize (5);
        splitPane.setLastDividerLocation (300);


        beanTreeView2.setPopupAllowed (false);
        beanTreeView2.setDefaultActionAllowed (false);

        explorerPanel.add (beanTreeView2, java.awt.BorderLayout.CENTER);

        splitPane.setLeftComponent (explorerPanel);


        list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer (this);
        list.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
                                           public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
                                               listValueChanged (evt);
                                           }
                                       }
                                      );

        jScrollPane1.setViewportView (list);

        splitPane.setRightComponent (jScrollPane1);


        add (splitPane, java.awt.BorderLayout.CENTER);


        jPanel1.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        jPanel1.setBorder (new javax.swing.border.EtchedBorder());

        addButton.setToolTipText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton1.toolTipText"));
        addButton.setText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton1.text"));
        addButton.setEnabled (false);
        addButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             addButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add (addButton, gridBagConstraints1);

        separatorButton.setToolTipText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton2.toolTipText"));
        separatorButton.setText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton2.text"));
        separatorButton.addActionListener (new java.awt.event.ActionListener () {
                                               public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                   separatorButtonActionPerformed (evt);
                                               }
                                           }
                                          );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add (separatorButton, gridBagConstraints1);

        removeButton.setToolTipText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton3.toolTipText"));
        removeButton.setText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton3.text"));
        removeButton.setEnabled (false);
        removeButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                removeButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add (removeButton, gridBagConstraints1);

        upButton.setToolTipText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton4.toolTipText"));
        upButton.setText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton4.text"));
        upButton.setEnabled (false);
        upButton.addActionListener (new java.awt.event.ActionListener () {
                                        public void actionPerformed (java.awt.event.ActionEvent evt) {
                                            upButtonActionPerformed (evt);
                                        }
                                    }
                                   );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add (upButton, gridBagConstraints1);

        downButton.setToolTipText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton5.toolTipText"));
        downButton.setText (org.openide.util.NbBundle.getBundle(LoaderActionsPanel.class).getString("LoaderActionsPanel.jButton5.text"));
        downButton.setEnabled (false);
        downButton.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              downButtonActionPerformed (evt);
                                          }
                                      }
                                     );

        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add (downButton, gridBagConstraints1);

        jPanel2.add (jPanel1);


        add (jPanel2, java.awt.BorderLayout.EAST);

    }//GEN-END:initComponents

    private void listValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listValueChanged
        int index = list.getSelectedIndex ();
        if (index == -1) {
            downButton.setEnabled (false);
            upButton.setEnabled (false);
            removeButton.setEnabled (false);
        } else {
            // [PENDING] remove button enabled after removing last action
            // but it is harmless to press it
            removeButton.setEnabled (true);
            downButton.setEnabled (index != model.getSize () - 1);
            upButton.setEnabled (index != 0);
        }
    }//GEN-LAST:event_listValueChanged

    private void downButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int index = list.getSelectedIndex ();
        if (index == -1 || index == model.getSize () - 1) return;
        Object temp = model.elementAt (index);
        model.setElementAt (model.elementAt (index + 1), index);
        model.setElementAt (temp, index + 1);
        list.setSelectedIndex (index + 1);
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int index = list.getSelectedIndex ();
        if (index == -1 || index == 0) return;
        Object temp = model.elementAt (index);
        model.setElementAt (model.elementAt (index - 1), index);
        model.setElementAt (temp, index - 1);
        list.setSelectedIndex (index - 1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void removeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = list.getSelectedIndex ();
        if (index == -1) return;
        model.remove (index);
        if (model.getSize () == 0)
            list.setSelectedIndices (new int[] { });
        else
            list.setSelectedIndex (Math.min (index, model.getSize () - 1));
    }//GEN-LAST:event_removeButtonActionPerformed

    private void separatorButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separatorButtonActionPerformed
        model.addElement (null);
        list.setSelectedIndex (model.getSize () - 1);
    }//GEN-LAST:event_separatorButtonActionPerformed

    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Node[] nodes = mgr.getSelectedNodes ();
        SystemAction action = findAction (nodes);
        if (action != null) model.addElement (action);
        list.setSelectedIndex (model.getSize () - 1);
    }//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private org.openide.explorer.ExplorerPanel explorerPanel;
    private org.openide.explorer.view.BeanTreeView beanTreeView2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList list;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton addButton;
    private javax.swing.JButton separatorButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    private javax.swing.JButton downButton;
    // End of variables declaration//GEN-END:variables

    /** Try to find a system action instance among a set of nodes. */
    private static SystemAction findAction (Node[] nodes) {
        if (nodes == null || nodes.length == 0 || nodes.length > 1) return null;
        InstanceCookie inst = (InstanceCookie) nodes[0].getCookie (InstanceCookie.class);
        if (inst == null) return null;
        try {
            Class clazz = inst.instanceClass ();
            if (! SystemAction.class.isAssignableFrom (clazz)) return null;
            return (SystemAction) inst.instanceCreate ();
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                e.printStackTrace ();
            return null;
        }
    }

    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        SystemAction[] actions = new SystemAction[model.getSize ()];
        model.copyInto (actions);
        return actions;
    }

    /** Return a component that has been configured to display the specified
     * value. That component's <code>paint</code> method is then called to
     * "render" the cell.  If it is necessary to compute the dimensions
     * of a list because the list cells do not have a fixed size, this method
     * is called to generate a component on which <code>getPreferredSize</code>
     * can be invoked.
     *
     * @param list The JList we're painting.
     * @param value The value returned by list.getModel().getElementAt(index).
     * @param index The cells index.
     * @param isSelected True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return A component whose paint() method will render the specified value.
     *
     * @see JList
     * @see ListSelectionModel
     * @see ListModel
     */
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        JLabel label = new JLabel ();
        if (value != null) {
            SystemAction action = (SystemAction) value;
            try {
                String name = action.getName ();
                if (name == null) name = NbBundle.getBundle(LoaderActionsPanel.class).getString("LBL_no_system_action_name");
                label.setText (Actions.cutAmpersand (name));
                Icon icon = action.getIcon ();
                if (icon != null) label.setIcon (icon);
            } catch (RuntimeException re) {
                // May happen if actions are misconfigured, bogus icons, etc.
                // So best to recover semigracefully.
                TopManager.getDefault ().notifyException (re);
            }
        } else {
            label.setText (NbBundle.getBundle(LoaderActionsPanel.class).getString("LBL_separator_rather_than_action"));
            // For alignment:
            label.setIcon (new ImageIcon (SystemAction.class.getClassLoader ().getResource
                                          ("org/openide/resources/actions/empty.gif"))); // NOI18N
        }
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        label.setEnabled(list.isEnabled());
        label.setFont(list.getFont());
        label.setOpaque (true);
        return label;
    }
}
