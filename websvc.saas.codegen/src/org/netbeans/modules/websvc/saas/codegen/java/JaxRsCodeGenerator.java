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

import com.sun.source.tree.ClassTree;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.DropFileType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsCodeGenerator extends SaasCodeGenerator {

    private FileObject saasServiceFile = null;
    private JavaSource saasServiceJS = null;
    private FileObject serviceFolder = null;
    private DropFileType dropFileType;
    private JaxRsAuthenticationGenerator authGen;

    public JaxRsCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        this(targetComponent, targetFile, new WadlSaasBean(m));
    }

    public JaxRsCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasBean bean) throws IOException {
        super(targetComponent, targetFile, bean);
        saasServiceFile = SourceGroupSupport.findJavaSourceFile(getProject(),
                getBean().getSaasServiceName());
        if (saasServiceFile != null) {
            saasServiceJS = JavaSource.forFileObject(saasServiceFile);
        }

        this.authGen = new JaxRsAuthenticationGenerator(bean, getProject());
        this.authGen.setLoginArguments(getLoginArguments());
        this.authGen.setAuthenticatorMethodParameters(getAuthenticatorMethodParameters());
        this.authGen.setSaasServiceFolder(getSaasServiceFolder());

    }

    @Override
    public WadlSaasBean getBean() {
        return (WadlSaasBean) bean;
    }

    public JaxRsAuthenticationGenerator getAuthenticationGenerator() {
        return authGen;
    }

    public JavaSource getSaasServiceSource() {
        return saasServiceJS;
    }

    public FileObject getSaasServiceFolder() throws IOException {
        if (serviceFolder == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            serviceFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],
                    getBean().getSaasServicePackageName(), true);
        }
        return serviceFolder;
    }

    public DropFileType getDropFileType() {
        return dropFileType;
    }

    void setDropFileType(DropFileType dropFileType) {
        this.dropFileType = dropFileType;
    }

    @Override
    protected void preGenerate() throws IOException {
        super.preGenerate();
        createRestConnectionFile(getProject());

        if (getBean().getMethod().getSaas().getLibraryJars().size() > 0) {
            Util.addClientJars(getBean(), getProject(), null);
        }
    }

    protected String getCustomMethodBody() throws IOException {
        return "";
    }

    protected String getServiceMethodBody() throws IOException {
        String fixedCode = getFixedParameterDeclaration();

        String pathParamsCode = "";
        if (getBean().getTemplateParameters() != null && getBean().getTemplateParameters().size() > 0) {
            pathParamsCode = getTemplateParameterDefinition(getBean().getTemplateParameters(), Constants.PATH_PARAMS, false);
        }
        String queryParamsCode = "";
        if (getBean().getQueryParameters() != null &&
                getBean().getQueryParameters().size() > 0) {
            queryParamsCode = Util.getHeaderOrParameterDefinition(getBean().getQueryParameters(), Constants.QUERY_PARAMS, false);
        }

        String methodBody = "";
        methodBody += "        " + fixedCode;

        //Insert authentication code before new "+Constants.REST_CONNECTION+"() call
        methodBody += "             " +
                getAuthenticationGenerator().getPreAuthenticationCode() + "\n";

        //Insert parameter declaration
        methodBody += "        " + pathParamsCode;
        methodBody += "        " + queryParamsCode;

        methodBody += "             " + Constants.REST_CONNECTION + " conn = new " + Constants.REST_CONNECTION + "(\"" + getBean().getUrl() + "\"";
        if (!pathParamsCode.trim().equals("")) {
            methodBody += ", " + Constants.PATH_PARAMS + ", " + (queryParamsCode.trim().equals("") ? "null" : Constants.QUERY_PARAMS);
        } else if (!queryParamsCode.trim().equals("")) {
            methodBody += ", " + Constants.QUERY_PARAMS;
        }
        methodBody += ");\n";

        //Insert authentication code after new "+Constants.REST_CONNECTION+"() call
        methodBody += "             " +
                getAuthenticationGenerator().getPostAuthenticationCode() + "\n";

        HttpMethodType httpMethod = getBean().getHttpMethod();
        String headerUsage = "null";
        if (getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0) {
            headerUsage = Constants.HEADER_PARAMS;
            methodBody += "        " + Util.getHeaderOrParameterDefinition(getBean().getHeaderParameters(), Constants.HEADER_PARAMS, false, httpMethod);
        }

        boolean hasRequestRep = !getBean().findInputRepresentations(getBean().getMethod()).isEmpty();
        //Insert the method call
        String returnStatement = "return conn";
        if (httpMethod == HttpMethodType.GET) {
            methodBody += "             " + returnStatement + ".get(" + headerUsage + ");\n";
        } else if (httpMethod == HttpMethodType.PUT) {
            if (hasRequestRep) {
                methodBody += "             " + returnStatement + ".put(" + headerUsage + ", " + Constants.PUT_POST_CONTENT + ");\n";
            } else {
                methodBody += "             " + returnStatement + ".put(" + headerUsage + ");\n";
            }
        } else if (httpMethod == HttpMethodType.POST) {
            if (hasRequestRep) {
                methodBody += "             " + returnStatement + ".post(" + headerUsage + ", " + Constants.QUERY_PARAMS + ");\n";
            } else {
                methodBody += "             " + returnStatement + ".post(" + headerUsage + ", (java.io.InputStream) null);\n";
            }
        } else if (httpMethod == HttpMethodType.DELETE) {
            methodBody += "             " + returnStatement + ".delete(" + headerUsage + ");\n";
        }

        return methodBody;
    }

    protected String getFixedParameterDeclaration() {
        String fixedCode = "";
        List<ParameterInfo> inputParams = getBean().getInputParameters();
        List<ParameterInfo> signParams = null;

        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            signParams = sessionKey.getParameters();
        } else {
            signParams = Collections.emptyList();
        }
        for (ParameterInfo param : getBean().getInputParameters()) {
            if (param.isFixed() && !Util.isContains(param, signParams)) {
                fixedCode += "String " + getVariableName(param.getName()) + " = \"" + findParamValue(param) + "\";\n";
            }
        }

        return fixedCode;
    }

    protected List<ParameterInfo> getServiceMethodParameters() {
        List<ParameterInfo> params = getBean().filterParametersByAuth(getBean().filterParameters(
                new ParamFilter[]{ParamFilter.FIXED}));
        HttpMethodType httpMethod = getBean().getHttpMethod();

        if (httpMethod == HttpMethodType.PUT || httpMethod == HttpMethodType.POST) {

            ParameterInfo contentTypeParam = Util.findParameter(getBean().getInputParameters(), Constants.CONTENT_TYPE);
            Class contentType = InputStream.class;

            if (contentTypeParam != null) {
                if (!contentTypeParam.isFixed() && !params.contains(contentTypeParam)) {
                    params.add(contentTypeParam);
                } else {
                    String value = findParamValue(contentTypeParam);
                    if (value.equals("text/plain") || value.equals("application/xml") ||
                            value.equals("text/xml")) {     //NOI18N

                        contentType = String.class;
                    }
                }
                if (!getBean().findInputRepresentations(getBean().getMethod()).isEmpty()) {
                    params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, contentType));
                }
            }
        }
        return params;
    }

    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    protected String getLoginArguments() {
        return "";
    }

    protected void addImportsToTargetFile() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(getBean().getSaasServicePackageName() + "." + getBean().getSaasServiceName());
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_RESPONSE);
//        if(getBean().canGenerateJAXBUnmarshaller()) {
//            imports.addAll(Util.getJaxBClassImports());
//        }
        Util.addImportsToSource(getTargetSource(), imports);
    }

    protected void addImportsToSaasService() throws IOException {
        List<String> imports = new ArrayList<String>();
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_CONNECTION);
        imports.add(REST_CONNECTION_PACKAGE + "." + REST_RESPONSE);
