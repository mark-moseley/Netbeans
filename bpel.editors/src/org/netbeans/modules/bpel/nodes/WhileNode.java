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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class WhileNode extends BpelNode<While> {
    
    public WhileNode(While reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public WhileNode(While reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.WHILE;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet, 
                BooleanExpr.class, PropertyType.BOOLEAN_EXPRESSION, 
                "getCondition", "setCondition", null); // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.ADD_FROM_PALETTE,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
//            ActionType.SEPARATOR,
//            ActionType.SHOW_BPEL_MAPPER,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }     
}
