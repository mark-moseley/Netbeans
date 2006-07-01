/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;
import java.io.ByteArrayInputStream;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import org.openide.filesystems.FileLock;

/**
 * 
 */
public final class FileObjectFactory {
    private final Map allInstances = Collections.synchronizedMap(new WeakHashMap());
    private RootObj root;

    public static FileObjectFactory getInstance(final FileInfo fInfo) {
        return new FileObjectFactory(fInfo);
    }

    public final RootObj getRoot() {
        return root;
    }
    public int getSize() {        
        synchronized (allInstances) {
            return allInstances.size();
        }
    }

    public final FileObject findFileObject(final FileInfo fInfo) {
        FileObject retVal = null;
        File f = fInfo.getFile();
        
        synchronized (allInstances) {
            retVal = this.get(f);
            if (retVal == null || !retVal.isValid()) {
                final File parent = f.getParentFile();
                if (parent != null) {
                    retVal = this.create(fInfo);
                } else {
                    retVal = this.getRoot();
                }
                
            }
     
            assert retVal == null || retVal.isValid() : retVal.toString();
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
        
        if (name == null) return null;

        if (name.isFile() && !name.isDirectory()) {
            assert name.getFile() != null &&  (name.getFile().isFile() || !name.getFile().isDirectory()) : name;
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }
        
        if (!name.isFile() && name.isDirectory()) {            
            assert name.getFile() != null &&  (!name.getFile().isFile() || name.getFile().isDirectory()) : name;
            final FolderObj realRoot = new FolderObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (!name.isFile() && !name.isDirectory() || fInfo.isUnixSpecialFile()) {
            assert name.getFile() != null &&  (name.getFile().isFile() == name.getFile().isDirectory()) : name;            
            final FileObj realRoot = new FileObj(file, name) {
                public InputStream getInputStream() throws FileNotFoundException {
                    return new ByteArrayInputStream(new byte[] {});
                }                
                public boolean isReadOnly() {
                    return true;
                }               
                
                public OutputStream getOutputStream(final FileLock lock) throws IOException {
                    throw new IOException(file.getAbsolutePath());
                }                

                public boolean canWrite() {
                    return !isReadOnly();
                }
            };
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }
    
    public final void refreshAll(final boolean expected) {
        final Set all2Refresh = new HashSet();
        synchronized (allInstances) {
            final Iterator it = allInstances.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                if (obj instanceof List) {
                    for (Iterator iterator = ((List)obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null)  {
                            all2Refresh.add(fo);
                        }                                            
                    }
                } else {
                    final WeakReference ref = (WeakReference) obj;
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                    if (fo != null)  {
                        all2Refresh.add(fo);
                    }                    
                }
            }
        }


        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final BaseFileObj fo = (BaseFileObj) iterator.next();
            fo.refresh(expected);
        }
    }

    public final void rename () {
        final Map toRename = new HashMap();
        synchronized (allInstances) {
            final Iterator it = allInstances.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                final Object obj = entry.getValue();
                final Integer key = (Integer)entry.getKey();
                if (!(obj instanceof List)) {
                    final WeakReference ref = (WeakReference) obj;
                
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);

                    if (fo != null) {
                        Integer computedId = fo.getFileName().getId();
                        if (!key.equals(computedId)) {
                          toRename.put(key,fo);      
                        }
                    }
                } else {
                    for (Iterator iterator = ((List)obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null) {
                            Integer computedId = fo.getFileName().getId();
                            if (!key.equals(computedId)) {
                              toRename.put(key,ref);      
                            }
                        }                        
                    }
                    
                }
            }
            
            for (Iterator iterator = toRename.entrySet().iterator(); iterator.hasNext();) {
                final Map.Entry entry = (Map.Entry ) iterator.next();
                Object key = entry.getKey();
                Object previous = allInstances.remove(key);
                if (previous instanceof List) {
                    List list = (List)previous;
                    list.remove(entry.getValue());
                    allInstances.put(key, previous);
                } else {
                    BaseFileObj bfo = (BaseFileObj )entry.getValue();
                    putInCache(bfo, bfo.getFileName().getId());
                }
            }            
        }
    }    
    
    public final  BaseFileObj get(final File file) {
        final Object o;
        synchronized (allInstances) {
            final Object value = allInstances.get(NamingFactory.createID(file));
            Reference ref = null;
            ref = (Reference) (value instanceof Reference ? value : null);
            ref = (ref == null && value instanceof List ? FileObjectFactory.getReference((List) value, file) : ref);

            o = (ref != null) ? ref.get() : null;
            assert (o == null || o instanceof BaseFileObj);
        }

        return (BaseFileObj) o;
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

    private FileObjectFactory(final FileInfo fInfo) {
        final File rootFile = fInfo.getFile();
        assert rootFile.getParentFile() == null;

        final BaseFileObj realRoot = create(fInfo);
        root = new RootObj(realRoot);
    }


    private BaseFileObj putInCache(final BaseFileObj newValue, final Integer id) {
        synchronized (allInstances) {
            final WeakReference newRef = new WeakReference(newValue);
            final Object listOrReference = allInstances.put(id, newRef);

            if (listOrReference != null) {                
                if (listOrReference instanceof List) {
                    ((List) listOrReference).add(newRef);                    
                    allInstances.put(id, listOrReference);
                } else {
                    assert (listOrReference instanceof WeakReference);
                    final Reference oldRef = (Reference) listOrReference;
                    BaseFileObj oldValue = (oldRef != null) ? (BaseFileObj)oldRef.get() : null;
                    
                    if (oldValue != null && !newValue.getFileName().equals(oldValue.getFileName())) {
                        final List l = new ArrayList();
                        l.add(oldRef);
                        l.add(newRef);
                        allInstances.put(id, l);
                    }                    
                }
            }
        }

        return newValue;
    }
}
