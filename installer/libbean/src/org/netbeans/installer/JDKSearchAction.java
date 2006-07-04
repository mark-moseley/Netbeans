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

package org.netbeans.installer;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import com.installshield.util.Log;
import com.installshield.wizard.CancelableWizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.platform.win32.Win32RegistryService;

public class JDKSearchAction extends CancelableWizardAction  {
    
    Vector jdkHomeList = new Vector();
    private String currentJDKHome = null;
    
    public JDKSearchAction() {
        Util.setJdkHomeList(jdkHomeList);
    }
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass(JDKVersion.class.getName());
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(JDKInfo.class.getName());
            support.putClass(JDKInfoAux.class.getName());
            support.putClass(SolarisRoutines.class.getName());
            support.putClass(Verify.class.getName());
            support.putRequiredService(Win32RegistryService.NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        
        // Return if jdkHomeList is not empty. This is an work around
        // to make the action not to search the JDKs if the user click
        // back button
        
        if (!jdkHomeList.isEmpty()) {
            return;
        }
        
        // Check if user specified jdkHome (alias jh) in the command Line
        // jdkHome property always overrides the alias jh
        String jdkHome = Util.getJdkHome();
        if (jdkHome == null) {
            jdkHome = "";
        }
        
        // If JDKHome is specified and is a valid one then
        // do not do the search.
        if (jdkHome.length() > 0) {
            if (JDKInfo.checkJdkHome(this, jdkHome)) {
                Util.setJdkHome(jdkHome);
                addToList(jdkHome);
                // in this case the list has only one item
                Util.setJdkHomeList(jdkHomeList);
                return;
            }
        }
        
        currentJDKHome = Util.getCurrentJDKHome();
        if (currentJDKHome == null) {
            JDKInfo jdkInfo = JDKInfo.getCurrentJDKInfo(this, false);
            if (jdkInfo.isJDKType()) {
                currentJDKHome = jdkInfo.getHome();
            }
        }

        //Handle silent mode where getUserInterface returns null
        if (evt.getUserInterface() != null) {
            String searchMsg = resolveString("$L(org.netbeans.installer.Bundle, JDKSearchAction.searchMessage)");
            evt.getUserInterface().setBusy(searchMsg);
        }
        
        if (Util.isWindowsOS()) {
            findWindowsJDK();
        } else {
            findUnixJDK();
        }
        
        Util.setJdkHomeList(jdkHomeList);
    }
    
    void findWindowsJDK(){
        String jdkHome;
        
        // Try the current jdk
        if (currentJDKHome != null) {
            addToList(currentJDKHome);
        }
        
        // On Win32 the following logic doesnt work and makes the
        // System to hang. So skip the steps.
        if (Util.isWindows98()) 
            return;
        
        // Try the Windows Registry
        try {
            logEvent(this, Log.DBG,"Checking Win32 Registry ... ");
            Win32RegistryService regserv = (Win32RegistryService)getService(Win32RegistryService.NAME);
            int HKLM = Win32RegistryService.HKEY_LOCAL_MACHINE;
            //SUN JDK
            String HKEY_jdk = "Software\\JavaSoft\\Java Development Kit";
            if (regserv.keyExists(HKLM, HKEY_jdk)) {
                String[] subKeyNames = regserv.getSubkeyNames(HKLM, HKEY_jdk);
                if (subKeyNames.length != 0) {
                    for (int i = 0; i < subKeyNames.length; i++) {
                        try {
                            logEvent(this, Log.DBG,"JavaSoft subkey: " + subKeyNames[i]);
                            if (Util.isAboveOrEqualMinimumVersion("1.4.2", subKeyNames[i])) {
                                String HKEY_jdkVersion = HKEY_jdk + "\\" + subKeyNames[i];
                                jdkHome  = regserv.getStringValue(HKLM, HKEY_jdkVersion, "JavaHome", false);
                                if (JDKInfo.checkJdkHome(this, jdkHome)) {
                                    addToList(jdkHome);
                                }
                            }
                        } catch (Exception ex) {
                            Util.logStackTrace(this, ex);
                        }
                    }
                }
            }
            //IBM JDK
            HKEY_jdk = "Software\\IBM\\Java Development Kit";
            if (regserv.keyExists(HKLM, HKEY_jdk)) {
                String[] subKeyNames = regserv.getSubkeyNames(HKLM, HKEY_jdk);

                if (subKeyNames.length != 0) {
                    for (int i = 0; i < subKeyNames.length; i++) {
                        try {
                            logEvent(this, Log.DBG,"IBM subkey: " + subKeyNames[i]);
                            if (Util.isAboveOrEqualMinimumVersion("1.4.2", subKeyNames[i])) {
                                String HKEY_jdkVersion = HKEY_jdk + "\\" + subKeyNames[i];
                                jdkHome  = regserv.getStringValue(HKLM, HKEY_jdkVersion, "JavaHome", false);
                                if (JDKInfo.checkJdkHome(this, jdkHome)) {
                                    addToList(jdkHome);
                                }
                            }
                        } catch (Exception ex) {
                            Util.logStackTrace(this, ex);
                        }
                    }
                }
            }
        } 
        catch (Exception ex) {
            Util.logStackTrace(this, ex);
        }
    }
    
