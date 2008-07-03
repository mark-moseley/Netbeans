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

package org.netbeans.modules.mobility.cldcplatform;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.event.*;
import javax.swing.*;

import org.openide.filesystems.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;

/**
 * This Panel launches autoconfiguration during the New J2ME Platform sequence.
 * The UI views properties of the platform, reacts to the end of detection by
 * updating itself. It triggers the detection task when the button is pressed.
 * The inner class WizardPanel acts as a controller, reacts to the UI completness
 * (jdk name filled in) and autoconfig result (passed successfully) - and manages
 * Next/Finish button (valid state) according to those.
 *
 * @author Svata Dedic, David Kaspar
 */
public class DetectPanel extends javax.swing.JPanel {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(560, 350);
    
    public static final String PLATFORM_LOCATION = "PlatformLocation"; //NOI18N
    public static final String PLATFORM = "Platform"; //NOI18N
    
    protected String detectedFolder = null;
    final private static String NAMEHINT = "LBL_DetectPanel_NameHint";
    final private static String BR = "<br>";
    
    /**
     * Creates a detect panel
     */
    public DetectPanel() {
        initComponents();
        initAccessibility();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        tPlatformPath = new javax.swing.JTextField();
        tNote = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        tPlatformDisplayName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        tPlatformType = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lDevices = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        tPlatformConfiguration = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tPlatformProfile = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tPlatformOptional = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();

        setLayout(new java.awt.GridBagLayout());

        setName(org.openide.util.NbBundle.getMessage(DetectPanel.class, "TITLE_J2MEWizardIterator_DetectPanel"));
        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Location").charAt(0));
        jLabel2.setLabelFor(tPlatformPath);
        jLabel2.setText(NbBundle.getMessage(DetectPanel.class, "LAB_DetectPanel_Location"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel2, gridBagConstraints);

        tPlatformPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformPath, gridBagConstraints);

        tNote.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        tNote.setEditable(false);
        tNote.setLineWrap(true);
        tNote.setRows(5);
        tNote.setText(NbBundle.getMessage(DetectPanel.class, "TXT_DetectPanel_Explain"));
        tNote.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        add(tNote, gridBagConstraints);
        tNote.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Panel"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        jLabel3.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Name").charAt(0));
        jLabel3.setLabelFor(tPlatformDisplayName);
        jLabel3.setText(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        tPlatformDisplayName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformDisplayName, gridBagConstraints);
        tPlatformDisplayName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Name"));

        jLabel4.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Type").charAt(0));
        jLabel4.setLabelFor(tPlatformType);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel4, gridBagConstraints);

        tPlatformType.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformType, gridBagConstraints);
        tPlatformType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Type"));

        jLabel1.setText(NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Devices"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 0));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(259, 100));
        jScrollPane1.setAutoscrolls(true);
        lDevices.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lDevices.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lDevices.setEnabled(false);
        jScrollPane1.setViewportView(lDevices);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jLabel6.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Configuration").charAt(0));
        jLabel6.setLabelFor(tPlatformConfiguration);
        jLabel6.setText(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Configuration"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel6, gridBagConstraints);

        tPlatformConfiguration.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformConfiguration, gridBagConstraints);
        tPlatformConfiguration.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Configuration"));

        jLabel5.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Profile").charAt(0));
        jLabel5.setLabelFor(tPlatformProfile);
        jLabel5.setText(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Profile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel5, gridBagConstraints);

        tPlatformProfile.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformProfile, gridBagConstraints);
        tPlatformProfile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Profile"));

        jLabel7.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DetectPanel.class, "MNM_DetectPanel_Optional").charAt(0));
        jLabel7.setLabelFor(tPlatformOptional);
        jLabel7.setText(org.openide.util.NbBundle.getMessage(DetectPanel.class, "LBL_DetectPanel_Optional"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel7, gridBagConstraints);

        tPlatformOptional.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(tPlatformOptional, gridBagConstraints);
        tPlatformOptional.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetectPanel.class, "ACD_DetectPanel_Optional"));

        progressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 6);
        gridBagConstraints.weightx = 1.0;
        add(progressBar, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void initAccessibility() {
        //getAccessibleContext().setAccessibleName();
        //getAccessibleContext().setAccessibleDescription();
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    public void addListener(final DocumentListener listener) {
        tPlatformDisplayName.getDocument().addDocumentListener(listener);
    }
    
    public void removeListener(final DocumentListener listener) {
        tPlatformDisplayName.getDocument().removeDocumentListener(listener);
    }
    
    public String getPlatformName() {
        return tPlatformDisplayName.getText();
    }
    
    public void storeData(final WizardDescriptor object) {
        final J2MEPlatform platform = (J2MEPlatform) object.getProperty(PLATFORM);
        if (platform == null)
            return;
        final String dName = getPlatformName();
        platform.setDisplayName(dName);
        platform.setName(J2MEPlatform.computeUniqueName(dName));
    }
    
    public void readData(final WizardDescriptor object) {
        final String platformLocation = (String) object.getProperty(PLATFORM_LOCATION);
        if (platformLocation == null)
            return;
        final J2MEPlatform[] platform = new J2MEPlatform[] {
            (J2MEPlatform) object.getProperty(PLATFORM)
        };
        detectPlatform(platform, platformLocation, object, null);
    }
    
    
    void detectPlatform(final J2MEPlatform[] platform, final String platformLocation, final WizardDescriptor object, final DialogDescriptor desc) {
        if (desc != null) desc.setValid(false);
        tPlatformPath.setText(platformLocation);
        progressBar.setIndeterminate(true);
        progressBar.setString(NbBundle.getMessage(DetectPanel.class, "TXT_DetectPanel_WaitMessage")); // NOI18N
        tPlatformDisplayName.setEditable(false);
        final J2MEPlatform oldPlatform = platform[0];
        clearData(object == null && oldPlatform != null ? oldPlatform.getDisplayName() : ""); //NOI18N
        final RequestProcessor.Task rpt = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (platform[0] != null  &&  detectedFolder != null  &&  platformLocation.equals(detectedFolder))
                    return;
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                try {
                    platform[0] = new UEIEmulatorConfiguratorImpl(platformLocation).getPlatform(pw);
                } finally {
                    pw.flush();
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, sw.getBuffer().toString());
                }
                if (object != null) object.putProperty(PLATFORM, platform[0]);
                else if (oldPlatform != null && platform[0] != null){
                    platform[0].setName(oldPlatform.getName());
                    platform[0].setDisplayName(oldPlatform.getDisplayName());
                }
                detectedFolder = platformLocation;
            }
        });
        rpt.addTaskListener(new TaskListener() {
            public void taskFinished(@SuppressWarnings("unused") Task task) {
                SwingUtilities.invokeLater( new Runnable() {
                    @SuppressWarnings("synthetic-access")
					public void run() {
                        progressBar.setIndeterminate(false);
                        progressBar.setString(NbBundle.getMessage(DetectPanel.class, (platform[0] != null) ? "TXT_DetectPanel_ConfigSuccess" : "TXT_DetectPanel_ConfigFailed")); // NOI18N
                        updateData(platform[0], object != null); //editable name only in wizard
                        tPlatformDisplayName.requestFocus();
                        if (desc != null && platform[0] != null) desc.setValid(true);
                    }
                });
            }
        });
        
    }
    
    void clearData(final String displayName) {
        tPlatformDisplayName.setText(displayName);
        tPlatformDisplayName.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
        tPlatformType.setText(""); //NOI18N
        tPlatformType.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
        tPlatformProfile.setText(""); //NOI18N
        tPlatformProfile.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
        tPlatformConfiguration.setText(""); //NOI18N
        tPlatformConfiguration.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
        tPlatformOptional.setText(""); //NOI18N
        tPlatformOptional.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
        lDevices.setModel(new DefaultListModel());
        lDevices.setToolTipText(NbBundle.getMessage(DetectPanel.class, NAMEHINT)); // NOI18N
    }
    
    /**
     * Updates static information from the detected platform's properties
     */
    void updateData(final J2MEPlatform platform, final boolean editableName) {
        if (platform == null) {
            clearData(""); //NOI18N
            return;
        }
        final String dname = platform.getDisplayName();
        tPlatformDisplayName.setText(dname == null ? platform.getName() : dname);
        tPlatformDisplayName.setToolTipText(NbBundle.getMessage(DetectPanel.class, "HINT_DetectPanel_DisplayName")); // NOI18N
        tPlatformDisplayName.setEditable(editableName);
        tPlatformType.setText(platform.getType());
        tPlatformType.setToolTipText(NbBundle.getMessage(DetectPanel.class, "HINT_DetectPanel_PlatformType")); // NOI18N
        
        final J2MEPlatform.Device ds[] = platform.getDevices();
        final ArrayList<String> profiles = new ArrayList<String>();
        final ArrayList<String> profilesHint = new ArrayList<String>();
        final ArrayList<String> configurations = new ArrayList<String>();
        final ArrayList<String> configurationsHint = new ArrayList<String>();
        final ArrayList<String> optionals = new ArrayList<String>();
        final ArrayList<String> optionalsHint = new ArrayList<String>();
        if (ds != null) for (int a = 0; a < ds.length; a ++) {
            final J2MEPlatform.J2MEProfile[] ps = ds[a].getProfiles();
            if (ps != null) for (int b = 0; b < ps.length; b ++) {
                final String n = ps[b].toString();
                final String dn = ps[b].isNameIsJarFileName() ? ps[b].getDisplayName() : ps[b].getDisplayNameWithVersion();
                final String type = ps[b].getType();
                if (J2MEPlatform.J2MEProfile.TYPE_PROFILE.equals(type)) {
                    if (!profiles.contains(n))
                        profiles.add(n);
                    if (!profilesHint.contains(dn))
                        profilesHint.add(dn);
                } else if (J2MEPlatform.J2MEProfile.TYPE_CONFIGURATION.equals(type)) {
                    if (!configurations.contains(n))
                        configurations.add(n);
                    if (!configurationsHint.contains(dn))
                        configurationsHint.add(dn);
                } else if (J2MEPlatform.J2MEProfile.TYPE_OPTIONAL.equals(type)) {
                    if (!optionals.contains(n))
                        optionals.add(n);
                    if (!optionalsHint.contains(dn))
                        optionalsHint.add(dn);
                }
            }
        }
        tPlatformProfile.setText(list2String(profiles));
        if (profilesHint.size() > 0)
            tPlatformProfile.setToolTipText("<html>" + NbBundle.getMessage(DetectPanel.class, "MSG_ProfilesHintTitle") + BR + list2String(profilesHint, BR)); // NOI18N
        tPlatformConfiguration.setText(list2String(configurations));
        if (configurationsHint.size() > 0)
            tPlatformConfiguration.setToolTipText("<html>" + NbBundle.getMessage(DetectPanel.class, "MSG_ConfigurationsHintTitle") + BR + list2String(configurationsHint, BR)); // NOI18N
        tPlatformOptional.setText(list2String(optionals));
        if (optionalsHint.size() > 0)
            tPlatformOptional.setToolTipText("<html>" + NbBundle.getMessage(DetectPanel.class, "MSG_OptionalsHintTitle") + BR + list2String(optionalsHint, BR)); // NOI18N
        
        if (ds != null && ds.length > 0) {
            final DefaultListModel list = new DefaultListModel();
            for (int a = 0; a < ds.length; a ++)
                list.addElement(ds[a].getName());
            lDevices.setModel(list);
            lDevices.setEnabled(true);
        } else {
            lDevices.setEnabled(false);
        }
        lDevices.setToolTipText(""); //NOI18N
    }
    
    private static String list2String(final ArrayList<String> list) {
        return list2String(list, ", "); //NOI18N
    }
    
    private static String list2String(final ArrayList<String> list, final String separator) {
        if (list == null  ||  list.isEmpty())
            return ""; //NOI18N
        final StringBuffer sb = new StringBuffer(list.get(0));
        for (int a = 1; a < list.size(); a ++) {
            sb.append(separator);
            sb.append(list.get(a));
        }
        return sb.toString();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList lDevices;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea tNote;
    private javax.swing.JTextField tPlatformConfiguration;
    private javax.swing.JTextField tPlatformDisplayName;
    private javax.swing.JTextField tPlatformOptional;
    private javax.swing.JTextField tPlatformPath;
    private javax.swing.JTextField tPlatformProfile;
    private javax.swing.JTextField tPlatformType;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Controller for the outer class: manages wizard panel's valid state
     * according to the user's input and detection state.
     */
    public static class WizardPanel implements WizardDescriptor.FinishablePanel, DocumentListener {
        
        DetectPanel component;
        WizardDescriptor wizard;
        ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        boolean finishable;
        
        public WizardPanel(boolean finishable) {
            this.finishable = finishable;
        }
        
        public void addChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        public void removeChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        public java.awt.Component getComponent() {
            if (component == null) {
                // !!! use unified workdir
                component = new DetectPanel();
                component.addListener(this);
                checkValid();
            }
            return component;
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(DetectPanel.class);
        }
        
        public boolean isFinishPanel() {
            return finishable;
        }
        
        public void showError(final String message) {
            if (wizard != null)
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }
        
        public boolean isValid() {
            J2MEPlatform platform = null;
            if (wizard != null)
                platform = (J2MEPlatform) wizard.getProperty(PLATFORM);
            if (platform == null) {
                showError(NbBundle.getMessage(DetectPanel.class, "ERR_DetectPanel_PlatformWasNotDetected")); //NOI18N
                return false;
            } 
            final String name = J2MEPlatform.computeUniqueName(component.getPlatformName());
            final FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource("Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
            if (platformsFolder.getFileObject(name, "xml") != null) { // NOI18N
                showError(NbBundle.getMessage(DetectPanel.class, "ERR_DetectPanel_PlatformAlreadyExists")); //NOI18N
                return false;
            }
            
            final String dispName = component.getPlatformName();
            final boolean valid = dispName != null ? dispName.length() > 0 : false;
            
            if (! valid)
                showError(NbBundle.getMessage(DetectPanel.class, "ERR_DetectPanel_EmptyDisplayName")); //NOI18N
            else
                showError(null);
            return valid;
        }
        
        public void readSettings(final Object obj) {
            wizard = (WizardDescriptor) obj;
            ((DetectPanel) getComponent()).readData(wizard);
        }
        
        public void storeSettings(final Object obj) {
            wizard = (WizardDescriptor) obj;
            ((DetectPanel) getComponent()).storeData(wizard);
        }
        
        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty())
                    return;
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
        }
        
        void checkValid() {
            if (isValid() != valid) {
                valid ^= valid;
                fireStateChange();
            }
        }
        
        public void changedUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        public void insertUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
        public void removeUpdate(@SuppressWarnings("unused")
		final javax.swing.event.DocumentEvent e) {
            checkValid();
        }
        
    }
    
}
