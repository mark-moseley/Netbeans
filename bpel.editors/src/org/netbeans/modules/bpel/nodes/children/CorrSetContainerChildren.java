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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of CorrelationSet and nested Scopes of a Process or a Scope
 * It's used by the CorrelationSet Chooser.
 *
 * @author nk160297
 */
public class CorrSetContainerChildren extends BaseScopeChildren {
    
    public CorrSetContainerChildren(BaseScope bScope, Lookup lookup) {
        super(bScope, lookup);
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof BaseScope)) {
            return null;
        }
        BaseScope bScope = (BaseScope)key;
        //
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        //
        List<Node> nodesList = new ArrayList<Node>();
        //
        addScopeNodes(bScope, nodesList);
        //
        // Add Variables
        CorrelationSetContainer corrSetContainer = bScope.getCorrelationSetContainer();
        if (corrSetContainer != null) {
            CorrelationSet[] corrSetArr = corrSetContainer.getCorrelationSets();
            for (CorrelationSet corrSet : corrSetArr) {
                Node newNode = nodeFactory.createNode(
                        NodeType.CORRELATION_SET, corrSet, myLookup);
                nodesList.add(newNode);
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
}
