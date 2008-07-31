/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.tools.firefox;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.execution.NbProcessDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * The Firefox extension installation manager
 *  
 * @author quynguyen
 */
public class FFExtensionManager {
    private static final long BUTTON_DELAY = 10000L; // 10 second delay
    private static final long FIREFOX_CHECK_PERIOD = 700L; // 700ms delay
    
    private static final String PROFILE_LOCK_WINDOWS = "parent.lock";
    private static final String PROFILE_LOCK = "lock";
    
    private static final String FIREBUG_MIN_VERSION = "1.1.0b12";
    
    private static final String EXTENSION_CACHE = "extensions.cache";
    private static final String UNINSTALL_KEYWORD = "needs-uninstall";
    private static final String INSTALL_KEYWORD = "needs-install";
    
    private static final String FIREBUG_EXTENSION_ID = "firebug@software.joehewitt.com"; // NOI18N
    
    private static final String FIREBUG_EXTENSION_PATH = "modules/ext/firebug-1.1.0b12.xpi"; // NOI18N
               
    private static final String FIREFOX_EXTENSION_ID = "netbeans-firefox-extension@netbeans.org"; // NOI18N

    private static final String FIREFOX_EXTENSION_PATH = "modules/ext/netbeans-firefox-extension.xpi"; // NOI18N
    
    private static final String CHECKSUM_FILENAME = "netbeans-firefox-extension.jar.MD5"; // NOI18N

    private static final String APPDATA_CMD = "cmd /c echo %AppData%"; // NOI18N

    private static final String[] WIN32_PROFILES_LOCATIONS = {
        "\\Mozilla\\Firefox\\" // NOI18N
    };
    private static final String[] LINUX_PROFILES_LOCATIONS = {
        "/.mozilla/firefox/" // NOI18N

    };
    private static final String[] MACOSX_PROFILES_LOCATIONS = {
        "/Library/Application Support/Firefox/", // NOI18N
        "/Library/Mozilla/Firefox/" // NOI18N

    };

