/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.ui;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.openide.ErrorManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Children;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.platform.wizard.PlatformInstallIterator;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;







/**
 *
 * @author  tom
 */
public class PlatformsCustomizer extends javax.swing.JPanel implements PropertyChangeListener, ExplorerManager.Provider {

    private static final String TEMPLATE = "Templates/Services/Platforms/org-netbeans-api-java-Platform/javaplatform.xml";  //NOI18N
    private static final String STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";  //NOI18N
    
    private static final Dimension PREFERRED_SIZE = new Dimension (720,400);

    private PlatformCategoriesChildren children;
    private ExplorerManager manager;

    /** Creates new form PlatformsCustomizer */
    public PlatformsCustomizer() {
        initComponents();
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length==0) {
                selectPlatform (null);
            }
            else {
                selectPlatform (nodes[0]);
            }
        }
    }
    
    
    public Dimension getPreferredSize () {
        return PREFERRED_SIZE;
    }

    public synchronized ExplorerManager getExplorerManager() {
        if (this.manager == null) {
            this.manager = new ExplorerManager ();
            this.manager.setRootContext(new AbstractNode (getChildren()));
            this.manager.addPropertyChangeListener (this);
        }
        return manager;
    }

    public void addNotify () {
        super.addNotify();
        this.expandPlatforms (JavaPlatformManager.getDefault().getDefaultPlatform());
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        platforms = new PlatformsView ();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        platformHome = new javax.swing.JTextField();
        clientArea = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        platformName = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_PlatformsCustomizer"));
        platforms.setBorder(new javax.swing.border.EtchedBorder());
        platforms.setPreferredSize(new java.awt.Dimension(220, 400));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 6);
        add(platforms, gridBagConstraints);
        platforms.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AN_PlatformsCustomizerPlatforms"));
        platforms.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_PlatformsCustomizerPlatforms"));

        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("CTL_AddPlatform"));
        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("MNE_AddPlatform").charAt(0));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewPlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 6);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_AddPlatform"));

        removeButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("CTL_Remove"));
        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("MNE_Remove").charAt(0));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_Remove"));

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("MNE_PlatformHome").charAt(0));
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("CTL_PlatformHome"));
        jLabel1.setLabelFor(platformHome);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 3);
        add(jLabel1, gridBagConstraints);

        platformHome.setEditable(false);
        platformHome.setColumns(25);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 6, 12);
        add(platformHome, gridBagConstraints);
        platformHome.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_PlatformHome"));

        clientArea.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 12);
        add(clientArea, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("MNE_PlatformName").charAt(0));
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("CTL_PlatformName"));
        jLabel2.setLabelFor(platformName);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 3);
        add(jLabel2, gridBagConstraints);

        platformName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 3, 6, 12);
        add(platformName, gridBagConstraints);
        platformName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/java/platform/ui/Bundle").getString("AD_PlatformName"));

    }//GEN-END:initComponents

    private void removePlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePlatform
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes.length!=1) {
            assert false : "Illegal number of selected nodes";      //NOI18N
            return;
        }
        DataObject dobj = (DataObject) nodes[0].getLookup().lookup (DataObject.class);
        if (dobj == null) {
            assert false : "Can not find platform definition.";      //NOI18N
            return;
        }
        try {
            dobj.delete();
            this.getChildren().refreshPlatforms();
            this.expandPlatforms(null);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
        }
    }//GEN-LAST:event_removePlatform

    private void addNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewPlatform
        try {
            WizardDescriptor wiz = new WizardDescriptor (PlatformInstallIterator.create());
            DataObject template = DataObject.find (
                    Repository.getDefault().getDefaultFileSystem().findResource(TEMPLATE));
            wiz.putProperty("targetTemplate", template);    //NOI18N
            DataFolder folder = DataFolder.findFolder(
                    Repository.getDefault().getDefaultFileSystem().findResource(STORAGE));
            wiz.putProperty("targetFolder",folder); //NOI18N
            wiz.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            wiz.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
            wiz.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
            wiz.setTitle(NbBundle.getMessage(PlatformsCustomizer.class,"CTL_AddPlatformTitle"));
            wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
            Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
            try {
                dlg.setVisible(true);
                if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
                    this.getChildren().refreshPlatforms();
                    Set result = wiz.getInstantiatedObjects();
                    this.expandPlatforms (result.size() == 0 ? null : (JavaPlatform)result.iterator().next());
                }
            } finally {
                dlg.dispose();
            }
        } catch (DataObjectNotFoundException dfne) {
            ErrorManager.getDefault().notify (dfne);
        }
        catch (IOException ioe) {
            ErrorManager.getDefault().notify (ioe);
        }
    }//GEN-LAST:event_addNewPlatform


    private synchronized PlatformCategoriesChildren getChildren () {
        if (this.children == null) {
            this.children = new PlatformCategoriesChildren ();
        }
        return this.children;
    }

    private void selectPlatform (Node pNode) {
        this.clientArea.removeAll();
        this.jLabel1.setVisible(false);
        this.platformHome.setVisible(false);
        this.jLabel2.setVisible(false);
        this.platformName.setVisible(false);
        this.removeButton.setEnabled (false);
        if (pNode == null) {
            return;
        }        
        JavaPlatform platform = (JavaPlatform) pNode.getLookup().lookup(JavaPlatform.class);
        if (platform != null) {
            this.jLabel1.setVisible(true);
            this.platformHome.setVisible(true);
            this.jLabel2.setVisible(true);
            this.platformName.setVisible(true);
            this.removeButton.setEnabled (isDefaultPLatform(platform));
            this.platformName.setText(pNode.getDisplayName());
            Iterator it = platform.getInstallFolders().iterator();
            if (it.hasNext()) {
                File file = FileUtil.toFile ((FileObject)it.next());
                if (file != null) {
                    this.platformHome.setText (file.getAbsolutePath());
                }
            }
        }        
        if (pNode.hasCustomizer()) {
            Component component = pNode.getCustomizer();
            if (component != null) {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = c.gridy = GridBagConstraints.RELATIVE;
                c.gridheight = c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.BOTH;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.weightx = c.weighty = 1.0;
                ((GridBagLayout)this.clientArea.getLayout()).setConstraints (component,c);
                this.clientArea.add (component);
            }
        }
        this.clientArea.revalidate();
    }

    private static boolean isDefaultPLatform (JavaPlatform platform) {
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        return defaultPlatform!=null && !defaultPlatform.equals(platform);
    }

    private void expandPlatforms (JavaPlatform platform) {
        ExplorerManager mgr = this.getExplorerManager();
        Node node = mgr.getRootContext();
        expandAllNodes(this.platforms, node, mgr, platform);
    }

    private static void expandAllNodes (BeanTreeView btv, Node node, ExplorerManager mgr, JavaPlatform platform) {
        btv.expandNode (node);
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {
            if (platform != null && platform.equals(node.getLookup().lookup(JavaPlatform.class))) {
                try {
                    mgr.setSelectedNodes (new Node[] {node});
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
            }
            return;
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i], mgr, platform);
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel clientArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField platformHome;
    private javax.swing.JTextField platformName;
    private org.openide.explorer.view.BeanTreeView platforms;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    
    private static class PlatformsView extends BeanTreeView {
        
        public PlatformsView () {
            super ();
            this.setPopupAllowed (false);
            this.setDefaultActionAllowed(false);
            this.setRootVisible (false);
            this.tree.setEditable(false);
            this.tree.setShowsRootHandles(false);
        }
        
    }
    
    private static class PlatformCategoriesDescriptor implements Comparable {
        private final String categoryName;
        private final List/*<Node>*/ platforms;
        
        public PlatformCategoriesDescriptor (String categoryName) {
            assert categoryName != null;
            this.categoryName = categoryName;
            this.platforms = new ArrayList ();
        }
        
        public String getName () {
            return this.categoryName;
        }
        
        public List getPlatform () {
            return Collections.unmodifiableList(this.platforms);
        }
        
        public void add (Node node) {
            this.platforms.add (node);
        }
        
        public int hashCode () {
            return this.categoryName.hashCode ();
        }
        
        public boolean equals (Object other) {
            if (other instanceof PlatformCategoriesDescriptor) {
                PlatformCategoriesDescriptor desc = (PlatformCategoriesDescriptor) other;
                return this.categoryName.equals(desc.categoryName) && 
                this.platforms.size() == desc.platforms.size();
            }
            return false;
        }
        
        public int compareTo(Object other) {
            if (!(other instanceof PlatformCategoriesDescriptor )) {
                throw new IllegalArgumentException ();
            }
            PlatformCategoriesDescriptor desc = (PlatformCategoriesDescriptor) other;
            return this.categoryName.compareTo (desc.categoryName);
        }
        
    }
    
    private static class PlatformsChildren extends Children.Keys {
        
        private List platforms;
        
        public PlatformsChildren (List/*<Node>*/ platforms) {
            this.platforms = platforms;
        }

        protected void addNotify() {
            super.addNotify();
            this.setKeys (this.platforms);
        }

        protected void removeNotify() {
            super.removeNotify();
            this.setKeys(new Object[0]);
        }

        protected Node[] createNodes(Object key) {
            return new Node[] {new FilterNode((Node) key, Children.LEAF)};
        }
    }
    
    private static class PlatformCategoryNode extends AbstractNode {
        
        private final PlatformCategoriesDescriptor desc;
        private Node iconDelegate;
        
        public PlatformCategoryNode (PlatformCategoriesDescriptor desc) {
            super (new PlatformsChildren (desc.getPlatform()));
            this.desc = desc;            
            this.iconDelegate = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }
        
        public String getName () {
            return this.desc.getName ();
        }
        
        public String getDisplayName () {
            return this.getName ();
        }
        
        public Image getIcon(int type) {
            return this.iconDelegate.getIcon(type);
        }        
        
        public Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon (type);
        }                        
        
    }
    
    private static class PlatformCategoriesChildren extends Children.Keys {
        
        protected void addNotify () {
            super.addNotify ();
            this.refreshPlatforms ();
        }
        
        protected void removeNotify () {
            super.removeNotify ();
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof PlatformCategoriesDescriptor) {
                PlatformCategoriesDescriptor desc = (PlatformCategoriesDescriptor) key;
                return new Node[] {
                    new PlatformCategoryNode (desc)
                };
            }
            else if (key instanceof Node) {
                return new Node[] {
                    new FilterNode ((Node)key,Children.LEAF)
                };
            }
            else {
                return new Node[0];
            }
        }       
        
        private void refreshPlatforms () {
            FileObject storage = Repository.getDefault().getDefaultFileSystem().findResource(STORAGE);
            if (storage != null) {
                HashMap/*<String,PlatformCategoriesDescriptor>*/ categories = new HashMap ();
                FileObject[] children = storage.getChildren();
                for (int i=0; i< children.length; i++) {
                    try {
                        DataObject dobj = DataObject.find (children[i]);
                        Node node = dobj.getNodeDelegate();
                        JavaPlatform platform = (JavaPlatform) node.getLookup().lookup(JavaPlatform.class);
                        if (platform != null) {
                            String platformType = platform.getSpecification().getName();
                            if (platformType != null) {
                                platformType = platformType.toUpperCase();
                                PlatformCategoriesDescriptor platforms = (PlatformCategoriesDescriptor) categories.get (platformType);
                                if (platforms == null ) {
                                    platforms = new PlatformCategoriesDescriptor (platformType);
                                    categories.put (platformType, platforms);
                                }
                                platforms.add (node);
                            }
                            else {
                                ErrorManager.getDefault().log ("Platform: "+ platform.getDisplayName() +" has invalid specification.");  //NOI18N
                            }
                        }
                        else {                        
                            ErrorManager.getDefault().log ("Platform node for : "+node.getDisplayName()+" has no platform in its lookup.");   //NOI18N
                        }                    
                    }catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                 }                                    
                List keys = new ArrayList (categories.values());
//                if (keys.size() == 1) {
//                    PlatformCategoriesDescriptor desc = (PlatformCategoriesDescriptor) keys.get(0);
//                    this.setKeys (desc.getPlatform());
//                }
//                else {
                    Collections.sort (keys);
                    this.setKeys(keys);
//                }
            }
        }
        

    }

}
