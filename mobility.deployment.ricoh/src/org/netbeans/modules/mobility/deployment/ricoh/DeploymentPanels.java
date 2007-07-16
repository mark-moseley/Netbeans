/*
 * DeploymentPanels.java
 *
 * Created on 03 July 2007, 11:20
 */

package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.text.JTextComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import ricoh.util.ssh.SSHKeyFileReader;

/**
 *
 * @author  Lukas Waldmann
 */
public class DeploymentPanels extends javax.swing.JPanel
{
    private static final String SDKJ_1_4       = "1.4";
    private static final String SDKJ_2_0       = "2.0";
    private static final int DEFAULT_SSH_PORT  = 22;
    private static final int DEFAULT_SMB_PORT  = 139;
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_OSGI_PORT = 8080;
    
    static String SSH_DEPLOY     = "scp"; //NOI18N
    static String SMB_DEPLOY     = "samba"; //NOI18N
    static String SD_CARD_DEPLOY = "sdcard"; //NOI18N
    static String HTTP_DEPLOY    = "httppost"; //NOI18N
    
    final private ActionListener fieldListener;
    
    /** Creates new form DeploymentPanels */
    DeploymentPanels()
    {
        initComponents();
        add(scpConfigPanel,SSH_DEPLOY);
        add(sambaConfigPanel,SMB_DEPLOY);
        add(sdCardConfigPanel,SD_CARD_DEPLOY);
        add(httpPostConfigPanel,HTTP_DEPLOY);
        
        //input verification
        PortVerifier verifier = new PortVerifier();
        sshServerHttpPort.setInputVerifier(verifier);
        smbServerHttpPort.setInputVerifier(verifier);
        sshRemotePortTextField.setInputVerifier(verifier);
        smbRemotePortTextField.setInputVerifier(verifier); 
        sdkGroup.add(sdkjV1_4RadioButton);
        sdkGroup.add(sdkjV2_0RadioButton);
        fieldListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateTooltips();
            }
        };
        addTextFieldListeners();
    }
    
    private void updateTooltips()
    {
        //tooltips for configuration JPanels
        this.smbHttpXletPath.setToolTipText(smbHttpXletPath.getText());
        this.smbRemotePathTextField.setToolTipText(smbRemotePathTextField.getText());
        this.sshHttpXletPath.setToolTipText(sshHttpXletPath.getText());
        this.sshRemotePathTextField.setToolTipText(sshRemotePathTextField.getText());
        this.sshKeyFileTextField.setToolTipText(sshKeyFileTextField.getText());
    }
    
    public void addTextFieldListeners()
    {
        //tooltips for configuration JPanels
        this.smbHttpXletPath.addActionListener(fieldListener);
        this.smbRemotePathTextField.addActionListener(fieldListener);
        this.sshHttpXletPath.addActionListener(fieldListener);
        this.sshRemotePathTextField.addActionListener(fieldListener);
        this.sshKeyFileTextField.addActionListener(fieldListener);
    }
    
    void setEditable(boolean b)
    {
        for (Component c1 : getComponents())
        {
            if (c1 instanceof Container)
            {
                for (Component comp : ((Container)c1).getComponents())
                {
                    if (comp instanceof JTextComponent)
                    {
                        ((JTextComponent)comp).setEditable(b);
                    }
                    else if (comp instanceof JButton)
                    {
                        comp.setVisible(b);
                    }
                    else if (comp instanceof JRadioButton)
                    {
                        ((JRadioButton)comp).setEnabled(b);
                    }
                }
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        sambaConfigPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        smbServerHttpPort = new javax.swing.JTextField();
        smbHttpXletPath = new javax.swing.JTextField();
        smbRemotePortTextField = new javax.swing.JTextField();
        smbRemotePathTextField = new javax.swing.JTextField();
        scpConfigPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        sshRemotePathTextField = new javax.swing.JTextField();
        sshRemotePortTextField = new javax.swing.JTextField();
        sshKeyFileTextField = new javax.swing.JTextField();
        browseSSHKeyFileButton = new javax.swing.JButton();
        getSSHKeyButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        sshServerHttpPort = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        sshHttpXletPath = new javax.swing.JTextField();
        sdCardConfigPanel = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        sdPathField = new javax.swing.JTextField();
        sdPathBrowse = new javax.swing.JButton();
        httpPostConfigPanel = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        httpServerPort = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        sdkjV1_4RadioButton = new javax.swing.JRadioButton();
        sdkjV2_0RadioButton = new javax.swing.JRadioButton();
        sdkGroup = new javax.swing.ButtonGroup();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SmbRemotePort"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SmbSharePath"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel14, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SmbHTTPPort"));

        jLabel21.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ServerConnection"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel24, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SmbXletPath"));

        smbServerHttpPort.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SMB_WEBPORT);

        smbHttpXletPath.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SMB_WEBPATH);
        smbHttpXletPath.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                smbHttpXletPathActionPerformed(evt);
            }
        });

        smbRemotePortTextField.setAutoscrolls(false);
        smbRemotePortTextField.setInheritsPopupMenu(true);
        smbRemotePortTextField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SMB_PORT);

        smbRemotePathTextField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SMB_PATH);

        org.jdesktop.layout.GroupLayout sambaConfigPanelLayout = new org.jdesktop.layout.GroupLayout(sambaConfigPanel);
        sambaConfigPanel.setLayout(sambaConfigPanelLayout);
        sambaConfigPanelLayout.setHorizontalGroup(
            sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sambaConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sambaConfigPanelLayout.createSequentialGroup()
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(sambaConfigPanelLayout.createSequentialGroup()
                                .add(jLabel24)
                                .add(37, 37, 37))
                            .add(sambaConfigPanelLayout.createSequentialGroup()
                                .add(jLabel7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(smbHttpXletPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, smbRemotePathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel6)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(smbRemotePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, smbServerHttpPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jLabel21))
                .addContainerGap())
        );
        sambaConfigPanelLayout.setVerticalGroup(
            sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, sambaConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sambaConfigPanelLayout.createSequentialGroup()
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel24)
                            .add(smbHttpXletPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(smbRemotePathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sambaConfigPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(smbServerHttpPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel14))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sambaConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(smbRemotePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpRemotePort"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpKeyFile"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpUploadPath"));

        sshRemotePathTextField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SSH_PATH);

        sshRemotePortTextField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SSH_PORT);

        sshKeyFileTextField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SSH_KEYFILE);

        org.openide.awt.Mnemonics.setLocalizedText(browseSSHKeyFileButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_Browse"));
        browseSSHKeyFileButton.setPreferredSize(new java.awt.Dimension(40, 23));
        browseSSHKeyFileButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                browseSSHKeyFileButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(getSSHKeyButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpGetKey"));
        getSSHKeyButton.setLocation((getWidth() / 2) - (getSSHKeyButton.getSize().width / 2),
            getSSHKeyButton.getHeight());
        getSSHKeyButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                getSSHKeyButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpHTTPPort"));

        sshServerHttpPort.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SSH_WEBPORT);

        jLabel22.setText(org.openide.util.NbBundle.getMessage(DeploymentPanels.class, "LBL_ServerConnection"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_ScpXletPath"));

        sshHttpXletPath.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SSH_WEBPATH);

        org.jdesktop.layout.GroupLayout scpConfigPanelLayout = new org.jdesktop.layout.GroupLayout(scpConfigPanel);
        scpConfigPanel.setLayout(scpConfigPanelLayout);
        scpConfigPanelLayout.setHorizontalGroup(
            scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scpConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scpConfigPanelLayout.createSequentialGroup()
                        .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel22)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, scpConfigPanelLayout.createSequentialGroup()
                                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel13)
                                    .add(jLabel19)
                                    .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(28, 28, 28)
                                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(sshKeyFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                    .add(sshRemotePathTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                                    .add(sshHttpXletPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(scpConfigPanelLayout.createSequentialGroup()
                                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel1)
                                    .add(jLabel9))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(sshRemotePortTextField)
                                    .add(sshServerHttpPort, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)))
                            .add(browseSSHKeyFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(10, 10, 10))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, scpConfigPanelLayout.createSequentialGroup()
                        .add(getSSHKeyButton)
                        .addContainerGap())))
        );
        scpConfigPanelLayout.setVerticalGroup(
            scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scpConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel22)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(sshHttpXletPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sshServerHttpPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel13)
                    .add(sshRemotePathTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sshRemotePortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scpConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(sshKeyFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseSSHKeyFileButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(getSSHKeyButton)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scpConfigPanelLayout.linkSize(new java.awt.Component[] {sshHttpXletPath, sshKeyFileTextField, sshRemotePathTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel15, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SDCardPath"));

        sdPathField.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_SDCARD_PATH);

        org.openide.awt.Mnemonics.setLocalizedText(sdPathBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_SDCardBrowse"));
        sdPathBrowse.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                sdPathBrowseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout sdCardConfigPanelLayout = new org.jdesktop.layout.GroupLayout(sdCardConfigPanel);
        sdCardConfigPanel.setLayout(sdCardConfigPanelLayout);
        sdCardConfigPanelLayout.setHorizontalGroup(
            sdCardConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sdCardConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sdPathField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sdPathBrowse)
                .addContainerGap())
        );
        sdCardConfigPanelLayout.setVerticalGroup(
            sdCardConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, sdCardConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sdCardConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel15)
                    .add(sdPathBrowse)
                    .add(sdPathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(0, 0, Short.MAX_VALUE))
        );
        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_HttpServerPort"));

        httpServerPort.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_HTTP_PORT);

        jLabel16.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("LBL_HttpPlatformType"));

        org.openide.awt.Mnemonics.setLocalizedText(sdkjV1_4RadioButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("RADIO_SDKJ_1.X"));
        sdkjV1_4RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sdkjV1_4RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sdkjV1_4RadioButton.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_HTTP_PLATFORM);

        sdkjV2_0RadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(sdkjV2_0RadioButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("RADIO_SDKJ_2.X"));
        sdkjV2_0RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sdkjV2_0RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sdkjV2_0RadioButton.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_HTTP_PLATFORM);

        org.jdesktop.layout.GroupLayout httpPostConfigPanelLayout = new org.jdesktop.layout.GroupLayout(httpPostConfigPanel);
        httpPostConfigPanel.setLayout(httpPostConfigPanelLayout);
        httpPostConfigPanelLayout.setHorizontalGroup(
            httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(httpPostConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(httpPostConfigPanelLayout.createSequentialGroup()
                        .add(jLabel30)
                        .add(12, 12, 12)
                        .add(httpServerPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel16)
                    .add(httpPostConfigPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sdkjV1_4RadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sdkjV2_0RadioButton)))
                .addContainerGap(182, Short.MAX_VALUE))
        );
        httpPostConfigPanelLayout.setVerticalGroup(
            httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(httpPostConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel30)
                    .add(httpServerPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sdkjV1_4RadioButton)
                    .add(sdkjV2_0RadioButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setLayout(new java.awt.CardLayout());

    }// </editor-fold>//GEN-END:initComponents

    private void sdPathBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sdPathBrowseActionPerformed
    {//GEN-HEADEREND:event_sdPathBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        String workDir = sdPathField.getText();
        if (workDir.equals(""))
        {
            //workDir = FileUtil.toFile(getProject().getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(DeploymentPanels.class, "TITLE_BrowseForSDCardFolder")); //NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this))
        { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            sdPathField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_sdPathBrowseActionPerformed

    private void getSSHKeyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_getSSHKeyButtonActionPerformed
    {//GEN-HEADEREND:event_getSSHKeyButtonActionPerformed
        if (this.sshKeyFileTextField.getText().trim().equals(""))
        {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(DeploymentPanels.class, "MSG_NoKey"), //NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        else
        {
            try
            {
                SSHKeyFileReader reader = new SSHKeyFileReader(this.sshKeyFileTextField.getText());                
                SSHKeyFileReader.SSHKey key = null;
                RicohDeploymentCustomizer rdc=(RicohDeploymentCustomizer)this.getParent();
                
                try
                {
                    //key returned is not an SSHKey, but a container to hold error messages on key failure or the accepted key's signature
                    key = reader.addServerKeyToFile(InetAddress.getByName(rdc.serverTextField.getText()),
                            rdc.usernameTextField.getText(),
                            new String(rdc.passwordField.getPassword()),
                            Integer.parseInt(this.sshRemotePortTextField.getText()));
                }
                catch(NumberFormatException nfe)
                {
                    this.sshRemotePortTextField.setText(Integer.toString(DEFAULT_SSH_PORT));
                    key = reader.addServerKeyToFile(InetAddress.getByName(rdc.serverTextField.getText()),
                            rdc.usernameTextField.getText(),
                            new String(rdc.passwordField.getPassword()),
                            Integer.parseInt(this.sshRemotePortTextField.getText()));
                }
                if ((key != null) && (key.getKeyType().equals(SSHKeyFileReader.ADDOK_TYPE)))
                {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message( NbBundle.getMessage(DeploymentPanels.class, "MSG_KeyAddedInfo",  //NOI18N
                            key.getKey()), NotifyDescriptor.INFORMATION_MESSAGE));
                }
                else
                    if ((key != null) && (key.getKeyType().equals(SSHKeyFileReader.CHECKOK_TYPE)))
                    {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message( NbBundle.getMessage(DeploymentPanels.class, "MSG_KeyOK") //NOI18N
                    , NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                    else
                    {
                    if (key == null)
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message( NbBundle.getMessage(DeploymentPanels.class, "MSG_BadKey"), //NOI18N
                                NotifyDescriptor.ERROR_MESSAGE));
                    else
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message( key.getKey(),
                                NotifyDescriptor.ERROR_MESSAGE));
                    }
            }
            catch (IOException ex)
            {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message( NbBundle.getMessage(DeploymentPanels.class, "MSG_BadKey"), //NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }
    }//GEN-LAST:event_getSSHKeyButtonActionPerformed

    private void browseSSHKeyFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseSSHKeyFileButtonActionPerformed
    {//GEN-HEADEREND:event_browseSSHKeyFileButtonActionPerformed
// TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setDialogTitle(NbBundle.getMessage(DeploymentPanels.class, "TITLE_SSHKeySelect")); //NOI18N
        chooser.setApproveButtonText(NbBundle.getMessage(DeploymentPanels.class, "LBL_Select")); //NOI18N
        boolean badFileSelected = true;
        int answer;
        
        //select file in chooser dialog if already has an entry
        if (sshKeyFileTextField.getText().trim().equals("") == false)
        {
            File currentSelectedKeyFile = new File(sshKeyFileTextField.getText());
            chooser.setCurrentDirectory(currentSelectedKeyFile.getParentFile());
            chooser.setSelectedFile(currentSelectedKeyFile);
        }
        
        //show the modal choose dialog and process the outcome
        do
        {
            answer = chooser.showOpenDialog(((JButton)evt.getSource()).getParent());
            if (answer == JFileChooser.APPROVE_OPTION)
            {
                //check for existing file
                if (chooser.getSelectedFile().exists() == false)
                {
                    
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(DeploymentPanels.class, "MSG_ConfirmNew", chooser.getSelectedFile().getAbsolutePath()), //NOI18N
                            NotifyDescriptor.YES_NO_OPTION);
                    
                    DialogDisplayer.getDefault().notify(nd);
                    if (nd.getValue() == NotifyDescriptor.YES_OPTION)
                    {
                        try
                        {
                            if (chooser.getSelectedFile().createNewFile())
                                this.sshKeyFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                            badFileSelected = false;
                        }
                        catch(IOException e)
                        {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(NbBundle.getMessage(DeploymentPanels.class, "ERR_SSHCreateKeyFileError", chooser.getSelectedFile().getName()),  //NOI18N
                                    NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                }
                else
                    if (isSSHFile(chooser.getSelectedFile()) == false)
                    {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(DeploymentPanels.class, "ERR_InvalidSSHKeyFile", chooser.getSelectedFile().getName()),  //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE));
                    }
                    else
                    {
                    this.sshKeyFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                    badFileSelected = false;
                    }
            }
            else
                badFileSelected = false;
        }
        while(badFileSelected);
    }//GEN-LAST:event_browseSSHKeyFileButtonActionPerformed

    private void smbHttpXletPathActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_smbHttpXletPathActionPerformed
    {//GEN-HEADEREND:event_smbHttpXletPathActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_smbHttpXletPathActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseSSHKeyFileButton;
    private javax.swing.JButton getSSHKeyButton;
    private javax.swing.JPanel httpPostConfigPanel;
    private javax.swing.JTextField httpServerPort;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel sambaConfigPanel;
    private javax.swing.JPanel scpConfigPanel;
    private javax.swing.JPanel sdCardConfigPanel;
    private javax.swing.JButton sdPathBrowse;
    private javax.swing.JTextField sdPathField;
    private javax.swing.ButtonGroup sdkGroup;
    private javax.swing.JRadioButton sdkjV1_4RadioButton;
    private javax.swing.JRadioButton sdkjV2_0RadioButton;
    private javax.swing.JTextField smbHttpXletPath;
    private javax.swing.JTextField smbRemotePathTextField;
    private javax.swing.JTextField smbRemotePortTextField;
    private javax.swing.JTextField smbServerHttpPort;
    private javax.swing.JTextField sshHttpXletPath;
    private javax.swing.JTextField sshKeyFileTextField;
    private javax.swing.JTextField sshRemotePathTextField;
    private javax.swing.JTextField sshRemotePortTextField;
    private javax.swing.JTextField sshServerHttpPort;
    // End of variables declaration//GEN-END:variables

    //Custom code
    private boolean isSSHFile(File target)
    {
        if (target.isDirectory())
            return false;
        else
        if (target.canRead())
        {
            try
            {
                SSHKeyFileReader reader = new SSHKeyFileReader(target);
                return reader.isValid();
            }
            catch(IOException ioe)
            {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(RicohDeploymentCustomizer.class, "ERR_MediaIOError"),  //NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
                return false;
            }
        }
        else
            return false;
    }
}
