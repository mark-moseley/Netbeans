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

package org.netbeans.modules.server.ui.manager;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.UIManager;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.modules.server.ui.wizard.AddServerInstanceWizard;
import org.netbeans.spi.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.nodes.Node;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.util.NbBundle;



/**
 * Servers customizer displays a list of registered server and allows to add,
 * remove and configure them.
 *
 * @author  Stepan Herold
 */
public class ServerManagerPanel extends javax.swing.JPanel implements PropertyChangeListener, VetoableChangeListener, ExplorerManager.Provider {

    private static final Dimension MINIMUM_SIZE = new Dimension(720, 400);

    private ServerCategoriesChildren children;
    private ExplorerManager manager;
    private ServerInstance initialInstance;

    /** Creates new form PlatformsCustomizer */
    public ServerManagerPanel(ServerInstance initialInstance) {
        initComponents();
        serverName.setColumns(30);
        serverType.setColumns(30);
        // set the preferred width, height is not very important here
        servers.setPreferredSize(new Dimension(200,200));
        this.initialInstance = initialInstance;
        setPreferredSize(MINIMUM_SIZE);
    }


    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
        Node[] nodes = (Node[]) evt.getNewValue();
        if (nodes.length!=1) {
            selectServer(null);
        } else {
            selectServer(nodes[0]);
        }
        }
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length>1) {
                throw new PropertyVetoException("Invalid length",evt);   //NOI18N
            }
        }
    }

    public synchronized ExplorerManager getExplorerManager() {
        if (this.manager == null) {
            this.manager = new ExplorerManager();
            this.manager.setRootContext(new AbstractNode(getChildren()));
            this.manager.addPropertyChangeListener(this);
            this.manager.addVetoableChangeListener(this);
        }
        return manager;
    }

    public void addNotify() {
        super.addNotify();
        expandServers(initialInstance);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        servers = new PlatformsView ();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        cards = new javax.swing.JPanel();
        messageArea = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        serverName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        serverType = new javax.swing.JTextField();
        clientArea = new javax.swing.JPanel();
        serversLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        servers.setPreferredSize(new java.awt.Dimension(220, 400));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 12, 6, 6);
        add(servers, gridBagConstraints);
        servers.getAccessibleContext().setAccessibleName("null");
        servers.getAccessibleContext().setAccessibleDescription("null");

        org.openide.awt.Mnemonics.setLocalizedText(addButton, "null");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServer(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 12, 0);
        add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleName("null");
        addButton.getAccessibleContext().setAccessibleDescription("null");

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, "null");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeServer(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 6);
        add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleName("null");
        removeButton.getAccessibleContext().setAccessibleDescription("null");

        cards.setLayout(new java.awt.CardLayout());

        messageArea.setLayout(new java.awt.GridBagLayout());
        cards.add(messageArea, "card3");

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(serverName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "null");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName("null");
        jLabel1.getAccessibleContext().setAccessibleDescription("null");

        serverName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(serverName, gridBagConstraints);
        serverName.getAccessibleContext().setAccessibleName("null");
        serverName.getAccessibleContext().setAccessibleDescription("null");

        jLabel2.setLabelFor(serverType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "null");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        jPanel1.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName("null");
        jLabel2.getAccessibleContext().setAccessibleDescription("null");

        serverType.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        jPanel1.add(serverType, gridBagConstraints);
        serverType.getAccessibleContext().setAccessibleName("null");
        serverType.getAccessibleContext().setAccessibleDescription("null");

        clientArea.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(clientArea, gridBagConstraints);

        cards.add(jPanel1, "card2");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 12);
        add(cards, gridBagConstraints);

        serversLabel.setLabelFor(servers);
        org.openide.awt.Mnemonics.setLocalizedText(serversLabel, "null");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(serversLabel, gridBagConstraints);
        serversLabel.getAccessibleContext().setAccessibleName("null");
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/server/ui/manager/Bundle"); // NOI18N
        serversLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ServerList")); // NOI18N

        getAccessibleContext().setAccessibleName("null");
        getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents

    private void removeServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeServer
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes.length!=1) {
            assert false : "Illegal number of selected nodes";      //NOI18N
            return;
        }
        if (nodes[0] instanceof ServerNode) {
            ServerInstance serverInstance = ((ServerNode)nodes[0]).getServerInstance();
            if (serverInstance.isRemovable()) {
                serverInstance.remove();
                getChildren().refreshServers();
                expandServers(null);
            }
        }
    }//GEN-LAST:event_removeServer

    private void addServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServer
        ServerInstance instance = AddServerInstanceWizard.showAddServerInstanceWizard();
        if (instance != null) {
            getChildren().refreshServers();
            if (instance != null) {
                expandServers(instance);
            }
        }
    }//GEN-LAST:event_addServer


        private synchronized ServerCategoriesChildren getChildren() {
            if (this.children == null) {
                this.children = new ServerCategoriesChildren();
            }
            return this.children;
        }

        private void selectServer(Node aNode) {
            clientArea.removeAll();

            ServerInstance serverInstance = null;
            if (aNode instanceof ServerNode) {
                serverInstance = ((ServerNode)aNode).getServerInstance();
                serverName.setText(serverInstance.getDisplayName());
                serverType.setText(serverInstance.getServerDisplayName());
                if (!serverInstance.isRemovable()) {
                    removeButton.setEnabled(false);
                } else {
                    removeButton.setEnabled(true);
                }
            } else {
                removeButton.setEnabled(false);
                ((CardLayout)cards.getLayout()).first(cards);
                return;
            }

            if (serverInstance.getCustomizer() != null) {
                Component component = serverInstance.getCustomizer();
                if (component != null) {
                    addComponent(clientArea, component);
                }
            }
            // handle the correct window size
            int height = getHeight();
            int width = getWidth();
            // reset the preferred size so that it can be computed during revalidation
            setPreferredSize(null);
            revalidate();
            // now we have the new computed preferred size
            Dimension prefSize = getPreferredSize();
            int prefWidth = (int)(prefSize.getWidth() > MINIMUM_SIZE.getWidth() ? prefSize.getWidth() : MINIMUM_SIZE.getWidth());
            int prefHeight = (int)(prefSize.getHeight() > MINIMUM_SIZE.getHeight() ? prefSize.getHeight() : MINIMUM_SIZE.getHeight());
            // do we need to resize the manager window?
            if (prefHeight > height || prefWidth > width) {
                setPreferredSize(new Dimension(prefWidth > width ? prefWidth : width,
                                               prefHeight > height ? prefHeight : height));
                // repack the parent window
                for (Container parent = getParent(); parent != null; parent = parent.getParent()) {
                    if (parent instanceof Window) {
                       ((Window)parent).pack();
                       break;
                    }
                }
            }
            CardLayout cl = (CardLayout)cards.getLayout();
            cl.last(cards);
        }

    private static void addComponent(Container container, Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = GridBagConstraints.RELATIVE;
        c.gridheight = c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = c.weighty = 1.0;
        ((GridBagLayout)container.getLayout()).setConstraints(component,c);
        container.add(component);
    }

    private void expandServers(ServerInstance servInst) {
        ExplorerManager mgr = this.getExplorerManager();
        Node node = mgr.getRootContext();
        expandAllNodes(servers, node, mgr, servInst);
    }

    private static void expandAllNodes(BeanTreeView btv, Node node, ExplorerManager mgr, ServerInstance servInst) {
        btv.expandNode(node);
        Children ch = node.getChildren();

        // preselect node for the specified server instance
        if (servInst != null && ch == Children.LEAF && node instanceof ServerNode) {
            try {
                if (((ServerNode)node).getServerInstance() == servInst) {
                    mgr.setSelectedNodes(new Node[] {node});
                }
            } catch (PropertyVetoException e) {
                //Ignore it
            }
        }

        // preselect first server
        if (servInst == null && ch == Children.LEAF && mgr.getSelectedNodes().length == 0) {
            try {
                mgr.setSelectedNodes(new Node[] {node});
            } catch (PropertyVetoException e) {
                //Ignore it
            }
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i], mgr, servInst);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel cards;
    private javax.swing.JPanel clientArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel messageArea;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField serverName;
    private javax.swing.JTextField serverType;
    private org.openide.explorer.view.BeanTreeView servers;
    private javax.swing.JLabel serversLabel;
    // End of variables declaration//GEN-END:variables


    private static class PlatformsView extends BeanTreeView {

        public PlatformsView() {
            super();
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.setRootVisible(false);
            this.tree.setEditable(false);
            this.tree.setShowsRootHandles(false);
            this.setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N
        }

    }

    private static class ServerCategoriesDescriptor implements Comparable {
        private final String categoryName;
        private final List/*<Node>*/ servers;

        public ServerCategoriesDescriptor(String categoryName) {
            assert categoryName != null;
            this.categoryName = categoryName;
            this.servers = new ArrayList();
        }

        public String getName() {
            return categoryName;
        }

        public List getServers() {
            Collections.sort(servers);
            return Collections.unmodifiableList(servers);
        }

        public void add(Node node) {
            servers.add(node);
        }

        public int hashCode() {
            return categoryName.hashCode();
        }

        public boolean equals(Object other) {
            if (other instanceof ServerCategoriesDescriptor) {
                ServerCategoriesDescriptor desc = (ServerCategoriesDescriptor) other;
                return categoryName.equals(desc.categoryName) &&
                        servers.size() == desc.servers.size();
            }
            return false;
        }

        public int compareTo(Object other) {
            if (!(other instanceof ServerCategoriesDescriptor )) {
                throw new IllegalArgumentException();
            }
            ServerCategoriesDescriptor desc = (ServerCategoriesDescriptor) other;
            return categoryName.compareTo(desc.categoryName);
        }

    }

    private static class ServersChildren extends Children.Keys {

        private List servers;

        public ServersChildren (List/*<Node>*/ servers) {
            this.servers = servers;
        }

        protected void addNotify() {
            super.addNotify();
            this.setKeys (this.servers);
        }

        protected void removeNotify() {
            super.removeNotify();
            this.setKeys(new Object[0]);
        }

        protected Node[] createNodes(Object key) {
            return new Node[] {(Node) key};
        }
    }

    private static class ServerNode extends FilterNode implements Comparable {

        private final ServerInstance serverInstance;

        public ServerNode(ServerInstance serverInstance) {
            super(serverInstance.getNode());
            disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME |
                    DELEGATE_GET_NAME | DELEGATE_SET_NAME);
            this.serverInstance = serverInstance;
            setChildren(Children.LEAF);
            setDisplayName(serverInstance.getDisplayName());
            //setName(serverInstance.getUrl());
        }

        public ServerInstance getServerInstance() {
            return serverInstance;
        }

        public int compareTo(Object other) {
            if (!(other instanceof ServerNode)) {
                throw new IllegalArgumentException();
            }
            return serverInstance.getDisplayName().compareTo(((ServerNode) other).serverInstance.getDisplayName());
        }
    }
    
    private static final String SERVERS_ICON = "org/netbeans/modules/server/ui/resources/servers.png"; // NOI18N    

    private static class ServerCategoryNode extends AbstractNode {

        //private final ServerCategoriesDescriptor desc;
        //private Node iconDelegate;

        public ServerCategoryNode (ServerCategoriesDescriptor desc) {
            super (new ServersChildren (desc.getServers()));
            setDisplayName(desc.getName());
            //this.iconDelegate = DataFolder.findFolder (Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
            setIconBaseWithExtension(SERVERS_ICON);
        }

//        public String getDisplayName () {
//            return desc.getName();
//        }
//
//        public Image getIcon(int type) {
//            return iconDelegate.getIcon(type);
//        }
//
//        public Image getOpenedIcon(int type) {
//            return iconDelegate.getOpenedIcon(type);
//        }
    }

    private static class ServerCategoriesChildren extends Children.Keys {

        protected void addNotify () {
            super.addNotify ();
            this.refreshServers();
        }

        protected void removeNotify () {
            super.removeNotify ();
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof ServerCategoriesDescriptor) {
                ServerCategoriesDescriptor desc = (ServerCategoriesDescriptor) key;
                return new Node[] {
                    new ServerCategoryNode (desc)
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

        private void refreshServers() {
            Collection<ServerInstance> servInstances = new ArrayList<ServerInstance>();
            for (ServerInstanceProvider provider : ServerRegistry.getInstance().getProviders()) {
                servInstances.addAll(provider.getInstances());
            }


            HashMap/*<String,ServerCategoriesDescriptor>*/ categories = new HashMap();

            // currently we have only j2eeServers category
            final String J2EE_SERVERS_CATEGORY = NbBundle.getMessage(ServerManagerPanel.class, "LBL_J2eeServersNode");  // NOI18N
            ServerCategoriesDescriptor j2eeServers = new ServerCategoriesDescriptor(J2EE_SERVERS_CATEGORY);
            for(Iterator it = servInstances.iterator(); it.hasNext();) {
                ServerInstance serverInstance = (ServerInstance)it.next();
                j2eeServers.add(new ServerNode(serverInstance));
            }
            categories.put(J2EE_SERVERS_CATEGORY, j2eeServers);
            List keys = new ArrayList(categories.values());
            // TODO sort by display name
            //Collections.sort(keys);
            this.setKeys(keys);
        }
    }
}

