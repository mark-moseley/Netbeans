/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.mercurial.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Level;
import org.ini4j.Ini;
import org.ini4j.InvalidIniFormatException;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * Handles the Mercurial <b>hgrc</b> configuration file.</br>
 *
 * @author Padraig O'Briain
 */
public class HgConfigFiles {    
    public static final String HG_EXTENSIONS = "extensions";  // NOI18N
    public static final String HG_EXTENSIONS_HGK = "hgext.hgk";  // NOI18N
    public static final String HG_EXTENSIONS_FETCH = "fetch";  // NOI18N
    public static final String HG_UI_SECTION = "ui";  // NOI18N
    public static final String HG_USERNAME = "username";  // NOI18N
    public static final String HG_PATHS_SECTION = "paths";  // NOI18N
    public static final String HG_DEFAULT_PUSH = "default-push";  // NOI18N
    public static final String HG_DEFAULT_PUSH_VALUE = "default-push";  // NOI18N
    public static final String HG_DEFAULT_PULL = "default-pull";  // NOI18N
    public static final String HG_DEFAULT_PULL_VALUE = "default";  // NOI18N

    /** The HgConfigFiles instance for user and system defaults */
    private static HgConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>hgrc</b>
     * file used by the Mercurial module */    
    private Ini hgrc = null;
    
    /** The repository directory if this instance is for a repository */
    private File dir;
    public static final String HG_RC_FILE = "hgrc";                                                                       // NOI18N
    public static final String HG_REPO_DIR = ".hg";                                                                       // NOI18N

    private static final String WINDOWS_HG_RC_FILE = "Mercurial.ini";                                 // NOI18N
    private static final String WINDOWS_DEFAULT_MECURIAL_INI_PATH = "C:\\Mercurial\\Mercurial.ini";                                 // NOI18N
    private boolean bIsProjectConfig;
    private InvalidIniFormatException initException;
    /**
     * Creates a new instance
     */
    private HgConfigFiles() {
        bIsProjectConfig = false;
        // get the system hgrc file
        if(Utilities.isWindows()) {
            hgrc = loadSystemAndGlobalFile(WINDOWS_HG_RC_FILE);
        }else{
            hgrc = loadSystemAndGlobalFile(HG_RC_FILE);
        }
    }
    
    /**
     * Returns a singleton instance
     *
     * @return the HgConfiles instance
     */
    public static HgConfigFiles getSysInstance() {
        if (instance==null) {
            instance = new HgConfigFiles();
        }
        return instance;
    }

    public HgConfigFiles(File file) {
        bIsProjectConfig = true;
        dir = file;        
        // <repository>/.hg/hgrc on all platforms
        hgrc = loadRepoHgrcFile(file);
    }

    public IOException getException () {
        return initException;
    }
 
