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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.cnd.makewizard;

import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import org.netbeans.modules.cnd.api.utils.IpeUtils;

/**
 * Create the standard libraries panel in the Makefile wizard.
 */

public class StandardLibsPanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -1354448784992649011L;

    // the fields in the panel...
    private JLabel libsLabel;

    private JPanel mainPanel;
    private JPanel currentCheckBoxPanel;
    private JPanel solarisCheckBoxPanel;
    private JPanel linuxCheckBoxPanel;
    private JPanel macosxCheckBoxPanel;

    private JCheckBox[]	solarisCheckBoxes;
    private JCheckBox[]	linuxCheckBoxes;
    private JCheckBox[]	macosxCheckBoxes;

    private JComboBox linkModeCB;

    /** Store the target key */
    private int		key;

    private boolean	    initialized;


    /**
     * Constructor for the Standard Libs panel.
     */
    public StandardLibsPanel(MakefileWizard wd) {
	super(wd);
	String subtitle = new String(getString("LBL_StandardLibsPanel")); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
        GridBagConstraints gridBagConstraints;
        setLayout(new java.awt.GridBagLayout());

	JPanel panel = new javax.swing.JPanel();
	mainPanel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        libsLabel = new JLabel(getString("LBL_StdLibs"));		// NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(libsLabel, gridBagConstraints);

	// checkboxes will be added dynamically in addNotify

	JPanel linkPanel = new JPanel();
        linkPanel.setLayout(new java.awt.GridBagLayout());
	//linkPanel.setLayout(new FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
	JLabel linkLabel = new JLabel(getString("LBL_LinkMode"));
	linkLabel.setDisplayedMnemonic(getString("MNEM_LinkMode").charAt(0)); // NOI18N
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
	linkPanel.add(linkLabel, gridBagConstraints);
	//linkPanel.add(linkLabel);
	linkModeCB = new JComboBox();
	linkLabel.setLabelFor(linkModeCB);
	gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridx = 1;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
	gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
	linkPanel.add(linkModeCB, gridBagConstraints);
	//linkPanel.add(linkModeCB);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(linkPanel, gridBagConstraints);

        panel.add(mainPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                
        add(scrollPane, gridBagConstraints);
    }

    /**
     * Will add the checkbox panel to the main panel
     */
    private void addCheckBoxPanel(JPanel checkBoxPanel) {
        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        mainPanel.add(checkBoxPanel, gridBagConstraints);
    }

    /**
     * Will remove the checkbox panel from the main panel
     */
    private void removeCheckBoxPanel(JPanel checkBoxPanel) {
	if (checkBoxPanel != null)
	    mainPanel.remove(checkBoxPanel);
    }

    /**
     * Returns a platform dependent checkbox panel 
     */
    private JPanel getCheckBoxPanel() {
	JPanel p;
	if (getMakefileData().getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
	    if (solarisCheckBoxPanel == null) {
		StdLib[] stdLibs = getMakefileData().getCurrentTarget().getStdLibFlags().getSolarisStdLibs();
		solarisCheckBoxes =  new JCheckBox[stdLibs.length];
		solarisCheckBoxPanel = constructCheckBoxPanel(stdLibs, solarisCheckBoxes);
	    }
	    p = solarisCheckBoxPanel;
	}
	else if (getMakefileData().getMakefileOS() == MakefileData.MACOSX_OS_TYPE) {
	    if (macosxCheckBoxPanel == null) {
		StdLib[] stdLibs = getMakefileData().getCurrentTarget().getStdLibFlags().getMacOSXStdLibs();
		macosxCheckBoxes =  new JCheckBox[stdLibs.length];
		macosxCheckBoxPanel = constructCheckBoxPanel(stdLibs, macosxCheckBoxes);
	    }
	    p = macosxCheckBoxPanel;
	}
	else {
	    if (linuxCheckBoxPanel == null) {
		StdLib[] stdLibs = getMakefileData().getCurrentTarget().getStdLibFlags().getLinuxStdLibs();
		linuxCheckBoxes =  new JCheckBox[stdLibs.length];
		linuxCheckBoxPanel = constructCheckBoxPanel(stdLibs, linuxCheckBoxes);
	    }
	    p = linuxCheckBoxPanel;
	}
	return p;
    }

    /**
     * Returns platform dependent checkboxes
     */
    private JCheckBox[] getCheckBoxes() {
	if (getMakefileData().getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
	    return solarisCheckBoxes;
	}
        else if (getMakefileData().getMakefileOS() == MakefileData.MACOSX_OS_TYPE) {
	    return macosxCheckBoxes;
	}
	else {
	    return linuxCheckBoxes;
	}
    }

    /**
     * Constructs the checkbox panel
     */
    private JPanel constructCheckBoxPanel(StdLib[] stdLibs, JCheckBox[] checkBoxes) {
	JPanel panel = new JPanel();
	panel.setLayout(new java.awt.GridBagLayout());
	GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
	gridBagConstraints.gridx = 0;
	gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
	gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
	for (int i = 0; i < stdLibs.length; i++) {
	    checkBoxes[i] = new JCheckBox(stdLibs[i].getName());		// NOI18N
	    checkBoxes[i].setMnemonic(stdLibs[i].getMnemonic());	// NOI18N
	    panel.add(checkBoxes[i], gridBagConstraints);
	}
	return panel;
    }


    /** Create the widgets if not already created */
    public void addNotify() {
	TargetData target = getMakefileData().getCurrentTarget();
	StdLibFlags stdLibFlags = target.getStdLibFlags();
	StdLib[] stdLibs;
	if (getMakefileData().getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
	    stdLibs = stdLibFlags.getSolarisStdLibs();
	}
        else if (getMakefileData().getMakefileOS() == MakefileData.MACOSX_OS_TYPE) {
	    stdLibs = stdLibFlags.getMacOSXStdLibs();
	}
	else {
	    stdLibs = stdLibFlags.getLinuxStdLibs();
	}
	key = target.getKey();

	if (!initialized) {
	    create();
	    initialized = true;
	}

	// Update checkbox panel dynamically depending on platform
	if (getCheckBoxPanel() != currentCheckBoxPanel) {
	    removeCheckBoxPanel(currentCheckBoxPanel);
	    currentCheckBoxPanel = getCheckBoxPanel();
	    addCheckBoxPanel(currentCheckBoxPanel);
	}
	IpeUtils.requestFocus(getCheckBoxes()[0]);

	// Preset certain libs if x-designer
	if (target.containsXdFiles()) {
	    stdLibFlags.motif.setUsed(true);
	    stdLibFlags.socketnsl.setUsed(true);
	    stdLibFlags.genlib.setUsed(true);
	}

	// Set checkboxes according to data
	for (int i = 0; i < stdLibs.length; i++) {
	    getCheckBoxes()[i].setSelected(stdLibs[i].isUsed());
	}

	// Link Mode
	linkModeCB.removeAllItems();
	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    linkModeCB.addItem(getString("CB_Static_Sun")); // NOI18N
	    linkModeCB.addItem(getString("CB_Dynamic_Sun")); // NOI18N
	}
	else {
	    linkModeCB.addItem(getString("CB_Static_GNU")); // NOI18N
	    linkModeCB.addItem(getString("CB_Dynamic_GNU")); // NOI18N
	}
	linkModeCB.setSelectedIndex(stdLibFlags.getLinkType());

	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	TargetData target = getMakefileData().getTarget(key);
	StdLibFlags stdLibFlags = target.getStdLibFlags();

	StdLib[] stdLibs;
	if (getMakefileData().getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
	    stdLibs = stdLibFlags.getSolarisStdLibs();
	}
        else if (getMakefileData().getMakefileOS() == MakefileData.MACOSX_OS_TYPE) {
	    stdLibs = stdLibFlags.getMacOSXStdLibs();
	}
	else {
	    stdLibs = stdLibFlags.getLinuxStdLibs();
	}
	for (int i = 0; i < stdLibs.length; i++) {
	    stdLibs[i].setUsed(getCheckBoxes()[i].isSelected());
	}

	// Link Mode
	stdLibFlags.setLinkType(linkModeCB.getSelectedIndex());
    }
}

