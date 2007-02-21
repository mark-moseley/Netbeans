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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Pick;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class AddPickAction extends AddPaletteActivityAction {
    private static final long serialVersionUID = 1L;

    public AddPickAction() {
    }    
    
    protected String getBundleName() {
        return NodeType.PICK.getDisplayName(); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_PICK;
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        return super.enable(bpelEntities)
        && bpelEntities[0] instanceof BpelContainer;
    }

    protected ExtendableActivity getPaletteActivity(BpelModel model) {
        Pick pick = model.getBuilder().createPick();
        pick.addOnMessage(model.getBuilder().createOnMessage());
        
        return pick;
    }

}
