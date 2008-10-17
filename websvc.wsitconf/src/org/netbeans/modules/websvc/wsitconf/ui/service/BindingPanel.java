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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.awt.Color;
import java.awt.Dialog;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.undo.UndoManager;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedRMPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.STSConfigServicePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.ValidatorsPanel;
import org.netbeans.modules.websvc.wsitconf.util.UndoCounter;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TransportModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.AdvancedSecurityPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KerberosConfigPanel;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AddressingModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMSequenceBinding;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWsStackProvider;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class BindingPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;
    private UndoManager undoManager;
    private Project project;
    private Service service;
    private JaxWsModel jaxwsmodel;

    private String oldProfile;

    private boolean doNotSync = false;

    private boolean inSync = false;
    private boolean isFromJava = true;

    private final Color RED = new java.awt.Color(255, 0, 0);
    private final Color REGULAR;

    private boolean updateServiceUrl = true;

    private SortedSet<ConfigVersion> supportedConfigVersions = new TreeSet<ConfigVersion>();

    public BindingPanel(SectionView view, Node node, Project p, Binding binding, UndoManager undoManager, JaxWsModel jaxwsmodel) {
        super(view);
        this.model = binding.getModel();
        this.project = p;
        this.node = node;
        this.undoManager = undoManager;
        this.binding = binding;
        this.jaxwsmodel = jaxwsmodel;
        
        initComponents();

        REGULAR = profileInfoField.getForeground();

        if (node != null) {
            service = node.getLookup().lookup(Service.class);
            if (service != null) {
                String wsdlUrl = service.getWsdlUrl();
                if (wsdlUrl != null) { // WS from WSDL
                    isFromJava = false;
                }
            }
        } else {
            isFromJava = false;
        }

        mtomChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        rmChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        orderedChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        securityChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileComboLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileInfoField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        profileInfoField.setFont(mtomChBox.getFont());
        stsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tcpChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        fiChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        devDefaultsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator1.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator2.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator3.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jSeparator4.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cfgVersionLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cfgVersionCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        addrChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());

        // detect and fill appropriate config options
        J2eePlatform platform = Util.getJ2eePlatform(project);
        WSStack<JaxWs> wsStack = platform == null ? null : JaxWsStackProvider.getJaxWsStack(platform);
        inSync = true;
        for (ConfigVersion cfgVersion : ConfigVersion.values()) {
            if ((wsStack == null) || (cfgVersion.isSupported(wsStack.getVersion()))) {
                supportedConfigVersions.add(cfgVersion);
                cfgVersionCombo.addItem(cfgVersion);
            }
        }
        inSync = false;

        String CONVERT = NbBundle.getMessage(BindingPanel.class, "LBL_Convert");
        String LEAVE = NbBundle.getMessage(BindingPanel.class, "LBL_LeaveIntact");
        String[] OPTIONS = new String[] {CONVERT, LEAVE};

        ConfigVersion configVersion = PolicyModelHelper.getWrittenConfigVersion(binding);
        if ((configVersion != null) && (!supportedConfigVersions.contains(configVersion))) {
            NotifyDescriptor dlgDesc = new NotifyDescriptor(
                NbBundle.getMessage(BindingPanel.class, "TXT_UnsupportedProfileDetected"),
                new NotifyDescriptor.Confirmation("test").getTitle(),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                OPTIONS, LEAVE);
            DialogDisplayer.getDefault().notify(dlgDesc);
            if (CONVERT.equals(dlgDesc.getValue())) {
                PolicyModelHelper.setConfigVersion(binding,
                    (ConfigVersion) supportedConfigVersions.toArray()[supportedConfigVersions.size() - 1],
                    project);
            } else if (LEAVE.equals(dlgDesc.getValue())) {
                cfgVersionCombo.addItem(configVersion);
                supportedConfigVersions.add(configVersion);
                enableDisable();
            } else {
                this.setVisible(false);
            }
        } else if (configVersion == null) {
            PolicyModelHelper.setConfigVersion(binding,
                (ConfigVersion) supportedConfigVersions.toArray()[supportedConfigVersions.size() - 1],
                project);
        }

        addImmediateModifier(cfgVersionCombo);
        addImmediateModifier(mtomChBox);
        addImmediateModifier(rmChBox);
        addImmediateModifier(orderedChBox);
        addImmediateModifier(securityChBox);
        addImmediateModifier(profileCombo);
        addImmediateModifier(stsChBox);
        addImmediateModifier(tcpChBox);
        addImmediateModifier(fiChBox);
        addImmediateModifier(devDefaultsChBox);
        addImmediateModifier(addrChBox);

        sync();

        if ((!isFromJava) &&
            (PolicyModelHelper.getPolicyUriForElement(binding) == null) &&
            (ProfilesModelHelper.isServiceUrlHttps(binding))) {
                updateServiceUrl = false;
        }

        model.addComponentListener(new ComponentListener() {
            public void valueChanged(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            public void childrenAdded(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
            public void childrenDeleted(ComponentEvent evt) {
                if (!doNotSync) {
                    sync();
                }
            }
        });
    }

    private void fillProfileCombo(boolean sts) {
        profileCombo.removeAllItems();
        Set<SecurityProfile> profiles = SecurityProfileRegistry.getDefault().getSecurityProfiles();
        for (SecurityProfile profile : profiles) {
            if (profile.isProfileSupported(project, binding, sts)) {
                profileCombo.addItem(profile.getDisplayName());
            }
        }
    }

    private ConfigVersion getUserExpectedConfigVersion() {
        return (ConfigVersion) cfgVersionCombo.getSelectedItem();
    }
    
    private void sync() {
        inSync = true; doNotSync = true;
        try {            
            ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(binding);
            cfgVersionCombo.setSelectedItem(configVersion);

            boolean addrEnabled = AddressingModelHelper.isAddressingEnabled(binding);
            setChBox(addrChBox, addrEnabled);

            boolean mtomEnabled = TransportModelHelper.isMtomEnabled(binding);
            setChBox(mtomChBox, mtomEnabled);

            boolean fiEnabled = TransportModelHelper.isFIEnabled(binding);
            setChBox(fiChBox, !fiEnabled);

            boolean tcpEnabled = TransportModelHelper.isTCPEnabled(binding);
            setChBox(tcpChBox, tcpEnabled);

            boolean rmEnabled = RMModelHelper.getInstance(configVersion).isRMEnabled(binding);
            setChBox(rmChBox, rmEnabled);
            setChBox(orderedChBox, RMModelHelper.getInstance(configVersion).isOrderedEnabled(binding));

            boolean stsEnabled = ProprietarySecurityPolicyModelHelper.isSTSEnabled(binding);
            setChBox(stsChBox, stsEnabled);

            fillProfileCombo(stsEnabled);

            boolean securityEnabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
            setChBox(securityChBox, securityEnabled);
            if (securityEnabled) {
                String profile = ProfilesModelHelper.getSecurityProfile(binding);
                setSecurityProfile(profile);
                boolean defaults = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
                setChBox(devDefaultsChBox, defaults);
                oldProfile = profile;
            } else {
                setSecurityProfile(ComboConstants.PROF_USERNAME);
                setChBox(devDefaultsChBox, true);
            }

            enableDisable();
        } finally {
            inSync = false; doNotSync = false;
        }
        refresh();
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;

        ConfigVersion userExpectedCfgVersion = getUserExpectedConfigVersion();
        if (source.equals(cfgVersionCombo)) {
            doNotSync = true;
            try {
                PolicyModelHelper.setConfigVersion(binding, userExpectedCfgVersion, project);
                try {
                    inSync = true;
                    fillProfileCombo(stsChBox.isSelected());
                } finally {
                    inSync = false;
                }
            } finally {
                doNotSync = false;
            }
            sync();
        }

        RMModelHelper rmh = RMModelHelper.getInstance(userExpectedCfgVersion);

        if (source.equals(rmChBox)) {
            boolean rm = rmh.isRMEnabled(binding);
            if (rmChBox.isSelected() != rm) {
                rmh.enableRM(binding, rmChBox.isSelected());
                if (securityChBox.isSelected()) {
                    if (!ProfilesModelHelper.isSCEnabled(binding)) {
                        ProfilesModelHelper.getInstance(userExpectedCfgVersion).setSecureConversation(binding, true);
                    }
                    if (ConfigVersion.CONFIG_1_3.equals(userExpectedCfgVersion) && rmChBox.isSelected()) {
                        String profile = ProfilesModelHelper.getSecurityProfile(binding);
                        if (ProfilesModelHelper.isSSLProfile(profile)) {
                            RMSequenceBinding.SECURED_TRANSPORT.set(userExpectedCfgVersion, binding);
                        } else {
                            RMSequenceBinding.SECURED_TOKEN.set(userExpectedCfgVersion, binding);
                        }
                    }
                }
            }
        }

        if (source.equals(orderedChBox)) {
            boolean ordered = rmh.isOrderedEnabled(binding);
            if (orderedChBox.isSelected() != ordered) {
                rmh.enableOrdered(binding, orderedChBox.isSelected());
            }
        }

        if (source.equals(mtomChBox)) {
            boolean mtom = TransportModelHelper.isMtomEnabled(binding);
            if (mtomChBox.isSelected() != mtom) {
                TransportModelHelper.enableMtom(binding, mtomChBox.isSelected());
            }
        }

        if (source.equals(fiChBox)) {
            boolean fi = TransportModelHelper.isFIEnabled(binding);
            if (!fiChBox.isSelected() != fi) { // fast infoset has a reverted meaning
                TransportModelHelper.enableFI(binding, !fiChBox.isSelected());
            }
        }

        if (source.equals(tcpChBox)) {
            boolean tcp = TransportModelHelper.isTCPEnabled(binding);
            if (tcpChBox.isSelected() != tcp) {
                boolean jsr109 = isJsr109Supported();
                TransportModelHelper.enableTCP(service, isFromJava, binding, project, tcpChBox.isSelected(), jsr109);
            }
        }

        if (source.equals(addrChBox)) {
            boolean addr = AddressingModelHelper.isAddressingEnabled(binding);
            if (addrChBox.isSelected() != addr) {
                if (addrChBox.isSelected()) { 
                    AddressingModelHelper.getInstance(getUserExpectedConfigVersion()).enableAddressing(binding, true);
                } else {
                    AddressingModelHelper.disableAddressing(binding);
                }
            }
        }

        if (source.equals(securityChBox)) {
            String profile = (String) profileCombo.getSelectedItem();
            if (securityChBox.isSelected()) {
                profileCombo.setSelectedItem(profile);
                if (devDefaultsChBox.isSelected()) {
                    Util.fillDefaults(project, false,true);
                    ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        ProfilesModelHelper.setSSLAttributes(binding);
                    }
                }
            } else {
                if (devDefaultsChBox.isSelected()) {
                    if (ProfilesModelHelper.isSSLProfile(profile)) {
                        ProfilesModelHelper.unsetSSLAttributes(binding);
                    }
                }
                Util.unfillDefaults(project);
                SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(getUserExpectedConfigVersion());
                spmh.disableSecurity(binding, true);
            }
            oldProfile = profile;
        }

        if (source.equals(devDefaultsChBox)) {
            if (devDefaultsChBox.isSelected()) {
                Util.fillDefaults(project, false,true);
                ProfilesModelHelper.setServiceDefaults((String) profileCombo.getSelectedItem(), binding, project);
            } else {
                Util.unfillDefaults(project);
            }
        }

        if (source.equals(stsChBox)) {
            if (stsChBox.isSelected() != ProprietarySecurityPolicyModelHelper.isSTSEnabled(binding)) {
                ProprietarySecurityPolicyModelHelper.getInstance(getUserExpectedConfigVersion()).
                        enableSTS(binding, stsChBox.isSelected());
                inSync = true; fillProfileCombo(true); inSync = false;
            }
        }

        if (source.equals(profileCombo)) {
            doNotSync = true;
            try {
                String profile = (String) profileCombo.getSelectedItem();
                ProfilesModelHelper.getInstance(getUserExpectedConfigVersion()).setSecurityProfile(binding, profile, oldProfile, updateServiceUrl);
                if (devDefaultsChBox.isSelected()) {
                    ProfilesModelHelper.setServiceDefaults(profile, binding, project);
                    if (ProfilesModelHelper.isSSLProfile(profile) && !ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        ProfilesModelHelper.setSSLAttributes(binding);
                    }
                    if (!ProfilesModelHelper.isSSLProfile(profile) && ProfilesModelHelper.isSSLProfile(oldProfile)) {
                        ProfilesModelHelper.unsetSSLAttributes(binding);
                    }
                }
                boolean defUsed = ProfilesModelHelper.isServiceDefaultSetupUsed(profile, binding, project);
                inSync = true; devDefaultsChBox.setSelected(defUsed); inSync = false;
                profileInfoField.setText(SecurityProfileRegistry.getDefault().getProfile(profile).getDescription());
                oldProfile = profile;
            } finally {
                doNotSync = false;
            }
        }

        enableDisable();
    }

    public Boolean getChBox(JCheckBox chBox) {
        if (chBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    // SECURITY PROFILE
    private void setSecurityProfile(String profile) {
        this.profileCombo.setSelectedItem(profile);
        SecurityProfile sp = SecurityProfileRegistry.getDefault().getProfile(profile);
        if (!ComboConstants.PROF_NOTRECOGNIZED.equals(profile)) {
            this.profileInfoField.setText(sp.getDescription());
        }
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        SectionView view = getSectionView();
        enableDisable();
        if (view != null) {
            view.getErrorPanel().clearError();
        }
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }

    @Override
    protected void endUIChange() { }

    public void linkButtonPressed(Object ddBean, String ddProperty) { }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return new JButton();
    }

    private void enableDisable() {

        cfgVersionCombo.setEnabled(cfgVersionCombo.getItemCount() > 1);
        cfgVersionLabel.setEnabled(cfgVersionCombo.getItemCount() > 1);

        boolean relSelected = rmChBox.isSelected();
        orderedChBox.setEnabled(relSelected);
        rmAdvanced.setEnabled(relSelected);

        tcpChBox.setEnabled(true);

        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        // everything is ok, disable security
        if (!amSec) {

            boolean gf = Util.isGlassfish(project);

            securityChBox.setEnabled(true);
            profileInfoField.setForeground(REGULAR);

            boolean secSelected = securityChBox.isSelected();

            profileComboLabel.setEnabled(secSelected);
            profileCombo.setEnabled(secSelected);
            profileInfoField.setEnabled(secSelected);

            boolean keyStoreConfigRequired = true;
            boolean trustStoreConfigRequired = true;
            boolean kerberosConfigRequired = false;

            boolean validatorsRequired = true;
            boolean stsAllowed = true;

            boolean defaults = devDefaultsChBox.isSelected();

            profConfigButton.setEnabled(secSelected);

            if (secSelected) {

                String secProfile = ProfilesModelHelper.getSecurityProfile(binding);

                boolean defaultsSupported = ProfilesModelHelper.isServiceDefaultSetupSupported(secProfile);
                if (!defaultsSupported) defaults = false;
                devDefaultsChBox.setEnabled(defaultsSupported);

                boolean isSSL = ProfilesModelHelper.isSSLProfile(secProfile);
                if (isSSL) {
                    keyStoreConfigRequired = false;
                    trustStoreConfigRequired = false;
                }
                if (ComboConstants.PROF_KERBEROS.equals(secProfile)) {
                    keyStoreConfigRequired = false;
                    trustStoreConfigRequired = false;
                    kerberosConfigRequired = true;
                }

                if (stsAllowed) {
                    if (ComboConstants.PROF_SAMLHOLDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSENDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSSL.equals(secProfile)) {
                            stsAllowed = false;
                    }
                }

                if (trustStoreConfigRequired && gf) {
                    if (ComboConstants.PROF_USERNAME.equals(secProfile) ||
                        ComboConstants.PROF_MUTUALCERT.equals(secProfile) ||
                        ComboConstants.PROF_ENDORSCERT.equals(secProfile) ||
                        ComboConstants.PROF_SAMLSENDER.equals(secProfile) ||
                        ComboConstants.PROF_SAMLHOLDER.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUED.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDCERT.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDSUPPORTING.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDENDORSE.equals(secProfile)
                        ) {
                            trustStoreConfigRequired = false;
                    }
                }

                if (validatorsRequired) {
                    if (ComboConstants.PROF_STSISSUED.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDCERT.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDSUPPORTING.equals(secProfile) ||
                        ComboConstants.PROF_STSISSUEDENDORSE.equals(secProfile)) {
                            validatorsRequired = false;
                    }
                }
            } else {
                devDefaultsChBox.setEnabled(false);
            }

            secAdvancedButton.setEnabled(secSelected && !defaults);

            stsChBox.setEnabled(secSelected && !isFromJava && stsAllowed);

            boolean stsSelected = stsChBox.isSelected();
            stsConfigButton.setEnabled(stsSelected);

            if (stsSelected) {
                trustStoreConfigRequired = true;
                keyStoreConfigRequired = true;
                validatorsRequired = true;
            }

            validatorsButton.setEnabled(secSelected && !(ConfigVersion.CONFIG_1_0.equals(getUserExpectedConfigVersion()) && gf) && !defaults && validatorsRequired);
            keyButton.setEnabled(secSelected && keyStoreConfigRequired && !defaults);
            trustButton.setEnabled(secSelected && trustStoreConfigRequired && !defaults);
            kerberosCfgButton.setEnabled(secSelected && kerberosConfigRequired && !defaults);

            addrChBox.setEnabled(!relSelected && !secSelected);

        } else { // no wsit fun, there's access manager security selected
            profileComboLabel.setEnabled(false);
            profileCombo.setEnabled(false);
            profileInfoField.setEnabled(false);
            profConfigButton.setEnabled(false);
            stsChBox.setEnabled(false);
            devDefaultsChBox.setEnabled(false);
            stsConfigButton.setEnabled(false);
            securityChBox.setEnabled(false);
            validatorsButton.setEnabled(false);
            keyButton.setEnabled(false);
            trustButton.setEnabled(false);
            profileInfoField.setEnabled(true);
            profileInfoField.setForeground(RED);
            profileInfoField.setText(NbBundle.getMessage(BindingPanel.class, "TXT_AMSecSelected"));
            addrChBox.setEnabled(false);
            cfgVersionLabel.setEnabled(false);
            cfgVersionCombo.setEnabled(false);
        }
    }

    private boolean isJsr109Supported(){
        J2eePlatform j2eePlatform = Util.getJ2eePlatform(project);
        if (j2eePlatform != null){
            Collection<WSStack> wsStacks = (Collection<WSStack>)
                    j2eePlatform.getLookup().lookupAll(WSStack.class);
            for (WSStack stack : wsStacks) {
                if (stack.isFeatureSupported(JaxWs.Feature.JSR109)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mtomChBox = new javax.swing.JCheckBox();
        rmChBox = new javax.swing.JCheckBox();
        securityChBox = new javax.swing.JCheckBox();
        orderedChBox = new javax.swing.JCheckBox();
        profileComboLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();
        rmAdvanced = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        stsChBox = new javax.swing.JCheckBox();
        tcpChBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        keyButton = new javax.swing.JButton();
        trustButton = new javax.swing.JButton();
        stsConfigButton = new javax.swing.JButton();
        profConfigButton = new javax.swing.JButton();
        fiChBox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        profileInfoField = new javax.swing.JTextArea();
        validatorsButton = new javax.swing.JButton();
        devDefaultsChBox = new javax.swing.JCheckBox();
        secAdvancedButton = new javax.swing.JButton();
        kerberosCfgButton = new javax.swing.JButton();
        cfgVersionLabel = new javax.swing.JLabel();
        cfgVersionCombo = new javax.swing.JComboBox();
        jSeparator4 = new javax.swing.JSeparator();
        addrChBox = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(mtomChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rmChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(securityChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(orderedChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox")); // NOI18N

        profileComboLabel.setLabelFor(profileCombo);
        org.openide.awt.Mnemonics.setLocalizedText(profileComboLabel, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel")); // NOI18N

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAML Sender Vouches With Certificates", "Anonymous with Bilateral Certificates" }));

        org.openide.awt.Mnemonics.setLocalizedText(rmAdvanced, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced")); // NOI18N
        rmAdvanced.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmAdvancedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(tcpChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keyButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton")); // NOI18N
        keyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(trustButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton")); // NOI18N
        trustButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(stsConfigButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton")); // NOI18N
        stsConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stsConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(profConfigButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton")); // NOI18N
        profConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profConfigButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fiChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox")); // NOI18N

        profileInfoField.setEditable(false);
        profileInfoField.setLineWrap(true);
        profileInfoField.setText("This is a text This is a text This is a text This is a text This is a text This is a text This is");
        profileInfoField.setWrapStyleWord(true);
        profileInfoField.setAutoscrolls(false);
        profileInfoField.setOpaque(false);
        jScrollPane1.setViewportView(profileInfoField);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(validatorsButton, bundle.getString("LBL_validatorsButton")); // NOI18N
        validatorsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatorsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(devDefaultsChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(secAdvancedButton, bundle.getString("LBL_Section_Service_Advanced")); // NOI18N
        secAdvancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secAdvancedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(kerberosCfgButton, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_kerberosCfgButton")); // NOI18N
        kerberosCfgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kerberosCfgButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cfgVersionLabel, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_versionChBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addrChBox, org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_addrChBox")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addrChBox)
                    .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(cfgVersionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cfgVersionCombo, 0, 344, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(mtomChBox)
                    .add(rmChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rmAdvanced)
                            .add(orderedChBox))
                        .add(79, 79, 79))
                    .add(securityChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(jScrollPane1))
                            .add(layout.createSequentialGroup()
                                .add(profileComboLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(profileCombo, 0, 234, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(profConfigButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(devDefaultsChBox))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(validatorsButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(secAdvancedButton))
                            .add(layout.createSequentialGroup()
                                .add(keyButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(trustButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(kerberosCfgButton)))
                        .add(109, 109, 109))
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(tcpChBox)
                    .add(layout.createSequentialGroup()
                        .add(stsChBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stsConfigButton))
                    .add(fiChBox)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cfgVersionLabel)
                    .add(cfgVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mtomChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(orderedChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmAdvanced)
                .add(8, 8, 8)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(profileComboLabel)
                    .add(profileCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(profConfigButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(devDefaultsChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(trustButton)
                    .add(keyButton)
                    .add(kerberosCfgButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(secAdvancedButton)
                    .add(validatorsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stsChBox)
                    .add(stsConfigButton))
                .add(11, 11, 11)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(tcpChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fiChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addrChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        mtomChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSN")); // NOI18N
        mtomChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSD")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSN")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSD")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSN")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSD")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSN")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSD")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSN")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSD")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSN")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSD")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSN")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSD")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSN")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSD")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSN")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSD")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSN")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSD")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSN")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSD")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSN")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSD")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSN")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSD")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSN")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSD")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    private void refresh() {

       org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addrChBox)
                    .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(cfgVersionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cfgVersionCombo, 0, 344, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(mtomChBox)
                    .add(rmChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rmAdvanced)
                            .add(orderedChBox))
                        .add(79, 79, 79))
                    .add(securityChBox)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(jScrollPane1))
                            .add(layout.createSequentialGroup()
                                .add(profileComboLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(profileCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(profConfigButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(devDefaultsChBox))
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(validatorsButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(secAdvancedButton))
                            .add(layout.createSequentialGroup()
                                .add(keyButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(trustButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(kerberosCfgButton)))
                        .add(109, 109, 109))
                    .add(jSeparator3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                    .add(tcpChBox)
                    .add(layout.createSequentialGroup()
                        .add(stsChBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stsConfigButton))
                    .add(fiChBox)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cfgVersionLabel)
                    .add(cfgVersionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mtomChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(orderedChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rmAdvanced)
                .add(8, 8, 8)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(securityChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(profileComboLabel)
                    .add(profileCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(profConfigButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(devDefaultsChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(trustButton)
                    .add(keyButton)
                    .add(kerberosCfgButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(secAdvancedButton)
                    .add(validatorsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stsChBox)
                    .add(stsConfigButton))
                .add(11, 11, 11)
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(8, 8, 8)
                .add(tcpChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fiChBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addrChBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {kerberosCfgButton, keyButton, secAdvancedButton, trustButton, validatorsButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        mtomChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSN")); // NOI18N
        mtomChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_mtomChBox_ACSD")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSN")); // NOI18N
        rmChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_rmChBox_ACSD")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSN")); // NOI18N
        securityChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_securityChBox_ACSD")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSN")); // NOI18N
        orderedChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_OrderedChBox_ACSD")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSN")); // NOI18N
        profileComboLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_profileComboLabel_ACSD")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSN")); // NOI18N
        rmAdvanced.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Advanced_ACSD")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSN")); // NOI18N
        stsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsChBox_ACSD")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSN")); // NOI18N
        tcpChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_tcpChBox_ACSD")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSN")); // NOI18N
        keyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keystoreButton_ACSD")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSN")); // NOI18N
        trustButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_truststoreButton_ACSD")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSN")); // NOI18N
        stsConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_stsConfigButton_ACSD")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSN")); // NOI18N
        profConfigButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_keyConfigButton_ACSD")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSN")); // NOI18N
        fiChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_fiChBox_ACSD")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSN")); // NOI18N
        validatorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_validatorsButton_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "LBL_Section_Service_Defaults_ACSD")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BindingPanel.class, "Panel_ACSD")); // NOI18N
        getAccessibleContext().setAccessibleParent(this);
        
    }

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
        enableDisable();
}//GEN-LAST:event_formAncestorAdded

    private void validatorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatorsButtonActionPerformed
        String profile = (String) profileCombo.getSelectedItem();
        ValidatorsPanel vPanel = new ValidatorsPanel(binding, project, profile, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(vPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Validators_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            vPanel.storeState();
        }

    }//GEN-LAST:event_validatorsButtonActionPerformed

    private void profConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profConfigButtonActionPerformed
        String prof = (String) profileCombo.getSelectedItem();
        SecurityProfile p = SecurityProfileRegistry.getDefault().getProfile(prof);
        p.displayConfig(binding, undoManager);
    }//GEN-LAST:event_profConfigButtonActionPerformed

    private void stsConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stsConfigButtonActionPerformed
        UndoCounter undoCounter = new UndoCounter();
        model.addUndoableEditListener(undoCounter);

        STSConfigServicePanel stsConfigPanel = new STSConfigServicePanel(project, binding, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(stsConfigPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_STSConfig_Panel_Title")); //NOI18N
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
    }//GEN-LAST:event_stsConfigButtonActionPerformed

    private void trustButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustButtonActionPerformed
        boolean jsr109 = isJsr109Supported();
        String profile = (String) profileCombo.getSelectedItem();
        TruststorePanel storePanel = new TruststorePanel(binding, project, jsr109, profile, false, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);
        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_trustButtonActionPerformed

    private void keyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyButtonActionPerformed
        boolean jsr109 = isJsr109Supported();
        KeystorePanel storePanel = new KeystorePanel(binding, project, jsr109, false, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(storePanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            storePanel.storeState();
        }
    }//GEN-LAST:event_keyButtonActionPerformed

    private void rmAdvancedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmAdvancedActionPerformed
        AdvancedRMPanel advancedRMPanel = new AdvancedRMPanel(binding, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedRMPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_AdvancedRM_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            advancedRMPanel.storeState();
        }
    }//GEN-LAST:event_rmAdvancedActionPerformed

    private void secAdvancedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secAdvancedButtonActionPerformed
        AdvancedSecurityPanel advancedSecPanel = new AdvancedSecurityPanel(binding, getUserExpectedConfigVersion()); //NOI18N
        DialogDescriptor dlgDesc = new DialogDescriptor(advancedSecPanel,
                NbBundle.getMessage(BindingPanel.class, "LBL_AdvancedSec_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            advancedSecPanel.storeState();
        }
}//GEN-LAST:event_secAdvancedButtonActionPerformed

    private void kerberosCfgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kerberosCfgButtonActionPerformed
        KerberosConfigPanel panel = new KerberosConfigPanel(binding, project, getUserExpectedConfigVersion());
        DialogDescriptor dlgDesc = new DialogDescriptor(panel,
                NbBundle.getMessage(BindingPanel.class, "LBL_KerberosConfig_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);
        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.storeState();
        }
}//GEN-LAST:event_kerberosCfgButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addrChBox;
    private javax.swing.JComboBox cfgVersionCombo;
    private javax.swing.JLabel cfgVersionLabel;
    private javax.swing.JCheckBox devDefaultsChBox;
    private javax.swing.JCheckBox fiChBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JButton kerberosCfgButton;
    private javax.swing.JButton keyButton;
    private javax.swing.JCheckBox mtomChBox;
    private javax.swing.JCheckBox orderedChBox;
    private javax.swing.JButton profConfigButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileComboLabel;
    private javax.swing.JTextArea profileInfoField;
    private javax.swing.JButton rmAdvanced;
    private javax.swing.JCheckBox rmChBox;
    private javax.swing.JButton secAdvancedButton;
    private javax.swing.JCheckBox securityChBox;
    private javax.swing.JCheckBox stsChBox;
    private javax.swing.JButton stsConfigButton;
    private javax.swing.JCheckBox tcpChBox;
    private javax.swing.JButton trustButton;
    private javax.swing.JButton validatorsButton;
    // End of variables declaration//GEN-END:variables
}
