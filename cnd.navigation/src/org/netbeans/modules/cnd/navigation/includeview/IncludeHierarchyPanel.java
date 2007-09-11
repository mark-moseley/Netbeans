/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.navigation.includeview;

import java.beans.PropertyVetoException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Alexander Simon
 */
public class IncludeHierarchyPanel extends JPanel implements ExplorerManager.Provider  {
    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N

    private AbstractNode root;
    private transient ExplorerManager explorerManager = new ExplorerManager();
    private CsmUID<CsmFile> object;
    private boolean recursive = true;
    private boolean plain = true;
    private boolean whoIncludes = true;
    
    /** Creates new form IncludeHierarchyPanel */
    public IncludeHierarchyPanel() {
        initComponents();
        setName(NbBundle.getMessage(getClass(), "CTL_IncludeViewTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeViewTopComponent")); // NOI18N
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        ((BeanTreeView)hierarchyPane).setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        root = new AbstractNode(children);
        getExplorerManager().setRootContext(root);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        recursiveGroup = new javax.swing.ButtonGroup();
        plainGroup = new javax.swing.ButtonGroup();
        includesGroup = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        recursiveButton = new javax.swing.JToggleButton();
        directOnlyButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();
        listButton = new javax.swing.JToggleButton();
        whoIncludesButton = new javax.swing.JToggleButton();
        whoIsIncludedButton = new javax.swing.JToggleButton();
        hierarchyPane = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setOrientation(1);
        toolBar.setRollover(true);
        toolBar.setMaximumSize(new java.awt.Dimension(28, 200));
        toolBar.setMinimumSize(new java.awt.Dimension(28, 200));

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setMaximumSize(new java.awt.Dimension(24, 24));
        refreshButton.setMinimumSize(new java.awt.Dimension(24, 24));
        refreshButton.setPreferredSize(new java.awt.Dimension(24, 24));
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        toolBar.add(refreshButton);

        recursiveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/recursive.png"))); // NOI18N
        recursiveButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.recursiveButton.toolTipText")); // NOI18N
        recursiveButton.setFocusable(false);
        recursiveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        recursiveButton.setMaximumSize(new java.awt.Dimension(24, 24));
        recursiveButton.setMinimumSize(new java.awt.Dimension(24, 24));
        recursiveButton.setPreferredSize(new java.awt.Dimension(24, 24));
        recursiveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        recursiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recursiveButtonActionPerformed(evt);
            }
        });
        toolBar.add(recursiveButton);

        directOnlyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/direct_only.png"))); // NOI18N
        directOnlyButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.directOnlyButton.toolTipText")); // NOI18N
        directOnlyButton.setFocusable(false);
        directOnlyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        directOnlyButton.setMaximumSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setMinimumSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setPreferredSize(new java.awt.Dimension(24, 24));
        directOnlyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        directOnlyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directOnlyButtonActionPerformed(evt);
            }
        });
        toolBar.add(directOnlyButton);

        treeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"))); // NOI18N
        treeButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.treeButton.toolTipText")); // NOI18N
        treeButton.setFocusable(false);
        treeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        treeButton.setMaximumSize(new java.awt.Dimension(24, 24));
        treeButton.setMinimumSize(new java.awt.Dimension(24, 24));
        treeButton.setPreferredSize(new java.awt.Dimension(24, 24));
        treeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        treeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeButtonActionPerformed(evt);
            }
        });
        toolBar.add(treeButton);

        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/list.png"))); // NOI18N
        listButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.listButton.toolTipText")); // NOI18N
        listButton.setFocusable(false);
        listButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        listButton.setMaximumSize(new java.awt.Dimension(24, 24));
        listButton.setMinimumSize(new java.awt.Dimension(24, 24));
        listButton.setPreferredSize(new java.awt.Dimension(24, 24));
        listButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        listButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listButtonActionPerformed(evt);
            }
        });
        toolBar.add(listButton);

        whoIncludesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_includes.png"))); // NOI18N
        whoIncludesButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIncludesButton.toolTipText")); // NOI18N
        whoIncludesButton.setFocusable(false);
        whoIncludesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        whoIncludesButton.setMaximumSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setMinimumSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setPreferredSize(new java.awt.Dimension(24, 24));
        whoIncludesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        whoIncludesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whoIncludesButtonActionPerformed(evt);
            }
        });
        toolBar.add(whoIncludesButton);

        whoIsIncludedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_is_included.png"))); // NOI18N
        whoIsIncludedButton.setToolTipText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIsIncludedButton.toolTipText")); // NOI18N
        whoIsIncludedButton.setFocusable(false);
        whoIsIncludedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        whoIsIncludedButton.setMaximumSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setMinimumSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setPreferredSize(new java.awt.Dimension(24, 24));
        whoIsIncludedButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        whoIsIncludedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whoIsIncludedButtonActionPerformed(evt);
            }
        });
        toolBar.add(whoIsIncludedButton);

        add(toolBar, java.awt.BorderLayout.WEST);
        add(hierarchyPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                update(file);
            }
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void recursiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recursiveButtonActionPerformed
        if (recursive == recursiveButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                recursive = recursiveButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_recursiveButtonActionPerformed

    private void directOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directOnlyButtonActionPerformed
        if (recursive != directOnlyButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                recursive = !directOnlyButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_directOnlyButtonActionPerformed

    private void treeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeButtonActionPerformed
        if (plain != treeButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                plain = !treeButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_treeButtonActionPerformed

    private void listButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listButtonActionPerformed
        if (plain == listButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                plain = listButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_listButtonActionPerformed

    private void whoIncludesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIncludesButtonActionPerformed
        if (whoIncludes == whoIncludesButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                whoIncludes = whoIncludesButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_whoIncludesButtonActionPerformed

    private void whoIsIncludedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIsIncludedButtonActionPerformed
        if (whoIncludes != whoIsIncludedButton.isSelected()) {
            return;
        }
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                whoIncludes = !whoIsIncludedButton.isSelected();
                updateButtons();
                update(file);
            }
        }
    }//GEN-LAST:event_whoIsIncludedButtonActionPerformed
    
    public void setFile(CsmFile file){
        object = file.getUID();
        if (file.isHeaderFile()) {
            recursive = false;
            plain = true;
            whoIncludes = true;
        } else {
            recursive = true;
            plain = true;
            whoIncludes = false;
        }
        updateButtons();
        update(file);
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return hierarchyPane.requestFocusInWindow();
    }

    private void updateButtons(){
        if (recursive) {
            recursiveButton.setSelected(true);
            directOnlyButton.setSelected(false);
        } else {
            recursiveButton.setSelected(false);
            directOnlyButton.setSelected(true);
        }
        if (plain) {
            treeButton.setSelected(false);
            listButton.setSelected(true);
        } else {
            treeButton.setSelected(true);
            listButton.setSelected(false);
        }
        if (whoIncludes) {
            whoIncludesButton.setSelected(true);
            whoIsIncludedButton.setSelected(false);
        } else {
            whoIncludesButton.setSelected(false);
            whoIsIncludedButton.setSelected(true);
        }
    }

    private synchronized void update(final CsmFile csmFile) {
        if (csmFile != null){
            final Children children = root.getChildren();
            if (!Children.MUTEX.isReadAccess()){
                Children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        children.remove(children.getNodes());
                        final Node node = new IncludeNode(csmFile, new IncludedModel(csmFile, whoIncludes, plain, recursive), null);
                        children.add(new Node[]{node});
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ((BeanTreeView) hierarchyPane).expandNode(node);
                                try {
                                    getExplorerManager().setSelectedNodes(new Node[]{node});
                                } catch (PropertyVetoException ex) {
                                }
                            }
                        });
                    }
                });
            }
        } else {
            final Children children = root.getChildren();
            if (!Children.MUTEX.isReadAccess()){
                Children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        children.remove(children.getNodes());
                    }
                });
            }
        }
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton directOnlyButton;
    private javax.swing.JScrollPane hierarchyPane;
    private javax.swing.ButtonGroup includesGroup;
    private javax.swing.JToggleButton listButton;
    private javax.swing.ButtonGroup plainGroup;
    private javax.swing.JToggleButton recursiveButton;
    private javax.swing.ButtonGroup recursiveGroup;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToggleButton treeButton;
    private javax.swing.JToggleButton whoIncludesButton;
    private javax.swing.JToggleButton whoIsIncludedButton;
    // End of variables declaration//GEN-END:variables
    
}
