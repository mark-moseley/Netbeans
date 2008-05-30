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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.tools.java.generator;

import org.netbeans.modules.xml.tools.generator.*;
import org.netbeans.modules.xml.tools.java.generator.SAXGeneratorAbstractPanel;
import org.netbeans.modules.xml.tools.java.generator.ParsletBindings;
import org.netbeans.modules.xml.tools.java.generator.GenerateSupportUtils;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import java.awt.*;
import java.util.*;
import java.beans.*;

//import org.openide.src.Type;
//import org.openide.src.Identifier;
import org.openide.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Allows to define mapping element name => (method identifier, return type)
 * in visual way. Class holding methods performing convertions is called parslet (set
 * of mini-parsers).
 *
 * @author  Petr Kuzel
 * @version
 */
public final class SAXGeneratorParsletPanel extends SAXGeneratorAbstractPanel {

    /** Serial Version UID */
    private static final long serialVersionUID =6038099487791703339L;    


    private TableModel tableModel;  //it is based on model

    private static final int ELEMENT_COLUMN = 0;
    private static final int METHOD_COLUMN = 1;  // represents parslet
    private static final int TYPE_COLUMN = 2;    
    private static final int COLUMNS = 3;
    
    private static final String NO_METHOD = "[none]"; // NOI18N
    
    private final String[] COLUMN_NAMES = new String[] {
        NbBundle.getMessage(SAXGeneratorParsletPanel.class, "SAXGeneratorParsletPanel.table.column1"),
        NbBundle.getMessage(SAXGeneratorParsletPanel.class, "SAXGeneratorParsletPanel.table.column2"),
        NbBundle.getMessage(SAXGeneratorParsletPanel.class, "SAXGeneratorParsletPanel.table.column3"),
    };

    private final ValidatingTextField.Validator TYPE_VALIDATOR = new ValidatingTextField.Validator() {
        public boolean isValid(String value) {
            boolean ret = GenerateSupportUtils.isValidReturnType(value);
            setValid(ret);
            return ret;
        }
        
        public String getReason() {
            return NbBundle.getMessage(SAXGeneratorParsletPanel.class, "MSG_parslet_err_1");
        }
    };
    
    private final ValidatingTextField.Validator METHOD_VALIDATOR = new ValidatingTextField.Validator() {
        public boolean isValid(String value) {
            boolean ret = NO_METHOD.equals(value) || Utilities.isJavaIdentifier(value);
            setValid(ret);
            return ret;
        }
        
        public String getReason() {
            return NbBundle.getMessage(SAXGeneratorParsletPanel.class, "MSG_parslet_err_2");
        }
        
    };
    
