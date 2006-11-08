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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;
import org.netbeans.installer.utils.exceptions.UnsupportedActionException;
import org.netbeans.installer.utils.system.GenericSystemUtils;
import org.netbeans.installer.utils.system.NativeUtils;

/**
 *
 * @author Kirill Sorokin
 */
public final class SystemUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final long MAX_EXECUTION_TIME = 120000; // 2 minutes
    public static final int  BUFFER_SIZE        = 4096;   // 4 kilobytes
    public static final int  DELAY              = 50;     // 50 milliseconds
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, String> environment = new ProcessBuilder().environment();
    
    private static NativeUtils nativeUtils;
    
    // string resolution ////////////////////////////////////////////////////////////
    public static String parseString(String string) {
        return parseString(string, SystemUtils.class.getClassLoader());
    }
    
    public static String parseString(String string, ClassLoader loader) {
        String parsedString = string;
        
        // N for Name
        parsedString = parsedString.replaceAll("(?<!\\\\)\\$N\\{install\\}", StringUtils.escapeForRE(getDefaultApplicationsLocation().getAbsolutePath()));
        parsedString = parsedString.replaceAll("(?<!\\\\)\\$N\\{home\\}", StringUtils.escapeForRE(getUserHomeDirectory().getAbsolutePath()));
        parsedString = parsedString.replaceAll("(?<!\\\\)\\$N\\{temp\\}", StringUtils.escapeForRE(getTempDirectory().getAbsolutePath()));
        parsedString = parsedString.replaceAll("(?<!\\\\)\\$N\\{current\\}", StringUtils.escapeForRE(getCurrentDirectory().getAbsolutePath()));
        
        Matcher matcher;
        
        // P for Properties
        matcher = Pattern.compile("(?<!\\\\)\\$P\\{(.*?), (.*?)(?:, (.*?))?\\}").matcher(parsedString);
        while (matcher.find()) {
            String basename        = matcher.group(1);
            String key             = matcher.group(2);
            String argumentsString = matcher.group(3);
            
            if (argumentsString == null) {
                parsedString = parsedString.replace(matcher.group(), ResourceUtils.getString(basename, key, loader));
            } else {
                Object[] arguments = (Object[]) argumentsString.split(", ?");
                
                parsedString = parsedString.replace(matcher.group(), ResourceUtils.getString(basename, key, loader, arguments));
            }
        }
        
        // F for Field
        matcher = Pattern.compile("(?<!\\\\)\\$F\\{((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+[a-zA-Z_][a-zA-Z_0-9]*)\\.([a-zA-Z_][a-zA-Z_0-9]*)\\}").matcher(parsedString);
        while (matcher.find()) {
            String classname = matcher.group(1);
            String fieldname = matcher.group(2);
            
            try {
                Object object = loader.loadClass(classname).getField(fieldname).get(null);
                if (object != null) {
                    String value = object.toString();
                    
                    parsedString = parsedString.replace(matcher.group(), value);
                }
            } catch (IllegalArgumentException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (SecurityException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (ClassNotFoundException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (IllegalAccessException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (NoSuchFieldException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            }
        }
        
        // M for Method
        matcher = Pattern.compile("(?<!\\\\)\\$M\\{((?:[a-zA-Z_][a-zA-Z_0-9]*\\.)+[a-zA-Z_][a-zA-Z_0-9]*)\\.([a-zA-Z_][a-zA-Z_0-9]*)\\(\\)\\}").matcher(parsedString);
        while (matcher.find()) {
            String classname = matcher.group(1);
            String methodname = matcher.group(2);
            
            try {
                Method method = loader.loadClass(classname).getMethod(methodname);
                if (method != null) {
                    Object object = method.invoke(null);
                    
                    if (object != null) {
                        String value = object.toString();
                        
                        parsedString = parsedString.replace(matcher.group(), value);
                    }
                }
            } catch (IllegalArgumentException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (SecurityException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (ClassNotFoundException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (IllegalAccessException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (NoSuchMethodException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } catch (InvocationTargetException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            }
        }
        
        // R for Resource
        matcher = Pattern.compile("(?<!\\\\)\\$R\\{(.*?)\\}").matcher(parsedString);
        while (matcher.find()) {
            String path = matcher.group(1);
            
            InputStream inputStream = null;
            try {
                inputStream  = ResourceUtils.getResource(path, loader);
                parsedString = parsedString.replace(matcher.group(), StringUtils.readStream(inputStream));
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, "Cannot parse pattern: " + matcher.group(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, "Cannot close input stream after reading resource: " + matcher.group(), e);
                    }
                }
            }
        }
        
        parsedString.replace("\\$", "$");
        parsedString.replace("\\\\", "\\");
        
        return parsedString;
    }
    
    public static File parsePath(String string) {
        return parsePath(string, SystemUtils.class.getClassLoader());
    }
    
    public static File parsePath(String path, ClassLoader loader) {
        String parsedString = parseString(path, loader);
        
        parsedString = parsedString.replace("\\", getFileSeparator());
        parsedString = parsedString.replace("/", getFileSeparator());
        
        return new File(parsedString).getAbsoluteFile();
    }
    
    public static File getUserHomeDirectory() {
        return new File(System.getProperty("user.home"));
    }
    
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    
    public static boolean isCurrentUserAdmin() {
        return getNativeUtils().isCurrentUserAdmin();
    }
    
    public static File getCurrentDirectory() {
        return new File(".");
    }
    
    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
    
    public static File getDefaultApplicationsLocation() {
        getNativeUtils().getDefaultApplicationsLocation();
    }
    
    public static File getCurrentJavaHome() {
        return new File(System.getProperty("java.home"));
    }
    
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }
    
    public static String getPathSeparator() {
        return System.getProperty("path.separator");
    }
    
    public static long getFreeSpace(File file) {
        return getNativeUtils().getFreeSpace(file);
    }
    
    public static ExecutionResults executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    public static ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException {
        // construct the initial log message
        StringBuilder stringBuilder = new StringBuilder();
        for(String temp : command) {
            stringBuilder.append(temp).append(" "); //NOI18N
        }
        
        if (workingDirectory == null) {
            workingDirectory = getCurrentDirectory();
        }
        
        LogManager.log(ErrorLevel.MESSAGE, "executing command: " + stringBuilder + ", in directory: " + workingDirectory);
        LogManager.indent();
        
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int           errorLevel = ExecutionResults.TIMEOUT_ERRORCODE;
        
        ProcessBuilder builder= new ProcessBuilder(command).directory(workingDirectory);
        
        if (environment != null) {
            builder.environment().clear();
            builder.environment().putAll(environment);
            environment = null;
        }
        Process process = builder.start();
        
        long runningTime;
        for (runningTime = 0; runningTime < MAX_EXECUTION_TIME; runningTime += DELAY) {
            String string;
            
            string = StringUtils.readStream(process.getInputStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                }
                
                processStdOut.append(string);
            }
            
            string = StringUtils.readStream(process.getErrorStream());
            if (string.length() > 0) {
                BufferedReader reader = new BufferedReader(new StringReader(string));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    LogManager.log(ErrorLevel.MESSAGE, "[stdout]: " + line);
                }
                
                processStdErr.append(string);
            }
            
            try {
                errorLevel = process.exitValue();
                break;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
            
            try {
                Thread.sleep(DELAY);
            }  catch (InterruptedException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, e);
            }
        }
        
        if (runningTime >= MAX_EXECUTION_TIME) {
            process.destroy();
            LogManager.log(ErrorLevel.MESSAGE, "[return]: killed by timeout");
        }  else {
            LogManager.log(ErrorLevel.MESSAGE, "[return]: " + errorLevel);
        }
        
        process.destroy();
        
        LogManager.unindent();
        LogManager.log(ErrorLevel.MESSAGE, "... command execution finished");
        
        return new ExecutionResults(errorLevel, processStdOut.toString(), processStdErr.toString());
    }
    
    public static boolean isPathValid(String path) {
        return getNativeUtils().isPathValid(path);
    }
    
    public static boolean isPortAvailable(int port) {
        ServerSocket socket = null;
        
        try {
            socket = new ServerSocket(port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.ERROR, "Could not close server socket on port " + port, e);
                }
            }
        }
    }
    
    public static int getAvailablePort(int basePort) {
        int port = basePort;
        
        while (!isPortAvailable(port)) {
            port++;
        }
        
        return port;
    }
    
    public static File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) {
        return getNativeUtils().getShortcutLocation(shortcut, locationType);
    }
    
    public static File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws IOException {
        return getNativeUtils().createShortcut(shortcut, locationType);
    }
    
    public static void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws IOException, UnsupportedActionException {
        getNativeUtils().removeShortcut(shortcut, locationType, deleteEmptyParents);
    }
    
    public static void addComponentToSystemInstallManager(ProductComponent comp) {
        getNativeUtils().addComponentToSystemInstallManager(comp);
    }
    
    public static void removeComponentFromSystemInstallManager(ProductComponent comp) {
        getNativeUtils().removeComponentFromSystemInstallManager(comp);
    }
    
    public static String getEnvironmentVariable(String name) throws IOException {
        getEnvironmentVariable(name, EnvironmentVariableScope.PROCESS, true);
    }
    
    public static String getEnvironmentVariable(String name, EnvironmentVariableScope scope, boolean expand) throws IOException {
        return getNativeUtils().getEnvironmentVariable(name, scope, expand);
    }
    
    public static void setEnvironmentVariable(String name, String value) throws IOException {
        setEnvironmentVariable(name, value, EnvironmentVariableScope.PROCESS, true);
    }
    
    public static void setEnvironmentVariable(String name, String value, EnvironmentVariableScope scope, boolean expand) throws IOException {
        getNativeUtils().setEnvironmentVariable(name, value, scope, expand);
    }
    
    public static List<File> findIrrelevantFiles(File parent) throws IOException {
        return getNativeUtils().findIrrelevantFiles(parent);
    }
    
    public static List<File> findIrrelevantFiles(File... parents) throws IOException {
        List<File> list = new LinkedList<File>();
        
        for (File parent: parents) {
            list.addAll(findIrrelevantFiles(parent));
        }
        
        return list;
    }
    
    public static void removeIrrelevantFiles(File parent) throws IOException {
        FileUtils.deleteFiles(findIrrelevantFiles(parent));
    }
    
    public static void removeIrrelevantFiles(File... parents) throws IOException {
        for (File file: parents) {
            removeIrrelevantFiles(file);
        }
    }
    
    public static List<File> findExecutableFiles(File parent) throws IOException {
        return getNativeUtils().findExecutableFiles(parent);
    }
    
    public static List<File> findExecutableFiles(File... parents) throws IOException {
        List<File> list = new LinkedList<File>();
        
        for (File parent: parents) {
            list.addAll(findExecutableFiles(parent));
        }
        
        return list;
    }
    
    public static void correctFilesPermissions(File parent) throws IOException {
        getNativeUtils().correctFilesPermissions(parent);
    }
    
    public static void correctFilesPermissions(File... parents) throws IOException {
        for (File file: parents) {
            correctFilesPermissions(file);
        }
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }  catch (InterruptedException e) {
            ErrorManager.notify(ErrorLevel.DEBUG,
                    "Interrupted while sleeping", e);
        }
    }
    
    public static Map<String, String> getEnvironment() {
        return environment;
    }
    
    // native accessor //////////////////////////////////////////////////////////////
    public static synchronized NativeUtils getNativeUtils() {
        if (nativeUtils == null) {
            nativeUtils = NativeUtils.getInstance();
        }
        
        return nativeUtils;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
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
            return name;
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
        ALL_USERS,
        PROCESS
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
        
        private String relativePath;
        private String fileName;
        
        private File executable;
        private File workingDirectory;
        private List<String> arguments = new ArrayList<String>();
        
        private File icon;
        
        private String[] categories;
        
        private String path;
        
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
            if (locale.equals(Locale.getDefault())) {
                throw new IllegalArgumentException("Removing of name for default locale is not supported");
            } else {
                names.remove(locale);
            }
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
        
        public String getDescription() {
            return descriptions.get(Locale.getDefault());
        }
        
        public void addDescription(final String description, final Locale locale) {
            descriptions.put(locale, description);
        }
        
        public void setDescription(final String description) {
            addDescription(description, Locale.getDefault());
        }
        
        public void removeDescription(final Locale locale) {
            descriptions.remove(locale);
        }
        
        public String getRelativePath() {
            return relativePath;
        }
        
        public void setRelativePath(final String relativePath) {
            this.relativePath = relativePath;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public void setFileName(final String fileName) {
            this.fileName = fileName;
        }
        
        public File getExecutable() {
            return executable;
        }
        
        public String getExecutablePath() {
            return executable.getAbsolutePath();
        }
        
        public void setExecutable(final File executable) {
            this.executable = executable;
        }
        
        public List<String> getArguments() {
            return arguments;
        }
        
        public String getArgumentsString() {
            if (arguments.size() == 0) {
                StringBuilder builder = new StringBuilder();
                
                for (int i = 0; i < arguments.size(); i++) {
                    builder.append(arguments.get(i));
                    
                    if (i != arguments.size() - 1) {
                        builder.append(" ");
                    }
                }
                
                return builder.toString();
            } else {
                return null;
            }
        }
        
        public void setArguments(final List<String> arguments) {
            this.arguments = arguments;
        }
        
        public void addArgument(final String argument) {
            arguments.add(argument);
        }
        
        public void removeArgument(final String argument) {
            arguments.remove(argument);
        }
        
        public File getWorkingDirectory() {
            return workingDirectory;
        }
        
        public String getWorkingDirectoryPath() {
            if (workingDirectory != null) {
                return workingDirectory.getAbsolutePath();
            } else {
                return null;
            }
        }
        
        public void setWorkingDirectory(final File workingDirectory) {
            this.workingDirectory = workingDirectory;
        }
        
        public File getIcon() {
            return icon;
        }
        
        public String getIconPath() {
            if (icon != null) {
                return icon.getAbsolutePath();
            } else {
                return null;
            }
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
        
        public String getPath() {
            return path;
        }
        
        public void setPath(final String path) {
            this.path = path;
        }
    }
}
