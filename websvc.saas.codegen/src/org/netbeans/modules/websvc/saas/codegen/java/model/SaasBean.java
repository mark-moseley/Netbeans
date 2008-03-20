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

package org.netbeans.modules.websvc.saas.codegen.java.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey.Login;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey.Logout;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey.Token;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.Method;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output.Media;
import org.netbeans.modules.websvc.saas.model.jaxb.Params;
import org.netbeans.modules.websvc.saas.model.jaxb.Params.Param;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SessionKey.Token.Prompt;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication.SignedUrl;
import org.netbeans.modules.websvc.saas.model.jaxb.Sign;

/**
 *
 * @author Peter Liu
 */
public abstract class SaasBean extends GenericResourceBean {

    public static final String RESOURCE_TEMPLATE = AbstractGenerator.TEMPLATES_SAAS+"WrapperResource.java"; //NOI18N
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

    public SaasBean(String name, String packageName, String uriTemplate, 
            MimeType[] mediaTypes, String[] representationTypes, HttpMethodType[] methodTypes) {
        super(name, packageName, uriTemplate, mediaTypes, representationTypes, methodTypes);
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
            outputWrapperName += AbstractGenerator.CONVERTER_SUFFIX;
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

    private  SaasBean.SessionKeyAuthentication.Method createSessionKeyMethod(Method method, SaasBean.SessionKeyAuthentication sessionKeyAuth) {
        if (method != null) {
            SaasBean.SessionKeyAuthentication.Method skMethod = sessionKeyAuth.createMethod();
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
    
    protected Object getAuthUsingId(Authentication auth) {
        return null;
    }
    
    public void findAuthentication(SaasMethod m) throws IOException {
        Authentication auth2 = m.getSaas().getSaasMetadata().getAuthentication();
        if(auth2 == null) {
            throw new IOException("Element saas-services/service-metadata/authentication " +
                    "missing in saas service xml for: "+getName());
        }
        if(auth2.getHttpBasic() != null) {
            setAuthenticationType(SaasAuthenticationType.HTTP_BASIC);
            setAuthentication(new HttpBasicAuthentication());
        } else if(auth2.getCustom() != null) {
            setAuthenticationType(SaasAuthenticationType.CUSTOM);
            setAuthentication(new CustomAuthentication());
        } else if(auth2.getApiKey() != null) {
            setAuthenticationType(SaasAuthenticationType.API_KEY);
            setAuthentication(new ApiKeyAuthentication(auth2.getApiKey().getId()));
        } else if(auth2.getSignedUrl() != null && auth2.getSignedUrl().size() > 0) {
            setAuthenticationType(SaasAuthenticationType.SIGNED_URL);
            List<SignedUrl> signedUrlList = auth2.getSignedUrl();
            SignedUrl signedUrl = (SignedUrl) getAuthUsingId(auth2);
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
            Login login = sessionKey.getLogin();
            if(login != null) {
                SaasBean.SessionKeyAuthentication.Login skLogin = sessionKeyAuth.createLogin();
                sessionKeyAuth.setLogin(skLogin);
                sign = login.getSign();
                if(sign != null) {
                    skLogin.setSignId(sign.getId());
                    skLogin.setParameters(findSignParameters(sign));
                }
                skLogin.setMethod(createSessionKeyMethod(login.getMethod(), sessionKeyAuth));
            }
            Token token = sessionKey.getToken();
            if(token != null) {
                SaasBean.SessionKeyAuthentication.Token skToken = sessionKeyAuth.createToken(token.getId());
                sessionKeyAuth.setToken(skToken);
                sign = token.getSign();
                if(sign != null) {
                    skToken.setSignId(sign.getId());
                    skToken.setParameters(findSignParameters(sign));
                }
                skToken.setMethod(createSessionKeyMethod(token.getMethod(), sessionKeyAuth));
                
                Prompt prompt = token.getPrompt();
                if(prompt != null) {
                    SaasBean.SessionKeyAuthentication.Token.Prompt skPrompt = skToken.createPrompt();
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
            Logout logout = sessionKey.getLogout();
        } else {
            setAuthenticationType(SaasAuthenticationType.PLAIN);
        }
        if(auth2.getProfile() != null)
            setAuthenticationProfile(auth2.getProfile());
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
    }
    
    public class HttpBasicAuthentication extends SaasAuthentication {
        public HttpBasicAuthentication() {
            
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
        List<ParameterInfo> params = Collections.emptyList();
        private SaasBean.SessionKeyAuthentication.Login login;
        private SaasBean.SessionKeyAuthentication.Token token;
        private SaasBean.SessionKeyAuthentication.Logout logout;

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
    
    public class CustomAuthentication extends SaasAuthentication {
        public CustomAuthentication() {
        }
    }

}