    public static boolean installFirefoxExtensions(HtmlBrowser.Factory browser) {
        File defaultProfile = getDefaultProfile();
        if (defaultProfile == null) {
            Log.getLogger().severe("Could not find Firefox default profile.  Firefox debugging not available.");
            return false;
        }
        
        // Check supported firefox versions first
        StringBuffer ffVersion = new StringBuffer();
        if (!isSupportedFirefox(browser, defaultProfile, ffVersion)) {
            final JButton ok = new JButton();
            Mnemonics.setLocalizedText(ok, NbBundle.getMessage(FFExtensionManager.class, "OK_BUTTON"));

            NotifyDescriptor nd = new NotifyDescriptor(
                    NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_VERSION_MSG"),
                    NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_VERSION_TITLE"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{ok},
                    ok);

            DialogDisplayer.getDefault().notify(nd);
            return false;
        }
        
        File nbExtensionFile = InstalledFileLocator.getDefault().locate(FIREFOX_EXTENSION_PATH,
                "org.netbeans.modules.web.client.tools.firefox.extension", // NOI18N
                false);
        if (nbExtensionFile == null) {
            Log.getLogger().severe("Could not find firefox extension in installation directory");
            return false;
        }
        
        File firebugExtensionFile = InstalledFileLocator.getDefault().locate(FIREBUG_EXTENSION_PATH,
                "org.netbeans.modules.web.client.tools.firefox.extension", // NOI18N 
                false);
        if (nbExtensionFile == null) {
            Log.getLogger().severe("Could not find firebug extension in installation directory");
            return false;
        }
        
        boolean nbExtInstall = extensionRequiresInstall(browser, FIREFOX_EXTENSION_ID, 
                null, nbExtensionFile, defaultProfile, true);
        boolean firebugInstall = extensionRequiresInstall(browser, FIREBUG_EXTENSION_ID, 
                FIREBUG_MIN_VERSION, firebugExtensionFile, defaultProfile, false);
        
        boolean installSuccess = true;

        if (nbExtInstall || firebugInstall) {
            // Ask the user if they want to install the extensions
            NotifyDescriptor installDesc = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_MSG"),
                    NbBundle.getMessage(FFExtensionManager.class, "INSTALL_EXTENSIONS_TITLE"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(installDesc);
            if (result != NotifyDescriptor.OK_OPTION) {
                return false;
            }
            
            if (isFirefoxRunning(defaultProfile)) {
                boolean cancelled = !displayFirefoxRunningDialog(defaultProfile);
                if (cancelled) {
                    return false;
                }
            }
        }
        
        if (nbExtInstall) {
            installSuccess &= installExtension(defaultProfile, nbExtensionFile, FIREFOX_EXTENSION_ID);
        }
        
        if (firebugInstall) {
            installSuccess &= installExtension(defaultProfile, firebugExtensionFile, FIREBUG_EXTENSION_ID);
        }
        
        return installSuccess;
    }
    
    public static boolean extensionRequiresInstall(HtmlBrowser.Factory browser, String extensionId, 
            String minExtVersion, File extensionFile, File defaultProfile, boolean isNbExtension) {
        
        File extensionDir = new File(defaultProfile, "extensions" + File.separator + extensionId);
        String extensionVersion = getVersion(extensionDir);
        
        boolean isInstalling = checkExtensionCache(extensionId, extensionDir, INSTALL_KEYWORD);
        
        // if the user did not restart before the check was made (user presses 'Continue'
        // without waiting for Firefox to restart)
        if (isInstalling) {
            return false;
        }
        
        if ( extensionUpdateRequired(extensionVersion, minExtVersion) || 
                (isNbExtension && !checkExtensionChecksum(extensionFile, defaultProfile, extensionId))) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean installExtension(File profileDir, File extensionXPI, String extensionId) {
        File extensionDir = new File(profileDir, "extensions" + File.separator + extensionId); // NOI18N
        
        // keep a backup of an existing extension just in case
        File backupFolder = null;
        if (extensionDir.exists()) {
            String tmp = extensionId + "-tmp"; // NOI18N
            
            do {
                tmp += "0";
                backupFolder = new File(extensionDir.getParentFile(), tmp);
            } while (backupFolder.exists());
            
            if (!extensionDir.renameTo(backupFolder)) {
                Log.getLogger().warning("Could not create backup for existing extension: " + extensionId);
                rmDir(extensionDir);
                backupFolder = null;
            }
        }
        
        // copy the archive
        boolean copySuccessful = false;
        try {
            extractFiles(extensionXPI, extensionDir);
            copySuccessful = true;
        } catch (IOException ex) {
            Log.getLogger().log(Level.SEVERE, "Could not copy extension: " + extensionId, ex);
            return false;
        } finally {
            if (backupFolder != null) {
                if (copySuccessful) {
                    rmDir(backupFolder);
                } else {
                    rmDir(extensionDir);
                    boolean movedBack = backupFolder.renameTo(extensionDir);
                    if (!movedBack) {
                        Log.getLogger().warning("Could not restore old extension: " + extensionId);
                    }
                }
            }
        }
        
        return true;
    }
    
    private static void rmDir(File folder) {
        if (!folder.exists()) {
            return;
        }
        
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                rmDir(child);
            }
        }
        folder.delete();
    }
    
