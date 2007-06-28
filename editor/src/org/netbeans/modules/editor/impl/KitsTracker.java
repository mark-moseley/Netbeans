/*
 * KitsTracker.java
 *
 * Created on February 21, 2007, 1:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.editor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.Repository;

/**
 *
 * @author vita
 */
public final class KitsTracker {
        
    private static final Logger LOG = Logger.getLogger(KitsTracker.class.getName());
    private static final Set<String> ALREADY_LOGGED = Collections.synchronizedSet(new HashSet<String>(10));
    
    private static KitsTracker instance = null;
    
    /**
     * Gets the <code>KitsTracker</code> singleton instance.
     * @return The <code>KitsTracker</code> instance.
     */
    public static synchronized KitsTracker getInstance() {
        if (instance == null) {
            instance = new KitsTracker();
        }
        return instance;
    }
    
    /**
     * Gets the list of mime types (<code>String</code>s) that use the given
     * class as an editor kit implementation.
     * 
     * @param kitClass The editor kit class to get mime types for.
     * @return The <code>List&lt;String&gt;</code> of mime types.
     */
    public List<String> getMimeTypesForKitClass(Class kitClass) {
        boolean reload;
        Map<String, Class> reloadedMap = new HashMap<String, Class>();
        List<FileObject> newEventSources = new ArrayList<FileObject>();
        
        synchronized (mimeType2kitClass) {
            reload = needsReloading;
        }
        
        // This needs to be outside of the synchronized block to prevent deadlocks
        // See eg #107400
        if (reload) {
            reload(reloadedMap, newEventSources);
        }
            
        synchronized (mimeType2kitClass) {
            // Stop listening
            if (eventSources != null) {
                for(FileObject fo : eventSources) {
                    fo.removeFileChangeListener(fcl);
                }
            }
            
            // Update the cache
            mimeType2kitClass.clear();
            mimeType2kitClass.putAll(reloadedMap);
            
            // Start listening again
            eventSources = newEventSources;
            for(FileObject fo : eventSources) {
                fo.addFileChangeListener(fcl);
            }
            
            // Set the flag
            needsReloading = false;
            
            // Compute the list
            ArrayList<String> list = new ArrayList<String>();
            for(String mimeType : mimeType2kitClass.keySet()) {
                Class clazz = mimeType2kitClass.get(mimeType);
                if (kitClass == clazz) {
                    list.add(mimeType);
                }
            }

            return list;
        }
    }

    /**
     * Find mime type for a given editor kit implementation class.
     * 
     * @param kitClass The editor kit class to get the mime type for.
     * @return The mime type or <code>null</code> if the mime type can't be
     *   resolved for the given kit class.
     */
    public String findMimeType(Class kitClass) {
        List mimeTypes = getMimeTypesForKitClass(kitClass);
        if (mimeTypes.size() == 0) {
            if (LOG.isLoggable(Level.WARNING)) {
                logOnce(Level.WARNING, "No mime type uses editor kit implementation class: " + kitClass); //NOI18N
            }
            return null;
        } else if (mimeTypes.size() == 1) {
            return (String) mimeTypes.get(0);
        } else {
            if (LOG.isLoggable(Level.WARNING)) {
//                Throwable t = new Throwable("Stacktrace"); //NOI18N
//                LOG.log(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes, t); //NOI18N
                logOnce(Level.WARNING, "Ambiguous mime types for editor kit implementation class: " + kitClass + "; mime types: " + mimeTypes); //NOI18N
            }
            return null;
        }
    }
    
    // ------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------
    
    // The map of mime type -> kit class
    private final Map<String, Class> mimeType2kitClass = new HashMap<String, Class>();
    private List<FileObject> eventSources = null;
    private boolean needsReloading = true;

    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileFolderCreated(FileEvent fe) {
            invalidateCache();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (fe.getFile().isFolder()) {
                invalidateCache();
            }
        }
    };
    
    private KitsTracker() {

    }

    /**
     * Scans fonlders under 'Editors' and finds <code>EditorKit</code>s for
     * each mime type.
     * 
     * @param map The map of a mime type to its registered <code>EditorKit</code>.
     * @param eventSources The list of folders with registered <code>EditorKits</code>.
     *   Changes in these folders mean that the map may need to be recalculated.
     */
    private static void reload(Map<String, Class> map, List<FileObject> eventSources) {
        // Get the root of the MimeLookup registry
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Editors"); //NOI18N

        // Generally may not exist (e.g. in tests)
        if (fo != null) {
            // Go through mime type types
            FileObject [] types = fo.getChildren();
            for(int i = 0; i < types.length; i++) {
                if (!isValidType(types[i])) {
                    continue;
                }

                // Go through mime type subtypes
                FileObject [] subTypes = types[i].getChildren();
                for(int j = 0; j < subTypes.length; j++) {
                    if (!isValidSubtype(subTypes[j])) {
                        continue;
                    }

                    String mimeType = types[i].getNameExt() + "/" + subTypes[j].getNameExt(); //NOI18N
                    MimePath mimePath = MimePath.parse(mimeType);
                    EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);

                    if (kit != null) {
                        String genericMimeType;
                        if (!kit.getContentType().equals(mimeType) && 
                            !(null != (genericMimeType = getGenericPartOfCompoundMimeType(mimeType)) && genericMimeType.equals(kit.getContentType())))
                        {
                            LOG.warning("Inconsistent mime type declaration for the kit: " + kit + //NOI18N
                                "; mimeType from the kit is '" + kit.getContentType() + //NOI18N
                                ", but the kit is registered for '" + mimeType + "'"); //NOI18N
                        }
                        map.put(mimeType, kit.getClass());
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("No kit for '" + mimeType + "'");
                        }
                    }
                }

                eventSources.add(types[i]);
            }

            eventSources.add(fo);
        }
    }

    private void invalidateCache() {
        synchronized (mimeType2kitClass) {
            needsReloading = true;
        }
    }

    private static String getGenericPartOfCompoundMimeType(String mimeType) {
        int plusIdx = mimeType.lastIndexOf('+'); //NOI18N
        if (plusIdx != -1 && plusIdx < mimeType.length() - 1) {
            int slashIdx = mimeType.indexOf('/'); //NOI18N
            String prefix = mimeType.substring(0, slashIdx + 1);
            String suffix = mimeType.substring(plusIdx + 1);

            // fix for #61245
            if (suffix.equals("xml")) { //NOI18N
                prefix = "text/"; //NOI18N
            }

            return prefix + suffix;
        } else {
            return null;
        }
    }
    
    private static boolean isValidType(FileObject typeFile) {
        if (!typeFile.isFolder()) {
            return false;
        }

        String typeName = typeFile.getNameExt();
        return MimePath.validate(typeName, null);
    }

    private static boolean isValidSubtype(FileObject subtypeFile) {
        if (!subtypeFile.isFolder()) {
            return false;
        }

        String typeName = subtypeFile.getNameExt();
        return MimePath.validate(null, typeName);
    }        
    
    private static void logOnce(Level level, String msg) {
        if (!ALREADY_LOGGED.contains(msg)) {
            LOG.log(level, msg);
            ALREADY_LOGGED.add(msg);
        }
    }
}
