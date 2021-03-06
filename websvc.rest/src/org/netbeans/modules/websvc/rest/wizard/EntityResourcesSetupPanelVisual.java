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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Pavel Buzek
 */
public class EntityResourcesSetupPanelVisual extends javax.swing.JPanel implements AbstractPanel.Settings {
    
    private Project project;
    private List<ChangeListener> listeners;
    private WizardDescriptor wizard;
    
    //    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    /** Creates new form CrudSetupPanel */
    public EntityResourcesSetupPanelVisual(String name) {
        initComponents();
        setName(name);
        this.listeners = new ArrayList<ChangeListener>();
        resourcePackageComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireChange();
            }
        });
        converterPackageComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fireChange();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        resourcePackageLabel = new javax.swing.JLabel();
        resourcePackageComboBox = new javax.swing.JComboBox();
        converterPackageLabel = new javax.swing.JLabel();
        converterPackageComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewTA = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        setName("null");

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        resourcePackageLabel.setLabelFor(resourcePackageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(resourcePackageLabel, org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "LBL_Package")); // NOI18N

        resourcePackageComboBox.setEditable(true);
        resourcePackageComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                resourcePackageComboBoxItemStateChanged(evt);
            }
        });
        resourcePackageComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                resourcePackageComboBoxPropertyChange(evt);
            }
        });

        converterPackageLabel.setLabelFor(converterPackageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(converterPackageLabel, org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "LBL_ResourceDir")); // NOI18N

        converterPackageComboBox.setEditable(true);
        converterPackageComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                converterPackageComboBoxItemStateChanged(evt);
            }
        });
        converterPackageComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                converterPackageComboBoxPropertyChange(evt);
            }
        });

        previewTA.setColumns(20);
        previewTA.setEditable(false);
        previewTA.setRows(6);
        previewTA.setText(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "MSG_GeneratedClassNames")); // NOI18N
        previewTA.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(previewTA);
        previewTA.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "PreviewGeneratedClassNames")); // NOI18N
        previewTA.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_Preview")); // NOI18N

        jLabel1.setLabelFor(previewTA);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "LBL_Preview")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, resourcePackageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, converterPackageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(projectLabel)
                    .add(locationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(locationComboBox, 0, 382, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .add(converterPackageComboBox, 0, 382, Short.MAX_VALUE)
                    .add(resourcePackageComboBox, 0, 382, Short.MAX_VALUE)))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLabel)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationLabel)
                    .add(locationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourcePackageLabel)
                    .add(resourcePackageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(converterPackageLabel)
                    .add(converterPackageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
        );

        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "Project")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_Project")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "Location")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_Location")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "Location")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_Location")); // NOI18N
        resourcePackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "ResourcePackage")); // NOI18N
        resourcePackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_ResourcePackage")); // NOI18N
        resourcePackageComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "ResourcePackage")); // NOI18N
        resourcePackageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_ResourcePackage")); // NOI18N
        converterPackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "ConverterPackage")); // NOI18N
        converterPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_ConverterPackage")); // NOI18N
        converterPackageComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "ConverterPackage")); // NOI18N
        converterPackageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EntityResourcesSetupPanelVisual.class, "DESC_ConverterPackage")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void converterPackageComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_converterPackageComboBoxItemStateChanged
    updatePreview();
}//GEN-LAST:event_converterPackageComboBoxItemStateChanged

