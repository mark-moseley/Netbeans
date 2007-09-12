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
package org.netbeans.modules.bpel.nodes;

import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.AddCorrelationSetAddAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class CorrelationSetContainerNode
        extends ContainerBpelNode<BaseScope, CorrelationSetContainer> {
    
    public CorrelationSetContainerNode(CorrelationSetContainer corrSetContainer, Children children, Lookup lookup) {
        super((BaseScope)corrSetContainer.getParent(), children, lookup);
    }

    public CorrelationSetContainerNode(CorrelationSetContainer corrSetContainer, Lookup lookup) {
        super((BaseScope)corrSetContainer.getParent(), lookup);
    }

    public CorrelationSetContainerNode(BaseScope baseScope, Children children, Lookup lookup) {
        super(baseScope, children, lookup);
    }

    public CorrelationSetContainerNode(BaseScope baseScope, Lookup lookup) {
        super(baseScope, new MyChildren(baseScope, lookup), lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_SET_CONTAINER;
    }
    
    public void reload() {
        MyChildren children = (MyChildren)getChildren();
        children.reloadFrom(getReference());
    }

    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    public boolean isContainerNode() {
        return true;
    }

    public CorrelationSetContainer getContainerReference() {
        BaseScope baseScope = getReference();
        if (baseScope == null) {
            return null;
        } else {
            return baseScope.getCorrelationSetContainer();
        }
    }

    private static class MyChildren extends Children.Keys {
        
        private Lookup lookup;
        
        public MyChildren(BaseScope baseScope, Lookup lookup) {
            super();
            this.lookup = lookup;
            reloadFrom(baseScope);
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof CorrelationSet;
            //
            CorrelationSet cSet = (CorrelationSet)key;
            Children children = new CorrelationSetNode.MyChildren(cSet, lookup);
            Node node = new CorrelationSetNode(cSet, children, lookup);
            return new Node[] {node};
        }
        
        public void reloadFrom(BaseScope baseScope)  {
            CorrelationSetContainer correlationSetContainer = 
                    baseScope.getCorrelationSetContainer();
            if (correlationSetContainer != null) {
                setKeys(correlationSetContainer.getCorrelationSets());
            } else {
                setKeys(new CorrelationSetContainer[0]);
            }
        }
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_CORRELATION_SET,
            ActionType.SEPARATOR,
            ActionType.GO_TO_CORRSETCONTAINER_SOURCE
        };
    }
    
    public Action createAction(ActionType actionType) {
        if (ActionType.ADD_CORRELATION_SET.equals(actionType)) {
            return SystemAction.get(AddCorrelationSetAddAction.class);
        }
        return super.createAction(actionType);
    }
    
}
