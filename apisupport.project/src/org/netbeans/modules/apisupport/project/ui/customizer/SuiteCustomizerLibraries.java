/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 * Represents <em>Libraries</em> panel in Suite customizer.
 *
 * @author Martin Krauskopf
 */
final class SuiteCustomizerLibraries extends NbPropertyPanel.Suite
implements Comparator, ExplorerManager.Provider, ChangeListener {

    private ExplorerManager manager;
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerLibraries(final SuiteProperties suiteProps) {
        super(suiteProps, SuiteCustomizerLibraries.class);
        initComponents();
        manager = new ExplorerManager();
        refresh();
        
        
        view.setProperties(new Node.Property[] { EnabledProp.TEMPLATE });
        view.setRootVisible(false);
        view.setDefaultActionAllowed(false);
        
        suiteProps.getBrandingModel().addChangeListener(this);
        suiteProps.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (SuiteProperties.NB_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {
                    refresh();
                }
            }
        });
    }
    
    void refresh() {
        refreshPlatforms();
        refreshModules();
    }
    
    private void refreshModules() {
        ModuleEntry[] entry = getProperties().getActivePlatform().getModules();
        Node root = createModuleNode(entry);
        manager.setRootContext(root);
    }
    
    private void refreshPlatforms() {
        platformValue.setModel(new org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.NbPlatformListModel()); // refresh
        platformValue.setSelectedItem(getProperties().getActivePlatform());
        platformValue.requestFocus();
    }
    
    public void store() {
        Set disabledClusters = new TreeSet();
        Set disabledModules = new TreeSet();
        
        Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] instanceof Enabled) {
                Enabled e = (Enabled)clusters[i];
                if (!e.isEnabled()) {
                    disabledClusters.add(e.getName());
                } else {
                    Node[] modules = e.getChildren().getNodes();
                    for (int j = 0; j < modules.length; j++) {
                        if (modules[j] instanceof Enabled) {
                            Enabled m = (Enabled)modules[j];
                            if (!m.isEnabled()) {
                                disabledModules.add(m.getName());
                            }
                        }
                    }
                }
            }
        }
        
        getProperties().setDisabledClusters((String[])disabledClusters.toArray(new String[0]));
        getProperties().setDisabledModules((String[])disabledModules.toArray(new String[0]));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        platformPanel = new javax.swing.JPanel();
        platformValue = org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory.getNbPlatformsComboxBox();
        platform = new javax.swing.JLabel();
        managePlafsButton = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        view = new org.openide.explorer.view.TreeTableView();
        viewLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        platformPanel.setLayout(new java.awt.GridBagLayout());

        platformValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                platformValueItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platformValue, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        platformPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlafsButton, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "CTL_ManagePlatform"));
        managePlafsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatforms(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        platformPanel.add(managePlafsButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(platformPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        view.setBorder(javax.swing.UIManager.getBorder("ScrollPane.border"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(view, gridBagConstraints);

        viewLabel.setLabelFor(view);
        org.openide.awt.Mnemonics.setLocalizedText(viewLabel, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_PlatformModules"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 2, 0);
        add(viewLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void platformValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_platformValueItemStateChanged
        getProperties().setActivePlatform((NbPlatform) platformValue.getSelectedItem());
        refreshModules();
    }//GEN-LAST:event_platformValueItemStateChanged
    
    private void managePlatforms(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatforms
        NbPlatformCustomizer.showCustomizer();
        refreshPlatforms();
    }//GEN-LAST:event_managePlatforms
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler;
    private javax.swing.JButton managePlafsButton;
    private javax.swing.JLabel platform;
    private javax.swing.JPanel platformPanel;
    private javax.swing.JComboBox platformValue;
    private org.openide.explorer.view.TreeTableView view;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables

    
    private Node createModuleNode(ModuleEntry[] entries) {
        HashSet disabledModuleCNB = new HashSet(Arrays.asList(getProperties().getDisabledModules()));
        HashSet disabledClusters = new HashSet(Arrays.asList(getProperties().getDisabledClusters()));
        
        HashMap clusterToChildren = new HashMap();
        
        Children.SortedArray clusters = new Children.SortedArray();
        clusters.setComparator(this);
        AbstractNode n = new AbstractNode(clusters);
        n.setName(org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ModuleListClusters"));
        n.setDisplayName(org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ModuleListClustersModules"));
        
        for (int i = 0; i < entries.length; i++) {
            Children clusterChildren = (Children)clusterToChildren.get(entries[i].getClusterDirectory());
            if (clusterChildren == null) {
                Children.SortedArray modules = new Children.SortedArray();
                modules.setComparator(this);
                clusterChildren = modules;
                
                String clusterName = entries[i].getClusterDirectory().getName();
                Enabled cluster = new Enabled(modules, !disabledClusters.contains(clusterName));
                cluster.setName(clusterName);
                cluster.setIconBaseWithExtension(SuiteProject.SUITE_ICON_PATH);
                clusterToChildren.put(entries[i].getClusterDirectory(), modules);
                n.getChildren().add(new Node[] { cluster });
            }
            
            String cnb = entries[i].getCodeNameBase();
            AbstractNode module = new Enabled(Children.LEAF, !disabledModuleCNB.contains(cnb));
            module.setName(cnb);
            module.setDisplayName(entries[i].getLocalizedName());
            module.setShortDescription(entries[i].getShortDescription());
            module.setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            
            clusterChildren.add(new Node[] { module });
        }
        
        return n;
    }
    
    public int compare(Object o1, Object o2) {
        Node n1 = (Node)o1;
        Node n2 = (Node)o2;
        
        return n1.getDisplayName().compareTo(n2.getDisplayName());
    }    
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private static final Set/*<String>*/ DISABLED_PLATFORM_MODULES = new HashSet();
    static {
	// Probably not needed for most platform apps, and won't even work under JNLP.
	DISABLED_PLATFORM_MODULES.add("org.netbeans.modules.autoupdate"); // NOI18N
	// XXX the following would not be shown in regular apps anyway, because they are autoloads,
	// but they *are* shown in JNLP apps because currently even unused autoloads are enabled under JNLP:
	// Just annoying; e.g. shows Runtime tab prominently.
	DISABLED_PLATFORM_MODULES.add("org.openide.execution"); // NOI18N
	DISABLED_PLATFORM_MODULES.add("org.netbeans.core.execution"); // NOI18N
	// Similar - unlikely to really be wanted by typical platform apps, and show some GUI.
	DISABLED_PLATFORM_MODULES.add("org.openide.io"); // NOI18N
	DISABLED_PLATFORM_MODULES.add("org.netbeans.core.output2"); // NOI18N
	DISABLED_PLATFORM_MODULES.add("org.netbeans.core.multiview"); // NOI18N
	// And these are deprecated:
	DISABLED_PLATFORM_MODULES.add("org.openide.compat"); // NOI18N
	DISABLED_PLATFORM_MODULES.add("org.openide.util.enumerations"); // NOI18N
    }
    public void stateChanged(ChangeEvent ev) {
        if (getProperties().getBrandingModel().isBrandingEnabled()) {
            // User is turning on branded mode. Let's take a guess: they want to
            // exclude the usual suspects from the module list. We do not want to set
            // these excludes on a new suite because user might want to use real IDE as the platform
            // (i.e. not be creating an app, but rather be creating some modules for the IDE).
            // Only do this if there are no existing exclusions.
            Node[] clusters = getExplorerManager().getRootContext().getChildren().getNodes();
            for (int i = 0; i < clusters.length; i++) {
                if (clusters[i] instanceof Enabled) {
                    Enabled e = (Enabled) clusters[i];
                    if (!e.isEnabled()) {
                        return;
                    } else {
                        Node[] modules = e.getChildren().getNodes();
                        for (int j = 0; j < modules.length; j++) {
                            if (modules[j] instanceof Enabled) {
                                Enabled m = (Enabled) modules[j];
                                if (!m.isEnabled()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < clusters.length; i++) {
                if (clusters[i] instanceof Enabled) {
                    Enabled e = (Enabled) clusters[i];
                    if (e.getName().startsWith("platform")) { // NOI18N
                        Node[] modules = e.getChildren().getNodes();
                        for (int j = 0; j < modules.length; j++) {
                            if (modules[j] instanceof Enabled) {
                                Enabled m = (Enabled) modules[j];
                                if (DISABLED_PLATFORM_MODULES.contains(m.getName())) {
                                    m.setEnabled(false);
                                }
                            }
                        }
                    } else {
                        e.setEnabled(false);
                    }
                }
            }
        }
    }

    static final class Enabled extends AbstractNode {
        private boolean enabled;
        private Children standard;
        
        public Enabled(Children ch, boolean enabled) {
            super(enabled ? ch : Children.LEAF);
            this.standard = ch;
            this.enabled = enabled;
            
            Sheet s = Sheet.createDefault();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            ss.put(new EnabledProp(this));
            setSheet(s);
        }
        
        public void setEnabled(boolean s) {
            if (s == enabled) {
                return;
            }
            enabled = s;
            
            firePropertySetsChange(null, null);
            setChildren(s ? standard : Children.LEAF);
            Node n = getParentNode();
            if (n instanceof Enabled) {
                Enabled en = (Enabled)n;
                en.firePropertySetsChange(null, null);
            }
        }
        
        public boolean isEnabled() {
            return enabled;
        }
    }
    
    private static final class EnabledProp extends PropertySupport.ReadWrite {
        
        private static final EnabledProp TEMPLATE = new EnabledProp(null);
        
        private Enabled node;
        
        public EnabledProp(Enabled node) {
            super("enabled", Boolean.TYPE, org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ModuleListEnabled"), org.openide.util.NbBundle.getMessage(SuiteCustomizerLibraries.class, "LBL_ModuleListEnabledShortDescription"));
            this.node = node;
        }
        
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            node.setEnabled(((Boolean)val).booleanValue());
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            Children ch = node.getChildren();
            if (ch == Children.LEAF) {
                return Boolean.valueOf(node.isEnabled());
            } else {
                Node[] arr = ch.getNodes();
                boolean on = false;
                boolean off = false;
                for (int i = 0; i < arr.length; i++) {
                    Enabled n = (Enabled)arr[i];
                    if (n.isEnabled()) {
                        on = true;
                    } else {
                        off = true;
                    }
                    
                    if (on && off) {
                        return null;
                    }
                }
                
                return Boolean.valueOf(on);
            }
        }
        
        public boolean canWrite() {
            Node parent = node.getParentNode();
            if (parent instanceof Enabled) {
                // cluster node
                return ((Enabled)parent).isEnabled();
            }
            return true;
        }

        private PropertyEditor editor;
        public PropertyEditor getPropertyEditor() {
            if (editor == null) {
                editor = super.getPropertyEditor();
            }
            return editor;
        }
        
    }

}
