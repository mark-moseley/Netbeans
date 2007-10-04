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

package org.netbeans.modules.web.wizards;


import java.io.IOException;
import java.io.InputStream;
import javax.swing.DefaultCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.ProjectUtils;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;

import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.web.core.Util;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

 /**
 * @author  mk115033
 */
public class TagHandlerPanelGUI extends javax.swing.JPanel implements ListSelectionListener {
    
    private TagInfoPanel panel;
    private TemplateWizard wiz;
    private Project proj;
    private SourceGroup[] folders;
    private String target;
    private FileObject tldFo;
    private java.util.Set tagValues;
    
    /** Creates new form TagHandlerPanelGUI */
    public TagHandlerPanelGUI(TemplateWizard wiz, final TagInfoPanel panel, Project proj, SourceGroup[] folders) {
        initComponents();
        this.wiz=wiz;
        this.panel=panel;
        this.proj=proj;
        this.folders=folders;
        attrTable.setModel(new AttrTableModel(
            new String[]{"attrName","attrType","attrRequired","attrRtexprvalue"}));//NOI18N
        attrTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attrTable.setIntercellSpacing(new java.awt.Dimension(6, 6));
        //DefaultCellEditor dce = new DefaultCellEditor(new javax.swing.JCheckBox());
        //DefaultCellEditor dce1 =  new DefaultCellEditor(new javax.swing.JComboBox());
        //attrTable.getColumnModel().getColumn(1).setCellEditor(dce1);
        //attrTable.getColumnModel().getColumn(2).setCellEditor(dce);
	//attrTable.getColumnModel().getColumn(3).setCellEditor(dce);
        attrTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 200));
        setName( org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class,"LBL_configure_TLD"));
        attrTable.getSelectionModel().addListSelectionListener(this);
        nameTextField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void removeUpdate(javax.swing.event.DocumentEvent evt) {
                panel.fireChangeEvent();
            }
            public void insertUpdate(javax.swing.event.DocumentEvent evt) {
                panel.fireChangeEvent();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                panel.fireChangeEvent();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        tldTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        tldFileLabel = new javax.swing.JLabel();
        tagNameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        tagClassLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        emptyButton = new javax.swing.JRadioButton();
        scriptlessButton = new javax.swing.JRadioButton();
        tegdependentButton = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        attrLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        bodyContentLb = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        descriptionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        attrTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagHandlerPanel2"));
        tldTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tldTextField, gridBagConstraints);
        tldTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TLDFile"));

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_Browse_Mnemonic").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_Browse"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_Browse"));

        tldFileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TLDName_mnem").charAt(0));
        tldFileLabel.setLabelFor(tldTextField);
        tldFileLabel.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_tldFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(tldFileLabel, gridBagConstraints);

        tagNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TagName_mnem").charAt(0));
        tagNameLabel.setLabelFor(nameTextField);
        tagNameLabel.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_tagName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(tagNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagName"));

        tagClassLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_ClassName_mnem").charAt(0));
        tagClassLabel.setLabelFor(classTextField);
        tagClassLabel.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_tagHandlerClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(tagClassLabel, gridBagConstraints);

        classTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(classTextField, gridBagConstraints);
        classTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagClass"));

        buttonGroup1.add(emptyButton);
        emptyButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TagEmpty_mnem").charAt(0));
        emptyButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_emptyBodyContent"));
        jPanel1.add(emptyButton);
        emptyButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagEmpty"));

        buttonGroup1.add(scriptlessButton);
        scriptlessButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TagScriptless_mnem").charAt(0));
        scriptlessButton.setSelected(true);
        scriptlessButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_scriptlessBodyContent"));
        jPanel1.add(scriptlessButton);
        scriptlessButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagScriptless"));

        buttonGroup1.add(tegdependentButton);
        tegdependentButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_TagTagdependent_mnem").charAt(0));
        tegdependentButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_tagdependentBodyContent"));
        jPanel1.add(tegdependentButton);
        tegdependentButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_TagTagdependent"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);

        attrLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_Attribs_mnem").charAt(0));
        attrLabel.setLabelFor(attrTable);
        attrLabel.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "TITLE_attributes"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 6, 0);
        add(attrLabel, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        newButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_new_mnemonic").charAt(0));
        newButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_newButton"));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(newButton, gridBagConstraints);
        newButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_newButton"));

        editButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_edit_mnemonic").charAt(0));
        editButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_editButton"));
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel3.add(editButton, gridBagConstraints);
        editButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_editButton"));

        deleteButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_delete_mnemonic").charAt(0));
        deleteButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_deleteButton"));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel3.add(deleteButton, gridBagConstraints);
        deleteButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("LBL_deleteButton"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jPanel3, gridBagConstraints);

        bodyContentLb.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "LBL_bodyContent"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 0);
        add(bodyContentLb, gridBagConstraints);

        jCheckBox1.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_AddToTLD_mnem").charAt(0));
        jCheckBox1.setSelected(true);
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_addToTLD"));
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jCheckBox1, gridBagConstraints);
        jCheckBox1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("OPT_addToTLD"));

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "HINT_tldFile"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(descriptionLabel, gridBagConstraints);

        jScrollPane1.setViewportView(attrTable);
        attrTable.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/wizards/Bundle").getString("A11Y_DESC_AttrTable"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(jScrollPane1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // TODO add your handling code here:
        int row = attrTable.getSelectedRow();
        if (row>=0) {
            ((AttrTableModel)attrTable.getModel()).removeRow(row);
            ((AttrTableModel)attrTable.getModel()).fireTableRowsDeleted(row, row);
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        // TODO add your handling code here:
        String title =  org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "TITLE_attr_add"); //NOI18N
        final AttrDialog panel = new AttrDialog();
        final AttrTableModel tableModel = (AttrTableModel)attrTable.getModel();
        EditDialog editDialog = new EditDialog(panel,title, true) {
            protected String validate() {
                String newAttrName = panel.getAttrName();
                //String errorMessage=null;
                if (newAttrName.length()==0) {
                    return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                       "MSG_attr_no_name");
                } else if (!isJavaIdentifier(newAttrName)) {
                    return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                       "MSG_wrong_attr_name",newAttrName);
                } else {
                    Object[][] attrs = tableModel.getAttributes();
                    for (int i=0;i<attrs.length;i++){
                        if (newAttrName.equals(attrs[i][0])) {
                            return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                           "MSG_attr_exists");
                        }
                    }
                }
                return null;
            }
        };
        editDialog.setValid(false);
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(editDialog);
        panel.getAttrNameTF().getDocument().addDocumentListener(docListener);
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(editDialog);
        d.getAccessibleContext().setAccessibleDescription(editDialog.getDialogPanel().getAccessibleContext().getAccessibleDescription());
        d.show();
        panel.getAttrNameTF().getDocument().removeDocumentListener(docListener);
        
        if (editDialog.getValue().equals(EditDialog.OK_OPTION)) {
                int rowCount = tableModel.getRowCount();
                tableModel.addRow(panel.getAttrName(),panel.getAttrType(),panel.isRequired(),panel.isRtexpr());
                tableModel.fireTableRowsInserted(rowCount, rowCount);
        }
    }//GEN-LAST:event_newButtonActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
        if ( browseButton == evt.getSource() ) {
            org.openide.filesystems.FileObject fo=null;
            // Show the browse dialog 
            if (folders!=null) fo = BrowseFolders.showDialog(folders,
                    TLDDataObject.class,
                    "");
            else {       
                Sources sources = ProjectUtils.getSources(proj);
                fo = BrowseFolders.showDialog( sources.getSourceGroups( Sources.TYPE_GENERIC ),
                                               org.openide.loaders.DataFolder.class,
                                               "");
            }
            
            if (fo != null) {
                tldFo = fo;
                FileObject targetFolder = Templates.getTargetFolder(wiz);
                WebModule wm = WebModule.getWebModule(targetFolder);
                //tldTextField.setText(target==null || target.length()==0?fo.getNameExt():target+"/"+fo.getNameExt());
                tldTextField.setText(FileUtil.getRelativePath(wm == null ? proj.getProjectDirectory() : wm.getDocumentBase(), fo));
                
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        try {
                            InputStream is = tldFo.getInputStream();
                            try {
                                // get existing tag names for testing duplicity
                                tagValues = Util.getTagValues(is, new String[]{"tag", "tag-file"}, "name"); //NOI18N
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (org.xml.sax.SAXException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                             Exceptions.printStackTrace(e);
                        }
                        panel.fireChangeEvent();
                    }
                });
            }
                        
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        if (attrTable.getSelectionModel().isSelectionEmpty()) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        } else {
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        // TODO add your handling code here:
        if (jCheckBox1.isSelected()) {
            //tldTextField.setEnabled(true);
            browseButton.setEnabled(true);
            nameTextField.setEnabled(true);
            emptyButton.setEnabled(true);
            scriptlessButton.setEnabled(true);
            tegdependentButton.setEnabled(true);
        } else {
            //tldTextField.setEnabled(false);
            browseButton.setEnabled(false);
            nameTextField.setEnabled(false);
            emptyButton.setEnabled(false);
            scriptlessButton.setEnabled(false);
            tegdependentButton.setEnabled(false);
        }
        panel.fireChangeEvent();
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
        String title =  org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "TITLE_attr_edit"); //NOI18N
        final AttrTableModel tableModel = (AttrTableModel)attrTable.getModel();
        final int row = attrTable.getSelectedRow();
        String attrName = (String)tableModel.getValueAt(row,0);
        String attrType = (String)tableModel.getValueAt(row,1);
        boolean required = ((Boolean)tableModel.getValueAt(row,2)).booleanValue();
        boolean rtexpr = ((Boolean)tableModel.getValueAt(row,3)).booleanValue();
        final AttrDialog panel = new AttrDialog(attrName,attrType,required,rtexpr);
        EditDialog editDialog = new EditDialog(panel,title, false) {
            protected String validate() {
                String newAttrName = panel.getAttrName();
                if (newAttrName.length()==0) {
                    return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                       "MSG_attr_no_name");
                } else if (!isJavaIdentifier(newAttrName)) {
                    return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                       "MSG_wrong_attr_name",newAttrName);
                } else {
                    Object[][] attrs = tableModel.getAttributes();
                    for (int i=0;i<attrs.length;i++){
                        if (i!=row && newAttrName.equals(attrs[i][0])) {
                            return NbBundle.getMessage(TagHandlerPanelGUI.class, 
                                                           "MSG_attr_exists");
                        }
                    }
                }
                return null;
            }
        };
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(editDialog);
        panel.getAttrNameTF().getDocument().addDocumentListener(docListener);

        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(editDialog);
        d.getAccessibleContext().setAccessibleDescription(editDialog.getDialogPanel().getAccessibleContext().getAccessibleDescription());
        d.show();
        panel.getAttrNameTF().getDocument().removeDocumentListener(docListener);
        if (editDialog.getValue().equals(EditDialog.OK_OPTION)) {
            tableModel.setData(panel.getAttrName(),panel.getAttrType(),panel.isRequired(),panel.isRtexpr(),row);
            tableModel.fireTableRowsUpdated(row, row);
        }
    }//GEN-LAST:event_editButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attrLabel;
    private javax.swing.JTable attrTable;
    private javax.swing.JLabel bodyContentLb;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField classTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JRadioButton emptyButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton newButton;
    private javax.swing.JRadioButton scriptlessButton;
    private javax.swing.JLabel tagClassLabel;
    private javax.swing.JLabel tagNameLabel;
    private javax.swing.JRadioButton tegdependentButton;
    private javax.swing.JLabel tldFileLabel;
    private javax.swing.JTextField tldTextField;
    // End of variables declaration//GEN-END:variables
    
    
    String getTagName() {
        return nameTextField.getText();
    }
    
    void setTagName(String name) {
        nameTextField.setText(name);
    }
    
    void setClassName(String name) {
        classTextField.setText(name);
    }
    
    FileObject getTLDFile() {
        return tldFo;
    }
    
    java.util.Set getTagValues() {
        return tagValues;
    }
    
    boolean isEmpty() {
        return emptyButton.isSelected();
    }
    boolean isScriptless() {
        return scriptlessButton.isSelected();
    }
    boolean isTegdependent() {
        return tegdependentButton.isSelected();
    }
    
    boolean writeToTLD() {
        return jCheckBox1.isSelected();
    }
    
    Object[][] getAttributes() {
        return ((AttrTableModel)attrTable.getModel()).getAttributes();
    }
    
    void setBodySupport(boolean bodySupport) {
        if (bodySupport) scriptlessButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_jspBodyContent"));
        else scriptlessButton.setText(org.openide.util.NbBundle.getMessage(TagHandlerPanelGUI.class, "OPT_scriptlessBodyContent"));
    }
    
    private boolean isJavaIdentifier(String s) {
        if (s.length()==0) return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i=1;i<s.length();i++)
            if (!Character.isJavaIdentifierPart(s.charAt(i))) return false;
        return true;
    }
    
}