    /** Creates new form SAXGeneratorParsletPanel */
    public SAXGeneratorParsletPanel() {
//        try {
//            this.putClientProperty("WizardPanel_helpURL", new URL("nbresloc:/org/netbeans/modules/xml/tools/generator/SAXGeneratorParsletPanel.html"));  //NOI18N
//        } catch (MalformedURLException ex) {
//        }            
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descTextArea = new javax.swing.JTextArea();
        tableScrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setName(NbBundle.getMessage(SAXGeneratorParsletPanel.class, "SAXGeneratorParsletPanel.Form.name")); // NOI18N
        setPreferredSize(new java.awt.Dimension(480, 350));
        setLayout(new java.awt.GridBagLayout());

        descTextArea.setWrapStyleWord(true);
        descTextArea.setLineWrap(true);
        descTextArea.setEditable(false);
        descTextArea.setForeground(new java.awt.Color(102, 102, 153));
        descTextArea.setFont(javax.swing.UIManager.getFont ("Label.font"));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/tools/generator/Bundle"); // NOI18N
        descTextArea.setText(bundle.getString("DESC_saxw_convertors")); // NOI18N
        descTextArea.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        descTextArea.setEnabled(false);
        descTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(descTextArea, gridBagConstraints);

        table.setModel(tableModel);
        tableScrollPane.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(tableScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Prepare all models used by this form
     */
    private void initModels() {
        
        // there is one huge table allowing main customization
        
        tableModel = new ParsletsTableModel();
    }
    
    
    /**
     * Upadate view according to model.
     */
    protected void initView() {
        initModels();
        initComponents ();
	
        table = new ParsletsTable();
        table.setModel(tableModel);        
        tableScrollPane.setViewportView(table);  //install it
        
        initAccessibility();
    }

    protected void updateView() {
    }
    
    /**
     * Our table model directly updates model data.
     * We need just reset parslets if not used.
     */
    protected void updateModel() {
//        if (parsletsCheckBox.isSelected()==false) {
//            model.getParsletBindings().clear();
//        }
    }

    
    // ~~~~~~~~~~~~~~ Table and its model ~~~~~~~~~~~~~~~~~~~~
    
    /**
     * The table using dynamic cell editors for TYPE_COLUMN and METHOD_COLUMN
     */
    private class ParsletsTable extends JTable {

        /** Serial Version UID */
        private static final long serialVersionUID =1076091649418718319L;
        
        public ParsletsTable() {
            getTableHeader().setReorderingAllowed(false);
           // setRowHeight(Util.getTextCellHeight(this));
        }
        
        public TableCellEditor getCellEditor(int row, int column) {
            
            switch (column) {                
                
                case METHOD_COLUMN:
                    Vector all = new Vector(model.getParsletBindings().keySet());
                    JComboBox editor = new JComboBox(all);
                    editor.addItem(NO_METHOD);
                    ValidatingTextField methodInput = new ValidatingTextField();
                    methodInput.setValidator(METHOD_VALIDATOR);
                    editor.setEditor(methodInput);
                    editor.setEditable(true);
                    return new DefaultCellEditor(editor);
                
                case TYPE_COLUMN:
                    String[] vals = {
                        "int", "boolean","long", "java.util.Date", "java.net.URL", // NOI18N
                        "java.lang.String", "java.lang.String[]" // NOI18N
                    };
                    JComboBox type_editor = new JComboBox(vals);
                    ValidatingTextField typeInput = new ValidatingTextField();
                    typeInput.setValidator(TYPE_VALIDATOR);
                    type_editor.setEditor(typeInput);
                    type_editor.setEditable(true);                    
                    return new DefaultCellEditor(type_editor);

                    
                default:
                    return super.getCellEditor(row, column);
            }
        }
        
    }
    
    /**
     * Model contrainig some parslets issues
     */
    private class ParsletsTableModel extends AbstractTableModel {
            
        /** Serial Version UID */
        private static final long serialVersionUID =3516749023312029787L;
        
        public String getColumnName(int col) {
            return COLUMN_NAMES[col];
        }

        public int getRowCount() {
            if (model == null) return 0;
            return model.getElementBindings().size();
        }

        public int getColumnCount() {
            return COLUMNS;
        }


        /**
          * @return Type or String depending on column
          */
        public Object getValueAt(int row, int column) {
            ElementBindings.Entry entry = model.getElementBindings().getEntry(row);
            ParsletBindings parslets = model.getParsletBindings();

            // set to parslet or null if none assigned
            String parslet = entry.getParslet();

            switch (column) {                
                case ELEMENT_COLUMN: 
                    return entry.getElement();
                    
                case TYPE_COLUMN: {
                    if (parslet == null) {
                        return String.class.getName();
                    } else {
                        return parslets.getReturnType(parslet).toString();
                    }
                }
                    
                case METHOD_COLUMN: {
                    if (parslet == null) {
                        return NO_METHOD;
                    } else {
                        return parslet;
                    }
                }
            }
            return null;
        }

        /**
          * @param value must be a String regardless column
          */
        public void setValueAt(Object value, int row, int col) {

//            new RuntimeException("Setting Value to " + value).printStackTrace();
            
            //TODO: Retouche
//            if (col == TYPE_COLUMN) {
//                if (TYPE_VALIDATOR.isValid((String) value) == false) return;
//            } else if (col == METHOD_COLUMN) {
//                if (METHOD_VALIDATOR.isValid((String)value) == false) return;
//            }
//            
//            ElementBindings binds = model.getElementBindings();
//            ElementBindings.Entry entry = binds.getEntry(row);
//
//            // set to parslet or null if none assigned
//            String currentParslet = entry.getParslet();
//
//            ParsletBindings parslets = model.getParsletBindings();
//            ParsletBindings.Entry parsletEntry = parslets.getEntry(currentParslet);
//
//            switch (col) {
//                case TYPE_COLUMN:
//                    // all parslets of the same name must return same type
//                    if (currentParslet == null) {
//                       break;  // can not set a value
//                    } else {
//                        if (binds.getParsletUsageCount(currentParslet)>1) {
//                            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Cannot change"); // NOI18N
//                        } else {
//                            Type returnType = 
//                                Type.createClass(Identifier.create((String)value));
//                            parsletEntry.setReturnType(returnType); 
//                        }
//                    }
//                    break;
//
//                // setting parslet determines return value    
//                case METHOD_COLUMN:    
//                                        
//                    String identifier = (String)value;
//                    String oldReturnType = parslets.getReturnType(currentParslet).toString();
//                    
//                    if (binds.getParsletUsageCount(currentParslet) == 1) {
//                        parslets.remove(currentParslet);
//                    }
//                    if (NO_METHOD.equals(identifier) == false) {
//                        entry.setParslet(identifier);
//                    
//                        if (parslets.getEntry(identifier) == null) {
//                            //parslets.put(identifier, (String) getValueAt(row, TYPE_COLUMN));
//                            parslets.put(identifier, oldReturnType);
//                        } else {
//                            fireTableRowsUpdated(row,row);
//                        }
//                    } else {
//                        entry.setParslet(null);
//                        fireTableRowsUpdated(row,row);
//                    }
//                    break;
//
//            }
//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(model.toString());

        }

        public boolean isCellEditable(int row, int col) {
            return col != ELEMENT_COLUMN;
        }
    };  //ParsletsTableModel
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JTable table;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

    /** Initialize accesibility
     */    
    public void initAccessibility(){

        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorParsletPanel.class, "ACSD_SAXGeneratorParsletPanel"));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorParsletPanel.class, "ACSD_ParsletsTable"));
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SAXGeneratorParsletPanel.class, "ACSN_ParsletsTable"));        
    }
}