    void findUnixJDK() {
        String jdkHome;
        // Try the standard places first
        logEvent(this, Log.DBG,"Checking Unix Standard Places ... ");
        if (Util.isSunOS()) {
            checkDir("/usr");
            checkDir("/usr/jdk");
            checkDir("/opt");
            checkDir("/export/home");
            checkDir(System.getProperty("user.home"));
        } else if (Util.isLinuxOS()) {
            checkDir("/usr");
            checkDir("/opt");
            checkDir("/usr/local");
            checkDir("/usr/java");
            checkDir("/opt/java");
            checkDir("/usr/local/java");
            checkDir(System.getProperty("user.home"));
        } else if (Util.isMacOSX()) {
            checkDirMacOSX("/System/Library/Frameworks/JavaVM.framework/Versions/1.4");
            checkDirMacOSX("/System/Library/Frameworks/JavaVM.framework/Versions/1.5");
        }
        
        // Try the current jdk
        if (currentJDKHome != null) {
            addToList(currentJDKHome);
        }
        
        logEvent(this, Log.DBG,"Checking Unix Path settings & environment variable ...");
        RunCommand runCommand = new RunCommand();
        runCommand.execute("/usr/bin/env");
        runCommand.waitFor();
        
        String line = null;
        while ((line = runCommand.getOutputLine()) != null) {
            if(line.startsWith("PATH")) {
                String path = line.substring(line.indexOf("=")+1);
                StringTokenizer st = new StringTokenizer(path.trim(),":");
                while(st.hasMoreTokens()) {
                    File jvmFile = (new File(st.nextToken().toString(),"java"));
                    if(jvmFile.exists()) {
                        // Check if jvm.dll exists as needed by ffj
                        File binDir = jvmFile.getParentFile();
                        File jdkDir= binDir.getParentFile();
                        jdkHome = jdkDir.getAbsolutePath();
                        if(JDKInfo.checkJdkHome(this, jdkHome)) addToList(jdkHome);
                    }
                }
            }
            
            // Check for different possible variable
            if(line.startsWith("JAVA_PATH") || line.startsWith("JAVA_HOME") ||
               line.startsWith("JAVAPATH") || line.startsWith("JAVAHOME") ||
               line.startsWith("JDK_PATH") || line.startsWith("JDK_HOME") ||
               line.startsWith("JDKPATH") || line.startsWith("JDKHOME")){
                jdkHome = line.substring(line.indexOf("=")+1).trim();
                if(JDKInfo.checkJdkHome(this, jdkHome)) 
                    addToList(jdkHome);
            }
        }
    }
        
    void checkDir (String rootDir) {
        logEvent(this, Log.DBG,"Checking Directory: " + rootDir);
        String jdkHome;
        File root = new File(rootDir);
        String[] list = root.list();
        if((list != null)){
            for (int i=0;i<list.length;i++) {
                if((list[i].startsWith("j2se") || list[i].startsWith("java") ||
                   list[i].startsWith("j2sdk") || list[i].startsWith("jdk")) ||
                   list[i].startsWith("IBMJava2")) {
                    if (rootDir.endsWith("\\") || rootDir.endsWith("/")) {
                        jdkHome = rootDir + list[i];
                    } else {
                        jdkHome = rootDir + File.separator + list[i];
                    }
                    if ((new File(jdkHome)).isDirectory()) {
                        if(JDKInfo.checkJdkHome(this, jdkHome)) {
                            addToList(jdkHome);
                        }
                    }
                }
            }
        }
    }
    
    /** Special version for Mac OS X as structure of directories is different */
    void checkDirMacOSX (String rootDir) {
        logEvent(this, Log.DBG,"Checking Directory: " + rootDir);
        String jdkHome;
        File root = new File(rootDir);
        if (rootDir.endsWith("\\") || rootDir.endsWith("/")) {
            jdkHome = rootDir + "Home";
        } else {
            jdkHome = rootDir + File.separator + "Home";
        }
        if ((new File(jdkHome)).isDirectory()) {
            if(JDKInfo.checkJdkHome(this, jdkHome)) {
                addToList(jdkHome);
            }
        }
    }
    
