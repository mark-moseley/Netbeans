/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.Iterator;

/**
 * Defines which <code>DataObject</code>s should be searched.
 * Iterator returned by this interface's method enumerates
 * <code>DataObject</code>s that should be searched.
 *
 * @see  DataObject
 * @see  SimpleSearchInfo
 * @author  Marian Petras
 */
public interface SearchInfo {

    /**
     * Determines whether the object which provided this <code>SearchInfo</code>
     * can be searched.
     * This method determines whether the <em>Find</em> action should be enabled
     * for the object or not.
     * <p>
     * This method must be very quick as it may be called frequently and its
     * speed may influence responsiveness of the whole application. If the exact
     * algorithm for determination of the result value should be slow, it is
     * better to return <code>true</code> than make the method slow.
     *
     * @return  <code>false</code> if the object is known that it cannot be
     *          searched; <code>true</code> otherwise
     */
    public boolean canSearch();

    /**
     * Specifies which <code>DataObject</code>s should be searched.
     * The returned <code>Iterator</code> needn't implement method
     * {@link java.util.Iterator#remove remove()} (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * @return  iterator which iterates over <code>DataObject</code>s
     *          to be searched
     */
    public Iterator objectsToSearch();
    
}
