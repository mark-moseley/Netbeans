/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.loaders.ExeElfObject;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class RunDialogPanel extends javax.swing.JPanel {
    private DocumentListener modifiedValidateDocumentListener = null;
    private DocumentListener modifiedRunDirectoryListener = null;
    private Project[] projectChoices = null;
    //private Profiles currentProfiles = null;
    private String currentProfilesDialogTitle = null;
    private boolean executableReadOnly = true;
    private JButton actionButton;

    private static String lastSelectedExecutable = null;
    private static Project lastSelectedProject = null;

    private static DefaultPicklistModel picklist = null;
    private static String picklistHomeDir = null;
    private static final String picklistName = "executables"; // NOI18N
    
    public RunDialogPanel() {
	initialize(null, false);
    }

    public RunDialogPanel(String exePath, boolean executableReadOnly, JButton actionButton) {
	this.actionButton = actionButton;
	initialize(exePath, executableReadOnly);
    }

    protected  void initialize(String exePath, boolean executableReadOnly) {
        initComponents();
	errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
	this.executableReadOnly = executableReadOnly;
	modifiedValidateDocumentListener = new ModifiedValidateDocumentListener();
	//modifiedRunDirectoryListener = new ModifiedRunDirectoryListener();
	if (executableReadOnly) {
	    guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT_RO"));
	    executableLabel1.setVisible(false);
	    executableComboBox.setVisible(false);
	    executableBrowseButton.setVisible(false);
	    if (exePath != null)
		executableTextField.setText(exePath);
	    initGui();
	}
	else {
	    guidanceTextarea.setText(getString("DIALOG_GUIDANCETEXT"));
	    executableLabel2.setVisible(false);
	    executableTextField.setVisible(false);
	    String[] savedExePaths = getExecutablePicklist().getElementsDisplayName();
	    for (int i = 0; i < savedExePaths.length; i++)
		executableComboBox.addItem(savedExePaths[i]);
	    String feed = null;
	    if (exePath != null) {
		feed = exePath;
	    }
	    else if (savedExePaths.length > 0) {
		feed = savedExePaths[0];
	    }
	    else {
		feed = ""; // NOI18N
	    }
	    ((JTextField)executableComboBox.getEditor().getEditorComponent()).setText(feed);
	    ((JTextField)executableComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(modifiedValidateDocumentListener);
	    initGui();
	}
	//runDirectoryTextField.getDocument().addDocumentListener(modifiedRunDirectoryListener);
        guidanceTextarea.setBackground(getBackground());
	setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        guidanceTextarea = new javax.swing.JTextArea();
        executableLabel1 = new javax.swing.JLabel();
        executableComboBox = new javax.swing.JComboBox();
        executableBrowseButton = new javax.swing.JButton();
        executableLabel2 = new javax.swing.JLabel();
        executableTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();
        errorLabel = new javax.swing.JLabel();
        fill = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        guidanceTextarea.setEditable(false);
        guidanceTextarea.setLineWrap(true);
        guidanceTextarea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("DIALOG_GUIDANCETEXT"));
        guidanceTextarea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextarea, gridBagConstraints);

        executableLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel1.setLabelFor(executableComboBox);
        executableLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
        add(executableLabel1, gridBagConstraints);

        executableComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(executableComboBox, gridBagConstraints);

        executableBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("BROWSE_BUTTON_MN").charAt(0));
        executableBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("BROWSE_BUTTON_TXT"));
        executableBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executableBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(executableBrowseButton, gridBagConstraints);

        executableLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel2.setLabelFor(executableTextField);
        executableLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("EXECUTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 8, 0);
        add(executableLabel2, gridBagConstraints);

        executableTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(executableTextField, gridBagConstraints);

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ASSOCIATED_PROJECT_MN").charAt(0));
        projectLabel.setLabelFor(projectComboBox);
        projectLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("ASSOCIATED_PROJECT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
        add(projectLabel, gridBagConstraints);

        projectComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        add(projectComboBox, gridBagConstraints);

        errorLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fill, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void projectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboBoxActionPerformed
	/*
	ActionListener profileComboBoxActionListener = runProfileComboBox.getActionListeners()[0];
	runProfileComboBox.removeActionListener(profileComboBoxActionListener);
	runProfileComboBox.removeAllItems();
	int selectedIndex = projectComboBox.getSelectedIndex();
	if (selectedIndex == 0) {
	    currentProfiles = ProfileSupport.getGlobalProfiles();
	    currentProfilesDialogTitle = getString("DEFAULT_SET_TITLE");
	}
	else {
	    currentProfiles = ProfileSupport.getProfilesObject(projectChoices[selectedIndex-1]);
	    currentProfilesDialogTitle = ProjectUtils.getInformation(projectChoices[selectedIndex-1]).getDisplayName();
	}

	for (int i = 0; i < currentProfiles.getProfiles().length; i++) {
	    runProfileComboBox.addItem(currentProfiles.getProfiles()[i]);
	}
	runProfileComboBox.setSelectedIndex(currentProfiles.getDefaultAsIndex());
	runProfileComboBox.addActionListener(profileComboBoxActionListener);
	runProfileComboBoxActionPerformed(null); // Updates args and rundir fielsd
	*/
    }//GEN-LAST:event_projectComboBoxActionPerformed

    private void executableBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executableBrowseButtonActionPerformed
	String startFolder = getExecutablePath();
	// Show the file chooser
	FileChooser fileChooser = new FileChooser(
	    getString("SelectExecutable"),
	    getString("SelectLabel"),
	    FileChooser.FILES_ONLY,
	    new FileFilter[] {ElfExecutableFileFilter.getInstance()},
	    startFolder,
	    false
	    );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == FileChooser.CANCEL_OPTION)
            return;
	((JTextField)executableComboBox.getEditor().getEditorComponent()).setText(fileChooser.getSelectedFile().getPath());
    }//GEN-LAST:event_executableBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton executableBrowseButton;
    private javax.swing.JComboBox executableComboBox;
    private javax.swing.JLabel executableLabel1;
    private javax.swing.JLabel executableLabel2;
    private javax.swing.JTextField executableTextField;
    private javax.swing.JPanel fill;
    private javax.swing.JTextArea guidanceTextarea;
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    private void initGui() {
	projectChoices = OpenProjects.getDefault().getOpenProjects();
	ActionListener projectComboBoxActionListener = projectComboBox.getActionListeners()[0];
	projectComboBox.removeActionListener(projectComboBoxActionListener);
	projectComboBox.removeAllItems();
	projectComboBox.addItem(getString("NO_PROJECT")); // always first
	for (int i = 0; i < projectChoices.length; i++) {
	    projectComboBox.addItem(ProjectUtils.getInformation(projectChoices[i]).getName());
	}

	int index = 0;
	// preselect project ???
	if (lastSelectedExecutable != null && getExecutablePath().equals(lastSelectedExecutable) && lastSelectedProject != null) {
	    for (int i = 0; index < projectChoices.length; i++) {
		if (projectChoices[i] == lastSelectedProject) {
		    index = i+1;
		    break;
		}
	    }
	}
	projectComboBox.setSelectedIndex(index);
	projectComboBox.addActionListener(projectComboBoxActionListener);
	projectComboBoxActionPerformed(null);
	//validateRunDirectory();
    }

    private void validateExecutable() {
	clearError();
	String exePath = getExecutablePath();
	File exeFile = new File(exePath);
	if (!exeFile.exists()) {
	    setError("ERROR_DONTEXIST", true); // NOI18N
	    return;
	}
	if (exeFile.isDirectory()) {
	    setError("ERROR_NOTAEXEFILE", true); // NOI18N
	    return;
	}
	FileObject fo = FileUtil.toFileObject(exeFile);
	if (fo == null) {
	    setError("ERROR_NOTAEXEFILE", true); // NOI18N
	    return;
	}
	DataObject dataObject = null;
	try {
	    dataObject = DataObject.find(fo);
	}
	catch (Exception e) {
	    setError("ERROR_DONTEXIST", true); // NOI18N
	    return;
	}
	if (!(dataObject instanceof ExeElfObject)) {
	    setError("ERROR_NOTAEXEFILE", true); // NOI18N
	    return;
	}
	
	projectComboBox.setSelectedIndex(0);
	projectComboBoxActionPerformed(null);
    }

    /*
    private void validateRunDirectory() {
	clearError();
	if (getOutput().length() == 0) {
	    actionButton.setEnabled(true);
	    return;
	}
	String runDirectory = runDirectoryTextField.getText();
	if (runDirectory.length() == 0)
	    runDirectory = ".";
	File runDirectoryFile;
	if (runDirectory.charAt(0) != File.separatorChar)
	    runDirectoryFile = new File(IpeUtils.getDirName(getOutput()) + File.separator + runDirectory);
	else
	    runDirectoryFile = new File(runDirectory);

	if (!runDirectoryFile.exists()) {
	    setError("ERROR_RUNDIR_DONTEXIST", false);
	    return;
	}
	if (!runDirectoryFile.isDirectory()) {
	    setError("ERROR_RUNDIR_INVALID", false);
	    return;
	}
    }
    */

    private void setError(String errorMsg, boolean disable) {
	setErrorMsg(getString(errorMsg));
	if (disable) {
	    projectComboBox.setEnabled(false);
	    //runProfileComboBox.setEnabled(false);
	    //runProfileButton.setEnabled(false);
	    //runDirectoryTextField.setEnabled(false);
	    //argumentTextField.setEnabled(false);
	    //runDirectoryButton.setEnabled(false);
	}
	actionButton.setEnabled(false);
    }

    private void clearError() {
	setErrorMsg(" "); // NOI18N
        projectComboBox.setEnabled(true);
        //runProfileComboBox.setEnabled(true);
        //runProfileButton.setEnabled(true);
        //runDirectoryTextField.setEnabled(true);
        //argumentTextField.setEnabled(true);
        //runDirectoryButton.setEnabled(true);

	actionButton.setEnabled(true);
    }


    // ModifiedDocumentListener
    public class ModifiedValidateDocumentListener implements DocumentListener {
	public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
	}    
    
	public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
	    validateExecutable();
	}
    
	public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
	    validateExecutable();
	}
    }

    // ModifiedRunDirectoryListener
    /*
    public class ModifiedRunDirectoryListener implements DocumentListener {
	public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
	}    
    
	public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
	    validateRunDirectory();
	}
    
	public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
	    validateRunDirectory();
	}
    }
    */

    public Project getSelectedProject() {
	Project project;
	lastSelectedExecutable = getExecutablePath();
	if (projectComboBox.getSelectedIndex() > 0) {
	    lastSelectedProject = projectChoices[projectComboBox.getSelectedIndex()-1];
	    project = lastSelectedProject;
	}
	else {
	    try {
		String projectFolder = ProjectGenerator.getDefaultProjectFolder();
		String projectName = ProjectGenerator.getValidProjectName(projectFolder);
		String baseDir = projectFolder + File.separator + projectName;
		MakeConfiguration conf = new MakeConfiguration(baseDir, "Default", MakeConfiguration.TYPE_APPLICATION);  // NOI18N
		conf.getLinkerConfiguration().getOutput().setValue(IpeUtils.toRelativePath(baseDir, getExecutablePath())); // FIXUP: shoul;d use rel or abs path
		project = MakeProjectGenerator.createBlankProject(projectName, projectFolder, new MakeConfiguration[] {conf}, true);
	    }
	    catch (Exception e) {
		project = null;
	    }
	    lastSelectedProject = project;
	}
	return project;
    }

    public String getExecutablePath() {
	if (executableReadOnly) {
	    return executableTextField.getText();
	}
	else {
	    return ((JTextField)executableComboBox.getEditor().getEditorComponent()).getText();
	}
    }

    private void setErrorMsg(String msg) {
	errorLabel.setText(msg);
    }

    private static DefaultPicklistModel getExecutablePicklist() {
	if (picklist == null) {
	    picklistHomeDir = System.getProperty("netbeans.user") + File.separator + "var" + File.separator + "picklists"; // NOI18N
	    picklist = (DefaultPicklistModel)DefaultPicklistModel.restorePicklist(picklistHomeDir, picklistName);
	    if (picklist == null)
		picklist = new DefaultPicklistModel(16);
	}
	return picklist;
    }

    public static void addElementToExecutablePicklist(String exePath) {
	getExecutablePicklist().addElement(exePath);
	getExecutablePicklist().savePicklist(picklistHomeDir, picklistName);
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(RunDialogPanel.class);
	}
	return bundle.getString(s);
    }

    public boolean asynchronous() {
	return false;
    }
}
