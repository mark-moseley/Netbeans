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
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.Attributes;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Mutex;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;

/**
 * Implements FileObject methods as simple as possible.
 *
 * @author Radek Matous
 */
//TODO: listeners still kept in EventListenerList
//TODO: check string constants and put them in bundles (DONE) but needs some sort of checking
//TODO: consider caching of isValid value not to call ever file.exists ()

public abstract class BaseFileObj extends FileObject {
    //constants
    private static final char EXTENSION_SEPARATOR = '.';
    private static final char UNC_PREFIX = '\\';//NOI18N
    private static final String PATH_SEPARATOR = "/";//NOI18N
    private static final char EXT_SEP = '.';//NOI18N
    
    //static fields 
    static final long serialVersionUID = -1244650210876356809L;
    static final Attributes attribs;
    static {
        final BridgeForAttributes attrBridge = new BridgeForAttributes();
        attribs = new Attributes(attrBridge, attrBridge, attrBridge);
    }


    //private fields
    private final EventListenerList eventSupport = new EventListenerList();
    private final FileNaming fileName;

    


    protected BaseFileObj(final File file) {
        this.fileName = NamingFactory.fromFile(file);
        //setValid(true)/*file.exists()*/;
        isValid (true);
    }

    public final String toString() {
        return getFileName().toString();
    }

    public final String getNameExt() {
        final File file = getFileName().getFile();
        final String retVal = BaseFileObj.getNameExt(file);
        return retVal;

    }

    static String getNameExt(final File file) {
        String retVal = (file.getParentFile() == null) ? file.getAbsolutePath() : file.getName();
        if (retVal.endsWith(String.valueOf(UNC_PREFIX)) || retVal.endsWith(PATH_SEPARATOR)) {//NOI18N
            assert (file.getParentFile() == null);
            final boolean isPermittedToStripSlash = !(file.getParentFile() == null && new FileInfo(file).isUNCFolder());
            if (isPermittedToStripSlash) {
                retVal = retVal.substring(0, retVal.length() - 1);
            }

        }
        return retVal;
    }

    public final boolean isData() {
        return !isFolder();
    }

    public final String getName() {
        final String name = getFileName ().getName ();
        final int i = name.lastIndexOf(EXTENSION_SEPARATOR);
        /** period at first position is not considered as extension-separator */
        String retVal = i <= 0 || i == name.length() ? name : name.substring(0, i); // NOI18N;
        assert assertGetNameExt(name, retVal, getExt());
        if (retVal.endsWith(String.valueOf(UNC_PREFIX)) || retVal.endsWith(PATH_SEPARATOR)) {//NOI18N
            //TODO: UNCPath workaround
            File file = getFileName ().getFile ();
            boolean isPermittedToStripSlash = !(file.getParentFile() == null && new FileInfo(file).isUNCFolder());
            if (isPermittedToStripSlash) {
                assert (file.getParentFile() == null);
                retVal = retVal.substring(0, retVal.length() - 1);
            }
        }
        return retVal;
    }

    public final String getExt() {
        final String name = getFileName ().getName ();
        final int i = name.lastIndexOf(EXTENSION_SEPARATOR) + 1;
        /** period at first position is not considered as extension-separator */
        String retVal = i <= 1 || i == name.length() ? "" : name.substring(i); // NOI18N;
        if (retVal.endsWith(String.valueOf(EXTENSION_SEPARATOR)) || retVal.endsWith(PATH_SEPARATOR)) {//NOI18N
            retVal = retVal.substring(0, retVal.length() - 1);
        }

        return retVal;
    }

    private static boolean assertGetNameExt(final String nameExt, final String name, final String ext) {
        final String computedNameExt;
        final boolean retVal;
        if (ext == null || ext.length() == 0) {
            computedNameExt = name;
        } else {
            computedNameExt = name + EXTENSION_SEPARATOR + ext;//NOI18N
        }
        retVal = computedNameExt.equals(nameExt);
        assert retVal : (computedNameExt + " | " + nameExt);//NOI18N
        return retVal;
    }

