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

package org.netbeans.modules.favorites.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.favorites.Actions;
import org.netbeans.modules.favorites.FavoritesNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;

/**
 * Singleton for access to Favorites.
 *
 * @author Richard Michalsky
 */
public final class Favorites {
    private static Favorites INSTANCE;

    private static Logger LOG = Logger.getLogger(Favorites.class.getName());

    /**
     * Returns a default instance of Favorites.
     * @return
     */
    public static synchronized Favorites getDefault() {
        if (INSTANCE == null)
            INSTANCE = new Favorites();
        return INSTANCE;
    }

    /**
     * Adds new files to Favorites.
     * File objects already present are silently ignored.
     * @param toAdd file objects to be added to Favorites. May be empty, but not <code>null</code>.
     * @throws NullPointerException When any of <code>toAdd</code> parameters
     *         is <code>null</code>. It is undefined whether some of other non-null
     *         parameters were added or not.
     * @throws DataObjectNotFoundException When corresponding <code>DataObject</code> to one of the file objects could not be found.
     */
    public synchronized void add(FileObject... toAdd)
            throws NullPointerException, DataObjectNotFoundException {
        if (toAdd.length == 0)
            return;
        addInternal(toAdd);
    }

    /**
     * Selects given file in Favorites view, adds it as root first if it is not already present.
     * @param fo file object to be selected and/or added if needed
     * @return <code>true</code> when file was also added, <code>false</code> when only selected.
     * @throws DataObjectNotFoundException When corresponding <code>DataObject</code> to the file object could not be found.
     * @see #add(org.openide.filesystems.FileObject)
     * @see #isInFavorites(org.openide.filesystems.FileObject)
     */
    public synchronized boolean selectWithAddition(FileObject fo) throws DataObjectNotFoundException {
        DataShadow obj = findShadow(fo);
        boolean result = false;
        if (obj == null) {
            obj = (addInternal(fo))[0];
            result = true;
        }
        Actions.Add.selectAfterAddition(obj);
        return result;
    }

    /**
     * Returns <code>true</code> if given file object is already in Favorites <b>as a root</b>,
     * i.e. descendants of root don't count.
     * @param fo File object to query.
     * @return
     */
    public synchronized boolean isInFavorites (FileObject fo) {
        return findShadow(fo) != null;
    }

    @Override
    public String toString() {
        return "Favorites: " + getFavoriteRoots().toString();    // NOI18N
    }

    /**
     * Removes references to nonexistent files.
     * Package-private for tests.
     */
    void clearBrokenShadows() throws IOException {
        DataFolder f = FavoritesNode.getFolder();
        DataObject [] arr = f.getChildren();
        for (DataObject obj : arr) {
            if (! (obj instanceof DataShadow)) {
                obj.delete();
            }
        }
    }

    private DataShadow findShadow(FileObject fo) {
        DataFolder f = FavoritesNode.getFolder();
        DataObject [] arr = f.getChildren();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof DataShadow) {
                DataShadow obj = (DataShadow) arr[i];
                if (fo.equals(obj.getOriginal().getPrimaryFile())) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Returns list of current root file objects in Favorites.
     * @return
     */
    public synchronized List<FileObject> getFavoriteRoots() {
        DataFolder f = FavoritesNode.getFolder();
        DataObject [] arr = f.getChildren();
        List<FileObject> ret = new ArrayList<FileObject>(arr.length);

        for (DataObject obj : arr) {
            if (obj instanceof DataShadow) {
                ret.add(((DataShadow) obj).getOriginal().getPrimaryFile());
            }
        }
        return ret;
    }

    /**
     * Removes given file objects from Favorites if they are found as roots.
     * Files that are not found are silently ignored.
     * @param toRemove file objects to be removed.
     * @throws IOException When corresponding entry in userdir could not be deleted
     * @throws NullPointerException When any of <code>toRemove</code> parameters
     *         is <code>null</code>. It is undefined whether some of other non-null
     *         parameters were removed or not.
     */
    public synchronized void remove(FileObject... toRemove) throws IOException, NullPointerException {
        DataFolder f = FavoritesNode.getFolder();
        DataObject [] arr = f.getChildren();
        for (DataObject obj : arr) {
            if (obj instanceof DataShadow) {
                FileObject root = ((DataShadow) obj).getOriginal().getPrimaryFile();
                for (FileObject rem : toRemove) {
                    if (rem.equals(root)) {
                        obj.delete();
                    }
                }
            }
        }
    }

    private DataShadow[] addInternal(FileObject... toAdd) throws DataObjectNotFoundException {
        final DataFolder f = FavoritesNode.getFolder();
        DataShadow[] createdDOs = createShadows(f, toAdd);
        //This is done to set desired order of nodes in view
        Actions.Add.reorderAfterAddition(f, f.getChildren(), Arrays.asList(createdDOs));
        return createdDOs;
    }

    private DataShadow[] createShadows(final DataFolder favorites, final FileObject[] toAdd) throws DataObjectNotFoundException, IllegalArgumentException {
        List<DataShadow> createdDO = new ArrayList<DataShadow>(toAdd.length);
        for (int i = 0; i < toAdd.length; i++) {
            if (!isInFavorites(toAdd[i])) {
                DataObject obj = DataObject.find(toAdd[i]);
                if (obj != null) {
                    try {
                        createdDO.add(obj.createShadow(favorites));
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
        return createdDO.toArray(new DataShadow[createdDO.size()]);
    }

}
