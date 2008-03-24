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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamFilter;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseGenerator.Method;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseGenerator.Token.Prompt;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.Time;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.SourceGroupSupport;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsCodeGenerator extends SaasCodeGenerator {

    private FileObject saasServiceFile = null;
    private JavaSource saasServiceJS = null;
    private Object saasAuthFile;
    private JavaSource saasAuthJS;
    private FileObject serviceFolder = null;
    private HashMap<String, ParameterInfo> filterParamMap;

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
    }

    @Override
    public WadlSaasBean getBean() {
        return (WadlSaasBean) bean;
    }

    public FileObject getSaasServiceFolder() throws IOException {
        if (serviceFolder == null) {
            SourceGroup[] srcGrps = SourceGroupSupport.getJavaSourceGroups(getProject());
            serviceFolder = SourceGroupSupport.getFolderForPackage(srcGrps[0],
                    getBean().getSaasServicePackageName(), true);
        }
        return serviceFolder;
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
        methodBody += "             " + getPreAuthenticationCode() + "\n";

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
        methodBody += "             " + getPostAuthenticationCode() + "\n";

        HttpMethodType httpMethod = getBean().getHttpMethod();
        String headerUsage = "null";
        if (getBean().getHeaderParameters() != null && getBean().getHeaderParameters().size() > 0) {
            headerUsage = Constants.HEADER_PARAMS;
            methodBody += "        " + Util.getHeaderOrParameterDefinition(getBean().getHeaderParameters(), Constants.HEADER_PARAMS, false, httpMethod);
        }

        //Insert the method call
        String returnStatement = "return conn";
        if (httpMethod == HttpMethodType.GET) {
            methodBody += "             " + returnStatement + ".get(" + headerUsage + ");\n";
        } else if (httpMethod == HttpMethodType.PUT) {
            methodBody += "             " + returnStatement + ".put(" + headerUsage + ", " + Constants.PUT_POST_CONTENT + ");\n";
        } else if (httpMethod == HttpMethodType.POST) {
            if (!queryParamsCode.trim().equals("")) {
                methodBody += "             " + returnStatement + ".post(" + headerUsage + ", " + Constants.QUERY_PARAMS + ");\n";
            } else {
                methodBody += "             " + returnStatement + ".post(" + headerUsage + ", " + Constants.PUT_POST_CONTENT + ");\n";
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
            if (param.isFixed() && !isContains(param, signParams)) {
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
            if (!Util.isContains(params, new ParameterInfo(Constants.CONTENT_TYPE, String.class))) {
                params.add(new ParameterInfo(Constants.CONTENT_TYPE, String.class));
            }
            params.add(new ParameterInfo(Constants.PUT_POST_CONTENT, InputStream.class));
        }
        return params;
    }

    protected List<ParameterInfo> getAuthenticatorMethodParameters() {
        return Collections.emptyList();
    }

    /*
     */
    private String getSignParamUsage(List<ParameterInfo> signParams, String groupName) {
        return Util.getSignParamUsage(signParams, groupName, 
                getBean().isDropTargetWeb());
    }

    /*
     * Generates something like 
    String apiKey = FacebookAuthenticator.getApiKey();
    String sessionKey = FacebookAuthenticator.getSessionKey();
    String method = "facebook.friends.get";
    String v = "1.0";
    String callId = String.valueOf(System.currentTimeMillis());
     */
    private String getSignParamDeclaration(List<ParameterInfo> signParams, List<ParameterInfo> filterParams) {
        String paramStr = "";
        for(ParameterInfo p:signParams) {
            String[] pIds = Util.getParamIds(p, getBean().getSaasName(), 
                    getBean().isDropTargetWeb());
            if(pIds != null) {//process special case
                paramStr += "        String "+ getVariableName(pIds[0]) +" = "+ pIds[1] +";\n";
                continue;
            }
            if (isContains(p, filterParams)) {
                continue;
            }
            
            paramStr += "        String " + getVariableName(p.getName()) + " = ";
            if (p.getFixed() != null) {
                paramStr += "\"" + p.getFixed() + "\";\n";
            } else if (p.getType() == Date.class) {
                paramStr += "conn.getDate();\n";
            } else if (p.getType() == Time.class) {
                paramStr += "String.valueOf(System.currentTimeMillis());\n";
            } else if (p.getType() == HttpMethodType.class) {
                paramStr += "\"" + getBean().getHttpMethod().value() + "\";\n";
            } else if (p.isRequired()) {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "\"\";\n";
                }
            } else {
                if (p.getDefaultValue() != null) {
                    paramStr += getQuotedValue(p.getDefaultValue().toString()) + ";\n";
                } else {
                    paramStr += "null;\n";
                }
            }
        }
        paramStr += "\n";
        return paramStr;
    }

    private String getQuotedValue(String value) {
        String normalized = value;
        if (normalized.startsWith("\"")) {
            normalized = normalized.substring(1);
        } else if (normalized.endsWith("\"")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return "\"" + normalized + "\"";
    }

    protected String getSessionKeyLoginArguments() {
        return "";
    }

    /* 
     * Insert this code before new "+Constants.REST_CONNECTION+"()
     */
    private String getPreAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.API_KEY) {
            methodBody += "        String apiKey = " + getBean().getAuthenticatorClassName() + ".getApiKey();";
        } else if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            methodBody += "        " + getBean().getAuthenticatorClassName() + ".login(" + getSessionKeyLoginArguments() + ");\n";
            List<ParameterInfo> signParams = sessionKey.getParameters();
            String paramStr = "";

            if (signParams != null && signParams.size() > 0) {
                paramStr = getSignParamDeclaration(signParams, Collections.<ParameterInfo>emptyList());
            }

            String sigName = sessionKey.getSigKeyName();
            paramStr += "        String " +
                    getVariableName(sigName) + " = " +
                    getBean().getAuthenticatorClassName() + ".sign(\n";//sig
            paramStr += "                new String[][] {\n";
            for (ParameterInfo p : getBean().getInputParameters()) {
                if (p.getName().equals(sigName)) continue;
                
                paramStr += "                    {\"" + p.getName() + "\", " +
                        getVariableName(p.getName()) + "},\n";
            }
            paramStr += "        });\n";
            methodBody += paramStr;

        }
        return methodBody;
    }

    /* 
     * Insert this code after new "+Constants.REST_CONNECTION+"()
     */
    private String getPostAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.HTTP_BASIC) {
            methodBody += "        conn.setAuthenticator(new " +
                    getBean().getSaasName() + Constants.SERVICE_AUTHENTICATOR + "());\n";
        } else if (authType == SaasAuthenticationType.SIGNED_URL) {
            SignedUrlAuthentication signedUrl = (SignedUrlAuthentication) getBean().getAuthentication();
            List<ParameterInfo> signParams = signedUrl.getParameters();
            if (signParams != null && signParams.size() > 0) {
                String paramStr = getSignParamDeclaration(signParams, getBean().getInputParameters());
                paramStr += "        String " +
                        getVariableName(signedUrl.getSigKeyName()) + " = " +
                        getBean().getAuthenticatorClassName() + ".sign(\n";
                paramStr += "                new String[][] {\n";
                for (ParameterInfo p : signParams) {
                    paramStr += "                    {\"" + p.getName() + "\", " +
                            getVariableName(p.getName()) + "},\n";
                }
                paramStr += "        });\n";
                methodBody += paramStr;
            }
        }
        return methodBody;
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
        Util.checkScanning();
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
     *  Create Authenticator
     */
    public void createAuthenticatorClass() throws IOException {
        FileObject targetFolder = getSaasServiceFolder();
        if(!getBean().isUseTemplates()) {
            if(saasAuthFile == null) {
                String authFileName = getBean().getAuthenticatorClassName();
                String authTemplate = null;
                SaasAuthenticationType authType = getBean().getAuthenticationType();
                if (authType == SaasAuthenticationType.API_KEY) {
                    authTemplate = TEMPLATES_SAAS + authType.getClassIdentifier();
                } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
                    authTemplate = TEMPLATES_SAAS + authType.getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SIGNED_URL) {
                    authTemplate = TEMPLATES_SAAS + authType.getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SESSION_KEY) {
                    authTemplate = TEMPLATES_SAAS + authType.getClassIdentifier();
                }
                if (authTemplate != null) {
                    saasAuthJS = JavaSourceHelper.createJavaSource(
                            authTemplate + Constants.SERVICE_AUTHENTICATOR + ".java",
                            targetFolder, getBean().getSaasServicePackageName(), authFileName);// NOI18n
                    Set<FileObject> files = new HashSet<FileObject>(saasAuthJS.getFileObjects());
                    if (files != null && files.size() > 0) {
                        saasAuthFile = files.iterator().next();
                    }
                }
            }
        } else {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
            UseTemplates useTemplates = sessionKey.getUseTemplates();
            for (Template template : useTemplates.getTemplates()) {
                String id = template.getId();
                String type = template.getType();
                String templateUrl = template.getUrl();

                String fileName = null;
                if (type.equals("auth")) {
                    fileName = getBean().getAuthenticatorClassName();
                } else
                    continue;
                
                if(templateUrl.endsWith(".java")) {
                    JavaSourceHelper.createJavaSource(templateUrl, targetFolder, 
                            getBean().getSaasServicePackageName(), fileName);
                } else {
                    if (templateUrl.indexOf("/") != -1) {
                        fileName = getBean().getSaasName() +
                                templateUrl.substring(templateUrl.lastIndexOf("/") + 1);
                    }
                    if (fileName != null) {
                        FileObject fobj = targetFolder.getFileObject(fileName);
                        if (fobj == null) {
                            Util.createDataObjectFromTemplate(templateUrl, targetFolder,
                                    fileName);
                        }
                    }
                }
            }
        }

        //Also copy profile.properties
        String profileName = getBean().getAuthenticatorClassName().toLowerCase();
        DataObject prof = null;
        String authProfile = getBean().getAuthenticationProfile();
        if (authProfile != null && !authProfile.trim().equals("")) {
            try {
                prof = Util.createDataObjectFromTemplate(authProfile,
                        targetFolder, profileName);
            } catch (Exception ex) {
                throw new IOException("Profile file specified in " +
                        "saas-services/service-metadata/authentication/@profile, " +
                        "not found: " + authProfile);// NOI18n
            }
        } else {
            try {
                prof = Util.createDataObjectFromTemplate(SAAS_SERVICES + "/" +
                        getBean().getGroupName() + "/" + getBean().getDisplayName() + "/profile.properties", targetFolder, profileName);// NOI18n
            } catch (Exception ex1) {
                try {
                    prof = Util.createDataObjectFromTemplate(SAAS_SERVICES + "/" +
                            getBean().getGroupName() + "/profile.properties",
                            targetFolder, profileName);// NOI18n
                } catch (Exception ex2) {
                    try {
                        prof = Util.createDataObjectFromTemplate(TEMPLATES_SAAS +
                                getBean().getAuthenticationType().value() +
                                ".properties", targetFolder, profileName);// NOI18n
                    } catch (Exception ex3) {//ignore
                    }
                }
            }
        }

    //Modify profile file with user defined values
    // Commenting this code out since we are getting the api key values from the input param diaglog.
