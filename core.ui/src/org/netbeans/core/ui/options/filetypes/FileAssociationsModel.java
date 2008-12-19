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
package org.netbeans.core.ui.options.filetypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** Model holds mapping between extension and MIME type.
 *
 * @author Jiri Skrivanek
 */
final class FileAssociationsModel {

    private static final String MIME_RESOLVERS_PATH = "Services/MIMEResolver";  //NOI18N
    private static final Logger LOGGER = Logger.getLogger(FileAssociationsModel.class.getName());
    /** Maps both system and user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeAll = new HashMap<String, String>();
    /** Maps system extensions to MIME type. */
    private HashMap<String, String> extensionToMimeSystem = new HashMap<String, String>();
    /** Maps user-defined extensions to MIME type. */
    private HashMap<String, String> extensionToMimeUser = new HashMap<String, String>();
    /** Ordered set of all MIME types registered in system. */
    private TreeSet<String> mimeTypes = new TreeSet<String>();
    /** Maps MIME type to MimeItem object which holds display name. */
    private HashMap<String, MimeItem> mimeToItem = new HashMap<String, MimeItem>();
    private boolean initialized = false;
    private final FileChangeListener mimeResolversListener = new FileChangeAdapter() {
        public @Override void fileDeleted(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            initialized = false;
        }
        public @Override void fileDataCreated(FileEvent fe) {
            initialized = false;
        }
        public @Override void fileChanged(FileEvent fe) {
            initialized = false;
        }
    };

    /** Creates new model. */
    FileAssociationsModel() {
        FileObject resolvers = Repository.getDefault().getDefaultFileSystem().findResource(MIME_RESOLVERS_PATH);
        if (resolvers != null) {
            resolvers.addFileChangeListener(FileUtil.weakFileChangeListener(mimeResolversListener, resolvers));
        }
    }

    /** Returns true if model includes given extension. */
    boolean containsExtension(String extension) {
        return extensionToMimeAll.containsKey(extension);
    }

    /** Returns string of extensions also associated with given MIME type
     * excluding given extension.
     * @param extension extension to be excluded from the list
     * @param newMimeType MIME type of interest
     * @return comma separated list of extensions (e.g. "gif, jpg, bmp")
     */
    String getAssociatedAlso(String extension, String newMimeType) {
        StringBuilder result = new StringBuilder();
        for (String extensionKey : getExtensions()) {
            if (!extensionKey.equals(extension) && extensionToMimeAll.get(extensionKey).equals(newMimeType)) {
                if (result.length() != 0) {
                    result.append(", ");  //NOI18N
                }
                result.append(extensionKey);
            }
        }
        return result.toString();
    }

