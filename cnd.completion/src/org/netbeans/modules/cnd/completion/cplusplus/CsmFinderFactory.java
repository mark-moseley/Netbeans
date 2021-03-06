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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import java.util.*;
import org.netbeans.modules.cnd.editor.cplusplus.CCKit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;


/**
 * Factory producing misc JCFinders.
 *
 * <p>This class uses three synchronization objects:</p>
 * <ul>
 * <li><code>CsmFinderFactory.class instance</code> is used to synchronize creation of
 *      CsmFinderFactory singleton in getDefault method</li>
 * <li><code>CsmFinderFactory instance</code> is used for synchronization of getFinder and
 *     getGlobalFinder methods. The purpose is to synchronize situation when two
 *     threads are asking for global finder or for finder for the same file. Both
 *     methods are relatively fast: they #1) retrieve classpath(s) and ask for 
 *     parsed DBs and #2) if parser DB does not exist it will schedule its 
 *     parsing and continues. After scheduled parser DB was created the
 *     parser thread will notify this class by resetCache() method.</li>
 * <li><code>CACHE_LOCK instance</code> is used for synchronization of access to internal caches.
 *     This lock is much finer compared to CsmFinderFactory instance lock.
 *     Only reading/modifying operations of the cache can happed under this lock.
 *     Its purpose is to allow reseting of caches from listeners which was
 *     found as deadlock prone when synchronized on CsmFinderFactory instance lock.</li>
 * </ul>
 */
public final class CsmFinderFactory {

    private static CsmFinderFactory DEFAULT;

    /** Empty finder */
//    private static final JCBaseFinder EMPTY = new JCBaseFinder(CCKit.class);
    
    /** Cache of <FileObject, SoftReference<CsmFinder>>.
     * The FO is classpath root. Access to cache must be always synchronized
     * on CACHE_LOCK instance.
     */
    private HashMap cache = new HashMap();
    
    /** Weak map whose value is always null and only key is relevant.
     * The key is ClassPath on which we are already listening. The purpose
     * of this map is to not attach one listener on the classpath multiple times.
     */
    private WeakHashMap cpListening = new WeakHashMap();
    

    /** Weak map whose value is always null and only key is relevant.
     * The key is fakeJCClass used in web/jsp => XXX
     */
    private WeakHashMap fakeClasses = new WeakHashMap();
    
    /** This is property change listener listening on classpaths and 
     * invalidating cache when cp has changed. It must be wrapped in weak
     * listener to allow cp to be garbage collected. */
    private static PropertyChangeListener cpListener;
    
    /** Cached global finder. Access to this variable must be always
     * synchronized on CACHE_LOCK instance. */
    private SoftReference globalFinder;
    
//    private GlobalPathRegistryListener gpListener;
    
    /** Object used as lock for cache updating synchronization. */
    private final Object CACHE_LOCK = new Object();
    
    /** Was FakeFinder initialized? Use it in CompoundFinder? */
    private boolean useFakeFinder = false;

    private CsmFinderFactory() {
//        cpListener = new ClassPathListener();
//        gpListener = new GlobalPathListener();
//        GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(gpListener);
    }
    
