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

import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationNode extends BpelNode<Correlation> {
    
    public CorrelationNode(Correlation correlation, Children children, Lookup lookup) {
        super(correlation, children, lookup);
    }

    public CorrelationNode(Correlation correlation, Lookup lookup) {
        super(correlation, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION;
    }

    protected  String getNameImpl() {
        CorrelationSet corrSet = null;
        Correlation ref = getReference();
        if (ref != null) {
            BpelReference<CorrelationSet> corrSetRef = ref.getSet();
            if (corrSetRef != null) {
                corrSet = corrSetRef.get();
            }
        }
        return corrSet == null ? "" : corrSet.getName();
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.SET, CORRELATION_SET, 
                "getSet", null, null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                BaseCorrelation.INITIATE, CORRELATION_INITIATE, 
                "getInitiate", "setInitiate", "removeInitiate"); // NOI18N
        //
        return sheet;
    }

    protected String getImplHtmlDisplayName() {
        String nodeName = null;
        Correlation ref = getReference();
        if (ref != null) {
            Initiate initiate = getReference().getInitiate();
            nodeName = initiate == null || initiate.equals(Initiate.INVALID)
                ? "" 
                : " initiate="+initiate.toString(); // NOI18N
        }
        
        return SoaUiUtil.getGrayString(getName(), nodeName == null ? "" : nodeName);
    }
    
//    protected String getImplShortDescription() {
//        return NbBundle.getMessage(CorrelationNode.class,
//            "LBL_CORRELATION_SET_NODE_TOOLTIP", // NOI18N
//            getName()
//            );
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
