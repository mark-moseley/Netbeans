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

package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.Function;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 * @author Alexander Simon
 */
public class CallGraphPanel extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider  {

    private ExplorerManager explorerManager = new ExplorerManager();
    private AbstractNode root;
    private Action[] actions;
    private CallModel model;
    private boolean showGraph;
    private boolean isCalls;
    public static final String IS_CALLS = "CallGraphIsCalls"; // NOI18N
    
    private CallGraphScene scene;
    private static double dividerLocation = 0.5;
    
    /** Creates new form CallGraphPanel */
    public CallGraphPanel(boolean showGraph) {
        initComponents();
        isCalls = NbPreferences.forModule(CallGraphPanel.class).getBoolean(IS_CALLS, true);
        getTreeView().setRootVisible(false);
        Children.Array children = new Children.SortedArray();
        this.showGraph = showGraph;
        if (showGraph) {
            scene = new CallGraphScene();
            actions = new Action[]{new RefreshAction(), new FocusOnAction(),
                                   null, new WhoIsCalledAction(), new WhoCallsAction(),
                                   null, new ExportAction(scene, this)};
            scene.setExportAction(actions[actions.length-1]);
        } else {
            actions = new Action[]{new RefreshAction(), new FocusOnAction(),
                                   null, new WhoIsCalledAction(), new WhoCallsAction()};
            
        }
        root = new AbstractNode(children){
            @Override
            public Action[] getActions(boolean context) {
                return actions;
            }
        };
        getExplorerManager().setRootContext(root);
        if (showGraph) {
            addComponentListener(new ComponentListener() {
                public void componentResized(ComponentEvent e) {
                    jSplitPane1.setDividerLocation(dividerLocation);
                }
                public void componentMoved(ComponentEvent e) {
                }
                public void componentShown(ComponentEvent e) {
                }
                public void componentHidden(ComponentEvent e) {
                }
            });
            jSplitPane1.addPropertyChangeListener(
                            new PropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(evt.getPropertyName())) {
                       dividerLocation = ((double)jSplitPane1.getDividerLocation())/
                       ((double)(jSplitPane1.getWidth() - jSplitPane1.getDividerSize()));
                    }
                }
            });
        
            initGraph();
        } else {
            Component left = jSplitPane1.getLeftComponent();
            remove(jSplitPane1);
            jSplitPane1.remove(left);
            add(left, java.awt.BorderLayout.CENTER);
        }
        getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Node[] selectedNodes = getExplorerManager().getSelectedNodes();
                if (selectedNodes.length == 1) {
                    Node selected = selectedNodes[0];
                    if (selected instanceof CallNode) {
                        getContextPanel().setRootContent((CallNode) selected);
                    } else {
                        getContextPanel().setRootContent(null);
                    }
                }
            }
        });
    }
    
    private void initGraph() {
        JComponent view = scene.createView();
        graphView.setViewportView(view);
        view.setFocusable(isCalls);
        GraphLayout<Function,Call> layout = new GridGraphLayout<Function,Call>();
        SceneLayout sceneLayout = LayoutFactory.createSceneGraphLayout(scene, layout);
        scene.setLayout(sceneLayout);
        sceneLayout.invokeLayout();
        graphView.setFocusable(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        refresh = new javax.swing.JButton();
        focusOn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        calls = new javax.swing.JToggleButton();
        callers = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        graphView = new JScrollPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        treeView = new BeanTreeView();
        contextPanel = new ContextPanel();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setOrientation(1);
        jToolBar1.setRollover(true);

        refresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/refresh.png"))); // NOI18N
        refresh.setToolTipText(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "RefreshActionTooltip")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setMaximumSize(new java.awt.Dimension(24, 24));
        refresh.setMinimumSize(new java.awt.Dimension(24, 24));
        refresh.setPreferredSize(new java.awt.Dimension(24, 24));
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshActionPerformed(evt);
            }
        });
        jToolBar1.add(refresh);

        focusOn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/focus.png"))); // NOI18N
        focusOn.setToolTipText(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "FocusOnActionTooltip")); // NOI18N
        focusOn.setFocusable(false);
        focusOn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        focusOn.setMaximumSize(new java.awt.Dimension(24, 24));
        focusOn.setMinimumSize(new java.awt.Dimension(24, 24));
        focusOn.setPreferredSize(new java.awt.Dimension(24, 24));
        focusOn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        focusOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusOnActionPerformed(evt);
            }
        });
        jToolBar1.add(focusOn);

        jSeparator1.setSeparatorSize(new java.awt.Dimension(0, 4));
        jToolBar1.add(jSeparator1);

        calls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/who_is_called.png"))); // NOI18N
        calls.setToolTipText(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CallsActionTooltip")); // NOI18N
        calls.setFocusable(false);
        calls.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calls.setMaximumSize(new java.awt.Dimension(24, 24));
        calls.setMinimumSize(new java.awt.Dimension(24, 24));
        calls.setPreferredSize(new java.awt.Dimension(24, 24));
        calls.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callsActionPerformed(evt);
            }
        });
        jToolBar1.add(calls);

        callers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/callgraph/resources/who_calls.png"))); // NOI18N
        callers.setToolTipText(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CallersActionTooltip")); // NOI18N
        callers.setFocusable(false);
        callers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        callers.setMaximumSize(new java.awt.Dimension(24, 24));
        callers.setMinimumSize(new java.awt.Dimension(24, 24));
        callers.setPreferredSize(new java.awt.Dimension(24, 24));
        callers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        callers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callersActionPerformed(evt);
            }
        });
        jToolBar1.add(callers);

        add(jToolBar1, java.awt.BorderLayout.LINE_START);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setFocusable(false);
        jSplitPane1.setOneTouchExpandable(true);

        graphView.setFocusable(false);
        jSplitPane1.setRightComponent(graphView);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(1.0);
        jSplitPane2.setFocusable(false);

        treeView.setFocusable(false);
        jSplitPane2.setLeftComponent(treeView);
        treeView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_TreeView_AN")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_TreeView_AD")); // NOI18N

        contextPanel.setFocusable(false);
        jSplitPane2.setRightComponent(contextPanel);
        contextPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AM")); // NOI18N
        contextPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AD")); // NOI18N

        jSplitPane1.setLeftComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshActionPerformed
        update();
    }//GEN-LAST:event_refreshActionPerformed

    private void callsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callsActionPerformed
        if (isCalls == calls.isSelected()) {
            return;
        }
        setDirection(true);
    }//GEN-LAST:event_callsActionPerformed

    private void callersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callersActionPerformed
        if (isCalls != callers.isSelected()) {
            return;
        }
        setDirection(false);
    }//GEN-LAST:event_callersActionPerformed