    public void setProperty(String name, String value) {
        if (name.equals(HG_USERNAME)) { 
            setProperty(HG_UI_SECTION, HG_USERNAME, value);
        } else if (name.equals(HG_DEFAULT_PUSH)) { 
            setProperty(HG_PATHS_SECTION, HG_DEFAULT_PUSH_VALUE, value);
        } else if (name.equals(HG_DEFAULT_PULL)) { 
            setProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE, value);
        } else if (name.equals(HG_EXTENSIONS_HGK)) { 
            // Allow hgext.hgk to be set to some other user defined value if required
            if(getProperty(HG_EXTENSIONS, HG_EXTENSIONS_HGK).equals("")){
                setProperty(HG_EXTENSIONS, HG_EXTENSIONS_HGK, value, true);
            }
        } else if (name.equals(HG_EXTENSIONS_FETCH)) { 
            // Allow fetch to be set to some other user defined value if required
            if(getProperty(HG_EXTENSIONS, HG_EXTENSIONS_FETCH).equals("")){ 
                setProperty(HG_EXTENSIONS, HG_EXTENSIONS_FETCH, value, true);
            }
        }

    }
 
    public void setProperty(String section, String name, String value, boolean allowEmpty) {
        if (!allowEmpty) {
            if (value.length() == 0) {
                removeProperty(section, name);
            } else {
                Ini.Section inisection = getSection(hgrc, section, true);
                inisection.put(name, value);
            }
        } else {
            Ini.Section inisection = getSection(hgrc, section, true);
            inisection.put(name, value);
        }
        if (!bIsProjectConfig && Utilities.isWindows()) {
            storeIni(hgrc, WINDOWS_HG_RC_FILE);
        } else {
            storeIni(hgrc, HG_RC_FILE);
        }
    }

    public void setProperty(String section, String name, String value) {
        setProperty(section, name,value, false);
    }

    public void setUserName(String value) {
        setProperty(HG_UI_SECTION, HG_USERNAME, value);
    }

    public String getSysUserName() {
        return getUserName(true);
    }

    public String getSysPushPath() {
        return getDefaultPush(true);
    }

    public String getSysPullPath() {
        return getDefaultPull(true);
    }

    public Properties getProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        Properties props = new Properties();
        if (inisection != null) {
            Set<String> keys = inisection.keySet();
            for (String key : keys) {
                props.setProperty(key, inisection.get(key));
            }
        }
        return props;
    }

    public void clearProperties(String section) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.clear();
            if (!bIsProjectConfig && Utilities.isWindows()) {
                storeIni(hgrc, WINDOWS_HG_RC_FILE);
            } else {
                storeIni(hgrc, HG_RC_FILE);
            }
         }
    }

    public void removeProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, false);
        if (inisection != null) {
             inisection.remove(name);
            if (!bIsProjectConfig && Utilities.isWindows()) {
                storeIni(hgrc, WINDOWS_HG_RC_FILE);
            } else {
                storeIni(hgrc, HG_RC_FILE);
            }
         }
    }

    public String getDefaultPull(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE); 
    }

    public String getDefaultPush(Boolean reload) {
        if (reload) {
            doReload();
        }
        String value = getProperty(HG_PATHS_SECTION, HG_DEFAULT_PUSH); 
        if (value.length() == 0) {
            value = getProperty(HG_PATHS_SECTION, HG_DEFAULT_PULL_VALUE); 
        }
        return value;
    }

    public String getUserName(Boolean reload) {
        if (reload) {
            doReload();
        }
        return getProperty(HG_UI_SECTION, HG_USERNAME);                                              
    }

    public String getProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, true);
        String value = inisection.get(name);
        return value != null ? value : "";        // NOI18N 
    }
    
    public boolean containsProperty(String section, String name) {
        Ini.Section inisection = getSection(hgrc, section, true);
        return inisection.containsKey(name);
    }

    private void doReload () {
        if (dir == null) {
            if(!bIsProjectConfig && Utilities.isWindows()) {
                hgrc = loadSystemAndGlobalFile(WINDOWS_HG_RC_FILE);
            }else{    
                hgrc = loadSystemAndGlobalFile(HG_RC_FILE);                                          
            }
        } else {
            hgrc = loadRepoHgrcFile(dir);                                      
        }
    }

    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null && create) {
            return ini.add(key);
        }
        return section;
    }
    
    private void storeIni(Ini ini, String iniFile) {
        assert initException == null;
        try {
            String filePath;
            if (dir != null) {
                filePath = dir.getAbsolutePath() + File.separator + HG_REPO_DIR + File.separator + iniFile; // NOI18N 
            } else {
                filePath =  getUserConfigPath() + iniFile;
            }
            File file = FileUtil.normalizeFile(new File(filePath));
            file.getParentFile().mkdirs();
            ini.store(new BufferedOutputStream(new FileOutputStream(file)));
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
    }

    /**
     * Returns a default ini instance, with classloader issue workaround (#141364)
     * @return ini instance
     */
    private Ini createIni () {
        return createIni(null);
    }

    /**
     * Returns an ini instance for given file, with classloader issue workaround (#141364)
     * @param file ini file being parsed. If null then a default ini will be created.
     * @return ini instance for the given file
     */
    private Ini createIni (File file) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Mercurial.class.getClassLoader());
        try {
            if (file == null) {
                return new Ini();
            } else {
                return new Ini(new FileReader(file));
            }
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (InvalidIniFormatException ex) {
            Mercurial.LOG.log(Level.INFO, "Cannot parse configuration file", ex); // NOI18N
            initException = ex;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        return null;
    }

    /**
     * Loads Repository configuration file  <repo>/.hg/hgrc on all platforms
     * */
    private Ini loadRepoHgrcFile(File dir) {
        String filePath = dir.getAbsolutePath() + File.separator + HG_REPO_DIR + File.separator + HG_RC_FILE; // NOI18N 
        File file = FileUtil.normalizeFile(new File(filePath));
        File tmpFile = HgUtils.fixPathsInIniFileOnWindows(file);
        Ini system = null;
        if (Utilities.isWindows() && tmpFile != null && tmpFile.isFile() && tmpFile.canWrite() && file != null) {
            tmpFile.deleteOnExit();
            system = createIni(tmpFile);
        } else {
            system = createIni(file);
        }

        if(system == null) {
            system = createIni();
            Mercurial.LOG.log(Level.FINE, "Could not load the file " + filePath + ". Falling back on hg defaults."); // NOI18N
        }
        return system;
    }
    /**
     * Loads user and system configuration files 
     * The settings are loaded and merged together in the folowing order:
     * <ol>
     *  <li> The per-user configuration file, Unix: ~/.hgrc, Windows: %USERPROFILE%\Mercurial.ini
     *  <li> The system-wide file, Unix: /etc/mercurial/hgrc, Windows: Mercurial_Install\Mercurial.ini
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the configuration file. 
     */       
    private Ini loadSystemAndGlobalFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + fileName;
        File file = FileUtil.normalizeFile(new File(filePath));
        File tmpFile = HgUtils.fixPathsInIniFileOnWindows(file);
        Ini system = null;
        if (Utilities.isWindows() && tmpFile != null && tmpFile.isFile() && tmpFile.canWrite() && file != null) {
            tmpFile.deleteOnExit();
            system = createIni(tmpFile);
        } else {
            system = createIni(file);
        }

        if(system == null) {
            system = createIni();
            Mercurial.LOG.log(Level.INFO, "Could not load the file " + filePath + ". Falling back on hg defaults."); // NOI18N
        }
        
        Ini global = null;
        File gFile = FileUtil.normalizeFile(new File(getGlobalConfigPath() + File.separator + fileName));
        File tmp2File = HgUtils.fixPathsInIniFileOnWindows(gFile);
        if (Utilities.isWindows() && tmp2File != null) {
            tmp2File.deleteOnExit();
            global = createIni(tmp2File);   // NOI18N
        } else {
            global = createIni(gFile);   // NOI18N
        }

        if(global != null) {
            merge(global, system);
        }                
        return system;
    }
    /**
     * Merges only sections/keys/values into target which are not already present in source
     * 
     * @param source the source ini file
     * @param target the target ini file in which the values from the source file are going to be merged
     */
    private void merge(Ini source, Ini target) {
        for (Iterator<String> itSections = source.keySet().iterator(); itSections.hasNext();) {
            String sectionName = itSections.next();
            Ini.Section sourceSection = source.get( sectionName );
            Ini.Section targetSection = target.get( sectionName );

            if(targetSection == null) {
                targetSection = target.add(sectionName);
            }

            for (Iterator<String> itVariables = sourceSection.keySet().iterator(); itVariables.hasNext();) {
                String key = itVariables.next();

                if(!targetSection.containsKey(key)) {
                    targetSection.put(key, sourceSection.get(key));
                }
            }            
        }
    }
   
    
    /**
     * Return the path for the user command lines configuration directory 
     */
    private static String getUserConfigPath() {        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/.";                                // NOI18N
        } else if (Utilities.isWindows()){
            String userConfigPath = getUSERPROFILE();
            return userConfigPath.equals("")? "":  userConfigPath + File.separator; // NOI18N 
        } 
        return "";                                                              // NOI18N
    }


    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/mercurial";               // NOI18N
        } else if (Utilities.isWindows()){
            // <Mercurial Install>\Mercurial.ini
            String mercurialPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if(mercurialPath != null && !mercurialPath.equals("")){
                File ini = new File(mercurialPath, WINDOWS_HG_RC_FILE);
                if(ini != null && ini.exists() && ini.canRead()){
                    return ini.getParentFile().getAbsolutePath();
                }
            }
            // C:\Mercurial\Mercurial.ini
            File ini = new File(WINDOWS_DEFAULT_MECURIAL_INI_PATH);// NOI18N
            if(ini != null && ini.exists() && ini.canRead()){
                    return ini.getParentFile().getAbsolutePath();                
            }        
        } 
        return "";                                  // NOI18N
    }

    private static String getUSERPROFILE() {
        if(!Utilities.isWindows()) return null;

        String userprofile = ""; // NOI18N
        userprofile = System.getenv("USERPROFILE");// NOI18N

        return userprofile!= null? userprofile: ""; // NOI18N
    }

}
