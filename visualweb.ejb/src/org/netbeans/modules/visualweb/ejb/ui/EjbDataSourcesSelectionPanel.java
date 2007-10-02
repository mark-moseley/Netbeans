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
/*
 * ExportImportDataSourcesDialog.java
 *
 * Created on March 8, 2004, 12:09 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 * A panel used to allow the user to select EJB datasources for exporting or importing
 * 
 * @author cao
 */
public class EjbDataSourcesSelectionPanel extends JPanel {

    private EjbDataSourcePropertiesPanel propsPanel;

    private PortableEjbDataSource[] ejbDataSources;

    private int prevSelectedIndex = 0;

    public EjbDataSourcesSelectionPanel(EjbDataSourcePropertiesPanel propsPanel) {
        initComponents();

        this.propsPanel = propsPanel;
        listScrollPane
                .setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        datasourceList.setCellRenderer(new CustomListRenderer());
        datasourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        datasourceList.addMouseListener(new CustomMouseListener());
        datasourceList.addKeyListener(new CustomKeyListener());
        selectAllButton.setEnabled(false);
        clearAllButton.setEnabled(false);
    }

    public void clear() {
        datasourceList.setListData(new Object[] {});
        datasourceList.repaint();
        propsPanel.clear();
    }

    /**
     * This method fills the list with the given EJB groups
     */
    public void setEjbDataSources(PortableEjbDataSource[] ejbDataSources) {
        this.ejbDataSources = ejbDataSources;

        if (ejbDataSources != null && ejbDataSources.length > 0) {
            propsPanel.setDataSourceProperties(ejbDataSources[0].getEjbGroup());
            datasourceList.setListData(ejbDataSources);
            datasourceList.setSelectedIndex(0);
            selectAllButton.setEnabled(true);
            clearAllButton.setEnabled(true);
        }

    }

    /**
     * Called to repaint the list after changing the name of the Datasource Config Info
     */
    public void repaintList() {
        datasourceList.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        listScrollPane = new javax.swing.JScrollPane();
        datasourceList = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        clearAllButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        listScrollPane.setBorder(null);
        listScrollPane.setMinimumSize(new java.awt.Dimension(200, 130));
        datasourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        datasourceList.setMaximumSize(null);
        datasourceList.setMinimumSize(new java.awt.Dimension(500, 500));
        datasourceList.setPreferredSize(null);
        datasourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                datasourceListValueChanged(evt);
            }
        });

        listScrollPane.setViewportView(datasourceList);
        datasourceList.getAccessibleContext().setAccessibleName(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle")
                        .getString("EJB_GROUPS"));
        datasourceList.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle")
                        .getString("EJB_GROUPS"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(listScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        selectAllButton.setMnemonic(java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/visualweb/ejb/ui/Bundle").getString(
                "SELECT_ALL_BUTTON_MNEMONIC").charAt(0));
        selectAllButton.setText(java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/visualweb/ejb/ui/Bundle")
                .getString("SELECT_ALL_BUTTON_LABEL"));
        selectAllButton.setPreferredSize(null);
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(selectAllButton);
        selectAllButton.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle")
                        .getString("SELECT_ALL_BUTTON_DESC"));

        clearAllButton.setMnemonic(java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("CLEAR_BUTTON_MNEMONIC")
                .charAt(0));
        clearAllButton.setText(java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("CLEAR_BUTTON_LABEL"));
        clearAllButton.setPreferredSize(null);
        clearAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(clearAllButton);
        clearAllButton.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle")
                        .getString("CLEAR_BUTTON_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(buttonPanel, gridBagConstraints);

    }

    // </editor-fold>//GEN-END:initComponents

    private void datasourceListValueChanged(javax.swing.event.ListSelectionEvent evt) {// GEN-FIRST:event_datasourceListValueChanged

        int index = ((JList) evt.getSource()).getSelectedIndex();
        if (index == prevSelectedIndex)
            return;

        // Save the changes the user has made
        if (!propsPanel.saveChange()) {
            datasourceList.setSelectedIndex(prevSelectedIndex);
        } else
            prevSelectedIndex = index;
    }// GEN-LAST:event_datasourceListValueChanged

    private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_clearAllButtonActionPerformed

        if (ejbDataSources != null) {
            for (int i = 0; i < ejbDataSources.length; i++)
                ejbDataSources[i].setIsPortable(false);

            datasourceList.repaint();
        }
    }// GEN-LAST:event_clearAllButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_selectAllButtonActionPerformed

        if (ejbDataSources != null) {
            for (int i = 0; i < ejbDataSources.length; i++)
                ejbDataSources[i].setIsPortable(true);

            datasourceList.repaint();
        }
    }// GEN-LAST:event_selectAllButtonActionPerformed

    class CustomListRenderer extends JCheckBox implements ListCellRenderer {

        public CustomListRenderer() {
            setHorizontalAlignment(JCheckBox.LEFT);
            setVerticalAlignment(JCheckBox.CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(((PortableEjbDataSource) value).getName());

            if (((PortableEjbDataSource) value).isPortable())
                setSelected(true);
            else
                setSelected(false);

            return this;
        }
    }

    /**
     * Let us have our own mouse listener to change the checkbox selection since checkbox will never
     * get the mouse events for itself to render.
     */
    class CustomMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            JList list = (JList) e.getSource();
            int index = list.getSelectedIndex();
            if (index < 0)
                return;
            Object selection = list.getModel().getElementAt(index);
            if (e.getX() < 20) {

                // Check it if it is not checked
                // Uncheck it if it is checked
                PortableEjbDataSource ejbSrc = (PortableEjbDataSource) selection;
                if (ejbSrc.isPortable())
                    ejbSrc.setIsPortable(false);
                else
                    ejbSrc.setIsPortable(true);

                list.repaint();
            }
            propsPanel.setDataSourceProperties(((PortableEjbDataSource) selection).getEjbGroup());
        }
    }

    private static class CustomKeyListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                JList list = (JList) e.getSource();
                int index = list.getSelectedIndex();
                if (index != -1) {
                    // Toggle the selection state
                    PortableEjbDataSource ejbSrc = (PortableEjbDataSource) list.getModel()
                            .getElementAt(index);
                    ejbSrc.setIsPortable(!ejbSrc.isPortable());
                    list.repaint();
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;

    private javax.swing.JButton clearAllButton;

    private javax.swing.JList datasourceList;

    private javax.swing.JScrollPane listScrollPane;

    private javax.swing.JButton selectAllButton;
    // End of variables declaration//GEN-END:variables

}
