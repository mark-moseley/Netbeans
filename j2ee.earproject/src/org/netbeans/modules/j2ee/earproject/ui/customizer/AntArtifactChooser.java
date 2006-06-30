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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.Dialog;
import java.awt.Dimension;

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

import org.openide.NotifyDescriptor;

/** Accessory component used in the ProjectChooser for choosing project
 * artifacts.
 *
 * @author  phrebejk
 */
public class AntArtifactChooser extends javax.swing.JPanel implements PropertyChangeListener {
    
    // XXX to become an array later
    private String[] artifactType;
    
    /** Creates new form AntArtifactChooser */
    public AntArtifactChooser( String[] artifactType, JFileChooser chooser ) {
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

        jLabelName.setText(NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectName_JLabel"));
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

        jLabelJarFiles.setText(NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectJarFiles_JLabel"));
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
            Project project = dir == null ? null : getProject(FileUtil.normalizeFile(dir));
            populateAccessory( project );
        }
    }
    
    private Project getProject( File projectDir ) {
        
        try {            
            projectDir = FileUtil.normalizeFile (projectDir);
            FileObject fo = FileUtil.toFileObject(projectDir);
            
            if (fo != null) {
                Project project = ProjectManager.getDefault().findProject(fo);
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
            
            for (int j = 0; j < artifactType.length; j++) {
                        
            AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType( project, artifactType[j] );
        
            for( int i = 0; i < artifacts.length; i++ ) {
                model.addElement( new ArtifactItem( artifacts[i]));
            }
            if (artifacts.length > 0)
                return;
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
    public static AntArtifact[] showDialog( String artifactType, Project master ) {
        return showDialog(master, new String[] { artifactType });
    }
    
    public static AntArtifact[] showDialog(Project master, String [] artifactTypes) {        
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_Title" ) ); // NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_SelectProject" ) ); // NOI18N
        
        AntArtifactChooser accessory = new AntArtifactChooser( artifactTypes, chooser );
        chooser.setAccessory( accessory );
        
        chooser.setPreferredSize( new Dimension( 650, 360 ) );
        
        int option = chooser.showOpenDialog( null ); // Show the chooser
              
        if ( option == JFileChooser.APPROVE_OPTION ) {
            
            File dir = chooser.getSelectedFile();
            dir = FileUtil.normalizeFile (dir);
            Project selectedProject = accessory.getProject( dir );

            if ( selectedProject == null ) {
                return null;
            }
            
            if ( selectedProject.getProjectDirectory().equals( master.getProjectDirectory() ) ) {
                DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message( 
                    NbBundle.getMessage( AntArtifactChooser.class, "MSG_AACH_RefToItself" ),
                    NotifyDescriptor.INFORMATION_MESSAGE ) );
                return null;
            }
            
            if ( ProjectUtils.hasSubprojectCycles( master, selectedProject ) ) {
                DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message( 
                    NbBundle.getMessage( AntArtifactChooser.class, "MSG_AACH_Cycles" ),
                    NotifyDescriptor.INFORMATION_MESSAGE ) );
                return null;
            }
            
            DefaultListModel model = (DefaultListModel)accessory.jListArtifacts.getModel();
            
            AntArtifact artifacts[] = new AntArtifact[ model.size() ];
            
            // XXX Adding references twice            
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
            return artifact.getArtifactLocations()[0].toString();
        }
        
    }
}
