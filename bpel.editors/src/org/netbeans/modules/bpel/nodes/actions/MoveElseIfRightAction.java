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
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.If;
import org.openide.util.NbBundle;

public class MoveElseIfRightAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;

    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_MoveElseIfRightAction"); // NOI18N
    }

    public ActionType getType() {
        return ActionType.MOVE_ELSE_IF_RIGHT;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
        
        ElseIf curElseIf = (ElseIf) bpelEntities[0];
        
        If parentIf = (If) curElseIf.getParent();
        
        ElseIf[] elseIfs = parentIf.getElseIfs();

        for (int i = 0; i < elseIfs.length - 1; i++) {
            if (elseIfs[i] == curElseIf) {
                ElseIf newElseIf = (ElseIf) curElseIf.cut();
                parentIf.insertElseIf(newElseIf, i + 1);
                return;
            }
        }
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        if (bpelEntities[0] instanceof ElseIf) {
            ElseIf curElseIf = (ElseIf) bpelEntities[0];
            If parentIf = (If) curElseIf.getParent();
            ElseIf[] elseIfs = parentIf.getElseIfs();
            
            return elseIfs[elseIfs.length - 1] != curElseIf;
        }
        return false;
    }
}
