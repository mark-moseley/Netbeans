/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

// XXX make it into a factory method, not a public concrete class

/**
 * An implementation of {@link FileBuiltQueryImplementation} intended to be
 * placed in {@link org.netbeans.api.project.Project#getLookup} which operates
 * by a simple glob-style file mapping.
 * It will return status objects for any files in the project matching a source
 * glob pattern - this must include exactly one asterisk (<code>*</code>)
 * representing a variable portion of a source file path (always slash-separated
 * and relative to the project directory) and may include some Ant property
 * references which will be resolved as per {@link AntProjectHelper#evaluateString}.
 * A file is considered out of date if there is no file represented by the
 * matching target pattern (which has the same format), or the target file is older
 * than the source file, or the source file is modified as per
 * {@link DataObject#isModified}.
 * An attempt is made to fire changes from the status object whenever the result
 * should change from one call to the next.
 * @author Jesse Glick
 */
public final class GlobFileBuiltQuery implements FileBuiltQueryImplementation {
    
    private final AntProjectHelper helper;
    private final FileObject projectDir;
    private final File projectDirF;
    private final String[] fromPrefixes;
    private final String[] fromSuffixes;
    private final String[] toPrefixes;
    private final String[] toSuffixes;
    private static final Object NONE = "NONE"; // NOI18N
    private final Map/*<FileObject,Reference<StatusImpl>|NONE>*/ stati = new WeakHashMap();
    private final FileL fileL;
    private final FileChangeListener weakFileL;

    /**
     * Create a new query implementation based on an Ant-based project.
     * <p>
     * The source pattern must be a relative path inside the project directory.
     * The target pattern currently must also, though this restriction may
     * be relaxed in the future.
     * </p>
     * <div class="nonnormative">
     * <p>
     * A typical set of source and target patterns would be:
     * </p>
     * <ol>
     * <li><samp>${src.dir}/*.java</samp>
     * <li><samp>${test.src.dir}/*.java</samp>
     * </ol>
     * <ol>
     * <li><samp>${build.classes.dir}/*.class</samp>
     * <li><samp>${test.build.classes.dir}/*.class</samp>
     * </ol>
     * </div>
     * @param helper the project helper object
     * @param from a list of glob patterns for source files
     * @param to a matching list of glob patterns for built files
     * @throws IllegalArgumentException if either from or to patterns
     *                                  have zero or multiple asterisks,
     *                                  or the arrays are not of equal lengths
     */
    public GlobFileBuiltQuery(AntProjectHelper helper, String[] from, String[] to) throws IllegalArgumentException {
        this.helper = helper;
        projectDir = helper.getProjectDirectory();
        projectDirF = FileUtil.toFile(projectDir);
        assert projectDirF != null;
        int l = from.length;
        if (to.length != l) {
            throw new IllegalArgumentException("Non-matching lengths"); // NOI18N
        }
        fromPrefixes = new String[l];
        fromSuffixes = new String[l];
        toPrefixes = new String[l];
        toSuffixes = new String[l];
        for (int i = 0; i < l; i++) {
            int idx = from[i].indexOf('*');
            if (idx == -1 || idx != from[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + from[i]); // NOI18N
            }
            fromPrefixes[i] = from[i].substring(0, idx);
            fromSuffixes[i] = from[i].substring(idx + 1);
            idx = to[i].indexOf('*');
            if (idx == -1 || idx != to[i].lastIndexOf('*')) {
                throw new IllegalArgumentException("Zero or multiple asterisks in " + to[i]); // NOI18N
            }
            toPrefixes[i] = to[i].substring(0, idx);
            toSuffixes[i] = to[i].substring(idx + 1);
            // XXX check that none of the pieces contain two slashes in a row, and
            // the path does not start with or end with a slash, etc.
        }
        fileL = new FileL();
        /* XXX because of #33162 (no listening to file trees), cannot just do:
        projectDir.addFileChangeListener(FileUtil.weakFileChangeListener(fileL, projectDir));
         */
        weakFileL = FileUtil.weakFileChangeListener(fileL, null);
        // XXX add properties listener to helper... if anything changes, refresh all
        // status objects and clear the stati cache; can then also keep a cache of
        // evaluated path prefixes & suffixes
    }
    
