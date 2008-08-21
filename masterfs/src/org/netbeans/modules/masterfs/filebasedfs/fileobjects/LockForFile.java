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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * FileLock with support for fine grained hard locking to ensure better performance
 * @author Radek Matous
 */
public class LockForFile extends FileLock {

    private static final ConcurrentHashMap<String, Namesakes> name2Namesakes =
            new ConcurrentHashMap<String, Namesakes>();
    private static final String PREFIX = ".LCK";
    private static final String SUFFIX = "~";
    private File file;
    private File lock;
    private boolean valid = false;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                hardUnlockAll();
            }
        });
    }

    private LockForFile(File file) {
        super();
        this.file = file;
        this.lock = getLockFile(file);
    }

    public static LockForFile findValid(final File file) {
        Namesakes namesakes = name2Namesakes.get(file.getName());
        return (namesakes != null) ? namesakes.getInstance(file) : null;
    }

    public static LockForFile tryLock(final File file) throws IOException {
        LockForFile result = new LockForFile(file);
        return registerLock(result);
    }

    private static LockForFile registerLock(LockForFile result) throws IOException, FileAlreadyLockedException {
        File file = result.getFile();
        Namesakes namesakes = new Namesakes();
        Namesakes oldNamesakes = name2Namesakes.putIfAbsent(file.getName(), namesakes);
        if (oldNamesakes != null) { 
            namesakes = oldNamesakes;
        }
        if (namesakes.putInstance(file, result) == null) {
            FileAlreadyLockedException alreadyLockedException = new FileAlreadyLockedException(file.getAbsolutePath());
            alreadyLockedException.initCause(result.lockedBy);
            throw alreadyLockedException;
        }
        result.valid = true;
        return result;
    }

    public static void relock(final File theOld, File theNew) {
        if (theNew.isDirectory()) {
            Collection<Namesakes> namesakes = name2Namesakes.values();
            for (Namesakes sake : namesakes) {
                Collection<Reference<LockForFile>> all = sake.values();
                for (Reference<LockForFile> ref : all) {
                    LockForFile lock = ref.get();
                    if (lock != null) {
                        File f = lock.getFile();
                        String relPath = Utils.getRelativePath(theOld, f);
                        if (relPath != null) {
                            lock.relock(new File(theNew, relPath));
                        }
                    }
                }
            }
        } else {
            LockForFile lock = findValid(theOld);
            if (lock != null) {
                lock.relock(theNew);
            }
        }
    }


    private static synchronized void deregisterLock(LockForFile lockForFile) {
        if (lockForFile.isValid()) {
            if (lockForFile.isHardLocked()) {
                lockForFile.hardUnlock();
            }
            File file = lockForFile.getFile();
            Namesakes namesakes = name2Namesakes.get(file.getName());
            if (namesakes != null) {
                namesakes.remove(file);
                if (namesakes.isEmpty()) {
                    name2Namesakes.remove(file.getName());
                }
            }
        }
    }

    private void relock(File theNew) {
        try {
            LockForFile.deregisterLock(this);
            this.file = theNew;
            this.lock = LockForFile.getLockFile(theNew);
            registerLock(this);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /*not private for tests*/
    boolean hardLock() throws IOException {
        if (isHardLocked()) {
            throw new FileAlreadyLockedException(file.getAbsolutePath());
        }
        File lock = getLock();
        lock.getParentFile().mkdirs();
        lock.createNewFile();
        OutputStream os = new FileOutputStream(lock);
        try {
            os.write(getFile().getAbsolutePath().getBytes());
            return true;
        } finally {
            os.close();
        }
    }

    /*not private for tests*/
    boolean hardUnlock() {
        File lock = getLock();
        return lock.delete();
    }

    private static synchronized boolean hardUnlockAll() {
        boolean result = true;
        Collection<Namesakes> sakes = name2Namesakes.values();
        for (LockForFile.Namesakes namesake : sakes) {
            Collection<Reference<LockForFile>> refs = namesake.values();
            for (Reference<LockForFile> reference : refs) {
                if (reference != null) {
                    LockForFile lockForFile = reference.get();
                    if (lockForFile.isHardLocked()) {
                        if (!lockForFile.hardUnlock()) {
                            result = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    public File getLock() {
        return lock;
    }

    public File getFile() {
        return file;
    }

    public File getHardLock() {
        if (FileChangedManager.getInstance().exists(lock)) {
            InputStream is = null;
            try {
                is = new FileInputStream(lock);
                byte[] path = new byte[is.available()];
                if (path.length > 0 && is.read(path) == path.length) {
                    return new File(new String(path));
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
    }

    public boolean isHardLocked() {
        File hLock = getHardLock();
        return (hLock != null) ? findValid(hLock) != null : false;
    }

    public void rename() {

    }

    public static File getLockFile(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException iex) {
            Exceptions.printStackTrace(iex);
        }

        final File parentFile = file.getParentFile();
        final StringBuilder sb = new StringBuilder();

        sb.append(LockForFile.PREFIX);//NOI18N
        sb.append(file.getName());//NOI18N
        sb.append(LockForFile.SUFFIX);//NOI18N

        final String lckName = sb.toString();
        final File lck = new File(parentFile, lckName);
        return lck;
    }

    @Override
    public boolean isValid() {
        Namesakes namesakes = name2Namesakes.get(file.getName());
        Reference<LockForFile> ref = (namesakes != null) ? namesakes.get(file) : null;
        return (ref != null && super.isValid() && valid);
    }

    @Override
    public void releaseLock() {
        LockForFile.deregisterLock(this);
        super.releaseLock();
        BaseFileObj fo = (BaseFileObj) FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fo != null) {
            fo.getProvidedExtensions().fileUnlocked(fo);
        }
    }

    private static class Namesakes extends ConcurrentHashMap<File, Reference<LockForFile>> {

        private LockForFile getInstance(File file) {
            Reference<LockForFile> ref = get(file);
            return (ref != null) ? ref.get() : null;
        }

        private LockForFile putInstance(File file, LockForFile lock) throws IOException {
            if (!isEmpty() && findValid(lock.getFile()) == null) {
                hardLock();
                lock.hardLock();
            }
            Reference<LockForFile> old = putIfAbsent(file, new WeakReference<LockForFile>(lock));
            return (old != null) ? null : lock;
        }

        private void hardLock() throws IOException {
            Collection<Reference<LockForFile>> refs = values();
            for (Reference<LockForFile> reference : refs) {
                if (reference != null) {
                    LockForFile lockForFile = reference.get();
                    if (lockForFile != null) {
                        if (!FileChangedManager.getInstance().exists(lockForFile.getLock())) {
                            lockForFile.hardLock();
                        }
                    }
                }
            }
        }
    }
}
