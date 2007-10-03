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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.localhistory.store;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.versioning.util.VersioningListener;

/**
 *
 * @author tomas
 */
public class LocalHistoryTestStore implements LocalHistoryStore {

    private final LocalHistoryStore store;        
    private Method getStoreFolderMethod;
    private Method getDataFileMethod;
    private Method getHistoryFileMethod;
    private Method getLabelsFileMethod;
    private Method getStoreFileMethod;
    private Method cleanUpImplMethod;
    
    public LocalHistoryTestStore(String storePath) {      
        store = LocalHistoryStoreFactory.getInstance().createLocalHistoryStorage();
    }

    public void setLabel(File file, long ts, String label) {
        store.setLabel(file, ts, label);
    }

    public void removeVersioningListener(VersioningListener l) {
        store.removeVersioningListener(l);
    }

    public void addVersioningListener(VersioningListener l) {
        store.addVersioningListener(l);    
    }
    
    public org.netbeans.modules.localhistory.store.StoreEntry getStoreEntry(File file, long ts) {
        return store.getStoreEntry(file, ts);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getStoreEntries(File file) {
        return store.getStoreEntries(file);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getFolderState(File root, File[] files, long ts) {
        return store.getFolderState(root, files, ts);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getDeletedFiles(File root) {
        return store.getDeletedFiles(root);
    }

    public void fileDeleteFromMove(File from, File to, long ts) {
        store.fileDeleteFromMove(from, to, ts);
    }

    public void fileDelete(File file, long ts) {
        store.fileDelete(file, ts);
    }

    public void fileCreateFromMove(File from, File to, long ts) {
        store.fileCreateFromMove(from, to, ts);
    }

    public void fileCreate(File file, long ts) {
        store.fileCreate(file, ts);
    }

    public void fileChange(File file, long ts) {
        store.fileChange(file, ts);
    }

    public void deleteEntry(File file, long ts) {
        store.deleteEntry(file, ts);
    }
    
    public void cleanUp(long ttl) {
        // screw the impl a bit as we won't run the cleanup asynchronously
        try {
            if(cleanUpImplMethod == null) {            
                cleanUpImplMethod = store.getClass().getDeclaredMethod("cleanUpImpl", new Class[] {long.class});
                cleanUpImplMethod.setAccessible(true);            
            }
            cleanUpImplMethod.invoke(store, new Object[]{ttl});           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File getHistoryFile(File file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if(getHistoryFileMethod == null) {            
            getHistoryFileMethod = store.getClass().getDeclaredMethod("getHistoryFile", new Class[] {File.class});
            getHistoryFileMethod.setAccessible(true);            
        }
        return (File) getHistoryFileMethod.invoke(store, new Object[]{file});           
    }
        
    File getStoreFolder(File file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getStoreFolderMethod == null) {            
            getStoreFolderMethod = store.getClass().getDeclaredMethod("getStoreFolder", new Class[] {File.class});
            getStoreFolderMethod.setAccessible(true);            
        }
        return (File) getStoreFolderMethod.invoke(store, new Object[]{file});           
    }    

    File getDataFile(File file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getDataFileMethod == null) {            
            getDataFileMethod = store.getClass().getDeclaredMethod("getDataFile", new Class[] {File.class});
            getDataFileMethod.setAccessible(true);            
        }
        return (File) getDataFileMethod.invoke(store, new Object[]{file});           
    }    

    File getLabelsFile(File file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getLabelsFileMethod == null) {            
            getLabelsFileMethod = store.getClass().getDeclaredMethod("getLabelsFile", new Class[] {File.class});
            getLabelsFileMethod.setAccessible(true);            
        }
        return (File) getLabelsFileMethod.invoke(store, new Object[]{file});           
    }    
    
    File getStoreFile(File file, long ts) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getStoreFileMethod == null) {            
            getStoreFileMethod = store.getClass().getDeclaredMethod("getStoreFile", new Class[] {File.class, String.class, boolean.class});
            getStoreFileMethod.setAccessible(true);            
        }
        return (File) getStoreFileMethod.invoke(store, new Object[]{file, Long.toString(ts), true});           
    }              
    
}
