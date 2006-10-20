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
package org.netbeans.modules.bpel.nodes.actions;

import java.awt.Dialog;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.ImportWsdlNode;
import org.netbeans.modules.bpel.nodes.PropertyAliasNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.PropertyAliasMainPanel2;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.editors.controls.valid.BpelDialogDisplayer;
import org.netbeans.modules.bpel.properties.editors.controls.valid.NodeEditorDescriptor;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class AddPropertyAliasToWsdlAction  extends BpelNodeAction {
    
    protected String getBundleName() {
        return NbBundle.getMessage(AddPropertyAliasToWsdlAction.class,
                "CTL_AddPropertyAliasToWsdlAction"); // NOI18N
    }
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        Import imprt = ((ImportWsdlNode)nodes[0]).getReference();
        if (imprt == null) {
            return;
        }
        Lookup lookup = nodes[0].getLookup();
        
        FileObject fo = ResolverUtility.getImportedFile(imprt.getLocation(),lookup);
//System.out.println("try to invoke Add property Alias dialog");

        
        // get wsdl model
        final WSDLModel wsdlModel = ResolverUtility.getImportedWsdlModel(
                imprt.getLocation(), lookup);
        if (wsdlModel == null) {
            return;
        }
        // create Correlation property alias
        final PropertyAlias alias = (PropertyAlias)wsdlModel.
                getFactory().create(
                wsdlModel.getDefinitions(),
                BPELQName.PROPERTY_ALIAS.getQName());
        // create correlation property node
        PropertyAliasNode propAliasNode = new PropertyAliasNode(alias, lookup);
        
        //
        final String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "DLG_AddPropertyAlias");
        
        SimpleCustomEditor customEditor = new SimpleCustomEditor<PropertyAlias>(
                propAliasNode, PropertyAliasMainPanel2.class, 
                EditingMode.CREATE_NEW_INSTANCE);
        //
        NodeEditorDescriptor descriptor =
                new NodeEditorDescriptor(customEditor, dialogTitle);
        descriptor.setOkButtonProcessor(new Callable<Boolean>() {
            public Boolean call() throws Exception {
//System.out.println("dialog "+dialogTitle+" ok button pressed");
                wsdlModel.addChildComponent(wsdlModel.getRootComponent(),alias,0);
                return Boolean.TRUE;
            }
        });
        descriptor.setHelpCtx(null);
        //
        Dialog dialog = BpelDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }
    
    public boolean enable(Node[] nodes) {
        boolean result = nodes != null
                && nodes.length == 1
                && nodes[0] instanceof ImportWsdlNode;
        //
        if (result) {
            Import imprt = ((ImportWsdlNode)nodes[0]).getReference();
            if (imprt != null) {
                FileObject fo = ResolverUtility.getImportedFile(imprt.getLocation(),nodes[0].getLookup());
                if (fo != null && fo.isValid()) {
                    return true;
                }
            }
        }
        //
        return false;
    }
    
    public ActionType getType() {
        return ActionType.ADD_PROPERTY_ALIAS_TO_WSDL;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}
