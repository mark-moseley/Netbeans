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


package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 5 April 2006 
 *
 */
public class PickChildren extends BpelNodeChildren<Pick> {
    
    public PickChildren(Pick pick, Lookup contextLookup) {
        super(pick, contextLookup);
    }

    public Collection getNodeKeys() {
        Pick ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        ArrayList<BpelEntity> childs = new ArrayList<BpelEntity>();

        // set onMessage nodes
        OnMessage[] onMessages = ref.getOnMessages();
        if (onMessages != null && onMessages.length > 0 ) {
            childs.addAll(Arrays.asList(onMessages));
        }

        // set onAlarm nodes
        OnAlarmPick[] onAlarms = ref.getOnAlarms();
        if (onAlarms != null && onAlarms.length > 0 ) {
            childs.addAll(Arrays.asList(onAlarms));
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
        if (object instanceof BpelEntity) {
            childNode = factory.createNode((BpelEntity)object,getLookup());
        } 
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
}
