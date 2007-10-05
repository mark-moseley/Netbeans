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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public abstract class OperationImpl extends NamedImpl implements Operation {
    
    /** Creates a new instance of OperationImpl */
    public OperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setInput(Input input) {
        throw new UnsupportedOperationException(
                "This operation does not support this message exchange pattern"); //NOI18N
    }
  
    public Input getInput() {
        return null;
    }
    
    public void setOutput(Output output) {
        throw new UnsupportedOperationException(
                "This operation does not support this message exchange pattern");//NOI18N
    }

    public Output getOutput() {
        return null;
    }
    
    public Collection<Fault> getFaults() {
        return getChildren(Fault.class);
    }

    public void addFault(Fault fault) {
        appendChild(Operation.FAULT_PROPERTY, fault);
    }

    public void removeFault(Fault fault) {
        removeChild(Operation.FAULT_PROPERTY, fault);
    }

    public List<String> getParameterOrder() {
        String s = getAttribute(WSDLAttribute.PARAMETER_ORDER);
        return Util.parse(s);
    }

    public void setParameterOrder(List<String> parameterOrder) {
        setAttribute(PARAMETER_ORDER_PROPERTY, WSDLAttribute.PARAMETER_ORDER, 
                Util.toString(parameterOrder));
    }
    
    protected Object getAttributeValueOf(WSDLAttribute attr, String s) {
        if (attr == WSDLAttribute.PARAMETER_ORDER) {
            return Util.parse(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }

}
