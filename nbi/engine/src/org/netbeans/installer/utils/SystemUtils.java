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
package org.netbeans.installer.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;
import org.netbeans.installer.utils.exceptions.UnsupportedActionException;
import org.netbeans.installer.utils.system.GenericSystemUtils;
import org.netbeans.installer.utils.system.UnixSystemUtils;
import org.netbeans.installer.utils.system.WindowsSystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class SystemUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static SystemUtils instance;
    
    public static synchronized SystemUtils getInstance() {
        if (instance == null) {
            switch (Platform.getCurrentPlatform()) {
                case WINDOWS:
                    instance = new WindowsSystemUtils();
                    break;
                case LINUX:
                case SOLARIS_X86:
                case SOLARIS_SPARC:
                    instance = new UnixSystemUtils();
                    break;
                case MACOS_X_PPC:
                case MACOS_X_X86:
                default:
                    instance = new GenericSystemUtils();
                    break;
            }
        }
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract String parseString(String string, ClassLoader loader);
    
    public abstract File parsePath(String path, ClassLoader loader);
    
    public abstract File getUserHomeDirectory();
    
    public abstract File getCurrentDirectory();
    
    public abstract File getTempDirectory();
    
    public abstract File getDefaultApplicationsLocation();
    
    public abstract String getLineSeparator();
    
    public abstract String getFileSeparator();
    
    public abstract String getPathSeparator();
    
    public abstract long getFreeSpace(File file) throws UnsupportedActionException;
    
    public abstract ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException;
    
    public abstract boolean isPathValid(String path);
    
    public abstract boolean isPortAvailable(int port);
    
    public abstract File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException, UnsupportedActionException;
    
    public abstract void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException, UnsupportedActionException;
    
    public abstract String getEnvironmentVariable(String name, EnvironmentVariableScope scope) throws IOException, UnsupportedActionException;
    
    public abstract String getRawEnvironmentVariable(String name, EnvironmentVariableScope scope) throws IOException, UnsupportedActionException;
    
    public abstract String setEnvironmentVariable(String name, String value, EnvironmentVariableScope scope) throws IOException, UnsupportedActionException;
    
    // some helper overloaded implementations //////////////////////////////////
    public String parseString(String string) {
        return parseString(string, getClass().getClassLoader());
    }
    
    public File parsePath(String string) {
        return parsePath(string, getClass().getClassLoader());
    }
    
    public ExecutionResults executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    public String getEnvironmentVariable(String name) throws IOException, UnsupportedActionException {
        return getEnvironmentVariable(name, EnvironmentVariableScope.CURRENT_USER);
    }
    
    public void setEnvironmentVariable(String name, String value) throws IOException, UnsupportedActionException {
        setEnvironmentVariable(name, value, EnvironmentVariableScope.CURRENT_USER);
    }
    
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }  catch (InterruptedException e) {
            ErrorManager.getInstance().notify(ErrorLevel.DEBUG,
                    "Interrupted while sleeping", e);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static enum Platform {
        WINDOWS("windows", "Windows"),
        LINUX("linux", "Linux"),
        SOLARIS_X86("solaris-x86", "Solaris X86"),
        SOLARIS_SPARC("solaris-sparc", "Solaris Sparc"),
        MACOS_X_PPC("macos-x-ppc", "MacOS X (PPC)"),
        MACOS_X_X86("macos-x-x86", "MacOS X (Intel)");
        
        public static Platform parsePlatform(String name) throws UnrecognizedObjectException {
            for (Platform platform: Platform.values()) {
                if (platform.name.equals(name)) {
                    return platform;
                }
            }
            
            throw new UnrecognizedObjectException("Platform \"" + name + "\" is not recognized.");
        }
        
        public static List<Platform> parsePlatforms(String platformsString) throws UnrecognizedObjectException {
            if (platformsString.equals("all")) {
                return Arrays.asList(Platform.values());
            } else {
                List<Platform> platforms = new ArrayList<Platform>();
                
                for (String name: platformsString.split(" ")) {
                    Platform platform = parsePlatform(name);
                    if (!platforms.contains(platform)) {
                        platforms.add(platform);
                    }
                }
                return platforms;
            }
        }
        
        public static Platform getCurrentPlatform() {
            if (System.getProperty("os.name").contains("Windows")) {
                return Platform.WINDOWS;
            }
            if (System.getProperty("os.name").contains("Linux")) {
                return Platform.LINUX;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && System.getProperty("os.arch").contains("ppc")) {
                return Platform.MACOS_X_PPC;
            }
            if (System.getProperty("os.name").contains("Mac OS X") && System.getProperty("os.arch").contains("i386")) {
                return Platform.MACOS_X_X86;
            }
            if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("sparc")) {
                return Platform.SOLARIS_SPARC;
            }
            if (System.getProperty("os.name").contains("SunOS") && System.getProperty("os.arch").contains("x86")) {
                return Platform.SOLARIS_X86;
            }
            
            return null;
        }
        
        public static boolean isWindows() {
            return Platform.getCurrentPlatform() == Platform.WINDOWS;
        }
        
        public static boolean isMacOS() {
            return (Platform.getCurrentPlatform() == Platform.MACOS_X_X86) ||
                    (Platform.getCurrentPlatform() == Platform.MACOS_X_PPC);
        }
        
        private String name;
        private String displayName;
        
        private Platform(String aName, String aDisplayName) {
            name = aName;
            displayName = aDisplayName;
        }
        
        public boolean equals(Platform platform) {
            return name.equals(platform.name);
        }
        
        public String getName() {
            return name;
        }
        
        public String toString() {
            return displayName;
        }
    }
    
    public static enum ShortcutLocationType {
        CURRENT_USER_DESKTOP,
        ALL_USERS_DESKTOP,
        CURRENT_USER_START_MENU,
        ALL_USERS_START_MENU
    }
    
    public static enum EnvironmentVariableScope {
        CURRENT_USER,
        ALL_USERS
    }
    
    public static class ExecutionResults {
        public static final int TIMEOUT_ERRORCODE = Integer.MAX_VALUE;
        
        private int    errorCode = TIMEOUT_ERRORCODE;
        private String stdOut    = "";
        private String stdErr    = "";
        
        public ExecutionResults() {
            // do nothing
        }
        
        public ExecutionResults(final int errorCode, final String stdOut, final String stdErr) {
            this.errorCode = errorCode;
            this.stdOut    = stdOut;
            this.stdErr    = stdErr;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
        
        public String getStdOut() {
            return stdOut;
        }
        
        public String getStdErr() {
            return stdErr;
        }
    }
    
    public static class Shortcut {
        private Map<Locale, String> names = new HashMap<Locale, String>();
        private Map<Locale, String> descriptions = new HashMap<Locale, String>();
        
        private File executable;
        private String[] arguments;
        private File workingDirectory;
        
        private String path;
        
        private File icon;
        
        private String[] categories;
        
        private String fileName;
        
        public Shortcut(final String name, final File executable) {
            this.names.put(Locale.getDefault(), name);
            
            this.executable = executable;
        }
        
        public Map<Locale, String> getNames() {
            return names;
        }
        
        public void setNames(final Map<Locale, String> names) {
            this.names = names;
        }
        
        public String getName(final Locale locale) {
            return names.get(locale);
        }
        
        public String getName() {
            return getName(Locale.getDefault());
        }
        
        public void addName(final String name, final Locale locale) {
            names.put(locale, name);
        }
        
        public void setName(final String name) {
            addName(name, Locale.getDefault());
        }
        
        public void removeName(final Locale locale) {
            names.remove(locale);
        }
        
        public Map<Locale, String> getDescriptions() {
            return descriptions;
        }
        
        public void setDescriptions(final Map<Locale, String> comments) {
            this.descriptions = comments;
        }
        
        public String getDescription(final Locale locale) {
            return descriptions.get(locale);
        }
        
        public void addDescription(final String comment, final Locale locale) {
            descriptions.put(locale, comment);
        }
        
        public void removeComment(final Locale locale) {
            descriptions.remove(locale);
        }
        
        public File getExecutable() {
            return executable;
        }
        
        public void setExecutable(final File executable) {
            this.executable = executable;
        }
        
        public String[] getArguments() {
            return arguments;
        }
        
        public void setArguments(final String[] arguments) {
            this.arguments = arguments;
        }
        
        public File getWorkingDirectory() {
            return workingDirectory;
        }
        
        public void setWorkingDirectory(final File workingDirectory) {
            this.workingDirectory = workingDirectory;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(final String path) {
            this.path = path;
        }
        
        public File getIcon() {
            return icon;
        }
        
        public void setIcon(final File icon) {
            this.icon = icon;
        }
        
        public String[] getCategories() {
            return categories;
        }
        
        public void setCategories(final String[] categories) {
            this.categories = categories;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }
    }
    
    public static final long MAX_EXECUTION_TIME = 120000; // 2 minutes seconds
    public static final int  BUFFER_SIZE        = 4096;   // 4 kilobytes
    public static final int  DELAY              = 50;     // 50 milliseconds
}
