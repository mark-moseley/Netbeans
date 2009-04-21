/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.mercurial;


import java.awt.EventQueue;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
//import org.netbeans.modules.mercurial.options.AnnotationExpression;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Stores Mercurial module configuration.
 *
 * @author Padraig O'Briain
 */
public class HgModuleConfig {
    
    public static final String PROP_IGNORED_FILEPATTERNS    = "ignoredFilePatterns";                        // NOI18N
    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";                           // NOI18N
    public static final String PROP_DEFAULT_VALUES          = "defaultValues";                              // NOI18N
    public static final String PROP_RUN_VERSION             = "runVersion";                                 // NOI18N
    public static final String KEY_EXECUTABLE_BINARY        = "hgExecBinary";                              // NOI18N
    public static final String KEY_EXPORT_FILENAME          = "hgExportFilename";                          // NOI18N
    public static final String KEY_EXPORT_FOLDER            = "hgExportFolder";                          // NOI18N
    public static final String KEY_IMPORT_FOLDER            = "hgImportFolder";                          // NOI18N
    public static final String KEY_ANNOTATION_FORMAT        = "annotationFormat";                           // NOI18N
    public static final String SAVE_PASSWORD                = "savePassword";                               // NOI18N
    public static final String KEY_BACKUP_ON_REVERTMODS = "backupOnRevert";                               // NOI18N
    public static final String KEY_SHOW_HITORY_MERGES = "showHistoryMerges";                               // NOI18N
    private static final String KEY_SHOW_FILE_INFO = "showFileInfo";        // NOI18N
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";        // NOI18N

    private static final String RECENT_URL = "repository.recentURL";                                        // NOI18N
    private static final String SHOW_CLONE_COMPLETED = "cloneCompleted.showCloneCompleted";        // NOI18N  

    private static final String SET_MAIN_PROJECT = "cloneCompleted.setMainProject";        // NOI18N  

    private static final String URL_EXP = "annotator.urlExp";                                               // NOI18N
    private static final String ANNOTATION_EXP = "annotator.annotationExp";                                 // NOI18N
    
    public static final String TEXT_ANNOTATIONS_FORMAT_DEFAULT = "{DEFAULT}";                               // NOI18N           

    private static final String DEFAULT_EXPORT_FILENAME = "%b_%r_%h";                                  // NOI18N
    private static final HgModuleConfig INSTANCE = new HgModuleConfig();    
    
    private static String userName;

    public static HgModuleConfig getDefault() {
        return INSTANCE;
    }
    
