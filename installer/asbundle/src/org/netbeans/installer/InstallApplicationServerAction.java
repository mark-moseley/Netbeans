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

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.RequiredBytesTable;
import com.installshield.product.service.desktop.DesktopService;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.platform.win32.Win32RegistryService;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.exitcode.ExitCodeService;
import com.installshield.wizard.service.file.FileService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;

public class InstallApplicationServerAction extends ProductAction implements FileFilter {
    
    //return code incase an error returns
    public static final int AS_UNHANDLED_ERROR = -500;
    
    private static final String AS_SETUP_DIR    = "as_setup";
    private static final String STATE_FILE_NAME = "statefile";
    private static final String UNINST_DIRECTORY_NAME = "_uninst";
    protected static final String IMAGE_DIRECTORY_NAME = "SunAppServer8.1";
    protected static final String JDK_DIRECTORY_NAME = "java";
    protected static final String POINTBASE_DIRECTORY_NAME = "pointbase";
    
    //protected static final String AS_EXEC_NAME_WINDOWS = "sjsas_pe-8_0-windows.exe";
    //protected static final String AS_EXEC_NAME_LINUX   = "sjsas_pe-8_0-linux.bin";
    //protected static final String AS_EXEC_NAME_SPARC   = "sjsas_pe-8_0-solaris-sparc.bin";
    //protected static final String AS_EXEC_NAME_X86     = "sjsas_pe-8_0-solaris-i586.bin";

    private static final String INSTALL_SH    = "custom-install.sh";
    private static final String UNINSTALL_SH  = "custom-uninstall.sh";
    private static final String INSTALL_BAT   = "custom-install.bat";
    private static final String UNINSTALL_BAT = "custom-uninstall.bat";
    private static final String AS8_LICENSE   = "appserv.lic";
    private static final String PERM_LICENSE  = "plf";
    private static final String J2EESDK_PROP_FILE = "/enterprise1/config/J2EE/InstalledServers/J2EESDK.properties";
    
    protected static int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    private static int adminPortNumber;
    
    private String statusDesc = "";
    private String instDirPath;
    private String imageDirPath;
    /** Location of JDK on which installer is running. */
    private String jdkDirPath;
    /** NetBeans installation directory. */
    private String nbInstallDir;
    private String statefilePath;
    private String asSetupDirPath;
    
    private boolean success = false;
    private boolean invalidPortFound = false;

    // Port info
    private String adminPort = null;
    private String webPort = null;
    private String httpsPort = null;
    
    private String tmpDir = null;

    private RunCommand runCommand = new RunCommand();
    
    //thread for updating the progress pane
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public InstallApplicationServerAction() {}
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass(FileComparator.class.getName());
            support.putClass(Util.class.getName());
            support.putClass(NetUtils.class.getName());
            support.putClass("org.netbeans.installer.InstallApplicationServerAction$ProgressThread");
            support.putClass("org.netbeans.installer.InstallApplicationServerAction$PointbaseFileFilter");
            support.putClass("org.netbeans.installer.InstallApplicationServerAction$PEFileFilter");
            support.putRequiredService(Win32RegistryService.NAME);
            support.putRequiredService(DesktopService.NAME);
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init(ProductActionSupport support) 
    throws Exception{
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        nbInstallDir = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")) + File.separator;
        instDirPath = nbInstallDir + UNINST_DIRECTORY_NAME;
        logEvent(this, Log.DBG,"instDirPath: "+ instDirPath);
        imageDirPath  = nbInstallDir + IMAGE_DIRECTORY_NAME;
	asSetupDirPath = instDirPath + File.separator + AS_SETUP_DIR;
	if (Util.isWindowsOS()) {
	    statefilePath = asSetupDirPath + File.separator + STATE_FILE_NAME;
	} else {
	    statefilePath = instDirPath + File.separator + STATE_FILE_NAME;
	}
        jdkDirPath = resolveString("$J(java.home)");
        logEvent(this, Log.DBG,"jdkDirPath: "+ jdkDirPath);

        // Get port information
	/*
        adminPort = (String)System.getProperties().get("adminPort");
        webPort = (String)System.getProperties().get("webPort");
        httpsPort = (String)System.getProperties().get("httpsPort");
	*/
        setTmpDir();
       
        mutableOperationState = support.getOperationState();
    }

    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(com.sun.installer.InstallerResources,APP_SERVER_INSTALL_DESCRIPTION)") + "\n" + resolveString("$L(com.sun.installer.InstallerResources, FIREWALL_WARNING)");
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            File setupFile = new File(statefilePath);
            logEvent(this, Log.DBG,"setupFile="+ setupFile.getAbsolutePath());
            mutableOperationState.setStatusDetail(setupFile.getAbsolutePath());
            
            if (!setupFile.exists()) {
		//InstallerExceptions.setErrors(true);
                logEvent(this, Log.ERROR, "Could not find Setup File - " + setupFile.getAbsolutePath());
                // Need I18N here
                System.out.println("Could not find Setup File - " + setupFile.getAbsolutePath());
                return;
            } 

