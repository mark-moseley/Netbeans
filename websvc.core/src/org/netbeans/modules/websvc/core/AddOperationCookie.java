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
package org.netbeans.modules.websvc.core;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/*
 * Provides a facility for obtaining the addOperation feature
 * for both JAX-WS and JAX-RPC web service.
 */
public interface AddOperationCookie extends Node.Cookie {
    
    /**
     * Adds a method definition to the the implementation class, possibly to SEI
     */
    public void addOperation(FileObject implementationClass);
    
    /**
     * Determines if the Add Operation pop up menu should be enabled
     * in the source editor.
     * @param implClass Implementation class that is shown in the editor
     */ 
    public boolean isEnabledInEditor(FileObject implClass);
    
}
