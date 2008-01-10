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


package org.netbeans.modules.asm.core.ui.top;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.netbeans.modules.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.asm.core.assistance.RegisterUsageAccesor;
import org.netbeans.modules.asm.core.assistance.RegisterUsageAccesor.RegisterStatus;
import org.netbeans.modules.asm.core.ui.top.ext.TableSorter;
import org.netbeans.modules.asm.core.assistance.RegisterChooserListener;
import org.netbeans.modules.asm.core.assistance.RegisterChooser;
import org.netbeans.modules.asm.model.AsmModel;
import org.netbeans.modules.asm.model.lang.Register;

public class RegisterUsagesPanel extends JPanel implements NavigatorTab,
                                          RegisterUsageAccesor {
        
    private AsmModel model;
    private final DefaultTableModel tableModel;  
    
    private RegisterChooserListener chooserListener;
            
    private static RegisterUsagesPanel instance;       
    
    public static synchronized RegisterUsagesPanel getInstance() {
        if (instance == null) {
            instance = new RegisterUsagesPanel();
        }
        return instance;
    }
        
    private RegisterUsagesPanel() {
        initComponents();        
        
        tableModel = new RegisterTableModel();
        TableSorter tmpModel = new TableSorter(tableModel);
                       
        tmpModel.setColumnComparator(String.class, TableSorter.LEXICAL_COMPARATOR);
        tmpModel.setTableHeader(jRegisterTable.getTableHeader());
        
        jRegisterTable.setDefaultRenderer(Register.class, new RegisterCellRendererForRegister());        
        jRegisterTable.setDefaultRenderer(RegisterStatus.class, new RegisterCellRendererForUsage());        
               
        jRegisterTable.setModel(tmpModel); 
        
        jRegisterTable.getSelectionModel().addListSelectionListener(new RegisterSelectionListener());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RegisterUsagesPanel.class, "CTL_REGS_NAME");
    }
    
    public void setChooserListener(RegisterChooserListener chooserListener) {
        this.chooserListener = chooserListener;
    }
    
    public JPanel getPanel() {
        return this;
    }
    
    public void setDocument(DataObject dob) {
        this.model = AsmObjectUtilities.getModel(dob);
        
        if (model == null)
            return;
        
        Document doc = AsmObjectUtilities.getDocument(dob);
        chooserListener = (RegisterChooserListener) 
                doc.getProperty(RegisterChooserListener.class);
        setRegisters();
    }
    
    public void setRegisterStatus(Register reg, RegisterStatus status) {        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (reg == tableModel.getValueAt(i, 0)) {
                tableModel.setValueAt(status, i, 1);
                return;
            }
        }                  
    }
    
    public void setRegisterStatus(Collection<Register> regs, RegisterStatus status) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (regs.contains(tableModel.getValueAt(i, 0))) {
                tableModel.setValueAt(status, i, 1);                
            }
        }         
    }
    
    public void clearStatuses() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("", i, 1);                            
        } 
    }
    
    private void setRegisters() {
        tableModel.setNumRows(0);
        
        for (Register reg : model.getRegisterSet()) {
            if (reg.getDirectParent() == null) {
                tableModel.addRow(new Object[] { reg, "" } );               
            }
        }                
    }
    
    private class RegisterSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            int sel = jRegisterTable.getSelectedRow();
            
            if (chooserListener != null) {
                if (sel != -1) {
                    final Register reg = (Register) tableModel.getValueAt(sel, 0);                
                    chooserListener.update(new RegisterChooser() {
                        public Collection<Register> getRegisters() {
                            return Arrays.asList(reg);
                        }
                    });
                }
                else {
                    chooserListener.update(new RegisterChooser() {
                        public Collection<Register> getRegisters() {
                            return Collections.<Register>emptyList();
                        }
                    });
                }
            }
        }        
    }
    
    public static final Color ARG_COLOR = new Color(235, 222, 194);
    public static final Color READ_COLOR = new Color(220, 234, 196);
    public static final Color USED_COLOR = new Color(253, 242, 196);
    public static final Color WRITE_COLOR = new Color(235, 199, 194);
    
    private static class RegisterCellRendererForUsage extends DefaultTableCellRenderer.UIResource {
	public RegisterCellRendererForUsage() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setEnabled(table == null || table.isEnabled());
            
            if (RegisterUsageAccesor.PredefinedStatuses.STATUS_ARG.equals(value)) {
                setBackground(ARG_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_READ.equals(value)) {
                setBackground(READ_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_USED.equals(value)) {
                setBackground(USED_COLOR);
            } else if (RegisterUsageAccesor.PredefinedStatuses.STATUS_WRITE.equals(value)) {
                setBackground(WRITE_COLOR);
            } else {
                setBackground(null);
            }
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }
    
    private static class RegisterCellRendererForRegister extends DefaultTableCellRenderer.UIResource {
	public RegisterCellRendererForRegister() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
	}	                                             
    }
    
    private static class RegisterTableModel extends DefaultTableModel {
          
        private Class[] types; 
         
        public RegisterTableModel() {
             super(new Object [][]  { },
                   new String [] { 
                        NbBundle.getMessage(RegisterUsagesPanel.class, "LBL_REGUSAGE_REGISTER"),
                        NbBundle.getMessage(RegisterUsagesPanel.class, "LBL_REGUSAGE_USAGE")});
             
             types = new Class [] { Register.class, RegisterStatus.class };
         }
               
        @Override
        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }      
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTableScrollPane = new javax.swing.JScrollPane();
        jRegisterTable = new javax.swing.JTable();

        jTableScrollPane.setViewportView(jRegisterTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
  
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jRegisterTable;
    private javax.swing.JScrollPane jTableScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
