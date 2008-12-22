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
package org.netbeans.modules.hibernate.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.hibernate.wizards.support.SourceGroupUISupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author  gowri
 */
public class HibernateCodeGenerationPanel extends javax.swing.JPanel {

    private Project project;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private JTextComponent packageComboBoxEditor;
    private HibernateEnvironment env;
    List<FileObject> configFileObjects;
    List<FileObject> revengFileObjects;


    public HibernateCodeGenerationPanel() {
        initComponents();        
        packageComboBoxEditor = (JTextComponent) cmbPackage.getEditor().getEditorComponent();
        Document packageComboBoxDocument = packageComboBoxEditor.getDocument();
        packageComboBoxDocument.addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent e) {
                packageChanged();
            }

            public void insertUpdate(DocumentEvent e) {
                packageChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                packageChanged();
            }
        });
    }

    /** Creates new form HibernateCodeGenerationPanel */
    public void initialize(Project project, FileObject targetFolder) {        
        this.project = project;
        env = project.getLookup().lookup(HibernateEnvironment.class);
        // Fill Configuration files dropdown
        fillConfiguration();

        // Fill Reveng Files dropdown
        fillRevengFiles();

        // Setting the project text field.
        txtProject.setText(ProjectUtils.getInformation(project).getDisplayName());

        // Setting the location drop down.
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        if(sourceGroups != null && sourceGroups.length == 0) {
            sourceGroups = HibernateUtil.getSourceGroups(project);
        }
        SourceGroupUISupport.connect(cmbLocation, sourceGroups);

        cmbPackage.setRenderer(PackageView.listRenderer());

        updatePackageComboBox();

        if (targetFolder != null) {
            // set default source group and package cf. targetFolder
            SourceGroup targetSourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                cmbLocation.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroups.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {                    
                    packageComboBoxEditor.setText(targetPackage);                   
                }
            }
        }
    }

    private void fillConfiguration() {        
        String[] configFiles = getConfigFilesFromProject(project);
        this.cmbConf.setModel(new DefaultComboBoxModel(configFiles));
    }

    private void fillRevengFiles() {
        String[] revengFiles = getRevengFilesFromProject(project);
        this.cmbReveng.setModel(new DefaultComboBoxModel(revengFiles));
    }

      // Gets the list of Config files from HibernateEnvironment.
    public String[] getConfigFilesFromProject(Project project) {
        List<String> configFiles = new ArrayList<String>();
        configFileObjects = env.getAllHibernateConfigFileObjects();
        for (FileObject fo : configFileObjects) {
            configFiles.add(fo.getNameExt());
        }
        return configFiles.toArray(new String[]{});
    }

    // Gets the list of Reveng files from HibernateEnvironment.
    public String[] getRevengFilesFromProject(Project project) {
        List<String> revengFiles = new ArrayList<String>();
        revengFileObjects = env.getAllHibernateReverseEnggFileObjects();
        for (FileObject fo : revengFileObjects) {
            revengFiles.add(fo.getNameExt());
        }
        return revengFiles.toArray(new String[]{});
    }


    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    private void packageChanged() {
        changeSupport.fireChange();
    }


    
   
    private void updatePackageComboBox() {
        SourceGroup sourceGroup = (SourceGroup) cmbLocation.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem() != null && model.getSelectedItem().toString().startsWith("META-INF") && model.getSize() > 1) { // NOI18N

                model.setSelectedItem(model.getElementAt(1));
            }
            cmbPackage.setModel(model);
        }
    }


    public FileObject getConfigurationFile() {
        if (cmbConf.getSelectedIndex() != -1) {
            return configFileObjects.get(cmbConf.getSelectedIndex());
        }
        return null;
    }

    public FileObject getRevengFile() {
        if (cmbReveng.getSelectedIndex() != -1) {
            return revengFileObjects.get(cmbReveng.getSelectedIndex());
        }
        return null;
    }
 

    public SourceGroup getLocationValue() {
        return (SourceGroup)cmbLocation.getSelectedItem();
    }

    public String getPackageName() {
        return packageComboBoxEditor.getText();
    }
    
    public boolean getChkDomain() {
        return chkDomain.isSelected();
    }
    
    public boolean getChkHbm() {
        return chkHbm.isSelected();
    } 
    
    public boolean getChkJava() {
        return chkJava.isSelected();
    }
    
    public boolean getChkEjb() {
        return chkEjb.isSelected();
    }
    
    private void locationChanged() {
        updatePackageComboBox();        
        changeSupport.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        chkDomain = new javax.swing.JCheckBox();
        chkHbm = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtProject = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cmbLocation = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbPackage = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        chkJava = new javax.swing.JCheckBox();
        chkEjb = new javax.swing.JCheckBox();
        cmbConf = new javax.swing.JComboBox();
        cmbReveng = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "LBL_GenerateClasses")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel1.text")); // NOI18N

        chkDomain.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkDomain, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.chkDomain.text")); // NOI18N

        chkHbm.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkHbm, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.chkHbm.text")); // NOI18N

        jLabel2.setLabelFor(txtProject);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel2.text")); // NOI18N

        txtProject.setEditable(false);
        txtProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProjectActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(cmbLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel3.text")); // NOI18N

        cmbLocation.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbLocationActionPerformed(evt);
            }
        });

        jLabel4.setLabelFor(cmbPackage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel4.text")); // NOI18N

        cmbPackage.setEditable(true);
        cmbPackage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(chkJava, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.chkJava.text")); // NOI18N
        chkJava.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkJavaActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(chkEjb, org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.chkEjb.text")); // NOI18N

        cmbConf.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbReveng.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel6.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(HibernateCodeGenerationPanel.class, "HibernateCodeGenerationPanel.jLabel7.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jLabel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 217, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cmbReveng, 0, 273, Short.MAX_VALUE)
                            .add(cmbConf, 0, 273, Short.MAX_VALUE)))
                    .add(jLabel5)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chkJava)
                            .add(chkEjb)))
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chkDomain)
                            .add(chkHbm)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel4)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtProject, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                            .add(cmbPackage, 0, 416, Short.MAX_VALUE)
                            .add(cmbLocation, 0, 416, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(cmbConf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(cmbReveng, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chkJava)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkEjb)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chkDomain)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chkHbm)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(txtProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(7, 7, 7)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbPackage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void cmbLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLocationActionPerformed
// TODO add your handling code here:
     locationChanged();
}//GEN-LAST:event_cmbLocationActionPerformed

private void chkJavaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkJavaActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chkJavaActionPerformed

private void txtProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProjectActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtProjectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkDomain;
    private javax.swing.JCheckBox chkEjb;
    private javax.swing.JCheckBox chkHbm;
    private javax.swing.JCheckBox chkJava;
    private javax.swing.JComboBox cmbConf;
    private javax.swing.JComboBox cmbLocation;
    private javax.swing.JComboBox cmbPackage;
    private javax.swing.JComboBox cmbReveng;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField txtProject;
    // End of variables declaration//GEN-END:variables
}