    private Set<String> exclusions;

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(HgModuleConfig.class);
    }
    
    public boolean getShowCloneCompleted() {
        return getPreferences().getBoolean(SHOW_CLONE_COMPLETED, true);
    }
    
    public boolean getSetMainProject() {
        return getPreferences().getBoolean(SET_MAIN_PROJECT, true);
    }
    
    public Pattern [] getIgnoredFilePatterns() {
        return getDefaultFilePatterns();
    }

    public boolean getShowFileInfo() {
        return getPreferences().getBoolean(KEY_SHOW_FILE_INFO, false);
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
        return (String) getPreferences().get(KEY_EXECUTABLE_BINARY, ""); // NOI18N
    }
    public boolean getBackupOnRevertModifications() {
        return getPreferences().getBoolean(KEY_BACKUP_ON_REVERTMODS, true);
    }
    
    public void setBackupOnRevertModifications(boolean bBackup) {
        getPreferences().putBoolean(KEY_BACKUP_ON_REVERTMODS, bBackup);
    }
    
    public boolean getShowHistoryMerges() {
        return getPreferences().getBoolean(KEY_SHOW_HITORY_MERGES, true);
    }

    public void setShowHistoryMerges(boolean bShowMerges) {
        getPreferences().putBoolean(KEY_SHOW_HITORY_MERGES, bShowMerges);
    }
    
    public void setShowFileInfo(boolean info) {
        getPreferences().putBoolean(KEY_SHOW_FILE_INFO, info);
    }

    public void setExecutableBinaryPath(String path) {
        if(Utilities.isWindows() && path.endsWith(HgCommand.HG_COMMAND + HgCommand.HG_WINDOWS_EXE)){
            path = path.substring(0, path.length() - (HgCommand.HG_COMMAND + HgCommand.HG_WINDOWS_EXE).length());
        }else  if(path.endsWith(HgCommand.HG_COMMAND)){
            path = path.substring(0, path.length() - HgCommand.HG_COMMAND.length());            
        }
        getPreferences().put(KEY_EXECUTABLE_BINARY, path);
    }

    public String getExportFolder() {
        return (String) getPreferences().get(KEY_EXPORT_FOLDER, System.getProperty("user.home")); // NOI18N
    }
    
    public void setExportFolder(String path) {
        getPreferences().put(KEY_EXPORT_FOLDER, path);
    }

    public String getImportFolder() {
        return (String) getPreferences().get(KEY_IMPORT_FOLDER, System.getProperty("user.home")); // NOI18N
    }
    
    public void setImportFolder(String path) {
        getPreferences().put(KEY_IMPORT_FOLDER, path);
    }

    public String getExportFilename() {
        String str = (String) getPreferences().get(KEY_EXPORT_FILENAME, ""); // NOI18N
        if (str.trim().length() == 0) str = DEFAULT_EXPORT_FILENAME;
        return str;
    }
    
    public void setExportFilename(String path) {
        getPreferences().put(KEY_EXPORT_FILENAME, path);
    }

    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutput(boolean value) {
        getPreferences().putBoolean(AUTO_OPEN_OUTPUT_WINDOW, value);
    }


    /**
     * This method returns the username specified in $HOME/.hgrc
     * or /etc/mercurial/hgrc 
     * or a default username if none is found.
     */
    public String getSysUserName() {
        userName = HgConfigFiles.getSysInstance().getSysUserName();
        if (userName.length() == 0) {
            String userId = System.getProperty("user.name"); // NOI18N
            String hostName;
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception ex) {
                hostName = "localhost"; //NOI18N
            }
            userName = userId + "@" + hostName; // NOI18N
        }
        return userName;
    }

    public String getSysPushPath() {
        return HgConfigFiles.getSysInstance().getSysPushPath();
    }
    
    public String getSysPullPath() {
        return HgConfigFiles.getSysInstance().getSysPullPath();
    }

    public void addHgkExtension() throws IOException {
        HgConfigFiles hcf = HgConfigFiles.getSysInstance();
        if (hcf.getException() == null) {
            hcf.setProperty("hgext.hgk", "");
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": Cannot set hgext.hgk property"); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }
    
    public void setUserName(String name) throws IOException {
        HgConfigFiles hcf = HgConfigFiles.getSysInstance();
        if (hcf.getException() == null) {
            hcf.setUserName(name);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": Cannot set username property"); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public Boolean isUserNameValid(String name) {
        if (userName == null) getSysUserName();
        if (name.equals(userName)) return true;
        if (name.length() == 0) return true;
        return HgMail.isUserNameValid(name);
    }

    public Boolean isExecPathValid(String name) {
        if (name.length() == 0) return true;
        File file = new File(name, HgCommand.HG_COMMAND); // NOI18N
        // I would like to call canExecute but that requires Java SE 6.
        if(file.exists() && file.isFile()) return true;
        file = new File(name, HgCommand.HG_COMMAND + HgCommand.HG_WINDOWS_EXE); // NOI18N
        return file.exists() && file.isFile();
    }

    /**
     *
     * @param file
     * @return null in case of a parsing error
     */
    public Properties getProperties(File file) {
        Properties props = new Properties();
        HgConfigFiles hgconfig = new HgConfigFiles(file);
        if (hgconfig.getException() != null) {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot load configuration file"); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hgconfig.getException());
            notifyParsingError();
            return null;
        }
        String name = hgconfig.getUserName(false);
        if (name.length() == 0) 
            name = getSysUserName();
        if (name.length() > 0) 
            props.setProperty("username", name); // NOI18N
        else
            props.setProperty("username", ""); // NOI18N
        
        name = hgconfig.getDefaultPull(false);
        if (name.length() == 0) 
            name = getSysPullPath();
        if (name.length() > 0) 
            props.setProperty("default-pull", name); // NOI18N
        else
            props.setProperty("default-pull", ""); // NOI18N
        
        name = hgconfig.getDefaultPush(false);
        if (name.length() == 0) 
            name = getSysPushPath();
        if (name.length() > 0) 
            props.setProperty("default-push", name); // NOI18N
        else
            props.setProperty("default-push", ""); // NOI18N
        
        return props;
    }

    public void clearProperties(File file, String section) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.clearProperties(section);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot clear properties for {0}", new File[] {file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void removeProperty(File file, String section, String name) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.removeProperty(section, name);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot remove property {0} for {1}", new Object[] {name, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(File file, String name, String value) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(name, value);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(File file, String section, String name, String value, boolean allowEmpty) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(section, name, value, allowEmpty);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    public void setProperty(File file, String section, String name, String value) throws IOException {
        HgConfigFiles hcf = getHgConfigFiles(file);
        if (hcf.getException() == null) {
            hcf.setProperty(section, name, value);
        } else {
            Mercurial.LOG.log(Level.WARNING, this.getClass().getName() + ": cannot set property {0}:{1} for {2}", new Object[] {name, value, file}); // NOI18N
            Mercurial.LOG.log(Level.INFO, null, hcf.getException());
            throw hcf.getException();
        }
    }

    /*
     * Get all properties for a particular section
     */
    public Properties getProperties(File file, String section) {
        return getHgConfigFiles(file).getProperties(section);
    }

    private HgConfigFiles getHgConfigFiles(File file) {
        if (file == null) {
            return HgConfigFiles.getSysInstance();
        } else {
            return new HgConfigFiles(file); 
        }
    }

    public String getAnnotationFormat() {
        return (String) getPreferences().get(KEY_ANNOTATION_FORMAT, getDefaultAnnotationFormat());                
    }
    
    public String getDefaultAnnotationFormat() {
        return "[{" + MercurialAnnotator.ANNOTATION_STATUS + "} {" + MercurialAnnotator.ANNOTATION_FOLDER + "}]"; // NOI18N
    }

    public void setAnnotationFormat(String annotationFormat) {
        getPreferences().put(KEY_ANNOTATION_FORMAT, annotationFormat);        
    }

    public boolean getSavePassword() {
        return getPreferences().getBoolean(SAVE_PASSWORD, true);
    }

    public void setSavePassword(boolean bl) {
        getPreferences().putBoolean(SAVE_PASSWORD, bl);
    }

    public void setShowCloneCompleted(boolean bl) {
        getPreferences().putBoolean(SHOW_CLONE_COMPLETED, bl);
    }
    
    public void setSetMainProject(boolean bl) {
        getPreferences().putBoolean(SET_MAIN_PROJECT, bl);
    }
    
    public void insertRecentUrl(RepositoryConnection rc) {        
        Preferences prefs = getPreferences();
        
        for (String rcOldString : Utils.getStringList(prefs, RECENT_URL)) {
            RepositoryConnection rcOld;
            try {
                rcOld = RepositoryConnection.parse(rcOldString);
            } catch (URISyntaxException ex) {
                Logger.global.throwing(getClass().getName(),
                                       "insertRecentUrl",               //NOI18N
                                       ex);
                continue;
            }
            if(rcOld.equals(rc)) {
                Utils.removeFromArray(prefs, RECENT_URL, rcOldString);
            }
        }        
        Utils.insert(prefs, RECENT_URL, RepositoryConnection.getString(rc), -1);                
    }    

    public void setRecentUrls(List<RepositoryConnection> recentUrls) {
        List<String> urls = new ArrayList<String>(recentUrls.size());
       
        int idx = 0;
        for (Iterator<RepositoryConnection> it = recentUrls.iterator(); it.hasNext();) {
            idx++;
            RepositoryConnection rc = it.next();
            urls.add(RepositoryConnection.getString(rc));            
        }
        Preferences prefs = getPreferences();
        Utils.put(prefs, RECENT_URL, urls);            
    }
    
    public List<RepositoryConnection> getRecentUrls() {
        Preferences prefs = getPreferences();
        List<String> urls = Utils.getStringList(prefs, RECENT_URL);
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(urls.size());
        for (String urlString : urls) {
            try {
                ret.add(RepositoryConnection.parse(urlString));
            } catch (URISyntaxException ex) {
                Logger.global.throwing(getClass().getName(),
                                       "getRecentUrls",                 //NOI18N
                                       ex);
            }
        }
        return ret;
    }
            
    //public void setAnnotationExpresions(List<AnnotationExpression> exps) {
    //    List<String> urlExp = new ArrayList<String>(exps.size());
    //    List<String> annotationExp = new ArrayList<String>(exps.size());        
        
    //    int idx = 0;
    //    for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
    //        idx++;
    //        AnnotationExpression exp = it.next();            
    //        urlExp.add(exp.getUrlExp());
    //        annotationExp.add(exp.getAnnotationExp());            
    //    }

    //    Preferences prefs = getPreferences();
    //    Utils.put(prefs, URL_EXP, urlExp);        
    //    Utils.put(prefs, ANNOTATION_EXP, annotationExp);                
    //}

    //public List<AnnotationExpression> getAnnotationExpresions() {
    //    Preferences prefs = getPreferences();
    //    List<String> urlExp = Utils.getStringList(prefs, URL_EXP);
    //    List<String> annotationExp = Utils.getStringList(prefs, ANNOTATION_EXP);        
              
    //    List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(urlExp.size());                
    //    for (int i = 0; i < urlExp.size(); i++) {                                        
    //        ret.add(new AnnotationExpression(urlExp.get(i), annotationExp.get(i)));
    //    }
    //    if(ret.size() < 1) {
    //        ret = getDefaultAnnotationExpresions();
    //    }
    //    return ret;
    //}

    //public List<AnnotationExpression> getDefaultAnnotationExpresions() {
    //    List<AnnotationExpression> ret = new ArrayList<AnnotationExpression>(1);
    //    ret.add(new AnnotationExpression(".*/(branches|tags)/(.+?)/.*", "\\2"));     // NOI18N 
    //    return ret;
    //}
    
    // TODO: persist state

    private TableSorter importTableSorter;
    private TableSorter commitTableSorter;
    
    public TableSorter getImportTableSorter() {
        return importTableSorter;        
    }

    public void setImportTableSorter(TableSorter sorter) {
        importTableSorter = sorter;        
    }

    public TableSorter getCommitTableSorter() {
        return commitTableSorter;
    }

    public void setCommitTableSorter(TableSorter sorter) {
        commitTableSorter = sorter;
    }

    /**
     * Notifies user of parsing error.
     */
    public static void notifyParsingError() {
        NotifyDescriptor nd = new NotifyDescriptor(
                NbBundle.getMessage(HgModuleConfig.class, "MSG_ParsingError"), // NOI18N
                NbBundle.getMessage(HgModuleConfig.class, "LBL_ParsingError"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.OK_OPTION);
        if (EventQueue.isDispatchThread()) {
            DialogDisplayer.getDefault().notify(nd);
        } else {
            DialogDisplayer.getDefault().notifyLater(nd);
        }
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
}
