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


package org.netbeans.modules.compapp.projects.base.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ant.AntArtifactProvider;



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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelJarFiles = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListArtifacts = new javax.swing.JList();

        setMinimumSize(new java.awt.Dimension(190, 80));
        setPreferredSize(new java.awt.Dimension(270, 187));
        jLabelName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/projects/base/ui/customizer/Bundle").getString("LBL_AACH_ProjectName_LabelMnemonic").charAt(0));
        jLabelName.setText(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectName_JLabel"));

        jTextFieldName.setEditable(false);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/projects/base/ui/customizer/Bundle").getString("ACS_AACH_ProjectName_A11YDesc"));

        jLabelJarFiles.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/projects/base/ui/customizer/Bundle").getString("LBL_AACH_ProjectJarFiles_LabelMnemonic").charAt(0));
        jLabelJarFiles.setText(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectJarFiles_JLabel"));

        jScrollPane1.setViewportView(jListArtifacts);
        jListArtifacts.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/compapp/projects/base/ui/customizer/Bundle").getString("ACS_AACH_ProjectJarFiles_A11YDesc"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelName)
                    .add(jTextFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .add(jLabelJarFiles)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelName)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabelJarFiles)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


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
            AntArtifactProvider prov = project.getLookup().lookup(AntArtifactProvider.class);
            if (prov != null) {
                AntArtifact[] artifacts = prov.getBuildArtifacts();
                if ((artifacts != null) && (artifacts.length > 0)) {
//                    AntArtifact aa = artifacts[0];
//                    if (aa.getType().startsWith(artifactType)) {
//                        model.addElement( new ArtifactItem( aa));
//                    }
                    for (int i = 0; i < artifacts.length; i++) {
                      if (artifacts[i].getType().startsWith(artifactType)) {
                          model.addElement( new ArtifactItem( artifacts[i]));
                      }
                    }
                }
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
    public static AntArtifact[] showDialog( String artifactType, DefaultListModel data, Project master ) {

        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_Title" ) ); // NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_SelectProject" ) ); // NOI18N

        AntArtifactChooser accessory = new AntArtifactChooser( artifactType, chooser );
        chooser.setAccessory( accessory );

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

            if (data != null) {
                for (int i = 0, size = data.getSize(); i < size; i++) {
                    VisualClassPathItem vi = (VisualClassPathItem) data.get(i);
                    if (vi != null) {
                        AntArtifact aa = (AntArtifact) vi.getObject();
                        if (selectedProject.equals(aa.getProject())) {
                            DialogDisplayer.getDefault().notify( new NotifyDescriptor.Message(
                                NbBundle.getMessage( AntArtifactChooser.class, "MSG_AACH_AlreadySelected" ),
                                NotifyDescriptor.INFORMATION_MESSAGE ) );
                            return null;
                        }
                    }
                }
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

    /** Shows dialog with the artifact chooser
     * @return null if canceled selected jars if some jars selected
     */
    public static AntArtifact[] showDialog( String artifactType, Project p ) {

        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_Title" ) ); // NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_SelectProject" ) ); // NOI18N

        AntArtifactChooser accessory = new AntArtifactChooser( artifactType, chooser );
        chooser.setAccessory( accessory );
        if (p != null) {
            FileObject dobj = p.getProjectDirectory().getParent();
            if (dobj != null) {
                chooser.setCurrentDirectory(FileUtil.toFile(dobj));
            }
        }
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
            URI[] us = artifact.getArtifactLocations();
            if ((us != null) && (us.length > 0)) {
                return us[0].toString();
            }
            return null;
        }
    }
}