    public static synchronized CsmFinderFactory getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new CsmFinderFactory();
        }
        return DEFAULT;
    }

    /**
     * Invalidate cache of finders. This method is expected to be called for example
     * when ParserThread finished parsing of a request or when parser DB was
     * deleted in parser DB manager by user.
     */
    public void resetCache() {
        synchronized (CACHE_LOCK) {
            cache = new HashMap();
            invalidateGlobalFinderCache();
        }
    }

    /** Append fake CsmClass. This support is needed from web/jsp module for evaluating of scriplets.
     *  XXX - it is not recommended to use this method!
     */
    public void appendClass(CsmClass cls){
        useFakeFinder = true;
        fakeClasses.put(cls, null);
        resetCache();
    }
    
    /** Returns finder for the given file. 
     * 
     * @return finder; cannot be null;
     */
    public synchronized CsmFinder getFinder(FileObject fo) {
        // the file must be on a SOURCE classpath
//        ClassPath sourceCP = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        FileObject owner;
//        if (sourceCP == null) {
            owner = null;
//        }else{
//            owner = sourceCP.findOwnerRoot(fo);
//        }
        FileObject cacheKey = (owner!=null) ? owner : fo;
        
        CsmFinder finder = retrieveFromCache(cacheKey);
        if (finder != null) {
            return finder;
        }
        
        CsmFile cf = CsmUtilities.getCsmFile(fo, true);
        
        ArrayList finders = new ArrayList();
        ArrayList fileObjects = new ArrayList();
        if (cf!=null){
//            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            addClasspathFinders(finders, fileObjects, cp, false);
//            cp = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//            addClasspathFinders(finders, fileObjects, cp, true);
//            cp = ClassPath.getClassPath(fo, ClassPath.BOOT);
//            addClasspathFinders(finders, fileObjects, cp, true);
            finder = new CsmFinderImpl(cf, getKitClass());
            return finder;
//            finders.add();
        }else{
            finders.add(getGlobalFinder());
        }
//        
//        // XXX - appending fake finder
//        if (useFakeFinder){
//            JCBaseFinder fakeFinder = new FakeFinder(getKitClass());
//            finders.add(fakeFinder);
//        }
//            
//        finder = new CompoundFinder(finders, getKitClass());
        finder  = getGlobalFinder();
        synchronized (CACHE_LOCK) {
            cache.put(cacheKey, new SoftReference(finder));
        }
        
        return finder;
    }

//    private class FakeFinder extends JCBaseFinder{
//        public FakeFinder(Class kitClass){
//            super(kitClass);
//            Set keySet = fakeClasses.keySet();
//            Iterator iter = keySet.iterator();
//            while (iter.hasNext()){
//                CsmClass cls = (CsmClass) iter.next();
//                appendClass(cls); 
//            }
//        }
//    }
    
    /** Returns global finder which uses GlobalPathRegistry to learn
     * all ClassPaths in use and returns finder on top of all these classpaths.
     */
    public synchronized CsmFinder getGlobalFinder() {
        CsmFinder finder;
        synchronized (CACHE_LOCK) {
            finder = globalFinder != null ? (CsmFinder)globalFinder.get() : null;
        }
        if (finder != null) {
            return finder;
        }
        ArrayList finders = new ArrayList();
        ArrayList fileObjects = new ArrayList();
//        Iterator it = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE).iterator();
//        while (it.hasNext()) {
//            ClassPath cp = (ClassPath)it.next();
//            addClasspathFinders(finders, fileObjects, cp, false);
//        }
//        ArrayList allCPs = new ArrayList();
//        allCPs.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE));
//        allCPs.addAll(GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT));
//        it = allCPs.iterator();
//        while (it.hasNext()) {
//            ClassPath cp = (ClassPath)it.next();
//            addClasspathFinders(finders, fileObjects, cp, true);
//        }

//        finder = new CompoundFinder(finders, CCKit.class);
	finder = new CsmFinderImpl((FileObject)null, getKitClass());
        synchronized (CACHE_LOCK) {
            globalFinder = new SoftReference(finder);
        }
        return finder;
    }

    /**
     * Invalidate global finder. This method is expected to be called for example
     * when list of globally registered ClassPaths has changed.
     */
    private void invalidateGlobalFinderCache() {
        synchronized (CACHE_LOCK) {
            globalFinder = null;
        }
    }
    
