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
 *//*
 * ComplexListDataEvent.java
 *
 * Created on May 26, 2003, 5:17 PM
 */

package org.netbeans.swing.tabcontrol.event;

import org.netbeans.swing.tabcontrol.TabData;

import javax.swing.event.ListDataEvent;

/**
 * An extension to ListDataEvent which can report data about non-contiguous
 * changes to data.
 *
 * Eventually <code>VeryComplexListDataEvent</code> (which can also report
 * about relocation of items) should be merged into this class; it's currently
 * slightly crufty.
 *
 * @author Tim Boudreau
 */
public class ComplexListDataEvent extends ListDataEvent {
    private static final int LAST = INTERVAL_REMOVED;
    /**
     * ID for events in which non-contiguous elements have been added
     */
    public static final int ITEMS_ADDED = LAST + 1;
    /**
     * ID for events in which non-contiguous elements have been removed
     */
    public static final int ITEMS_REMOVED = LAST + 2;
    /**
     * ID for events in which non-contiguous elements have been added, removed
     * or changed
     */
    static final int ITEMS_CHANGED = LAST + 3;
    private int[] indices;
    private boolean textChanged;
    private boolean componentChanged = false;

    /**
     * Creates a new instance of ComplexListDataEvent.  The index0 and index1
     * properties will return -1.
     *
     * @param source      The event source
     * @param id          The type of change
     * @param indices     An array of possibly non-contiguous indices of data
     *                    which has changed
     * @param textChanged True if the change is one that can affect display
     *                    (icon width or text changes)
     */
    public ComplexListDataEvent(Object source, int id, int[] indices,
                                boolean textChanged) {
        super(source, id, -1, -1);
        this.textChanged = textChanged;
        this.indices = indices;
    }

    /**
     * Passthrough constructor for ListDataEvent.  <code>getIndices()</code>
     * will return null for this event.
     *
     * @param source The source of the event
     * @param id     The type of change
     * @param start  The start index for the change
     * @param end    The end index for the change
     */
    public ComplexListDataEvent(Object source, int id, int start, int end) {
        super(source, id, start, end);
        textChanged = true;
        indices = null;
    }

    public ComplexListDataEvent(Object source, int id, int start, int end,
                                boolean textChanged, boolean compChange) {
        super(source, id, start, end);
        textChanged = true;
        indices = null;
        componentChanged = compChange;
    }

    /**
     * Passthrough constructor for ListDataEvent.  <code>getIndices()</code>
     * will return null for this event.
     *
     * @param source      The source of the event
     * @param id          The type of change
     * @param start       The start index of a contiguous change
     * @param end         The end index of a contiguous change
     * @param textChanged True if the change is one that can affect display
     *                    metrics (text or icon size)
     */
    public ComplexListDataEvent(Object source, int id, int start, int end,
                                boolean textChanged) {
        this(source, id, start, end);
        this.textChanged = textChanged;
        indices = null;
    }

    /**
     * Get the indices which have changed for this event.
     *
     * @return The changed indices, or null for contiguous data changes
     */
    public int[] getIndices() {
        return indices;
    }

    /**
     * Does the change event represent a change that can affect display metrics
     *
     * @return True if the change affected text length or icon width
     */
    public boolean isTextChanged() {
        return textChanged;
    }

    /**
     * Does the change event represent a change in components.  This should be
     * true for cases where a component was replaced, added or removed
     */
    public boolean isUserObjectChanged() {
        return componentChanged;
    }

    public String toString() {
        String[] types = new String[]{
            "CONTENTS_CHANGED", "INTERVAL_ADDED", //NOI18N
            "INTERVAL_REMOVED", "ITEMS_ADDED",
            "ITEMS_REMOVED"}; //NOI18N
        StringBuffer out = new StringBuffer(getClass().getName());
        out.append(" - " + types[getType()] + " - ");
        if (getType() <= INTERVAL_REMOVED) {
            out.append("start=" + getIndex0() + " end=" + getIndex1() + " "); //NOI18N
        } else {
            int[] ids = getIndices();
            if (ids != null) {
                for (int i = 0; i < ids.length; i++) {
                    out.append(ids[i]);
                    if (i != ids.length - 1) {
                        out.append(','); //NOI18N
                    }
                }
            } else {
                out.append("null"); //NOI18N
            }
        }
        return out.toString();
    }

    public void setAffectedItems(TabData[] td) {
        affectedItems = td;
    }

    public TabData[] getAffectedItems() {
        return affectedItems;
    }

    private TabData[] affectedItems = null;
}