//        if(getBean().canGenerateJAXBUnmarshaller()) {
//            imports.add(InputStream.class.getName());
//        }
        Util.addImportsToSource(saasServiceJS, imports);
    }

    /**
     *  Insert the Saas client call
     */
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String code = "";
            if (isInBlock) {
                code = getCustomMethodBody();
            } else {
                code = "\nprivate String call" + getBean().getName() + "Service() {\n"; // NOI18n

                code += getCustomMethodBody() + "\n";
                code += "return result;\n";
                code += "}\n";
            }
            insert(code, getTargetComponent(), true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     *  Create Saas Service
     */
    public void createSaasServiceClass() throws IOException {
        if (saasServiceFile == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            String pkg = getBean().getSaasServicePackageName();
            FileObject targetFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0], pkg, true);
            saasServiceJS = JavaSourceHelper.createJavaSource(
                    getBean().getSaasServiceTemplate(), targetFolder, pkg, getBean().getSaasServiceName());
            Set<FileObject> files = new HashSet<FileObject>(saasServiceJS.getFileObjects());
            if (files != null && files.size() > 0) {
                saasServiceFile = files.iterator().next();
            }
        }
    }

    /**
     *  Return target and generated file objects
     */
    protected void addSaasServiceMethod() throws IOException {
        List<ParameterInfo> filterParams = getServiceMethodParameters();
        final String[] parameters = getGetParamNames(filterParams);
        final Object[] paramTypes = getGetParamTypes(filterParams);

        if (JavaSourceHelper.isContainsMethod(saasServiceJS,
                getBean().getSaasServiceMethodName(), parameters, paramTypes)) {
            return;
        }
        ModificationResult result = saasServiceJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                Modifier[] modifiers = Constants.PUBLIC_STATIC;

                String type = REST_RESPONSE;
                String bodyText = "{ \n" + getServiceMethodBody() + "\n }";


                String comment = "Retrieves representation of an instance of " + getBean().getQualifiedClassName() + "\n";// NOI18N

                for (String param : parameters) {
                    comment += "@param $PARAM$ resource URI parameter\n".replace("$PARAM$", param);// NOI18N

                }
                comment += "@return an instance of " + type;// NOI18N

                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        getBean().getSaasServiceMethodName(), type, parameters, paramTypes,
                        null, null, new String[]{"java.io.IOException"},
                        bodyText, comment);      //NOI18N

                copy.rewrite(initial, tree);
            }
        });
        result.commit();
    }

    public ParameterInfo findParameter(String name) {
        List<ParameterInfo> params = getBean().getInputParameters();
        if (params != null) {
            for (ParameterInfo param : params) {
                if (param.getName().equals(name)) {
                    return param;
                }
            }
        }
        return null;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params,
            String indent) {
        if (indent == null) {
            indent = " ";
        }
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getVariableName(param.getName());
            String paramVal = findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl += indent + param.getType().getName() + " " + name + " = " + paramVal + ";\n";
            } else {
                if (paramVal != null) {
                    paramDecl += indent + "String " + name + " = \"" + paramVal + "\";\n";
                } else {
                    paramDecl += indent + "String " + name + " = null;\n";
                }
            }
        }
        return paramDecl;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String indent = "                 ";
        return getHeaderOrParameterDeclaration(params, indent);
    }

    //String pathParams[] = new String[][]  { {"{volumeId}", volumeId},  {"{objectId}", objectId}}; 
    private String getTemplateParameterDefinition(List<ParameterInfo> params, String varName, boolean evaluate) {
        String paramsStr = null;
        StringBuffer sb = new StringBuffer();
        for (ParameterInfo param : params) {
            String paramName = getParameterName(param);
            String paramVal = null;
            if (evaluate) {
                paramVal = findParamValue(param);
                if (param.getType() != String.class) {
                    sb.append("{\"" + paramName + "\", \"" + paramVal + "\".toString()},\n");
                } else {
                    if (paramVal != null) {
                        sb.append("{\"{" + paramName + "}\", \"" + paramVal + "\"},\n");
                    } else {
                        sb.append("{\"{" + paramName + "}\", null},\n");
                    }
                }
            } else {
                sb.append("{\"{" + paramName + "}\", " + paramName + "},\n");
            }
        }
        paramsStr = sb.toString();
        if (params.size() > 0) {
            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
        }

        String paramCode = "";
        paramCode += "             String[][] " + varName + " = new String[][]{\n";
        paramCode += "                 " + paramsStr + "\n";
        paramCode += "             };\n";
        return paramCode;
    }

    private String findParamValue(ParameterInfo param) {
        return Util.findParamValue(param);
    }
}
