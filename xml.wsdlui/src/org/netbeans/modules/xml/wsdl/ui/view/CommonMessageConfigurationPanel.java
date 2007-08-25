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

/*
 * CommonMessageConfigurationPanel.java
 *
 * Created on August 25, 2006, 1:18 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author  radval
 */
public class CommonMessageConfigurationPanel extends javax.swing.JPanel {
    
    public static final String PARTS_LISTENER = "PARTS_LISTENER";
    private Map<String, String> namespaceToPrefixMap;
    private Project mProject;
    
    private ElementOrTypeTableCellRenderer elementOrTypeRenderer;
    private WSDLModel mModel;
    
    /** Creates new form CommonMessageConfigurationPanel */
    public CommonMessageConfigurationPanel(Project project, Map<String, String> namespaceToPrefixMap, WSDLModel model) {
        mProject = project;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
        mModel = model;
        initComponents();
        initGUI();
    }
    
    /** Mattise require default constructor otherwise will not load in design view of mattise
     **/
    public CommonMessageConfigurationPanel() {
        namespaceToPrefixMap = new HashMap<String, String>();
        if (!namespaceToPrefixMap.containsKey("xsd")) {
            namespaceToPrefixMap.put("http://www.w3.org/2001/XMLSchema", "xsd");
        }
        initComponents();
        initGUI();
    }
    