    /** Returns ordered list of registered extensions.
     * @return list of ordered extensions
     */
    List<String> getExtensions() {
        init();
        ArrayList<String> list = new ArrayList<String>(extensionToMimeAll.keySet());
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    /** Returns ordered set of all known MIME types
     * @return ordered set of MIME types
     */
    Set<String> getMimeTypes() {
        init();
        return mimeTypes;
    }

    /** Reads MIME types registered in Loaders folder and fills mimeTypes set. */
    private void readMimeTypesFromLoaders() {
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject[] children = defaultFS.findResource("Loaders").getChildren();  //NOI18N
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];
            String mime1 = child.getNameExt();
            FileObject[] subchildren = child.getChildren();
            for (int j = 0; j < subchildren.length; j++) {
                FileObject subchild = subchildren[j];
                FileObject factoriesFO = subchild.getFileObject("Factories");  //NOI18N
                if(factoriesFO != null && factoriesFO.getChildren().length > 0) {
                    // add only MIME types where some loader exists
                    mimeTypes.add(mime1 + "/" + subchild.getNameExt()); //NOI18N
                }
            }
        }
        mimeTypes.remove("content/unknown"); //NOI18N
    }

    /** Returns MIME type corresponding to given extension. Cannot return null. */
    String getMimeType(String extension) {
        init();
        return extensionToMimeAll.get(extension);
    }

    /** Returns MimeItem corresponding to given extension. */
    MimeItem getMimeItem(String extension) {
        return mimeToItem.get(getMimeType(extension));
    }

    /** Removes user defined extension to MIME type mapping. */
    void remove(String extension) {
        extensionToMimeUser.remove(extension);
        extensionToMimeAll.remove(extension);
    }

    /** Sets default (system) MIME type for given extension. */
    void setDefault(String extension) {
        remove(extension);
        extensionToMimeAll.put(extension, extensionToMimeSystem.get(extension));
    }

    /** Sets new extension to MIME type mapping (only if differs from current).
     * Returns true if really changed, false otherwise. */
    boolean setMimeType(String extension, String newMimeType) {
        String oldMmimeType = getMimeType(extension);
        if (!newMimeType.equals(oldMmimeType)) {
            LOGGER.fine("setMimeType - " + extension + "=" + newMimeType);
            extensionToMimeUser.put(extension, newMimeType);
            extensionToMimeAll.put(extension, newMimeType);
            return true;
        }
        return false;
    }

    /** Returns true if mapping of extension to MIME type was changed and 
     * exists default/system mapping. */
    boolean canBeRestored(String extension) {
        return extensionToMimeUser.containsKey(extension) && extensionToMimeSystem.containsKey(extension);
    }

    /** Returns true if extension doesn't have default/system mapping. */
    boolean canBeRemoved(String extension) {
        return !extensionToMimeSystem.containsKey(extension);
    }
    
        /** Returns localized display name of loader for given MIME type or null if not defined. */
    private static String getLoaderDisplayName(String mimeType) {
        FileSystem root = Repository.getDefault().getDefaultFileSystem();
        FileObject factoriesFO = root.findResource("Loaders/" + mimeType + "/Factories");  //NOI18N
        if(factoriesFO != null) {
            FileObject[] children = factoriesFO.getChildren();
            for (FileObject child : children) {
                String childName = child.getNameExt();
                String displayName = root.getStatus().annotateName(childName, Collections.singleton(child));
                if(!childName.equals(displayName)) {
                    return displayName;
                }
            }
        }
        return null;
    }

    /** Returns sorted list of MimeItem objects. */
    ArrayList<MimeItem> getMimeItems() {
        init();
        ArrayList<MimeItem> items = new ArrayList<MimeItem>(mimeToItem.values());
        Collections.sort(items);
        return items;
    }
    
    /** Stores current state of model. It deletes user-defined mime resolver
     * and writes a new one. */
    void store() {
        Map<String, Set<String>> mimeToExtensions = new HashMap<String, Set<String>>();
        for (Map.Entry<String, String> entry : extensionToMimeUser.entrySet()) {
            String extension = entry.getKey();
            String mimeType = entry.getValue();
            Set<String> extensions = mimeToExtensions.get(mimeType);
            if (extensions == null) {
                extensions = new HashSet<String>();
                mimeToExtensions.put(mimeType, extensions);
            }
            extensions.add(extension);
        }
        MIMEResolverImpl.storeUserDefinedResolver(mimeToExtensions);
    }

    private void init() {
        if (initialized) {
            return;
        }
        LOGGER.fine("FileAssociationsModel.init");  //NOI18N
        initialized = true;
        for (FileObject mimeResolverFO : MIMEResolverImpl.getOrderedResolvers().values()) {
            boolean userDefined = MIMEResolverImpl.isUserDefined(mimeResolverFO);
            Map<String, Set<String>> mimeToExtensions = MIMEResolverImpl.getMIMEToExtensions(mimeResolverFO);
            for (Map.Entry<String, Set<String>> entry : mimeToExtensions.entrySet()) {
                String mimeType = entry.getKey();
                Set<String> extensions = entry.getValue();
                for (String extension : extensions) {
                    extensionToMimeAll.put(extension, mimeType);
                    if (userDefined) {
                        extensionToMimeUser.put(extension, mimeType);
                    } else {
                        extensionToMimeSystem.put(extension, mimeType);
                    }
                }
                mimeTypes.add(mimeType);
            }
        }
        readMimeTypesFromLoaders();
        // init mimeItems
        for (String mimeType : mimeTypes) {
            MimeItem mimeItem = new MimeItem(mimeType, getLoaderDisplayName(mimeType));
            mimeToItem.put(mimeType, mimeItem);
        }
        LOGGER.fine("extensionToMimeSystem=" + extensionToMimeSystem);  //NOI18N
        LOGGER.fine("extensionToMimeUser=" + extensionToMimeUser);  //NOI18N
    }
    
    /** To store MIME type and its loader display name. It is used in combo box. */
    static final class MimeItem implements Comparable<MimeItem> {

        String mimeType;
        String displayName;

        MimeItem(String mimeType, String displayName) {
            this.mimeType = mimeType;
            this.displayName = displayName;
        }

        String getMimeType() {
            return mimeType;
        }

        @Override
        public String toString() {
            return displayName == null ? mimeType : displayName + " (" + mimeType + ")";
        }

        public int compareTo(MimeItem o) {
            return toString().compareToIgnoreCase(o.toString());
        }
    }
}
