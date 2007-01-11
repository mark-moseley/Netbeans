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
package org.netbeans.modules.vmd.inspector;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 *
 *
 * @author Karol Harezlak 
 * Based on Core Model GlobalDescriptorRegistry
 */

final class GlobalFolderRegistry {
    
    private static final String FILE_SYSTEM_FOLDER_NAME = "/inspectorfolders";  // NOI18N
    private static final HashMap<String, WeakReference<GlobalFolderRegistry>> registries = new HashMap<String, WeakReference<GlobalFolderRegistry>>();
    
    static GlobalFolderRegistry getGlobalFolderRegistry(String projectType) {
        assert Debug.isFriend(FolderRegistry.class);
        
        synchronized(registries) {
            WeakReference<GlobalFolderRegistry> ref = registries.get(projectType);
            GlobalFolderRegistry registry = ref != null ? ref.get() : null;
            
            if (registry == null) {
                registry = new GlobalFolderRegistry(projectType);
                registries.put(projectType, new WeakReference<GlobalFolderRegistry>(registry));
            }
            return registry;
        }
    }
    
    private final DataFolder registryFolders;
    private final Mutex mutex = new Mutex();
    private HashMap<TypeID, InspectorFolder> descriptors = new HashMap<TypeID, InspectorFolder>();
    private FolderRegistry.Listener listener;
    
    private GlobalFolderRegistry(String projectType) {
        assert projectType != null && projectType.length() > 0;
        FileObject resource = Repository.getDefault ().getDefaultFileSystem ().findResource (projectType + FILE_SYSTEM_FOLDER_NAME);
        if (resource != null) {
            registryFolders = DataFolder.findFolder(resource); // NOI18N

            registryFolders.getPrimaryFile().addFileChangeListener(new FileChangeListener() {
                public void fileFolderCreated(FileEvent fileEvent) {}
                public void fileDataCreated(FileEvent fileEvent) { reloadAll(); };
                public void fileChanged(FileEvent fileEvent) { reloadAll(); }
                public void fileDeleted(FileEvent fileEvent) { reloadAll(); }
                public void fileRenamed(FileRenameEvent fileRenameEvent) { reloadAll(); }
                public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) { reloadAll(); }
            });
        } else
            registryFolders = null;
        
        reload();
    }
    
    private void reloadAll(){
        reload();
        listener.notifyRegistryContentChange();
    }
    
    void readAccess(Runnable runnable) {
        assert Debug.isFriend(FolderRegistry.class);
        mutex.readAccess(runnable);
    }
    
    private void reload() {
        mutex.writeAccess(new Runnable() {
            public void run() {
                reloadCore();
            }
        });
    }
    
    private void reloadCore() {
        HashMap<TypeID, InspectorFolder> tempDescriptors = new HashMap<TypeID, InspectorFolder> ();

        if (registryFolders != null) {
            Enumeration enumeration = registryFolders.children();

            while (enumeration.hasMoreElements()) {
                DataObject dataObject = (DataObject) enumeration.nextElement();
                InspectorFolder descriptor = dao2descriptor(dataObject);

                if (descriptor == null) {
                    Debug.warning("No descriptor", dataObject.getPrimaryFile().getNameExt());  // NOI18N
                    continue;
                }
                TypeID type = descriptor.getTypeID();
                if (type == null) {
                    Debug.warning("Null type descriptor", descriptor);  // NOI18N
                    continue;
                }
                final TypeID thisType = descriptor.getTypeID();
                if (tempDescriptors.containsKey(thisType)) {
                    Debug.warning("Duplicate descriptor", thisType);  // NOI18N
                    continue;
                }
                tempDescriptors.put(thisType, descriptor);
            }
        }

        for (InspectorFolder descriptor : tempDescriptors.values())
            resolveDescriptor(descriptor);
        descriptors = tempDescriptors;
        //if (descriptors.isEmpty()) //TODO remove it later
        //    Debug.warning(" Couldnt find folders descriptors");  // NOI18N //TODO remove it later
        
    }
    
    private  static void resolveDescriptor( InspectorFolder descriptor) {
        resolveDescriptor(new HashSet<TypeID>(), descriptor);
    }
    
    private static void resolveDescriptor(HashSet<TypeID> resolvingDescriptors, InspectorFolder descriptor) {
        assert Debug.isFriend(GlobalFolderRegistry.class)  ||  Debug.isFriend(FolderRegistry.class);
        
        TypeID thisType = descriptor.getTypeID();
        if (thisType == null) {
            Debug.warning("Null Type", descriptor);  // NOI18N
            return;
        }
        
        resolvingDescriptors.add(thisType);
    }
    
    private static InspectorFolder dao2descriptor(DataObject dataObject) {
        InstanceCookie.Of instanceCookie = dataObject.getCookie(InstanceCookie.Of.class);
        
        if (instanceCookie != null) {
            try {
                Object instance = instanceCookie.instanceCreate();
                if (instance instanceof InspectorFolder)
                    return (InspectorFolder) instance;
            } catch (IOException e) {
                Debug.warning(e);
            } catch (ClassNotFoundException e) {
                Debug.warning(e);
            }
            Debug.warning("Instance is not InspectorFolder class"); // NOI18N
            return null;
        }
        
        return null;
    }
    
    Collection<InspectorFolder> getInspectorFolder() {
        assert Debug.isFriend(FolderRegistry.class);
        return Collections.unmodifiableCollection(descriptors.values());
    }
    
    void addListener(FolderRegistry.Listener listener) {
        this.listener = listener;
    }
    
    void removeListener(FolderRegistry.Listener listener) {
        if (this.listener != listener)
            Debug.warning("Listener to remove != registred listener"); //NOI18N
        listener = null;
    }
    
    
}
