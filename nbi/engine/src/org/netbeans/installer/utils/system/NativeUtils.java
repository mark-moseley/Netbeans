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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.EngineUtils;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.cleaner.JavaOnExitCleanerHandler;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.shortcut.LocationType;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static    
    protected static boolean nativeLibraryLoaded;
    private static HashSet<File> forbiddenDeletingFiles = new HashSet<File>();
    private static List <LauncherResource> uninstallerJVMs = new ArrayList <LauncherResource> ();
    private static Platform currentPlatform;    
    public final static String NATIVE_RESOURCE_SUFFIX = "native/"; // NOI18N
    public final static String NATIVE_JNILIB_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "jnilib/"; // NOI18N
    public final static String NATIVE_LAUNCHER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "launcher/"; // NOI18N
    public final static String NATIVE_CLEANER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "cleaner/"; // NOI18N
    private static OnExitCleanerHandler cleanerHandler;

    protected abstract Platform getPlatform();
    
    final public Platform getCurrentPlatform() {
        if (currentPlatform == null) {
            currentPlatform = getPlatform();
        }        
        return currentPlatform;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////////
    protected NativeUtils() {
        // does nothing
    }
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract boolean isCurrentUserAdmin() throws NativeException;
    
    public abstract File getDefaultApplicationsLocation() throws NativeException;
    
    public abstract long getFreeSpace(File file) throws NativeException;
    
    public abstract boolean isPathValid(String path);
    
    public abstract boolean isUNCPath(String path);
    
    public File getRoot(final File file) {
        File parent = file;
        while (parent.getParentFile() != null) {
            parent = parent.getParentFile();
        }
        
        return parent;
    }
    
    public abstract File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract void removeShortcut(Shortcut shortcut, LocationType locationType, boolean deleteEmptyParents) throws NativeException;
    
    public void addUninstallerJVM(LauncherResource jvm) {
        if(jvm!=null && !uninstallerJVMs.contains(jvm)) {
        uninstallerJVMs.add(jvm);
        }
    }
    protected Launcher createUninstaller(ApplicationDescriptor descriptor, boolean uninstall, Progress progress) throws IOException {
        LogManager.log("creating uninstaller...");
        
        final File engine = new File(descriptor.getInstallPath(),
                "uninstall.jar");
        try {
            EngineUtils.cacheEngine(engine, new Progress());
            
            final LauncherProperties props = new LauncherProperties();
            
            props.addJVM(new LauncherResource(false, SystemUtils.getCurrentJavaHome()));
            for(LauncherResource jvm : uninstallerJVMs) {
                props.addJVM(jvm);
            }
            props.addJar(new LauncherResource(true, engine));
            props.setJvmArguments(new String[]{
                "-Xmx256m",
                "-Xms64m",
                "-D" + Installer.LOCAL_DIRECTORY_PATH_PROPERTY +
                        "=" + Installer.getInstance().getLocalDirectory()});
            props.setMainClass(EngineUtils.getEngineMainClass().getName());
            
            if (uninstall) {
                props.setAppArguments(descriptor.getUninstallCommand());
                props.setOutput(
                        new File(descriptor.getInstallPath(), "uninstall"),
                        true);
            } else {
                props.setAppArguments(descriptor.getModifyCommand());
                props.setOutput(
                        new File(descriptor.getInstallPath(), "modify-install"),
                        true);
            }
            return SystemUtils.createLauncher(props, progress);
        } finally {
            FileUtils.deleteFile(engine);
        }
    }
    
    public abstract FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract List<File> findIrrelevantFiles(File parent) throws IOException;
    
    public abstract List<File> findExecutableFiles(File parent) throws IOException;
    
    public abstract void correctFilesPermissions(File parent) throws IOException;
    
    public abstract void setPermissions(
            final File file,
            final int mode,
            final int change) throws IOException;
    
    public abstract int getPermissions(
            final File file) throws IOException;
    
    public boolean checkFileAccess(File file, boolean isReadNotModify) throws NativeException {
        return true;
    }
    public final void addDeleteOnExitFile(File file) {        
        getDeleteOnExitHandler().addDeleteOnExitFile(file);
    }
    
    public final void removeDeleteOnExitFile(File file) {
        getDeleteOnExitHandler().removeDeleteOnExitFile(file);
    }
    protected OnExitCleanerHandler newDeleteOnExitCleanerHandler() {
        return new JavaOnExitCleanerHandler();
    }
    protected OnExitCleanerHandler getDeleteOnExitHandler() {
        if(cleanerHandler == null) {
            cleanerHandler = newDeleteOnExitCleanerHandler();
            Runtime.getRuntime().addShutdownHook(cleanerHandler);
        }   
        return cleanerHandler;
    }
    
    
    public abstract List<File> getFileSystemRoots() throws IOException;
    
    // protected ////////////////////////////////////////////////////////////////////
    protected void loadNativeLibrary(String path) {
        LogManager.logIndent("loading jni library");
        LogManager.log("library resource path: " + path);
        
        if (path != null) {
            InputStream input = null;
            
            try {
                final File file = FileUtils.createTempFile();
                
                LogManager.log("library file path: " + file.getAbsolutePath());
                
                input = getClass().getClassLoader().getResourceAsStream(path);
                FileUtils.writeFile(file, input);
                
                System.load(file.getAbsolutePath());
                nativeLibraryLoaded = true;
                addDeleteOnExitFile(file);
            } catch (IOException e) {
                ErrorManager.notifyCritical(
                        "Cannot load native library from path: " + path, e);
            } catch (UnsatisfiedLinkError e) {
                ErrorManager.notifyCritical(
                        "Cannot load native library from path: " + path, e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ErrorManager.notifyDebug("Failed to close the input " +
                                "stream to the cached jni library", e);
                    }
                }
            }
        }
        
        LogManager.logUnindent("... successfully loaded the library");
    }
    
    protected void initializeForbiddenFiles(String ... filepaths) {
        for (String path : filepaths) {
            if(path!=null) {
                File file = new File(path);
                if(file.exists() && !forbiddenDeletingFiles.contains(file)) {
                    forbiddenDeletingFiles.add(file);
                }
            }
        }
    }
    
    public boolean isDeletingAllowed(File file) {
        return !(forbiddenDeletingFiles.contains(file));
    }
    
    public final  static int FA_MODE_SET = 1;
    public final  static int FA_MODE_ADD = 2;
    public final  static int FA_MODE_REMOVE = 4;
}
