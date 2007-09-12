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

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.PartnerLinkMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.DeletePLinkAction;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class PartnerLinkNode extends BpelNode<PartnerLink> {
    
    private String wsdlFile;
    
    public PartnerLinkNode(PartnerLink reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public PartnerLinkNode(PartnerLink reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PARTNER_LINK;
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
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Node.Property prop;
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        prop = PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                WSDL_FILE, "getWsdlFile", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_LINK_TYPE, PARTNER_LINK_TYPE,
                "getPartnerLinkType", "setPartnerLinkType", null); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.MY_ROLE, MY_ROLE,
                "getMyRole", "setMyRole", "removeMyRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        prop = PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                PartnerLink.PARTNER_ROLE, PARTNER_ROLE,
                "getPartnerRole", "setPartnerRole", "removePartnerRole"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        // prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        return sheet;
    }
    
    public String getWsdlFile() {
        if (wsdlFile == null) {
            wsdlFile = calculateWsdlUri();
        }
        return wsdlFile;
    }
    
    private String calculateWsdlUri() {
        try {
            PartnerLink pLink = getReference();
            if (pLink != null) {
                WSDLReference<PartnerLinkType> pltRef = pLink.getPartnerLinkType();
                if(pltRef != null){
                    PartnerLinkType plt = pltRef.get();
                    if (plt != null) {
                        Lookup modellookup = 
                                plt.getModel().getModelSource().getLookup();
                        FileObject modelFo = 
                                (FileObject) modellookup.lookup(FileObject.class);
                        String result = ResolverUtility.calculateRelativePathName(
                                modelFo, pLink.getBpelModel());
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return "";
    }
    
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new SimpleCustomEditor<PartnerLink>(
                this, PartnerLinkMainPanel.class, editingMode);
    }
    
//    protected String getImplShortDescription() {
//        PartnerLink pl = getReference();
//        if (pl == null) {
//            return super.getImplShortDescription();
//        }
//        
//        StringBuffer result = new StringBuffer();
//        WSDLReference myRoleRef = pl.getMyRole();
//        result.append(myRoleRef == null 
//                ? EMPTY_STRING 
//                : NbBundle.getMessage(
//                    BpelNode.class,
//                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                    PartnerLink.MY_ROLE, 
//                    myRoleRef.getRefString()
//                    )
//                ); 
//        
//        WSDLReference partnerRoleRef = pl.getPartnerRole();
//        result.append(partnerRoleRef == null 
//                ? EMPTY_STRING 
//                : NbBundle.getMessage(
//                    BpelNode.class,
//                    "LBL_ATTRIBUTE_HTML_TEMPLATE", // NOI18N
//                    PartnerLink.PARTNER_ROLE, 
//                    partnerRoleRef.getRefString()
//                    )
//                ); 
//
//        return NbBundle.getMessage(BpelNode.class,
//                "LBL_LONG_TOOLTIP_HTML_TEMPLATE", // NOI18N
//                getNodeType().getDisplayName(), 
//                getName(),
//                result.toString()
//                ); 
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.FIND_USAGES,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.OPEN_PL_IN_EDITOR,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    public Action createAction(ActionType actionType) {
        switch (actionType) {
            case REMOVE: 
                return SystemAction.get(DeletePLinkAction.class);
            default: 
                return super.createAction(actionType);
        }
    }
    
}
