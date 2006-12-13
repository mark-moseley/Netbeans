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
 */

package org.netbeans.modules.websvc.jaxrpc;

import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.websvc.jaxrpc.nodes.PrototypeWebServicesClientView;

/**
 *
 * @author Peter Williams
 */
public class PrototypeWebServicesClientViewProvider implements WebServicesClientViewProvider {

    private PrototypeWebServicesClientView viewGenerator = new PrototypeWebServicesClientView();
	
    /** Creates a new instance of PrototypeWebServicesClientViewProvider */
    public PrototypeWebServicesClientViewProvider() {
    }
	
    public WebServicesClientView findWebServicesClientView(FileObject file) {
        return WebServicesClientViewFactory.createWebServicesClientView(viewGenerator);
    }
}