    private static void extractFiles(File zipFile, File destDir) throws IOException {
        ZipFile zip = new ZipFile(zipFile);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                
                if (entry.isDirectory()) {
                    File newFolder = new File(destDir, fileName);
                    newFolder.mkdirs();
                } else {
                    File file = new File(destDir, fileName);
                    if (file.exists() && file.isDirectory()) {
                        throw new IOException("Cannot write normal file to existing directory with the same path");
                    }
                    
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
                    InputStream input = zip.getInputStream(entry);
                    
                    try {
                        final byte[] buffer = new byte[4096];
                        int len;
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len);
                        }
                    } finally {
                        output.close();
                        input.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    
    private static boolean displayFirefoxRunningDialog(final File profileDir) {
        String dialogText = NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_RUNNING_MSG");
        String dialogTitle = NbBundle.getMessage(FFExtensionManager.class, "FIREFOX_RUNNING_TITLE");

        final JButton ok = new JButton();
        Mnemonics.setLocalizedText(ok, NbBundle.getMessage(FFExtensionManager.class, "INSTALL_BUTTON"));
        ok.setEnabled(false);

        JButton cancel = new JButton();
        Mnemonics.setLocalizedText(cancel, NbBundle.getMessage(FFExtensionManager.class, "CANCEL_BUTTON"));

        Object[] options = new Object[]{ok, cancel};
        DialogDescriptor dd = new DialogDescriptor(dialogText, dialogTitle, true, 
                options, cancel, DialogDescriptor.BOTTOM_ALIGN, null, null);

        dd.setClosingOptions(new Object[] { cancel });
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        final boolean[] cancelled = { true };
        
        final TimerTask runningCheck = new TimerTask() {
            @Override
            public void run() {
                if (dialog.isVisible() && !isFirefoxRunning(profileDir)) {
                    cancelled[0] = false;
                    dialog.setVisible(false);
                    this.cancel();
                }
            }
        };
        
        ok.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                cancelled[0] = false;
                dialog.setVisible(false);
            }
            
        });

        // enable button after BUTTON_DELAY seconds
        TimerTask enableButton = new TimerTask() {
            public void run() {
                ok.setEnabled(true);
            }
        };
        Timer timer = new Timer();
        timer.schedule(enableButton, BUTTON_DELAY);
        
        Timer profileCheckTimer = new Timer();
        profileCheckTimer.schedule(runningCheck, FIREFOX_CHECK_PERIOD, FIREFOX_CHECK_PERIOD);
        
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
            timer.cancel();
            profileCheckTimer.cancel();
        }
        
        return !cancelled[0];
    }
    
    private static boolean isFirefoxRunning(File profileDir) {
        if (Utilities.isWindows()) {
            return new File(profileDir, PROFILE_LOCK_WINDOWS).exists();
        } else if (Utilities.isMac()) {
            // XXX TODO Figure out how to detect if FF is running on MacOS
            return false;
        } else {
            String[] fileNames = profileDir.list();
            if (fileNames != null) {
                for (String fileName : fileNames) {
                    if (fileName.equals(PROFILE_LOCK)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    private static boolean extensionUpdateRequired(String extVersion, String minVersion) {
        if (extVersion == null) {
            return true;
        }else if (minVersion == null) {
            return false;
        }
        
        List<Integer> extList = getVersionParts(extVersion);
        List<Integer> minList = getVersionParts(minVersion);
        
        for (int i = 0; i < Math.max(extList.size(), minList.size()); i++) {
            int extValue = (i >= extList.size()) ? 0 : extList.get(i).intValue();
            int minValue = (i >= minList.size()) ? 0 : minList.get(i).intValue();
            
            if (extValue < minValue) {
                return true;
            } else if (extValue > minValue) {
                return false;
            }
        }
        
        return false;
    }
    
    private static List<Integer> getVersionParts(String version) {
        List<Integer> result = new ArrayList<Integer>();
        
        StringTokenizer tokens = new StringTokenizer(version, ".");
        while (tokens.hasMoreTokens()) {
            String nextToken = tokens.nextToken();
            if (nextToken.contains("b")) {
                int index = nextToken.indexOf("b");
                
                String first = nextToken.substring(0, index);
                String second = nextToken.substring(index+1, nextToken.length());
                
                // version xxbyy is greater than any version xx-1 without a beta
                // but less than version xx without a beta
                result.add(new Integer(Integer.valueOf(first).intValue() - 1));
                result.add(Integer.valueOf(second));
            }else {
                result.add(Integer.valueOf(nextToken));
            }
        }
        
        return result;
    }
    
    
    /**
     * 
     * @param extensionXpi
     * @param profileDir
     * @param extensionId
     * @return true if the checksums match
     */
    private static boolean checkExtensionChecksum(File extensionXpi, File profileDir, String extensionId) {
        if (extensionXpi == null) return true;
        
        File extensionDir = new File(new File(profileDir, "extensions"), extensionId); // NOI18N
        File checksumFile = new File(extensionDir, CHECKSUM_FILENAME);
        
        if (checksumFile.exists() && checksumFile.isFile()) {
            ZipFile extensionZip = null;
            try {
                extensionZip = new ZipFile(extensionXpi);
                ZipEntry entry = extensionZip.getEntry(CHECKSUM_FILENAME);
                
                if (entry != null) {                    
                    BufferedInputStream profileInput  = new BufferedInputStream(new FileInputStream(checksumFile));
                    InputStream xpiInput = extensionZip.getInputStream(entry);
                    
                    try {
                        final byte[] profileBuffer = new byte[512];
                        final byte[] xpiBuffer = new byte[512];
                        int profileLen, xpiLen;
                        
                        do {
                            profileLen = profileInput.read(profileBuffer);
                            xpiLen = xpiInput.read(xpiBuffer);
                            
                            if (profileLen != xpiLen) {
                                return false;
                            }
                            
                            for (int i = 0; i < profileLen; i++) {
                                if (profileBuffer[i] != xpiBuffer[i]) {
                                    return false;
                                }
                            }
                            
                        } while (profileLen >= 0);
                        
                        return true;
                    } finally {
                        profileInput.close();
                        xpiInput.close();
                    }
                    
                }
            } catch (IOException ex) {
                Log.getLogger().log(Level.SEVERE, "Error checking extension XPI", ex);
            } finally {
                if (extensionZip != null) {
                    try {
                        extensionZip.close();
                    }catch (IOException ex) {
                        Log.getLogger().log(Level.SEVERE, "Error closing zip file", ex);
                    }
                }
            }
        }
        
        return false;
    }
    
    private static String getVersion(File extensionDir) {
        File rdfFile = new File(extensionDir, "install.rdf"); // NOI18N
        
        if (rdfFile.isFile()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            Document doc = null;
            
            try {
                builder = factory.newDocumentBuilder();
                doc = builder.parse(rdfFile);
            }catch (Exception ex) {
                Log.getLogger().log(Level.WARNING, "Unexpected exception", ex);
                return null;
            }
            
            Node descriptionNode = null;
            
            for (Node node = doc.getDocumentElement().getFirstChild(); descriptionNode == null && node != null; node = node.getNextSibling()) {
                String nodeName = node.getNodeName();
                nodeName = (nodeName == null) ? "" : nodeName.toLowerCase();
                
                
                if (nodeName.equals("description") || nodeName.equals("rdf:description")) { // NOI18N
                    Node aboutNode = node.getAttributes().getNamedItem("about"); // NOI18N
                    if (aboutNode == null) {
                        aboutNode = node.getAttributes().getNamedItem("RDF:about"); // NOI18N
                    }
                    
                    if (aboutNode != null) {
                        String aboutText = aboutNode.getNodeValue();
                        aboutText = (aboutText == null) ? "" : aboutText.toLowerCase();
                        
                        if (aboutText.equals("urn:mozilla:install-manifest")) { // NOI18N
                            descriptionNode = node;
                        }
                    }
                }
            }
            
            if (descriptionNode == null) {
                return null;
            }
            
            // check node attributes for version info
            Node versionNode = descriptionNode.getAttributes().getNamedItem("em:version"); // NOI18N
            if (versionNode != null) {
                return versionNode.getNodeValue();
            }
            
            // check children nodes
            NodeList children = descriptionNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String name = child.getNodeName();
                name = (name == null) ? "" : name.toLowerCase();
                
                if (name.equals("em:version")) { // NOI18N
                    return child.getTextContent();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks extension.cache to determine if the extension has been scheduled for removal
     * 
     * @param extensionID the Firefox extension ID to check
     * @param profileDir the profile directory
     * @param keywords the keyword in the extension.cache to check
     * 
     * @return true if the keyword was found for the given extension
     */
    private static boolean checkExtensionCache(String extensionID, File profileDir, String keyword) {
        File extensionCache = new File(profileDir, EXTENSION_CACHE);
        if (extensionCache.exists() && extensionCache.isFile()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(extensionCache));
                boolean foundExtension = false;
                
                while (br.ready() && !foundExtension) {
                    String nextLine = br.readLine();
                    if (nextLine != null) {
                        String[] words = nextLine.split("\\s");
                        for (String element : words) {
                            if (element.equals(extensionID)) {
                                foundExtension = true;
                            } else if (foundExtension == true && element.equals(keyword)) {
                                return true;
                            }
                        }
                    }
                }
            }catch (IOException ex) {
                Log.getLogger().log(Level.WARNING, "Error reading " + extensionCache.getAbsolutePath(), ex);
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    }catch (IOException ex) {
                        Log.getLogger().log(Level.WARNING, "Unexpected exception", ex);
                    }
                }
            }
        }
        
        return false;
    }
    
    
    private static String[] getLocationsForOS() {
        if (Utilities.isWindows()) { // NOI18N
            return getUserPaths(WIN32_PROFILES_LOCATIONS);
        } else if (Utilities.isMac()) {
            return getUserPaths(MACOSX_PROFILES_LOCATIONS);
        } else {
            // assuming that linux/unix/sunos firefox paths are equivalent
            return getUserPaths(LINUX_PROFILES_LOCATIONS);
        }

    }

    private static String[] getUserPaths(String[] paths) {
        String[] result = new String[paths.length];
        String appRoot = getUserHome();

        if (appRoot == null) {
            return null;
        }
        for (int i = 0; i < paths.length; i++) {
            result[i] = appRoot + paths[i];
        }

        return result;
    }

    /**
     *
     * @return user home, %AppData% on Windows
     */
    private static String getUserHome() {
        String userHome = System.getProperty("user.home"); // NOI18N

        if (!Utilities.isWindows()) {
            return userHome;
        } else {
            String appData = userHome + File.separator + NbBundle.getMessage(FFExtensionManager.class, "WIN32_APPDATA_FOLDER");

            BufferedReader br = null;
            try {
                Process process = Runtime.getRuntime().exec(APPDATA_CMD);
                process.waitFor();

                InputStream input = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(input));

                while (br.ready()) {
                    String nextLine = br.readLine();

                    if (nextLine.trim().length() == 0) continue;

                    File f = new File(nextLine.trim());
                    if (f.exists() && f.isDirectory()) {
                        return f.getAbsolutePath();
                    }
                }
            }catch (Exception ex) {
                Log.getLogger().info("Unable to run process: " + APPDATA_CMD);
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    }catch (IOException ex) {
                    }
                }
            }

            return appData;
        }
    }

    private static File getDefaultProfile() {
        String[] firefoxDirs = getLocationsForOS();
        
        if (firefoxDirs != null) {
            for (String firefoxUserDir : firefoxDirs) {
                File dir = new File(firefoxUserDir);
                if (dir.isDirectory() && dir.exists()) {
                    List<FirefoxProfile> profiles = getAllProfiles(dir);
                    
                    if (profiles == null || profiles.size() == 0) {
                        // guess the default profile
                        File profilesDir = new File(dir, "Profiles"); // NOI18N
                        
                        if (profilesDir.isDirectory()) {
                            File[] childrenFiles = profilesDir.listFiles();
                            for (int i = 0; childrenFiles != null && i < childrenFiles.length; i++) {
                                File childFile = childrenFiles[i];
                                
                                if (childFile.isDirectory() && childFile.getAbsolutePath().endsWith(".default")) { // NOI18N
                                    return childFile;
                                }
                            }
                        }
                    }else {
                        // find a "default" profile
                        for (FirefoxProfile profile : profiles) {
                            if (profile.isDefaultProfile()) {
                                File profileDir = null;
                                
                                if (profile.isRelative()) {
                                    profileDir = new File(dir, profile.getPath());
                                }else {
                                    profileDir = new File(profile.getPath());
                                }
                                
                                if (profileDir.isDirectory()) {
                                    return profileDir;
                                }
                            }
                        }
                        
                        // otherwise pick the first valid profile
                        for (FirefoxProfile profile : profiles) {
                            File profileDir = null;

                            if (profile.isRelative()) {
                                profileDir = new File(dir, profile.getPath());
                            } else {
                                profileDir = new File(profile.getPath());
                            }

                            if (profileDir.isDirectory()) {
                                return profileDir;
                            }                            
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    // XXX Copied from JSAbstractDebugger
    protected static String getBrowserExecutable(HtmlBrowser.Factory browser) {
        if (browser != null) {
            try {
                Method method = browser.getClass().getMethod("getBrowserExecutable");
                NbProcessDescriptor processDescriptor = (NbProcessDescriptor) method.invoke(browser);
                return processDescriptor.getProcessName();
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return null;

    }

    /**
     * Checks $firefox_install_dir/defaults/pref/firefox.js for version string from
     * <code>prefs("general.useragent.extra.firefox", "Firefox/*****");</code>, or 
     * $profile_dir/compatibility.ini if the firefox installation directory cannot be
     * found.
     * 
     * @param browser
     * @param defaultProfile
     * @param actualVersion
     * @return true if Firefox version is 2.*.*
     */
    private static boolean isSupportedFirefox(HtmlBrowser.Factory browser, File defaultProfile, StringBuffer actualVersion) {
        String browserExecutable = getBrowserExecutable(browser);
        if (browserExecutable == null) {
            return isCompatibleFirefox(defaultProfile);
        }
        
        File firefox_js = new File(new File(browserExecutable).getParentFile(), "defaults/pref/firefox.js"); // NOI18N
        if (!firefox_js.exists()) {
            return isCompatibleFirefox(defaultProfile);
        }
        
        Pattern lineMatch = Pattern.compile("\\s*pref\\s*\\(\\s*\"general\\.useragent\\.extra\\.firefox\""); // NOI18N
        Pattern versionMatch = Pattern.compile("\"Firefox/[^\"]+\""); // NOI18N
        int majorVersion = -1;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(firefox_js));
            while (br.ready()) {
                String nextLine = br.readLine();
                if (lineMatch.matcher(nextLine).find()) {
                    Matcher matcher = versionMatch.matcher(nextLine);
                    if (matcher.find()) {
                        String version = matcher.group();
                        majorVersion = Integer.valueOf(version.substring(9, 10)).intValue();
                        actualVersion.append(version);
                        break;
                    }
                }
            }
            
            return majorVersion == 2;
        } catch (IOException ex) {
            Log.getLogger().log(Level.INFO, "Error reading Firefox version.", ex);
            return isCompatibleFirefox(defaultProfile);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Log.getLogger().log(Level.SEVERE, "Could not read Firefox version file", ex);
                }
            }
        }
    }
    

    /**
     * Check if the Firefox version is compatible by reading the compatibility.ini file in the
     * Firefox profile and then reading the value of key <code>LastVersion</code>.  This check is
     * only used if the Firefox installation directory is not available (installed on /usr/dist/exe,
     * for example)
     *
     * @param defaultProfile
     * @return true if the Firefox version is compatible
     */
    private static boolean isCompatibleFirefox(File defaultProfile) {
        assert defaultProfile != null;
        File compatibilityDotIni = new File(defaultProfile, "compatibility.ini"); // NOI18N

        if (compatibilityDotIni.exists() && compatibilityDotIni.isFile() && compatibilityDotIni.canRead()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(compatibilityDotIni));
                String aLine;
                while ((aLine = bufferedReader.readLine()) != null) {
                    if (aLine.startsWith("LastVersion=")) { // NOI18N

                        aLine = aLine.substring(12);
                        return aLine.startsWith("2.0.0");
                    }
                }
            } catch (FileNotFoundException ex) {
                Log.getLogger().log(Level.INFO, "File not found: " + compatibilityDotIni.getAbsolutePath());
            } catch (IOException ex) {
                Log.getLogger().log(Level.INFO, "Error reading " + compatibilityDotIni.getAbsolutePath());
            }
        }
        return false;
    }

    
    private static boolean isHttpURLValid(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            int response = connection.getResponseCode();
            
            return response == HttpURLConnection.HTTP_OK;
        }catch (Exception ex) {
            return false;
        }
    }
    
    private static List<FirefoxProfile> getAllProfiles(File dir) {
        File profileCfg = new File(dir, "profiles.ini"); // NOI18N
        try {
            BufferedReader reader = new BufferedReader(new FileReader(profileCfg));
            List<List<String>> profileData = new ArrayList<List<String>>();
            
            List<String> currentProfile = null;
            while(reader.ready()) {
                String line = reader.readLine().trim();
                
                if (line.startsWith("[") && line.endsWith("]")) { // NOI18N
                    if (currentProfile != null) {
                        profileData.add(currentProfile);
                    }
                    
                    currentProfile = new ArrayList<String>();
                    currentProfile.add(line);
                }else if (line.indexOf('=') > 0) { // NOI18N
                    currentProfile.add(line);
                }
            }
            
            if (currentProfile != null && !profileData.contains(currentProfile)) {
                profileData.add(currentProfile);
            }
            
            List<FirefoxProfile> allProfiles = new ArrayList<FirefoxProfile>();            
            
            for (List<String> profileText : profileData) {
                if (profileText.size() == 0) {
                    continue;
                }else if (!profileText.get(0).startsWith("[")) {
                    continue;
                }
                
                FirefoxProfile profile = new FirefoxProfile();
                boolean isValidProfile = false;
                for (String line : profileText) {
                    int index = line.indexOf('='); // NOI18N
                    
                    if (index > 0 && index < line.length()-1) {
                        String var = line.substring(0, index);
                        String val = line.substring(index+1, line.length());
                        
                        if (var.equals("Name")) { // NOI18N
                            isValidProfile = true;
                        }else if (var.equals("IsRelative")) { // NOI18N
                            if (val.equals("1")) { // NOI18N
                                profile.setRelative(true);
                            }
                        }else if (var.equals("Path")) { // NOI18N
                            profile.setPath(val);
                        }else if (var.equals("Default")) { // NOI18N
                            if (val.equals("1")) { // NOI18N
                                profile.setDefaultProfile(true);
                            }
                        }
                    }
                }
                
                if (isValidProfile) {
                    allProfiles.add(profile);
                }
            }
            
            return allProfiles;
        }catch (IOException ex) {
            Log.getLogger().log(Level.WARNING, "Could not read Firefox profiles", ex);
        }
        
        return null;
    }
    
    private static final class FirefoxProfile {
        private boolean relative = false;
        private boolean defaultProfile = false;
        private String path = "";
        
        public FirefoxProfile() {
        }

        public boolean isDefaultProfile() {
            return defaultProfile;
        }

        public void setDefaultProfile(boolean defaultProfile) {
            this.defaultProfile = defaultProfile;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isRelative() {
            return relative;
        }

        public void setRelative(boolean relative) {
            this.relative = relative;
        }
    }
}
