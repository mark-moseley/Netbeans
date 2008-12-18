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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.node.CatalogNode;
import org.netbeans.modules.db.explorer.node.SchemaNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.util.TextFieldValidator;
import org.netbeans.modules.db.util.ValidableTextField;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


public class AddTableColumnDialog {
    static final Logger LOGGER = Logger.getLogger(AddTableColumnDialog.class.getName());
    boolean result = false;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    Dialog dialog = null;
    Specification spec;
    AddTableColumnDDL ddl;
    Map ixmap;
    Map ix_uqmap;
    String colname = null;
    transient private static final String tempStr = new String();
    JTextField colnamefield, colsizefield, colscalefield, defvalfield;
    JTextArea checkfield;
    JComboBox coltypecombo, idxcombo;
    JCheckBox pkcheckbox, ixcheckbox, checkcheckbox, nullcheckbox, uniquecheckbox;
    DataModel dmodel = new DataModel();
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public AddTableColumnDialog(final Specification spe, final TableNode nfo) throws DatabaseException {
        spec = spe;
        try {
            String tableName = nfo.getName();
            String schemaName = nfo.getSchemaName();
            String catName = nfo.getCatalogName();

            if (schemaName == null) {
                schemaName = catName;
            } else if (catName == null) {
                catName = schemaName;
            }

            DriverSpecification drvSpec = nfo.getLookup().lookup(DatabaseConnection.class).getConnector().getDriverSpecification(catName);
            
            ddl = new AddTableColumnDDL(spec, drvSpec, schemaName, tableName);

            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(12, 12, 5, 11)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con;
            pane.setLayout (layout);

            TextFieldListener fldlistener = new TextFieldListener(dmodel);
            IntegerFieldListener intfldlistener = new IntegerFieldListener(dmodel);

            // Column name

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnName")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnNameA11yDesc"));
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 0;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colnamefield = new JTextField(35);
            colnamefield.setName(ColumnItem.NAME);
            colnamefield.addFocusListener(fldlistener);
            colnamefield.setToolTipText(bundle.getString("ACS_AddTableColumnNameTextFieldA11yDesc"));
            colnamefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnNameTextFieldA11yName"));
            label.setLabelFor(colnamefield);
            pane.add(colnamefield, con);
            colnamefield.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    validate();
                }
                public void removeUpdate(DocumentEvent e) {
                    validate();
                }
                public void changedUpdate(DocumentEvent e) {
                    validate();
                }
            });

            // Column type

            Map tmap = spec.getTypeMap();
            Vector ttab = new Vector(tmap.size());
            Iterator iter = tmap.keySet().iterator();
            while (iter.hasNext()) {
                String iterkey = (String)iter.next();
                String iterval = (String)tmap.get(iterkey);
                ttab.add(new TypeElement(iterkey, iterval));
            }

            ColumnItem item = new ColumnItem();
            item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
            dmodel.addRow(item);

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnType")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnTypeA11yDesc"));
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 1;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            coltypecombo = new JComboBox(ttab);
            coltypecombo.addActionListener(new ComboBoxListener(dmodel));
            coltypecombo.setName(ColumnItem.TYPE);
            coltypecombo.setToolTipText(bundle.getString("ACS_AddTableColumnTypeComboBoxA11yDesc"));
            coltypecombo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnTypeComboBoxA11yName"));
            label.setLabelFor(coltypecombo);
            pane.add(coltypecombo, con);

            // Column size

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnSize")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnSizeA11yDesc"));
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colsizefield = new ValidableTextField(new TextFieldValidator.integer());
            colsizefield.setName(ColumnItem.SIZE);
            colsizefield.addFocusListener(intfldlistener);
            colsizefield.setToolTipText(bundle.getString("ACS_AddTableColumnSizeTextFieldA11yDesc"));
            colsizefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnSizeTextFieldA11yName"));
            label.setLabelFor(colsizefield);
            pane.add(colsizefield, con);

            // Column scale

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnScale")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnScaleA11yDesc"));
            con = new GridBagConstraints ();
            con.gridx = 2;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 3;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colscalefield = new ValidableTextField(new TextFieldValidator.integer());
            colscalefield.setName(ColumnItem.SCALE);
            colscalefield.addFocusListener(intfldlistener);
            colscalefield.setToolTipText(bundle.getString("ACS_AddTableColumnScaleTextFieldA11yDesc"));
            colscalefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnScaleTextFieldA11yName"));
            label.setLabelFor(colscalefield);
            pane.add(colscalefield, con);

            // Column default value

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnDefault")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnDefaultA11yDesc"));
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 3;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 3;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            defvalfield = new JTextField(35);
            defvalfield.setName(ColumnItem.DEFVAL);
            defvalfield.addFocusListener(fldlistener);
            defvalfield.setToolTipText(bundle.getString("ACS_AddTableColumnDefaultTextFieldA11yDesc"));
            defvalfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnDefaultTextFieldA11yName"));
            label.setLabelFor(defvalfield);
            layout.setConstraints(defvalfield, con);
            pane.add(defvalfield);

            // Check subpane

            JPanel subpane = new JPanel();
            subpane.setBorder(new TitledBorder(bundle.getString("AddTableColumnConstraintsTitle"))); //NOI18N
            GridBagLayout sublayout = new GridBagLayout();
            subpane.setLayout(sublayout);

            ActionListener cbxlistener = new CheckBoxListener(dmodel);

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pkcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(pkcheckbox, bundle.getString("AddTableColumnConstraintPKTitle")); //NOI18N
            pkcheckbox.setName(ColumnItem.PRIMARY_KEY);
            pkcheckbox.addActionListener(cbxlistener);
            pkcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintPKTitleA11yDesc"));
            subpane.add(pkcheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            uniquecheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(uniquecheckbox, bundle.getString("AddTableColumnConstraintUniqueTitle")); //NOI18N
            uniquecheckbox.setName(ColumnItem.UNIQUE);
            uniquecheckbox.addActionListener(cbxlistener);
            uniquecheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintUniqueTitleA11yDesc"));
            subpane.add(uniquecheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 2;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            nullcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(nullcheckbox, bundle.getString("AddTableColumnConstraintNullTitle")); //NOI18N
            nullcheckbox.setName(ColumnItem.NULLABLE);
            nullcheckbox.addActionListener(cbxlistener);
            nullcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintNullTitleA11yDesc"));
            subpane.add(nullcheckbox, con);

            // Insert subpane

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 4;
            con.gridwidth = 4;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            pane.add(subpane, con);

            // are there primary keys?
            boolean isPK = false;
            try {
                drvSpec.getPrimaryKeys(tableName);

                ResultSet rs = drvSpec.getResultSet();

                if( rs != null ) {
                    if(rs.next())
                        isPK = true;
                    rs.close();
                }
                
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }

            // Index name combo

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 5;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.NORTHWEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            ixcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(ixcheckbox, bundle.getString("AddTableColumnIndexName")); //NOI18N
            ixcheckbox.setName(ColumnItem.INDEX);
            ixcheckbox.addActionListener(cbxlistener);
            ixcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnIndexNameA11yDesc"));
            pane.add(ixcheckbox, con);

            ixmap = ddl.getIndexMap();
            ix_uqmap = ddl.getUniqueIndexMap();
            
            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 5;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            idxcombo = new JComboBox(new Vector(ixmap.keySet()));
            idxcombo.setToolTipText(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yDesc"));
            idxcombo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yName"));
            idxcombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yDesc"));
            //idxcombo.setSelectedIndex(0);
            pane.add(idxcombo, con);

            // Check title and textarea

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 6;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.NORTHWEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            checkcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(checkcheckbox, bundle.getString("AddTableColumnConstraintCheckTitle")); //NOI18N
            checkcheckbox.setName(ColumnItem.CHECK);
            checkcheckbox.addActionListener(cbxlistener);
            checkcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnCheckTitleA11yDesc"));
            pane.add(checkcheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 6;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 1.0;
            checkfield = new JTextArea(3, 35);
            checkfield.setName(ColumnItem.CHECK_CODE);
            checkfield.addFocusListener(fldlistener);
            checkfield.setToolTipText(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yDesc"));
            checkfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yName"));
            checkfield.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yDesc"));
            JScrollPane spane = new JScrollPane(checkfield);
            pane.add(spane, con);

            checkcheckbox.setSelected(false);
            checkcheckbox.setSelected(false);
            nullcheckbox.setSelected(true);
            uniquecheckbox.setSelected(false);
            pkcheckbox.setEnabled(!isPK);
            idxcombo.setEnabled(idxcombo.getItemCount()>0);
            ixcheckbox.setEnabled(idxcombo.isEnabled());
            
            item.addPropertyChangeListener(new PropertyChangeListener() {
                                               public void propertyChange(PropertyChangeEvent evt) {
                                                   String pname = evt.getPropertyName();
                                                   Object nval = evt.getNewValue();
                                                   if (nval instanceof Boolean) {
                                                       boolean set = ((Boolean)nval).booleanValue();
                                                       if (pname.equals(ColumnItem.PRIMARY_KEY)) {
                                                           pkcheckbox.setSelected(set);
                                                           //idxcombo.setEnabled(!set);
                                                           //ixcheckbox.setEnabled(!set);
                                                           //ixcheckbox.setSelected(set);
                                                       } else if (pname.equals(ColumnItem.INDEX)) {
                                                           ixcheckbox.setSelected(set);
                                                       } else if (pname.equals(ColumnItem.UNIQUE)) {
                                                           uniquecheckbox.setSelected(set);
                                                           idxcombo.setEnabled(!set);
                                                           ixcheckbox.setEnabled(!set);
                                                           ixcheckbox.setSelected(set);
                                                           if(set) {
                                                               idxcombo.addItem(tempStr);
                                                               idxcombo.setSelectedItem(tempStr);
                                                           } else {
                                                               idxcombo.removeItem(tempStr);
                                                               idxcombo.setEnabled(idxcombo.getItemCount()>0);
                                                               ixcheckbox.setEnabled(idxcombo.isEnabled());
                                                           }
                                                       } else if (pname.equals(ColumnItem.NULLABLE)) {
                                                           nullcheckbox.setSelected(set);
                                                       }
                                                   }
                                               }
                                           });

            ActionListener listener = new ActionListener() {
                  public void actionPerformed(ActionEvent event) {
                      if (event.getSource() != DialogDescriptor.OK_OPTION) {
                          return;
                      }
                      result = validate();
                      if ( ! result ) {
                          String msg = bundle.getString(
                              "EXC_InsufficientAddColumnInfo");
                          DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(msg, 
                                    NotifyDescriptor.ERROR_MESSAGE));
                          return;
                      }

                      colname = colnamefield.getText();
                      final ColumnItem citem = (ColumnItem)dmodel.getData().elementAt(0);
                      final String indexName = (String)idxcombo.getSelectedItem();
                      boolean wasException;
                      try {
                          wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                              public Boolean call() throws Exception {
                                  return ddl.execute(colname, citem, indexName);
                              }
                          });
                      } catch (InvocationTargetException e) {
                          Throwable cause = e.getCause();
                          if (cause instanceof DDLException) {
                              DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                          } else {
                              LOGGER.log(Level.INFO, null, cause);
                              DbUtilities.reportError(bundle.getString("ERR_UnableToAddColumn"), e.getMessage());
                          }
                          return;
                      }

                      // was execution of commands with or without exception?
                      if( wasException ) {
                          return;
                      }
                      
                      // dialog is closed after successfully add column
                      dialog.setVisible(false);
                      dialog.dispose();
                  }
              };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnDialogA11yDesc"));
                  
            descriptor = new DialogDescriptor(pane, bundle.getString("AddColumnDialogTitle"), true, listener); //NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            statusLine = descriptor.createNotificationLineSupport();
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
            validate();
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    private boolean validate() {
        String colname = colnamefield.getText();
        if (colname == null || colname.length() < 1) {
            statusLine.setInformationMessage(bundle.getString("AddTableColumn_EmptyColName"));
            updateOK(false);
            return false;
        }

        statusLine.clearMessages();
        updateOK(true);

        Enumeration colse = dmodel.getData().elements();
        while(colse.hasMoreElements()) {
            ColumnItem ci = (ColumnItem)colse.nextElement();
            if (!ci.validate()) {
                // Model is updated only after focus from a field is lost...
                // ... so we cannot test this here ...
                // statusLine.setErrorMessage(bundle.getString("AddTableColumn_InvalidColInfo")+ci.getName());
                // updateOK(false);
                return false;
            }
        }

        return true;
    }

    private void updateOK(boolean valid) {
        if (descriptor != null) {
            descriptor.setValid(valid);
        }
    }
    
    public String getColumnName() {
        return colname;
    }

    class CheckBoxListener implements ActionListener {
        private DataModel data;

        CheckBoxListener(DataModel data) {
            this.data = data;
        }

        public void actionPerformed(ActionEvent event) {
            JCheckBox cbx = (JCheckBox)event.getSource();
            String code = cbx.getName();
            data.setValue(cbx.isSelected() ? Boolean.TRUE : Boolean.FALSE, code, 0);
        }
    }

    class ComboBoxListener implements ActionListener {
        private DataModel data;

        ComboBoxListener(DataModel data) {
            this.data = data;
        }

        public void actionPerformed(ActionEvent event) {
            JComboBox cbx = (JComboBox)event.getSource();
            String code = cbx.getName();
            data.setValue(cbx.getSelectedItem(), code, 0);
        }
    }

    class TextFieldListener implements FocusListener {
        private DataModel data;

        TextFieldListener(DataModel data) {
            this.data = data;
        }

        public void focusGained(FocusEvent event) {
        }

        public void focusLost(FocusEvent event) {
            JTextComponent fld = (JTextComponent)event.getSource();
            String code = fld.getName();
            data.setValue(fld.getText(), code, 0);
        }
    }

    class IntegerFieldListener implements FocusListener {
        private DataModel data;

        IntegerFieldListener(DataModel data) {
            this.data = data;
        }

        public void focusGained(FocusEvent event) {
        }

        public void focusLost(FocusEvent event) {
            JTextComponent fld = (JTextComponent)event.getSource();
            String code = fld.getName();
            String numero = fld.getText();
            Integer ival;
            if (numero == null || numero.length()==0) numero = "0"; //NOI18N
            ival = new Integer(numero);
            data.setValue(ival, code, 0);
        }
    }
}
