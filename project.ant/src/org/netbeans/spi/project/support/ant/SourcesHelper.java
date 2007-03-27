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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.modules.project.ant.FileChangeSupport;
import org.netbeans.modules.project.ant.FileChangeSupportEvent;
import org.netbeans.modules.project.ant.FileChangeSupportListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

// XXX should perhaps be legal to call add* methods at any time (should update things)
// and perhaps also have remove* methods
// and have code names for each source dir?

// XXX should probably all be wrapped in ProjectManager.mutex

/**
 * Helper class to work with source roots and typed folders of a project.
 * @author Jesse Glick
 */
public final class SourcesHelper {
    
    private class Root {
        protected final String location;
        public Root(String location) {
            this.location = location;
        }
        public final File getActualLocation() {
            String val = evaluator.evaluate(location);
            if (val == null) {
                return null;
            }
            return project.resolveFile(val);
        }
        public Collection<FileObject> getIncludeRoots() {
            File loc = getActualLocation();
            if (loc != null) {
                FileObject fo = FileUtil.toFileObject(loc);
                if (fo != null) {
                    return Collections.singleton(fo);
                }
            }
            return Collections.emptySet();
        }
    }
    
    private class SourceRoot extends Root {

        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        private final String includes;
        private final String excludes;
        private PathMatcher matcher;

        public SourceRoot(String location, String includes, String excludes, String displayName, Icon icon, Icon openedIcon) {
            super(location);
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
            this.includes = includes;
            this.excludes = excludes;
        }

        public final SourceGroup toGroup(FileObject loc) {
            assert loc != null;
            return new Group(loc);
        }

        @Override
        public String toString() {
            return "SourceRoot[" + location + "]"; // NOI18N
        }

        // Copied w/ mods from GenericSources.
        private final class Group implements SourceGroup, PropertyChangeListener {

            private final FileObject loc;
            private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

            Group(FileObject loc) {
                this.loc = loc;
                evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
            }

            public FileObject getRootFolder() {
                return loc;
            }

            public String getName() {
                return location.length() > 0 ? location : "generic"; // NOI18N
            }

            public String getDisplayName() {
                return displayName;
            }

            public Icon getIcon(boolean opened) {
                return opened ? icon : openedIcon;
            }

            public boolean contains(FileObject file) throws IllegalArgumentException {
                if (file == loc) {
                    return true;
                }
                String path = FileUtil.getRelativePath(loc, file);
                if (path == null) {
                    throw new IllegalArgumentException();
                }
                if (file.isFolder()) {
                    path += "/"; // NOI18N
                }
                computeIncludeExcludePatterns();
                if (!matcher.matches(path, true)) {
                    return false;
                }
                Project p = getProject();
                if (file.isFolder() && file != p.getProjectDirectory() && ProjectManager.getDefault().isProject(file)) {
                    // #67450: avoid actually loading the nested project.
                    return false;
                }
                Project owner = FileOwnerQuery.getOwner(file);
                if (owner != null && owner != p) {
                    return false;
                }
                File f = FileUtil.toFile(file);
                if (f != null && SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE) {
                    return false;
                } // else MIXED, UNKNOWN, or SHARABLE; or not a disk file
                return true;
            }

            public void addPropertyChangeListener(PropertyChangeListener l) {
                pcs.addPropertyChangeListener(l);
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
                pcs.removePropertyChangeListener(l);
            }

            @Override
            public String toString() {
                return "SourcesHelper.Group[name=" + getName() + ",rootFolder=" + getRootFolder() + "]"; // NOI18N
            }

            public void propertyChange(PropertyChangeEvent ev) {
                assert ev.getSource() == evaluator : ev;
                String prop = ev.getPropertyName();
                if (prop == null ||
                        (includes != null && includes.contains("${" + prop + "}")) || // NOI18N
                        (excludes != null && excludes.contains("${" + prop + "}"))) { // NOI18N
                    matcher = null;
                    pcs.firePropertyChange(PROP_CONTAINERSHIP, null, null);
                }
                // XXX should perhaps react to ProjectInformation changes? but nothing to fire currently
            }

        }

        private String evalForMatcher(String raw) {
            if (raw == null) {
                return null;
            }
            String patterns = evaluator.evaluate(raw);
            if (patterns == null) {
                return null;
            }
            if (patterns.matches("\\$\\{[^}]+\\}")) { // NOI18N
                // Unevaluated single property, treat like null.
                return null;
            }
            return patterns;
        }

