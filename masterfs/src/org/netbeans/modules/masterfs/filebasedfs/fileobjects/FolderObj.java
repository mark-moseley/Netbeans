/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.FolderName;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

import java.io.*;
import java.util.*;

/**
 * @author rm111737
 */
public final class FolderObj extends BaseFileObj {
    static final long serialVersionUID = -1022430210876356809L;
    private static final Mutex.Privileged mp = new Mutex.Privileged();
    private static final Mutex mutex = new Mutex(FolderObj.mp);

    private final FolderChildrenCache folderChildren = new FolderChildrenCache();
    boolean valid;


    /**
     * Creates a new instance of FolderImpl
     */
    public FolderObj(final File file) {
        super(file);
        //assert getFileName().getFile().isDirectory() : getFileName().getFile().getAbsolutePath();
    }

    public final boolean isFolder() {
        return true;
    }


    public final FileObject getFileObject(final String name, final String ext) {
        if (!isValid(true)) {
            return null;   
        }
        
        FileObject retVal = null;
        final File f = getFileName().getFile();
        final ChildrenCache childrenCache = getChildrenCache();        
        FileNaming child;
        File file;
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterReadAccess();
        try {
            file = BaseFileObj.getFile(f, name, ext);
            final String nameExt = BaseFileObj.getNameExt(file);
            child = childrenCache.getChild(nameExt, false);
        } finally {
            mutexPrivileged.exitReadAccess();
        }

        final FileBasedFileSystem lfs = getLocalFileSystem();
        assert lfs != null;

        if (child != null) {
            retVal = lfs.findFileObject(file);
        }

        return retVal;
    }

