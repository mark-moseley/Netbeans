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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.parsing.spi.indexing.IndexerFactory;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class PathRegistry {
    private static PathRegistry instance;
    private static final RequestProcessor firer = new RequestProcessor ();
    
    private final GlobalPathRegistry regs;
    private final Lookup.Result<? extends IndexerFactory> indexers;

    private Set<ClassPath> activeCps;
    private Map<URL, SourceForBinaryQuery.Result2> sourceResults;
    private Map<URL, URL[]> translatedRoots;
    private Map<URL, WeakValue> unknownRoots;
    private long timeStamp;             //Lamport event ordering
    private Runnable debugCallBack;
    private volatile boolean useLibraries = true;
    private List<URL>  sourcePaths;
    private List<URL> binaryPath;
    private List<URL> unknownSourcePath;
    private Set<String> sourceIds;
    private Set<String> binaryIds;

    private final Listener listener;
    private final List<PathRegistryListener> listeners;

    private  PathRegistry () {
        regs = GlobalPathRegistry.getDefault();
        assert regs != null;
        indexers = Lookup.getDefault().lookupResult(IndexerFactory.class);
        assert indexers != null;
        this.listener = new Listener ();
        this.timeStamp = -1;
        this.activeCps = Collections.emptySet();
        this.sourceResults = Collections.emptyMap();
        this.unknownRoots = new HashMap<URL, WeakValue>();
        this.translatedRoots = new HashMap<URL, URL[]> ();
        this.listeners = new CopyOnWriteArrayList<PathRegistryListener>();
        this.regs.addGlobalPathRegistryListener ((GlobalPathRegistryListener)WeakListeners.create(GlobalPathRegistryListener.class,this.listener,this.regs));
    }

    public static synchronized PathRegistry getInstance () {
        if (instance == null) {
            instance = new PathRegistry();
        }
        return instance;
    }

    public void addPathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.add(listener);
    }
    
    public void removePathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.remove(listener);
    }

    public List<? extends URL> getSources () {
        Request request;
        synchronized (this) {
            if (this.sourcePaths != null) {
                return this.sourcePaths;
            }
            request = new Request (
                getTimeStamp(),
                regs.getPaths(ClassPath.SOURCE),
                regs.getPaths(ClassPath.BOOT),
                regs.getPaths(ClassPath.COMPILE),
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
                    this.binaryPath = res.binaryPath;
                    this.unknownSourcePath = res.sourcePath;
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

    public List<? extends URL> getBinaries () {
        Request request;
        synchronized (this) {
            if (this.binaryPath != null) {
                return this.binaryPath;
            }
            request = new Request (
                this.getTimeStamp(),
                this.regs.getPaths(ClassPath.SOURCE),
                this.regs.getPaths(ClassPath.BOOT),
                this.regs.getPaths(ClassPath.COMPILE),
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
                if (this.binaryPath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.binaryPath = res.binaryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                }
                return this.binaryPath;
            }
            else {
                return res.binaryPath;
            }
        }
    }

    public List<? extends URL> getUnknownRoots () {
        Request request;
        synchronized (this) {
            if (this.unknownSourcePath != null) {
                return unknownSourcePath;
            }
            request = new Request (
                getTimeStamp(),
                regs.getPaths(ClassPath.SOURCE),
                regs.getPaths(ClassPath.BOOT),
                regs.getPaths(ClassPath.COMPILE),
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
                    this.binaryPath = res.binaryPath;
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

    private Result createResources (final Request request) {
        assert request != null;
        return null;
    }

    private void resetCacheAndFire (final EventKind eventKind,
            final PathKind pathKind,
            final Set<? extends ClassPath> paths) {
        synchronized (this) {
            this.sourcePaths = null;
            this.binaryPath = null;
            this.unknownSourcePath = null;
            this.timeStamp++;
        }

        firer.post(new Runnable () {
            public void run() {
                fire (eventKind, pathKind, paths);
            }
        });
    }

    private void fire (final EventKind eventKind,
            final PathKind pathKind,
            final Set<? extends ClassPath> paths) {
        final PathRegistryEvent event = new PathRegistryEvent(this, eventKind, pathKind, paths);
        for (PathRegistryListener l : listeners) {
            l.pathsChanged(event);
        }
    }

    private PathKind getPathKind (final String pathId) {
        assert pathId != null;
        if (pathId == null) {
            return null;
        }
        final Set<String> sourceIds = getSourceIds();
        if (sourceIds.contains(pathId)) {
            return PathKind.SOURCE;
        }
        final Set<String> binaryIds = getBinaryIds();
        if (binaryIds.contains(pathId)) {
            return PathKind.BINARY;
        }
        return null;
    }

    private Set<String> getSourceIds () {
        synchronized (this) {
            if (this.sourceIds != null) {
                return this.sourceIds;
            }
        }
        final Set<String> sourceIds = new HashSet<String>();
        final Set<String> binaryIds = new HashSet<String>();
        lookup (sourceIds, binaryIds);
        synchronized (this) {
            if (this.sourceIds == null) {
                this.sourceIds = sourceIds;
            }
            return this.sourceIds;
        }
    }

    private Set<String> getBinaryIds () {
        synchronized (this) {
            if (this.binaryIds != null) {
                return this.binaryIds;
            }
        }
        final Set<String> sourceIds = new HashSet<String>();
        final Set<String> binaryIds = new HashSet<String>();
        lookup (sourceIds, binaryIds);
        synchronized (this) {
            if (this.binaryIds == null) {
                this.binaryIds = binaryIds;
            }
            return this.binaryIds;
        }
    }

    private void lookup (final Set<? super String> sourceIds,
            final Set<? super String> binaryIds) {
        for (IndexerFactory f : indexers.allInstances()) {
            Set<String> ids = f.getSourcePathIds();
            assert ids != null;
            sourceIds.addAll(ids);
            ids = f.getBinaryPathIds();
            assert ids != null;
            binaryIds.addAll(ids);
        }
    }

    private long getTimeStamp () {
        return this.timeStamp;
    }

    private static class Request {

        final long timeStamp;
        final Set<ClassPath> sourceCps;
        final Set<ClassPath> bootCps;
        final Set<ClassPath> compileCps;
        final Set<ClassPath> oldCps;
        final Map <URL, SourceForBinaryQuery.Result2> oldSR;
        final Map<URL, WeakValue> unknownRoots;
        final PropertyChangeListener propertyListener;
        final ChangeListener changeListener;

        public Request (final long timeStamp, final Set<ClassPath> sourceCps, final Set<ClassPath> bootCps, final Set<ClassPath> compileCps,
            final Set<ClassPath> oldCps, final Map <URL, SourceForBinaryQuery.Result2> oldSR, final Map<URL, WeakValue> unknownRoots,
            final PropertyChangeListener propertyListener, final ChangeListener changeListener) {
            assert sourceCps != null;
            assert bootCps != null;
            assert compileCps != null;
            assert oldCps != null;
            assert oldSR != null;
            assert unknownRoots != null;
            assert propertyListener != null;
            assert changeListener != null;

            this.timeStamp = timeStamp;
            this.sourceCps = sourceCps;
            this.bootCps = bootCps;
            this.compileCps = compileCps;
            this.oldCps = oldCps;
            this.oldSR = oldSR;
            this.unknownRoots = unknownRoots;
            this.propertyListener = propertyListener;
            this.changeListener = changeListener;
        }
    }

    private static class Result {

        final long timeStamp;
        final List<URL> sourcePath;
        final List<URL> binaryPath;
        final List<URL> unknownSourcePath;
        final Set<ClassPath> newCps;
        final Map<URL, SourceForBinaryQuery.Result2> newSR;
        final Map<URL, URL[]> translatedRoots;
        final Map<URL, WeakValue> unknownRoots;

        public Result (final long timeStamp, final List<URL> sourcePath,
            final List<URL> binaryPath,
            final List<URL> unknownSourcePath,
            final Set<ClassPath> newCps,
            final Map<URL, SourceForBinaryQuery.Result2> newSR, final Map<URL, URL[]> translatedRoots,
            final Map<URL, WeakValue> unknownRoots) {
            assert sourcePath != null;
            assert binaryPath != null;
            assert unknownSourcePath != null;
            assert newCps != null;
            assert newSR  != null;
            assert translatedRoots != null;
            this.timeStamp = timeStamp;
            this.sourcePath = sourcePath;
            this.binaryPath = binaryPath;
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
                resetCacheAndFire(EventKind.PATHS_REMOVED, PathKind.UNKNOWN_SOURCE,null);
            }
        }
    }

    private class Listener implements GlobalPathRegistryListener, PropertyChangeListener, ChangeListener {

            private WeakReference<Object> lastPropagationId;

            public void pathsAdded(GlobalPathRegistryEvent event) {
                final PathKind pk = getPathKind (event.getId());
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_ADDED, pk, event.getChangedPaths());
                }
            }

            public void pathsRemoved(GlobalPathRegistryEvent event) {
                final PathKind pk = getPathKind (event.getId());
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_REMOVED, pk, event.getChangedPaths());
                }
            }

            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (ClassPath.PROP_ENTRIES.equals(propName)) {
                    resetCacheAndFire (EventKind.PATHS_CHANGED,null, Collections.singleton((ClassPath)evt.getSource()));
                }
                else if (ClassPath.PROP_INCLUDES.equals(propName)) {                    
                    final Object newPropagationId = evt.getPropagationId();
                    boolean fire;
                    synchronized (this) {
                        fire = (newPropagationId == null || lastPropagationId == null || lastPropagationId.get() != newPropagationId);
                        lastPropagationId = new WeakReference<Object>(newPropagationId);
                    }
                    if (fire) {
                        resetCacheAndFire (EventKind.PATHS_CHANGED, PathKind.SOURCE,null);
                    }
                }
            }

            public void stateChanged (final ChangeEvent event) {
                resetCacheAndFire(EventKind.PATHS_CHANGED, PathKind.BINARY,null);
            }
    }
}
