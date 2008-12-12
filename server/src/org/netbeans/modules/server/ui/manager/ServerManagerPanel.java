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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.modules.server.ui.wizard.AddServerInstanceWizard;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.nodes.Node;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
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

    private static final String SERVERS_ICON = "org/netbeans/modules/server/ui/resources/servers.png"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(ServerManagerPanel.class.getName());

    private static final Dimension MINIMUM_SIZE = new Dimension(750, 450);

    //private ServerCategoriesChildren children;
    private ServersChildren children;
    private ExplorerManager manager;
    private ServerInstance initialInstance;

    /** Creates new form PlatformsCustomizer */
    public ServerManagerPanel(ServerInstance initialInstance) {
        initComponents();
        serverName.setColumns(30);
        serverType.setColumns(30);
        // set the preferred width, height is not very important here
        serversView.setPreferredSize(new Dimension(200,200));
        this.initialInstance = initialInstance;
        setPreferredSize(MINIMUM_SIZE);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        expandServers(initialInstance);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length != 1) {
                selectServer(null);
            } else {
                selectServer(nodes[0]);
            }
        }
    }

    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes.length != 1) {
                throw new PropertyVetoException("Invalid length", evt);   //NOI18N
            }
        }
    }

    public synchronized ExplorerManager getExplorerManager() {
        if (this.manager == null) {
            this.manager = new ExplorerManager();
            this.manager.setRootContext(new ServersNode(Children.create(getChildren(), false)));
            this.manager.addPropertyChangeListener(this);
            this.manager.addVetoableChangeListener(this);
        }
        return manager;
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
        serversView = new org.openide.explorer.view.BeanTreeView();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        cardsPanel = new javax.swing.JPanel();
        emptyPanel = new javax.swing.JPanel();
        customizerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        serverName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        serverType = new javax.swing.JTextField();
        clientPanel = new javax.swing.JPanel();
        serversLabel = new javax.swing.JLabel();

        serversView.setBorder(UIManager.getBorder("Nb.ScrollPane.border"));
        serversView.setPopupAllowed(false);
        serversView.setPreferredSize(new java.awt.Dimension(220, 400));
        serversView.setSelectionMode(0);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(ServerManagerPanel.class, "CTL_AddServer")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServer(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(ServerManagerPanel.class, "CTL_Remove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeServer(evt);
            }
        });

        cardsPanel.setLayout(new java.awt.CardLayout());

        emptyPanel.setLayout(new java.awt.GridBagLayout());
        cardsPanel.add(emptyPanel, "card3");

        customizerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(serverName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(ServerManagerPanel.class, "CTL_ServerName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        customizerPanel.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_ServerName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_ServerName")); // NOI18N

        serverName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        customizerPanel.add(serverName, gridBagConstraints);
        serverName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_ServerName")); // NOI18N
        serverName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_ServerName")); // NOI18N

        jLabel2.setLabelFor(serverType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(ServerManagerPanel.class, "CTL_ServerType")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        customizerPanel.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_ServerType")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_ServerType")); // NOI18N

        serverType.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        customizerPanel.add(serverType, gridBagConstraints);
        serverType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_ServerType")); // NOI18N
        serverType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_ServerType")); // NOI18N

        clientPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        customizerPanel.add(clientPanel, gridBagConstraints);

        cardsPanel.add(customizerPanel, "card2");

        serversLabel.setLabelFor(serversView);
        org.openide.awt.Mnemonics.setLocalizedText(serversLabel, org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "CTL_Servers")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serversLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(serversView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(addButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(removeButton)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cardsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(serversLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, cardsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, serversView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(removeButton))
                .addContainerGap())
        );

        serversView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_InstalledServers")); // NOI18N
        serversView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_InstalledServers")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_AddServer")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_AddServer")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCN_Remove")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ASCD_Remove")); // NOI18N
        serversLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ACSN_ServerList")); // NOI18N
        serversLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ACSD_ServerList")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ACSN_ServerManager")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerManagerPanel.class, "ACSD_ServerManager")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removeServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeServer
        Node[] nodes = getExplorerManager().getSelectedNodes();

        if (nodes[0] instanceof ServerNode) {
            ServerInstance serverInstance = ((ServerNode) nodes[0]).getServerInstance();
            if (serverInstance.isRemovable()) {
                serverInstance.remove();
                getChildren().refresh();
                expandServers(null);
            }
        }
    }//GEN-LAST:event_removeServer

    private void addServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServer
        final ServerInstance instance = AddServerInstanceWizard.showAddServerInstanceWizard();
        if (instance != null) {
            getChildren().refresh();
            expandServers(instance);
        }
    }//GEN-LAST:event_addServer


        private synchronized ServersChildren getChildren() {
            if (this.children == null) {
                this.children = new ServersChildren();
            }
            return this.children;
        }

        private void selectServer(Node aNode) {
            clientPanel.removeAll();

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
                ((CardLayout)cardsPanel.getLayout()).first(cardsPanel);
                return;
            }

            if (serverInstance.getCustomizer() != null) {
                Component component = serverInstance.getCustomizer();
                if (component != null) {
                    addComponent(clientPanel, component);
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
            CardLayout cl = (CardLayout)cardsPanel.getLayout();
            cl.last(cardsPanel);
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
        expandAllNodes(serversView, node, mgr, servInst);
    }

    private static void expandAllNodes(BeanTreeView btv, Node node, ExplorerManager mgr, ServerInstance servInst) {
        Children ch = node.getChildren();

        // preselect node for the specified server instance
        if (servInst != null && ch == Children.LEAF && node instanceof ServerNode) {
            try {
                if (((ServerNode)node).getServerInstance() == servInst) {
                    mgr.setSelectedNodes(new Node[] {node});
                }
            } catch (PropertyVetoException e) {
                // Ignore it
                LOGGER.log(Level.FINE, null, e);
            }
        }

        // preselect first server
        if (servInst == null && ch == Children.LEAF && mgr.getSelectedNodes().length == 0) {
            try {
                mgr.setSelectedNodes(new Node[] {node});
            } catch (PropertyVetoException e) {
                //Ignore it
                LOGGER.log(Level.FINE, null, e);
            }
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i], mgr, servInst);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel cardsPanel;
    private javax.swing.JPanel clientPanel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField serverName;
    private javax.swing.JTextField serverType;
    private javax.swing.JLabel serversLabel;
    private org.openide.explorer.view.BeanTreeView serversView;
    // End of variables declaration//GEN-END:variables


    private static class ServersNode extends AbstractNode {

        public ServersNode(Children children) {
            super(children);

            setName(""); // NOI18N
            setDisplayName(NbBundle.getMessage(ServerManagerPanel.class, "Server_Registry_Node_Name"));
            setShortDescription(NbBundle.getMessage(ServerManagerPanel.class, "Server_Registry_Node_Short_Description"));
            setIconBaseWithExtension(SERVERS_ICON);
        }


    }

    private static class ServersChildren extends ChildFactory<ServerInstance> {

        private static final Comparator<ServerInstance> COMPARATOR = new InstanceComparator();

        public ServersChildren() {
            super();
        }

        protected final void refresh() {
            refresh(true);
        }

        @Override
        protected Node createNodeForKey(ServerInstance key) {
            return new ServerNode(key);
        }


        @Override
        protected boolean createKeys(List<ServerInstance> toPopulate) {
            List<ServerInstance> fresh = new ArrayList<ServerInstance>();
            for (ServerInstanceProvider provider : ServerRegistry.getInstance().getProviders()) {
                // need to make sure instances with null display names do not
                // end up in fresh...  152834
                for (ServerInstance instance : provider.getInstances()) {
                    if (null != instance.getDisplayName()) {
                        fresh.add(instance);
                    } else {
                        LOGGER.finer("found instance with a null display name: "+
                                instance.getServerDisplayName() + "  " +
                                instance.toString());
                    }
                }
                //fresh.addAll(provider.getInstances());
            }

            Collections.sort(fresh, COMPARATOR);

            toPopulate.addAll(fresh);
            return true;
        }
    }

    private static class ServerNode extends FilterNode {

        private final ServerInstance serverInstance;

        public ServerNode(ServerInstance serverInstance) {
            super(serverInstance.getBasicNode());
            this.serverInstance = serverInstance;
            setChildren(Children.LEAF);
        }

        public ServerInstance getServerInstance() {
            return serverInstance;
        }
    }

    private static class InstanceComparator implements Comparator<ServerInstance>, Serializable {

        public int compare(ServerInstance o1, ServerInstance o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }
}

