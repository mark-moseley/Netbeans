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
import java.util.List;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils.EnvironmentScope;
import org.netbeans.installer.utils.SystemUtils.Platform;
import org.netbeans.installer.utils.SystemUtils.Shortcut;
import org.netbeans.installer.utils.SystemUtils.ShortcutLocationType;
import org.netbeans.installer.utils.exceptions.NativeException;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static NativeUtils instance;
    
    public static synchronized NativeUtils getInstance() {
        switch (Platform.getCurrentPlatform()) {
            case WINDOWS:
                instance = new WindowsNativeUtils();
                break;
            case LINUX:
            case SOLARIS_X86:
            case SOLARIS_SPARC:
                instance = new UnixNativeUtils();
                break;
            case MACOS_X_PPC:
            case MACOS_X_X86:
                instance = new MacOsNativeUtils();
                break;
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract boolean isCurrentUserAdmin() throws NativeException;
    
    public abstract File getDefaultApplicationsLocation() throws NativeException;
    
    public abstract long getFreeSpace(File file) throws NativeException;
    
    public abstract boolean isPathValid(String path);
    
    public abstract File getShortcutLocation(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException;
    
    public abstract File createShortcut(Shortcut shortcut, ShortcutLocationType locationType) throws NativeException;
    
    public abstract void removeShortcut(Shortcut shortcut, ShortcutLocationType locationType, boolean deleteEmptyParents) throws NativeException;
    
    public abstract void addComponentToSystemInstallManager(ProductComponent comp) throws NativeException;
    
    public abstract void removeComponentFromSystemInstallManager(ProductComponent comp) throws NativeException;
    
    public abstract String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract List<File> findIrrelevantFiles(File parent) throws IOException;
    
    public abstract List<File> findExecutableFiles(File parent) throws IOException;
    
    public abstract void correctFilesPermissions(File parent) throws IOException;
    
    // protected ////////////////////////////////////////////////////////////////////
    protected NativeUtils() {
        // does nothing
    }
    
    protected void loadNativeLibrary(String libraryPath) {
        if (libraryPath != null) {
            InputStream input = null;
            
            try {
                File file = FileUtils.createTempFile();
                
                input = getClass().getClassLoader().getResourceAsStream(libraryPath);
                
                FileUtils.writeFile(file, input);
                
                System.load(file.getAbsolutePath());
                scheduleCleanup(file.getAbsolutePath());
            } catch (IOException e) {
                ErrorManager.notify(ErrorLevel.CRITICAL, "Cannot load native library from path: " + libraryPath, e);
            } catch (UnsatisfiedLinkError e) {
                ErrorManager.notify(ErrorLevel.CRITICAL, "Cannot load native library from path: " + libraryPath, e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, e);
                    }
                }
            }
        }
    }
    
    protected abstract void scheduleCleanup(String libraryPath);
}
