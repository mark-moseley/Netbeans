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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package com.sun.data.provider;

/**
 * <p>TableDataListener is an event listener interface that supports
 * processing events produced by a corresponding {@link TableDataProvider}
 * instance, in addition to those fired by the underlying {@link DataProvider}.
 * </p>
 *
 * <p>If a value change occurs for the row whose row matches the current cursor
 * row for this {@link TableDataProvider}, the <code>valueChanged()</code> event
 * from the underlying {@link DataListener} interface will be fired after the
 * <code>valueChanged()</code> method from this interface.</p>
 *
 * @author Joe Nuxoll
 */
public interface TableDataListener extends DataListener {

    /**
     * <p>Process an event indicating that a data element's value has
     * been changed for the specified row.</p>
     *
     * @param provider <code>TableDataProvider</code> containing the data
     *        element that has had a value change
     * @param fieldKey <code>FieldKey</code> representing the specific data
     *        element that has had a value change
     * @param rowKey <code>RowKey</code> for the row whose data element value
     *        has been changed
     * @param oldValue The old value of this data element
     * @param newValue The new value of this data element
     */
    public void valueChanged(TableDataProvider provider,
        FieldKey fieldKey, RowKey rowKey, Object oldValue, Object newValue);

    /**
     * <p>A new row has been added to the {@link TableDataProvider}.</p>
     *
     * @param provider <code>TableDataProvider</code> that added an row
     * @param rowKey The newly added row
     */
    public void rowAdded(TableDataProvider provider, RowKey rowKey);

    /**
     * <p>An row has been removed from the {@link TableDataProvider}.</p>
     *
     * @param provider <code>TableDataProvider</code> that removed an row
     * @param rowKey The recently removed row
     */
    public void rowRemoved(TableDataProvider provider, RowKey rowKey);
}