private void focusOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_focusOnActionPerformed
        Node[] nodes = getExplorerManager().getSelectedNodes();
        if (nodes == null || nodes.length != 1){
            return;
        }
        Node node = nodes[0];
        if (node instanceof FunctionRootNode){
            update();
        } else if (node instanceof CallNode){
            Call call = ((CallNode)node).getCall();
            if (isCalls) {
                model.setRoot(call.getCallee());
            } else {
                model.setRoot(call.getCaller());
            }
            setName(model.getName());
            setToolTipText(getName()+" - "+NbBundle.getMessage(getClass(), "CTL_CallGraphTopComponent")); // NOI18N
            Container parent = getParent();
            while (parent != null) {
                if (parent instanceof JTabbedPane) {
                    int i = ((JTabbedPane) parent).getSelectedIndex();
                    if (i >=0) {
                        ((JTabbedPane) parent).setTitleAt(i, getName() + "  "); // NOI18N
                    }
                    break;
                } else if (parent instanceof TopComponent) {
                    ((TopComponent) parent).setName(getToolTipText()); // NOI18N
                    break;
                }
            }
            update();
        }
}//GEN-LAST:event_focusOnActionPerformed
    
    private void setDirection(boolean direction){
        isCalls = direction;
        NbPreferences.forModule(CallGraphPanel.class).putBoolean(IS_CALLS, isCalls);
        updateButtons();
        update();
    }

    private void updateButtons(){
        calls.setSelected(isCalls);
        callers.setSelected(!isCalls);
    }
    
    public void setModel(CallModel model) {
        this.model = model;
        //this.isCalls = model.isCalls();
        if (showGraph) {
            scene.setModel(model);
        }
        updateButtons();
        update();
    }

    private synchronized void update() {
        if (showGraph) {
            scene.clean();
        }
        final Function function = model.getRoot();
        if (function != null){
            final Children children = root.getChildren();
            if (!Children.MUTEX.isReadAccess()){
                Children.MUTEX.writeAccess(new Runnable(){
                    public void run() {
                        children.remove(children.getNodes());
                        CallGraphState state = new CallGraphState(model, scene, actions);
                        final Node node = new FunctionRootNode(function, state, isCalls);
                        children.add(new Node[]{node});
                        try {
                            getExplorerManager().setSelectedNodes(new Node[]{node});
                        } catch (PropertyVetoException ex) {
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                getTreeView().expandNode(node);
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

    @Override
    public void requestFocus() {
        super.requestFocus();
        treeView.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return treeView.requestFocusInWindow();
    }
    
    public BeanTreeView getTreeView(){
        return (BeanTreeView)treeView;
    }

    private ContextPanel getContextPanel(){
        return (ContextPanel)contextPanel;
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("CallGraphView"); // NOI18N
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton callers;
    private javax.swing.JToggleButton calls;
    private javax.swing.JPanel contextPanel;
    private javax.swing.JButton focusOn;
    private javax.swing.JScrollPane graphView;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton refresh;
    private javax.swing.JScrollPane treeView;
    // End of variables declaration//GEN-END:variables
    
    private final class RefreshAction extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        public RefreshAction() {
            putValue(Action.NAME, NbBundle.getMessage(CallGraphPanel.class, "RefreshAction"));  // NOI18N
            putValue(Action.SMALL_ICON, refresh.getIcon());
            menuItem = new JMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        public void actionPerformed(ActionEvent e) {
            refreshActionPerformed(e);
        }

        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private final class WhoCallsAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public WhoCallsAction() {
            putValue(Action.NAME, NbBundle.getMessage(CallGraphPanel.class, "CallersAction"));  // NOI18N
            putValue(Action.SMALL_ICON, callers.getIcon());
            menuItem = new JRadioButtonMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }
 
        public void actionPerformed(ActionEvent e) {
            setDirection(false);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(!isCalls);
            return menuItem;
        }
    }

    private final class WhoIsCalledAction extends AbstractAction implements Presenter.Popup {
        private JRadioButtonMenuItem menuItem;
        public WhoIsCalledAction() {
            putValue(Action.NAME, NbBundle.getMessage(CallGraphPanel.class, "CallsAction"));  // NOI18N
            putValue(Action.SMALL_ICON, calls.getIcon());
            menuItem = new JRadioButtonMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }
 
        public void actionPerformed(ActionEvent e) {
            setDirection(true);
        }

        public final JMenuItem getPopupPresenter() {
            menuItem.setSelected(isCalls);
            return menuItem;
        }
    }

    private final class FocusOnAction extends AbstractAction implements Presenter.Popup {
        private JMenuItem menuItem;
        public FocusOnAction() {
            putValue(Action.NAME, NbBundle.getMessage(CallGraphPanel.class, "FocusOnAction"));  // NOI18N
            putValue(Action.SMALL_ICON, focusOn.getIcon());
            menuItem = new JMenuItem(this); 
            Mnemonics.setLocalizedText(menuItem, (String)getValue(Action.NAME));
        }

        public void actionPerformed(ActionEvent e) {
            focusOnActionPerformed(e);
        }

        public final JMenuItem getPopupPresenter() {
            return menuItem;
        }
    }

    private static final class ContextPanel extends JPanel implements ExplorerManager.Provider {
        private ExplorerManager managerCtx = new ExplorerManager();
        private ListView listView = new ListView();
        private ContextPanel(){
            listView.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AM")); // NOI18N
            listView.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallGraphPanel.class, "CGP_ListView_AD")); // NOI18N
            setLayout(new java.awt.BorderLayout());
            add(listView, java.awt.BorderLayout.CENTER);
            listView.setFocusable(false);
        }
        
        public ExplorerManager getExplorerManager() {
            return managerCtx;
        }

        @Override
        public boolean requestFocusInWindow() {
            super.requestFocusInWindow();
            return listView.requestFocusInWindow();
        }
        
        private void setRootContent(CallNode node){
            Collection<Node> list;
            if (node == null) {
                list = Collections.<Node>emptyList();
            } else {
                list = new ArrayList<Node>(1);
                Call call = node.getCall();
                list.add(new CallContext(call));
            }
            CallContextRoot root = new CallContextRoot(new ContextList(list));
            getExplorerManager().setRootContext(root);
        }
        private static final class ContextList extends Children.Array {
            private ContextList(Collection<Node> nodes){
                super(nodes);
            }
        }

        private static final class CallContextRoot extends AbstractNode {
            public CallContextRoot(Children.Array array) {
                super(array);
            }
        }

        public static class CallContext extends AbstractNode {
            private Call call;
            public CallContext(Call element) {
                super( Children.LEAF);
                call = element;
            }

            @Override
            public String getHtmlDisplayName() {
                if (call != null) {
                    return call.getHtmlDisplayName();
                }
                return super.getHtmlDisplayName();
            }
            @Override
            public Action getPreferredAction() {
                return null;
            }

            @Override
            public String getShortDescription() {
                String ret = call.getDescription();
                if (ret != null){
                    return ret;
                }
                return super.getShortDescription();
            }
    
            @Override
            public Action[] getActions(boolean context) {
                return new Action[0];
            }
        }
    }
 }
