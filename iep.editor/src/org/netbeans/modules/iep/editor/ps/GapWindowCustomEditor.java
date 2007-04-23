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
import org.netbeans.modules.iep.editor.model.AttributeMetadata;
import org.netbeans.modules.iep.editor.model.ModelManager;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.tcg.dialog.NotifyHelper;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.tcg.table.MoveableRowTable;
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
import java.awt.dnd.DragGestureEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * GapWindowCustomEditor.java
 *
 * Created on November 10, 2006, 10:23 AM
 *
 * @author Bing Lu
 */
public class GapWindowCustomEditor extends TcgComponentNodePropertyEditor implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(GapWindowCustomEditor.class.getName());
    
    private static Set INTEGER_TYPES = new HashSet();
    static {
//        INTEGER_TYPES.add(SQL_TYPE_TINYINT);
//        INTEGER_TYPES.add(SQL_TYPE_SMALLINT);
        INTEGER_TYPES.add(SQL_TYPE_INTEGER);
        INTEGER_TYPES.add(SQL_TYPE_BIGINT);
    }

    /** Creates a new instance of GapWindowCustomEditor */
    public GapWindowCustomEditor() {
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
        private TcgComponent mComponent;
        private PropertyPanel mNamePanel;
        private PropertyPanel mOutputSchemaNamePanel;
        private PropertyPanel mStartPanel;
        private PropertyPanel mAttributePanel;
        private PartitionKeySelectionPanel mPartitionKeyPanel;
        private DefaultMoveableRowTableModel mTableModel;
        private MoveableRowTable mTable;
        private Vector mColTitle;
        private JLabel mStatusLbl;
        
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
                
                // create input selection panel first to parse schema information
                // which is used by properties in property panel created
                // by createPropertyPanel()
                
                // create partition key panel first to parse schema information
                // which is used by properties in property panel created
                // by createPropertyPanel()
                mPartitionKeyPanel = new PartitionKeySelectionPanel((Plan)mProperty.getNode().getDoc(), mComponent);

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
                JSplitPane attributePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.ATTRIBUTES");
                attributePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
                add(attributePane, gbc);
                
                // left attribute pane
                ((JSplitPane)attributePane).setOneTouchExpandable(true);
                mPartitionKeyPanel.setPreferredSize(new Dimension(150, 300));
                ((JSplitPane)attributePane).setLeftComponent(mPartitionKeyPanel);
                mPartitionKeyPanel.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        MyCustomizer.this.updateTable();
                    }
                });
                
                // right attribute pane
                JScrollPane rightPane = new JScrollPane();
                rightPane.setPreferredSize(new Dimension(450, 300));
                attributePane.setRightComponent(rightPane);
                
                mColTitle = new Vector();
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.DATA_TYPE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SIZE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SCALE"));
                mTableModel = new DefaultMoveableRowTableModel();
                updateTable();
                mTable = new MoveableRowTable(mTableModel) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        return;
                    }
                };
                rightPane.getViewport().add(mTable);
                
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
        
        private void updateTable() {
            try {
                Vector data = new Vector();
                String indexAttribute = mAttributePanel.getStringValue();
                boolean indexSelected = indexAttribute != null && !indexAttribute.trim().equals("");
                List attributeList = mPartitionKeyPanel.getSelectedAttributeList();
                Vector r;
                for (int i = 0, I = attributeList.size(); i < I; i++) {
                    AttributeMetadata attr = (AttributeMetadata)attributeList.get(i);
                    r = new Vector();
                    r.add(attr.getAttributeName());
                    r.add(attr.getAttributeType());
                    r.add(attr.getAttributeSize());
                    r.add(attr.getAttributeScale());
                    data.add(r);
                }
                if (indexSelected) {
                    AttributeMetadata attr = mPartitionKeyPanel.getAttribute(indexAttribute);
                    r = new Vector();
                    r.add(attr.getAttributeName());
                    r.add(attr.getAttributeType());
                    r.add(attr.getAttributeSize());
                    r.add(attr.getAttributeScale());
                    data.add(r);
                }
                mTableModel.setDataVector(data, mColTitle);
            } catch (Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(GapWindowCustomEditor.class, "CustomEditor.FAILED_UPDATE_TABLE"),
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
            if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                mOutputSchemaNamePanel.setStringValue(((Plan)mProperty.getNode().getDoc()).getNameForNewSchema());
            }
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

            // start
            TcgProperty startProp = mComponent.getProperty(START_KEY);
            String startStr = NbBundle.getMessage(GapWindowCustomEditor.class, "CustomEditor.START");
            mStartPanel = PropertyPanel.createIntNumberPanel(startStr, startProp, false);
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mStartPanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mStartPanel.component[1], gbc);

            // attribute
            TcgProperty attributeProp = mComponent.getProperty(ATTRIBUTE_KEY);
            String attributeStr = NbBundle.getMessage(TupleBasedAggregatorCustomEditor.class, "CustomEditor.SORT_BY");
            List attributeList = mPartitionKeyPanel.getAttributeNameList(INTEGER_TYPES);
            attributeList.add(0, "");
            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[1], gbc);
            
            ((JComboBox)mAttributePanel.input[0]).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateTable();
                }
            });
            
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
                
                // output schema name
                mOutputSchemaNamePanel.validateContent(evt);
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                String schemaName = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                if (!newSchemaName.equals(schemaName) && plan.hasSchema(newSchemaName)) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.OUTPUT_SCHEMA_NAME_IS_ALREADY_TAKENBY_ANOTHER_SCHEMA",
                            newSchemaName);
                    throw new PropertyVetoException(msg, evt);
                }
                
                // start
                mStartPanel.validateContent(evt);
                
                // attribute index
                mAttributePanel.validateContent(evt);
                
                // attribute for index cannot be part of partition key
                String indexName = mAttributePanel.getStringValue();
                List partitionKey = mPartitionKeyPanel.getSelectedAttributeNameList();
                for (int i = 0, I = partitionKey.size(); i < I; i++) {
                    if (indexName.equals(partitionKey.get(i))) {
                        String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                "CustomEditor.SORT_BY_ATTRIBUTE_MUST_NOT_BE_PART_OF_PARTITION_KEY");
                        throw new PropertyVetoException(msg, evt);
                        
                    }
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        private List getAttributeMetadataAsList() {
            List attributeMetadataList = new ArrayList();
            Vector r = mTableModel.getDataVector();
            for (int i = 0, I = r.size(); i < I; i++) {
                Vector c = (Vector) r.elementAt(i);
                attributeMetadataList.add(c.elementAt(0));
                attributeMetadataList.add(c.elementAt(1));
                attributeMetadataList.add(c.elementAt(2));
                attributeMetadataList.add(c.elementAt(3));
                attributeMetadataList.add("");
            }
            return attributeMetadataList;
        }
        
        public void setValue() {
            Plan plan = (Plan)mProperty.getNode().getDoc();
            Schema newSchema = null;
            mNamePanel.store();
            mStartPanel.store();
            mAttributePanel.store();
            mPartitionKeyPanel.store();
            try {
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                String schemaName = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                Schema schema = plan.getSchema(schemaName);
                boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && schema != null;
                List attributes = getAttributeMetadataAsList();
                if (schemaExist) {
                    if (!newSchemaName.equals(schemaName)) {
                        newSchema = ModelManager.createSchema(newSchemaName);
                        newSchema.setAttributeMetadataAsList(attributes);
                        plan.addSchema(newSchema);
                        plan.removeSchema(schemaName);
                        mOutputSchemaNamePanel.store();
                        mProperty.getNode().getView().updateTcgComponentNodeView();
                        plan.getPropertyChangeSupport().firePropertyChange("Schema Name",
                                schemaName, newSchemaName);
                        
                    } else {
                        if (!schema.hasSameAttributeMetadata(attributes)) {
                            schema.setAttributeMetadataAsList(attributes);
                            plan.getPropertyChangeSupport().firePropertyChange("Schema Column Metadata",
                                    "old", "new");
                        }
                    }
                } else {
                    newSchema = ModelManager.createSchema(newSchemaName);
                    plan.addSchema(newSchema);
                    newSchema.setAttributeMetadataAsList(attributes);
                    mOutputSchemaNamePanel.store();
                    mProperty.getNode().getView().updateTcgComponentNodeView();
                    plan.getPropertyChangeSupport().firePropertyChange("Schema",
                            null, newSchema);
                }
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
    }
}
