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
 * ExportDataSourcesDialog.java
 *
 * Created on March 8, 2004, 12:09 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import java.awt.Component;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * A panle to allow the user to configure the EJB business methods
 *
 * @author dongmei cao
 */
public class ConfigureMethodsPanel extends JPanel{
    
    private EjbGroup ejbGroup;
    private MethodDetailPanel methodDetailPanel;
    
    public ConfigureMethodsPanel( EjbGroup ejbGrp ) {
        
        initComponents();
        
        setName( NbBundle.getMessage(ConfigureMethodsPanel.class, "CONFIGURE_METHODS" ) );
        
        // Have the cell renderer to display nice icons
        methodTree.setCellRenderer( new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent( JTree tree,
                                                           Object value,
                                                            boolean sel,
                                                            boolean expanded,
                                                            boolean leaf,
                                                            int row,
                                                             boolean hasFocus) {
            
                super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus);
                
                if( value instanceof EjbGroupTreeNodes.MethodNode ) {
                    // Method node
                    MethodInfo method = ((EjbGroupTreeNodes.MethodNode)value).getMethod();
                    if( method.getReturnType().isCollection() && method.getReturnType().getElemClassName() == null )
                        // Show the warning icon to indicate to the user that extra info needed
                        setIcon( new ImageIcon( Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/warning.png") ) );
                    else
                        setIcon( new ImageIcon( Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif") ) );
                } else {
                    // Must be session ejb node
                    setIcon( new ImageIcon( Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/session_bean.png" ) ) );
                }
            
                return this;
            }
        });
        
        this.ejbGroup = ejbGrp;
        
        setEjbGroup( ejbGrp );
    }
    
    public void setEjbGroup( EjbGroup ejbGrp ) {
        this.ejbGroup = ejbGrp;
        
        EjbGroupTreeNodes nodes = new EjbGroupTreeNodes( ejbGroup );
        DefaultTreeModel treeModel = new DefaultTreeModel( nodes.getRoot() );
        methodTree.setModel( treeModel );
        methodTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // By default, expand all the nodes
        expandAll( true );
        
        // Programatically select a node
        methodTree.setSelectionPath( new TreePath( nodes.geFirstNodeToBeSelected().getPath() ) );
        methodDetailPanel = new MethodDetailPanel( ejbGroup, nodes.geFirstNodeToBeSelected().getMethod() );
        methodPanel.add( methodDetailPanel );
        
        methodTree.addTreeSelectionListener( new TreeSelectionListener() {
            public void valueChanged( TreeSelectionEvent e ) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)methodTree.getLastSelectedPathComponent();
                
                if (node == null) return;
                
                // Set the element class and parameter name modification for the previous selection
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode)e.getOldLeadSelectionPath().getLastPathComponent();
                if( prevNode instanceof EjbGroupTreeNodes.MethodNode )
                {
                    MethodInfo prevMethod = ((EjbGroupTreeNodes.MethodNode)prevNode).getMethod();
                    if( prevMethod.getReturnType().isCollection() )
                        methodDetailPanel.updateColElemClassName();
                    
                    // Programmatically stop the CellEditor so that we do not lose the very last editting value
                    methodDetailPanel.stopLastCellEditing();
                }
                    
                if( node instanceof EjbGroupTreeNodes.MethodNode ) {
                    // A method is selected
                    methodDetailPanel.setMethod( ((EjbGroupTreeNodes.MethodNode)node).getMethod() );
                }
            }
        });
        
    }
    
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll( boolean expand) {
        TreeNode root = (TreeNode)methodTree.getModel().getRoot();
        
        // Traverse tree from root
        expandAll(methodTree, new TreePath(root), expand);
    }
    
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        
        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    
    public MethodDetailPanel getMethodDetailPanel() {
        return this.methodDetailPanel;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectionPanel = new javax.swing.JPanel();
        treeScrollPanel = new javax.swing.JScrollPane();
        methodTree = new javax.swing.JTree();
        methodPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigureMethodsPanel.class, "EXPORT_EJB_DATASOURCES"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureMethodsPanel.class, "EXPORT_EJB_DATASOURCES"));
        selectionPanel.setLayout(new java.awt.BorderLayout());

        selectionPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 10, 10, 10)));
        selectionPanel.setVerifyInputWhenFocusTarget(false);
        methodTree.setRootVisible(false);
        methodTree.setVisibleRowCount(12);
        treeScrollPanel.setViewportView(methodTree);
        methodTree.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));
        methodTree.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));

        selectionPanel.add(treeScrollPanel, java.awt.BorderLayout.CENTER);
        treeScrollPanel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));
        treeScrollPanel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(selectionPanel, gridBagConstraints);
        selectionPanel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));
        selectionPanel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("EJB_METHODS"));

        methodPanel.setLayout(new java.awt.BorderLayout());

        methodPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 17, 0, 12);
        add(methodPanel, gridBagConstraints);
        methodPanel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("METHOD_DETAILS"));
        methodPanel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/ejb/ui/Bundle").getString("METHOD_DETAILS"));

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel methodPanel;
    private javax.swing.JTree methodTree;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JScrollPane treeScrollPanel;
    // End of variables declaration//GEN-END:variables

}
