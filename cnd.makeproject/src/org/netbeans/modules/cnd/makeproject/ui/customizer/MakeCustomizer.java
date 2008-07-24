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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import org.netbeans.modules.cnd.makeproject.configurations.ui.ProjectPropPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class MakeCustomizer extends javax.swing.JPanel implements HelpCtx.Provider {
    
    private Component currentCustomizer;
    private PropertyNode currentConfigurationNode = null;
    private Node previousNode;
    
    private GridBagConstraints fillConstraints;
    
    private Project project;
    
    private MakeCustomizer makeCustomizer;
    
    private DialogDescriptor dialogDescriptor;
    
    private ConfigurationDescriptor projectDescriptor;
    private Item item;
    private Folder folder;
    private Vector controls;
    private CategoryView currentCategoryView;
    private String currentNodeName;
    private Configuration[] configurationItems;
    private Configuration[] selectedConfigurations;
    private int lastComboboxIndex = -1;
    
    /** Creates new form MakeCustomizer */
    public MakeCustomizer(Project project, String preselectedNodeName, ConfigurationDescriptor projectDescriptor, Item item, Folder folder, Vector controls) {
        initComponents();
        this.projectDescriptor = projectDescriptor;
        this.controls = controls;
        this.project = project;
        this.makeCustomizer = this;
        this.item = item;
        this.folder = folder;
        controls.add(configurationComboBox);
        controls.add(configurationsButton);
        
        configurationItems = projectDescriptor.getConfs().getConfs();
        for (int i = 0; i < configurationItems.length; i++)
            configurationComboBox.addItem(configurationItems[i]);
        if (configurationItems.length > 1)
            configurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        if (configurationItems.length > 2)
            configurationComboBox.addItem(getString("MULTIPLE_CONFIGURATIONS"));
        // Select default configuraton
        int selectedIndex = projectDescriptor.getConfs().getActiveAsIndex();
        if (selectedIndex < 0)
            selectedIndex = 0;
        configurationComboBox.setSelectedIndex(selectedIndex);
        calculateSelectedConfs();
        
        HelpCtx.setHelpIDString( customizerPanel, "org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer" ); // NOI18N
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_MakeCustomizer")); // NOI18N
        fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, item, folder), preselectedNodeName );
        currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class,"AN_BeanTreeViewCategories")); // NOI18N
        currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_BeanTreeViewCategories")); // NOI18N
        categoryPanel.add( currentCategoryView, fillConstraints );
        
        // Accessibility
        configurationsButton.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_BUTTON_AD"));
        configurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATION_COMBOBOX_AD"));
        
        allConfigurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        allConfigurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_BUTTON_AD"));
        allConfigurationComboBox.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATION_COMBOBOX_AD"));
    }
    
    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        categoryLabel = new javax.swing.JLabel();
        categoryPanel = new javax.swing.JPanel();
        propertyPanel = new javax.swing.JPanel();
        configurationPanel = new javax.swing.JPanel();
        configurationLabel = new javax.swing.JLabel();
        configurationComboBox = new javax.swing.JComboBox();
        allConfigurationComboBox = new javax.swing.JComboBox();
        configurationsButton = new javax.swing.JButton();
        customizerPanel = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(800, 500));
        setLayout(new java.awt.GridBagLayout());

        categoryLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CATEGORIES_LABEL_MN").charAt(0));
        categoryLabel.setLabelFor(categoryPanel);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle"); // NOI18N
        categoryLabel.setText(bundle.getString("CATEGORIES_LABEL_TXT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(categoryLabel, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setMinimumSize(new java.awt.Dimension(220, 4));
        categoryPanel.setPreferredSize(new java.awt.Dimension(220, 4));
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(categoryPanel, gridBagConstraints);
        categoryPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer_categoryPanel")); // NOI18N
        categoryPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer_categoryPanel")); // NOI18N

        propertyPanel.setLayout(new java.awt.GridBagLayout());

        configurationPanel.setLayout(new java.awt.GridBagLayout());

        configurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATION_COMBOBOX_MNE").charAt(0));
        configurationLabel.setLabelFor(configurationComboBox);
        configurationLabel.setText(bundle.getString("CONFIGURATION_COMBOBOX_LBL")); // NOI18N
        configurationPanel.add(configurationLabel, new java.awt.GridBagConstraints());

        configurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboBoxActionPerformed(evt);
            }
        });
        configurationPanel.add(configurationComboBox, new java.awt.GridBagConstraints());
        configurationPanel.add(allConfigurationComboBox, new java.awt.GridBagConstraints());

        configurationsButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATIONS_BUTTON_MNE").charAt(0));
        configurationsButton.setText(bundle.getString("CONFIGURATIONS_BUTTON_LBL")); // NOI18N
        configurationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        configurationPanel.add(configurationsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        propertyPanel.add(configurationPanel, gridBagConstraints);

        customizerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        propertyPanel.add(customizerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 12);
        add(propertyPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void configurationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationsButtonActionPerformed
        MyListEditorPanel configurationsEditor = new MyListEditorPanel(projectDescriptor.getConfs().getConfs());
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        outerPanel.add(configurationsEditor, gridBagConstraints);
        
        Object[] options = new Object[] {NotifyDescriptor.OK_OPTION};
        DialogDescriptor dd = new DialogDescriptor(outerPanel, getString("CONFIGURATIONS_EDITOR_TITLE"), true, options, NotifyDescriptor.OK_OPTION, 0, null, null);
        
        DialogDisplayer dialogDisplayer = DialogDisplayer.getDefault();
        java.awt.Dialog dl = dialogDisplayer.createDialog(dd);
        //dl.setPreferredSize(new java.awt.Dimension(400, (int)dl.getPreferredSize().getHeight()));
        dl.getAccessibleContext().setAccessibleDescription(getString("CONFIGURATIONS_EDITOR_AD"));
        dl.pack();
        dl.setSize(new java.awt.Dimension(400, (int)dl.getPreferredSize().getHeight()));
        dl.setVisible(true);
        // Update data structure
        Configuration[] editedConfs = (Configuration[])configurationsEditor.getListData().toArray(new Configuration[configurationsEditor.getListData().size()]);
        projectDescriptor.getConfs().init(editedConfs, -1);
        // Update gui with changes
        ActionListener[] actionListeners = configurationComboBox.getActionListeners();
        configurationComboBox.removeActionListener(actionListeners[0]); // assuming one and only one!
        configurationComboBox.removeAllItems();
        configurationComboBox.addActionListener(actionListeners[0]); // assuming one and only one!
        configurationItems = projectDescriptor.getConfs().getConfs();
        for (int i = 0; i < configurationItems.length; i++)
            configurationComboBox.addItem(configurationItems[i]);
        if (configurationItems.length > 1)
            configurationComboBox.addItem(getString("ALL_CONFIGURATIONS"));
        if (configurationItems.length > 2)
            configurationComboBox.addItem(getString("MULTIPLE_CONFIGURATIONS"));
        configurationComboBox.setSelectedIndex(configurationsEditor.getSelectedIndex());
        calculateSelectedConfs();
    }//GEN-LAST:event_configurationsButtonActionPerformed
    
    private void configurationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboBoxActionPerformed
        calculateSelectedConfs();
        refresh();
    }//GEN-LAST:event_configurationComboBoxActionPerformed
    
    public void refresh() {
        if (currentCategoryView != null) {
            String selectedNodeName = currentNodeName;
            categoryPanel.remove(currentCategoryView);
            currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, item, folder), null );
            currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class,"AN_BeanTreeViewCategories"));
            currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_BeanTreeViewCategories"));
            categoryPanel.add(currentCategoryView, fillConstraints );
            if (selectedNodeName != null)
                currentCategoryView.selectNode(selectedNodeName);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox allConfigurationComboBox;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JComboBox configurationComboBox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JButton configurationsButton;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JPanel propertyPanel;
    // End of variables declaration//GEN-END:variables
    
    // HelpCtx.Provider implementation -----------------------------------------
    
    public HelpCtx getHelpCtx() {
        if ( currentConfigurationNode != null ) {
            return HelpCtx.findHelp( currentConfigurationNode );
        } else {
            return null;
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private class CategoryView extends JPanel implements ExplorerManager.Provider {
        
        private ExplorerManager manager;
        private BeanTreeView btv;
        private String preselectedNodeName;
        
        CategoryView( Node rootNode, String preselectedNodeName ) {
            this.preselectedNodeName = preselectedNodeName;
            // See #36315
            manager = new ExplorerManager();
            
            setLayout( new BorderLayout() );
            
            Dimension size = new Dimension( 220, 4 );
            btv = new BeanTreeView();    // Add the BeanTreeView
            btv.setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION  );
            btv.setPopupAllowed( false );
            btv.setRootVisible( true );
            btv.setDefaultActionAllowed( false );
            btv.setMinimumSize( size );
            btv.setPreferredSize( size );
            btv.setMaximumSize( size );
            btv.setDragSource(false);
            btv.setRootVisible(false);
            btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class,"AN_BeanTreeViewCategories"));
            btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_BeanTreeViewCategories"));
            this.add( btv, BorderLayout.CENTER );
            manager.setRootContext( rootNode );
            ManagerChangeListener managerChangeListener = new ManagerChangeListener();
            manager.addPropertyChangeListener(managerChangeListener);
            selectNode( preselectedNodeName );
            //btv.expandAll();
            //expandCollapseTree(rootNode, btv);
            
            // Add been tree view to controls so it can be enabled/disabled correctly
            controls.add(btv);
        }
        
        private void expandCollapseTree(Node rootNode, BeanTreeView btv) {
            Children children = rootNode.getChildren();
            Node[] nodes1 = children.getNodes();
            for (int i = 0; i < nodes1.length; i++) {
                if (nodes1[i].getName().equals("Build")) // NOI18N
                    btv.expandNode(nodes1[i]);
                else 
                    btv.collapseNode(nodes1[i]);
            }
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            //btv.expandAll();
            expandCollapseTree(manager.getRootContext(), btv);
            if (preselectedNodeName != null && preselectedNodeName.length() > 0) {
                selectNode( preselectedNodeName );
            }
        }
        
        private Node findNode(Node pnode, String name) {
            // First try all children of this node
            Node node = NodeOp.findChild(pnode, name);
            if (node != null)
                return node;
            // Then try it's children
            Children ch = pnode.getChildren();
            Node nodes[] = ch.getNodes(true);
            for (int i = 0; i < nodes.length; i++) {
                Node cnode = findNode(nodes[i], name);
                if (cnode != null)
                    return cnode;
            }
            
            return null;
        }
        
        private void selectNode(String name) {
            Node node = null;
            if (name != null)
                node = findNode(manager.getRootContext(), name);
            if (node == null)
                node = (manager.getRootContext().getChildren().getNodes()[0]);
            if (node != null) {
                try {
                    manager.setSelectedNodes(new Node[] {node});
                } catch (Exception e) {
                }
            }
        }
        
        
        
        /** Listens to selection change and shows the customizers as
         *  panels
         */
        
        private class ManagerChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getSource() != manager) {
                    return;
                }
                
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    Node nodes[] = manager.getSelectedNodes();
                    if ( nodes == null || nodes.length <= 0 ) {
                        return;
                    }
                    Node node = nodes[0];
                    currentNodeName = node.getName();
                    
                    if ( currentCustomizer != null ) {
                        customizerPanel.remove( currentCustomizer );
                    }
                    JPanel panel = new JPanel();
                    panel.setLayout(new java.awt.GridBagLayout());
                    currentConfigurationNode = (PropertyNode)node;
                    if (currentConfigurationNode.custumizerStyle() == CustomizerNode.CustomizerStyle.PANEL) {
                        panel.add(currentConfigurationNode.getPanel(project, projectDescriptor), fillConstraints);
                        configurationLabel.setEnabled(false);
                        configurationComboBox.setEnabled(false);
                        configurationsButton.setEnabled(true);
                        configurationComboBox.setVisible(false);
                        allConfigurationComboBox.setVisible(true);
                        allConfigurationComboBox.setEnabled(false);
                    }
                    else if (currentConfigurationNode.custumizerStyle() == CustomizerNode.CustomizerStyle.SHEET) {
                        panel.setBorder(new javax.swing.border.EtchedBorder());
                        PropertySheet propertySheet = new PropertySheet(); // See IZ 105525 for details.
                        DummyNode[] dummyNodes = new DummyNode[selectedConfigurations.length];
                        for (int i = 0; i < selectedConfigurations.length; i++) {
                            dummyNodes[i] = new DummyNode(currentConfigurationNode.getSheet(project, projectDescriptor, selectedConfigurations[i]), selectedConfigurations[i].getName());
                        }
                        propertySheet.setNodes(dummyNodes);
                        panel.add(propertySheet, fillConstraints);
                        configurationLabel.setEnabled(true);
                        configurationComboBox.setEnabled(true);
                        configurationsButton.setEnabled(true);
                        configurationComboBox.setVisible(true);
                        allConfigurationComboBox.setVisible(false);
                    }
                    else {
                        configurationLabel.setEnabled(false);
                        configurationComboBox.setEnabled(false);
                        configurationsButton.setEnabled(false);
                        configurationComboBox.setVisible(true);
                        allConfigurationComboBox.setVisible(false);
                    }
                    customizerPanel.add(panel, fillConstraints );
                    customizerPanel.validate();
                    customizerPanel.repaint();
                    currentCustomizer = panel;
                    
                    IpeUtils.requestFocus(btv);
                    
                    if (dialogDescriptor != null && currentConfigurationNode != null) {
                        dialogDescriptor.setHelpCtx(HelpCtx.findHelp(currentConfigurationNode));
                    }
                    return;
                }
            }
        }
    }
        
    private void calculateSelectedConfs() {
        if (configurationComboBox.getSelectedIndex() < configurationItems.length) {
            // One selected
            selectedConfigurations = new Configuration[] {(MakeConfiguration)configurationComboBox.getSelectedItem()};
            lastComboboxIndex = configurationComboBox.getSelectedIndex();
        } else if (configurationComboBox.getSelectedIndex() == configurationItems.length) {
            // All selected
            selectedConfigurations = configurationItems;
            lastComboboxIndex = configurationComboBox.getSelectedIndex();
        } else {
            // Some Selected
            while (true) {
                ConfSelectorPanel confSelectorPanel = new ConfSelectorPanel(getString("SELECTED_CONFIGURATIONS_LBL"), 'v', configurationItems, null);
                DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, getString("MULTIPLE_CONFIGURATIONS_TITLE"));
                DialogDisplayer.getDefault().notify(dd);
                if (dd.getValue() != DialogDescriptor.OK_OPTION) {
                    if (lastComboboxIndex <= configurationItems.length) {
                        configurationComboBox.setSelectedIndex(lastComboboxIndex);
                    }
                    break;
                }
                if (confSelectorPanel.getSelectedConfs().length > 1) {
                    selectedConfigurations = confSelectorPanel.getSelectedConfs();
                    lastComboboxIndex = configurationComboBox.getSelectedIndex();
                    break;
                } else {
                    String errormsg = getString("SELECT_MORE");
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }
    }
    
    
    // Private methods ---------------------------------------------------------
    
    private Node createRootNode(Project project, ConfigurationDescriptor projectDescriptor, Item item, Folder folder) {
        if (item != null)
            return createRootNodeItem(project, item);
        else if (folder != null)
            return createRootNodeFolder(project, folder);
        else
            return createRootNodeProject(project, projectDescriptor);
        }
    
    private Node createRootNodeProject(Project project, ConfigurationDescriptor projectDescriptor) {
        boolean includeMakefileDescription = true;
        boolean includeNewDescription = true;
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        boolean includeLinkerDescription = true;
        boolean includeArchiveDescription = true;
        boolean includeRunDebugDescriptions = true;
        
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)selectedConfigurations[i];
            
            if (compilerSet >= 0 && makeConfiguration.getCompilerSet().getValue() != compilerSet)
                includeNewDescription = false;
            compilerSet = makeConfiguration.getCompilerSet().getValue();
            
            if ((isCompileConfiguration && !makeConfiguration.isCompileConfiguration()) || (!isCompileConfiguration && makeConfiguration.isCompileConfiguration()))
                includeNewDescription = false;
            
            if (makeConfiguration.isMakefileConfiguration()) {
                //includeNewDescription = false;
                includeLinkerDescription = false;
                includeArchiveDescription = false;
            }
            if (makeConfiguration.isLinkerConfiguration()) {
                includeMakefileDescription = false;
                includeArchiveDescription = false;
            }
            if (makeConfiguration.isArchiverConfiguration()) {
                includeMakefileDescription = false;
                includeLinkerDescription = false;
            }
            if (makeConfiguration.isLibraryConfiguration()) {
                includeRunDebugDescriptions = false;
            }
        }
        
        Vector descriptions = new Vector();
        descriptions.add(createGeneralDescription(project));
        descriptions.add(createBuildDescription(project));
        // Add customizer nodes
        if (includeRunDebugDescriptions) {
            if (!descriptions.addAll(CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Run"))) { // NOI18N
                descriptions.add(createNotFoundNode("Run")); // NOI18N
            }
            if (!descriptions.addAll(CustomizerRootNodeProvider.getInstance().getCustomizerNodes("Debug"))) { // NOI18N
                descriptions.add(createNotFoundNode("Debug")); // NOI18N
            }
    //      descriptions.addAll(CustomizerRootNodeProvider.getInstance().getCustomizerNodes(false));
            CustomizerNode advanced = getAdvancedCutomizerNode(descriptions);
            if (advanced != null)
                descriptions.add(advanced);
        }
        if (includeMakefileDescription) {
            //descriptions.add(createMakefileDescription(project));
            descriptions.add(createRequiredProjectsDescription(project));
            descriptions.add(createCodeAssistantDescription(project, compilerSet, null, null, isCompileConfiguration));
        }
        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()]));  // NOI18N
        
        return new PropertyNode(rootDescription);
    }
        
    
    // Code Assistant Node
    private CustomizerNode createCodeAssistantDescription(Project project, int compilerSetIdx, Item item, Folder folder, boolean isCompilerConfiguration) {
        Vector descriptions = new Vector();
        descriptions.add(createCCompilerDescription(project, compilerSetIdx, item, folder, isCompilerConfiguration));
        descriptions.add(createCCCompilerDescription(project, compilerSetIdx, item, folder, isCompilerConfiguration));
        String nodeLabel = nodeLabel = getString("LBL_PARSER_NODE");
        
        CustomizerNode rootDescription = new CustomizerNode(
                "CodeAssistant", // NOI18N
                nodeLabel,
                (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()])
                );
        
        return rootDescription;
    }
    
    CustomizerNode getAdvancedCutomizerNode(Vector descriptions) {
//      Vector advancedNodes = CustomizerRootNodeProvider.getInstance().getCustomizerNodes(true);
        Vector advancedNodes = new Vector();
        List<CustomizerNode> nodes = CustomizerRootNodeProvider.getInstance().getCustomizerNodes();
        for (CustomizerNode node : nodes) {
            if (!descriptions.contains(node))
                advancedNodes.add(node);
        }
        if (advancedNodes.size() == 0)
            return null;
        return new CustomizerNode(
                "advanced", // NOI18N
                getString("ADVANCED_CUSTOMIZER_NODE"), // NOI18N
                (CustomizerNode[])advancedNodes.toArray(new CustomizerNode[advancedNodes.size()]));
    }
    
    private CustomizerNode createNotFoundNode(String nodeName) {
        return new CustomizerNode(nodeName, nodeName + " - not found", null); // NOI18N
    }
    
    private Node createRootNodeItem(Project project, Item item) {
        CustomizerNode descriptions[];
        
        int tool = -1;
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)selectedConfigurations[i];
            int compilerSet2 = makeConfiguration.getCompilerSet().getValue();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)((MakeConfiguration)makeConfiguration).getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                continue;
            }
            int tool2 = itemConfiguration.getTool();
            if (tool == -1 && compilerSet == -1) {
                tool = tool2;
                compilerSet = compilerSet2;
            }
            if (tool != tool2 || compilerSet != compilerSet2) {
                tool = -1;
                break;
            }
            
            if ((isCompileConfiguration && !makeConfiguration.isCompileConfiguration()) || (!isCompileConfiguration && makeConfiguration.isCompileConfiguration())) {
                tool = -1;
                break;
            }
        }
        
        int count = 1;
        if (tool >= 0)
            count++;
        descriptions = new CustomizerNode[count];
        int index = 0;
        descriptions[index++] = createGeneralItemDescription(project, item);
        if (tool >= 0) {
            if (tool == Tool.CCompiler)
                descriptions[index++] = createCCompilerDescription(project, compilerSet, item, folder, isCompileConfiguration);
            else if (tool == Tool.CCCompiler)
                descriptions[index++] = createCCCompilerDescription(project, compilerSet, item, folder, isCompileConfiguration);
            else if (tool == Tool.FortranCompiler)
                descriptions[index++] = createFortranCompilerDescription(project, compilerSet, item, isCompileConfiguration);
            else if (tool == Tool.CustomTool)
                descriptions[index++] = createCustomBuildItemDescription(project, item);
            else
                descriptions[index++] = createCustomBuildItemDescription(project, item); // FIXUP
        }
        
        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions );  // NOI18N
        
        return new PropertyNode(rootDescription);
    }
    
    private Node createRootNodeFolder(Project project, Folder folder) {
        Vector descriptions;
        
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)selectedConfigurations[i];
            int compilerSet2 = makeConfiguration.getCompilerSet().getValue();
            if (compilerSet == -1) {
                compilerSet = compilerSet2;
            }
            if (compilerSet != compilerSet2) {
                compilerSet = -1;
                break;
            }
            
            if ((isCompileConfiguration && !makeConfiguration.isCompileConfiguration()) || (!isCompileConfiguration && makeConfiguration.isCompileConfiguration())) {
                compilerSet = -1;
                break;
            }
        }
        descriptions = new Vector(); //new CustomizerNode[2];
        descriptions.add(createGeneralFolderDescription(project, folder));
        if (compilerSet >= 0) {
            descriptions.add(createCCompilerDescription(project, compilerSet, null, folder, isCompileConfiguration));
            descriptions.add(createCCCompilerDescription(project, compilerSet, null, folder, isCompileConfiguration));
        }
        
        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()]));  // NOI18N
        
        return new PropertyNode(rootDescription);
    }
    
    private CustomizerNode createGeneralDescription(Project project) {
        return new GeneralCustomizerNode(
                "General", // NOI18N
                getString( "LBL_Config_General" ), // NOI18N
                null );
    }
    
    private JPanel projectPropPanel = null;
    class GeneralCustomizerNode extends CustomizerNode {
        public GeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        @Override
        public JPanel getPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
            if (projectPropPanel == null) {
                projectPropPanel = new ProjectPropPanel(project, configurationDescriptor);
            }
            return projectPropPanel;
        }

        @Override
        public CustomizerStyle customizerStyle() {
            return CustomizerStyle.PANEL;
        }
    
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectProperties"); // NOI18N
        }
    }
    
    private CustomizerNode createBuildDescription(Project project) {
        
        boolean includeMakefileDescription = true;
        boolean includeNewDescription = true;
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        boolean includeLinkerDescription = true;
        boolean includeArchiveDescription = true;
        boolean includeRunDebugDescriptions = true;
        
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)selectedConfigurations[i];
            
            if (compilerSet >= 0 && makeConfiguration.getCompilerSet().getValue() != compilerSet)
                includeNewDescription = false;
            compilerSet = makeConfiguration.getCompilerSet().getValue();
            
            if ((isCompileConfiguration && !makeConfiguration.isCompileConfiguration()) || (!isCompileConfiguration && makeConfiguration.isCompileConfiguration()))
                includeNewDescription = false;
            
            if (makeConfiguration.isMakefileConfiguration()) {
                includeNewDescription = false;
                includeLinkerDescription = false;
                includeArchiveDescription = false;
            }
            if (makeConfiguration.isLinkerConfiguration()) {
                includeMakefileDescription = false;
                includeArchiveDescription = false;
            }
            if (makeConfiguration.isArchiverConfiguration()) {
                includeMakefileDescription = false;
                includeLinkerDescription = false;
            }
            if (makeConfiguration.isLibraryConfiguration()) {
                includeRunDebugDescriptions = false;
            }
        }
        
        Vector descriptions = new Vector();
        if (includeMakefileDescription) {
            descriptions.add(createMakefileDescription(project));
            //descriptions.add(createRequiredProjectsDescription(project));
        }
        if (includeNewDescription) {
            descriptions.addAll(createCompilerNodes(project, compilerSet, -1, null, null, isCompileConfiguration, null));
        }
        if (includeLinkerDescription)
            descriptions.add(createLinkerDescription());
        if (includeArchiveDescription)
            descriptions.add(createArchiverDescription());
        
        descriptions.add(createPackagingDescription());
        
        return new BuildCustomizerNode(
                "Build", // NOI18N
                getString( "LBL_Config_Build" ), // NOI18N
                (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()]) );
    }
    
    class BuildCustomizerNode extends CustomizerNode {
        public BuildCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getGeneralSheet(project);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectProperties"); // NOI18N
        }
    }
    
    private CustomizerNode createGeneralItemDescription(Project project, Item item) {
        return new GeneralItemCustomizerNode(
                item,
                "GeneralItem", // NOI18N
                getString("LBL_Config_General"),
                null );
    }
    
    class GeneralItemCustomizerNode extends CustomizerNode {
        private Item item;
        
        public GeneralItemCustomizerNode(Item item, String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
            this.item = item;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration); //ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
            return itemConfiguration.getGeneralSheet();
        }
    }
    
    private CustomizerNode createGeneralFolderDescription(Project project, Folder folder) {
        return new GeneralFolderCustomizerNode(
                folder,
                "GeneralItem", // NOI18N
                getString("LBL_Config_General"),
                null );
    }
    
    class GeneralFolderCustomizerNode extends CustomizerNode {
        private Folder folder;
        
        public GeneralFolderCustomizerNode(Folder folder, String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
            this.folder = folder;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return folder.getFolderConfiguration(configuration).getGeneralSheet();
        }
    }
    
    private CustomizerNode createCustomBuildItemDescription(Project project, Item item) {
        return new CustomBuildItemCustomizerNode(
                item,
                "Custom Build Step", // NOI18N
                getString( "LBL_Config_Custom_Build" ), // NOI18N
                null );
    }
    
    class CustomBuildItemCustomizerNode extends CustomizerNode {
        private Item item;
        
        public CustomBuildItemCustomizerNode(Item item, String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
            this.item = item;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration); //ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
            return itemConfiguration.getCustomToolConfiguration().getSheet();
        }
    }
    
    
    // Make Node
    private CustomizerNode createMakefileDescription(Project project) {
        return new MakefileCustomizerNode(
                "Make", // NOI18N
                getString("LBL_MAKE_NODE"),
                null );
    }
    
    class MakefileCustomizerNode extends CustomizerNode {
        public MakefileCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getMakefileConfiguration().getSheet();
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsMake"); // NOI18N
        }
    }
    
    // Required Projects Node
    private CustomizerNode createRequiredProjectsDescription(Project project) {
        return new RequiredProjectsCustomizerNode(
                "RequiredProjects", // NOI18N
                getString("LBL_REQUIRED_PROJECTS_NODE"),
                null );
    }
    
    class RequiredProjectsCustomizerNode extends CustomizerNode {
        public RequiredProjectsCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getRequiredProjectsSheet(project, (MakeConfiguration)configuration);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsRequiredProjects"); // NOI18N
        }
    }

    
    // C/C++/Fortran Node
    private ArrayList createCompilerNodes(Project project, int compilerSetIdx, int tool, Item item, Folder folder, boolean isCompilerConfiguration, CustomizerNode linkerNode ) {
        ArrayList descriptions = new ArrayList();
        if (tool < 0 || tool == Tool.CCompiler)
            descriptions.add(createCCompilerDescription(project, compilerSetIdx, item, folder, isCompilerConfiguration));
        if (tool < 0 || tool == Tool.CCCompiler)
            descriptions.add(createCCCompilerDescription(project, compilerSetIdx, item, folder, isCompilerConfiguration));
        if (((tool < 0 && CppSettings.getDefault().isFortranEnabled() && folder == null) || tool == Tool.FortranCompiler) && isCompilerConfiguration)
            descriptions.add(createFortranCompilerDescription(project, compilerSetIdx, item, isCompilerConfiguration));
        
        if (linkerNode != null) {
            descriptions.add(linkerNode);
        }
        
        return descriptions;
    }
    
    // Linker
    private CustomizerNode createLinkerDescription() {
        CustomizerNode generalLinkerNode = new LinkerGeneralCustomizerNode("Linker", getString("LBL_LINKER_NODE"), null); // NOI18N
        return generalLinkerNode;
    }
    class LinkerGeneralCustomizerNode extends CustomizerNode {
        public LinkerGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getLinkerConfiguration().getGeneralSheet(project, (MakeConfigurationDescriptor)configurationDescriptor, (MakeConfiguration)configuration);
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsLinking"); // NOI18N
        }
    }
    
    
    // Archiver
    private CustomizerNode createArchiverDescription() {
        CustomizerNode generalNode = new ArchiverGeneralCustomizerNode("Archiver", getString("LBL_ARCHIVER_NODE"), null); // NOI18N
        return generalNode;
    }
    class ArchiverGeneralCustomizerNode extends CustomizerNode {
        public ArchiverGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getArchiverConfiguration().getGeneralSheet();
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsArchiverGeneral"); // NOI18N
        }
    }
    
    // Packaging
    private CustomizerNode createPackagingDescription() {
        CustomizerNode node = new PackagingCustomizerNode("Packaging", getString("LBL_PACKAGING_NODE"), null); // NOI18N
        return node;
    }
    class PackagingCustomizerNode extends CustomizerNode {
        public PackagingCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getPackagingConfiguration().getGeneralSheet(makeCustomizer);
        }
        @Override
        public HelpCtx getHelpCtx() {
            return null; //return new HelpCtx("ProjectPropsArchiverGeneral"); // NOI18N // FIXUP
        }
    }
    
    
    // C Compiler Node
    private CustomizerNode createCCompilerDescription(Project project, int compilerSetIdx,
            Item item, Folder folder, boolean isCompilerConfiguration) {
        String hostName = getSelectedHostName();
                CompilerSet csm = CompilerSetManager.getDefault(hostName).getCompilerSet(compilerSetIdx);
                String compilerName = csm.getTool(BasicCompiler.CCompiler).getName();
                String compilerDisplayName = csm.getTool(BasicCompiler.CCompiler).getDisplayName();
                CustomizerNode cCompilerCustomizerNode = new CCompilerCustomizerNode(
                    compilerName,
                    compilerDisplayName,
                    null,
                    item,
                    folder,
                    isCompilerConfiguration);
        return cCompilerCustomizerNode;
    }
    
    private String getSelectedHostName() {
        String host;
        if (configurationComboBox.getSelectedIndex() == configurationItems.length) {
            // All Configurations is selected.
            // Which host to use? localhost...
            host = CompilerSetManager.LOCALHOST;
        } else {
            MakeConfiguration conf = (MakeConfiguration) configurationComboBox.getSelectedItem();
            host = conf.getDevelopmentHost().getName();
        }
        return host;
    }

    class CCompilerCustomizerNode extends CustomizerNode {
        private Item item;
        private Folder folder;
	private boolean isCompilerConfiguration;
	
        public CCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item, Folder folder, boolean isCompilerConfiguration) {
            super(name, displayName, children);
            this.item = item;
            this.folder = folder;
	    this.isCompilerConfiguration = isCompilerConfiguration;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration); //ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration, folder);
            } else if (folder != null) {
                return folder.getFolderConfiguration((MakeConfiguration)configuration).getCCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration, folder);
            } else
                return ((MakeConfiguration)configuration).getCCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration, folder);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(isCompilerConfiguration ? "ProjectPropsCompiling" : "ProjectPropsParser"); // NOI18N
        }
    }
    
    
    
    // CC Compiler Node
    private CustomizerNode createCCCompilerDescription(Project project, int compilerSetIdx, Item item, Folder folder, boolean isCompilerConfiguration) {
        String hostName = getSelectedHostName();
        String compilerName = CompilerSetManager.getDefault(hostName).getCompilerSet(compilerSetIdx).getTool(BasicCompiler.CCCompiler).getName();
        String compilerDisplayName = CompilerSetManager.getDefault(hostName).getCompilerSet(compilerSetIdx).getTool(BasicCompiler.CCCompiler).getDisplayName();
        CustomizerNode ccCompilerCustomizerNode = new CCCompilerCustomizerNode(
                compilerName,
                compilerDisplayName,
                null,
                item,
                folder,
		isCompilerConfiguration);
        return ccCompilerCustomizerNode;
    }
    
    class CCCompilerCustomizerNode extends CustomizerNode {
        private Item item;
        private Folder folder;
	private boolean isCompilerConfiguration;
	
        public CCCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item, Folder folder, boolean isCompilerConfiguration) {
            super(name, displayName, children);
            this.item = item;
            this.folder = folder;
	    this.isCompilerConfiguration = isCompilerConfiguration;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration); //ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCCompilerConfiguration().getSheet((MakeConfiguration)configuration, folder);
            } else if (folder != null) {
                return folder.getFolderConfiguration(configuration).getCCCompilerConfiguration().getSheet((MakeConfiguration)configuration, folder);
            } else {
                return ((MakeConfiguration)configuration).getCCCompilerConfiguration().getSheet((MakeConfiguration)configuration, folder);
            }
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(isCompilerConfiguration ? "ProjectPropsCompiling" : "ProjectPropsParser"); // NOI18N
        }
    }
    
    
    
    
    // Fortran Compiler Node
    private CustomizerNode createFortranCompilerDescription(Project project, int compilerSetIdx, Item item, boolean isCompilerConfiguration) {
        String hostName = getSelectedHostName();
        String compilerName = CompilerSetManager.getDefault(hostName).getCompilerSet(compilerSetIdx).getTool(BasicCompiler.FortranCompiler).getName();
        String compilerDisplayName = CompilerSetManager.getDefault(hostName).getCompilerSet(compilerSetIdx).getTool(BasicCompiler.FortranCompiler).getDisplayName();
        CustomizerNode fortranCompilerCustomizerNode = new FortranCompilerCustomizerNode(
                compilerName,
                compilerDisplayName,
                null,
                item);
        return fortranCompilerCustomizerNode;
    }
    
    class FortranCompilerCustomizerNode extends CustomizerNode {
        private Item item;
	
        public FortranCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        @Override
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = item.getItemConfiguration(configuration); //ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getFortranCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
            } else
                return ((MakeConfiguration)configuration).getFortranCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("ProjectPropsCompiling"); // NOI18N
        }
    }
    
    
    /*
    class DummyCustomizerNode extends CustomizerNode {
        public DummyCustomizerNode(String name, String displayName) {
            super(name, displayName, null);
        }
    }
     */
    
    // Private meyhods ---------------------------------------------------------
    
    private javax.swing.JLabel createEmptyLabel( String text ) {
        
        JLabel label;
        if ( text == null ) {
            label = new JLabel();
        } else {
            label = new JLabel( text );
            label.setHorizontalAlignment( JLabel.CENTER );
        }
        
        return label;
    }
    
    private class DummyNode extends AbstractNode {
        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null)
                setSheet(sheet);
            setName(name);
        }
    }
    
    /** Node to be used for configuration
     */
    private class PropertyNode extends AbstractNode  implements HelpCtx.Provider {
        
        private CustomizerNode description;
        
        public PropertyNode( CustomizerNode description ) {
            super( description.children == null ? Children.LEAF : new PropertyNodeChildren( description.children ) );
            setName( description.name );
            setDisplayName( description.displayName );
            setIconBaseWithExtension(CustomizerNode.icon);
            this.description = description;
        }
        
        public CustomizerNode.CustomizerStyle custumizerStyle() {
            return description.customizerStyle();
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return description.getSheet(project, configurationDescriptor, configuration);
        }
        
        public JPanel getPanel(Project project, ConfigurationDescriptor configurationDescriptor) {
            return description.getPanel(project, configurationDescriptor);
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return description.getHelpCtx();
        }
    }
    
    /** Children used for configuration
     */
    private class PropertyNodeChildren extends Children.Keys {
        
        private Collection descriptions;
        
        public PropertyNodeChildren( CustomizerNode[] descriptions ) {
            this.descriptions = Arrays.asList( descriptions );
        }
        
        // Children.Keys impl --------------------------------------------------
        
        @Override
        public void addNotify() {
            setKeys( descriptions );
        }
        
        @Override
        public void removeNotify() {
            setKeys( Collections.EMPTY_LIST );
        }
        
        protected Node[] createNodes( Object key ) {
            return new Node[] { new PropertyNode( (CustomizerNode)key ) };
        }
    }
    
    private class MyListEditorPanel extends ListEditorPanel {
        public MyListEditorPanel(Object[] objects) {
            super(objects);
            setAllowedToRemoveAll(false);
        }
        
        @Override
        public Object addAction() {
            String newName = ConfigurationSupport.getUniqueNewName(getConfs());
            int type = MakeConfiguration.TYPE_MAKEFILE;
            if (getActive() != null)
                type = ((MakeConfiguration)getActive()).getConfigurationType().getValue();
            Configuration newconf = projectDescriptor.defaultConf(newName, type);
            ((MakeConfiguration)newconf).reCountLanguages((MakeConfigurationDescriptor)projectDescriptor);
            return newconf;
        }
        
        @Override
        public Object copyAction(Object o) {
            Configuration c = (Configuration)o;
            Configuration copyConf = c.copy();
            copyConf.setDefault(false);
            copyConf.setName(ConfigurationSupport.getUniqueCopyName(getConfs(), c));
            copyConf.setCloneOf(null);
            return copyConf;
        }
        
        @Override
        public void removeAction(Object o) {
            Configuration c = (Configuration)o;
            if (c.isDefault()) {
                if (getListData().elementAt(0) == o)
                    ((Configuration)getListData().elementAt(1)).setDefault(true);
                else
                    ((Configuration)getListData().elementAt(0)).setDefault(true);
            }
        }
        
        @Override
        public void defaultAction(Object o) {
            Vector confs = getListData();
            for (Enumeration e = confs.elements() ; e.hasMoreElements() ;) {
                ((Configuration)e.nextElement()).setDefault(false);
            }
            ((Configuration)o).setDefault(true);
        }
        
        @Override
        public void editAction(Object o) {
            Configuration c = (Configuration)o;
            
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("CONFIGURATION_RENAME_DIALOG_LABEL"), getString("CONFIGURATION_RENAME_DIALOG_TITLE")); // NOI18N
            notifyDescriptor.setInputText(c.getName());
            // Rename conf
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION)
                return;
            if (c.getName().equals(notifyDescriptor.getInputText()))
                return; // didn't change the name
            String suggestedName = ConfigurationSupport.makeNameLegal(notifyDescriptor.getInputText());
            String name = ConfigurationSupport.getUniqueName(getConfs(), suggestedName);
            c.setName(name);
        }
        
        @Override
        public String getListLabelText() {
            return getString("CONFIGURATIONS_LIST_NAME");
        }
        @Override
        public char getListLabelMnemonic() {
            return getString("CONFIGURATIONS_LIST_MNE").charAt(0);
        }
        
        public Configuration[] getConfs() {
            return (Configuration[]) getListData().toArray(new Configuration[getListData().size()]);
        }
        
        public Configuration getActive() {
            Configuration[] confs = getConfs();
            Configuration active = null;
            for (int i = 0; i < confs.length; i++) {
                if (confs[i].isDefault()) {
                    active = confs[i];
                    break;
                }
            }
            return active;
        }
    }
    
    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakeCustomizer.class);
        }
        return bundle.getString(s);
    }
}
