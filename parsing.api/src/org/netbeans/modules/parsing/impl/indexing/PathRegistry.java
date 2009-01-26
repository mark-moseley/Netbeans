/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class PathRegistry implements Runnable {
    private static PathRegistry instance;
    private static final RequestProcessor firer = new RequestProcessor ("Path Registry Request Processor"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PathRegistry.class.getName());

    private final RequestProcessor.Task firerTask;
    private final GlobalPathRegistry regs;
    private final List<PathRegistryEvent.Change> changes = new LinkedList<PathRegistryEvent.Change>();

    private Set<ClassPath> activeCps;
    private Map<URL, SourceForBinaryQuery.Result2> sourceResults;
    private Map<URL, URL[]> translatedRoots;
    private Map<URL, WeakValue> unknownRoots;
    private long timeStamp;             //Lamport event ordering
    private volatile Runnable debugCallBack;
    private volatile boolean useLibraries = true;
    private Collection<URL>  sourcePaths;
    private Collection<URL> libraryPath;
    private Collection<URL> binaryLibraryPath;
    private Collection<URL> unknownSourcePath;

    private final Listener listener;
    private final List<PathRegistryListener> listeners;

    private  PathRegistry () {
        firerTask = firer.create(this, true);
        regs = GlobalPathRegistry.getDefault();
        assert regs != null;
        this.listener = new Listener ();
        this.timeStamp = -1;
        this.activeCps = Collections.emptySet();
        this.sourceResults = Collections.emptyMap();
        this.unknownRoots = new HashMap<URL, WeakValue>();
        this.translatedRoots = new HashMap<URL, URL[]> ();
        this.listeners = new CopyOnWriteArrayList<PathRegistryListener>();
        this.regs.addGlobalPathRegistryListener (WeakListeners.create(GlobalPathRegistryListener.class,this.listener,this.regs));
    }

    public static synchronized PathRegistry getDefault () {
        if (instance == null) {
            instance = new PathRegistry();
        }
        return instance;
    }

    void setDebugCallBack (final Runnable r) {
        this.debugCallBack = r;
    }

    public void addPathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.add(listener);
    }

    public void removePathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.remove(listener);
    }

    public URL[] sourceForBinaryQuery (final URL binaryRoot, final ClassPath definingClassPath, final boolean fire) {
        URL[] result = this.translatedRoots.get(binaryRoot);
        if (result != null) {
            if (result.length > 0) {
                return result;
            }
            else {
                return null;
            }
        }
        else {
            List<URL> cacheRoots = new ArrayList<URL> ();
            Collection<? extends URL> unknownRes = getSources(SourceForBinaryQuery.findSourceRoots2(binaryRoot),cacheRoots,null);
            if (unknownRes.isEmpty()) {
                return null;
            }
            else {
                synchronized (this) {
                    for (URL u : unknownRes) {
                        unknownRoots.put(u,new WeakValue(definingClassPath,u));
                    }
                }
                if (fire) {
                    this.resetCacheAndFire(EventKind.PATHS_CHANGED, PathKind.UNKNOWN_SOURCE, null, Collections.singleton(definingClassPath));
                }
                return result;
            }
        }
    }

    public Collection<? extends URL> getSources () {
        Request request;
        synchronized (this) {
            if (this.sourcePaths != null) {
                return this.sourcePaths;
            }
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashSet<ClassPath> (this.activeCps),
                new HashMap<URL,SourceForBinaryQuery.Result2> (this.sourceResults),
                new HashMap<URL,WeakValue> (this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (this.sourcePaths == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                }
                return this.sourcePaths;
            }
            else {
                return res.sourcePath;
            }
        }
    }

    public Collection<? extends URL> getLibraries () {
        Request request;
        synchronized (this) {
            if (this.libraryPath != null) {
                return this.libraryPath;
            }
            request = new Request (
                this.getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashSet<ClassPath>(this.activeCps),
                new HashMap<URL,SourceForBinaryQuery.Result2>(this.sourceResults),
                new HashMap<URL, WeakValue> (this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (this.getTimeStamp() == res.timeStamp) {
                if (this.libraryPath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                }
                return this.libraryPath;
            }
            else {
                return res.libraryPath;
            }
        }
    }

    public Collection<? extends URL> getBinaryLibraries () {
        Request request;
        synchronized (this) {
            if (this.binaryLibraryPath != null) {
                return this.binaryLibraryPath;
            }
            request = new Request (
                this.getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashSet<ClassPath>(this.activeCps),
                new HashMap<URL,SourceForBinaryQuery.Result2>(this.sourceResults),
                new HashMap<URL, WeakValue> (this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (this.getTimeStamp() == res.timeStamp) {
                if (this.binaryLibraryPath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                }
                return this.binaryLibraryPath;
            }
            else {
                return res.binaryLibraryPath;
            }
        }
    }

    public Collection<? extends URL> getUnknownRoots () {
        Request request;
        synchronized (this) {
            if (this.unknownSourcePath != null) {
                return unknownSourcePath;
            }
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashSet<ClassPath> (this.activeCps),
                new HashMap<URL,SourceForBinaryQuery.Result2> (this.sourceResults),
                new HashMap<URL, WeakValue> (this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (unknownSourcePath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                }
                return this.unknownSourcePath;
            }
            else {
                return res.unknownSourcePath;
            }
        }
    }

    public void run () {
        assert firer.isRequestProcessorThread();
        long now = System.currentTimeMillis();
        try {
            LOGGER.log(Level.FINE, "resetCacheAndFire waiting for projects"); // NOI18N
            OpenProjects.getDefault().openProjects().get();
            LOGGER.log(Level.FINE, "resetCacheAndFire blocked for {0} ms", System.currentTimeMillis() - now); // NOI18N
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "resetCacheAndFire timeout", ex); // NOI18N
        }

        Iterable<? extends PathRegistryEvent.Change> ch;
        synchronized (this) {
            ch = new ArrayList<PathRegistryEvent.Change>(this.changes);
            this.changes.clear();
        }
        fire(ch);
        LOGGER.log(Level.FINE, "resetCacheAndFire, firing done"); // NOI18N
    }

    private static Result createResources (final Request request) {
        assert request != null;
        final Set<URL> sourceResult = new HashSet<URL> ();
        final Set<URL> unknownResult = new HashSet<URL> ();
        final Set<URL> libraryResult = new HashSet<URL> ();
        final Set<URL> binaryLibraryResult = new HashSet<URL> ();
        final Map<URL,URL[]> translatedRoots = new HashMap<URL, URL[]>();
        final Set<ClassPath> newCps = new HashSet<ClassPath> ();
        final Map<URL,SourceForBinaryQuery.Result2> newSR = new HashMap<URL,SourceForBinaryQuery.Result2> ();

        for (ClassPath cp : request.sourceCps) {
            boolean isNew = !request.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                sourceResult.add(entry.getURL());
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
               cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (ClassPath cp : request.libraryCps) {
            boolean isNew = !request.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                libraryResult.add(entry.getURL());
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
               cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (ClassPath cp : request.binaryLibraryCps) {
            boolean isNew = !request.oldCps.remove(cp);
            for (ClassPath.Entry entry : cp.entries()) {
                URL url = entry.getURL();
                if (!translatedRoots.containsKey(url)) {
                    SourceForBinaryQuery.Result2 sr = request.oldSR.remove (url);
                    boolean isNewSR;
                    if (sr == null) {
                        sr = SourceForBinaryQuery.findSourceRoots2(url);
                        isNewSR = true;
                    }
                    else {
                        isNewSR = false;
                    }
                    assert !newSR.containsKey(url);
                    newSR.put(url,sr);
                    final List<URL> cacheURLs = new ArrayList<URL> ();
                    Collection<URL> srcRoots = getSources(sr, cacheURLs, request.unknownRoots);
                    if (srcRoots.isEmpty()) {
                        binaryLibraryResult.add(url);
                    }
                    else {
                        libraryResult.addAll(srcRoots);
                    }
                    translatedRoots.put(url, cacheURLs.toArray(new URL[cacheURLs.size()]));
                    if (isNewSR) {
                        sr.addChangeListener(request.changeListener);
                    }
                }
            }
            boolean notContained = newCps.add (cp);
            if (isNew && notContained) {
                cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (ClassPath cp : request.oldCps) {
            cp.removePropertyChangeListener(request.propertyListener);
        }

        for (Map.Entry<URL,SourceForBinaryQuery.Result2> entry : request.oldSR.entrySet()) {
            entry.getValue().removeChangeListener(request.changeListener);
        }
        unknownResult.addAll(request.unknownRoots.keySet());

        return new Result (request.timeStamp, sourceResult, libraryResult, binaryLibraryResult, unknownResult,
                newCps, newSR, translatedRoots, request.unknownRoots);
    }

    private static Collection <URL> getSources (final SourceForBinaryQuery.Result2 sr, final List<URL> cacheDirs, final Map<URL, WeakValue> unknownRoots) {
        assert sr != null;
        if (sr.preferSources()) {
            final FileObject[] roots = sr.getRoots();
            assert roots != null;
            List<URL> result = new ArrayList<URL> (roots.length);
            for (int i=0; i<roots.length; i++) {
                try {
                    final URL url = roots[i].getURL();
                    if (cacheDirs != null) {
                        cacheDirs.add (url);
                    }
                    if (unknownRoots != null) {
                        unknownRoots.remove (url);
                    }
                    result.add(url);
                } catch (FileStateInvalidException e) {
                    //Actually never happens, just declared in FileObject.getURL()
                    Exceptions.printStackTrace(e);
                }
            }
            return result;
        }
        else {
            return Collections.<URL>emptySet();
        }
    }

    private void resetCacheAndFire (final EventKind eventKind,
            final PathKind pathKind, final String pathId,
            final Set<? extends ClassPath> paths) {
        synchronized (this) {
            this.sourcePaths = null;
            this.libraryPath = null;
            this.binaryLibraryPath = null;
            this.unknownSourcePath = null;
            this.timeStamp++;
            this.changes.add(new PathRegistryEvent.Change(eventKind, pathKind, pathId, paths));
        }

        LOGGER.log(Level.FINE, "resetCacheAndFire"); // NOI18N
        firerTask.schedule(0);
    }

    private void fire (final Iterable<? extends PathRegistryEvent.Change> changes) {
        final PathRegistryEvent event = new PathRegistryEvent(this, changes);
        for (PathRegistryListener l : listeners) {
            l.pathsChanged(event);
        }
    }

    private PathKind getPathKind (final String pathId) {
        assert pathId != null;
        if (pathId == null) {
            return null;
        }
        final Set<String> sIds = PathRecognizerRegistry.getDefault().getSourceIds();
        if (sIds.contains(pathId)) {
            return PathKind.SOURCE;
        }
        final Set<String> lIds = PathRecognizerRegistry.getDefault().getLibraryIds();
        if (lIds.contains(pathId)) {
            return PathKind.LIBRARY;
        }
        final Set<String> bIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
        if (bIds.contains(pathId)) {
            return PathKind.BINARY_LIBRARY;
        }
        return null;
    }

    private Set<ClassPath> getSourcePaths () {
        return getPaths(PathKind.SOURCE);
    }

    private Set<ClassPath> getLibraryPaths () {
        return getPaths(PathKind.LIBRARY);
    }

    private Set<ClassPath> getBinaryLibraryPaths () {
        return getPaths(PathKind.BINARY_LIBRARY);
    }

    private Set<ClassPath> getPaths (final PathKind kind) {
        Set<String> ids;
        switch (kind) {
            case SOURCE:
                ids = PathRecognizerRegistry.getDefault().getSourceIds();
                break;
            case LIBRARY: 
                ids = PathRecognizerRegistry.getDefault().getLibraryIds();
                break;
            case BINARY_LIBRARY: 
                ids = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
                break;
            default:
                LOGGER.warning("Not expecting PathKind of " + kind); //NOI18N
                return Collections.<ClassPath>emptySet();
        }

        Set<ClassPath> result = new HashSet<ClassPath>();   //Maybe caching, but should be called once per change
        for (String id : ids) {
            result.addAll (this.regs.getPaths(id));
        }
        return result;
    }

    private long getTimeStamp () {
        return this.timeStamp;
    }

    private static class Request {

        final long timeStamp;
        final Set<ClassPath> sourceCps;
        final Set<ClassPath> libraryCps;
        final Set<ClassPath> binaryLibraryCps;
        final Set<ClassPath> oldCps;
        final Map <URL, SourceForBinaryQuery.Result2> oldSR;
        final Map<URL, WeakValue> unknownRoots;
        final PropertyChangeListener propertyListener;
        final ChangeListener changeListener;

        public Request (final long timeStamp, final Set<ClassPath> sourceCps, final Set<ClassPath> libraryCps, final Set<ClassPath> binaryLibraryCps,
            final Set<ClassPath> oldCps, final Map <URL, SourceForBinaryQuery.Result2> oldSR, final Map<URL, WeakValue> unknownRoots,
            final PropertyChangeListener propertyListener, final ChangeListener changeListener) {
            assert sourceCps != null;
            assert libraryCps != null;
            assert binaryLibraryCps != null;
            assert oldCps != null;
            assert oldSR != null;
            assert unknownRoots != null;
            assert propertyListener != null;
            assert changeListener != null;

            this.timeStamp = timeStamp;
            this.sourceCps = sourceCps;
            this.libraryCps = libraryCps;
            this.binaryLibraryCps = binaryLibraryCps;
            this.oldCps = oldCps;
            this.oldSR = oldSR;
            this.unknownRoots = unknownRoots;
            this.propertyListener = propertyListener;
            this.changeListener = changeListener;
        }
    }

    private static class Result {

        final long timeStamp;
        final Collection<URL> sourcePath;
        final Collection<URL> libraryPath;
        final Collection<URL> binaryLibraryPath;
        final Collection<URL> unknownSourcePath;
        final Set<ClassPath> newCps;
        final Map<URL, SourceForBinaryQuery.Result2> newSR;
        final Map<URL, URL[]> translatedRoots;
        final Map<URL, WeakValue> unknownRoots;

        public Result (final long timeStamp,
            final Collection<URL> sourcePath,
            final Collection<URL> libraryPath,
            final Collection<URL> binaryLibraryPath,
            final Collection<URL> unknownSourcePath,
            final Set<ClassPath> newCps,
            final Map<URL, SourceForBinaryQuery.Result2> newSR, final Map<URL, URL[]> translatedRoots,
            final Map<URL, WeakValue> unknownRoots) {
            assert sourcePath != null;
            assert libraryPath != null;
            assert binaryLibraryPath != null;
            assert unknownSourcePath != null;
            assert newCps != null;
            assert newSR  != null;
            assert translatedRoots != null;
            this.timeStamp = timeStamp;
            this.sourcePath = sourcePath;
            this.libraryPath = libraryPath;
            this.binaryLibraryPath = binaryLibraryPath;
            this.unknownSourcePath = unknownSourcePath;
            this.newCps = newCps;
            this.newSR = newSR;
            this.translatedRoots = translatedRoots;
            this.unknownRoots = unknownRoots;
        }
    }

    private class WeakValue extends WeakReference<ClassPath> implements Runnable {

        private URL key;

        public WeakValue (ClassPath ref, URL key) {
            super (ref, Utilities.activeReferenceQueue());
            assert key != null;
            this.key = key;
        }

        public void run () {
            boolean fire = false;
            synchronized (PathRegistry.this) {
                fire = (unknownRoots.remove (key) != null);
            }
            if (fire) {
                resetCacheAndFire(EventKind.PATHS_REMOVED, PathKind.UNKNOWN_SOURCE, null, null);
            }
        }
    }

    private class Listener implements GlobalPathRegistryListener, PropertyChangeListener, ChangeListener {

            private WeakReference<Object> lastPropagationId;

            public void pathsAdded(GlobalPathRegistryEvent event) {
                final String pathId = event.getId();
                final PathKind pk = getPathKind (pathId);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("pathsAdded: " + event.getId() + ", paths=" + event.getChangedPaths()); //NOI18N
                    LOGGER.fine("'" + pathId + "' -> '" + pk + "'"); //NOI18N
                }
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_ADDED, pk, pathId, event.getChangedPaths());
                }
            }

            public void pathsRemoved(GlobalPathRegistryEvent event) {
                final String pathId = event.getId();
                final PathKind pk = getPathKind (pathId);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("pathsRemoved: " + event.getId() + ", paths=" + event.getChangedPaths()); //NOI18N
                    LOGGER.fine("'" + pathId + "' -> '" + pk + "'"); //NOI18N
                }
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_REMOVED, pk, pathId, event.getChangedPaths());
                }
            }

            public void propertyChange(PropertyChangeEvent evt) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("propertyChange: " + evt.getPropertyName() //NOI18N
                            + ", old=" + evt.getOldValue() //NOI18N
                            + ", new=" + evt.getNewValue()); //NOI18N
                }

                String propName = evt.getPropertyName();
                if (ClassPath.PROP_ENTRIES.equals(propName)) {
                    resetCacheAndFire (EventKind.PATHS_CHANGED,null, null, Collections.singleton((ClassPath)evt.getSource()));
                }
                else if (ClassPath.PROP_INCLUDES.equals(propName)) {
                    final Object newPropagationId = evt.getPropagationId();
                    boolean fire;
                    synchronized (this) {
                        fire = (newPropagationId == null || lastPropagationId == null || lastPropagationId.get() != newPropagationId);
                        lastPropagationId = new WeakReference<Object>(newPropagationId);
                    }
                    if (fire) {
                        resetCacheAndFire (EventKind.PATHS_CHANGED, PathKind.SOURCE, null, Collections.singleton((ClassPath)evt.getSource()));
                    }
                }
            }

            public void stateChanged (final ChangeEvent event) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("stateChanged: " + event); //NOI18N
                }
                resetCacheAndFire(EventKind.PATHS_CHANGED, PathKind.BINARY_LIBRARY, null, null);
            }
    }
}
