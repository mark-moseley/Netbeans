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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sun.manager.jbi.actions;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Enhanced refresh action that can be applied to multiple nodes.
 * 
 * @author jqian
 */
public class RefreshAction extends org.netbeans.modules.j2ee.sun.bridge.apis.RefreshAction {
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null) {
            return;
        }
        
        try {
            for (Node node : activatedNodes) {
                Lookup lookup = node.getLookup();
                RefreshCookie refreshCookie = lookup.lookup(RefreshCookie.class);
                if (refreshCookie != null) {
                    refreshCookie.refresh();
                }
            }
        } catch(RuntimeException rex) {
            //gobble up exception
        }
    }
}
