/*
 * EndpointConfigPanel.java
 *
 * Created on January 31, 2008, 12:50 PM
 */

package org.netbeans.modules.compapp.projects.jbi.jeese.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.modules.compapp.javaee.codegen.model.EndpointCfg;
import org.openide.util.NbBundle;

/**
 *
 * @author  gpatil
 */
public class EndpointConfigPanel extends javax.swing.JPanel {

    /** Creates new form EndpointConfigPanel */
    public EndpointConfigPanel(List<EndpointCfg> cfgs) {
        this.epCfgs = cfgs;
        if (this.epCfgs != null){
            for (EndpointCfg cfg : this.epCfgs){
                if (EndpointCfg.EndPointType.Provider.equals(cfg.getEndPointType())){
                    this.providerEps.add(cfg);
                }else {
                    this.consumerEps.add(cfg);
                }
            }
        }
        initComponents();
        
        TableColumn col = this.jtblEndpoints.getColumnModel().getColumn(0);
        col.setCellRenderer(new EndpointColumnRenderer(this.jtblEndpoints.getDefaultRenderer(String.class), this.providerEps));

        col = this.jtblEndpoints1.getColumnModel().getColumn(0);
        col.setCellRenderer(new EndpointColumnRenderer(this.jtblEndpoints1.getDefaultRenderer(String.class), this.consumerEps));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jtblEndpoints = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jtblEndpoints1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 100));

        jtblEndpoints.setModel(getProviderEndpointsTableModel());
        jtblEndpoints.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jtblEndpoints.setName("EndpointsTable");
        jScrollPane1.setViewportView(jtblEndpoints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        jScrollPane2.setAutoscrolls(true);
        jScrollPane2.setPreferredSize(new java.awt.Dimension(400, 100));

        jtblEndpoints1.setModel(getConsumerEndpointsTableModel());
        jtblEndpoints1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jtblEndpoints1.setName("EndpointsTable");
        jScrollPane2.setViewportView(jtblEndpoints1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane2, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(EndpointConfigPanel.class, "LBL_CFG_Provider")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(EndpointConfigPanel.class, "LBL_Consumer_Cfg")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLabel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jtblEndpoints;
    private javax.swing.JTable jtblEndpoints1;
    // End of variables declaration//GEN-END:variables

    private List<EndpointCfg> epCfgs;
    private List<EndpointCfg> providerEps = new ArrayList<EndpointCfg>();
    private List<EndpointCfg> consumerEps = new ArrayList<EndpointCfg>();
    private String lblconsumerEpt = NbBundle.getMessage(EndpointConfigPanel.class, "LBL_TblCol_ConsumerEpt"); //NOI18N
    private String lblActivateNMR = NbBundle.getMessage(EndpointConfigPanel.class, "LBL_TblCol_ActivateForNMR"); //NOI18N
    private String lblProviderEpt = NbBundle.getMessage(EndpointConfigPanel.class, "LBL_TblCol_ProviderEpt");//NOI18N
    private String lblActivateJavaEEHttp = NbBundle.getMessage(EndpointConfigPanel.class, "LBL_TblCol_ActivateJavaEEHttpPort"); //NOI18N    
    
    private TableModel getProviderEndpointsTableModel(){
        return new ProviderEndpointsTableModel(this.providerEps, new String[]{lblProviderEpt, lblActivateJavaEEHttp});
        

//                new javax.swing.table.DefaultTableModel(
//            new Object [][] {
//                {"Provides {svcNs.com}svc1/{intfcNs.com}Interface/Port", new Boolean(true), new Boolean(true)},
//                {"Consumes {svcNs.com}svc1/{intfcNs.com}Interface/Port", new Boolean(true), new Boolean(true)},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null}
//            },
//            new String [] {
//                "Endpoint", "Activate Thru JBI", "Listern on default HTTP Port"
//            }
//        ) {
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
//            };
//            boolean[] canEdit = new boolean [] {
//                false, true, true
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//
//            public boolean isCellEditable(int rowIndex, int columnIndex) {
//                return canEdit [columnIndex];
//            }
//        };
    }

    private TableModel getConsumerEndpointsTableModel(){
        return new ConsumerEndpointsTableModel(this.consumerEps, new String[] {lblconsumerEpt, lblActivateNMR});
//            new Object [][] {
//                {"Provides {svcNs.com}svc1/{intfcNs.com}Interface/Port", new Boolean(true), new Boolean(true)},
//                {"Consumes {svcNs.com}svc1/{intfcNs.com}Interface/Port", new Boolean(true), new Boolean(true)},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null},
//                {null, null, null}
//            },
//            new String [] {
//                "Endpoint", "Activate Thru JBI", "Listern on default HTTP Port"
//            }
//        ) {
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
//            };
//            boolean[] canEdit = new boolean [] {
//                false, true, true
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//
//            public boolean isCellEditable(int rowIndex, int columnIndex) {
//                return canEdit [columnIndex];
//            }
//        };
    }

    public List<EndpointCfg> getEndpointCfgs(){
        //update EndpointCfgs from the table.
        return this.epCfgs;
    }


    static class ProviderEndpointsTableModel extends javax.swing.table.DefaultTableModel{
        List<EndpointCfg> providers = null;
        String[] colHdrs = null;
        Class[] colTypes = new Class[]{String.class, Boolean.class};
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return colTypes[columnIndex];
        }
        
        public ProviderEndpointsTableModel(List<EndpointCfg> epts, String[] ch) {
            super(epts.size(), 2);
            providers = epts;
            colHdrs = ch;
        }

        @Override
        public String getColumnName(int column) {
            return colHdrs[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 1){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            EndpointCfg cfg = providers.get(row);
            if ((cfg != null) && (column == 1)){
                cfg.setActivateJavaEEHttpPort(((Boolean)aValue).booleanValue());
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            EndpointCfg cfg = providers.get(row);

            if (cfg != null){
                if (column == 0){
                    return cfg.getEndPointName();
                }else {
                    if (cfg.isActivateJavaEEHttpPort()){
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }
            }

            return null;
        }
    }

    static class ConsumerEndpointsTableModel extends javax.swing.table.DefaultTableModel{
        List<EndpointCfg> consumers = null;
        String[] colHeaders = null;
        Class[] colTypes = new Class[]{String.class, Boolean.class};
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return colTypes[columnIndex];
        }
        
        public ConsumerEndpointsTableModel(List<EndpointCfg> epts, String[] colHdrs) {
            super(epts.size(), 2);
            consumers = epts;
            colHeaders = colHdrs;
        }

        @Override
        public String getColumnName(int column) {
            return colHeaders[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 1){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            EndpointCfg cfg = consumers.get(row);
            if ((cfg != null) && (column == 1)){
                cfg.setActivateForNMR(((Boolean)aValue).booleanValue());
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            EndpointCfg cfg = consumers.get(row);

            if (cfg != null){
                if (column == 0){
                    return cfg.getEndPointName();
                }else {
                    if (cfg.isActivateForNMR()){
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                }
            }

            return null;
        }
    }
        
    public class EndpointColumnRenderer extends JLabel implements TableCellRenderer {
        private TableCellRenderer delegate;
        private List<EndpointCfg> cfgs ;
        public EndpointColumnRenderer(TableCellRenderer dlg, List<EndpointCfg> cs){
            delegate = dlg;
            cfgs = cs;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            JComponent comp = (JComponent) delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
            EndpointCfg cfg = cfgs.get(rowIndex);
            if (cfg != null){
                comp.setToolTipText(cfg.getEPToolTip());                    
            }

            return comp;
        }
    
        // The following methods override the defaults for performance reasons
        @Override
        public void validate() {}
        @Override
        public void revalidate() {}
        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
}
