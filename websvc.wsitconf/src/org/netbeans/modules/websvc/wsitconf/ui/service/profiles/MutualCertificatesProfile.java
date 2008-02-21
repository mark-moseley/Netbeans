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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import java.awt.Dialog;
import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.features.ClientDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.spi.features.ServiceDefaultsFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Transport Security Profile definition
 *
 * @author Martin Grebac
 */
public class MutualCertificatesProfile extends ProfileBase 
        implements SecureConversationFeature,ClientDefaultsFeature,ServiceDefaultsFeature {
    
    public int getId() {
        return 20;
    }

    public String getDisplayName() {
        return ComboConstants.PROF_MUTUALCERT;
    }

    public String getDescription() {
        return ComboConstants.PROF_MUTUALCERT_INFO;
    }
    
    /**
     * Should return true if the profile is set on component, false otherwise
     */
    public boolean isCurrentProfile(WSDLComponent component) {
        return getDisplayName().equals(ProfilesModelHelper.getWSITSecurityProfile(component));
    }
    
    @Override()
    public void displayConfig(WSDLComponent component, UndoManager undoManager) {
        UndoCounter undoCounter = new UndoCounter();
        WSDLModel model = component.getModel();
        
        model.addUndoableEditListener(undoCounter);

        JPanel profConfigPanel = new MutualCertificates(component, this);
        DialogDescriptor dlgDesc = new DialogDescriptor(profConfigPanel, getDisplayName());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 
        if (dlgDesc.getValue() == DialogDescriptor.CANCEL_OPTION) {
            for (int i=0; i<undoCounter.getCounter();i++) {
                if (undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        }
        
        model.removeUndoableEditListener(undoCounter);
    }
    
    public void setServiceDefaults(WSDLComponent component, Project p) {
//        ProprietarySecurityPolicyModelHelper pmh = ProprietarySecurityPolicyModelHelper.getInstance(cfgVersion);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, false);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, false);
//        if (Util.isTomcat(p)) {
            String storeLoc = Util.getStoreLocation(p, false, false);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, storeLoc, false, false);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, false);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, Util.getDefaultPassword(p), false, false);
//        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, false);
    }    

    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project p) {
//        ProprietarySecurityPolicyModelHelper pmh = ProprietarySecurityPolicyModelHelper.getInstance(cfgVersion);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, false, true);
        ProprietarySecurityPolicyModelHelper.setStoreLocation(component, null, true, true);
        ProprietarySecurityPolicyModelHelper.removeCallbackHandlerConfiguration((Binding) component);
//        if (Util.isTomcat(p)) {
            String kstoreLoc = Util.getStoreLocation(p, false, true);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, kstoreLoc, false, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, false, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, Util.getDefaultPassword(p), false, true);

            String tstoreLoc = Util.getStoreLocation(p, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreLocation(component, tstoreLoc, true, true);
            ProprietarySecurityPolicyModelHelper.setStoreType(component, KeystorePanel.JKS, true, true);
            ProprietarySecurityPolicyModelHelper.setStorePassword(component, Util.getDefaultPassword(p), true, true);
//        }
        ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(component,ProfilesModelHelper.XWS_SECURITY_CLIENT, true);
        ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(component,ProfilesModelHelper.XWS_SECURITY_SERVER, true);
    }    
    
    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        if (ProprietarySecurityPolicyModelHelper.getCBHConfiguration((Binding) component) != null) {
            return false;
        }
        String keyAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String trustAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, true);
        String trustPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, true);
        String keyPasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        String keyLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String trustLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, true);        
        if (ProfilesModelHelper.XWS_SECURITY_CLIENT.equals(keyAlias) && 
            ProfilesModelHelper.XWS_SECURITY_SERVER.equals(trustAlias)) {
                String defPassword = Util.getDefaultPassword(p);
                String defKeyLocation = Util.getStoreLocation(p, false, true);
                String defTrustLocation = Util.getStoreLocation(p, true, true);
                if ((defPassword != null) && (defKeyLocation != null) && (defTrustLocation != null)) {
                    if ((defPassword.equals(keyPasswd)) && defPassword.equals(trustPasswd) &&
                        (defKeyLocation.equals(keyLoc)) && (defTrustLocation.equals(trustLoc))) {
                            return true;
                    }
                }
        }
        return false;
    }

    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        String storeAlias = ProprietarySecurityPolicyModelHelper.getStoreAlias(component, false);
        String storeLoc = ProprietarySecurityPolicyModelHelper.getStoreLocation(component, false);
        String storePasswd = ProprietarySecurityPolicyModelHelper.getStorePassword(component, false);
        if (ProfilesModelHelper.XWS_SECURITY_SERVER.equals(storeAlias)) {
            String defPassword = Util.getDefaultPassword(p);
            String defLocation = Util.getStoreLocation(p, false, false);
            if ((defPassword != null) && (defLocation != null)) {
                if ((defPassword.equals(storePasswd)) && 
                    (defLocation.equals(storeLoc))) {
                        return true;
                }
            }
        }
        return false;
    }

    public boolean isSecureConversation(WSDLComponent component) {
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(component);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);        
        return (protToken instanceof SecureConversationToken);
    }

    public void enableSecureConversation(WSDLComponent component, boolean enable) {
//        ProfilesModelHelper pmh = ProfilesModelHelper.getInstance(cfgVersion);
        ProfilesModelHelper.enableSecureConversation(component, enable);
    }
    
}
