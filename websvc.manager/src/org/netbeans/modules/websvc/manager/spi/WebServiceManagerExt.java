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

package org.netbeans.modules.websvc.manager.spi;

import javax.swing.Action;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.*;

public interface WebServiceManagerExt {
    
    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc);
    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc);
    
    /**
     * @return list of consumer-specific actions for webservice root node.
     */
    public Action[] getWebServicesActions();

    /**
     * @return list of consumer-specific actions for group nodes.
     */
    public Action[] getGroupActions();

    /**
     * @return list of consumer-specific actions for port nodes.
     */
    public Action[] getPortActions();

    /**
     * @return list of consumer-specific actions for method nodes.
     */
    public Action[] getMethodActions();
}
