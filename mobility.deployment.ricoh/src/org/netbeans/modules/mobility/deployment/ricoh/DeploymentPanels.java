/*
 * DeploymentPanels.java
 *
 * Created on 03 July 2007, 11:20
 */

package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.text.JTextComponent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Lukas Waldmann
 */
public class DeploymentPanels extends javax.swing.JPanel
{
    /** Creates new form DeploymentPanels */
    DeploymentPanels()
    {
        initComponents();
        add(sdCardConfigPanel,DeploymentComboBoxModel.SD_CARD_DEPLOY);
        add(httpPostConfigPanel,DeploymentComboBoxModel.HTTP_DEPLOY);
        
        //input verification
        PortVerifier verifier = new PortVerifier();
        httpServerPort.setInputVerifier(verifier);
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
                .add(jLabel15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sdPathField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sdPathBrowse))
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

        sdkGroup.add(sdkjV1_4RadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(sdkjV1_4RadioButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/mobility/deployment/ricoh/Bundle").getString("RADIO_SDKJ_1.X"));
        sdkjV1_4RadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sdkjV1_4RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sdkjV1_4RadioButton.setName(RicohDeploymentProperties.PROP_RICOH_DEPLOY_HTTP_PLATFORM);

        sdkGroup.add(sdkjV2_0RadioButton);
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
                .add(httpPostConfigPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(httpPostConfigPanelLayout.createSequentialGroup()
                        .add(jLabel30)
                        .add(12, 12, 12)
                        .add(httpServerPort, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel16)
                    .add(httpPostConfigPanelLayout.createSequentialGroup()
                        .add(sdkjV1_4RadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sdkjV2_0RadioButton)))
                .addContainerGap(192, Short.MAX_VALUE))
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
                .add(0, 0, Short.MAX_VALUE))
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
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel httpPostConfigPanel;
    private javax.swing.JTextField httpServerPort;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JPanel sdCardConfigPanel;
    private javax.swing.JButton sdPathBrowse;
    private javax.swing.JTextField sdPathField;
    private javax.swing.ButtonGroup sdkGroup;
    private javax.swing.JRadioButton sdkjV1_4RadioButton;
    private javax.swing.JRadioButton sdkjV2_0RadioButton;
    // End of variables declaration//GEN-END:variables
}
