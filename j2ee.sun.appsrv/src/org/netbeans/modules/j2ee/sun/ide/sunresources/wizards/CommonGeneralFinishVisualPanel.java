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
/*
 * CommonGeneralFinishVisualPanel.java
 *
 * Created on October 10, 2002
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import org.openide.util.NbBundle;
import java.util.ResourceBundle;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;

/** A single panel for a wizard - the GUI portion.
 *
 * @author  shirleyc
 */
public class CommonGeneralFinishVisualPanel extends javax.swing.JPanel implements ChangeListener, WizardConstants {
    
    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
    
    public static final String TYPE_JMS_RESOURCE = "_JMS";  //NOI18N
    public static final String TYPE_MAIL_RESOUCE = "_MAIL";  //NOI18N
    
    protected ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N

    protected final CommonGeneralFinishPanel panel;
    protected FieldGroup[] groups;
    protected int fieldSize;
    protected ResourceConfigHelper helper;
    protected String panelType = TYPE_JMS_RESOURCE;
    protected ArrayList beans = new ArrayList();
    protected boolean createNewResource = false;
    
    // Variables declaration - general attributes
    protected javax.swing.JTextArea descriptionTextArea = null;
    protected javax.swing.JPanel jPanel1[] = null;
    protected javax.swing.JLabel jLabels[] = null;
    protected javax.swing.JComponent jFields[] = null;
    protected Field fields[] = null;
    protected String resourceName;
    protected boolean showGroupTitle = false;
    
    // Variable declaration - choice 
    private javax.swing.JPanel jPanel0;
    private boolean firstTime = true;
    private int y = 0;
    
