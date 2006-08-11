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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import javax.swing.filechooser.FileSystemView;
import org.openide.filesystems.FileUtil;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/** SystemOption to store the list of open projects
 *  XXX Should be removed later and changed either to registry
 *      or something else
 */
public class OpenProjectListSettings extends SystemOption {

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
    
    // PERSISTENCE
    private static final long serialVersionUID = 8754987489474L;
    
    
    public static OpenProjectListSettings getInstance() {
        return SystemOption.findObject(OpenProjectListSettings.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage (OpenProjectListSettings.class,"TXT_UISettings"); //NOI18N
    }        

    public List<URL> getOpenProjectsURLs() {
        List list = (List) getProperty(OPEN_PROJECTS_URLS);
        return list == null ? new ArrayList<URL>(3) : NbCollections.checkedListByCopy(list, URL.class, true);
    }

    public void setOpenProjectsURLs( List<URL> list ) {
        putProperty( OPEN_PROJECTS_URLS, list, true  );
    }
    
    public boolean isOpenSubprojects() {        
        Boolean value = (Boolean)getProperty( OPEN_SUBPROJECTS );        
        return value == null ? true : value;
    }
    
    public void setOpenSubprojects( boolean openSubprojects ) {
        putProperty(OPEN_SUBPROJECTS, openSubprojects, true);
    }
    
    public boolean isOpenAsMain() {        
        Boolean value = (Boolean)getProperty( OPEN_AS_MAIN );        
        return value == null ? true : value;
    }
    
    public void setOpenAsMain( boolean openAsMain ) {
        putProperty(OPEN_AS_MAIN, openAsMain, true);
    }
    
    public URL getMainProjectURL() {
        return (URL)getProperty( MAIN_PROJECT_URL );
    }
    
    public void setMainProjectURL( URL mainProjectURL ) {
        putProperty( MAIN_PROJECT_URL, mainProjectURL, true  );
    }
    
    public String getLastOpenProjectDir() {
        String result = (String)getProperty( LAST_OPEN_PROJECT_DIR );
        if (result == null) {
            result = getProjectsFolder().getAbsolutePath();
        }
        return result;
    }
    
    public void setLastOpenProjectDir( String path ) {
        putProperty( LAST_OPEN_PROJECT_DIR, path, true  );
    }
    
    public List<URL> getRecentProjectsURLs() {
        List list = (List) getProperty(RECENT_PROJECTS_URLS);
        return list == null ? new ArrayList<URL>(5) : NbCollections.checkedListByCopy(list, URL.class, true);
    }
    
    public List<String> getRecentProjectsDisplayNames() {
        List list = (List) getProperty(RECENT_PROJECTS_DISPLAY_NAMES);
        return list == null ? new ArrayList<String>(5) : NbCollections.checkedListByCopy(list, String.class, true);
    }
    
    public List<ExtIcon> getRecentProjectsIcons() {
        List list = (List) getProperty(RECENT_PROJECTS_DISPLAY_ICONS);
        return list == null ? new ArrayList<ExtIcon>(5) : NbCollections.checkedListByCopy(list, ExtIcon.class, true);
    }
    
    public void setRecentProjectsURLs( List<URL> list ) {
        putProperty( RECENT_PROJECTS_URLS, list, true  );
    }
    
    public void setRecentProjectsDisplayNames(List<String> list) {
        putProperty(RECENT_PROJECTS_DISPLAY_NAMES, list, true);
    }
    
    public void setRecentProjectsIcons(List<ExtIcon> list) {
        putProperty(RECENT_PROJECTS_DISPLAY_ICONS, list, true);
    }
    
    public File getProjectsFolder () {
        String result = (String) this.getProperty (PROP_PROJECTS_FOLDER);
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
            this.putProperty(PROP_PROJECTS_FOLDER,null);
        }
        else {
            this.putProperty(PROP_PROJECTS_FOLDER, folder.getAbsolutePath());
        }
    }
    
    public List<String> getRecentTemplates() {        
        List list = (List) getProperty(RECENT_TEMPLATES);
        return list == null ? new ArrayList<String>(100) : NbCollections.checkedListByCopy(list, String.class, true);
    }
    
    public void setRecentTemplates( List<String> templateNames ) {
        putProperty( RECENT_TEMPLATES, templateNames, true  );
    }
    
    public String getLastSelectedProjectCategory () {
        return (String) getProperty (PROP_PROJECT_CATEGORY);
    }
    
    public void setLastSelectedProjectCategory (String category) {
        putProperty(PROP_PROJECT_CATEGORY,category,true);
    }
    
    public String getLastSelectedProjectType () {
        return (String) getProperty (PROP_PROJECT_TYPE);
    }
    
    public void setLastSelectedProjectType (String type) {
        putProperty(PROP_PROJECT_TYPE,type,true);
    }

}
