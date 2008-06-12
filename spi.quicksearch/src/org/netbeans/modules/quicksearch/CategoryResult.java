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

package org.netbeans.modules.quicksearch;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;

/**
 * Thread safe model of provider results of asociated category.
 * 
 * @author  Jan Becicka, Dafe Simonek
 */
public final class CategoryResult {
    
    private static final int MAX_RESULTS = 7;
    
    private final Object LOCK = new Object();
    
    private ProviderModel.Category category;
    
    private List<ItemResult> items;
    
    private int counter;
    
    private boolean obsolete;

    CategoryResult (ProviderModel.Category category) {
        this.category = category;
        items = new ArrayList<ItemResult>(MAX_RESULTS);
    }
    
    public boolean addItem (ItemResult item) {
        synchronized (LOCK) {
            if (obsolete || items.size() >= MAX_RESULTS) {
                return false;
            }
            items.add(item);
            counter++;
        }
        return true;
    }
    
    /**
     * Get the value of item
     *
     * @return the value of item
     */
    public List<ItemResult> getItems() {
        List<ItemResult> rItems = null;
        synchronized (LOCK) {
            rItems = new ArrayList<ItemResult>(items);
        }
        return rItems;
    }
    
    public boolean isFirstItem (ItemResult ir) {
        synchronized (LOCK) {
            if (items.size() > 0 && items.get(0).equals(ir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the value of Category
     *
     * @return the value of Category
     */
    public ProviderModel.Category getCategory() {
        return category;
    }

    public void setObsolete(boolean obsolete) {
        synchronized (LOCK) {
            this.obsolete = obsolete;
        }
    }

}