    /** Create the wizard panel and set up some basic properties. */
    public CommonGeneralFinishVisualPanel(CommonGeneralFinishPanel panel, FieldGroup[] groups, String panelType) {
        this.firstTime = true;
        this.panel = panel;
        this.helper = panel.getHelper();
        this.resourceName = panel.getResourceName();
        setFieldGroups(groups);
        if (groups.length > 1)
            this.showGroupTitle = true;
        
        setLayout(new java.awt.GridBagLayout());
        initDescriptionComponent();
        
            
        setPanelType(panelType);
        initComponents();

        refreshFields();
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(CommonGeneralFinishVisualPanel.class, "TITLE_" + groups[0].getName())); //NOI18N
        getAccessibleContext().setAccessibleName(bundle.getString("TITLE_" + groups[0].getName())); 
        getAccessibleContext().setAccessibleDescription(bundle.getString("mail-resource_Description"));
        this.firstTime = false;
    }
    
    public void setFieldGroups(FieldGroup[] groups) {
        this.groups = groups;
        fieldSize = 0;
        for (int i = 0; i < groups.length; i++) {
            fieldSize += groups[i].sizeField();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        int gap = 0;
        int itemHeight = 28;
        if (fieldSize < 6) {
            itemHeight = 42;
            gap = 7;
        }
        int i = 0;
        jPanel1 = new JPanel[groups.length];
        fields = new Field[fieldSize];
        jLabels = new JLabel[fieldSize];
        jFields = new JComponent[fieldSize];   
      
      for (int j = 0; j < groups.length; j++) {
        if (showGroupTitle) {
            JLabel label = new JLabel();
            label.setText(bundle.getString("LBL_GROUP_" + groups[j].getName()));  //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_LBL_GROUP_" + groups[j].getName() + "A11yDesc"));  //NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = this.y;  this.y++;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.ipadx = 0;
            gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
            add(label, gridBagConstraints);
        }  
        
        Field[] flds = groups[j].getField();
        int size = flds.length;
        jPanel1[j] = new javax.swing.JPanel();
        jPanel1[j].setLayout(new java.awt.GridBagLayout());
        
        jPanel1[j].setMaximumSize(new java.awt.Dimension(480, itemHeight * size));
        jPanel1[j].setMinimumSize(new java.awt.Dimension(480, itemHeight * size));
        jPanel1[j].setPreferredSize(new java.awt.Dimension(480, itemHeight * size));
        

        for (int k = 0; k < size; k++) {
            fields[i] = flds[k];
            JLabel jLabel = new JLabel();
            jLabel.setText(bundle.getString("LBL_" + fields[i].getName()));  //NOI18N
            jLabel.setDisplayedMnemonic(bundle.getString("LBL_" + fields[i].getName() + "_Mnemonic").charAt(0)); //NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = k;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.ipadx = 0;
            gridBagConstraints.insets = new java.awt.Insets(2, 12, gap, 0);
            jLabels[i] = jLabel;
            jPanel1[j].add(jLabels[i], gridBagConstraints);
            
            if (FieldHelper.isList(fields[i])) {
                JComboBox jComboBox = new JComboBox();
                jLabel.setLabelFor(jComboBox);
                jComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ToolTip_" + fields[i].getName()));  
                String tags[] = FieldHelper.getTags(fields[i]);
                for (int h = 0; h < tags.length; h++) {
                    jComboBox.addItem(tags[h]);
                }
                final int index = i;
                jComboBox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jComboBoxActionPerformed(evt, index);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy =  k;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(2, 20, gap, 0);
                gridBagConstraints.weightx = 1.0;
                jFields[i] = jComboBox;
                jPanel1[j].add(jComboBox, gridBagConstraints);
            }  else {
                JTextField jTextField = new JTextField();
                jTextField.setText("jTextField1"); //NOI18N
                jLabel.setLabelFor(jTextField);
                jTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ToolTip_" + fields[i].getName())); 
                jTextField.setToolTipText(bundle.getString("ToolTip_" + fields[i].getName()));  //NOI18N
                jTextField.setMinimumSize(new java.awt.Dimension(340, 21));
                jTextField.setPreferredSize(new java.awt.Dimension(340, 21));
                final int index = i;
                /*
                jTextField.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jTextField1ActionPerformed(evt, index);
                    }
                });
                 */
                jTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        jTextField1KeyReleased(evt, index);
                    }
                });
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy =  k;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.insets = new java.awt.Insets(2, 20, gap, 0);
                jFields[i] = jTextField;
                jPanel1[j].add(jTextField, gridBagConstraints);
            }
            i++;
        }
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = this.y;   this.y++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 11);
        jPanel1[j].setBorder(new javax.swing.border.EtchedBorder());
        add(jPanel1[j], gridBagConstraints);
      }
    }    
    
    private void initDescriptionComponent() {
        java.awt.GridBagConstraints gridBagConstraints;
        
        descriptionTextArea = new javax.swing.JTextArea();
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setFont(javax.swing.UIManager.getFont("Label.font")); //NOI18N
        descriptionTextArea.setText(bundle.getString(this.resourceName + "_Description"));  //NOI18N
        descriptionTextArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));  //NOI18N
        descriptionTextArea.setRequestFocusEnabled(false);
        descriptionTextArea.setEnabled(false);
        descriptionTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = this.y; this.y++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 20, 11);
        add(descriptionTextArea, gridBagConstraints);        
    }
    

    
    protected String getPanelType() {
        return panelType;
    }
    
    protected void setPanelType(String type) {
        this.panelType = type; 
    }
    

    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
    }

    public boolean createNew() {
        return createNewResource;
    }
    
    public void refreshFields() {
        ResourceConfigData data = this.helper.getData();
        for (int i = 0; i < jFields.length; i++) {
            String item;
            if (FieldHelper.isList(fields[i])) {
                item = (String)((JComboBox)jFields[i]).getSelectedItem();
            } else {
                item = (String)((JTextField)jFields[i]).getText();
            }
            String fieldName = fields[i].getName();
            Object value = data.get(fieldName);
            if (value == null) {
                value = FieldHelper.getDefaultValue(fields[i]);
                data.set(fieldName, value);
            }
            String val = (String)value;
            if (!item.equals(val)) {
                if (FieldHelper.isList(fields[i])) {
                    ((JComboBox)jFields[i]).setSelectedItem(val);
                } else {
                    ((JTextField)jFields[i]).setText(val);
                }
            }
         }
    }
    
    private void jComboBoxActionPerformed(java.awt.event.ActionEvent evt, int index) {
        ResourceConfigData data = this.helper.getData();
        String item = (String)((JComboBox)jFields[index]).getSelectedItem();
        String fieldName = fields[index].getName();
        String val = data.getString(fieldName);
        if (!item.equals(val))
            data.setString(fieldName, item);
    }
    
    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt, int index) {
        ResourceConfigData data = this.helper.getData();
        String item = (String)((JTextField)jFields[index]).getText();
        String fieldName = fields[index].getName();
        String val = data.getString(fieldName);
        if (FieldHelper.isInt(fields[index])) {
            try {
                Integer.parseInt(item);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, bundle.getString("MSG_NotNumber"));  //NOI18N
                if (val == null || val.length() == 0)
                    val = FieldHelper.getDefaultValue(fields[index]);
                ((JTextField)jFields[index]).setText(val);
                return;
            }
        }
        String jLabel = (String)this.jLabels[index].getText();
        //if (!item.equals(val) && !jLabel.equals(bundle.getString("LBL_" + __JndiName)) ) {  // don't save jndi name here or it will break 'duplicate jndi name validation' in edit mode
        if (!item.equals(val) && jLabel.equals(bundle.getString("LBL_" + __JndiName)) ) {  
//            item = item + data.getTargetFile();
            data.setString(fieldName, item);
        }
        if (!item.equals(val)){
            data.setString(fieldName, item);
            //panel.fireChangeEvent();
        }
        panel.fireChange(evt.getSource());
    }
    
    public CommonGeneralFinishVisualPanel setFirstTime(boolean first) {
        this.firstTime = first;
        return this;
    }
    
    public void initData() {
        refreshFields();
    }
    
     public void setHelper(ResourceConfigHelper helper){
        this.helper = helper;
        this.helper.getData().setString("jndi-name", helper.getData().getTargetFile()); //NOI18N
        refreshFields();
    }
}