private void resourcePackageComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_resourcePackageComboBoxItemStateChanged
    updatePreview();
}//GEN-LAST:event_resourcePackageComboBoxItemStateChanged

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void resourcePackageComboBoxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_resourcePackageComboBoxPropertyChange
        fireChange();
    }//GEN-LAST:event_resourcePackageComboBoxPropertyChange

    private void converterPackageComboBoxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_converterPackageComboBoxPropertyChange
        fireChange();
    }//GEN-LAST:event_converterPackageComboBoxPropertyChange
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox converterPackageComboBox;
    private javax.swing.JLabel converterPackageLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextArea previewTA;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JComboBox resourcePackageComboBox;
    private javax.swing.JLabel resourcePackageLabel;
    // End of variables declaration//GEN-END:variables
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    private void fireChange() {
        ChangeEvent event =  new ChangeEvent(this);
        
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    public boolean valid(WizardDescriptor wizard) {
        AbstractPanel.clearErrorMessage(wizard);
        SourceGroup[] groups = SourceGroupSupport.getJavaSourceGroups(project);
        
        if (groups == null || groups.length < 1) {
            AbstractPanel.setErrorMessage(wizard, "MSG_NoJavaSourceRoots");
        } else if (! Util.isValidPackageName(getResourcePackage())) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidResourcePackageName");
            return false;
        } else if (! Util.isValidPackageName(getConverterPackage())) {
            AbstractPanel.setErrorMessage(wizard, "MSG_InvalidConverterPackageName");
            return false;
        }
        return true;
    }
    
    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }
    
    public String getResourcePackage() {
        return ((JTextComponent)resourcePackageComboBox.getEditor().getEditorComponent()).getText();
    }
    
    public String getConverterPackage() {
        return ((JTextComponent)converterPackageComboBox.getEditor().getEditorComponent()).getText();
    }
    
    private void setResourcePackage(String text) {
        ((JTextComponent)resourcePackageComboBox.getEditor().getEditorComponent()).setText(text);
    }
    
    private void setConverterPackage(String text) {
        ((JTextComponent)converterPackageComboBox.getEditor().getEditorComponent()).setText(text);
    }
    
    private void locationChanged() {
        updateSourceGroupPackages();
        fireChange();
    }
    
    public void read(WizardDescriptor settings) {
        if (project != null) return;
        
        this.wizard = settings;
        
        project = Templates.getProject(settings);
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);
        
        resourcePackageComboBox.setRenderer(PackageView.listRenderer());
        converterPackageComboBox.setRenderer(PackageView.listRenderer());
        updateSourceGroupPackages();
        
        FileObject targetFolder = Templates.getTargetFolder(settings);
        SourceGroup targetSourceGroup = null;
        
        if (targetFolder == null) {
            targetSourceGroup = getSourceGroup();
            targetFolder = targetSourceGroup.getRootFolder();
        } else {
            targetSourceGroup = SourceGroupSupport.findSourceGroupForFile(sourceGroups, targetFolder);
            setSourceGroup(targetSourceGroup);
        }
        
        String targetPackage = SourceGroupSupport.getPackageForFolder(targetSourceGroup, targetFolder);
        targetPackage = (targetPackage.length() == 0) ? "" : targetPackage + ".";
        String resourcePackage = targetPackage + EntityResourcesGenerator.RESOURCE_FOLDER;
        setResourcePackage(resourcePackage);
        String converterPackage = targetPackage + EntityResourcesGenerator.CONVERTER_FOLDER;
        setConverterPackage(converterPackage);
        
        addComboBoxListener(resourcePackageComboBox);
        addComboBoxListener(converterPackageComboBox);
        updatePreview();
    }
    
    public void store(WizardDescriptor settings) {
        if (Templates.getTargetFolder(settings) == null) {
            Templates.setTargetFolder(settings, getSourceGroup().getRootFolder());
        }
        
        settings.putProperty(WizardProperties.RESOURCE_PACKAGE, getResourcePackage());
        settings.putProperty(WizardProperties.CONVERTER_PACKAGE, getConverterPackage());
    }
    
    private void addComboBoxListener(JComboBox comboBox) {
        JTextComponent text = ((JTextComponent)comboBox.getEditor().getEditorComponent());
        text.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                updatePreview();
            }
        });
    }
    
    
    private void updateSourceGroupPackages() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSize() > 0) {
                model.setSelectedItem(model.getElementAt(0));
            }
            resourcePackageComboBox.setModel(model);
            model = PackageView.createListView(sourceGroup);
            if (model.getSize() > 0) {
                model.setSelectedItem(model.getElementAt(0));
            }
            converterPackageComboBox.setModel(model);
        }
    }
    
    private void updatePreview() {
        EntityResourceBeanModel model = (EntityResourceBeanModel) wizard.getProperty(WizardProperties.ENTITY_RESOURCE_MODEL);
        EntityResourcesGenerator generator = new EntityResourcesGenerator(model,
                getResourcePackage(), getConverterPackage());
        Collection<String> previewClasses = generator.previewClasses();
        String previewText = "";        //NOI18N
        
        for (String c : previewClasses) {
            previewText += c + "\n";        //NOI18N
        }
        
        previewTA.setText(previewText);
        previewTA.setCaretPosition(0);
    }
    
    private SourceGroup getSourceGroup() {
        return (SourceGroup) locationComboBox.getSelectedItem();
    }
    
    private void setSourceGroup(SourceGroup group) {
        locationComboBox.setSelectedItem(group);
    }
}
