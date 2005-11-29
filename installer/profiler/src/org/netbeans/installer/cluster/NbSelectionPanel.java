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

package org.netbeans.installer.cluster;

import com.installshield.product.SoftwareObject;
import com.installshield.product.i18n.ProductResourcesConst;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.service.registry.RegistryService;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.util.Log;
import com.installshield.util.StringUtils;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.WizardServicesUI;
import com.installshield.wizard.service.file.FileService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Vector;

import org.netbeans.installer.DestinationItem;
import org.netbeans.installer.DirectoryChooserPanel;
import org.netbeans.installer.RunCommand;
import org.netbeans.installer.Util;

/** Used to select NetBeans IDE location where Profiler shoudl be installed.
 * List shows all found IDE installations.
 * Note: It is used by Profiler installer only now.
 */
public class NbSelectionPanel extends DirectoryChooserPanel {    
    
    private String nbHome;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    public void build(WizardBuilderSupport support) {
        super.build(support);
        
        support.putRequiredService(RegistryService.NAME);
        support.putRequiredService(ProductService.NAME);
        support.putRequiredService(FileService.NAME);
        try {
            support.putClass(Util.class.getName());
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(NbSelectionPanel.RunUninstaller.class.getName());
            support.putClass(NbSelectionPanel.PlatformClusterFilter.class.getName());
        } catch (IOException ex) {
            logEvent(this, Log.ERROR, ex);
        }
    }

    public boolean queryEnter(WizardBeanEvent event) {
        
        //Get the object of NbSearchAction set in the system property (set by NbSearchAction)
        //and retrieve the searched Nb Home List
        Vector nbHomeList = NbSearchAction.getNbHomeList();
        
        String description = null;
        if (nbHomeList.size() != 0) {
            description = resolveString(BUNDLE + "NbSelectionPanel.description)");
        } else {
            description = resolveString(BUNDLE + "NbSelectionPanel.descriptionNbNotFound)");
        }
        
        setDescription(description);
        
        setDestinationCaption(BUNDLE + "NbSelectionPanel.nbHomeLabel)");
        
        String listLabelText = resolveString
        (BUNDLE + "NbSelectionPanel.nbListLabel)");
        setDestinationsCaption(listLabelText);
        
        destinations = new Vector(nbHomeList.size());
        for (int i = 0; i < nbHomeList.size(); i++) {
            DestinationItem item = new DestinationItem();
            SoftwareObject so = (SoftwareObject) nbHomeList.elementAt(i);
            item.setValue(so.getInstallLocation());
            //Fix because for NB 4.1 product properties (like name) was not resolved.
            if ("410000".equals(so.getKey().getUID().substring(26,32))) {
                item.setDescription("NetBeans IDE 4.1" + " " + so.getInstallLocation());
            } else {
                item.setDescription(so.getName() + " " + so.getInstallLocation());
            }
            destinations.add(item);
        }
        setSelectedDestinationIndex("" + NbSearchAction.getLatestVersionIndex());
        
        return true;
    }
    
    public boolean queryExit(WizardBeanEvent event) {
        nbHome = getDestination();
        logEvent(this, Log.DBG, "nbHome: " + nbHome);
        if (!validateNbDir()) {
            return false;
        }
        return validateDestination();
    }
    
    public void exited(WizardBeanEvent event) {
        super.exited(event);
        try {
            String installDir = getInstallDir();
            
            ProductService service = (ProductService)getService(ProductService.NAME);
            service.setProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE,
            null,
            "installLocation",
            resolveString(getInstallDir()));
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
    }
    
    private String getInstallDir() {
        return new File(nbHome, resolveString(BUNDLE + "Product.clusterDir)")).getPath();
    }

