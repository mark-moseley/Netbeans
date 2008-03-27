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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.HttpBasicAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SessionKeyAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseGenerator.Method;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseGenerator.Token.Prompt;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseTemplates;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SaasAuthentication.UseTemplates.Template;
import org.netbeans.modules.websvc.saas.codegen.java.model.SaasBean.SignedUrlAuthentication;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.AbstractTask;
import org.netbeans.modules.websvc.saas.codegen.java.support.JavaSourceHelper;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author nam
 */
public class JaxRsAuthenticationGenerator {

    private WadlSaasBean bean = null;
    private String loginArgs;
    private FileObject serviceFolder;
    private Object saasAuthFile;
    private JavaSource saasAuthJS;
    private List<ParameterInfo> authMethodParams;
    private JavaSource loginJS;
    private FileObject loginFile;
    private JavaSource callbackJS;
    private FileObject callbackFile;
    private Project project;

    public JaxRsAuthenticationGenerator(WadlSaasBean bean,
            Project project) throws IOException {
        this.bean = bean;
        this.project = project;
    }

    public WadlSaasBean getBean() {
        return (WadlSaasBean) bean;
    }
    
    public Project getProject() {
        return project;
    }
    
    public String getLoginArguments() {
        return loginArgs;
    }
    
    public void setLoginArguments(String loginArgs) {
        this.loginArgs = loginArgs;
    }
    
    public List<ParameterInfo> getAuthenticatorMethodParameters() {
        return authMethodParams;
    }
    
    public void setAuthenticatorMethodParameters(List<ParameterInfo> authMethodParams) {
        this.authMethodParams = authMethodParams;
    }
    
    public FileObject getSaasServiceFolder() throws IOException {
        return serviceFolder;
    }
    
    public void setSaasServiceFolder(FileObject serviceFolder) throws IOException {
        this.serviceFolder = serviceFolder;
    }
    
    public SaasAuthenticationType getAuthenticationType() throws IOException {
        return getBean().getAuthenticationType();
    }

