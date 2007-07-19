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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.repository.disk.StorageAllocator;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.CorruptedIndexedFile;

/**
 * Stores Persistent objects in two files;
 * the main purpose of the two files is managing defragmentation:
 * while one file is active (recent changes are written into it),
 * other one might be defragmented
 *
 * @author Vladimir Kvashin
 */
public class DoubleFileStorage extends FileStorage {
    
    private Map<Key, Persistent> fickleMap = new HashMap<Key, Persistent>();
    private File basePath;
    
    private IndexedStorageFile cache_0_dataFile;
    private IndexedStorageFile cache_1_dataFile;
    
    private boolean defragmenting = false;
    private AtomicBoolean cache_1_dataFileIsActive = new AtomicBoolean(false);

    private boolean getFlag () {
        return cache_1_dataFileIsActive.get();
    }
    
    private IndexedStorageFile getFileByFlag(boolean flag) {
        return (flag? cache_1_dataFile : cache_0_dataFile);
    }
    
    private IndexedStorageFile getActive() {
        return (cache_1_dataFileIsActive.get() ? cache_1_dataFile : cache_0_dataFile);
    }
    
    private IndexedStorageFile getPassive() {
        return (cache_1_dataFileIsActive.get() ? cache_0_dataFile : cache_1_dataFile) ;
    }
    
    public DoubleFileStorage(final String unitName) throws IOException {
        this(new File(StorageAllocator.getInstance().getUnitStorageName(unitName)));        
    }
    
    protected DoubleFileStorage(final File basePath) throws IOException {
        this (basePath, false);
    }
    /**
     * Creates a new <code>DoubleFileStorage</code> instance 
     * 
     * @param basePath  A File representing path to the storage
     * @param createCleanExistent    A flag if the storage should be created, not opened
     */
    protected DoubleFileStorage (final File basePath, boolean createCleanExistent) throws IOException {
        this.basePath = basePath;

        cache_0_dataFile = new IndexedStorageFile(basePath, "cache-0", createCleanExistent); // NOI18N
        
        cache_1_dataFile = new IndexedStorageFile(basePath, "cache-1", createCleanExistent); // NOI18N

        if ((cache_0_dataFile.getDataFileUsedSize() == 0 ) &&
            (cache_1_dataFile.getDataFileUsedSize() == 0)) {
            cache_1_dataFileIsActive.set(false);
        } else if ((cache_0_dataFile.getDataFileUsedSize() != 0 ) &&
                    (cache_1_dataFile.getDataFileUsedSize() != 0)) {
            cache_1_dataFileIsActive.set( 
             (cache_0_dataFile.getFragmentationPercentage() < cache_1_dataFile.getFragmentationPercentage())?false:true);
        } else {
            cache_1_dataFileIsActive.set((cache_0_dataFile.getDataFileUsedSize() == 0)?false:true);
        }
    }
    
    public void close() throws IOException{
       cache_0_dataFile.close();
       cache_1_dataFile.close();
    }
    
    public Persistent get(final Key key) throws IOException {
        if( Stats.hardFickle && key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            return fickleMap.get(key);
        }
        
        boolean activeFlag = getFlag();
        Persistent object = getFileByFlag(activeFlag).get(key);
        if( object == null ) {
            object = getFileByFlag(!activeFlag).get(key);
        }
        return object;
        
    }
    
