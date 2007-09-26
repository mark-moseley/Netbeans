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

import java.util.Iterator;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;

/**
 * Implements FileObject methods as simple as possible.
 *
 * @author Radek Matous
 */
//TODO: listeners still kept in EventListenerList

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
    private EventListenerList eventSupport;
    private final FileNaming fileName;


    protected BaseFileObj(final File file) {
        this.fileName = NamingFactory.fromFile(file);
    }
    
    protected BaseFileObj(final File file, final FileNaming name) {
        this.fileName = name;
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
            assert (file.getParentFile() == null) : 
                (file.getAbsolutePath()  + " exists: " + file.exists());//NOI18N
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
        return FileInfo.getName(getNameExt());
    }

    public final String getExt() {
        return FileInfo.getExt(getNameExt());
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

    public void move(FileLock lock, FolderObj target, String name, String ext, ProvidedExtensions.IOHandler moveHandler) throws IOException {
        moveHandler.handle();
        String nameExt = FileInfo.composeName(name,ext);
        target.getChildrenCache().getChild(nameExt, true);
        BaseFileObj result = (BaseFileObj)FileBasedFileSystem.getFileObject(
                new File(target.getFileName().getFile(),nameExt));
        assert result != null;
        result.fireFileDataCreatedEvent(false);
        fireFileDeletedEvent(false);
    }


    public void rename(final FileLock lock, final String name, final String ext, ProvidedExtensions.IOHandler handler) throws IOException {
        if (!checkLock(lock)) {
            FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N
        }
        
        final File file = getFileName().getFile();
        final File parent = file.getParentFile();

        final File file2Rename = BaseFileObj.getFile(parent, name, ext);
        if (parent == null || !parent.exists()) {
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, file2Rename.getName());// NOI18N            
        }
        boolean cannotRename = file2Rename.exists() && !file2Rename.equals(file);
        //#108690
        if (cannotRename && Utilities.isMac()) {
            final File parentFile2 = file2Rename.getParentFile();
            final File parentFile = file.getParentFile();
            if (parentFile2 != null && parentFile != null && parentFile.equals(parentFile2)) {
                if (file2Rename.getName().equalsIgnoreCase(file.getName())) {
                    cannotRename = false;
                }
            }
        }

        if (cannotRename) {
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, file2Rename.getName());// NOI18N            
        }        
        
        final String originalName = getName();
        final String originalExt = getExt();
        
        //TODO: no lock used
        FileNaming[] allRenamed = NamingFactory.rename(getFileName(),file2Rename.getName(),handler);
        if (allRenamed == null) {
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, file2Rename.getName());// NOI18N            
        }
        FileBasedFileSystem fs = getLocalFileSystem();
        fs.getFactory().rename(); 
        BaseFileObj.attribs.renameAttributes(file.getAbsolutePath().replace('\\', '/'), file2Rename.getAbsolutePath().replace('\\', '/'));//NOI18N
        for (int i = 0; i < allRenamed.length; i++) {
            FolderObj par = (allRenamed[i].getParent() != null) ? 
                (FolderObj)fs.getFactory().get(allRenamed[i].getParent().getFile()) : null;
            if (par != null) {
                ChildrenCache childrenCache = par.getChildrenCache();
                final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
                try {
                    childrenCache.removeChild(allRenamed[i]);
                    childrenCache.getChild(allRenamed[i].getName(), true);
                } finally {
                    if (mutexPrivileged != null) mutexPrivileged.exitWriteAccess();
                }                
            }
        }
        WriteLock.relock(file,file2Rename);
        fireFileRenamedEvent(originalName, originalExt);
    }


    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        rename(lock, name, ext, null);
    }


    public Object getAttribute(final String attrName) {
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
        getEventSupport().add(FileChangeListener.class, fcl);
    }

    public final void removeFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        getEventSupport().remove(FileChangeListener.class, fcl);
    }

    private Enumeration getListeners() {
        if (eventSupport == null) {
            return Enumerations.empty();
        }
        return org.openide.util.Enumerations.array(getEventSupport().getListeners(FileChangeListener.class));
    }


    public final long getSize() {
        return getFileName().getFile().length();
    }

    public final void setImportant(final boolean b) {
    }


    public boolean isReadOnly() {
        final File f = getFileName().getFile();

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
        } else {
            retVal = getLocalFileSystem().getRoot();
        }
        return retVal;
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

    final void fireFileDataCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();

        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;
        
        assert this.isValid() : this.toString();
        fireFileDataCreatedEvent(getListeners(), new FileEvent(this, this, expected));
        
        if (parent != null && pListeners != null) {
            assert parent.isValid() : parent.toString();
            parent.fireFileDataCreatedEvent(pListeners, new FileEvent(parent, this, expected));
        }
        stopWatch.stop();
    }


    final void fireFileFolderCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;
        
        fireFileFolderCreatedEvent(getListeners(), new FileEvent(this, this, expected));

        if (parent != null && pListeners != null) {
            parent.fireFileFolderCreatedEvent(pListeners, new FileEvent(parent, this, expected));
        }

        stopWatch.stop();
    }

    FolderObj getExistingParent() {         
        final File parentFile = (getFileName().getParent() == null) ? null : getFileName().getParent().getFile();
        final FolderObj parent = (parentFile == null) ? null : (FolderObj) getLocalFileSystem().getFactory().get(parentFile);
        return parent;
    }


    public final void fireFileChangedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        FileObject p = getParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;
        
        fireFileChangedEvent(getListeners(), new FileEvent(this, this, expected));

        if (parent != null && pListeners != null) {
            parent.fireFileChangedEvent(pListeners, new FileEvent(parent, this, expected));
        }
        stopWatch.stop();
    }


    final void fireFileDeletedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        FileObject p = getParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        
        
        fireFileDeletedEvent(getListeners(), new FileEvent(this, this, expected));

        if (parent != null && pListeners != null) {
            parent.fireFileDeletedEvent(pListeners, new FileEvent(parent, this, expected));
        }
        stopWatch.stop();
    }


    private void fireFileRenamedEvent(final String originalName, final String originalExt) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        
        
        fireFileRenamedEvent(getListeners(), new FileRenameEvent(this, originalName, originalExt));

        if (parent != null && pListeners != null) {
            parent.fireFileRenamedEvent(pListeners, new FileRenameEvent(parent, this, originalName, originalExt));
        }
        
        stopWatch.stop();
    }

    private void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        

        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(this, this, attrName, oldValue, newValue));

        if (parent != null && pListeners != null) {
            parent.fireFileAttributeChangedEvent(pListeners, new FileAttributeEvent(parent, this, attrName, oldValue, newValue));
        }
    }


    public final FileNaming getFileName() {
        return fileName;
    }
    
    public final void delete(final FileLock lock) throws IOException {
        delete(lock, null);
    }    

    public void delete(final FileLock lock, ProvidedExtensions.DeleteHandler deleteHandler) throws IOException {        
        final File f = getFileName().getFile();

        final FolderObj existingParent = getExistingParent();
        final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;
        final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;

        if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
        try {
            if (!checkLock(lock)) {
                FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N                
            }

            boolean deleteStatus = (deleteHandler != null) ? deleteHandler.delete(f) : f.delete();
            if (!deleteStatus) {
                FileObject parent = getExistingParent();
                String parentPath = (parent != null) ? parent.getPath() : f.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotDelete", f.getName(), parentPath);// NOI18N            
            } 
            BaseFileObj.attribs.deleteAttributes(f.getAbsolutePath().replace('\\', '/'));//NOI18N
            if (childrenCache != null) {
                if (deleteHandler != null) {
                    childrenCache.removeChild(getFileName());
                } else {
                    childrenCache.getChild(BaseFileObj.getNameExt(f), true);
                }
            }
        } finally {
            if (mutexPrivileged != null) mutexPrivileged.exitWriteAccess();
            setValid(false);
        }

        fireFileDeletedEvent(false);

    }
    
    abstract boolean checkLock(FileLock lock) throws IOException;

    public Object writeReplace() {
        return new ReplaceForSerialization(getFileName().getFile());
    }

    abstract protected void setValid(boolean valid);

    abstract public void refresh(final boolean expected, boolean fire);


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
                FSException.io("EXC_CannotDelete", file.getName(), ""); // NOI18N                                
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

    private synchronized EventListenerList getEventSupport() {
        if (eventSupport == null) {
            eventSupport = new EventListenerList();
        }
        return eventSupport;
    }
}
