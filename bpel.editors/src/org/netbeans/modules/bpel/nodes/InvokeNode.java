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

import java.awt.Component;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.InvokeCustomEditor;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class InvokeNode extends DiagramBpelNode<Invoke, DiagramExtInfo> {
    
    public InvokeNode(Invoke reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public InvokeNode(Invoke reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.INVOKE;
    }
    
    @Override
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        if (super.isEventRequreUpdate(event)) {
            return true;
        }
        
        //PatternedCorrelationContainer
        BpelEntity entity = event.getParent();
        if (entity == null) {
            return false;
        }
        Invoke ref = getReference();
        return  ref != null && ref == entity.getParent() 
            && entity.getElementType() == PatternedCorrelationContainer.class;
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
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        Sheet.Set messagePropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MESSAGE_SET);
        //
        Node.Property property;
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                messagePropertySet,
                PartnerLinkReference.PARTNER_LINK, PARTNER_LINK, 
                "getPartnerLink", "setPartnerLink", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                messagePropertySet,
                PortTypeReference.PORT_TYPE, PORT_TYPE, 
                "getPortType", "setPortType", "removePortType"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                messagePropertySet,
                OperationReference.OPERATION, OPERATION, 
                "getOperation", "setOperation", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                messagePropertySet,
                Invoke.INPUT_VARIABLE, INPUT, 
                "getInputVariable", "setInputVariable", "removeInputVariable"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                messagePropertySet,
                Invoke.OUTPUT_VARIABLE, OUTPUT, 
                "getOutputVariable", "setOutputVariable", "removeOutputVariable"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        return sheet;
    }
    
    public Component getCustomizer() {
        return new InvokeCustomEditor(this, EditingMode.EDIT_INSTANCE);
    }

    protected String getImplHtmlDisplayName() {
        Invoke invoke = getReference();
        if (getReference() == null) {
            return super.getImplHtmlDisplayName();
        }
        StringBuffer result = new StringBuffer();

        return Util.getGrayString(super.getImplHtmlDisplayName(), result.toString());
    }

//    protected String getImplShortDescription() {
//        Invoke invoke = getReference();
//        if (invoke == null) {
//            return super.getImplShortDescription();
//        }
//        StringBuffer result = new StringBuffer();
//        result.append(getName());
//        result.append(invoke.getOutputVariable() == null ? 
//            "" : " out="+invoke.getOutputVariable().getRefString()); // NOI18N
//        result.append(invoke.getInputVariable() == null ? 
//            "" : " in="+invoke.getInputVariable().getRefString()); // NOI18N
//        result.append(invoke.getPartnerLink() == null ? 
//            "" : " partnerLink="+invoke.getPartnerLink().getRefString()); // NOI18N
//        result.append(invoke.getOperation() == null ? 
//            "" : " operation="+invoke.getOperation().getRefString()); // NOI18N
//
//        return NbBundle.getMessage(InvokeNode.class,
//            "LBL_INVOKE_NODE_TOOLTIP", // NOI18N
//            result.toString()
//            );
//    }

    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
//            ActionType.ADD_NEWTYPES,  // issue 79780, 79777
//            ActionType.SEPARATOR,  // issue 79780, 79777
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
//            ActionType.SEPARATOR,
//            ActionType.ADD_CATCH,
//            ActionType.ADD_CATCH_ALL,
//            ActionType.ADD_COMPENSATION_HANDLER,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }    
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
//            ActionType.ADD_CATCH, // issue 79780
//            ActionType.ADD_CATCH_ALL,  // issue 79780
//            ActionType.ADD_COMPENSATION_HANDLER // issue 79777
        };
    }
}
