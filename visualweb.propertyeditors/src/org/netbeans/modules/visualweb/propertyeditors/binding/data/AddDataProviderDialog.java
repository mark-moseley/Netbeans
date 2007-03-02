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
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author  Winston Prakash
 */
public class AddDataProviderDialog extends JPanel implements PropertyChangeListener{

    private String addString =  NbBundle.getMessage(AddDataProviderDialog.class, "ADD");
    private String cancelString =  NbBundle.getMessage(AddDataProviderDialog.class, "CANCEL");
    private JDialog dialog;
    private DialogDescriptor dlg = null;

    private JButton addButton = new JButton(addString);
    private JButton cancelButton = new JButton(cancelString);

    private static DataProviderExplorerPanel dataProviderExplorerPanel = null;

    boolean result = false;

    TopComponent tc = WindowManager.getDefault().findTopComponent("serverNavigator");

    /** Creates new form AddDataProviderDialog */
    public AddDataProviderDialog() {
        initComponents();
        dataProviderExplorerPanel = new DataProviderExplorerPanel();
        dataProviderExplorerPanel.getExplorerManager().addPropertyChangeListener(this);
        add(dataProviderExplorerPanel, BorderLayout.CENTER);
        // This is a pure hack for not to show the Query view as it screws up the Add Data Provider
        // functionality. We need better way to add data provider, some kind of service offered by Server Navigator.
        System.setProperty("AddDataProviderMode","true");
    }

    public void propertyChange(PropertyChangeEvent evt) {

        Node[] nodes = dataProviderExplorerPanel.getExplorerManager().getSelectedNodes();
        tc.requestActive();
        tc.setActivatedNodes(nodes);
        boolean canAdd = false;
        if((nodes != null) && (nodes.length > 0)){
            Action[] actions = nodes[0].getActions(true);
            for(int i=0; i< actions.length; i++){
                if(actions[i] instanceof NodeAction){
                    final NodeAction nodeAction = (NodeAction)actions[i];
                    if (nodeAction.getName().equals(NbBundle.getMessage(AddDataProviderDialog.class, "Add_to_Form"))){
                        canAdd = true;
                        break;
                    }
                }
            }
        }
        addButton.setEnabled(canAdd);
    }

    /**
     * Show the Add data provider dialog
     */
    public boolean showDialog(){
        // Add a listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //Remove the  AddDataProviderMode property as it is not needed any more.
                System.getProperties().remove("AddDataProviderMode");
                Object o = evt.getSource();

                Object[] option = dlg.getOptions();

                if (o == option[1]) {
                    // cancel button or escape
                } else if (o == option[0]) {
                    // Add the Data Provider
                    Node[] nodes = dataProviderExplorerPanel.getExplorerManager().getSelectedNodes();
                    tc.requestActive();
                    tc.setActivatedNodes(nodes);
                    Action[] actions = nodes[0].getActions(true);
                    for(int i=0; i< actions.length; i++){
                        if(actions[i] instanceof NodeAction){
                            final NodeAction nodeAction = (NodeAction)actions[i];
                            if (nodeAction.getName().equals(NbBundle.getMessage(AddDataProviderDialog.class, "Add_to_Form"))){
                                nodeAction.performAction();
                                result = true;
                                break;
                            }
                        }
                    }
                    dialog.dispose() ;
                }
            }
        };
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddDataProviderDialog.class, "ADD_DATA_PROVIDER"), true, listener);
        dlg.setOptions(new Object[] { addButton, cancelButton });
        dlg.setClosingOptions(new Object[] {cancelButton});
        //dlg.setHelpCtx(new HelpCtx("projrave_ui_elements_server_nav_add_datasourcedb")); // NOI18N
        
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(true);
        dialog.pack();
        addButton.setEnabled(false);
        Point loc = dialog.getLocation();
        dialog.setLocation((int)loc.getX() + 50, (int)loc.getY() + 50);
        dialog.show();
        return result;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(10, 10));

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 10, 10)));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddDataProviderDialog.class, "ADD_DATA_PROVIDER_MESSAGE"));
        add(jLabel1, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
