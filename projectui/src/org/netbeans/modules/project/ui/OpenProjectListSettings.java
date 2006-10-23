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

package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.filechooser.FileSystemView;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.NbPreferences;

/** SystemOption to store the list of open projects
 *  XXX Should be removed later and changed either to registry
 *      or something else
 */
public class OpenProjectListSettings {

    private static OpenProjectListSettings INSTANCE = new OpenProjectListSettings();
    
    private static final String OPEN_PROJECTS_URLS = "OpenProjectsURLs"; //NOI18N
    private static final String LAST_OPEN_PROJECT_DIR = "LastOpenProjectDir"; //NOI18N
    private static final String OPEN_SUBPROJECTS = "OpenSubprojects"; //NOI18N
    private static final String OPEN_AS_MAIN = "OpenAsMain"; //NOI18N
    private static final String MAIN_PROJECT_URL = "MainProjectURL"; //NOI18N
    private static final String RECENT_PROJECTS_URLS = "RecentProjectsURLs"; //NOI18N
    private static final String RECENT_PROJECTS_DISPLAY_NAMES = "RecentProjectsDisplayNames"; //NOI18N
    private static final String RECENT_PROJECTS_DISPLAY_ICONS = "RecentProjectsIcons"; //NOI18N
    private static final String PROP_PROJECTS_FOLDER = "projectsFolder"; //NOI18N
    private static final String RECENT_TEMPLATES = "recentlyUsedTemplates"; // NOI18N
    private static final String PROP_PROJECT_CATEGORY = "lastSelectedProjectCategory"; //NOI18N
    private static final String PROP_PROJECT_TYPE = "lastSelectedProjectType"; //NOI18N
    
    
    private OpenProjectListSettings() {
    }
    
    public static OpenProjectListSettings getInstance() {
        return INSTANCE;
    }
    
    public String displayName() {
        return NbBundle.getMessage (OpenProjectListSettings.class,"TXT_UISettings"); //NOI18N
    }        
    
