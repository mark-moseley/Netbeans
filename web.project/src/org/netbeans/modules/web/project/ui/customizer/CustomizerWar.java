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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customizer for WAR packaging.
 */
public class CustomizerWar extends JPanel implements HelpCtx.Provider {
    
    WebProjectProperties uiProperties;
    
    /** Creates new form CustomizerCompile */
    public CustomizerWar(WebProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerWar.class, "ACS_CustomizeWAR_A11YDesc")); //NOI18N
        
        jTextFieldFileName.setDocument( uiProperties.WAR_NAME_MODEL );
        jTextFieldExContent.setDocument( uiProperties.BUILD_CLASSES_EXCLUDES_MODEL );
        uiProperties.WAR_COMPRESS_MODEL.setMnemonic( jCheckBoxCompress.getMnemonic() );
        jCheckBoxCompress.setModel( uiProperties.WAR_COMPRESS_MODEL );
        
        initTableVisualProperties(jTableAddContent);
        
        WarIncludesUi.EditMediator.register( uiProperties.getProject(),
                jTableAddContent,
                jButtonAddJar.getModel(),
                jButtonAddLib.getModel(),
                jButtonAddProject.getModel(),
                jButtonRemove.getModel());
    }
    
    private void initTableVisualProperties(JTable table) {
        WarIncludesUiSupport.ClasspathTableModel model = uiProperties.WAR_CONTENT_ADDITIONAL_MODEL;
        table.setModel(model);
        
        table.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(CustomizerWar.class, "TXT_WAR_Item"));
        table.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(CustomizerWar.class, "TXT_WAR_PathInWAR"));
        table.getColumnModel().getColumn(0).setCellRenderer(new WarIncludesUi.ClassPathCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null) {
                    setToolTipText(value.toString());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        table.setRowHeight(jTableAddContent.getRowHeight() + 4);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(240);
        column.setWidth(240);
        column.setMinWidth(75);
        column = table.getColumnModel().getColumn(1);
        column.setMinWidth(187);
        column.setWidth(187);
        column.setMinWidth(75);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabelFileName = new javax.swing.JLabel();
        jTextFieldFileName = new javax.swing.JTextField();
        jLabelExContent = new javax.swing.JLabel();
        jTextFieldExContent = new javax.swing.JTextField();
        excludeMessage = new javax.swing.JLabel();
        jCheckBoxCompress = new javax.swing.JCheckBox();
        jLabelAddContent = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAddContent = new javax.swing.JTable();
        jButtonAddProject = new javax.swing.JButton();
        jButtonAddLib = new javax.swing.JButton();
        jButtonAddJar = new javax.swing.JButton();
        jButtonRemove = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelFileName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_FileName_LabelMnemonic").charAt(0));
        jLabelFileName.setLabelFor(jTextFieldFileName);
        jLabelFileName.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_FileName_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        jPanel1.add(jLabelFileName, gridBagConstraints);

        jTextFieldFileName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        jPanel1.add(jTextFieldFileName, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle"); // NOI18N
        jTextFieldFileName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_FileName_A11YDesc")); // NOI18N

        jLabelExContent.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_Content_LabelMnemonic").charAt(0));
        jLabelExContent.setLabelFor(jTextFieldExContent);
        jLabelExContent.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Content_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        jPanel1.add(jLabelExContent, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jTextFieldExContent, gridBagConstraints);
        jTextFieldExContent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "ACS_CustomizeWAR_Content_A11YDesc")); // NOI18N

        excludeMessage.setLabelFor(jTextFieldExContent);
        excludeMessage.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizerWAR_ExcludeMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(excludeMessage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jPanel1, gridBagConstraints);

        jCheckBoxCompress.setMnemonic(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Commpres_LabelMnemonic").charAt(0));
        jCheckBoxCompress.setText(org.openide.util.NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Commpres_JCheckBox")); // NOI18N
        jCheckBoxCompress.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxCompress.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jCheckBoxCompress, gridBagConstraints);
        jCheckBoxCompress.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_Commpres_A11YDesc")); // NOI18N

        jLabelAddContent.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddContent_LabelMnemonic").charAt(0));
        jLabelAddContent.setLabelFor(jTableAddContent);
        jLabelAddContent.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddContent_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 2, 0);
        add(jLabelAddContent, gridBagConstraints);

        jTableAddContent.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTableAddContent);
        jTableAddContent.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddContent_A11YDesc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        add(jScrollPane2, gridBagConstraints);

        jButtonAddProject.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddProject_LabelMnemonic").charAt(0));
        jButtonAddProject.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddProject_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddProject, gridBagConstraints);
        jButtonAddProject.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddProject_A11YDesc")); // NOI18N

        jButtonAddLib.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddLib_LabelMnemonic").charAt(0));
        jButtonAddLib.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddLib_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButtonAddLib, gridBagConstraints);
        jButtonAddLib.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddLib_A11YDesc")); // NOI18N

        jButtonAddJar.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AddJar_LabelMnemonic").charAt(0));
        jButtonAddJar.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_AddJar_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jButtonAddJar, gridBagConstraints);
        jButtonAddJar.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AddJar_A11YDesc")); // NOI18N

        jButtonRemove.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/project/ui/customizer/Bundle").getString("LBL_CustomizeWAR_AdditionalRemove_LabelMnemonic").charAt(0));
        jButtonRemove.setText(NbBundle.getMessage(CustomizerWar.class, "LBL_CustomizeWAR_Remove_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(jButtonRemove, gridBagConstraints);
        jButtonRemove.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CustomizeWAR_AdditionalRemove_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JButton jButtonAddJar;
    private javax.swing.JButton jButtonAddLib;
    private javax.swing.JButton jButtonAddProject;
    private javax.swing.JButton jButtonRemove;
    private javax.swing.JCheckBox jCheckBoxCompress;
    private javax.swing.JLabel jLabelAddContent;
    private javax.swing.JLabel jLabelExContent;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableAddContent;
    private javax.swing.JTextField jTextFieldExContent;
    private javax.swing.JTextField jTextFieldFileName;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerWar.class);
    }
    
}
