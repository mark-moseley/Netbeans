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
 *
 * $Id$
 */
package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.launchers.Launcher;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.unix.shell.BourneShell;
import org.netbeans.installer.utils.system.unix.shell.CShell;
import org.netbeans.installer.utils.system.unix.shell.KornShell;
import org.netbeans.installer.utils.system.unix.shell.Shell;
import org.netbeans.installer.utils.system.unix.shell.TCShell;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class UnixNativeUtils extends NativeUtils {
    private boolean isUserAdminSet;
    private boolean isUserAdmin;
    private static final String [] FORBIDDEN_DELETING_FILES_UNIX = {
        System.getProperty("user.home"),
        System.getProperty("java.home"),
        "/",
        "/bin",
        "/boot",
        "/dev",
        "/etc",
        "/home",
        "/lib",
        "/mnt",
        "/opt",
        "/sbin",
        "/share",
        "/usr",
        "/usr/bin",
        "/usr/include",
        "/usr/lib",
        "/usr/man",
        "/usr/sbin",
        "/var" };
    
    protected void scheduleCleanup(String libraryPath) {
        new File(libraryPath).deleteOnExit();
    }
    
    public boolean isCurrentUserAdmin() {
        if(isUserAdminSet) {
            return isUserAdmin;
        }
        boolean result = false;
        try {
            ExecutionResults resRealID = SystemUtils.executeCommand("id", "-ru");
            ExecutionResults resEffID = SystemUtils.executeCommand("id", "-u");
            String realID = resRealID.getStdOut();
            String effID = resRealID.getStdOut();
            if(realID!=null && effID!=null) {
                result = (realID.equals("0") && effID.equals("0"));
            }
            
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.CRITICAL,
                    "Can`t execute id command");
            LogManager.log(ErrorLevel.CRITICAL,ex);
        }
        isUserAdmin = result;
        isUserAdminSet = true;
        return result;
    }
    
    public void updateApplicationsMenu() {
        try {
            SystemUtils.executeCommand(null,new String [] {
                "pkill", "-u", SystemUtils.getUserName(), "panel"});
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        final String XDG_DATA_HOME = SystemUtils.getEnvironmentVariable("XDG_DATA_HOME");
        final String XDG_DATA_DIRS = SystemUtils.getEnvironmentVariable("XDG_DATA_DIRS");
        
        File currentUserLocation;
        if (XDG_DATA_HOME == null) {
            currentUserLocation = new File(SystemUtils.getUserHomeDirectory(), ".local/share");
        } else {
            currentUserLocation = new File(XDG_DATA_HOME);
        }
        
        File allUsersLocation;
        if (XDG_DATA_DIRS == null) {
            allUsersLocation = new File("/usr/share");
        } else {
            allUsersLocation = new File(XDG_DATA_DIRS.split(SystemUtils.getPathSeparator())[0]);
        }
        
        String fileName = shortcut.getFileName();
        if (fileName == null) {
            fileName = shortcut.getExecutable().getName() + ".desktop";
        }
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                return new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
            case ALL_USERS_DESKTOP:
                return new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
            case CURRENT_USER_START_MENU:
                return new File(currentUserLocation, "applications/" + fileName);
            case ALL_USERS_START_MENU:
                return new File(allUsersLocation, "applications/" + fileName);
            default:
                return null;
        }
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        final File          file     = getShortcutLocation(shortcut, locationType);
        final StringBuilder contents = new StringBuilder();
        final String        nl       = SystemUtils.getLineSeparator();
        
        contents.append("[Desktop Entry]").append(nl);
        contents.append("Encoding=UTF-8").append(nl);
        
        contents.append("Name=" + shortcut.getName()).append(nl);
        contents.append("Exec=" + shortcut.getExecutable()).append(nl);
        
        if(shortcut.getIcon()!=null) {
            contents.append("Icon=" + shortcut.getIconPath()).append(nl);
        }
        
        if(shortcut.getCategories().length != 0) {
            contents.append("Categories=" +
                    StringUtils.asString(shortcut.getCategories(),";")).
                    append(nl);
        }
        
        contents.append("Version=1.0").append(nl);
        contents.append("StartupNotify=true").append(nl);
        contents.append("Type=Application").append(nl);
        contents.append("Terminal=0").append(nl);
        
        
        try {
            FileUtils.writeFile(file, contents);
        } catch (IOException e) {
            throw new NativeException("Cannot create shortcut", e);
        }
        
        return file;
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean cleanupParents) throws NativeException {
        try {
            File shortcutFile = getShortcutLocation(shortcut, locationType);
            
            FileUtils.deleteFile(shortcutFile);
            
            if(cleanupParents &&
                    (locationType == ShortcutLocationType.ALL_USERS_START_MENU ||
                    locationType == ShortcutLocationType.CURRENT_USER_START_MENU)) {
                FileUtils.deleteEmptyParents(shortcutFile);
            }
        } catch (IOException e) {
            throw new NativeException("Cannot remove shortcut", e);
        }
    }
    
    public List<File> findExecutableFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();
        
        if (parent.exists()) {
            if(parent.isDirectory()) {
                File [] children = parent.listFiles();
                for(File child : children) {
                    files.addAll(findExecutableFiles(child));
                }
            } else {
                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] scriptExtensions = { ".sh", ".pl", ".py"};  //shell, perl, python
                for(String ext : scriptExtensions) {
                    if (name.endsWith(ext)) {
                        files.add(child);
                        return files;
                    }
                }
                // contents based analysis
                String line = FileUtils.readFirstLine(child);
                if (line != null) {
                    if (line.startsWith("#!")) { // a script of some sort
                        files.add(child);
                        return files;
                    }
                }
            }
        }
        return files;        
    }
    
    public List<File> findIrrelevantFiles(File parent) throws IOException {
        List<File> files = new ArrayList<File>();
        
        if (parent.exists()) {
            if(parent.isDirectory()) {
                File [] children = parent.listFiles();
                for(File child : children) {
                    files.addAll(findIrrelevantFiles(child));
                }
            } else {
                // contents based analysis - none at this point
                
                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] windowsExtensions = { 
                    ".bat", ".cmd", ".dll", ".exe", ".com", 
                    ".vbs", ".vbe", ".wsf", ".wsh"} ;
                for(String ext : windowsExtensions) {
                    if(name.endsWith(ext)) {
                        files.add(child);
                        break;
                    }
                }
            }
        }
        return files;
    }
    
    public void chmod(File file, String mode) throws IOException {
        chmod(Arrays.asList(file), mode);
    }
    
    public void chmod(File file, int mode) throws IOException {
        chmod(file, Integer.toString(mode));
    }
    
    public void chmod(List<File> files, String mode) throws IOException {
        for(File file : files) {
            File   directory = file.getParentFile();
            String name      = file.getName();
            
            SystemUtils.executeCommand(directory, "chmod", mode, name);
        }
    }
    
    public void removeIrrelevantFiles(File parent) throws IOException {
        FileUtils.deleteFiles(findIrrelevantFiles(parent));
    }
    
    public void correctFilesPermissions(File parent) throws IOException {
        chmod(findExecutableFiles(parent), "ugo+x");
    }
    
    public long getFreeSpace(File file) {
        if ((file == null) || file.getPath().equals("")) {
            return 0;
        } else {
            return getFreeSpace0(file.getPath());
        }
    }
    
    // other ... //////////////////////////
    
    public String getEnvironmentVariable(String name, EnvironmentScope scope, boolean flag) {
        return System.getenv(name);
    }
    
    public void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean flag) throws NativeException {
        if(EnvironmentScope.PROCESS == scope) {
            SystemUtils.getEnvironment().put(name, value);
        } else {
            try {
                getCurrentShell().setVar(name, value, scope);
            } catch (IOException e) {
                throw new NativeException("Cannot set the environment variable value", e);
            }
        }
    }
    
    public Shell getCurrentShell() {
        LogManager.log(ErrorLevel.DEBUG,
                "Getting current shell..");
        LogManager.indent();
        Shell [] avaliableShells =  {
            new BourneShell(),
            new CShell() ,
            new TCShell(),
            new KornShell()
        };
        String shell = System.getenv("SHELL");
        Shell result = null;
        if(shell == null) {
            shell = System.getenv("shell");
        }
        LogManager.log(ErrorLevel.DEBUG,
                "... shell env variable = " + shell);
        
        if(shell != null) {
            if(shell.lastIndexOf(File.separator)!=-1) {
                shell = shell.substring(shell.lastIndexOf(File.separator) + 1);
            }
            LogManager.log(ErrorLevel.DEBUG,
                    "... searching for the shell with name [" + shell +  "] " +
                    "among available shells names");
            for(Shell sh : avaliableShells) {
                if(sh.isCurrentShell(shell)) {
                    result = sh;
                    LogManager.log(ErrorLevel.DEBUG,
                            "... detected shell: " +
                            sh.getClass().getSimpleName());
                    break;
                }
            }
            
        }
        if(result == null) {
            LogManager.log(ErrorLevel.DEBUG,
                    "... no shell found");
        }
        LogManager.unindent();
        LogManager.log(ErrorLevel.DEBUG,
                "... finished detecting shell");
        return result;
    }
    
    public File getDefaultApplicationsLocation() {
        return SystemUtils.getUserHomeDirectory();
    }
    
    public boolean isPathValid(String path) {
        return true;
    }
    
    public FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        FilesList list = new FilesList();
        try {
            Launcher launcher = createUninstaller(descriptor, true, new Progress());
            correctFilesPermissions(launcher.getOutputFile());
            list.add(launcher.getOutputFile());
        }  catch (IOException ex) {
            String exString = "Can`t create uninstaller";
            LogManager.log(ErrorLevel.WARNING, exString);
            LogManager.log(ErrorLevel.WARNING, ex);
            throw new NativeException(exString, ex);
        }
        return list;
    }
    
    public void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) {
        // does nothing - no support for unix package managers yet
    }
    
    public FilesList createSymLink(File source, File target) throws IOException {
        return createSymLink(source, target, true);
    }
    
    public FilesList createSymLink(File source, File target, boolean useRelativePath) throws IOException {
        FilesList list = new FilesList();
        
        list.add(FileUtils.mkdirs(source.getParentFile()));
        list.add(source);
        
        String relativePath = null;
        if (useRelativePath) {
            relativePath = FileUtils.getRelativePath(source, target);
        }
        
        SystemUtils.executeCommand(
                "ln",
                "-s",
                relativePath == null ? target.getAbsolutePath() : relativePath,
                source.getAbsolutePath());
        
        return list;
    }
    
    // native declarations //////////////////////////////////////////////////////////
    private native long getFreeSpace0(String s);
    
    protected void initializeForbiddenFiles(String ... files) {
        super.initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_UNIX);
        super.initializeForbiddenFiles(files);
    }
}