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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.openide.util.NbBundle;

public class AddCatchAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;

    public AddCatchAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddCatchAction")); // NOI18N
    }    

    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddCatchAction"); // NOI18N
    }


    public ActionType getType() {
        return ActionType.ADD_CATCH;
    }
    
    
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelEntity bpelEntity = bpelEntities[0];
        
        Catch newCatch = bpelEntity.getBpelModel().getBuilder().createCatch();
        
        if (bpelEntity instanceof Invoke) {
            Invoke invoke = (Invoke) bpelEntity;
            invoke.addCatch(newCatch);
        } else if (bpelEntity instanceof FaultHandlers) {
            ((FaultHandlers) bpelEntity).addCatch(newCatch);
        }
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
                
        return (bpelEntity instanceof Invoke) 
                || (bpelEntity instanceof FaultHandlers);
    }
}
