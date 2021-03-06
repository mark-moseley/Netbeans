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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard.table;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.mbeanwizard.listener.AttributeTextFieldKeyListener;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanAttributeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;

/**
 * Class responsible for the attribute table in the Method and Attribute Panel
 * 
 */
public class AttributeTable extends JTable implements ActionListener {
    
    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    protected FireEvent wiz;
    public static final int ACCESSWIDTH = 95;
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the wizard panel
     */
    public AttributeTable(AbstractTableModel model, FireEvent wiz) {
        super(model);
        this.wiz = wiz;
        this.setColumnSelectionAllowed(false);
        this.setCellSelectionEnabled(true);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        ajustAccessColumnWidth();
    }
    
    protected void ajustAccessColumnWidth() {
        TableColumnModel colModel = this.getColumnModel();
        TableColumn tc = colModel.getColumn(MBeanAttributeTableModel.IDX_ATTR_ACCESS);
        tc.setMaxWidth(ACCESSWIDTH);
        tc.setMinWidth(ACCESSWIDTH);
        tc.setPreferredWidth(ACCESSWIDTH);
    }
    
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
     public TableCellEditor getCellEditor(int row, int column) {   
        if(row >= getRowCount())
            return null;
        if (column == 0) { // attribute name
            final JTextField nameField = new JTextField();
            String o = (String)getModel().getValueAt(row,column);
            nameField.setText(o);
            nameField.addKeyListener(new AttributeTextFieldKeyListener()); 
    
            /* OLD
            nameField.addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent e) {}
                public void keyReleased(KeyEvent e) {}
                public void keyTyped(KeyEvent e) {
                    String txt = nameField.getText();
                    int selectionStart = nameField.getSelectionStart();
                    int selectionEnd = nameField.getSelectionEnd();
                    char typedKey = e.getKeyChar();
                    boolean acceptedKey = false;
                    if (selectionStart == 0) {
                        acceptedKey = Character.isJavaIdentifierStart(typedKey);
                    } else {
                        acceptedKey = Character.isJavaIdentifierPart(typedKey);
                    }
                    if (acceptedKey) {
                        if ((typedKey != KeyEvent.VK_BACK_SPACE) && 
                                (typedKey != KeyEvent.VK_DELETE)) {
                            txt = txt.substring(0, selectionStart) +
                                    typedKey +
                                    txt.substring(selectionEnd);
                        } else if (typedKey == KeyEvent.VK_DELETE) {
                            txt = txt.substring(0, selectionStart) +
                                    txt.substring(selectionEnd);
                        } else {
                            txt = txt.substring(0, selectionStart) +
                                    txt.substring(selectionEnd);
                        }
                    } else {
                        getToolkit().beep();
                    }
                    txt = WizardHelpers.capitalizeFirstLetter(txt);
                    nameField.setText(txt);
                    if ((typedKey == KeyEvent.VK_BACK_SPACE) || 
                            (typedKey == KeyEvent.VK_DELETE))
                        nameField.setCaretPosition(selectionStart);
                    else {
                        if (acceptedKey) {
                            nameField.setCaretPosition(selectionStart + 1);
                        } else {
                            nameField.setCaretPosition(selectionStart);
                        }                            
                    }
                        
                    e.consume();
                }
            });*/
            
            return new JTextFieldCellEditor(nameField, this);
        } else {
            if (column == 1) { //attribute type
                JComboBox typeBox = WizardHelpers.instanciateTypeJComboBox();
                Object o = getModel().getValueAt(row,column);
                typeBox.setSelectedItem(o);
                typeBox.addActionListener(this); 
                return new JComboBoxCellEditor(typeBox, this);
            } else {
                if (column == 2) { //access mode
                    JComboBox accessBox = 
                            WizardHelpers.instanciateAccessJComboBox();
                    accessBox.setName("attrAccessBox");// NOI18N
                    Object o = getModel().getValueAt(row,column);
                    accessBox.setSelectedItem(o);
                    return new JComboBoxCellEditor(accessBox, this);
                } else {
                    if (column == 3) { //attribute description
                        JTextField descrField = new JTextField();
                        String o = (String)getModel().getValueAt(row,column);
                        descrField.setText(o);
                        return new JTextFieldCellEditor(descrField, this);
                    }
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
     /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        if(row >= getRowCount())
            return null;
        
            if (column == 1) {
                JComboBox typeBox = WizardHelpers.instanciateTypeJComboBox();
                return new ComboBoxRenderer(typeBox, true, true);
            } else {
                if (column == 2) {
                    JComboBox accessBox = 
                            WizardHelpers.instanciateAccessJComboBox();
                    return new ComboBoxRenderer(accessBox);
                }
            }
        return super.getCellRenderer(row,column);
    }
    
    
    /**
     * Returns the wizard panel
     * @return AttributesWizardPanel the wizard panel
     */
    public FireEvent getWiz() {
    
        return this.wiz;
    }

    public void actionPerformed(ActionEvent arg0) {
        wiz.event();
    }
}
