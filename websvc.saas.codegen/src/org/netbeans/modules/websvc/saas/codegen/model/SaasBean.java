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

package org.netbeans.modules.websvc.saas.codegen.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.SaasClientCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.model.SaasBean.SaasAuthentication.UseGenerator.Token.Prompt;
import org.netbeans.modules.websvc.saas.codegen.util.Util;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.HttpBasic;
import org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator.Login;
import org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator.Logout;
import org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator.Token;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifact;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifacts;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output.Media;
import org.netbeans.modules.websvc.saas.model.jaxb.Params;
import org.netbeans.modules.websvc.saas.model.jaxb.Params.Param;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication;
import org.netbeans.modules.websvc.saas.model.jaxb.Authenticator;
import org.netbeans.modules.websvc.saas.model.jaxb.UseGenerator;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SignedUrl;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.CodeGen;
import org.netbeans.modules.websvc.saas.model.jaxb.Sign;
import org.netbeans.modules.websvc.saas.model.jaxb.TemplateType.Template;
import org.netbeans.modules.websvc.saas.model.jaxb.UseTemplates;
import org.netbeans.modules.websvc.saas.util.SaasUtil;

/**
 *
 * @author Peter Liu
 */
public abstract class SaasBean extends GenericResourceBean {

    public static final String RESOURCE_TEMPLATE = SaasClientCodeGenerator.TEMPLATES_SAAS+"WrapperResource.java"; //NOI18N
    private String outputWrapperName;
    private String wrapperPackageName;
    private List<ParameterInfo> inputParams;
    private List<ParameterInfo> headerParams;
    private List<ParameterInfo> templateParams;
    private List<ParameterInfo> queryParams;
    private String resourceTemplate;
    private SaasAuthenticationType authType;
    private SaasBean.SaasAuthentication auth;
    private String authProfile;
    private boolean isDropTargetWeb = false;
    private String groupName;
    private String displayName;
    private Saas saas;