            if (Util.isWindowsOS()) {
		createScript(asSetupDirPath + File.separator + "as-win-install.template",
			     INSTALL_BAT, INSTALL);
		createScript(instDirPath + File.separator + "as-win-uninstall.template",
			     UNINSTALL_BAT, UNINSTALL);
	    }
            else {
                String installTemplate = instDirPath + File.separator + "as-unix-install.template";
		boolean executable = createScript(installTemplate, INSTALL_SH, INSTALL);
		if (!executable) {
		    // Can't execute install script so exit
		    //InstallerExceptions.setErrors(true);
		    //InstallerExceptions.addErrorMsg(resolveString("$L(com.sun.installer.InstallerResources,IE_EXEC_PERM_NOT_SET)") + INSTALL_SH);
		    logEvent(this, Log.ERROR, "Could not set execute permissions for Unix install script: " + INSTALL_SH);
		    return;
		}
                String uninstallTemplate = instDirPath + File.separator + "as-unix-uninstall.template";
		executable = createScript(uninstallTemplate, UNINSTALL_SH, UNINSTALL);
		if (!executable) {
		    // Install anyway but can't uninstall
		    //InstallerExceptions.setWarnings(true);
		    String msg = resolveString("$L(com.sun.installer.InstallerResources,AS_UNINSTALLER_NOT_EXECUTABLE)") + uninstallTemplate;
		    //InstallerExceptions.addWarningMsg(msg);
		    logEvent(this, Log.DBG, msg);
		}
	    }

            boolean modified = modifyStatefile(setupFile);

            if (!modified) {
                logEvent(this, Log.DBG, "Error occured while modifying the statefile " + setupFile.getAbsolutePath());
                if (invalidPortFound == true) {
                logEvent(this, Log.ERROR, "Error occured while searching for unused port.  Please make sure one from each of the 3 following port ranges is not in use:\n\t4848 - 4858\n\t8081 - 8091\n\t1043 - 1053\nClean up the partial install and rerun the installer."); 
                }
		//InstallerExceptions.setErrors(true);
                setAppserverExitCode(AS_UNHANDLED_ERROR);
                return;
            }
            mutableOperationState.setStatusDetail(imageDirPath);
            
            String cmdArray[] = new String[1];
            // To be cleaned up later
            if (Util.isWindowsOS()) {
                cmdArray[0] = "\"" + asSetupDirPath + File.separator 
		    + INSTALL_BAT + "\"";
            } else {
                cmdArray[0] = instDirPath + File.separator + INSTALL_SH;
            }

            logEvent(this, Log.DBG,"****RunCommand Start " );
            runCommand(cmdArray, support);
            logEvent(this, Log.DBG,"****RunCommand End " );
 
	    // installPermanentLicense();
	
            //for debugging purposes, allow not to remove instDirPath
            boolean cleanInstDir = !(Boolean.getBoolean("keep.as_inst"));
            logEvent(this, Log.DBG,"cleanInstDir -> " + cleanInstDir);
            statusDesc = resolveString("$L(com.sun.installer.InstallerResources,AS_CLEAN_INST_DIR_DESCRIPTION)");
            mutableOperationState.setStatusDescription(statusDesc);

