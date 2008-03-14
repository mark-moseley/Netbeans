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

package org.netbeans.modules.viewmodel;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.spi.viewmodel.Models;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakSet;

/**
 * Ugly class, that takes care that the expansion state is always managed for the object under the given node.
 * This is necessary for trees that have equal nodes under various branches, or for recursive trees.
 * 
 * @author Martin
 */
public class DefaultTreeExpansionManager {
    
    private static Map<Models.CompoundModel, DefaultTreeExpansionManager> managers = new WeakHashMap<Models.CompoundModel, DefaultTreeExpansionManager>();
    
    private Children currentChildren;
    private Map<Children, Set<Object>> expandedNodes = new WeakHashMap<Children, Set<Object>>();
    private Map<Children, Set<Object>> collapsedNodes = new WeakHashMap<Children, Set<Object>>();
    
    public static synchronized DefaultTreeExpansionManager get(Models.CompoundModel model) {
        if (model == null) throw new NullPointerException();
        DefaultTreeExpansionManager manager = managers.get(model);
        if (manager == null) {
            manager = new DefaultTreeExpansionManager();
            managers.put(model, manager);
        }
        return manager;
    }
    
    private DefaultTreeExpansionManager() {}
    
    /** Must be called before every query, external synchronization with the model call is required. */
    public void setChildrenToActOn(Children ch) {
        currentChildren = ch;
    }
    
    /** External synchronization with currentNode required. */
    public boolean isExpanded(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            Set<Object> collapsed = collapsedNodes.get(currentChildren);
            if (expanded != null && expanded.contains(child)) {
                return true;
            }
            if (collapsed != null && collapsed.contains(child)) {
                return false;
            }
            // Default behavior follows:
            return false;
        } finally {
            currentChildren = null;
        }
    }

    public void setExpanded(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            Set<Object> collapsed = collapsedNodes.get(currentChildren);
            if (expanded == null) {
                expanded = new WeakSet<Object>();
                expandedNodes.put(currentChildren, expanded);
            }
            expanded.add(child);
            if (collapsed != null) {
                collapsed.remove(child);
            }
        } finally {
            currentChildren = null;
        }
    }

    public void setCollapsed(Object child) {
        if (currentChildren == null) throw new NullPointerException("Call setChildrenToActOn() before!!!");
        try {
            Set<Object> expanded = expandedNodes.get(currentChildren);
            Set<Object> collapsed = collapsedNodes.get(currentChildren);
            if (collapsed == null) {
                collapsed = new WeakSet<Object>();
                collapsedNodes.put(currentChildren, collapsed);
            }
            collapsed.add(child);
            if (expanded != null) {
                expanded.remove(child);
            }
        } finally {
            currentChildren = null;
        }
    }

}
