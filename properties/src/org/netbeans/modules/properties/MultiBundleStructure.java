/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author alexeybutenko
 */
class MultiBundleStructure extends BundleStructure implements Serializable {

    private transient FileObject[] files;
    private transient PropertiesFileEntry primaryEntry;
    private String baseName;

    /** Generated Serialized Version UID. */
    static final long serialVersionUID = 7501232754255253334L;

    private transient PropertiesOpen openSupport;
    /** Lock used for synchronization of <code>openSupport</code> instance creation */
    private final transient Object OPEN_SUPPORT_LOCK = new Object();


    protected MultiBundleStructure() {
//        super();
//        files = null;
//        primaryEntry = null;
//        baseName = null;
    }

    public MultiBundleStructure(PropertiesDataObject obj) {
//        super(obj);
        this.obj = obj;
        baseName = Util.getBaseName(obj.getName());
    }

    /**
     * Find entries according to PropertiesDataObject
     *
     */
    private void findEntries() {
        try {
            if (obj == null) return;
            if (!obj.isValid()) {
                if (files!=null && files.length == 1) {
                    primaryEntry = null;
                    obj = null;
                    files = null;
                    return;
                }
            } else {
                obj = Util.findPrimaryDataObject(obj);
                primaryEntry = (PropertiesFileEntry) obj.getPrimaryEntry();
            }
            FileObject primary = primaryEntry.getFile();
            FileObject parent = primary.getParent();
            List<FileObject> listFileObjects = new ArrayList<FileObject>();
            String fName;
            FileObject oldCandidate;
            for (FileObject file : parent.getChildren()) {
                if (!file.hasExt(PropertiesDataLoader.PROPERTIES_EXTENSION)) {
                    continue;
                }
                fName = file.getName();
                if (fName.equals(baseName) && file.isValid()) {
                    listFileObjects.add(0,file);
                }
                if (fName.indexOf(baseName) != -1) {
                    int index = fName.indexOf(PropertiesDataLoader.PRB_SEPARATOR_CHAR);
                    if (index == baseName.length()) {
                        oldCandidate = null;
                        while (index != -1) {
                            FileObject candidate = file;
                            if (candidate != null && isValidLocaleSuffix(fName.substring(index)) && oldCandidate == null && file.isValid()) {
                                listFileObjects.add(candidate);
                                oldCandidate = candidate;
                            }
                            index = fName.indexOf(PropertiesDataLoader.PRB_SEPARATOR_CHAR, index + 1);
                        }
                    }
                }
            }
            files = listFileObjects.toArray(new FileObject[listFileObjects.size()]);
            primaryEntry = getNthEntry(0);
            obj = (PropertiesDataObject) primaryEntry.getDataObject();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void updateEntries() {
        findEntries();
        if (files != null) {
            buildKeySet();
        }
    }

    @Override
    public PropertiesFileEntry getNthEntry(int index) {
        if (files == null) {
            return null;//super.getNthEntry(index);
//            notifyEntriesNotInitialized();
        }
        if (index >= 0 && index < files.length) {
            try {
                return (PropertiesFileEntry) ((PropertiesDataObject) DataObject.find(files[index])).getPrimaryEntry();
            } catch (DataObjectNotFoundException ex) {
//                ex.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Retrieves an index of a file entry representing the given file.
     *
     * @param  fileName  simple name (without path and extension) of the
     *                   primary or secondary file
     * @return  index of the entry representing a file with the given filename;
     *          or <code>-1</code> if no such entry is found
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     * @see  #getEntryByFileName
     */
    @Override
    public int getEntryIndexByFileName(String fileName) {
        if (files == null) {
            notifyEntriesNotInitialized();
        }
        for (int i = 0; i < getEntryCount(); i++) {
            if (files[i].getName().equals(fileName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves a file entry representing the given file
     *
     * @param  fileName  simple name (excl. path, incl. extension) of the
     *                   primary or secondary file
     * @return  entry representing the given file;
     *          or <code>null</code> if not such entry is found
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     * @see  #getEntryIndexByFileName
     */
    @Override
    public PropertiesFileEntry getEntryByFileName(String fileName) {
        int index = getEntryIndexByFileName(fileName);
        try {
            return (index == -1) ? null : (PropertiesFileEntry) ((PropertiesDataObject) DataObject.find(files[index])).getPrimaryEntry();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Retrieves number of file entries.
     *
     * @return  number of file entries
     * @exception  java.lang.IllegalStateException
     *             if the list of entries has not been initialized yet
     */
    @Override
    public int getEntryCount() {
        if (files == null) {
            return 0;//super.getEntryCount();
//            notifyEntriesNotInitialized();
        }
        return files.length;
    }

    /**
     * Throws a runtime exception with a message that the entries
     * have not been initialized yet.
     *
     * @exception  java.lang.IllegalStateException  thrown always
     * @see  #updateEntries
     */
    private void notifyEntriesNotInitialized() {
        throw new IllegalStateException(
                "Resource Bundles: Entries not initialized");           //NOI18N
    }

    private static boolean isValidLocaleSuffix(String s) {
        // first char is _
        int n = s.length();
        String s1;
        // check first suffix - language (two chars)
        if (n == 3 || (n > 3 && s.charAt(3) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)) {
            s1 = s.substring(1, 3).toLowerCase();
        // language must be followed by a valid country suffix or no suffix
        } else {
            return false;
        }
        // check second suffix - country (two chars)
        String s2;
        if (n == 3) {
            s2 = null;
        } else if (n == 6 || (n > 6 && s.charAt(6) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)) {
            s2 = s.substring(4, 6).toUpperCase();
        // country may be followed by whatever additional suffix
        } else {
            return false;
        }

        Set<String> knownLanguages = new HashSet<String>(Arrays.asList(Locale.getISOLanguages()));
        if (!knownLanguages.contains(s1)) {
            return false;
        }

        if (s2 != null) {
            Set<String> knownCountries = new HashSet<String>(Arrays.asList(Locale.getISOCountries()));
            if (!knownCountries.contains(s2)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public PropertiesOpen getOpenSupport() {
        synchronized (OPEN_SUPPORT_LOCK) {
            if (openSupport == null) {
                openSupport = new PropertiesOpen(this);
            }
            return openSupport;
        }
    }

    @Override
    public int getKeyCount() {
        try {
            return super.getKeyCount();
        } catch (IllegalStateException ie) {
            return 0;
        }
    }
}