/*
 * RegisterMBeanPanel.java
 *
 * Created on June 3, 2005, 8:51 AM
 */

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.Introspector;
import org.netbeans.modules.jmx.MBeanOperation;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.actions.AddAttrAction;
import org.netbeans.modules.jmx.actions.AddOpAction;
import org.netbeans.modules.jmx.mbeanwizard.MBeanAttrAndMethodPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  tl156378
 */
public class AddOperationsPanel extends javax.swing.JPanel 
        implements FireEvent, ListSelectionListener {
    
    /** class to add registration of MBean */
    private JavaClass currentClass;
    
    private AddMBeanOperationTableModel operationModel;
    private AddOperationTable operationTable;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    
    public MBeanOperation[] getOperations() {
        MBeanOperation[] Operations = new MBeanOperation[
                operationModel.getRowCount() - operationModel.getFirstEditable()];
        for (int i = 0; i < Operations.length; i++)
            Operations[i] = operationModel.getOperation(
                    operationModel.getFirstEditable() + i);
        return Operations;
    }
     
    /** 
     * Creates new form RemoveAttrPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public AddOperationsPanel(Node node) {
        bundle = NbBundle.getBundle(AddAttrAction.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        Resource rc = JavaModel.getResource(fo);
        currentClass = WizardHelpers.getJavaClass(rc,fo.getName());
        
        // init tags
        
        initComponents();
        
        operationModel = new AddMBeanOperationTableModel();
        operationTable = new AddOperationTable(this,operationModel,this);
        operationTable.setName("OperationTable");
        operationTable.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setViewportView(operationTable);
        operationTable.getSelectionModel().addListSelectionListener(this);
       
        //discovery of existing Operations
        MBeanOperation[] existOperations = Introspector.getOperations(currentClass);
        for (int i = 0; i < existOperations.length; i++)
            operationModel.addOperation(existOperations[i]);
        operationModel.setFirstEditable(existOperations.length);
        
        removeButton.setEnabled(false);
        addButton.addActionListener(
                new AddTableRowListenerWithFireEvent(operationTable, operationModel,
                removeButton, this));
        removeButton.addActionListener(new RemTableRowListenerWithFireEvent(
                operationTable, operationModel, removeButton,this));
        
        // init labels
        Mnemonics.setLocalizedText(addButton,
                     bundle.getString("LBL_Button_AddOperation"));//NOI18N
        Mnemonics.setLocalizedText(removeButton,
                     bundle.getString("LBL_Button_RemoveOperation"));//NOI18N
        
    }
    
    public void event() {
        removeButton.setEnabled(
                (operationModel.getRowCount() > operationModel.getFirstEditable()));
        btnOK.setEnabled(isAcceptable());
    }
    
    public boolean isAcceptable() {
        if (!(operationModel.getRowCount() > operationModel.getFirstEditable())) {
            stateLabel.setText(bundle.getString("LBL_NoOperation"));
            return false;
        } else if (operationNameAlreadyContained()) {
            stateLabel.setText(NbBundle.getMessage(MBeanAttrAndMethodPanel.class,
                                "LBL_State_Same_Operation"));
            return false;
        } else {
            stateLabel.setText("");
            return true;
        }
    }
    
    private boolean operationNameAlreadyContained() {
        //for each operation, construction of the concat operation name 
        //+ all parameter types
        ArrayList operations = new ArrayList(operationModel.size());
        for (int i=0; i < operationModel.size(); i++) {
            //the current operation
            MBeanOperation oper = operationModel.getOperation(i);
            String operationName = oper.getName();
            //for this operation, get all his parameter types concat
            String operationParameter = (String)
                                        oper.getSimpleSignature();
            String operation = operationName.concat(operationParameter);
            operations.add(operation);
        }
        
        // for each operation constructed, verification if there is another one
        // which has same name and
        // parameter types; the order of the parameter types matters
        for (int i=0; i < operations.size(); i++) {
            int count = 0;
            String currentValue = ((String)operations.get(i));
            for(int j=0; j < operations.size(); j++) {
                String compareValue = ((String)operations.get(j));
                if (compareValue.equals(currentValue))
                    count ++;
                if (count >= 2)
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = NbBundle.getMessage(AddAttrAction.class,
                                           "LBL_AddOperationsAction.Title"); //NOI18N

        btnOK = new JButton(bundle.getString("LBL_OK")); //NOI18N
        btnOK.setEnabled(isAcceptable());
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                        btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx(AddOpAction.class),
                        (ActionListener) null
                ));
        
        if (returned == btnOK) {
            return true;
        }
        return false;
    }

    public JavaClass getMBeanClass() {
        return currentClass;
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int firstEditable = operationModel.getFirstEditable();
        if ((operationTable.getSelectedRow() < firstEditable) &&
                (operationModel.getRowCount() > firstEditable))
                operationTable.setRowSelectionInterval(firstEditable, firstEditable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        stateLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(380, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        leftPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        leftPanel.add(addButton, gridBagConstraints);

        buttonsPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(buttonsPanel, gridBagConstraints);

        stateLabel.setForeground(java.awt.SystemColor.activeCaption);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 12);
        add(stateLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel stateLabel;
    // End of variables declaration//GEN-END:variables
    
}
