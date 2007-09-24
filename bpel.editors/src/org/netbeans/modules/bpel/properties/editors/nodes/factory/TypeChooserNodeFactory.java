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
package org.netbeans.modules.bpel.properties.editors.nodes.factory;

import org.netbeans.modules.soa.ui.axinodes.AxiomChildren;
import org.netbeans.modules.soa.ui.axinodes.AxiomTreeNodeFactory;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.nodes.children.AllImportsChildren;
import org.netbeans.modules.bpel.nodes.children.MessagePartsChildren;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.nodes.children.WsdlTypesChildren;
import org.netbeans.modules.bpel.nodes.children.PrimitiveTypeChildren;
import org.netbeans.modules.bpel.nodes.children.SchemaCategoriesChildren;
import org.netbeans.modules.bpel.nodes.children.SchemaImportsChildren;
import org.netbeans.modules.bpel.nodes.children.WsdlImportsChildren;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This factory create the nodes tree for any Type chooser.
 * It contains the following structure:
 *
 * @author nk160297
 */
public class TypeChooserNodeFactory implements NodeFactory<NodeType> {
    
    private NodeFactory myDelegate;
    private AxiomTreeNodeFactory axiomTreeNodeFactory;
    
    /** Creates a new instance of PropertyChooserNodeFactory */
    public TypeChooserNodeFactory(NodeFactory delegate) {
        myDelegate = delegate;
        //
        axiomTreeNodeFactory = new AxiomTreeNodeFactory();
    }
    
    public Node createNode(NodeType nodeType, Object ref, 
            Children children, Lookup lookup) {
        return myDelegate.createNode(nodeType, ref, children, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        //
        Children children = null;
        Node newNode = null;
        //
        NodeFactory nodeFactory = lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof TypeChooserNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case PROCESS:
                assert ref instanceof Process;
                //
                StereotypeFilter stFilter = lookup.lookup(StereotypeFilter.class);
                if (stFilter != null && stFilter.isSingleStereotype()) {
                    VariableStereotype vs = stFilter.getAllowedStereotypes()[0];
                    switch (vs) {
                        case GLOBAL_SIMPLE_TYPE:
                        case GLOBAL_COMPLEX_TYPE:
                        case GLOBAL_TYPE:
                        case GLOBAL_ELEMENT:
                            children = new SchemaImportsChildren(
                                    (Process)ref, lookup);
                            break;
                        case MESSAGE:
                            children = new WsdlImportsChildren(
                                    (Process)ref, lookup);
                            break;
                        case PRIMITIVE_TYPE:
                            children = new PrimitiveTypeChildren(lookup);
                            newNode = new CategoryFolderNode(NodeType.PRIMITIVE_TYPE,
                                        children, lookup);
                            return newNode;
                        default:
                            children = Children.LEAF;
                            break;
                    }
                } else {
                    children = new AllImportsChildren((Process)ref, lookup);
                }
                //
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case IMPORT:
            case IMPORT_SCHEMA:
            case IMPORT_WSDL:
                assert ref instanceof Import;
                Import importObj = (Import)ref;
                String importTypeName = importObj.getImportType();
                Constants.StandardImportType importType =
                        Constants.StandardImportType.forName(importTypeName);
                switch (importType) {
                    case IMPORT_WSDL:
                        children = new WsdlTypesChildren(importObj, lookup);
                        newNode = myDelegate.createNode(
                                nodeType, ref, children, lookup);
                        break;
                    case IMPORT_SCHEMA:
                        SchemaModel schemaModel =
                                ImportHelper.getSchemaModel(importObj, true);
                        if (schemaModel != null) {
                            children = new SchemaCategoriesChildren(
                                    schemaModel, lookup);
                            newNode = myDelegate.createNode(
                                    nodeType, ref, children, lookup);
                        } else {
                            newNode = myDelegate.createNode(
                                    nodeType, ref, lookup);
                        }
                        break;
                    case IMPORT_UNKNOWN:
                        // Create a node without childern
                        newNode = myDelegate.createNode(
                                nodeType, ref, lookup);
                        break;
                }
                return newNode;
            case WSDL_FILE:
                assert ref instanceof WSDLModel;
                WSDLModel wsdlModel = (WSDLModel)ref;
                //
                children = new WsdlTypesChildren(wsdlModel, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case SCHEMA_FILE:
                assert ref instanceof SchemaModel;
                SchemaModel schemaModel = (SchemaModel)ref;
                //
                children = new SchemaCategoriesChildren(schemaModel, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case EMBEDDED_SCHEMA:
                assert ref instanceof Schema;
                SchemaModel model = ((Schema)ref).getModel();
                children = new SchemaCategoriesChildren(model, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case MESSAGE_TYPE:
                assert ref instanceof Message;
                children = new MessagePartsChildren((Message)ref, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case MESSAGE_PART:
                assert ref instanceof Part;
                lookup = new ExtendedLookup(lookup, axiomTreeNodeFactory);
                SchemaComponent sComp = AxiomUtils.getPartType((Part)ref);
                children = new AxiomChildren(sComp, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case GLOBAL_COMPLEX_TYPE:
            case GLOBAL_ELEMENT:
                assert ref instanceof SchemaComponent;
                lookup = new ExtendedLookup(lookup, axiomTreeNodeFactory);
                children = new AxiomChildren((SchemaComponent)ref, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
//            case GLOBAL_SIMPLE_TYPE:
//                newNode = myDelegate.createNode(nodeType, ref, lookup);
//                return newNode;
            default:
                newNode = myDelegate.createNode(nodeType, ref, lookup);
                return newNode;
        }
    }
    
}
