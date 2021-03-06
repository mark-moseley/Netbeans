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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address10;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10Metadata;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10QName;
import org.netbeans.modules.websvc.wsitmodelext.mex.Metadata;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataSection;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.RequestSecurityTokenTemplate;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TransportToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssGssKerberosV5ApReqToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssKerberosV5ApReqToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV10Token10;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeySize;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeyType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TokenType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TrustQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EncryptionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.EndorsingSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.IssuedToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.Issuer;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.KerberosToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RecipientToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RelToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SamlToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecurityContextToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignatureToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedEndorsingSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SignedSupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SupportingTokens;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TokensQName;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.UsernameToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV10Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV11Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV11Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssSamlV20Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssUsernameToken10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssUsernameToken11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509Pkcs7Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509Pkcs7Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509PkiPathV1Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509PkiPathV1Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V1Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V1Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V3Token10;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.WssX509V3Token11;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.X509Token;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class SecurityTokensModelHelper {

    public static final int SUPPORTING = 0;     
    public static final int SIGNED_SUPPORTING = 1;
    public static final int ENDORSING = 2;
    public static final int SIGNED_ENDORSING = 3;
    public static final int NONE = 4;
    
    /**
     * Creates a new instance of SecurityTokensModelHelper
     */
    public SecurityTokensModelHelper() { }

    public static boolean isRequireClientCertificate(HttpsToken token) {
        return token.isRequireClientCertificate();
    }

    public static void setRequireClientCertificate(HttpsToken token, boolean require) {
        WSDLModel model = token.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            token.setRequireClientCertificate(require);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static String getTokenType(WSDLComponent tokenKind) {
        if (tokenKind != null) {
            WSDLComponent wc = null;
            wc = getTokenElement(tokenKind, UsernameToken.class);
            if (wc != null) return ComboConstants.USERNAME;
            wc = getTokenElement(tokenKind, X509Token.class);
            if (wc != null) return ComboConstants.X509;
            wc = getTokenElement(tokenKind, SamlToken.class);
            if (wc != null) return ComboConstants.SAML;
            wc = getTokenElement(tokenKind, RelToken.class);
            if (wc != null) return ComboConstants.REL;
            wc = getTokenElement(tokenKind, KerberosToken.class);
            if (wc != null) return ComboConstants.KERBEROS;
            wc = getTokenElement(tokenKind, SecurityContextToken.class);
            if (wc != null) return ComboConstants.SECURITYCONTEXT;
            wc = getTokenElement(tokenKind, SecureConversationToken.class);
            if (wc != null) return ComboConstants.SECURECONVERSATION;
            wc = getTokenElement(tokenKind, IssuedToken.class);
            if (wc != null) return ComboConstants.ISSUED;
        }
        return null;
    }

    public static WSDLComponent getTokenTypeElement(WSDLComponent tokenKind) {
        if (tokenKind == null) return null;
        WSDLComponent wc = null;
        wc = getTokenElement(tokenKind, HttpsToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, UsernameToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, X509Token.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SamlToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, RelToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, KerberosToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SecurityContextToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, SecureConversationToken.class);
        if (wc != null) return wc;
        wc = getTokenElement(tokenKind, IssuedToken.class);
        return wc;
    }
    
    public static String getTokenInclusionLevel(WSDLComponent tokenType) {
        String incLevelStr = ((ExtensibilityElement)tokenType).getAnyAttribute(TokensQName.INCLUDETOKENATTRIBUTE.getQName());
        if (incLevelStr != null) {
            incLevelStr = incLevelStr.substring(incLevelStr.lastIndexOf('/')+1, incLevelStr.length()); //NOI18N
            return NbBundle.getMessage(ComboConstants.class, "COMBO_" + incLevelStr); //NOI18N
        } else {
            return ComboConstants.NONE;
        }
    }

    public static String getTokenProfileVersion(WSDLComponent tokenType) {
        if (tokenType instanceof UsernameToken) {
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken10.class)) {
                return ComboConstants.WSS10;
            }
            if (SecurityPolicyModelHelper.isAttributeEnabled((ExtensibilityElement) tokenType, WssUsernameToken11.class)) {
                return ComboConstants.WSS11;
            }
        }
        if (tokenType instanceof SamlToken) {
            ExtensibilityElement e = (ExtensibilityElement) tokenType;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV10Token10.class)) return ComboConstants.SAML_V1010;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV10Token11.class)) return ComboConstants.SAML_V1011;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV11Token10.class)) return ComboConstants.SAML_V1110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV11Token11.class)) return ComboConstants.SAML_V1111;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssSamlV20Token11.class)) return ComboConstants.SAML_V2011;
        }
        if (tokenType instanceof X509Token) {
            ExtensibilityElement e = (ExtensibilityElement) tokenType;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V1Token10.class)) return ComboConstants.X509_V110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V1Token11.class)) return ComboConstants.X509_V111;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V3Token10.class)) return ComboConstants.X509_V310;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509V3Token11.class)) return ComboConstants.X509_V311;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509Pkcs7Token10.class)) return ComboConstants.X509_PKCS710;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509Pkcs7Token11.class)) return ComboConstants.X509_PKCS711;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509PkiPathV1Token10.class)) return ComboConstants.X509_PKIPATHV110;
            if (SecurityPolicyModelHelper.isAttributeEnabled(e, WssX509PkiPathV1Token11.class)) return ComboConstants.X509_PKIPATHV111;
        }
        return ComboConstants.NONE;
    }

    public static WSDLComponent getTokenElement(WSDLComponent e, Class tokenClass) {
        if (e == null) return null;
        List<Policy> policies = e.getExtensibilityElements(Policy.class);
        if ((policies != null) && (!policies.isEmpty())) {
            Policy p = policies.get(0);
            return PolicyModelHelper.getTopLevelElement(p, tokenClass);
        }
        return null;
    }
    
    public static WSDLComponent setTokenType(WSDLComponent secBinding, String tokenKindStr, String tokenTypeStr) {
        WSDLModel model = secBinding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;
        WSDLComponent tokenKind = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            Policy p = PolicyModelHelper.createElement(secBinding, PolicyQName.POLICY.getQName(), Policy.class, false);
            List<ExtensibilityElement> tokenKinds = p.getExtensibilityElements();
            if ((tokenKinds != null) && (!tokenKinds.isEmpty())) {
                for (ExtensibilityElement tkind : tokenKinds) {
                    if (ComboConstants.PROTECTION.equals(tokenKindStr) || 
                        ComboConstants.TRANSPORT.equals(tokenKindStr)) {
                        if (tkind instanceof SignatureToken ||
                            tkind instanceof TransportToken ||
                            tkind instanceof EncryptionToken ||
                            tkind instanceof InitiatorToken ||
                            tkind instanceof ProtectionToken ||
                            tkind instanceof RecipientToken) {
                                p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.ENCRYPTION.equals(tokenKindStr)) {
                        if (!(tkind instanceof SignatureToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.SIGNATURE.equals(tokenKindStr)) {
                        if (!(tkind instanceof EncryptionToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.INITIATOR.equals(tokenKindStr)) {
                        if (!(tkind instanceof RecipientToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    } else if (ComboConstants.RECIPIENT.equals(tokenKindStr)) {
                        if (!(tkind instanceof InitiatorToken)) {
                            p.removeExtensibilityElement(tkind);
                        }
                    }
                }
            }
            
            if (ComboConstants.PROTECTION.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.PROTECTIONTOKEN.getQName());
            }
            if (ComboConstants.SIGNATURE.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.SIGNATURETOKEN.getQName());
            }
            if (ComboConstants.ENCRYPTION.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.ENCRYPTIONTOKEN.getQName());
            }
            if (ComboConstants.INITIATOR.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.INITIATORTOKEN.getQName());
            }
            if (ComboConstants.RECIPIENT.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.RECIPIENTTOKEN.getQName());
            }
            if (ComboConstants.TRANSPORT.equals(tokenKindStr)) {
                tokenKind = wcf.create(p, TokensQName.TRANSPORTTOKEN.getQName());
            }

            p.addExtensibilityElement((ExtensibilityElement) tokenKind);

            Policy pinner = (Policy) wcf.create(tokenKind, PolicyQName.POLICY.getQName());
            tokenKind.addExtensibilityElement(pinner);

            if (ComboConstants.HTTPS.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.HTTPSTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                ((HttpsToken)tokenType).setRequireClientCertificate(false);
            }
            if (ComboConstants.X509.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.X509TOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.X509_V310);
            }
            if (ComboConstants.SAML.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SAMLTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.SAML_V1110);
            }
            if (ComboConstants.KERBEROS.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.KERBEROSTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.KERBEROS_KERBEROSGSS);
            }
            if (ComboConstants.ISSUED.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.ISSUEDTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());
                tokenType.addExtensibilityElement(template);

                TokenType trustTokenType = (TokenType) wcf.create(template, TrustQName.TOKENTYPE.getQName());
                template.addExtensibilityElement(trustTokenType);
                trustTokenType.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);

                KeyType trustKeyType = (KeyType) wcf.create(template, TrustQName.KEYTYPE.getQName());
                template.addExtensibilityElement(trustKeyType);
                trustKeyType.setContent(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);

                KeySize trustKeySize = (KeySize) wcf.create(template, TrustQName.KEYSIZE.getQName());
                template.addExtensibilityElement(trustKeySize);
                trustKeySize.setContent(ComboConstants.ISSUED_KEYSIZE_256);
                
                SecurityPolicyModelHelper.enableRequireInternalReference(tokenType, true);
            }
            
            if (ComboConstants.USERNAME.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.USERNAMETOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                setTokenProfileVersion(tokenType, ComboConstants.WSS10);
            }
            if (ComboConstants.REL.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.RELTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (ComboConstants.SECURECONVERSATION.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SECURECONVERSATIONTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
//                setBootstrapPolicy(tokenType, 
//                        ComboConstants.SYMMETRIC, 
//                        ComboConstants.X509, 
//                        ComboConstants.X509,
//                        ComboConstants.WSS10);
            }
            if (ComboConstants.SECURITYCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SECURITYCONTEXTTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }
            if (ComboConstants.SPNEGOCONTEXT.equals(tokenTypeStr)) {
                tokenType = wcf.create(pinner, TokensQName.SPNEGOCONTEXTTOKEN.getQName());
                pinner.addExtensibilityElement((ExtensibilityElement) tokenType);
                SecurityPolicyModelHelper.enableRequireDerivedKeys(tokenType, true);
            }

//            setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);

        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        return tokenType;
    }

    public static void setTokenInclusionLevel(WSDLComponent tokenType, String incLevel) {
        WSDLModel model = tokenType.getModel();
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            String levelStr = null;
            if (ComboConstants.NEVER.equals(incLevel)) {
                levelStr = ComboConstants.NEVER_POLICYSTR;
            } else if (ComboConstants.ALWAYS.equals(incLevel)) {
                levelStr = ComboConstants.ALWAYS_POLICYSTR;
            } else if (ComboConstants.ALWAYSRECIPIENT.equals(incLevel)) {
                levelStr = ComboConstants.ALWAYSRECIPIENT_POLICYSTR;
            } else if (ComboConstants.ONCE.equals(incLevel)) {
                levelStr = ComboConstants.ONCE_POLICYSTR;
            }
            ((ExtensibilityElement)tokenType).setAnyAttribute(TokensQName.INCLUDETOKENATTRIBUTE.getQName(), levelStr);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
   }

    public static void setTokenProfileVersion(WSDLComponent tokenType, String profileVersion) {
        WSDLModel model = tokenType.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            Policy p = PolicyModelHelper.createElement(tokenType, PolicyQName.POLICY.getQName(), Policy.class, false);
            WSDLComponent profileVersionAssertion = null;
            List<ExtensibilityElement> tokenAssertions = p.getExtensibilityElements();
            
            if (tokenType instanceof UsernameToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssUsernameToken10) ||
                            (e instanceof WssUsernameToken11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.WSS10.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSUSERNAMETOKEN10.getQName());
                if (ComboConstants.WSS11.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSUSERNAMETOKEN11.getQName());
            }
            if (tokenType instanceof SamlToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssSamlV10Token11) ||
                            (e instanceof WssSamlV10Token10) || 
                            (e instanceof WssSamlV11Token10) || 
                            (e instanceof WssSamlV11Token11) || 
                            (e instanceof WssSamlV20Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.SAML_V1010.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV10TOKEN10.getQName());
                if (ComboConstants.SAML_V1011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV10TOKEN11.getQName());
                if (ComboConstants.SAML_V1110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV11TOKEN10.getQName());
                if (ComboConstants.SAML_V1111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV11TOKEN11.getQName());
                if (ComboConstants.SAML_V2011.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSSAMLV20TOKEN11.getQName());
            }

            if (tokenType instanceof X509Token) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssX509V1Token10) ||
                            (e instanceof WssX509V3Token10) || 
                            (e instanceof WssX509V1Token11) || 
                            (e instanceof WssX509V3Token11) || 
                            (e instanceof WssX509Pkcs7Token10) || 
                            (e instanceof WssX509Pkcs7Token11) || 
                            (e instanceof WssX509PkiPathV1Token10) || 
                            (e instanceof WssX509PkiPathV1Token11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }

                if (ComboConstants.X509_V110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V1TOKEN10.getQName());
                if (ComboConstants.X509_V310.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V3TOKEN10.getQName());
                if (ComboConstants.X509_V111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V1TOKEN11.getQName());
                if (ComboConstants.X509_V311.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509V3TOKEN11.getQName());
                if (ComboConstants.X509_PKCS710.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKCS7TOKEN10.getQName());
                if (ComboConstants.X509_PKCS711.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKCS7TOKEN11.getQName());
                if (ComboConstants.X509_PKIPATHV110.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKIPATHV1TOKEN10.getQName());
                if (ComboConstants.X509_PKIPATHV111.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSX509PKIPATHV1TOKEN11.getQName());
            }
            
            if (tokenType instanceof KerberosToken) {
                if ((tokenAssertions != null) && (!tokenAssertions.isEmpty())) {
                    for (ExtensibilityElement e : tokenAssertions) {
                        if ((e instanceof WssGssKerberosV5ApReqToken11) ||
                            (e instanceof WssKerberosV5ApReqToken11)) {                     
                                p.removeExtensibilityElement(e);
                        }
                    }
                }
                if (ComboConstants.KERBEROS_KERBEROS.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSKERBEROSV5APREQTOKEN11.getQName());
                if (ComboConstants.KERBEROS_KERBEROSGSS.equals(profileVersion)) profileVersionAssertion = wcf.create(p, TokensQName.WSSGSSKERBEROSV5APREQTOKEN11.getQName());
            }

            if (profileVersionAssertion != null) p.addExtensibilityElement((ExtensibilityElement) profileVersionAssertion);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static List<WSDLComponent> getSupportingTokens(WSDLComponent c) {
        ArrayList<WSDLComponent> result = new ArrayList<WSDLComponent>(1);
        if (c == null) return null;
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }
        if (p == null) return null;
        Class[] a = { SupportingTokens.class, SignedSupportingTokens.class, 
                      EndorsingSupportingTokens.class, SignedEndorsingSupportingTokens.class };
        for (Class cl : a) {
            WSDLComponent token = PolicyModelHelper.getTopLevelElement(p, cl);
            if (token != null) {
                result.add(token);
            }
        }
        return result;
    }
    
    public static WSDLComponent getSupportingToken(WSDLComponent c, int supportingType) {
        if (c == null) return null;
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }
        if (p == null) return null;
        if (SUPPORTING == supportingType) {
            return PolicyModelHelper.getTopLevelElement(p, SupportingTokens.class);
        }
        if (SIGNED_SUPPORTING == supportingType) {
            return PolicyModelHelper.getTopLevelElement(p, SignedSupportingTokens.class);
        }
        if (ENDORSING == supportingType) {
            return PolicyModelHelper.getTopLevelElement(p, EndorsingSupportingTokens.class);
        }
        if (SIGNED_ENDORSING == supportingType) {
            return PolicyModelHelper.getTopLevelElement(p, SignedEndorsingSupportingTokens.class);
        }
        return null;
    }

    public static void removeSupportingTokens(WSDLComponent c) {
        if (c == null) return;
        WSDLModel model = c.getModel();
        WSDLComponent p = c;
        if ((c instanceof Binding) || (c instanceof BindingOperation) || 
           (c instanceof BindingInput) || (c instanceof BindingOutput) || (c instanceof BindingFault)) {
             p = PolicyModelHelper.getPolicyForElement(c);
        }

        if (p == null) return;
        
        ExtensibilityElement rem = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            rem = PolicyModelHelper.getTopLevelElement(p, SupportingTokens.class);
            if (rem != null) {
                rem.getParent().removeExtensibilityElement(rem);
            }

            rem = PolicyModelHelper.getTopLevelElement(p, SignedSupportingTokens.class);
            if (rem != null) {
                rem.getParent().removeExtensibilityElement(rem);
            }

            rem = PolicyModelHelper.getTopLevelElement(p, EndorsingSupportingTokens.class);
            if (rem != null) {
                rem.getParent().removeExtensibilityElement(rem);
            }

            rem = PolicyModelHelper.getTopLevelElement(p, SignedEndorsingSupportingTokens.class);
            if (rem != null) {
                rem.getParent().removeExtensibilityElement(rem);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static WSDLComponent setSupportingTokens(WSDLComponent c, String authToken, int supportingType) {
        if (c == null) return null;
        
        WSDLModel model = c.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        WSDLComponent tokenType = null;
        WSDLComponent tokenKind = null;

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            for (int i=0; i < 4; i++) {
                tokenKind = getSupportingToken(c, i);
                if (tokenKind != null) {
                    if (ComboConstants.NONE.equals(authToken) || (authToken == null)) { 
                        if ((i == supportingType) || (supportingType == NONE)) {
                            tokenKind.getParent().removeExtensibilityElement((ExtensibilityElement) tokenKind);
                        }
                        if (supportingType != NONE) return null;
                    } else {
                        if (i == supportingType) {
                            tokenKind.getParent().removeExtensibilityElement((ExtensibilityElement) tokenKind);
                        }
                    }
                }
            }
            
            if (supportingType == NONE) return null;
            
            WSDLComponent topLevel = null;
            if (c instanceof Policy) {
                topLevel = c;
            } else {
                topLevel = PolicyModelHelper.createPolicy(c, true);
            }
        
            if (SUPPORTING == supportingType) {
                tokenKind = wcf.create(topLevel, TokensQName.SUPPORTINGTOKENS.getQName());
            }
            if (SIGNED_SUPPORTING == supportingType) {
                tokenKind = wcf.create(topLevel, TokensQName.SIGNEDSUPPORTINGTOKENS.getQName());
            }
            if (ENDORSING == supportingType) {
                tokenKind = wcf.create(topLevel, TokensQName.ENDORSINGSUPPORTINGTOKENS.getQName());
            }
            if (SIGNED_ENDORSING == supportingType) {
                tokenKind = wcf.create(topLevel, TokensQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName());
            }
            topLevel.addExtensibilityElement((ExtensibilityElement) tokenKind);

            if (ComboConstants.USERNAME.equals(authToken)) {
                tokenType = PolicyModelHelper.createElement(tokenKind, TokensQName.USERNAMETOKEN.getQName(), UsernameToken.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.WSS10);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.X509.equals(authToken)) {
                tokenType = PolicyModelHelper.createElement(tokenKind, TokensQName.X509TOKEN.getQName(), X509Token.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.X509_V310);
//                SecurityPolicyModelHelper.enableRequireThumbprintReference(tokenType, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.SAML.equals(authToken)) {
                tokenType = PolicyModelHelper.createElement(tokenKind, TokensQName.SAMLTOKEN.getQName(), SamlToken.class, true);
                setTokenProfileVersion(tokenType, ComboConstants.SAML_V1110);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.SECURECONVERSATION.equals(authToken)) {
                tokenType = PolicyModelHelper.createElement(tokenKind, TokensQName.SECURECONVERSATIONTOKEN.getQName(), SecureConversationToken.class, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);
            }
            if (ComboConstants.ISSUED.equals(authToken)) {
                tokenType = PolicyModelHelper.createElement(tokenKind, TokensQName.ISSUEDTOKEN.getQName(), IssuedToken.class, true);
                setTokenInclusionLevel(tokenType, ComboConstants.ALWAYSRECIPIENT);

                RequestSecurityTokenTemplate template = 
                        (RequestSecurityTokenTemplate) wcf.create(tokenType, SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName());
                tokenType.addExtensibilityElement(template);
                
                TokenType trustTokenType = PolicyModelHelper.createElement(template, TrustQName.TOKENTYPE.getQName(), TokenType.class, false);
                trustTokenType.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);
                
                KeyType trustKeyType = PolicyModelHelper.createElement(template, TrustQName.KEYTYPE.getQName(), KeyType.class, false);
                trustKeyType.setContent(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);

                KeySize trustKeySize = PolicyModelHelper.createElement(template, TrustQName.KEYSIZE.getQName(), KeySize.class, false);
                trustKeySize.setContent(ComboConstants.ISSUED_KEYSIZE_256);

                SecurityPolicyModelHelper.enableRequireInternalReference(tokenType, true);            
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        return tokenType;
    }    
    
    public static String getIssuedIssuerAddress(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<Issuer> issuerList = tokenType.getExtensibilityElements(Issuer.class);
            if ((issuerList != null) && (!issuerList.isEmpty())) {
                Issuer issuer = issuerList.get(0);
                List<Address10> addrs = issuer.getExtensibilityElements(Address10.class);
                if ((addrs != null) && (!addrs.isEmpty())) {
                    Address10 a = addrs.get(0);
                    if (a != null) {
                        return a.getAddress();
                    }
                }
            }
        }
        return null;
    }

    public static String getIssuedTokenType(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                TokenType tType = rst.getTokenType();
                if (tType != null) {
                    String type = tType.getContent();
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML10;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML11;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML20;
                    }   
                }
            }
        }
        return null;
    }

    public static String getIssuedKeyType(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                KeyType kType = rst.getKeyType();
                if (kType != null) {
                    String type = kType.getContent();
                    if (ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_KEYTYPE_PUBLIC;
                    }
                    if (ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR.equals(type)) {
                        return ComboConstants.ISSUED_KEYTYPE_SYMMETRIC;
                    }
                }
            }
        }
        return null;
    }

    public static String getIssuedKeySize(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<RequestSecurityTokenTemplate> rstList = tokenType.getExtensibilityElements(RequestSecurityTokenTemplate.class);
            if ((rstList != null) && (!rstList.isEmpty())) {
                RequestSecurityTokenTemplate rst = rstList.get(0);
                KeySize kSize = rst.getKeySize();
                if (kSize != null) {
                    return kSize.getContent();
                }
            }
        }
        return null;
    }
    
    public static String getIssuedIssuerMetadataAddress(WSDLComponent tokenType) {
        if (tokenType != null) {
            List<Issuer> issuerList = tokenType.getExtensibilityElements(Issuer.class);
            if ((issuerList != null) && (!issuerList.isEmpty())) {
                Issuer issuer = issuerList.get(0);
                List<Addressing10Metadata> amdatas = issuer.getExtensibilityElements(Addressing10Metadata.class);
                if ((amdatas != null) && (!amdatas.isEmpty())) {
                    Addressing10Metadata amdata = amdatas.get(0);
                    List<Metadata> mdata = amdata.getExtensibilityElements(Metadata.class);
                    if ((mdata != null) && (!mdata.isEmpty())) {
                        Metadata m = mdata.get(0);
                        if (m != null) {
                            MetadataSection ms = m.getMetadataSection();
                            if (ms != null) {
                                MetadataReference mr = ms.getMetadataReference();
                                if (mr != null) {
                                    Address10 a = mr.getAddress();
                                    if (a != null) {
                                        return a.getAddress();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void setIssuedTokenAddressAttributes(WSDLComponent token, String address, String metaAddress) {
        WSDLModel model = token.getModel();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            Issuer i = PolicyModelHelper.createElement(token, TokensQName.ISSUER.getQName(), Issuer.class, false);
            Address10 a = PolicyModelHelper.createElement(i, Addressing10QName.ADDRESS.getQName(), Address10.class, false);
            a.setAddress(address);

            Addressing10Metadata am = PolicyModelHelper.createElement(i, 
                    Addressing10QName.ADDRESSINGMETADATA.getQName(), Addressing10Metadata.class, false);
            Metadata m = PolicyModelHelper.createElement(am, MexQName.METADATA.getQName(), Metadata.class, false);
            MetadataSection ms = PolicyModelHelper.createElement(m, MexQName.METADATASECTION.getQName(), MetadataSection.class, false);
            MetadataReference mr = PolicyModelHelper.createElement(ms, MexQName.METADATAREFERENCE.getQName(), MetadataReference.class, false);
            Address10 ma = PolicyModelHelper.createElement(mr, Addressing10QName.ADDRESS.getQName(), Address10.class, false);
            ma.setAddress(metaAddress);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setIssuedTokenRSTAttributes(WSDLComponent token, String tokenType, String keyType, String keySize) {
        WSDLModel model = token.getModel();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            RequestSecurityTokenTemplate rst = PolicyModelHelper.createElement(token, 
                    SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(), 
                    RequestSecurityTokenTemplate.class, false);
            
            TokenType t = PolicyModelHelper.createElement(rst, TrustQName.TOKENTYPE.getQName(), TokenType.class, false); 
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML20)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR);
            }
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML11)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR);
            }
            if (tokenType.equals(ComboConstants.ISSUED_TOKENTYPE_SAML10)) {
                t.setContent(ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR);
            }

            KeyType k = PolicyModelHelper.createElement(rst, TrustQName.KEYTYPE.getQName(), KeyType.class, false);
            if (keyType.equals(ComboConstants.ISSUED_KEYTYPE_PUBLIC)) {
                k.setContent(ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR);
            }
            if (keyType.equals(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC)) {
                k.setContent(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR);
            }

            KeySize s = PolicyModelHelper.createElement(rst, TrustQName.KEYSIZE.getQName(), KeySize.class, false);
            s.setContent(keySize);
            
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
}
