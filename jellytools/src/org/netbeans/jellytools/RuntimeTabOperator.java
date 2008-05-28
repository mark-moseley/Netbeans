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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.RuntimeViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Runtime TopComponent.<p>
 * Functionality related to Runtime tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 * Example:<p>
 * <pre>
 *      RuntimeTabOperator rto = RuntimeTabOperator.invoke();
 *      // or when Runtime pane is already opened
 *      //RuntimeTabOperator rto = new RuntimeTabOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = rto.tree();
 *      // work with nodes
 *      rto.getRootNode().select();
 *      Node node = new Node(rto.getRootNode(), "subnode|sub subnode");
 * </pre> 
 *
 * @see RuntimeViewAction
 */
public class RuntimeTabOperator extends TopComponentOperator {

    static final String RUNTIME_CAPTION = Bundle.getString("org.netbeans.core.ide.resources.Bundle", "UI/Runtime");
    private static final RuntimeViewAction viewAction = new RuntimeViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Runtime TopComponent through all IDE. */    
    public RuntimeTabOperator() {
        super(waitTopComponent(null, RUNTIME_CAPTION, 0, new RuntimeTabSubchooser()));
    }
    
    /** invokes Runtime and returns new instance of RuntimeTabOperator
     * @return new instance of RuntimeTabOperator */    
    public static RuntimeTabOperator invoke() {
        viewAction.perform();
        return new RuntimeTabOperator();
    }
    
    /** getter for Runtime JTreeOperator
     * @return JTreeOperator of Runtime tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** getter for Runtime root node
     * @return RuntimeRootNode */    
    public Node getRootNode() {
        return new Node(tree(), "");
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.core.NbMainExplorer$MainTab
     * Used in constructor.
     */
    private static final class RuntimeTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("MainTab");
        }
        
        public String getDescription() {
            return "org.netbeans.core.NbMainExplorer$MainTab";
        }
    }
}
