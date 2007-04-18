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
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.system.windows.SystemApplication;
import org.netbeans.installer.utils.system.windows.FileExtension;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.StringUtils.*;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.cleaner.ProcessOnExitCleanerHandler;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.*;

/**
 *
 * @author Dmitry Lipin
 * @author Kirill Sorokin
 */
public class WindowsNativeUtils extends NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LIBRARY_PATH =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "windows/" + //NOI18N
            "windows.dll"; //NOI18N
    
    private static final String CLEANER_RESOURCE =
            NATIVE_CLEANER_RESOURCE_SUFFIX +
            "windows/" + "cleaner.exe";
    
    private static final String CLEANER_FILENAME =
            "nbi-cleaner.exe";
    
    public static final String UNINSTALL_KEY = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall";
    
    public static final String DISPLAY_NAME     = "DisplayName";
    public static final String DISPLAY_ICON     = "DisplayIcon";
    public static final String UNINSTALL_STRING = "UninstallString";
    public static final String MODIFY_STRING    = "ModifyPath";
    public static final String NO_REPAIR        = "NoRepair";
    public static final String INSTALL_LOCATION = "InstallLocation";
    
    private static final String NBI_UID_PREFIX = "nbi-";
    private static final String UID_SEPARATOR  = "-";
    
    private static final int MIN_UID_INDEX = 1;
    private static final int MAX_UID_INDEX = 100;
    
    private static final String SHELL_FOLDERS_KEY =
            "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders";
    
    public static final String CURRENT_USER_ENVIRONMENT_KEY =
            "Environment";
    public static final String ALL_USERS_ENVIRONMENT_KEY    =
            "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
    
    private static final String RUNONCE_KEY =
            "Software\\Microsoft\\Windows\\CurrentVersion\\RunOnce";
    private static final String RUNONCE_DELETE_VALUE_NAME =
            "NBI Temporary Files Delete";
    
    private static final String EXT_PREFIX = "NBI.";
    private static final String EXT_SUFFIX = "";
    
    
    private static final String SEP = SEPARATOR;
    
    ///////////////////////////////////////////////////////////////////////////
    // File Association Constants
    private static final String PERCEIVED_TYPE_VALUE_NAME = "PerceivedType";
    private static final String CONTENT_TYPE_VALUE_NAME = "Content Type";
    private static final String DEFAULT_ICON_KEY_NAME = "DefaultIcon";
    private static final String SHELL_OPEN_COMMAND =
            SEP + "shell" + SEP +  "open"  + SEP +  "command";
    private static final String CONTENT_TYPE_KEY =
            "MIME" + SEP + "Database" + SEP + "Content Type";
    private static final String APPLICATIONS_KEY_NAME = "Applications";
    private static final String FRIENDLYAPPNAME_VALUE_NAME = "FriendlyAppName";
    private static final String APPLICATION_VALUE_NAME = "Application";
    private static final String OPEN_WITH_LIST_KEY_NAME = "OpenWithList";
    private static final String EXTENSION_VALUE_NAME = "Extension";
    public static final String DEFAULT_OPEN_COMMAND = "\"%1\"";
    
    private static final String CURRENT_USER_FILE_EXT_KEY =
            "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts";
    private static final String CURRENT_USER_MUI_CACHE_KEY =
            "Software\\Microsoft\\Windows\\ShellNoRoam\\MUICache";
    private static final String OPEN_WITH_PROGIDS_KEY_NAME = "OpenWithProgids";
    private static final String MRULIST_VALUE_NAME = "MRUList";
    private static final String MRU_VALUES = "abcdefghijklmnopqrstuvwxyz";
    
    
    // properties for file associations
    private static final String CREATED = "created";
    private static final String EXT_PERCEIVEDTYPE_PROPERTY = "perceivedType";
    private static final String EXT_CONTENTTYPE_PROPERTY = "contentType";
    private static final String EXT_LONGEXT_PROPERTY = "longExt";
    private static final String EXT_DESCRIPTION_PROPERTY = "description";
    private static final String EXT_ICON_PROPERTY = "defaultIcon";
    private static final String EXT_HKCRSHELL_OPEN_COMMAND_PROPERTY = "hkcrShellOpenCommand";
    private static final String EXT_HKCU_DEFAULTAPP_PROPERTY = "hkcuDefaultApp";
    private static final String EXT_HKCU_FILEXT_PROPERTY = "hkcuFileExt";
    private static final String EXT_HKCR_APPLICATIONS_PROPERTY = "hkcrApplications";
    private static final String EXT_HKCR_OPENWITHPROGIDS_PROPERTY = "hkcrOpenWithProgids";
    private static final String EXT_HKCR_OPENWITHLIST_PROPERTY = "hkcrOpenWithList";
    private static final String EXT_HKCU_MUICACHE_PROPERTY = "hkcuMuiCache";
    private static final String EXT_HKCU_OPENWITHPROGIDS_PROPERTY = "hkcuOpenWithProgids";
    private static final String EXT_HKCU_OPENWITHLIST_PROPERTY = "hkcuOpenWithList";
    
    private static final String CURRENT_USER_CLASSES = "Software\\Classes\\";
    private int clSection;
    private String clKey;
    private int uninstallSection;
    
    private boolean isUserAdminSet;
    private boolean isUserAdmin;
    
    
    //////////////////////////////////////////////////////////////////////////
    // file access
    // windows API constants
    private static final int FILE_READ_DATA = 0;
    private static final int FILE_LIST_DIRECTORY = 0;
    private static final int FILE_WRITE_DATA = 1;
    private static final int FILE_ADD_FILE =1;
    private static final int FILE_APPEND_DATA = 4;
    private static final int FILE_ADD_SUBDIRECTORY = 4;
    private static final int FILE_READ_EA = 8;
    private static final int FILE_WRITE_EA = 16;
    private static final int FILE_EXECUTE = 32;
    private static final int FILE_TRAVERSE = 32;
    private static final int FILE_DELETE_CHILD = 64;
    private static final int FILE_READ_ATTRIBUTES = 128;
    private static final int FILE_WRITE_ATTRIBUTES = 256;
    private static final int FILE_DELETE = 65536;
    
    //////////////////////////////////////////////////////////////////////////
    
    private static final WindowsRegistry registry = new WindowsRegistry();
    
    private static final String[] FORBIDDEN_DELETING_FILES_WINDOWS = {
        System.getenv("ProgramFiles"),
        System.getenv("SystemRoot"),
        System.getenv("USERPROFILE"),
        System.getenv("SystemDrive") + File.separator
    };
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File defaultApplicationsLocation;
    
    // constructor //////////////////////////////////////////////////////////////////
    WindowsNativeUtils() {
        loadNativeLibrary(LIBRARY_PATH);
        //initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_WINDOWS);
        initializeForbiddenFiles();
        initializeRegistryKeys();
    }
    
    private void initializeRegistryKeys() {
        boolean result = false;
        try {
            result = isCurrentUserAdmin();
        } catch (NativeException ex) {
            LogManager.log(ex);
        }
        
        try {
            clSection = registry.canModifyKey(HKCR, "") ? HKCR : HKCU;
            clKey = (result) ? EMPTY_STRING : CURRENT_USER_CLASSES;
            uninstallSection = registry.canModifyKey(HKLM,UNINSTALL_KEY) ? HKLM : HKCU;
        } catch (NativeException ex) {
            LogManager.log(ex);
            clSection = HKCU;
            clKey = CURRENT_USER_CLASSES;
            uninstallSection = HKCU;
        }
    }
    // parent implementation ////////////////////////////////////////////////////////
    public boolean isCurrentUserAdmin() throws NativeException {
        if(isUserAdminSet) {
            return isUserAdmin;
        }
        boolean result = isCurrentUserAdmin0();
        isUserAdmin = result;
        isUserAdminSet = true;
        return result;
        
    }
    
    @Override
    protected OnExitCleanerHandler getDeleteOnExit() {
        return new WindowsProcessOnExitCleanerHandler(CLEANER_FILENAME);
    }
    
    public File getDefaultApplicationsLocation() throws NativeException {
        if (defaultApplicationsLocation == null) {
            defaultApplicationsLocation = SystemUtils.getUserHomeDirectory();
            
            String path = SystemUtils.getEnvironmentVariable("ProgramFiles");
            
            if (path != null) {
                defaultApplicationsLocation = new File(path).getAbsoluteFile();
            } else {
                ErrorManager.notify(ErrorLevel.DEBUG, "Value of the environment variable ProgramFiles is not set");
            }
        }
        
        return defaultApplicationsLocation;
    }
    
    public long getFreeSpace(File file) throws NativeException {
        if ((file == null) || !isPathValid(file.getPath())) {
            return 0;
        } else {
            return getFreeSpace0(file.getPath());
        }
    }
    
    public boolean isPathValid(String path) {
        // there is a max length limitation
        if (path.length() > 256) {
            return false;
        }
        
        // the path should be absolute, i.e. should start with "<Drive>:\"
        if (!path.matches("^[A-Z,a-z]:\\\\.*")) {
            return false;
        }
        
        String[] parts = path.split("\\\\");
        
        for (int i = 1; i < parts.length; i++) {
            if (Pattern.compile("[\\/:*\\?\"<>|]").matcher(parts[i]).find()) {
                return false;
            }
            if (parts[i].startsWith(" ") ||
                    parts[i].startsWith("\t") ||
                    parts[i].endsWith(" ") ||
                    parts[i].endsWith("\t")) {
                return false;
            }
        }
        
        return true;
    }
    
    public File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        String path = shortcut.getRelativePath();
        if (path == null) {
            path = "";
        }
        
        String fileName = shortcut.getName() + ".lnk";
        
        final String allUsersRootPath = SystemUtils.getEnvironmentVariable("allusersprofile");
        
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                String userDesktop = registry.getStringValue(HKCU, SHELL_FOLDERS_KEY, "Desktop", false);
                if (userDesktop == null) {
                    userDesktop = SystemUtils.getUserHomeDirectory() + File.separator + "Desktop";
                }
                
                return new File(userDesktop, fileName);
                
            case ALL_USERS_DESKTOP:
                String commonDesktop = registry.getStringValue(HKLM, SHELL_FOLDERS_KEY, "Common Desktop", false);
                if (commonDesktop == null) {
                    commonDesktop = allUsersRootPath + File.separator + "Desktop";
                }
                
                return new File(commonDesktop, fileName);
                
            case CURRENT_USER_START_MENU:
                String userStartMenu = registry.getStringValue(HKCU, SHELL_FOLDERS_KEY, "Programs", false);
                if (userStartMenu == null) {
                    userStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                }
                
                return new File(userStartMenu, path + File.separator + fileName);
                
            case ALL_USERS_START_MENU:
                String commonStartMenu = registry.getStringValue(HKLM, SHELL_FOLDERS_KEY, "Common Programs", false);
                if (commonStartMenu == null) {
                    commonStartMenu = SystemUtils.getUserHomeDirectory() + File.separator + "Start Menu" + File.separator + "Programs";
                }
                
                return new File(commonStartMenu, path + File.separator + fileName);
        }
        
        return null;
    }
    
    public File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        shortcut.setPath(shortcutFile.getAbsolutePath());
        
        createShortcut0(shortcut);
        
        return shortcutFile;
    }
    
    public void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean cleanupParents) throws NativeException {
        File shortcutFile = getShortcutLocation(shortcut, locationType);
        
        try {
            FileUtils.deleteFile(shortcutFile);
            
            if (cleanupParents) {
                switch (locationType) {
                    case CURRENT_USER_START_MENU:
                    case ALL_USERS_START_MENU:
                        FileUtils.deleteEmptyParents(shortcutFile);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            
        }
    }
    
    public FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        FilesList list = new FilesList();
        LogManager.log("adding new Add or Remove Programs entry with id [" + descriptor.getUid() + "]");
        
        final String uid = getVacantUninstallUid(descriptor.getUid());
        final String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + uid;
        
        registry.createKey(uninstallSection, key);
        
        if (descriptor.getDisplayName() != null) {
            LogManager.log("Set '" + DISPLAY_NAME + "' = [" + descriptor.getDisplayName() + "]");
            
            registry.setStringValue(uninstallSection, key, DISPLAY_NAME, descriptor.getDisplayName(), false);
        }
        if (descriptor.getIcon() != null) {
            LogManager.log("Set '" + DISPLAY_ICON + "' = [" + descriptor.getIcon() + "]");
            
            registry.setStringValue(uninstallSection, key, DISPLAY_ICON, descriptor.getIcon(), false);
        }
        if (descriptor.getInstallPath() != null) {
            LogManager.log("Set '" + INSTALL_LOCATION + "' = [" + descriptor.getInstallPath() + "]");
            
            registry.setStringValue(uninstallSection, key, INSTALL_LOCATION, descriptor.getInstallPath(), false);
        }
        
        String [] execCommand;
        try {
            Launcher launcher = createUninstaller(descriptor, false, new Progress());
            execCommand = launcher.getExecutionCommand();
            list.add(launcher.getOutputFile());
        }  catch (IOException ex) {
            String exString = "Can`t create uninstaller";
            LogManager.log(ErrorLevel.WARNING, exString);
            LogManager.log(ErrorLevel.WARNING, ex);
            throw new NativeException(exString, ex);
        }
        String modifyCommand = StringUtils.EMPTY_STRING;
        for(String s : execCommand) {
            modifyCommand += StringUtils.SPACE +
                    (s.contains(StringUtils.SPACE) ? (StringUtils.QUOTE + s + StringUtils.QUOTE) : s);
        }
        modifyCommand = modifyCommand.trim();
        
        if (descriptor.getModifyCommand() != null) {
            LogManager.log("Set '" + NO_REPAIR + "' = [" + 1 + "]");
            
            registry.set32BitValue(uninstallSection, key, NO_REPAIR, 1);
            
            LogManager.log("Set '" + MODIFY_STRING + "' = [" + modifyCommand + "]");
            
            registry.setStringValue(uninstallSection, key, MODIFY_STRING, modifyCommand, false);
        }
        
        if (descriptor.getUninstallCommand() != null) {
            String uninstallString = modifyCommand;
            for(String s : descriptor.getUninstallCommand()) {
                boolean add = true;
                for(String sm : descriptor.getModifyCommand()) {
                    if(s.equals(sm)) {
                        add = false;
                        continue ;
                    }
                }
                if(add) {
                    uninstallString += StringUtils.SPACE +
                            (s.contains(StringUtils.SPACE) ? (StringUtils.QUOTE + s + StringUtils.QUOTE) : s);
                }
            }
            uninstallString = uninstallString.trim();
            LogManager.log("Set '" + UNINSTALL_STRING + "' = [" + uninstallString + "]");
            
            registry.setStringValue(uninstallSection, key, UNINSTALL_STRING, uninstallString, false);
        }
        
        registry.setAdditionalValues(uninstallSection, key, descriptor.getParameters());
        return list;
    }
    
    public void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        String properUid = getProperUninstallUid(
                descriptor.getUid(),
                descriptor.getInstallPath());
        
        if (properUid != null) {
            registry.deleteKey(uninstallSection, UNINSTALL_KEY, properUid);
        }
    }
    
    public String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException {
        String value = null;
        
        if ((scope != null) && (name != null)) {
            if (scope == EnvironmentScope.PROCESS) {
                value = System.getenv(name);
            } else {
                String rootKey = null;
                int    section = 0;
                if (scope == EnvironmentScope.CURRENT_USER) {
                    rootKey = CURRENT_USER_ENVIRONMENT_KEY;
                    section = HKCU;
                }
                if (scope == EnvironmentScope.ALL_USERS) {
                    rootKey = ALL_USERS_ENVIRONMENT_KEY;
                    section = HKLM;
                }
                
                if (registry.keyExists(section, rootKey)) {
                    value = registry.getStringValue(section, rootKey, name, expand);
                } else {
                    LogManager.log(ErrorLevel.DEBUG, "Root environment key doesn`t exist. Can`t get environment variable");
                }
            }
        }
        
        return value;
    }
    
    public void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException {
        if ((name != null) && (scope != null)) {
            if (scope == EnvironmentScope.PROCESS) {
                SystemUtils.getEnvironment().put(name, value);
            } else {
                String rootKey = null;
                int    section = 0;
                if (scope == EnvironmentScope.CURRENT_USER) {
                    rootKey = CURRENT_USER_ENVIRONMENT_KEY;
                    section = HKCU;
                }
                if (scope == EnvironmentScope.ALL_USERS) {
                    rootKey = ALL_USERS_ENVIRONMENT_KEY;
                    section = HKLM;
                }
                
                if (registry.keyExists(section, rootKey)) {
                    registry.setStringValue(section, rootKey, name, value, expand);
                    notifyEnvironmentChanged0();
                } else {
                    LogManager.log(ErrorLevel.WARNING,
                            "Root envonment key doesn`t exist. " +
                            "Can`t set environment variable");
                }
            }
        }
    }
    
    public List<File> findIrrelevantFiles(File parent) throws IOException {
        List<File> files = new LinkedList<File>();
        
        if (parent.exists()) {
            if(parent.isDirectory()) {
                for(File child : parent.listFiles()) {
                    files.addAll(findIrrelevantFiles(child));
                }
            } else {
                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] unixExtensions = {".sh", ".so", ".dylib"};
                for(String ext : unixExtensions) {
                    if(name.endsWith(ext)) {
                        files.add(child);
                        break;
                    }
                }
                
                // contents based analysis
                // Switched off due to Issue 97995
                // This analysis can be switched back only after the serious invesigation
                // The main additional check should be done based on the name
                // If it contains any extenstion (except .sh) then check is failed anyway
                // E.G: GlassFish\imq\lib\props\broker\default.properties
                /*
                String line = FileUtils.readFirstLine(child);
                if (line != null) {
                    if (line.startsWith("#!/bin/sh")) { // shell script
                        files.add(child);
                        continue;
                    }
                }
                 */
            }
        }
        
        return files;
    }
    
    public List<File> findExecutableFiles(File parent) throws IOException {
        List<File> files = new LinkedList<File>();
        
        if (parent.exists()) {
            if(parent.isDirectory()) {
                File [] children = parent.listFiles();
                for(File child : children) {
                    files.addAll(findIrrelevantFiles(child));
                }
            } else {
                // name based analysis
                File child = parent;
                String name = child.getName();
                String [] windowsExecutableExtensions = {
                    ".exe", ".com", ".bat", ".cmd", ".vbs",
                    ".vbe", ".js",".jse", ".wsf", ".wsh" };
                for(String ext : windowsExecutableExtensions) {
                    if (name.endsWith(ext)) {
                        files.add(child);
                        break;
                    }
                }
            }
        }
        return files;
    }
    
    public void correctFilesPermissions(File parent) {
        // does nothing, as there is no such thing as execute permissions
    }
    
    public List<File> getFileSystemRoots() throws IOException {
        return Arrays.asList(File.listRoots());
    }
    
