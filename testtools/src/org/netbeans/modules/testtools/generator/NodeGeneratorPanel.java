/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.generator;

import java.util.StringTokenizer;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/*
 * ComponentGeneratorPanel.java
 *
 * Created on February 7, 2002, 10:34 AM
 */


/** Component Generator panel
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 0.2
 */
public class NodeGeneratorPanel extends javax.swing.JPanel implements java.beans.PropertyChangeListener, java.beans.VetoableChangeListener, org.openide.loaders.DataFilter {

    /** root node */
    private Node rootNode;
    private static java.awt.Dialog dialog;
    private static NodeGeneratorPanel panel;
    private String directory;
    private Thread thread;
    private java.util.Properties props;
    
    /** creates ans shows Component Generator dialog
     */    
    public static void showDialog(Node[] nodes){
        if (dialog==null) {
            panel = new NodeGeneratorPanel(nodes);
            dialog = org.openide.TopManager.getDefault().createDialog(new org.openide.DialogDescriptor(panel, "Jelly Node Generator", false, new Object[0], null, org.openide.DialogDescriptor.BOTTOM_ALIGN, null, null)); // NOI18N
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    panel.closeButtonActionPerformed(null);
                }
            });
        }
        panel.setSelectedNodes(nodes);
        dialog.show();
    }
    
    /** Creates new ComponentGeneratorPanel
     */
    public NodeGeneratorPanel(Node[] nodes) {
        initComponents();
        rootNode = createPackagesNode();
        packagesPanel.getExplorerManager().setRootContext(rootNode);
        packagesPanel.getExplorerManager().addVetoableChangeListener(this);
        packagesPanel.getExplorerManager().addPropertyChangeListener(this);
    }
    
    private void setSelectedNodes(Node[] nodes) {
        DataFolder df;
        if (packagesTreeView.isEnabled() && nodes!=null && nodes.length>0 && (df=(DataFolder)nodes[0].getCookie(DataFolder.class))!=null) {
            try {
                StringTokenizer packageName = new StringTokenizer(df.getPrimaryFile().getPackageName('.'), "."); // NOI18N
                Node node = packagesPanel.getExplorerManager().getRootContext().getChildren().findChild(df.getPrimaryFile().getFileSystem().getSystemName());
                while (packageName.hasMoreTokens()) {
                    node = node.getChildren().findChild(packageName.nextToken());
                }
                packagesPanel.getExplorerManager().setSelectedNodes (new Node[]{node});
            }
            catch(Exception e) {}
        }
    }
    
    /** Creates node that displays all packages.
    */
    private Node createPackagesNode () {
        Node orig = org.openide.TopManager.getDefault().getPlaces().nodes ().repository(this);
        return orig;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        packagesPanel = new org.openide.explorer.ExplorerPanel();
        packagesTreeView = new org.openide.explorer.view.BeanTreeView();
        selectLabel = new javax.swing.JLabel();
        helpLabel = new javax.swing.JLabel();
        helpLabel.setVisible(false);
        stopButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        stopButton.setVisible(false);
        closeButton = new javax.swing.JButton();
        nodeField = new javax.swing.JTextField();
        actionField = new javax.swing.JTextField();
        nodeLabel = new javax.swing.JLabel();
        actionLabel = new javax.swing.JLabel();
        inlineCheck = new javax.swing.JCheckBox();
        noBlockCheck = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setPreferredSize(new java.awt.Dimension(420, 300));
        packagesPanel.setName("");
        packagesTreeView.setPopupAllowed(false);
        packagesTreeView.setAutoscrolls(true);
        packagesPanel.add(packagesTreeView, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 10.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(packagesPanel, gridBagConstraints);

        selectLabel.setLabelFor(packagesTreeView);
        selectLabel.setText("Select Destination Filesystem:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(selectLabel, gridBagConstraints);

        helpLabel.setFont(new java.awt.Font("Dialog", 2, 12));
        helpLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        helpLabel.setText("Use Ctrl-F11 key to grab popup menu content.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(helpLabel, gridBagConstraints);
        helpLabel.getAccessibleContext().setAccessibleDescription("N/A");

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(stopButton, gridBagConstraints);

        startButton.setText("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(startButton, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 5, 12, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(closeButton, gridBagConstraints);

        nodeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nodeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 12);
        add(nodeField, gridBagConstraints);

        actionField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                actionFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 12);
        add(actionField, gridBagConstraints);

        nodeLabel.setText("Nodes Package: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(nodeLabel, gridBagConstraints);

        actionLabel.setText("Actions Package: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(actionLabel, gridBagConstraints);

        inlineCheck.setText("Default Inline");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(inlineCheck, gridBagConstraints);

        noBlockCheck.setText("Default NoBlock");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 12);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(noBlockCheck, gridBagConstraints);

    }//GEN-END:initComponents

    private void actionFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_actionFieldFocusGained
        actionField.selectAll();
    }//GEN-LAST:event_actionFieldFocusGained

    private void nodeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nodeFieldFocusGained
        nodeField.selectAll();
    }//GEN-LAST:event_nodeFieldFocusGained

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        stopButtonActionPerformed(evt);
        dialog.dispose();
        dialog=null;
    }//GEN-LAST:event_closeButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if (thread!=null) {
            thread.interrupt();
            thread=null;
        }
        stopButton.setVisible(false);
        helpLabel.setVisible(false);
        packagesTreeView.setEnabled(true);
        startButton.setVisible(true);
        nodeLabel.setEnabled(true);
        nodeField.setEnabled(true);
        actionLabel.setEnabled(true);
        actionField.setEnabled(true);
        inlineCheck.setEnabled(true);
        noBlockCheck.setEnabled(true);
    }//GEN-LAST:event_stopButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        packagesTreeView.setEnabled(false);
        startButton.setVisible(false);
        stopButton.setVisible(true);
        nodeLabel.setEnabled(false);
        nodeField.setEnabled(false);
        actionLabel.setEnabled(false);
        actionField.setEnabled(false);
        helpLabel.setVisible(true);
        inlineCheck.setEnabled(false);
        noBlockCheck.setEnabled(false);
        if (thread!=null) {
            thread.interrupt();
        }
        helpLabel.setText("Use Ctrl-F11 key to grab popup menu content."); // NOI18N
        thread = new Thread(new NodeGeneratorRunnable(directory, nodeField.getText(), actionField.getText(), inlineCheck.isSelected(), noBlockCheck.isSelected(), this));
        thread.start();
    }//GEN-LAST:event_startButtonActionPerformed

    //
    // Filter to accept only folders
    //

    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject(org.openide.loaders.DataObject obj) {
        Object o = obj.getCookie(org.openide.loaders.DataFolder.class);
        if (o == null) {
            return false;
        }
        return true;
    }

    /** Allow only simple selection.
     * @param ev PropertyChangeEvent
     * @throws PropertyVetoException PropertyVetoException
     */
    public void vetoableChange(java.beans.PropertyChangeEvent ev) throws java.beans.PropertyVetoException {
        if (org.openide.explorer.ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node n[] = (Node[])ev.getNewValue();
            if (n.length > 1 ) {
                throw new java.beans.PropertyVetoException ("Only single selection allowed", ev); // NOI18N
            } 
        }
    }
    
    /** Changes in selected node in packages.
     * @param ev PropertyChangeEvent
     */
    public void propertyChange(java.beans.PropertyChangeEvent ev) {
        if (org.openide.explorer.ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            startButton.setEnabled(false);
            Node[] arr = packagesPanel.getExplorerManager ().getSelectedNodes ();
            if (arr.length == 1) {
                org.openide.loaders.DataFolder df = (org.openide.loaders.DataFolder)arr[0].getCookie (org.openide.loaders.DataFolder.class);
                try {
                    if ((df != null) && (!df.getPrimaryFile().getFileSystem().isReadOnly())) {
                        startButton.setEnabled(true);
                        String packageName = df.getPrimaryFile().getPackageName('.');
                        if (packageName.endsWith(".actions")) {
                            actionField.setText(packageName);
                            nodeField.setText(packageName.substring(0, packageName.length()-8)+".nodes");
                        } else if (packageName.endsWith(".nodes")) {
                            nodeField.setText(packageName);
                            actionField.setText(packageName.substring(0, packageName.length()-6)+".actions");
                        } else if (packageName.length()>0) {
                            nodeField.setText(packageName+".nodes");
                            actionField.setText(packageName+".actions");
                        } else {
                            nodeField.setText("nodes");
                            actionField.setText("actions");
                        }                            
                        directory = org.openide.filesystems.FileUtil.toFile(df.getPrimaryFile().getFileSystem().getRoot()).getAbsolutePath();
                    }
                } catch (org.openide.filesystems.FileStateInvalidException e) {}
            }
        }
    }
    
    /** returns JLabel used as status line
     * @return JLabel used as status line
     */    
    public javax.swing.JLabel getHelpLabel() {
        return helpLabel;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton startButton;
    private javax.swing.JLabel helpLabel;
    private org.openide.explorer.view.BeanTreeView packagesTreeView;
    private javax.swing.JCheckBox inlineCheck;
    private javax.swing.JTextField actionField;
    private javax.swing.JLabel actionLabel;
    private javax.swing.JCheckBox noBlockCheck;
    private javax.swing.JTextField nodeField;
    private javax.swing.JLabel selectLabel;
    private org.openide.explorer.ExplorerPanel packagesPanel;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel nodeLabel;
    private javax.swing.JButton closeButton;
    // End of variables declaration//GEN-END:variables

    /** creates Component Generator dialog for debugging purposes
     * @param args command line arguments
     */    
    public static void main(String args[]) {
        showDialog(null);
    }
    
}