    public final String getPath() {
        return (isRoot()) ? "" : getFileName().getFile().getAbsolutePath().replace(UNC_PREFIX, '/');//NOI18N
    }

    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return getLocalFileSystem();
    }

    public final boolean isRoot() {
        return false;
    }

    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        final File file = getFileName().getFile();
        final File parent = file.getParentFile();

        final File file2Rename = BaseFileObj.getFile(parent, name, ext);
        if (parent == null || !parent.exists()) {
            FSException.io("EXC_CannotRename", file.getName(), getFileSystem().getDisplayName(), file2Rename.getName()); // NOI18N                            
        }

        final String originalName = getName();
        final String originalExt = getExt();

        if (file2Rename.exists()) {
            FSException.io("EXC_CannotRename", file.getName(), getFileSystem().getDisplayName(), file2Rename.getName()); // NOI18N                                        
        }
        
        //TODO: no lock used
        if (!NamingFactory.rename(getFileName(),file2Rename.getName())) {
            FSException.io("EXC_CannotRename", file.getName(), getFileSystem().getDisplayName(), file2Rename.getName()); // NOI18N                                        
        }

        FileBasedFileSystem fs = getLocalFileSystem();
        fs.getFactory().rename(); 
        BaseFileObj.attribs.renameAttributes(file.getAbsolutePath().replace('\\', '/'), file2Rename.getAbsolutePath().replace('\\', '/'));//NOI18N
        fireFileRenamedEvent(originalName, originalExt);
    }


    public final Object getAttribute(final String attrName) {
        //TODO: convert absolute path into '/'
        return BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
    }

    public final void setAttribute(final String attrName, final Object value) throws java.io.IOException {
        final Object oldValue = BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
        BaseFileObj.attribs.writeAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName, value);//NOI18N
        fireFileAttributeChangedEvent(attrName, oldValue, value);
    }

    public final java.util.Enumeration getAttributes() {
        return BaseFileObj.attribs.attributes(getFileName().getFile().getAbsolutePath().replace('\\', '/'));//NOI18N
    }

    public final void addFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        eventSupport.add(FileChangeListener.class, fcl);
    }

    public final void removeFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        eventSupport.remove(FileChangeListener.class, fcl);
    }

    private Enumeration getListeners() {
        return org.openide.util.Enumerations.array(eventSupport.getListeners(FileChangeListener.class));
    }


    public final long getSize() {
        return getFileName().getFile().length();
    }

    public final void setImportant(final boolean b) {
    }


    public final boolean isReadOnly() {
        final File f = getFileName().getFile();
        assert f.exists() || !isValid(true) ;

        return !f.canWrite() && f.exists();
    }

    public final FileObject getParent() {
        final FileNaming parent = getFileName().getParent();
        FileObject retVal;
        if ((parent != null)) {
            final FileBasedFileSystem localFileSystem = getLocalFileSystem();
            final File file = parent.getFile();
            retVal = localFileSystem.getFactory().get(file);
            retVal = (retVal == null) ? localFileSystem.findFileObject(file) : retVal;
            assert retVal == null || ((BaseFileObj) retVal).getFileName().getFile().equals(this.getFileName().getFile().getParentFile()) ;
        } else {
            retVal = getLocalFileSystem().getRoot();
        }
        return retVal;
    }


    public final boolean isValid(boolean refresh) {            
        if (refresh) {
            setValid(getFileName().getFile().exists());                 
        }
        
        return isValid();/*getFileName().getFile().exists()*/
    }

    
    static File getFile(final File f, final String name, final String ext) {
        File retVal;

        final StringBuffer sb = new StringBuffer();
        sb.append(name);
        if (ext != null && ext.length() > 0) {
            sb.append(BaseFileObj.EXT_SEP);
            sb.append(ext);
        }
        retVal = new File(f, sb.toString());
        return retVal;
    }

    final FileBasedFileSystem getLocalFileSystem() {
        return FileBasedFileSystem.getInstance(getFileName().getFile());
    }

    static boolean createRecursiveFolder(File f) {
        if (f.exists()) return true;
        if (!f.isAbsolute())
            f = f.getAbsoluteFile();
        final String par = f.getParent();
        if (par == null) return false;
        if (!BaseFileObj.createRecursiveFolder(new File(par))) return false;
        f.mkdir();
        return f.exists();
    }


    final void fireFileDataCreatedEvent(final boolean expected) {
        assert getFileName().getFile().exists();
        assert getFileName().getFile().isFile();         
        fireFileDataCreatedEvent(getListeners(), new FileEvent(this, this, expected));

        final BaseFileObj parent = getExistingParent();
        parent.fireFileDataCreatedEvent(parent.getListeners(), new FileEvent(parent, this, expected));
    }


    final void fireFileFolderCreatedEvent(final boolean expected) {
        assert getFileName().getFile().exists();
        assert getFileName().getFile().isDirectory(); 
        fireFileFolderCreatedEvent(getListeners(), new FileEvent(this, this, expected));

        final BaseFileObj parent = getExistingParent();
        if (parent != null) {
            parent.fireFileFolderCreatedEvent(parent.getListeners(), new FileEvent(parent, this, expected));
        }

    }

    FolderObj getExistingParent() {
        final File parentFile = getFileName().getParent().getFile();
        final FolderObj parent = (FolderObj) getLocalFileSystem().getFactory().get(parentFile);
        return parent;
    }


    public final void fireFileChangedEvent(final boolean expected) {
        assert getFileName().getFile().exists();        
        fireFileChangedEvent(getListeners(), new FileEvent(this, this, expected));

        final BaseFileObj parent = getExistingParent();
        if (parent != null) {
            parent.fireFileChangedEvent(parent.getListeners(), new FileEvent(parent, this, expected));
        }
    }


    final void fireFileDeletedEvent(final boolean expected) {
        assert !getFileName().getFile().exists(); 
        fireFileDeletedEvent(getListeners(), new FileEvent(this, this, expected));

        final BaseFileObj parent = getExistingParent();
        if (parent != null) {
            parent.fireFileDeletedEvent(parent.getListeners(), new FileEvent(parent, this, expected));
        }
    }


    private void fireFileRenamedEvent(final String originalName, final String originalExt) {
        //assert getFileName().getFile().exists();         
        fireFileRenamedEvent(getListeners(), new FileRenameEvent(this, originalName, originalExt));

        final BaseFileObj parent = getExistingParent();
        if (parent != null) {
            parent.fireFileRenamedEvent(parent.getListeners(), new FileRenameEvent(parent, this, originalName, originalExt));
        }
    }

    private void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(this, this, attrName, oldValue, newValue));

        final BaseFileObj parent = getExistingParent();
        if (parent != null) {
            parent.fireFileAttributeChangedEvent(parent.getListeners(), new FileAttributeEvent(parent, this, attrName, oldValue, newValue));

        }

    }


    public final FileNaming getFileName() {
        return fileName;
    }

    public void delete(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        assert f.exists() || !isValid(true) ;

        final FolderObj existingParent = getExistingParent();
        final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;
        final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;

        if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
        try {
            if (!checkLock(lock)) {
                FSException.io("EXC_InvalidLock", lock, getPath(), getFileSystem().getDisplayName(), f.getAbsolutePath()); // NOI18N                
            }

            if (!f.delete()) {
                FSException.io("EXC_CannotDelete", f.getName(), f.getParentFile().getAbsolutePath(), f.getAbsolutePath()); // NOI18N                                
            } 
            BaseFileObj.attribs.deleteAttributes(f.getAbsolutePath().replace('\\', '/'));//NOI18N
            if (childrenCache != null) childrenCache.getChild(BaseFileObj.getNameExt(f), true);
        } finally {
            if (mutexPrivileged != null) mutexPrivileged.exitWriteAccess();
            isValid(true);
        }

        fireFileDeletedEvent(false);

    }

    abstract boolean checkLock(FileLock lock) throws IOException;

    public Object writeReplace() {
        return new ReplaceForSerialization(getFileName().getFile());
    }

    abstract protected void setValid(boolean valid);

    //TODO: attributes written by VCS must be readable by FileBaseFS and vice versa  
