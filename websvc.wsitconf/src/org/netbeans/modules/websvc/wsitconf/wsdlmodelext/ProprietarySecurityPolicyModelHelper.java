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

import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.ServiceProviderElement;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Timestamp;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Validator;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ValidatorConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.CertAlias;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.Contract;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.Issuer;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.KeyType;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySCServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySecurityPolicyServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietaryTrustServiceQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.SCConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ServiceProviders;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.TokenType;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandlerConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.KeyStore;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.LifeTime;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.PreconfiguredSTS;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySCClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryTrustClientQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.SCClientConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.TrustStore;
import org.netbeans.modules.xml.wsdl.model.*;
import java.util.List;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.DisableStreamingSecurity;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ServiceProvider;

/**
 *
 * @author Martin Grebac
 */
public class ProprietarySecurityPolicyModelHelper {

    public static final String DEFAULT_LIFETIME = "300000";                     //NOI18N
    public static final String DEFAULT_CONTRACT_CLASS = "com.sun.xml.ws.trust.impl.IssueSamlTokenContractImpl"; //NOI18N
    public static final String DEFAULT_HANDLER_TIMESTAMP_TIMEOUT = "300";                     //NOI18N
    public static final String DEFAULT_MAXCLOCKSKEW = "300000";                     //NOI18N
    public static final String DEFAULT_TIMESTAMPFRESHNESS = "300000";                     //NOI18N
    
    /**
     * Creates a new instance of ProprietarySecurityPolicyModelHelper
     */
    public ProprietarySecurityPolicyModelHelper() {
    }
   
//    public static String getMaxNonceAge(Binding b, boolean client) {
//        WSDLModel model = b.getModel();
//        Policy p = PolicyModelHelper.getPolicyForElement(b);
//        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
//        if (vc != null) {
//            return vc.getMaxNonceAge();
//        }
//        return null;
//    }

