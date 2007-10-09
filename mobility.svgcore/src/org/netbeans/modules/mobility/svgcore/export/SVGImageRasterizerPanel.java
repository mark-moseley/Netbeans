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
/*
 * SVGAnimationRasterizer.java
 *
 * Created on November 30, 2005, 10:53 AM
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.export.AnimationRasterizer.ColorReductionMethod;
import org.netbeans.modules.mobility.svgcore.export.AnimationRasterizer.ImageType;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Benes
 */
public final class SVGImageRasterizerPanel extends SVGRasterizerPanel {
    private final ComponentGroup m_currentTime;
    /** Creates new form SVGAnimationRasterizer */
    public SVGImageRasterizerPanel(SVGDataObject dObj, String elementId) throws IOException, BadLocationException {
        super(dObj, elementId);
        initComponents();
        
        m_currentTime = createTimeGroup( currentTimeSpinner, animationSlider, true);
        createCompressionGroup( compressionLevelCombo, compressionQualitySpinner);
        
        radioExportAll.setEnabled(isInProject() && m_elementId == null);
        radioExportCurrent.setEnabled(isInProject());
        
        m_ratio = m_dim.getHeight() / m_dim.getWidth();
        spinnerHeight.setModel(new SpinnerNumberModel((int)m_dim.getHeight(), 1, 2048, 1));
        spinnerWidth.setModel(new SpinnerNumberModel((int)m_dim.getWidth(), 1, 2048, 1));
        spinnerWidth.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (keepRatio.isSelected()){
                    spinnerHeight.setValue(new Integer((int)(((Integer)spinnerWidth.getValue()).doubleValue() * m_ratio)));
                }
                updateImage(spinnerWidth, true);
            }
        });
        
        updateImage(null, true);
    }
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        sizePanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        spinnerWidth = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
        spinnerHeight = new javax.swing.JSpinner();
        keepRatio = new javax.swing.JCheckBox();
        optionsPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        formatComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        progressiveCheckBox = new javax.swing.JCheckBox();
        compressionLabel = new javax.swing.JLabel();
        compressionLevelCombo = new JComboBox( AnimationRasterizer.CompressionLevel.values());
        compressionQualityLabel = new javax.swing.JLabel();
        transparentCheckBox = new javax.swing.JCheckBox();
        compressionQualitySpinner = new javax.swing.JSpinner();
        reductionLabel = new javax.swing.JLabel();
        reductionCombo = new JComboBox( AnimationRasterizer.ColorReductionMethod.values());
        timeLinePanel = new javax.swing.JPanel();
        currentTimeSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 30.0, 1.0));
        javax.swing.JLabel startTimeLabel = new javax.swing.JLabel();
        animationSlider = new javax.swing.JSlider();
        exportPanel = new javax.swing.JPanel();
        radioExportCurrent = new javax.swing.JRadioButton();
        radioExportAll = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        imageHolder = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        javax.swing.JLabel previewFormatLabel = new javax.swing.JLabel();
        javax.swing.JLabel previewSizeLabel = new javax.swing.JLabel();
        javax.swing.JLabel previewFileLabel = new javax.swing.JLabel();
        previewFormatText = new javax.swing.JTextField();
        previewSizeText = new javax.swing.JTextField();
        previewFileText = new javax.swing.JTextField();

        sizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_ImageSize"))); // NOI18N

        jLabel11.setLabelFor(spinnerWidth);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationImageWidth")); // NOI18N
        jLabel11.setName(""); // NOI18N

        jLabel12.setLabelFor(spinnerHeight);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationImageHeight")); // NOI18N

        spinnerHeight.setEnabled(false);

        keepRatio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(keepRatio, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationKeepRatio")); // NOI18N
        keepRatio.setToolTipText("Images for other configurations are transformed using screen ratio.");
        keepRatio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        keepRatio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keepRatio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepRatioActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout sizePanelLayout = new org.jdesktop.layout.GroupLayout(sizePanel);
        sizePanel.setLayout(sizePanelLayout);
        sizePanelLayout.setHorizontalGroup(
            sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sizePanelLayout.createSequentialGroup()
                .add(sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(sizePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel11)
                            .add(jLabel12))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(spinnerHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(spinnerWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(sizePanelLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(keepRatio)))
                .addContainerGap(91, Short.MAX_VALUE))
        );
        sizePanelLayout.setVerticalGroup(
            sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sizePanelLayout.createSequentialGroup()
                .add(sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(spinnerWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sizePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(spinnerHeight, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keepRatio))
        );

        spinnerWidth.getAccessibleContext().setAccessibleName("Image width");
        spinnerHeight.getAccessibleContext().setAccessibleName("Image height");
        keepRatio.getAccessibleContext().setAccessibleDescription("");

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_ImageOptions"))); // NOI18N

        jLabel2.setLabelFor(formatComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsFormat")); // NOI18N

        formatComboBox.setModel(createImageTypeComboBoxModel());
        formatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(progressiveCheckBox, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsProgressive")); // NOI18N
        progressiveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        progressiveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compressionLabel.setLabelFor(compressionLevelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(compressionLabel, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsQuality")); // NOI18N

        compressionQualityLabel.setLabelFor(compressionQualitySpinner);
        org.openide.awt.Mnemonics.setLocalizedText(compressionQualityLabel, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsRate")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(transparentCheckBox, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsTransparent")); // NOI18N
        transparentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transparentCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transparentCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transparencyChanged(evt);
            }
        });

        reductionLabel.setLabelFor(reductionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(reductionLabel, org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_OptionsColorReduction")); // NOI18N

        reductionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorReductionChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(245, 245, 245)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(transparentCheckBox)
                        .addContainerGap(102, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(compressionLabel)
                        .addContainerGap(76, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, reductionCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, reductionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(54, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, optionsPanelLayout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(formatComboBox, 0, 87, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, progressiveCheckBox)
                            .add(optionsPanelLayout.createSequentialGroup()
                                .add(compressionLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(compressionQualityLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(compressionQualitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(106, 106, 106))))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(formatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(progressiveCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(transparentCheckBox)
                .add(14, 14, 14)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reductionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(reductionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(compressionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(compressionQualitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(compressionLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(compressionQualityLabel))
                .addContainerGap())
        );

        formatComboBox.getAccessibleContext().setAccessibleName("Image format");
        formatComboBox.getAccessibleContext().setAccessibleDescription("Image format");
        progressiveCheckBox.getAccessibleContext().setAccessibleDescription("Progressive display");
        compressionLevelCombo.getAccessibleContext().setAccessibleName("Compression level");
        compressionLevelCombo.getAccessibleContext().setAccessibleDescription("Compression level");
        transparentCheckBox.getAccessibleContext().setAccessibleDescription("Transparent background");
        compressionQualitySpinner.getAccessibleContext().setAccessibleName("Compression quality");
        reductionLabel.getAccessibleContext().setAccessibleName("Color reduction method:");
        reductionLabel.getAccessibleContext().setAccessibleDescription("Color reduction method");
        reductionCombo.getAccessibleContext().setAccessibleName("Color reduction method");

        timeLinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_AnimationFrameTime"))); // NOI18N

        startTimeLabel.setLabelFor(currentTimeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(startTimeLabel, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationTime")); // NOI18N

        org.jdesktop.layout.GroupLayout timeLinePanelLayout = new org.jdesktop.layout.GroupLayout(timeLinePanel);
        timeLinePanel.setLayout(timeLinePanelLayout);
        timeLinePanelLayout.setHorizontalGroup(
            timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(timeLinePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(timeLinePanelLayout.createSequentialGroup()
                        .add(startTimeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(currentTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(animationSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
                .addContainerGap())
        );
        timeLinePanelLayout.setVerticalGroup(
            timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(timeLinePanelLayout.createSequentialGroup()
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startTimeLabel)
                    .add(currentTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(animationSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        animationSlider.getAccessibleContext().setAccessibleName("Current time selector");
        animationSlider.getAccessibleContext().setAccessibleDescription("Current time selector");

        exportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_ExportLabel"))); // NOI18N

        buttonGroup1.add(radioExportCurrent);
        radioExportCurrent.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportCurrent, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationOnlyActiveConfiguration")); // NOI18N
        radioExportCurrent.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioExportCurrent.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(radioExportAll);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportAll, org.openide.util.NbBundle.getBundle(SVGImageRasterizerPanel.class).getString("LBL_AnimationAllConfigurations")); // NOI18N
        radioExportAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioExportAll.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout exportPanelLayout = new org.jdesktop.layout.GroupLayout(exportPanel);
        exportPanel.setLayout(exportPanelLayout);
        exportPanelLayout.setHorizontalGroup(
            exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioExportCurrent)
                    .add(radioExportAll))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        exportPanelLayout.setVerticalGroup(
            exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportPanelLayout.createSequentialGroup()
                .add(radioExportCurrent)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(radioExportAll))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_PreviewTitle"))); // NOI18N

        imageHolder.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        previewFormatLabel.setLabelFor(previewFormatText);
        previewFormatLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_PreviewFormat")); // NOI18N

        previewSizeLabel.setLabelFor(previewSizeText);
        previewSizeLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_PreviewSize")); // NOI18N

        previewFileLabel.setLabelFor(previewFileText);
        previewFileLabel.setText(org.openide.util.NbBundle.getMessage(SVGImageRasterizerPanel.class, "LBL_PreviewFile")); // NOI18N

        previewFormatText.setEditable(false);
        previewFormatText.setText("JPEG");

        previewSizeText.setEditable(false);
        previewSizeText.setText("200 KBytes");

        previewFileText.setEditable(false);
        previewFileText.setText("C:\\Program Files\\about.svg");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(previewFormatLabel)
                    .add(previewSizeLabel)
                    .add(previewFileLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(previewSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewFileText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .add(previewFormatText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewFormatLabel)
                    .add(previewFormatText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewSizeLabel)
                    .add(previewSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewFileLabel)
                    .add(previewFileText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        previewFormatText.getAccessibleContext().setAccessibleDescription("File format");
        previewSizeText.getAccessibleContext().setAccessibleDescription("File size");
        previewFileText.getAccessibleContext().setAccessibleDescription("File location");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, imageHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(imageHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(sizePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(timeLinePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(exportPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(optionsPanel, 0, 221, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(sizePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(optionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 233, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(timeLinePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void colorReductionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorReductionChanged
    updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_colorReductionChanged

private void transparencyChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transparencyChanged
    updateImage((JComponent) evt.getSource(), true);
}//GEN-LAST:event_transparencyChanged

private void formatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatComboBoxActionPerformed
     updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_formatComboBoxActionPerformed

    private void keepRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepRatioActionPerformed
     spinnerHeight.setEnabled( !keepRatio.isSelected());   
}//GEN-LAST:event_keepRatioActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider animationSlider;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel compressionLabel;
    private javax.swing.JComboBox compressionLevelCombo;
    private javax.swing.JLabel compressionQualityLabel;
    private javax.swing.JSpinner compressionQualitySpinner;
    private javax.swing.JSpinner currentTimeSpinner;
    private javax.swing.JPanel exportPanel;
    private javax.swing.JComboBox formatComboBox;
    private javax.swing.JScrollPane imageHolder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox keepRatio;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTextField previewFileText;
    private javax.swing.JTextField previewFormatText;
    private javax.swing.JTextField previewSizeText;
    private javax.swing.JCheckBox progressiveCheckBox;
    private javax.swing.JRadioButton radioExportAll;
    private javax.swing.JRadioButton radioExportCurrent;
    private javax.swing.JComboBox reductionCombo;
    private javax.swing.JLabel reductionLabel;
    private javax.swing.JPanel sizePanel;
    private javax.swing.JSpinner spinnerHeight;
    private javax.swing.JSpinner spinnerWidth;
    private javax.swing.JPanel timeLinePanel;
    private javax.swing.JCheckBox transparentCheckBox;
    // End of variables declaration//GEN-END:variables

    public int getImageWidth(){
        return m_overrideWidth != -1 ? m_overrideWidth : ((Integer)spinnerWidth.getValue()).intValue();
    }
    
    public int getImageHeight(){
        return m_overrideHeight != -1 ? m_overrideHeight : ((Integer)spinnerHeight.getValue()).intValue();
    }
        
    public boolean isForAllConfigurations(){
        return radioExportAll.isSelected();
    }
        
    public float getCompressionQuality() {
        int sliderValue = ((Number)compressionQualitySpinner.getValue()).intValue();
        return ((float)sliderValue)/100f;
    }
    
    public boolean isProgressive() {
        return progressiveCheckBox.isSelected();
    }
    
    public boolean isTransparent() {
        return transparentCheckBox.isSelected();
    }
        
    public AnimationRasterizer.ImageType getImageType() {
        return (AnimationRasterizer.ImageType)formatComboBox.getSelectedItem();
    }  

    public ColorReductionMethod getColorReductionMethod() {
        return (ColorReductionMethod) reductionCombo.getSelectedItem();
    }
    
    public float getStartTime(){
        return m_currentTime.getValue();
    }
    
    public int getNumberFrames() {
        return 1;
    }
    
    public boolean isInSingleImage() {
        return true;
    }
        
    protected void updateImage(JComponent source, boolean isOutputChanged) {
        final JLabel label = new JLabel( "Updating image...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        imageHolder.setViewportView(label);
        
        final ImageType imgType = getImageType();
        boolean supportsCompression = imgType.supportsCompression();
        
        compressionQualitySpinner.setEnabled(supportsCompression);
        compressionLevelCombo.setEnabled(supportsCompression);
        compressionLabel.setEnabled(supportsCompression);
        compressionQualityLabel.setEnabled(supportsCompression);
        
        boolean needsColorReduction = imgType.needsColorReduction();
        reductionCombo.setEnabled(needsColorReduction);
        reductionLabel.setEnabled(needsColorReduction);
        
        if (imgType.supportsTransparency()) {
            transparentCheckBox.setEnabled(true);
        } else {
            transparentCheckBox.setEnabled(false);
            transparentCheckBox.setSelected(false);
        }
        
        String filenameRoot      = AnimationRasterizer.createFileNameRoot(m_dObj, this, null, true);
        final String fileName    = AnimationRasterizer.createFileName(filenameRoot, this, -1, -1);
        final float  currentTime = m_currentTime.getValue();
        
        RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                try {
                    final AnimationRasterizer.PreviewInfo preview = AnimationRasterizer.previewFrame( getSVGImage(), SVGImageRasterizerPanel.this, -1, currentTime);
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            String sizeText;
                            if ( preview.m_imageSize < 1024) {
                                sizeText = preview.m_imageSize + " Bytes";
                            } else {
                                sizeText = (Math.round(preview.m_imageSize / 102.4) / 10.0) + " KBytes";
                            }
                            previewSizeText.setText( sizeText);
                            previewFormatText.setText(preview.m_imageFormat);
                            //TODO Handle images not saved yet
                            previewFileText.setText( fileName);
                            label.setText(null);
                            label.setIcon(new ImageIcon(preview.m_image));
                            label.invalidate();
                            imageHolder.validate();
                            imageHolder.repaint();
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } 
            }
        });
    }
}
    
    
