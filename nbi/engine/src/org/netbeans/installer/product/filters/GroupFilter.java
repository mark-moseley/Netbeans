/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.product.filters;

import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.RegistryNode;

/**
 *
 * @author Kirill Sorokin
 */
public class GroupFilter implements RegistryFilter {
    private String  uid;
    
    public GroupFilter() {
        // does nothing
    }
    
    public GroupFilter(final String uid) {
        this.uid = uid;
    }
    
    public boolean accept(final RegistryNode node) {
        if (node instanceof Group) {
            Group group = (Group) node;
            if (uid != null) {
                if (!group.getUid().equals(uid)) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
}
