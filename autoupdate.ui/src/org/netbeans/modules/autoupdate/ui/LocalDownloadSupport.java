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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class LocalDownloadSupport {

    private static final FileFilter NBM_FILE_FILTER = new NbmFileFilter ();
    private static String LOCAL_DOWNLOAD_DIRECTORY_KEY = "local-download-directory"; // NOI18N
    private static String LOCAL_DOWNLOAD_FILES = "local-download-files"; // NOI18N    
    private static String LOCAL_DOWNLOAD_CHECKED_FILES = "local-download-checked-files"; // NOI18N    
    private FileList fileList = new FileList ();
    private Logger err = Logger.getLogger (LocalDownloadSupport.class.getName ());
    private Map<File, String> nbm2unitCodeName = null;
    private Map<String, UpdateUnit> codeName2unit = null;

    public LocalDownloadSupport () {
    }

    public boolean chooseNbmFiles () {
        JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter (NBM_FILE_FILTER);
        chooser.setFileFilter (NBM_FILE_FILTER);
        chooser.setMultiSelectionEnabled (true);
        chooser.setFileHidingEnabled (false);
        chooser.setDialogTitle (getBundle ("CTL_FileChooser_Title"));

        File dir = getDefaultDir ();
        if (dir != null && dir.exists ()) {
            chooser.setCurrentDirectory (dir);
        }

        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();
        if (chooser.showOpenDialog (parent) == JFileChooser.APPROVE_OPTION) {
            synchronized (LocalDownloadSupport.class) {
                getPreferences ().put (LOCAL_DOWNLOAD_DIRECTORY_KEY, chooser.getCurrentDirectory ().getAbsolutePath ());
                fileList.addFiles (chooser.getSelectedFiles ());
                addUpdateUnits (chooser.getSelectedFiles ());
            }
            return true;
        }
        return false;
    }

    public Collection<UpdateUnit> getUpdateUnits () {
        Collection<UpdateUnit> res = null;
        synchronized (LocalDownloadSupport.class) {
            res = new LinkedList<UpdateUnit> (getCodeName2Unit ().values ());
        }
        return res;
    }

    public void checkUnit (UpdateUnit updateUnit) {
        fileList.makePersistentCheckedFile (getNbm (updateUnit.getCodeName ()));
    }

    public void uncheckUnit (UpdateUnit updateUnit) {
        fileList.makePersistentUncheckedFile (getNbm (updateUnit.getCodeName ()));
    }

    public boolean isChecked (UpdateUnit unit) {
        return fileList.isChecked (getNbm (unit.getCodeName ()));
    }
    
    private Collection<File> getAllFiles () {
        return fileList.getAllFiles ();
    }

    private void addUpdateUnits (File... newFiles) {
        Collection<UpdateUnit> alreadyInstalled = new HashSet<UpdateUnit> ();
        for (File nbm : newFiles) {
            UpdateUnit u = null;
            if(NBM_FILE_FILTER.accept(nbm)) {
                u = createUpdateUnitFromNBM (nbm, false);
            }
            if (u != null) {
                if (u.getAvailableUpdates () == null || u.getAvailableUpdates ().isEmpty ()) {
                    // already installed
                    alreadyInstalled.add (u);
                } else if (getCodeName2Unit ().containsKey (u.getCodeName ())) {
                    UpdateElement uE1 = u.getAvailableUpdates ().get (0);
                    UpdateElement uE2 = getCodeName2Unit ().get (u.getCodeName ()).getAvailableUpdates ().get (0);
                    UpdateUnit winnerUnit = null;
                    File winnerFile = null;
                    UpdateUnit looserUnit = null;
                    File looserFile = null;
                    // both are valid, an user have to choose
                    String name1 = getBundle ("NotificationPlugin", uE1.getDisplayName (), uE1.getSpecificationVersion ()); // NOI18N
                    String name2 = getBundle ("NotificationPlugin", uE2.getDisplayName (), uE2.getSpecificationVersion ()); // NOI18N
                    Object res = DialogDisplayer.getDefault ().notify (
                            new NotifyDescriptor.Confirmation (
                            getBundle ("NotificationAlreadyPresent", name2, name1), // NOI18N
                            getBundle ("NotificationAlreadyPresentTitle"))); // NOI18N
                    if (NotifyDescriptor.YES_OPTION.equals (res)) {
                        winnerUnit = uE1.getUpdateUnit ();
                        winnerFile = nbm;
                        looserUnit = uE2.getUpdateUnit ();
                        looserFile = getNbm (winnerUnit.getCodeName ());
                    } else if (NotifyDescriptor.NO_OPTION.equals (res)) {
                        winnerUnit = uE2.getUpdateUnit ();
                        winnerFile = getNbm (winnerUnit.getCodeName ());
                        looserUnit = uE1.getUpdateUnit ();
                        looserFile = nbm;
                    } else {
                        // CANCEL_OPTION
                        break;
                    }
                    getNbm2CodeName ().remove (looserFile);
                    getCodeName2Unit ().remove (looserUnit.getCodeName ());
                    getNbm2CodeName ().put (winnerFile, winnerUnit.getCodeName ());
                    getCodeName2Unit ().put (winnerUnit.getCodeName (), winnerUnit);
                    fileList.removeFile (looserFile);
                    Containers.forUpdateNbms ().removeAll ();
                    Containers.forAvailableNbms ().removeAll ();
                } else {
                    getNbm2CodeName ().put (nbm, u.getCodeName ());
                    getCodeName2Unit ().put (u.getCodeName (), u);
                }
            } else {
                fileList.removeFile (nbm);
            }
            if (!alreadyInstalled.isEmpty ()) {
                String msg = null;
                if (alreadyInstalled.size () == 1) {
                    msg = getBundle ("NotificationOneAlreadyInstalled", getDisplayNames (alreadyInstalled)); //NOI18N
                } else {
                    msg = getBundle ("NotificationMoreAlreadyInstalled", getDisplayNames (alreadyInstalled)); //NOI18N
                }
                DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message (
                        msg,
                        NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }

    synchronized private void initCodeName2Unit () {
        assert nbm2unitCodeName == null : "Cannot initialize nbm2unit twice!";
        nbm2unitCodeName = new HashMap<File, String> ();
        codeName2unit = new HashMap<String, UpdateUnit> ();
        Iterator<File> it = getAllFiles ().iterator ();
        while (it.hasNext ()) {
            File nbm = it.next ();
            UpdateUnit u = createUpdateUnitFromNBM (nbm, true);
            if (u != null && ! u.getAvailableUpdates ().isEmpty ()) {
                nbm2unitCodeName.put (nbm, u.getCodeName ());
                codeName2unit.put (u.getCodeName (), u);
            } else {
                it.remove ();
            }
        }
    }

    private Map<File, String> getNbm2CodeName () {
        synchronized (LocalDownloadSupport.class) {
            if (nbm2unitCodeName == null) {
                initCodeName2Unit ();
            }
        }
        return nbm2unitCodeName;
    }

    private Map<String, UpdateUnit> getCodeName2Unit () {
        synchronized (LocalDownloadSupport.class) {
            if (codeName2unit == null) {
                initCodeName2Unit ();
            }
        }
        return codeName2unit;
    }

    private String getUnitCodeName (File nbm) {
        assert nbm != null : "Invalid file " + nbm;
        if (nbm != null) {
            return getNbm2CodeName ().get (nbm);
        }
        return null;
    }

    private File getNbm (String codeName) {
        assert codeName != null : "Invalid code name " + codeName;
        if (codeName != null) {
            for (File nbm : getNbm2CodeName ().keySet ()) {
                if (codeName.equals (getUnitCodeName(nbm))) {
                    return nbm;
                }
            }
        }
        return null;
    }

    private UpdateUnit createUpdateUnitFromNBM (File nbm, boolean quiet) {
        UpdateUnitProviderFactory factory = UpdateUnitProviderFactory.getDefault ();
        UpdateUnitProvider provider = factory.create (nbm.getName (), new File[] {nbm});
        List<UpdateUnit> units = Collections.emptyList ();
        try {
            units = provider.getUpdateUnits (UpdateManager.TYPE.MODULE);
        } catch (RuntimeException re) {
            if (!quiet) {
                err.log (Level.INFO, re.getMessage (), re);
                DialogDisplayer.getDefault ().notifyLater (new NotifyDescriptor.Exception (re,
                        getBundle ("LocalDownloadSupport_BrokenNBM_Exception",
                        nbm.getName (),
                        re.getLocalizedMessage ())));
                fileList.removeFile (nbm);
            }
        }
        if (units == null || units.size () == 0) {
            // skip to another one
            return null;
        }
        assert units.size () == 1 : "Only once UpdateUnit for " + nbm + " but " + units;
        return units.get (0);
    }

    public void removeInstalledUnit() {
        synchronized (LocalDownloadSupport.class) {
            Iterator<UpdateUnit> it = getCodeName2Unit().values().iterator();
            while (it.hasNext()) {
                UpdateUnit u = it.next();
                if (u.getInstalled() != null && u.getAvailableUpdates().isEmpty()) {
                    it.remove();
                    getNbm2CodeName().remove(getNbm(u.getCodeName()));
                }
            }
        }
    }

    public boolean remove (UpdateUnit unit) {
        File f = getNbm (unit.getCodeName ());
        if (f != null) {
            fileList.removeFile (f);
            synchronized (LocalDownloadSupport.class) {
                File nbm = getNbm (unit.getCodeName ());
                getCodeName2Unit ().remove (unit.getCodeName ());
                getNbm2CodeName ().remove (nbm);
            }
        }
        return f != null;
    }

    private static class NbmFileFilter extends FileFilter {

        public boolean accept (File f) {
            return f.isDirectory () || f.getName ().toLowerCase ().endsWith (".nbm"); // NOI18N
        }

        public String getDescription () {
            return getBundle ("CTL_FileFilterDescription"); // NOI18N
        }
    }

    private static File getDefaultDir () {
        File retval = new File (getPreferences ().get (LOCAL_DOWNLOAD_DIRECTORY_KEY, System.getProperty ("netbeans.user")));// NOI18N
        return (retval.exists ()) ? retval : new File (System.getProperty ("netbeans.user")); // NOI18N
    }

    public static String getBundle (String key, String... params) {
        return NbBundle.getMessage (LocalDownloadSupport.class, key, params);
    }

    private static Preferences getPreferences () {
        return NbPreferences.forModule (LocalDownloadSupport.class);
    }

    private static class FileList {

        private Set<File> allFiles = null;
        private Set<File> checkedFiles = null;

        Set<File> getAllFiles () {
            if (allFiles == null) {
                allFiles = new LinkedHashSet<File> ();
                addFiles (loadPresistentState ());
            }
            return allFiles;
        }

        Set<File> getCheckedFiles () {
            if (checkedFiles == null) {
                checkedFiles = new HashSet<File> ();
                for (File f : getAllFiles ()) {
                    if (isChecked (f)) {
                        checkedFiles.add (f);
                    }
                }
            }
            return checkedFiles;
        }

        void addFiles (File[] files) {
            addFiles (Arrays.asList (files));
        }

        void addFiles (Collection<File> files) {
            getAllFiles ().addAll (files);
            Collection<String> names = new HashSet<String> ();
            for (File f : files) {
                names.add (f.getAbsolutePath ());
            }
            allFiles = stripNoNBMs (stripNotExistingFiles (getAllFiles ()));
            makePersistent (allFiles);
            makePersistentCheckedNames (names);
        }

        void removeFile (File file) {
            removeFiles (Collections.singleton (file));
        }

        void removeFiles (Collection<File> files) {
            getAllFiles ().removeAll (files);
            allFiles = stripNoNBMs (stripNotExistingFiles (getAllFiles ()));
            makePersistent (allFiles);
            for (File f : files) {
                makePersistentUncheckedFile (f);
            }
        }

        private Set<File> loadPresistentState () {
            Set<File> retval = new HashSet<File> ();
            String files = getPreferences ().get (LOCAL_DOWNLOAD_FILES, null);
            if (files != null) {
                String[] fileArray = files.split (","); // NOI18N  
                for (String file : fileArray) {
                    retval.add (new File (file));
                }
            }
            return retval;
        }

        private boolean isChecked (File f) {
            return getCheckedPaths ().contains (f.getAbsolutePath ());
        }

        private Collection<String> getCheckedPaths () {
            Set<String> res = new HashSet<String> ();
            String names = getPreferences ().get (LOCAL_DOWNLOAD_CHECKED_FILES, null);
            if (names != null) {
                StringTokenizer st = new StringTokenizer (names, ",");
                while (st.hasMoreTokens ()) {
                    res.add (st.nextToken ().trim ());
                }
            }
            return res;
        }

        private void makePersistentUncheckedFile (File f) {
            if (isChecked (f)) {
                Collection<String> newNames = getCheckedPaths ();
                newNames.remove (f.getAbsolutePath ());
                makePersistentCheckedNames (newNames);
            }
        }

        private void makePersistentCheckedFile (File f) {
            if (!isChecked (f)) {
                Collection<String> newNames = getCheckedPaths ();
                newNames.add (f.getAbsolutePath ());
                makePersistentCheckedNames (newNames);
            }
        }

        private void makePersistentCheckedNames (Collection<String> names) {
            StringBuilder sb = null;
            if (!names.isEmpty ()) {
                for (String s : names) {
                    if (sb == null) {
                        sb = new StringBuilder (s);
                    } else {
                        sb.append (", ").append (s); // NOI18N
                    }
                }
            }
            if (sb == null) {
                getPreferences ().remove (LOCAL_DOWNLOAD_CHECKED_FILES);
            } else {
                getPreferences ().put (LOCAL_DOWNLOAD_CHECKED_FILES, sb.toString ());
            }
        }

        private static void makePersistent (Set<File> files) {
            StringBuilder sb = null;
            if (!files.isEmpty ()) {
                for (File file : files) {
                    if (sb == null) {
                        sb = new StringBuilder (file.getAbsolutePath ());
                    } else {
                        sb.append (',').append (file.getAbsolutePath ()); // NOI18N
                    }
                }
            }
            if (sb == null) {
                getPreferences ().remove (LOCAL_DOWNLOAD_FILES);
            } else {
                getPreferences ().put (LOCAL_DOWNLOAD_FILES, sb.toString ());
            }
        }

        private static Set<File> stripNotExistingFiles (Set<File> files) {
            Set<File> retval = new HashSet<File> ();
            for (File file : files) {
                if (file.exists ()) {
                    retval.add (file);
                }
            }
            return retval;
        }

        private static Set<File> stripNoNBMs (Set<File> files) {
            Set<File> retval = new HashSet<File> ();
            for (File file : files) {
                if (NBM_FILE_FILTER.accept (file)) {
                    retval.add (file);
                }
            }
            return retval;
        }
    }

    private String getDisplayNames (Collection<UpdateUnit> units) {
        SortedSet<String> names = new TreeSet<String> ();
        for (UpdateUnit uu : units) {
            if (uu.getInstalled () != null) {
                names.add (uu.getInstalled ().getDisplayName ());
            } else {
                names.add (uu.getAvailableUpdates ().get (0).getDisplayName ());
            }
        }
        String res = "";
        for (String dn : names) {
            res += res.length () == 0 ? dn : ", " + dn; // NOI18N
        }
        return res;
    }
}
