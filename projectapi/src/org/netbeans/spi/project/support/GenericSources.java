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

package org.netbeans.spi.project.support;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

// XXX need test

/**
 * Factories for standard {@link Sources} implementations.
 * @author Jesse Glick
 */
public class GenericSources {
    
    private GenericSources() {}
    
    /**
     * Lists only one source folder group, of {@link Sources#TYPE_GENERIC},
     * containing the project directory, as by {@link #group}.
     * @param p a project
     * @return a simple sources implementation
     */
    public static Sources genericOnly(Project p) {
        return new GenericOnlySources(p);
    }
    
    private static final class GenericOnlySources implements Sources {
        
        private final Project p;
        
        GenericOnlySources(Project p) {
            this.p = p;
        }
        
        public SourceGroup[] getSourceGroups(String type) {
            if (type.equals(Sources.TYPE_GENERIC)) {
                return new SourceGroup[] {
                    group(p, p.getProjectDirectory(), "generic", // NOI18N
                          ProjectUtils.getInformation(p).getDisplayName(),
                          null, null),
                };
            } else {
                return new SourceGroup[0];
            }
        }
        
        public void addChangeListener(ChangeListener listener) {}
        
        public void removeChangeListener(ChangeListener listener) {}
        
    }
    
    /**
     * Default kind of source folder group.
     * Contains everything inside the supplied root folder which belongs to the
     * supplied project and is considered sharable by VCS.
     * @param p a project
     * @param rootFolder the root folder to use for sources
     * @param name a code name for the source group
     * @param displayName a display name for the source group
     * @param icon a regular icon to use for the source group, or null
     * @param openedIcon an opened variant icon to use, or null
     */
    public static SourceGroup group(Project p, FileObject rootFolder, String name, String displayName, Icon icon, Icon openedIcon) {
        return new Group(p, rootFolder, name, displayName, icon, openedIcon);
    }
    
    private static final class Group implements SourceGroup {
        
        private final Project p;
        private final FileObject rootFolder;
        private final String name;
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        
        Group(Project p, FileObject rootFolder, String name, String displayName, Icon icon, Icon openedIcon) {
            this.p = p;
            this.rootFolder = rootFolder;
            this.name = name;
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
        }
        
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                throw new IllegalArgumentException();
            }
            if (FileOwnerQuery.getOwner(file) != p) {
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null) {
                // MIXED, UNKNOWN, and SHARABLE -> include it
                return SharabilityQuery.getSharability(f) != SharabilityQuery.NOT_SHARABLE;
            } else {
                // Not on disk, include it.
                return true;
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // XXX should react to ProjectInformation changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // XXX
        }
        
        public String toString() {
            return "GenericSources.Group[name=" + name + ",rootFolder=" + rootFolder + "]"; // NOI18N
        }
        
    }
    
}
