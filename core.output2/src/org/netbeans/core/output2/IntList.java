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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * IntList.java
 *
 * Created on March 21, 2004, 12:18 AM
 */

package org.netbeans.core.output2;

import java.util.Arrays;

/** A collections-like lineStartList of primitive integers.  Entries may be added only
 * in ascending order.  This is used to map lines to file offsets.
 *
 * @author  Tim Boudreau
 */
final class IntList {
    private int[] array;
    private int used = 0;
    private int lastAdded = Integer.MIN_VALUE;

    /** Creates a new instance of IntMap */
    IntList(int capacity) {
        array = allocArray (capacity);
    }
    
    /** Add an integer to the lineStartList.  Must be greater than the preceding value
     * or an exception is thrown. */
    public synchronized void add (int value) {
        if (value < lastAdded) {
            throw new IllegalArgumentException ("Contents must be presorted - " + //NOI18N
                "added value " + value + " is less than preceding " + //NOI18N
                "value " + lastAdded); //NOI18N
        }
        if (used >= array.length) {
            growArray();
        }
        array[used++] = value;
        lastAdded = value;
    }
    
    private int[] allocArray (int size) {
        int[] result = new int[size];
        //Fill it with Integer.MAX_VALUE so binarySearch works properly (must
        //be sorted, cannot have 0's after the actual data
        Arrays.fill(result, Integer.MAX_VALUE);
        return result;
    }
    
    public synchronized int get(int index) {
        if (index >= used) {
            throw new ArrayIndexOutOfBoundsException("List contains " + used 
                + " items, but tried to fetch item " + index);
        }
        return array[index];
    }
    
    public boolean contains (int val) {
        return Arrays.binarySearch(array, val) >= 0;
    }
    
    /** Return the <strong>index</strong> of the value closest to but lower than
     * the passed value */
    public int findNearest (int val) {
        if (size() == 0) {
            return -1;
        }
        return findInRange (val, 0, size());
    }
    
    /** Recursive binary search */
    private int findInRange (int val, int start, int end) {
        if (end - start <= 1) {
            return start;
        }
        int midPoint = start + ((end - start) / 2);
        int valAtMidpoint = get (midPoint);
        if (valAtMidpoint > val) {
            return findInRange (val, start, start + ((end - start) / 2));
        } else {
            return findInRange (val, start + ((end - start) / 2), end);
        }
    }
    
    public int indexOf (int val) {
        int result = Arrays.binarySearch(array, val);
        if (result < 0) {
            result = -1;
        }
        if (result >= used) {
            result = -1;
        }
        return result;
    }
    
    
    public synchronized int size() {
        return used;
    }
    
    private void growArray() {
        int[] old = array;
        array = allocArray(Math.round(array.length * 1.5f));
        System.arraycopy(old, 0, array, 0, old.length);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer ("IntList [");
        for (int i=0; i < used; i++) {
            result.append (i);
            result.append (':');
            result.append (array[i]);
            if (i != used-1) {
                result.append(',');
            }
        }
        result.append (']');
        return result.toString();
    }
    
}