    protected final String putProperty(String key, String value, boolean notify) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    protected final List<URL> getURLList(String key) {
        List<String> strs = getStringList(key);
        List<URL> toRet = new ArrayList<URL>();
        for (String val : strs) {
            try {
                toRet.add(new URL(val));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return toRet;
    }
    
    protected final List<String> getStringList(String key) {
        Preferences pref = getPreferences();
        int count = 0;
        String val = pref.get(key + "." + count, null);
        List<String> toRet = new ArrayList<String>();
        while (val != null) {
            toRet.add(val);
            count = count + 1;
            val = pref.get(key + "." + count, null);
        }
        return toRet;
    }
    
    protected final List<ExtIcon> getIconList(String key) {
        Preferences pref = getPreferences();
        int count = 0;
        byte[] val = pref.getByteArray(key + "." + count, null);
        List<ExtIcon> toRet = new ArrayList<ExtIcon>();
        while (val != null) {
            toRet.add(new ExtIcon(val));
            count = count + 1;
            val = pref.getByteArray(key + "." + count, null);
        }
        return toRet;
    }
    
    protected final void setIconList(String basekey, List<ExtIcon> list) throws IOException {
        assert list != null;
        Preferences pref = getPreferences();
        int count = 0;
        String key = basekey + "." + count;
        String val = pref.get(key, null);
        Iterator<ExtIcon> it = list.iterator();
        while (val != null || it.hasNext()) {
            if (it.hasNext()) {
                pref.putByteArray(key, it.next().getBytes());
            } else {
                pref.remove(key);
            }
            count = count + 1;
            key = basekey + "." + count;
            val = pref.get(key, null);
        }
    }
    
    
    protected final void setStringList(String basekey, List<String> list) {
        assert list != null;
        Preferences pref = getPreferences();
        int count = 0;
        String key = basekey + "." + count;
        String val = pref.get(key, null);
        Iterator<String> it = list.iterator();
        while (val != null || it.hasNext()) {
            if (it.hasNext()) {
                pref.put(key, it.next());
            } else {
                pref.remove(key);
            }
            count = count + 1;
            key = basekey + "." + count;
            val = pref.get(key, null);
        }
    }
    
    protected final void setURLList(String basekey, List<URL> list) {
        assert list != null;
        List<String> strs = new ArrayList<String>(list.size());
        for (URL url : list) {
            strs.add(url.toExternalForm());
        }
        setStringList(basekey, strs);
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.forModule(OpenProjectListSettings.class);
    }

    public List<URL> getOpenProjectsURLs() {
        return getURLList(OPEN_PROJECTS_URLS);
    }

    public void setOpenProjectsURLs( List<URL> list ) {
        setURLList( OPEN_PROJECTS_URLS, list);
    }
    
    public boolean isOpenSubprojects() {        
        return getPreferences().getBoolean( OPEN_SUBPROJECTS, true);
    }
    
    public void setOpenSubprojects( boolean openSubprojects ) {
        getPreferences().putBoolean(OPEN_SUBPROJECTS, openSubprojects);
    }
    
    public boolean isOpenAsMain() {        
        return getPreferences().getBoolean( OPEN_AS_MAIN, true);
    }
    
    public void setOpenAsMain( boolean openAsMain ) {
        getPreferences().putBoolean(OPEN_AS_MAIN, openAsMain);
    }
    
    public URL getMainProjectURL() {
        String str = getProperty(MAIN_PROJECT_URL);
        if (str != null) {
            try {
                return new URL(str);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    public void setMainProjectURL( URL mainProjectURL ) {
        putProperty( MAIN_PROJECT_URL, mainProjectURL != null ? mainProjectURL.toString() : null, true );
    }
    
    public String getLastOpenProjectDir() {
        String result = getProperty( LAST_OPEN_PROJECT_DIR );
        if (result == null) {
            result = getProjectsFolder().getAbsolutePath();
        }
        return result;
    }
    
    public void setLastOpenProjectDir( String path ) {
        putProperty( LAST_OPEN_PROJECT_DIR, path, true  );
    }
    
    public List<URL> getRecentProjectsURLs() {
        return getURLList(RECENT_PROJECTS_URLS);
    }
    
    public List<String> getRecentProjectsDisplayNames() {
        return getStringList(RECENT_PROJECTS_DISPLAY_NAMES);
    }
    
    public List<ExtIcon> getRecentProjectsIcons() {
        return getIconList(RECENT_PROJECTS_DISPLAY_ICONS);
    }
    
    public void setRecentProjectsURLs( List<URL> list ) {
        setURLList(RECENT_PROJECTS_URLS, list);
    }
    
    public void setRecentProjectsDisplayNames(List<String> list) {
        setStringList(RECENT_PROJECTS_DISPLAY_NAMES, list);
    }
    
    public void setRecentProjectsIcons(List<ExtIcon> list) {
        try {
            setIconList(RECENT_PROJECTS_DISPLAY_ICONS, list);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public File getProjectsFolder () {
        String result = getProperty (PROP_PROJECTS_FOLDER);
        if (result == null) {
            // property for overriding default projects dir location
            String userPrjDir = System.getProperty("netbeans.projects.dir"); // NOI18N
            if (userPrjDir != null) {
                File f = new File(userPrjDir);
                if (f.exists() && f.isDirectory()) {
                    return FileUtil.normalizeFile(f);
                }
            }
            File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();
            if (defaultDir != null && defaultDir.exists() && defaultDir.isDirectory()) {
                String nbPrjDirName = NbBundle.getMessage(OpenProjectListSettings.class, "DIR_NetBeansProjects");
                File nbPrjDir = new File(defaultDir, nbPrjDirName);
                if (nbPrjDir.exists() && nbPrjDir.canWrite()) {
                    return nbPrjDir;
                } else {
                    boolean created = nbPrjDir.mkdir();
                    if (created) return nbPrjDir; 
                }
            }
            result = System.getProperty("user.home");   //NOI18N
        }
        return FileUtil.normalizeFile(new File(result));
    }

    public void setProjectsFolder (File folder) {
        if (folder == null) {
            putProperty(PROP_PROJECTS_FOLDER, (String)null, true);
        }
        else {
            putProperty(PROP_PROJECTS_FOLDER, folder.getAbsolutePath(), true);
        }
    }
    
    public List<String> getRecentTemplates() {        
        return getStringList(RECENT_TEMPLATES);
    }
    
    public void setRecentTemplates( List<String> templateNames ) {
        setStringList( RECENT_TEMPLATES, templateNames );
    }
    
    public String getLastSelectedProjectCategory () {
        return getProperty (PROP_PROJECT_CATEGORY);
    }
    
    public void setLastSelectedProjectCategory (String category) {
        putProperty(PROP_PROJECT_CATEGORY,category,true);
    }
    
    public String getLastSelectedProjectType () {
        return getProperty (PROP_PROJECT_TYPE);
    }
    
    public void setLastSelectedProjectType (String type) {
        putProperty(PROP_PROJECT_TYPE,type,true);
    }

}
