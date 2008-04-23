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
package org.netbeans.modules.websvc.axis2.config.model.impl;

import org.netbeans.modules.websvc.axis2.config.model.Axis2QNames;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Visitor;
import org.netbeans.modules.websvc.axis2.config.model.JavaGenerator;
import org.w3c.dom.Element;

public class JavaGeneratorImpl extends Axis2ComponentImpl implements JavaGenerator {
    
    private static String SERVICE_NAME_ATTR_PROP = Axis2Attributes.attrServiceName.getName();
    private static String PORT_NAME_ATTR_PROP = Axis2Attributes.attrPortName.getName();
    private static String DATABINDING_NAME_ATTR_PROP = Axis2Attributes.attrDatabindingName.getName();
    private static String PACKAGE_NAME_ATTR_PROP = Axis2Attributes.attrPackageName.getName();
    private static String SEI_ATTR_PROP = Axis2Attributes.attrSEI.getName();
    private static String OPTIONS_ATTR_PROP = Axis2Attributes.attrOptions.getName();
    
    public JavaGeneratorImpl(Axis2ModelImpl model, Element e) {
        super(model, e);
    }
    
    public JavaGeneratorImpl(Axis2ModelImpl model) {
        this(model, createElementNS(model, Axis2QNames.JAVA_GENERATOR));
    }
    
    public void accept(Axis2Visitor visitor) {
        visitor.visit(this);
    }

    public String getPackageNameAttr() {
        return getAttribute(Axis2Attributes.attrPackageName);
    }

    public void setPackageNameAttr(String packageName) {
        super.setAttribute(PACKAGE_NAME_ATTR_PROP, Axis2Attributes.attrPackageName, packageName);
    }

    public String getDatabindingNameAttr() {
        return getAttribute(Axis2Attributes.attrDatabindingName);
    }

    public void setDatabindingNameAttr(String databindingName) {
        super.setAttribute(DATABINDING_NAME_ATTR_PROP, Axis2Attributes.attrDatabindingName, databindingName);
    }
    
    public boolean isSEIAttr() {
        String sei = getAttribute(Axis2Attributes.attrName);
        return (sei != null && "true".equals(sei) ? true:false); // NOI18N
    }

    public void setSEIAttr(boolean sei) {
        if (sei) {
            super.setAttribute(SEI_ATTR_PROP, Axis2Attributes.attrSEI, "true"); // NOI18N
        } else {
            super.setAttribute(SEI_ATTR_PROP, Axis2Attributes.attrSEI, null);
        }
    }

    public String getServiceNameAttr() {
        return getAttribute(Axis2Attributes.attrServiceName);
    }

    public void setServiceNameAttr(String serviceName) {
        super.setAttribute(SERVICE_NAME_ATTR_PROP, Axis2Attributes.attrServiceName, serviceName);
    }
    
    public String getPortNameAttr() {
        return getAttribute(Axis2Attributes.attrPortName);
    }

    public void setPortNameAttr(String portName) {
        super.setAttribute(PORT_NAME_ATTR_PROP, Axis2Attributes.attrPortName, portName);
    }

    public String getOptionsAttr() {
        return getAttribute(Axis2Attributes.attrOptions);
    }

    public void setOptionsAttr(String options) {
        super.setAttribute(OPTIONS_ATTR_PROP, Axis2Attributes.attrOptions, options);
    }
    
}