        private void computeIncludeExcludePatterns() {
            if (matcher != null) {
                return;
            }
            String includesPattern = evalForMatcher(includes);
            String excludesPattern = evalForMatcher(excludes);
            matcher = new PathMatcher(includesPattern, excludesPattern, getActualLocation());
        }


        @Override
        public Collection<FileObject> getIncludeRoots() {
            Collection<FileObject> supe = super.getIncludeRoots();
            computeIncludeExcludePatterns();
            if (supe.size() == 1) {
                Set<FileObject> roots = new HashSet<FileObject>();
                for (File r : matcher.findIncludedRoots()) {
                    FileObject subroot = FileUtil.toFileObject(r);
                    if (subroot != null) {
                        roots.add(subroot);
                    }
                }
                return roots;
            } else {
                assert supe.isEmpty();
                return supe;
            }
        }

    }
    
    private final class TypedSourceRoot extends SourceRoot {
        private final String type;
        public TypedSourceRoot(String type, String location, String includes, String excludes, String displayName, Icon icon, Icon openedIcon) {
            super(location, includes, excludes, displayName, icon, openedIcon);
            this.type = type;
        }
        public final String getType() {
            return type;
        }
    }
    
    private final AntProjectHelper project;
    private final PropertyEvaluator evaluator;
    private final List<SourceRoot> principalSourceRoots = new ArrayList<SourceRoot>();
    private final List<Root> nonSourceRoots = new ArrayList<Root>();
    private final List<TypedSourceRoot> typedSourceRoots = new ArrayList<TypedSourceRoot>();
    private int registeredRootAlgorithm;
    /**
     * If not null, external roots that we registered the last time.
     * Used when a property change is encountered, to see if the set of external
     * roots might have changed. Hold the actual files (not e.g. URLs); see
     * {@link #registerExternalRoots} for the reason why.
     */
    private Set<FileObject> lastRegisteredRoots;
    private PropertyChangeListener propChangeL;
    
    /**
     * Create the helper object, initially configured to recognize only sources
     * contained inside the project directory.
     * @param project an Ant project helper
     * @param evaluator a way to evaluate Ant properties used to define source locations
     */
    public SourcesHelper(AntProjectHelper project, PropertyEvaluator evaluator) {
        this.project = project;
        this.evaluator = evaluator;
    }
    
    /**
     * Add a possible principal source root, or top-level folder which may
     * contain sources that should be considered part of the project.
     * <p>
     * If the actual value of the location is inside the project directory,
     * this is simply ignored; so it safe to configure principal source roots
     * for any source directory which might be set to use an external path, even
     * if the common location is internal.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @see #registerExternalRoots
     * @see Sources#TYPE_GENERIC
     */
    public void addPrincipalSourceRoot(String location, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        addPrincipalSourceRoot(location, null, null, displayName, icon, openedIcon);
    }

    /**
     * Add a possible principal source root, or top-level folder which may
     * contain sources that should be considered part of the project, with
     * optional include and exclude lists.
     * <p>
     * If an include or exclude string is given as null, then it is skipped. A non-null value is
     * evaluated and then treated as a comma- or space-separated pattern list,
     * as detailed in the Javadoc for {@link PathMatcher}.
     * (As a special convenience, a value consisting solely of an Ant property reference
     * which cannot be evaluated, e.g. <samp>${undefined}</samp>, is treated like null.)
     * {@link SourceGroup#contains} will then reflect the includes and excludes for files, but note that the
     * semantics of that method requires that a folder be "contained" in case any folder or file
     * beneath it is contained, and in particular the root folder is always contained.
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param includes Ant-style includes; may contain Ant property substitutions;
     *                 if not null, only files and folders
     *                 matching the pattern (or patterns), and not specified in the excludes list,
     *                 will be {@link SourceGroup#contains included}
     * @param excludes Ant-style excludes; may contain Ant property substitutions;
     *                 if not null, files and folders
     *                 matching the pattern (or patterns) will not be {@link SourceGroup#contains included},
     *                 even if specified in the includes list
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @see #registerExternalRoots
     * @see Sources#TYPE_GENERIC
     * @since org.netbeans.modules.project.ant/1 1.15
     */
    public void addPrincipalSourceRoot(String location, String includes, String excludes, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        principalSourceRoots.add(new SourceRoot(location, includes, excludes, displayName, icon, openedIcon));
    }
    
