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

package org.netbeans.modules.xslt.mapper.model;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.AXIUtils;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.AttrValueTamplateHolder;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.NamespaceSpec;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslModel;
/**
 * Constructs missing XSLT components which are required to
 * the specified SchemaNode be presented in the XSLT document.
 *
 * The XSL model transaction is started and ended automatically if recessary.
 */

public class BranchConstructor {
    
    
    
    private boolean transactionStarted = false;
    private boolean exitTranactionOnFinish = true;
    private SchemaNode startFromNode;
    private XsltMapper mapper;
    private XslModel myModel;
    public BranchConstructor(SchemaNode node, XsltMapper mapper) {
        startFromNode = node;
        this.mapper = mapper;
        myModel = mapper.getContext().getXSLModel();
    }
    
    /**
     * Allows preventing end transaction on finish.
     * This method should be used carefully.
     */
    public void exitTranactionOnFinish(boolean newValue) {
        exitTranactionOnFinish = newValue;
    }
    
    public XslComponent construct() {
        XslComponent result = null;
       
        try {
            result = createXslComponent(startFromNode);
        } finally {
            if (exitTranactionOnFinish && myModel != null && transactionStarted) {
                myModel.endTransaction();
            }
        }
        return result;
    }
    
    private XslComponent createXslComponent(TreeNode node) {
        XslComponent result = null;
        //
        if (node instanceof StylesheetNode){
            result = (XslComponent) node.getDataObject();
        } else if (node instanceof SchemaNode){
            TreeNode parent = node.getParent();
            XslComponent parent_xsl = createXslComponent(parent);
            if (parent_xsl != null) {
                //
                // Start transaction if necessary
                if (!transactionStarted && myModel != null) {
                    if (!myModel.isIntransaction()) {
                        transactionStarted = myModel.startTransaction();
                    }
                }
                //
                result = createXslElementOrAttribute(parent_xsl, node.getType(), mapper);
            }
        }
        //
        return result;
    }
    public static XslComponent createXslElementOrAttribute(XslComponent parent, AXIComponent type, XsltMapper mapper){
        assert (parent instanceof SequenceConstructor);
        XslModel model = parent.getModel();
        XslComponent nameHolder = null;
        if (type instanceof AbstractAttribute) {
            nameHolder = model.getFactory().createAttribute();
        } else if (type instanceof AbstractElement){
            nameHolder = model.getFactory().createElement();
        } else {
            assert false : "Cant recognize element type for new XSL element";
        }
        //
        if (nameHolder != null){
            AttributeValueTemplate nameAVT;
            AttributeValueTemplate namespaceAVT = null;
            //
            String name = ((AXIType) type).getName();
            if (AxiomUtils.isUnqualified(type)) {
                nameAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate(name);
                namespaceAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate("");
            } else {
                String namespace = type.getTargetNamespace();
                QName elementQName = new QName(namespace, name);
                nameAVT = ((AttrValueTamplateHolder)nameHolder).
                        createTemplate(elementQName);
            }
            
            int index = calculateNodeIndex(parent, type, mapper);
            //
            if (model.isIntransaction()) {
                //
                ((AttrValueTamplateHolder)nameHolder).setName(nameAVT);
                if (namespaceAVT != null) {
                    ((NamespaceSpec)nameHolder).setNamespace(namespaceAVT);
                }
                //
                ((SequenceConstructor)parent).addSequenceChild(
                        (SequenceElement)nameHolder, index);
            } else {
                model.startTransaction();
                try {
                    //
                    ((AttrValueTamplateHolder)nameHolder).setName(nameAVT);
                    if (namespaceAVT != null) {
                        ((NamespaceSpec)nameHolder).setNamespace(namespaceAVT);
                    }
                    //
                    ((SequenceConstructor)parent).addSequenceChild(
                            (SequenceElement)nameHolder, index);
                } finally {
                    model.endTransaction();
                }
            }
        }
        return nameHolder;
    }
    
    
    
    private static  int calculateNodeIndex(XslComponent parent_xsl, AXIComponent type, XsltMapper mapper) {
        List<XslComponent> xsl_children = parent_xsl.getChildren();
        
        AXIComponent parent_type = AXIUtils.getType(parent_xsl, mapper);
        
        List<AXIComponent> axi_children = AXIUtils.getChildTypes(parent_type);
        
        int type_index = axi_children.indexOf(type);
        if (type_index == -1){
            return 0;
        }
        
        for (int n = 0; n < xsl_children.size(); n++){
            AXIComponent child_type = AXIUtils.getType(xsl_children.get(n), mapper);
            int comp_index = axi_children.indexOf(child_type);
            if (comp_index > type_index){
                return n;
            }
        }
        return xsl_children.size();
        
        
    }
}

