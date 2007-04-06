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
package org.netbeans.modules.swingapp;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.editors.IconEditor;
import org.netbeans.modules.swingapp.actions.AcceleratorKeyListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * This is the actual dialog used for editing actions. It lets the user set
 * almost any attribute on an action. The action will not be saved from this
 * dialog, however. When the user presses the Okay button (defined in the NB supplied
 * property editor dialog) the ActionEditor class will do the actual saving.
 * @author  joshua.marinacci@sun.com
 */
public class ActionPropertyEditorPanel extends javax.swing.JPanel {
    
    public static final String LARGE_ICON_KEY = "SwingLargeIconKey"; // NOI18N
    
    private Map<ProxyAction.Scope, List<ProxyAction>> parsedActions;
    private boolean newActionCreated = false;
    private boolean actionPropertiesUpdated = false;
    private boolean returnsTask = false;
    private ProxyAction.Scope newActionScope = ProxyAction.Scope.Application;
    private boolean viewSource = false;
    private String newMethodName = "";
    private boolean isChanging = false;
    private Map<ProxyAction.Scope, String> scopeClasses = new HashMap<ProxyAction.Scope, String>();
    private FileObject sourceFile;
    private String smallIconName = null;
    private String largeIconName = null;
    private ProxyAction newAction = null;
    private ProxyAction globalAction = null;
    private boolean globalMode = false;
    
    
    
