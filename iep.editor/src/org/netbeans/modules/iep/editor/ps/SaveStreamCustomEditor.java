package org.netbeans.modules.iep.editor.ps;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SaveStreamOperatorComponent;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.openide.DialogDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

public class SaveStreamCustomEditor extends DefaultCustomEditor {
    private static final Logger mLog = Logger.getLogger(SaveStreamCustomEditor.class.getName());
    
    private boolean mValidOperatorSelection = false;
    
    /** Creates a new instance of SaveStreamCustomEditor */
    public SaveStreamCustomEditor() {
        super();
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }
    
    private class MyCustomizer extends DefaultCustomizer {
        protected PropertyPanel mAttributePanel;
        protected PropertyPanel mSizePanel;
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
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
            Property nameProp = mComponent.getProperty(NAME_KEY);
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
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[1], gbc);
            
            // output schema
            Property outputSchemaNameProp = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            if (mIsSchemaOwner) {
                if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                    IEPModel model = mComponent.getModel();
                    String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                    mOutputSchemaNamePanel.setStringValue(schemaName);
                }
            } else {
                ((JTextField)mOutputSchemaNamePanel.input[0]).setEditable(false);
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
            
            // table name
            Property invokedProcessName = mComponent.getProperty(SaveStreamOperatorComponent.PROP_TABLE_NAME);
            String invokedProcessNameLabel = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.TABLE_NAME");
            mAttributePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(invokedProcessNameLabel, invokedProcessName, false);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[1], gbc);

            
            // database jndi name
            Property sizeProp = mComponent.getProperty(SaveStreamOperatorComponent.PROP_DATABASE_JNDI_NAME);
            String sizeStr = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.DATABASE_JNDI_NAME");
            mSizePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(sizeStr, sizeProp, false);
            if(mSizePanel.getStringValue() == null || mSizePanel.getStringValue().equals("")) {
                mSizePanel.setStringValue(SharedConstants.DEFAULT_JNDINAME);
                
            }
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mSizePanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mSizePanel.component[1], gbc);
            
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
        
        @Override
        protected SelectPanel createSelectPanel(IEPModel model, OperatorComponent component) {
            return new MySelectPanel(model, component);
        }

        @Override
        protected boolean isShowFromClause() {
            return false;
        }
        
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            try {
                mAttributePanel.validateContent(evt);
                mSizePanel.validateContent(evt);
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
            
            //validate for atleast one input and that input should be used
            //in from clause and expressions are columns from input
        }
        
        public void setValue() {
            super.setValue();
            mAttributePanel.store();
            mSizePanel.store();
            
            //store from clause
            List<OperatorComponent> inputs = mComponent.getInputOperatorList();
            if(inputs.size() == 1) {
                OperatorComponent comp = inputs.get(0);
                String displayName = comp.getDisplayName();
                if(displayName != null) {
                    Property fromProp = mComponent.getProperty(FROM_CLAUSE_KEY);
                    if(fromProp != null) {
                        mComponent.getModel().startTransaction();
                        fromProp.setValue(displayName);
                        mComponent.getModel().endTransaction();
                    }
                }
            }
        }
    }
    
    
    
    class StreamInputChooserPanelPropertyListener implements PropertyChangeListener {
        
        private DialogDescriptor mDD;
        
        StreamInputChooserPanelPropertyListener(DialogDescriptor dd) {
            this.mDD = dd;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if(StreamInputChooserPanel.PROP_SELECTED_INPUT_OPERATOR_COMPONENT.equals(evt.getPropertyName())){
                if(evt.getNewValue() != null) {
                    mValidOperatorSelection = true;
                } else {
                    mValidOperatorSelection = false;
                }
            } else {
                mValidOperatorSelection = false;
            }
            
            this.mDD.setValid(mValidOperatorSelection);
            
        }
        
    }
    
    class MySelectPanel extends SelectPanel {

        /**
         * 
         */
        private static final long serialVersionUID = -4195259789503600814L;

        public MySelectPanel(IEPModel model, OperatorComponent component) {
            super(model, component);
        }
        
        @Override
        protected boolean isAddEmptyRow() {
            return false;
        }

                @Override
                protected boolean isShowButtons() {
                    return false;
                }
        
                
        @Override
        protected DefaultMoveableRowTableModel createTableModel() {
            return new MyTableModel();
        }
    }
    
    class MyTableModel extends DefaultMoveableRowTableModel {
        
        @Override
        public boolean isCellEditable(int row, int column) {
            if(column == 0) {
                return true;
            }
            return false;
        }
    }
}
