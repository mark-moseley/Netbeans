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

package org.netbeans.modules.tomcat5.wizard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatFactory55;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Add Tomcat wizard panel.
 * 
 * @author Martin Grebac
 */
class InstallPanelVisual extends javax.swing.JPanel {
    
    private final String SERVER_XML = "conf/server.xml"; // NOI18N
    
    private final List listeners = new ArrayList();
    private final int tomcatVersion;
    
    private String errorMessage;
    
    private String serverPort;
    private String shutdownPort;
    
    private RequestProcessor.Task validationTask;
    
    
    /** Creates new form JPanel */
    public InstallPanelVisual(int aTomcatVersion) {
        tomcatVersion = aTomcatVersion;
        initComponents();
        DocumentListener updateListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }
        };
        jTextFieldHomeDir.getDocument().addDocumentListener(updateListener);
        jTextFieldBaseDir.getDocument().addDocumentListener(updateListener);
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // if JWSDP installed, disable the catalina base directory
                if (isJWSDP()) {
                    if (jCheckBoxShared.isEnabled()) {
                        jCheckBoxShared.setEnabled(false);
                        setBaseEnabled(false);
                    }
                } else {
                    if (!jCheckBoxShared.isEnabled()) {
                        jCheckBoxShared.setEnabled(true);
                        if (jCheckBoxShared.isSelected()) {
                            setBaseEnabled(true);
                        }
                    }
                }
            }
        });
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
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

        jLabelHomeDir = new javax.swing.JLabel();
        jLabelBaseDir = new javax.swing.JLabel();
        jTextFieldHomeDir = new javax.swing.JTextField();
        jTextFieldBaseDir = new javax.swing.JTextField();
        jButtonBaseBrowse = new javax.swing.JButton();
        jButtonHomeBrowse = new javax.swing.JButton();
        jCheckBoxShared = new javax.swing.JCheckBox();
        jLabelUsername = new javax.swing.JLabel();
        jLabelPassword = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextFieldUsername = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTextFieldPassword = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setName(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_InstanceProperties"));
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_panel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_panel"));
        jLabelHomeDir.setLabelFor(jTextFieldHomeDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelHomeDir, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_home_dir"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabelHomeDir, gridBagConstraints);
        jLabelHomeDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelHomeDir"));
        jLabelHomeDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelHomeDir"));

        jLabelBaseDir.setLabelFor(jTextFieldBaseDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelBaseDir, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_base_dir"));
        jLabelBaseDir.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jLabelBaseDir, gridBagConstraints);
        jLabelBaseDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelBaseDir"));
        jLabelBaseDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelBaseDir"));

        jTextFieldHomeDir.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jTextFieldHomeDir, gridBagConstraints);
        jTextFieldHomeDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_homeDir"));
        jTextFieldHomeDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_homeDir"));

        jTextFieldBaseDir.setColumns(15);
        jTextFieldBaseDir.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jTextFieldBaseDir, gridBagConstraints);
        jTextFieldBaseDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_baseDir"));
        jTextFieldBaseDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_baseDir"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBaseBrowse, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_file_chooser_base"));
        jButtonBaseBrowse.setEnabled(false);
        jButtonBaseBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBaseBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jButtonBaseBrowse, gridBagConstraints);
        jButtonBaseBrowse.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_ButtonBaseBrowse"));
        jButtonBaseBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_ButtonBaseBrowse"));

        org.openide.awt.Mnemonics.setLocalizedText(jButtonHomeBrowse, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_file_chooser_home"));
        jButtonHomeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHomeBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jButtonHomeBrowse, gridBagConstraints);
        jButtonHomeBrowse.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_ButtonHomeBrowse"));
        jButtonHomeBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_ButtonHomeBrowse"));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxShared, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_SharedInstall"));
        jCheckBoxShared.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSharedActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jCheckBoxShared, gridBagConstraints);
        jCheckBoxShared.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_shared"));
        jCheckBoxShared.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_shared"));

        jLabelUsername.setLabelFor(jTextFieldUsername);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelUsername, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_Username"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelUsername, gridBagConstraints);
        jLabelUsername.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelUsername"));
        jLabelUsername.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelUsername"));

        jLabelPassword.setLabelFor(jTextFieldPassword);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPassword, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_Password"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabelPassword, gridBagConstraints);
        jLabelPassword.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelPassword"));
        jLabelPassword.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelPassword"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "MSG_TextAbove"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jTextFieldUsername.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        jPanel1.add(jTextFieldUsername, gridBagConstraints);
        jTextFieldUsername.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_username"));
        jTextFieldUsername.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_username"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_ForManager"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTextFieldPassword.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel2.add(jTextFieldPassword, gridBagConstraints);
        jTextFieldPassword.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_password"));
        jTextFieldPassword.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_password"));

        jLabel3.setForeground(getBackground());
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_ForManager"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel2.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jButtonBaseBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBaseBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(jTextFieldBaseDir.getText().trim()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jTextFieldBaseDir.setText(chooser.getSelectedFile().getAbsolutePath());
            fireChange();
        }
    }//GEN-LAST:event_jButtonBaseBrowseActionPerformed

    private void jButtonHomeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHomeBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File(jTextFieldHomeDir.getText().trim()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jTextFieldHomeDir.setText(chooser.getSelectedFile().getAbsolutePath());
            fireChange();
        }
    }//GEN-LAST:event_jButtonHomeBrowseActionPerformed

    private void jCheckBoxSharedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSharedActionPerformed
        setBaseEnabled(jCheckBoxShared.isSelected());
        fireChange();
    }//GEN-LAST:event_jCheckBoxSharedActionPerformed
    
    public java.util.Properties getProperties() {
        Properties p = new Properties();
        p.put(TomcatProperties.PROP_SERVER_PORT, serverPort);
        p.put(TomcatProperties.PROP_SHUTDOWN,    shutdownPort);
        p.put(TomcatProperties.PROP_MONITOR,     "false"); // NOI18N
        return p;
    }
    
    public String getUrl() {
        String url;
        if (tomcatVersion == TomcatManager.TOMCAT_55) {
            url = TomcatFactory55.tomcatUriPrefix;
        } else {
            url = TomcatFactory.tomcatUriPrefix;
        }
        
        url += "home=" + jTextFieldHomeDir.getText();       // NOI18N
        
        if (jCheckBoxShared.isEnabled() && jCheckBoxShared.isSelected()) {
            url += ":base=" + jTextFieldBaseDir.getText();  // NOI18N
        }
        
        TomcatFactory.getEM().log("TomcatInstall.getUrl: " + url);    // NOI18N
        return url;
    }
    
    public String getUsername() {
        return jTextFieldUsername.getText();
    }
    
    public String getPassword() {
        return new String(jTextFieldPassword.getPassword());
    }
    
    public File getHomeDir() {
        return new File(jTextFieldHomeDir.getText());
    }
    
    public String getErrorMessage() {
        // prevent the message from being cut off - wizard descriptor issue work-around
        return errorMessage == null 
                ? null
                : "<html>" + errorMessage.replaceAll("<",  "&lt;").replaceAll(">",  "&gt;") + "</html>"; // NIO18N
    }
    
    boolean isServerXmlValid(File file) {
        try {
            Server server = Server.createGraph(file);
            serverPort = TomcatInstallUtil.getPort(server);
            shutdownPort = TomcatInstallUtil.getShutdownPort(server);
            if (serverPort != null && shutdownPort != null) {
                // test whether it's parseable
                Integer.parseInt(serverPort);
                Integer.parseInt(shutdownPort);
                return true;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        } catch (NumberFormatException nfe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nfe);
        } catch (RuntimeException e) {
            // catch any runtime exception that may occur during graph parsing
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return false;
    }
    
    private boolean isHomeValid() {
        String homeDir = jTextFieldHomeDir.getText();
        if (homeDir.length() == 0) {
            errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_SpecifyHomeDir");
            return false;
        } 
        if (!new File(homeDir, "bin/bootstrap.jar").exists()) { // NOI18N
            errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_InvalidHomeDir");
            return false;
        }
        if ((!jCheckBoxShared.isEnabled() || !jCheckBoxShared.isSelected()) && !isServerXmlValid(new File(homeDir, SERVER_XML))) {
            errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_CorruptedHomeServerXml");
            return false;
        }
        return true;
    }
    
    /** Is it Tomcat with the JWSDP installed? Does it contain the jwsdp-shared folder? */
    private boolean isJWSDP() {
        if (isHomeValid()) {
            File homeDir = getHomeDir();
            if (homeDir != null && homeDir.exists()) {
                File files[] = homeDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if ("jwsdp-shared".equals(name)) { // NOI18N
                            return true;
                        }
                        return false;
                    }
                });
                return files.length != 0 ? true : false;
            }
        }
        return false;
    }
    
    private boolean isBaseValid() {        
        // catalina base
        if (jCheckBoxShared.isEnabled() && jCheckBoxShared.isSelected()) {
            String base = jTextFieldBaseDir.getText();
            if (base.length() == 0) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_SpecifyBaseDir");
                return false;
            }
            File baseDir = new File(base);
            String[] files = baseDir.list();
            File serverXml = new File(baseDir, SERVER_XML);
            // is the base dir empty or does the server.xml file exists?
            if (!baseDir.exists() || files == null
                || (files.length > 0 &&  !serverXml.exists())) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_InvalidBaseDir");
                return false;
            }
            if (files.length > 0) {
                // check CATALINA_BASE/conf/server.xml, if base dir not empty
                if (!isServerXmlValid(serverXml)) {
                    errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_CorruptedBaseServerXml");
                    return false;
                }
            } else {
                // otherwise check CATALINA_HOME/conf/server.xml which we will copy to base dir
                if (!isServerXmlValid(new File(jTextFieldHomeDir.getText(), SERVER_XML))) {
                    errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_CorruptedHomeServerXml");
                    return false;
                }
            }
        }        
        return true;
    }
    
    private boolean isAlreadyRegistered() {
        if (InstanceProperties.getInstanceProperties(getUrl()) != null) {
            errorMessage = NbBundle.getMessage(InstallPanelVisual.class, 
                                jCheckBoxShared.isEnabled() && jCheckBoxShared.isSelected() 
                                    ? "MSG_AlreadyRegisteredBase" 
                                    : "MSG_AlreadyRegisteredHome");
            return true;
        }
        return false;
    }
    
    private void setBaseEnabled(boolean enabled) {
        jLabelBaseDir.setEnabled(enabled);
        jTextFieldBaseDir.setEnabled(enabled);
        jButtonBaseBrowse.setEnabled(enabled);
    }
    
    public boolean isValid() {
        errorMessage = null;
        return isHomeValid() && isBaseValid() && !isAlreadyRegistered();
    }
    
    private void fireChange() {
        // schedule the validation task so that error messages won't flash e.g. 
        // when calling jTextFieldBaseDir.setText which triggers two consecutive 
        // events removeUpdate and insertUpdate. validation after the first one 
        // inevitably leads to a failure.
        if (validationTask == null) {
            validationTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    
                    ChangeEvent event = new ChangeEvent(this);
                    ArrayList tempList;
                    
                    synchronized(listeners) {
                        tempList = new ArrayList(listeners);
                    }
                    
                    Iterator iter = tempList.iterator();
                    while (iter.hasNext()) {
                        ((ChangeListener)iter.next()).stateChanged(event);
                    }
                }
            });
        }
        validationTask.schedule(60);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBaseBrowse;
    private javax.swing.JButton jButtonHomeBrowse;
    private javax.swing.JCheckBox jCheckBoxShared;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelBaseDir;
    private javax.swing.JLabel jLabelHomeDir;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JLabel jLabelUsername;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextFieldBaseDir;
    private javax.swing.JTextField jTextFieldHomeDir;
    private javax.swing.JPasswordField jTextFieldPassword;
    private javax.swing.JTextField jTextFieldUsername;
    // End of variables declaration//GEN-END:variables
    
}
