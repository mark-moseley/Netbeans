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
package org.netbeans.modules.subversion.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Ini;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.ProxySettings;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * Handles the Subversions <b>servers</b> and <b>config</b> configuration files.</br>
 * Everytime the singleton instance is created are the values from the commandline clients
 * configuration directory merged into the Subversion modules configuration files. 
 * (registry on windows are ignored). 
 * Already present proxy setting values wan't be changed, 
 * the remaining values are always taken from the commandline clients configuration files. 
 * The only exception is the 'store-auth-creds' key, which is always set to 'no'.
 * 
 * @author Tomas Stupka
 */
public class SvnConfigFiles {    

    /** the only SvnConfigFiles instance */
    private static SvnConfigFiles instance;

    /** the Ini instance holding the configuration values stored in the <b>servers</b>
     * file used by the Subversion module */    
    private Ini svnServers = null;
    
    /** the Ini instance holding the configuration values stored in the <b>config</b>
     * file used by the Subversion module */
    private Ini config = null;

    private ProxySettings proxySettings;
    
    private static final String UNIX_CONFIG_DIR = ".subversion/";                                                               // NOI18N
    private static final String GROUPS_SECTION = "groups";                                                                      // NOI18N
    private static final String GLOBAL_SECTION = "global";                                                                      // NOI18N
    private static final String WINDOWS_USER_APPDATA = getAPPDATA();
    private static final String WINDOWS_CONFIG_DIR = WINDOWS_USER_APPDATA + "\\Subversion";                                     // NOI18N
    private static final String WINDOWS_GLOBAL_CONFIG_DIR = getGlobalAPPDATA() + "\\Subversion";                                // NOI18N
    private static final List<String> DEFAULT_GLOBAL_IGNORES = 
            parseGlobalIgnores("*.o *.lo *.la #*# .*.rej *.rej .*~ *~ .#* .DS_Store");                                          // NOI18N

    private interface IniFilePatcher {
        void patch(Ini file);
    }

    /**
     * The value for the 'store-auth-creds' key in the config cofiguration file is alway set to 'no'
     * so the commandline client wan't create a file holding the authentication credentials when
     * a svn command is called. The reason for this is that the Subverion module holds the credentials
     * in files with the same format as the commandline client but with a different name.
     */
    private class ConfigIniFilePatcher implements IniFilePatcher {
        public void patch(Ini file) {
            // patch store-auth-creds to "no"
            Ini.Section auth = (Ini.Section) file.get("auth");                  // NOI18N
            if(auth == null) {
                auth = file.add("auth");                                        // NOI18N
            }
            auth.put("store-auth-creds", "no");                                 // NOI18N
        }
    }

    /**
     * Creates a new instance
     */
    private SvnConfigFiles() {      
        // copy config file        
        config = copyConfigFileToIDEConfigDir("config", new ConfigIniFilePatcher());    // NOI18N
        // get the system servers file 
        svnServers = loadSystemIniFile("servers");        
    }
    
    /**
     * Returns a singleton instance.
     *
     * @return the SvnConfigFiles instance
     */
    public static SvnConfigFiles getInstance() {
        
        //T9Y - singleton is not required - always create new instance of this class
        String t9yUserConfigPath = System.getProperty("netbeans.t9y.svn.user.config.path");
        if (t9yUserConfigPath != null && t9yUserConfigPath.length() > 0) {
            //make sure that new instance will be created
            instance = null;
        }
        
        if(instance==null) {
            instance = new SvnConfigFiles();                    
        }
        return instance;
    }

    /**
     * Stores the cert file and password, proxy host, port, username and password for the given
     * {@link SVNUrl} in the
     * <b>servers</b> file used by the Subversion module.  
     *     
     * @param host the host
     */
    public void storeSvnServersSettings(SVNUrl url) {
                        
        assert url != null : "can\'t do anything for a null host";               // NOI18N
                         
        if(!(url.getProtocol().startsWith("http") ||
             url.getProtocol().startsWith("https")) ) 
        {            
            // we need the settings only for remote http and https repositories
            return;
        }
        
        Ini nbServers = new Ini();   
        Ini.Section nbGlobalSection = nbServers.add(GLOBAL_SECTION);
        if(url.getProtocol().startsWith("https")) {
            setCert(url, nbGlobalSection);
        }
        setProxy(url, nbGlobalSection);
        storeIni(nbServers, "servers");                                                       // NOI18N    
    }        

