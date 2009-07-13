/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Special container very close to Map. 
 * The difference is that it holds multiple objects for a key.
 *
 * @author Nikita Krjukov
 */
public class MultivalueMap<K, V> {

    private HashMap<K, Object> mContainer = new HashMap<K, Object>();

    public void put(K key, V value) {
        Object oldValue = mContainer.get(key);
        if (oldValue == null) {
            mContainer.put(key, value);
        } else if (oldValue instanceof List) {
            List.class.cast(oldValue).add(value);
        } else {
            ArrayList newList = new ArrayList();
            mContainer.put(key, newList);
            newList.add(oldValue);
            newList.add(value);
        }
    }

    public List<V> get(K key) {
        Object values = mContainer.get(key);
        if (values == null) {
            return Collections.EMPTY_LIST;
        } else if (values instanceof List) {
            return List.class.cast(values);
        } else {
            return Collections.singletonList((V)values);
        }
    }

    public boolean containsKey(K key) {
        return mContainer.containsKey(key);
    }

    public boolean isEmpty() {
        return mContainer.isEmpty();
    }

    /**
     * Special kind of MultivalueMap. It has key and value of the same type.
     * The is called Graph because it is helpful for graph representation.
     * 
     * @param <I>
     */
    public static class Graph<I> extends MultivalueMap<I, I> {
    }

    public static class Utils {

        public static <I> Set<I> getTreeLeafs(Graph<I> source, I startItem) {
            HashSet<I> leafs = new HashSet<I>();
            //
            // The processedItems is required because the sourceTree can be not
            // a tree but rather a graph. So it's necessary to exclude cycling!
            HashSet<I> processedItems = new HashSet<I>();
            populateLeafs(source, startItem, leafs, processedItems);
            return leafs;
        }

        private static <I> void populateLeafs(Graph<I> source,
                I startItem, Set<I> leafs, Set<I> processedItems) {
            //
            List<I> children = source.get(startItem);
            if (children.isEmpty()) {
                leafs.add(startItem);
            }
            processedItems.add(startItem);
            //
            // Process children
            for (I child : children) {
                if (!processedItems.contains(child)) {
                    populateLeafs(source, child, leafs, processedItems);
                }
            }
        }

        public static <I> Set<I> collectAllSubItems(Graph<I> source, I startItem) {
            HashSet<I> result = new HashSet<I>();
            populateAllSubItems(source, startItem, result);
            return result;
        }

        public static <I> void populateAllSubItems(Graph<I> source, I startItem, Set<I> result) {
            result.add(startItem);
            //
            //
            // Process children
            List<I> children = source.get(startItem);
            for (I child : children) {
                if (!result.contains(child)) {
                    populateAllSubItems(source, child, result);
                }
            }
        }

    }
}
