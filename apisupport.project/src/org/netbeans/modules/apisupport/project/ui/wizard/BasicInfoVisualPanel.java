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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.ui.wizard.spi.ModuleTypePanel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
public class BasicInfoVisualPanel extends BasicVisualPanel.NewTemplatePanel
        implements PropertyChangeListener
{
    
    private boolean locationUpdated;
    private boolean nameUpdated;
    private boolean mainProjectTouched;
    
    /** Creates new form BasicInfoVisualPanel */
    BasicInfoVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        initPlatformCombo(suitePlatformValue);
        initPanels();
        setComponentsVisibility();
        switch (data.getWizardType()) {
            case SUITE:
            case APPLICATION:
            case LIBRARY_MODULE:
                break;
            case MODULE:
            case SUITE_COMPONENT:
                if (TypeChooserPanelImpl.getDefaultSuitesComboBox().getItemCount() > 0) {
                    mainProject.setSelected(false);
                }
                break;
            default:
                assert false : "Unknown wizard type = " + data.getWizardType();
        }
        attachDocumentListeners();
        setInitialLocation();
        setInitialProjectName();
        // temporary fix for SUITE_COMPONENT wizard type.
        putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
    }
    
    protected static boolean isNetBeansOrgFolder(File folder) {
        return ModuleList.findNetBeansOrg(folder) != null;
    }
    
    protected static void initPlatformCombo(JComboBox combo) {
        if (combo == null){
            return;
        }
        if (combo.getItemCount() <= 0) {
            return;
        }
        boolean set = false;
        String idToSelect = ModuleUISettings.getDefault().getLastUsedPlatformID();
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (((NbPlatform) combo.getItemAt(i)).getID().equals(idToSelect)) {
                combo.setSelectedIndex(i);
                set = true;
                break;
            }
        }
        if (!set) {
            NbPlatform defPlaf = NbPlatform.getDefaultPlatform();
            combo.setSelectedItem(defPlaf == null ? combo.getItemAt(0) : defPlaf);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ModuleTypePanel.isPanelUpdated(evt)){
            moduleTypePanelUpdated();
        }
    }
    
    private void moduleTypePanelUpdated() {
        boolean isStandAlone = ModuleTypePanel.isStandalone(getSettings());
        boolean isSuiteComponent = ModuleTypePanel.isSuiteComponent(getSettings());
        // both radio buttons are deselected and disaled
        if (!isStandAlone && !isSuiteComponent){
            return;
        }
        
        if (!mainProjectTouched) {
            mainProject.setSelected(isStandAlone);
        }
        if (!locationUpdated) {
            setInitialLocation();
        }
        if (!nameUpdated) {
            setInitialProjectName();
        }
        updateAndCheck();
    }
    
    private void setInitialLocation() {
        if (ModuleTypePanel.isSuiteComponent(getSettings())) {
            computeAndSetLocation(ModuleTypePanel.getSuiteRoot(getSettings()), true);
        } else { // suite or standalone module
            String location = computeLocationValue(ProjectChooser.getProjectsFolder().getAbsolutePath());
            File locationF = new File(location);
            if (SuiteUtils.isSuite(locationF)) {
                computeAndSetLocation(locationF.getParent(), true);
            } else {
                setLocation(location, true);
            }
        }
    }
    
    private void initPanels(){
        if (typeChooserPanel != null){
            typeChooserPanelContainer.remove(typeChooserPanel);
            typeChooserPanel = null;
        }
        typeChooserPanel = new TypeChooserPanelImpl(getSettings(), getData().getWizardType());
        typeChooserPanelContainer.add(typeChooserPanel);
        typeChooserPanelContainer.validate();
        validate();
    }

    private void setComponentsVisibility() {
        boolean isSuiteWizard = isSuiteWizard();
        boolean isSuiteComponentWizard = isSuiteComponentWizard();
        boolean isLibraryWizard = isLibraryWizard();
        
        typeChooserPanel.setVisible(!isSuiteWizard);
        suitePlatform.setVisible(isSuiteWizard);
        suitePlatformValue.setVisible(isSuiteWizard);
        manageSuitePlatform.setVisible(isSuiteWizard);
        mainProject.setVisible(!isLibraryWizard);
        
        mainProject.setSelected(!isLibraryWizard);
        
        if (typeChooserPanel != null){
            typeChooserPanel.setComponentsVisibility(isSuiteComponentWizard, isLibraryWizard);
        }
    }
    
    private String getNameValue() {
        return nameValue.getText().trim();
    }
    
    private String getLocationValue() {
        return locationValue.getText().trim();
    }
    
    private File getLocationFile() {
        return new File(getLocationValue());
    }
    
    void updateAndCheck() {
        updateGUI();
        
        if ("".equals(getNameValue())) {
            setInfo(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_NameCannotBeEmpty"), false);//NOI18N
        } else if ("".equals(getLocationValue())) {
            setInfo(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_LocationCannotBeEmpty"), false);//NOI18N
        } else if (isLibraryWizard() && isNetBeansOrgFolder()) {
            setError(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_LibraryWrapperForNBOrgUnsupported"));//NOI18N
        } else if (!ModuleTypePanel.validate(getSettings())) {
            setError((String)getSettings().getProperty(WizardDescriptor.PROP_ERROR_MESSAGE));
        } else if (isSuiteWizard() &&
                (suitePlatformValue.getSelectedItem() == null || !((NbPlatform) suitePlatformValue.getSelectedItem()).isValid())) 
        {
            setError(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_ChosenPlatformIsInvalid"));//NOI18N
        } else if (getFolder().exists()) {
            setError(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_ProjectFolderExists"));//NOI18N
        } else if (!getLocationFile().exists()) {
            setError(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_LocationMustExist"));//NOI18N
        } else if (!getLocationFile().canWrite()) {
            setError(NbBundle.getMessage(
                    BasicInfoVisualPanel.class, "MSG_LocationNotWritable"));//NOI18N
        } else {
            markValid();
        }
    }
    
    private void updateGUI() {
        // update project folder
        folderValue.setText(getFolder().getPath());

        ModuleTypePanel.setProjectFolder(getSettings(), getFolder());
    }
    
    /** Set <em>next</em> free project name. */
    private void setProjectName(String formater, int counter) {
        String name;
        while ((name = validFreeModuleName(formater, counter)) == null) {
            counter++;
        }
        nameValue.setText(name);
    }
    
    // stolen (then adjusted) from j2seproject
    private String validFreeModuleName(String formater, int index) {
        String name = MessageFormat.format(formater, new Object[]{ new Integer(index) });
        File file = new File(getLocationValue(), name);
        return file.exists() ? null : name;
    }
    
    /** Stores collected data into model. */
    void storeData() {
        getData().setProjectName(getNameValue());
        getData().setProjectLocation(getLocationValue());
        getData().setProjectFolder(folderValue.getText());
        getData().setMainProject(mainProject.isSelected());
        getData().setNetBeansOrg(isNetBeansOrgFolder());
        getData().setStandalone(ModuleTypePanel.isStandalone(getSettings()));
        getData().setSuiteRoot(ModuleTypePanel.getSuiteRoot(getSettings()));
        if (isSuiteWizard() && suitePlatformValue.getSelectedItem() != null) {
            getData().setPlatformID(((NbPlatform) suitePlatformValue.getSelectedItem()).getID());
        } else if (ModuleTypePanel.getActivePlatformId(getSettings()) != null) {
            getData().setPlatformID(ModuleTypePanel.getActivePlatformId(getSettings()));
        }
    }
    
    void refreshData() {
        if (getData().getProjectName() != null) {
            nameValue.setText(getData().getProjectName());
        } else {
            setInitialProjectName();
        }
    }
    
    private void setInitialProjectName() {
        String bundlekey = null;
        int counter = 0;
        switch (getData().getWizardType()) {
            case SUITE:
            case APPLICATION:
                counter = ModuleUISettings.getDefault().getNewSuiteCounter() + 1;
                bundlekey = "TXT_Suite"; //NOI18N
                getData().setSuiteCounter(counter);
                break;
            case MODULE:
            case SUITE_COMPONENT:
                counter = ModuleUISettings.getDefault().getNewModuleCounter() + 1;
                bundlekey = "TXT_Module"; //NOI18N
                getData().setModuleCounter(counter);
                break;
            case LIBRARY_MODULE:
                counter = ModuleUISettings.getDefault().getNewModuleCounter() + 1;
                bundlekey = "TXT_Library"; //NOI18N
                getData().setModuleCounter(counter);
                break;
            default:
                assert false : "Unknown wizard type = " + getData().getWizardType();
        }
        setProjectName(NbBundle.getMessage(BasicInfoVisualPanel.class, bundlekey), counter);
        nameValue.select(0, nameValue.getText().length());
        nameUpdated = false;
    }
    
    private void attachDocumentListeners() {
        DocumentListener fieldsDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { updateAndCheck(); }
        };
        nameValue.getDocument().addDocumentListener(fieldsDL);
        nameValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { nameUpdated = true; }
        });
        locationValue.getDocument().addDocumentListener(fieldsDL);
        locationValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { locationUpdated = true; }
        });
        ActionListener plafAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAndCheck();
            }
        };
        suitePlatformValue.addActionListener(plafAL);
        getSettings().addPropertyChangeListener(this);
    }
    
    private File getFolder() {
        StringBuffer destFolder = new StringBuffer(getLocationValue());
        if (destFolder.length() != 0) {
            destFolder.append(File.separator);
        }
        destFolder.append(getNameValue());
        return FileUtil.normalizeFile(new File(destFolder.toString()));
    }
    
    private boolean isNetBeansOrgFolder() {
        return isNetBeansOrgFolder(getFolder());
    }
    
    private void setLocation(String location, boolean silently) {
        boolean revert = silently && !locationUpdated;
        locationValue.setText(location);
        locationUpdated = revert ^ true;
    }
    
    private void computeAndSetLocation(String value, boolean silently) {
        setLocation(computeLocationValue(value), silently);
    }
    
    private String computeLocationValue(String value) {
        if (value == null) {
            value = System.getProperty("user.home"); // NOI18N
        }
        File file = new File(value);
        if (!file.exists() && file.getParent() != null) {
            return computeLocationValue(file.getParent());
        } else {
            return file.exists() ? value : System.getProperty("user.home"); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleTypeGroup = new javax.swing.ButtonGroup();
        infoPanel = new javax.swing.JPanel();
        nameLbl = new javax.swing.JLabel();
        locationLbl = new javax.swing.JLabel();
        folderLbl = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        locationValue = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        folderValue = new javax.swing.JTextField();
        suitePlatform = new javax.swing.JLabel();
        suitePlatformValue = PlatformComponentFactory.getNbPlatformsComboxBox();
        manageSuitePlatform = new javax.swing.JButton();
        separator3 = new javax.swing.JSeparator();
        mainProject = new javax.swing.JCheckBox();
        pnlThouShaltBeholdLayout = new javax.swing.JPanel();
        typeChooserPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        nameLbl.setLabelFor(nameValue);
        org.openide.awt.Mnemonics.setLocalizedText(nameLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(nameLbl, gridBagConstraints);

        locationLbl.setLabelFor(locationValue);
        org.openide.awt.Mnemonics.setLocalizedText(locationLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectLocation")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 12);
        infoPanel.add(locationLbl, gridBagConstraints);

        folderLbl.setLabelFor(folderValue);
        org.openide.awt.Mnemonics.setLocalizedText(folderLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(folderLbl, gridBagConstraints);

        nameValue.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        infoPanel.add(nameValue, gridBagConstraints);
        nameValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_NameValue")); // NOI18N
        nameValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_NameValue")); // NOI18N

        locationValue.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        infoPanel.add(locationValue, gridBagConstraints);
        locationValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_LocationValue")); // NOI18N
        locationValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_LocationValue")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_BrowseButton_o")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocation(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        infoPanel.add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_BrowseButton")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_BrowseButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        infoPanel.add(filler, gridBagConstraints);

        folderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        infoPanel.add(folderValue, gridBagConstraints);
        folderValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_FolderValue")); // NOI18N
        folderValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_FolderValue")); // NOI18N

        suitePlatform.setLabelFor(suitePlatformValue);
        org.openide.awt.Mnemonics.setLocalizedText(suitePlatform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_NetBeansPlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 12);
        infoPanel.add(suitePlatform, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        infoPanel.add(suitePlatformValue, gridBagConstraints);
        suitePlatformValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_SuitePlatformValue")); // NOI18N
        suitePlatformValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_SuitePlatformValue")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageSuitePlatform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_ManagePlatforms_g")); // NOI18N
        manageSuitePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSuitePlatformActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        infoPanel.add(manageSuitePlatform, gridBagConstraints);
        manageSuitePlatform.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_ManageSuitePlatform")); // NOI18N
        manageSuitePlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_ManageSuitePlatform")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 0);
        infoPanel.add(separator3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(infoPanel, gridBagConstraints);

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_SetAsMainProject")); // NOI18N
        mainProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainProjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(mainProject, gridBagConstraints);
        mainProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_MainProject")); // NOI18N
        mainProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_CTL_MainProject")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlThouShaltBeholdLayout, gridBagConstraints);

        typeChooserPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(typeChooserPanelContainer, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_BasicInfoVisualPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "ACS_BasicInfoVisualPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void manageSuitePlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageSuitePlatformActionPerformed
        managePlatform(suitePlatformValue);
    }//GEN-LAST:event_manageSuitePlatformActionPerformed
    
    private void mainProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainProjectActionPerformed
        mainProjectTouched = true;
    }//GEN-LAST:event_mainProjectActionPerformed
        
    private void managePlatform(final JComboBox platformCombo) {
        NbPlatformCustomizer.showCustomizer();
        platformCombo.setModel(new PlatformComponentFactory.NbPlatformListModel()); // refresh
        platformCombo.requestFocus();
        updateAndCheck();
    }
                    
    private void browseLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocation
        JFileChooser chooser = new JFileChooser(getLocationValue());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            computeAndSetLocation(chooser.getSelectedFile().getAbsolutePath(), false);
        }
    }//GEN-LAST:event_browseLocation
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel filler;
    private javax.swing.JLabel folderLbl;
    private javax.swing.JTextField folderValue;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel locationLbl;
    private javax.swing.JTextField locationValue;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JButton manageSuitePlatform;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel nameLbl;
    javax.swing.JTextField nameValue;
    private javax.swing.JPanel pnlThouShaltBeholdLayout;
    private javax.swing.JSeparator separator3;
    private javax.swing.JLabel suitePlatform;
    private javax.swing.JComboBox suitePlatformValue;
    private javax.swing.JPanel typeChooserPanelContainer;
    // End of variables declaration//GEN-END:variables
    
    private TypeChooserPanelImpl typeChooserPanel;
}
