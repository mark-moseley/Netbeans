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

package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Nam Nguyen
 */
public class SingletonSetupPanelVisual extends javax.swing.JPanel implements AbstractPanel.Settings {
    
    private Project project;
    private List<ChangeListener> listeners;
    private boolean uriOveridden;
    private boolean resourceClassNameOveridden;
    
    
    /** Creates new form CrudSetupPanel */
    public SingletonSetupPanelVisual(String name) {
        setName(name);
        this.listeners = new ArrayList<ChangeListener>();
        initComponents();
        
        medaTypeComboBox.setModel(new DefaultComboBoxModel(GenericResourceBean.getSupportedMimeTypes()));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        classLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        uriLabel = new javax.swing.JLabel();
        uriTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        medaTypeComboBox = new javax.swing.JComboBox();
        mediaTypeLabel = new javax.swing.JLabel();
        contentClassLabel = new javax.swing.JLabel();
        selectClassButton = new javax.swing.JButton();
        contentClassTextField = new javax.swing.JTextField();
        resourceNameLabel = new javax.swing.JLabel();
        resourceNameTextField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();

        classLabel.setLabelFor(classTextField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/rest/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(classLabel, bundle.getString("MSG_ClassName")); // NOI18N

        uriLabel.setLabelFor(uriTextField);
        org.openide.awt.Mnemonics.setLocalizedText(uriLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_UriTemplate")); // NOI18N

        uriTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                uriChanged(evt);
            }
        });

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.setEnabled(false);
        locationComboBox.setMinimumSize(new java.awt.Dimension(4, 20));
        locationComboBox.setPreferredSize(new java.awt.Dimension(130, 23));
        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_Package")); // NOI18N

        packageComboBox.setEditable(true);
        packageComboBox.setMinimumSize(new java.awt.Dimension(4, 20));
        packageComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                packageChanged(evt);
            }
        });

        medaTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        medaTypeComboBox.setMinimumSize(new java.awt.Dimension(4, 20));

        mediaTypeLabel.setLabelFor(medaTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(mediaTypeLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_MimeType")); // NOI18N

        contentClassLabel.setLabelFor(contentClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(contentClassLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_RepresentationClass")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectClassButton, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_Select")); // NOI18N
        selectClassButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        selectClassButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mouseClickHandler(evt);
            }
        });
        selectClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectClassButtonActionPerformed(evt);
            }
        });

        contentClassTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                representationClassChanged(evt);
            }
        });

        resourceNameLabel.setLabelFor(resourceNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resourceNameLabel, org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_ResourceName")); // NOI18N

        resourceNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resourceNameChanged(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                resourceNameChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(locationLabel)
                    .add(projectLabel))
                .add(73, 73, 73)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .add(locationComboBox, 0, 330, Short.MAX_VALUE))
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(contentClassLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentClassTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectClassButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(uriLabel)
                    .add(mediaTypeLabel))
                .add(58, 58, 58)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(medaTypeComboBox, 0, 316, Short.MAX_VALUE)
                    .add(uriTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE))
                .addContainerGap())
            .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(packageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(packageComboBox, 0, 330, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(resourceNameLabel)
                .add(43, 43, 43)
                .add(resourceNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(classLabel)
                .add(65, 65, 65)
                .add(classTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addContainerGap())
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
                    .add(locationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(packageLabel)
                    .add(packageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourceNameLabel)
                    .add(resourceNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(classTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(classLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uriLabel)
                    .add(uriTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mediaTypeLabel)
                    .add(medaTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contentClassLabel)
                    .add(contentClassTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectClassButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        classLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "ClassName")); // NOI18N
        classLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ClassName")); // NOI18N
        classTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "ClassName")); // NOI18N
        classTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ClassName")); // NOI18N
        uriLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "UirTemplate")); // NOI18N
        uriLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Uri")); // NOI18N
        uriTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "UriTemplate")); // NOI18N
        uriTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Uri")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "Project")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Project")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "Location")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Location")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "Location")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_Location")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "ResourcePackage")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ResourcePackage")); // NOI18N
        packageComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "ResourcePackage")); // NOI18N
        packageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ResourcePackage")); // NOI18N
        medaTypeComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "MimeType")); // NOI18N
        medaTypeComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_MimeType")); // NOI18N
        mediaTypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "MimeType")); // NOI18N
        mediaTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_MimeType")); // NOI18N
        contentClassLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "RepresentationClass")); // NOI18N
        contentClassLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_RepresentationClass")); // NOI18N
        selectClassButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "SelectRepresentationClass")); // NOI18N
        selectClassButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_SelectRepresenationClass")); // NOI18N
        contentClassTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "RepresentationClass")); // NOI18N
        contentClassTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_RepresentationClass")); // NOI18N
        resourceNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_ResourceName")); // NOI18N
        resourceNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ResourceName")); // NOI18N
        resourceNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "ResourceName")); // NOI18N
        resourceNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "DESC_ResourceName")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SingletonSetupPanelVisual.class, "LBL_Select")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void containerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_containerTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_containerTextFieldActionPerformed

