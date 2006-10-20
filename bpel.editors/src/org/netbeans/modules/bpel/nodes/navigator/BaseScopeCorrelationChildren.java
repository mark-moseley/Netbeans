/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.nodes.children.BpelNodeChildren;
import org.netbeans.modules.bpel.nodes.children.ChildrenType;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 10 April 2006 
 *
 */
public class BaseScopeCorrelationChildren extends BpelNodeChildren<BaseScope> {
    
    public BaseScopeCorrelationChildren(BaseScope entity, Lookup contextLookup) {
        super(entity, contextLookup);
    }

    public Collection getNodeKeys() {
        BaseScope ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        
        
        //set Correlation nodes
        CorrelationSetContainer corrSetContainer = ref.getCorrelationSetContainer();
        if (corrSetContainer != null) {
            CorrelationSet[] corrSets = corrSetContainer.getCorrelationSets();
            if (corrSets != null && corrSets.length > 0) {
                childs.addAll(Arrays.asList(corrSets));
            }
        }
        
        // Set BaseScope Nodes
        List<BaseScope> scopes = Util.getClosestBaseScopes(ref.getChildren());
        if (scopes != null && scopes.size() > 0) {
            childs.addAll(scopes);
        }
        
        return childs;
    }

    protected Node[] createNodes(Object object) {
        if (object == null) {
            return new Node[0];
        }
        NavigatorNodeFactory factory 
                = NavigatorNodeFactory.getInstance();
        Node childNode = null;
        
        // create variable container node
        if (object instanceof CorrelationSet) 
        {
            childNode = factory.createNode(
                    NodeType.CORRELATION_SET
                    ,(CorrelationSet)object
                    ,getLookup());
        } else if (object instanceof BaseScope) 
        { // create correlation set container
            childNode = factory.createNode(
                    NodeType.SCOPE
                    ,(BaseScope)object
                    , null
                    , ChildrenType.SCOPE_CORRELATIONS_CHILD
                    ,getLookup());
        } 
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
}
