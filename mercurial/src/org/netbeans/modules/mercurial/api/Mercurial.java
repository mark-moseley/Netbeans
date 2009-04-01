/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.api;

import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.log.SearchHistoryAction;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author Marian Petras
 */
public class Mercurial {

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole chcekout is done. Do not call in AWT.
     * 
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String defaultPull,
                                       String defaultPush) throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        cloneRepository(repositoryUrl,
                        targetDir,
                        cloneName,
                        defaultPull,
                        defaultPush,
                        null,
                        null);
    }

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole chcekout is done. Do not call in AWT.
     *
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     * @param  username  username for access to the given repository
     * @param  password  password for access to the given repository
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String pullUrl,
                                       String pushUrl,
                                       String username,
                                       String password)
            throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

        File cloneFile = new File(targetDir, cloneName);
        CloneAction.performClone(repositoryUrl,
                                 cloneFile.getAbsolutePath(),
                                 true,
                                 null,
                                 pullUrl,
                                 pushUrl).waitFinished();

        try {
            storeWorkingDir(new URL(repositoryUrl), targetDir.toURI().toURL());
        } catch (Exception e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.FINE, "Cannot store mercurial workdir preferences", e);
        }
    }
    
    private static final String WORKINGDIR_KEY_PREFIX = "working.dir."; //NOI18N

    /**
     * Stores working directory for specified remote root
     * into NetBeans preferences.
     * These are later used in kenai.ui module
     */
    private static void storeWorkingDir(URL remoteUrl, URL localFolder) {
        Preferences prf = NbPreferences.forModule(Mercurial.class);
        prf.put(WORKINGDIR_KEY_PREFIX + remoteUrl, localFolder.toString());
    }

    /**
     * Opens search history panel with a specific DiffResultsView, which does not moves accross differences but initially fixes on the given line.
     * Right panel shows current local changes if the file, left panel shows revisions in the file's repository.
     * Do not run in AWT, IllegalStateException is thrown.
     * Validity of the arguments is checked and result is returned as a return value
     * @param path requested file absolute path. Must be a versioned file (not a folder), otherwise false is returned and the panel would not open
     * @param lineNumber requested line number to fix on
     * @return true if suplpied arguments are valid and the search panel is opened, otherwise false
     */
    public static boolean showFileHistory (final File file, final int lineNumber) {
        assert !EventQueue.isDispatchThread();

        if (!file.exists()) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Trying to show history for non-existent file {0}", file.getAbsolutePath());
            return false;
        }
        if (!file.isFile()) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Trying to show history for a folder {0}", file.getAbsolutePath());
            return false;
        }
        if (!org.netbeans.modules.mercurial.Mercurial.getInstance().isManaged(file)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.INFO, "Trying to show history for an unmanaged file {0}", file.getAbsolutePath());
            return false;
        }
        if(!org.netbeans.modules.mercurial.Mercurial.getInstance().isGoodVersion()) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.INFO, "Mercurial client is unavailable");
            return false;
        }
        /**
         * Open in AWT
         */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                SearchHistoryAction.openSearch(file, lineNumber);
            }
        });
        return true;
    }

    /**
     * Adds a remote url for the combos used in Clone wizard
     *
     * @param url
     * @throws java.net.MalformedURLException
     */
    public static void addRecentUrl(String url) throws MalformedURLException {
        new URL(url); // check url format

        RepositoryConnection rc = new RepositoryConnection(url);
        HgModuleConfig.getDefault().insertRecentUrl(rc);
    }
}
