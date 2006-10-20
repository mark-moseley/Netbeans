/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Viatly Bychkov
 */
public class RepeatUntilNode extends DiagramBpelNode<RepeatUntil, DiagramExtInfo> {
    
    public RepeatUntilNode(RepeatUntil reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public RepeatUntilNode(RepeatUntil reference, Lookup lookup) {
        super(reference, lookup);
    }

    public NodeType getNodeType() {
        return NodeType.REPEAT_UNTIL;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        DiagramExtInfo diagramReference = getDiagramReference();
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, PropertyType.NAME, "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet, 
                BooleanExpr.class, PropertyType.BOOLEAN_EXPRESSION, 
                "getCondition", "setCondition", null); // NOI18N
        //
        return sheet;
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.SHOW_BPEL_MAPPER,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }     
}