    public final FileObject[] getChildren() {
        if (!isValid(true)) {
            return new FileObject [] {};   
        }
        
        final List results = new ArrayList();

        final ChildrenCache childrenCache = getChildrenCache();
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterWriteAccess();

        Set fileNames;
        try {
            fileNames = new HashSet(childrenCache.getChildren(true));
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        for (Iterator iterator = fileNames.iterator(); iterator.hasNext();) {
            final FileNaming fileName = (FileNaming) iterator.next();
            final FileBasedFileSystem lfs = getLocalFileSystem();
            final FileObject fo = lfs.findFileObject(fileName.getFile());
            if (fo != null) {
                results.add(fo);
            }
        }
        return (FileObject[]) results.toArray(new FileObject[0]);
    }

    public final FileObject createFolder(final String name) throws java.io.IOException {
        FolderObj retVal = null;
        File folder2Create;
        if (!isValid(true)) {
            //TODO: annotate, NOI18N + bundle 
            throw new IOException(getPath ());//I18    
        }        
        final ChildrenCache childrenCache = getChildrenCache();
        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterWriteAccess();

        try {
            folder2Create = BaseFileObj.getFile(getFileName().getFile(), name, null);
            if (folder2Create.exists()) {
                FSException.io("EXC_CannotCreateF", new Object[]{folder2Create.getName(), getFileSystem().getDisplayName(), folder2Create.getAbsolutePath()}); // NOI18N                
            }

            if (!BaseFileObj.createRecursiveFolder(folder2Create)) {
                FSException.io("EXC_CannotCreateF", new Object[]{folder2Create.getName(), getFileSystem().getDisplayName(), folder2Create.getAbsolutePath()}); // NOI18N                                
            }

            final FileNaming childName = this.getChildrenCache().getChild(folder2Create.getName(), true);
            assert childName != null;
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileBasedFileSystem localFileBasedFileSystem = getLocalFileSystem();
        if (localFileBasedFileSystem != null) {
            retVal = (FolderObj) localFileBasedFileSystem.findFileObject(folder2Create);
        }
        assert retVal != null : folder2Create.getAbsolutePath();
        retVal.fireFileFolderCreatedEvent(false);

        return retVal;
    }

    public final FileObject createData(final String name, final String ext) throws java.io.IOException {
        if (!isValid(true)) {
            //TODO: annotate, NOI18N + bundle 
            throw new IOException(getPath ());//I18    
        }
        final ChildrenCache childrenCache = getChildrenCache();        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterWriteAccess();

        FileObj retVal;
        File f;
        try {
            f = BaseFileObj.getFile(getFileName().getFile(), name, ext);
            boolean isError = f.createNewFile() ? false : true;
            isError = isError ? true : !f.exists();

            if (isError) {
                FSException.io("EXC_CannotCreateD", name, ext, getFileName().getFile().getName(), getFileSystem().getDisplayName());// NOI18N
            }

            final FileNaming childName = getChildrenCache().getChild(f.getName(), true);
            assert childName != null;

        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileBasedFileSystem localFileBasedFileSystem = getLocalFileSystem();
        retVal = null;
        if (localFileBasedFileSystem != null) {
            retVal = (FileObj) localFileBasedFileSystem.findFileObject(f);
        }

        assert retVal != null;
        retVal.fireFileDataCreatedEvent(false);

        return retVal;
    }

    public final void delete(final FileLock lock) throws IOException {
        if (!isValid(true)) {
            //TODO: annotate, NOI18N + bundle 
            throw new IOException(getPath ());//I18    
        }        
        final LinkedList all = new LinkedList();

        final File file = getFileName().getFile();
        if (!deleteFile(file, all, getLocalFileSystem().getFactory())) {
            FSException.io("EXC_CannotDelete", file.getName(), getFileSystem().getDisplayName(), file.getAbsolutePath()); // NOI18N                            
        }

        BaseFileObj.attribs.deleteAttributes(file.getAbsolutePath().replace('\\', '/'));//NOI18N

        for (int i = 0; i < all.size(); i++) {
            final BaseFileObj toDel = (BaseFileObj) all.get(i);
            toDel.isValid(true);
            toDel.fireFileDeletedEvent(false);
        }
    }


    public final void refresh(final boolean expected) {
        isValid(true);
        final ChildrenCache cache = getChildrenCache();
        final Mutex.Privileged mutexPrivileged = cache.getMutexPrivileged();

        Set oldChildren = null;
        Map refreshResult = null;
        mutexPrivileged.enterWriteAccess();
        try {
            oldChildren = cache.getChildren(false);
            refreshResult = cache.refresh();
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        oldChildren.removeAll(refreshResult.keySet());
        for (Iterator iterator = oldChildren.iterator(); iterator.hasNext();) {
            final FileName child = (FileName) iterator.next();
            final BaseFileObj childObj = getLocalFileSystem().getFactory().get(child.getFile());
            if (childObj != null && childObj.isData()) {
                childObj.refresh(expected);
            }
        }

        final Iterator iterator = refreshResult.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final FileName child = (FileName) entry.getKey();
            final Integer operationId = (Integer) entry.getValue();

            final FileBasedFileSystem localFileSystem = this.getLocalFileSystem();
            final FileObjectFactory factory = localFileSystem.getFactory();
            BaseFileObj newChild = factory.get(child.getFile());
            newChild = (BaseFileObj) ((newChild != null) ? newChild : getFileObject(child.getName()));
            if (operationId == ChildrenCache.ADDED_CHILD && newChild != null) {
                if (newChild.isFolder()) {
                    newChild.fireFileFolderCreatedEvent(expected);
                } else {
                    newChild.fireFileDataCreatedEvent(expected);
                }

            } else if (operationId == ChildrenCache.REMOVED_CHILD) {
                if (newChild != null) {
                    newChild.fireFileDeletedEvent(expected);
                } else {
                    //TODO: should be rechecked
                    //assert false;
                    final File f = child.getFile();
                    if (!(new FileInfo(f).isConvertibleToFileObject())) {
                        final BaseFileObj fakeInvalid;
                        if (child instanceof FolderName) {
                            fakeInvalid = new FolderObj(f);
                        } else {
                            fakeInvalid = new FileObj(f);
                        }

                        fakeInvalid.fireFileDeletedEvent(expected);
                    }
                }

            } else {
                assert !(new FileInfo(child.getFile()).isConvertibleToFileObject());
            }

        }
    }

    //TODO: rewrite partly and check FileLocks for existing FileObjects
    private boolean deleteFile(final File file, final LinkedList all, final FileObjectFactory factory) throws IOException {
        final boolean ret = file.delete();

        if (ret) {
            final FileObject aliveFo = factory.get(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
            return true;
        }

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            // first of all delete whole content
            final File[] arr = file.listFiles();
            for (int i = 0; i < arr.length; i++) {
                final File f2Delete = arr[i];
                if (!deleteFile(f2Delete, all, factory)) {
                    return false;
                }
            }
        } 
        
        // delete the file itself
        //super.delete(lock());
        

        final boolean retVal = file.delete();
        if (retVal) {
            final FileObject aliveFo = factory.get(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
        }


        return true;
    }

    protected void setValid(boolean valid) {
        this.valid = valid; 
    }

    public boolean isValid() {
        return valid;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new IOException(getPath());
    }

    public final java.util.Date lastModified() {
        final File f = getFileName().getFile();
        assert f.exists() || !isValid(true) ;

        return new Date(f.lastModified());
    }

    public final FileLock lock() throws IOException {
        return new FileLock();
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        return true;
    }

    public final ChildrenCache getChildrenCache() {
        assert getFileName().getFile().isDirectory() || !getFileName().getFile().exists();
        return folderChildren;
    }

    public final class FolderChildrenCache implements ChildrenCache {
        final ChildrenSupport ch = new ChildrenSupport();


        public final Set getChildren(final boolean rescan) {
            return ch.getChildren(getFileName(), rescan);
        }

        public final FileNaming getChild(final String childName, final boolean rescan) {
            return ch.getChild(childName, getFileName(), rescan);
        }

        public final Map refresh() {
            return ch.refresh(getFileName());
        }

        public final Mutex.Privileged getMutexPrivileged() {
            return FolderObj.mp;
        }

        public final String toString() {
            return getFileName().toString();
        }
    }

}
