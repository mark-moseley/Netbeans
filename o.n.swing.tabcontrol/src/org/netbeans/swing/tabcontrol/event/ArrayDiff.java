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

package org.netbeans.swing.tabcontrol.event;

import org.netbeans.swing.tabcontrol.TabData;

import java.util.*;

/*
 * ArrayDiff.java
 *
 * Created on November 5, 2003, 12:44 PM
 */

/**
 * Class representing a diff of two arrays.  Note that it is
 * <strong>not</strong> designed to work with arrays which contain the same
 * element more than one time - in that case, the results are undefined.
 * <p/>
 * <p>Note the current implementation is unoptimized and fairly brute force.
 *
 * @author Tim Boudreau
 */
public final class ArrayDiff {
    /**
     * The old array
     */
    private TabData[] old;
    /**
     * The new array
     */
    private TabData[] nue;
    
    //XXX all of this could be implemented more efficiently with a single
    //loop to calculate all statistics and so forth.  The approach is algorithmically
    //inelegant and brute force. To do that would significantly
    //increase the complexity of the code, but it could be done later as an
    //optimization
    
    /**
     * Creates a new instance of ArrayDiff
     */
    private ArrayDiff(TabData[] old, TabData[] nue) {
        this.old = old;
        this.nue = nue;
        if (nue == null || old == null) {
            throw new NullPointerException(old == null && nue == null ?
                                           "Both arrays are null" :
                                           old == null ?
                                           "Old array is null" :
                                           "New array is null");
        }
    }

    /**
     * Get the array representing the old state
     */
    public TabData[] getOldData() {
        return old;
    }

    /**
     * Get the array representing the new state
     */
    public TabData[] getNewData() {
        return nue;
    }

    /**
     * Returns an ArrayDiff object if the two arrays are not the same, or null
     * if they are
     */
    public static ArrayDiff createDiff(TabData[] old, TabData[] nue) {
        if (!Arrays.equals(old, nue)) {
            return new ArrayDiff(old, nue);
        } else {
            return null;
        }
    }

    private Set deleted = null;

    /**
     * Returns the indices of objects in the old array which are not present in
     * the new array.  The resulting array's size will be that of the old array
     */
    public Set getDeletedIndices() {
        if (deleted == null) {
            HashSet set = new HashSet(Arrays.asList(nue));
            HashSet results = new HashSet(old.length);
            for (int i = 0; i < old.length; i++) {
                if (!set.contains(old[i])) {
                    results.add(new Integer(i));
                }
            }
            deleted = results;
        }
        return deleted;
    }

    private Set added = null;

    /**
     * Returns the indices of objects in the new array which are not present in
     * the old array
     */
    public Set getAddedIndices() {
        if (added == null) {
            HashSet set = new HashSet(Arrays.asList(old));
            Set results = new HashSet(nue.length);
            for (int i = 0; i < nue.length; i++) {
                if (!set.contains(nue[i])) {
                    results.add(new Integer(i));
                }
            }
            added = results;
        }
        return added;
    }

    /**
     * Returns the indices of objects which differ in any way between the new
     * and old array.  The size of the result is Math.max(old.length,
     * nue.length).
     */
    public Set getChangedIndices() {
        //XXX can add similar caching as with deleted/added fields if it looks
        //to prove useful.  getDeletedIndices() and getAddedIndices() are called
        //more than once, and the computation can be expensive.
        int max = Math.max(nue.length, old.length);
        HashSet results = new HashSet(max);

        for (int i = 0; i < max; i++) {
            if (i < old.length && i < nue.length) {
                if (!old[i].equals(nue[i])) {
                    results.add(new Integer(i));
                }
            } else {
                results.add(new Integer(i));
            }
        }
        return results;
    }

    /**
     * Returns the indices of objects which were in the old array and are also
     * in the new array, but at a different index.  The indices returned are
     * indices into the old array.
     */
    public Set getMovedIndices() {
        HashSet set = new HashSet(Arrays.asList(nue));
        HashSet results = new HashSet(old.length);

        for (int i = 0; i < old.length; i++) {
            boolean isPresent = set.contains(old[i]);
            if (isPresent) {
                boolean isMoved = (i < nue.length
                        && !nue[i].equals(old[i])) || i >= nue.length;
                if (isMoved) {
                    results.add(new Integer(i));
                }
            }
        }
        return results;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<ArrayDiff: deleted indices: [");
        sb.append(outCol(getDeletedIndices()));
        sb.append("] added indices: [");
        sb.append(outCol(getAddedIndices()));
        sb.append("] changed indices: [");
        sb.append(outCol(getChangedIndices()));
        sb.append("] moved indices: [");
        sb.append(outCol(getChangedIndices()));
        sb.append("]>");
        return sb.toString();
    }

    private static String outCol(Collection c) {
        Iterator i = c.iterator();
        StringBuffer result = new StringBuffer();
        while (i.hasNext()) {
            Object o = i.next();
            result.append(o.toString());
            if (i.hasNext()) {
                result.append(",");
            }
        }
        return result.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof ArrayDiff) {
            if (o == this) {
                return true;
            }
            TabData[] otherOld = ((ArrayDiff) o).getOldData();
            TabData[] otherNue = ((ArrayDiff) o).getNewData();
            return Arrays.equals(old, otherOld)
                    && Arrays.equals(nue, otherNue);
        }
        return false;
    }

    public int hashCode() {
        return arrayHashCode(old) ^ arrayHashCode(nue);
    }

    private static int arrayHashCode(Object[] o) {
        int result = 0;
        for (int i = 0; i < o.length; i++) {
            result += o[i].hashCode() ^ i;
        }
        return result;
    }
}
