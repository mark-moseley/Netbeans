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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.tcg.dialog.NotifyHelper;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodeProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizer;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * TableOutputCustomEditor.java
 *
 * Created on November 10,PartTableOutputCustomEditor Bing Lu
 */
public class TableOutputCustomEditor extends TcgComponentNodePropertyEditor {
    private static final Logger mLog = Logger.getLogger(TableOutputCustomEditor.class.getName());
    
    private static final String COL_SEQID = "ems_seqid";    // consistent with 
    
    /**PartTableOutputCustomEditoricIOCustomEditor */
    public TableOutputCustomEditor() {
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(mProperty, mEnv);
        }
        return new MyCustomizer(mProperty, mCustomizerState);
    }
    
    private static class MyCustomizer extends TcgComponentNodePropertyCustomizer implements SharedConstants {
        protected TcgComponent mComponent;
        protected PropertyPanel mNamePanel;
        protected PropertyPanel mOutputSchemaNamePanel;
        protected PropertyPanel mIsGlobalPanel;
        protected PropertyPanel mGlobalIdPanel;
        protected TableOutputSchemaPanel mSchemaPanel;
        protected JLabel mStatusLbl;
        
        public MyCustomizer(TcgComponentNodeProperty prop, PropertyEnv env) {
            super(prop, env);
        }
        
        public MyCustomizer(TcgComponentNodeProperty prop, TcgComponentNodePropertyCustomizerState customizerState) {
            super(prop, customizerState);
        }
        
        protected void initialize() {
            try {
                mComponent = mProperty.getProperty().getParentComponent();
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(3, 3, 3, 3);
                int gGridy = 0;
                
                // property pane
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.0D;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                JPanel topPanel = createPropertyPanel();
                add(topPanel, gbc);
                
                // attribute pane
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 1.0D;
                gbc.fill = GridBagConstraints.BOTH;
                JPanel attributePane = new JPanel();
                attributePane.setLayout(new GridBagLayout());
                String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.ATTRIBUTES");
                attributePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
                add(attributePane, gbc);
                
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.3D;
                gbc.fill = GridBagConstraints.BOTH;
                mSchemaPanel = new TableOutputSchemaPanel((Plan)mProperty.getNode().getDoc(), mComponent);
                mSchemaPanel.setPreferredSize(new Dimension(700, 300));
                attributePane.add(mSchemaPanel, gbc);
                
                // status bar
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.0D;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                mStatusLbl = new JLabel();
                mStatusLbl.setForeground(Color.RED);
                add(mStatusLbl, gbc);
            } catch(Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.FAILED_TO_LAYOUT"),
                        e);
            }
        }
        
        protected JPanel createPropertyPanel() throws Exception {
            JPanel pane = new JPanel();
            String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.DETAILS");
            pane.setBorder(new CompoundBorder(
                    new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            pane.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            
            // name
            TcgProperty nameProp = mComponent.getProperty(NAME_KEY);
            String nameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.NAME");
            mNamePanel = PropertyPanel.createSingleLineTextPanel(nameStr, nameProp, false);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[1], gbc);

            // output schema
            TcgProperty outputSchemaNameProp = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            ((JTextField)mOutputSchemaNamePanel.input[0]).setEditable(false);
            ((JTextField)mOutputSchemaNamePanel.input[0]).setText(outputSchemaNameProp.getStringValue() + "_Ext");
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[0], gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[1], gbc);
            
            // struct
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(Box.createHorizontalStrut(20), gbc);

            // is global
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            TcgProperty isGlobalProp = mComponent.getProperty(IS_GLOBAL_KEY);
            String isGlobalStr = NbBundle.getMessage(TableInputCustomEditor.class, "CustomEditor.IS_GLOBAL");
            mIsGlobalPanel = PropertyPanel.createCheckBoxPanel(isGlobalStr, isGlobalProp);
            if (!isGlobalProp.getType().isWritable()) {
                ((JCheckBox)mIsGlobalPanel.input[0]).setEnabled(false);
            }
            pane.add(mIsGlobalPanel.panel, gbc);
            
            // global id
            TcgProperty globalIdProp = mComponent.getProperty(GLOBAL_ID_KEY);
            String globalIdStr = NbBundle.getMessage(TableInputCustomEditor.class, "CustomEditor.GLOBAL_ID");
            mGlobalIdPanel = PropertyPanel.createSingleLineTextPanel(globalIdStr, globalIdProp, false);
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mGlobalIdPanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mGlobalIdPanel.component[1], gbc);
            
            // glue
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);
            return pane;
        }        
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            try {
                Plan plan = (Plan)mProperty.getNode().getDoc();
                // name
                mNamePanel.validateContent(evt);
                String newName = mNamePanel.getStringValue();
                String name = mComponent.getProperty(NAME_KEY).getStringValue();
                if (!newName.equals(name) && plan.hasOperator(newName)) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.NAME_IS_ALREADY_TAKEN_BY_ANOTHER_OPERATOR",
                            newName);
                    throw new PropertyVetoException(msg, evt);
                }
                // if (isGlobal) then globalId != ""
                boolean isGlobal = mIsGlobalPanel.getBooleanValue();
                String globalId = mGlobalIdPanel.getStringValue();
                if (isGlobal && (globalId == null || globalId.trim().equals(""))) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.GLOBAL_ID_MUST_BE_DEFINED_FOR_A_GLOBAL_ENTITY");
                    throw new PropertyVetoException(msg, evt);
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        public void setValue() {
            Plan plan = (Plan)mProperty.getNode().getDoc();
            try {
                mNamePanel.store();
                mIsGlobalPanel.store();
                mGlobalIdPanel.store();
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }       
        }
    }
}