    void addToList(String jdkhome) {
        addToList(new File(jdkhome.trim()));
    }
    
    void addToList(File jdkHomeFile) {
        logEvent(this, Log.DBG, "Adding - " + jdkHomeFile.getAbsolutePath());
        /* Search as File instead of String to prevent duplicate entries with
         * capitilization or additional path separators in the end of the path. */
        boolean found = false;
        logEvent(this, Log.DBG, "Searching " + jdkHomeFile + " in " + jdkHomeList);
        if (jdkHomeList.contains(jdkHomeFile)) {
            //Nothing to do. JDK is already in list.
            return;
        }
        
        //Add JDK to list.
        //Following check is done only for asbundle installer on Windows and Linux.
        if (Names.INSTALLER_AS_BUNDLE.equals(Util.getStringPropertyValue(Names.INSTALLER_TYPE)) &&
            (Util.isWindowsOS() || Util.isLinuxOS())) {
            //Get os.arch property value
            logEvent(this, Log.DBG,"Checking os.arch of " + jdkHomeFile);
            String jvm = Util.getJVMName();
            File jvmFile = new File(jdkHomeFile + File.separator + "bin" + File.separator + jvm);
            RunCommand runCommand = new RunCommand();
            runCommand = new RunCommand();
            String [] cmdArr = new String[5];
            cmdArr[0] = jvmFile.getAbsolutePath();
            cmdArr[1] = "-cp";
            cmdArr[2] = JDKSearchAction.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            cmdArr[3] = "org.netbeans.installer.Verify";
            cmdArr[4] = "os.arch";
            runCommand.execute(cmdArr);
            runCommand.waitFor();

            String osArch = runCommand.getOutputLine().trim();
            if (!osArch.equals("amd64")) {
                logEvent(this, Log.DBG,"Found Valid JDK Home ... " + jdkHomeFile);
                jdkHomeList.add(jdkHomeFile);
            }
        } else {
            logEvent(this, Log.DBG,"Found Valid JDK Home ... " + jdkHomeFile);
            jdkHomeList.add(jdkHomeFile);
        }
    }
    
    public static int getLatestVersionIndex() {
        return getLatestVersionIndex(Util.getJdkHomeList());
    }
    
    public static int getLatestVersionIndex(Vector jdkHomeList) {
        if (jdkHomeList == null) {
            return -1;
        }
        int latestVersionIndex = 0;
        if (jdkHomeList.size() > 1) {
            JDKVersion latestVersion = new JDKVersion(JDKVersion.getVersionString(((File)jdkHomeList.firstElement()).getAbsolutePath()));
            for (int i = 1; i < jdkHomeList.size(); i++) {
                JDKVersion newVersion = new JDKVersion(JDKVersion.getVersionString(((File)jdkHomeList.elementAt(i)).getAbsolutePath()));
                if (latestVersion.compareTo(newVersion) < 0) {
                    latestVersionIndex = i;
                    latestVersion = newVersion;
                }
            }
        }
        return latestVersionIndex;
    }
    
    /** Returns Vector of JDKInfoAux instances */
    public static Vector getJdkList() {
        Vector jdkHomeList = Util.getJdkHomeList();
        Vector jdkHomeList1 = new Vector();
        if (jdkHomeList == null) {
            return jdkHomeList1;
        }
        
        String jvm = Util.getJVMName();
        for (int i = 0; i < jdkHomeList.size(); i++) {
            //Get Java version
            String jdkPath = ((File) jdkHomeList.get(i)).getPath();
            File jvmFile = new File(jdkPath + File.separator + "bin" + File.separator + jvm);
            RunCommand runCommand = new RunCommand();
            String [] cmdArr = new String[2];
            cmdArr[0] = jvmFile.getAbsolutePath();
            cmdArr[1] = "-version";
            runCommand.execute(cmdArr);
            runCommand.waitFor();
            
            //Look for line starting with "java version"
            String line = "", s = "";
            while (s != null) {
                s = runCommand.getErrorLine();
                if (s.startsWith("java version")) {
                    line = s;
                    break;
                }
            }
            
            String version = "";
            String jdkVersion = "";
            if (line.length() > 0) {
                StringTokenizer st = new StringTokenizer(line.trim());
                while (st.hasMoreTokens()) {
                    version = st.nextToken();
                }
                StringBuffer stringBuffer = new StringBuffer();
                for (int j = 0; j < version.length(); j++) {
                    if (version.charAt(j) != '\"') {
                        stringBuffer.append(version.charAt(j));
                    }
                }
                jdkVersion = stringBuffer.toString();
            }
            jdkHomeList1.add(new JDKInfoAux(jdkPath,jdkVersion));
        }
        return jdkHomeList1;
    }
}
