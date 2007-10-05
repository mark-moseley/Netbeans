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

package org.netbeans.modules.cnd.navigation.includeview;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.navigation.services.HierarchyFactory;
import org.netbeans.modules.cnd.navigation.services.IncludedModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

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
    private Action[] actions;
    private Action close;
    
    /** Creates new form IncludeHierarchyPanel */
    public IncludeHierarchyPanel(boolean isView) {
        initComponents();
        if (!isView){
            // refresh
            toolBar.remove(0);
            // separstor
            toolBar.remove(0);
            // a11n
            directOnlyButton.setFocusable(true);
            treeButton.setFocusable(true);
            whoIncludesButton.setFocusable(true);
            whoIsIncludedButton.setFocusable(true);
        }
        setName(NbBundle.getMessage(getClass(), "CTL_IncludeViewTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeViewTopComponent")); // NOI18N
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        getTreeView().setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        if (isView) {
            actions = new Action[]{new RefreshAction(), 
                                   null, new WhoIncludesAction(), new WhoIsIncludedAction(),
                                   null, new DirectOnlyAction(), new TreeAction()};
        } else {
            actions = new Action[]{new WhoIncludesAction(), new WhoIsIncludedAction(),
                                   null, new DirectOnlyAction(), new TreeAction()};
        }
        root = new AbstractNode(children){
            @Override
            public Action[] getActions(boolean context) {
                return actions;
            }
        };
        getExplorerManager().setRootContext(root);
    }

    public void setClose(Action close) {
        this.close = close;
    }

    public BeanTreeView getTreeView(){
        return (BeanTreeView)hierarchyPane;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        refreshButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        whoIncludesButton = new javax.swing.JToggleButton();
        whoIsIncludedButton = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        directOnlyButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        hierarchyPane = new BeanTreeView();

        setLayout(new java.awt.GridBagLayout());

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setMaximumSize(new java.awt.Dimension(182, 26));
        toolBar.setMinimumSize(new java.awt.Dimension(182, 26));
        toolBar.setOpaque(false);
        toolBar.setPreferredSize(new java.awt.Dimension(182, 26));

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
        toolBar.add(jSeparator1);

        buttonGroup1.add(whoIncludesButton);
        whoIncludesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_includes.png"))); // NOI18N
        whoIncludesButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIncludesButton.text")); // NOI18N
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

        buttonGroup1.add(whoIsIncludedButton);
        whoIsIncludedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/who_is_included.png"))); // NOI18N
        whoIsIncludedButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.whoIsIncludedButton.text")); // NOI18N
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
        toolBar.add(jSeparator2);

        directOnlyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/navigation/includeview/resources/direct_only.png"))); // NOI18N
        directOnlyButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.directOnlyButton.text")); // NOI18N
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
        treeButton.setText(org.openide.util.NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.treeButton.text")); // NOI18N
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        add(toolBar, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("SplitPane.shadow"))); // NOI18N
        jPanel2.setMinimumSize(new java.awt.Dimension(1, 1));
        jPanel2.setPreferredSize(new java.awt.Dimension(1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(hierarchyPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                update(file);
            }
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void whoIncludesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIncludesButtonActionPerformed
        setWhoIncludes(true);
    }//GEN-LAST:event_whoIncludesButtonActionPerformed

    private void whoIsIncludedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whoIsIncludedButtonActionPerformed
        setWhoIncludes(false);
    }//GEN-LAST:event_whoIsIncludedButtonActionPerformed

    private void directOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directOnlyButtonActionPerformed
        setRecursive(!directOnlyButton.isSelected());
    }//GEN-LAST:event_directOnlyButtonActionPerformed

    private void treeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeButtonActionPerformed
        setPlain(!treeButton.isSelected());
    }//GEN-LAST:event_treeButtonActionPerformed

    private void setRecursive(boolean isRecursive){
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                recursive = isRecursive;
                updateButtons();
                update(file);
            }
        }
    }

    
    private void setPlain(boolean isPlain){
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                plain = isPlain;
                updateButtons();
                update(file);
            }
        }
    }
    
    private void setWhoIncludes(boolean isWhoIncludes) {
        if (object != null) {
            CsmFile file = object.getObject();
            if (file != null){
                whoIncludes = isWhoIncludes;
                updateButtons();
                update(file);
            }
        }
    }
        
    public void setFile(CsmFile file){
        object = file.getUID();
        if (file.isHeaderFile()) {
            recursive = false;
            plain = true;
            whoIncludes = true;
        } else {
            recursive = true;
            plain = false;
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
        whoIncludesButton.setSelected(whoIncludes);
        whoIsIncludedButton.setSelected(!whoIncludes);
        directOnlyButton.setSelected(!recursive);
        treeButton.setSelected(!plain);
    }

    private synchronized void update(final CsmFile csmFile) {
        if (csmFile != null){
            final Children children = root.getChildren();
            if (!Children.MUTEX.isReadAccess()){
                Children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        children.remove(children.getNodes());
                        IncludedModel model = HierarchyFactory.getInstance().buildIncludeHierarchyModel(csmFile, actions, whoIncludes, plain, recursive);
                        model.setCloseWindowAction(close);
                        final Node node = new IncludeNode(csmFile, model, null);
                        children.add(new Node[]{node});
                        try {
                            getExplorerManager().setSelectedNodes(new Node[]{node});
                        } catch (PropertyVetoException ex) {
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ((BeanTreeView) hierarchyPane).expandNode(node);
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JToggleButton directOnlyButton;
    private javax.swing.JScrollPane hierarchyPane;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToggleButton treeButton;
    private javax.swing.JToggleButton whoIncludesButton;
    private javax.swing.JToggleButton whoIsIncludedButton;
    // End of variables declaration//GEN-END:variables
    
    private class RefreshAction extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        public RefreshAction() {
            putValue(Action.NAME, NbBundle.getMessage(IncludeHierarchyPanel.class, "IncludeHierarchyPanel.refreshButton.toolTipText")); //NOI18N
            putValue(Action.SMALL_ICON, refreshButton.getIcon());
            menuItem = new JMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }

        public void actionPerformed(ActionEvent e) {
            refreshButtonActionPerformed(e);
        }

        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private class WhoIncludesAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public WhoIncludesAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.WHO_INCLUDES));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.WHO_INCLUDES));
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            setWhoIncludes(true);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(whoIncludes);
            return menuItem;
        }
    }

    private class WhoIsIncludedAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public WhoIsIncludedAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.WHO_IS_INCLUDED));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.WHO_IS_INCLUDED));
            menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            setWhoIncludes(false);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!whoIncludes);
            return menuItem;
        }
    }

    private class DirectOnlyAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public DirectOnlyAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.DIRECT_ONLY));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.DIRECT_ONLY));
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            setRecursive(!recursive);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!recursive);
            return menuItem;
        }
    }

    private class TreeAction extends AbstractAction implements Presenter.Popup {
        private JCheckBoxMenuItem menuItem;
        public TreeAction() {
            putValue(Action.NAME, getButtonTooltip(IncludeHierarchyPanel.TREE));
            putValue(Action.SMALL_ICON, getButtonIcon(IncludeHierarchyPanel.TREE));
            menuItem = new JCheckBoxMenuItem((String)getValue(Action.NAME)); 
            menuItem.setAction(this);
        }
 
        public void actionPerformed(ActionEvent e) {
            setPlain(!plain);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!plain);
            return menuItem;
        }
    }

    private static final int WHO_INCLUDES = 1;
    private static final int WHO_IS_INCLUDED= 2;

    private static final int DIRECT_ONLY = 3;
    private static final int TREE = 4;


    private ImageIcon getButtonIcon(int kind){
        String path = null;
        switch (kind){
        case WHO_INCLUDES:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/who_includes.png"; // NOI18N
            break;
        case WHO_IS_INCLUDED:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/who_is_included.png"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/direct_only.png"; // NOI18N
            break;
        case TREE:
            path = "/org/netbeans/modules/cnd/navigation/includeview/resources/tree.png"; // NOI18N
            break;
        }
        return new javax.swing.ImageIcon(getClass().getResource(path));
    }

    private String getButtonTooltip(int kind){
        String path = null;
        switch (kind){
        case WHO_INCLUDES:
            path = "IncludeHierarchyPanel.whoIncludesButton.toolTipText"; // NOI18N
            break;
        case WHO_IS_INCLUDED:
            path = "IncludeHierarchyPanel.whoIsIncludedButton.toolTipText"; // NOI18N
            break;
        case DIRECT_ONLY:
            path = "IncludeHierarchyPanel.directOnlyButton.toolTipText"; // NOI18N
            break;
        case TREE:
            path = "IncludeHierarchyPanel.treeButton.toolTipText"; // NOI18N
            break;
        }
        return org.openide.util.NbBundle.getMessage(getClass(), path);
    }
}
