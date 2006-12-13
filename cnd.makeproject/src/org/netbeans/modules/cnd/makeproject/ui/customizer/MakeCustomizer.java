/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertySheetView;
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
    private Node previousNode;
    
    private GridBagConstraints fillConstraints;
    
    private Project project;
    
    private DialogDescriptor dialogDescriptor;
    private MakeCustomizer makeCustomizer;
    
    private ConfigurationDescriptor projectDescriptor;
    private Item item;
    private Vector controls;
    private CategoryView currentCategoryView;
    private String currentNodeName;
    private Configuration[] configurationItems;
    private Configuration[] selectedConfigurations;
    private int lastComboboxIndex = -1;
    
    /** Creates new form MakeCustomizer */
    public MakeCustomizer(Project project, String preselectedNodeName, ConfigurationDescriptor projectDescriptor, Item item, Vector controls) {
        initComponents();
        this.projectDescriptor = projectDescriptor;
        this.controls = controls;
        this.project = project;
        this.makeCustomizer = this;
        this.item = item;
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
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_MakeCustomizer"));
        fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, item), preselectedNodeName );
        currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class,"AN_BeanTreeViewCategories"));
        currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_BeanTreeViewCategories"));
        categoryPanel.add( currentCategoryView, fillConstraints );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configurationPanel = new javax.swing.JPanel();
        configurationLabel = new javax.swing.JLabel();
        configurationComboBox = new javax.swing.JComboBox();
        configurationsButton = new javax.swing.JButton();
        categoryPanel = new javax.swing.JPanel();
        customizerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(750, 450));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer"));
        configurationPanel.setLayout(new java.awt.GridBagLayout());

        configurationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATION_COMBOBOX_MNE").charAt(0));
        configurationLabel.setLabelFor(configurationComboBox);
        configurationLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATION_COMBOBOX_LBL"));
        configurationPanel.add(configurationLabel, new java.awt.GridBagConstraints());

        configurationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        configurationPanel.add(configurationComboBox, gridBagConstraints);

        configurationsButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATIONS_BUTTON_MNE").charAt(0));
        configurationsButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/customizer/Bundle").getString("CONFIGURATIONS_BUTTON_LBL"));
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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(configurationPanel, gridBagConstraints);

        categoryPanel.setLayout(new java.awt.GridBagLayout());

        categoryPanel.setBorder(new javax.swing.border.EtchedBorder());
        categoryPanel.setMinimumSize(new java.awt.Dimension(220, 4));
        categoryPanel.setPreferredSize(new java.awt.Dimension(220, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(categoryPanel, gridBagConstraints);
        categoryPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSN_MakeCustomizer_categoryPanel"));
        categoryPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeCustomizer.class, "ACSD_MakeCustomizer_categoryPanel"));

        customizerPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 8, 8);
        add(customizerPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
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
            currentCategoryView = new CategoryView(createRootNode(project, projectDescriptor, item), null );
            currentCategoryView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MakeCustomizer.class,"AN_BeanTreeViewCategories"));
            currentCategoryView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizer.class,"AD_BeanTreeViewCategories"));
            categoryPanel.add(currentCategoryView, fillConstraints );
            if (selectedNodeName != null)
                currentCategoryView.selectNode(selectedNodeName);
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JComboBox configurationComboBox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JButton configurationsButton;
    private javax.swing.JPanel customizerPanel;
    // End of variables declaration//GEN-END:variables
    
    
    public void setDialogDescriptor( DialogDescriptor dialogDescriptor ) {
        this.dialogDescriptor = dialogDescriptor;
    }
    
    // HelpCtx.Provider implementation -----------------------------------------
    
    public HelpCtx getHelpCtx() {
        if ( currentCustomizer != null ) {
            return HelpCtx.findHelp( currentCustomizer );
        } else {
            return HelpCtx.findHelp( customizerPanel );
        }
    }
    
    
    
    // Private innerclasses ----------------------------------------------------
    
    private class CategoryView extends JPanel implements ExplorerManager.Provider {
        
        private ExplorerManager manager;
        private BeanTreeView btv;
        
        CategoryView( Node rootNode, String preselectedNodeName ) {
            
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
                btv.expandNode(nodes1[i]);
                Node[] nodes2 = nodes1[i].getChildren().getNodes();
                for (int j = 0; j < nodes2.length; j++) {
                    btv.collapseNode(nodes2[j]);
                }
            }
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public void addNotify() {
            super.addNotify();
            //btv.expandAll();
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
                    ConfigurationNode configurationNode = (ConfigurationNode)node;
                    PropertySheetView currentPropertySheetView = new PropertySheetView();
                    DummyNode[] dummyNodes = new DummyNode[selectedConfigurations.length];
                    for (int i = 0; i < selectedConfigurations.length; i++) {
                        dummyNodes[i] = new DummyNode(configurationNode.getSheet(project, projectDescriptor, selectedConfigurations[i]), selectedConfigurations[i].getName());
                    }
                    currentPropertySheetView.setNodes(dummyNodes);
                    JPanel panel = new JPanel();
                    panel.setLayout(new java.awt.GridBagLayout());
                    panel.setBorder(new javax.swing.border.EtchedBorder());
                    panel.add(currentPropertySheetView, fillConstraints);
                    customizerPanel.add(panel, fillConstraints );
                    customizerPanel.validate();
                    customizerPanel.repaint();
                    currentCustomizer = panel;
                    
                    btv.requestFocus();
                    
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
                ConfSelectorPanel confSelectorPanel = new ConfSelectorPanel(getString("SELECTED_CONFIGURATIONS_LBL"), configurationItems, null);
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
    
    private Node createRootNode(Project project, ConfigurationDescriptor projectDescriptor, Item item) {
        if (item == null)
            return createRootNodeProject(project, projectDescriptor);
        else
            return createRootNodeItem(project, item);
    }
    
    private Node createRootNodeProject(Project project, ConfigurationDescriptor projectDescriptor) {
        boolean includeMakefileDescription = true;
        boolean includeNewDescription = true;
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        boolean includeLinkerDescription = true;
        boolean includeArchiveDescription = true;
        
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
        }
        
        Vector descriptions = new Vector();
        descriptions.add(createGeneralDescription(project));
        descriptions.add(getAuxDescription("Running")); // NOI18N
        descriptions.add(getAuxDescription("Debug")); // NOI18N
        if (includeMakefileDescription)
            descriptions.add(createMakefileDescription(project));
        if (includeNewDescription)
            descriptions.add(createNewDescription(project, compilerSet, -1, null, isCompileConfiguration));
        if (includeLinkerDescription)
            descriptions.add(createLinkerDescription());
        if (includeArchiveDescription)
            descriptions.add(createArchiverDescription());
        
        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()]));  // NOI18N
        
        return new ConfigurationNode(rootDescription);
    }
    
    private Node createRootNodeItem(Project project, Item item) {
        CustomizerNode descriptions[];
        
        int tool = -1;
        int compilerSet = -1;
        boolean isCompileConfiguration = ((MakeConfiguration)selectedConfigurations[0]).isCompileConfiguration();
        
        for (int i = 0; i < selectedConfigurations.length; i++) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)selectedConfigurations[i];
            int compilerSet2 = makeConfiguration.getCompilerSet().getValue();
            ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)makeConfiguration).getAuxObject(ItemConfiguration.getId(item.getPath()));
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
                descriptions[index++] = createNewDescription(project, compilerSet, tool, item, isCompileConfiguration);
            else if (tool == Tool.CCCompiler)
                descriptions[index++] = createNewDescription(project, compilerSet, tool, item, isCompileConfiguration);
            else if (tool == Tool.FortranCompiler)
                descriptions[index++] = createNewDescription(project, compilerSet, tool, item, isCompileConfiguration);
            else if (tool == Tool.CustomTool)
                descriptions[index++] = createCustomBuildItemDescription(project, item);
            else
                descriptions[index++] = createCustomBuildItemDescription(project, item); // FIXUP
        }
        
        CustomizerNode rootDescription = new CustomizerNode(
                "Configuration Properties", getString("CONFIGURATION_PROPERTIES"), descriptions );  // NOI18N
        
        return new ConfigurationNode(rootDescription);
    }
    
    private CustomizerNode getAuxDescription(String nodeName) {
        CustomizerNode node = CustomizerRootNodeProvider.getInstance().getCustomizerNode(nodeName);
        if (node != null)
            return node;
        return new CustomizerNode(
                nodeName, // NOI18N
                nodeName + " - not found", // NOI18N
                null);
    }
    
    private CustomizerNode createGeneralDescription(Project project) {
        ResourceBundle bundle = NbBundle.getBundle( MakeCustomizer.class );
        
        return new GeneralCustomizerNode(
                "General", // NOI18N
                bundle.getString( "LBL_Config_General" ), // NOI18N
                null );
    }
    
    class GeneralCustomizerNode extends CustomizerNode {
        public GeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getGeneralSheet(project);
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
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
            return itemConfiguration.getGeneralSheet();
        }
    }
    
    private CustomizerNode createCustomBuildItemDescription(Project project, Item item) {
        ResourceBundle bundle = NbBundle.getBundle( MakeCustomizer.class );
        
        return new CustomBuildItemCustomizerNode(
                item,
                "Custom Build Step", // NOI18N
                bundle.getString( "LBL_Config_Custom_Build" ), // NOI18N
                null );
    }
    
    class CustomBuildItemCustomizerNode extends CustomizerNode {
        private Item item;
        
        public CustomBuildItemCustomizerNode(Item item, String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
            return itemConfiguration.getCustomToolConfiguration().getSheet();
        }
    }
    
    
    // Make Node
    private CustomizerNode createMakefileDescription(Project project) {
        ResourceBundle bundle = NbBundle.getBundle( MakeCustomizer.class );
        
        return new MakefileCustomizerNode(
                "Make", // NOI18N
                getString("LBL_MAKE_NODE"),
                null );
    }
    
    class MakefileCustomizerNode extends CustomizerNode {
        public MakefileCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getMakefileConfiguration().getSheet();
        }
    }
    
    
    // C/C++/Fortran Node
    private CustomizerNode createNewDescription(Project project, int compilerSet, int tool, Item item, boolean isCompilerConfiguration) {
        ResourceBundle bundle = NbBundle.getBundle( MakeCustomizer.class );
        
        Vector descriptions = new Vector();
        if (tool < 0 || tool == Tool.CCompiler)
            descriptions.add(createCCompilerDescription(project, compilerSet, item, isCompilerConfiguration));
        if (tool < 0 || tool == Tool.CCCompiler)
            descriptions.add(createCCCompilerDescription(project, compilerSet, item, isCompilerConfiguration));
        if ((tool < 0 && MakeOptions.getInstance().getFortran()) || tool == Tool.FortranCompiler)
            descriptions.add(createFortranCompilerDescription(project, compilerSet, item, isCompilerConfiguration));
        
        String nodeLabel;
        if (isCompilerConfiguration) {
            nodeLabel = MakeOptions.getInstance().getFortran() ? getString("LBL_CCPPFORTRAN_NODE") : getString("LBL_CCPP_NODE");
        } else {
            nodeLabel = "Parser Configuration";
        }
        
        CustomizerNode rootDescription = new CustomizerNode(
                "C/C++", // NOI18N
                nodeLabel,
                (CustomizerNode[])descriptions.toArray(new CustomizerNode[descriptions.size()])
                );
        
        return rootDescription;
    }
    
    
    // Linker
    private CustomizerNode createLinkerDescription() {
        CustomizerNode generalLinkerNode = new LinkerGeneralCustomizerNode("LinkerGeneral", getString("LBL_Config_General"), null); // NOI18N
        CustomizerNode librariesNode = new LibrariesGeneralCustomizerNode("Libraries", getString("LBL_LIBRARIES"), null); // NOI18N
        CustomizerNode clCustomizerNode = new LinkerCommandLineCustomizerNode("LinkerCommandLine", getString("LBL_COMMAND_LINE"), null); // NOI18N
        return  new CustomizerNode(
                "Linker", // NOI18N
                getString("LBL_LINKER_NODE"),
                new CustomizerNode[] {generalLinkerNode, librariesNode, clCustomizerNode});
    }
    class LinkerGeneralCustomizerNode extends CustomizerNode {
        public LinkerGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getLinkerConfiguration().getGeneralSheet((MakeConfigurationDescriptor)configurationDescriptor, (MakeConfiguration)configuration);
        }
    }
    class LibrariesGeneralCustomizerNode extends CustomizerNode {
        public LibrariesGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getLinkerConfiguration().getLibrariesSheet(project, (MakeConfiguration)configuration);
        }
    }
    class LinkerCommandLineCustomizerNode extends CustomizerNode {
        public LinkerCommandLineCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getLinkerConfiguration().getCommandLineSheet();
        }
    }
    
    
    // Archiver
    private CustomizerNode createArchiverDescription() {
        CustomizerNode generalNode = new ArchiverGeneralCustomizerNode("ArchiverGeneral", getString("LBL_Config_General"), null); // NOI18N
        CustomizerNode clNode = new ArchiverCommandLineCustomizerNode("ArchiverCommandLine", getString("LBL_COMMAND_LINE"), null); // NOI18N
        return  new CustomizerNode(
                "Archiver", // NOI18N
                getString("LBL_ARCHIVER_NODE"), // FIXUP
                new CustomizerNode[] {generalNode, clNode});
    }
    class ArchiverGeneralCustomizerNode extends CustomizerNode {
        public ArchiverGeneralCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getArchiverConfiguration().getGeneralSheet();
        }
    }
    class ArchiverCommandLineCustomizerNode extends CustomizerNode {
        public ArchiverCommandLineCustomizerNode(String name, String displayName, CustomizerNode[] children) {
            super(name, displayName, children);
        }
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return ((MakeConfiguration)configuration).getArchiverConfiguration().getCommandLineSheet();
        }
    }
    
    
    // C Compiler Node
    private CustomizerNode createCCompilerDescription(Project project, int compilerSet, Item item, boolean isCompilerConfiguration) {
        String compilerName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.CCompiler).getName();
        String compilerDisplayName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.CCompiler).getDisplayName();
        ResourceBundle bundle = NbBundle.getBundle(MakeCustomizer.class);
        CustomizerNode cCompilerCustomizerNode = new CCompilerCustomizerNode(
                "GeneralCCompiler", // NOI18N
                bundle.getString("LBL_Config_General"), // NOI18N
                null,
                item);
        CustomizerNode[] customizerNodes;
        if (isCompilerConfiguration) {
            CustomizerNode clCustomizerNode = new CCompilerCommandLineNode("CCommandLine", getString("LBL_COMMAND_LINE"), null, item); // NOI18N
            customizerNodes = new CustomizerNode[] {cCompilerCustomizerNode, clCustomizerNode};
        } else {
            customizerNodes = new CustomizerNode[] {cCompilerCustomizerNode};
        }
        
        return  new CustomizerNode(
                compilerName, // NOI18N
                compilerDisplayName, // FIXUP
                customizerNodes);
    }
    
    class CCompilerCustomizerNode extends CustomizerNode {
        private Item item;
        
        public CCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
            } else
                return ((MakeConfiguration)configuration).getCCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
        }
    }
    
    class CCompilerCommandLineNode extends CustomizerNode {
        private Item item;
        
        public CCompilerCommandLineNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCompilerConfiguration().getCommandLineSheet(configuration);
            } else
                return ((MakeConfiguration)configuration).getCCompilerConfiguration().getCommandLineSheet(configuration);
        }
    }
    
    
    
    // CC Compiler Node
    private CustomizerNode createCCCompilerDescription(Project project, int compilerSet, Item item, boolean isCompilerConfiguration) {
        String compilerName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.CCCompiler).getName();
        String compilerDisplayName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.CCCompiler).getDisplayName();
        ResourceBundle bundle = NbBundle.getBundle(MakeCustomizer.class);
        CustomizerNode ccCompilerCustomizerNode = new CCCompilerCustomizerNode(
                "GeneralCCCompiler", // NOI18N
                bundle.getString("LBL_Config_General"), // NOI18N
                null,
                item);
        CustomizerNode[] customizerNodes;
        if (isCompilerConfiguration) {
            CustomizerNode clCustomizerNode = new CCCompilerCommandLineNode("CCCommandLine", getString("LBL_COMMAND_LINE"), null, item); // NOI18N
            customizerNodes = new CustomizerNode[] {ccCompilerCustomizerNode, clCustomizerNode};
        } else {
            customizerNodes = new CustomizerNode[] {ccCompilerCustomizerNode};
        }
        
        return  new CustomizerNode(
                compilerName, // NOI18N
                compilerDisplayName, // FIXUP
                customizerNodes);
    }
    
    class CCCompilerCustomizerNode extends CustomizerNode {
        private Item item;
        
        public CCCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCCompilerConfiguration().getSheet((MakeConfiguration)configuration);
            } else {
                return ((MakeConfiguration)configuration).getCCCompilerConfiguration().getSheet((MakeConfiguration)configuration);
            }
        }
    }
    
    class CCCompilerCommandLineNode extends CustomizerNode {
        private Item item;
        
        public CCCompilerCommandLineNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getCCCompilerConfiguration().getCommandLineSheet(configuration);
            } else
                return ((MakeConfiguration)configuration).getCCCompilerConfiguration().getCommandLineSheet(configuration);
        }
    }
    
    
    
    // Fortran Compiler Node
    private CustomizerNode createFortranCompilerDescription(Project project, int compilerSet, Item item, boolean isCompilerConfiguration) {
        String compilerName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.FortranCompiler).getName();
        String compilerDisplayName = CompilerSets.getCompilerSet(compilerSet).getTool(BasicCompiler.FortranCompiler).getDisplayName();
        CustomizerNode fortranCompilerCustomizerNode = new FortranCompilerCustomizerNode(
                "GeneralFortranCompiler", // NOI18N
                getString("LBL_Config_General"), // NOI18N
                null,
                item);
        CustomizerNode[] customizerNodes;
        if (isCompilerConfiguration) {
            CustomizerNode clCustomizerNode = new FortranCompilerCommandLineNode("FortranCommandLine", getString("LBL_COMMAND_LINE"), null, item); // NOI18N
            customizerNodes = new CustomizerNode[] {fortranCompilerCustomizerNode, clCustomizerNode};
        } else {
            customizerNodes = new CustomizerNode[] {fortranCompilerCustomizerNode};
        }
        
        return  new CustomizerNode(
                compilerName, // NOI18N
                compilerDisplayName, // FIXUP
                customizerNodes);
    }
    
    class FortranCompilerCustomizerNode extends CustomizerNode {
        private Item item;
        
        public FortranCompilerCustomizerNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getFortranCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
            } else
                return ((MakeConfiguration)configuration).getFortranCompilerConfiguration().getGeneralSheet((MakeConfiguration)configuration);
        }
    }
    
    class FortranCompilerCommandLineNode extends CustomizerNode {
        private Item item;
        
        public FortranCompilerCommandLineNode(String name, String displayName, CustomizerNode[] children, Item item) {
            super(name, displayName, children);
            this.item = item;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            if (item != null) {
                ItemConfiguration itemConfiguration = (ItemConfiguration)((MakeConfiguration)configuration).getAuxObject(ItemConfiguration.getId(item.getPath()));
                return itemConfiguration.getFortranCompilerConfiguration().getCommandLineSheet(configuration);
            } else
                return ((MakeConfiguration)configuration).getFortranCompilerConfiguration().getCommandLineSheet(configuration);
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
    private class ConfigurationNode extends AbstractNode {
        
        private CustomizerNode description;
        
        public ConfigurationNode( CustomizerNode description ) {
            super( description.children == null ? Children.LEAF : new ConfigurationChildren( description.children ) );
            setName( description.name );
            setDisplayName( description.displayName );
            setIconBaseWithExtension(description.icon);
            this.description = description;
        }
        
        public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
            return description.getSheet(project, configurationDescriptor, configuration);
        }
    }
    
    /** Children used for configuration
     */
    private class ConfigurationChildren extends Children.Keys {
        
        private Collection descriptions;
        
        public ConfigurationChildren( CustomizerNode[] descriptions ) {
            this.descriptions = Arrays.asList( descriptions );
        }
        
        // Children.Keys impl --------------------------------------------------
        
        public void addNotify() {
            setKeys( descriptions );
        }
        
        public void removeNotify() {
            setKeys( Collections.EMPTY_LIST );
        }
        
        protected Node[] createNodes( Object key ) {
            return new Node[] { new ConfigurationNode( (CustomizerNode)key ) };
        }
    }
    
    private class MyListEditorPanel extends ListEditorPanel {
        public MyListEditorPanel(Object[] objects) {
            super(objects);
            setAllowedToRemoveAll(false);
        }
        
        public Object addAction() {
            String newName = ConfigurationSupport.getUniqueNewName(getConfs());
            Configuration newconf = projectDescriptor.defaultConf(newName, 0); // FIXUP: should macth current type
            return newconf;
        }
        
        public Object copyAction(Object o) {
            Configuration c = (Configuration)o;
            Configuration copyConf = c.copy();
            copyConf.setDefault(false);
            copyConf.setName(ConfigurationSupport.getUniqueCopyName(getConfs(), c));
            copyConf.setCloneOf(null);
            return copyConf;
        }
        
        public void removeAction(Object o) {
            /*
            Configuration c = (Configuration)o;
            if (c.isDefault()) {
                if (getListData().elementAt(0) == o)
                    ((Configuration)getListData().elementAt(1)).setDefault(true);
                else
                    ((Configuration)getListData().elementAt(0)).setDefault(true);
            }
             */
        }
        
        public void defaultAction(Object o) {
            Vector confs = getListData();
            for (Enumeration e = confs.elements() ; e.hasMoreElements() ;) {
                ((Configuration)e.nextElement()).setDefault(false);
            }
            ((Configuration)o).setDefault(true);
        }
        
        public void editAction(Object o) {
            Configuration c = (Configuration)o;
            
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine("", getString("CONFIGURATION_RENAME_DIALOG_TITLE")); // NOI18N
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
        
        public String getListLabelText() {
            return getString("CONFIGURATIONS_LIST_NAME");
        }
        public char getListLabelMnemonic() {
            return getString("CONFIGURATIONS_LIST_MNE").charAt(0);
        }
        
        public Configuration[] getConfs() {
            return (Configuration[]) getListData().toArray(new Configuration[getListData().size()]);
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
