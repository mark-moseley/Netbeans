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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.SourceGroup;
import org.netbeans.spi.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

// XXX I18N

/**
 *
 * @author  phrebejk
 */
public class JavaTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
  
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    
    private Project project;
    private String expectedExtension;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    /** Creates new form SimpleTargetChooserGUI */
    public JavaTargetChooserPanelGUI() {
        initComponents();
        //initValues( project, null, null );
        documentNameTextField.getDocument().addDocumentListener( this );
        
        rootComboBox.setRenderer( CELL_RENDERER );
        packageComboBox.setRenderer( CELL_RENDERER );
                
        rootComboBox.addActionListener( this );
        packageComboBox.addActionListener( this );
        setName( "Name & Location");
    }
    
    public void initValues( Project p, FileObject template, String preselectedFolder ) {
        this.project = p;
        // Show name of the project
        projectTextField.setText( ProjectUtils.getInformation(p).getDisplayName() );
        // Setup comboboxes 
        rootComboBox.setModel( new DefaultComboBoxModel( getSourceFolderNodes( p ) ) );
        updatePackages();        
        // Determine the extension
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
    }
        
    public String getTargetFolder() {
        FileObject fo = getFolder();
        
        if ( fo == null ) {
            return null;
        }
        else {
            // XXX have to account for FU.tF returning null
            File folder = FileUtil.toFile( fo );
            return folder.getPath();
        }
    }
    
    public String getTargetName() {
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }

    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Java Class Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

        jLabel5.setText("Project");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);

        jLabel1.setText("Package Root:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        gridBagConstraints.weightx = 1.0;
        add(rootComboBox, gridBagConstraints);

        jLabel2.setText("Package:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        gridBagConstraints.weightx = 1.0;
        add(packageComboBox, gridBagConstraints);

        jLabel4.setText("Created File:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        gridBagConstraints.weightx = 1.0;
        add(fileTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanel3, gridBagConstraints);

    }//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( rootComboBox == e.getSource() ) {            
            updatePackages();
            updateText();
        }
        else if ( packageComboBox == e.getSource() ) {
            updateText();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {                
        updateText();
        fireChange();        
    }    
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    // Private methods ---------------------------------------------------------
    
    private Node[] getSourceFolderNodes( Project p ) {
        // XXX Prety nice hack
        LogicalViewProvider lvp = (LogicalViewProvider)p.getLookup().lookup( LogicalViewProvider.class );
        Node root = lvp.createLogicalView();
        return root.getChildren().getNodes();
    }
    
    private Node[] getCurrentPackageNodes() {
        Node packageRoot = (Node)rootComboBox.getSelectedItem();
        return packageRoot.getChildren().getNodes();
    }

    private void updatePackages() {
        packageComboBox.setModel( new DefaultComboBoxModel( getCurrentPackageNodes() ) );        
    }
    
    private FileObject getFolder() {
        Node node = (Node)packageComboBox.getSelectedItem();
        DataObject dobj = (DataObject)node.getLookup().lookup( DataObject.class );
        return dobj.getPrimaryFile();
    }
    
    private void updateText() {
        File projdirFile = FileUtil.toFile(project.getProjectDirectory());
        if (projdirFile != null) {
            String documentName = documentNameTextField.getText().trim();
            if (documentName.length() == 0) {
                fileTextField.setText(""); // NOI18N
            } else {
                FileObject folder = getFolder();
                File newFile = new File( FileUtil.toFile( folder ), documentName + expectedExtension);
                fileTextField.setText(newFile.getAbsolutePath());
            }
        } else {
            // Not on disk.
            fileTextField.setText(""); // NOI18N
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class NodeCellRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer implements ListCellRenderer {
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            javax.swing.plaf.basic.BasicComboBoxRenderer cbr = (javax.swing.plaf.basic.BasicComboBoxRenderer)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );   
            
            if ( value != null ) {
                Node node = (Node)value;
                cbr.setText(node.getDisplayName());
                cbr.setIcon( new ImageIcon( node.getIcon( java.beans.BeanInfo.ICON_COLOR_16x16 ) ) );
            }
            return cbr;
        }
        
    }
    
}
