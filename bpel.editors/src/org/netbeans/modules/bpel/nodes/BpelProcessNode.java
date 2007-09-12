/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.SequenceEntityPasteType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class BpelProcessNode extends BaseScopeNode<Process> {
    
    public BpelProcessNode(Process reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public BpelProcessNode(Process reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PROCESS;
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
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                Process.TARGET_NAMESPACE, TARGET_NAMESPACE, 
                "getTargetNamespace", "setTargetNamespace", null); // NOI18N

//        PropertyUtils.registerAttributeProperty(this, expertPropertySet,
//                Process.QUERY_LANGUAGE, QUERY_LANGUAGE, 
//                "getQueryLanguage", "setQueryLanguage", null); // NOI18N

//        PropertyUtils.registerAttributeProperty(this, expertPropertySet,
//                ExpressionLanguageSpec.EXPRESSION_LANGUAGE, EXPRESSION_LANGUAGE, 
//                "getExpressionLanguage", "setExpressionLanguage", null); // NOI18N

//        PropertyUtils.registerAttributeProperty(expertPropertySet,
//                JoinFailureSuppressor.SUPPRESS_JOIN_FAILURE, 
//                SUPPRESS_JOIN_FAILURE, modelReference,
//                "getSuppressJoinFailure", "setSuppressJoinFailure",  // NOI18N
//                "removeSuppressJoinFailure"); // NOI18N

//        PropertyUtils.registerAttributeProperty(this, expertPropertySet,
//                Process.ABSTRACT_PROCESS, ABSTRACT_PROCESS, 
//                "getAbstractProcess", "setAbstractProcess",  // NOI18N
//                "removeAbstractProcess"); // NOI18N

//        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
//                BaseScope.EXIT_ON_STANDART_FAULT, EXIT_ON_STANDART_FAULT, 
//                "getExitOnStandardFault", "setExitOnStandardFault",  // NOI18N
//                "removeExitOnStandardFault"); // NOI18N
        //
        return sheet;
    }
    
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        if (!isDropNodeSupported(childNode)) {
            return false;
        }
        
        if (childNode instanceof PartnerLink
            && getReference() != null 
            && getReference().getPartnerLinkContainer() != null) 
        {
            return true;
        }
        
        if (childNode instanceof Activity
            && getReference() != null 
            && getReference().getActivity() == null) 
        {
            return true;
        }
        
        
        return false;
    }
    
    public BpelEntityPasteType createSupportedPasteType(BpelNode childNode) {
        if (!isDropNodeInstanceSupported(childNode)) {
            return null;
        }
        if (childNode instanceof Activity) {
            return new SequenceEntityPasteType(getReference(),(BpelEntity)childNode.getReference());
        }
        
        return null;
    }
    
    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
                ActionType.GO_TO_SOURCE,
                ActionType.GO_TO_DIAGRAMM,
                ActionType.SEPARATOR,
                ActionType.PROPERTIES
            };
        } 
        
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_VARIABLE,
            ActionType.ADD_CORRELATION_SET,
            // ActionType.ADD_MESSAGE_EXCHANGE, // Issue 85553
            ActionType.ADD_PARTNER_LINK,
            ActionType.ADD_EVENT_HANDLERS,
            ActionType.ADD_FAULT_HANDLERS,
            ActionType.ADD_WSDL_IMPORT,
            ActionType.ADD_SCHEMA_IMPORT
        };
    }

    protected String getImplHtmlDisplayName() {
        String name = getName();
        if (name == null) {
            return getNodeType().getDisplayName();
        }
//        name = name.replaceAll("&","&amp;"); // NOI18N
        return name;
    }
}
