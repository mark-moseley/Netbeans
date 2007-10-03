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
package org.netbeans.modules.visualweb.propertyeditors.css;

import org.netbeans.modules.visualweb.propertyeditors.css.model.CssStyleData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.MarginPaddingModel;
import org.netbeans.modules.visualweb.propertyeditors.css.model.PropertyWithUnitData;
import org.netbeans.modules.visualweb.propertyeditors.css.model.Utils;
import java.beans.PropertyChangeSupport;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Margin Width field wiith text field and unit combo box
 * @author  Winston Prakash
 */
public class MarginWidthField extends javax.swing.JPanel {
    PropertyWithUnitData borderMarginData = new PropertyWithUnitData();
    MarginPaddingModel marginPaddingModel = new MarginPaddingModel();
    
    /** Creates new form borderMarginField */
    public MarginWidthField() {
        initComponents();
        borderMarginCombo.setModel(marginPaddingModel.getMarginList());
        borderMarginUnitCombo.setModel(marginPaddingModel.getMarginUnitList());
        
        // Add editor listeners to the margin width combobox
        final JTextField borderMarginComboEditor = (JTextField) borderMarginCombo.getEditor().getEditorComponent();
        borderMarginComboEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        borderMarginUnitCombo.setEnabled(Utils.isInteger(borderMarginComboEditor.getText()));
                    }
                });
            }
        });
    }
    
    private PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addCssPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removeCssPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    public void setMarginString(String marginStr){
        if((marginStr != null) && !marginStr.equals("")){
            if(Utils.isInteger(marginStr)){
                setMarginValue(marginStr);
            }else{
                String unit = getUnit(marginStr);
                setMarginUnit(unit);
                setMarginValue(marginStr.replaceAll(unit,"").trim());
            }
        }else{
            setMarginValue(null);
            setMarginUnit(null);
        }
    }
    
    public String getMarginString(){
        return borderMarginData.toString();
    }
    
    private String getUnit(String positionStr){
        DefaultComboBoxModel unitList = marginPaddingModel.getMarginUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(positionStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }
    
    public void setMarginValue(String value){
        if((value == null) || value.equals("")){
            borderMarginCombo.setSelectedIndex(0);
        }else{
            borderMarginCombo.setSelectedItem(value);
            borderMarginData.setValue(value);
        }
    }
    
    public void setMarginUnit(String value){
        if((value == null) || value.equals("")){
            borderMarginUnitCombo.setSelectedIndex(marginPaddingModel.getMarginUnitList().getIndexOf("px")); //NOI18N
        }else{
            if(marginPaddingModel.getMarginUnitList().getIndexOf(value) != -1){
                borderMarginUnitCombo.setSelectedIndex(marginPaddingModel.getMarginUnitList().getIndexOf(value));
            }else{
                borderMarginUnitCombo.setSelectedIndex(marginPaddingModel.getMarginUnitList().getIndexOf("px")); //NOI18N
            }
            borderMarginData.setUnit(value);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        borderMarginCombo = new javax.swing.JComboBox();
        borderMarginUnitCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(3, 0));

        borderMarginCombo.setEditable(true);
        borderMarginCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderMarginComboItemStateChanged(evt);
            }
        });
        borderMarginCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borderMarginComboActionPerformed(evt);
            }
        });
        borderMarginCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderMarginComboFocusLost(evt);
            }
        });

        add(borderMarginCombo, java.awt.BorderLayout.CENTER);

        borderMarginUnitCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                borderMarginUnitComboItemStateChanged(evt);
            }
        });
        borderMarginUnitCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                borderMarginUnitComboFocusLost(evt);
            }
        });

        add(borderMarginUnitCombo, java.awt.BorderLayout.EAST);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void borderMarginUnitComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderMarginUnitComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setborderMargin();
        }
    }//GEN-LAST:event_borderMarginUnitComboItemStateChanged
    
    private void borderMarginUnitComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderMarginUnitComboFocusLost
        setborderMargin();
    }//GEN-LAST:event_borderMarginUnitComboFocusLost
    // For accessibility
    public void setAccessibleName(String comboName, String unitName){
        borderMarginCombo.getAccessibleContext().setAccessibleName(comboName);
        borderMarginUnitCombo.getAccessibleContext().setAccessibleName(unitName);
    }
    
    // For accessibility
    public void setAccessibleDescription(String comboDesc, String unitDesc){
        borderMarginCombo.getAccessibleContext().setAccessibleDescription(comboDesc);
        borderMarginUnitCombo.getAccessibleContext().setAccessibleDescription(unitDesc);
    }
    
    private void borderMarginComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_borderMarginComboItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED) {
            setborderMargin();
        }
    }//GEN-LAST:event_borderMarginComboItemStateChanged
    
    private void borderMarginComboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_borderMarginComboFocusLost
        setborderMargin();
    }//GEN-LAST:event_borderMarginComboFocusLost
    
    private void borderMarginComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borderMarginComboActionPerformed
        setborderMargin();
    }//GEN-LAST:event_borderMarginComboActionPerformed
    
    private void setborderMargin(){
        String oldValue = borderMarginData.toString();
        borderMarginData.setUnit(borderMarginUnitCombo.getSelectedItem().toString());
        borderMarginData.setValue( borderMarginCombo.getSelectedItem().toString());
        propertyChangeSupport.firePropertyChange("margin-width", oldValue, borderMarginData.toString()); //NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox borderMarginCombo;
    private javax.swing.JComboBox borderMarginUnitCombo;
    // End of variables declaration//GEN-END:variables
    
}
