/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.disk;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.repository.disk.api.RepositoryFilesHelperCacheStrategy;
import org.netbeans.modules.cnd.repository.sfs.ConcurrentFileRWAccess;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.util.RepositoryCacheMap;


class SimpleRepositoryHelperCacheImpl implements RepositoryFilesHelperCacheStrategy { 
    private static volatile SimpleRepositoryHelperCacheImpl instance;
    private static Lock     lock = new ReentrantLock();
    
    private RepositoryCacheMap<String, ConcurrentFileRWAccess> nameToFileCache;
    
    public static SimpleRepositoryHelperCacheImpl getInstance(int newCapacity) {
        
        if (instance == null) {
            try {
                lock.lock();
                if (instance == null) {
                    instance =new SimpleRepositoryHelperCacheImpl(newCapacity);
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }
    
    public void adjustCapacity(int newCapacity) throws IOException {
        Set<ConcurrentFileRWAccess> removedFiles = nameToFileCache.adjustCapacity(newCapacity);
        
        if (removedFiles == null)
            return ;
        
        for (ConcurrentFileRWAccess fileToRemove: removedFiles) {
            try {
                fileToRemove.writeLock().lock();
                
                if (fileToRemove.getFD().valid()) {
                    fileToRemove.close();
                }
                
            } finally {
                fileToRemove.writeLock().unlock();
            }
        }
    }
    
    protected SimpleRepositoryHelperCacheImpl (int openFilesLimit) {
        nameToFileCache = new RepositoryCacheMap<String, ConcurrentFileRWAccess>(openFilesLimit);
    }
    
    public String lookupInCacheName(Key id) {
        return null;
    }
    
    public ConcurrentFileRWAccess lookupInCacheFile(String fileName) {
        return nameToFileCache.get(fileName);
    }
    
    public void cacheNameRemove(String fileName) throws IOException {
        ConcurrentFileRWAccess removedFile = nameToFileCache.remove(fileName);
        if (removedFile != null) {
            try {
                removedFile.writeLock().lock();
                
                if (removedFile.getFD().valid() )
                    removedFile.close();
                
            }  finally {
                removedFile.writeLock().unlock();
            }
        }
    }
    
    public void putCacheFile(String fileName, ConcurrentFileRWAccess aFile) throws IOException {
        ConcurrentFileRWAccess removedFile = nameToFileCache.put(fileName, aFile);
        if (removedFile != null) {
            try {
                removedFile.writeLock().lock();
                if (removedFile.getFD().valid()) {
                    removedFile.close();
                }
                
            } finally {
                removedFile.writeLock().unlock();
            }
        }
    }

    public void putCacheName(Key id, String fileName) {
    }

    public void cacheFileRemove(ConcurrentFileRWAccess aFile) {
    }
}