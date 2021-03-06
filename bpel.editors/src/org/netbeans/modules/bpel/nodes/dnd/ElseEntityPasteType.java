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
package org.netbeans.modules.bpel.nodes.dnd;

import java.awt.dnd.DnDConstants;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.If;

/**
 *
 * @author Vitaly Bychkov
 * @version 24 March 2006
 */
public class ElseEntityPasteType extends BpelEntityPasteType<If, Else> {
    public ElseEntityPasteType(If parent,
        Else transferedEntity) {
        super(parent,transferedEntity);
    }

    public int[] getSupportedDnDOperations() {
        return new int[] {
            DnDConstants.ACTION_MOVE
        };
    }
    
    protected void moveEntity() {
            Else cuttedEntity = (Else) getTransferedEntity().cut();
            getParentEntity().setElse(cuttedEntity);
    }
}
