/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.io.*;
import java.net.Socket;
import java.util.*;
import com.installshield.product.*;
import com.installshield.product.service.desktop.DesktopService;
import com.installshield.product.service.product.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.wizard.platform.win32.*;
import com.installshield.wizard.service.file.*;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.exitcode.ExitCodeService;

public class InstallJ2sdkAction extends ProductAction implements FileFilter {
    
    //return code incase an error returns
    public static final int J2SDK_UNHANDLED_ERROR = -250;
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String j2seInstallDir = "";
    private String jreInstallDir = "";
    private String tempDir = "";
    private String defaultSubdir = "";
    private String origJ2SEInstallDir = "";
    
    private boolean success = false;
    
    private RunCommand runCommand = new RunCommand();
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public InstallJ2sdkAction() {
    }
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass(FileComparator.class.getName());
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.InstallJ2sdkAction$ProgressThread");
            support.putRequiredService(Win32RegistryService.NAME);
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init(ProductActionSupport support)
    throws Exception{
	/*
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        instDirPath = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")); */
        origJ2SEInstallDir = (String) System.getProperties().get("j2seInstallDir");
        logEvent(this, Log.DBG,"$D(common): " + resolveString("$D(common)"));
        logEvent(this, Log.DBG,"$D(install): " + resolveString("$D(install)"));
        jreInstallDir = resolveString("$D(install)") + "\\Java\\"
        + resolveString("$L(org.netbeans.installer.Bundle,JRE.defaultInstallDirectory)");
        logEvent(this, Log.DBG,"jreInstallDir: " + jreInstallDir);
        
        System.getProperties().put("jreInstallDir",jreInstallDir);
        
        tempDir = resolveString("$J(temp.dir)");
        logEvent(this, Log.DBG,"Tempdir: " + tempDir);
        
        mutableOperationState = support.getOperationState();
    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.installMessage,"
        + "$L(org.netbeans.installer.Bundle,JDK.shortName))")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        
        support.getOperationState().setStatusDescription(statusDesc);
        
        defaultSubdir = resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
        
