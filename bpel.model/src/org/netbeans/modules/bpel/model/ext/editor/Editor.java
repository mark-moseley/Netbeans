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
package org.netbeans.modules.bpel.model.ext.editor;

import org.netbeans.modules.bpel.model.api.*;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface Editor extends ExtensionEntity, BpelContainer {
    String EDITOR_NAMESPACE_URI =
            "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/Editor";

    Casts getCasts();

    void removeCasts( );

    void setCasts( Casts casts);

}