    public static String getSTSLifeTime(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<LifeTime> elems = sc.getExtensibilityElements(LifeTime.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getLifeTime();
            }
        }
        return null;
    }

    public static String getSTSIssuer(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<Issuer> elems = sc.getExtensibilityElements(Issuer.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getIssuer();
            }
        }
        return null;
    }
    
    public static STSConfiguration getSTSConfiguration(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        return getSTSConfiguration(p);
    }
    
    public static String getSTSContractClass(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent sc = getSTSConfiguration(p);
        if (sc != null) {
            List<Contract> elems = sc.getExtensibilityElements(Contract.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getContract();
            }
        }
        return null;
    }

    public static boolean getSTSEncryptKey(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        STSConfiguration sc = getSTSConfiguration(p);
        if (sc != null) {
            return sc.isEncryptIssuedKey();
        }
        return false;
    }
    
    public static boolean getSTSEncryptToken(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        STSConfiguration sc = getSTSConfiguration(p);
        if (sc != null) {
            return sc.isEncryptIssuedToken();
        }
        return false;
    }

    public static String getSPCertAlias(ServiceProvider sp) {
        if (sp != null) {
            List<CertAlias> elems = sp.getExtensibilityElements(CertAlias.class);
            if ((elems != null) && !(elems.isEmpty())) {
                return elems.get(0).getCertAlias();
            }
        }
        return null;
    }

    public static String getSPTokenType(ServiceProvider sp) {
        if (sp != null) {
            List<TokenType> elems = sp.getExtensibilityElements(TokenType.class);
            if ((elems != null) && !(elems.isEmpty())) {
                String tType = elems.get(0).getTokenType();
                if (tType != null) {
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR.equals(tType)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML10;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR.equals(tType)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML11;
                    }
                    if (ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR.equals(tType)) {
                        return ComboConstants.ISSUED_TOKENTYPE_SAML20;
                    }   
                }
            }
        }
        return null;
    }

    public static String getSPKeyType(ServiceProvider sp) {
        if (sp != null) {
            List<KeyType> elems = sp.getExtensibilityElements(KeyType.class);
            if ((elems != null) && !(elems.isEmpty())) {
                String kType = elems.get(0).getKeyType();
                if (kType != null) {
                    if (ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR.equals(kType)) {
                        return ComboConstants.ISSUED_KEYTYPE_PUBLIC;
                    }
                    if (ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR.equals(kType)) {
                        return ComboConstants.ISSUED_KEYTYPE_SYMMETRIC;
                    }
                }
            }
        }
        return null;
    }
    
    public static List<ServiceProvider> getSTSServiceProviders(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent sc = getSTSConfiguration(p);
        return getSTSServiceProviders((STSConfiguration) sc);
    }

    public static List<ServiceProvider> getSTSServiceProviders(STSConfiguration stsConfig) {
        if (stsConfig != null) {
            List<ServiceProviders> elems = stsConfig.getExtensibilityElements(ServiceProviders.class);
            if ((elems != null) && (!elems.isEmpty())) {
                List<ServiceProvider> sps = elems.get(0).getExtensibilityElements(ServiceProvider.class);
                return sps;
            }
        }
        return null;
    }

    public static String getLifeTime(Binding b, boolean client) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent sc = null;
        if (client) { 
            sc = getSCClientConfiguration(p);
        } else {
            sc = getSCConfiguration(p);
        }
        if (sc != null) {
            List<LifeTime> elems = sc.getExtensibilityElements(LifeTime.class);
            if ((elems != null) && (!elems.isEmpty())) {
                return elems.get(0).getLifeTime();
            }
        }
        return null;
    }

    public static String getTimestampTimeout(WSDLComponent b, boolean client) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        Timestamp t = PolicyModelHelper.getTopLevelElement(p, Timestamp.class);
        return t == null ? null : t.getTimeout();
    }
       
    public static String getPreSTSEndpoint(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getEndpoint();
        }
        return null;
    }

    public static String getPreSTSMetadata(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getMetadata();
        }
        return null;
    }

    public static String getPreSTSNamespace(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getNamespace();
        }
        return null;
    }

    public static String getPreSTSPortName(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getPortName();
        }
        return null;
    }

    public static String getPreSTSServiceName(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getServiceName();
        }
        return null;
    }

    public static String getPreSTSWsdlLocation(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps != null) {
            return ps.getWsdlLocation();
        }
        return null;
    }
    
    public static boolean isRenewExpired(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if (sc != null) {
            return sc.isRenewExpiredSCT();
        }
        return false;
    }

    public static boolean isRequireCancel(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if (sc != null) {
            return sc.isRequireCancelSCT();
        }
        return false;
    }

    public static boolean isRevocationEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        ValidatorConfiguration vc = (ValidatorConfiguration) 
                PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        if (vc != null) {
            return vc.isRevocationEnabled();
        }
        return false;
    }
    
    public static boolean isStreamingSecurity(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        DisableStreamingSecurity streaming = PolicyModelHelper.getTopLevelElement(p, DisableStreamingSecurity.class);
        return (streaming == null) ? true : false;
    }
    
    public static void setStreamingSecurity(Binding b, boolean enable) {
        if (enable == isStreamingSecurity(b)) return;
        if (!enable) {
            All a = PolicyModelHelper.createPolicy(b, true);
            PolicyModelHelper.createElement(a, 
                    ProprietarySecurityPolicyServiceQName.DISABLESTREAMINGSECURITY.getQName(), 
                    DisableStreamingSecurity.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            DisableStreamingSecurity streaming = PolicyModelHelper.getTopLevelElement(p, DisableStreamingSecurity.class);
            if (streaming != null) {
                PolicyModelHelper.removeElement(streaming.getParent(), DisableStreamingSecurity.class, false);
            }
            PolicyModelHelper.cleanPolicies(b);
        }
    }
    
    public static String getMaxClockSkew(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        if (vc != null) {
            return vc.getMaxClockSkew();
        }
        return null;
    }
    
    public static String getTimestampFreshness(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        if (vc != null) {
            return vc.getTimestampFreshnessLimit();
        }
        return null;
    }

    public static WSDLComponent getStore(Policy p, boolean trust) {
        if (trust) {
            return PolicyModelHelper.getTopLevelElement(p, TrustStore.class);
        } else {
            return PolicyModelHelper.getTopLevelElement(p, KeyStore.class);
        }
    }

    public static CallbackHandlerConfiguration getCBHConfiguration(Binding b) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
        return chc;
    }
    
    public static SCClientConfiguration getSCClientConfiguration(Policy p) {
            return (SCClientConfiguration) PolicyModelHelper.getTopLevelElement(p, SCClientConfiguration.class);
    }
    
    public static PreconfiguredSTS getPreconfiguredSTS(Policy p) {
            return (PreconfiguredSTS) PolicyModelHelper.getTopLevelElement(p, PreconfiguredSTS.class);
    }

    public static STSConfiguration getSTSConfiguration(Policy p) {
            return (STSConfiguration) PolicyModelHelper.getTopLevelElement(p, STSConfiguration.class);
    }

    public static SCConfiguration getSCConfiguration(Policy p) {
            return (SCConfiguration) PolicyModelHelper.getTopLevelElement(p, SCConfiguration.class);
    }

    /** Gets store location value for specified Binding or BindingOperation 
     */
    public static String getStoreLocation(WSDLComponent b, boolean trust) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, true);
            return ts == null ? null : ts.getLocation();
        } else {
            KeyStore ks = (KeyStore) getStore(p, false);
            return ks == null ? null : ks.getLocation();
        }
    }

    public static String getStoreAlias(WSDLComponent b, boolean trust) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, trust);
            return (ts != null) ? ts.getPeerAlias() : null;
        } else {
            KeyStore ks = (KeyStore) getStore(p, trust);
            return (ks != null) ? ks.getAlias() : null;
        }
    }

    public static String getStoreType(WSDLComponent b, boolean trust) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, trust);
            return (ts != null) ? ts.getType() : null;
        } else {
            KeyStore ks = (KeyStore) getStore(p, trust);
            return (ks != null) ? ks.getType() : null;
        }
    }
    
    public static String getTrustSTSAlias(Binding b) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        TrustStore ks = (TrustStore) getStore(p, true);
        return (ks != null) ? ks.getSTSAlias() : null;
    }

    public static String getTrustPeerAlias(WSDLComponent b) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        TrustStore ks = (TrustStore) getStore(p, true);
        return (ks != null) ? ks.getPeerAlias() : null;
    }

    public static String getValidator(WSDLComponent c, String validatorType) {
        if (c == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        if (p == null) return null;
        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        Validator v = getValidator(validatorType, vc);
        if (v != null) {
            return v.getClassname();
        }
        return null;
    }
    
    private static Validator getValidator(String type, ValidatorConfiguration vc) {
        if (vc == null) return null;
        List<Validator> validators = vc.getExtensibilityElements(Validator.class);
        for (Validator v : validators) {
            if (type.equals(v.getName())) {
                return v;
            }
        }
        return null;
    }

    private static LifeTime getLifeTime(WSDLComponent c) {
        if (c != null) {
            List<LifeTime> attrs = c.getExtensibilityElements(LifeTime.class);
            if ((attrs != null) && !(attrs.isEmpty())) {
                return attrs.get(0);
            }
        }
        return null;
    }

    private static Issuer getIssuer(WSDLComponent c) {
        if (c != null) {
            List<Issuer> attrs = c.getExtensibilityElements(Issuer.class);
            if ((attrs != null) && !(attrs.isEmpty())) {
                return attrs.get(0);
            }
        }
        return null;
    }
    
    private static Contract getContract(WSDLComponent c) {
        if (c != null) {
            List<Contract> attrs = c.getExtensibilityElements(Contract.class);
            if ((attrs != null) && !(attrs.isEmpty())) {
                return attrs.get(0);
            }
        }
        return null;
    }
    
    public static String getDefaultUsername(Binding b) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
        CallbackHandler ch = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER, chc);
        if (ch != null) {
            return ch.getDefault();
        }
        return null;
    }

    public static String getDefaultPassword(Binding b) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
        CallbackHandler ch = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER, chc);
        if (ch != null) {
            return ch.getDefault();
        }
        return null;
    }
    
    public static String getCallbackHandler(Binding b, String cbhType) {
        if (b == null) return null;
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
        CallbackHandler ch = getCallbackHandler(cbhType, chc);
        if (ch != null) {
            return ch.getClassname();
        }
        return null;
    }
    
    private static CallbackHandler getCallbackHandler(String type, CallbackHandlerConfiguration vc) {
        if (vc == null) return null;
        List<CallbackHandler> handlers = vc.getExtensibilityElements(CallbackHandler.class);
        for (CallbackHandler h : handlers) {
            if (type.equals(h.getName())) {
                return h;
            }
        }
        return null;
    }

    public static String getKeyPassword(WSDLComponent b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        KeyStore ks = (KeyStore) getStore(p, false);
        return (ks == null) ? null : ks.getKeyPassword();
    }
    
    public static String getStorePassword(WSDLComponent b, boolean trust) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        if (trust) {
            TrustStore ts = (TrustStore) getStore(p, trust);
            return (ts == null) ? null : ts.getStorePassword();
        } else {
            KeyStore ks = (KeyStore) getStore(p, trust);
            return (ks == null) ? null : ks.getStorePassword();
        }
    }

    public static String getAliasSelector(WSDLComponent b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        KeyStore ks = (KeyStore) getStore(p, false);
        return (ks == null) ? null : ks.getAliasSelector();
    }

    public static String getCertSelector(WSDLComponent b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p == null) return null;
        TrustStore ts = (TrustStore) getStore(p, true);
        return (ts == null) ? null : ts.getCertSelector();
    }
    
    public static void disableSTS(Binding b) {
        STSConfiguration stsConfig = getSTSConfiguration(b);
        if (stsConfig != null) {
            PolicyModelHelper.removeElement(stsConfig);
        }
        PolicyModelHelper.cleanPolicies(b);        
    }

    public static void enableSTS(Binding b) {
        setSTSContractClass(b, DEFAULT_CONTRACT_CLASS);
        setSTSLifeTime(b, DEFAULT_LIFETIME);
    }

    public static boolean isSTSEnabled(Binding b) {
        STSConfiguration stsConfig = getSTSConfiguration(b);
        return (stsConfig != null);
    }
    
    public static void setStoreLocation(WSDLComponent b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if (value == null) {
                    if (ks != null) {
                        PolicyModelHelper.removeElement(ks);
                    }
                    return;
                }
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setLocation(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if (value == null) {
                    if (ks != null) {
                        PolicyModelHelper.removeElement(ks);
                    }
                    return;
                }
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setLocation(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setAliasSelector(WSDLComponent b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            KeyStore ks = (KeyStore) getStore(p, false);
            if ((p == null) || (ks == null)) {
                ks = (KeyStore) createStore(b, false, client);
            }
            ks.setAliasSelector(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setCertSelector(WSDLComponent b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            TrustStore ts = (TrustStore) getStore(p, true);
            if ((p == null) || (ts == null)) {
                ts = (TrustStore) createStore(b, true, client);
            }
            ts.setCertSelector(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setKeyStoreAlias(WSDLComponent b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        KeyStore ks = (KeyStore) getStore(p, false);
        if ((p == null) || (ks == null)) {
            ks = (KeyStore) createStore(b, false, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setAlias(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setTrustPeerAlias(WSDLComponent b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        TrustStore ks = (TrustStore) getStore(p, true);
        if ((p == null) || (ks == null)) {
            ks = (TrustStore) createStore(b, true, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ks.setPeerAlias(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setValidator(WSDLComponent c, String type, String value, boolean client) {
        WSDLModel model = c.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(c);        
        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        
        if (value == null) {
            if (vc != null) {
                Validator v = getValidator(type, vc);
                if (v != null) {
                    PolicyModelHelper.removeElement(v);
                }
            }
            return;
        }
        
        if ((p == null) || (vc == null)) {
            vc = createValidatorConfiguration(c, client);
        }
        Validator v = getValidator(type, vc);
        if (v == null) {
            v = createValidator(vc, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            v.setName(type);
            v.setClassname(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setLifeTime(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        WSDLComponent c = client ? getSCClientConfiguration(p) : getSCConfiguration(p);
        
        if (value == null) {
            if (c != null) {
                LifeTime lt = getLifeTime(c);
                if (lt != null) {
                    PolicyModelHelper.removeElement(lt);
                }
            }
            return;
        }
        
        if ((p == null) || (c == null)) {
            c = createSCConfiguration(b, client);
        }
        LifeTime lt = getLifeTime(c);
        if (lt == null) {
            lt = createLifeTime(c, client);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            lt.setLifeTime(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static String getHandlerTimestampTimeout(Binding b) {
        CallbackHandlerConfiguration chc = getCBHConfiguration(b);
        if (chc != null) {
            return chc.getTimestampTimeout();
        }
        return null;
    }    

    public static void setHandlerTimestampTimeout(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
            if (((p == null) || (chc == null)) && value != null) {
                chc = createCallbackHandlerConfiguration(b, client);
            }
            if (chc != null) {
                chc.setTimestampTimeout(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setTimestampTimeout(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        WSDLComponent c = PolicyModelHelper.getTopLevelElement(p, Timestamp.class);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if ((value == null) && (c != null)) {
                PolicyModelHelper.removeElement(c);
            } else {
                if ((p == null) || (c == null)) {
                    c = createTimestamp(b, client);
                }
                ((Timestamp)c).setTimeout(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setSTSLifeTime(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        STSConfiguration c = getSTSConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSTSConfiguration(b);
        }
        LifeTime lt = getLifeTime(c);
        if (lt == null) {
            lt = createSTSLifeTime(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            lt.setLifeTime(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setSTSIssuer(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        STSConfiguration c = getSTSConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSTSConfiguration(b);
        }
        Issuer i = getIssuer(c);
        if (i == null) {
            i = createSTSIssuer(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            i.setIssuer(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void addSTSServiceProvider(STSConfiguration stsConfig, ServiceProviderElement spe) {
        if ((stsConfig == null) || (spe == null)) return;
        WSDLModel model = stsConfig.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            WSDLComponentFactory wcf = model.getFactory();
            List<ServiceProviders> sProvidersList = stsConfig.getExtensibilityElements(ServiceProviders.class);
            ServiceProviders sProviders = null;
            if ((sProvidersList != null) && (!sProvidersList.isEmpty())) {
               sProviders = sProvidersList.get(0); 
            }
            if (sProviders == null) {
                sProviders = (ServiceProviders)wcf.create(stsConfig, ProprietaryTrustServiceQName.SERVICEPROVIDERS.getQName());
                stsConfig.addExtensibilityElement(sProviders);
            }
            ServiceProvider sp = (ServiceProvider)wcf.create(sProviders, ProprietaryTrustServiceQName.SERVICEPROVIDER.getQName());
            sProviders.addExtensibilityElement(sp);
            sp.setEndpoint(spe.getEndpoint());
            CertAlias calias = (CertAlias)wcf.create(sp, ProprietaryTrustServiceQName.CERTALIAS.getQName());
            sp.addExtensibilityElement(calias);
            calias.setCertAlias(spe.getCertAlias());
            
            TokenType ttype = (TokenType)wcf.create(sp, ProprietaryTrustServiceQName.TOKENTYPE.getQName());
            sp.addExtensibilityElement(ttype);

            String tTypePolicyStr = ComboConstants.ISSUED_TOKENTYPE_SAML11_POLICYSTR;
            
            String tTypeShort = spe.getTokenType();
            if (ComboConstants.ISSUED_TOKENTYPE_SAML20.equals(tTypeShort)) {
                tTypePolicyStr = ComboConstants.ISSUED_TOKENTYPE_SAML20_POLICYSTR;
            }
            if (ComboConstants.ISSUED_TOKENTYPE_SAML10.equals(tTypeShort)) {
                tTypePolicyStr = ComboConstants.ISSUED_TOKENTYPE_SAML10_POLICYSTR;
            }            
            ttype.setTokenType(tTypePolicyStr);
            
            KeyType ktype = (KeyType)wcf.create(sp, ProprietaryTrustServiceQName.KEYTYPE.getQName());
            sp.addExtensibilityElement(ktype);

            String kTypePolicyStr = ComboConstants.ISSUED_KEYTYPE_PUBLIC_POLICYSTR;
            
            String kTypeShort = spe.getKeyType();
            if (ComboConstants.ISSUED_KEYTYPE_SYMMETRIC.equals(kTypeShort)) {
                kTypePolicyStr = ComboConstants.ISSUED_KEYTYPE_SYMMETRIC_POLICYSTR;
            }
            ktype.setKeyType(kTypePolicyStr);
            
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void removeSTSServiceProvider(STSConfiguration stsConfig, ServiceProviderElement spe) {
        if ((spe == null) || (stsConfig == null)) return;
        WSDLModel model = stsConfig.getModel();
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            List<ServiceProvider> spList = getSTSServiceProviders(stsConfig);
            for (ServiceProvider sp : spList) {
                String ep = spe.getEndpoint();
                if (ep.equals(sp.getEndpoint())) {
                    if (sp.isInDocumentModel()) {
                        sp.getParent().removeExtensibilityElement(sp);
                    }
                    break;
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setSTSContractClass(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        STSConfiguration c = getSTSConfiguration(p);
        if ((p == null) || (c == null)) {
            c = createSTSConfiguration(b);
        }
        Contract contract = getContract(c);
        if (contract == null) {
            contract = createSTSContract(c);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            contract.setContract(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setSTSEncryptKey(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        STSConfiguration sc = getSTSConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = createSTSConfiguration(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setEncryptIssuedKey(enable);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    

    public static void setSTSEncryptToken(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        STSConfiguration sc = getSTSConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = createSTSConfiguration(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setEncryptIssuedToken(enable);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setRequireCancel(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (SCClientConfiguration) createSCConfiguration(b, true);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setRequireCancelSCT(enable);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setRenewExpired(Binding b, boolean enable) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        SCClientConfiguration sc = getSCClientConfiguration(p);
        if ((p == null) || (sc == null)) {
            sc = (SCClientConfiguration) createSCConfiguration(b, true);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            sc.setRenewExpiredSCT(enable);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void removeCallbackHandlerConfiguration(Binding b) {
        CallbackHandlerConfiguration cbh = getCBHConfiguration(b);
        if (cbh != null) {
            WSDLModel model = cbh.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                cbh.getParent().removeExtensibilityElement(cbh);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
    }
    
    public static void setCallbackHandler(Binding b, String type, String value, String defaultVal, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
            if ((p == null) || (chc == null)) {
                chc = createCallbackHandlerConfiguration(b, client);
            }
            CallbackHandler h = getCallbackHandler(type, chc);
            if (h == null) {
                h = createCallbackHandler(chc, client);
            }
            if ((defaultVal == null) && (value == null)) {
                chc.removeExtensibilityElement(h);
                if ((chc.getExtensibilityElements().size() == 0) && (chc.getTimestampTimeout() == null)) {
                    chc.getParent().removeExtensibilityElement(chc);
                }
            } else {           
                h.setDefault(defaultVal);
                h.setName(type);
                h.setClassname(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setStoreType(WSDLComponent b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setType(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setType(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setStorePassword(WSDLComponent b, String value, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (trust) {
                TrustStore ks = (TrustStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (TrustStore) createStore(b, trust, client);
                }
                ks.setStorePassword(value);
            } else {
                KeyStore ks = (KeyStore) getStore(p, trust);
                if ((p == null) || (ks == null)) {
                    ks = (KeyStore) createStore(b, trust, client);
                }
                ks.setStorePassword(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setKeyPassword(WSDLComponent b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            KeyStore ks = (KeyStore) getStore(p, false);
            if ((p == null) || (ks == null)) {
                ks = (KeyStore) createStore(b, false, client);
            }
            ks.setKeyPassword(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSEndpoint(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setEndpoint(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setPreSTSMetadata(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setMetadata(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSNamespace(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setNamespace(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSServiceName(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setServiceName(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setPreSTSPortName(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setPortName(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setPreSTSWsdlLocation(Binding b, String value) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if ((ps == null) || (p == null)) {
            ps = createPreconfiguredSTS(b);
        }
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ps.setWsdlLocation(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setMaxClockSkew(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
            if ((vc == null) || (p == null)) {
                vc = createValidatorConfiguration(b, client);
            }
            if (vc != null) {
                vc.setMaxClockSkew(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static void setRevocation(Binding b, boolean value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
            if (vc == null) {
                vc = createValidatorConfiguration(b, client);
            }
            if (vc != null) {
                vc.setRevocationEnabled(value);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static void setTimestampFreshness(Binding b, String value, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
            if ((vc == null) || (p == null)) {
                vc = createValidatorConfiguration(b, client);
            }
            vc.setTimestampFreshnessLimit(value);
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

//    public static void setMaxNonceAge(Binding b, String value, boolean client) {
//        WSDLModel model = b.getModel();
//        Policy p = PolicyModelHelper.getPolicyForElement(b);
//        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
//        if ((vc == null) || (p == null)) {
//            vc = createValidatorConfiguration(b, client);
//        }
//        boolean isTransaction = model.isIntransaction();
//        if (!isTransaction) {
//            model.startTransaction();
//        }
//        try {
//            vc.setMaxNonceAge(value);
//        } finally {
//            if (!isTransaction) {
//                model.endTransaction();
//            }
//        }
//    }
    
    public static WSDLComponent createStore(WSDLComponent b, boolean trust, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (trust) {
            TrustStore ks = (TrustStore) getStore(p, trust);
            if (ks == null) {
                boolean isTransaction = model.isIntransaction();
                if (!isTransaction) {
                    model.startTransaction();
                }
                try {
                    WSDLComponentFactory wcf = model.getFactory();
                    All all = PolicyModelHelper.createPolicy(b, !client);
                    if (client) {
                        ks = (TrustStore)wcf.create(all, ProprietarySecurityPolicyQName.TRUSTSTORE.getQName());
                    } else {
                        ks = (TrustStore)wcf.create(all, ProprietarySecurityPolicyServiceQName.TRUSTSTORE.getQName());
                    }
                    all.addExtensibilityElement(ks);
                    ks.setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } finally {
                    if (!isTransaction) {
                        model.endTransaction();
                    }
                }
                return ks;
            }
        } else {
            KeyStore ks = (KeyStore) getStore(p, trust);
            if (ks == null) {
                boolean isTransaction = model.isIntransaction();
                if (!isTransaction) {
                    model.startTransaction();
                }
                try {
                    WSDLComponentFactory wcf = model.getFactory();
                    All all = PolicyModelHelper.createPolicy(b,!client);
                    if (client) {
                        ks = (KeyStore)wcf.create(all, ProprietarySecurityPolicyQName.KEYSTORE.getQName());
                    } else {
                        ks = (KeyStore)wcf.create(all, ProprietarySecurityPolicyServiceQName.KEYSTORE.getQName());
                    }
                    all.addExtensibilityElement(ks);
                    ks.setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } finally {
                    if (!isTransaction) {
                        model.endTransaction();
                    }
                }
                return ks;
            }
        }
        return null;
    }
    
    public static ValidatorConfiguration createValidatorConfiguration(WSDLComponent c, boolean client) {
        WSDLModel model = c.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(c);
        ValidatorConfiguration vc = (ValidatorConfiguration) PolicyModelHelper.getTopLevelElement(p, ValidatorConfiguration.class);
        if (vc == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                All all = PolicyModelHelper.createPolicy(c, !client);
                if (client) {
                    vc = (ValidatorConfiguration)wcf.create(all, ProprietarySecurityPolicyQName.VALIDATORCONFIGURATION.getQName());
                } else {
                    vc = (ValidatorConfiguration)wcf.create(all, ProprietarySecurityPolicyServiceQName.VALIDATORCONFIGURATION.getQName());
                }
                all.addExtensibilityElement(vc);
                vc.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return vc;
    }

    public static PreconfiguredSTS createPreconfiguredSTS(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        PreconfiguredSTS ps = getPreconfiguredSTS(p);
        if (ps == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                All all = PolicyModelHelper.createPolicy(b, false);
                ps = (PreconfiguredSTS)wcf.create(all, ProprietaryTrustClientQName.PRECONFIGUREDSTS.getQName());
                all.addExtensibilityElement(ps);
                ps.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return ps;
    }

    public static STSConfiguration createSTSConfiguration(Binding b) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        STSConfiguration sts = getSTSConfiguration(p);
        if (sts == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = PolicyModelHelper.createPolicy(b, true);
                sts = (STSConfiguration)wcf.create(all, ProprietaryTrustServiceQName.STSCONFIGURATION.getQName());
                all.addExtensibilityElement(sts);
                sts.setVisibility(ProprietaryPolicyQName.INVISIBLE);
                sts.setEncryptIssuedKey(true);
                sts.setEncryptIssuedToken(false);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return sts;
    }
    
    public static WSDLComponent createSCConfiguration(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent c = client ? getSCClientConfiguration(p) : getSCConfiguration(p);
        if (c == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = PolicyModelHelper.createPolicy(b, !client);
                if (client) {
                    c = (SCClientConfiguration)wcf.create(all, ProprietarySCClientQName.SCCLIENTCONFIGURATION.getQName());
                    all.addExtensibilityElement((ExtensibilityElement) c);
                    ((SCClientConfiguration)c).setVisibility(ProprietaryPolicyQName.INVISIBLE);
                } else {
                    c = (SCConfiguration)wcf.create(all, ProprietarySCServiceQName.SCCONFIGURATION.getQName());
                    all.addExtensibilityElement((ExtensibilityElement) c);
                    ((SCConfiguration)c).setVisibility(ProprietaryPolicyQName.INVISIBLE);
                }
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return c;
    }

    public static WSDLComponent createTimestamp(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        WSDLComponent c = PolicyModelHelper.getTopLevelElement(p, Timestamp.class);
        if (c == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                All all = PolicyModelHelper.createPolicy(b, !client);
                if (client) {
                    c = (Timestamp)wcf.create(all, ProprietarySecurityPolicyQName.TIMESTAMP.getQName());
                } else {
                    c = (Timestamp)wcf.create(all, ProprietarySecurityPolicyServiceQName.TIMESTAMP.getQName());
                }
                all.addExtensibilityElement((ExtensibilityElement) c);
                ((Timestamp)c).setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return c;
    }
    
    public static Validator createValidator(ValidatorConfiguration vc, boolean client) {
        Validator v = null;
        if (vc != null) {
            WSDLModel model = vc.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                if (client) {
                    v = (Validator)wcf.create(vc, ProprietarySecurityPolicyQName.VALIDATOR.getQName());
                } else {
                    v = (Validator)wcf.create(vc, ProprietarySecurityPolicyServiceQName.VALIDATOR.getQName());
                }
                vc.addExtensibilityElement(v);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return v;
    }

    public static LifeTime createLifeTime(WSDLComponent c, boolean client) {
        LifeTime lt = null;
        if (c != null) {
            WSDLModel model = c.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                if (client) {
                    lt = (LifeTime)wcf.create(c, ProprietarySCClientQName.LIFETIME.getQName());
                } else {
                    lt = (LifeTime)wcf.create(c, ProprietarySCServiceQName.LIFETIME.getQName());
                }
                c.addExtensibilityElement(lt);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return lt;
    }

    public static LifeTime createSTSLifeTime(WSDLComponent c) {
        LifeTime lt = null;
        if (c != null) {
            WSDLModel model = c.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                lt = (LifeTime)wcf.create(c, ProprietaryTrustServiceQName.LIFETIME.getQName());
                c.addExtensibilityElement(lt);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return lt;
    }

    public static Issuer createSTSIssuer(WSDLComponent c) {
        Issuer i = null;
        if (c != null) {
            WSDLModel model = c.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                i = (Issuer)wcf.create(c, ProprietaryTrustServiceQName.ISSUER.getQName());
                c.addExtensibilityElement(i);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return i;
    }

    public static Contract createSTSContract(WSDLComponent c) {
        Contract contract = null;
        if (c != null) {
            WSDLModel model = c.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                contract = (Contract)wcf.create(c, ProprietaryTrustServiceQName.CONTRACT.getQName());
                c.addExtensibilityElement(contract);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return contract;
    }
    
    public static CallbackHandlerConfiguration createCallbackHandlerConfiguration(Binding b, boolean client) {
        WSDLModel model = b.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        CallbackHandlerConfiguration chc = (CallbackHandlerConfiguration) PolicyModelHelper.getTopLevelElement(p, CallbackHandlerConfiguration.class);
        if (chc == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = PolicyModelHelper.createPolicy(b, !client);
                if (client) {
                    chc = (CallbackHandlerConfiguration)wcf.create(all, 
                            ProprietarySecurityPolicyQName.CALLBACKHANDLERCONFIGURATION.getQName());
                } else {
                    chc = (CallbackHandlerConfiguration)wcf.create(all, 
                            ProprietarySecurityPolicyServiceQName.CALLBACKHANDLERCONFIGURATION.getQName());
                }
                all.addExtensibilityElement(chc);
                chc.setVisibility(ProprietaryPolicyQName.INVISIBLE);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return chc;
    }

    public static CallbackHandler createCallbackHandler(CallbackHandlerConfiguration chc, boolean client) {
        CallbackHandler h = null;
        if (chc != null) {
            WSDLModel model = chc.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();
                if (client) {
                    h = (CallbackHandler)wcf.create(chc, ProprietarySecurityPolicyQName.CALLBACKHANDLER.getQName());
                } else {
                    h = (CallbackHandler)wcf.create(chc, ProprietarySecurityPolicyServiceQName.CALLBACKHANDLER.getQName());
                }
                chc.addExtensibilityElement(h);
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
        }
        return h;
    }
        
}
