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

package org.netbeans.lib.profiler.results.cpu.cct;

import org.netbeans.lib.profiler.results.cpu.cct.RuntimeCPUCCTNodeVisitor;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.CategoryCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode;


/**
 * This class is used to walk the Call-Context-Tree structure identified by its root node
 * in an easy and predictable manner. The tree walking process starts by accepting this walker
 * by a tree root node.
 * @author Jaroslav Bachorik
 */
public abstract class CPUCCTWalker implements RuntimeCPUCCTNodeVisitor {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void visit(MethodCPUCCTNode node) {
        walkTree(node);
    }

    public void visit(ThreadCPUCCTNode node) {
        walkTree(node);
    }

    public void visit(ServletRequestCPUCCTNode node) {
        walkTree(node);
    }

    public void visit(SimpleCPUCCTNode node) {
        walkTree(node);
    }

    public void visit(CategoryCPUCCTNode node) {
        walkTree(node);
    }

    /**
     * Will initiate the tree walking process
     * starting in the rootNode
     * @param rootNode The node to start the tree walking process
     */
    public void walk(RuntimeCPUCCTNode rootNode) {
        rootNode.accept(this);
    }

    private void walkTree(RuntimeCPUCCTNode rootNode) {
        rootNode.getChildren().accept(this);
    }
}
