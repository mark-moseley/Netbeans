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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;



/** Accessory component used in the ProjectChooser for choosing project
 * artifacts.
 *
 * @author  phrebejk
 */
public class AntArtifactChooser extends javax.swing.JPanel implements PropertyChangeListener {
    
    // XXX to become an array later
    private String artifactType;
    
    /** Creates new form JarArtifactChooser */
    public AntArtifactChooser( String artifactType, JFileChooser chooser ) {
        this.artifactType = artifactType;
        
        initComponents();
        jListArtifacts.setModel( new DefaultListModel() );
        chooser.addPropertyChangeListener( this );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelJarFiles = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListArtifacts = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        jLabelName.setText(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectName_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 2, 0);
        add(jLabelName, gridBagConstraints);

        jTextFieldName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        add(jTextFieldName, gridBagConstraints);

        jLabelJarFiles.setText(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectJarFiles_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 2, 0);
        add(jLabelJarFiles, gridBagConstraints);

        jScrollPane1.setViewportView(jListArtifacts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals( e.getPropertyName() ) ) {             
            // We have to update the Accessory
            JFileChooser chooser = (JFileChooser)e.getSource();
            File dir = chooser.getSelectedFile();
            DefaultListModel spListModel = (DefaultListModel)jListArtifacts.getModel();
            
            Project project = getProject( dir );
            populateAccessory( project );                                    
        }
    }
    
    private Project getProject( File projectDir ) {
        
        try {            
            FileObject projectRoot = FileUtil.toFileObject ( projectDir );
            
            if ( projectRoot != null ) {
                Project project = ProjectManager.getDefault().findProject( projectRoot );
                return project;
            }
        }
        catch ( IOException e ) {
            // Return null
        }
        
        return null;
    }    
    
    
    private void populateAccessory( Project project ) {
        
        DefaultListModel model = (DefaultListModel)jListArtifacts.getModel();
        model.clear();
        jTextFieldName.setText(project == null ? "" : ProjectUtils.getInformation(project).getDisplayName()); //NOI18N
        
        if ( project != null ) {
                        
            AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType( project, artifactType );
        
            for( int i = 0; i < artifacts.length; i++ ) {
                model.addElement( new ArtifactItem( artifacts[i]));
            }
        }
        
    }
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelJarFiles;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JList jListArtifacts;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables

    
    /** Shows dialog with the artifact chooser 
     * @return null if canceled selected jars if some jars selected
     */
    public static AntArtifact[] showDialog( String artifactType ) {
        
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_Title" ) ); // NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_SelectProject" ) ); // NOI18N
        
        AntArtifactChooser accessory = new AntArtifactChooser( artifactType, chooser );
        chooser.setAccessory( accessory );
        

        int option = chooser.showOpenDialog( null ); // Show the chooser
              
        if ( option == JFileChooser.APPROVE_OPTION ) {

            DefaultListModel model = (DefaultListModel)accessory.jListArtifacts.getModel();
            
            AntArtifact artifacts[] = new AntArtifact[ model.size() ];
            
            // XXX Adding references twice
            
            // XXX What about adding reference to itself            
            for( int i = 0; i < artifacts.length; i++ ) {
                artifacts[i] = ((ArtifactItem)model.getElementAt( i )).getArtifact();
            }
            
            return artifacts;
            
        }
        else {
            return null; 
        }
                
    }
        
    
    private static class ArtifactItem {
        
        private AntArtifact artifact;
        
        ArtifactItem( AntArtifact artifact ) {
            this.artifact = artifact;
        }
        
        AntArtifact getArtifact() {
            return artifact;
        }
        
        public String toString() {
            return artifact.getArtifactLocation().toString();
        }
        
    }
}
