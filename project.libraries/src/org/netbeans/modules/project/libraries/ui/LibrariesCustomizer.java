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

package org.netbeans.modules.project.libraries.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

public final class LibrariesCustomizer extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {    
    
    private ExplorerManager manager;
    private LibrariesModel model;
    private BeanTreeView libraries;
    private LibraryStorageArea libraryStorageArea;

    public LibrariesCustomizer (LibraryStorageArea libraryStorageArea) {
        this.model = new LibrariesModel ();
        this.libraryStorageArea = (libraryStorageArea != null ? libraryStorageArea : LibrariesModel.GLOBAL_AREA);
        initComponents();
        postInitComponents ();
        expandTree();
    }
    
    private void expandTree() {
        // get first library node
        Node[] n = getExplorerManager().getRootContext().getChildren().getNodes()[0].getChildren().getNodes();
        if (n.length != 0) {
            try {
                getExplorerManager().setSelectedNodes(new Node[]{n[0]});
            } catch (PropertyVetoException ex) {
                // OK to ignore - it is just selection initialization
            }
        }
    }
    
    public void setLibraryStorageArea(LibraryStorageArea libraryStorageArea) {
        this.libraryStorageArea = (libraryStorageArea != null ? libraryStorageArea : LibrariesModel.GLOBAL_AREA);
        forceTreeRecreation();
        expandTree();
    }
    
    public LibrariesModel getModel() {
        return model;
    }
    
    public void hideLibrariesList() {
        libsPanel.setVisible(false);
        jLabel2.setVisible(false);
        createButton.setVisible(false);
        deleteButton.setVisible(false);
    }
    
    /**
     * Force nodes recreation after LibrariesModel change. The nodes listen on
     * model and eventually refresh themselves but usually it is too late.
     * So forcing recreation makes sure that any subsequent call to 
     * NodeOp.findPath is successful and selects just created library node.
     */
    public void forceTreeRecreation() {
        getExplorerManager().setRootContext(buildTree());
    }

