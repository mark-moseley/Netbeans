/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.product.filters;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.RegistryNode;

/**
 *
 * @author Kirill Sorokin
 */
public class SubTreeFilter implements RegistryFilter {
    private List<RegistryNode> leaves;
    
    public SubTreeFilter(List<? extends RegistryNode> nodes) {
        this.leaves = new LinkedList<RegistryNode>();
        this.leaves.addAll(nodes);
    }
    
    public boolean accept(final RegistryNode node) {
        if (leaves.contains(node)) {
            return true;
        }
        
        for (RegistryNode leaf: leaves) {
            if (node.isAncestor(leaf)) {
                return true;
            }
        }
        
        return false;
    }
}
