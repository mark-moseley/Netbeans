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
package org.netbeans.installer.utils.applications;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKCU;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.SEPARATOR;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 * @author Kirill Sorokin
 */
public class JavaUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String JDK_KEY = "SOFTWARE\\JavaSoft\\Java Development Kit";
    
    public static final String JAVAHOME_VALUE        = "JavaHome";
    public static final String MICROVERSION_VALUE    = "MicroVersion";
    public static final String CURRENT_VERSION_VALUE = "CurrentVersion";
    
    public static final String TEST_JDK_RESOURCE =
            "org/netbeans/installer/utils/applications/TestJDK.class";
    
    public static final String TEST_JDK_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + TEST_JDK_RESOURCE;
    
    public static final String TEST_JDK_CLASSNAME = "TestJDK";
    public static final String TEST_JDK_FILENAME  = "TestJDK.class";
    public static final int TEST_JDK_OUTPUT_PARAMETERS = 5; // java.version, java.vm.version, java.vendor, os.name, os.arch
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Map<File, JavaInfo> knownJdks = new HashMap<File, JavaInfo>();
    
    public static boolean isJavaHome(File javaHome) {
        if (!javaHome.exists() || !javaHome.isDirectory()) {
            return false;
        }
        
        File probe = getExecutable(javaHome);
        if (!probe.exists() || !probe.isFile()) {
            return false;
        }
        
        // check for lib subdir
        probe = new File(javaHome, "lib");
        if (!probe.exists() || !probe.isDirectory()) {
            return false;
        }
        
        // now try to deduct whether this is a jre or a jdk (for macos all java
        // installations would be considered jre, which is ok for the validation
        // purposes)
        probe = new File(javaHome, "jre");
        if (probe.exists()) {
            probe = new File(javaHome, "lib/dt.jar");
            if (!probe.exists() || !probe.isFile()) {
                return false;
            }
            
            probe = new File(javaHome, "jre/lib/jce.jar");
            if (!probe.exists() || !probe.isFile()) {
                return false;
            }
        } else {
            probe = new File(javaHome, "lib/charsets.jar");
            if (!probe.exists() || !probe.isFile()) {
                // if the probe does not exist, this may mean that we're on macos,
                // in this case additionally check for 'jce.jar'
                probe = new File(javaHome, "lib/jce.jar");
                if (!probe.exists() || !probe.isFile()) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static boolean isJdk(File javaHome) {
        if (!isJavaHome(javaHome)) {
            return false;
        }
        
        // there is no such thing as private JRE for macos, thus skipping this
        // part; in fact every javahome on a mac is a jdk
        if (!SystemUtils.isMacOS()) {
            File privateJRE = new File(javaHome, "jre");
            
            if (!privateJRE.exists()) {
                return false;
            }
            
            if (!privateJRE.isDirectory()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static Version getVersion(File javaHome) {
        return getInfo(javaHome).getVersion();
    }
    
    public static JavaInfo getInfo(final File javaHome) {
        File location = javaHome;
        try {
            location = javaHome.getCanonicalFile();
        } catch (IOException e) {
            ErrorManager.notifyDebug("Cannot canonize " + javaHome, e);
        }
        
        if (knownJdks.get(location) != null) {
            return knownJdks.get(location);
        }
        
        if (!isJavaHome(location)) {
            return null;
        }
        
        final File executable = getExecutable(location);
        
        final File testJdk;
        try {
            testJdk = FileProxy.getInstance().getFile(TEST_JDK_URI);
        } catch (DownloadException e) {
            ErrorManager.notifyError("Cannot download TestJDK.class" , e);
            return null;
        }
        
        JavaInfo jdkInfo = null;
        try {
            final ExecutionResults results = SystemUtils.executeCommand(
                    executable.getAbsolutePath(),
                    "-classpath",
                    testJdk.getParentFile().getAbsolutePath(),
                    TEST_JDK_CLASSNAME);
            
            jdkInfo = JavaInfo.getInfo(results.getStdOut());
            
            if (jdkInfo != null) {
                knownJdks.put(location, jdkInfo);
            }
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to execute the jdk verification procedure", e);
        }
        
        if (!testJdk.delete()) {
            ErrorManager.notifyError("Cannot delete " + testJdk.getAbsolutePath());
        }
        
        return jdkInfo;
    }
    
    public static File getExecutable(File javaHome) {
        if (SystemUtils.isWindows()) {
            return new File(javaHome, "bin/java.exe");
        } else {
            return new File(javaHome, "bin/java");
        }
    }
    
    public static File getExecutableW(File javaHome) {
        if (SystemUtils.isWindows()) {
            return new File(javaHome, "bin/javaw.exe");
        } else {
            return new File(javaHome, "bin/java");
        }
    }
    
    // windows-only /////////////////////////////////////////////////////////////////
    public static void createJdkKey(Version version, String javaHome) throws NativeException {
        if (!SystemUtils.isWindows()) {
            return;
        }
        
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        
        String key  = registry.constructKey(JDK_KEY, version.toJdkStyle());
        
        setJdkData(key, version, javaHome);
        updateJdkKey(registry.constructKey(JDK_KEY, version.toMinor()));
        updateCurrentVersion();
    }
    
    public static void deleteJdkKey(Version version, String javaHome) throws NativeException {
        if (!SystemUtils.isWindows()) {
            return;
        }
        
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        
        String key  = registry.constructKey(JDK_KEY, version.toJdkStyle());
        int section = getJDKRegistrySection(registry);
        if (registry.keyExists(section, key) && registry.valueExists(section, key, JAVAHOME_VALUE)) {
            String currentJavaHome = registry.getStringValue(section, key, JAVAHOME_VALUE);
            if (currentJavaHome.equals(javaHome)) {
                registry.deleteKey(section, key);
                updateJdkKey(registry.constructKey(JDK_KEY, version.toMinor()));
            }
        }
        
        updateCurrentVersion();
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static int getJDKRegistrySection(WindowsRegistry registry) throws NativeException {
        return (registry.canModifyKey(HKLM,JDK_KEY) ? HKLM : HKCU);
    }
    
    private static void setJdkData(String key, Version version, String javaHome) throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, key);
        registry.setStringValue(section, key, JAVAHOME_VALUE, javaHome);
        registry.setStringValue(section, key, MICROVERSION_VALUE, version.getMicro());
    }
    
    private static void updateJdkKey(String key) throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, key);
        
        String  javaHome = null;
        Version version  = null;
        for (String subkey: registry.getSubKeys(section, JDK_KEY)) {
            if (subkey.startsWith(key) && !subkey.equals(key) && registry.valueExists(section, subkey, JAVAHOME_VALUE)) {
                final String  tempJavaHome = registry.getStringValue(section, subkey, JAVAHOME_VALUE);
                final Version tempVersion  = JavaUtils.getVersion(new File(tempJavaHome));
                if ((tempVersion != null) && ((version == null) || version.olderThan(tempVersion))) {
                    javaHome = tempJavaHome;
                    version  = tempVersion;
                }
            }
        }
        
        if ((version != null) && (javaHome != null)) {
            setJdkData(key, version, javaHome);
        } else {
            registry.deleteKey(section, key);
        }
    }
    
    private static void updateCurrentVersion() throws NativeException {
        final WindowsRegistry registry = ((WindowsNativeUtils) SystemUtils.getNativeUtils()).getWindowsRegistry();
        int section = getJDKRegistrySection(registry);
        registry.createKey(section, JDK_KEY);
        
        String  name    = null;
        Version version = null;
        
        for (String key: registry.getSubKeys(section, JDK_KEY)) {
            if (registry.valueExists(section, key, JAVAHOME_VALUE)) {
                String  tempName     = registry.getKeyName(key);
                String  tempJavaHome = registry.getStringValue(section, key, JAVAHOME_VALUE);
                Version tempVersion  = JavaUtils.getVersion(new File(tempJavaHome));
                if ((tempVersion != null) && ((version == null) || version.olderThan(tempVersion))) {
                    name    = tempName;
                    version = tempVersion;
                }
            }
        }
        
        if ((name != null) && (version != null)) {
            registry.setStringValue(section, JDK_KEY, CURRENT_VERSION_VALUE, name);
        } else {
            registry.deleteKey(section, JDK_KEY);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private JavaUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class JavaInfo {
        /////////////////////////////////////////////////////////////////////////////
        // Static
        public static JavaInfo getInfo(String string) {
            final String[] lines = string.split(SystemUtils.getLineSeparator());
            
            Version version = null;
            String  vendor  = null;
            String  osName  = null;
            String  osArch  = null;
            
            if (lines.length == TEST_JDK_OUTPUT_PARAMETERS) {
                final String javaVersion = lines[0]; // java.version
                final String vmVersion   = lines[1]; // java.vm.version
                
                vendor = lines[2]; // java.vendor
                osName = lines[3]; // os.name
                osArch = lines[4]; // os.arch
                
                String versionString;
                
                // workaround as some vendors provide different data in these
                // properties
                if (vmVersion.indexOf(javaVersion) != -1) {
                    versionString =
                            vmVersion.substring(vmVersion.indexOf(javaVersion));
                } else {
                    versionString =
                            javaVersion;
                }
                
                // convert 1.6.0-b105 to 1.6.0.0.105
                if (versionString.matches(
                        "[0-9]+\\.[0-9]+\\.[0-9]+-b[0-9]+")) {
                    versionString = versionString.replace("-b", ".0.");
                }
                
                // convert 1.6.0_01-b105 to 1.6.0_01.105
                if (versionString.matches(
                        "[0-9]+\\.[0-9]+\\.[0-9]+_[0-9]+-b[0-9]+")) {
                    versionString = versionString.replace("-b", ".");
                }
                
                // and create the version
                final Matcher matcher = Pattern.
                        compile("[0-9][0-9_\\.\\-]+[0-9]").
                        matcher(versionString);
                
                if (matcher.find()) {
                    version = Version.getVersion(matcher.group());
                }
                
                // if the version was created successfully, then we can provide a
                // JavaInfo object
                if (version != null) {
                    return new JavaInfo(version, vendor);
                }
            }
            
            return null;
        }
        
        /////////////////////////////////////////////////////////////////////////////
        // Instance
        private Version version;
        private String  vendor;
        
        public JavaInfo(Version version, String vendor) {
            this.version = version;
            this.vendor = vendor;
        }
        
        public Version getVersion() {
            return version;
        }
        
        public String getVendor() {
            return vendor;
        }
    }
}