    /** Checks if there is NB cluster dir in selected NB installation directory.
     * If it fails at specific test try to check subdir on Mac OS X.
     */
    private boolean validateNbDir () {
        logEvent(this, Log.DBG,"Enter validateNbDir nbHome: " + nbHome);
        //Find nb cluster dir
        if ("".equals(nbHome)) {
            //Empty string
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        File dir = new File(nbHome);
        //isDirectory() returns true only if given dir exists and is directory
        if (!dir.isDirectory()) {
            //Entered dir does not exist
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        File [] files = dir.listFiles(new PlatformClusterFilter());
        for (int i = 0; i < files.length; i++) {
            logEvent(this,Log.DBG,"validateNbDir files[" + i + "]: " + files[i].getPath());
        }
        if (files.length == 0) {
            //NB Cluster dir not found
            if (Util.isMacOSX() && nbHome.endsWith(".app")) {
                return validateNbDirMacOSX();
            } else {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        }

        String minVersion = resolveString(BUNDLE + "NetBeans.platformMinClusterDirVersion)");
        String maxVersion = resolveString(BUNDLE + "NetBeans.platformMaxClusterDirVersion)");
        String basePlatformName = resolveString(BUNDLE + "NetBeans.platformClusterDirBase)");
        String minClusterDir = basePlatformName + minVersion;
        String maxClusterDir = basePlatformName + maxVersion;

        if ((minClusterDir.length() > 0) && (maxClusterDir.length() > 0)) {
            if (minClusterDir.compareTo(files[0].getName()) > 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
            if (maxClusterDir.compareTo(files[0].getName()) < 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        } else if ((minClusterDir.length() > 0) && (maxClusterDir.length() == 0)) {
            if (minClusterDir.compareTo(files[0].getName()) > 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        } else if ((minClusterDir.length() == 0) && (maxClusterDir.length() > 0)) {
            if (maxClusterDir.compareTo(files[0].getName()) < 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        }

        if (!files[0].isDirectory()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        if (!files[0].canWrite()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.cannotWriteNbDir)"));
            return false;
        }
        //Platform cluster dir check passed
        //Check ide cluster dir
        //Get string after base name like ie. strip base name from "platform5"
        //to get "5".
        String version = files[0].getName().substring(basePlatformName.length(),files[0].getName().length());
        
        String baseIdeName = resolveString(BUNDLE + "NetBeans.ideClusterDirBase)");
        String ideClusterDir = baseIdeName + version;
        
        File f = new File(nbHome, ideClusterDir);
        if (!f.isDirectory()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        if (!f.canWrite()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.cannotWriteNbDir)"));
            return false;
        }
        return true;
    }
    
    /** Checks if there is NB cluster dir in selected NB installation directory
     * on Mac OS X.
     */
    private boolean validateNbDirMacOSX () {
        //Find nb cluster dir
        nbHome = nbHome + resolveString(BUNDLE + "NetBeans.nbSubDir)");
        
        logEvent(this, Log.DBG,"Enter validateNbDirMacOSX nbHome: " + nbHome);
        
        if ("".equals(nbHome)) {
            //Empty string
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        File dir = new File(nbHome);
        //isDirectory() returns true only if given dir exists and is directory
        if (!dir.isDirectory()) {
            //Entered dir does not exist
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        File [] files = dir.listFiles(new PlatformClusterFilter());
        for (int i = 0; i < files.length; i++) {
            logEvent(this,Log.DBG,"validateNbDir files[" + i + "]: " + files[i].getPath());
        }
        if (files.length == 0) {
            //NB Cluster dir not found
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }

        String minVersion = resolveString(BUNDLE + "NetBeans.platformMinClusterDirVersion)");
        String maxVersion = resolveString(BUNDLE + "NetBeans.platformMaxClusterDirVersion)");
        String basePlatformName = resolveString(BUNDLE + "NetBeans.platformClusterDirBase)");
        String minClusterDir = basePlatformName + minVersion;
        String maxClusterDir = basePlatformName + maxVersion;

        if ((minClusterDir.length() > 0) && (maxClusterDir.length() > 0)) {
            if (minClusterDir.compareTo(files[0].getName()) > 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
            if (maxClusterDir.compareTo(files[0].getName()) < 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        } else if ((minClusterDir.length() > 0) && (maxClusterDir.length() == 0)) {
            if (minClusterDir.compareTo(files[0].getName()) > 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        } else if ((minClusterDir.length() == 0) && (maxClusterDir.length() > 0)) {
            if (maxClusterDir.compareTo(files[0].getName()) < 0) {
                showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
                resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
                return false;
            }
        }

        if (!files[0].isDirectory()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        if (!files[0].canWrite()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.cannotWriteNbDir)"));
            return false;
        }
        //Platform cluster dir check passed
        //Check ide cluster dir
        //Get string after base name like ie. strip base name from "platform5"
        //to get "5".
        String version = files[0].getName().substring(basePlatformName.length(),files[0].getName().length());

        String baseIdeName = resolveString(BUNDLE + "NetBeans.ideClusterDirBase)");
        String ideClusterDir = baseIdeName + version;

        File f = new File(nbHome, ideClusterDir);
        if (!f.isDirectory()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.invalidNbDir)"));
            return false;
        }
        if (!f.canWrite()) {
            showErrorMsg(resolveString(BUNDLE + "NetBeansDirChooser.dirChooserDialogTitle)"),
            resolveString(BUNDLE + "NetBeansDirChooser.cannotWriteNbDir)"));
            return false;
        }
        return true;
    }
    
    /**
     * Helper method to validate  the data entered for the product destination.
     *
     * @boolean true if the specified destination directory exists and writable. false otherwise.
     */
    private boolean validateDestination () {
        // There are three ways a directory can be invalid:
        //
        //  1 - If it is blank or is all white-space
        //  2 - If the file service rejects it as valid
        //  3 - If it cannot be created
        
        // blank/all white-space check
        String dest = getInstallDir();
        String title = LocalizedStringResolver.resolve
        (ProductResourcesConst.NAME,"DestinationPanel.destinationDirectory");
        logEvent(this, Log.DBG,"validateDestination dest: " + dest);
        if (StringUtils.isWhitespace(dest)) {
            String msg = LocalizedStringResolver.resolve(ProductResourcesConst.NAME,"DestinationPanel.specifyDirectory");
            showErrorMsg(title,msg);
            return false;
        }
        
        try {
            FileService fileService = (FileService) getService(FileService.NAME);
            
            // validate file name
            fileService.validateFileName(dest);
            //Create destination dir if it does not exist already
            if (!fileService.fileExists(dest)) {
                fileService.createDirectory(dest);
            } else {
                //If destination dir already exists check if it is valid and empty.
                String [] files = fileService.getDirectoryList(dest,FileService.FILES_AND_DIRECTORIES);
                if (files == null) {
                    String msg = resolveString(BUNDLE + "NetBeansDirChooser.invalidDir," + dest + ")");
                    showErrorMsg(title,msg);
                    return false;
                } else if (files.length > 0) {
                    //Destination directory exists and is not empty.
                    //Check its content: It can be either profiler installation or uknown content.
                    return checkDestinationDirContent();
                }
            }
            
            // writable check
            if (!isDirectoryValid(fileService, dest)) {
                String msg = resolveString(BUNDLE + "NetBeansDirChooser.invalidDir," + dest + ")");
                showErrorMsg(title,msg);
                return false;
            }
            
        } catch (ServiceException e) {
            showErrorMsg(title, e.getMessage());
            return false;
        }
        
        // all checks pass, directory is ok
        return true;
    }
    /** Check content of selected destination directory if it is not empty.
     * It can contain:
     * 1.Uknown content
     * 2.Older profiler installation
     * 3.The same profiler installation
     * 4.Newer profiler installation.
     * In case 2 it offers to uninstall previous profiler installation first.
     */
    private boolean checkDestinationDirContent () {
        String currentUID = resolveString(BUNDLE + "Product.UID)");
        String title = LocalizedStringResolver.resolve
        (ProductResourcesConst.NAME,"DestinationPanel.destinationDirectory");
        String dest = getInstallDir();
        try {
            // Get the instance of RegistryService
            RegistryService regserv = (RegistryService)getService(RegistryService.NAME);  
            String [] arr = regserv.getAllSoftwareObjectUIDs();
            /*for (int i = 0; i < arr.length; i++) {
               System.out.println("arr[" + i + "]: " + arr[i]);
            }*/
            //Look for any profiler installation
            SoftwareObject so = null;
            for (int i = 0; i < arr.length; i++) {
               if (arr[i].startsWith(currentUID.substring(0,26))) {
                   so = regserv.getSoftwareObject(arr[i],dest);
                   if (so != null) {
                       break;
                   }
               }
            }
            if (so != null) {
                //Profiler is installed in destination directory
                //Check version
                logEvent(this, Log.DBG,"so.UID:" + so.getKey().getUID()
                + " so.location:" + so.getInstallLocation());
                String installedUID = so.getKey().getUID();
                //Check main version
                if (currentUID.substring(0,29).equals(installedUID.substring(0,29))) {
                    //The same main version
                    //We have following possibilities "de" or "00".
                    int orderLength = 0;
                    String s = resolveString(BUNDLE + "Order.length)");
                    try {
                        orderLength = Integer.parseInt(s);
                    } catch (NumberFormatException exc) {
                        logEvent(this, Log.ERROR,"Incorrect number for Order.length: " + s);
                    }
                    //No order is defined.
                    if (orderLength == 0) {
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty3," + dest + ")");
                        showErrorMsg(title,msg);
                        return false;
                    }
                    String [] orderArray = new String[orderLength];
                    for (int i = 0; i < orderLength; i++) {
                        orderArray[i] = resolveString(BUNDLE + "Order." + i + ")");
                        //System.out.println("orderArray[" + i + "]: " + orderArray[i]);
                    }
                    //Find indices for current and installed version
                    int currentIndex = 0;
                    for (int i = 0; i < orderLength; i++) {
                        if (orderArray[i].equals(currentUID.substring(29,32))) {
                            currentIndex = i;
                        }
                    }
                    int installedIndex = 0;
                    for (int i = 0; i < orderLength; i++) {
                        if (orderArray[i].equals(installedUID.substring(29,32))) {
                            installedIndex = i;
                        }
                    }
                    if (currentIndex < installedIndex) {
                        //Newer version is installed ie. currentUID < installedUID
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty4," + dest + ")");
                        showErrorMsg(title,msg);
                        return false;
                    } else if (currentIndex > installedIndex) {
                        //Older version is installed ie. currentUID > installedUID
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty2," + dest + ")");
                        String uninstall = "Uninstall";
                        String cancel = "Cancel";
                        Object ret = getWizard().getServices().getUserInput(title, msg, new Object [] {uninstall,cancel},cancel);
                        if (ret == uninstall) {
                            logEvent(this, Log.DBG,"UNINSTALL");
                            return uninstallPrevious();
                        } else {
                            logEvent(this, Log.DBG,"CANCEL");
                            return false;
                        }
                    } else {
                        //The same version
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty3," + dest + ")");
                        String uninstall = "Uninstall";
                        String cancel = "Cancel";
                        Object ret = getWizard().getServices().getUserInput(title, msg, new Object [] {uninstall,cancel},cancel);
                        if (ret == uninstall) {
                            logEvent(this, Log.DBG,"UNINSTALL");
                            return uninstallPrevious();
                        } else {
                            logEvent(this, Log.DBG,"CANCEL");
                            return false;
                        }
                    }
                } else {
                    if (currentUID.compareTo(installedUID) < 0) {
                        //Newer version is installed ie. currentUID < installedUID
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty4," + dest + ")");
                        showErrorMsg(title,msg);
                        return false;
                    } else if (currentUID.compareTo(installedUID) > 0) {
                        //Older version is installed ie. currentUID > installedUID
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty2," + dest + ")");
                        String uninstall = "Uninstall";
                        String cancel = "Cancel";
                        Object ret = getWizard().getServices().getUserInput(title, msg, new Object [] {uninstall,cancel},cancel);
                        if (ret == uninstall) {
                            logEvent(this, Log.DBG,"UNINSTALL");
                            return uninstallPrevious();
                        } else {
                            logEvent(this, Log.DBG,"CANCEL");
                            return false;
                        }
                    } else {
                        //This should not happen
                        String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty1," + dest + ")");
                        showErrorMsg(title,msg);
                        return false;
                    }
                }
            } else {
                //Uknown content
                String msg = resolveString(BUNDLE + "NetBeansDirChooser.notEmpty1," + dest + ")");
                showErrorMsg(title,msg);
                return false;
            }
        } catch (ServiceException exc) {
            logEvent(this, Log.ERROR, exc);
            return false;
        }
    }
    
    private boolean uninstallPrevious () {
        String currentUID = resolveString(BUNDLE + "Product.UID)");
        String title = LocalizedStringResolver.resolve
        (ProductResourcesConst.NAME,"DestinationPanel.destinationDirectory");
        String dest = getInstallDir();
        
        try {
            FileService fileService = (FileService) getService(FileService.NAME);
            String uninstallerPath;
            if (Util.isWindowsOS()) {
                uninstallerPath = dest + File.separator + "_uninst" + File.separator + "uninstaller.exe";
            } else {
                uninstallerPath = dest + File.separator + "_uninst" + File.separator + "uninstaller";
            }
            if (!fileService.fileExists(uninstallerPath)) {
                //Uninstaller not found, cannot uninstall
                String msg = resolveString(BUNDLE + "NetBeansDirChooser.uninstallerNotFound,"
                + uninstallerPath + ")");
                showErrorMsg(title,msg);
                return false;
            }
            //Run uninstaller is separate thread and disable navigation buttons.
            disableNavigation();

            //RegistryService must be finalized here so that uninstaller can access VPD.
            //Current VPD impl does not allow simultaneous access from more processes
            RegistryService regserv = (RegistryService) getService(RegistryService.NAME);
            regserv.finalizeRegistry();
            logEvent(this, Log.DBG, "Call of finalizeRegistry");
            
            RunUninstaller runUninstaller = new RunUninstaller(uninstallerPath,this);
            runUninstaller.start();
            return false;
        } catch (ServiceException exc) {
            Util.logStackTrace(this,exc);
            String msg = resolveString(BUNDLE + "NetBeansDirChooser.uninstallerError,"
            + exc.getLocalizedMessage() + ")");
            showErrorMsg(title,msg);
            return false;
        }
    }
    
    /**
     * Helper method to check whether the directory is writeable.
     *
     * @param destination the directory hierarchy.
     * @param fileService the runtime service used for the task.
     */
    private boolean isDirectoryValid(FileService fileService, String destination) {
        try {
            // check if the directory already existed
            if (!fileService.fileExists(destination)) {
                throw new IOException("Destination doesn't exist");
            }
            if (!fileService.isDirectory(destination)) {
                throw new IOException("Destination isn't a valid directory");
            }
            if (!fileService.isDirectoryWritable(destination)) {
                throw new IOException("Destination isn't a valid directory");
            }
            // we were able to read and use selected destination
            return true;
            
        } catch (Exception e) {
            logEvent(this, Log.WARNING, e.getMessage());
            return false;
        }
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey, String[] params) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey, params);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    private void disableNavigation () {
        logEvent(this, Log.DBG,"disableNavigation");
        getWizard().getUI().setNavigationEnabled(false);
        getWizard().getUI().setBusy
        (resolveString(BUNDLE + "NetBeansDirChooser.uninstallerRunning)"));
    }
    
    private void enableNavigation () {
        logEvent(this, Log.DBG,"enableNavigation");
        getWizard().getUI().setNavigationEnabled(true);
        getWizard().getUI().clearBusy();
    }
    
    /** Used to run uninstaller in separate thread not to block AWT thread in installer
     * when uninstaller is running.
     */
    private class RunUninstaller extends Thread {
	private final String uninstallerPath;
        private final Log log;
	private Throwable throwable = null;
        
	RunUninstaller (String uninstallerPath, Log log) {
	    this.uninstallerPath = uninstallerPath;
            this.log = log;
	}
        
	public void run() {
            int returnValue = -1;
	    try {
                RunCommand runCommand = new RunCommand();
                String [] params = new String[2];
                params[0] = uninstallerPath;
                params[1] = "-silent";
                runCommand.execute(params);
                runCommand.waitFor();
                log.logEvent(this, Log.DBG,runCommand.print());
                returnValue = runCommand.getReturnStatus();
                log.logEvent(this, Log.DBG,"Uninstaller returned: " + returnValue);
	    } catch (Throwable t) {
                Util.logStackTrace(log,t);
	    } finally {
                enableNavigation();
                if (returnValue != 0) {
                    String title = LocalizedStringResolver.resolve
                    (ProductResourcesConst.NAME,"DestinationPanel.destinationDirectory");
                    String msg = resolveString(BUNDLE + "NetBeansDirChooser.uninstallerFailed)");
                    showErrorMsg(title,msg);
                }
                //RegistryService must be reinitialized here so that installer can update VPD
                try {
                    RegistryService regserv = (RegistryService) getService(RegistryService.NAME);
                    regserv.initializeRegistry();
                    logEvent(this, Log.DBG, "Call of initializeRegistry");
                } catch (ServiceException ex) {
                    Util.logStackTrace(log,ex);
                }
            }
	}
    }
    
    /** Used to filter all but nb cluster dir from file list.
     */
    private static class PlatformClusterFilter implements FileFilter {
        
	public boolean accept (File f) {
            if (!f.isDirectory()) {
                return false;
            }
            return f.getName().matches("platform[0-9]");
	}
    }
}