        try {
            init(support);
            installMode = INSTALL;
            if (!Util.isWindowsOS()) {
                j2seInstallDir = origJ2SEInstallDir + File.separator + defaultSubdir;
            } else {
                j2seInstallDir = origJ2SEInstallDir;
            }
            
            String uninstDir  = origJ2SEInstallDir + File.separator + "_uninst";
	    if (!Util.isWindowsOS()) {
		String jdkInstallScript = uninstDir + File.separator 
                + "j2se-install.template";
		createInstallScript(jdkInstallScript, "custom-install");
		String jdkUninstallScript = uninstDir + File.separator 
		+ "j2se-uninstall.template";
		createUninstallScript(jdkUninstallScript, "uninstall.sh");
	    } else {
		String jdkInstallScript = uninstDir + File.separator 
                + "custom-install-jdk.template";
		createInstallScriptJDKWindows(jdkInstallScript, "custom-install-jdk.bat");
		String jdkUninstallScript = uninstDir + File.separator 
		+ "custom-uninstall.template";
		createUninstallScriptWindows(jdkUninstallScript, "custom-uninstall.bat");
            }
            String execName;
            String driveName;
            int paramCount;

	    // We'll be running the j2se installer in silent mode so most 
	    // of this code is putting together the path and script or exe to
	    // run. Determine the script or exe to run.
            if (Util.isWindowsNT() || Util.isWindows98()) {
                execName =  findJDKWindowsInstaller(j2seInstallDir);
                paramCount = 1;
                driveName = "";                
            }
            else if (Util.isWindowsOS()) {
                driveName = j2seInstallDir.substring(0, j2seInstallDir.indexOf(File.separator));
                execName = "custom-install-jdk.bat";
                paramCount = 5;
            }
            else {
                execName = "custom-install";
                driveName = "";
                paramCount = 1;
            }
            
            String cmdArray[] = new String[paramCount];
            String execPath   = uninstDir + File.separator + execName;
            String logPath    = origJ2SEInstallDir + File.separator + "install-jdk.log";
            String envP[] = null;
            
	    // Put the command and arguments together for windows
            if (Util.isWindowsNT() || Util.isWindows98()) {
                cmdArray[0] = j2seInstallDir + File.separator + execName
                + " /s /v\"/qn INSTALLDIR=\\\"" + j2seInstallDir + "\\\"\"";
            } else if (Util.isWindowsOS()) {
                cmdArray[0] = execPath;
                cmdArray[1] = "\"" + logPath + "\""; //logfile
                cmdArray[2] = "\"" + j2seInstallDir + "\\\""; //instDir NOTE: the opening backslash is in the script
                cmdArray[3] = driveName;
                cmdArray[4] = "\"" + uninstDir + "\""; //uninstDir
            } else {
                cmdArray[0] = execPath;
            }
            
	    // Invoke the correct command
            logEvent(this, Log.DBG,"# # # # # # # #");
            logEvent(this, Log.DBG,"Start Invoking JDK installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
            runCommand(cmdArray, envP, support);
            
            // Clean up jdk bat file
            File file;
            file = new File(cmdArray[0]);
            if (file.exists()) {
                file.delete();
                logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
            }
            
            //Run JRE installer separately on Win NT / Win 98.
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //Install public JRE only when it is not already installed.
                if (!Util.isJREAlreadyInstalled()) {
                    String jreInstaller = findJREWindowsInstaller();
                    if (jreInstaller != null) {
                        cmdArray[0] = "msiexec.exe /qn /i \"" + jreInstaller + "\""
                        + " IEXPLORER=1 MOZILLA=1";
                        logEvent(this, Log.DBG,"# # # # # # # #");
                        logEvent(this, Log.DBG,"Start Invoking JRE installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
                        runCommand(cmdArray, envP, support);
                    }
                }
            } else if (Util.isWindowsOS()) {
                //Install public JRE only when it is not already installed.
                if (!Util.isJREAlreadyInstalled()) {
                    String jreInstallScript = uninstDir + File.separator 
                    + "custom-install-jre.template";
                    createInstallScriptJREWindows(jreInstallScript, "custom-install-jre.bat");
                    driveName = j2seInstallDir.substring(0, j2seInstallDir.indexOf(File.separator));
                    execName = "custom-install-jre.bat";
                    paramCount = 4;
                    cmdArray = new String[paramCount];
                    execPath   = uninstDir + File.separator + execName;
                    logPath    = origJ2SEInstallDir + File.separator + "install-jre.log";
                    cmdArray[0] = execPath;
                    cmdArray[1] = "\"" + logPath + "\""; //logfile
                    cmdArray[2] = driveName;
                    cmdArray[3] = "\"" + uninstDir + "\""; //uninstDir
                    logEvent(this, Log.DBG,"# # # # # # # #");
                    logEvent(this, Log.DBG,"Start Invoking JRE installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
                    runCommand(cmdArray, envP, support);
                    
                    // Clean up jre bat file
                    file = new File(cmdArray[0]);
                    if (file.exists()) {
                        file.delete();
                        logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
                    }
                }
            }
            
            //workaround
            if (Util.isWindowsNT() || Util.isWindows98()) {
                file = new File(uninstDir + File.separator + "custom-install-jdk.bat" );
                if (file.exists()) {
                    file.delete();
                    logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
                }
                file = new File(uninstDir + File.separator + "custom-install-jre.bat" );
                if (file.exists()) {
                    file.delete();
                    logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
                }
            }
            
            //Move JDK up one dir level
            if (!Util.isWindowsOS()) {
                moveJ2SEDirContents();
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"J2SE installation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    /** Does nothing. JDK is not uninstalled by jdkbundle uninstaller. */
    public void uninstall(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        logEvent(this, Log.DBG,"Uninstalling -> ");
        //statusDesc = resolveString("$L(com.sun.installer.InstallerResources,UNINSTALLING_WAIT_MSG)");
        //support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            logEvent(this, Log.DBG,"origJ2SEInstallDir = " + origJ2SEInstallDir);
            installMode = UNINSTALL;
            
            logEvent(this, Log.DBG,"Do nothing here -> " + origJ2SEInstallDir);
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"J2SE uninstallation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, String [] envP, ProductActionSupport support)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //HACK: don't exec script for NT or 98
                runCommand.execute(cmdArray[0], envP, null);
            }
            else {
                runCommand.execute(cmdArray, envP, null);
            }
            
            if ((installMode == INSTALL) && doProgress)
                startProgress();
            
            if (Util.isWindowsOS()) {
                //UGLY HACK: make sure there are enough time elapsed before starting to flush
                int ms = (installMode == INSTALL) ? 2000 : 4500;
                Thread.currentThread().sleep(ms);
            }
            
            
            //int status;
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //HACK: don't flush for NT or 98
            }
            else {
                logEvent(this, Log.DBG,"Flushing ...!");
                runCommand.flush();
                logEvent(this, Log.DBG,"Flushing done!");
                
            }
            
            int status = runCommand.getReturnStatus();
            
            logEvent(this, Log.DBG,"Return status: " + status);
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
            if (!isCompletedSuccessfully()) {
                String mode = (installMode == INSTALL) ? "install" : "uninstall";
                String commandStr = Util.arrayToString(cmdArray, " ");
                logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + commandStr);
                logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + commandStr);
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(J2SDK_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                }
            }
            
            logEvent(this, Log.DBG,"Flushing 2...!");
            runCommand.flush();
            logEvent(this, Log.DBG,"Flushing 2 done!");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    
    /** check whether or not the un/installation was successful */
    private boolean isCompletedSuccessfully() {
        // For now return success
        return Util.checkJdkHome(j2seInstallDir);
    }
    
    
    /** Overridden abstract FileFilter method.
     * List the files which shouldn't be cleaned up after installation
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File pathname) {
        String path = pathname.getAbsolutePath();
        if (installMode == INSTALL) {
            if (path.equals(origJ2SEInstallDir + File.separator + "uninstall.sh")
            || path.equals(origJ2SEInstallDir + File.separator + "uninstall.bat"))
                return false;
        }
        else if (installMode == UNINSTALL) {
            if (path.equals(origJ2SEInstallDir + File.separator + "uninstall.log"))
                return false;
        }
        
        return true;
    }
    
    /** Returns checksum for j2sdk directory in bytes. It does not include public JRE on Windows.
     * JRE can be installed on another disk so it is split. */
    public long getCheckSum() {
        if (Util.isWindowsOS()) {
            return 130000000L;
        }
        else if (Util.isSunOS()) {
            return 140000000L;
        }
        else if (Util.isLinuxOS()) {
            return 140000000L;
        }
        return 0L;
    }
    
    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        //#48948: We must set dirs here because init() is not run yet when getRequiredBytes
        //is called.
        origJ2SEInstallDir = (String) System.getProperties().get("j2seInstallDir");
        tempDir = resolveString("$J(temp.dir)");
        
        RequiredBytesTable req = new RequiredBytesTable();
	//  String imageDirPath = getProductTree().getInstallLocation(this);
	// logEvent(this, Log.DBG,"imageDirPath -> " + imageDirPath);
        req.addBytes(origJ2SEInstallDir, getCheckSum());
        logEvent(this, Log.DBG, "origJ2SEInstallDir: " + origJ2SEInstallDir);
        logEvent(this, Log.DBG, "tempDir: " + tempDir);
	logEvent(this, Log.DBG, "Total size = " + req.getTotalBytes());
        
        if (Util.isWindowsNT() || Util.isWindows98()) {
            //TMP dir is by default on system disk and we are unable to change it #48281
	    //The j2se base image directory goes in the system drive and also cache of installer
            //is stored to Local Settings folder it is 130MB
            //TMP is used by bundled JVM and MSI to store its cache temporarily about 170MB
            //when it is checked here bundled JVM is already present in TMP so only about
            //additional 50MB is necessary ie.130MB+50MB=180MB at system dir.
	    String sysDir = new String( (new Character(getWinSystemDrive())).toString().concat(":\\"));
            logEvent(this, Log.DBG, "sysDir: " + sysDir);
	    req.addBytes(sysDir, 180000000L);
        } else if (Util.isWindowsOS()) {
	    //The j2se base image directory goes in the system drive and also cache of installer
            //is stored to Local Settings folder it is 130MB
            //TMP is used by bundled JVM and MSI to store its cache temporarily about 170MB
            //when it is checked here bundled JVM is already present in TMP so only about
            //additional 50MB is necessary
	    String sysDir = new String( (new Character(getWinSystemDrive())).toString().concat(":\\"));
            logEvent(this, Log.DBG, "sysDir: " + sysDir);
	    req.addBytes(sysDir, 130000000L);
            
            if (!Util.isJREAlreadyInstalled()) {
                jreInstallDir = resolveString("$D(install)") + "\\Java\\"
                + resolveString("$L(org.netbeans.installer.Bundle,JRE.defaultInstallDirectory)");
                req.addBytes(jreInstallDir,70000000L);
            }

            req.addBytes(tempDir, 50000000L);
	}
	logEvent(this, Log.DBG, "Total (not necessarily on one disk when tempdir is redirected) Mbytes = " + (req.getTotalBytes()>>20));
        logEvent(this, Log.DBG, "RequiredBytesTable: " + req);
        return req;
    }

