/*+
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
package org.netbeans.modules.mercurial;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import javax.swing.JOptionPane;
import java.util.prefs.Preferences;

/**
 * Main entry point for Mercurial functionality, use getInstance() to get the Mercurial object.
 * 
 * @author Maros Sandor
 */
public class Mercurial {
    public static final String MERCURIAL_OUTPUT_TAB_TITLE = org.openide.util.NbBundle.getMessage(Mercurial.class, "CTL_Mercurial_MainMenu"); // NOI18N
    public static final String CHANGESET_STR = "changeset:"; // NOI18N

    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; // NOI18N
    public static final String PROP_CHANGESET_CHANGED = "changesetChanged"; // NOI18N

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial"); // NOI18N

    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static final String MERCURIAL_GOOD_VERSION = "0.9.3"; // NOI18N
    private static final String MERCURIAL_BETTER_VERSION = "0.9.4"; // NOI18N
    private static Mercurial instance;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public static synchronized Mercurial getInstance() {
        if (instance == null) {
            instance = new Mercurial();
            instance.init();
        }
        return instance;
    }
    
    private MercurialAnnotator   mercurialAnnotator;
    private MercurialInterceptor mercurialInterceptor;
    private FileStatusCache     fileStatusCache;
    private HashMap<String, RequestProcessor>   processorsToUrl;
    private boolean goodVersion;
    private String version;
    private boolean checkedVersion;

    private Mercurial() {
    }
    
    
    private void init() {
        checkedVersion = false;
        setDefaultPath();
        fileStatusCache = new FileStatusCache();
        mercurialAnnotator = new MercurialAnnotator();
        mercurialInterceptor = new MercurialInterceptor();
        // Need hgext.hgk added to user's .hgrc file to support Mercurial > View
        HgModuleConfig.getDefault().addHgkExtension();
        checkVersion(); // Does the Hg check but postpones querying user until menu is activated
    }

    private void setDefaultPath() {
        // Set default executable location for mercurial on mac
        if (System.getProperty("os.name").equals("Mac OS X")) { // NOI18N
            String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if (defaultPath == null || defaultPath.length() == 0) {
                String[] pathNames  = {"/Library/Frameworks/Python.framework/Versions/Current/bin", "/usr/local/bin"}; // NOI18N
                for (int i = 0; i < pathNames.length; i++) {
                    if (HgModuleConfig.getDefault().isExecPathValid(pathNames[i])) {
                        HgModuleConfig.getDefault().setExecutableBinaryPath (pathNames[i]); // NOI18N
                        break;
                     } 
                 } 
            }
        }
    }

    private void checkVersion() {
        version = HgCommand.getHgVersion();
        LOG.log(Level.FINE, "version: {0}", version); // NOI18N
        if (version != null) {
            goodVersion = version.startsWith(MERCURIAL_GOOD_VERSION) ||
                          version.startsWith(MERCURIAL_BETTER_VERSION);
        } else {
            goodVersion = false;
        }
    }
    
    public void checkVersionNotify() {  
        if (version != null && !goodVersion) {
            Preferences prefs = HgModuleConfig.getDefault().getPreferences();
            String runVersion = prefs.get(HgModuleConfig.PROP_RUN_VERSION, null);
            if (runVersion == null || !runVersion.equals(version)) {
                int response = JOptionPane.showOptionDialog(null,
                        NbBundle.getMessage(Mercurial.class, "MSG_VERSION_CONFIRM_QUERY", version, MERCURIAL_BETTER_VERSION), // NOI18N
                        NbBundle.getMessage(Mercurial.class, "MSG_VERSION_CONFIRM"), // NOI18N
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

                if (response == JOptionPane.YES_OPTION) {
                    goodVersion = true;
                    prefs.put(HgModuleConfig.PROP_RUN_VERSION, version);
                } else {
                    prefs.remove(HgModuleConfig.PROP_RUN_VERSION);
                }
            } else {
                goodVersion = true;
            }
        }
    }

    public MercurialAnnotator getMercurialAnnotator() {
        return mercurialAnnotator;
    }

    MercurialInterceptor getMercurialInterceptor() {
        return mercurialInterceptor;
    }

    /**
     * Gets the File Status Cache for the mercurial repository
     * 
     * @return FileStatusCache for the repository  
     */
    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }
    
   /**
     * Tests <tt>.hg</tt> directory itself.  
     */
    public boolean isAdministrative(File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public boolean isAdministrative(String fileName) {
        return fileName.equals(".hg"); // NOI18N
    }
    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status. 
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     * 
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */ 
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof MercurialVCS && !HgUtils.isPartOfMercurialMetadata(file);
    }

    public File getTopmostManagedParent(File file) {
        if (HgUtils.isPartOfMercurialMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (isAdministrative(file)) {
                    file = file.getParentFile();
                    break;
                }
            }
        }
        File topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (new File(file, ".hg").canWrite()){ // NOI18N
                topmost =  file;
                break;
            }
        }
        return topmost;
    }

    public HgFileNode [] getNodes(VCSContext context, int includeStatus) {
        File [] files = fileStatusCache.listFiles(context, includeStatus);
        HgFileNode [] nodes = new HgFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new HgFileNode(files[i]);
        }
        return nodes;
    }

   /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        if (fo == null) {
            foMime = "content/unknown"; // NOI18N
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) { // NOI18N
                foMime = "text/plain"; // NOI18N
            }
        }
        if ((fileStatusCache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            return HgUtils.isFileContentBinary(file) ? "application/octet-stream" : foMime; // NOI18N
        } else {
            return foMime;
        }
    }

    public boolean isGoodVersion() {
        if (checkedVersion == false) {
            checkVersionNotify();
            checkedVersion = true;
        }
        return goodVersion;
    }

    public void versionedFilesChanged() {    
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void changesetChanged(File repository) {
        support.firePropertyChange(PROP_CHANGESET_CHANGED, repository, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) return;

        try {
            File original = VersionsCache.getInstance().getFileRevision(workingCopy, Setup.REVISION_BASE);
            if (original == null) {
                Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file {0}", workingCopy); // NOI18N
                 return;
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
            original.delete();
        } catch (IOException e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file", e); // NOI18N
        }
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() { 
        return getRequestProcessor((String)null);
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(File file) {
        return getRequestProcessor(file.getAbsolutePath());
    }

    public RequestProcessor getRequestProcessor(String url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<String, RequestProcessor>();
        }

        String key;
        if(url != null) {
            key = url;
        } else {
            key = "ANY_URL"; // NOI18N
        }

        RequestProcessor rp = processorsToUrl.get(key);
        if(rp == null) {
            rp = new RequestProcessor("Mercurial - " + key, 1, true); // NOI18N
            processorsToUrl.put(key, rp);
        }
        return rp;
    }
    
    public void clearRequestProcessor(String url) {
        if(processorsToUrl != null & url != null) {
             processorsToUrl.remove(url);
        }
    }

}