    /**
     * Similar to {@link #addPrincipalSourceRoot} but affects only
     * {@link #registerExternalRoots} and not {@link #createSources}.
     * <p class="nonnormative">
     * Useful for project type providers which have external paths holding build
     * products. These should not appear in {@link Sources}, yet it may be useful
     * for {@link FileOwnerQuery} to know the owning project (for example, in order
     * for a project-specific {@link org.netbeans.spi.queries.SourceForBinaryQueryImplementation} to work).
     * </p>
     * @param location a project-relative or absolute path giving the location
     *                 of a non-source tree; may contain Ant property substitutions
     * @throws IllegalStateException if this method is called after
     *                               {@link #registerExternalRoots} was called
     */
    public void addNonSourceRoot(String location) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        nonSourceRoots.add(new Root(location));
    }
    
    /**
     * Add a typed source root which will be considered only in certain contexts.
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param type a source root type such as <a href="@JAVA/PROJECT@/org/netbeans/api/java/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     */
    public void addTypedSourceRoot(String location, String type, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        addTypedSourceRoot(location, null, null, type, displayName, icon, openedIcon);
    }
    
    /**
     * Add a typed source root with optional include and exclude lists.
     * See {@link #addPrincipalSourceRoot(String,String,String,String,Icon,Icon)}
     * for details on semantics of includes and excludes.
     * @param location a project-relative or absolute path giving the location
     *                 of a source tree; may contain Ant property substitutions
     * @param includes an optional list of Ant-style includes
     * @param excludes an optional list of Ant-style excludes
     * @param type a source root type such as <a href="@JAVA/PROJECT@/org/netbeans/api/java/project/JavaProjectConstants.html#SOURCES_TYPE_JAVA"><code>JavaProjectConstants.SOURCES_TYPE_JAVA</code></a>
     * @param displayName a display name (for {@link SourceGroup#getDisplayName})
     * @param icon a regular icon for the source root, or null
     * @param openedIcon an opened variant icon for the source root, or null
     * @throws IllegalStateException if this method is called after either
     *                               {@link #createSources} or {@link #registerExternalRoots}
     *                               was called
     * @since org.netbeans.modules.project.ant/1 1.15
     */
    public void addTypedSourceRoot(String location, String includes, String excludes, String type, String displayName, Icon icon, Icon openedIcon) throws IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called"); // NOI18N
        }
        typedSourceRoots.add(new TypedSourceRoot(type, location, includes, excludes, displayName, icon, openedIcon));
    }
    
    private Project getProject() {
        return AntBasedProjectFactorySingleton.getProjectFor(project);
    }
    
    /**
     * Register all external source or non-source roots using {@link FileOwnerQuery#markExternalOwner}.
     * <p>
     * Only roots added by {@link #addPrincipalSourceRoot} and {@link #addNonSourceRoot}
     * are considered. They are registered if (and only if) they in fact fall
     * outside of the project directory, and of course only if the folders really
     * exist on disk. Currently it is not defined when this file existence check
     * is done (e.g. when this method is first called, or periodically) or whether
     * folders which are created subsequently will be registered, so project type
     * providers are encouraged to create all desired external roots before calling
     * this method.
     * </p>
     * <p>
     * If the actual value of the location changes (due to changes being
     * fired from the property evaluator), roots which were previously internal
     * and are now external will be registered, and roots which were previously
     * external and are now internal will be unregistered. The (un-)registration
     * will be done using the same algorithm as was used initially.
     * </p>
     * <p>
     * If an explicit include list is configured for a principal source root, only those
     * subfolders which are included (or folders directly containing included files)
     * will be registered. Note that the source root, or an included subfolder, will
     * be registered even if it contains excluded files or folders beneath it.
     * </p>
     * <p>
     * Calling this method causes the helper object to hold strong references to the
     * current external roots, which helps a project satisfy the requirements of
     * {@link FileOwnerQuery#EXTERNAL_ALGORITHM_TRANSIENT}.
     * </p>
     * <p>
     * You may <em>not</em> call this method inside the project's constructor, as
     * it requires the actual project to exist and be registered in {@link ProjectManager}.
     * Typically you would use {@link org.openide.util.Mutex#postWriteRequest} to run it
     * later, if you were creating the helper in your constructor, since the project construction
     * normally occurs in read access.
     * </p>
     * @param algorithm an external root registration algorithm as per
     *                  {@link FileOwnerQuery#markExternalOwner}
     * @throws IllegalArgumentException if the algorithm is unrecognized
     * @throws IllegalStateException if this method is called more than once on a
     *                               given <code>SourcesHelper</code> object
     */
    public void registerExternalRoots(int algorithm) throws IllegalArgumentException, IllegalStateException {
        if (lastRegisteredRoots != null) {
            throw new IllegalStateException("registerExternalRoots was already called before"); // NOI18N
        }
        registeredRootAlgorithm = algorithm;
        remarkExternalRoots();
    }
    
    private void remarkExternalRoots() throws IllegalArgumentException {
        List<Root> allRoots = new ArrayList<Root>(principalSourceRoots);
        allRoots.addAll(nonSourceRoots);
        Project p = getProject();
        FileObject pdir = project.getProjectDirectory();
        // First time: register roots and add to lastRegisteredRoots.
        // Subsequent times: add to newRootsToRegister and maybe add them later.
        if (lastRegisteredRoots == null) {
            // First time.
            lastRegisteredRoots = Collections.emptySet();
            propChangeL = new PropChangeL(); // hold a strong ref
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(propChangeL, evaluator));
        }
        Set<FileObject> newRegisteredRoots = new HashSet<FileObject>();
        // XXX might be a bit more efficient to cache for each root the actualLocation value
        // that was last computed, and just check if that has changed... otherwise we wind
        // up calling APH.resolveFileObject repeatedly (for each property change)
        for (Root r : allRoots) {
            for (FileObject loc : r.getIncludeRoots()) {
                if (!loc.isFolder()) {
                    continue;
                }
                if (FileUtil.getRelativePath(pdir, loc) != null) {
                    // Inside projdir already. Skip it.
                    continue;
                }
                try {
                    Project other = ProjectManager.getDefault().findProject(loc);
                    if (other != null) {
                        // This is a foreign project; we cannot own it. Skip it.
                        continue;
                    }
                } catch (IOException e) {
                    // Assume it is a foreign project and skip it.
                    continue;
                }
                // It's OK to go.
                newRegisteredRoots.add(loc);
            }
        }
        // Just check for changes since the last time.
        Set<FileObject> toUnregister = new HashSet<FileObject>(lastRegisteredRoots);
        toUnregister.removeAll(newRegisteredRoots);
        for (FileObject loc : toUnregister) {
            FileOwnerQuery.markExternalOwner(loc, null, registeredRootAlgorithm);
        }
        Set<FileObject> toRegister = new HashSet<FileObject>(newRegisteredRoots);
        toRegister.removeAll(lastRegisteredRoots);
        for (FileObject loc : toRegister) {
            FileOwnerQuery.markExternalOwner(loc, p, registeredRootAlgorithm);
        }
        lastRegisteredRoots = newRegisteredRoots;
    }

    /**
     * Create a source list object.
     * <p>
     * All principal source roots are listed as {@link Sources#TYPE_GENERIC} unless they
     * are inside the project directory. The project directory itself is also listed
     * (with a display name according to {@link ProjectUtils#getInformation}), unless
     * it is contained by an explicit principal source root (i.e. ancestor directory).
     * Principal source roots should never overlap; if two configured
     * principal source roots are determined to have the same root folder, the first
     * configured root takes precedence (which only matters in regard to the display
     * name); if one root folder is contained within another, the broader
     * root folder subsumes the narrower one so only the broader root is listed.
     * </p>
     * <p>
     * Other source groups are listed according to the named typed source roots.
     * There is no check performed that these do not overlap (though a project type
     * provider should for UI reasons avoid this situation).
     * </p>
     * <p>
     * Any source roots which do not exist on disk are ignored, as if they had
     * not been configured at all. Currently it is not defined when this existence
     * check is performed (e.g. when this method is called, when the source root
     * is first accessed, periodically, etc.), so project type providers are
     * generally encouraged to make sure all desired source folders exist
     * before calling this method, if creating a new project.
     * </p>
     * <p>
     * Source groups are created according to the semantics described in
     * {@link GenericSources#group}. They are listed in the order they
     * were configured (for those roots that are actually used as groups).
     * </p>
     * <p>
     * You may call this method inside the project's constructor, but
     * {@link Sources#getSourceGroups} may <em>not</em> be called within the
     * constructor, as it requires the actual project object to exist and be
     * registered in {@link ProjectManager}.
     * </p>
     * @return a source list object suitable for {@link Project#getLookup}
     */
    public Sources createSources() {
        return new SourcesImpl();
    }
    
    private final class SourcesImpl implements Sources, PropertyChangeListener, FileChangeSupportListener {
        
        private final ChangeSupport cs = new ChangeSupport(this);
        private boolean haveAttachedListeners;
        private final Set<File> rootsListenedTo = new HashSet<File>();
        /**
         * The root URLs which were computed last, keyed by group type.
         */
        private final Map<String,List<URL>> lastComputedRoots = new HashMap<String,List<URL>>();
        
        public SourcesImpl() {
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        }
        
        public SourceGroup[] getSourceGroups(String type) {
            List<SourceGroup> groups = new ArrayList<SourceGroup>();
            if (type.equals(Sources.TYPE_GENERIC)) {
                List<SourceRoot> roots = new ArrayList<SourceRoot>(principalSourceRoots);
                // Always include the project directory itself as a default:
                roots.add(new SourceRoot("", null, null, ProjectUtils.getInformation(getProject()).getDisplayName(), null, null)); // NOI18N
                Map<FileObject,SourceRoot> rootsByDir = new LinkedHashMap<FileObject,SourceRoot>();
                // First collect all non-redundant existing roots.
                for (SourceRoot r : roots) {
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        continue;
                    }
                    if (rootsByDir.containsKey(loc)) {
                        continue;
                    }
                    rootsByDir.put(loc, r);
                }
                // Remove subroots.
                Iterator<FileObject> it = rootsByDir.keySet().iterator();
                while (it.hasNext()) {
                    FileObject loc = it.next();
                    FileObject parent = loc.getParent();
                    while (parent != null) {
                        if (rootsByDir.containsKey(parent)) {
                            // This is a subroot of something, so skip it.
                            it.remove();
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                // Everything else is kosher.
                for (Map.Entry<FileObject,SourceRoot> entry : rootsByDir.entrySet()) {
                    groups.add(entry.getValue().toGroup(entry.getKey()));
                }
            } else {
                Set<FileObject> dirs = new HashSet<FileObject>();
                for (TypedSourceRoot r : typedSourceRoots) {
                    if (!r.getType().equals(type)) {
                        continue;
                    }
                    File locF = r.getActualLocation();
                    if (locF == null) {
                        continue;
                    }
                    listen(locF);
                    FileObject loc = FileUtil.toFileObject(locF);
                    if (loc == null) {
                        continue;
                    }
                    if (!dirs.add(loc)) {
                        // Already had one.
                        continue;
                    }
                    groups.add(r.toGroup(loc));
                }
            }
            // Remember what we computed here so we know whether to fire changes later.
            List<URL> rootURLs = new ArrayList<URL>(groups.size());
            for (SourceGroup g : groups) {
                try {
                    rootURLs.add(g.getRootFolder().getURL());
                } catch (FileStateInvalidException e) {
                    assert false : e; // should be a valid file object!
                }
            }
            lastComputedRoots.put(type, rootURLs);
            return groups.toArray(new SourceGroup[groups.size()]);
        }
        
        private void listen(File rootLocation) {
            // #40845. Need to fire changes if a source root is added or removed.
            if (rootsListenedTo.add(rootLocation) && /* be lazy */ haveAttachedListeners) {
                FileChangeSupport.DEFAULT.addListener(this, rootLocation);
            }
        }
        
        public void addChangeListener(ChangeListener listener) {
            if (!haveAttachedListeners) {
                haveAttachedListeners = true;
                for (File rootLocation : rootsListenedTo) {
                    FileChangeSupport.DEFAULT.addListener(this, rootLocation);
                }
            }
            cs.addChangeListener(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }
        
        private void maybeFireChange() {
            // #47451: check whether anything really changed.
            boolean change = false;
            // Cannot iterate over entrySet, as the map will be modified by getSourceGroups.
            for (String type : new HashSet<String>(lastComputedRoots.keySet())) {
                List<URL> previous = new ArrayList<URL>(lastComputedRoots.get(type));
                getSourceGroups(type);
                List<URL> nue = lastComputedRoots.get(type);
                if (!nue.equals(previous)) {
                    change = true;
                    break;
                }
            }
            if (change) {
                cs.fireChange();
            }
        }

        public void fileCreated(FileChangeSupportEvent event) {
            // Root might have been created on disk.
            maybeFireChange();
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            // Root might have been deleted.
            maybeFireChange();
        }

        public void fileModified(FileChangeSupportEvent event) {
            // ignore; generally should not happen (listening to dirs)
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            // Properties may have changed so as cause external roots to move etc.
            maybeFireChange();
        }

    }
    
    private final class PropChangeL implements PropertyChangeListener {
        
        public PropChangeL() {}
        
        public void propertyChange(PropertyChangeEvent evt) {
            // Some properties changed; external roots might have changed, so check them.
            for (SourceRoot r : principalSourceRoots) {
                r.matcher = null;
            }
            remarkExternalRoots();
        }
        
    }
    
}
