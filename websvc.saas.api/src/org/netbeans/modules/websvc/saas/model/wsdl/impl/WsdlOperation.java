/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.model.wsdl.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlOperation implements WSOperation{
    private Operation operation;

    public WsdlOperation(Operation  operation){
        this.operation = operation;

    }
    public Object getInternalJAXWSOperation() {
        return operation;
    }

    public JavaMethod getJavaMethod() {
        return null;
    }

    public String getName() {
        return operation.getName();
    }

    public String getJavaName() {
        return null;
    }

    public String getReturnTypeName() {
        return Utils.getTypeName(operation.getOutput().getMessage().getQName() ); //TODO need to qualify this
    }

    private List<WSParameter> unWrapPart(Part part){
        List<WSParameter> parms = new  ArrayList<WSParameter>();
        NamedComponentReference<GlobalElement> gbr = part.getElement();
        if(gbr != null){
            GlobalElement gb = gbr.get();
            List<ComplexType> complexTypes = gb.getChildren(ComplexType.class);
            if(complexTypes != null && !complexTypes.isEmpty()){
                for(ComplexType complexType : complexTypes){
                    ComplexTypeDefinition def = complexType.getDefinition();
                    List<LocalElement> elements = def.getChildren(LocalElement.class);
                    for(LocalElement element : elements){
                        parms.add(new WsdlParameter(element));
                    }
                }
            }
        }
        return parms;
    }

    public List<WSParameter> getParameters() {
        List<WSParameter> parameters = new ArrayList<WSParameter>();
        Input input = operation.getInput();
        NamedComponentReference<Message> message = input.getMessage();
        Collection<Part> parts = message.get().getParts();
        for(Part part : parts){
            parameters.addAll(unWrapPart(part));
        }
        return parameters;
    }

    public Iterator<String> getExceptions() {
        List<String> exceptions = new ArrayList<String>();
        Collection<Fault> faults = operation.getFaults();
        for(Fault fault : faults){
            exceptions.add(fault.getMessage().getQName().getLocalPart());
        }
        return exceptions.iterator();
    }

    public int getOperationType() {
        return 0;
    }

    public String getOperationName() {
        return operation.getName();
    }

}