    private char getWinSystemDrive() {
	char sysDrive = 'C';
        try {
            String sysLib=resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive=sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, "Found system drive is: " + String.valueOf(sysDrive));
        } catch(Exception ex) {
            Util.logStackTrace(this,ex);
            return 'C';
        }        
	return sysDrive;
    }
    
    
    private static int ESTIMATED_TIME = 3500; // tenths of seconds
    public int getEstimatedTimeToInstall() {
        return ESTIMATED_TIME;
    }
    
    public void startProgress() {
        progressThread = new ProgressThread();
        progressThread.start();
    }
    
    public void stopProgress() {
        logEvent(this, Log.DBG,"in progress stop");
        progressThread.interrupt();
        logEvent(this, Log.DBG,"interrupting ProgressThread ");
        //wait until progressThread is interrupted
        while (progressThread.isAlive()) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
        }
        logEvent(this, Log.DBG,"ProgressThread interrupted");
        progressThread.finish();
        //progressThread = null;
        
        Thread.currentThread().yield();
        logEvent(this, Log.DBG,"active Threads -> " + Thread.currentThread().activeCount());
    }

    private boolean setExecutable(String filename) {
	try {
	    FileService fileService = (FileService)getService(FileService.NAME);
	    if (fileService == null) {
		logEvent(this, Log.DBG, "FileService is null. Cannot set file as executable: " + filename);
		return false;
	    }
	    fileService.setFileExecutable(filename);
	} catch (Exception ex) {
            logEvent(this, Log.DBG, "Cannot set file as executable: " + filename
		     + "\nException: " + ex);
	    return false;
	}
	return true;
    }
    
    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScript(String template, String scriptName)
	throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
	String installerName = null;
	String arch = (String) System.getProperty("os.arch");
        File installDirFile = new File(origJ2SEInstallDir);
        logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
	if (Util.isLinuxOS()) {
            //Try to locate Linux JDK installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("linux-i586") != -1) && 
                    children[i].getName().endsWith(".bin")) {
                    installerName = children[i].getName();
                    break;
                }
            }
            if (installerName != null) {
                logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
            } else {
                logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                installerName = "jdk-installer-not-found";
            }
	} else if (Util.isSunOS()) {
	    if (arch.startsWith("sparc")) {
                //Try to locate Solaris Sparc JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("solaris-sparc") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
                if (installerName != null) {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
                } else {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                    installerName = "jdk-installer-not-found";
                }
	    } else {
                //Try to locate Solaris X86 JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("solaris-i586") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
                if (installerName != null) {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
                } else {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                    installerName = "jdk-installer-not-found";
                }
	    }
	}

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("J2SE_INSTALL_DIR=")) {
                line = "J2SE_INSTALL_DIR=" + origJ2SEInstallDir;
            } else if (line.startsWith("J2SE_INSTALLER_NAME=")) {
                line = "J2SE_INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("J2SE_VER=")) {
                line = "J2SE_VER=" + resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " is set as executable file.");
	    return true;
	}
	return false;
    }

    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createUninstallScript(String template, String scriptName)
	throws Exception {
        
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);
        
        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("J2SE_INSTALL_DIR=")) {
                line = "J2SE_INSTALL_DIR=" + origJ2SEInstallDir;
            } else if (line.startsWith("J2SE_VER=")) {
                line = "J2SE_VER=" + resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not uninstall template file: " + templateFile);
	}
              
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " is set as executable file.");
	    return true;
	}
	return false;
    }
    
    /** Create the JDK install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScriptJDKWindows (String template, String scriptName)
	throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String installerName = findJDKWindowsInstaller(j2seInstallDir);
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET INSTALLER_NAME=")) {
                line = "SET INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	return true;
    }
    
    /** Create the JRE install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScriptJREWindows (String template, String scriptName)
	throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String installerName = findJREWindowsInstaller();
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            } else if (line.startsWith("SET JRE_MSI_PROJECT=")) {
                //Installation of public JRE. Path must be set according to JDK version.
                line = "SET JRE_MSI_PROJECT=\"" + installerName + "\"";
                logEvent(this, Log.DBG, "JRE line:" + line);
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	return true;
    }
    
    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createUninstallScriptWindows(String template, String scriptName)
	throws Exception {

	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));

	String installerName = findJDKWindowsInstaller(j2seInstallDir);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET INSTALLER_NAME=")) {
                line = "SET INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not uninstall template file: " + templateFile);
	}
              
        writer.close();
	return true;
    }
    
    private String findJDKWindowsInstaller (String j2seInstallDir) {
	String installerName = null;
        //Try to locate Windows JDK installer
        File installDirFile = new File(j2seInstallDir);
        logEvent(this, Log.DBG, "findJDKWindowsInstaller installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("windows-i586") != -1) && 
                children[i].getName().endsWith(".exe")) {
                installerName = children[i].getName();
                break;
            }
        }
        if (installerName != null) {
            logEvent(this, Log.DBG, "findJDKWindowsInstaller JDK installer found: " + installerName);
        } else {
            logEvent(this, Log.DBG, "findJDKWindowsInstaller JDK installer NOT found. JDK cannot be installed.");
            installerName = "jdk-installer-not-found";
        }
        return installerName;
    }
    
    /** Find path to public JRE installer ie. jre.msi file. 
     * @return null if jre.msi file for given JRE version is not found 
     */
    private String findJREWindowsInstaller () {
        //+ "\\Java\\Update\\Base Images\\jdk1.5.0_01.b06\\patch-jdk1.5.0_01.b06\\jre.msi\"";
	String installerName = null;
        //String baseDir = resolveString("$D(common)") + "\\Java\\Update\\Base Images\\";
        File baseDirFile = new File(resolveString("$D(common)") + "\\Java\\Update\\Base Images\\");
        if (!baseDirFile.exists()) {
            return null;
        }
        String defaultJDKDir = resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
        File [] files = baseDirFile.listFiles();
        File jdkDirFile = null;
        boolean found = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(defaultJDKDir)) {
                found = true;
                jdkDirFile = files[i];
                break;
            }
        }
        if (!found) {
            return null;
        }
        if (!jdkDirFile.exists()) {
            return null;
        }
        
        files = jdkDirFile.listFiles();
        File patchDirFile = null;
        found = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith("patch-" + defaultJDKDir)) {
                found = true;
                patchDirFile = files[i];
                break;
            }
        }
        if (!found) {
            return null;
        }
        if (!patchDirFile.exists()) {
            return null;
        }
        File jreInstallerFile = new File(patchDirFile,"jre.msi");
        if (!jreInstallerFile.exists()) {
            return null;
        }
        
        logEvent(this, Log.DBG, "findJREWindowsInstaller JRE installer found: " + jreInstallerFile.getPath());
        return jreInstallerFile.getPath();
    }
    
    private boolean moveJ2SEDirContents() {
         try {
             FileService fileService = (FileService)getService(FileService.NAME);
             if (fileService == null) {
                 logEvent(this, Log.DBG, "FileService is null. Cannot move J2SE files");
                 return false;
             }
             if (fileService != null) {
                 File srcFile = new File(j2seInstallDir);
                 File[] srcFileList = srcFile.listFiles();
                 if (srcFileList == null) {
                     logEvent(this, Log.DBG, "Could not rename the J2SE files.");
                     return false;
                 }
                 String parent = srcFile.getParent();                 
                 logEvent(this, Log.DBG, "Moving files from " + j2seInstallDir +
                          "\n to  " + parent);
                 try {
                     for (int i=0; i < srcFileList.length; i++) {
                         logEvent(this, Log.DBG, "Rename " +
                                  srcFileList[i].getAbsolutePath() +
                                  "  to  " + parent + "/" +
                                  srcFileList[i].getName());
                                 srcFileList[i].renameTo(new File(parent +
                                                          File.separator +
                                                          srcFileList[i].getName()));
                     }
                 } catch (Exception ex) {
                     logEvent(this, Log.DBG, "Could not rename the J2SE files.");
                     logEvent(this, Log.DBG, ex);
                     return false;
                 }
                 try {
                     fileService.deleteDirectory(j2seInstallDir, true, false);
                 } catch (Exception ex) {
                     logEvent(this, Log.DBG,
                              "Could not remove empty J2SDK directory: " + j2seInstallDir);
                 }
             }
         } catch (Exception ex) {
             logEvent(this, Log.ERROR, "Cannot get FileService for moving J2SE files\nException: " + ex);
             return false;
         }
         return true;
     }

    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        private File jdkDir;
        private File jreDir;
        
        //progress bar related variables
        private long percentageCompleted = 0L;
        private long checksum = 0L;
        
        //status detail related variables
        //progress dots (...) after the path if it is being shown since s while
        private final FileComparator fileComp = new FileComparator();
        private final int MIN_DOTS = 3;
        private int fileCounter = 0;
        private String lastPathShown;
        
        //status description related variables
        private File logFile;
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            lastPathShown = j2seInstallDir;
            jdkDir = new File(j2seInstallDir);
            jreDir = new File(jreInstallDir);
            logFile = new File(j2seInstallDir, "install.log");
            checksum = getCheckSum();
            //JRE is installed by default, adjust checksum to display progress bar correctly
            if (!Util.isJREAlreadyInstalled()) {
                checksum += 70000000L;
            }
        }
        
        public void run() {
            int sleepTime = 1000;
            while (loop) {
                logEvent(this, Log.DBG,"looping");
                try {
                    if (jdkDir.exists()) {
                        //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                        updateProgressBar();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDetail");
                        updateStatusDetail();
                        
                        sleepTime = 1200;
                    } else {
                        updateStatusDetail();
                        sleepTime = 2000;
                    }
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) return;
                } catch (InterruptedException ex) {
                    //ex.printStackTrace();
                    loop = false;
                    return;
                } catch (Exception ex) {
                    loop = false;
                    String trace = Util.getStackTrace(ex);
                    logEvent(this, Log.DBG, trace);
                    logEvent(this, Log.ERROR, trace);
                    return;
                }
            }
        }
        
        public void finish() {
            loop = false;
            Thread.currentThread().yield();
            mos.setStatusDetail("");
            logEvent(this, Log.DBG,"Finishing");;
            if(!mos.isCanceled()) {
                mos.setStatusDescription("");
                for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }
            }
            else {
                String statusDesc = resolveString("$L(com.sun.installer.InstallerResources,AS_OPERATION_CANCELED)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /** Check if the operation is canceled. If not yield to other threads. */
        private boolean isCanceled() {
            if(mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
                runCommand.interrupt();
            }
            else {
                Thread.currentThread().yield();
            }
            
            return mos.isCanceled();
        }
        
        /** Updates the progress bar. */
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }
            long size = Util.getFileSize(jdkDir) + Util.getFileSize(jreDir);
            long perc = (size * 100) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
        /** Updates the status detail. */
        public void updateStatusDetail() {
            if (isCanceled()) return;
            if (!jdkDir.exists()) {
                mos.setStatusDetail(resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.prepareMessage)"));
                logEvent(this, Log.DBG,"StatusDetailThread-> " + lastPathShown + " NOT created yet");
                return;
            }
            String recentFilePath = fileComp.getMostRecentFile(jdkDir).getAbsolutePath();
            logEvent(this, Log.DBG,"StatusDetailThread-> " + recentFilePath + "  MODIFIED!!!");
            String filename = getDisplayPath(recentFilePath);
            if (Util.isWindowsOS()) {
                mos.setStatusDetail(filename);
            } else {
                mos.setStatusDetail(modifyFilename(filename));
            }
        }
        
        private String modifyFilename(String name) {
             StringBuffer filename = new StringBuffer(name);
             int subdirSize = defaultSubdir.length();
             int nameSize = name.length();
             for (int i=0; i < nameSize-subdirSize; i++) {
                 if (defaultSubdir.equals(filename.substring(i,i+subdirSize))) {
                     name = filename.substring(0, i)
                          + filename.substring(i+subdirSize+1, nameSize);
                     break;
                 }
             }
             return name;
         }
        
        //progress dots (...) after the path if it is being shown since s while
        private String getDisplayPath(String recentFilePath) {
            try {
                String displayStr = recentFilePath;
                int max_len = 60;
                if (displayStr.length() > max_len) {
                    String fileName = displayStr.substring(displayStr.lastIndexOf(File.separatorChar));
                    displayStr = displayStr.substring(0, max_len - fileName.length() - 4)
                    + "...."
                    + fileName;
                }
                if (! recentFilePath.equalsIgnoreCase(lastPathShown)) {
                    lastPathShown = recentFilePath;
                    fileCounter = 0;
                    return displayStr;
                }
                else if ( fileCounter < MIN_DOTS) {
                    fileCounter++;
                    return displayStr;
                }
                fileCounter = Math.max(fileCounter % 10, MIN_DOTS);
                char [] array = new char[fileCounter];
                Arrays.fill(array, '.');
                fileCounter++;
                return  displayStr + " " + String.valueOf(array);
            } catch (Exception ex) {
                String trace = Util.getStackTrace(ex);
                logEvent(this, Log.DBG, trace);
                logEvent(this, Log.ERROR, trace);
                return recentFilePath;
            }
        }
        
        
    }
    
}