    private void setCert(SVNUrl url, Ini.Section nbGlobalSection) {
        RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(url.toString());
        if(rc == null) {
            return;
        }
        String certFile = rc.getCertFile();
        if(certFile == null || certFile.equals("")) {
            return;
        }
        String certPassword = rc.getCertPassword();
        if(certPassword == null || certPassword.equals("")) {
            return;
        }
        nbGlobalSection.put("ssl-client-cert-file", certFile);
        nbGlobalSection.put("ssl-client-cert-password", certPassword);
    }

    private void setProxy(SVNUrl url, Ini.Section nbGlobalSection) {
        String host =  SvnUtils.ripUserFromHost(url.getHost());
        ProxySettings ps = new ProxySettings();
        if(proxySettings != null && ps.equals(proxySettings)) {
            return;
        } else {
            proxySettings = ps;
        }

        Ini.Section svnGlobalSection = svnServers.get(GLOBAL_SECTION);
        if(!proxySettings.isDirect()) {
            String proxyHost = "";
            int proxyPort = -1;
            if(url.getProtocol().startsWith("https")) {
                proxyHost = proxySettings.getHttpsHost();
                proxyPort = proxySettings.getHttpsPort();
            }
            if(proxyHost.equals("")) {
                proxyHost = proxySettings.getHttpHost();
                proxyPort = proxySettings.getHttpPort();
            }
            String exceptions = proxySettings.getNotProxyHosts();

            if(proxyHost != null && !proxyHost.equals("")) {
                nbGlobalSection.put("http-proxy-host", proxyHost);                     // NOI18N
                nbGlobalSection.put("http-proxy-port", Integer.toString(proxyPort));   // NOI18N
                if(!exceptions.equals("")) {
                    nbGlobalSection.put("http-proxy-exceptions", exceptions);   // NOI18N
                }

                // and the authentication
                Preferences prefs = org.openide.util.NbPreferences.root ().node ("org/netbeans/core");  // NOI18N
                boolean useAuth = prefs.getBoolean ("useProxyAuthentication", false);                   // NOI18N
                if(useAuth) {
                    String username = prefs.get ("proxyAuthenticationUsername", "");                    // NOI18N
                    String password = prefs.get ("proxyAuthenticationPassword", "");                    // NOI18N

                    nbGlobalSection.put("http-proxy-username", username);                               // NOI18N
                    nbGlobalSection.put("http-proxy-password", password);                               // NOI18N
                }
            }
        }
        // check if there are also some no proxy settings
        // we should get from the original svn servers file
        mergeNonProxyKeys(host, svnGlobalSection, nbGlobalSection);
    }

    private void mergeNonProxyKeys(String host, Ini.Section svnGlobalSection, Ini.Section nbGlobalSection) {                             
        if(svnGlobalSection != null) {
            // if there is a global section, than get the no proxy settings                                                                 
            mergeNonProxyKeys(svnGlobalSection, nbGlobalSection);
        }
        Ini.Section svnHostGroup = getServerGroup(host);
        if(svnHostGroup != null) {
            // if there is a section for the given host, than get the no proxy settings                                                                 
            mergeNonProxyKeys(svnHostGroup, nbGlobalSection);                
        }                                
    }
    
    private void mergeNonProxyKeys(Ini.Section source, Ini.Section target) {
        for (String key : source.keySet()) {
            if(!isProxyConfigurationKey(key)) {
                target.put(key, source.get(key));                                                
            }                    
        }
    }
    
    public void setExternalCommand(String tunnelName, String command) {
        Ini.Section tunnels = getSection(config, "tunnels", true);
        tunnels.put(tunnelName, command);
        storeIni(config, "config");                                                     // NOI18N
    }

    public String getExternalCommand(String tunnelName) {
        Ini.Section tunnels = getSection(config, "tunnels", true);
        String cmd = tunnels.get(tunnelName);
        return cmd != null ? cmd : "";        
    }
    
    private Ini.Section getSection(Ini ini, String key, boolean create) {
        Ini.Section section = ini.get(key);
        if(section == null) {
            return ini.add(key);
        }
        return section;
    }
    
