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
package org.netbeans.modules.websvc.core.webservices.ui.panels;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.websvc.core.ProjectClientView;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;

import org.openide.DialogDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;

/**
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public class ClientExplorerPanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {
    
    private DialogDescriptor descriptor;
    private ExplorerManager manager;
    private BeanTreeView treeView;
    private FileObject srcFileObject;
    private Node selectedMethod;
    private Project[] projects;
    private Children rootChildren;
    private Node explorerClientRoot;
    private List<Node> projectNodeList;
    
    public ClientExplorerPanel(FileObject srcFileObject) {
        this.srcFileObject=srcFileObject;
        projects = OpenProjects.getDefault().getOpenProjects();
        rootChildren = new Children.Array();
        explorerClientRoot = new AbstractNode(rootChildren);
        projectNodeList = new ArrayList<Node>();
        manager = new ExplorerManager();
        selectedMethod = null;
        
        initComponents();
        initUserComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblTreeView = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLblTreeView, NbBundle.getMessage(ClientExplorerPanel.class, "LBL_AvailableWebServices")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(jLblTreeView, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblTreeView;
    // End of variables declaration//GEN-END:variables
    
    private void initUserComponents() {
        treeView = new BeanTreeView();
        treeView.setRootVisible(false);
        treeView.setPopupAllowed(false);
        
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(treeView, gridBagConstraints);
        jLblTreeView.setLabelFor(treeView.getViewport().getView());
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ClientExplorerPanel.class, "ACSD_AvailableWebServicesTree"));
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClientExplorerPanel.class, "ACSD_AvailableWebServicesTree"));
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public void addNotify() {
        super.addNotify();
        manager.addPropertyChangeListener(this);
        
        for (int i=0;i<projects.length;i++) {
            Project srcFileProject = FileOwnerQuery.getOwner(srcFileObject);
            if (srcFileProject!=null && JaxWsUtils.isProjectReferenceable(projects[i], srcFileProject)) {
                LogicalViewProvider logicalProvider = (LogicalViewProvider)projects[i].getLookup().lookup(LogicalViewProvider.class);
                if (logicalProvider!=null) {
                    Node rootNode = logicalProvider.createLogicalView();
                    Node servicesNode = ProjectClientView.createClientView(projects[i]);
                    if (servicesNode!=null) {
                        Children children = new Children.Array();
                        Node[] nodes= servicesNode.getChildren().getNodes();
                        if (nodes!=null && nodes.length>0) {
                            //jaxWsServices=true;
                            Node[] filterNodes = new Node[nodes.length];
                            for (int j=0;j<nodes.length;j++) filterNodes[j] = new FilterNode(nodes[j]);
                            children.add(filterNodes);
                            projectNodeList.add(new ProjectNode(children, rootNode));
                        }
                    }
                }
            }
            
        }
        Node[] projectNodes = new Node[projectNodeList.size()];
        projectNodeList.<Node>toArray(projectNodes);
        rootChildren.add(projectNodes);
        manager.setRootContext(explorerClientRoot);
        treeView.expandAll();
        
        // !PW If we preselect a node, this can go away.
        descriptor.setValid(false);
    }
    
    public void removeNotify() {
        manager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    public void setDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    public Node getSelectedMethod() {
        return selectedMethod;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getSource() == manager) {
            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node nodes[] = manager.getSelectedNodes();
                if(nodes != null && nodes.length > 0 ) {
                    Node node = nodes[0];
                    InvokeOperationCookie invokeCookie = WebServiceActionProvider.getInvokeOperationAction(srcFileObject);
                    if(invokeCookie != null){
                        if(invokeCookie.isWebServiceOperation(node)) {
                            // This is a method node.
                            selectedMethod = node;
                            descriptor.setValid(true);
                        } else {
                            // This is not a method node.
                            selectedMethod = null;
                            descriptor.setValid(false);
                        }
                    }else{
                        selectedMethod = null;
                        descriptor.setValid(false);
                    }
                }
            }
        }
    }
    
    private class ProjectNode extends AbstractNode {
        private Node rootNode;
        
        ProjectNode(Children children, Node rootNode) {
            super(children);
            this.rootNode=rootNode;
            setName(rootNode.getDisplayName());
        }
        
        public Image getIcon(int type) {
            return rootNode.getIcon(type);
        }
        
        public Image getOpenedIcon(int type) {
            return rootNode.getOpenedIcon(type);
        }
        
    }
    
}
