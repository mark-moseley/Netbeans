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
package org.netbeans.modules.websvc.saas.codegen.java;

import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
public class JaxRsServletCodeGenerator extends JaxRsJavaClientCodeGenerator {

    private JavaSource loginJS;
    private FileObject loginFile;
    private JavaSource callbackJS;
    private FileObject callbackFile;
    
    public JaxRsServletCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
    }
    
    /**
     *  Create Authorization Frame
     */
    @Override
    public void createAuthorizationClasses() throws IOException {
        List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
        final String[] parameters = getGetParamNames(filterParams);
        final Object[] paramTypes = getGetParamTypes(filterParams);
        Util.createSessionKeyAuthorizationClassesForWeb(
            getBean(), getProject(),
            getBean().getDisplayName(), getBean().getSaasServicePackageName(), 
            getSaasServiceFolder(), 
            loginJS, loginFile, 
            callbackJS, callbackFile,
            parameters, paramTypes
        );
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = getServiceMethodParameters();//includes request, response also
        paramUse += Util.getHeaderOrParameterUsage(filterParams);
        filterParams = super.getServiceMethodParameters();
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        
        String methodBody = "";
        methodBody += "             try {\n";
        methodBody += paramDecl + "\n";
        methodBody += "             "+REST_RESPONSE+" result = " + getBean().getSaasServiceName() + 
                "." + getBean().getSaasServiceMethodName() + "(" + paramUse + ");\n";
        if(getBean().getHttpMethod() == HttpMethodType.GET &&
                    !getBean().findRepresentationTypes(getBean().getMethod()).isEmpty()) {
            String resultClass = getBean().getOutputWrapperPackageName()+ "." +getBean().getOutputWrapperName();
            methodBody += "        "+resultClass+" resultObj = null;\n";
            methodBody += "             javax.xml.bind.JAXBContext jc = \n";
            methodBody += "                 javax.xml.bind.JAXBContext.newInstance(\n"+resultClass+".class.getPackage().getName());\n";
            methodBody += "             javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();\n";
            methodBody += "             resultObj = ("+resultClass+") u.unmarshal(\n";
            methodBody += "                 new javax.xml.transform.stream.StreamSource(\n";
            methodBody += "                     new java.io.StringReader(result.getDataAsString()))\n";
            methodBody += "                 );\n";
            methodBody += "             System.out.println(\"The SaasService returned: \"+resultObj.toString());\n";
        } else {
            methodBody += "             System.out.println(\"The SaasService returned: \"+result.getDataAsString());\n";
        }
        methodBody += "             } catch (Exception ex) {\n";
        methodBody += "                 //java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);\n";
        methodBody += "                 ex.printStackTrace();\n";
        methodBody += "             }\n";
       
        return methodBody;
    }
    
    @Override
    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        if(bean.getAuthenticationType() == SaasAuthenticationType.SESSION_KEY)
            return Util.getAuthenticatorMethodParametersForWeb();
        else
            return super.getAuthenticatorMethodParameters();
    }
    
    @Override
    protected List<ParameterInfo> getServiceMethodParameters() {
        if(bean.getAuthenticationType() == SaasAuthenticationType.SESSION_KEY)
            return Util.getServiceMethodParametersForWeb(getBean());
        else
            return super.getServiceMethodParameters();
    }
    
    @Override
    protected String getLoginBody(WadlSaasBean bean, 
            String groupName, String paramVariableName) throws IOException {
        if(getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY)
            return null;
        return Util.createSessionKeyLoginBodyForWeb(bean, groupName, paramVariableName);
    }
    
    @Override
    protected String getTokenBody(WadlSaasBean bean, 
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException {
        if(getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY)
            return null;
        return Util.createSessionKeyTokenBodyForWeb(bean, groupName, paramVariableName,
                saasServicePkgName);
    }
    
    @Override
    protected String getSessionKeyLoginArguments() {
        return Util.getSessionKeyLoginArgumentsForWeb();
    }
}