    private void storeIni(Ini ini, String iniFile) {
        try {
            File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + iniFile));   // NOI18N
            file.getParentFile().mkdirs();
            ini.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);            
        }
    }    

    /**
     * Returns the miscellany/global-ignores setting from the config file.
     *
     * @return a list with the inore patterns
     *
     */
    public List<String> getGlobalIgnores() {
        Ini.Section miscellany = config.get("miscellany");                      // NOI18N
        if (miscellany != null) {
            String ignores = miscellany.get("global-ignores");                  // NOI18N
            if (ignores != null && ignores.trim().length() > 0) {
                return parseGlobalIgnores(ignores);
            }
        }
        return DEFAULT_GLOBAL_IGNORES;
    }

    public String getClientCertFile(String host) {
        return getMergeValue("ssl-client-cert-file", host);                     // NOI18N
    }

    public String getClientCertPassword(String host) {
        return getMergeValue("ssl-client-cert-password", host);                 // NOI18N    
    }
    
    private String getMergeValue(String key, String host) {
        Ini.Section group = getServerGroup(host);
        if(group != null) {
            return group.get(key);
        }
        group = svnServers.get(GLOBAL_SECTION);
        if(group != null) {
            return group.get(key);
        }
        return null;
    }
    
    private static List<String> parseGlobalIgnores(String ignores) {
        StringTokenizer st = new StringTokenizer(ignores, " ");                 // NOI18N
        List<String> ret = new ArrayList<String>(10);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.equals(""))                                              // NOI18N
                ret.add(entry);
        }
        return ret;
    }

    /**
     * Returns the path for the Sunbversion configuration dicectory used 
     * by the systems Subversion commandline client.
     *
     * @return the path
     *
     */ 
    public static String getUserConfigPath() {        
        
        //T9Y - user svn config files should be changable
        String t9yUserConfigPath = System.getProperty("netbeans.t9y.svn.user.config.path");
        if (t9yUserConfigPath != null && t9yUserConfigPath.length() > 0) {
            return t9yUserConfigPath;
        }
        
        if(Utilities.isUnix()) {
            String path = System.getProperty("user.home") ;                     // NOI18N
            return path + "/" + UNIX_CONFIG_DIR;                                // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_CONFIG_DIR;
        } 
        return "";                                                              // NOI18N
    }

    /**
     * Returns the path for the Sunbversion configuration directory used 
     * by the Netbeans Subversion module.
     *
     * @return the path
     *
     */ 
    public static String getNBConfigPath() {
        
        //T9Y - nb svn confing should be changable
        String t9yNbConfigPath = System.getProperty("netbeans.t9y.svn.nb.config.path");
        if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
            return t9yNbConfigPath;
        }
        
        String nbHome = System.getProperty("netbeans.user");                    // NOI18N
        return nbHome + "/config/svn/config/";                                  // NOI18N
    }
    
    /**
     * Returns the section from the <b>servers</b> config file used by the Subversion module which 
     * is holding the proxy settings for the given host
     *
     * @param host the host
     * @return the section holding the proxy settings for the given host
     */ 
    private Ini.Section getServerGroup(String host) {
        if(host == null || host.equals("")) {                                   // NOI18N
            return null;
        }
        Ini.Section groups = svnServers.get(GROUPS_SECTION);
        if(groups != null) {
            for (Iterator<String> it = groups.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                String value = groups.get(key);
                if(value != null) {     
                    value = value.trim();                    
                    if(value != null && match(value, host)) {
                        return svnServers.get(key);
                    }      
                }
            }
        }
        return null;
    }
       
    /**
     * Evaluates if the given hostaname or IP address is in the given value String.
     *
     * @param value the value String. A list of host names or IP addresses delimited by ",". 
     *                          (e.g 192.168.0.1,*.168.0.1, some.domain.com, *.anything.com, ...)
     * @param host the hostname or IP address
     * @return true if the host name or IP address was found in the values String, otherwise false.
     */
    private boolean match(String value, String host) {                    
        String[] values = value.split(",");                                     // NOI18N
        for (int i = 0; i < values.length; i++) {
            value = values[i].trim();

            if(value.equals("*") || value.equals(host) ) {                      // NOI18N
                return true;
            }

            int idx = value.indexOf("*");                                       // NOI18N
            if(idx > -1 && matchSegments(value, host) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluates if the given hostaname or IP address matches with the given value String representing 
     * a hostaname or IP adress with one or more "*" wildcards in it.
     *
     * @param value the value String. A host name or IP addresse with a "*" wildcard. (e.g *.168.0.1 or *.anything.com)
     * @param host the hostname or IP address
     * @return true if the host name or IP address matches with the values String, otherwise false.
     */
    private boolean matchSegments(String value, String host) {
        value = value.replace(".", "\\.");
        value = value.replace("*", ".*");
        Matcher m = Pattern.compile(value).matcher(host);
        return m.matches();
    }

    /**
     * Copies the given configuration file from the Subversion commandline client
     * configuration directory into the configuration directory used by the Netbeans Subversion module. </br>
     */
    private Ini copyConfigFileToIDEConfigDir(String fileName, IniFilePatcher patcher) {
        Ini systemIniFile = loadSystemIniFile(fileName);

        patcher.patch(systemIniFile);

        File file = FileUtil.normalizeFile(new File(getNBConfigPath() + "/" + fileName)); // NOI18N
        try {
            file.getParentFile().mkdirs();
            systemIniFile.store(FileUtils.createOutputStream(file));
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ; // should not happen
        }
        return systemIniFile;
    }

    /**
     * Loads the ini configuration file from the directory used by 
     * the Subversion commandline client. The settings are loaded and merged together in 
     * in the folowing order:
     * <ol>
     *  <li> The per-user INI files
     *  <li> The system-wide INI files
     * </ol> 
     *
     * @param fileName the file name
     * @return an Ini instance holding the cofiguration file. 
     */       
    private Ini loadSystemIniFile(String fileName) {
        // config files from userdir
        String filePath = getUserConfigPath() + "/" + fileName;                         // NOI18N
        File file = FileUtil.normalizeFile(new File(filePath));
        Ini system = null;
        try {            
            system = new Ini(new FileReader(file));
        } catch (FileNotFoundException ex) {
            // ignore
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ;
        }

        if(system == null) {
            system = new Ini();
            Subversion.LOG.warning("Could not load the file " + filePath + ". Falling back on svn defaults."); // NOI18N
        }
        
        Ini global = null;      
        try {
            global = new Ini(new FileReader(getGlobalConfigPath() + "/" + fileName));   // NOI18N
        } catch (FileNotFoundException ex) {
            // just doesn't exist - ignore
        } catch (IOException ex) {
            Subversion.LOG.log(Level.INFO, null, ex)     ;
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
     * Evaluates if the value stored under the key is a proxy setting value.
     *
     * @param key the key
     * @return true if the value stored under the key is a proxy setting value. Otherwise false
     */
    private boolean isProxyConfigurationKey(String key) {
        return key.equals("http-proxy-host")     || // NOI18N
               key.equals("http-proxy-port")     || // NOI18N
               key.equals("http-proxy-username") || // NOI18N
               key.equals("http-proxy-password") || // NOI18N
               key.equals("http-proxy-exceptions"); // NOI18N        
    }
    
    /**
     * Return the path for the systemwide command lines configuration directory 
     */
    private static String getGlobalConfigPath () {
        if(Utilities.isUnix()) {
            return "/etc/subversion";               // NOI18N
        } else if (Utilities.isWindows()){
            return WINDOWS_GLOBAL_CONFIG_DIR;
        } 
        return "";                                  // NOI18N
    }

    /**
     * Returns the value for the %APPDATA% env variable on windows
     *
     */
    private static String getAPPDATA() {
        String appdata = "";
        if(Utilities.isWindows()) {
            appdata = System.getenv("APPDATA");// NOI18N
        }
        return appdata!= null? appdata: "";
    }

    /**
     * Returns the value for the %ALLUSERSPROFILE% + the last foder segment from %APPDATA% env variables on windows
     *
     */
    private static String getGlobalAPPDATA() {
        if(Utilities.isWindows()) {
            String globalProfile = System.getenv("ALLUSERSPROFILE");                                // NOI18N
            if(globalProfile == null || globalProfile.trim().equals("")) {                          // NOI18N
                globalProfile = "";
            }
            String appdataPath = WINDOWS_USER_APPDATA;
            if(appdataPath == null || appdataPath.equals("")) {                                     // NOI18N
                return "";                                                                          // NOI18N
            }
            String appdata = "";                                                                    // NOI18N
            int idx = appdataPath.lastIndexOf("\\");                                                // NOI18N
            if(idx > -1) {
                appdata = appdataPath.substring(idx + 1);
                if(appdata.trim().equals("")) {                                                     // NOI18N
                    int previdx = appdataPath.lastIndexOf("\\", idx);                               // NOI18N
                    if(idx > -1) {
                        appdata = appdataPath.substring(previdx + 1, idx);
                    }
                }
            } else {
                return "";                                                                          // NOI18N
            }
            return globalProfile + "/" + appdata;                                                   // NOI18N
        }
        return "";                                                                                  // NOI18N
    }
        
}
