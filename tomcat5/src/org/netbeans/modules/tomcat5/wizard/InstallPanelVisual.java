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

package org.netbeans.modules.tomcat5.wizard;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.config.gen.Server;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.modules.tomcat5.util.TomcatUsers;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Add Tomcat wizard panel.
 * 
 * @author Martin Grebac
 */
class InstallPanelVisual extends javax.swing.JPanel {
    
    private static final String SERVER_XML = "conf/server.xml"; // NOI18N
    
    private final List listeners = new ArrayList();
    private final TomcatVersion tomcatVersion;
    
    private String errorMessage;
    
    private String serverPort;
    private String shutdownPort;
    
    private RequestProcessor.Task validationTask;
    
    
    /** Creates new form JPanel */
    public InstallPanelVisual(TomcatVersion tomcatVersion) {
        this.tomcatVersion = tomcatVersion;
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
        jTextFieldUsername.getDocument().addDocumentListener(updateListener);
        jTextFieldPassword.getDocument().addDocumentListener(updateListener);
        createUserCheckBox.getModel().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fireChange();
            }
        });
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
        jTextFieldPassword = new javax.swing.JPasswordField();
        jTextFieldUsername = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        createUserCheckBox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_InstallationAndLoginDetails")); // NOI18N

        jLabelHomeDir.setLabelFor(jTextFieldHomeDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelHomeDir, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_home_dir")); // NOI18N

        jLabelBaseDir.setLabelFor(jTextFieldBaseDir);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelBaseDir, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_base_dir")); // NOI18N
        jLabelBaseDir.setEnabled(false);

        jTextFieldHomeDir.setColumns(15);

        jTextFieldBaseDir.setColumns(15);
        jTextFieldBaseDir.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBaseBrowse, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_file_chooser_base")); // NOI18N
        jButtonBaseBrowse.setEnabled(false);
        jButtonBaseBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBaseBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonHomeBrowse, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_file_chooser_home")); // NOI18N
        jButtonHomeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHomeBrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxShared, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_SharedInstall")); // NOI18N
        jCheckBoxShared.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSharedActionPerformed(evt);
            }
        });

        jLabelUsername.setLabelFor(jTextFieldUsername);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelUsername, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_Username")); // NOI18N
        jLabelUsername.setToolTipText(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_CreateUserToolTip")); // NOI18N

        jLabelPassword.setLabelFor(jTextFieldPassword);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelPassword, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_Password")); // NOI18N
        jLabelPassword.setToolTipText(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_CreateUserToolTip")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "MSG_TextAbove")); // NOI18N

        jTextFieldPassword.setColumns(20);

        jTextFieldUsername.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_Credentials")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_CreateUserToolTip")); // NOI18N

        createUserCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createUserCheckBox, org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_CreateUser")); // NOI18N
        createUserCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(InstallPanelVisual.class, "LBL_CreateUserToolTip")); // NOI18N
        createUserCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createUserCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jLabel1)
            .add(jCheckBoxShared)
            .add(jLabel2)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelUsername)
                    .add(jLabelPassword)
                    .add(layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(jLabelBaseDir))
                    .add(jLabelHomeDir))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(createUserCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldBaseDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTextFieldHomeDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jButtonHomeBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jButtonBaseBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldPassword, 0, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldUsername, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jLabelHomeDir))
                    .add(layout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButtonHomeBrowse)
                            .add(jTextFieldHomeDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(5, 5, 5)
                .add(jCheckBoxShared)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jLabelBaseDir))
                    .add(layout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextFieldBaseDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButtonBaseBrowse))))
                .add(14, 14, 14)
                .add(jLabel2)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(jLabelUsername))
                    .add(layout.createSequentialGroup()
                        .add(5, 5, 5)
                        .add(jTextFieldUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelPassword)
                    .add(jTextFieldPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createUserCheckBox)
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jLabelHomeDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelHomeDir"));
        jLabelHomeDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelHomeDir"));
        jLabelBaseDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelBaseDir"));
        jLabelBaseDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelBaseDir"));
        jTextFieldHomeDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_homeDir"));
        jTextFieldHomeDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_homeDir"));
        jTextFieldBaseDir.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_baseDir"));
        jTextFieldBaseDir.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_baseDir"));
        jButtonBaseBrowse.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_ButtonBaseBrowse"));
        jButtonBaseBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_ButtonBaseBrowse"));
        jButtonHomeBrowse.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_ButtonHomeBrowse"));
        jButtonHomeBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_ButtonHomeBrowse"));
        jCheckBoxShared.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_shared"));
        jCheckBoxShared.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_shared"));
        jLabelUsername.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelUsername"));
        jLabelUsername.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelUsername"));
        jLabelPassword.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_labelPassword"));
        jLabelPassword.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_labelPassword"));
        jTextFieldPassword.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_password"));
        jTextFieldPassword.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_password"));
        jTextFieldUsername.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_username"));
        jTextFieldUsername.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_username"));

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_NAME_panel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InstallPanelVisual.class, "A11Y_DESC_panel"));
    }// </editor-fold>//GEN-END:initComponents

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
    
    public TomcatVersion getTomcatVersion() {
        return tomcatVersion;
    }
    
    public String getUrl() {
        String url;
        switch (tomcatVersion) {
            case TOMCAT_60:
                url = TomcatFactory.TOMCAT_URI_PREFIX_60;
                break;
            case TOMCAT_55:
                url = TomcatFactory.TOMCAT_URI_PREFIX_55;
                break;
            case TOMCAT_50:
            default:
                url = TomcatFactory.TOMCAT_URI_PREFIX_50;
                break;
        }
        
        url += "home=" + jTextFieldHomeDir.getText();       // NOI18N
        
        if (jCheckBoxShared.isEnabled() && jCheckBoxShared.isSelected()) {
            url += ":base=" + jTextFieldBaseDir.getText();  // NOI18N
        }
        Logger.getLogger(InstallPanelVisual.class.getName()).log(Level.INFO, "TomcatInstall.getUrl: " + url);    // NOI18N
        return url;
    }
    
    public String getUsername() {
        return jTextFieldUsername.getText();
    }
    
    public String getPassword() {
        return new String(jTextFieldPassword.getPassword());
    }

    boolean createUserEnabled() {
        return createUserCheckBox.isSelected();
    }
    
    public File getHomeDir() {
        return new File(jTextFieldHomeDir.getText());
    }
    
    /**
     * Returns catalina base folder if the base folder exists and is not empty, 
     * otherwise it returns catalina home folder.
     */
    private File getBaseDir() {
        if (jCheckBoxShared.isSelected()) {
            File base = new File(jTextFieldBaseDir.getText());
            if (base.isDirectory()) {
                File[] files = base.listFiles();
                if (files != null && files.length > 0) {
                    return base;
                }
            }
        }
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
            Logger.getLogger(InstallPanelVisual.class.getName()).log(Level.INFO, null, ioe);
        } catch (NumberFormatException nfe) {
            Logger.getLogger(InstallPanelVisual.class.getName()).log(Level.INFO, null, nfe);
        } catch (RuntimeException e) {
            // catch any runtime exception that may occur during graph parsing
            Logger.getLogger(InstallPanelVisual.class.getName()).log(Level.INFO, null, e);
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

        // check the lib directory
        File libDir = TomcatVersion.TOMCAT_60.equals(tomcatVersion)
            ? new File(homeDir, "lib") // NOI18N
            : new File(homeDir, "common" + File.separator + "lib"); // NOI18N
        if (!(libDir.exists() && libDir.isDirectory())) {
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

    private boolean isUsernamePasswordValid() {
        if (createUserCheckBox.isSelected()) {
            if (jTextFieldUsername.getText().length() == 0) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_UsernameEmpty");
                return false;
            }
            if (jTextFieldPassword.getPassword().length == 0) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_PasswordEmpty");
                return false;
            }
        } else {
            if (jTextFieldUsername.getText().length() == 0) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_UsernameEmptyWarning");
            }
            File tomcatUsersXml = new File(getBaseDir(), "conf/tomcat-users.xml");
            try {
                if (!TomcatUsers.userExists(tomcatUsersXml, jTextFieldUsername.getText())) {
                    errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_UserDoesNotExist");
                } else if (!TomcatUsers.hasManagerRole(tomcatUsersXml, jTextFieldUsername.getText())) {
                    errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_UserHasNotManagerRole");
                }
            } catch (IOException e) {
                errorMessage = NbBundle.getMessage(InstallPanelVisual.class, "MSG_MissingOrInvalidTomcatUsersXml", tomcatUsersXml.getPath());
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
        return isHomeValid() && isBaseValid() && !isAlreadyRegistered() && isUsernamePasswordValid();
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
    private javax.swing.JCheckBox createUserCheckBox;
    private javax.swing.JButton jButtonBaseBrowse;
    private javax.swing.JButton jButtonHomeBrowse;
    private javax.swing.JCheckBox jCheckBoxShared;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelBaseDir;
    private javax.swing.JLabel jLabelHomeDir;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JLabel jLabelUsername;
    private javax.swing.JTextField jTextFieldBaseDir;
    private javax.swing.JTextField jTextFieldHomeDir;
    private javax.swing.JPasswordField jTextFieldPassword;
    private javax.swing.JTextField jTextFieldUsername;
    // End of variables declaration//GEN-END:variables
    
}