//            if(prof != null) {
//                EditorCookie ec = (EditorCookie) prof.getCookie(EditorCookie.class);
//                StyledDocument doc = ec.openDocument();
//                String profileText = null;
//                if(getBean().getAuthenticationType() == SaasAuthenticationType.API_KEY) {
//                    ParameterInfo p = findParameter(((ApiKeyAuthentication)getBean().getAuthentication()).getApiKeyName());
//                    if(p != null && p.getDefaultValue() != null)
//                        profileText = "api_key="+p.getDefaultValue()+"\n";
//                }
//                if(profileText != null) {
//                    try {
//                        doc.insertString(doc.getLength(), profileText, null);
//                    } catch (BadLocationException ex) {
//                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, 
//                                NbBundle.getMessage(AbstractGenerator.class, 
//                                    "MSG_PropertyReplaceFailed"), ex); // NOI18N
//                    }
//                }
//            }
    }

    /**
     *  Create Authorization Classes
     */
    public void createAuthorizationClasses() throws IOException {
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

    /**
     *  Return target and generated file objects
     */
    protected void modifyAuthenticationClass() throws IOException {
        if (bean.getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
            return;
        }
        Modifier[] modifiers = Constants.PUBLIC_STATIC;
        Object[] throwList = null;
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        if (sessionKey.getUseGenerator() != null) {
            UseGenerator useGenerator = sessionKey.getUseGenerator();
            //create getSessionKey() method
            String methodName = "getSessionKey";
            String comment = "";
            String bodyText = "";
            Object returnType = null;
            if (sessionKey.getSessionKeyName() != null) {
                String name = Util.getParameterName(sessionKey.getSessionKeyName(), true, true);
                List<ParameterInfo> fields = new ArrayList<ParameterInfo>();
                fields.add(new ParameterInfo(name, String.class));
                Modifier[] modifier = Constants.PRIVATE_STATIC;
                addInputParamFields(saasAuthJS, fields, modifier);//add sessionKey field. apiKey, secret fields already in template
                methodName = Util.getSessionKeyMethodName(name);
                comment = methodName + "\n";
                returnType = "String";
                bodyText = "return " + name + ";\n";
                if (bodyText != null) {
                    modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                            null, null, throwList, bodyText);
                }
            }

            //create login() method
            returnType = Constants.VOID;
            methodName = "login";
            comment = methodName + "\n";
            List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
            final String[] parameters = getGetParamNames(filterParams);
            final Object[] paramTypes = getGetParamTypes(filterParams);
            bodyText = getLoginBody(getBean(), getBean().getDisplayName(), Constants.QUERY_PARAMS);
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }

            //create getToken() method
            methodName = Util.getTokenMethodName(useGenerator);
            comment = methodName + "\n";
            returnType = "String";
            bodyText = getTokenBody(getBean(), getBean().getDisplayName(), Constants.QUERY_PARAMS,
                    getBean().getSaasServicePackageName());
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }

            //create logout() method
            methodName = "logout";
            comment = methodName + "\n";
            returnType = Constants.VOID;
            bodyText = getLogoutBody();
            if (bodyText != null) {
                modifyAuthenticationClass(comment, modifiers, returnType, methodName,
                        parameters, paramTypes, throwList, bodyText);
            }
        }
    }

    private void addInputParamFields(JavaSource source, final List<ParameterInfo> params, final Modifier[] modifier) throws IOException {
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                List<ParameterInfo> addList = new ArrayList<ParameterInfo>();
                for (ParameterInfo p : params) {
                    if (JavaSourceHelper.getField(copy, getParameterName(p, true, true, true)) == null) {
                        addList.add(p);
                    }
                }
                JavaSourceHelper.addFields(copy, getParamNames(addList),
                        getParamTypeNames(addList), getParamValues(addList), modifier);
            }
        });
        result.commit();
    }

    /**
     *  Return target and generated file objects
     */
    protected void modifyAuthenticationClass(final String comment, final Modifier[] modifiers,
            final Object returnType, final String name, final String[] parameters, final Object[] paramTypes,
            final Object[] throwList, final String bodyText)
            throws IOException {
        if (JavaSourceHelper.isContainsMethod(saasAuthJS, name, parameters, paramTypes)) {
            return;
        }
        ModificationResult result = saasAuthJS.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                ClassTree initial = JavaSourceHelper.getTopLevelClassTree(copy);
                ClassTree tree = JavaSourceHelper.addMethod(copy, initial,
                        modifiers, null, null,
                        name, returnType, parameters, paramTypes,
                        null, null,
                        throwList, "{ \n" + bodyText + "\n }", comment);
                copy.rewrite(initial, tree);
            }
        });
        result.commit();
    }

    protected String getLoginBody(WadlSaasBean bean,
            String groupName, String paramVariableName) throws IOException {
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Login login = useGenerator.getLogin();
            if (login != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String tokenMethodName = Util.getTokenMethodName(useGenerator);
                methodBody += "        if (" + getVariableName(sessionKey.getSessionKeyName()) + " == null) {\n";
                methodBody += "            String " + tokenName + " = " + tokenMethodName + "(" +
                        Util.getHeaderOrParameterUsage(getAuthenticatorMethodParameters()) + ");\n\n";

                methodBody += "            if (" + tokenName + " != null) {\n";
                methodBody += "                try {\n";
                Map<String, String> tokenMap = new HashMap<String, String>();
                methodBody += Util.getLoginBody(login, getBean(), groupName, tokenMap);
                methodBody += "                } catch (IOException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n\n";

                methodBody += "            }\n";
                methodBody += "        }\n";
            }
        }
        return methodBody;
    }

    protected String getLogoutBody() {
        String methodBody = "";
        return methodBody;
    }

    protected String getTokenBody(WadlSaasBean bean,
            String groupName, String paramVariableName, String saasServicePkgName) throws IOException {
        String authFileName = getBean().getAuthorizationFrameClassName();
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Token token = useGenerator.getToken();
            if (token != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String sigId = "sig";
                if (token.getSignId() != null) {
                    sigId = token.getSignId();
                }
                String methodName = null;
                Method method = token.getMethod();
                if (method != null) {
                    methodName = method.getHref();
                    if (methodName == null) {
                        return methodBody;
                    } else {
                        methodName = methodName.startsWith("#") ? methodName.substring(1) : methodName;
                    }
                }
                methodBody += "       String " + tokenName + " = null;\n";
                methodBody += "       try {\n";
                methodBody += "            String method = \"" + methodName + "\";\n";
                methodBody += "            String v = \"1.0\";\n\n";

                List<ParameterInfo> signParams = token.getParameters();
                if (signParams != null && signParams.size() > 0) {
                    String paramStr = "";
                    paramStr += "        String " + sigId + " = sign(secret, \n";
                    paramStr += getSignParamUsage(signParams, groupName);
                    paramStr += ");\n\n";
                    methodBody += paramStr;
                }

                String queryParamsCode = "";
                Map<String, String> tokenMap = new HashMap<String, String>();
                if (method != null) {
                    String id = method.getId();
                    if (id != null) {
                        String[] tokens = id.split(",");
                        for (String tk : tokens) {
                            String[] tokenElem = tk.split("=");
                            if (tokenElem.length == 2) {
                                tokenMap.put(tokenElem[0], tokenElem[1]);
                            }
                        }
                    }
                    String href = method.getHref();
                    if (href != null) {
                        org.netbeans.modules.websvc.saas.model.wadl.Method wadlMethod =
                                SaasUtil.wadlMethodFromIdRef(
                                bean.getMethod().getSaas().getWadlModel(), href);
                        if (wadlMethod != null) {
                            ArrayList<ParameterInfo> params = bean.findWadlParams(wadlMethod);
                            if (params != null &&
                                    params.size() > 0) {
                                queryParamsCode = Util.getHeaderOrParameterDefinition(params, paramVariableName, false);
                            }
                        }
                    }
                }

                //Insert parameter declaration
                methodBody += "        " + queryParamsCode;

                methodBody += "             " + Constants.REST_CONNECTION + " conn = new " + Constants.REST_CONNECTION + "(\"" + bean.getUrl() + "\"";
                if (!queryParamsCode.trim().equals("")) {
                    methodBody += ", " + paramVariableName;
                }
                methodBody += ");\n";

                methodBody += "            String result = conn.get();\n";

                for (Entry e : tokenMap.entrySet()) {
                    String name = getVariableName((String) e.getKey());
                    String val = (String) e.getValue();
                    if (val.startsWith("{")) {
                        val = val.substring(1);
                    }
                    if (val.endsWith("}")) {
                        val = val.substring(0, val.length() - 1);
                    }
                    methodBody += "            " + name + " = result.substring(result.indexOf(\"<" + val + "\"),\n";
                    methodBody += "                            result.indexOf(\"</" + val + ">\"));\n\n";
                    methodBody += "            " + name + " = " + name + ".substring(" + name + ".indexOf(\">\") + 1);\n\n";
                }


                if (token.getPrompt() != null) {
                    Prompt prompt = token.getPrompt();
                    signParams = prompt.getParameters();
                    if (signParams != null && signParams.size() > 0) {
                        methodBody += "            String perms = \"write\";";
                        String paramStr = "";
                        paramStr += "        " + sigId + " = sign(\n";
                        paramStr += "                new String[][] {\n";
                        for (ParameterInfo p : signParams) {
                            paramStr += "                    {\"" + p.getName() + "\", " +
                                    getParameterName(p, true, true) + "},\n";
                        }
                        paramStr += "        });\n\n";
                        methodBody += paramStr;
                    }
                    String url = prompt.getDesktopUrl();
                    methodBody += "            String loginUrl = \"" + Util.getTokenPromptUrl(token, url) + "\";\n";
                }
                methodBody += "            " + authFileName + " frame = new " + authFileName + "(loginUrl);\n";
                methodBody += "            synchronized (frame) {\n";
                methodBody += "                try {\n";
                methodBody += "                    frame.wait();\n";
                methodBody += "                } catch (InterruptedException ex) {\n";
                methodBody += "                    Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "                }\n";
                methodBody += "            }\n";
                methodBody += "       } catch (IOException ex) {\n";
                methodBody += "            Logger.getLogger(" + getBean().getAuthenticatorClassName() + ".class.getName()).log(Level.SEVERE, null, ex);\n";
                methodBody += "       }\n\n";
                methodBody += "       return " + tokenName + ";\n";
            }
        }
        return methodBody;
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

    @Override
    public String[] getUriParamTypes() {
        String defaultType = String.class.getName();
        String[] types = new String[getBean().getUriParams().length];
        for (int i = 0; i < types.length; i++) {
            types[i] = defaultType;
        }
        return types;
    }

    protected String getHeaderOrParameterDeclaration(List<ParameterInfo> params) {
        String paramDecl = "";
        for (ParameterInfo param : params) {
            String name = getVariableName(param.getName());
            String paramVal = findParamValue(param);
            if (param.getType() != String.class) {
                paramDecl += "                 " + param.getType().getName() + " " + name + " = " + paramVal + ";\n";
            } else {
                if (paramVal != null) {
                    paramDecl += "                 String " + name + " = \"" + paramVal + "\";\n";
                } else {
                    paramDecl += "                 String " + name + " = null;\n";
                }
            }
        }
        return paramDecl;
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

    private boolean isContains(ParameterInfo pInfo, List<ParameterInfo> params) {
        String name = pInfo.getName();
        for (ParameterInfo p : params) {
            if (name.equals(p.getName())) {
                return true;
            }
        }

        return false;
    }
}