//    private void addClasspathFinders(List finders, List fileObjects, ClassPath cp, boolean findSources) {
//        if (cp == null) {
//            return;
//        }
//        Iterator it = cp.entries().iterator();
//        
//        while (it.hasNext()) {
//            ClassPath.Entry entry = (ClassPath.Entry)it.next();
//            if (findSources) {
//                FileObject[] sroots = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
//                if (sroots.length > 0) {
//                    for (int i=0; i<sroots.length; i++) {
//                        addFinder(finders, fileObjects, sroots[i]);
//                    }
//                } else {
//                    addFinder(finders, fileObjects, entry.getRoot());
//                }
//            } else {
//                addFinder(finders, fileObjects, entry.getRoot());
//            }
//        }
//        
//        // start listening on this cp
//        if (!cpListening.containsKey(cp)) {
//            cp.addPropertyChangeListener(WeakListeners.propertyChange(cpListener, cp));
//            cpListening.put(cp, null);
//        }
//    }
    
    private void addFinder(List finders, List fileObjects, FileObject fo) {
        if (fo == null) {
            return;
        }
        
        if (fileObjects.contains(fo)) {
            return;
        }
        fileObjects.add(fo);
        
        FileSystem fs = null;

        // if the fo is root of Jar I need to get the fo of the jar itself
        /*FileObject fo2 = FileUtil.getArchiveFile(fo);
        if (fo2 != null) {
            fo = fo2;
        }
        
         */
        
        CsmFinder finder = null;
//        CsmFinder finder = new MDRFinder(fo, getKitClass());//JCStorage.getStorage().getFinder(fo);
        if (finder != null) {
            finders.add(finder);
        } 
    }
    
    /** Returns kitClass over MDRFinder will operate and retrieve settings */
    protected Class getKitClass(){
        return CCKit.class;
    }
    
    private void removeFromCache(FileObject fo) {
        synchronized (CACHE_LOCK) {
            cache.remove(fo);
        }
    }
    
    private CsmFinder retrieveFromCache(FileObject fo) {
        synchronized (CACHE_LOCK) {
            SoftReference ref = (SoftReference)cache.get(fo);
            return ref != null ? (CsmFinder)ref.get() : null;
        }
    }

    /**
     * This method is called when classpath has changed.
     */
//    private void updateCache(ClassPath cp) {
//        List keys;
//        synchronized (CACHE_LOCK) {
//            keys = new ArrayList(cache.keySet());
//        }
//        Iterator it = keys.iterator();
//        while (it.hasNext()) {
//            FileObject fo = (FileObject)it.next();
//            
//            // first check that this item from cache is still valid
//            CsmFinder finder = retrieveFromCache(fo);
//            if (finder == null) {
//                // the finder was garbage collected -> remove the key
//                removeFromCache(fo);
//                continue;
//            }
//            
//            ClassPath c = ClassPath.getClassPath(fo, ClassPath.COMPILE);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//            c = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//            c = ClassPath.getClassPath(fo, ClassPath.BOOT);
//            if (c != null && c.equals(cp)) {
//                removeFromCache(fo);
//                continue;
//            }
//        }
//        // the global finder is affected too: invalidate it
//        invalidateGlobalFinderCache();
//    }
    

//    private class ClassPathListener implements PropertyChangeListener {
//        
//        public void propertyChange(PropertyChangeEvent evt) {
//            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
//                assert evt != null && evt.getSource() instanceof ClassPath;
//                updateCache((ClassPath)evt.getSource());
//            }
//        }
//        
//    }
//    
//    private class GlobalPathListener implements GlobalPathRegistryListener {
//        
//        public void pathsAdded(GlobalPathRegistryEvent event) {
//            invalidateGlobalFinderCache();
//            
//            // any change (e.g.: a project was open) will trigger parser DB creation
//            // if it does not exist yet
//            // Post it to RP and do not block event processing. The getGlobalFinder()
//            // method is relatively fast and result is cached, but opening 
//            // multiple projects at once is visibly slower if global finder
//            // is refreshed directly here.
//            RequestProcessor.getDefault().postRequest(new Runnable() {
//                public void run() {
//                    CsmFinderFactory.getDefault().getGlobalFinder();
//                }
//            }, 1000); // meaning of 1 sec is just to slightly delay this operation
//        }
//        
//        public void pathsRemoved(GlobalPathRegistryEvent event) {
//            invalidateGlobalFinderCache();
//        }
//        
//    }
    
}
