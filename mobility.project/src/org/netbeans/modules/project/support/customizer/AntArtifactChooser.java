/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.project.support.customizer;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/** Accessory component used in the ProjectChooser for choosing project
 * artifacts.
 *
 * @author  phrebejk
 */
public class AntArtifactChooser extends javax.swing.JPanel implements PropertyChangeListener {
    
    // XXX to become an array later
    private String artifactType;
    private JFileChooser chooser;
    private JButton approveButton;
    
    /** Creates new form JarArtifactChooser */
    public AntArtifactChooser( String artifactType, JFileChooser chooser ) {
        this.artifactType = artifactType;
        this.chooser = chooser;
        initComponents();
        initAccessibility();
        jListArtifacts.setModel( new DefaultListModel() );
        chooser.addPropertyChangeListener( this );
        super.addNotify();
        if (setValid(false)) jListArtifacts.addListSelectionListener(new ListSelectionListener() {
            @SuppressWarnings("synthetic-access")
			public void valueChanged(@SuppressWarnings("unused")
			final ListSelectionEvent e) {
                setValid(jListArtifacts.getSelectedIndex() >= 0);
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelName = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        jLabelJarFiles = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListArtifacts = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        jLabelName.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelName, NbBundle.getMessage(AntArtifactChooser.class, "LBL_AAChooser_ProjectName")); // NOI18N
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

        jLabelJarFiles.setLabelFor(jListArtifacts);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJarFiles, NbBundle.getMessage(AntArtifactChooser.class, "LBL_AAChooser_JARFiles")); // NOI18N
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
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        //getAccessibleContext().setAccessibleName();
        //getAccessibleContext().setAccessibleDescription();
    }
    
    private boolean setValid(final boolean valid) {
        synchronized (this) {
            if (approveButton == null) try {
                final Object ui = chooser.getUI();
                final Method m = ui.getClass().getDeclaredMethod("getApproveButton", new Class[] {JFileChooser.class}); //NOI18N
                m.setAccessible(true);
                approveButton = (JButton)m.invoke(ui, new Object[] {chooser});
            } catch (Exception e) {
            }
        }
        if (approveButton != null) approveButton.setEnabled(valid);
        return approveButton != null;
    }
    
    public void propertyChange( final PropertyChangeEvent e ) {
        
        if ( JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals( e.getPropertyName() ) ) {
            // We have to update the Accessory
            final JFileChooser chooser = (JFileChooser)e.getSource();
            final File dir = chooser.getSelectedFile();
            final Project project = getProject( dir );
            populateAccessory( project );
        }
    }
    
    private Project getProject( final File projectDir ) {
        
        if (projectDir == null) {
            return null;
        }
        
        try {            
            File normProjectDir = FileUtil.normalizeFile(projectDir);
            final FileObject fo = FileUtil.toFileObject(normProjectDir);
            
            if (fo != null) {
                return ProjectManager.getDefault().findProject(fo);
            }
        } catch ( IOException e ) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return null;
    }
    
    
    private void populateAccessory( final Project project ) {
        
        final DefaultListModel model = (DefaultListModel)jListArtifacts.getModel();
        model.clear();
        jTextFieldName.setText(project == null ? "" : ProjectUtils.getInformation(project).getDisplayName()); //NOI18N
        if ( project != null ) {
            final AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType( project, artifactType );
            for( int i = 0; i < artifacts.length; i++ ) {
                final URI uris[] = artifacts[i].getArtifactLocations();
                for (int j=0; j<uris.length; j++) {
                    model.addElement( new ArtifactItem( artifacts[i], uris[j]));
                }
            }
            jListArtifacts.setSelectionInterval(0, model.size());            
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelJarFiles;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JList jListArtifacts;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
    
    private static Container findParent() {
        return DialogDisplayer.getDefault().createDialog(new DialogDescriptor(new JPanel(), "")).getParent();//NOI18N
    }
    
    /** Shows dialog with the artifact chooser
     * @return null if canceled selected jars if some jars selected
     */
    public static ArtifactItem[] showDialog( final String artifactType ) {
        
        final JFileChooser chooser = ProjectChooser.projectChooser();
        chooser.setDialogTitle( NbBundle.getMessage(AntArtifactChooser.class, "LBL_AAChooserSelectProject") ); //NOI18N
        chooser.setApproveButtonText( NbBundle.getMessage(AntArtifactChooser.class, "LBL_AAChooser_AddJARFiles") ); //NOI18N
        
        final AntArtifactChooser accessory = new AntArtifactChooser( artifactType, chooser );
        chooser.setAccessory( accessory );
        chooser.setPreferredSize( new Dimension( 650, 380 ) );
        
        final int option = chooser.showOpenDialog( findParent() ); // show file chooser
        
        if ( option == JFileChooser.APPROVE_OPTION ) {
            
            final Object elements[] = accessory.jListArtifacts.getSelectedValues();
            final ArtifactItem artifacts[] = new ArtifactItem[elements.length];
            System.arraycopy(elements, 0, artifacts, 0, elements.length);
            return artifacts;
            
        } 
        return null;
    }
    
    
    public static class ArtifactItem {
        
        final private AntArtifact artifact;
        final private URI uri;
        
        ArtifactItem( AntArtifact artifact, URI uri ) {
            this.artifact = artifact;
            this.uri = uri;
        }
        
        public AntArtifact getArtifact() {
            return artifact;
        }
        
        public URI getURI() {
            return uri;
        }
        
        public String toString() {
            return uri.getPath();
        }
        
    }
}
