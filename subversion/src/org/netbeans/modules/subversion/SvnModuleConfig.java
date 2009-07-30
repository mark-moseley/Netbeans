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

package org.netbeans.modules.subversion;


import java.util.regex.Pattern;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.modules.subversion.options.AnnotationExpression;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Stores Subversion module configuration.
 *
 * @author Maros Sandor
 */
public class SvnModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";                        // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";                           // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";                              // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "svnExecBinary";                              // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";                           // NOI18N
    public static final String SAVE_PASSWORD                = "savePassword";                               // NOI18N
    private static final String FILE_SELECTOR_PREFIX        = "fileSelector";                               // NOI18N
    private static final String SEARCH_HISTORY_ALL_INFO     = "histAllInfo";                                // NOI18N
    
    public static final String KEY_RECENT_URL = "repository.recentURL";                                     // NOI18N
    private static final String SHOW_CHECKOUT_COMPLETED = "checkoutCompleted.showCheckoutCompleted";        // NOI18N  

    private static final String URL_EXP = "annotator.urlExp";                                               // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                                 // NOI18N
    
    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";                               // NOI18N
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";                                 // NOI18N


    private static final SvnModuleConfig INSTANCE = new SvnModuleConfig();    
        
    private Map<String, String[]> urlCredentials;
    
    public static SvnModuleConfig getDefault() {
        return INSTANCE;
    }
    
    private Set<String> exclusions;

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(SvnModuleConfig.class);
    }
    
    public boolean getShowCheckoutCompleted() {
        return getPreferences().getBoolean(SHOW_CHECKOUT_COMPLETED, true);
    }
    
    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }
    
    public boolean isExcludedFromCommit(String path) {
        return getCommitExclusions().contains(path);
    }
    
    /**
     * @param paths collection of paths, of File.getAbsolutePath()
     */
    public void addExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.addAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    public String getExecutableBinaryPath() {
        return (String) getPreferences().get(KEY_EXECUTABLE_BINARY, "");        
    }
    
    public void setExecutableBinaryPath(String path) {
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);        
    }

    public String getAnnotationFormat() {
        return (String) getPreferences().get(KEY_ANNOTATION_FORMAT, getDefaultAnnotationFormat());                
    }
    
    public String getDefaultAnnotationFormat() {
        return "[{" + Annotator.ANNOTATION_STATUS + "} {" + Annotator.ANNOTATION_FOLDER + "}]";
    }

    public void setAnnotationFormat(String annotationFormat) {
        getPreferences().put(KEY_ANNOTATION_FORMAT, annotationFormat);        
    }

    public void setShowCheckoutCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CHECKOUT_COMPLETED, bl);
    }
    
    public boolean getSavePassword() {
        return getPreferences().getBoolean(SAVE_PASSWORD, true);
    }

    public void setSavePassword(boolean bl) {
        getPreferences().putBoolean(SAVE_PASSWORD, bl);
    }

    public String getFileSelectorPreset(String hash) {
        return getPreferences().get(FILE_SELECTOR_PREFIX + "-" + hash, "");
    }

    public void setFileSelectorPreset(String hash, String path) {
        getPreferences().put(FILE_SELECTOR_PREFIX + "-" + hash, path);
    }
    
    public boolean getShowFileAllInfo() {
        return getPreferences().getBoolean(SEARCH_HISTORY_ALL_INFO, false);
    }

    public void setShowFileAllInfo(boolean value) {
        getPreferences().putBoolean(SEARCH_HISTORY_ALL_INFO, value);
    }

    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutputo(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }

    public RepositoryConnection getRepositoryConnection(String url) {
        if(url.endsWith("/")) url = url.substring(0, url.length() - 1);
        List<RepositoryConnection> rcs = getRecentUrls();
        for (Iterator<RepositoryConnection> it = rcs.iterator(); it.hasNext();) {
            RepositoryConnection rc = it.next();
            String rcUrl = rc.getUrl();
            if(rcUrl.endsWith("/")) rcUrl = rcUrl.substring(0, rcUrl.length() - 1);
            if(url.equals(rcUrl)) {
                return rc; // exact match
            }            
        }
        for (Iterator<RepositoryConnection> it = rcs.iterator(); it.hasNext();) {
            RepositoryConnection rc = it.next();
            String rcUrl = rc.getUrl();
            if(rcUrl.endsWith("/")) rcUrl = rcUrl.substring(0, rcUrl.length() - 1);
            if(rcUrl.startsWith(url)) {
                return rc; // try this
            }
        }
        return null;
    }            
    
    public void insertRecentUrl(RepositoryConnection rc) {        
        Preferences prefs = getPreferences();
        
        List<String> urlValues = Utils.getStringList(prefs, KEY_RECENT_URL);
        for (Iterator<String> it = urlValues.iterator(); it.hasNext();) {
            String rcOldString = it.next();
            RepositoryConnection rcOld =  RepositoryConnection.parse(rcOldString);
            if(rcOld.equals(rc)) {
                Utils.removeFromArray(prefs, KEY_RECENT_URL, rcOldString);
            }
        }
        handleCredentials(rc);
        String url = RepositoryConnection.getString(rc);
        if (!"".equals(url)) {                                          //NOI18N
            Utils.insert(prefs, KEY_RECENT_URL, url, -1);
        }
    }    

    public void setRecentUrls(List<RepositoryConnection> recentUrls) {
        List<String> urls = new ArrayList<String>(recentUrls.size());
        
        int idx = 0;
        for (Iterator<RepositoryConnection> it = recentUrls.iterator(); it.hasNext();) {
            idx++;
            RepositoryConnection rc = it.next();
            handleCredentials(rc);
            String url = RepositoryConnection.getString(rc);
            if (!"".equals(url)) {                                      //NOI18N
                urls.add(url);
            }
        }
        Preferences prefs = getPreferences();
        Utils.put(prefs, KEY_RECENT_URL, urls);
    }
    
    public List<RepositoryConnection> getRecentUrls() {
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, KEY_RECENT_URL);
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(urls.size());
        for (Iterator<String> it = urls.iterator(); it.hasNext();) {
            RepositoryConnection rc = RepositoryConnection.parse(it.next());
            if(getUrlCredentials().containsKey(rc.getUrl())) {
                String[] creds = getUrlCredentials().get(rc.getUrl());
                if(creds.length < 2) continue; //skip garbage
                rc = new RepositoryConnection(rc.getUrl(), creds[0], creds[1], rc.getExternalCommand(), rc.getSavePassword(), rc.getCertFile(), rc.getCertPassword());
            }
            ret.add(rc);
        }
        return ret;
    }
            
    public void setAnnotationExpresions(List<AnnotationExpression> exps) {
        List<String> urlExp = new ArrayList<String>(exps.size());
        List<String> annotationExp = new ArrayList<String>(exps.size());        
        
        int idx = 0;
        for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
            idx++;
            AnnotationExpression exp = it.next();            
            urlExp.add(exp.getUrlExp());
            annotationExp.add(exp.getAnnotationExp());            
        }

        Preferences prefs = getPreferences();
        Utils.put(prefs, URL_EXP, urlExp);        
        Utils.put(prefs, ANNOTATION_EXP, annotationExp);                
    }

    public List<AnnotationExpression> getAnnotationExpresions() {
        Preferences prefs = getPreferences();
        List<String> urlExp = Utils.getStringList(prefs, URL_EXP);
        List<String> annotationExp = Utils.getStringList(prefs, ANNOTATION_EXP);        
                
        List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(urlExp.size());                
        for (int i = 0; i < urlExp.size(); i++) {                                        
            ret.add(new AnnotationExpression(urlExp.get(i), annotationExp.get(i)));
        }
        if(ret.size() < 1) {
            ret = getDefaultAnnotationExpresions();
        }
        return ret;
    }

    public List<AnnotationExpression> getDefaultAnnotationExpresions() {
        List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(1);
        ret.add(new AnnotationExpression(".*/(branches|tags)/(.+?)/.*", "\\2"));     
        return ret;
    }
    
    // private methods ~~~~~~~~~~~~~~~~~~
    
    private synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }
    
    private static Pattern[] getDefaultFilePatterns() {
        return new Pattern [] {
                        Pattern.compile("cvslog\\..*"), // NOI18N
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
    
    private void handleCredentials(RepositoryConnection rc) {
        if(!rc.getSavePassword()) {
            getUrlCredentials().put(rc.getUrl(), new String[]{rc.getUsername(), rc.getPassword()});
        } else {
            getUrlCredentials().remove(rc.getUrl());
        }                      
    }    
    
    private Map<String, String[]> getUrlCredentials() {
        if(urlCredentials == null) {
            urlCredentials =  new HashMap<String, String[]>();
        }
        return urlCredentials;
    }    
    
}
