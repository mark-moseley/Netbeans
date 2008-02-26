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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
public final class FileObjectFactory {
    public static Map AllFactories = new HashMap();
    public static boolean WARNINGS = false;
    final Map allIBaseFileObjects = Collections.synchronizedMap(new WeakHashMap());
    private BaseFileObj root;
    public static enum Caller {
        ToFileObject, GetFileObject, GetChildern, GetParent, Others
    }
        
    private FileObjectFactory(final File rootFile) {
        this(new FileInfo(rootFile));
    }

    private FileObjectFactory(final FileInfo fInfo) {
        final File rootFile = fInfo.getFile();
        assert rootFile.getParentFile() == null;

        final BaseFileObj realRoot = create(fInfo);
        root = realRoot;
    }
    
    public static FileObjectFactory getInstance(final File file) {
        return getInstance(file, true);
    }

    public static FileObjectFactory getInstance(final File file, boolean addMising) {
        FileObjectFactory retVal = null;
        final FileInfo rootInfo = new FileInfo(file).getRoot();
        final File rootFile = rootInfo.getFile();

        synchronized (FileObjectFactory.AllFactories) {
            retVal = (FileObjectFactory) FileObjectFactory.AllFactories.get(rootFile);
        }
        if (retVal == null && addMising) {
            if (rootInfo.isConvertibleToFileObject()) {
                synchronized (FileObjectFactory.AllFactories) {
                    retVal = (FileObjectFactory) FileObjectFactory.AllFactories.get(rootFile);
                    if (retVal == null) {
                        retVal = new FileObjectFactory(rootFile);
                        FileObjectFactory.AllFactories.put(rootFile, retVal);
                    }
                }
            }
        }
        return retVal;
    }

    public static Collection getInstances() {
        synchronized (FileObjectFactory.AllFactories) {
            return new ArrayList(AllFactories.values());
        }
    }
    

    public final BaseFileObj getRoot() {
        return root;
    }

    public static int getFactoriesSize() {
        synchronized (FileObjectFactory.AllFactories) {
            return AllFactories.size();
        }
    }

    public int getSize() {
        int retval = 0;

        List list = new ArrayList();
        synchronized (allIBaseFileObjects) {
            list.addAll(allIBaseFileObjects.values());
        }
        List list2 = new ArrayList();


        for (Iterator it = list.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Reference) {
                list2.add(obj);
            } else {
                list2.addAll((List) obj);
            }
        }

        for (Iterator it = list2.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            FileObject fo = (ref != null) ? (FileObject) ref.get() : null;
            if (fo != null) {
                retval++;
            }
        }

