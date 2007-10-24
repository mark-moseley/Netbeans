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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * PositionStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.ClipData;
import org.netbeans.modules.css.visual.model.ClipModel;
import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.model.CssRuleContent;
import org.netbeans.modules.css.visual.model.PositionData;
import org.netbeans.modules.css.visual.model.PositionModel;
import org.netbeans.modules.css.visual.model.PropertyData;
import org.netbeans.modules.css.visual.model.PropertyWithUnitData;
import org.netbeans.modules.css.visual.model.PropertyData;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.modules.css.visual.model.Utils;
import org.openide.util.NbBundle;

/**
 * Position Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class PositionStyleEditor extends StyleEditor {

    DefaultComboBoxModel positionModeList;
    PositionData positionData = new PositionData();
    ClipData clipData = new ClipData();

    /** Creates new form FontStyleEditor */
    public PositionStyleEditor() {
        setName("positionStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(PositionStyleEditor.class, "POSITION_EDITOR_DISPNAME"));
        initComponents();
        initialize();

        // Add editor listeners to the width & height combobox
        final JTextField widthComboBoxEditor = (JTextField) widthComboBox.getEditor().getEditorComponent();
        widthComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        widthUnitComboBox.setEnabled(Utils.isInteger(widthComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField heightComboBoxEditor = (JTextField) heightComboBox.getEditor().getEditorComponent();
        heightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        heightUnitComboBox.setEnabled(Utils.isInteger(heightComboBoxEditor.getText()));
                    }
                });
            }
        });

        // Add editor listeners to the top, right, bottom & left combobox

        final JTextField posTopComboBoxEditor = (JTextField) posTopComboBox.getEditor().getEditorComponent();
        posTopComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posTopUnitComboBox.setEnabled(Utils.isInteger(posTopComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField posRightComboBoxEditor = (JTextField) posRightComboBox.getEditor().getEditorComponent();
        posRightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posRightUnitComboBox.setEnabled(Utils.isInteger(posRightComboBoxEditor.getText()));
                    }
                });
            }
        });
        final JTextField posBottomComboBoxEditor = (JTextField) posBottomComboBox.getEditor().getEditorComponent();
        posBottomComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posBottomUnitComboBox.setEnabled(Utils.isInteger(posBottomComboBoxEditor.getText()));
                    }
                });   
            }
        });
        final JTextField posLeftComboBoxEditor = (JTextField) posLeftComboBox.getEditor().getEditorComponent();
        posLeftComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                         posLeftUnitComboBox.setEnabled(Utils.isInteger(posLeftComboBoxEditor.getText()));
                    }
                });   
                 
            }
        });
        
        // Add editor listeners to the top, right, bottom & left Clip combobox
        
        final JTextField clipTopComboBoxEditor = (JTextField) clipTopComboBox.getEditor().getEditorComponent();
        clipTopComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipTopUnitComboBox.setEnabled(Utils.isInteger(clipTopComboBoxEditor.getText()));
                    }
                });       
            }
        });
        final JTextField clipRightComboBoxEditor = (JTextField) clipRightComboBox.getEditor().getEditorComponent();
        clipRightComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipRightUnitComboBox.setEnabled(Utils.isInteger(clipRightComboBoxEditor.getText()));
                    }
                });         
            }
        });
        final JTextField clipBottomComboBoxEditor = (JTextField) clipBottomComboBox.getEditor().getEditorComponent();
        clipBottomComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipBottomUnitComboBox.setEnabled(Utils.isInteger(clipBottomComboBoxEditor.getText()));
                    }
                });          
            }
        });
        final JTextField clipLeftComboBoxEditor = (JTextField) clipLeftComboBox.getEditor().getEditorComponent();
        clipLeftComboBoxEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        clipLeftComboBoxEditor.setEnabled(Utils.isInteger(clipLeftComboBoxEditor.getText()));
                    }
                });     
            }
        });
    }
    
    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssRuleContent cssStyleData){
        removeCssPropertyChangeListener();
        String  positionMode = cssStyleData.getProperty(CssProperties.POSITION);
        if(positionMode != null){
            positionModeCombo.setSelectedItem(positionMode);
        }else{
            positionModeCombo.setSelectedIndex(0);
        }
        String  posTop = cssStyleData.getProperty(CssProperties.TOP);
        if(posTop != null){
            positionData.setTop(posTop);
            posTopComboBox.setSelectedItem(positionData.getTopValue());
            posTopUnitComboBox.setSelectedItem(positionData.getTopUnit());
        }else{
            posTopComboBox.setSelectedIndex(0);
            posTopUnitComboBox.setSelectedItem("px"); //NOI18N
        }
        
        String  posBottom = cssStyleData.getProperty(CssProperties.BOTTOM);
        if(posBottom != null){
            positionData.setBottom(posBottom);
            posBottomComboBox.setSelectedItem(positionData.getBottomValue());
            posBottomUnitComboBox.setSelectedItem(positionData.getBottomUnit());
        }else{
            posBottomComboBox.setSelectedIndex(0);
            posBottomUnitComboBox.setSelectedItem("px"); //NOI18N
        }
        
        String  posLeft = cssStyleData.getProperty(CssProperties.LEFT);
        if(posLeft != null){
            positionData.setLeft(posLeft);
            posLeftComboBox.setSelectedItem(positionData.getLeftValue());
            posLeftUnitComboBox.setSelectedItem(positionData.getLeftUnit());
        }else{
            posLeftComboBox.setSelectedIndex(0);
            posLeftUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String  width = cssStyleData.getProperty(CssProperties.WIDTH);
        if(width != null){
            positionData.setWidth(width);
            widthComboBox.setSelectedItem(positionData.getWidthValue());
            widthUnitComboBox.setSelectedItem(positionData.getWidthUnit());
        }else{
            widthComboBox.setSelectedIndex(0); 
            widthUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String height = cssStyleData.getProperty(CssProperties.HEIGHT);
        if(height != null){
            positionData.setHeight(height);
            heightComboBox.setSelectedItem(positionData.getHeightValue());
            heightUnitComboBox.setSelectedItem(positionData.getHeightUnit());
        }else{
            heightComboBox.setSelectedIndex(0);
            heightUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String visibility = cssStyleData.getProperty(CssProperties.VISIBILITY);
        if(visibility != null){
            visibleComboBox.setSelectedItem(visibility);
        }else{
            visibleComboBox.setSelectedIndex(0);
        }
        
        String zindex = cssStyleData.getProperty(CssProperties.Z_INDEX);
        if(zindex != null){
            zindexComboBox.setSelectedItem(zindex);
        }else{
            zindexComboBox.setSelectedIndex(0);
        }
        
        String  posRight = cssStyleData.getProperty(CssProperties.RIGHT);
        if(posRight != null){
            positionData.setRight(posRight);
            posRightComboBox.setSelectedItem(positionData.getRightValue());
            posRightUnitComboBox.setSelectedItem(positionData.getRightUnit());
        }else{
            posRightComboBox.setSelectedIndex(0);
            posRightUnitComboBox.setSelectedItem("px");  //NOI18N
        }
        
        String  clip = cssStyleData.getProperty(CssProperties.CLIP);
        clipData.setClip(clip);
        clipTopComboBox.setSelectedItem(clipData.getTopValue());
        clipTopUnitComboBox.setSelectedItem(clipData.getTopUnit());
        clipBottomComboBox.setSelectedItem(clipData.getBottomValue());
        clipBottomUnitComboBox.setSelectedItem(clipData.getBottomUnit());
        clipLeftComboBox.setSelectedItem(clipData.getLeftValue());
        clipLeftUnitComboBox.setSelectedItem(clipData.getLeftUnit());
        clipRightComboBox.setSelectedItem(clipData.getRightValue());
        clipRightUnitComboBox.setSelectedItem(clipData.getRightUnit());
        
        setCssPropertyChangeListener(cssStyleData);
    }
    
    public void initialize(){
        PositionModel positionModel = new PositionModel();
        
        // Set the Position Mode to the GUI
        positionModeList = positionModel.getModeList();
        positionModeCombo.setModel(positionModeList);
        
        // Set the Position Top to the GUI
        DefaultComboBoxModel posTopList = positionModel.getPositionList();
        posTopComboBox.setModel(posTopList);
        posTopUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Position Bottom to the GUI
        DefaultComboBoxModel posBottomList = positionModel.getPositionList();
        posBottomComboBox.setModel(posBottomList);
        posBottomUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Position Left to the GUI
        DefaultComboBoxModel posLeftList = positionModel.getPositionList();
        posLeftComboBox.setModel(posLeftList);
        posLeftUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Width to the GUI
        DefaultComboBoxModel widthList = positionModel.getSizeList();
        widthComboBox.setModel(widthList);
        widthUnitComboBox.setModel(positionModel.getPositionUnitList());

        // Set the Height to the GUI
        DefaultComboBoxModel heightList = positionModel.getSizeList();
        heightComboBox.setModel(heightList);
        heightUnitComboBox.setModel(positionModel.getPositionUnitList());
 
        // Set the Visibility to the GUI
        DefaultComboBoxModel visibilityList = positionModel.getVisibilityList();
        visibleComboBox.setModel(visibilityList);

        // Set the Visibility to the GUI
        DefaultComboBoxModel zindexList = positionModel.getZIndexList();
        zindexComboBox.setModel(zindexList);

        // Set the Position Left to the GUI
        DefaultComboBoxModel posRightList = positionModel.getPositionList();
        posRightComboBox.setModel(posRightList);
        posRightUnitComboBox.setModel(positionModel.getPositionUnitList());

        ClipModel clipModel = new ClipModel();
        
        // Set the Position Top to the GUI
        clipTopComboBox.setModel(clipModel.getClipList());
        clipTopUnitComboBox.setModel(clipModel.getClipUnitList());
        clipBottomComboBox.setModel(clipModel.getClipList());
        clipBottomUnitComboBox.setModel(clipModel.getClipUnitList());
        clipLeftComboBox.setModel(clipModel.getClipList());
        clipLeftUnitComboBox.setModel(clipModel.getClipUnitList());
        clipRightComboBox.setModel(clipModel.getClipList());
        clipRightUnitComboBox.setModel(clipModel.getClipUnitList());
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPositionPanel = new javax.swing.JPanel();
        clipPanel = new javax.swing.JPanel();
        clipLeftLabel1 = new javax.swing.JLabel();
        clipBottomUnitComboBox = new javax.swing.JComboBox();
        clipLeftUnitComboBox = new javax.swing.JComboBox();
        clipTopLabel1 = new javax.swing.JLabel();
        clipLeftComboBox = new javax.swing.JComboBox();
        clipTopUnitComboBox = new javax.swing.JComboBox();
        clipRightLabel1 = new javax.swing.JLabel();
        clipBottomComboBox = new javax.swing.JComboBox();
        clipRightUnitComboBox = new javax.swing.JComboBox();
        clipBottomLabel = new javax.swing.JLabel();
        clipTopComboBox = new javax.swing.JComboBox();
        clipRightComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        positionContainerPanel = new javax.swing.JPanel();
        posTopLabel = new javax.swing.JLabel();
        posTopComboBox = new javax.swing.JComboBox();
        posTopUnitComboBox = new javax.swing.JComboBox();
        posBottomLabel1 = new javax.swing.JLabel();
        posBottomComboBox = new javax.swing.JComboBox();
        posBottomUnitComboBox = new javax.swing.JComboBox();
        posRightLabel = new javax.swing.JLabel();
        posRightComboBox = new javax.swing.JComboBox();
        posRightUnitComboBox = new javax.swing.JComboBox();
        posLeftLabel1 = new javax.swing.JLabel();
        posLeftComboBox = new javax.swing.JComboBox();
        posLeftUnitComboBox = new javax.swing.JComboBox();
        positionModeCombo = new javax.swing.JComboBox();
        positionModeLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sizeContainerPanel = new javax.swing.JPanel();
        heightLabel = new javax.swing.JLabel();
        heightComboBox = new javax.swing.JComboBox();
        heightUnitComboBox = new javax.swing.JComboBox();
        widthLabel = new javax.swing.JLabel();
        widthComboBox = new javax.swing.JComboBox();
        widthUnitComboBox = new javax.swing.JComboBox();
        visibleLabel1 = new javax.swing.JLabel();
        visibleComboBox = new javax.swing.JComboBox();
        zIndexLabel1 = new javax.swing.JLabel();
        zindexComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainPositionPanel.setLayout(new java.awt.GridBagLayout());

        clipPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        clipPanel.setLayout(new java.awt.GridBagLayout());

        clipLeftLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_LEFT").charAt(0));
        clipLeftLabel1.setLabelFor(clipLeftComboBox);
        clipLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(clipLeftLabel1, gridBagConstraints);

        clipBottomUnitComboBox.setEnabled(false);
        clipBottomUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipBottomUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipBottomUnitComboBox, gridBagConstraints);
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleName("null");
        clipBottomUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipLeftUnitComboBox.setEnabled(false);
        clipLeftUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipLeftUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipLeftUnitComboBox, gridBagConstraints);
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleName("null");
        clipLeftUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipTopLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_TOP").charAt(0));
        clipTopLabel1.setLabelFor(clipTopComboBox);
        clipTopLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(clipTopLabel1, gridBagConstraints);

        clipLeftComboBox.setEditable(true);
        clipLeftComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        clipLeftComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        clipLeftComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipLeftComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipLeftComboBox, gridBagConstraints);
        clipLeftComboBox.getAccessibleContext().setAccessibleName("null");
        clipLeftComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipTopUnitComboBox.setEnabled(false);
        clipTopUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipTopUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipTopUnitComboBox, gridBagConstraints);
        clipTopUnitComboBox.getAccessibleContext().setAccessibleName("null");
        clipTopUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipRightLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_RIGHT").charAt(0));
        clipRightLabel1.setLabelFor(clipRightComboBox);
        clipRightLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(clipRightLabel1, gridBagConstraints);

        clipBottomComboBox.setEditable(true);
        clipBottomComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        clipBottomComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        clipBottomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipBottomComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipBottomComboBox, gridBagConstraints);
        clipBottomComboBox.getAccessibleContext().setAccessibleName("null");
        clipBottomComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipRightUnitComboBox.setEnabled(false);
        clipRightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipRightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipRightUnitComboBox, gridBagConstraints);
        clipRightUnitComboBox.getAccessibleContext().setAccessibleName("null");
        clipRightUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipBottomLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_CLIP_BOTTOM").charAt(0));
        clipBottomLabel.setLabelFor(clipBottomComboBox);
        clipBottomLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(clipBottomLabel, gridBagConstraints);

        clipTopComboBox.setEditable(true);
        clipTopComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        clipTopComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        clipTopComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipTopComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipTopComboBox, gridBagConstraints);
        clipTopComboBox.getAccessibleContext().setAccessibleName("null");
        clipTopComboBox.getAccessibleContext().setAccessibleDescription("null");

        clipRightComboBox.setEditable(true);
        clipRightComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        clipRightComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        clipRightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                clipRightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        clipPanel.add(clipRightComboBox, gridBagConstraints);
        clipRightComboBox.getAccessibleContext().setAccessibleName("null");
        clipRightComboBox.getAccessibleContext().setAccessibleDescription("null");

        jLabel3.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "CLIP_TITLE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        clipPanel.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        mainPositionPanel.add(clipPanel, gridBagConstraints);

        positionContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        positionContainerPanel.setLayout(new java.awt.GridBagLayout());

        posTopLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_TOP").charAt(0));
        posTopLabel.setLabelFor(posTopComboBox);
        posTopLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_TOP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posTopLabel, gridBagConstraints);

        posTopComboBox.setEditable(true);
        posTopComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        posTopComboBox.setPreferredSize(new java.awt.Dimension(60, 24));
        posTopComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posTopComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posTopComboBox, gridBagConstraints);
        posTopComboBox.getAccessibleContext().setAccessibleName("null");
        posTopComboBox.getAccessibleContext().setAccessibleDescription("null");

        posTopUnitComboBox.setEnabled(false);
        posTopUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posTopUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posTopUnitComboBox, gridBagConstraints);
        posTopUnitComboBox.getAccessibleContext().setAccessibleName("null");
        posTopUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        posBottomLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_BOTTOM").charAt(0));
        posBottomLabel1.setLabelFor(posBottomComboBox);
        posBottomLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_BOTTOM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posBottomLabel1, gridBagConstraints);

        posBottomComboBox.setEditable(true);
        posBottomComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        posBottomComboBox.setPreferredSize(new java.awt.Dimension(60, 24));
        posBottomComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posBottomComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posBottomComboBox, gridBagConstraints);
        posBottomComboBox.getAccessibleContext().setAccessibleName("null");
        posBottomComboBox.getAccessibleContext().setAccessibleDescription("null");

        posBottomUnitComboBox.setEnabled(false);
        posBottomUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posBottomUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posBottomUnitComboBox, gridBagConstraints);
        posBottomUnitComboBox.getAccessibleContext().setAccessibleName("null");
        posBottomUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        posRightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_RIGHT").charAt(0));
        posRightLabel.setLabelFor(posRightComboBox);
        posRightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_RIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posRightLabel, gridBagConstraints);

        posRightComboBox.setEditable(true);
        posRightComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        posRightComboBox.setPreferredSize(new java.awt.Dimension(60, 24));
        posRightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posRightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posRightComboBox, gridBagConstraints);
        posRightComboBox.getAccessibleContext().setAccessibleName("null");
        posRightComboBox.getAccessibleContext().setAccessibleDescription("null");

        posRightUnitComboBox.setEnabled(false);
        posRightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posRightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posRightUnitComboBox, gridBagConstraints);
        posRightUnitComboBox.getAccessibleContext().setAccessibleName("null");
        posRightUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        posLeftLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_LEFT").charAt(0));
        posLeftLabel1.setLabelFor(posLeftComboBox);
        posLeftLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_LEFT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(posLeftLabel1, gridBagConstraints);

        posLeftComboBox.setEditable(true);
        posLeftComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        posLeftComboBox.setPreferredSize(new java.awt.Dimension(60, 24));
        posLeftComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posLeftComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posLeftComboBox, gridBagConstraints);
        posLeftComboBox.getAccessibleContext().setAccessibleName("null");
        posLeftComboBox.getAccessibleContext().setAccessibleDescription("null");

        posLeftUnitComboBox.setEnabled(false);
        posLeftUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                posLeftUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(posLeftUnitComboBox, gridBagConstraints);
        posLeftUnitComboBox.getAccessibleContext().setAccessibleName("null");
        posLeftUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        positionModeCombo.setMinimumSize(null);
        positionModeCombo.setPreferredSize(null);
        positionModeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                positionModeComboItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        positionContainerPanel.add(positionModeCombo, gridBagConstraints);
        positionModeCombo.getAccessibleContext().setAccessibleName("null");
        positionModeCombo.getAccessibleContext().setAccessibleDescription("null");

        positionModeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_MODE").charAt(0));
        positionModeLabel.setLabelFor(positionModeCombo);
        positionModeLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_MODE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(positionModeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POISTION_TITLE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        positionContainerPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        mainPositionPanel.add(positionContainerPanel, gridBagConstraints);

        sizeContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        sizeContainerPanel.setLayout(new java.awt.GridBagLayout());

        heightLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_HEIGHT").charAt(0));
        heightLabel.setLabelFor(heightComboBox);
        heightLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_HEIGHT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(heightLabel, gridBagConstraints);

        heightComboBox.setEditable(true);
        heightComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        heightComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        heightComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                heightComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(heightComboBox, gridBagConstraints);
        heightComboBox.getAccessibleContext().setAccessibleName("null");
        heightComboBox.getAccessibleContext().setAccessibleDescription("null");

        heightUnitComboBox.setEnabled(false);
        heightUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                heightUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(heightUnitComboBox, gridBagConstraints);
        heightUnitComboBox.getAccessibleContext().setAccessibleName("null");
        heightUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        widthLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_POSITION_WIDTH").charAt(0));
        widthLabel.setLabelFor(widthComboBox);
        widthLabel.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "POSITION_WIDTH")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(widthLabel, gridBagConstraints);

        widthComboBox.setEditable(true);
        widthComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        widthComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        widthComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                widthComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(widthComboBox, gridBagConstraints);
        widthComboBox.getAccessibleContext().setAccessibleName("null");
        widthComboBox.getAccessibleContext().setAccessibleDescription("null");

        widthUnitComboBox.setEnabled(false);
        widthUnitComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                widthUnitComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(widthUnitComboBox, gridBagConstraints);
        widthUnitComboBox.getAccessibleContext().setAccessibleName("null");
        widthUnitComboBox.getAccessibleContext().setAccessibleDescription("null");

        visibleLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_VISIBILITY").charAt(0));
        visibleLabel1.setLabelFor(visibleComboBox);
        visibleLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "VISIBILITY")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(visibleLabel1, gridBagConstraints);

        visibleComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        visibleComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        visibleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                visibleComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(visibleComboBox, gridBagConstraints);
        visibleComboBox.getAccessibleContext().setAccessibleName("null");
        visibleComboBox.getAccessibleContext().setAccessibleDescription("null");

        zIndexLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/css/visual/ui/Bundle").getString("MNE_Z_INDEX").charAt(0));
        zIndexLabel1.setLabelFor(zindexComboBox);
        zIndexLabel1.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "Z_INDEX")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(zIndexLabel1, gridBagConstraints);

        zindexComboBox.setEditable(true);
        zindexComboBox.setMinimumSize(new java.awt.Dimension(60, 24));
        zindexComboBox.setPreferredSize(new java.awt.Dimension(100, 24));
        zindexComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                zindexComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        sizeContainerPanel.add(zindexComboBox, gridBagConstraints);
        zindexComboBox.getAccessibleContext().setAccessibleName("null");
        zindexComboBox.getAccessibleContext().setAccessibleDescription("null");

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PositionStyleEditor.class, "SIZE_TITLE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        sizeContainerPanel.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        mainPositionPanel.add(sizeContainerPanel, gridBagConstraints);

        add(mainPositionPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    
    private void clipLeftUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setLeftUnit(clipLeftUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipLeftUnitComboBoxItemStateChanged
    
    private void clipLeftComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipLeftComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setLeft(clipLeftComboBox.getSelectedItem().toString());
            clipLeftUnitComboBox.setEnabled(clipData.isLeftValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipLeftComboBoxItemStateChanged
        
    private void clipBottomUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setBottomUnit(clipBottomUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipBottomUnitComboBoxItemStateChanged
    
    private void clipBottomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipBottomComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setBottom(clipBottomComboBox.getSelectedItem().toString());
            clipBottomUnitComboBox.setEnabled(clipData.isBottomValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipBottomComboBoxItemStateChanged
        
    private void clipRightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setRightUnit(clipRightUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipRightUnitComboBoxItemStateChanged
    
    private void clipRightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipRightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setRight(clipRightComboBox.getSelectedItem().toString());
            clipRightUnitComboBox.setEnabled(clipData.isRightValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipRightComboBoxItemStateChanged
        
    private void clipTopUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setTopUnit(clipTopUnitComboBox.getSelectedItem().toString());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipTopUnitComboBoxItemStateChanged
    
    private void clipTopComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_clipTopComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            clipData.setTop(clipTopComboBox.getSelectedItem().toString());
            clipTopUnitComboBox.setEnabled(clipData.isTopValueInteger());
            cssPropertyChangeSupport().firePropertyChange(CssProperties.CLIP, null, clipData.toString());
        }
    }//GEN-LAST:event_clipTopComboBoxItemStateChanged
        
    private void zindexComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_zindexComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setZindex();
        }
    }//GEN-LAST:event_zindexComboBoxItemStateChanged
    
    private void visibleComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_visibleComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setVisibility();
        }
    }//GEN-LAST:event_visibleComboBoxItemStateChanged
    
    private void heightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_heightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setHeight();
        }
    }//GEN-LAST:event_heightUnitComboBoxItemStateChanged
    
    private void heightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_heightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setHeight();
        }
    }//GEN-LAST:event_heightComboBoxItemStateChanged
            
    private void widthComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_widthComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWidth();
        }
    }//GEN-LAST:event_widthComboBoxItemStateChanged
    
    private void widthUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_widthUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setWidth();
        }
    }//GEN-LAST:event_widthUnitComboBoxItemStateChanged
    
    private void posRightUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posRightUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setRightPos();
        }
    }//GEN-LAST:event_posRightUnitComboBoxItemStateChanged
    
    private void posRightComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posRightComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setRightPos();
        }
    }//GEN-LAST:event_posRightComboBoxItemStateChanged
        
    private void posLeftUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posLeftUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLeftPos();
        }
    }//GEN-LAST:event_posLeftUnitComboBoxItemStateChanged
    
    private void posLeftComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posLeftComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setLeftPos();
        }
    }//GEN-LAST:event_posLeftComboBoxItemStateChanged
        
    private void posBottomUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posBottomUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBottomPos();
        }
    }//GEN-LAST:event_posBottomUnitComboBoxItemStateChanged
    
    private void posBottomComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posBottomComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setBottomPos();
        }
    }//GEN-LAST:event_posBottomComboBoxItemStateChanged
        
    private void posTopUnitComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posTopUnitComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTopPos();
        }
    }//GEN-LAST:event_posTopUnitComboBoxItemStateChanged
    
    private void posTopComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_posTopComboBoxItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setTopPos();
        }
    }//GEN-LAST:event_posTopComboBoxItemStateChanged
        
    private void positionModeComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_positionModeComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setPosition();
        }
    }//GEN-LAST:event_positionModeComboItemStateChanged
    
    private void setZindex(){
        PropertyData zindexData = new PropertyData();
        zindexData.setValue(zindexComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.Z_INDEX, null, zindexData.toString());
    }
    
    public void setVisibility(){
        PropertyData visibleData = new PropertyData();
        visibleData.setValue(visibleComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.VISIBILITY, null, visibleData.toString());
    }
    
    private void setHeight(){
        PropertyWithUnitData heightData = new PropertyWithUnitData();
        heightData.setUnit(heightUnitComboBox.getSelectedItem().toString());
        heightData.setValue(heightComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.HEIGHT, null, heightData.toString());
        heightUnitComboBox.setEnabled(heightData.isValueInteger());
    }
    
    private void setWidth(){
        PropertyWithUnitData widthData = new PropertyWithUnitData();
        widthData.setUnit(widthUnitComboBox.getSelectedItem().toString());
        widthData.setValue(widthComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.WIDTH, null, widthData.toString());
        widthUnitComboBox.setEnabled(widthData.isValueInteger());
    }
    
    private void setRightPos(){
        PropertyWithUnitData posRightData = new PropertyWithUnitData();
        posRightData.setUnit(posRightUnitComboBox.getSelectedItem().toString());
        posRightData.setValue(posRightComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.RIGHT, null, posRightData.toString());
        posRightUnitComboBox.setEnabled(posRightData.isValueInteger());
    }
    
    private void setLeftPos(){
        PropertyWithUnitData posLeftData = new PropertyWithUnitData();
        posLeftData.setUnit(posLeftUnitComboBox.getSelectedItem().toString());
        posLeftData.setValue(posLeftComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.LEFT, null, posLeftData.toString());
        posLeftUnitComboBox.setEnabled(posLeftData.isValueInteger());
    }
    
    private void setBottomPos(){
        PropertyWithUnitData posBottomData = new PropertyWithUnitData();
        posBottomData.setUnit(posBottomUnitComboBox.getSelectedItem().toString());
        posBottomData.setValue(posBottomComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.BOTTOM, null, posBottomData.toString());
        posBottomUnitComboBox.setEnabled(posBottomData.isValueInteger());
    }
    
    private void setTopPos(){
        PropertyWithUnitData posTopData = new PropertyWithUnitData();
        posTopData.setUnit(posTopUnitComboBox.getSelectedItem().toString());
        posTopData.setValue(posTopComboBox.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.TOP, null, posTopData.toString());
        posTopUnitComboBox.setEnabled(posTopData.isValueInteger());
    }
    
    public void setPosition(){
        PropertyData positionData = new PropertyData();
        positionData.setValue(positionModeCombo.getSelectedItem().toString());
        cssPropertyChangeSupport().firePropertyChange(CssProperties.POSITION, null, positionData.toString());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clipBottomComboBox;
    private javax.swing.JLabel clipBottomLabel;
    private javax.swing.JComboBox clipBottomUnitComboBox;
    private javax.swing.JComboBox clipLeftComboBox;
    private javax.swing.JLabel clipLeftLabel1;
    private javax.swing.JComboBox clipLeftUnitComboBox;
    private javax.swing.JPanel clipPanel;
    private javax.swing.JComboBox clipRightComboBox;
    private javax.swing.JLabel clipRightLabel1;
    private javax.swing.JComboBox clipRightUnitComboBox;
    private javax.swing.JComboBox clipTopComboBox;
    private javax.swing.JLabel clipTopLabel1;
    private javax.swing.JComboBox clipTopUnitComboBox;
    private javax.swing.JComboBox heightComboBox;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JComboBox heightUnitComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel mainPositionPanel;
    private javax.swing.JComboBox posBottomComboBox;
    private javax.swing.JLabel posBottomLabel1;
    private javax.swing.JComboBox posBottomUnitComboBox;
    private javax.swing.JComboBox posLeftComboBox;
    private javax.swing.JLabel posLeftLabel1;
    private javax.swing.JComboBox posLeftUnitComboBox;
    private javax.swing.JComboBox posRightComboBox;
    private javax.swing.JLabel posRightLabel;
    private javax.swing.JComboBox posRightUnitComboBox;
    private javax.swing.JComboBox posTopComboBox;
    private javax.swing.JLabel posTopLabel;
    private javax.swing.JComboBox posTopUnitComboBox;
    private javax.swing.JPanel positionContainerPanel;
    private javax.swing.JComboBox positionModeCombo;
    private javax.swing.JLabel positionModeLabel;
    private javax.swing.JPanel sizeContainerPanel;
    private javax.swing.JComboBox visibleComboBox;
    private javax.swing.JLabel visibleLabel1;
    private javax.swing.JComboBox widthComboBox;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JComboBox widthUnitComboBox;
    private javax.swing.JLabel zIndexLabel1;
    private javax.swing.JComboBox zindexComboBox;
    // End of variables declaration//GEN-END:variables
    
}
