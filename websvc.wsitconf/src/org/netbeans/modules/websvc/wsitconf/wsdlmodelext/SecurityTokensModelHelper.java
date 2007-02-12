/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.util.List;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Address;
import org.netbeans.modules.websvc.wsitmodelext.addressing.AddressingQName;
import org.netbeans.modules.websvc.wsitmodelext.mex.Metadata;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataReference;
import org.netbeans.modules.websvc.wsitmodelext.mex.MetadataSection;
import org.netbeans.modules.websvc.wsitmodelext.mex.MexQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
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
        return token.getRequireClientCertificate();
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
            incLevelStr = incLevelStr.substring(incLevelStr.lastIndexOf("/")+1, incLevelStr.length()); //NOI18N
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
            List<WSDLComponent> ptokens = p.getExtensibilityElements(tokenClass);
            if ((ptokens != null) && (!ptokens.isEmpty())) {
                return ptokens.get(0);
            }
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
        WSDLComponentFactory wcf = model.getFactory();
        
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
                    tokenKind.getParent().removeExtensibilityElement((ExtensibilityElement) tokenKind);
                }
            }

            if (supportingType == NONE) return null;
            
            All all = PolicyModelHelper.createPolicy(c);
            if (SUPPORTING == supportingType) {
                tokenKind = wcf.create(all, TokensQName.SUPPORTINGTOKENS.getQName());
            }
            if (SIGNED_SUPPORTING == supportingType) {
                tokenKind = wcf.create(all, TokensQName.SIGNEDSUPPORTINGTOKENS.getQName());
            }
            if (ENDORSING == supportingType) {
                tokenKind = wcf.create(all, TokensQName.ENDORSINGSUPPORTINGTOKENS.getQName());
            }
            if (SIGNED_ENDORSING == supportingType) {
                tokenKind = wcf.create(all, TokensQName.SIGNEDENDORSINGSUPPORTINGTOKENS.getQName());
            }
            all.addExtensibilityElement((ExtensibilityElement) tokenKind);

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
                List<Address> addrs = issuer.getExtensibilityElements(Address.class);
                if ((addrs != null) && (!addrs.isEmpty())) {
                    Address a = addrs.get(0);
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
                List<Metadata> mdata = issuer.getExtensibilityElements(Metadata.class);
                if ((mdata != null) && (!mdata.isEmpty())) {
                    Metadata m = mdata.get(0);
                    if (m != null) {
                        MetadataSection ms = m.getMetadataSection();
                        if (ms != null) {
                            MetadataReference mr = ms.getMetadataReference();
                            if (mr != null) {
                                Address a = mr.getAddress();
                                if (a != null) {
                                    return a.getAddress();
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
        WSDLComponentFactory wcf = model.getFactory();

        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            Issuer i = PolicyModelHelper.createElement(token, TokensQName.ISSUER.getQName(), Issuer.class, false);
            Address a = PolicyModelHelper.createElement(i, AddressingQName.ADDRESS.getQName(), Address.class, false);
            a.setAddress(address);

            Metadata m = PolicyModelHelper.createElement(i, MexQName.METADATA.getQName(), Metadata.class, false);
            MetadataSection ms = PolicyModelHelper.createElement(m, MexQName.METADATASECTION.getQName(), MetadataSection.class, false);
            MetadataReference mr = PolicyModelHelper.createElement(ms, MexQName.METADATAREFERENCE.getQName(), MetadataReference.class, false);
            Address ma = PolicyModelHelper.createElement(mr, AddressingQName.ADDRESS.getQName(), Address.class, false);
            ma.setAddress(metaAddress);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setIssuedTokenRSTAttributes(WSDLComponent token, String tokenType, String keyType, String keySize) {
        WSDLModel model = token.getModel();
        WSDLComponentFactory wcf = model.getFactory();

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
