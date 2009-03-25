/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.debugger.gdb.ui;

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.netbeans.modules.cnd.debugger.gdb.GdbContext;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.disassembly.RegisterValue;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class RegistersTopComponent extends TopComponent implements PropertyChangeListener {

    private static RegistersTopComponent instance;
    /** path to the icon used by the component and its open action */
    // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
    static final String ICON_PATH = "org/netbeans/modules/cnd/debugger/gdb/resources/registers.png"; // NOI18N

    private static final String PREFERRED_ID = "RegistersTopComponent"; // NOI18N
    
    private final RegisterTableModel model = new RegisterTableModel();
    
    private RegistersTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(RegistersTopComponent.class, "CTL_RegistersTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(RegistersTopComponent.class, "HINT_RegistersTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        
        jScrollPane1.getViewport().setBackground(UIManager.getColor("Table.background")); // NOI18N
        jTable1.setDefaultRenderer(RegisterValue.class, new RegisterCellRendererForValue());
        TableColumn col = jTable1.getColumnModel().getColumn(RegisterTableModel.COLUMN_VALUE);
        col.setCellEditor(new RegisterTableCellEditor());
        
        model.refresh();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jTable1.setModel(model);
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized RegistersTopComponent getDefault() {
        if (instance == null) {
            instance = new RegistersTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the RegistersTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized RegistersTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(RegistersTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof RegistersTopComponent) {
            return (RegistersTopComponent) win;
        }
        Logger.getLogger(RegistersTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        GdbContext.getInstance().addPropertyChangeListener(GdbContext.PROP_REGISTERS, this);
    }

    @Override
    public void componentClosed() {
        GdbContext.getInstance().removePropertyChangeListener(GdbContext.PROP_REGISTERS, this);
    }

    @Override
    protected void componentShowing() {
        model.refresh();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        model.refresh();
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return RegistersTopComponent.getDefault();
        }
    }
    
    private static class RegisterTableModel extends AbstractTableModel {
        
        private final List<RegisterValue> values = new ArrayList<RegisterValue>();
        
        public static final int COLUMN_REGISTER=0;
        public static final int COLUMN_VALUE=1;
        
        private final String[] columnNames = new String[] {
            NbBundle.getMessage(RegistersTopComponent.class, "LBL_REGUSAGE_REGISTER"), // NOI18N
            NbBundle.getMessage(RegistersTopComponent.class, "LBL_REGUSAGE_VALUE") // NOI18N
        };
        
        private final Class[] types = new Class[] {
            String.class, RegisterValue.class
        };
               
        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return values.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            RegisterValue val = values.get(rowIndex);
            switch (columnIndex) {
                case COLUMN_REGISTER: return val.getName();
                case COLUMN_VALUE: return val;
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == COLUMN_VALUE;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            RegisterValue oldVal = values.get(rowIndex);
            String value = (String) aValue;

            if (oldVal.getValue().equals(value)) {
                return;
            }

            GdbDebugger gdbDebugger = GdbDebugger.getGdbDebugger();
            if (gdbDebugger == null) {
                return;
            }
            String gdbValue = gdbDebugger.updateVariable("$" + oldVal.getName(), value); // NOI18N

            // gdb returns value in decimal format
            try {
                gdbValue = "0x" + Integer.toHexString(Integer.parseInt(gdbValue)); // NOI18N
            } catch (Exception e) {
                // do nothing
            }

            RegisterValue val = new RegisterValue(oldVal.getName(), gdbValue, false);
            values.set(rowIndex, val);
            GdbContext.getInstance().setProperty(GdbContext.PROP_REGISTERS, new ArrayList<RegisterValue>(values));
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @SuppressWarnings("unchecked")
        private void refresh() {
            values.clear();
            Collection<RegisterValue> res = 
                    (Collection<RegisterValue>)GdbContext.getInstance().getProperty(GdbContext.PROP_REGISTERS);
            if (res != null) {
                values.addAll(res);
            }
            Collections.sort(values, REGISTER_COMPARATOR);
            fireTableDataChanged();
        }
    }
    
    private static final Comparator<RegisterValue> REGISTER_COMPARATOR = new Comparator<RegisterValue>() {
        public int compare(RegisterValue o1, RegisterValue o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static class RegisterTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the component that will handle the editing of the cell value
        JComponent component = new JTextField();

        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int vColIndex) {
            // 'value' is value contained in the cell located at (rowIndex, vColIndex)

            if (isSelected) {
                // cell (and perhaps other cells) are selected
            }

            RegisterValue rv = (RegisterValue) value;

            // Configure the component with the specified value
            ((JTextField)component).setText(rv.getValue());

            // Return the configured component
            return component;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            return ((JTextField)component).getText();
        }
    }

    
    private static class RegisterCellRendererForValue extends DefaultTableCellRenderer.UIResource {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setEnabled(table == null || table.isEnabled());
            
            super.getTableCellRendererComponent(table, ((RegisterValue)value).getValue(), isSelected, hasFocus, row, column);
            
            if (value instanceof RegisterValue) {
                RegisterValue rval = (RegisterValue) value;
                if (rval.isModified()) {
                    super.setFont(getFont().deriveFont(Font.BOLD));
                }
            }
            
            return this;
        }
    }
}
