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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Marian Petras
 */
class CompoundSearchInfo implements SearchInfo {

    /** */
    private final SearchInfo[] elements;

    /**
     * Creates a new instance of CompoundSearchInfo
     *
     * @param  elements  elements of this <code>SearchInfo</code>
     * @exception  java.lang.IllegalArgumentException
     *             if the argument was <code>null</code>
     */
    CompoundSearchInfo(SearchInfo... elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }
        
        this.elements = elements.length != 0 ? elements
                                             : null;
    }

    /**
     */
    public boolean canSearch() {
        if (elements != null) {
            for (SearchInfo element : elements) {
                if (element.canSearch()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    public Iterator<DataObject> objectsToSearch() {
        if (elements == null) {
            return Collections.<DataObject>emptyList().iterator();
        }
        
        List<SearchInfo> searchableElements = new ArrayList<SearchInfo>(elements.length);
        for (SearchInfo element : elements) {
            if (element.canSearch()) {
                searchableElements.add(element);
            }
        }
        return new CompoundSearchIterator(
            searchableElements.toArray(
                    new SearchInfo[searchableElements.size()]));
    }
    
}