// windows-specific operations //////////////////////////////////////////////////
    public WindowsRegistry getWindowsRegistry() {
        return registry;
    }
    
    public void deleteFileOnReboot(File file) throws NativeException {
        String path = file.getAbsolutePath();
        
        if (isCurrentUserAdmin()) {
            deleteFileOnReboot0(path);
        } else {
            // just in case...
            if (!registry.keyExists(HKCU, RUNONCE_KEY)) {
                registry.createKey(HKCU, RUNONCE_KEY);
            }
            
            // find an appropriate name, which does not exist
            String name = RUNONCE_DELETE_VALUE_NAME;
            for (int i = 0; registry.valueExists(HKCU, RUNONCE_KEY, name); i++) {
                name = RUNONCE_DELETE_VALUE_NAME + UID_SEPARATOR + i;
            }
            
            // set the value
            String command = "cmd /q /c del /F /Q \"" + path + "\"";
            registry.setStringValue(HKCU, RUNONCE_KEY, name, command);
        }
    }
    
// private //////////////////////////////////////////////////////////////////////
    private String getVacantUninstallUid(final String baseUid) throws NativeException {
        String vacantUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + vacantUid;
        if (registry.keyExists(uninstallSection, key)) {
            for (int index = MIN_UID_INDEX; index < MAX_UID_INDEX; index++) {
                vacantUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + vacantUid;
                
                if (!registry.keyExists(uninstallSection, key)) {
                    return vacantUid;
                }
            }
            return null;
        } else {
            return vacantUid;
        }
    }
    
    private String getProperUninstallUid(final String baseUid, final String installLocation) throws NativeException {
        String properUid = baseUid;
        
        String key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + properUid;
        if (registry.keyExists(uninstallSection, key) &&
                registry.getStringValue(uninstallSection, key, INSTALL_LOCATION).equals(installLocation)) {
            return properUid;
        } else {
            for (int index = MIN_UID_INDEX; index < MAX_UID_INDEX; index++) {
                properUid = baseUid + UID_SEPARATOR + index;
                key = UNINSTALL_KEY + WindowsRegistry.SEPARATOR + properUid;
                
                if (registry.keyExists(uninstallSection, key) &&
                        registry.getStringValue(uninstallSection, key, INSTALL_LOCATION).equals(installLocation)) {
                    return properUid;
                }
            }
            return null;
        }
    }
    
    public boolean checkFileAccess(File file, boolean isReadNotModify) throws NativeException {
        int result = 0;
        try {
            int accessLevel = 0;
            if(isReadNotModify) {
                accessLevel = FILE_READ_DATA | FILE_LIST_DIRECTORY;
            } else {
                accessLevel  = FILE_ADD_FILE |
                        FILE_ADD_SUBDIRECTORY |
                        FILE_APPEND_DATA |
                        FILE_WRITE_DATA;
            }
            result = checkAccessTokenAccessLevel0(file.getPath(), accessLevel);
        } catch (UnsatisfiedLinkError e) {
            throw new NativeException("Cannot access native method", e);
        }
        return (result==1);
    }
    
    public synchronized void setFileAssociation(FileExtension ext, SystemApplication app, Properties props)  throws NativeException {
        if (ext==null && isEmpty(ext.getName())) {
            return;
        }
        notifyAssociationChanged();
        FileExtensionKey feExt = new FileExtensionKey(ext,getLongExtensionName(ext));
        setExtensionDetails(feExt,props);
        
        if(app!=null && !isEmpty(app.getLocation())) {
            SystemApplicationKey appExt = new SystemApplicationKey(app,getApplicationKey(app));
            registerApplication(appExt,feExt,props);
            changeDefaultApplication(appExt, feExt, props);
            addToOpenWithList(appExt, feExt,props);
        }
        notifyAssociationChanged();
    }
    public synchronized void removeFileAssociation(FileExtension ext, SystemApplication app, Properties props) throws NativeException {
        if (ext==null && isEmpty(ext.getName())) {
            return;
        }
        notifyAssociationChanged();
        String prefix = EXTENSION_VALUE_NAME + ext.getDotName();
        if(props.getProperty(prefix)!=null) {
            //extension was created
            if(registry.valueExists(clSection, clKey + ext.getDotName(),EMPTY_STRING)) {
                String extKey = registry.getStringValue(clSection, clKey + ext.getDotName(),EMPTY_STRING);
                FileExtensionKey feExt = new FileExtensionKey(ext,extKey);
                String appKey = getApplicationKey(app);
                SystemApplicationKey sap = new SystemApplicationKey(app,appKey);
                
                removeFromOpenWithList(sap, feExt,props);
                rollbackDefaultApplication(sap, feExt,props);
                unregisterApplication(sap,feExt, props);
                clearExtensionDetails(sap,feExt,props);
            }
        }
        notifyAssociationChanged();
    }
    
    
    
    private void setExtensionDetails(FileExtensionKey ext, Properties props) throws NativeException {
        String name = ext.getDotName();
        String extKey = ext.getKey();
        // create key HKEY_CLASSES_ROOT\.EXTENSION
        if(!registry.keyExists(clSection, clKey +  name)) {
            registry.createKey(clSection, clKey +  name);
            setExtProperty(props, name , CREATED);
        }
        
        // Set perceived and content time if necessary
        if(ext.getPerceivedType()!=null) {
            if(!registry.valueExists(clSection, clKey +  name , PERCEIVED_TYPE_VALUE_NAME)) {
                registry.setStringValue(clSection, clKey +  name, PERCEIVED_TYPE_VALUE_NAME,ext.getPerceivedType().toString());
                setExtProperty(props, name , EXT_PERCEIVEDTYPE_PROPERTY, CREATED);
            }
        }
        if(!isEmpty(ext.getMimeType())) {
            registry.setStringValue(clSection, clKey +  name, CONTENT_TYPE_VALUE_NAME,ext.getMimeType());
            if(!registry.keyExists(clSection, clKey + CONTENT_TYPE_KEY,ext.getMimeType())) {
                registry.createKey(clSection, clKey + CONTENT_TYPE_KEY,ext.getMimeType());
                registry.setStringValue(clSection, clKey + CONTENT_TYPE_KEY + SEP + ext.getMimeType(), EXTENSION_VALUE_NAME, name);
                setExtProperty(props, name, EXT_CONTENTTYPE_PROPERTY, CREATED);
            }
        }
        // make connection with HKEY_CLASSES_ROOT\.EXTENSION
        registry.setStringValue(clSection, clKey +  name, EMPTY_STRING, extKey);
        
        // create key HKEY_CLASSES_ROOT\EXT_PREFIX_EXTENSION_EXT_SUFFIX
        if(!registry.keyExists(clSection, clKey +  extKey)) {
            registry.createKey(clSection, clKey +  extKey);
            setExtProperty(props, name, EXT_LONGEXT_PROPERTY, CREATED);
        }
        
        
        // Set extension description and icon if necessary
        if(!isEmpty(ext.getDescription())) {
            if(registry.valueExists(clSection, clKey +  extKey, EMPTY_STRING)) {
                setExtProperty(props, name, EXT_DESCRIPTION_PROPERTY,
                        registry.getStringValue(clSection, clKey +  extKey, EMPTY_STRING));
            }
            registry.setStringValue(clSection, clKey +  extKey, EMPTY_STRING, ext.getDescription());
            
        }
        if(!isEmpty(ext.getIcon())) {
            if(!registry.keyExists(clSection, clKey +  extKey, DEFAULT_ICON_KEY_NAME)) {
                registry.createKey(clSection, clKey +  extKey, DEFAULT_ICON_KEY_NAME);
                registry.setStringValue(clSection, clKey +  extKey + SEP + DEFAULT_ICON_KEY_NAME,EMPTY_STRING, ext.getIcon());
                setExtProperty(props, name, EXT_ICON_PROPERTY, CREATED);
            }
        }
        
        //create current user extension key in HKCU\CURRENT_USER_FILE_EXT_KEY
        if(!registry.keyExists(HKCU, CURRENT_USER_FILE_EXT_KEY, name)) {
            registry.createKey(HKCU, CURRENT_USER_FILE_EXT_KEY, name);
            setExtProperty(props, name, EXT_HKCU_FILEXT_PROPERTY, CREATED);
        }
    }
    private void clearExtensionDetails(SystemApplicationKey app, FileExtensionKey fe, Properties props) throws NativeException {
        String name = fe.getDotName();
        String extKey = fe.getKey();
        String property;
        property = getExtProperty(props, name, EXT_HKCU_FILEXT_PROPERTY);
        if(property!=null) {
            if(registry.keyExists(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name)) {
                if(registry.keyExists(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name,OPEN_WITH_LIST_KEY_NAME)) {
                    registry.deleteKey(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name,OPEN_WITH_LIST_KEY_NAME);
                }
                if(registry.keyExists(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name,OPEN_WITH_PROGIDS_KEY_NAME)) {
                    registry.deleteKey(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name,OPEN_WITH_PROGIDS_KEY_NAME);
                }
                if(registry.getSubKeys(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP+ name).length==0) {
                    registry.deleteKey(HKCU, CURRENT_USER_FILE_EXT_KEY, name);
                }
            }
        }
        
        property = getExtProperty(props, name, EXT_DESCRIPTION_PROPERTY);
        if(property!=null) {
            //restore description
            registry.setStringValue(clSection, clKey +  extKey, EMPTY_STRING, property);
        }
        
        property = getExtProperty(props, name, EXT_ICON_PROPERTY);
        if(property!=null) {
            if(registry.keyExists(clSection, clKey +  extKey + SEP + DEFAULT_ICON_KEY_NAME)) {
                registry.deleteKey(clSection, clKey +  extKey + SEP + DEFAULT_ICON_KEY_NAME);
            }
        }
        
        property = getExtProperty(props, name, EXT_LONGEXT_PROPERTY);
        if(property!=null) {
            if(registry.getSubKeys(clSection, clKey +  extKey).length==0) {
                registry.deleteKey(clSection, clKey +  extKey);
            }
        }
        
        property = getExtProperty(props, name, EXT_CONTENTTYPE_PROPERTY);
        if(property!=null) {
            if(registry.getSubKeys(clSection, clKey + CONTENT_TYPE_KEY + SEP + fe.getMimeType()).length ==0) {
                registry.deleteKey(clSection, clKey + CONTENT_TYPE_KEY, fe.getMimeType());
            }
        }
        property = getExtProperty(props, name, EXT_PERCEIVEDTYPE_PROPERTY);
        if(property!=null) {
            registry.deleteValue(clSection, clKey +  name, PERCEIVED_TYPE_VALUE_NAME);
        }
        
        property = getExtProperty(props, name);
        if(property!=null) {
            if(registry.keyExists(clSection, clKey +  name) && registry.getSubKeys(clSection, clKey +  name).length==0) {
                registry.deleteKey(clSection, clKey +  name);
            }
        }
    }
    private void changeDefaultApplication(SystemApplicationKey app, FileExtensionKey fe, Properties props) throws NativeException {
        if(app.isUseByDefault()) {
            String name = fe.getDotName();
            String extKey = fe.getKey();
            String appLocation = app.getLocation();
            String appKey = app.getKey();
            String command = app.getCommand();
            
            if(!registry.keyExists(clSection, clKey +  extKey + SHELL_OPEN_COMMAND)) {
                registry.createKey(clSection, clKey +  extKey + SHELL_OPEN_COMMAND);
                registry.setStringValue(clSection, clKey +
                        extKey + SHELL_OPEN_COMMAND,
                        EMPTY_STRING,
                        constructCommand(app));
                setExtProperty(props, name, EXT_HKCRSHELL_OPEN_COMMAND_PROPERTY, CREATED);
            }
            
            //change current user 'default-app' for this extension
            String s = null;
            if(registry.valueExists(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name, APPLICATION_VALUE_NAME)) {
                s = registry.getStringValue(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name, APPLICATION_VALUE_NAME);
            }
            
            registry.setStringValue(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name, APPLICATION_VALUE_NAME, appKey);
            if(s!=null) {
                setExtProperty(props, name, EXT_HKCU_DEFAULTAPP_PROPERTY,s);
            }
            
        }
    }
    private void rollbackDefaultApplication(SystemApplicationKey app, FileExtensionKey fe, Properties props) throws NativeException {
        String property;
        if(app.isUseByDefault()) {
            String name = fe.getDotName();
            String extKey = fe.getKey();
            String appLocation = app.getLocation();
            String appKey = app.getKey();
            String command = app.getCommand();
            property = getExtProperty(props, name, EXT_HKCRSHELL_OPEN_COMMAND_PROPERTY);
            if(property!=null) {
                String s = SHELL_OPEN_COMMAND;
                registry.deleteKey(clSection, clKey +  extKey + s);  //  delete command
                s = s.substring(0,s.lastIndexOf(SEP));
                registry.deleteKey(clSection, clKey +  extKey + s);  //  delete open
                s = s.substring(0,s.lastIndexOf(SEP)); //
                registry.deleteKey(clSection, clKey +  extKey + s);  //  delete shell
            }
            property = getExtProperty(props, name, DOT + EXT_HKCU_DEFAULTAPP_PROPERTY);
            
            if(registry.keyExists(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name)) {
                if(property!=null) {
                    registry.setStringValue(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name, APPLICATION_VALUE_NAME, property);
                } else {
                    registry.deleteValue(HKCU, CURRENT_USER_FILE_EXT_KEY + SEP + name, APPLICATION_VALUE_NAME);
                }
            }
        }
    }
    private void addToOpenWithList(SystemApplicationKey app, FileExtensionKey ext, Properties props) throws NativeException {
        String name = ext.getDotName();
        String extKey = ext.getKey();
        String appName = app.getKey();
        if(app.isAddOpenWithList()) {
            if(!isEmpty(name) && !isEmpty(extKey) && !isEmpty(appName)) {
                if(!registry.keyExists(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME,appName)) {
                    registry.createKey(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME,appName);
                    setExtProperty(props, name, EXT_HKCR_OPENWITHLIST_PROPERTY, CREATED);
                }
                addCurrentUserOpenWithList(name, extKey,appName, props);
                
                if(!registry.keyExists(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME)) {
                    registry.createKey(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME);
                    setExtProperty(props, name, EXT_HKCR_OPENWITHPROGIDS_PROPERTY, CREATED);
                }
                registry.setNoneValue(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME, extKey);
                addCurrentUserOpenWithProgids(name, extKey, appName, props);
            }
        }
    }
    private void removeFromOpenWithList(SystemApplicationKey app, FileExtensionKey ext, Properties props) throws NativeException {
        String property;
        String name = ext.getDotName();
        String extKey = ext.getKey();
        String appName = app.getKey();
        property = getExtProperty(props, name, EXT_HKCR_OPENWITHLIST_PROPERTY);
        if(property!=null) {
            if(registry.keyExists(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME,appName)) {
                registry.deleteKey(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME,appName);
            }
            if(registry.keyExists(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME)) {
                if(registry.getSubKeys(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME).length==0) {
                    registry.deleteKey(clSection, clKey +  name + SEP + OPEN_WITH_LIST_KEY_NAME);
                }
            }
        }
        property = getExtProperty(props, name, EXT_HKCR_OPENWITHPROGIDS_PROPERTY);
        if(property!=null) {
            if(registry.keyExists(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME)) {
                if(registry.valueExists(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME, extKey)) {
                    registry.deleteValue(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME, extKey);
                }
                if(registry.keyEmpty(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME)) {
                    registry.deleteKey(clSection, clKey +  name + SEP + OPEN_WITH_PROGIDS_KEY_NAME);
                }
            }
        }
        String cuExtKey = CURRENT_USER_FILE_EXT_KEY + SEP + name;
        property = getExtProperty(props,name, EXT_HKCU_OPENWITHPROGIDS_PROPERTY);
        if(property!=null) {
            if(registry.keyExists(HKCU, cuExtKey, OPEN_WITH_PROGIDS_KEY_NAME)) {
                if(registry.valueExists(HKCU, cuExtKey + SEP +  OPEN_WITH_PROGIDS_KEY_NAME,ext.getKey())) {
                    registry.deleteValue(HKCU, cuExtKey + SEP + OPEN_WITH_PROGIDS_KEY_NAME,ext.getKey());
                }
            }
        }
        property = getExtProperty(props,name, EXT_HKCU_OPENWITHLIST_PROPERTY);
        if(property!=null &&registry.keyExists(HKCU, cuExtKey, OPEN_WITH_LIST_KEY_NAME)) {
            for(int i=0;i<MRU_VALUES.length();i++) {
                String ch = MRU_VALUES.substring(i,i+1);
                if( registry.valueExists(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,ch) &&
                        registry.getStringValue(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,ch).equals(appName)) {
                    
                    registry.deleteValue(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,ch);
                    if(registry.valueExists(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,MRULIST_VALUE_NAME)) {
                        String mru = registry.getStringValue(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,MRULIST_VALUE_NAME);
                        mru = mru.replace(ch,EMPTY_STRING);
                        if(mru.equals(EMPTY_STRING)) {
                            registry.deleteValue(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,MRULIST_VALUE_NAME);
                        } else {
                            registry.setStringValue(HKCU, cuExtKey + SEP +  OPEN_WITH_LIST_KEY_NAME,MRULIST_VALUE_NAME,mru);
                        }
                    }
                    break;
                }
            }
            if(registry.keyEmpty(HKCU, cuExtKey + SEP+ OPEN_WITH_LIST_KEY_NAME)) {
                registry.deleteKey(HKCU, cuExtKey, OPEN_WITH_LIST_KEY_NAME);
            }
        }
    }
    private void addCurrentUserOpenWithList(String name, String extKey, String appName, Properties props) throws NativeException {
        boolean found = false;
        String freeValue = MRU_VALUES.substring(0,1);//=a
        String cuExtKey = CURRENT_USER_FILE_EXT_KEY + SEP + name;
        
        if(!registry.keyExists(HKCU, cuExtKey , OPEN_WITH_LIST_KEY_NAME)) {
            registry.createKey(HKCU, cuExtKey , OPEN_WITH_LIST_KEY_NAME);
        } else {
            freeValue = null;
            for(int i=0;i<MRU_VALUES.length();i++) {
                String s = MRU_VALUES.substring(i,i+1);
                
                if(registry.valueExists(HKCU, cuExtKey + SEP + OPEN_WITH_LIST_KEY_NAME, s)) {
                    
                    String app = registry.getStringValue(HKCU, cuExtKey + SEP + OPEN_WITH_LIST_KEY_NAME, s);
                    if(app.equals(appName)) {
                        found = true;
                    }
                } else if(freeValue==null) {
                    freeValue = s;
                }
            }
        }
        if(!found) {
            registry.setStringValue(HKCU,
                    cuExtKey + SEP
                    + OPEN_WITH_LIST_KEY_NAME, freeValue, appName);
            setExtProperty(props,name,EXT_HKCU_OPENWITHLIST_PROPERTY,CREATED);
            
            String mru = freeValue;
            if(registry.valueExists(HKCU, cuExtKey + SEP
                    + OPEN_WITH_LIST_KEY_NAME, MRULIST_VALUE_NAME)) {
                
                mru = mru + registry.getStringValue(HKCU,
                        cuExtKey + SEP + OPEN_WITH_LIST_KEY_NAME,
                        MRULIST_VALUE_NAME);
            }
            registry.setStringValue(HKCU,
                    cuExtKey + SEP
                    + OPEN_WITH_LIST_KEY_NAME, MRULIST_VALUE_NAME, mru);
        }
    }
    
    private void addCurrentUserOpenWithProgids(String name, String extKey, String appName, Properties props) throws NativeException {
        String cuExtKey = CURRENT_USER_FILE_EXT_KEY + SEP + name;
        
        if(!registry.keyExists(HKCU, cuExtKey , OPEN_WITH_PROGIDS_KEY_NAME)) {
            registry.createKey(HKCU, cuExtKey , OPEN_WITH_PROGIDS_KEY_NAME);
            
        } else {
            String [] values = registry.getValueNames(HKCU, cuExtKey + SEP + OPEN_WITH_PROGIDS_KEY_NAME);
            for(String value: values) {
                if (value.equals(appName)) {
                    return;
                }
            }
        }
        
        registry.setNoneValue(HKCU,
                cuExtKey + SEP
                + OPEN_WITH_PROGIDS_KEY_NAME, extKey);
        setExtProperty(props,name,EXT_HKCU_OPENWITHPROGIDS_PROPERTY,CREATED);
    }
    
    private void registerApplication( SystemApplicationKey app, FileExtensionKey key, Properties props) throws NativeException {
        String appLocation = app.getLocation();
        String appKey = app.getKey();
        String appFriendlyName = app.getFriendlyName();
        String command = app.getCommand();
        String name = key.getDotName();
        if(!registry.keyExists(clSection, clKey + APPLICATIONS_KEY_NAME,appKey)) {
            registry.createKey(clSection, clKey + APPLICATIONS_KEY_NAME,appKey);
            setExtProperty(props, name, EXT_HKCR_APPLICATIONS_PROPERTY, CREATED);
            if(!isEmpty(appFriendlyName)) {
                registry.setStringValue(clSection, clKey +
                        APPLICATIONS_KEY_NAME + SEP + appKey,
                        FRIENDLYAPPNAME_VALUE_NAME,
                        appFriendlyName);
                if(registry.keyExists(HKCU,CURRENT_USER_MUI_CACHE_KEY)) {
                    String s = CREATED;
                    if(registry.valueExists(HKCU,CURRENT_USER_MUI_CACHE_KEY, appLocation)) {
                        s = registry.getStringValue(HKCU,CURRENT_USER_MUI_CACHE_KEY, appLocation);
                    }
                    registry.setStringValue(HKCU,CURRENT_USER_MUI_CACHE_KEY, appLocation, appFriendlyName);
                    setExtProperty(props, name, EXT_HKCU_MUICACHE_PROPERTY,s);
                }
            }
            //set application`s 'open' command
            registry.createKey(clSection, clKey +  APPLICATIONS_KEY_NAME + SEP + appKey + SHELL_OPEN_COMMAND);
            registry.setStringValue(clSection, clKey +  APPLICATIONS_KEY_NAME + SEP + appKey + SHELL_OPEN_COMMAND,
                    EMPTY_STRING, constructCommand(app));
        }
    }
    private void unregisterApplication(SystemApplicationKey app, FileExtensionKey key, Properties props) throws NativeException {
        String name = key.getDotName();
        String property = getExtProperty(props, name, EXT_HKCR_APPLICATIONS_PROPERTY);
        if(property!=null) {
            String appKey = app.getKey();
            if(registry.keyExists(clSection, clKey + APPLICATIONS_KEY_NAME,appKey)) {
                String [] openCommandKey = SHELL_OPEN_COMMAND.split(SEP + SEP);
                for(int i=openCommandKey.length-1;i>=0;i--) {
                    String str = EMPTY_STRING;
                    for(int j=i-1;j>=0;j--) {
                        str = str + SEP + openCommandKey[i-j];
                    }
                    if(registry.keyExists(clSection, clKey + APPLICATIONS_KEY_NAME + SEP + appKey + str)) {
                        if(registry.getSubKeys(clSection, clKey + APPLICATIONS_KEY_NAME + SEP + appKey + str).length==0) {
                            registry.deleteKey(clSection, clKey + APPLICATIONS_KEY_NAME + SEP + appKey + str);
                        }
                    }
                }
            }
        }
        property = getExtProperty(props, name, EXT_HKCU_MUICACHE_PROPERTY);
        if(property!=null) {
            if(registry.valueExists(HKCU,CURRENT_USER_MUI_CACHE_KEY, app.getLocation())) {
                if(property.equals(CREATED)) {
                    registry.deleteValue(HKCU,CURRENT_USER_MUI_CACHE_KEY, app.getLocation());
                } else {
                    registry.setStringValue(HKCU,CURRENT_USER_MUI_CACHE_KEY, app.getLocation(),property);
                }
            }
        }
    }
    
    private String getApplicationKey(SystemApplication app) throws NativeException {
        String appName = new File(app.getLocation()).getName();
        String appKey = appName;
        int index = 1;
        while(registry.keyExists(clSection, clKey +  APPLICATIONS_KEY_NAME, appKey)) {
            if(registry.keyExists(clSection, clKey +  APPLICATIONS_KEY_NAME + SEP + appKey + SHELL_OPEN_COMMAND)) {
                String command = registry.getStringValue(clSection, clKey +
                        APPLICATIONS_KEY_NAME + SEP + appKey + SHELL_OPEN_COMMAND,
                        EMPTY_STRING);
                if(command.equals(constructCommand(app))) {
                    break;
                }
            }
            appKey = appName + DOT + (index++);
        }
        return appKey;
    }
    
    private String getLongExtensionName(FileExtension ext) throws NativeException {
        String dotname = ext.getDotName();
        String name = ext.getName();
        String key = null;
        if(registry.keyExists(clSection, clKey +  dotname)) {
            key = registry.getStringValue(clSection, clKey + dotname,EMPTY_STRING);
        }
        if(isEmpty(key) || !registry.keyExists(clSection, clKey +  key)) {
            int index = 1;
            do {
                key = EXT_PREFIX + name + EXT_SUFFIX + DOT + (index++);
            } while(registry.keyExists(clSection, clKey + key));
        }
        return key;
    }
    
    private String constructCommand(SystemApplication app) {
        String command = app.getCommand();
        if(command==null) {
            command = DEFAULT_OPEN_COMMAND;
        }
        return ("\"" +  app.getLocation() + "\"" +  SPACE + command);
    }
    
    private boolean isEmpty(String str) {
        return (str==null || str.equals(EMPTY_STRING));
    }
    
    private void notifyAssociationChanged() throws NativeException {
        notifyAssociationChanged0();
    }
    
    private class WindowsProcessOnExitCleanerHandler extends ProcessOnExitCleanerHandler {
        public WindowsProcessOnExitCleanerHandler(String cleanerDefaultFileName) {
            super(cleanerDefaultFileName);
        }
        protected void writeCleaner(File cleanerFile) throws IOException {            
            InputStream is = ResourceUtils.getResource(CLEANER_RESOURCE);
            FileUtils.writeFile(cleanerFile, is);
            is.close();              
        }
        
        protected void writeCleaningFileList(File listFile, List<String> files) throws IOException {
            FileUtils.writeStringList(listFile, files, "UNICODE");
        }
    }
    
    private class FileExtensionKey extends FileExtension {
        private String key;
        public FileExtensionKey(FileExtension fe, String key) {
            super(fe);
            this.key = key;
        }
        public String getKey() {
            return key;
        }
    }
    
    private class SystemApplicationKey extends SystemApplication {
        private String key;
        public SystemApplicationKey(SystemApplication sapp, String extKey) {
            super(sapp);
            key = extKey;
        }
        public String getKey() {
            return key;
        }
    }
    private String getExtProperty(Properties props, String name) {
        return props.getProperty(EXTENSION_VALUE_NAME + name);
    }
    private String getExtProperty(Properties props, String name, String prop) {
        return props.getProperty(EXTENSION_VALUE_NAME + name + DOT + prop);
    }
    private void setExtProperty(Properties props, String name, String value) {
        props.setProperty(EXTENSION_VALUE_NAME + name, value);
    }
    private void setExtProperty(Properties props, String name, String prop, String value) {
        props.setProperty(EXTENSION_VALUE_NAME + name + DOT + prop, value);
    }
    
// native declarations //////////////////////////////////////////////////////////
    private native boolean isCurrentUserAdmin0();
    
    private native long getFreeSpace0(String string);
    
    private native void createShortcut0(Shortcut shortcut);
    
    private native void deleteFileOnReboot0(String file);
    
    private native void notifyAssociationChanged0();
    
    private native int checkAccessTokenAccessLevel0(String path, int desiredLevel) throws NativeException;
    
    private native int notifyEnvironmentChanged0() throws NativeException;
}