    public void write(final Key key, final Persistent object) throws IOException {
        WriteStatistics.instance().update(1);
        if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            fickleMap.put(key, object);
            return;
        }

        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).write(key,object);
        getFileByFlag(!activeFlag).remove(key);
    }
    
    public void remove(final Key key) throws IOException {
        if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            fickleMap.remove(key);
            return;
        }
        
        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).remove(key);
        getFileByFlag(!activeFlag).remove(key);
    }
    
    public boolean maintenance(final long timeout) throws IOException {
        
        boolean needMoreTime = false;
        
        WriteStatistics.instance().update(0);
        
        if( Stats.traceDefragmentation ) {
            System.out.printf(">>> Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
            System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
            System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
        }
        
        if( timeout > 0 ) {
            if( ! defragmenting ) {
                if( getFragmentationPercentage() < Stats.defragmentationThreashold ) {
                    if( Stats.traceDefragmentation ) System.out.printf("\tFragmentation is too low\n"); // NOI18N
                    return needMoreTime;
                }
            }
        }
        
        
        if( ! defragmenting ) {
            defragmenting = true;
            cache_1_dataFileIsActive.set(!cache_1_dataFileIsActive.get());
        }
        
        needMoreTime = _defragment(timeout);
        
        if( getPassive().getObjectsCount() == 0 ) {
            defragmenting = false;
        }
        
        if( Stats.traceDefragmentation ) {
            System.out.printf("<<< Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
            System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
            System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
        }
        
        return needMoreTime;
    }
    
    private boolean _defragment(final long timeout) throws IOException {
        
        boolean needMoreTime = false;
        final long time = ((timeout > 0) || Stats.traceDefragmentation) ? System.currentTimeMillis() : 0;
        
        int cnt = 0;
        boolean activeFlag = getFlag();        
        Iterator<Key> it = getFileByFlag(!activeFlag).getKeySetIterator();

        while( it.hasNext() ) {
            Key key = it.next();
            ChunkInfo chunk = getFileByFlag(!activeFlag).getChunkInfo(key);
            int size = chunk.getSize();
            long newOffset = getFileByFlag(activeFlag).getSize();
            getFileByFlag(activeFlag).moveDataFromOtherFile(getFileByFlag(!activeFlag).getDataFile(), chunk.getOffset(), size, newOffset, key);
            it.remove();
            cnt++;
            
            if( (timeout > 0) && (cnt % 10 == 0) ) {
                if( System.currentTimeMillis()-time >= timeout ) {
                    needMoreTime = true;
                    break;
                }
            }
        }
        if( Stats.traceDefragmentation ) {
            String text = it.hasNext() ? " finished by timeout" : " completed"; // NOI18N
            System.out.printf("\t # defragmentinging %s %s; moved: %d remaining: %d \n", // NOI18N
                    getFileByFlag(!activeFlag).getDataFileName(), 
                    text, 
                    cnt, 
                    getFileByFlag(!activeFlag).getObjectsCount()); // NOI18N
        }
        return needMoreTime;
    }
    
    public void dump(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N
        
        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).dump(ps);
        ps.printf("\nPassive file:\n"); // NOI18N
        getFileByFlag(!activeFlag).dump(ps);
            
        ps.printf("\n"); // NOI18N
    }
    
    public void dumpSummary(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N
        
        boolean activeFlag = getFlag();
        getFileByFlag(activeFlag).dumpSummary(ps);
        ps.printf("\nPassive file:\n"); // NOI18N
        getFileByFlag(!activeFlag).dumpSummary(ps);
        
        ps.printf("\n"); // NOI18N
    }
    
    public int getFragmentationPercentage() throws IOException {
        final long fileSize;
        final float delta;
        
        boolean activeFlag = getFlag();
        fileSize = getFileByFlag(activeFlag).getSize() + getFileByFlag(!activeFlag).getSize();
        delta = fileSize - (getFileByFlag(activeFlag).getDataFileUsedSize() + getFileByFlag(!activeFlag).getDataFileUsedSize());
        final float percentage = delta * 100 / fileSize;
        return Math.round(percentage);
    }
    
    public long getSize() throws IOException {
        boolean activeFlag = getFlag();
        return getFileByFlag(activeFlag).getSize() + getFileByFlag(!activeFlag).getSize();
    }
    
    public int getObjectsCount() {
        boolean activeFlag = getFlag();
        return getFileByFlag(activeFlag).getObjectsCount() + getFileByFlag(!activeFlag).getObjectsCount();
    }
}
