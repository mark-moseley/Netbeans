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
package org.netbeans.modules.websvc.saas.codegen.php;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
public class SoapClientPhpCodeGenerator extends SaasClientCodeGenerator {

    @Override
    public boolean canAccept(SaasMethod method, Document doc) {
        if (method instanceof WsdlSaasMethod && Util.isPhp(doc)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<FileObject> generate() throws IOException {
        try {
            insert(getCustomMethodBody(), true);

        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
        return super.generate();
    }

    @Override
    public void init(SaasMethod method, Document doc) throws IOException {
        super.init(method, doc);
        setBean(new SoapClientSaasBean((WsdlSaasMethod) method, getProject()));

    }

    @Override
    public SoapClientSaasBean getBean() {
        return (SoapClientSaasBean) super.getBean();
    }

   
    private String genPhpParms(SoapClientSaasBean bean) {
        StringBuffer params = new StringBuffer("");
        List<ParameterInfo> parameters = bean.getInputParameters();
        for (ParameterInfo parameter : parameters) {
            String parmName = parameter.getName();
            String parmTypeName = parameter.getTypeName();
            if (!parameter.getType().isPrimitive() && !parmTypeName.equals("java.lang.String")) {
                params.append("'" + parmName + "'" + "=> NULL, \n");
            } else {
                String def = (String) parameter.getDefaultValue();
                if (parmTypeName.equals("java.lang.String") ||
                        parmTypeName.equals("String")) {
                    if (def != null) {
                        params.append("'" + parmName + "'" + "=> \"" + def + "\", \n");
                    } else {
                        params.append("'" + parmName + "'" + "=> \"\",\n");
                    }

                } else {
                    if (def != null) {
                        params.append("'" + parmName + "'" + "=>" + def + ", \n");
                    } else {
                        params.append("'" + parmName + "'" + "=> 0,\n");
                    }
                }
            }
        }
        return params.toString();
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String wsdlUrl = "";
        String methodName = "";
        SoapClientSaasBean bean = this.getBean();
        SoapClientOperationInfo[] infos = bean.getOperationInfos();
        if (infos.length > 0) {
            wsdlUrl = infos[0].getWsdlURL();
            methodName = infos[0].getOperationName();
        }
        String paramDecl = "$params = array( " + "\n" + genPhpParms(bean) + ");";

        String methodBody = "\n<?php\n";
        methodBody += "$wsdl_url = '" + wsdlUrl + "';\n";
        methodBody += "$client     = new SOAPClient($wsdl_url);\n";
        methodBody += paramDecl + "\n";
        methodBody += "$return = $client->" + methodName + "($params);\n";
        methodBody += "print_r($return);";
        methodBody += "\n?>\n";
        return methodBody;
    }

    @Override
    protected void createRestConnectionFile(Project project) throws IOException {
    }
}
