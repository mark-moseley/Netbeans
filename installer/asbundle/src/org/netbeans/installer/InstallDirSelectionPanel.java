/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.util.MnemonicString;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.console.ConsoleWizardPanelImpl;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.WizardServicesUI;
import com.installshield.wizard.swing.JFlowLabel;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizardx.panels.ExtendedWizardPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class InstallDirSelectionPanel extends ExtendedWizardPanel implements ActionListener {
    
    private JTextField nbInstallDirTF;
    private JButton    nbBrowseButton;
    private JLabel     nbInputLabel;
    
    private JTextField asInstallDirTF;
    private JButton    asBrowseButton;
    private JLabel     asInputLabel;
    
    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel inputPanel;
    private JFlowLabel infoLabel;
    
    private String installedVersion = "";
    
    private String nbLabel          = null;
    private String asLabel          = null;
    
    private boolean emptyExistingDirNB = false;
    private boolean emptyExistingDirAS = false;
    
    private static final int NB_INSTALL_DIR = 1;
    private static final int AS_INSTALL_DIR = 2;
    
    private static final int AS_INSTALL_PATH_MAX_LENGTH = 48;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
    public void build(WizardBuilderSupport support) {
        super.build(support);
        try {
            support.putClass(Util.class.getName());
            support.putClass(ConsoleWizardPanelImpl.class.getName());
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public boolean queryEnter(WizardBeanEvent event) {
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String destination = (String) service.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE, null, "installLocation");
            
            if (!Util.isWindowsOS()) {
                File root = new File("/");
                if (!root.canWrite()) {
                    service.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "installLocation",
                    resolveString("$L(org.netbeans.installer.Bundle, Product.installLocationForNonRoot)"));
                }
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        
        return super.queryEnter(event);
    }
    
    public boolean entered(WizardBeanEvent event) {
        nbInstallDirTF.requestFocus();
        return true;
    }
    
    protected void initialize() {
        super.initialize();
	
        GridBagConstraints gridBagConstraints;
        mainPanel = new JPanel();        
        mainPanel.setLayout(new BorderLayout());
        
	displayPanel = new JPanel(new java.awt.BorderLayout(0,10));
	displayPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        
        infoLabel = new JFlowLabel();	
        
        String desc = resolveString(BUNDLE + "InstallLocationPanel.description,"
        + BUNDLE + "Product.displayName),"
        + BUNDLE + "AS.name))");
        
        infoLabel.setText(desc);        
        mainPanel.add(infoLabel, BorderLayout.NORTH);
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        
	//----------------------------------------------------------------------
	// netbeans install dir components
	String nbInstallDir;
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
	if (Util.isMacOSX()) {
            //Replace install dir
            nbInstallDir = nbInstallDir.substring(0,nbInstallDir.lastIndexOf(File.separator))
            + File.separator
            + resolveString(BUNDLE + "Product.installDirMacOSX)");
        }
        
	nbLabel = resolveString(BUNDLE + "InstallLocationPanel.nbInstallDirectoryLabel)");
        MnemonicString nbInputLabelMn = new MnemonicString(nbLabel);
        nbLabel = nbInputLabelMn.toString();
        nbInputLabel = new JLabel(nbLabel);
        if (nbInputLabelMn.isMnemonicSpecified()) {
            nbInputLabel.setDisplayedMnemonic(nbInputLabelMn.getMnemonicChar());
        }
	nbInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(15, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(nbInputLabel, gridBagConstraints);
        
        nbInstallDirTF = new JTextField(nbInstallDir);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.5;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        nbInputLabel.setLabelFor(nbInstallDirTF);
        inputPanel.add(nbInstallDirTF, gridBagConstraints);

        String browseButtonText = resolveString(BUNDLE + "InstallLocationPanel.nbBrowseButtonLabel)");
        MnemonicString browseMn = new MnemonicString(browseButtonText);
        browseButtonText = browseMn.toString();
        nbBrowseButton = new JButton(browseButtonText);
        if (browseMn.isMnemonicSpecified()) {
            nbBrowseButton.setMnemonic(browseMn.getMnemonicChar());
        }
	nbBrowseButton.setActionCommand("nb");
        nbBrowseButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 12, 3, 0);
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        inputPanel.add(nbBrowseButton, gridBagConstraints);
	
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 20, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        //inputPanel.add(nbListLabel, gridBagConstraints);

	//----------------------------------------------------------------------
	// SJS AS install dir components
        
	String asInstallDir;
        if (Util.isAdmin()) {
            asInstallDir = resolveString("$D(install)$J(file.separator)" + BUNDLE + "AS.defaultInstallDirectory)");
        } else {
            asInstallDir = resolveString("$D(home)$J(file.separator)" + BUNDLE + "AS.defaultInstallDirectory)");
        }
        
        asLabel = resolveString(BUNDLE + "InstallLocationPanel.asInstallDirectoryLabel)");
        MnemonicString asInputLabelMn = new MnemonicString(asLabel);
        asLabel = asInputLabelMn.toString();

        asInputLabel = new JLabel(asLabel);
        if (asInputLabelMn.isMnemonicSpecified()) {
            asInputLabel.setDisplayedMnemonic(asInputLabelMn.getMnemonicChar());
        }
        asInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(asInputLabel, gridBagConstraints);

        asInstallDirTF = new JTextField(asInstallDir);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        asInputLabel.setLabelFor(asInstallDirTF);
        inputPanel.add(asInstallDirTF, gridBagConstraints);

        String asBrowseButtonText = resolveString(BUNDLE + "InstallLocationPanel.asBrowseButtonLabel)");
        MnemonicString asBrowseMn = new MnemonicString(asBrowseButtonText);
        asBrowseButtonText = asBrowseMn.toString();
        asBrowseButton = new JButton(asBrowseButtonText);
        if (asBrowseMn.isMnemonicSpecified()) {
            asBrowseButton.setMnemonic(asBrowseMn.getMnemonicChar());
        }
        asBrowseButton.setActionCommand("as"); 
        asBrowseButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 12, 3, 0);
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        inputPanel.add(asBrowseButton, gridBagConstraints);

        //j2seListLabel = new JTextArea();
        //j2seListLabel.setText("(" + defaultDirLabel + " " + j2seInstallDir + ")");	
        //j2seListLabel.setWrapStyleWord(true);
        //j2seListLabel.setLineWrap(true);
        //j2seListLabel.setEditable(false);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 0, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        //inputPanel.add(j2seListLabel, gridBagConstraints);

        //jdkBrowseButton.setBackground(getContentPane().getBackground());
        //jdkInputLabel.setBackground(getContentPane().getBackground());
        //j2seListLabel.setBackground(getContentPane().getBackground());
	//----------------------------------------------------------------------

        mainPanel.add(inputPanel, BorderLayout.CENTER);

	displayPanel.add(mainPanel, BorderLayout.CENTER);

        //displayPanel.setBackground(getContentPane().getBackground());
        //mainPanel.setBackground(getContentPane().getBackground());
        //inputPanel.setBackground(getContentPane().getBackground());
        //infoLabel.setBackground(mainPanel.getBackground());
        //nbBrowseButton.setBackground(getContentPane().getBackground());
        //nbInputLabel.setBackground(getContentPane().getBackground());
        //nbListLabel.setBackground(getContentPane().getBackground());
	
        getContentPane().add(displayPanel, BorderLayout.CENTER);
    }
    
    public boolean queryExit(WizardBeanEvent event) {
        String nbInstallDir = nbInstallDirTF.getText().trim();
        File instDirFile = new File(nbInstallDir);
	String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	logEvent(this, Log.DBG, "queryExit productURL: " + productURL);
	// Only get the canonical path if the install dir is not an empty string.
	// getCanonicalPath returns the directory the installer is run from if the
	// install dir is an empty string.
	if (nbInstallDir.length() > 0) {
	    try { // cleanup any misc chars in path such as "."
		nbInstallDir = instDirFile.getCanonicalPath();
		instDirFile = new File(nbInstallDir);
	    } catch (IOException ioerr) {
		logEvent(this, Log.DBG, "IOException: Could not get canonical path: " + ioerr);
		nbInstallDir = instDirFile.getAbsolutePath();
	    }
	}
        nbInstallDirTF.setText(nbInstallDir);

	// Remove the colon at the end of the label strings which is used to prefix
	// the error/warning messages.
	String nbMsgStart = null;
	int index = nbLabel.lastIndexOf(':');
	if (index > 0) {
	    nbMsgStart = nbLabel.substring(0, index);
	} else {
	    nbMsgStart = nbLabel;
	}
        
	// If there is a problem with the specified directory, then return false
        if (!checkInstallDir(nbInstallDir, NB_INSTALL_DIR, nbMsgStart)) {
            return false;
	}
	try {
	    ProductService service = (ProductService)getService(ProductService.NAME);
	    service.setRetainedProductBeanProperty(productURL,
            Names.CORE_IDE_ID, "installLocation", nbInstallDir);
            service.setRetainedProductBeanProperty(productURL, Names.APP_SERVER_ID, "installLocation",
            nbInstallDir + File.separator + InstallApplicationServerAction.UNINST_DIRECTORY_NAME);
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbInstallDir);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbInstallDir);
            //Set install location for Storage Builder
            String sbDestination = nbInstallDir + File.separator + "_uninst" + File.separator + "storagebuilder";
            logEvent(this, Log.DBG, "Storage Builder Destination: " + sbDestination);
            service.setRetainedProductBeanProperty(productURL,
            Names.STORAGE_BUILDER_ID, "installLocation", sbDestination);
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
	}
	
	System.getProperties().put("nbInstallDir", nbInstallDir);
	logEvent(this, Log.DBG, "User specified nbInstallDir: " + nbInstallDir);
        
        String asInstallDir = asInstallDirTF.getText().trim();
        instDirFile = new File(asInstallDir);
	// Only get the canonical path if the install dir is not an empty string.
	// getCanonicalPath returns the directory the installer is run from if the
	// install dir is an empty string.
	if (asInstallDir.length() > 0) {
	    try { // cleanup any misc chars in path such as "."
		asInstallDir = instDirFile.getCanonicalPath();
		instDirFile = new File(asInstallDir);
	    } catch (IOException ioerr) {
		logEvent(this, Log.DBG, "IOException: Could not get canonical path: " + ioerr);
		asInstallDir = instDirFile.getAbsolutePath();
	    }
	}
        asInstallDirTF.setText(asInstallDir);
        
        Util.setASInstallDir(asInstallDir);
        
 	// Check the as directory
        String asMsgStart = null;
        index = asLabel.lastIndexOf(':');
        if (index > 0) {
            asMsgStart = asLabel.substring(0, index);
        } else {
            asMsgStart = asLabel;
        }
        if (!checkInstallDir(asInstallDir, AS_INSTALL_DIR, asMsgStart)) {
            return false;
	}
        
        if (!checkBothInstallDirs(nbInstallDir,asInstallDir)) {
            return false;
        }
        
	// Last thing to do is to create the NB directory unless the
	// directory exists and is empty.
	if (!emptyExistingDirNB) {
	    if (!createDirectory(nbInstallDir, nbMsgStart)) {
                return false;
            }
	}
        
	if (!emptyExistingDirAS) {
	    if (!createDirectory(asInstallDir, asMsgStart)) {
                return false;
            }
	}
        
	return true;
    }
    
    /* Check the installation directory to see if it has illegal chars,
     * or if it already exits or if it's empty then create it.
     */ 
    private boolean checkInstallDir (String dir, int installDirType, String msgPrefix) {
        String[] okString  = {resolveString("$L(com.installshield.wizard.i18n.WizardResources, ok)")};
	String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryDialogTitle)");
        StringTokenizer st = new StringTokenizer(dir);
        String dialogMsg = "";
        
        if (dir.length() == 0) {
            //Directory must be specified
            dialogMsg = msgPrefix + " "
            + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNotSpecifiedMessage)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
	} else if (Util.isWindowsOS() && (installDirType == AS_INSTALL_DIR) && (dir.length() > AS_INSTALL_PATH_MAX_LENGTH)) {
            //Path is too long
            dialogMsg = msgPrefix + " "
            + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryTooLong," + AS_INSTALL_PATH_MAX_LENGTH 
	    + "," + dir.length() + ")");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        } else if (st.countTokens() > 1) {
            //Check for spaces in path - it is not supported by AS installer on Linux/Solaris
	    if (!Util.isWindowsOS() && (installDirType == AS_INSTALL_DIR)) {
		dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryHasSpaceMessage)");
		showErrorMsg(dialogTitle,dialogMsg);
		return false;
	    }
        } else if ((Util.isWindowsOS() && (dir.indexOf("@") != -1 || dir.indexOf("&") != -1 ||
					 dir.indexOf("%") != -1 || dir.indexOf("#") != -1)) ||
	    dir.indexOf("+") != -1 || dir.indexOf("\"") != -1) {
            //Check for illegal characters in a windows path name
	    dialogMsg = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryIllegalCharsMessage)");
            showErrorMsg(dialogTitle,dialogMsg);
	    return false;
	}
        
        File dirFile = new File(dir);
        if (dirFile.exists()) {
	    // This File object should be a directory
            if (!dirFile.isDirectory()) {
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryInvalidMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
	    // This File object must have write permissions
            if (!(dirFile.canWrite())) {
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
	    
	    // for some reason, the previous check was not enough to detect permissions on Windows
	    // thus, attempt to create a temporary file and delete it and return false if it fails
	    // if (os.startsWith("Windows")) {
	    if (Util.isWindowsOS()) {
		File testFile = new File(dirFile, "test.tmp");
		if (testFile.exists()) {
		    try {
			boolean result = testFile.delete();
			if (result == false) {
			    throw new IOException();
			}
		    } catch (Exception e) {
			dialogMsg = msgPrefix + " "
		        + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
                        showErrorMsg(dialogTitle,dialogMsg);
			return false;
		    }
		} else {
		    try {
			testFile.createNewFile();
			boolean result = testFile.delete();
			if (result == false) {
			    throw new IOException();
			}		    
		    } catch (Exception e) {
			dialogMsg = msgPrefix + " "
		        + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
                        showErrorMsg(dialogTitle,dialogMsg);
			return false;
		    }
		}	 
	    }
	    // We have write permissions, now see if the directory is empty
	    boolean isEmpty = checkNonEmptyDir(dir);
	    if (installDirType == NB_INSTALL_DIR) {
		emptyExistingDirNB = isEmpty;
	    }
	    else {
		emptyExistingDirAS = isEmpty;
	    }
            if(!isEmpty) {
                // Another version installed. prompt to uninstall
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directorySpecifyEmptyMessage)")
                + "\n\n" + installedVersion;
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
        }
        return true;
    }
    
    /* Check if AS and NB installation dirs are the same or not.
     */
    private boolean checkBothInstallDirs(String nbDir, String asDir) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
        String dialogMsg = "";
        
        if (nbDir.equals(asDir)) {
            dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.directoryNBASTheSame)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        }
        return true;
    }
    
    /** Create directory. Do not ask user. Report only error when it fails. */
    private boolean createDirectory(String path, String msgPrefix) {
	String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryDialogTitle)");
	File dirFile  = new File(path);
	String dialogMsg;
	try {
	    logEvent(this, Log.DBG, "CheckInstallDir() dirFile:" + dirFile.getAbsolutePath());
	    if (!dirFile.mkdirs()) {
		dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryCouldNotCreateMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
		return false;
	    }
	} catch (Exception e) {
	    logEvent(this, Log.ERROR, e);
	}
	return true;
    }
    
    private boolean checkNonEmptyDir(String instDir) {
        boolean empty = true;

	File f = new java.io.File(instDir);
	if (f.exists()) {
	    String[] list = f.list();
	    if (list == null) {
		// this is a file not a directory, for now just flag as false
		empty = false;
	    } else if (list.length > 0) {
		empty = false;
	    }
	}
        logEvent(this, Log.DBG, "empty install directory = " + empty);
        return empty;
    }
    
    public void actionPerformed(ActionEvent event) {
        Object obj = event.getSource();
        if (obj instanceof JButton) {
            SwingWizardUI wizardUI = (SwingWizardUI) getWizard().getUI();
            if (wizardUI != null) {
                wizardUI.restoreDefaultColors();
            }
            String str = event.getActionCommand();
            JTextField tf = null;
	    if (str.equals("nb")) {
		str = nbInstallDirTF.getText();
		tf = nbInstallDirTF;
	    } else {
		str = asInstallDirTF.getText();
		tf = asInstallDirTF;
	    }
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(new File(str));
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tf.setText(chooser.getSelectedFile().getAbsolutePath());
            }
            if (wizardUI != null) {
                wizardUI.setWizardColors();
            }
        }
    }
    
    private char getWinSystemDrive() {
	char sysDrive = 'C';
        try {
            String sysLib = resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive = sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, " Found system drive is: " + String.valueOf(sysDrive));
        } catch(Exception ex) {
            Util.logStackTrace(this,ex);
            return 'C';
        }        
	return sysDrive;
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
