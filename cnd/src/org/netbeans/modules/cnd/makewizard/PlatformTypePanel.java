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
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

public class PlatformTypePanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -8937832625633215976L;

    // the fields in the first panel...
    private boolean	    initialized;
    private JPanel panel2;
    private JTextArea message1;
    private JTextArea message2;
    private JLabel makefileTypeLabel;
    private ButtonGroup compilerButtonGroup;
    private JRadioButton sunCollectionRadioButton;
    private JRadioButton gnuCollectionRadioButton;
    private JLabel makefilePlatformLabel;
    private ButtonGroup platformButtonGroup;
    private JRadioButton solarisRadioButton;
    private JRadioButton linuxRadioButton;
    private JRadioButton windowsRadioButton;

    /**
     * Constructor for the Makefile name panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    PlatformTypePanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_PlatformTypePanel"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
        setLayout(new GridBagLayout());
	GridBagConstraints gridBagConstraints;
	JPanel panel = new JPanel(new GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
	add(panel, gridBagConstraints);

	panel2 = new JPanel();
        message1 = new JTextArea();
	makefileTypeLabel = new JLabel();
	compilerButtonGroup = new ButtonGroup();
        sunCollectionRadioButton = new JRadioButton();
        gnuCollectionRadioButton = new JRadioButton();
        message2 = new JTextArea();
        makefilePlatformLabel = new JLabel();
	platformButtonGroup = new ButtonGroup();
        solarisRadioButton = new JRadioButton();
        linuxRadioButton = new JRadioButton();
        windowsRadioButton = new JRadioButton();
        panel2.setLayout(new java.awt.GridBagLayout());

        makefileTypeLabel.setText(getString("LBL_CompilerType"));		    // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(makefileTypeLabel, gridBagConstraints);

        sunCollectionRadioButton.setText(getString("RB_CompilerTypeSun"));		    // NOI18N
	sunCollectionRadioButton.setMnemonic(getString("MNEM_CompilerTypeSun").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(sunCollectionRadioButton, gridBagConstraints);

        gnuCollectionRadioButton.setText(getString("RB_CompilerTypeGNU"));		    // NOI18N
	gnuCollectionRadioButton.setMnemonic(getString("MNEM_CompilerTypeGNU").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(gnuCollectionRadioButton, gridBagConstraints);

        message1.setBackground(panel.getBackground());
        message1.setEditable(false);
        message1.setLineWrap(true);
        message1.setWrapStyleWord(true);
        message1.setFocusable(false);
        message1.setText(getString("TXT_PlatformTypeMsg1")); // NOI18N
        message1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel2.add(message1, gridBagConstraints);

        makefilePlatformLabel.setText(getString("LBL_PlatformType"));		    // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(makefilePlatformLabel, gridBagConstraints);

        solarisRadioButton.setText(getString("RB_PlatformTypeSolaric"));		    // NOI18N
	solarisRadioButton.setMnemonic(getString("MNEM_PlatformTypeSolaric").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(solarisRadioButton, gridBagConstraints);

        linuxRadioButton.setText(getString("RB_PlatformTypeLinux"));		    // NOI18N
	linuxRadioButton.setMnemonic(getString("MNEM_PlatformTypeLinux").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(linuxRadioButton, gridBagConstraints);

        windowsRadioButton.setText(getString("RB_PlatformTypeWindows"));		    // NOI18N
	windowsRadioButton.setMnemonic(getString("MNEM_PlatformTypeWindows").charAt(0));  // NOI18N
	windowsRadioButton.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
		    if (sunCollectionRadioButton.isSelected()) {
			gnuCollectionRadioButton.setSelected(true);
		    }
		    sunCollectionRadioButton.setEnabled(false);
		} else {
		    sunCollectionRadioButton.setEnabled(true);
		}
	    }
	});
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(windowsRadioButton, gridBagConstraints);

        message2.setBackground(panel.getBackground());
        message2.setEditable(false);
        message2.setLineWrap(true);
        message2.setWrapStyleWord(true);
        message2.setFocusable(false);
        message2.setText(getString("TXT_PlatformTypeMsg2")); // NOI18N
        message2.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel2.add(message2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel.add(panel2, gridBagConstraints);

	// Create button groups and the buttons ...
	compilerButtonGroup.add(sunCollectionRadioButton);
	compilerButtonGroup.add(gnuCollectionRadioButton);

	platformButtonGroup.add(solarisRadioButton);
	platformButtonGroup.add(linuxRadioButton);
	platformButtonGroup.add(windowsRadioButton);
    }


    /** Create the widgets if not initialized. Also set the RadioButtons */
    public void addNotify() {
	if (!initialized) {
	    create();
	    initialized = true;

	    // Check OS. If Solaris, choose Sun as default platform....
	    String osname = System.getProperty("os.name"); // NOI18N
            osname = osname.toLowerCase();
            if (osname.indexOf("sunos") >= 0) { // NOI18N
		getMakefileData().setToolset(MakefileData.SUN_TOOLSET_TYPE);
		getMakefileData().setMakefileOS(MakefileData.SOLARIS_OS_TYPE);
	    } else if (osname.indexOf("linux") >= 0) { // NOI18N
		getMakefileData().setToolset(MakefileData.GNU_TOOLSET_TYPE);
		getMakefileData().setMakefileOS(MakefileData.LINUX_OS_TYPE);
	    } else if (osname.startsWith("windows")) { // NOI18N
		getMakefileData().setToolset(MakefileData.GNU_TOOLSET_TYPE);
		getMakefileData().setMakefileOS(MakefileData.WINDOWS_OS_TYPE);
	    } else {
		getMakefileData().setToolset(MakefileData.GNU_TOOLSET_TYPE);
		getMakefileData().setMakefileOS(MakefileData.LINUX_OS_TYPE);
	    }
	}

	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    sunCollectionRadioButton.setSelected(true);
	} else if (getMakefileData().getToolset() == MakefileData.GNU_TOOLSET_TYPE) {
	    gnuCollectionRadioButton.setSelected(true);
	}

	if (getMakefileData().getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
	    solarisRadioButton.setSelected(true);
	} else if (getMakefileData().getMakefileOS() == MakefileData.LINUX_OS_TYPE) {
	    linuxRadioButton.setSelected(true);
	} else if (getMakefileData().getMakefileOS() == MakefileData.WINDOWS_OS_TYPE) {
	    windowsRadioButton.setSelected(true);
	}

	super.addNotify();
	sunCollectionRadioButton.requestFocus();
    }


    public void removeNotify() {
	super.removeNotify();
	if (sunCollectionRadioButton.isSelected()) {
	    getMakefileData().setToolset(MakefileData.SUN_TOOLSET_TYPE);
	} else if (gnuCollectionRadioButton.isSelected()) {
	    getMakefileData().setToolset(MakefileData.GNU_TOOLSET_TYPE);
	}

	if (solarisRadioButton.isSelected()) {
	    getMakefileData().setMakefileOS(MakefileData.SOLARIS_OS_TYPE);
	} else if (linuxRadioButton.isSelected()) {
	    getMakefileData().setMakefileOS(MakefileData.LINUX_OS_TYPE);
	} else if (windowsRadioButton.isSelected()) {
	    getMakefileData().setMakefileOS(MakefileData.WINDOWS_OS_TYPE);
	}
    }

}
