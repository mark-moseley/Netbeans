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

package org.netbeans.spi.project.ui;

import org.openide.nodes.Node;

/**
 * Ability for a {@link org.netbeans.api.project.Project} to supply
 * a logical view of itself.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface LogicalViewProvider {

    /**
     * Create a logical view node.
     * Projects should not attempt to cache this node in any way;
     * this call should always create a fresh node with no parent.
     * The node's lookup should contain the project object.
     * @return a node displaying the contents of the project in an intuitive way
     */
    Node createLogicalView();
    
    /**  
    * Try to find a given node in the logical view.  
    * If some node within the logical view tree has the supplied object  
    * in its lookup, it ought to be returned if that is practical.  
    * If there are multiple such nodes, the one most suitable for display
    * to the user should be returned.<BR>
    * This may be used to select nodes corresponding to files, etc.  
    * The following constraint should hold:  
    * <pre>  
    * private static boolean isAncestor(Node root, Node n) {  
    *     if (n == null) return false;  
    *     if (n == root) return true;  
    *     return isAncestor(root, n.getParentNode());  
    * }  
    * // ...  
    * Node root = ...;  
    * Object target = ...;  
    * LogicalViewProvider lvp = ...;  
    * Node n = lvp.findPath(root, target);  
    * if (n != null) {  
    *     assert isAncestor(root, n);  
    *     Lookup.Template tmpl = new Lookup.Template(null, null, target); 
    *     Collection res = n.getLookup().lookup(tmpl).allInstances();  
    *     assert Collections.singleton(target).equals(new HashSet(res)); 
    * }  
    * </pre>  
    * @param root a root node. E.g. a node from {@link #createLogicalView} or some wapper
    *        (FilterNode) around the node. The provider of the functionality is
    *        responsible for finding the appropriate node in the wrapper's children.
    * @param target a target cookie, such as a {@link org.openide.loaders.DataObject}
    * @return a subnode with that cookie, or null  
    */
    Node findPath(Node root, Object target);
    
}
