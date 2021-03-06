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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeOrMessagePartProvider.ParameterType;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

public class ElementOrTypeOrMessagePart {
    private GlobalElement mElement;
    private GlobalType mType;
    private Part mPart;
    private Message mMessage;
    private QName mQName;
    private ParameterType pType = ParameterType.NONE;
    private WSDLModel mModel;
    private String mPartName;

    public ElementOrTypeOrMessagePart(QName elementOrTypeQName, WSDLModel model, ParameterType elementOrType) {
        this.pType = elementOrType;
        mQName = elementOrTypeQName;
        mModel = model;
        if (pType == ParameterType.ELEMENT) {
            mElement = Utility.findGlobalElement(model, elementOrTypeQName);
        } else if (pType == ParameterType.TYPE){
            mType = Utility.findGlobalType(model, elementOrTypeQName);
        }
    }
    
    public Part getMessagePart() {
        return mPart;
    }

    public ParameterType getParameterType() {
        return pType;
    }

    public ElementOrTypeOrMessagePart(QName messageQName, WSDLModel model, String partName) {
        pType = ParameterType.MESSAGEPART;
        mModel = model;
        mQName = messageQName;
        mPartName = partName;
        mMessage = mModel.findComponentByName(messageQName, Message.class);
        if (mMessage != null) {
            for (Part part : mMessage.getParts()) {
                if (part.getName().equals(partName)) {
                    mPart = part;
                    break;
                }
            }
        }
    }

    public ElementOrTypeOrMessagePart(GlobalElement element, WSDLModel model) {
        mElement = element;
        mModel = model;
        pType = ParameterType.ELEMENT;
    }

    public ElementOrTypeOrMessagePart(GlobalType type, WSDLModel model) {
        mType= type;
        mModel = model;
        pType = ParameterType.TYPE;
    }
    
    public ElementOrTypeOrMessagePart(Part part, WSDLModel model) {
        mPart= part;
        mMessage = (Message) part.getParent();
        mModel = model;
        pType = ParameterType.MESSAGEPART;
    }
    
    public GlobalElement getElement() {
        return mElement;
    }
    
    public GlobalType getType() {
        return mType;
    }
    
    @Override
    public String toString() {
        if (mQName != null) {
            String str = Utility.fromQNameToString(mQName);
            if (pType == ParameterType.MESSAGEPART) {
                str = str + "/" + mPartName;
            }
            return str;
        }
        
        String namespace = null;
        String localPart = "";
        if (mElement != null) {
            namespace = mElement.getModel().getSchema().getTargetNamespace();
            localPart = mElement.getName();
        }
        if (mType != null) {
            namespace = mType.getModel().getSchema().getTargetNamespace();
            localPart = mType.getName();
        }
        
        if (mMessage != null) {
            namespace = mMessage.getModel().getDefinitions().getTargetNamespace();
            localPart = mMessage.getName();
        }
        
        if (namespace == null) {
            return localPart;
        }
        if (mModel == null) {
            return new QName(namespace, localPart).toString();
        }
        String namespacePrefix = Utility.getNamespacePrefix(namespace, mModel);
        if (namespacePrefix == null) {
            namespacePrefix = NameGenerator.getInstance().generateNamespacePrefix(null, mModel);
            boolean isInTransaction = Utility.startTransaction(mModel);
            ((AbstractDocumentComponent)mModel.getDefinitions()).addPrefix(namespacePrefix, namespace);
            
            Utility.endTransaction(mModel, isInTransaction);
        }
        if (pType == ParameterType.MESSAGEPART) {
            return namespacePrefix + ":" + localPart + "/" + mPart.getName();
        }
        return namespacePrefix + ":" + localPart;
    }
}
