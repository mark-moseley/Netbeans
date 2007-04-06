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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.table.JTableHeader;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Simple property editor for <code>JTableHeader</code>.
 *
 * @author Jan Stola
 */
public class JTableHeaderEditor extends PropertyEditorSupport
        implements NamedPropertyEditor, FormCodeAwareEditor, XMLPropertyEditor {
    /** Property being edited. */
    private FormProperty property;
    /** Determines whether UI of the customizer have been initialized. */
    private boolean initialized;
    
    /**
     * Retruns display name of this property editor. 
     * 
     * @return diaplay name of this property editor.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "TableHeaderEditor"); // NOI18N
    }

    /**
     * Sets context of the property editor. 
     * 
     * @param formModel form model.
     * @param property property being edited.
     */
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = property;
    }

    /**
     * Determines whether this property editor supports custom editing. 
     * 
     * @return <code>true</code>.
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * Returns custom editor.
     * 
     * @return custom editor.
     */
    public Component getCustomEditor() {
        if (!initialized) {
            initialized = true;
            initComponents();
        }
        updateUI();
        return customizer;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        customizer = new javax.swing.JPanel();
        resizingCheckBox = new javax.swing.JCheckBox();
        reorderingCheckBox = new javax.swing.JCheckBox();

        FormListener formListener = new FormListener();

        resizingCheckBox.setText(org.openide.util.NbBundle.getMessage(JTableHeaderEditor.class, "TableHeaderEditor_Resizing")); // NOI18N
        resizingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        resizingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resizingCheckBox.addActionListener(formListener);

        reorderingCheckBox.setText(org.openide.util.NbBundle.getMessage(JTableHeaderEditor.class, "TableHeaderEditor_Reordering")); // NOI18N
        reorderingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reorderingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reorderingCheckBox.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout customizerLayout = new org.jdesktop.layout.GroupLayout(customizer);
        customizer.setLayout(customizerLayout);
        customizerLayout.setHorizontalGroup(
            customizerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .add(customizerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resizingCheckBox)
                    .add(reorderingCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        customizerLayout.setVerticalGroup(
            customizerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .add(resizingCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reorderingCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == resizingCheckBox) {
                JTableHeaderEditor.this.headerChanged(evt);
            }
            else if (evt.getSource() == reorderingCheckBox) {
                JTableHeaderEditor.this.headerChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Invoked when some value has been changed in customizer. 
     * 
     * @param evt event describing the change.
     */
    private void headerChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerChanged
        updateFromUI();
        firePropertyChange();
    }//GEN-LAST:event_headerChanged
    
    /**
     * Updates property according to the value in the customizer.
     */
    private void updateFromUI() {
//        if (resizingCheckBox.isSelected() && reorderingCheckBox.isSelected()) {
//            try {
//                Object value = property.getRealValue();
//                if (value instanceof JTableHeader) {
//                    JTableHeader header = (JTableHeader)value;
//                    header.setResizingAllowed(true);
//                    header.setReorderingAllowed(true);
//                    setValue(header);
//                }
//            } catch (IllegalAccessException iaex) {
//                iaex.printStackTrace();
//            } catch (InvocationTargetException itex) {
//                itex.printStackTrace();
//            }
//        } else {
            setValue(new FormTableHeader(property, resizingCheckBox.isSelected(), reorderingCheckBox.isSelected()));
//        }
    }

    /**
     * Updates UI of the customizer according to the current value of the property.
     */
    private void updateUI() {
        Object value = getValue();
        boolean resizing = true;
        boolean reordering = true;
        if (value instanceof FormTableHeader) {
            FormTableHeader header = (FormTableHeader)value;
            resizing = header.isResizingAllowed();
            reordering = header.isReorderingAllowed();
        } else if (value instanceof JTableHeader) {
            JTableHeader header = (JTableHeader)value;
            resizing = header.getResizingAllowed();
            reordering = header.getReorderingAllowed();
        }
        resizingCheckBox.setSelected(resizing);
        reorderingCheckBox.setSelected(reordering);
    }
    
    public String getSourceCode() {
        RADProperty property = (RADProperty)this.property;
        RADComponent comp = property.getRADComponent();
        CodeVariable var = comp.getCodeExpression().getVariable();
        String varName = (var == null) ? null : var.getName();
        String readMethod = property.getPropertyDescriptor().getReadMethod().getName();
        String getter = readMethod + "()"; // NOI18N
        if (varName != null) {
            getter = varName + '.' + getter;
        }

        boolean resizing = true;
        boolean reordering = true;
        Object value = getValue();
        if (value instanceof FormTableHeader) {
            FormTableHeader header = (FormTableHeader)value;
            resizing = header.isResizingAllowed();
            reordering = header.isReorderingAllowed();
        }
        String code = ""; // NOI18N
        if (!resizing) {
            code += getter + ".setResizingAllowed(false);\n"; // NOI18N
        }
        if (!reordering) {
            code += getter + ".setReorderingAllowed(false);\n"; // NOI18N
        }
        return (resizing && reordering) ? null : code;
    }

    private static final String XML_TABLE_HEADER = "TableHeader"; // NOI18N
    private static final String ATTR_RESIZING = "resizingAllowed"; // NOI18N
    private static final String ATTR_REORDERING = "reorderingAllowed"; // NOI18N

    public void readFromXML(Node element) throws IOException {
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        Node node = attributes.getNamedItem(ATTR_RESIZING);
        boolean resizing = true;
        if (node != null) {
            resizing = Boolean.valueOf(node.getNodeValue()).booleanValue();
        }
        node = attributes.getNamedItem(ATTR_REORDERING);
        boolean reordering = true;
        if (node != null) {
            reordering = Boolean.valueOf(node.getNodeValue()).booleanValue();
        }
        setValue(new FormTableHeader(property, resizing, reordering));
    }

    public Node storeToXML(Document doc) {
        Object value = getValue();
        org.w3c.dom.Element el = null;
        if (value instanceof FormTableHeader) {
            FormTableHeader header = (FormTableHeader)value;
            el = doc.createElement(XML_TABLE_HEADER);
            el.setAttribute(ATTR_RESIZING, Boolean.toString(header.isResizingAllowed()));
            el.setAttribute(ATTR_REORDERING, Boolean.toString(header.isReorderingAllowed()));
        }
        return el;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizer;
    private javax.swing.JCheckBox reorderingCheckBox;
    private javax.swing.JCheckBox resizingCheckBox;
    // End of variables declaration//GEN-END:variables

    public static class FormTableHeader extends FormDesignValueAdapter {
        private FormProperty property;
        private boolean resizingAllowed;
        private boolean reorderingAllowed;
        
        public FormTableHeader(FormProperty property, boolean resizingAllowed, boolean reorderingAllowed) {
            this.property = property;
            this.resizingAllowed = resizingAllowed;
            this.reorderingAllowed = reorderingAllowed;
        }

        public boolean isResizingAllowed() {
            return resizingAllowed;
        }

        public boolean isReorderingAllowed() {
            return reorderingAllowed;
        }
        
        public Object getDesignValue() {
            Object value = null;
            try {
                value = property.getTargetValue();
                if (value instanceof JTableHeader) {
                    JTableHeader header = (JTableHeader)value;
                    header.setResizingAllowed(resizingAllowed);
                    header.setReorderingAllowed(reorderingAllowed);
                }
            } catch (IllegalAccessException iaex) {
                iaex.printStackTrace();
            } catch (InvocationTargetException itex) {
                itex.printStackTrace();
            }
            return value;
        }

        public Object getDesignValue(Object target) {
            // PENDING
            JTableHeader header = null;
            if (target instanceof javax.swing.JTable) {
                header = ((javax.swing.JTable)target).getTableHeader();
                header.setResizingAllowed(resizingAllowed);
                header.setReorderingAllowed(reorderingAllowed);                
            }
            return header;
        }

        public Object copy(FormProperty targetFormProperty) {
            return new FormTableHeader(targetFormProperty, resizingAllowed, reorderingAllowed);
        }
        
    }
    
}
