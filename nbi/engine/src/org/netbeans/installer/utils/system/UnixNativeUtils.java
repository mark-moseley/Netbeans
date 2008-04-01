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

package org.netbeans.installer.utils.system;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.InternetShortcut;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.Pair;
import org.netbeans.installer.utils.system.cleaner.ProcessOnExitCleanerHandler;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.shortcut.LocationType;
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
    private boolean checkQuota = true;
    private File quotaExecutable = null;
    
    private static final String[] FORBIDDEN_DELETING_FILES_UNIX = {
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
        "/var",
    };
    
    private static final String CLEANER_RESOURCE =
            NATIVE_CLEANER_RESOURCE_SUFFIX + "unix/cleaner.sh"; // NOI18N
    
    private static final String CLEANER_FILENAME =
            "nbi-cleaner.sh"; // NOI18N
    
    public static final String XDG_DATA_HOME_ENV_VARIABLE =
            "XDG_DATA_HOME"; // NOI18N
    
    public static final String XDG_DATA_DIRS_ENV_VARIABLE =
            "XDG_DATA_DIRS"; // NOI18N
    
    public static final String DEFAULT_XDG_DATA_HOME =
            ".local/share"; // NOI18N
    
    public static final String DEFAULT_XDG_DATA_DIRS =
            "/usr/share"; // NOI18N
    
    public boolean isCurrentUserAdmin() throws NativeException{
        if(isUserAdminSet) {
            return isUserAdmin;
        }
        boolean result = isCurrentUserAdmin0();
        isUserAdmin = result;
        isUserAdminSet = true;
        return result;
    }
    
    @Override
    protected OnExitCleanerHandler newDeleteOnExitCleanerHandler() {
        return new UnixProcessOnExitCleanerHandler(CLEANER_FILENAME);
    }
    
    public void updateApplicationsMenu() {
        try {
            SystemUtils.executeCommand(null,new String [] {
                "pkill", "-u", SystemUtils.getUserName(), "panel"});
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    
    public File getShortcutLocation(
            final Shortcut shortcut,
            final LocationType locationType) throws NativeException {
        LogManager.logIndent(
                "devising the shortcut location by type: " + locationType); // NOI18N
        
        final String XDG_DATA_HOME =
                SystemUtils.getEnvironmentVariable(XDG_DATA_HOME_ENV_VARIABLE);
        final String XDG_DATA_DIRS =
                SystemUtils.getEnvironmentVariable(XDG_DATA_DIRS_ENV_VARIABLE);
        
        final File currentUserLocation;
        if (XDG_DATA_HOME == null) {
            currentUserLocation = new File(
                    SystemUtils.getUserHomeDirectory(),
                    DEFAULT_XDG_DATA_HOME);
        } else {
            currentUserLocation = new File(
                    XDG_DATA_HOME);
        }
        
        final File allUsersLocation;
        if (XDG_DATA_DIRS == null) {
            allUsersLocation = new File(DEFAULT_XDG_DATA_DIRS);
        } else {
            // Workaround for Issue 131194 : 
            // Cannot install nb 6.1beta in ubuntu with xubuntu session
            // http://www.netbeans.org/issues/show_bug.cgi?id=131194
            String firstPath = XDG_DATA_DIRS.split(SystemUtils.getPathSeparator())[0];
            if(firstPath.startsWith("etc/xdg/")) {
                firstPath = File.separator + firstPath;
            }
            allUsersLocation = new File(firstPath);
        }
        
        LogManager.log(
                "XDG_DATA_HOME = " + currentUserLocation); // NOI18N
        LogManager.log(
                "XDG_DATA_DIRS = " + allUsersLocation); // NOI18N
        
        String fileName = shortcut.getFileName();
        if (fileName == null) {
            if (shortcut instanceof FileShortcut) {
                final File target = ((FileShortcut) shortcut).getTarget();
                
                fileName = target.getName();
                if(!target.isDirectory()) {
                    fileName += ".desktop";
                }
            } else if(shortcut instanceof InternetShortcut) {
                fileName = ((InternetShortcut) shortcut).getURL().getFile() +
                        ".desktop";
            }
        }
        
        LogManager.log(""); // NOI18N
        
        final File shortcutFile;
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
            case ALL_USERS_DESKTOP:
                shortcutFile = new File(
                        SystemUtils.getUserHomeDirectory(),
                        "Desktop/" + fileName);
                break;
            case CURRENT_USER_START_MENU:
                shortcutFile = new File(
                        currentUserLocation,
                        "applications/" + fileName);
                break;
            case ALL_USERS_START_MENU:
                shortcutFile = new File(
                        allUsersLocation,
                        "applications/" + fileName);
                break;
            default:
                shortcutFile = null;
        }
        
        LogManager.logUnindent(
                "shortcut file: " + shortcutFile); // NOI18N
        
        return shortcutFile;
    }
    
    private List <String> getDesktopEntry(FileShortcut shortcut) {
        final List <String> list = new ArrayList<String> ();
        
        list.add("[Desktop Entry]");
        list.add("Encoding=UTF-8");
        list.add("Name=" + shortcut.getName());
        list.add("Exec=/bin/sh \"" + shortcut.getTarget() + "\"" + 
                ((shortcut.getArguments()!=null && shortcut.getArguments().size()!=0) ? 
                    StringUtils.SPACE + shortcut.getArgumentsString() : StringUtils.EMPTY_STRING)
                    );
       
        if(shortcut.getIcon()!=null) {
            list.add("Icon=" + shortcut.getIconPath());
        }
        if(shortcut.getCategories().length != 0) {
            list.add("Categories=" +
                    StringUtils.asString(shortcut.getCategories(),";"));
        }
        
        list.add("Version=1.0");
        list.add("StartupNotify=true");
        list.add("Type=Application");
        list.add("Terminal=0");
        list.add(SystemUtils.getLineSeparator());
        return list;
    }
    
    protected List <String> getDesktopEntry(InternetShortcut shortcut) {
        final List <String> list = new ArrayList<String> ();
        list.add("[Desktop Entry]");
        list.add("Encoding=UTF-8");
        list.add("Name=" + shortcut.getName());
        list.add("URL=" + shortcut.getURL());
        if(shortcut.getIcon()!=null) {
            list.add("Icon=" + shortcut.getIconPath());
        }
        if(shortcut.getCategories().length != 0) {
            list.add("Categories=" +
                    StringUtils.asString(shortcut.getCategories(),";"));
        }
        list.add("Version=1.0");
        list.add("StartupNotify=true");
        list.add("Type=Link");
        list.add(SystemUtils.getLineSeparator());
        return list;
    }
    
    
    public File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException {
        final File          file     = getShortcutLocation(shortcut, locationType);
        try {
            if(shortcut instanceof FileShortcut) {
                File target = ((FileShortcut)shortcut).getTarget();
                if(target.isDirectory()) {
                    createSymLink(file, target);
                } else {
                    FileUtils.writeStringList(file,
                            getDesktopEntry((FileShortcut)shortcut));
                }
            } else if(shortcut instanceof InternetShortcut) {
                FileUtils.writeStringList(file,
                        getDesktopEntry((InternetShortcut)shortcut));
            }
        } catch (IOException e) {
            throw new NativeException("Cannot create shortcut", e);
        }
        
        return file;
    }
    
    public void removeShortcut(Shortcut shortcut, LocationType locationType, boolean cleanupParents) throws NativeException {
        try {
            File shortcutFile = getShortcutLocation(shortcut, locationType);
            
            FileUtils.deleteFile(shortcutFile);
            
            if(cleanupParents &&
                    (locationType == LocationType.ALL_USERS_START_MENU ||
                    locationType == LocationType.CURRENT_USER_START_MENU)) {
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
                // is it an ELF file?
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(child));
                byte[] buf = new byte[4];
                bis.read(buf);
                bis.close();
                if (Arrays.equals(buf, ELF_BYTES)){
                    files.add(child);
                    return files;
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
    
    public void setPermissions(File file, int mode, int change) throws IOException {
        LogManager.log("setting permissions " + Integer.toString(mode, 8) + " on " + file);
        
        setPermissions0(file.getAbsolutePath(), mode, change);
    }
    
    public int getPermissions(File file) throws IOException {
        return getPermissions0(file.getAbsolutePath());
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
            long freeSpace = getFreeSpace0(file.getPath());
            if(checkQuota) {
                // #123587 Disk space check should take into account user quota
                try {
                    LogManager.indent();                    
                    long freeSpaceQuota = getFreeSpaceUsingQuota(file);
                    if(freeSpaceQuota!=-1L) {
                        LogManager.log("... free space (due to the quote) is " + freeSpaceQuota + ", physical is : " + freeSpace);                        
                        freeSpace = freeSpaceQuota;                        
                    }
                } catch (IOException e) {
                    LogManager.log("... quota check is disabled");
                    checkQuota = false;
                } finally {
                    LogManager.unindent();
                }
            }
            return freeSpace;
        }
    }
    
    private long getFreeSpaceUsingQuota(File file) throws IOException {
        String path = file.getAbsolutePath();
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            LogManager.log(e);
        }

        LogManager.log("Checking free space with quota in " + path);
        try {
            setEnvironmentVariable("LANG", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable("LC_TIME", "C", EnvironmentScope.PROCESS, false);
        } catch (NativeException e) {
            LogManager.log(e);
        }
        if (quotaExecutable == null) {
            for (String q : QUOTA_LOCATIONS) {
                final File f = new File(q);
                if (FileUtils.exists(f)) {
                    quotaExecutable = f;
                    break;
                }
            }
            if (quotaExecutable == null) {
                LogManager.log("... no quota executable found");
                throw new IOException();
            }
        }
        final List<String> stdoutList = new ArrayList<String>();

        Thread quotaThread = null;
        try {
            quotaThread = new Thread() {
                @Override
                public void run() {
                    try {
                        LogManager.log("... running command : " + quotaExecutable.getPath() + " -v");
                        Process p = new ProcessBuilder(quotaExecutable.getPath(), "-v").start();
                        final InputStream is = p.getInputStream();
                        final InputStream err = p.getErrorStream();
                        p.waitFor();
                        final String output = StringUtils.readStream(is);
                        final String error = StringUtils.readStream(err);
                        LogManager.log("... stdout:");
                        LogManager.log(output);
                        LogManager.log("... stderr:");
                        LogManager.log(error);
                        LogManager.log("... return : " + p.exitValue());
                        stdoutList.add(output);
                        is.close();
                        err.close();
                    } catch (IOException e) {
                        LogManager.log("... error occured when running quota executable", e);
                    } catch (InterruptedException e) {
                        LogManager.log("... interrupted");
                    }
                }
            };

            quotaThread.start();
            quotaThread.join(QUOTA_TIMEOUT_MILLIS);
            if (quotaThread.isAlive()) {
                LogManager.log("... quota command is hanging more than 5 seconds so killing it");
                quotaThread.interrupt();
                LogManager.log("... killed");
            }
        } catch (InterruptedException ie) {
            LogManager.log("... interrupted", ie);
            quotaThread.interrupt();
        }
        if(stdoutList.size()==0) {
            LogManager.log("... quota produced no stdout for analysis");
            throw new IOException();
        }

        final String stdout = stdoutList.get(0);
        final String[] lines = StringUtils.splitByLines(stdout);
        if (lines.length <= 2) {
            LogManager.log("... no quota set for the user (number of lines in output less that 3)");
            throw new IOException();
        }
        // Usual format is the following
        // Disk quotas for <userid> (<uid>):
        // Filesystem  usage  quota  limit  timeleft  files  quota  limit   timeleft
        // /home/<userid> 943880  0 1577704           18992    0      0   
        // /home/<userid2> 943880  0 1577704      1    18992    0      0     1
        List<Pair<String, Long>> pathSpace = new ArrayList<Pair<String, Long>>();

        try {
            for (int i = 2; i < lines.length; i++) {
                String s = lines[i].trim();
                if (s.startsWith(File.separator) && s.indexOf(StringUtils.SPACE) != -1) {
                    String quotedPath = s.substring(0, s.indexOf(StringUtils.SPACE));
                    String[] numbers = s.substring(s.indexOf(StringUtils.SPACE) + 1).
                            trim().split("[ |\t]+");
                    if (numbers.length < 6) {
                        LogManager.log("...cannot parse the quota numbers [" + numbers.length + "]");
                        throw new IOException();
                    }

                    final long limit = new Long(numbers[2]).longValue();
                    final long usage = new Long(numbers[0]).longValue();
                    final long freespace = (limit - usage) * 1024;

                    if (limit > 0 && freespace >= 0) {
                        pathSpace.add(new Pair<String, Long>(quotedPath, freespace));
                    }
                }
            }
            if (pathSpace.size() == 0) {
                LogManager.log("... no quota set for the user (no paths in quota output)");
                throw new IOException();
            }
            String longestPath = StringUtils.EMPTY_STRING;
            long freespace = -1L;
            for (Pair<String, Long> p : pathSpace) {
                final String s = p.getFirst();
                if (s.length() > longestPath.length() && path.startsWith(s)) {
                    longestPath = s;
                    freespace = p.getSecond().longValue();
                }
            }
            return freespace;
        } catch (NumberFormatException e) {
            LogManager.log("...cannot parse the quota numbers", e);
            throw new IOException();
        } catch (PatternSyntaxException e) {
            LogManager.log("...cannot parse the quota numbers", e);
            throw new IOException();
        }
    }
    
    public boolean isUNCPath(String path) {
        // for Unix UNC is smth like servername:/folder...
        return path.matches("^.+:/.+");
    }
    
    // other ... //////////////////////////
    
    public String getEnvironmentVariable(String name, EnvironmentScope scope, boolean flag) {
        return System.getenv(name);
    }
    
    public void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean flag) throws NativeException {
        if (EnvironmentScope.PROCESS == scope) {
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
        File opt = new File("/opt");
        
        if (opt.exists() && opt.isDirectory() && FileUtils.canWrite(opt)) {
            return opt;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }
    
    public boolean isPathValid(String path) {
        return true;
    }
    
    public FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException {
        final FilesList list = new FilesList();
        
        if (descriptor.getModifyCommand() != null) {
            try {
                final Launcher launcher = createUninstaller(descriptor, false, new Progress());
                correctFilesPermissions(launcher.getOutputFile());
                list.add(launcher.getOutputFile());
            } catch (IOException e) {
                throw new NativeException("Can't create uninstaller", e);
            }
        }
        
        if (descriptor.getUninstallCommand() != null) {
            try {
                final Launcher launcher = createUninstaller(descriptor, true, new Progress());
                correctFilesPermissions(launcher.getOutputFile());
                list.add(launcher.getOutputFile());
            } catch (IOException e) {
                throw new NativeException("Can't create uninstaller", e);
            }
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
    
    public List<File> getFileSystemRoots() throws IOException {
        try {
            setEnvironmentVariable(
                    "LANG", "C", EnvironmentScope.PROCESS, false);
            
            setEnvironmentVariable(
                    "LC_COLLATE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_CTYPE", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MESSAGES", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_MONETARY", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_NUMERIC", "C", EnvironmentScope.PROCESS, false);
            setEnvironmentVariable(
                    "LC_TIME", "C", EnvironmentScope.PROCESS, false);
            
            final String stdout = SystemUtils.executeCommand("df", "-h").getStdOut();
            final String[] lines = StringUtils.splitByLines(stdout);
            
            // a quick and dirty solution - we assume that % is present only once in
            // each line - in the part where the percentage is reported, hence we
            // look for the percentage sign and then for the first slash
            final List<File> roots = new LinkedList<File>();
            for (int i = 1; i < lines.length; i++) {
                int index = lines[i].indexOf("%");
                if (index != -1) {
                    index = lines[i].indexOf("/", index);
                    
                    if (index != -1) {
                        final String path = lines[i].substring(index);
                        final File file = new File(path);
                        
                        if (!roots.contains(file)) {
                            roots.add(file);
                        }
                    }
                }
            }
            
            return roots;
        } catch (NativeException e) {
            final IOException ioException =
                    new IOException("Cannot define the environment");
            
            throw (IOException) ioException.initCause(e);
        }
    }
    
    // native declarations //////////////////////////////////////////////////////////
    private native long getFreeSpace0(String s);
    
    private native void setPermissions0(String path, int mode, int change);
    
    private native int getPermissions0(String path);
    
    private native boolean isCurrentUserAdmin0();
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class UnixProcessOnExitCleanerHandler extends ProcessOnExitCleanerHandler {
        public UnixProcessOnExitCleanerHandler(String cleanerFileName) {
            super(cleanerFileName);
        }
        protected void writeCleaner(File cleanerFile) throws IOException {
            InputStream is = ResourceUtils.getResource(CLEANER_RESOURCE);
            CharSequence cs = StreamUtils.readStream(is);
            is.close();
            String [] lines = StringUtils.splitByLines(cs);
            FileUtils.writeFile(cleanerFile, StringUtils.asString(lines, SystemUtils.getLineSeparator()));
        }
        
        protected void writeCleaningFileList(File listFile, List<String> files) throws IOException {
            // be sure that the list file contains end-of-line
            // otherwise the installer will run into Issue 104079
            List<String> newList = new LinkedList<String> (files);
            newList.add(SystemUtils.getLineSeparator());
            FileUtils.writeStringList(listFile, newList);
        }
    }
    
    public static class FileAccessMode {
        /** Read by user */
        public static final int RU = 0400;
        /** Write by user */
        public static final int WU = 0200;
        /** Execute by user */
        public static final int EU = 0100;
        
        /** Read by group */
        public static final int RG = 040;
        /** Write by group */
        public static final int WG = 020;
        /** Execute by group */
        public static final int EG = 010;
        
        /** Read by others */
        public static final int RO = 04;
        /** Write by others */
        public static final int WO = 02;
        /** Execute by others */
        public static final int EO = 01;
    }
    
    @Override
    protected void initializeForbiddenFiles(String ... files) {
        super.initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_UNIX);
        super.initializeForbiddenFiles(files);
    }
    private static final String [] QUOTA_LOCATIONS = {
      "/usr/sbin/quota", //NOI18N
      "/usr/bin/quota",  //NOI18N    
      "/sbin/quota",     //NOI18N
      "/bin/quota",      //NOI18N
    };
    private static final byte [] ELF_BYTES = new byte[]{'\177','E','L','F'};
    private static final long QUOTA_TIMEOUT_MILLIS = 5000;//NOMAGI
}
