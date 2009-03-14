/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * QueryPanel.java
 *
 * Created on Oct 14, 2008, 5:45:44 PM
 */

package org.netbeans.modules.bugzilla.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.util.NoContentPanel;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValueCellRenderer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class QueryPanel extends javax.swing.JPanel {
    final ExpandablePanel byText;
    final ExpandablePanel byDetails;
    final ExpandablePanel byPeople;
    final ExpandablePanel byLastChange;
    private JComponent tableComponent;
    private NoContentPanel noContentPanel;

    /** Creates new form QueryPanel */
    public QueryPanel(JComponent tableComponent) {
        initComponents();
        Font f = new JLabel().getFont();
        int s = f.getSize();
        nameLabel.setFont(new Font(f.getName(), f.getStyle(), (int) (s * 1.7)));

        this.tableComponent = tableComponent;
        noContentPanel = new NoContentPanel();
        tablePanel.add(tableComponent);

        JTree tv = new JTree();
        BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
        Icon ei = tvui.getExpandedIcon();
        Icon ci = tvui.getCollapsedIcon();

        byTextContainer.add(byTextPanel);
        byDetailsContainer.add(byDetailsPanel);
        byPeopleContainer.add(byPeoplePanel);
        byLastChangeContainer.add(byLastChangePanel);

        byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
        byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);
        byPeople = new ExpandablePanel(byPeopleLabel, byPeopleContainer, ei, ci);
        byLastChange = new ExpandablePanel(byLastChangeLabel, byLastChangeContainer, ei, ci);

        byText.expand();
        byDetails.expand();
        byPeople.colapse();
        byLastChange.colapse();

        urlPanel.setVisible(false);
        queryHeaderPanel.setVisible(false);
        tableFieldsPanel.setVisible(false);
        saveChangesButton.setVisible(false);
        cancelChangesButton.setVisible(false);
        filterComboBox.setVisible(false);
        filterLabel.setVisible(false);
        noContentContainer.setVisible(false);
        noContentContainer.add(noContentPanel);

        summaryComboBox.setModel(new DefaultComboBoxModel());
        commentComboBox.setModel(new DefaultComboBoxModel());
        keywordsComboBox.setModel(new DefaultComboBoxModel());
        peopleComboBox.setModel(new DefaultComboBoxModel());
        
        summaryComboBox.setRenderer(new ParameterValueCellRenderer());
        commentComboBox.setRenderer(new ParameterValueCellRenderer());
        keywordsComboBox.setRenderer(new ParameterValueCellRenderer());
        peopleComboBox.setRenderer(new ParameterValueCellRenderer());
        severityList.setCellRenderer(new ParameterValueCellRenderer());
        productList.setCellRenderer(new ParameterValueCellRenderer());
        componentList.setCellRenderer(new ParameterValueCellRenderer());
        versionList.setCellRenderer(new ParameterValueCellRenderer());
        statusList.setCellRenderer(new ParameterValueCellRenderer());
        resolutionList.setCellRenderer(new ParameterValueCellRenderer());
        priorityList.setCellRenderer(new ParameterValueCellRenderer());
        changedList.setCellRenderer(new ParameterValueCellRenderer());

        saveErrorLabel.setForeground(new Color(153,0,0));
        Image img = ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"); //NOI18N
        saveErrorLabel.setIcon( new ImageIcon(img) );
        saveErrorLabel.setVisible(false);

        filterComboBox.setRenderer(new FilterCellRenderer());
        validate();
        repaint();
    }

    void setQueryRunning(boolean running) {
        modifyButton.setEnabled(!running);
        refreshButton.setEnabled(!running);
        seenButton.setEnabled(!running);
        filterLabel.setEnabled(!running);
        filterComboBox.setEnabled(!running);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        byLastChangePanel = new javax.swing.JPanel();
        changedLabel = new javax.swing.JLabel();
        changedAndLabel = new javax.swing.JLabel();
        changedHintLabel = new javax.swing.JLabel();
        changedWhereLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        changedBlaBlaLabel = new javax.swing.JLabel();
        byPeoplePanel = new javax.swing.JPanel();
        byTextPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tableFieldsPanel = new javax.swing.JPanel();
        tableHeaderPanel = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        criteriaPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        queryHeaderPanel = new javax.swing.JPanel();
        lastRefreshLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        noContentContainer = new javax.swing.JPanel();

        byLastChangePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        changedLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedLabel.text_1")); // NOI18N

        changedFromTextField.setColumns(8);
        changedFromTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedFromTextField.text")); // NOI18N

        changedAndLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedAndLabel.text")); // NOI18N

        changedToTextField.setColumns(8);
        changedToTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedToTextField.text")); // NOI18N

        changedHintLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground"));
        changedHintLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedHintLabel.text")); // NOI18N

        changedWhereLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedWhereLabel.text")); // NOI18N

        jScrollPane1.setViewportView(changedList);

        changedBlaBlaLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.changedBlaBlaLabel.text")); // NOI18N

        newValueTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.newValueTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout byLastChangePanelLayout = new org.jdesktop.layout.GroupLayout(byLastChangePanel);
        byLastChangePanel.setLayout(byLastChangePanelLayout);
        byLastChangePanelLayout.setHorizontalGroup(
            byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(changedWhereLabel)
                    .add(changedLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(changedHintLabel)
                    .add(byLastChangePanelLayout.createSequentialGroup()
                        .add(changedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedAndLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(byLastChangePanelLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 179, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(changedBlaBlaLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byLastChangePanelLayout.setVerticalGroup(
            byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byLastChangePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(changedLabel)
                    .add(changedAndLabel)
                    .add(changedToTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(changedFromTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(4, 4, 4)
                .add(changedHintLabel)
                .add(18, 18, 18)
                .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(changedWhereLabel)
                    .add(byLastChangePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(changedBlaBlaLabel)
                        .add(newValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        byPeoplePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        peopleLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleLabel.text")); // NOI18N

        bugAssigneeCheckBox.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.bugAssigneeCheckBox.text")); // NOI18N

        reporterCheckBox.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.reporterCheckBox.text")); // NOI18N

        ccCheckBox.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.ccCheckBox.text")); // NOI18N

        commenterCheckBox.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commenterCheckBox.text")); // NOI18N

        peopleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        peopleTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.peopleTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout byPeoplePanelLayout = new org.jdesktop.layout.GroupLayout(byPeoplePanel);
        byPeoplePanel.setLayout(byPeoplePanelLayout);
        byPeoplePanelLayout.setHorizontalGroup(
            byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(peopleLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commenterCheckBox)
                    .add(ccCheckBox)
                    .add(byPeoplePanelLayout.createSequentialGroup()
                        .add(bugAssigneeCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(peopleComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(peopleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 163, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(reporterCheckBox))
                .addContainerGap(121, Short.MAX_VALUE))
        );
        byPeoplePanelLayout.setVerticalGroup(
            byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byPeoplePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(peopleComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(peopleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(byPeoplePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bugAssigneeCheckBox)
                        .add(peopleLabel)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(reporterCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ccCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commenterCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        productLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        productLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.productLabel.text")); // NOI18N

        versionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        versionLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.versionLabel.text")); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        versionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        versionList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane2.setViewportView(versionList);

        statusLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        statusLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.statusLabel.text")); // NOI18N

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        statusList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statusList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane3.setViewportView(statusList);

        resolutionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.resolutionLabel.text")); // NOI18N

        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        priorityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        priorityList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane4.setViewportView(priorityList);

        priorityLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        priorityLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.priorityLabel.text")); // NOI18N

        jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        resolutionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        resolutionList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane5.setViewportView(resolutionList);

        componentLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        componentLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.componentLabel.text")); // NOI18N

        jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane6.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane6.setViewportView(componentList);

        jScrollPane7.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane7.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        productList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        productList.setMaximumSize(new java.awt.Dimension(100, 2));
        jScrollPane7.setViewportView(productList);

        severityLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        severityLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.severityLabel.text")); // NOI18N

        jScrollPane8.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane8.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        severityList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        severityList.setMinimumSize(new java.awt.Dimension(100, 2));
        jScrollPane8.setViewportView(severityList);

        org.jdesktop.layout.GroupLayout byDetailsPanelLayout = new org.jdesktop.layout.GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byDetailsPanelLayout.createSequentialGroup()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .add(22, 22, 22)
                        .add(productLabel))
                    .add(byDetailsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(componentLabel)
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(versionLabel)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(34, 34, 34)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(statusLabel)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resolutionLabel)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(priorityLabel)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(severityLabel)
                    .add(jScrollPane8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(37, 37, 37))
        );
        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, byDetailsPanelLayout.createSequentialGroup()
                            .add(severityLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane8))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, byDetailsPanelLayout.createSequentialGroup()
                            .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(priorityLabel)
                                .add(resolutionLabel)
                                .add(statusLabel))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jScrollPane4)
                                .add(jScrollPane5)
                                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))))
                    .add(byDetailsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(byDetailsPanelLayout.createSequentialGroup()
                            .add(componentLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane6))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, byDetailsPanelLayout.createSequentialGroup()
                            .add(versionLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane2))
                        .add(byDetailsPanelLayout.createSequentialGroup()
                            .add(productLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryLabel.text_1")); // NOI18N

        commentLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentLabel.text")); // NOI18N

        keywordsLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsLabel.text")); // NOI18N

        summaryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        commentComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        keywordsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        summaryTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.summaryTextField.text")); // NOI18N

        commentTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.commentTextField.text")); // NOI18N

        keywordsTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsTextField.text")); // NOI18N

        keywordsButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.keywordsButton.text")); // NOI18N
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout byTextPanelLayout = new org.jdesktop.layout.GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryLabel)
                    .add(commentLabel)
                    .add(keywordsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(summaryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(commentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(keywordsTextField)
                    .add(commentTextField)
                    .add(summaryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keywordsButton)
                .addContainerGap(38, Short.MAX_VALUE))
        );
        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commentLabel)
                    .add(commentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(commentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(byTextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keywordsLabel)
                    .add(keywordsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel3.text")); // NOI18N

        queryNameTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.queryNameTextField.text")); // NOI18N

        saveErrorLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveErrorLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout savePanelLayout = new org.jdesktop.layout.GroupLayout(savePanel);
        savePanel.setLayout(savePanelLayout);
        savePanelLayout.setHorizontalGroup(
            savePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(saveErrorLabel)
            .add(savePanelLayout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        savePanelLayout.setVerticalGroup(
            savePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(savePanelLayout.createSequentialGroup()
                .add(saveErrorLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(savePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(queryNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        tablePanel.setMinimumSize(new java.awt.Dimension(100, 350));
        tablePanel.setLayout(new java.awt.BorderLayout());

        tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        tableSummaryLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.tableSummaryLabel.text_1")); // NOI18N

        filterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        filterLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.filterLabel.text_1")); // NOI18N

        org.jdesktop.layout.GroupLayout tableHeaderPanelLayout = new org.jdesktop.layout.GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableHeaderPanelLayout.createSequentialGroup()
                .add(tableSummaryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 603, Short.MAX_VALUE)
                .add(filterLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(filterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        tableHeaderPanelLayout.setVerticalGroup(
            tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(tableSummaryLabel)
                .add(filterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(filterLabel))
        );

        org.jdesktop.layout.GroupLayout tableFieldsPanelLayout = new org.jdesktop.layout.GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, tableFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tableHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        tableFieldsPanelLayout.setVerticalGroup(
            tableFieldsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableFieldsPanelLayout.createSequentialGroup()
                .add(tableHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tablePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        webButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.webButton.text")); // NOI18N
        webButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButtonActionPerformed(evt);
            }
        });

        urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl")); // NOI18N
        urlToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlToggleButtonActionPerformed(evt);
            }
        });

        searchButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.searchButton.text")); // NOI18N

        criteriaPanel.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.disabledText")));

        byTextLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        byTextLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byTextLabel.text_1")); // NOI18N

        byTextContainer.setLayout(new java.awt.BorderLayout());

        byDetailsContainer.setLayout(new java.awt.BorderLayout());

        byDetailsLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        byDetailsLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byDetailsLabel.text")); // NOI18N

        byPeopleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        byPeopleLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byPeopleLabel.text")); // NOI18N

        byPeopleContainer.setLayout(new java.awt.BorderLayout());

        byLastChangeLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        byLastChangeLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.byLastChangeLabel.text")); // NOI18N

        byLastChangeContainer.setLayout(new java.awt.BorderLayout());

        jLabel2.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel2.text_1")); // NOI18N

        urlTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout urlPanelLayout = new org.jdesktop.layout.GroupLayout(urlPanel);
        urlPanel.setLayout(urlPanelLayout);
        urlPanelLayout.setHorizontalGroup(
            urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(urlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(urlTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                .addContainerGap())
        );
        urlPanelLayout.setVerticalGroup(
            urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(urlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(urlTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel2))
        );

        org.jdesktop.layout.GroupLayout criteriaPanelLayout = new org.jdesktop.layout.GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byTextLabel)
                .addContainerGap())
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byLastChangeLabel)
                .addContainerGap())
            .add(byTextContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
            .add(byLastChangeContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byDetailsLabel)
                .addContainerGap())
            .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byPeopleLabel)
                .addContainerGap())
            .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
            .add(urlPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(criteriaPanelLayout.createSequentialGroup()
                .add(byTextLabel)
                .add(0, 0, 0)
                .add(byTextContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(0, 0, 0)
                .add(byDetailsLabel)
                .add(0, 0, 0)
                .add(byDetailsContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byPeopleLabel)
                .add(0, 0, 0)
                .add(byPeopleContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(byLastChangeLabel)
                .add(0, 0, 0)
                .add(byLastChangeContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(urlPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        cancelChangesButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.cancelChangesButton.text")); // NOI18N

        saveChangesButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveChangesButton.text")); // NOI18N

        saveButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel1.text_1")); // NOI18N

        idTextField.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.idTextField.text")); // NOI18N

        gotoIssueButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.gotoIssueButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout gotoPanelLayout = new org.jdesktop.layout.GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(gotoIssueButton)
                .addContainerGap(417, Short.MAX_VALUE))
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(gotoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(gotoIssueButton)
                .add(jLabel1))
            .add(idTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jLabel7.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel7.text")); // NOI18N
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel8.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel8.text")); // NOI18N
        jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout searchPanelLayout = new org.jdesktop.layout.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchPanelLayout.createSequentialGroup()
                        .add(searchButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(saveChangesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cancelChangesButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 96, Short.MAX_VALUE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel7)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel8)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(urlToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(criteriaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .add(gotoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanelLayout.createSequentialGroup()
                .add(gotoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(criteriaPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(searchButton)
                        .add(cancelChangesButton)
                        .add(saveChangesButton))
                    .add(searchPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(urlToggleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(webButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel7)
                        .add(jLabel8)))
                .addContainerGap())
        );

        searchPanelLayout.linkSize(new java.awt.Component[] {jLabel7, jLabel8, saveButton, urlToggleButton, webButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        refreshButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        lastRefreshLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshLabel.text_1")); // NOI18N

        removeButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        lastRefreshDateLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.lastRefreshDateLabel.text_1")); // NOI18N

        seenButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.seenButton.text")); // NOI18N
        seenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seenButtonActionPerformed(evt);
            }
        });

        nameLabel.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.nameLabel.text_1")); // NOI18N

        modifyButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.modifyButton.text")); // NOI18N
        modifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyButtonActionPerformed(evt);
            }
        });

        jLabel4.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel4.text_1")); // NOI18N
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel5.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.jdesktop.layout.GroupLayout queryHeaderPanelLayout = new org.jdesktop.layout.GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(
            queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(nameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 627, Short.MAX_VALUE)
                        .add(lastRefreshLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lastRefreshDateLabel))
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(modifyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(7, 7, 7)
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(5, 5, 5)
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        queryHeaderPanelLayout.setVerticalGroup(
            queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(queryHeaderPanelLayout.createSequentialGroup()
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(nameLabel))
                    .add(queryHeaderPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lastRefreshDateLabel)
                            .add(lastRefreshLabel))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 10, Short.MAX_VALUE)
                .add(queryHeaderPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6)
                    .add(modifyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(seenButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        queryHeaderPanelLayout.linkSize(new java.awt.Component[] {jLabel4, jLabel5, jLabel6, modifyButton, removeButton, seenButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        noContentContainer.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        noContentContainer.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(searchPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(queryHeaderPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(noContentContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 820, Short.MAX_VALUE)
            .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(queryHeaderPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(searchPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tableFieldsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noContentContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_refreshButtonActionPerformed

    private void modifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_modifyButtonActionPerformed

    private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_seenButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_removeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_saveButtonActionPerformed

    private void webButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_webButtonActionPerformed

    private void urlToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlToggleButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_urlToggleButtonActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keywordsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JCheckBox bugAssigneeCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
    final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
    final javax.swing.JPanel byLastChangeContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byLastChangeLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byLastChangePanel;
    final javax.swing.JPanel byPeopleContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byPeopleLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byPeoplePanel;
    final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
    final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
    private javax.swing.JPanel byTextPanel;
    final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
    final javax.swing.JCheckBox ccCheckBox = new javax.swing.JCheckBox();
    private javax.swing.JLabel changedAndLabel;
    private javax.swing.JLabel changedBlaBlaLabel;
    final javax.swing.JTextField changedFromTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedHintLabel;
    private javax.swing.JLabel changedLabel;
    final javax.swing.JList changedList = new javax.swing.JList();
    final javax.swing.JTextField changedToTextField = new javax.swing.JTextField();
    private javax.swing.JLabel changedWhereLabel;
    final javax.swing.JComboBox commentComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel commentLabel = new javax.swing.JLabel();
    final javax.swing.JTextField commentTextField = new javax.swing.JTextField();
    final javax.swing.JCheckBox commenterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel componentLabel = new javax.swing.JLabel();
    final javax.swing.JList componentList = new javax.swing.JList();
    private javax.swing.JPanel criteriaPanel;
    final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
    private javax.swing.JLabel filterLabel;
    final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
    final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
    final javax.swing.JTextField idTextField = new javax.swing.JTextField();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane4 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane5 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane6 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane7 = new javax.swing.JScrollPane();
    final javax.swing.JScrollPane jScrollPane8 = new javax.swing.JScrollPane();
    final javax.swing.JButton keywordsButton = new javax.swing.JButton();
    final javax.swing.JComboBox keywordsComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel keywordsLabel = new javax.swing.JLabel();
    final javax.swing.JTextField keywordsTextField = new javax.swing.JTextField();
    final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
    private javax.swing.JLabel lastRefreshLabel;
    public final org.netbeans.modules.bugtracking.util.LinkButton modifyButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
    final javax.swing.JTextField newValueTextField = new javax.swing.JTextField();
    private javax.swing.JPanel noContentContainer;
    final javax.swing.JComboBox peopleComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel peopleLabel = new javax.swing.JLabel();
    final javax.swing.JTextField peopleTextField = new javax.swing.JTextField();
    final javax.swing.JLabel priorityLabel = new javax.swing.JLabel();
    final javax.swing.JList priorityList = new javax.swing.JList();
    final javax.swing.JLabel productLabel = new javax.swing.JLabel();
    final javax.swing.JList productList = new javax.swing.JList();
    private javax.swing.JPanel queryHeaderPanel;
    final javax.swing.JTextField queryNameTextField = new javax.swing.JTextField();
    final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    public final org.netbeans.modules.bugtracking.util.LinkButton removeButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JCheckBox reporterCheckBox = new javax.swing.JCheckBox();
    final javax.swing.JLabel resolutionLabel = new javax.swing.JLabel();
    final javax.swing.JList resolutionList = new javax.swing.JList();
    final org.netbeans.modules.bugtracking.util.LinkButton saveButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
    final javax.swing.JLabel saveErrorLabel = new javax.swing.JLabel();
    final javax.swing.JPanel savePanel = new javax.swing.JPanel();
    final javax.swing.JButton searchButton = new javax.swing.JButton();
    final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
    final org.netbeans.modules.bugtracking.util.LinkButton seenButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel severityLabel = new javax.swing.JLabel();
    final javax.swing.JList severityList = new javax.swing.JList();
    final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
    final javax.swing.JList statusList = new javax.swing.JList();
    final javax.swing.JComboBox summaryComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel summaryLabel = new javax.swing.JLabel();
    final javax.swing.JTextField summaryTextField = new javax.swing.JTextField();
    private javax.swing.JPanel tableFieldsPanel;
    private javax.swing.JPanel tableHeaderPanel;
    final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
    final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
    final javax.swing.JPanel urlPanel = new javax.swing.JPanel();
    final javax.swing.JTextField urlTextField = new javax.swing.JTextField();
    final org.netbeans.modules.bugtracking.util.LinkButton urlToggleButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JLabel versionLabel = new javax.swing.JLabel();
    final javax.swing.JList versionList = new javax.swing.JList();
    final org.netbeans.modules.bugtracking.util.LinkButton webButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    // End of variables declaration//GEN-END:variables

    /**
     * enables/disables all but the parameter fields
     * @param enable
     */
    void enableFields(boolean enable) {
//        summaryComboBox.setEnabled(enable);
//        commentComboBox.setEnabled(enable);
//        keywordsComboBox.setEnabled(enable);
//        summaryTextField.setEnabled(enable);
//        commentTextField.setEnabled(enable);
//        keywordsTextField.setEnabled(enable);
        summaryLabel.setEnabled(enable);
        commentLabel.setEnabled(enable);
        keywordsLabel.setEnabled(enable);

        productLabel.setEnabled(enable);
//        productList.setEnabled(enable);
        componentLabel.setEnabled(enable);
//        componentList.setEnabled(enable);
        versionLabel.setEnabled(enable);
//        versionList.setEnabled(enable);
        statusLabel.setEnabled(enable);
        severityLabel.setEnabled(enable);
//        statusList.setEnabled(enable);
        resolutionLabel.setEnabled(enable);
//        resolutionList.setEnabled(enable);
        priorityLabel.setEnabled(enable);
//        priorityList.setEnabled(enable);

        peopleLabel.setEnabled(enable);
//        peopleComboBox.setEnabled(enable);
        peopleTextField.setEnabled(enable);
//        bugAssigneeCheckBox.setEnabled(enable);
//        reporterCheckBox.setEnabled(enable);
//        ccCheckBox.setEnabled(enable);
//        commenterCheckBox.setEnabled(enable);

        searchButton.setEnabled(enable);
        saveButton.setEnabled(enable);
        webButton.setEnabled(enable);
        urlToggleButton.setEnabled(enable);

        changedLabel.setEnabled(enable);
//        changedFromTextField.setEnabled(enable);
        changedAndLabel.setEnabled(enable);
//        changedToTextField.setEnabled(enable);
        changedWhereLabel.setEnabled(enable);
//        changedList.setEnabled(enable);
        changedBlaBlaLabel.setEnabled(enable);
        changedHintLabel.setEnabled(enable);
//        newValueTextField.setEnabled(enable);
    }

    void switchQueryFields(boolean showAdvanced) {
        byDetails.setVisible(showAdvanced);
        byText.setVisible(showAdvanced);
        byLastChange.setVisible(showAdvanced);
        byPeople.setVisible(showAdvanced);

        urlPanel.setVisible(!showAdvanced);
        if(showAdvanced) {
            urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textUrl"));
        } else {
            urlToggleButton.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "QueryPanel.urlToggleButton.textForm"));
        }
    }


    void showSearchingProgress(boolean on, JComponent progressBar, String text) {
//        searchPanel.setVisible(!on);
        noContentContainer.setVisible(on);
        tableSummaryLabel.setVisible(!on);
        tableFieldsPanel.setVisible(!on);
        if(on && text != null) {
            noContentPanel.setText(text);
        }
        noContentPanel.setProgressComponent(on ? progressBar : null);
    }

    void showRetrievingProgress(boolean on, JComponent progressBar, String text, boolean searchPanelVisible) {
        noContentContainer.setVisible(on);
//        tableSummaryLabel.setVisible(!on);
        if(searchPanelVisible) {
            searchPanel.setVisible(!on);
        }
        if(on && text != null) {
            noContentPanel.setText(text);
        }
        noContentPanel.setProgressComponent(on ? progressBar : null);
    }

    void showNoContentPanel(boolean on) {
        showSearchingProgress(on, null, null);
    }

    void setModifyVisible(boolean b) {
        searchPanel.setVisible(b);
        cancelChangesButton.setVisible(b);
        saveChangesButton.setVisible(b);

        tableFieldsPanel.setVisible(!b);
        searchButton.setVisible(!b);
        saveButton.setVisible(!b);
        webButton.setVisible(!b);
        urlToggleButton.setVisible(!b);
    }

    void setSaved(String name, String lastRefresh) {
        searchPanel.setVisible(false);
        queryHeaderPanel.setVisible(true);
        tableHeaderPanel.setVisible(true);
        filterComboBox.setVisible(true); // XXX move to bugtracking IssueTable component
        filterLabel.setVisible(true);
        tablePanel.setVisible(true);
        nameLabel.setText(name);
        setLastRefresh(lastRefresh);
    }

    void setLastRefresh(String lastRefresh) {
        lastRefreshDateLabel.setText(lastRefresh);
    }

    class ExpandablePanel {
        private final JPanel panel;
        private final JLabel label;
        private final Icon ei;
        private final Icon ci;
        private boolean expaned = true;
        public ExpandablePanel(JLabel l, JPanel p, final Icon ei, final Icon ci) {
            this.panel = p;
            this.label = l;
            this.ci = ci;
            this.ei = ei;
            this.label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(panel.isVisible()) {
                        colapse();
                    } else {
                        expand();
                    }
                }
            });
        }
        public void expand() {
            expaned = true;
            panel.setVisible(true);
            label.setIcon(ei);
        }
        public void colapse() {
            expaned = false;
            panel.setVisible(false);
            label.setIcon(ci);
        }
        public void setVisible(boolean visible ) {
            label.setVisible(visible);
            panel.setVisible(visible && expaned);
        }
    }

    private static class FilterCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof Query.Filter) {
                value = ((Query.Filter)value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
