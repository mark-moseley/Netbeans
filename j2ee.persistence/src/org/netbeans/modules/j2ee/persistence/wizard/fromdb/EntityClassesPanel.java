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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class EntityClassesPanel extends javax.swing.JPanel {

    private final static Logger LOGGER = Logger.getLogger(EntityClassesPanel.class.getName());

    private JTextComponent packageComboBoxEditor;

    private PersistenceGenerator persistenceGen;
    private Project project;
    private boolean cmp;
    private String tableSourceName; //either Datasource or a connection

    private SelectedTables selectedTables;

    private ChangeSupport changeSupport = new ChangeSupport(this);

    private PersistenceUnit persistenceUnit;

    public EntityClassesPanel() {
        initComponents();

        classNamesTable.getParent().setBackground(classNamesTable.getBackground());
        classNamesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N

        packageComboBoxEditor = ((JTextComponent)packageComboBox.getEditor().getEditorComponent());
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

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void initialize(PersistenceGenerator persistenceGen, Project project, boolean cmp, FileObject targetFolder) {
        this.persistenceGen = persistenceGen;
        this.project = project;
        this.cmp = cmp;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        packageComboBox.setRenderer(PackageView.listRenderer());

        updatePackageComboBox();

        if (targetFolder != null) {
            // set default source group and package cf. targetFolder
            SourceGroup targetSourceGroup = SourceGroupSupport.getFolderSourceGroup(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                locationComboBox.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroupSupport.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {
                    packageComboBoxEditor.setText(targetPackage);
                }
            }
        }

        if (!cmp) {
            // change text of named query/finder checkbox
            Mnemonics.setLocalizedText(generateFinderMethodsCheckBox,
                    NbBundle.getMessage(EntityClassesPanel.class, "TXT_GenerateNamedQueryAnnotations"));
            // hide local interface checkbox
            cmpFieldsInInterfaceCheckBox.setVisible(false);
        }

        if (cmp) {
            classNamesLabel.setVisible(false);
            classNamesScrollPane.setVisible(false);
            spacerPanel.setVisible(false);

            Mnemonics.setLocalizedText(specifyNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SpecifyBeansLocation"));
        }

        updatePersistenceUnitButton();
    }

    public void update(TableClosure tableClosure, String tableSourceName) {
        try {
            if (selectedTables == null) {
                selectedTables = new SelectedTables(persistenceGen, tableClosure, getLocationValue(), getPackageName());
                selectedTables.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent event) {
                        changeSupport.fireChange();
                    }
                });
            } else {
                selectedTables.setTableClosureAndTargetFolder(tableClosure, getLocationValue(), getPackageName());
            }
            selectedTables.ensureUniqueClassNames();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        TableUISupport.connectClassNames(classNamesTable, selectedTables);
        this.tableSourceName = tableSourceName;
    }

    public SelectedTables getSelectedTables() {
        return selectedTables;
    }

    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getPackageName() {
        return packageComboBoxEditor.getText();
    }

    public boolean getCmpFieldsInInterface() {
        return cmpFieldsInInterfaceCheckBox.isSelected();
    }

    public boolean getGenerateFinderMethods() {
        return generateFinderMethodsCheckBox.isSelected();
    }

    public PersistenceUnit getPersistenceUnit() {
        return persistenceUnit;
    }

    private void locationChanged() {
        updatePackageComboBox();
        updateSelectedTables();
        changeSupport.fireChange();
    }

    private void packageChanged() {
        updateSelectedTables();
        changeSupport.fireChange();
    }

    private void updatePackageComboBox() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            packageComboBox.setModel(model);
        }
    }

    private void updatePersistenceUnitButton() {
        String warning = " ";
        try{

            boolean showWarning = !cmp
                    && !ProviderUtil.persistenceExists(project)
                    && getPersistenceUnit() == null;

            createPUButton.setVisible(showWarning);

            if (showWarning) {
                warning = NbBundle.getMessage(EntityClassesPanel.class, "ERR_NoPersistenceUnit");
            }

        } catch (InvalidPersistenceXmlException ipx){
            createPUButton.setVisible(false);
            warning = NbBundle.getMessage(EntityClassesPanel.class, "ERR_InvalidPersistenceUnit", ipx.getPath());
        }

        createPUWarningLabel.setText(warning);
        createPUWarningLabel.setToolTipText(warning);
    }

    private void updateSelectedTables() {
        if (selectedTables != null) {
            try {
                selectedTables.setTargetFolder(getLocationValue(), getPackageName());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public void doLayout() {
        // preventing the PU warning label from enlarging the wizard
        Dimension size = createPUWarningLabel.getPreferredSize();
        size.width = getWidth();
        createPUWarningLabel.setPreferredSize(size);

        super.doLayout();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        specifyNamesLabel = new javax.swing.JLabel();
        classNamesLabel = new javax.swing.JLabel();
        classNamesScrollPane = new javax.swing.JScrollPane();
        classNamesTable = new javax.swing.JTable();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        createPUButton = new javax.swing.JButton();
        generateFinderMethodsCheckBox = new javax.swing.JCheckBox();
        cmpFieldsInInterfaceCheckBox = new javax.swing.JCheckBox();
        spacerPanel = new javax.swing.JPanel();
        createPUWarningLabel = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_EntityClasses"));
        org.openide.awt.Mnemonics.setLocalizedText(specifyNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SpecifyEntityClassNames"));

        org.openide.awt.Mnemonics.setLocalizedText(classNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_ClassNames"));

        classNamesScrollPane.setViewportView(classNamesTable);

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_Project"));

        projectTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SrcLocation"));

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_Package"));

        packageComboBox.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(createPUButton, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_CreatePersistenceUnit"));
        createPUButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPUButtonActionPerformed(evt);
            }
        });

        generateFinderMethodsCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateFinderMethodsCheckBox, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "TXT_GenerateFinderMethods"));
        generateFinderMethodsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        generateFinderMethodsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cmpFieldsInInterfaceCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cmpFieldsInInterfaceCheckBox, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "TXT_AddFieldsToInterface"));
        cmpFieldsInInterfaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        cmpFieldsInInterfaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout spacerPanelLayout = new org.jdesktop.layout.GroupLayout(spacerPanel);
        spacerPanel.setLayout(spacerPanelLayout);
        spacerPanelLayout.setHorizontalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 388, Short.MAX_VALUE)
        );
        spacerPanelLayout.setVerticalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 9, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(createPUWarningLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(specifyNamesLabel)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNamesLabel)
                    .add(projectLabel)
                    .add(locationLabel)
                    .add(packageLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spacerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(packageComboBox, 0, 388, Short.MAX_VALUE)
                    .add(locationComboBox, 0, 388, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .add(classNamesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)))
            .add(cmpFieldsInInterfaceCheckBox)
            .add(generateFinderMethodsCheckBox)
            .add(createPUButton)
            .add(createPUWarningLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(specifyNamesLabel)
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNamesLabel)
                    .add(classNamesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spacerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(locationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(packageLabel))
                .add(21, 21, 21)
                .add(generateFinderMethodsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmpFieldsInInterfaceCheckBox)
                .add(21, 21, 21)
                .add(createPUWarningLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createPUButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void createPUButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPUButtonActionPerformed
        persistenceUnit = Util.buildPersistenceUnitUsingWizard(project, tableSourceName, TableGeneration.NONE);
        if (persistenceUnit != null){
            updatePersistenceUnitButton();
            changeSupport.fireChange();
        }
    }//GEN-LAST:event_createPUButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNamesLabel;
    private javax.swing.JScrollPane classNamesScrollPane;
    private javax.swing.JTable classNamesTable;
    private javax.swing.JCheckBox cmpFieldsInInterfaceCheckBox;
    private javax.swing.JButton createPUButton;
    private javax.swing.JLabel createPUWarningLabel;
    private javax.swing.JCheckBox generateFinderMethodsCheckBox;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JPanel spacerPanel;
    private javax.swing.JLabel specifyNamesLabel;
    // End of variables declaration//GEN-END:variables

    public static final class WizardPanel implements WizardDescriptor.Panel, ChangeListener {

        private EntityClassesPanel component;
        private boolean componentInitialized;

        private WizardDescriptor wizardDescriptor;
        private Project project;
        private boolean cmp;

        private ChangeSupport changeSupport = new ChangeSupport(this);

        public EntityClassesPanel getComponent() {
            if (component == null) {
                component = new EntityClassesPanel();
                component.addChangeListener(this);
            }
            return component;
        }

        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        public HelpCtx getHelp() {
            if (cmp) {
                return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp." + EntityClassesPanel.class.getSimpleName()); // NOI18N
            } else {
                return new HelpCtx(EntityClassesPanel.class);
            }
        }

        public void readSettings(Object settings) {
            wizardDescriptor = (WizardDescriptor)settings;
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

            if (!componentInitialized) {
                componentInitialized = true;

                PersistenceGenerator persistenceGen = helper.getPersistenceGenerator();
                project = Templates.getProject(wizardDescriptor);
                cmp = RelatedCMPWizard.isCMP(wizardDescriptor);
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);

                getComponent().initialize(persistenceGen, project, cmp, targetFolder);
            }

            TableSource tableSource = helper.getTableSource();
            String tableSourceName = null;
            if (tableSource != null) {
                // the name of the table source is only relevant if the source
                // was a data source of connection, since it will be sent to the
                // persistence unit panel, which only deals with data sources
                // or connections
                TableSource.Type tableSourceType = tableSource.getType();
                if (tableSourceType == TableSource.Type.DATA_SOURCE || tableSourceType == TableSource.Type.CONNECTION) {
                    tableSourceName = tableSource.getName();
                }
            }

            getComponent().update(helper.getTableClosure(), tableSourceName);
        }

        public boolean isValid() {
            SourceGroup sourceGroup = getComponent().getLocationValue();
            if (sourceGroup == null) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_SelectSourceGroup"));
                return false;
            }

            String packageName = getComponent().getPackageName();
            if (packageName.trim().equals("")) { // NOI18N
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_CantUseDefaultPackage"));
                return false;
            }

            if (!SourceGroupSupport.isValidPackageName(packageName)) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class,"ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
                return false;
            }

            if (!SourceGroupSupport.isFolderWritable(sourceGroup, packageName)) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
                return false;
            }

            // issue 92192: need to check that we will have a persistence provider
            // available to add to the classpath while generating entity classes (unless
            // the classpath already contains one)
            ClassPath classPath = null;
            try {
                FileObject packageFO = SourceGroupSupport.getFolderForPackage(sourceGroup, packageName, false);
                if (packageFO == null) {
                    packageFO = sourceGroup.getRootFolder();
                }
                classPath = ClassPath.getClassPath(packageFO, ClassPath.COMPILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            if (classPath != null) {
                if (classPath.findResource("javax/persistence/EntityManager.class") == null && // NOI18N
                        PersistenceLibrarySupport.getProvidersFromLibraries().size() == 0) {
                    setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_NoJavaPersistenceAPI")); // NOI18N
                    return false;
                }
            } else {
                LOGGER.warning("Cannot get a classpath for package " + packageName + " in " + sourceGroup); // NOI18N
            }

            SelectedTables selectedTables = getComponent().getSelectedTables();
            // check for null needed since isValid() can be called when
            // EntityClassesPanel.update() has not been called yet, e.g. from within
            // EntityClassesPanel.initialize()
            if (selectedTables != null) {
                String problem = selectedTables.getFirstProblemDisplayName();
                if (problem != null) {
                    setErrorMessage(problem);
                    return false;
                }
            }

            setErrorMessage(" "); // NOI18N
            return true;
        }

        public void storeSettings(Object settings) {
            Object buttonPressed = ((WizardDescriptor)settings).getValue();
            if (buttonPressed.equals(WizardDescriptor.NEXT_OPTION) ||
                    buttonPressed.equals(WizardDescriptor.FINISH_OPTION)) {

                RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

                helper.setSelectedTables(getComponent().getSelectedTables());
                helper.setLocation(getComponent().getLocationValue());
                helper.setPackageName(getComponent().getPackageName());
                helper.setCmpFieldsInInterface(getComponent().getCmpFieldsInInterface());
                helper.setGenerateFinderMethods(getComponent().getGenerateFinderMethods());
                helper.setPersistenceUnit(getComponent().getPersistenceUnit());
            }
        }

        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange();
        }

        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
        }
    }
}
