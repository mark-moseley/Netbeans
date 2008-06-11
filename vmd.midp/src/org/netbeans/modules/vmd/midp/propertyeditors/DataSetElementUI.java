/*
 * DataSetDatabindingElement.java
 *
 * Created on June 3, 2008, 4:20 PM
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;

/**
 *
 * @author Karol Harezlak
 */
class DataSetElementUI extends javax.swing.JPanel {

    private static String NULL = "<null>"; //TODO Localized
    private String bindedProperty;

    /** Creates new form DataSetDatabindingElement */
    DataSetElementUI(String bindedProperty) {
        this.bindedProperty = bindedProperty;
      
        initComponents();
        jComboBoxDataSets.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                changeComponentsState((String) jComboBoxDataSets.getSelectedItem());
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxDataSets = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldExpression = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabelExpression = new javax.swing.JLabel();
        jComboBoxUpdateCommands = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        jComboBoxDataSets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxDataSetsActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DataSetElementUI.class, "DataSetElementUI.jLabel1.text")); // NOI18N

        jTextFieldExpression.setText(org.openide.util.NbBundle.getMessage(DataSetElementUI.class, "DataSetElementUI.jTextFieldExpression.text")); // NOI18N
        jTextFieldExpression.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setText(org.openide.util.NbBundle.getMessage(DataSetElementUI.class, "DataSetElementUI.jLabel3.text")); // NOI18N

        jLabelExpression.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelExpression.setText(org.openide.util.NbBundle.getMessage(DataSetElementUI.class, "DataSetElementUI.jLabelExpression.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(DataSetElementUI.class, "DataSetElementUI.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBoxUpdateCommands, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 85, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jComboBoxDataSets, 0, 229, Short.MAX_VALUE))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(51, 51, 51)
                                .add(jLabel3))
                            .add(layout.createSequentialGroup()
                                .add(53, 53, 53)
                                .add(jLabelExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(1, 1, 1)
                                .add(jTextFieldExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jLabel2))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxDataSets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabelExpression)
                    .add(jTextFieldExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxUpdateCommands, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(99, 99, 99))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jComboBoxDataSetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxDataSetsActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jComboBoxDataSetsActionPerformed

    void updateComponent(final DesignComponent component) {
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                final DesignDocument document = component.getDocument();

                if (document == null) {
                    return;
                }
                jComboBoxDataSets.setModel(new Model(component, DatabindingCategoryCD.TYPEID));
                jComboBoxUpdateCommands.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                //final DesignComponent dataSet = component.readProperty(dataSetPropertyName).getComponent();
                //if (dataSet == null) {
                //    return;
                //}
                //String name = (String) dataSet.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
                //changeComponentsState(name);
            }
        });
    }

    void saveToModel(final DesignComponent component) {
        final DesignDocument document = component.getDocument();
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                DesignComponent connector = MidpDatabindingSupport.getConnector(component, bindedProperty);
                String selectedDataSet = (String) jComboBoxDataSets.getSelectedItem();
                String selectedUpdateCommand = (String) jComboBoxUpdateCommands.getSelectedItem();
                Collection<DesignComponent> dataSets = MidpDocumentSupport.getCategoryComponent(document, DatabindingCategoryCD.TYPEID).getComponents();
                for (DesignComponent dataSet : dataSets) {
                    if (dataSet.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().equals(selectedDataSet)) {
                        if (connector == null) {
                            connector = document.createComponent(DataSetConnectorCD.TYPEID);
                            connector.writeProperty(DataSetConnectorCD.PROP_BINDED_PROPERTY, MidpTypes.createStringValue(bindedProperty));
                            dataSet.addComponent(connector);
                        }
                        connector.writeProperty(DataSetConnectorCD.PROP_COMPONENT_ID, MidpTypes.createLongValue(component.getComponentID()));
                        connector.writeProperty(DataSetConnectorCD.PROP_EXPRESSION, MidpTypes.createStringValue(jTextFieldExpression.getText())); //NOI18N
                        if (selectedUpdateCommand != null && !selectedDataSet.equalsIgnoreCase(NULL)) {
                            Collection<DesignComponent> commands = MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).getComponents();
                            for (DesignComponent command : commands) {
                                if (command.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().equals(selectedUpdateCommand)) {
                                    connector.writeProperty(DataSetConnectorCD.PROP_UPDATE_COMMAND, PropertyValue.createComponentReference(command));
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    void resetValuesInModel(final DesignComponent component) {
        final DesignDocument document = component.getDocument();
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                DesignComponent connector = MidpDatabindingSupport.getConnector(component, bindedProperty);
                if (connector != null) {
                    document.deleteComponent(connector);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxDataSets;
    private javax.swing.JComboBox jComboBoxUpdateCommands;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelExpression;
    private javax.swing.JTextField jTextFieldExpression;
    // End of variables declaration//GEN-END:variables

    private void changeComponentsState(String name) {
        if (name != null) {
            enableComponents(name);
        } else {
            disableComponents();
        }
        safeRepaint();
    }

    private void enableComponents(String name) {
        jComboBoxDataSets.setSelectedItem(name);
        jLabelExpression.setText(name + "."); //NOI18N

        jTextFieldExpression.setEnabled(true);
    }

    private void disableComponents() {
        jComboBoxDataSets.setSelectedItem(null);
        jTextFieldExpression.setText(null);
        jLabelExpression.setText(NULL + "."); //NOI18N

        jTextFieldExpression.setEnabled(false);
    }

    private void safeRepaint() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                DataSetElementUI.this.repaint();
            }
        });
    }

    private class Model implements ComboBoxModel {

        private final List<String> names;
        private WeakReference compRef;
        private String selectedItem;
        private TypeID categoryType;

        Model(DesignComponent component, TypeID categoryType) {
            this.categoryType = categoryType;
            this.compRef = new WeakReference(component);
            this.names = new ArrayList<String>();
            this.names.add(NULL);
            Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(component.getDocument(), categoryType).getComponents();
            for (DesignComponent c : components) {
                String name = (String) c.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
                if (name != null || !name.trim().equals("")) { //NOI18N
                    names.add(name);
                }
            }
            
        }

        public void setSelectedItem(final Object item) {
            final DesignComponent component = (DesignComponent) compRef.get();
            if (component == null) {
                return;
            }
            if (item instanceof String) {
                component.getDocument().getTransactionManager().readAccess(new Runnable() {

                    public void run() {
                        String n = (String) item;
                        Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(component.getDocument(), categoryType).getComponents();
                        for (DesignComponent c : components) {
                            if (c.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().equals(n) || n.equals(NULL)) {
                                selectedItem = (String) item;
                                break;
                            }
                        }
                    }
                });
            } else if (item == null) {
                this.selectedItem = NULL;
            } else {
                throw new IllegalArgumentException("Setting argumant is not String type"); //NOI18N

            }
        }

        public Object getSelectedItem() {
            return this.selectedItem;

        }

        public int getSize() {
            return names.size();
        }

        public Object getElementAt(int index) {
            return names.get(index);
        }

        public void addListDataListener(ListDataListener l) {
        }

        public void removeListDataListener(ListDataListener l) {
        }
    }
}