    public void setSelectedLibrary (LibraryImplementation library) {
        if (library == null)
            return;
        ExplorerManager currentManager = this.getExplorerManager();
        Node root = currentManager.getRootContext();        
        String[] path = {library.getType(), library.getName()};
        try {
            Node node = NodeOp.findPath(root, path);
            if (node != null) {
                currentManager.setSelectedNodes(new Node[] {node});
            }
        } catch (NodeNotFoundException e) {
            //Ignore it
        }
        catch (PropertyVetoException e) {
            //Ignore it
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( LibrariesCustomizer.class );
    }
    
    public boolean apply () {
        try {
            this.model.apply();
            return true;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        }
    }

    public void addNotify() {
        super.addNotify();
        expandAllNodes(this.libraries,this.getExplorerManager().getRootContext());
        //Select first library if nothing selected
        if (this.getExplorerManager().getSelectedNodes().length == 0) {
            SELECTED: for (Node areaNode : getExplorerManager().getRootContext().getChildren().getNodes(true)) {
                for (Node typeNode : areaNode.getChildren().getNodes(true)) {
                    for (Node libNode : typeNode.getChildren().getNodes(true)) {
                        try {
                            getExplorerManager().setSelectedNodes(new Node[] {libNode});
                        } catch (PropertyVetoException e) {
                            //Ignore it
                        }
                        break SELECTED;
                    }
                }
            }
        }
        this.libraries.requestFocus();
    }    
    
    public ExplorerManager getExplorerManager () {
        if (this.manager == null) {
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener (new PropertyChangeListener() {
                public void propertyChange (PropertyChangeEvent event) {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue ();
                        selectLibrary(nodes);                            
                        libraries.requestFocus();
                    }                    
                }
            });
            this.manager.addVetoableChangeListener(new VetoableChangeListener() {
                public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue();
                        if (nodes.length <=1) {
                            return;
                        }
                        else {
                            throw new PropertyVetoException ("Invalid length", event);  //NOI18N
                        }
                    }
                }
            });            
            manager.setRootContext(buildTree());
        }
        return this.manager;
    }

    private void postInitComponents () {
        this.libraries = new LibrariesView ();        
        GridBagConstraints c = new GridBagConstraints ();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;        
        ((GridBagLayout)this.libsPanel.getLayout()).setConstraints(this.libraries,c);
        this.libsPanel.add(this.libraries);
        this.libraries.setPreferredSize(new Dimension (200,334));
        this.libraryName.setColumns(25);
        this.libraryName.setEnabled(false);
        this.libraryName.addActionListener(
                new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        nameChanged();
                    }
                });                        
    }

    private void nameChanged () {
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            LibraryImplementation lib = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (lib == null) {
                return;
            }
            String newName = this.libraryName.getText();
            if (newName.equals(lib.getName())) {
                return;
            }
            if (newName.length () == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_InvalidName"),
                        NotifyDescriptor.ERROR_MESSAGE));
            } else if (isValidName(model, newName, model.getArea(lib))) {
                lib.setName(newName);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_ExistingName", newName),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }                        
    }

    private void selectLibrary (Node[] nodes) {
        int tabCount = this.properties.getTabCount();
        for (int i=0; i<tabCount; i++) {
            this.properties.removeTabAt(0);
        }
        this.libraryName.setEnabled(false);
        this.libraryName.setText("");   //NOI18N
        this.jLabel1.setVisible(false);
        this.libraryName.setVisible(false);
        this.properties.setVisible(false);
        this.deleteButton.setEnabled(false);        
        if (nodes.length != 1) {
            return;
        }
        LibraryImplementation impl = nodes[0].getLookup().lookup(LibraryImplementation.class);
        if (impl == null) {
            return;
        }
        this.jLabel1.setVisible(true);
        this.libraryName.setVisible(true);
        this.properties.setVisible(true);
        boolean editable = model.isLibraryEditable (impl);
        this.libraryName.setEnabled(editable);
        this.deleteButton.setEnabled(editable);
        this.libraryName.setText (getLocalizedString(impl.getLocalizingBundle(),impl.getName()));
        LibraryTypeProvider provider = nodes[0].getLookup().lookup(LibraryTypeProvider.class);
        if (provider == null)
            return;
        // a library customizer needs to know location of sharable library in order to
        // relativize paths. that's why object implementing both LibraryImplementation
        // and LibraryStorageArea is passed to JComponent here:
        LibraryStorageArea area = nodes[0].getLookup().lookup(LibraryStorageArea.class);
        if (area != null && area != LibrariesModel.GLOBAL_AREA) {
            impl = new LibraryImplementationWrapper(impl, area);
        }

        String[] volumeTypes = provider.getSupportedVolumeTypes();
        for (int i=0; i< volumeTypes.length; i++) {
            Customizer c = provider.getCustomizer (volumeTypes[i]);
            if (c instanceof JComponent) {
                c.setObject (impl);
                JComponent component = (JComponent) c;
                component.setEnabled (editable);
                String tabName = component.getName();
                if (tabName == null) {
                    tabName = volumeTypes[i];
                }
                this.properties.addTab(tabName, component);
            }
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        libraryName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        properties = new javax.swing.JTabbedPane();
        createButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        libsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(642, 395));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(libraryName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_CustomizerLibraryName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jLabel1, gridBagConstraints);

        libraryName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(libraryName, gridBagConstraints);
        libraryName.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibraryName")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        properties.setPreferredSize(new java.awt.Dimension(400, 300));
        jPanel1.add(properties, java.awt.BorderLayout.CENTER);
        properties.getAccessibleContext().setAccessibleName(bundle.getString("AN_LibrariesCustomizerProperties")); // NOI18N
        properties.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizerProperties")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, bundle.getString("CTL_NewLibrary")); // NOI18N
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibrary(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(createButton, gridBagConstraints);
        createButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_NewLibrary")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, bundle.getString("CTL_DeleteLibrary")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibrary(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(deleteButton, gridBagConstraints);
        deleteButton.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_DeleteLibrary")); // NOI18N

        libsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        libsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        add(libsPanel, gridBagConstraints);
        libsPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_libsPanel")); // NOI18N

        jLabel2.setLabelFor(libsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("TXT_LibrariesPanel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 12);
        add(jLabel2, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(bundle.getString("AD_LibrariesCustomizer")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void deleteLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrary
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1) {
            LibraryImplementation library = nodes[0].getLookup().lookup(LibraryImplementation.class);
            if (library == null) {
                return;
            }
            Node[] sib = nodes[0].getParentNode().getChildren().getNodes(true);            
            Node selNode = null;
            for (int i=0; i < sib.length; i++) {
                if (nodes[0].equals(sib[i])) {
                    if (i>0) {
                        selNode = sib[i-1];
                    }
                    else if (i<sib.length-1){
                        selNode = sib[i+1];
                    }
                }
            }            
            model.removeLibrary(library);
            try {
                if (selNode != null) {
                    this.getExplorerManager().setSelectedNodes(new Node[] {selNode});            
                }
            } catch (PropertyVetoException e) {
                //Ignore it
            }
            this.libraries.requestFocus();
        }
    }//GEN-LAST:event_deleteLibrary

    private void createLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibrary
        Dialog dlg = null;
        try {
            String preselectedLibraryType = null;
            LibraryStorageArea area = null;
            Node[] preselectedNodes = this.getExplorerManager().getSelectedNodes();
            if (preselectedNodes.length == 1) {
                LibraryTypeProvider provider = preselectedNodes[0].getLookup().lookup(LibraryTypeProvider.class);
                if (provider != null) {
                    preselectedLibraryType = provider.getLibraryType();
                }
                area = preselectedNodes[0].getLookup().lookup(LibraryStorageArea.class);
            }
            if (area == null) {
                area = LibrariesModel.GLOBAL_AREA;
            }
            NewLibraryPanel p = new NewLibraryPanel(model, preselectedLibraryType, area);
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(LibrariesCustomizer.class,"CTL_CreateLibrary"),
                    true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
            p.setDialogDescriptor(dd);
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                String libraryType = p.getLibraryType();
                String currentLibraryName = p.getLibraryName();
                LibraryImplementation impl;
                if (area != LibrariesModel.GLOBAL_AREA) {
                    impl = model.createArealLibrary(libraryType, currentLibraryName, area);
                } else {
                    LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider(libraryType);
                    if (provider == null) {
                        return;
                    }
                    impl = provider.createLibrary();
                    impl.setName(currentLibraryName);
                }
                model.addLibrary (impl);                
                forceTreeRecreation();
                String[] path = {impl.getType(), impl.getName()};
                ExplorerManager mgr = this.getExplorerManager();
                try {
                    Node node = NodeOp.findPath(mgr.getRootContext(),path);
                    if (node != null) {
                        mgr.setSelectedNodes(new Node[] {node});
                    }
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
                catch (NodeNotFoundException e) {
                    //Ignore it
                }
                this.libraryName.requestFocus();
                this.libraryName.selectAll();
            }
            else {
                this.libraries.requestFocus();
            }
        }
        finally {
            if (dlg != null)
                dlg.dispose();
        }
    }//GEN-LAST:event_createLibrary

    static boolean isValidName(LibrariesModel model, String name, LibraryStorageArea area) {
        for (LibraryImplementation lib : model.getLibraries()) {
            if (lib.getName().equals(name) && Utilities.compareObjects(model.getArea(lib), area)) {
                return false;
            }
        }
        return true;
    }

    static String getLocalizedString (String bundleResourceName, String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mre);
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField libraryName;
    private javax.swing.JPanel libsPanel;
    private javax.swing.JTabbedPane properties;
    // End of variables declaration//GEN-END:variables

    private static void expandAllNodes (BeanTreeView btv, Node node) {
        btv.expandNode (node);
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {            
            return;
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i]);
        }

    }
    
    private static class LibrariesView extends BeanTreeView {
        
        public LibrariesView () {
            super ();
            this.setRootVisible(false);
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.tree.setEditable (false);
            this.tree.setShowsRootHandles (false);
        }
        
    }
    
    private class AreaChildren extends Children.Keys<LibraryStorageArea> implements ChangeListener {

        @Override
        protected void addNotify() {
            super.addNotify();
            model.addChangeListener(this);
            computeKeys();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            model.removeChangeListener(this);
            setKeys(Collections.<LibraryStorageArea>emptySet());
        }

        private void computeKeys() {
            setKeys(getSortedAreas(model));
        }

        protected Node[] createNodes(LibraryStorageArea area) {
            return new Node[] {new AreaNode(area)};
        }

        public void stateChanged(ChangeEvent e) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    computeKeys();
                }
            });
        }

    }

    static Collection<? extends LibraryStorageArea> getSortedAreas(LibrariesModel model) {
        List<LibraryStorageArea> areas = new ArrayList<LibraryStorageArea>(model.getAreas());
        Collections.sort(areas,new Comparator<LibraryStorageArea>() {
            Collator COLL = Collator.getInstance();
            public int compare(LibraryStorageArea a1, LibraryStorageArea a2) {
                return COLL.compare(a1.getDisplayName(), a2.getDisplayName());
            }
        });
        areas.add(0, LibrariesModel.GLOBAL_AREA);
        assert !areas.contains(null);
        return areas;
    }

    private final class AreaNode extends AbstractNode {

        private final LibraryStorageArea area;

        AreaNode(LibraryStorageArea area) {
            super(new TypeChildren(area), Lookups.singleton(area));
            this.area = area;
        }

        @Override
        public String getName() {
            return getDisplayName();
        }

        @Override
        public String getDisplayName() {
            return area.getDisplayName();
        }

        private Node delegate() {
            return DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }

        public Image getIcon(int type) {
            return delegate().getIcon(type);
        }

        public Image getOpenedIcon(int type) {
            return delegate().getOpenedIcon(type);
        }

    }

    private class TypeChildren extends Children.Keys<LibraryTypeProvider> {

        private final LibraryStorageArea area;

        TypeChildren(LibraryStorageArea area) {
            this.area = area;
        }

        public void addNotify () {
            // Could also filter by area (would then need to listen to model too)
            this.setKeys(LibraryTypeRegistry.getDefault().getLibraryTypeProviders());
        }
        
        public void removeNotify () {
            this.setKeys(new LibraryTypeProvider[0]);
        }
        
        protected Node[] createNodes(LibraryTypeProvider provider) {
            return new Node[] {new CategoryNode(provider, area)};
        }
        
    }
    
    private class CategoryNode extends AbstractNode {
        
        private LibraryTypeProvider provider;
        private Node iconDelegate;
                
        public CategoryNode(LibraryTypeProvider provider, LibraryStorageArea area) {
            super(new CategoryChildren(provider, area), Lookups.fixed(provider, area));
            this.provider = provider;       
            this.iconDelegate = DataFolder.findFolder (Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }
        
        public String getName () {
            return provider.getLibraryType ();
        }
        
        public String getDisplayName() {
            return this.provider.getDisplayName();
        }
        
        public Image getIcon(int type) {            
            return this.iconDelegate.getIcon (type);
        }        
        
        public Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon (type);
        }        
                        
    }    

    private class CategoryChildren extends Children.Keys<LibraryImplementation> implements ChangeListener {
        
        private LibraryTypeProvider provider;
        private final LibraryStorageArea area;
        
        public CategoryChildren(LibraryTypeProvider provider, LibraryStorageArea area) {
            this.provider = provider;
            this.area = area;
            model.addChangeListener(this);
        }
        
        public void addNotify () {
            Collection<LibraryImplementation> keys = new ArrayList<LibraryImplementation>();
            for (LibraryImplementation impl : model.getLibraries()) {
                if (provider.getLibraryType().equals(impl.getType()) && model.getArea(impl).equals(area)) {
                    keys.add (impl);
                }
            }
            this.setKeys(keys);
        }
        
        public void removeNotify () {
            this.setKeys(new LibraryImplementation[0]);
        }
        
        protected Node[] createNodes(LibraryImplementation impl) {
            return new Node[] {new LibraryNode(impl, provider, area)};
        }
        
        public void stateChanged(ChangeEvent e) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    addNotify();
                }
            });
        }
        
    }
    
    private static class LibraryNode extends AbstractNode {
        
        private static final String ICON = "org/netbeans/modules/project/libraries/resources/libraries.gif";  //NOI18N
        
        private LibraryImplementation lib;
        private LibraryTypeProvider provider;
        
        public LibraryNode(LibraryImplementation lib, LibraryTypeProvider provider, LibraryStorageArea area) {
            super(Children.LEAF, Lookups.fixed(lib, provider, area));
            this.lib = lib;
            this.provider = provider;
            this.setIconBaseWithExtension(ICON);
        }
        
        public String getName () {            
            return this.lib.getName ();
        }
        
        public String getDisplayName () {
            return getLocalizedString(this.lib.getLocalizingBundle(), this.lib.getName());
        }
        
    }
    
    private Node buildTree() {
        return new AbstractNode(new TypeChildren(libraryStorageArea));
    }

    private static class LibraryImplementationWrapper implements LibraryImplementation, LibraryStorageArea {
        
        private LibraryImplementation lib;
        private LibraryStorageArea area;
        
        public LibraryImplementationWrapper(LibraryImplementation lib, LibraryStorageArea area) {
            this.lib =  lib;
            this.area = area;
        }

        public String getType() {
            return lib.getType();
        }

        public String getName() {
            return lib.getName();
        }

        public String getDescription() {
            return lib.getDescription();
        }

        public String getLocalizingBundle() {
            return lib.getLocalizingBundle();
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            return lib.getContent(volumeType);
        }

        public void setName(String name) {
            lib.setName(name);
        }

        public void setDescription(String text) {
            lib.setDescription(text);
        }

        public void setLocalizingBundle(String resourceName) {
            lib.setLocalizingBundle(resourceName);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            lib.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            lib.removePropertyChangeListener(l);
        }

        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            lib.setContent(volumeType, path);
        }

        public URL getLocation() {
            return area.getLocation();
        }

        public String getDisplayName() {
            return area.getDisplayName();
        }
    }
    
}