/**
 * FileBaseFS 
 * <fileobject name="E:\work\nb_all8\openide\masterfs\src\org\netbeans\modules\masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="S"/>
 *
 * VCS FS
 * </fileobject>
 * <fileobject name="e:|work|nb_all8openide|masterfs|src|org|netbeans|modules|masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="F"/>
 *  
 */    
    private static final class BridgeForAttributes implements AbstractFileSystem.List, AbstractFileSystem.Change, AbstractFileSystem.Info {
        public final Date lastModified(final String name) {
            final File file = new File(name);
            return new Date(file.lastModified());
        }

        public final boolean folder(final String name) {
            final File file = new File(name);
            return file.isDirectory();
        }

        public final boolean readOnly(final String name) {
            final File file = new File(name);
            return !file.canWrite();

        }

        public final String mimeType(final String name) {
            return "content/unknown"; // NOI18N;
        }

        public final long size(final String name) {
            final File file = new File(name);
            return file.length();
        }

        public final InputStream inputStream(final String name) throws FileNotFoundException {
            final File file = new File(name);
            return new FileInputStream(file);

        }

        public final OutputStream outputStream(final String name) throws IOException {
            final File file = new File(name);
            return new FileOutputStream(file);
        }

        public final void lock(final String name) throws IOException {
        }

        public final void unlock(final String name) {
        }

        public final void markUnimportant(final String name) {
        }

        public final String[] children(final String f) {
            final File file = new File(f);
            return file.list();
        }

        public final void createFolder(final String name) throws IOException {
            final File file = new File(name);
            if (!file.mkdirs()) {
                final IOException ioException = new IOException(name);
                throw ioException;
            }
        }

        public final void createData(final String name) throws IOException {
            final File file = new File(name);
            if (!file.createNewFile()) {
                throw new IOException(name);
            }
        }

        public final void rename(final String oldName, final String newName) throws IOException {
            final File file = new File(oldName);
            final File dest = new File(newName);

            if (!file.renameTo(dest)) {
                FSException.io("EXC_CannotRename", file.getName(), "", dest.getName()); // NOI18N                
            }
        }

        public final void delete(final String name) throws IOException {
            final File file = new File(name);
            final boolean isDeleted = (file.isFile()) ? file.delete() : deleteFolder(file);
            if (isDeleted) {
                FSException.io("EXC_CannotDelete", file.getName(), "", file.getAbsolutePath()); // NOI18N                                
            }
        }

        private boolean deleteFolder(final File file) throws IOException {
            final boolean ret = file.delete();

            if (ret) {
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
                    if (!deleteFolder(f2Delete)) {
                        return false;
                    }
                }
            }

            return file.delete();
        }

    }
}
