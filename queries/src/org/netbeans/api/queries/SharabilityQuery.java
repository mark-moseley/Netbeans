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

package org.netbeans.api.queries;

import java.io.File;
import java.util.Iterator;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.util.Lookup;

// XXX perhaps should be in the Filesystems API instead of here?

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Likely to be of use only to a VCS filesystem.
 * <p>
 * This query can be considered to obsolete {@link FileObject#setImportant}.
 * Unlike that method, the information is pulled by the VCS filesystem on
 * demand, which may be more reliable than ensuring that the information
 * is pushed by a project type (or other implementor) eagerly.
 * @see SharabilityQueryImplementation
 * @author Jesse Glick
 */
public final class SharabilityQuery {
    
    private static final Lookup.Result/*<SharabilityQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup(new Lookup.Template(SharabilityQueryImplementation.class));

    /**
     * Constant indicating that nothing is known about whether a given
     * file should be considered sharable or not.
     * A client should therefore behave in the safest way it can.
     */
    public static final int UNKNOWN = 0;
    
    /**
     * Constant indicating that the file or directory is sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * sharable.
     */
    public static final int SHARABLE = 1;
    
    /**
     * Constant indicating that the file or directory is not sharable.
     * In the case of a directory, this means that all files and
     * directories recursively contained in this directory are also
     * not sharable.
     */
    public static final int NOT_SHARABLE = 2;
    
    /**
     * Constant indicating that a directory is sharable but files and
     * directories recursively contained in it may or may not be sharable.
     * A client interested in children of this directory should explicitly
     * ask about each in turn.
     */
    public static final int MIXED = 3;
    
    private SharabilityQuery() {}
    
    /**
     * Check whether an existing file is sharable.
     * @param file a file or directory (may or may not already exist)
     * @return one of the constants in this class
     */
    public static int getSharability(File file) {
        if (file == null) throw new IllegalArgumentException();
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            SharabilityQueryImplementation sqi = (SharabilityQueryImplementation)it.next();
            int x = sqi.getSharability(file);
            if (x != UNKNOWN) {
                return x;
            }
        }
        return UNKNOWN;
    }
    
}
