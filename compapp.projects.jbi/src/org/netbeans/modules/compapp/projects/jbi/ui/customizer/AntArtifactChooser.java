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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ant.AntArtifactProvider;



/** Accessory component used in the ProjectChooser for choosing project
 * artifacts.
 *
 * @author  phrebejk
 */
public class AntArtifactChooser extends javax.swing.JPanel implements PropertyChangeListener {
    private List<String> artifactTypes;
    
    public AntArtifactChooser(List<String> nArtifactTypes, JFileChooser chooser ) {
        this.artifactTypes = nArtifactTypes;
        initComponents();
        jListArtifacts.setModel( new DefaultListModel() );
        chooser.addPropertyChangeListener( this );
    }
 
    /** Creates new form JarArtifactChooser */
    public AntArtifactChooser( String artifactType, JFileChooser chooser ) {
        List<String> newList = new ArrayList<String>();
        newList.add(artifactType);
        this.artifactTypes = newList;
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

        jLabelName.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelName, org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectName_JLabel"));

        jTextFieldName.setEditable(false);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "ACS_AACH_ProjectName_A11YDesc"));

        jLabelJarFiles.setLabelFor(jListArtifacts);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJarFiles, org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "LBL_AACH_ProjectJarFiles_JLabel"));

        jScrollPane1.setViewportView(jListArtifacts);
        jListArtifacts.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(AntArtifactChooser.class).getString("ACS_AACH_ProjectJarFiles_A11YName"));
        jListArtifacts.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AntArtifactChooser.class, "ACS_AACH_ProjectJarFiles_A11YDesc"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabelName)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jTextFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                        .add(12, 12, 12))
                    .add(layout.createSequentialGroup()
                        .add(jLabelJarFiles)
                        .addContainerGap(176, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                        .add(12, 12, 12))))
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
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
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
        if (projectDir == null) { // #46744
            return null;
        }
        
        try {
            FileObject projectRoot = FileUtil.toFileObject( projectDir );
            
            if ( projectRoot != null ) {
                Project project = ProjectManager.getDefault().findProject( projectRoot );
                return project;
            }
        } catch ( IOException e ) {
            // Return null
        }
        
        return null;
    }
    
    private void populateAccessory( Project project ) {
        DefaultListModel model = (DefaultListModel)jListArtifacts.getModel();
        model.clear();
        jTextFieldName.setText(project == null ? "" : ProjectUtils.getInformation(project).getDisplayName()); // NOI18N
        
        if ( project != null ) {
            AntArtifactProvider prov = (AntArtifactProvider)project.getLookup().lookup(AntArtifactProvider.class);
            if (prov != null) {
                AntArtifact[] artifacts = prov.getBuildArtifacts();
                Iterator<String> artifactTypeItr = null;
                String artifactType = null;
                if (artifacts != null) {
                    for (int i = 0; i < artifacts.length; i++) {
                        artifactTypeItr = this.artifactTypes.iterator();
                        while (artifactTypeItr.hasNext()){
                            artifactType = artifactTypeItr.next();
                            if (artifacts[i].getType().startsWith(artifactType)) {
                                model.addElement( new ArtifactItem( artifacts[i]));
                                return;
                            }
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
    
    private static void applyFilefilters(JFileChooser fc, List<FileFilter> filefilterList, FileFilter def) {
        if ( filefilterList != null ) {
            if (def == null){
                def = fc.getFileFilter();
            }
            
            for ( FileFilter ff : filefilterList ) {
                fc.addChoosableFileFilter( ff );
            }
            if (def != null){
                fc.setFileFilter(def);
            }
        }
    }

    public static AntArtifact[] showDialog(List<String> artifactTypes, Project p, List<FileFilter> filters, FileFilter defFilter ) {
        JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_Title" ) ); // NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage( AntArtifactChooser.class, "LBL_AACH_SelectProject" ) ); // NOI18N
        
        AntArtifactChooser accessory = new AntArtifactChooser(artifactTypes, chooser );
        chooser.setAccessory( accessory );
        if (p != null) {
            FileObject dobj = p.getProjectDirectory().getParent();
            if (dobj != null) {
                chooser.setCurrentDirectory(FileUtil.toFile(dobj));
            }
        }
        if ( filters != null )
            applyFilefilters( chooser, filters, defFilter );
        
        int option = chooser.showOpenDialog( null ); // Show the chooser
        
        if ( option == JFileChooser.APPROVE_OPTION ) {
            DefaultListModel model = (DefaultListModel)accessory.jListArtifacts.getModel();
            AntArtifact artifacts[] = new AntArtifact[ model.size() ];

            for( int i = 0; i < artifacts.length; i++ ) {
                artifacts[i] = ((ArtifactItem)model.getElementAt( i )).getArtifact();
            }
            
            return artifacts;            
        } else {
            return null;
        }        
    }
    
    /** Shows dialog with the artifact chooser
     * @return null if canceled selected jars if some jars selected
     */
    public static AntArtifact[] showDialog(String artifactType, Project p ) {        
        List<String> arts = new ArrayList<String>();
        arts.add(artifactType);
        return showDialog(arts, p, null, null);
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