private void packageChanged(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_packageChanged
    fireChange();
}//GEN-LAST:event_packageChanged

private void representationClassChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_representationClassChanged
    fireChange();
}//GEN-LAST:event_representationClassChanged

private void uriChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_uriChanged
    uriOveridden = true;
    fireChange();
}//GEN-LAST:event_uriChanged

private void resourceNameChanged(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resourceNameChanged
    String newName = resourceNameTextField.getText();
    if (! resourceClassNameOveridden) {
        classTextField.setText(Util.upperFirstChar(newName) + EntityResourcesGenerator.RESOURCE_SUFFIX);
    }
    if (! uriOveridden) {
        uriTextField.setText(Util.lowerFirstChar(newName));
    }
    fireChange();
    
}//GEN-LAST:event_resourceNameChanged

    private void selectClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectClassButtonActionPerformed
    fireChange();
    
}//GEN-LAST:event_selectClassButtonActionPerformed

    private void mouseClickHandler(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseClickHandler
        String className = Util.chooseClass(project);
        if (className != null) {
            contentClassTextField.setText(className);
            fireChange();
        }
    
}//GEN-LAST:event_mouseClickHandler

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed
                
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel contentClassLabel;
    private javax.swing.JTextField contentClassTextField;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox medaTypeComboBox;
    private javax.swing.JLabel mediaTypeLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel resourceNameLabel;
    private javax.swing.JTextField resourceNameTextField;
    private javax.swing.JButton selectClassButton;
    private javax.swing.JLabel uriLabel;
    private javax.swing.JTextField uriTextField;
    // End of variables declaration//GEN-END:variables
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    public void fireChange() {
        ChangeEvent event =  new ChangeEvent(this);
        
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    public boolean valid(WizardDescriptor wizard) {
        //TODO
        AbstractPanel.clearErrorMessage(wizard);
        return true;
    }
    
    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getPackage() {
        return ((JTextComponent)packageComboBox.getEditor().getEditorComponent()).getText();
    }

    private void locationChanged() {
        updateSourceGroupPackages();
        fireChange();
    }
    
    private String getResourceName() {
        return resourceNameTextField.getText();
    }
    
    public static final String DEFAULT_RESOURCE_NAME = "Generic";
    
    public void read(WizardDescriptor settings) {
        String value = (String) settings.getProperty(WizardProperties.RESOURCE_NAME);
        if (value == null || value.trim().length() == 0) {
            resourceNameTextField.setText(DEFAULT_RESOURCE_NAME);
            classTextField.setText(Util.deriveResourceClassName(getResourceName()));
            //uriTextField.setText("/" + Util.pluralize(Util.lowerFirstChar(getResourceName())) + "/{name}"); //NOI18N
            uriTextField.setText(Util.lowerFirstChar(getResourceName()));
            contentClassTextField.setText(GenericResourceBean.
                getDefaultRepresetationClass((MimeType)medaTypeComboBox.getSelectedItem()));
        } else {
            resourceNameTextField.setText(value);
            classTextField.setText((String) settings.getProperty(WizardProperties.RESOURCE_CLASS));
            uriTextField.setText((String) settings.getProperty(WizardProperties.RESOURCE_URI));
            medaTypeComboBox.setSelectedItem(((MimeType[]) settings.getProperty(WizardProperties.MIME_TYPES))[0]);
            String[] types = (String[]) settings.getProperty(WizardProperties.REPRESENTATION_TYPES);
            if (types != null && types.length > 0) {
                contentClassTextField.setText(types[0]);
            }
        }
        
        project = Templates.getProject(settings);
        FileObject targetFolder = Templates.getTargetFolder(settings);
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        packageComboBox.setRenderer(PackageView.listRenderer());

        updateSourceGroupPackages();

        // set default source group and package cf. targetFolder
        if (targetFolder != null) {
            SourceGroup targetSourceGroup = SourceGroupSupport.findSourceGroupForFile(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                locationComboBox.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroupSupport.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {
                    ((JTextComponent)packageComboBox.getEditor().getEditorComponent()).setText(targetPackage);
               }
            }
        }
    }
    
    public void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.RESOURCE_PACKAGE, getPackage());
        settings.putProperty(WizardProperties.RESOURCE_NAME, classTextField.getText());
        settings.putProperty(WizardProperties.RESOURCE_URI, uriTextField.getText());
        settings.putProperty(WizardProperties.MIME_TYPES, new MimeType[] { (MimeType) medaTypeComboBox.getSelectedItem() });
        settings.putProperty(WizardProperties.REPRESENTATION_TYPES, new String[] { contentClassTextField.getText()} );
        
        try {            
            Templates.setTargetFolder(settings, SourceGroupSupport.getFolderForPackage(getLocationValue(), getPackage()));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    private void updateSourceGroupPackages() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        ComboBoxModel model = PackageView.createListView(sourceGroup);
        if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                && model.getSize() > 1) { // NOI18N
            model.setSelectedItem(model.getElementAt(1));
        }
        packageComboBox.setModel(model);
    }
    
}
