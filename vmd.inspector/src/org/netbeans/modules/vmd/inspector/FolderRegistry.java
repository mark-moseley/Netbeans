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
package org.netbeans.modules.vmd.inspector;


import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.Mutex;

/**
 *
 * @author Karol Harezlak
 */
final class FolderRegistry {

    private static final HashMap<String, WeakReference<FolderRegistry>> registries = new HashMap<String, WeakReference<FolderRegistry>> ();

    private Mutex mutex = new Mutex();
    private HashMap<TypeID, InspectorFolder> folders = new HashMap<TypeID, InspectorFolder>();
    private GlobalFolderRegistry globalFolderRegistry;

    static FolderRegistry getRegistry(String projectType, String projectID)  {
        synchronized (registries) {
            WeakReference<FolderRegistry> ref = registries.get(projectID);
            FolderRegistry registry = ref != null ? ref.get() : null;
            if (registry == null) {
                registry = new FolderRegistry(projectType);
                registries.put(projectID, new WeakReference<FolderRegistry>(registry));
            }
            return registry;
        }
    }

    private FolderRegistry(String projectType) {
        globalFolderRegistry = GlobalFolderRegistry.getGlobalFolderRegistry(projectType);
        reload();
    }

    private boolean isAccess() {
        return mutex.isReadAccess() || mutex.isWriteAccess();
    }

    void readAccess(final Runnable runnable) {
        globalFolderRegistry.readAccess(new Runnable() {
            public void run() {
                mutex.readAccess(runnable);
            }
        });
    }

    private void writeAccess(final Runnable runnable) {
        globalFolderRegistry.readAccess(new Runnable() {
            public void run() {
                mutex.writeAccess(runnable);
            }
        });
    }

    private void reload() {
        writeAccess(new Runnable() {
            public void run() {
                reloadCore();
            }
        });
    }

    private void reloadCore() {
        HashMap<TypeID, InspectorFolder> tempFolders = new HashMap<TypeID, InspectorFolder>();
        Collection<InspectorFolder> _folders = globalFolderRegistry.getInspectorFolder();

        for (InspectorFolder folder : _folders)
            tempFolders.put(folder.getTypeID(), folder);

        this.folders = tempFolders;
    }

    Collection<InspectorFolder> getFolders() {
        assert isAccess();
        return Collections.unmodifiableCollection(folders.values());
    }

    void addListener(Listener listener) {
        globalFolderRegistry.addListener(listener);
    }

    void removeListener(Listener listener) {
        globalFolderRegistry.removeListener(listener);
    }

    interface Listener {
        void notifyRegistryContentChange();
    }
}
