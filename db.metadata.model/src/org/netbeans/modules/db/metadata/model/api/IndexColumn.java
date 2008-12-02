/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.api;

import org.netbeans.modules.db.metadata.model.spi.IndexColumnImplementation;

/**
 * This class represents a column in an index.  It provides more information
 * about the column, such as whether it's used in the index ascending or
 * descending
 *
 * @author David Van Couvering
 */
public class IndexColumn extends MetadataElement {
    private IndexColumnImplementation impl;

    IndexColumn(IndexColumnImplementation impl) {
        this.impl = impl;
    }

    /**
     * Return the ordering for this column in the index
     *
     * @return the ordering for this column
     */
    public Ordering getOrdering() {
        return impl.getOrdering();
    }

    /**
     * Get the ordinal position for the column in this index
     *
     * @return the ordinal position, starting at 1
     */
    public int getPosition() {
        return impl.getPosition();
    }

    @Override
    public Index getParent() {
        return impl.getParent();
    }

    /**
     * Returns the name of the column
     *
     * @return the column name
     */
    @Override
    public String getName() {
        return impl.getName();
    }

    /**
     * Get the underlying column for this index column
     *
     * @return the column for this index column.
     */
    public Column getColumn() {
        return impl.getColumn();
    }

    @Override
    public String toString() {
        return impl.toString();
    }


}
