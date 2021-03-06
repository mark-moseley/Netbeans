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

import java.awt.Dimension;
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
public final class SVGAnimationRasterizerPanel extends SVGRasterizerPanel {
    private       SpinnerNumberModel m_previewSpinnerModel;
    private final ComponentGroup     m_startTime;    
    private final ComponentGroup     m_stopTime;    
    private       Thread             m_sizeCalculationThread;

    /** Creates new form SVGAnimationRasterizer */
    public SVGAnimationRasterizerPanel(SVGDataObject dObj) throws IOException, BadLocationException {
        super(dObj, null);
        initComponents();
        createCompressionGroup( compressionLevelCombo, compressionQualitySpinner);
        
        m_startTime = createTimeGroup( startTimeSpinner, startTimeSlider, true);
        m_stopTime  = createTimeGroup( stopTimeSpinner, stopTimeSlider, false);
                
        radioExportAll.setEnabled(isInProject());
        m_ratio = m_dim.getHeight() / m_dim.getWidth();
        spinnerHeight.setModel(new SpinnerNumberModel((int)m_dim.getHeight(), 1, 2048, 1));
        spinnerWidth.setModel(new SpinnerNumberModel((int)m_dim.getWidth(), 1, 2048, 1));
        
        framesPerSecSpinner.setModel( new SpinnerNumberModel( 2, 0.1, 30, 1));
        previewFrameSpinner.setModel( m_previewSpinnerModel = new SpinnerNumberModel(1, 1, 10, 1));
        
        spinnerWidth.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (keepRatio.isSelected()){
                    spinnerHeight.setValue(new Integer((int)(((Integer)spinnerWidth.getValue()).doubleValue() * m_ratio)));
                }
                updateImage(spinnerWidth, true);
            }
        });

        m_previewSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateImage(previewFrameSpinner, false);
            }
        });

        framesPerSecSpinner.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateImage(framesPerSecSpinner, true);
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
        compressionQualitySpinner = new javax.swing.JSpinner();
        reductionLabel = new javax.swing.JLabel();
        reductionCombo = new JComboBox( AnimationRasterizer.ColorReductionMethod.values());
        transparentCheckBox = new javax.swing.JCheckBox();
        timeLinePanel = new javax.swing.JPanel();
        startTimeSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 30.0, 1.0));
        javax.swing.JLabel startTimeLabel = new javax.swing.JLabel();
        javax.swing.JLabel stopTimeLabel = new javax.swing.JLabel();
        stopTimeSpinner = new javax.swing.JSpinner();
        startTimeSlider = new javax.swing.JSlider();
        stopTimeSlider = new javax.swing.JSlider();
        javax.swing.JLabel framesPerSecLabel = new javax.swing.JLabel();
        framesPerSecSpinner = new javax.swing.JSpinner();
        exportPanel = new javax.swing.JPanel();
        radioExportCurrent = new javax.swing.JRadioButton();
        radioExportAll = new javax.swing.JRadioButton();
        allFramesCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        previewFormatText = new javax.swing.JTextField();
        previewFrameSizeText = new javax.swing.JTextField();
        previewFileText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        previewAnimationSizeText = new javax.swing.JTextField();
        imageHolder = new javax.swing.JScrollPane();
        javax.swing.JLabel previewLabel = new javax.swing.JLabel();
        previewFrameSpinner = new javax.swing.JSpinner();
        previewMaxFrameText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        previewCurrentTimeText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        previewEndTimeText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();

        setOpaque(false);

        sizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ImageSize"))); // NOI18N

        jLabel11.setLabelFor(spinnerWidth);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationImageWidth")); // NOI18N

        jLabel12.setLabelFor(spinnerHeight);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationImageHeight")); // NOI18N

        spinnerHeight.setEnabled(false);

        keepRatio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(keepRatio, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationKeepRatio")); // NOI18N
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
                .addContainerGap(97, Short.MAX_VALUE))
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
                .add(keepRatio)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel11.getAccessibleContext().setAccessibleName("");
        jLabel11.getAccessibleContext().setAccessibleDescription("");
        jLabel12.getAccessibleContext().setAccessibleName("");
        keepRatio.getAccessibleContext().setAccessibleDescription("");

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ImageOptions"))); // NOI18N

        jLabel2.setLabelFor(formatComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsFormat")); // NOI18N

        formatComboBox.setModel(createImageTypeComboBoxModel());
        formatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(progressiveCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsProgressive")); // NOI18N
        progressiveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        progressiveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compressionLabel.setLabelFor(compressionLevelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(compressionLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsQuality")); // NOI18N

        compressionQualityLabel.setLabelFor(compressionQualitySpinner);
        org.openide.awt.Mnemonics.setLocalizedText(compressionQualityLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsRate")); // NOI18N

        reductionLabel.setLabelFor(reductionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(reductionLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsColorReduction")); // NOI18N

        reductionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorReductionChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(transparentCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsTransparent")); // NOI18N
        transparentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        transparentCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transparentCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transparentCheckBoxActionPerformed(evt);
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
                        .add(84, 84, 84)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(formatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 93, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(progressiveCheckBox)
                        .addContainerGap(112, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(compressionLabel)
                            .add(optionsPanelLayout.createSequentialGroup()
                                .add(compressionLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(compressionQualityLabel)
                                .add(5, 5, 5)
                                .add(compressionQualitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(40, 40, 40))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(transparentCheckBox)
                        .addContainerGap(108, Short.MAX_VALUE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, reductionCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, reductionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(60, Short.MAX_VALUE))))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reductionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                .add(compressionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(compressionLevelCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(compressionQualityLabel)
                    .add(compressionQualitySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel2.getAccessibleContext().setAccessibleName("");
        reductionCombo.getAccessibleContext().setAccessibleName("Color reduction method");

        timeLinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationFrameTime"))); // NOI18N

        startTimeLabel.setLabelFor(startTimeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(startTimeLabel, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationTime")); // NOI18N

        stopTimeLabel.setLabelFor(stopTimeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(stopTimeLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationEndTime")); // NOI18N

        framesPerSecLabel.setLabelFor(framesPerSecSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(framesPerSecLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationFramePerSec")); // NOI18N

        org.jdesktop.layout.GroupLayout timeLinePanelLayout = new org.jdesktop.layout.GroupLayout(timeLinePanel);
        timeLinePanel.setLayout(timeLinePanelLayout);
        timeLinePanelLayout.setHorizontalGroup(
            timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(timeLinePanelLayout.createSequentialGroup()
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(timeLinePanelLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(framesPerSecLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 37, Short.MAX_VALUE)
                        .add(framesPerSecSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(startTimeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, stopTimeSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .add(timeLinePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(timeLinePanelLayout.createSequentialGroup()
                                .add(stopTimeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 44, Short.MAX_VALUE)
                                .add(stopTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(timeLinePanelLayout.createSequentialGroup()
                                .add(startTimeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 43, Short.MAX_VALUE)
                                .add(startTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 83, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        timeLinePanelLayout.setVerticalGroup(
            timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(timeLinePanelLayout.createSequentialGroup()
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startTimeLabel)
                    .add(startTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(startTimeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stopTimeLabel)
                    .add(stopTimeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stopTimeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(timeLinePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(framesPerSecLabel)
                    .add(framesPerSecSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(24, 24, 24))
        );

        startTimeSlider.getAccessibleContext().setAccessibleName("Start time");
        startTimeSlider.getAccessibleContext().setAccessibleDescription("Start time");
        stopTimeSlider.getAccessibleContext().setAccessibleName("End time");
        stopTimeSlider.getAccessibleContext().setAccessibleDescription("End time");

        exportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ExportLabel"))); // NOI18N

        buttonGroup1.add(radioExportCurrent);
        radioExportCurrent.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportCurrent, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationOnlyActiveConfiguration")); // NOI18N
        radioExportCurrent.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioExportCurrent.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(radioExportAll);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportAll, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationAllConfigurations")); // NOI18N
        radioExportAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        radioExportAll.setMargin(new java.awt.Insets(0, 0, 0, 0));

        allFramesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(allFramesCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationInSingleFile")); // NOI18N
        allFramesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allFramesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allFramesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allFramesInSingleFileChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout exportPanelLayout = new org.jdesktop.layout.GroupLayout(exportPanel);
        exportPanel.setLayout(exportPanelLayout);
        exportPanelLayout.setHorizontalGroup(
            exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioExportAll)
                    .add(allFramesCheckBox)
                    .add(radioExportCurrent))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        exportPanelLayout.setVerticalGroup(
            exportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(exportPanelLayout.createSequentialGroup()
                .add(radioExportCurrent)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioExportAll)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                .add(allFramesCheckBox))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewTitle"))); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setLabelFor(previewFormatText);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewFormat")); // NOI18N

        jLabel1.setLabelFor(previewFrameSizeText);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewSize")); // NOI18N

        jLabel4.setLabelFor(previewFileText);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewFile")); // NOI18N

        previewFormatText.setEditable(false);
        previewFormatText.setText("JPEG");
        previewFormatText.setToolTipText("Image format");

        previewFrameSizeText.setEditable(false);
        previewFrameSizeText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        previewFrameSizeText.setText("5.6KBytes");
        previewFrameSizeText.setToolTipText("Single frame size");

        previewFileText.setEditable(false);
        previewFileText.setText("C:\\Program Files\\about.svg");
        previewFileText.setToolTipText("File location");

        jLabel5.setText("/");

        previewAnimationSizeText.setEditable(false);
        previewAnimationSizeText.setText("132.5 KBytes");
        previewAnimationSizeText.setToolTipText("Whole animation size");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel3)
                    .add(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel1))
                    .add(jLabel4))
                .add(13, 13, 13)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(previewFrameSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewAnimationSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(previewFileText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(previewFormatText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(previewFormatText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel5)
                    .add(previewFrameSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewAnimationSizeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(previewFileText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        previewFormatText.getAccessibleContext().setAccessibleDescription("");
        previewFrameSizeText.getAccessibleContext().setAccessibleName("");
        previewFrameSizeText.getAccessibleContext().setAccessibleDescription("");
        previewFileText.getAccessibleContext().setAccessibleName("");
        previewFileText.getAccessibleContext().setAccessibleDescription("");
        previewAnimationSizeText.getAccessibleContext().setAccessibleName("");

        imageHolder.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        imageHolder.setPreferredSize(new java.awt.Dimension(300, 300));

        previewLabel.setLabelFor(previewFrameSpinner);
        previewLabel.setText(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationPreviewFrame")); // NOI18N

        previewMaxFrameText.setEditable(false);
        previewMaxFrameText.setText("30");
        previewMaxFrameText.setToolTipText("Number of frames");

        jLabel7.setText(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationPreviewTime")); // NOI18N

        previewCurrentTimeText.setEditable(false);
        previewCurrentTimeText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        previewCurrentTimeText.setText("0");
        previewCurrentTimeText.setToolTipText("Current time");

        jLabel8.setText("/");

        jLabel9.setText("/");

        previewEndTimeText.setEditable(false);
        previewEndTimeText.setText("60");
        previewEndTimeText.setToolTipText("Duration");

        jLabel10.setText("[s]");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, imageHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3Layout.createSequentialGroup()
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewFrameSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewMaxFrameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 62, Short.MAX_VALUE)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewCurrentTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewEndTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel10)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewLabel)
                    .add(previewFrameSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9)
                    .add(jLabel8)
                    .add(jLabel10)
                    .add(jLabel7)
                    .add(previewMaxFrameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewCurrentTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewEndTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(imageHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        previewMaxFrameText.getAccessibleContext().setAccessibleName("");
        previewCurrentTimeText.getAccessibleContext().setAccessibleName("Current time");
        previewEndTimeText.getAccessibleContext().setAccessibleName("End time");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(timeLinePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(optionsPanel, 0, 227, Short.MAX_VALUE)
                    .add(sizePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(exportPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .add(optionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(timeLinePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(exportPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void transparentCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transparentCheckBoxActionPerformed
    updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_transparentCheckBoxActionPerformed

private void colorReductionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorReductionChanged
    updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_colorReductionChanged

private void allFramesInSingleFileChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allFramesInSingleFileChanged
    updateImage((JComponent) evt.getSource(), true);
}//GEN-LAST:event_allFramesInSingleFileChanged

private void formatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatComboBoxActionPerformed
     updateImage((JComponent) evt.getSource(), true);
}//GEN-LAST:event_formatComboBoxActionPerformed

    private void keepRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepRatioActionPerformed
     spinnerHeight.setEnabled( !keepRatio.isSelected());   
}//GEN-LAST:event_keepRatioActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allFramesCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel compressionLabel;
    private javax.swing.JComboBox compressionLevelCombo;
    private javax.swing.JLabel compressionQualityLabel;
    private javax.swing.JSpinner compressionQualitySpinner;
    private javax.swing.JPanel exportPanel;
    private javax.swing.JComboBox formatComboBox;
    private javax.swing.JSpinner framesPerSecSpinner;
    private javax.swing.JScrollPane imageHolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox keepRatio;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTextField previewAnimationSizeText;
    private javax.swing.JTextField previewCurrentTimeText;
    private javax.swing.JTextField previewEndTimeText;
    private javax.swing.JTextField previewFileText;
    private javax.swing.JTextField previewFormatText;
    private javax.swing.JTextField previewFrameSizeText;
    private javax.swing.JSpinner previewFrameSpinner;
    private javax.swing.JTextField previewMaxFrameText;
    private javax.swing.JCheckBox progressiveCheckBox;
    private javax.swing.JRadioButton radioExportAll;
    private javax.swing.JRadioButton radioExportCurrent;
    private javax.swing.JComboBox reductionCombo;
    private javax.swing.JLabel reductionLabel;
    private javax.swing.JPanel sizePanel;
    private javax.swing.JSpinner spinnerHeight;
    private javax.swing.JSpinner spinnerWidth;
    private javax.swing.JSlider startTimeSlider;
    private javax.swing.JSpinner startTimeSpinner;
    private javax.swing.JSlider stopTimeSlider;
    private javax.swing.JSpinner stopTimeSpinner;
    private javax.swing.JPanel timeLinePanel;
    private javax.swing.JCheckBox transparentCheckBox;
    // End of variables declaration//GEN-END:variables
    
    public int getImageWidth(){
        return m_overrideWidth != -1 ? m_overrideWidth : ((Integer)spinnerWidth.getValue()).intValue();
    }
    
    public int getImageHeight(){
        return m_overrideHeight != -1 ? m_overrideHeight : ((Integer)spinnerHeight.getValue()).intValue();
    }
        
    public float getStartTime(){
        return ((Double)startTimeSpinner.getValue()).floatValue();
    }

    public float getEndTime(){
        return ((Double)stopTimeSpinner.getValue()).floatValue();
    }
    
    public float getFramesPerSecond(){
        return ((Double)framesPerSecSpinner.getValue()).floatValue();
    }
    
    public boolean isForAllConfigurations(){
        return radioExportAll.isSelected();
    }
        
    public float getCompressionQuality() {
        int value = ((Integer)compressionQualitySpinner.getValue()).intValue();
        return ((float)value)/100f;
    }
    
    public boolean isProgressive() {
        return progressiveCheckBox.isSelected();
    }
    
    public boolean isTransparent() {
        return transparentCheckBox.isSelected();
    }
    
    public boolean isInSingleImage() {
        return allFramesCheckBox.isSelected();
    }
    
    public int getNumberFrames() {
        float duration = getEndTime() - getStartTime();
        return 1 + (duration <= 0 ? 0 : (int) ((duration) * getFramesPerSecond()));
    }
        
    public AnimationRasterizer.ImageType getImageType() {
        return (AnimationRasterizer.ImageType)formatComboBox.getSelectedItem();
    }    

    public ColorReductionMethod getColorReductionMethod() {
        return (ColorReductionMethod) reductionCombo.getSelectedItem();
    }
    
    protected void updateImage(JComponent source, boolean isOutputChanged) {
        if ( !m_updateInProgress) {
            m_updateInProgress = true;
            //System.out.println("Updating model " + source);
            final JLabel label = new JLabel( "Updating image...");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setMinimumSize( new Dimension( 100, 100));
            imageHolder.setViewportView(label);

            updateTimelineBounds(source);

            ImageType imgType = getImageType();
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

            float startTime    = getStartTime(),
                  endTime      = getEndTime(),
                  framesPerSec = getFramesPerSecond();
            
            final float duration      = endTime - startTime;
            final int   totalFrameNum = getNumberFrames();
            
            m_previewSpinnerModel.setMaximum(totalFrameNum);
            if ( m_startTime.findWrapper(source) != null) {
                previewFrameSpinner.setValue( new Integer(1));
            } else if ( m_stopTime.findWrapper(source) != null) {
                previewFrameSpinner.setValue( new Integer(totalFrameNum));
            }
            final int currentFrame = ((Integer) previewFrameSpinner.getValue()).intValue() - 1;
            assert currentFrame >= 0;
            final float frameTime  = startTime + currentFrame / framesPerSec;

            String filenameRoot = AnimationRasterizer.createFileNameRoot(m_dObj, this, null, true);
            final String fileName = AnimationRasterizer.createFileName(filenameRoot, this, currentFrame, totalFrameNum);

            if ( isOutputChanged) {
                previewAnimationSizeText.setText("Calculating animation size ...");

                if ( m_sizeCalculationThread != null) {
                    m_sizeCalculationThread.interrupt();
                }
                m_sizeCalculationThread = new Thread() {
                    public void run() {
                        try {
                            AnimationRasterizer.calculateAnimationSize(getSVGImage(), 
                                    SVGAnimationRasterizerPanel.this, new AnimationRasterizer.ProgressUpdater() {
                                public void updateProgress(final String text) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            previewAnimationSizeText.setText(text);
                                        }
                                    });
                                }
                            });
                        } catch( InterruptedException e) {
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                m_sizeCalculationThread.setDaemon(true);
                m_sizeCalculationThread.setPriority(Thread.MIN_PRIORITY);
                m_sizeCalculationThread.setName("AnimationSizeCalculationThread");
                m_sizeCalculationThread.start();
            }
            
            RequestProcessor.getDefault().post( new Runnable() {
                public void run() {
                    try {
                        final AnimationRasterizer.PreviewInfo preview = AnimationRasterizer.previewFrame( getSVGImage(), SVGAnimationRasterizerPanel.this, currentFrame, frameTime);
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                previewFrameSizeText.setText( AnimationRasterizer.getSizeText(preview.m_imageSize));
                                previewFormatText.setText(preview.m_imageFormat);
                                previewFileText.setText(fileName);
                                previewMaxFrameText.setText( String.valueOf(totalFrameNum));
                                previewCurrentTimeText.setText( String.valueOf(roundTime(frameTime)));
                                previewEndTimeText.setText(  String.valueOf(roundTime(duration)));
                                //TODO Handle images not saved yet
                                label.setText(null);
                                label.setIcon(new ImageIcon(preview.m_image));
                                label.invalidate();
                                imageHolder.validate();
                                imageHolder.repaint();
                            }
                        });
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        m_updateInProgress = false;
                    }
                }
            });
        }
    }
    
    private void updateTimelineBounds(JComponent source) {
        ComponentGroup.ComponentWrapper wrapper;
        if ( (wrapper = m_startTime.findWrapper(source)) != null) {
            float value = wrapper.getValue();
            stopTimeSlider.setExtent( Math.round( value * 100));
            ((SpinnerNumberModel)stopTimeSpinner.getModel()).setMinimum( new Double(value));
        } else if ((wrapper = m_stopTime.findWrapper(source)) != null) {
            float value = wrapper.getValue();
            startTimeSlider.setExtent( startTimeSlider.getMaximum() - Math.round( value * 100));
            ((SpinnerNumberModel)startTimeSpinner.getModel()).setMaximum( new Double(value));
        }
    }
}
