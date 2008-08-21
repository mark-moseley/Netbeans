/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.helper.ShortcutLocationType;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.NativeUtils;
import org.netbeans.installer.utils.system.NativeUtilsFactory;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherFactory;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.resolver.StringResolverUtil;
import org.netbeans.installer.utils.system.shortcut.LocationType;

/**
 *
 * @author Kirill Sorokin
 */
public final class SystemUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<String, String> environment =
            new ProcessBuilder().environment();
    
    private static NativeUtils nativeUtils;
    
    // string resolution ////////////////////////////////////////////////////////////
    public static String resolveString(String string) {
        return resolveString(string, SystemUtils.class.getClassLoader());
    }
    
    public static String resolveString(String string, ClassLoader loader) {
        return StringResolverUtil.resolve(string, loader);
    }
    
    public static File resolvePath(String string) {
        return resolvePath(string, SystemUtils.class.getClassLoader());
    }
    
    public static File resolvePath(String path, ClassLoader loader) {
        final String separator = getFileSeparator();
        
        String parsed = resolveString(path, loader);
        
        parsed = parsed.replace("\\", separator);
        parsed = parsed.replace("/", separator);
        
        try {
            if (parsed.contains(separator + ".." + separator) ||
                    parsed.contains(separator + "." + separator) ||
                    parsed.endsWith(separator + "..") ||
                    parsed.endsWith(separator + ".")) {
                return new File(parsed).getCanonicalFile();
            }
        } catch (IOException e) {
            ErrorManager.notifyDebug("Could not get the cannonical path", e);
        }
        
        return new File(parsed).getAbsoluteFile();
    }
    
    // system info //////////////////////////////////////////////////////////////////
    public static File getUserHomeDirectory() {
        return new File(USER_HOME);
    }
    
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    
    public static boolean isCurrentUserAdmin() throws NativeException {
        return getNativeUtils().isCurrentUserAdmin();
    }
    
    public static File getCurrentDirectory() {
        return new File(".");
    }
    
    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
    
    public static File getDefaultApplicationsLocation() throws NativeException {
        return getNativeUtils().getDefaultApplicationsLocation();
    }
    
    public static File getCurrentJavaHome() {
        return new File(JAVA_HOME);
    }
    
    public static boolean isCurrentJava64Bit() {
        final String osArch = System.getProperty("os.arch");
        return "64".equals(System.getProperty("sun.arch.data.model")) ||
                "64".equals(System.getProperty("com.ibm.vm.bitmode")) || //IBM`s JDK
                osArch.equals("amd64") ||
                osArch.equals("sparcv9") ||
                osArch.equals("x86_64") ||
                osArch.equals("ppc64");
    }
    
    public static File getPacker() {
        if (isWindows()) {
            return new File(getCurrentJavaHome(), "bin/pack200.exe");
        } else {
            return new File(getCurrentJavaHome(), "bin/pack200");
        }
    }
    
    public static File getUnpacker() {
        if (isWindows()) {
            return new File(getCurrentJavaHome(), "bin/unpack200.exe");
        } else {
            return new File(getCurrentJavaHome(), "bin/unpack200");
        }
    }
    
    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }
    
    public static String getFileSeparator() {
        return FILE_SEPARATOR;
    }
    
    public static String getPathSeparator() {
        return PATH_SEPARATOR;
    }
    
    public static long getFreeSpace(File file) throws NativeException {
        //LogManager.log("[SystemUtils] getFreeSpace");
        //LogManager.indent();
        //LogManager.log(ErrorLevel.DEBUG,
        //        "... getting free space [requested path]  : " + file.getPath());
        File directory = file;
        while (directory!=null && (!directory.exists() || !directory.isDirectory())) {
            directory = directory.getParentFile();
        }
        //LogManager.log(ErrorLevel.DEBUG,
        //        "... getting free space [existing parent] : " + directory.getPath());
        long space = getNativeUtils().getFreeSpace(directory);
        //LogManager.unindent();
        //LogManager.log(ErrorLevel.DEBUG, "... free space is : " + space);
        return space;
    }
    
    public static ExecutionResults executeCommand(String... command) throws IOException {
        return executeCommand(null, command);
    }
    
    public static ExecutionResults executeCommand(File workingDirectory, String... command) throws IOException {
        // construct the initial log message
        String commandString = StringUtils.asString(command, StringUtils.SPACE);
        
        if (workingDirectory == null) {
            workingDirectory = getCurrentDirectory();
        }
        
        LogManager.log(ErrorLevel.MESSAGE,
                "executing command: " + commandString +
                ", in directory: " + workingDirectory);
        LogManager.indent();
        
        StringBuilder processStdOut = new StringBuilder();
        StringBuilder processStdErr = new StringBuilder();
        int           errorLevel = ExecutionResults.TIMEOUT_ERRORCODE;
        
        ProcessBuilder builder= new ProcessBuilder(command).directory(workingDirectory);
        
        builder.environment().clear();
        builder.environment().putAll(environment);
        setDefaultEnvironment();
        
        Process process = builder.start();
        
        long startTime = System.currentTimeMillis();
        long endTime   = startTime + MAX_EXECUTION_TIME;
        boolean doRun = true;
        long delay = INITIAL_DELAY;
        while (doRun && (System.currentTimeMillis() < endTime)) {
            try {
                Thread.sleep(delay);
                if(delay < MAX_DELAY) {
                    delay += DELTA_DELAY;
                }
            }  catch (InterruptedException e) {
                ErrorManager.notifyDebug("Interrupted", e);
            }
            try {
                errorLevel = process.exitValue();
                doRun = false;
            } catch (IllegalThreadStateException e) {
                ; // do nothing - the process is still running
            }
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
                    LogManager.log(ErrorLevel.MESSAGE, "[stderr]: " + line);
                }
                
                processStdErr.append(string);
            }
        }
        
        LogManager.log(ErrorLevel.MESSAGE, 
                (doRun) ? 
                    "[return]: killed by timeout" : 
                    "[return]: " + errorLevel);
        process.destroy();        
        LogManager.unindent();
        LogManager.log(ErrorLevel.MESSAGE, "... command execution finished");
        
        return new ExecutionResults(errorLevel, processStdOut.toString(), processStdErr.toString());
    }
    
    public static boolean isPathValid(String path) {
        return getNativeUtils().isPathValid(path);
    }
    
    public static boolean isPortAvailable(int port, int... forbiddenPorts) {
        return NetworkUtils.isPortAvailable(port, forbiddenPorts);       
    }
    
    public static int getAvailablePort(int basePort, int... forbiddenPorts) {
        return NetworkUtils.getAvailablePort(basePort, forbiddenPorts);        
    }
    
    public static boolean isDeletingAllowed(File file) {
        return getNativeUtils().isDeletingAllowed(file);
    }
    
    @Deprecated
    private static LocationType toLocationType(ShortcutLocationType type) {
        LocationType tp = null;
        switch(type) {
            case CURRENT_USER_DESKTOP :
                tp = LocationType.CURRENT_USER_DESKTOP;
                break;
            case CURRENT_USER_START_MENU :
                tp = LocationType.CURRENT_USER_START_MENU;
                break;
            case ALL_USERS_DESKTOP :
                tp = LocationType.ALL_USERS_DESKTOP;
                break;
            case ALL_USERS_START_MENU :
                tp = LocationType.ALL_USERS_START_MENU;
                break;
        }
        return tp;
    }
    
    @Deprecated
    public static File getShortcutLocation(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().getShortcutLocation((FileShortcut)shortcut, toLocationType(locationType));
    }
    
    @Deprecated
    public static File createShortcut(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType) throws NativeException {
        return getNativeUtils().createShortcut((FileShortcut)shortcut, toLocationType(locationType));
    }
    
    @Deprecated
    public static void removeShortcut(org.netbeans.installer.utils.helper.Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws NativeException {
        getNativeUtils().removeShortcut((FileShortcut)shortcut, toLocationType(locationType), deleteEmptyParents);
    }
    
    public static File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException {
        return getNativeUtils().getShortcutLocation(shortcut, locationType);
    }
    
    /**
     * Create shortcut at the specified location that is set using <code>locationType</code>.
     * <br>For the current moment the following logic is implemented:
     * <ul>
     * <li> For Windows FileShortcut is created as an <i>.lnk</i> file.<br>
     *      InternetShortcut is created as a standard <i>.url</i> file.<br></li>
     * <li> For Linux/Solaris FileShortcut is created as a <i>.desktop</i> entry with
     *      type <b>Application</b> if the target is normal file.<br>
     *      If the file is actually a directory then a symlink is created <br>
     *      InternetShortcut is created as a <i>.desktop</i> entry with type
     *      <b>Link</b>.</li>
     * <li> For MacOS FileShortcut on desktop is created as a symlink
     *      (with, possibly, moving up-parents to the first .app).<br>
     *      InternetShortcut on desktop is created as a standard <i>.url</i> file.<br>
     *      "Start Menu" file shortcuts for MacOS are created at Dock.<br>
     *      InternetShortcut creation in Dock actually does nothing since it
     *      seems that there is no way add an internet shortcut ot the Dock
     *      at all.</li>
     * </ul>
     *
     *
     */
    public static File createShortcut(
            final Shortcut shortcut,
            final LocationType locationType) throws NativeException {
        return getNativeUtils().createShortcut(shortcut, locationType);
    }
    
    public static void removeShortcut(Shortcut shortcut, LocationType locationType, boolean deleteEmptyParents) throws NativeException {
        getNativeUtils().removeShortcut(shortcut, locationType, deleteEmptyParents);
    }
    
    public static FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        return getNativeUtils().addComponentToSystemInstallManager(descriptor);
    }
    
    public static void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        getNativeUtils().removeComponentFromSystemInstallManager(descriptor);
    }
    
    public static String getEnvironmentVariable(String name) throws NativeException {
        return getEnvironmentVariable(name, EnvironmentScope.PROCESS, true);
    }
    
    public static String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException {
        return getNativeUtils().getEnvironmentVariable(name, scope, expand);
    }
    
    public static void setEnvironmentVariable(String name, String value) throws NativeException {
        setEnvironmentVariable(name, value, EnvironmentScope.PROCESS, true);
    }
    
    public static void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException {
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
    
    public static void setPermissions(final File file, final int mode, final int change) throws IOException {
        getNativeUtils().setPermissions(file, mode, change);
    }
    
    public static int getPermissions(final File file) throws IOException {
        return getNativeUtils().getPermissions(file);
    }
    
    public static Launcher createLauncher(LauncherProperties props, Progress progress) throws IOException {
        return createLauncher(props, getCurrentPlatform(), progress);
    }
    
    public static Launcher createLauncher(LauncherProperties props, Platform platform, Progress progress) throws IOException {
        Progress prg = (progress == null) ? new Progress() : progress;
        LogManager.log("Create native launcher for " + platform.toString());
        Launcher launcher  =null;
        try {
            LogManager.indent();
            launcher = LauncherFactory.newLauncher(props, platform);
            long start = System.currentTimeMillis();
            launcher.initialize();
            launcher.create(prg);
            long seconds = System.currentTimeMillis() - start ;
            LogManager.unindent();
            LogManager.log("[launcher] Time : " + (seconds/1000) + "."+ (seconds%1000)+ " seconds");
        } catch (IOException e) {
            LogManager.unindent();
            LogManager.log("[launcher] Build failed with the following exception :");
            LogManager.log(e);
            throw e;
        }
        return launcher;
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }  catch (InterruptedException e) {
            ErrorManager.notify(ErrorLevel.DEBUG,
                    "Interrupted while sleeping", e);
        }
    }
    
    public static void setDefaultEnvironment() {
        environment = new ProcessBuilder().environment();
    }
    
    public static Map<String, String> getEnvironment() {
        return environment;
    }
    
    public static Platform getCurrentPlatform() {
        return getNativeUtils().getCurrentPlatform();        
    }
    
    public static String getHostName() {
        return NetworkUtils.getHostName();        
    }
    
    public static List<File> getFileSystemRoots() throws IOException {
        return getNativeUtils().getFileSystemRoots();
    }
    
    // platforms probes /////////////////////////////////////////////////////////////
    public static boolean isWindows() {
        return getCurrentPlatform().isCompatibleWith(Platform.WINDOWS);
    }
    
    public static boolean isMacOS() {
        return getCurrentPlatform().isCompatibleWith(Platform.MACOSX);
    }
    
    public static boolean isLinux() {
        return getCurrentPlatform().isCompatibleWith(Platform.LINUX);
    }
    
    public static boolean isSolaris() {
        return getCurrentPlatform().isCompatibleWith(Platform.SOLARIS);
    }
    public static boolean isUnix() {
        return getCurrentPlatform().isCompatibleWith(Platform.UNIX);
    }
    // miscellanea //////////////////////////////////////////////////////////////////
    public static boolean intersects(
            final List<? extends Object> list1,
            final List<? extends Object> list2) {
        for (int i = 0; i < list1.size(); i++) {
            for (int j = 0; j < list2.size(); j++) {
                if (list1.get(i).equals(list2.get(j))) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static <T> List<T> intersect(
            final List<? extends T> list1,
            final List<? extends T> list2) {
        final List<T> intersection = new LinkedList<T>();
        
        for (T item: list1) {
            if (list2.contains(item)) {
                intersection.add(item);
            }
        }
        
        return intersection;
    }
    
    public static <T> List<T> substract(
            final List<? extends T> list1,
            final List<? extends T> list2) {
        final List<T> result = new LinkedList<T>();
        
        for (T item1: list1) {
            boolean found = false;
            
            for (T item2: list2) {
                if (item1.equals(item2)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                result.add(item1);
            }
        }
        
        return result;
    }
    
    // native accessor //////////////////////////////////////////////////////////////
    public static synchronized NativeUtils getNativeUtils() {
        if (nativeUtils == null) {
            nativeUtils = NativeUtilsFactory.newNativeUtils();
        }
        return nativeUtils;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final long MAX_EXECUTION_TIME = 600000;
    
    public static final int MAX_DELAY = 50; // NOMAGI
    public static final int INITIAL_DELAY = 5; // NOMAGI
    public static final int DELTA_DELAY = 5; // NOMAGI
    public static final String LINE_SEPARATOR = 
            System.getProperty("line.separator");//NOI18N
    public static final String FILE_SEPARATOR = 
            System.getProperty("file.separator");//NOI18N
    public static final String PATH_SEPARATOR = 
            System.getProperty("path.separator");//NOI18N
    public static final String JAVA_HOME = 
            System.getProperty("java.home");//NOI18N
    public static final String USER_HOME = 
            System.getProperty("user.home");//NOI18N
    public static final String NO_SPACE_CHECK_PROPERTY = 
            "no.space.check";//NOI18N      
}