            if (cleanInstDir) {
		if (Util.isWindowsOS()) {
		    Util.deleteDirectory(new File(asSetupDirPath), this);
		    mutableOperationState.setStatusDescription("");
		    logEvent(this, Log.DBG,"Deleted contents of: " + asSetupDirPath);
		} else {
		    File script = new File(instDirPath, INSTALL_SH);
		    if (script.exists()) {
			script.delete();
			logEvent(this, Log.DBG,"Deleted file: " + script.getAbsolutePath());
		    }
		    String installerName = null;
		    if (Util.isWindowsOS()) {
			//installerName = AS_EXEC_NAME_WINDOWS;
		    } else if (Util.isLinuxOS()) {
			//installerName = AS_EXEC_NAME_LINUX;
		    } 
		    else if (Util.isSunOS()) {
			String arch = (String) System.getProperty("os.arch");
			if (arch.startsWith("sparc")) {
			    //installerName = AS_EXEC_NAME_SPARC;
			} else {
			    //installerName = AS_EXEC_NAME_X86;
			}
		    }
		    if (installerName != null) {
			File installer = new File(instDirPath, installerName);
			if (installer.exists()) {
			    installer.delete();
			    logEvent(this, Log.DBG,"Deleted file: " + installer.getAbsolutePath());
			}
		    }
		}
	    }
            removeAppserverFromAddRemovePrograms();
            cleanAppserverStartMenu();

        }catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
	/* Create the file the plugin uses to determine how to
	 * configure itself.
	 */
	if (!createJ2EESDKPropertiesFile()) {
	    //InstallerExceptions.setWarnings(true);
	}

        logEvent(this, Log.DBG,"Appserver installation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    public void uninstall(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        logEvent(this, Log.DBG,"Uninstalling -> ");
        statusDesc = resolveString("$L(com.sun.installer.InstallerResources,UNINSTALL_WAIT_MSG)");
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = UNINSTALL;
            String scriptName;
            if (Util.isWindowsOS()) {
		scriptName = UNINSTALL_BAT;
	    } else {
		scriptName = UNINSTALL_SH;
	    }
            String uninstallScriptPath = instDirPath + File.separator + scriptName;
            if (!(new File(uninstallScriptPath).exists())) {
		//InstallerExceptions.setErrors(true);
		logEvent(this, Log.ERROR, "Cannot uninstall due to missing uninstall script: " + uninstallScriptPath);
                throw new Exception("Cannot uninstall due to missing uninstall script: " + uninstallScriptPath);
            }
            
            File classFile = new File(imageDirPath, "appserv_uninstall.class");
            if (!(classFile.exists())) {
		//InstallerExceptions.setErrors(true);
		logEvent(this, Log.ERROR, "Cannot uninstall due to missing uninstall class: " + classFile.getAbsolutePath());
                throw new Exception(classFile.getAbsolutePath() + " cannot be found.");
            }
            
            String cmdArray[] = new String[1];
	    if (Util.isWindowsOS()) {
	    cmdArray[0] = "\"" + uninstallScriptPath + "\"";
	    } else {
		cmdArray[0] = uninstallScriptPath;
	    }

            runCommand(cmdArray, support);

             //for debugging purposes, remove imageDirPath
            boolean cleanImageDir = Boolean.getBoolean("remove.as_image");
            logEvent(this, Log.DBG,"cleanImageDir -> " + cleanImageDir);
            cleanImageDir = true; // Force it to clean for now
            if (success && cleanImageDir) {
                logEvent(this, Log.DBG,"Deleting -> " + imageDirPath);
                Util.deleteDirectory(new File(imageDirPath));
                logEvent(this, Log.DBG,"Deleted -> " + imageDirPath);
            }            
        }catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"Appserver uninstallation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, ProductActionSupport support)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            String prevLogFile = getPELogPath(getPEDirLogPath());

            /*String workingDirectory = (installMode == INSTALL) ? instDirPath : imageDirPath;
            String env = "user.dir=" + workingDirectory;
            runCommand.execute(cmdArray, new String[] {env}, new File(workingDirectory));*/
            
            runCommand.execute(cmdArray, null, null);
            
            if ((installMode == INSTALL) && doProgress)
                startProgress();
            
            if (Util.isWindowsOS()) {
                //UGLY HACK: make sure there are enough time elapsed before starting to flush
                int ms = (installMode == INSTALL) ? 2000 : 4500;
                Thread.currentThread().sleep(ms);
            }
            
            //int status;

	    // Strange bug cropped up in Windows uninstall via Add/Remove window.
	    // Got NPE while executing the flush cmd.
	    if (runCommand.getInputReader() != null) {
		logEvent(this, Log.DBG,"Flushing ...!");
		runCommand.flush();
		logEvent(this, Log.DBG,"Flushing done!");
	    }
            int status = runCommand.getReturnStatus();
            logEvent(this, Log.DBG, "status code = " + status + " which is " + ((status == 73 || status == 72 || status == 0) ? "successful" : "unsuccessful")); 
            if (!isCompletedSuccessfully() || ((status != 0) && status != (installMode == INSTALL ? 73 : 72))) {
                String mode = (installMode == INSTALL) ? "install" : "uninstall";
                String command = Util.arrayToString(cmdArray, " ");
                logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + command);
                logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + command);

                //InstallerExceptions.setErrors(true);
                setAppserverExitCode(AS_UNHANDLED_ERROR);

                String currentLogFile = getPELogPath(getPEDirLogPath());
                logEvent(this, Log.DBG, "currentLogFile = " + currentLogFile);
                logEvent(this, Log.DBG, "prevLogFile = " + prevLogFile);
                if (currentLogFile != null && !prevLogFile.equals(currentLogFile)) {
                     logEvent(this, Log.DBG, "there is a log file");
                     logEvent(this, Log.ERROR, "Error occured while " + resolveString("$L(com.sun.installer.InstallerResources,APP_SERVER_INSTALL_DESCRIPTION)") + "View log file " + currentLogFile + " for more details.");
                } else {
                     currentLogFile = getPEDirLogPath();
                     String tmp = ".";
                     if (status == 50) {
                          tmp = resolveString("$L(com.sun.installer.InstallerResources, PE_FATAL)");
                     }

                     logEvent(this, Log.DBG, "there is NO log file");
                     logEvent(this, Log.ERROR, "Error occured while " + resolveString("$L(com.sun.installer.InstallerResources,APP_SERVER_INSTALL_DESCRIPTION)") + tmp);
                }
            } else {
                System.getProperties().put("appserverHome", imageDirPath);
            }
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
            // System.out.println("getPELogPath() = " + (getPELogPath(getPEDirLogPath())));

	    // Strange bug cropped up in Windows uninstall via Add/Remove window.
	    // Got NPE  during first flush above so check before executing flush.
	    if (runCommand.getInputReader() != null) {
		logEvent(this, Log.DBG,"Flushing 2...!");
		runCommand.flush();
		logEvent(this, Log.DBG,"Flushing 2 done!");
	    }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private void setAppserverExitCode(int code) {
          try {
              ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
              ecservice.setExitCode(code);
          } catch (Exception ex) {
              logEvent(this, Log.ERROR, "Couldn't set exit code. "); 
          }
    }

    private void installPermanentLicense() {
	// install the permanent app server license if app server was installed
	if (success) {
	    File as8License;
   
	    if (Util.isWindowsOS()) {
		String installDir = (String)System.getProperties().get("installDir");
		as8License  = new File(installDir + File.separator + IMAGE_DIRECTORY_NAME + 
				       File.separator + "config",  AS8_LICENSE);
	    } else {
		String configDir = (String)System.getProperties().get("AS_INSTALL_CONFIG_DIR");
		logEvent(this, Log.DBG,"App Server config dir: " + configDir);
		as8License  = new File(configDir,  AS8_LICENSE);
	    }
	    File permLicense = new File(instDirPath, PERM_LICENSE);
	    logEvent(this, Log.DBG,"Source File: " + permLicense.getAbsolutePath());
	    logEvent(this, Log.DBG,"Destination File: " + as8License.getAbsolutePath());

	    if (permLicense.exists()) {
		try {
		    if (as8License.exists()) {
			as8License.delete();
			logEvent(this, Log.DBG,"Deleted File: " + as8License.getName());
		    }
		    Util.copyFile(permLicense, as8License);
		    logEvent(this, Log.DBG,"Copied source to dest.");
		} catch (SecurityException secerr) {
		    logEvent(this, Log.ERROR, "Could not delete a license file " + secerr);
		} catch (Exception msg) { 
		    logEvent(this, Log.ERROR, msg);
		}
	    } else {
		String errmsg = "Cannot find Permanent License File: " + 
		                 permLicense.getAbsolutePath();
		logEvent(this, Log.ERROR, errmsg);
		System.out.println(errmsg);
	    }
	}
}
    /** check whether or not the un/installation was successful*/
    private boolean isCompletedSuccessfully() {
        File file = new File(imageDirPath, "appserv_uninstall.class");
        if (installMode == UNINSTALL) {
            //check for the following file. If it doesn't exists, the uninstallation didn't go thru
            success = !(file.exists());
        } else if (installMode == INSTALL) {
            success = (file.exists());
        }
        logEvent(this, Log.DBG, "success is " + success);
        System.getProperties().put("isAppServerInstallationSuccessful",new Boolean(success));
        return success;
    }
    
    /**returns whther the file was modified successfully or not */
    private boolean modifyStatefile(File setupFile)
    throws Exception {
        File setupFileNew = new File(setupFile.getAbsolutePath() + ".new");
        
        BufferedReader reader = new BufferedReader(new FileReader(setupFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(setupFileNew));
        
        logEvent(this, Log.DBG,"InstallApplicationServerAction: in modifyStatefile(): setupFile=" + setupFile.getAbsolutePath() + "; setupFileNew=" + setupFileNew.getAbsolutePath());
        
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("defaultInstallDirectory")) {
                line = "defaultInstallDirectory = " + imageDirPath;
            }
            else if (line.startsWith("currentInstallDirectory")) {
                line = "currentInstallDirectory = " + imageDirPath;
            }
            else if (line.startsWith("JDK_LOCATION")) {
                String jdkHome = (String)System.getProperties().get("jdkHome");
                line = "JDK_LOCATION = " + jdkDirPath; 
            }
            else if (line.startsWith("INST_ASADMIN_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_ASADMIN_PORT");
                // Make sure check the "adminPort" property in silent mode
                String portNumber;
                if (adminPort != null && adminPort.length() > 0) {
                    portNumber = adminPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                logEvent(this, Log.DBG,"Start with adminPort =" + portNumber);
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
		adminPortNumber = validPortNum;
                line = "INST_ASADMIN_PORT = " + validPortNum;
                System.getProperties().put("adminPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused adminPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid adminPort =" + validPortNum);
                }
            }
            else if (line.startsWith("INST_ASWEB_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_ASWEB_PORT");
                String portNumber;
                if (webPort != null && webPort.length() > 0) {
                    portNumber = webPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
                line = "INST_ASWEB_PORT = " + validPortNum;
                System.getProperties().put("webPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused asWebPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid webPort =" + validPortNum);
                }
            }
            else if (line.startsWith("INST_HTTPS_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_HTTPS_PORT");
                String portNumber;
                if (httpsPort != null && httpsPort.length() > 0) {
                    portNumber = httpsPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
                line = "INST_HTTPS_PORT = " + validPortNum;
                System.getProperties().put("httpsPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused httpsPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid httpsPort =" + validPortNum);
                }
            }
            /*
            else if (line.startsWith("INST_ASADMIN_USERNAME = ")) {
                String name = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("INST_ASADMIN_USERNAME",name);
            }
            else if (line.startsWith("INST_ASADMIN_PASSWORD = ")) {
                String passwd = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("INST_ASADMIN_PASSWORD", passwd);
            }
            else if (line.startsWith("INST_ASADMIN_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_ASADMIN_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                // int validPortNum =  getValidPortNumber(this, portNumber, 10);
                // Make sure check the "adminPort" property in silent mode
                int validPortNum =  findValidPortNumber(portNumber, 10);
                line = "INST_ASADMIN_PORT = " + validPortNum;
                System.getProperties().put("INST_ASADMIN_PORT",String.valueOf(validPortNum));
            }
            else if (line.startsWith("INST_ASWEB_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_ASWEB_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                int validPortNum =  getValidPortNumber(this, portNumber, 10);
                line = "INST_ASWEB_PORT = " + validPortNum;
                System.getProperties().put("INST_ASWEB_PORT",String.valueOf(validPortNum));
            }
            else if (line.startsWith("INST_HTTPS_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_HTTPS_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                int validPortNum =  getValidPortNumber(this, portNumber, 10);
                line = "INST_HTTPS_PORT = " + validPortNum;
                System.getProperties().put("INST_HTTPS_PORT",String.valueOf(validPortNum));
            }
            */
            else if (line.startsWith("AS_INSTALL_CONFIG_DIR")) {
                logEvent(this, Log.DBG,"Checking AS_INSTALL_CONFIG_DIR");
                String appserverConfigDir = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("AS_INSTALL_CONFIG_DIR", appserverConfigDir);
            }
            logEvent(this, Log.DBG,line);
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
        logEvent(this, Log.DBG,"Finished modifying the file " + setupFileNew.getAbsolutePath());
        
        if (!setupFile.delete()) {
            logEvent(this, Log.ERROR, "Could not delete - " + setupFile.getAbsolutePath());
            return false;
        }
        
        logEvent(this, Log.DBG,"Removing " + setupFile.getAbsolutePath());
        
        writer.close();

        // Now if cannot locate unused port, return false
        if (invalidPortFound) {
            logEvent(this, Log.ERROR, "Could not find valid port -  aborting the install of PE");
            return false;
        }
        
        if (!setupFileNew.renameTo(setupFile)) {
            logEvent(this, Log.ERROR, "Could not rename - " + setupFileNew.getAbsolutePath());
            return false;
        }
        logEvent(this, Log.DBG,"Renaming " + setupFileNew.getAbsolutePath() + " to " + setupFile.getAbsolutePath());        
        return true;
    }
    
    /** Validates the different port numbers(orb,http,web,pointbase)
     * @param portNumber initial portNumber to validate
     * @param numberOfAttempts number of attempts made to find a valid port  
     *                         number. if the inital portNumber is not valid. 
     *                         -1 indicates forever.
     */
    static public int RANDOM_PORT_NUMBER = 3566;
    static public int getValidPortNumber(com.installshield.util.Log logger, 
					 String portNumber, int numberOfTries)
    throws Exception{
	String serverName = "localhost";
	if(Util.isLinuxOS()) {
	    try {
		serverName = java.net.InetAddress.getLocalHost().getHostName();
	    } catch (Exception ex) {
		Util.logStackTrace(logger, ex);
	    }
	}
        
        logger.logEvent(logger, logger.DBG,"serverName -> " + serverName);

        int intportNumber=0;
        if(portNumber == null || portNumber.length() == 0 ||
        portNumber.length() > 5)
            portNumber = Integer.toString(RANDOM_PORT_NUMBER); //default number
        try {
            intportNumber = Integer.parseInt(portNumber);
        }
        catch (NumberFormatException dummy) {
            
        }
        
        /**    if portnumber does not match the range then return false **/
        if(intportNumber <= 0 || intportNumber > 65535)
            intportNumber = RANDOM_PORT_NUMBER;
        
        boolean forever = (numberOfTries < 0) ? true : false;
        while (forever || (numberOfTries >= 0)) {
            try {
                Socket socket = new Socket(serverName, intportNumber);
                OutputStream theOutputStream = socket.getOutputStream();
                InputStream theInputStream = socket.getInputStream();
                theOutputStream.close();
                theOutputStream = null;
                theInputStream.close();
                theInputStream = null;
                socket.close();
                socket = null;
            }
            catch (Exception ex) {
                /**Valid Port/Socket **/               
                //System.out.println("***ValidPort*** -> " + intportNumber + "\n" + ex);
                return intportNumber;
                
            }
            /**InValid Port/Socket **/           
            //System.out.println("InvalidPort-> " + intportNumber + "\n" );
            if (!forever) {
                --numberOfTries;
                if (numberOfTries < 0)
                    throw new RuntimeException("No available ports found.") ;
            }
            intportNumber = ( ++intportNumber > 65535) ?  0 : intportNumber;
        }
        //should never reach here
        return -1;
    }
    
    /**
     * List the files which shouldn't be cleaned up after installation
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File pathname) {
        String path = pathname.getAbsolutePath();
        if (installMode == INSTALL) {
            if ( path.equals(instDirPath + File.separator + UNINSTALL_SH)
            || path.equals(instDirPath + File.separator + UNINSTALL_BAT)
            || path.equals(instDirPath + File.separator + "uninstall.dat")
            || path.equals(instDirPath + File.separator + "uninstall.bin")
            || path.equals(instDirPath + File.separator + "uninstall.exe")
            || path.equals(instDirPath + File.separator + "_jvm"))
                return false;  
        }
        else if (installMode == UNINSTALL) {
            if ( path.equals(imageDirPath + File.separator + "uninstall.log"))
                return false;  
        }
        
        return true;
    }
    
    /**removes the Appserver entry in Add/Remove Programs panel*/
    public void removeAppserverFromAddRemovePrograms()
    throws ServiceException{
        if (Util.isWindowsOS()) {
            logEvent(this, Log.DBG,"Updating Add/Remove Programs ...");
            Win32RegistryService regserv = (Win32RegistryService)getService(Win32RegistryService.NAME);
            regserv.deleteKey(Win32RegistryService.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall", "Sun Java System Application Server Platform Edition", false);
        }
    }
    
    /**removes the Uninstall menu item from Appserver start menu*/
    public void cleanAppserverStartMenu() {
        if (Util.isWindowsOS()) {
            String folder = "Sun Microsystems" + File.separator +  "J2EE 1.4 SDK";
            logEvent(this, Log.DBG,"Removing Appserver Uninstall From Programs Menu ->" + folder);
            try {
                DesktopService ds = (DesktopService)getService(DesktopService.NAME);
                // context help see: $(ISMP)\InstallShield\MP50\platforms\win32\index.html
		String context = "$UserPrograms$";
                logEvent(this, Log.DBG,"Attr: -> " + ds.getDesktopFolderAttributes(context, folder).toString());
                ds.removeDesktopItem(context, folder, "Uninstall");
		logEvent(this, Log.DBG, "remove menu item Uninstall");
            }catch (ServiceException se){
                se.printStackTrace();
            }
        }
    }

    public String getPEDirLogPath() {
        String dirPath = "";

        /* Determine if admin */
        
        boolean isAdmin = Util.isAdmin();

        /* Which platform first */
        if (Util.isWindowsOS()) {
            dirPath = tmpDir;
        } else if (Util.isLinuxOS()) {
            dirPath = ((isAdmin) ? "/var/log/wizards":"/var/tmp");
        } else if (Util.isSunOS()) {
            dirPath = ((isAdmin) ? "/var/sadm/install/logs":"/var/tmp");
        }
        return dirPath;
    }

    /** Gets the path of the pointbase jar*/
    public String getPELogPath(String dirPath) {

        File logDir = new File(dirPath);
        
        if (!logDir.exists()) {
            // System.out.println( "Directory doesn't exists - " + logDir.getAbsolutePath());
            return null;
        }
        
        FileFilter ff = new InstallApplicationServerAction.PEFileFilter();
        File[] list = logDir.listFiles(ff);

        if (list == null || list.length < 1){
            // System.out.println("*.log file not found in - " + logDir.getAbsolutePath());
            return null;
        }
        
        String recentFilePath = (String) list[list.length - 1].getAbsolutePath();
        // System.out.println("log path = " + recentFilePath);

        return recentFilePath;
    }


    /** Gets the path of the pointbase jar*/
    static String getPointbaseJarPath(String pathToPointbase) {
        if (pathToPointbase == null) return null;
        File pbaseLibDir = new File(pathToPointbase + File.separator +
        "client_tools" + File.separator +
        "lib") ;
        
        if (!pbaseLibDir.exists()) {
            //System.out.println( "Directory doesn't exists - " + pbaseLibDir.getAbsolutePath());
            return null;
        }
        
        FileFilter ff = new InstallApplicationServerAction.PointbaseFileFilter();
        File[] list = pbaseLibDir.listFiles(ff);
        //there will be only 1 jar file specified in the filter
        if (list == null){
            //System.out.println("pbclient*.jar file not found in - " + pbaseLibDir.getAbsolutePath());
            return null;
        }
        
        return list[0].getAbsolutePath();
    }
    
    /** Appends the location of the pointbase jar to the ide.cfg file*/
    static void updateIDECfgFile(String pathToPointbase) {
        try {
            String xpFlags = null;
            if (Util.isWindowsXP()) {
                xpFlags = "-J-Dsun.java2d.noddraw=true -J-Dsun.java2d.d3d=false";
                //System.out.println("xpFlags -> " + xpFlags );
            }
            
            String jarFilePath = getPointbaseJarPath(pathToPointbase);
            
            if ((xpFlags != null) || (jarFilePath != null)) {
                String forteHome=(String)System.getProperties().get("installDir");
                String cfgFile = forteHome + File.separator + "bin" + File.separator + "ide.cfg";
                
                //System.out.println("jarFilePath -> " + jarFilePath );
                //System.out.println("cfgFile -> " + cfgFile );
                
                RandomAccessFile file = new RandomAccessFile(cfgFile, "rwd");
                file.seek(file.length());
                if (xpFlags != null) {
                    file.writeBytes(xpFlags);
                    file.writeBytes("\n");
                }
                //make a new entry
                file.writeBytes("-cp:a " + jarFilePath);
                file.writeBytes("\n");
                file.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    /**Returns checksum for appserver directory in bytes*/
    public long getCheckSum() {
        if (Util.isWindowsOS()) {
            return 103000000L;
        }
        else if (Util.isSunOS()) {
            return 115000000L;
        }
        else if (Util.isLinuxOS()) {
            return 100000000L;
        }
        return 0L;
    }

    public void setTmpDir() {

        if (tmpDir != null && tmpDir.length() > 0)
            return;

        tmpDir = (String)System.getProperties().get("tmpDir");
        if ((tmpDir == null) || tmpDir.length() < 1) {
            tmpDir = resolveString("$D(temp)");
        }
        logEvent(this, Log.DBG,"in getTmpDir(): tmpDir -> " + tmpDir);
    }
        
    /* Returns the required bytes table information for application server.  
     * @return required bytes table for application server.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
	
        String imageDirPath = getProductTree().getInstallLocation(this) + File.separator + IMAGE_DIRECTORY_NAME;
        RequiredBytesTable req = new RequiredBytesTable();
        logEvent(this, Log.DBG,"imageDirPath -> " + imageDirPath);
        req.addBytes(imageDirPath , getCheckSum());

        setTmpDir();

        logEvent(this, Log.DBG,"in getRequiredBytes(): tmpDir -> " + tmpDir);
        // same for all platforms, actually Solaris is biggest
        req.addBytes(tmpDir , 40000000L);

        return req;            
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
	int count = 0;
        while (progressThread.isAlive() && count < 20) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            // Sometimes the 1st interrupt is not good enough
	    if (count == 10)
		progressThread.interrupt();
	    else if (count == 15) {
                // now try something else instead of interrupt
		progressThread.setLoop(false);
	    }

            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
	    count++;
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
		logEvent(this, Log.ERROR, "FileService is null. Cannot set file as executable: " + filename);
		return false;
	    }
	    fileService.setFileExecutable(filename);
	} catch (Exception ex) {
            logEvent(this, Log.ERROR, "Cannot set file as executable: " + filename
		     + "\nException: " + ex);
	    return false;
	}
	return true;
    }
    
    /** Create the App Server install or uninstall script from the provided 
     *  template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createScript(String template, String scriptName, int scriptType)
	throws Exception {
	logEvent(this, Log.DBG, "Creating script: " + scriptName);
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have the _uninst dir as parent
	}

	File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
	String logfile;
	if (scriptType == INSTALL) {
	    logfile = "install.log";
	} else {
	    logfile = "uninstall.log";
	}
        String installerName = findASInstaller();
	// Replace the script variables with real values
	if (Util.isWindowsOS()) {
	    winScriptSetup(reader, writer, logfile, installerName, scriptType);
	} else {
	    unixScriptSetup(reader, writer, logfile, installerName);
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
	} else {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " could not be set as executable file.");
	}
	return false;
    }

    private void winScriptSetup(BufferedReader reader, BufferedWriter writer, 
        String logfileName, String installerName, int type) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET EXECNAME")) {
                line = "SET EXECNAME=" + installerName;
            } else if (line.startsWith("SET APPSERVERDIR")) {
                line = "SET APPSERVERDIR=\"" + imageDirPath + "\"";
            } else if (line.startsWith("SET INSTDIR")) {
		if (type == INSTALL) {
		    line = "SET INSTDIR=\"" + asSetupDirPath + "\"";
		} else {
		    line = "SET INSTDIR=\"" + instDirPath + "\"";
		}
            } else if (line.startsWith("SET STATEFILE")) {
                line = "SET STATEFILE=\"" + statefilePath + "\"";
            } else if (line.startsWith("SET LOGFILE")) {
                line = "SET LOGFILE=\"" + nbInstallDir + logfileName + "\"";
            } else if (line.startsWith("SET TMPDIR")) {
                line = "SET TMPDIR=\"" + tmpDir + "\"";
            } else if (line.startsWith("SET DRIVE")) {
                line = "SET DRIVE="
		+ instDirPath.substring(0, instDirPath.indexOf(File.separator));
            } else if (line.startsWith("SET JAVAHOME")) {
                line = "SET JAVAHOME=\"" + jdkDirPath + "\"";
	    }
            writer.write(line + System.getProperty("line.separator"));
        }
    }

    private void unixScriptSetup(BufferedReader reader, BufferedWriter writer,
        String logfileName, String installerName) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("EXECNAME")) {
                line = "EXECNAME=" + installerName;
            } else if (line.startsWith("APPSERVERDIR")) {
                line = "APPSERVERDIR=" + imageDirPath;
            } else if (line.startsWith("INSTDIR")) {
                line = "INSTDIR=" + instDirPath;
            } else if (line.startsWith("STATEFILE")) {
                line = "STATEFILE=" + statefilePath;
            } else if (line.startsWith("LOGFILE")) {
                line = "LOGFILE=" + nbInstallDir + logfileName;
            } else if (line.startsWith("TMPDIR")) {
                line = "TMPDIR=" + tmpDir;
            } else if (line.startsWith("JAVAHOME")) {
                line = "JAVAHOME=" + jdkDirPath;
	    }
            writer.write(line + System.getProperty("line.separator"));
        }
    }
    
    private String findASInstaller () {
	String installerName = null;
	String arch = (String) System.getProperty("os.arch");
        File installDirFile = new File(nbInstallDir);
        logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        if (Util.isWindowsOS()) {
            //Try to locate Windows AS installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith("sjsas_pe-8_1") && (children[i].getName().indexOf("windows") != -1) &&
                    children[i].getName().endsWith(".exe")) {
                    installerName = children[i].getName();
                    break;
                }
            }
        } else if (Util.isLinuxOS()) {
            //Try to locate Linux AS installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith("sjsas_pe-8_1") && (children[i].getName().indexOf("linux") != -1) &&
                    children[i].getName().endsWith(".bin")) {
                    installerName = children[i].getName();
                    break;
                }
            }
	} else if (Util.isSunOS()) {
	    if (arch.startsWith("sparc")) {
                //Try to locate Solaris Sparc JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("sjsas_pe-8_1") && (children[i].getName().indexOf("solaris-sparc") != -1) && 
                        children[i].getName().endsWith(".bin")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    } else {
                //Try to locate Solaris X86 JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("sjsas_pe-8_1") && (children[i].getName().indexOf("solaris-i586") != -1) && 
                        children[i].getName().endsWith(".bin")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    }
	}
        if (installerName != null) {
            logEvent(this, Log.DBG, "findASInstaller AS installer found: " + installerName);
        } else {
            logEvent(this, Log.DBG, "findASInstaller AS installer NOT found. AS cannot be installed.");
            installerName = "as-installer-not-found";
        }
        return installerName;
    }
    
    private boolean createJ2EESDKPropertiesFile() {
	String filename = nbInstallDir + J2EESDK_PROP_FILE; 
        File file = new File(filename);
	logEvent(this, Log.DBG, "Creating J2SDKEE properties file: " + 
		 file.getAbsolutePath());
	try {
	    FileService fileService = (FileService)getService(FileService.NAME);
	    if (fileService == null) {
		logEvent(this, Log.ERROR, "FileService is null. Cannot create directory for: " +  file.getAbsolutePath());
		return false;
	    }
	    fileService.createDirectory(file.getParent());
	} catch (Exception ex) {
            logEvent(this, Log.ERROR, "Cannot create the directory for: " 
		     + file.getAbsolutePath() + "\nException: " + ex);
	    return false;
	}
	try {
	    PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(file)));
	    
	    ps.println("serverName=J2EESDK");
	    ps.println("url=deployer:Sun:AppServer::localhost:" + adminPortNumber);
	    
	    ps.close();
        } catch (java.io.IOException e) {
            logEvent(this, Log.ERROR, file.getAbsolutePath() 
		     + " file may not have been created.");
	    return false;
        }
	return true;
    }
    
    
    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        private File appserverDir;
        
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
        private BufferedReader logFileReader = null;
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            lastPathShown = imageDirPath;
            appserverDir = new File(imageDirPath);
            logFile = new File(imageDirPath, "install.log");
            checksum = getCheckSum();
            
            if(Util.isSunOS()) {
                unzipLog = new File(instDirPath, "unzip.log");
                isUnzipping = true;
                startTime = System.currentTimeMillis();
                String statusDesc2 = resolveString("$L(com.sun.installer.InstallerResources,AS_UNZIPPING_MSG)");
                mos.setStatusDescription(statusDesc + "\n" + statusDesc2);
            }
        }
        
	public void setLoop(boolean b) {
	    loop = b;
	}

        public void run() {
            int sleepTime = 1000;
            while (loop) {
                logEvent(this, Log.DBG,"looping");
                try {
                    if (appserverDir.exists()) {
                        //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                        updateProgressBar();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDetail");
                        updateStatusDetail();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDescription");
                        if (doStatusDescUpdate) updateStatusDescription();
                        sleepTime = 1200;
                    }
                    else {
                        if (isUnzipping) updateUnzippingInfo();
                        else updateStatusDetail();
                        sleepTime = 2000;
                    }
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) return;
                } catch (InterruptedIOException ex) {
                    //ex.printStackTrace();
                    loop = false;
                    return;
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
            logEvent(this, Log.DBG,"Finishing");
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
            
            stopReader(logFileReader);
        }
        
        /**check if the operation is canceled. If not yield to other threads.*/
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
               
        /** Updates the progress bar*/
        private void updateProgressBar() {
            if (isCanceled()) return;

            long size = Util.getFileSize(appserverDir);
            long perc = (size * 100) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted)
                return;
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
        /** Updates the status detail*/
        public void updateStatusDetail() {
            if (isCanceled()) return;
            if (!appserverDir.exists()) {
                mos.setStatusDetail(getDisplayPath(lastPathShown));
                logEvent(this, Log.DBG,"StatusDetailThread-> " + lastPathShown + " NOT created yet");
                return;
            }
            String recentFilePath = fileComp.getMostRecentFile(appserverDir).getAbsolutePath();
            logEvent(this, Log.DBG,"StatusDetailThread-> " + recentFilePath + "  MODIFIED!!!");
            mos.setStatusDetail(getDisplayPath(recentFilePath));
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
        
        public void updateStatusDescription()
        throws Exception{
            if (isCanceled()) return;
            try{
                if (logFileReader == null) {
                    if (!logFile.exists()) {
                        logEvent(this, Log.DBG,"StatusDescriptionThread-> Logfile NOT created yet");
                        return;
                    }
                    logEvent(this, Log.DBG,"StatusDescriptionThread-> Logfile CREATED!!!");
                    logFileReader = new BufferedReader(new FileReader(logFile));
                    success = true;
                }
                
                if (logFileReader.ready()) {
                    String line = null;
                    while ((line = logFileReader.readLine()) != null) {
                        logEvent(this, Log.DBG,"line = " + line);
                        //check if there is an error
                        if (success && (line.toLowerCase().indexOf("error") != -1)) {
                            success = false;
                            mos.setStatusDescription(statusDesc + "\n" + line);
                        }
                    }                    
                }
            } catch (Exception ex) {
                mos.setStatusDescription("");
                stopReader(logFileReader);
                doStatusDescUpdate = false;
                if ((ex instanceof InterruptedIOException) 
                   || (ex instanceof InterruptedException)) {
                    throw ex;
                }
            }
        }
        
        public void updateUnzippingInfo() {
            if (isCanceled()) return;
            try{
                if (unzipLogReader == null) {
                    if (!unzipLog.exists())  return;
                    unzipLogReader = new BufferedReader(new FileReader(unzipLog));
                }
                
                if (unzipLogReader.ready()) {
                    String line = null;
                    while ((line = unzipLogReader.readLine()) != null) {
                        if (line.equalsIgnoreCase("DONE")) {
                            throw new Exception();   
                        }
                        else
                            mos.setStatusDetail(line);                      
                    }                   
                }
            } catch (Exception ex) {
                isUnzipping = false;
                mos.setStatusDetail("");
                stopReader(unzipLogReader);
                mos.setStatusDescription(statusDesc);
            }
        }
 
        private void stopReader(BufferedReader reader) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
                reader = null;
            } 
        }
    }
    
    
    /** FileFilter to extract the pointbase jarfile name*/
    static public class PointbaseFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            String name = pathname.getName();
            if ( name.startsWith("pbclient") &&
            name.endsWith(".jar"))
                return true;
            return false;
        }
    }

    /** FileFilter to extract the PE logfile name*/
    static public class PEFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            String name = pathname.getName();

            String prefix = (installMode == INSTALL ? "Install" : "Uninstall") + "_Application_Server_8PE_";
            if ( name.startsWith(prefix) &&
            name.endsWith(".log"))
                return true;
            return false;
        }
    }
}