    /** Creates new form ActionPropertyEditorPanel */
    public ActionPropertyEditorPanel(final FormProperty property, FileObject sourceFile) {
        initComponents();
        this.sourceFile = sourceFile;
        if(property == null) {
            globalMode = true;
        }
        parsedActions = new HashMap<ProxyAction.Scope, List<ProxyAction>>();
        Object[] vals = new Object[] {
            ProxyAction.Scope.Application,
            ProxyAction.Scope.Form };
        DefaultComboBoxModel model = new DefaultComboBoxModel(vals);
        scopeCombo.setModel(model);
        scopeCombo.setSelectedItem(ProxyAction.Scope.Form);
        
        vals = new Object[] {
            ProxyAction.BlockingType.NONE,
            ProxyAction.BlockingType.ACTION,
            ProxyAction.BlockingType.COMPONENT,
            ProxyAction.BlockingType.WINDOW,
            ProxyAction.BlockingType.APPLICATION };
        blockingType.setModel(new DefaultComboBoxModel(vals));
        blockingType.setSelectedItem(ProxyAction.BlockingType.NONE);
        
        newActionButton.addActionListener(new ShowCreateNewActionDialog());
        DocumentListener dirtyListener = new DirtyDocumentListener();
        textField.getDocument().addDocumentListener(dirtyListener);
        tooltipField.getDocument().addDocumentListener(dirtyListener);
        acceleratorText.getDocument().addDocumentListener(dirtyListener);
        
        this.addPropertyChangeListener("action", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getNewValue() != null) {
                    ProxyAction act = (ProxyAction)evt.getNewValue();
                    if(!isNewActionCreated()) {
                        updateFieldsFromAction(act);
                    }
                }
            }
            
        });
        
        ((IconButton)iconButtonLarge).setIconText("large");
        ((IconButton)iconButtonSmall).setIconText("small");
        iconButtonSmall.addActionListener(new IconButtonListener(property,iconButtonSmall, Action.SMALL_ICON));
        iconButtonLarge.addActionListener(new IconButtonListener(property,iconButtonLarge, LARGE_ICON_KEY));
        
        scopeCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                ProxyAction.Scope scope = (ProxyAction.Scope)value;
                String className = scopeClasses.get(scope);
                if(className != null) {
                    ((JLabel)comp).setText(className);
                } else {
                    ((JLabel)comp).setText(scope.toString());
                }
                return comp;
            }
        });
        
        actionsCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                ProxyAction act = (ProxyAction)value;
                ((JLabel)comp).setText(act != null ? act.getId() : "<none>");
                return comp;
            }
        });
        
        setupAccelField();
    }
    
    
    private void updateFieldsFromAction(final ProxyAction act) {
        if(act == null) {
            clearFieldsForNull();
        } else {
            textField.setEnabled(true);
            tooltipField.setEnabled(true);
            acceleratorText.setEnabled(true);
            iconButtonLarge.setEnabled(true);
            iconButtonSmall.setEnabled(true);
            selectedTextfield.setEnabled(true);
            enabledTextfield.setEnabled(true);
            viewSourceButton.setEnabled(true);
        }
        
        setFromActionProperty(textField,act,Action.NAME);
        setFromActionProperty(tooltipField,act,Action.SHORT_DESCRIPTION);
        setFromActionProperty(acceleratorText,act,Action.ACCELERATOR_KEY);
        
        
        StringBuffer sig = new StringBuffer();
        sig.append("@Action"); // NOI18N
        if(act.isTaskEnabled()) {
            sig.append(" Task"); // NOI18N
        } else {
            sig.append(" void"); // NOI18N
        }
        sig.append(" " + act.getId()); // NOI18N
        sig.append("()"); // NOI18N
        
        if(act == null) {
            methodNameField.setText("< none >");
            methodSigLabel.setText(""); // NOI18N
        } else {
            methodNameField.setText(sig.toString());
            methodSigLabel.setText(act.getClassname()+"."+act.getId()+"()"); // NOI18N
        }

        smallIconName = (String) act.getValue(Action.SMALL_ICON +".IconName"); // NOTI18N
        largeIconName = (String) act.getValue(LARGE_ICON_KEY +".IconName"); // NOTI18N
        if(act.getValue(Action.SMALL_ICON) != null) {
            iconButtonSmall.setIcon((Icon)act.getValue(Action.SMALL_ICON));
            iconButtonSmall.setText(null);
        } else {
            iconButtonSmall.setIcon(null);
            iconButtonSmall.setText("..."); // NOI18N
        }
        if(act.getValue(LARGE_ICON_KEY) != null) {
            iconButtonLarge.setIcon((Icon)act.getValue(LARGE_ICON_KEY));
            iconButtonLarge.setText(null);
        } else {
            iconButtonLarge.setIcon(null);
            iconButtonLarge.setText("..."); // NOI18N
        }
        
        blockingType.setEnabled(act.isTaskEnabled());
        blockingDialogText.setEnabled(act.isTaskEnabled());
        blockingDialogTitle.setEnabled(act.isTaskEnabled());
        if(act.isTaskEnabled()) {
            if(act.getBlockingType()!= null) {
                blockingType.setSelectedItem(act.getBlockingType());
            }
        } else {
            blockingType.setSelectedItem(NbBundle.getMessage(ActionPropertyEditorPanel.class,"BlockingTypeNone")); // NOI18N
        }
        
        setFromActionProperty(blockingDialogText,act,"BlockingDialog.message"); //NOI18N
        setFromActionProperty(blockingDialogTitle,act,"BlockingDialog.title"); //NOI18N
        if(act.getSelectedName()!=null) {
            selectedTextfield.setText(act.getSelectedName());
        } else {
            selectedTextfield.setText(null);
        }
        if(act.getEnabledName()!=null) {
            enabledTextfield.setText(act.getEnabledName());
        } else {
            enabledTextfield.setText(null);
        }
    }
    
    private void setFromActionProperty(JTextField textField, ProxyAction act, String key) {
        if(act.getValue(key)== null) {
            textField.setText(""); // NOI18N
        } else {
            textField.setText(""+act.getValue(key)); // NOI18N
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        componentNameLabel = new javax.swing.JLabel();
        methodSigLabel = new javax.swing.JLabel();
        methodNameField = new javax.swing.JLabel();
        scopeCombo = new javax.swing.JComboBox();
        actionsCombo = new javax.swing.JComboBox();
        newActionButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        tooltipField = new javax.swing.JTextField();
        acceleratorText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        iconButtonSmall = new IconButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        iconButtonLarge = new IconButton();
        clearAccelButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        enabledTextfield = new javax.swing.JTextField();
        selectedTextfield = new javax.swing.JTextField();
        blockingType = new javax.swing.JComboBox();
        blockingDialogTitle = new javax.swing.JTextField();
        blockingDialogText = new javax.swing.JTextField();
        viewSourceButton = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();

        componentNameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        componentNameLabel.setText("fooButton");
        methodSigLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        methodSigLabel.setText("main.MainPanel.doStuff()");
        methodNameField.setText("methodName");
        methodNameField.setEnabled(false);

        scopeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Application Wide", "MyTestForm" }));
        scopeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scopeComboActionPerformed(evt);
            }
        });

        actionsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "save", "open", "new", "exit", "cut" }));
        actionsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionsComboActionPerformed(evt);
            }
        });

        newActionButton.setText("New Action");

        jPanel2.setOpaque(false);
        jLabel2.setText("Text:");

        textField.setText("Save");

        tooltipField.setText("Save the current document");

        acceleratorText.setText("command+S");

        jLabel5.setText("Icon:");

        iconButtonSmall.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonSmall.setBorder(null);
        iconButtonSmall.setContentAreaFilled(false);

        jLabel7.setText("Accelerator:");

        jLabel4.setText("Tool Tip:");

        iconButtonLarge.setBackground(new java.awt.Color(255, 255, 255));
        iconButtonLarge.setBorder(null);
        iconButtonLarge.setOpaque(false);

        clearAccelButton.setText("Clear");
        clearAccelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAccelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, tooltipField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, textField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(acceleratorText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(clearAccelButton)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(textField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(tooltipField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(clearAccelButton)
                    .add(acceleratorText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(iconButtonSmall, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(iconButtonLarge, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jTabbedPane1.addTab("Basic", jPanel2);

        jPanel3.setOpaque(false);
        jLabel6.setText("Blocking Dialog Text:");

        jLabel3.setText("Blocking Dialog Title:");

        jLabel1.setLabelFor(blockingType);
        jLabel1.setText("Blocking Type:");

        jLabel11.setText("Selected Property:");

        jLabel8.setText("Enabled Property:");

        blockingType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Action", "Component", "Window", "Application" }));
        blockingType.setEnabled(false);
        blockingType.setOpaque(false);

        blockingDialogTitle.setText("jTextField1");
        blockingDialogTitle.setEnabled(false);

        blockingDialogText.setText("jTextField2");
        blockingDialogText.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel8)
                        .add(36, 36, 36)
                        .add(enabledTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel11)
                        .add(33, 33, 33)
                        .add(selectedTextfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel6)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(blockingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(blockingDialogText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(enabledTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectedTextfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .add(26, 26, 26)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(blockingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(blockingDialogTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(blockingDialogText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jTabbedPane1.addTab("Advanced", jPanel3);

        viewSourceButton.setText("View Source");
        viewSourceButton.setOpaque(false);
        viewSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSourceButtonActionPerformed(evt);
            }
        });

        jLabel16.setText("Invoke:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel16)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(newActionButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(viewSourceButton))
                            .add(layout.createSequentialGroup()
                                .add(scopeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(actionsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(43, 43, 43))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel16)
                    .add(scopeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(actionsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(newActionButton)
                    .add(viewSourceButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearAccelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAccelButtonActionPerformed
        // clear the accelerator because you can't actually use backspace to clear it
        acceleratorText.setText(""); // NOI18N
    }//GEN-LAST:event_clearAccelButtonActionPerformed
    
    
    
    private void viewSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSourceButtonActionPerformed
        viewSource = true;
        doViewSource();
    }//GEN-LAST:event_viewSourceButtonActionPerformed
    
    private void actionsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionsComboActionPerformed
        if(!isChanging) {
            firePropertyChange("action",null,getSelectedAction()); // NOI18N
        }
    }//GEN-LAST:event_actionsComboActionPerformed
    
    private void scopeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scopeComboActionPerformed
        List<ProxyAction> selectedActions = parsedActions.get(scopeCombo.getSelectedItem());
        if(selectedActions != null) {
            actionsCombo.setModel(new DefaultComboBoxModel(selectedActions.toArray()));
        }
        if(!isChanging) {
            firePropertyChange("action",null,getSelectedAction()); // NOI18N
        }
    }//GEN-LAST:event_scopeComboActionPerformed
    
    void setParsedActions(Map<ProxyAction.Scope, List<ProxyAction>> actionMap) {
        this.parsedActions = actionMap;
        scopeCombo.setSelectedIndex(0);
    }
    
    // returns the selected action with the properties filled in from the
    // text fields on the form
    ProxyAction getUpdatedAction() {
        ProxyAction act = getSelectedAction();
        act.putValue(Action.NAME,textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION,tooltipField.getText());
        if(acceleratorText.getText() != null && !acceleratorText.getText().equals("")) {
            KeyStroke key = KeyStroke.getKeyStroke(acceleratorText.getText());
            act.putValue(Action.ACCELERATOR_KEY,key);
        } else {
            act.putValue(Action.ACCELERATOR_KEY,null);
        }
        if(act.isTaskEnabled()) {
            act.setBlockingType(getSelectedBlockingType());
            setFromTextField(blockingDialogText,act,"BlockingDialog.message"); // NOI18N
            setFromTextField(blockingDialogTitle,act,"BlockingDialog.title"); // NOI18N
        }
        if(selectedTextfield.getText() != null) {
            act.setSelectedName(selectedTextfield.getText());
        }
        if(enabledTextfield.getText() != null) {
            act.setEnabledName(enabledTextfield.getText());
        }
        act.putValue(Action.SMALL_ICON+".IconName",  smallIconName); // NOTI18N
        act.putValue(LARGE_ICON_KEY+".IconName",  largeIconName); // NOTI18N
        return act;
    }
    
    private void setFromTextField(JTextField textField, ProxyAction act, String key) {
        if(textField.getText() != null && !textField.getText().trim().equals("")) {
            act.putValue(key,textField.getText());
        }
    }
    
    ProxyAction getNewAction() {
        ProxyAction act = newAction;
        act.setId(newMethodName);//methodNameField.getText());
        act.putValue(Action.NAME,textField.getText());
        act.putValue(Action.SHORT_DESCRIPTION,tooltipField.getText());
        act.setTaskEnabled(returnsTask);
        
        act.putValue(Action.SMALL_ICON+".IconName",this.smallIconName); // NOI18N
        act.putValue(LARGE_ICON_KEY+".IconName",this.largeIconName); // NOI18N
        act.putValue(Action.SMALL_ICON,iconButtonSmall.getIcon());
        act.putValue(LARGE_ICON_KEY,iconButtonLarge.getIcon());
        
        if(act.isTaskEnabled()) {
            act.setBlockingType(getSelectedBlockingType());
            setFromTextField(blockingDialogText,   act, "BlockingDialog.message"); // NOI18N
            setFromTextField(blockingDialogTitle,  act, "BlockingDialog.title"); // NOI18N
        }
        
        if(acceleratorText.getText() != null && !acceleratorText.getText().equals("")) { // NOI18N
            act.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(acceleratorText.getText()));
        }
        if(selectedTextfield.getText() != null) {
            act.setSelectedName(selectedTextfield.getText());
        }
        if(enabledTextfield.getText() != null) {
            act.setEnabledName(enabledTextfield.getText());
        }
        return act;
    }
    
    private ProxyAction.BlockingType getSelectedBlockingType() {
        return (ProxyAction.BlockingType)blockingType.getSelectedItem();
    }
    
    ProxyAction.Scope getSelectedScope() {
        return newActionScope;
    }
    
    ProxyAction getSelectedAction() {
        if(globalMode) {
            return globalAction;
        }
        if(isNewActionCreated()) {
            return newAction;
        }
        Object action = actionsCombo.getSelectedItem();
        if(action instanceof ProxyAction) {
            return (ProxyAction)action;
        }
        return null;
    }
    
    public void updatePanel(Map<ProxyAction.Scope, List<ProxyAction>> actionMap, ProxyAction selectedAction,
            Map<ProxyAction.Scope, String> scopeClasses, String componentName, FileObject sourceFile) {
        isChanging = true;
        setParsedActions(actionMap);
        setSelectedAction(selectedAction);
        this.scopeClasses = scopeClasses;
        componentNameLabel.setText(componentName);
        this.sourceFile = sourceFile;
        isChanging = false;
    }
    
    private void setSelectedAction(ProxyAction act) {
        if(act != null) {
            if(globalMode) {
                globalAction = act;
            }
            setNewActionCreated(false);
            actionsCombo.setEnabled(true);
            scopeCombo.setEnabled(true);
            scopeCombo.setSelectedItem(act.getScope());
            //set the selection action by finding the right match
            for(int i=0; i<actionsCombo.getModel().getSize(); i++) {
                Object o = actionsCombo.getModel().getElementAt(i);
                if (o != null) {
                    ProxyAction act2 = (ProxyAction)o;
                    if(act2.getId().equals(act.getId())) {
                        actionsCombo.setSelectedItem(act2);
                        break;
                    }
                }
            }
            updateFieldsFromAction(act);
        } else {
            clearFields();
            clearFieldsForNull();
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField acceleratorText;
    private javax.swing.JComboBox actionsCombo;
    private javax.swing.JTextField blockingDialogText;
    private javax.swing.JTextField blockingDialogTitle;
    private javax.swing.JComboBox blockingType;
    private javax.swing.JButton clearAccelButton;
    private javax.swing.JLabel componentNameLabel;
    private javax.swing.JTextField enabledTextfield;
    private javax.swing.JButton iconButtonLarge;
    private javax.swing.JButton iconButtonSmall;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel methodNameField;
    private javax.swing.JLabel methodSigLabel;
    private javax.swing.JButton newActionButton;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JTextField selectedTextfield;
    private javax.swing.JTextField textField;
    private javax.swing.JTextField tooltipField;
    private javax.swing.JButton viewSourceButton;
    // End of variables declaration//GEN-END:variables
    
    
    private void clearFields() {
        textField.setText(""); // NOI18N
        acceleratorText.setText(""); // NOI18N
        tooltipField.setText(""); // NOI18N
        methodNameField.setText(""); // NOI18N
        newMethodName = ""; // NOI18N
        iconButtonSmall.setIcon(null);
        iconButtonLarge.setIcon(null);
    }
    
    private void clearFieldsForNull() {
        textField.setEnabled(false);
        tooltipField.setEnabled(false);
        acceleratorText.setEnabled(false);
        iconButtonLarge.setEnabled(false);
        iconButtonSmall.setEnabled(false);
        selectedTextfield.setEnabled(false);
        enabledTextfield.setEnabled(false);
        viewSourceButton.setEnabled(false);
    }
    
    private void clearFieldsForNewAction() {
        textField.setEnabled(true);
        tooltipField.setEnabled(true);
        acceleratorText.setEnabled(true);
        iconButtonLarge.setEnabled(true);
        iconButtonSmall.setEnabled(true);
        selectedTextfield.setEnabled(true);
        enabledTextfield.setEnabled(true);
        actionsCombo.setEnabled(false);
        scopeCombo.setEnabled(false);
        viewSourceButton.setEnabled(false);
        textField.setText(""); // NOI18N
        acceleratorText.setText(""); // NOI18N
        tooltipField.setText(""); // NOI18N
        methodNameField.setText(""); // NOI18N
        newMethodName = ""; // NOI18N
        iconButtonSmall.setIcon(null);
        iconButtonLarge.setIcon(null);
        // josh: is this next line correct?
        blockingType.setEnabled(false);
    }
    
    public void resetFields() {
        this.setNewActionCreated(false);
        this.viewSource = false;
        this.actionPropertiesUpdated = false;
        this.viewSourceButton.setEnabled(true);
    }
    
    public void setNewActionCreated(boolean newActionCreated) {
        this.newActionCreated = newActionCreated;
        if(newActionCreated) {
            clearFieldsForNewAction();
        } else {
            scopeCombo.setEnabled(true);
            blockingType.setEnabled(true);
            actionsCombo.setEnabled(true);
        }
    }
        
    boolean canCreateNewAction() {
        if(newMethodName == null) {
            return false;
        }
        if(newMethodName.trim().equals("")) {
            return false;
        }
        if(newMethodName.contains(" ")) {
            return false;
        }
        return true;
    }
    
    String getNewMethodName() {
        return newMethodName;
    }
        
    private class DirtyDocumentListener implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        public void insertUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
        
        public void removeUpdate(DocumentEvent e) {
            actionPropertiesUpdated = true;
        }
    }
    
    private class IconButtonListener implements ActionListener {
        
        private FormProperty property;
        private JButton iconButton;
        private String iconKey;
        
        public IconButtonListener(FormProperty property, JButton iconButton, String iconKey) {
            super();
            this.property = property;
            this.iconButton = iconButton;
            this.iconKey = iconKey;
        }
        
        public void actionPerformed(ActionEvent e) {
            ProxyAction action = getSelectedAction();
            IconEditor iconEditor = new IconEditor();
            iconEditor.setSourceFile(sourceFile);
            
            if(Action.SMALL_ICON.equals(iconKey)){
                if (smallIconName != null) {
                    iconEditor.setAsText(smallIconName);
                }
            } else {
                if (largeIconName != null) {
                    iconEditor.setAsText(largeIconName);
                }
            }
            DialogDescriptor dd = new DialogDescriptor(iconEditor.getCustomEditor(), NbBundle.getMessage(ActionPropertyEditorPanel.class, "CTL_SelectIcon_Title"));
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                IconEditor.NbImageIcon nbIcon = (IconEditor.NbImageIcon) iconEditor.getValue();
                Icon icon = nbIcon != null ? nbIcon.getIcon() : null;
                iconButton.setIcon(icon);
                iconButton.setText(icon == null ? "..." : null);
                action.putValue(iconKey, icon);
                String iconName = nbIcon != null ? nbIcon.getName() : null;
                if(Action.SMALL_ICON.equals(iconKey)) {
                    smallIconName = iconName;
                } else {
                    largeIconName = iconName;
                }
                action.putValue(iconKey+".IconName", iconName); // NOI18N
            }
        }
    }

    private void doViewSource() {
        ProxyAction act = getSelectedAction();
        ActionManager am = ActionManager.getActionManager(this.sourceFile);
        am.jumpToActionSource(act);
    }
    
    private void setupAccelField() {
        // turn off foucs keys
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        acceleratorText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
        acceleratorText.addKeyListener(new AcceleratorKeyListener());
    }
    
    private class ShowCreateNewActionDialog implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final CreateNewActionPanel panel = new CreateNewActionPanel(sourceFile, scopeClasses);
            final DialogDescriptor dd = new DialogDescriptor(panel,"Create New Action");
            dd.setOptions(new String[] {"Cancel","Create"});
            dd.setValue("Create");
            dd.setModal(true);
            dd.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);
            dd.setClosingOptions(new String[0]);
            dd.setLeaf(false);
            dd.setValue("Create");
            final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dd.setButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(dd.getValue().equals("Cancel") || panel.validateFields()) {
                        dialog.setVisible(false);
                    } else {
                    }
                }
            });
            panel.setDialog(dialog);
            dialog.pack();
            dialog.setVisible(true);
            
            if(panel.isInputIsValid()) {
                clearFields();

                newAction = new ProxyAction(panel.getSelectedClassName(),
                                            panel.getMethodText());
                StringBuffer sig = new StringBuffer();
                sig.append("@Action"); // NOI18N
                if(panel.isAsynchronous()) {
                    returnsTask = true;
                    newAction.setTaskEnabled(true);
                    sig.append(" Task"); // NOI18N
                } else {
                    returnsTask = false;
                    newAction.setTaskEnabled(false);
                    sig.append(" void"); // NOI18N
                }
                sig.append(" " + panel.getMethodText()); // NOI18N
                sig.append("()"); // NOI18N
                methodNameField.setText(sig.toString());
                methodSigLabel.setText(panel.getMethodText()+"()"); // NOI18N
                newActionScope = panel.getSelectedScope();
                setNewActionCreated(true);
                newMethodName = panel.getMethodText();
            }
        }
    }
    
    public boolean isNewActionCreated() {
        return newActionCreated;
    }
    
    boolean isViewSource() {
        return this.viewSource;
    }
    
    boolean isActionPropertiesUpdated() {
        return this.actionPropertiesUpdated;
    }
    
}

