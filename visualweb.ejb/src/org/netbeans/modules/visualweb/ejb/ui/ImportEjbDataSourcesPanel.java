/*
 * ExportDataSourcesDialog.java
 *
 * Created on March 8, 2004, 12:09 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * A panle to allow the user to export EJB datasources to a jar file
 *
 * @author dongmei cao
 */
public class ImportEjbDataSourcesPanel extends JPanel{
    
    private EjbDataSourcesSelectionPanel ejbDataSourceSelectionPanel;
    private EjbDataSourcePropertiesPanel propsPanel;
    private PortableEjbDataSource[] ejbDataSources;
    
    public ImportEjbDataSourcesPanel()
    {
        initComponents();
        
        propsPanel = new EjbDataSourcePropertiesPanel();
        ejbDataSourceSelectionPanel = new EjbDataSourcesSelectionPanel( propsPanel );
        
        selectionPanel.add( ejbDataSourceSelectionPanel, BorderLayout.CENTER );
        propertiesPanel.add( propsPanel, BorderLayout.CENTER );
    }
    
    public ImportEjbDataSourcesPanel(PortableEjbDataSource[] ejbDataSources)
    {
        initComponents();
        
        propsPanel = new EjbDataSourcePropertiesPanel();
        ejbDataSourceSelectionPanel = new EjbDataSourcesSelectionPanel( propsPanel );
        
        selectionPanel.add( ejbDataSourceSelectionPanel, BorderLayout.CENTER );
        propertiesPanel.add( propsPanel, BorderLayout.CENTER );
    }
    
    public void setImportFilePath( String filePath )
    {
        fileNameTextField.setText( filePath );
    }
    
    public String getImportFilePath()
    {
        return fileNameTextField.getText().trim();
    }
    
    public void setEjbDataSources( PortableEjbDataSource[] ejbDataSources )
    {
        this.ejbDataSources = ejbDataSources;
        ejbDataSourceSelectionPanel.setEjbDataSources( ejbDataSources );
    }
    
    public PortableEjbDataSource[] getEjbDataSources()
    {
        return this.ejbDataSources;
    }
    
    public boolean saveChange()
    {
        return propsPanel.saveChange();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        title = new javax.swing.JLabel();
        selectionPanel = new javax.swing.JPanel();
        filePanel = new javax.swing.JPanel();
        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        propertiesPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "IMPORT_EJB_DATASOURCES"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "IMPORT_EJB_DATASOURCES"));
        title.setLabelFor(selectionPanel);
        title.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("IMPORT_EJB_DATASOURCES_LABEL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(title, gridBagConstraints);
        title.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("IMPORT_EJB_DATASOURCES_LABEL"));
        title.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("IMPORT_EJB_DATASOURCES"));

        selectionPanel.setLayout(new java.awt.BorderLayout());

        selectionPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 10, 10, 10)));
        selectionPanel.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(selectionPanel, gridBagConstraints);

        filePanel.setLayout(new java.awt.BorderLayout(5, 1));

        filePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 10, 10, 10)));
        fileNameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "FILE_NAME_MNEMONIC").charAt(0));
        fileNameLabel.setLabelFor(fileNameTextField);
        fileNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("FILE_NAME"));
        filePanel.add(fileNameLabel, java.awt.BorderLayout.WEST);
        fileNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("IMPORT_FILE_NAME_DESC"));

        fileNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNameTextFieldActionPerformed(evt);
            }
        });

        filePanel.add(fileNameTextField, java.awt.BorderLayout.CENTER);
        fileNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("IMPORT_FILE_NAME_DESC"));

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("BROWSE_IMPORT_FILE_BUTTON_MNEMONIC").charAt(0));
        browseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("BROWSE_IMPORT_FILE_BUTTON_LABEL"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        filePanel.add(browseButton, java.awt.BorderLayout.EAST);
        browseButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("BROWSE_IMPORT_FILE_BUTTON_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 0, 0);
        add(filePanel, gridBagConstraints);

        propertiesPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 17, 0, 12);
        add(propertiesPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void fileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNameTextFieldActionPerformed
        if( !(new File(getImportFilePath())).exists() ) {
            String msg = NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "IMPORT_FILE_NOT_FOUND", getImportFilePath() );
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        else {
            // This file will be the default file the file chooser
            ImportExportFileChooser.setCurrentFilePath( getImportFilePath() );
            
            // start a new thread to read in the data
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    PortableEjbDataSource[] ejbDataSources = ImportEjbDataSourcesHelper.readDataSourceImports( getImportFilePath() );
                    if( ejbDataSources != null )
                        setEjbDataSources( ejbDataSources );
                    else
                        return;
                }
            });
        }
    }//GEN-LAST:event_fileNameTextFieldActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        
        // Chooser a file to import from
        
        ImportExportFileChooser fileChooser = new ImportExportFileChooser( this );
        String selectedFile = fileChooser.getImportFile();
        
        if( selectedFile != null )
        {
            if( !(new File(selectedFile)).exists() ) 
            {
                String msg = NbBundle.getMessage(ImportEjbDataSourcesPanel.class, "IMPORT_FILE_NOT_FOUND", selectedFile );
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            fileChooser.setCurrentFilePath( selectedFile );
            fileNameTextField.setText(selectedFile);
            
            // No need to check file existence here because it is done in the file chooser
            
            // start a new thread to read in the data
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    PortableEjbDataSource[] ejbDataSources = ImportEjbDataSourcesHelper.readDataSourceImports( getImportFilePath() );
                    if( ejbDataSources != null )
                        setEjbDataSources( ejbDataSources );
                    else
                        return;
                }
            });
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JPanel filePanel;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables
    
}
