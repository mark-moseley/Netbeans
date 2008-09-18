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

/*
 * Created on May 24, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.model.StringAttribute;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BindingOperationInputNode extends WSDLExtensibilityElementNode<BindingInput> {
    
    private static Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/bindinginput.png");
    
    public BindingOperationInputNode(BindingInput wsdlConstruct) {
        super(wsdlConstruct, new BindingOperationInputNewTypesFactory());
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT;
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    protected void updateDisplayName() {
        super.updateDisplayName();
        // Need a component connected to a model to work properly.
        if (isValid()) {
            // Automatically keep the name in sync for named components.
            BindingInput param = getWSDLComponent();
            String name = param.getAttribute(new StringAttribute(Named.NAME_PROPERTY));
            if (name == null || name.length() == 0) {
                Reference<Input> ref = param.getInput();
                if (ref != null && ref.get() != null) {
                    name = ref.get().getName();
                }
            }
            setDisplayName(name);
        }
    }
    
    @Override
    protected Component getSuperDefinition() {
        Reference<Input> ref = getWSDLComponent().getInput();
        if (ref != null) {
            return ref.get();
        }
        return null;
    }
    
    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(BindingInput.NAME_PROPERTY)) { 
                //name
                attrValueProperty = createNameProperty();
                
                
            }  else {
                attrValueProperty = super.createAttributeProperty(attrQName);
            }
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        return attrValueProperty;
    }
    
    
    @Override
    protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
        ArrayList<Node.Property> alwaysPresentAttrProperties = new ArrayList<Node.Property>();
        alwaysPresentAttrProperties.add(createNameProperty());
        
        return alwaysPresentAttrProperties;
    }
    
    private Node.Property createNameProperty() {
        Node.Property attrValueProperty;
        attrValueProperty = new BindingInputNameProperty(BindingInput.NAME_PROPERTY, 
                NbBundle.getMessage(BindingOperationInputNode.class, "PROP_NAME_DISPLAYNAME"), 
                NbBundle.getMessage(BindingOperationInputNode.class, "BINDINGOPERATIONINPUT_NAME_DESCRIPTION"));

        return attrValueProperty;
    }


    public class BindingInputNameProperty extends PropertySupport.ReadOnly<String> {

        public BindingInputNameProperty(String name, String displayName, String shortDesc) {
            super(name, String.class, displayName, shortDesc);
        }

        @Override
        public String getValue() {
            String name = getWSDLComponent().getName();
            return name == null ? "" : name;
        }
    }
    
    public static final class BindingOperationInputNewTypesFactory implements NewTypesFactory{
        
        public NewType[] getNewTypes(WSDLComponent def) {
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(BindingOperationInputNode.class, "LBL_BindingInputNode_TypeDisplayName");
    }
}
