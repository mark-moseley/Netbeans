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
import java.util.List;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.properties.BpelStandardFaults;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of the BPEL Standard Faults. 
 * This list is immutable so it doesn't matter to do it reloadable. 
 *
 * @author nk160297
 */
public class FaultNameStandardChildren extends Children.Array {
    
    public FaultNameStandardChildren(NodeFactory factory, Lookup lookup) {
        super();
        //
        BpelStandardFaults[] faultsArr = BpelStandardFaults.values();
        List<Node> nodesList = new ArrayList<Node>(faultsArr.length);
        for (BpelStandardFaults fault : faultsArr) {
            Node newNode = factory.createNode(
                    NodeType.FAULT, fault.getQName(), null, lookup);
            nodesList.add(newNode);
        }
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        add(nodesArr);
    }
    
}