    public synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        Object o = stati.get(file);
        if (o == NONE) {
            return null;
        }
        Reference r = (Reference)o;
        StatusImpl status = (r != null) ? (StatusImpl)r.get() : null;
        if (status == null) {
            status = createStatus(file);
            stati.put(file, new WeakReference(status));
        } else {
            stati.put(file, NONE);
        }
        return status;
    }
    
    private void updateAll() {
        // Need to post a fresh task since otherwise there can be lock
        // order conflicts with masterfs.
        // XXX probably better to coalesce refreshes here...
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                synchronized (GlobFileBuiltQuery.this) {
                    Iterator/*<Reference<StatusImpl>|NONE>*/ it = stati.values().iterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o == NONE) {
                            continue;
                        }
                        Reference r = (Reference)o;
                        if (r == null) {
                            continue;
                        }
                        StatusImpl status = (StatusImpl)r.get();
                        if (status == null) {
                            continue;
                        }
                        status.isBuilt();
                    }
                }
            }
        });
    }
    
    private StatusImpl createStatus(FileObject file) {
        String path = FileUtil.getRelativePath(projectDir, file);
        if (path == null) {
            throw new IllegalArgumentException("Cannot check for status on file " + file + " outside of " + projectDir); // NOI18N
        }
        for (int i = 0; i < fromPrefixes.length; i++) {
            String prefixEval = helper.evaluateString(fromPrefixes[i]);
            if (prefixEval == null) {
                return null;
            }
            if (!path.startsWith(prefixEval)) {
                continue;
            }
            String remainder = path.substring(prefixEval.length());
            String suffixEval = helper.evaluateString(fromSuffixes[i]);
            if (suffixEval == null) {
                continue;
            }
            if (!remainder.endsWith(suffixEval)) {
                continue;
            }
            String particular = remainder.substring(0, remainder.length() - suffixEval.length());
            String toPrefixEval = helper.evaluateString(toPrefixes[i]);
            if (toPrefixEval == null) {
                continue;
            }
            String toSuffixEval = helper.evaluateString(toSuffixes[i]);
            if (toSuffixEval == null) {
                continue;
            }
            return new StatusImpl(file, toPrefixEval + particular + toSuffixEval);
        }
        return null;
    }
    
    private final class FileL implements FileChangeListener {
        
        FileL() {}
        
        public void fileChanged(FileEvent fe) {
            updateAll();
        }
        
        public void fileDataCreated(FileEvent fe) {
            updateAll();
        }
        
        public void fileDeleted(FileEvent fe) {
            updateAll();
        }
        
        public void fileFolderCreated(FileEvent fe) {
            updateAll();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            updateAll();
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
    }
    
    private final class StatusImpl implements FileBuiltQuery.Status, PropertyChangeListener/*<DataObject>*/, FileChangeListener {
        
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        private Boolean built = null;
        private final DataObject source;
        private final String[] targetPath;
        /**
         * A Filesystems representation of the current target file, or if it does not
         * exist, the lowest ancestor in the project directory which does.
         * We don't do anything with it - intentionally; its only purpose is to not
         * be garbage collected, so that changes will still be fired in it when
         * appropriate. Every time we check the timestamp on disk, we also update
         * this file object, to force the Filesystems infrastructure to keep on
         * listening to it. Wouldn't be necessary if all changes to the target file
         * went through the Filesystems API, but more typically they will occur on
         * disk and cause a refresh of some high-up parent directory.
         * Also because of the lack of hierarchical listeners (#33162), we need to
         * keep a file change listener on the last available parent.
         */
        private FileObject lastTargetApproximation;
        
        StatusImpl(FileObject source, String targetPath) {
            try {
                this.source = DataObject.find(source);
            } catch (DataObjectNotFoundException e) {
                throw new Error(e);
            }
            this.source.addPropertyChangeListener(WeakListeners.propertyChange(this, this.source));
            source.addFileChangeListener(FileUtil.weakFileChangeListener(this, source));
            StringTokenizer tok = new StringTokenizer(targetPath, "/"); // NOI18N
            this.targetPath = new String[tok.countTokens()];
            int i = 0;
            while (tok.hasMoreTokens()) {
                this.targetPath[i++] = tok.nextToken();
            }
        }
        
        // Side effect is to update its cache and maybe fire changes.
        public synchronized boolean isBuilt() {
            boolean b = isReallyBuilt();
            if (built != null && built.booleanValue() != b) {
                // XXX do not fire change from within synch block
                fireChange();
            }
            built = Boolean.valueOf(b);
            return b;
        }
        
        private boolean isReallyBuilt() {
            if (!source.isValid()) {
                return false; // whatever
            }
            if (source.isModified()) {
                return false;
            }
            File target = projectDirF;
            if (lastTargetApproximation != null) {
                lastTargetApproximation.removeFileChangeListener(weakFileL);
            }
            lastTargetApproximation = projectDir;
            for (int i = 0; i < targetPath.length; i++) {
                String piece = targetPath[i];
                target = new File(target, piece);
                if (!target.exists()) {
                    lastTargetApproximation.addFileChangeListener(weakFileL);
                    return false;
                }
                FileObject lta2 = lastTargetApproximation.getFileObject(piece);
                if (lta2 != null) {
                    lastTargetApproximation = lta2;
                }
            }
            lastTargetApproximation.addFileChangeListener(weakFileL);
            long targetTime = target.lastModified();
            long sourceTime = source.getPrimaryFile().lastModified().getTime();
            return targetTime >= sourceTime;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            if (listeners.isEmpty()) {
                return;
            }
            ChangeListener[] _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
            ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < _listeners.length; i++) {
                _listeners[i].stateChanged(ev);
            }
        }
        
        private void update() {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    isBuilt();
                }
            });
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            assert evt.getSource() instanceof DataObject;
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                update();
            }
        }
        
        public void fileChanged(FileEvent fe) {
            update();
        }
        
        public void fileDeleted(FileEvent fe) {
            update();
        }
        
        public void fileRenamed(FileRenameEvent fe) {
            update();
        }
        
        public void fileDataCreated(FileEvent fe) {
            // ignore
        }
        
        public void fileFolderCreated(FileEvent fe) {
            // ignore
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
    }
    
}