        return retval;
    }


    public BaseFileObj getFileObject(FileInfo fInfo, Caller caller) {
        File file = fInfo.getFile();
        FileObject retVal = null;
        FolderObj parent = BaseFileObj.getExistingParentFor(file, this);
        FileNaming child = null;
        boolean isInitializedCache = true;
        if (parent != null) {
            final ChildrenCache childrenCache = parent.getChildrenCache();
            final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();
            mutexPrivileged.enterReadAccess();
            try {
                final String nameExt = BaseFileObj.getNameExt(file);
                isInitializedCache = childrenCache.isCacheInitialized();
                child = childrenCache.getChild(nameExt, false);
            } finally {
                mutexPrivileged.exitReadAccess();
            }
        }
        int initTouch = (isInitializedCache) ? -1 : (child != null ? 1 : 0);        
        if (initTouch == -1  && FileBasedFileSystem.isModificationInProgress()) {
            initTouch = file.exists() ? 1 : 0;
        }
        return issueIfExist(file, caller, parent, child, initTouch);
    }


    private boolean checkCacheState(boolean exist, File file, Caller caller) {
        return checkCacheState(exist, file, caller, false);
    }

    private boolean checkCacheState(boolean exist, File file, Caller caller, boolean afterRecovering) {
        if (!exist && (caller.equals(Caller.GetParent) || caller.equals(Caller.ToFileObject))) {
            return true;
        }
        if (isWarningEnabled() && caller != null && !caller.equals(Caller.GetChildern)) {
            boolean notsame = exist != file.exists();
            if (notsame) {
                if (afterRecovering) {
                    printWarning(file, Status.RecoverFail);
                } else {
                    printWarning(file, Status.NoRecover);
                }
            } else {
                if (afterRecovering) {
                    printWarning(file, Status.RecoverSuccess);
                }
            }
        }
        return true;
    }

    public static enum Status {

        RecoverSuccess, RecoverFail, NoRecover
    }

    private Integer initRealExists(int initTouch) {
        final Integer retval = new Integer(initTouch);
        return retval;
    }

    private void printWarning(File file, Status stat) {
        StringBuilder sb = new StringBuilder("WARNING(please REPORT):  Externally ");
        sb.append(file.exists() ? "created " : "deleted "); //NOI18N
        sb.append(file.isDirectory() ? "folder: " : "file: "); //NOI18N
        sb.append(file.getAbsolutePath());
        sb.append("  (For additional information see: http://wiki.netbeans.org/wiki/view/FileSystems)");//NOI18N        
        throw new AssertionError(sb.toString());
    }

    private BaseFileObj issueIfExist(File file, Caller caller, FileObject parent, FileNaming child, int initTouch) {
        boolean exist = false;
        BaseFileObj foForFile = null;
        Integer realExists = initRealExists(initTouch);
        final FileChangedManager fcb = FileChangedManager.getInstance();

        //use cached info as much as possible + do refresh if something is wrong
        //exist = (parent != null) ? child != null : (((foForFile = get(file)) != null && foForFile.isValid()) || touchExists(file, realExists));
        foForFile = getCachedOnly(file);
        if (parent != null && parent.isValid()) {
            if (child != null) {
                if (foForFile == null) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            parent.refresh();
                        }
                    }
                    assert checkCacheState(true, file, caller);
                } else if (foForFile.isValid()) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            parent.refresh();
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                } else {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist) {
                        parent.refresh();
                    }
                }
            } else {
                if (foForFile == null) {
                    exist = (realExists == -1) ? false : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                    }
                    assert checkCacheState(exist, file, caller);
                } else if (foForFile.isValid()) {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist) {
                        foForFile.refresh();
                    }
                } else {
                    exist = touchExists(file, realExists);
                    if (exist) {
                        parent.refresh();
                    }
                }
            }
        } else {
            if (foForFile == null) {
                exist = touchExists(file, realExists);
            } else if (foForFile.isValid()) {
                if (parent == null) {
                    exist = (realExists == -1) ? true : touchExists(file, realExists);
                    if (fcb.impeachExistence(file, exist)) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            foForFile.refresh();
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                } else {
                    //!!!!!!!!!!!!!!!!! inconsistence
                    exist = touchExists(file, realExists);
                    if (!exist) {
                        foForFile.refresh();
                    }
                }
            } else {
                exist = (realExists == -1) ? false : touchExists(file, realExists);
                if (fcb.impeachExistence(file, exist)) {
                    exist = touchExists(file, realExists);
                }
                assert checkCacheState(exist, file, caller);
            }
        }
        if (!exist) {
            switch (caller) {
                case GetParent:
                    //guarantee issuing parent
                    BaseFileObj retval = null;
                    if (foForFile != null && !foForFile.isRoot()) {
                        retval = foForFile;
                    } else {
                        retval = getOrCreate(new FileInfo(file, 1));
                    }
                    if (retval instanceof BaseFileObj && retval.isValid()) {
                        exist = touchExists(file, realExists);
                        if (!exist) {
                            //parent is exception must be issued even if not valid
                            ((BaseFileObj) retval).setValid(false);
                        }
                    }
                    assert checkCacheState(exist, file, caller);
                    return retval;
                case ToFileObject:
                    //guarantee issuing for existing file
                    exist = touchExists(file, realExists);
                    if (exist && parent != null && parent.isValid()) {
                        parent.refresh();
                    }
                    assert checkCacheState(exist, file, caller);
                    break;
            }
        }
        //ratio 59993/507 (means 507 touches for 59993 calls)
        return (exist) ? getOrCreate(new FileInfo(file, 1)) : null;
    }

    private static boolean touchExists(File f, Integer state) {
        if (state == -1) {
            state = FileChangedManager.getInstance().exists(f) ? 1 : 0;
        }
        assert state != -1;
        return (state == 1) ? true : false;
    }

    private final BaseFileObj getOrCreate(final FileInfo fInfo) {
        BaseFileObj retVal = null;
        File f = fInfo.getFile();

        boolean issue45485 = fInfo.isWindows() && f.getName().endsWith(".");//NOI18N        
        if (issue45485) {
            File f2 = FileUtil.normalizeFile(f);
            issue45485 = !f2.getName().endsWith(".");
            if (issue45485) {
                return null;
            }
        }
        synchronized (allIBaseFileObjects) {
            retVal = this.getCachedOnly(f);
            if (retVal == null || !retVal.isValid()) {
                final File parent = f.getParentFile();
                if (parent != null) {
                    retVal = this.create(fInfo);
                } else {
                    retVal = this.getRoot();
                }

            }
            return retVal;
        }
    }

    private BaseFileObj create(final FileInfo fInfo) {
        if (fInfo.isWindowsFloppy()) {
            return null;
        }

        if (!fInfo.isConvertibleToFileObject()) {
            return null;
        }

        final File file = fInfo.getFile();
        FileNaming name = fInfo.getFileNaming();
        name = (name == null) ? NamingFactory.fromFile(file) : name;

        if (name == null) {
            return null;
        }

        if (name.isFile() && !name.isDirectory()) {
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (!name.isFile() && name.isDirectory()) {
            final FolderObj realRoot = new FolderObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }

    public final void refreshAll(final boolean expected) {
        Set all2Refresh = collectForRefresh();
        refresh(all2Refresh, expected);
    }

    private Set collectForRefresh() {
        final Set all2Refresh = new HashSet();
        synchronized (allIBaseFileObjects) {
            final Iterator it = allIBaseFileObjects.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                if (obj instanceof List) {
                    for (Iterator iterator = ((List) obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null) {
                            all2Refresh.add(fo);
                        }
                    }
                } else {
                    final WeakReference ref = (WeakReference) obj;
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                    if (fo != null) {
                        all2Refresh.add(fo);
                    }
                }
            }
        }
        return all2Refresh;
    }

    private void refresh(final Set all2Refresh, File... files) {
        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final BaseFileObj fo = (BaseFileObj) iterator.next();
            for (File file : files) {
                if (isParentOf(file, fo.getFileName().getFile())) {
                    fo.refresh(true);
                    break;
                }                
            }
        }
    }    
    
    private void refresh(final Set all2Refresh, final boolean expected) {
        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final BaseFileObj fo = (BaseFileObj) iterator.next();
            fo.refresh(expected);
        }
    }

    public static boolean isParentOf(final File dir, final File file) {
        Stack stack = new Stack();
        File tempFile = file;
        while (tempFile != null && !tempFile.equals(dir)) {
            stack.push(tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        return tempFile != null;
    }

    public final void rename() {
        final Map toRename = new HashMap();
        synchronized (allIBaseFileObjects) {
            final Iterator it = allIBaseFileObjects.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                final Object obj = entry.getValue();
                final Integer key = (Integer) entry.getKey();
                if (!(obj instanceof List)) {
                    final WeakReference ref = (WeakReference) obj;

                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);

                    if (fo != null) {
                        Integer computedId = fo.getFileName().getId();
                        if (!key.equals(computedId)) {
                            toRename.put(key, fo);
                        }
                    }
                } else {
                    for (Iterator iterator = ((List) obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null) {
                            Integer computedId = fo.getFileName().getId();
                            if (!key.equals(computedId)) {
                                toRename.put(key, ref);
                            }
                        }
                    }

                }
            }

            for (Iterator iterator = toRename.entrySet().iterator(); iterator.hasNext();) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                Object key = entry.getKey();
                Object previous = allIBaseFileObjects.remove(key);
                if (previous instanceof List) {
                    List list = (List) previous;
                    list.remove(entry.getValue());
                    allIBaseFileObjects.put(key, previous);
                } else {
                    BaseFileObj bfo = (BaseFileObj) entry.getValue();
                    putInCache(bfo, bfo.getFileName().getId());
                }
            }
        }
    }

    public final BaseFileObj getCachedOnly(final File file) {
        final Object o;
        synchronized (allIBaseFileObjects) {
            final Object value = allIBaseFileObjects.get(NamingFactory.createID(file));
            Reference ref = null;
            ref = (Reference) (value instanceof Reference ? value : null);
            ref = (ref == null && value instanceof List ? FileObjectFactory.getReference((List) value, file) : ref);

            o = (ref != null) ? ref.get() : null;
            assert (o == null || o instanceof BaseFileObj);
        }
        BaseFileObj retval = (BaseFileObj) o;
        if (retval != null) {
            if (!file.getName().equals(retval.getNameExt())) {
                if (!file.equals(retval.getFileName().getFile())) {
                    retval = null;
                }
            }
        }
        return retval;
    }

    private static Reference getReference(final List list, final File file) {
        Reference retVal = null;
        for (int i = 0; retVal == null && i < list.size(); i++) {
            final Reference ref = (Reference) list.get(i);
            final BaseFileObj cachedElement = (ref != null) ? (BaseFileObj) ref.get() : null;
            if (cachedElement != null && cachedElement.getFileName().getFile().compareTo(file) == 0) {
                retVal = ref;
            }
        }
        return retVal;
    }

    private BaseFileObj putInCache(final BaseFileObj newValue, final Integer id) {
        synchronized (allIBaseFileObjects) {
            final WeakReference newRef = new WeakReference(newValue);
            final Object listOrReference = allIBaseFileObjects.put(id, newRef);

            if (listOrReference != null) {
                if (listOrReference instanceof List) {
                    ((List) listOrReference).add(newRef);
                    allIBaseFileObjects.put(id, listOrReference);
                } else {
                    assert (listOrReference instanceof WeakReference);
                    final Reference oldRef = (Reference) listOrReference;
                    BaseFileObj oldValue = (oldRef != null) ? (BaseFileObj) oldRef.get() : null;

                    if (oldValue != null && !newValue.getFileName().equals(oldValue.getFileName())) {
                        final List l = new ArrayList();
                        l.add(oldRef);
                        l.add(newRef);
                        allIBaseFileObjects.put(id, l);
                    }
                }
            }
        }

        return newValue;
    }

    @Override
    public String toString() {
        List list = new ArrayList();
        synchronized (allIBaseFileObjects) {
            list.addAll(allIBaseFileObjects.values());
        }
        List l2 = new ArrayList();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            FileObject fo = (ref != null) ? (FileObject) ref.get() : null;
            if (fo != null) {
                l2.add(fo.getPath());
            }
        }


        return l2.toString();
    }

    public static synchronized Map<File, FileObjectFactory> factories() {
        return new HashMap<File, FileObjectFactory>(AllFactories);
    }

    public boolean isWarningEnabled() {
        return WARNINGS && !Utilities.isMac();
    }

    //only for tests purposes
    public static void reinitForTests() {
        FileObjectFactory.AllFactories = new HashMap();
    }



    public final BaseFileObj getValidFileObject(final File f, FileObjectFactory.Caller caller) {
        final BaseFileObj retVal = (getFileObject(new FileInfo(f), caller));
        return (retVal != null && retVal.isValid()) ? retVal : null;
    }

    public final void refresh(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        final Runnable r = new Runnable() {
            public void run() {
                refreshAll(expected);                
            }            
        };        
        
        stopWatch.start();
        try {
            FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileBasedFileSystem.runAsInconsistent(r);
                }
            });
        } catch (IOException iex) {/*method refreshAll doesn't throw IOException*/

        }
        stopWatch.stop();

        // print refresh stats unconditionally in trunk
        Logger.getLogger("org.netbeans.modules.masterfs.REFRESH").fine(
                "FS.refresh statistics (" + Statistics.fileObjects() + "FileObjects):\n  " +
                Statistics.REFRESH_FS.toString() + "\n  " +
                Statistics.LISTENERS_CALLS.toString() + "\n  " +
                Statistics.REFRESH_FOLDER.toString() + "\n  " +
                Statistics.REFRESH_FILE.toString() + "\n");

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }

    public final void refreshFor(final File... files) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FS);
        final Runnable r = new Runnable() {
            public void run() {
                Set all2Refresh = collectForRefresh();
                refresh(all2Refresh, files);
            }            
        };        
        stopWatch.start();
        try {
            FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileBasedFileSystem.runAsInconsistent(r);
                }
            });
        } catch (IOException iex) {/*method refreshAll doesn't throw IOException*/

        }
        stopWatch.stop();

        // print refresh stats unconditionally in trunk
        Logger.getLogger("org.netbeans.modules.masterfs.REFRESH").fine(
                "FS.refresh statistics (" + Statistics.fileObjects() + "FileObjects):\n  " +
                Statistics.REFRESH_FS.toString() + "\n  " +
                Statistics.LISTENERS_CALLS.toString() + "\n  " +
                Statistics.REFRESH_FOLDER.toString() + "\n  " +
                Statistics.REFRESH_FILE.toString() + "\n");

        Statistics.REFRESH_FS.reset();
        Statistics.LISTENERS_CALLS.reset();
        Statistics.REFRESH_FOLDER.reset();
        Statistics.REFRESH_FILE.reset();
    }

}