    /* 
     * Insert this code before new "+Constants.REST_CONNECTION+"()
     */
    public String getPreAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.API_KEY) {
            methodBody += "        String apiKey = " + getBean().getAuthenticatorClassName() + ".getApiKey();";
        } else if (authType == SaasAuthenticationType.SESSION_KEY) {
            SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) getBean().getAuthentication();
            methodBody += "        " + getBean().getAuthenticatorClassName() + ".login(" + getLoginArguments() + ");\n";
            List<ParameterInfo> signParams = sessionKey.getParameters();
            String paramStr = "";

            if (signParams != null && signParams.size() > 0) {
                paramStr = Util.getSignParamDeclaration(getBean(), signParams, Collections.<ParameterInfo>emptyList());
            }

            String sigName = sessionKey.getSigKeyName();
            paramStr += "        String " +
                    Util.getVariableName(sigName) + " = " +
                    getBean().getAuthenticatorClassName() + ".sign(\n";//sig
            paramStr += "                new String[][] {\n";
            for (ParameterInfo p : getBean().getInputParameters()) {
                if (p.getName().equals(sigName)) continue;
                
                paramStr += "                    {\"" + p.getName() + "\", " +
                        Util.getVariableName(p.getName()) + "},\n";
            }
            paramStr += "        });\n";
            methodBody += paramStr;

        } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
            HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) getBean().getAuthentication();
            methodBody += "        " + getBean().getAuthenticatorClassName() + ".login(" + getLoginArguments() + ");\n";
        }
        return methodBody;
    }

    /* 
     * Insert this code after new "+Constants.REST_CONNECTION+"()
     */
    public String getPostAuthenticationCode() {
        String methodBody = "";
        SaasAuthenticationType authType = getBean().getAuthenticationType();
        if (authType == SaasAuthenticationType.SIGNED_URL) {
            SignedUrlAuthentication signedUrl = (SignedUrlAuthentication) getBean().getAuthentication();
            List<ParameterInfo> signParams = signedUrl.getParameters();
            if (signParams != null && signParams.size() > 0) {
                String paramStr = Util.getSignParamDeclaration(getBean(), signParams, getBean().getInputParameters());
                paramStr += "        String " +
                        Util.getVariableName(signedUrl.getSigKeyName()) + " = " +
                        getBean().getAuthenticatorClassName() + ".sign(\n";
                paramStr += "                new String[][] {\n";
                for (ParameterInfo p : signParams) {
                    paramStr += "                    {\"" + p.getName() + "\", " +
                            Util.getVariableName(p.getName()) + "},\n";
                }
                paramStr += "        });\n";
                methodBody += paramStr;
            }
        }
        return methodBody;
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
                    authTemplate = AbstractGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.HTTP_BASIC) {
                    authTemplate = AbstractGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SIGNED_URL) {
                    authTemplate = AbstractGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                } else if (authType == SaasAuthenticationType.SESSION_KEY) {
                    authTemplate = AbstractGenerator.TEMPLATES_SAAS + 
                            getAuthenticationType().getClassIdentifier();
                }
                if (authTemplate != null) {
                    saasAuthJS = JavaSourceHelper.createJavaSource(
                            authTemplate + Constants.SERVICE_AUTHENTICATOR + "."+Constants.JAVA_EXT,
                            targetFolder, getBean().getSaasServicePackageName(), authFileName);// NOI18n
                    Set<FileObject> files = new HashSet<FileObject>(saasAuthJS.getFileObjects());
                    if (files != null && files.size() > 0) {
                        saasAuthFile = files.iterator().next();
                    }
                }
            }
        } else {
            UseTemplates useTemplates = null;
            if(bean.getAuthentication() instanceof SessionKeyAuthentication) {
                SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
                useTemplates = sessionKey.getUseTemplates();
            } else if(bean.getAuthentication() instanceof HttpBasicAuthentication) {
                HttpBasicAuthentication httpBasic = (HttpBasicAuthentication) bean.getAuthentication();
                useTemplates = httpBasic.getUseTemplates();
            }
            if(useTemplates != null) {
                for (Template template : useTemplates.getTemplates()) {
                    String id = template.getId();
                    String type = template.getType();
                    String templateUrl = template.getUrl();

                    String fileName = null;
                    if (type.equals(Constants.AUTH)) {
                        fileName = getBean().getAuthenticatorClassName();
                    } else
                        continue;

                    if(templateUrl.endsWith("."+Constants.JAVA_EXT)) {
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
                prof = Util.createDataObjectFromTemplate(AbstractGenerator.SAAS_SERVICES + "/" +
                        getBean().getGroupName() + "/" + getBean().getDisplayName() + "/profile.properties", targetFolder, profileName);// NOI18n
            } catch (Exception ex1) {
                try {
                    prof = Util.createDataObjectFromTemplate(AbstractGenerator.SAAS_SERVICES + "/" +
                            getBean().getGroupName() + "/profile.properties",
                            targetFolder, profileName);// NOI18n
                } catch (Exception ex2) {
                    try {
                        prof = Util.createDataObjectFromTemplate(AbstractGenerator.TEMPLATES_SAAS +
                                getBean().getAuthenticationType().value() +
                                ".properties", targetFolder, profileName);// NOI18n
                    } catch (Exception ex3) {//ignore
                    }
                }
            }
        }
    }

    
    /**
     *  Create Authorization Frame
     */
    public void createAuthorizationClasses() throws IOException {
        if (getBean().isDropTargetWeb()) {
            List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
            final String[] parameters = Util.getGetParamNames(filterParams);
            final Object[] paramTypes = Util.getGetParamTypes(filterParams);
            Util.createSessionKeyAuthorizationClassesForWeb(
                getBean(), getProject(),
                getBean().getSaasName(), getBean().getSaasServicePackageName(), 
                getSaasServiceFolder(), 
                loginJS, loginFile, 
                callbackJS, callbackFile,
                parameters, paramTypes, getBean().isUseTemplates()
            );
        }
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
                Util.addInputParamFields(saasAuthJS, fields, modifier);//add sessionKey field. apiKey, secret fields already in template
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
            methodName = Constants.LOGIN;
            comment = methodName + "\n";
            List<ParameterInfo> filterParams = getAuthenticatorMethodParameters();
            final String[] parameters = Util.getGetParamNames(filterParams);
            final Object[] paramTypes = Util.getGetParamTypes(filterParams);
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
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyLoginBodyForWeb(bean, groupName, paramVariableName);
        }
        String methodBody = "";
        SessionKeyAuthentication sessionKey = (SessionKeyAuthentication) bean.getAuthentication();
        UseGenerator useGenerator = sessionKey.getUseGenerator();
        if (useGenerator != null) {
            Login login = useGenerator.getLogin();
            if (login != null) {
                String tokenName = Util.getTokenName(useGenerator);
                String tokenMethodName = Util.getTokenMethodName(useGenerator);
                methodBody += "        if (" + Util.getVariableName(sessionKey.getSessionKeyName()) + " == null) {\n";
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
        if (getBean().isDropTargetWeb()) {
            if (getBean().getAuthenticationType() != SaasAuthenticationType.SESSION_KEY) {
                return null;
            }
            return Util.createSessionKeyTokenBodyForWeb(bean, groupName, paramVariableName,
                    saasServicePkgName);
        }
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
                    String name = Util.getVariableName((String) e.getKey());
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
                                    Util.getParameterName(p, true, true) + "},\n";
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

    private String getSignParamUsage(List<ParameterInfo> signParams, String groupName) {
        return Util.getSignParamUsage(signParams, groupName, 
                getBean().isDropTargetWeb());
    }
}
