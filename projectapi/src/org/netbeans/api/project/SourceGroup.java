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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.openide.filesystems.FileObject;

/**
 * Representation of one area of sources.
 * @author Jesse Glick
 * @see Sources
 */
public interface SourceGroup {
    
    /**
     * Pseudo-property used to indicate changes in containership of some subfiles.
     */
    String PROP_CONTAINERSHIP = "containership"; // NOI18N
    
    /**
     * Get the folder forming the root of this group of sources.
     * @return the root folder (must be a folder, not a file)
     */
    FileObject getRootFolder();
    
    /**
     * Get a code name suitable for internal identification of this source group.
     * Should be unique among the source groups of a given type
     * contained in a single {@link Sources} object.
     * @return a code name
     */
    String getName();

    /**
     * Get a display name suitable for presentation to a user.
     * Should preferably be unique among the source groups of a given type
     * contained in a single {@link Sources} object.
     * @return a display name
     */
    String getDisplayName();

    /**
     * Get an icon for presentation to a user.
     * @param opened if true, may select an alternative "open" variant
     * @return an icon, or null if no specific icon is needed
     */
    Icon getIcon(boolean opened);

    /**
     * Check whether the given file is contained in this group.
     * A constraint is that the root folder must be contained and
     * if any file (other than the root folder) is contained then
     * its parent must be as well.
     * @param file a file or folder; must be a descendant of the root folder
     * @return true if the group contains that file, false if it is to be excluded
     * @throws IllegalArgumentException if a file is passed which is not inside the root
     */
    boolean contains(FileObject file) throws IllegalArgumentException;
    
    /**
     * Add a listener to changes in aspects of the source group.
     * The property names used may be normal JavaBean names
     * (<code>rootFolder</code>, <code>name</code>, <code>displayName</code>,
     * <code>icon</code>) or {@link #PROP_CONTAINERSHIP}.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a listener to changes in aspects of the source group.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
