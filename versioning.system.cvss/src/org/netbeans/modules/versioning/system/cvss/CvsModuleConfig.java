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
 */

package org.netbeans.modules.versioning.system.cvss;

import java.util.regex.Pattern;
import java.util.*;
import java.lang.String;
import java.util.prefs.Preferences;

import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxyDescriptor;
import org.netbeans.lib.cvsclient.CVSRoot;

/**
 * Stores CVS module configuration.
 *
 * @author Maros Sandor
 */
public class CvsModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";  // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";  // NOI18N
    public static final String PROP_TEXT_ANNOTATIONS_FORMAT = "textAnnotations";  // NOI18N
    public static final String ROOTS_CONFIG = "rootsConfig";  // NOI18N
    
    private static final String FIELD_SEPARATOR = "<~>";
    
    private static final CvsModuleConfig INSTANCE = new CvsModuleConfig();

    public static CvsModuleConfig getDefault() {
        return INSTANCE;
    }

    private Set<String> exclusions;

    private Map<String, RootSettings> rootsMap;
    
    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }

    public boolean isExcludedFromCommit(String path) {
        return getCommitExclusions().contains(path);
    }
    
    /**
     * @param path File.getAbsolutePath()
     */
    public void addExclusionPath(String path) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.add(path)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    /**
     * @param path File.getAbsolutePath()
     */
    public void removeExclusionPath(String path) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.remove(path)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }
    
    // clients code ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Loads value stored by {@link #setProxyFor}. By default
     * it returns {@link org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxyDescriptor#SYSTEM}.
     *
     * @return a proxy descriptor, never <code>null</code>
     */
    public synchronized ProxyDescriptor getProxyFor(CVSRoot root) {
        ProxyDescriptor proxyDescriptor = ProxyDescriptor.SYSTEM;
        if (root != null) {        
            Map<String, RootSettings> rootsMap = getRootsMap();
            String rootString = root.toString();
            RootSettings rootSettings = (RootSettings) rootsMap.get(rootString);
            if (rootSettings != null && rootSettings.proxyDescriptor != null) {
                proxyDescriptor = rootSettings.proxyDescriptor;
            }
        }
        return proxyDescriptor;
    }

    public synchronized void setProxyFor(CVSRoot root, ProxyDescriptor proxyDescriptor) {
        Map<String, RootSettings> map = getRootsMap();
        String key = root.toString();
        RootSettings settings = (RootSettings) map.get(key);
        if (settings == null) {
            settings = new RootSettings();
        }
        settings.proxyDescriptor = proxyDescriptor;
        map.put(key, settings);

        storeRootsMap();
    }

    public synchronized boolean hasExtSettingsFor(CVSRoot root) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> rootsMap = getRootsMap();
        String rootString = root.toString();
        RootSettings rootSettings = (RootSettings) rootsMap.get(rootString);
        if (rootSettings != null) {
            ExtSettings extSettings = rootSettings.extSettings;
            return extSettings != null;
        }
        return false;
    }

    /**
     * Loads a value set by {@link #setExtSettingsFor}.
     *
     * @param root cvs root with <code>:ext:</code> connection method
     * @return  additional ext settings or their default value
     */
    public synchronized ExtSettings getExtSettingsFor(CVSRoot root) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> rootsMap = getRootsMap();
        String rootString = root.toString();
        RootSettings rootSettings = (RootSettings) rootsMap.get(rootString);
        if (rootSettings != null) {
            ExtSettings extSettings = rootSettings.extSettings;
            if (extSettings != null) {
                if (extSettings.extUseInternalSsh == false && extSettings.extCommand == null) {
                    extSettings.extCommand = System.getProperty("Env-CVS_RSH"); // NOI18N
                }
                return extSettings;
            }
        }

        // hardcoded default value
        ExtSettings defaults = new ExtSettings();
        defaults.extRememberPassword = false;
        defaults.extCommand = System.getProperty("Env-CVS_RSH"); // NOI18N
        defaults.extUseInternalSsh = true;
        defaults.extPassword = null;
        return defaults;
    }

    public synchronized void setExtSettingsFor(CVSRoot root, ExtSettings extSettings) {
        assert "ext".equals(root.getMethod());  // NOI18N
        Map<String, RootSettings> map = getRootsMap();
        String key = root.toString();
        RootSettings settings = (RootSettings) map.get(key);
        if (settings == null) {
            settings = new RootSettings();
        }
        settings.extSettings = extSettings;
        map.put(key, settings);

        storeRootsMap();
    }
    
    private Map<String, RootSettings> getRootsMap() {
        if (rootsMap == null) {
            rootsMap = loadRootsMap();
        }
        return rootsMap;
    }

    private Map<String, RootSettings> loadRootsMap() {
        List<String> smap = Utils.getStringList(getPreferences(), "cvsRootSettings");
        Map<String, RootSettings> map = new HashMap<String, RootSettings>(smap.size());
        for (String s : smap) {
            String [] fields = s.split(FIELD_SEPARATOR);
            if (fields.length >= 8) {
                RootSettings rs = new RootSettings();
                map.put(fields[0], rs);
                int type = Integer.parseInt(fields[1]);
                int port = Integer.parseInt(fields[2]);
                String host = fields[3].length() == 0 ? null : fields[3];
                String userName = fields[4].length() == 0 ? null : fields[4];
                String password = fields[5].length() == 0 ? null : fields[5];
                String description = fields[6].length() == 0 ? null : fields[6];
                boolean system = Boolean.valueOf(fields[7]);
                rs.proxyDescriptor = new ProxyDescriptor(type, host, port, userName, password, description, system);
                if (fields.length >= 11) {
                    ExtSettings es = new ExtSettings();
                    rs.extSettings = es;
                    es.extUseInternalSsh = Boolean.valueOf(fields[8]);
                    es.extRememberPassword = Boolean.valueOf(fields[9]);
                    es.extCommand = fields[10];
                    if (fields.length >= 12) {
                        es.extPassword = fields[11];
                    }
                }
            }
        }
        return map;
    }

    private void storeRootsMap() {
        List<String> smap = new ArrayList<String>();
        for (Map.Entry<String, RootSettings> entry : rootsMap.entrySet()) {
            StringBuffer es = new StringBuffer(100);
            es.append(entry.getKey());
            RootSettings settings = entry.getValue();
            if (settings.proxyDescriptor != null) {
                es.append(FIELD_SEPARATOR);
                es.append(settings.proxyDescriptor.getType());
                es.append(FIELD_SEPARATOR);
                es.append(settings.proxyDescriptor.getPort());
                es.append(FIELD_SEPARATOR);
                if (settings.proxyDescriptor.getHost() != null) es.append(settings.proxyDescriptor.getHost());
                es.append(FIELD_SEPARATOR);
                if (settings.proxyDescriptor.getUserName() != null) es.append(settings.proxyDescriptor.getUserName());
                es.append(FIELD_SEPARATOR);
                if (settings.proxyDescriptor.getPassword() != null) es.append(settings.proxyDescriptor.getPassword());
                es.append(FIELD_SEPARATOR);
                if (settings.proxyDescriptor.getDescription() != null) es.append(settings.proxyDescriptor.getDescription());
                es.append(FIELD_SEPARATOR);
                es.append(settings.proxyDescriptor.isSystemProxyDescriptor());
            }
            if (settings.extSettings != null) {
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extUseInternalSsh);
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extRememberPassword);
                es.append(FIELD_SEPARATOR);
                es.append(settings.extSettings.extCommand);
                if (settings.extSettings.extRememberPassword) {
                    es.append(FIELD_SEPARATOR);
                    es.append(settings.extSettings.extPassword);
                }
            }
            smap.add(es.toString());
        }
        Utils.put(getPreferences(), "cvsRootSettings", smap);
    }
    
    /**
     * Gets the backing store of module preferences, use this to store and retrieve simple properties and stored values. 
     *  
     * @return Preferences backing store
     */
    public Preferences getPreferences() {
        return NbPreferences.forModule(CvsModuleConfig.class);
    }
    
    // private methods ~~~~~~~~~~~~~~~~~~
       
    private synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }
            
    private Pattern[] getDefaultFilePatterns() {
        return new Pattern [] {
                        Pattern.compile("cvslog\\..*"),  // NOI18N
                        Pattern.compile("\\.make\\.state"), // NOI18N
                        Pattern.compile("\\.nse_depinfo"), // NOI18N
                        Pattern.compile(".*~"), // NOI18N
                        Pattern.compile("#.*"), // NOI18N
                        Pattern.compile("\\.#.*"), // NOI18N
                        Pattern.compile(",.*"), // NOI18N
                        Pattern.compile("_\\$.*"), // NOI18N
                        Pattern.compile(".*\\$"), // NOI18N
                        Pattern.compile(".*\\.old"), // NOI18N
                        Pattern.compile(".*\\.bak"), // NOI18N
                        Pattern.compile(".*\\.BAK"), // NOI18N
                        Pattern.compile(".*\\.orig"), // NOI18N
                        Pattern.compile(".*\\.rej"), // NOI18N
                        Pattern.compile(".*\\.del-.*"), // NOI18N
                        Pattern.compile(".*\\.a"), // NOI18N
                        Pattern.compile(".*\\.olb"), // NOI18N
                        Pattern.compile(".*\\.o"), // NOI18N
                        Pattern.compile(".*\\.obj"), // NOI18N
                        Pattern.compile(".*\\.so"), // NOI18N
                        Pattern.compile(".*\\.exe"), // NOI18N
                        Pattern.compile(".*\\.Z"), // NOI18N
                        Pattern.compile(".*\\.elc"), // NOI18N
                        Pattern.compile(".*\\.ln"), // NOI18N
                    };
    }

    /**
     * Holds associated settings.
     */
    private final static class RootSettings {

        private ProxyDescriptor proxyDescriptor;

        private ExtSettings extSettings;
    }

    /** External method additional settings */
    public final static class ExtSettings {

        public boolean extUseInternalSsh;

        /** Makes sense if extUseInternalSsh == true */
        public boolean extRememberPassword;

        /** Makes sense if extUseInternalSsh == true */
        public String extPassword;

        /** Makes sense if extUseInternalSsh == false */
        public String extCommand;
    }
}