    public SaasBean(Saas saas, String name, String packageName, String uriTemplate, 
            MimeType[] mediaTypes, String[] representationTypes, HttpMethodType[] methodTypes) {
        super(name, packageName, uriTemplate, mediaTypes, representationTypes, methodTypes);
        this.saas = saas;
        
        SaasGroup g = saas.getParentGroup();
        if(g.getParent() == null) //g is root group, so use topLevel group usually the vendor group
            g = saas.getTopLevelGroup();
        this.groupName = Util.normailizeName(g.getName());
        this.displayName = Util.normailizeName(saas.getDisplayName());
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getSaasName() {
        return getGroupName()+getDisplayName();
    }
    
    public String getSaasServiceName() {
        return getGroupName()+getDisplayName()/*+"Service"*/;
    }
    
    public String getSaasServicePackageName() {
        return SaasClientCodeGenerator.REST_CONNECTION_PACKAGE+"."+
                SaasUtil.toValidJavaName(getGroupName()).toLowerCase();
    }
    
    public String getAuthenticatorClassName() {
        return Util.getAuthenticatorClassName(getSaasName());
    }
    
    public String getAuthorizationFrameClassName() {
        return Util.getAuthorizationFrameClassName(getSaasName());
    }
    
    public boolean isDropTargetWeb() {
        return isDropTargetWeb;
    }
    
    public void setIsDropTargetWeb(boolean isDropTargetWeb) {
        this.isDropTargetWeb = isDropTargetWeb;
    }
    
    protected void setInputParameters(List<ParameterInfo> inputParams) {
        this.inputParams = inputParams;
    }

    @Override
    public List<ParameterInfo> getInputParameters() {
        if (inputParams == null) {
            inputParams = initInputParameters();
        }

        return inputParams;
    }

    public List<ParameterInfo> getHeaderParameters() {
        if (headerParams == null) {
            headerParams = new ArrayList<ParameterInfo>();

            for (ParameterInfo param : getInputParameters()) {
                if (param.getStyle() == ParamStyle.HEADER) {
                    headerParams.add(param);
                }
            }
        }
        return headerParams;
    }
    
    @Override
    public String[] getUriParams() {
        List<String> uriParams = new ArrayList<String>();

        for (ParameterInfo param : getTemplateParameters()) {
            uriParams.add(param.getName());
        }
        return uriParams.toArray(new String[0]);
    }
    
    public List<ParameterInfo> getTemplateParameters() {
        if (templateParams == null) {
            templateParams = new ArrayList<ParameterInfo>();

            for (ParameterInfo param : getInputParameters()) {
                if (param.getStyle() == ParamStyle.TEMPLATE) {
                    templateParams.add(param);
                }
            }
        }
        return templateParams;
    }
    
    protected void setTemplateParameters(List<ParameterInfo> templateParams) {
        this.templateParams = templateParams;
    }

    protected abstract List<ParameterInfo> initInputParameters();

    @Override
    public List<ParameterInfo> getQueryParameters() {
        if (queryParams == null) {
            queryParams = new ArrayList<ParameterInfo>();

            for (ParameterInfo param : getInputParameters()) {
                if (param.getStyle() == ParamStyle.QUERY) {
                    queryParams.add(param);
                }
            }
        }
        return queryParams;
    }
    
    public String getOutputWrapperName() {
        if (outputWrapperName == null) {
            outputWrapperName = getName();

            if (outputWrapperName.endsWith(RESOURCE_SUFFIX)) {
                outputWrapperName = outputWrapperName.substring(0, outputWrapperName.length() - 8);
            }
            outputWrapperName += SaasClientCodeGenerator.CONVERTER_SUFFIX;
        }
        return outputWrapperName;
    }
    
    public void setOutputWrapperName(String outputWrapperName) {
        this.outputWrapperName = outputWrapperName;
    }

    public String getOutputWrapperPackageName() {
        return wrapperPackageName;
    }

    public void setOutputWrapperPackageName(String packageName) {
        wrapperPackageName = packageName;
    }

    @Override
    public String[] getRepresentationTypes() {
        if (getMimeTypes().length == 1 && getMimeTypes()[0] == MimeType.HTML) {
            return new String[]{String.class.getName()};
        } else {
            String rep = getOutputWrapperPackageName() + "." + getOutputWrapperName();
            List<String> repList = new ArrayList<String>();
            for(MimeType m:getMimeTypes()) {//stuff rep with as much mimetype length
                repList.add(rep);
            }
            return repList.toArray(new String[0]);
        }
    }

    public String[] getOutputTypes() {
        String[] types = new String[]{"java.lang.String"}; //NOI18N
        return types;
    }

    public String getResourceClassTemplate() {
        return resourceTemplate;
    }
    
    protected void setResourceClassTemplate(String template) {
        this.resourceTemplate = template;
    }
    
    @Override
    public SaasAuthenticationType getAuthenticationType() {
        return authType;
    }
    
    public void setAuthenticationType(SaasAuthenticationType authType) {
        this.authType = authType;
    }
    
    
    @Override
    public SaasAuthentication getAuthentication() {
        return auth;
    }
    
    public void setAuthentication(SaasAuthentication auth) {
        this.auth = auth;
    }
    
    
    public String getAuthenticationProfile() {
        return this.authProfile;
    }

    private  SaasBean.SessionKeyAuthentication.UseGenerator.Method createSessionKeyUseGeneratorMethod(
            Method method, SaasBean.SessionKeyAuthentication.UseGenerator useGenerator) {
        if (method != null) {
            SaasBean.SessionKeyAuthentication.UseGenerator.Method skMethod = 
                    useGenerator.createMethod();
            skMethod.setId(method.getId());
            skMethod.setName(method.getName());
            skMethod.setHref(method.getHref());
            return skMethod;
        }
        return null;
    }

    private List<ParameterInfo> findSignParameters(Sign sign) {
        if (sign != null) {
            Params params = sign.getParams();
            if (params != null && params.getParam() != null) {
                List<ParameterInfo> signParams = new ArrayList<ParameterInfo>();
                findSaasParams(signParams, params.getParam());
                return signParams;
            }
        }
        return Collections.emptyList();
    }
    
    private void setAuthenticationProfile(String profile) {
        this.authProfile = profile;
    }
    
    protected Object getSignedUrl(Authentication auth) {
        return null;
    }
    
    public void findAuthentication(SaasMethod m) throws IOException {
        Authentication auth2 = m.getSaas().getSaasMetadata().getAuthentication();
        if(auth2 == null) {
            setAuthenticationType(SaasAuthenticationType.PLAIN);
            return;
            //throw new IOException("Element saas-services/service-metadata/authentication " +
            //        "missing in saas service xml for: "+getName());
        }
        if(auth2.getHttpBasic() != null) {
            HttpBasic httpBasic = auth2.getHttpBasic();
            setAuthenticationType(SaasAuthenticationType.HTTP_BASIC);
            HttpBasicAuthentication httpBasicAuth = new HttpBasicAuthentication(
                    httpBasic.getUsername(),
                    httpBasic.getPassword());
            setAuthentication(httpBasicAuth);
            SaasBean.SaasAuthentication.UseGenerator skUseGenerator = 
                    httpBasicAuth.createUseGenerator();
            SaasBean.SaasAuthentication.UseTemplates skUseTemplates = 
                    httpBasicAuth.createUseTemplates();
            if(findUseGenerator(m, httpBasic.getAuthenticator(), skUseGenerator)) {
                httpBasicAuth.setUseGenerator(skUseGenerator);
            } else if(findUseTemplates(m, httpBasic.getAuthenticator(), skUseTemplates)) {
                httpBasicAuth.setUseTemplates(skUseTemplates);
            } else {
                throw new IOException("authentication element has no use-generator or use-templates children.");
            }
        } else if(auth2.getCustom() != null) {
            setAuthenticationType(SaasAuthenticationType.CUSTOM);
            setAuthentication(new CustomAuthentication());
        } else if(auth2.getApiKey() != null) {
            setAuthenticationType(SaasAuthenticationType.API_KEY);
            setAuthentication(new ApiKeyAuthentication(auth2.getApiKey().getId()));
        } else if(auth2.getSignedUrl() != null && auth2.getSignedUrl().size() > 0) {
            setAuthenticationType(SaasAuthenticationType.SIGNED_URL);
            List<SignedUrl> signedUrlList = auth2.getSignedUrl();
            SignedUrl signedUrl = (SignedUrl) getSignedUrl(auth2);
            if(signedUrl == null)
                signedUrl = signedUrlList.get(0);
            SignedUrlAuthentication signedUrlAuth = new SignedUrlAuthentication();
            if(signedUrl.getSigId() != null) {
                signedUrlAuth.setSigKeyName(signedUrl.getSigId());
            }
            setAuthentication(signedUrlAuth);
            Sign sign = signedUrl.getSign();
            if (sign != null) {
                Params params = sign.getParams();
                if(params != null && params.getParam() != null) {
                    List<ParameterInfo> signParams = new ArrayList<ParameterInfo>();
                    findSaasParams(signParams, params.getParam());
                    signedUrlAuth.setParameters(signParams);
                }
            }
        } else if(auth2.getSessionKey() != null) {
            SessionKey sessionKey = auth2.getSessionKey();
            setAuthenticationType(SaasAuthenticationType.SESSION_KEY);
            SessionKeyAuthentication sessionKeyAuth = new SessionKeyAuthentication(
                    sessionKey.getApiId(), 
                    sessionKey.getSessionId(),
                    sessionKey.getSigId());
            setAuthentication(sessionKeyAuth);
            Sign sign = sessionKey.getSign();
            if (sign != null) {
                Params params = sign.getParams();
                if(params != null && params.getParam() != null) {
                    List<ParameterInfo> signParams = new ArrayList<ParameterInfo>();
                    findSaasParams(signParams, params.getParam());
                    sessionKeyAuth.setParameters(signParams);
                }
            }
            SaasBean.SaasAuthentication.UseGenerator skUseGenerator = 
                    sessionKeyAuth.createUseGenerator();
            SaasBean.SaasAuthentication.UseTemplates skUseTemplates = 
                    sessionKeyAuth.createUseTemplates();
            if(findUseGenerator(m, sessionKey.getAuthenticator(), skUseGenerator)) {
                sessionKeyAuth.setUseGenerator(skUseGenerator);
            } else if(findUseTemplates(m, sessionKey.getAuthenticator(), skUseTemplates)) {
                sessionKeyAuth.setUseTemplates(skUseTemplates);
            } else {
                throw new IOException("authentication element has no use-generator or use-templates children.");
            }
        } else {
            setAuthenticationType(SaasAuthenticationType.PLAIN);
        }
        if(auth2.getProfile() != null)
            setAuthenticationProfile(auth2.getProfile());
    }
    
    private boolean findUseGenerator(SaasMethod m, Authenticator authenticator,
            SaasBean.SaasAuthentication.UseGenerator skUseGenerator) throws IOException {
        if(authenticator == null)
            throw new IOException("No authentication element inside sessionkey element in saas-metadata.");
        if(authenticator.getUseGenerator() != null) {
            UseGenerator useGenerator = authenticator.getUseGenerator();
            
            Sign sign = null;
            Login login = useGenerator.getLogin();
            if(login != null) {
                SaasBean.SessionKeyAuthentication.UseGenerator.Login skLogin = skUseGenerator.createLogin();
                skUseGenerator.setLogin(skLogin);
                sign = login.getSign();
                if(sign != null) {
                    skLogin.setSignId(sign.getId());
                    skLogin.setParameters(findSignParameters(sign));
                }
                skLogin.setMethod(createSessionKeyUseGeneratorMethod(
                        login.getMethod(), skUseGenerator));
            }
            Token token = useGenerator.getToken();
            if(token != null) {
                SaasBean.SessionKeyAuthentication.UseGenerator.Token skToken = skUseGenerator.createToken(token.getId());
                skUseGenerator.setToken(skToken);
                sign = token.getSign();
                if(sign != null) {
                    skToken.setSignId(sign.getId());
                    skToken.setParameters(findSignParameters(sign));
                }
                skToken.setMethod(createSessionKeyUseGeneratorMethod(
                        token.getMethod(), skUseGenerator));

                Token.Prompt prompt = token.getPrompt();
                if(prompt != null) {
                    SaasBean.SessionKeyAuthentication.UseGenerator.Token.Prompt skPrompt = skToken.createPrompt();
                    skToken.setPrompt(skPrompt);
                    sign = prompt.getSign();
                    if(sign != null) {
                        skPrompt.setSignId(sign.getId());
                        skPrompt.setParameters(findSignParameters(sign));
                    }
                    skPrompt.setDesktopUrl(prompt.getDesktop().getUrl());
                    skPrompt.setWebUrl(prompt.getWeb().getUrl());
                }
            }
            Logout logout = useGenerator.getLogout();
            return true;
        }
        return false;
    }
    
    private boolean findUseTemplates(SaasMethod m, Authenticator authenticator,
            SaasBean.SessionKeyAuthentication.UseTemplates skUseTemplates) throws IOException {
        if(authenticator == null)
            throw new IOException("No authentication element inside sessionkey element in saas-metadata.");
        if(authenticator.getUseTemplates() != null) {
            UseTemplates useTemplates = authenticator.getUseTemplates();
            List<Template> templates = null;
            if(!isDropTargetWeb() && useTemplates.getDesktop() != null && 
                    useTemplates.getDesktop().getTemplate() != null) {
                templates = useTemplates.getDesktop().getTemplate();
            } else if(isDropTargetWeb() && useTemplates.getWeb() != null && useTemplates.getWeb().getTemplate() != null) {
                templates = useTemplates.getWeb().getTemplate();
            }
            if(templates == null || templates.isEmpty())
                throw new IOException(Constants.UNSUPPORTED_DROP);
            List<SaasBean.SessionKeyAuthentication.UseTemplates.Template> templateNames = 
                    new ArrayList<SaasBean.SessionKeyAuthentication.UseTemplates.Template>();
            Map<String, String> artifactsMap = getArtifactTemplates(m);
            for(Template t:templates) {
                if(t.getHref() != null && !t.getHref().equals("")) {
                    String artifactUrl = artifactsMap.get(t.getHref());
                    if(artifactUrl != null)
                        templateNames.add(skUseTemplates.createTemplate(
                            t.getHref(), t.getType(), artifactUrl));
                }
            }
            skUseTemplates.setTemplates(templateNames);
            return true;
        }
        return false;
    }

    public boolean isUseTemplates() {
        return (getAuthenticationType() == SaasAuthenticationType.SESSION_KEY &&
                ((SessionKeyAuthentication)this.getAuthentication()).getUseTemplates() != null) ||
                    (getAuthenticationType() == SaasAuthenticationType.HTTP_BASIC &&
                        ((HttpBasicAuthentication)this.getAuthentication()).getUseTemplates() != null);
    }
    
    private Map<String, String> getArtifactTemplates(SaasMethod m) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        CodeGen codegen = m.getSaas().getSaasMetadata().getCodeGen();
        if(codegen != null) {
            List<Artifacts> artifactsList = codegen.getArtifacts();
            if(artifactsList != null) {
                for(Artifacts artifacts: artifactsList) {
                    List<Artifact> artifactList = artifacts.getArtifact();
                    if(artifactList != null) {
                        for(Artifact artifact: artifactList) {
                            String id = artifact.getId();
                            String type = artifact.getType();
                            if(type == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@type value is null.");
                            String artifactUrl = artifact.getUrl();
                            if(artifactUrl == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@url value is null.");
                            if(type.equals(CustomClientSaasBean.ARTIFACT_TYPE_TEMPLATE)) {
                                map.put(id, artifactUrl);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    public void findSaasParams(List<ParameterInfo> paramInfos, List<Param> params) {
        if (params != null) {
            for (Param param:params) {
                //<param name="replace" type="xsd:boolean" style="query" required="false" default="some value">
                String paramName = param.getName();
                Class paramType = findJavaType(param.getType());
                ParameterInfo paramInfo = new ParameterInfo(paramName, paramType);
                if(param.getId() != null && !param.getId().trim().equals(""))
                    paramInfo.setId(param.getId());
                paramInfo.setIsRequired(param.isRequired()!=null?param.isRequired():false);
                paramInfo.setFixed(param.getFixed());
                paramInfo.setDefaultValue(param.getDefault());
                paramInfos.add(paramInfo);
            }
        }
    }
 
    public static Class findJavaType(String schemaType) {       
        if(schemaType != null) {
            int index = schemaType.indexOf(":");        //NOI18N
            
            if(index != -1) {
                schemaType = schemaType.substring(index+1);
            }
            
            if(schemaType.equalsIgnoreCase("string")) {     //NOI18N
                return String.class;
            } else if(schemaType.equalsIgnoreCase("int")) {       //NOI18N
                return Integer.class;
            } else if(schemaType.equalsIgnoreCase("date")) {       //NOI18N
                return Date.class;
            } else if(schemaType.equalsIgnoreCase("time")) {       //NOI18N
                return Time.class;
            } else if(schemaType.equalsIgnoreCase("httpMethod")) {       //NOI18N
                return HttpMethodType.class;
            }
        }
        
        return String.class;
    }
    
    public void findSaasMediaType(List<MimeType> mimeTypes, Media media) {
        String mediaType = media.getType();
        String[] mTypes = mediaType.split(",");
        for(String m1:mTypes) {
            MimeType mType = MimeType.find(m1);
            if (mType != null) {
                mimeTypes.add(mType);
            }
        }
    }
    
    public class Time {
        public Time() {
        }
    }
    
    public class SaasAuthentication {
        public SaasAuthentication() {
        }

        public UseGenerator createUseGenerator() {
            return new UseGenerator();
        }
        
        public UseTemplates createUseTemplates() {
            return new UseTemplates();
        }
    
        public class UseGenerator {
        
            private Login login;
            private Token token;
            private Logout logout;
            
            public UseGenerator() {
                
            }

            public Login getLogin() {
                return login;
            }

            public void setLogin(Login login) {
                this.login = login;
            }

            public Token getToken() {
                return token;
            }

            public void setToken(Token token) {
                this.token = token;
            }

            public Logout getLogout() {
                return logout;
            }

            public void setLogout(Logout logout) {
                this.logout = logout;
            }

            public Login createLogin() {
                return new Login();
            }

            public Token createToken(String id) {
                return new Token(id);
            }

            public Logout createLogout() {
                return new Logout();
            }

            public Method createMethod() {
                return new Method();
            }

            public class Login {

                List<ParameterInfo> params = Collections.emptyList();
                Method method;
                String signId;

                public Login() {
                }

                public String getSignId() {
                    return signId;
                }

                public void setSignId(String signId) {
                    this.signId = signId;
                }

                public List<ParameterInfo> getParameters() {
                    return params;
                }

                public void setParameters(List<ParameterInfo> params) {
                    this.params = params;
                }

                public Method getMethod() {
                    return method;
                }

                public void setMethod(Method method) {
                    this.method = method;
                }
            }

            public class Token extends Login {

                private String id;
                private Prompt prompt;

                public Token(String id) { 
                    this.id = id;
                }

                public String getId() {
                    return id;
                }

                public Prompt getPrompt() {
                    return prompt;
                }

                public void setPrompt(Prompt prompt) {
                    this.prompt = prompt;
                }

                private Prompt createPrompt() {
                    return new Prompt();
                }

                public class Prompt {

                    private String deskTopUrl;
                    private String webUrl;
                    List<ParameterInfo> params = Collections.emptyList();
                    private String signId;

                    public Prompt() {    
                    }

                    public String getSignId() {
                        return signId;
                    }

                    public void setSignId(String signId) {
                        this.signId = signId;
                    }

                    public List<ParameterInfo> getParameters() {
                        return params;
                    }

                    public void setParameters(List<ParameterInfo> params) {
                        this.params = params;
                    }

                    public String getDesktopUrl() {
                        return deskTopUrl;
                    }

                    public void setDesktopUrl(String deskTopUrl) {
                        this.deskTopUrl = deskTopUrl;
                    }

                    public String getWebUrl() {
                        return webUrl;
                    }

                    public void setWebUrl(String webUrl) {
                        this.webUrl = webUrl;
                    }
                }
            }

            public class Logout extends Login {
                public Logout() {
                }
            }

            public class Method {

                String id;
                String name;
                String href;

                public Method() {  
                }

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getHref() {
                    return href;
                }

                public void setHref(String href) {
                    this.href = href;
                }
            }
        }
        
        public class UseTemplates {
        
            private List<Template> templates = Collections.emptyList();
            
            public UseTemplates() {
            }
            
            public List<Template> getTemplates() {
                return templates;
            }

            public void setTemplates(List<Template> templates) {
                this.templates = templates;
            }
            
            public Template createTemplate(String id, String type, String url) {
                return new Template(id, type, url);
            }
            
            public class Template {

                private String id;
                private String type;
                private String url;

                public Template(String id, String type, String url) {
                    this.id = id;
                    this.type = type;
                    this.url = url;
                }

                public String getId() {
                    return id;
                }

                public String getType() {
                    return type;
                }
                
                public String getUrl() {
                    return url;
                }
            }
        }
    }
    
    public class HttpBasicAuthentication extends SaasAuthentication {
        private String username;
        private String password;

        private UseTemplates useTemplates;
        private UseGenerator useGenerator;

        public HttpBasicAuthentication(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUserNameId() {
            return username;
        }
        
        public String getPasswordId() {
            return password;
        }
        
        public UseTemplates getUseTemplates() {
            return useTemplates;
        }
        
        public void setUseTemplates(UseTemplates useTemplates) {
            this.useTemplates = useTemplates;
        }
        
        public UseGenerator getUseGenerator() {
            return useGenerator;
        }
        
        public void setUseGenerator(UseGenerator useGenerator) {
            this.useGenerator = useGenerator;
        }
    }
    
    public class ApiKeyAuthentication extends SaasAuthentication {
        private String keyName;
        
        public ApiKeyAuthentication(String keyName) {
            this.keyName = keyName;
        }
        
        public String getApiKeyName() {
            return keyName;
        }
    }
    
    public class SignedUrlAuthentication extends SaasAuthentication {
        
        private String sig;
        List<ParameterInfo> params = Collections.emptyList();
        public SignedUrlAuthentication() {
        }
        
        public String getSigKeyName() {
            return sig;
        }
        
        public void setSigKeyName(String sig) {
            this.sig = sig;
        }
        
        public List<ParameterInfo> getParameters() {
            return params;
        }
        
        public void setParameters(List<ParameterInfo> params) {
            this.params = params;
        }
    }

    public class SessionKeyAuthentication extends SaasAuthentication {
        
        private String apiId;
        private String sessionId;
        private String sig;
        private List<ParameterInfo> params = Collections.emptyList();
        private UseTemplates useTemplates;
        private UseGenerator useGenerator;

        public SessionKeyAuthentication(String apiId,
                String sessionId, String sig) {
            this.apiId = apiId;
            this.sessionId = sessionId;
            this.sig = sig;
        }
        
        public String getApiKeyName() {
            return apiId;
        }
        
        public String getSessionKeyName() {
            return sessionId;
        }

        public String getSigKeyName() {
            return sig;
        }
        
        public List<ParameterInfo> getParameters() {
            return params;
        }

        public void setParameters(List<ParameterInfo> params) {
            this.params = params;
        }
        
        public UseTemplates getUseTemplates() {
            return useTemplates;
        }
        
        public void setUseTemplates(UseTemplates useTemplates) {
            this.useTemplates = useTemplates;
        }
        
        public UseGenerator getUseGenerator() {
            return useGenerator;
        }
        
        public void setUseGenerator(UseGenerator useGenerator) {
            this.useGenerator = useGenerator;
        }
    }
    
    public class CustomAuthentication extends SaasAuthentication {
        public CustomAuthentication() {
        }
    }
   
}