    public void addNewRow() {
        PartAndElementOrTypeTableModel model = (PartAndElementOrTypeTableModel) partsTable.getModel();
        model.addNewRow();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        partsScrollPane = new javax.swing.JScrollPane();
        partsTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        partsScrollPane.setAutoscrolls(true);

        partsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null}
            },
            new String [] {
                "Message Part Name", "Element Or Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        partsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        partsTable.setSurrendersFocusOnKeystroke(true);
        partsScrollPane.setViewportView(partsTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(CommonMessageConfigurationPanel.class, "CommonMessageConfigurationPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(CommonMessageConfigurationPanel.class, "CommonMessageConfigurationPanel.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(addButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(removeButton)
                .addContainerGap())
            .add(partsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(partsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        PartAndElementOrTypeTableModel model = (PartAndElementOrTypeTableModel) partsTable.getModel();
        if (partsTable.getCellEditor() != null) {
            partsTable.getCellEditor().cancelCellEditing();
        }
        model.addNewRow();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        if (partsTable.getCellEditor() != null) {
            partsTable.getCellEditor().cancelCellEditing();
        }
        PartAndElementOrTypeTableModel model = (PartAndElementOrTypeTableModel) partsTable.getModel();
        int[] rows = partsTable.getSelectedRows();
        if(rows != null) {
            for(int i = rows.length; i > 0; i--) {
                if (rows[i-1] < partsTable.getRowCount()) {
                    model.removeSelectedRow(rows[i-1]);
                }
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed
            
    private void initGUI() {
        PartAndElementOrTypeTableModel model =  new PartAndElementOrTypeTableModel(namespaceToPrefixMap);
        partsTable.setModel(model);
        partsTable.getColumnModel().getColumn(0).setCellRenderer(new ElementOrTypeTableCellRenderer());
        setUpElementOrTypeColumn(partsTable.getColumnModel().getColumn(1));
        model.addTableModelListener(new TableModelChangeListener());
        partsTable.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            	if (e.getSource() == partsTable) {
            		if (partsTable.getRowCount() == 0) {
            			addButton.requestFocus();
            			return;
            		}
            		if (partsTable.getSelectedRow() == -1) {
            			partsTable.setRowSelectionInterval(0, 0);
            		}
            	}
            }
            
            public void focusLost(FocusEvent e) {
            	//do nothing
            }
            
        });
        
        partsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (partsTable.getSelectedRowCount() > 0) {
                        removeButton.setEnabled(true);
                    } else {
                    	addButton.requestFocus();
                        removeButton.setEnabled(false);
                    }
                }
            }
        });
        
        partsTable.getTableHeader().setReorderingAllowed(false);
        
        //This code enables the mnemonic for add and remove button to work only when this panel is focused.
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK), "button-pressed");
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK), "button-pressed");
        
        Action buttonAction = new AbstractAction() {
		
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("a")) {
					addButton.doClick();
				} else if (e.getActionCommand().equals("r")) {
					removeButton.doClick();
				}
			}
		
		};
        getActionMap().put("button-pressed", buttonAction);
    }
    
    
    public void setUpElementOrTypeColumn(TableColumn elementOrTypeColumn) {
        elementOrTypeColumn.setCellEditor(new ElementOrTypeTableCellEditor(partsTable, namespaceToPrefixMap, mProject, mModel));
        
        //Set up tool tips for the sport cells.
        elementOrTypeRenderer =
                new ElementOrTypeTableCellRenderer();
        elementOrTypeRenderer.setToolTipText(NbBundle.getMessage(CommonMessageConfigurationPanel.class, "CommonMessageConfigurationPanel.renderer.toolTipText"));
        elementOrTypeColumn.setCellRenderer(elementOrTypeRenderer);
    }
    
    public List<PartAndElementOrTypeTableModel.PartAndElementOrType> getPartAndElementOrType() {
        PartAndElementOrTypeTableModel  model = (PartAndElementOrTypeTableModel) partsTable.getModel();
        return model.getPartAndElementOrType();
    }
    
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        addButton.setEnabled(enable);
        removeButton.setEnabled(enable);
        partsTable.setEnabled(enable);
        partsScrollPane.setEnabled(enable);
        
    }
    
    public void scrollToVisible(int rowIndex, int vColIndex) {
        if (!(partsTable.getParent() instanceof JViewport)) {
            return;
        }
        JViewport viewport = (JViewport)partsTable.getParent();
    
        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = partsTable.getCellRect(rowIndex, vColIndex, true);
        Rectangle viewportRect = viewport.getViewRect();
        Dimension tableDimension = partsTable.getSize();
        
        if (viewportRect.contains(rect)) {
        	return;
        }

        //if view port does not contain the cell, make sure that the viewport is moved to the start of the cell.
        viewportRect.x = rect.x;
        viewportRect.y = rect.y;
        
        viewport.setViewPosition(viewportRect.getLocation());
    }
    
    
    class TableModelChangeListener implements TableModelListener {
        
        public void tableChanged(TableModelEvent e) {
            PartAndElementOrTypeTableModel model = (PartAndElementOrTypeTableModel) e.getSource();
            if (e.getType() == TableModelEvent.DELETE) {
                int firstRow = e.getFirstRow();
                int lastRow = e.getLastRow();
                int newRow = firstRow >= lastRow ? firstRow -1 : lastRow -1;
                if (newRow < 0) {
                    newRow = 0;
                }
                if (newRow < model.getRowCount()) {
                    partsTable.setRowSelectionInterval(newRow, newRow);
                    scrollToVisible(newRow, 0);
                }
                firePropertyChange(PARTS_LISTENER, null, "deleted"); 
            } else if (e.getType() == TableModelEvent.INSERT) {
                int rowCount = model.getRowCount();
                int newRow = rowCount - 1;
                if (newRow > -1) {
                    partsTable.setRowSelectionInterval(newRow, newRow);
                    scrollToVisible(newRow, 0);
                }
                firePropertyChange(PARTS_LISTENER, null, "inserted"); 
            } else if (e.getType() == TableModelEvent.UPDATE) {
                int lastRow = e.getLastRow();
                
                boolean allColumns = e.getColumn() == TableModelEvent.ALL_COLUMNS;
                
                if (lastRow > -1 && lastRow < model.getRowCount()) {
                    partsTable.setRowSelectionInterval(lastRow, lastRow);
                    if (!allColumns) {
                        partsTable.setColumnSelectionInterval(e.getColumn(), e.getColumn());
                        scrollToVisible(lastRow, e.getColumn());
                    }
                }
            }
            partsTable.requestFocus();
        }
    }
    
    
    public static void main(String[] args) {
        
//        JFrame frame = new JFrame();
//        frame.getContentPane().setLayout(new BorderLayout());
//        CommonMessageConfigurationPanel p = new CommonMessageConfigurationPanel();
//        frame.getContentPane().add(p, BorderLayout.CENTER);
//        frame.setSize(200, 200);
//        frame.setVisible(true);
        
        
    }

    public void clearSelection() {
        partsTable.clearSelection();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane partsScrollPane;
    private javax.swing.JTable partsTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}
