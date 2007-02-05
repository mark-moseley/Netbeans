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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2me.cdc.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class CDCPlatformCustomizer extends JPanel implements CustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private static final String[] PROPERTY_NAMES = new String[] {
        DefaultPropertiesDescriptor.PLATFORM_ACTIVE,
        DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION,
        DefaultPropertiesDescriptor.PLATFORM_DEVICE,
        DefaultPropertiesDescriptor.PLATFORM_PROFILE,
        DefaultPropertiesDescriptor.PLATFORM_BOOTCLASSPATH,
        DefaultPropertiesDescriptor.PLATFORM_TYPE,
        DefaultPropertiesDescriptor.JAVAC_SOURCE,
        DefaultPropertiesDescriptor.JAVAC_TARGET,
        CDCPropertiesDescriptor.PLATFORM_FAT_JAR
    };

    private Map<String, Object> props;
    private VisualPropertySupport vps;
    private String configuration;
    private String platformNames[];
    private HashMap<String,CDCPlatform> name2platform;
    private HashMap<String,CDCDevice> name2device;
    private HashMap<String,CDCDevice.CDCProfile> name2profile;
    private boolean useDefault;
    
    /** Creates new form CDCPlatformCustomizer */
    public CDCPlatformCustomizer() {
        initComponents();
        initAll();
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        this.vps = VisualPropertySupport.getDefault(props);
        this.configuration = configuration;
    }
    
    public void initGroupValues(boolean useDefault) {
        jComboBoxTarget.removeActionListener(this);
        if (platformNames.length > 0) {
            this.useDefault = useDefault;
            vps.register(jComboBoxTarget, platformNames, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, useDefault);
            jComboBoxTarget.addActionListener(this);
        } else {
            jComboBoxTarget.removeAllItems();
            final String errorMessage = NbBundle.getMessage(CDCPlatformCustomizer.class, "ERR_CDCCust_NoPlatform"); //NOI18N
            jComboBoxTarget.addItem(errorMessage);
            jComboBoxTarget.setSelectedItem(errorMessage);
            jComboBoxTarget.setEnabled(false);
        }
        initDevices((String)jComboBoxTarget.getSelectedItem());
        enableLabels(!useDefault);
//        props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_SOURCE, useDefault), "1.3"); //NOI18N
//        props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_TARGET, useDefault), "1.3"); //NOI18N
    }
    
    private synchronized void initAll() {
        name2platform = new HashMap();
        name2device = new HashMap();
        name2profile = new HashMap();
        // Read defined platforms and all configurations, profiles and optional packages
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification(CDCPlatform.PLATFORM_CDC, null));
        for( int i = 0; i < platforms.length; i++ ) {
            if (platforms[i] instanceof CDCPlatform) {
                final CDCPlatform platform = (CDCPlatform)platforms[i];
                name2platform.put(platform.getDisplayName(), platform);
            }
        }
        platformNames = name2platform.keySet().toArray(new String[name2platform.size()]);
        Arrays.sort(platformNames);
    }
    private synchronized void initDevices(final String platformName) {
        jComboBoxDevice.removeActionListener(this);
        final CDCPlatform platform = name2platform.get(platformName);
        if (platform != null) {
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION, useDefault), platform.getDisplayName());
            props.put(VisualPropertySupport.translatePropertyName(configuration, CDCPropertiesDescriptor.PLATFORM_FAT_JAR, useDefault), Boolean.valueOf(platform.isFatJar()));
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_SOURCE, useDefault), platform.getClassVersion());
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.JAVAC_TARGET, useDefault), platform.getClassVersion());
            props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_TYPE, useDefault), platform.getType());
            final CDCDevice[] devices = platform.getDevices();
            name2device = new HashMap<String,CDCDevice>();
            for (int i=0; i<devices.length; i++) {
                name2device.put(devices[i].getName(), devices[i]);
            }
            String[] devNames = name2device.keySet().toArray(new String[name2device.size()]);
            Arrays.sort(devNames);
            vps.register(jComboBoxDevice, devNames, DefaultPropertiesDescriptor.PLATFORM_DEVICE, useDefault);
            initProfiles((String)jComboBoxDevice.getSelectedItem());
            jComboBoxDevice.addActionListener(this);
        } else {
            jComboBoxDevice.removeAllItems();
            jComboBoxDevice.setEnabled(false);
            enableLabels(false);
        }
    }
    
    private synchronized void initProfiles(final String deviceName) {
        jComboBoxProfile.removeActionListener(this);
        final CDCDevice device = name2device.get(deviceName);
        if (device != null) {
            CDCDevice.CDCProfile[] profiles = device.getProfiles();
            name2profile = new HashMap<String,CDCDevice.CDCProfile>();
            for (int i=0; i<profiles.length; i++) {
                name2profile.put(profiles[i].getName(), profiles[i]);
            }
            String[] profNames = name2profile.keySet().toArray(new String[name2profile.size()]);
            Arrays.sort(profNames);
            vps.register(jComboBoxProfile, profNames, DefaultPropertiesDescriptor.PLATFORM_PROFILE, useDefault);
            jComboBoxProfile.addActionListener(this);
        } else {
            jComboBoxProfile.removeAllItems();
            jComboBoxProfile.setEnabled(false);
        }
        saveClassPath();
    }
      
    private void saveClassPath() {
        CDCDevice.CDCProfile profile = name2profile.get(jComboBoxProfile.getSelectedItem());
        if (profile !=null) props.put(VisualPropertySupport.translatePropertyName(configuration, DefaultPropertiesDescriptor.PLATFORM_BOOTCLASSPATH, useDefault), profile.getBootClassPath());
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_NAMES;
    }
    
    private void enableLabels(final boolean enabled) {
        jLabel1.setEnabled(enabled);
        jLabel2.setEnabled(enabled);
        jLabelTarget.setEnabled(enabled);
        jButton1.setEnabled(enabled);
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (jComboBoxTarget.equals(e.getSource())) {
            initDevices((String)jComboBoxTarget.getSelectedItem());
        } else if  (jComboBoxDevice.equals(e.getSource())) {
            initProfiles((String)jComboBoxDevice.getSelectedItem());
        } else if (jComboBoxProfile.equals(e.getSource())) {
            saveClassPath();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxDevice = new javax.swing.JComboBox();
        jComboBoxProfile = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, org.openide.util.NbBundle.getMessage(CDCPlatformCustomizer.class, "CDCPlatformCustomizer.jLabelTarget.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(jLabelTarget, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxTarget, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CDCPlatformCustomizer.class, "CDCPlatformCustomizer.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1createNewPlatform(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jButton1, gridBagConstraints);

        jLabel1.setLabelFor(jComboBoxDevice);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CDCPlatformCustomizer.class,"CDCPlatformCustomizer.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(jLabel1, gridBagConstraints);

        jLabel2.setLabelFor(jComboBoxProfile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CDCPlatformCustomizer.class,"CDCPlatformCustomizer.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboBoxDevice, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxProfile, gridBagConstraints);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 227, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void jButton1createNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1createNewPlatform
    PlatformsCustomizer.showCustomizer(name2platform.get(jComboBoxTarget.getSelectedItem()));
    initAll();
    initGroupValues(useDefault);
}//GEN-LAST:event_jButton1createNewPlatform
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBoxDevice;
    private javax.swing.JComboBox jComboBoxProfile;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}
