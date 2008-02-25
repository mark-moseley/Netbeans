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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsResourceClassCodeGenerator extends JaxRsCodeGenerator {

    public JaxRsResourceClassCodeGenerator(JTextComponent targetComponent, 
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
    }
    
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle);

        preGenerate();
        
        createAuthenticatorClass();
        
        createSaasServiceClass();
        addSaasServiceMethod();
        addImportsToSaasService();
        
        insertSaasServiceAccessCode(isInBlock(getTargetComponent()));
        addImportsToTargetFile();

        FileObject outputWrapperFO = generateJaxbOutputWrapper();
        if (outputWrapperFO != null) {
            setJaxbOutputWrapperSource(JavaSource.forFileObject(outputWrapperFO));
        }
        generateSaasServiceResourceClass();
        addSubresourceLocator();
        FileObject refConverterFO = getOrCreateGenericRefConverter().getFileObjects().iterator().next();
        modifyTargetConverter();
        FileObject[] result = new FileObject[]{getTargetFile(), getWrapperResourceFile(), refConverterFO, outputWrapperFO};
        if (outputWrapperFO == null) {
            result = new FileObject[]{getTargetFile(), getWrapperResourceFile(), refConverterFO};
        }
        JavaSourceHelper.saveSource(result);

        finishProgressReporting();

        return new HashSet<FileObject>(Arrays.asList(result));
    }

    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        String converterName = getConverterName();
        
        //Evaluate template parameters
        paramUse += getQueryParameterUsage(getBean().getTemplateParameters());
        paramDecl += getQueryParameterDeclaration(getBean().getTemplateParameters());

        //Evaluate query parameters
        paramUse += getQueryParameterUsage(getBean().getQueryParameters());
        paramDecl += getQueryParameterDeclaration(getBean().getQueryParameters());

        if(paramUse.endsWith(", "))
            paramUse = paramUse.substring(0, paramUse.length()-2);
        
        String methodBody = "";
        methodBody += "        " + converterName + " converter = new " + converterName + "();\n";
        methodBody += "             String result = " + getSaasServiceName() + "." + getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += "             converter.setString(result);\n";
        methodBody += "             return converter;\n";
        
        return methodBody;
    }
}
