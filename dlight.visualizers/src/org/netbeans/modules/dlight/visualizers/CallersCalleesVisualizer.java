/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.OpenFunctionInEditorActionProvider;
import org.netbeans.modules.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

class CallersCalleesVisualizer extends TreeTableVisualizer<FunctionCallTreeTableNode> {

    private static final int TOP_FUNCTIONS_COUNT = 10;
    public static final String IS_CALLS = "TopTenFunctionsIsCalls"; // NOI18N
    private JToggleButton callers;
    private JToggleButton calls;
    private JButton focusOn;
    private boolean isCalls = true;
    private List<? extends FunctionMetric> metricsList = null;
    private StackDataProvider dataProvider;
    private DefaultMutableTreeNode focusedTreeNode = null;
    private CallersCalleesVisualizerConfiguration configuration;

    CallersCalleesVisualizer(StackDataProvider dataProvider, TreeTableVisualizerConfiguration configuration) {
        super(configuration, dataProvider);
        this.configuration = (CallersCalleesVisualizerConfiguration) configuration;
        this.configuration.setNodeActionProvider(new NodeActionsProviderImpl());
        this.dataProvider = dataProvider;
        isCalls = NbPreferences.forModule(CallersCalleesVisualizer.class).getBoolean(IS_CALLS, true);
        setEmptyContent();
    }

    public TreeTableVisualizerConfiguration getConfiguration() {
        return super.getVisualizerConfiguration();
    }

    @Override
    protected void initComponents() {
        super.initComponents();
        if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(getConfiguration())) {//we do not need focus on and other buttons here
            return;
        }
        focusOn = new JButton();
        calls = new JToggleButton();
        callers = new JToggleButton();
        JToolBar buttonsToolbar = getButtonsTolbar();
        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);


        // focusOn button...
        focusOn.setIcon(ImageLoader.loadIcon("focus.png")); // NOI18N
//    focusOn.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "FocusOnActionTooltip")); // NOI18N
        focusOn.setFocusable(false);
        focusOn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        focusOn.setMaximumSize(new java.awt.Dimension(28, 28));
        focusOn.setMinimumSize(new java.awt.Dimension(28, 28));
        focusOn.setPreferredSize(new java.awt.Dimension(28, 28));
        focusOn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        focusOn.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusOnActionPerformed(evt);
            }
        });

        buttonsToolbar.add(focusOn);

        buttonsToolbar.add(new JToolBar.Separator());

        calls.setIcon(ImageLoader.loadIcon("who_is_called.png")); // NOI18N
//    calls.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "CallsActionTooltip")); // NOI18N
        calls.setFocusable(false);
        calls.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calls.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calls.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callsActionPerformed(evt);
            }
        });

        buttonsToolbar.add(calls);

        callers.setIcon(ImageLoader.loadIcon("who_calls.png")); // NOI18N
//    callers.setToolTipText(org.openide.util.NbBundle.getMessage(PerformanceMonitorViewTopComponent.class, "CallersActionTooltip")); // NOI18N
        callers.setFocusable(false);
        callers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        callers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        callers.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callersActionPerformed(evt);
            }
        });

        buttonsToolbar.add(callers);
        repaint();
        revalidate();
    }

    private void callsActionPerformed(ActionEvent evt) {
        if (isCalls == calls.isSelected()) {
            return;
        }
        setDirection(true);
    }

    private void callersActionPerformed(ActionEvent evt) {
        if (isCalls != callers.isSelected()) {
            return;
        }
        setDirection(false);
    }

    private void focusOnActionPerformed(ActionEvent evt) {
        //find selected
        //functionsCallTreeModel.
        //throw new UnsupportedOperationException("Not yet implemented");
        ExplorerManager manager = getExplorerManager();
        if (manager == null) {
            System.out.println("RETURN NO ExplorerManager defined");
            return;
        }
        //get selected
        Node[] selectedNodes = manager.getSelectedNodes();
        if (selectedNodes == null || selectedNodes.length == 0) {
            System.out.println("ACHTUNG!! NULL SELECION!!");
            return;
        }
        Node selectedNode = selectedNodes[0];
        focusedTreeNode = selectedNode.getLookup().lookup(DefaultMutableTreeNode.class);
        FunctionCall focusedFunction = focusedTreeNode == null ? null : ((FunctionCallTreeTableNode) focusedTreeNode.getUserObject()).getDeligator();
        getTreeRoot().removeAllChildren();
        getTreeRoot().add(focusedTreeNode);
        fireTreeModelChanged();

        loadTree(focusedTreeNode, Arrays.asList(new FunctionCallTreeTableNode(focusedFunction)));

    //
    //and now chage tree and invoke fireTreeModelChanged()

    }

    private ExplorerManager getExplorerManager() {
        if (treeTableView != null && treeTableView instanceof ExplorerManager.Provider) {
            return ((ExplorerManager.Provider) treeTableView).getExplorerManager();
        }
        return null;
    }

    private void setDirection(boolean direction) {
        isCalls = direction;
        NbPreferences.forModule(CallersCalleesVisualizer.class).putBoolean(IS_CALLS, isCalls);
        updateButtons();
        update();
    }

    private synchronized void update() {
        if (focusedTreeNode == null) {
            //just update tree
            asyncFillModel(getConfiguration().getMetadata().getColumns());
            return;
        }
        //otherwise we should update
        loadTree(focusedTreeNode, Arrays.asList((FunctionCallTreeTableNode) focusedTreeNode.getUserObject()));
    }

    /**
     * This method will be invoked when
     */
    @Override
    protected void loadTree(final DefaultMutableTreeNode rootNode, final List<FunctionCallTreeTableNode> ppath) {
        //we should show Loading Node
        //this.functionsCallTreeModel.get
        //go away from AWT Thread
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {

                List<FunctionCall> result = null;
                FunctionCall[] path = new FunctionCall[ppath.size()];
                for (int i = 0, size = ppath.size(); i < size; i++) {
                    path[i] = ppath.get(i).getDeligator();
                }
                //FunctionCall[] path = ppath.toArray(new FunctionCallTreeTableNode[0]);
                if (CallersCalleesVisualizer.this.isCalls) {
                    result = dataProvider.getCallees(path, isCalls);
                } else {
                    result = dataProvider.getCallers(path, false);
                }
                update(rootNode, result);
            }
        });
    }

    @Override
    protected void updateTree(final DefaultMutableTreeNode rootNode, List<FunctionCallTreeTableNode> result) {
        rootNode.removeAllChildren();
        if (result != null) {
            for (FunctionCallTreeTableNode call : result) {
                rootNode.add(new DefaultMutableTreeNode(call));
            }
        }

        fireTreeModelChanged(rootNode);
    }

    private void update(final DefaultMutableTreeNode rootNode, List<FunctionCall> result) {
        //add them all as a children to rootNode
        rootNode.removeAllChildren();
        if (result != null) {
            for (FunctionCall call : result) {
                rootNode.add(new DefaultMutableTreeNode(new FunctionCallTreeTableNode(call)));
            }
        }

        fireTreeModelChanged(rootNode);

    }

    @Override
    protected void syncFillModel(final List<Column> columns) {
        final List<FunctionCall> list =
            dataProvider.getHotSpotFunctions(columns, null, TOP_FUNCTIONS_COUNT);
        final boolean isEmptyConent = list == null || list.isEmpty();
        UIThread.invoke(new Runnable() {

            public void run() {
                setContent(isEmptyConent);
                if (isEmptyConent) {
                    return;
                }

                update(list);
            }
        });
    }

    private void update(List<FunctionCall> list) {
        List<FunctionCallTreeTableNode> res = new ArrayList<FunctionCallTreeTableNode>();
        for (FunctionCall c : list) {
            res.add(new FunctionCallTreeTableNode(c));
        }
        updateList(res);
    }

    @Override
    protected void updateButtons() {
        if (TreeTableVisualizerConfigurationAccessor.getDefault().isTableView(getConfiguration())) {
            return;
        }
        calls.setSelected(isCalls);
        callers.setSelected(!isCalls);
    }

    private FunctionMetric getMetricByID(String id) {
        for (FunctionMetric metric : metricsList) {
            if (metric.getMetricID().equals(id)) {
                return metric;
            }
        }
        return null;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateButtons();
    }

    @Override
    public int onTimer() {
        if (!isShown() || !isShowing()) {
            return 0;
        }
        syncFillModel(this.configuration.getMetadata().getColumns());
//    update(dataProvider.getHotSpotFunctions(, null, TOP_FUNCTIONS_COUNT));
        return 0;
    }

    class NodeActionsProviderImpl implements NodeActionsProvider {

        public void performDefaultAction(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            Object nodeObject = treeNode.getUserObject();
            if (!(nodeObject instanceof FunctionCallTreeTableNode)) {
                return;
            }
            //find function name
            OpenFunctionInEditorActionProvider.getInstance().openFunction(((FunctionCallTreeTableNode) nodeObject).getValue() + "");
//                return (nodeObject instanceof FunctionCallTreeTableNode) ? ((TreeTableNode) nodeObject).getValue() + " " : nodeObject.toString();

        }

        public Action[] getActions(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
            Object nodeObject = treeNode.getUserObject();
            if (!(nodeObject instanceof FunctionCallTreeTableNode)) {
                return null;
            }
            final String functionName = ((FunctionCallTreeTableNode) nodeObject).getValue() + "";
            //final TableVisualizerEvent event = (TableVisualizerEvent)node;
            AbstractAction goToSourceAction = new AbstractAction(NbBundle.getMessage(CallersCalleesVisualizer.class, "GoToSourceActionName") + " " + functionName) {

                public void actionPerformed(ActionEvent e) {
                    OpenFunctionInEditorActionProvider.getInstance().openFunction(functionName);
                }

                @Override
                public boolean isEnabled() {
                    return Lookup.getDefault().lookup(SourceSupportProvider.class) != null &&
                        Lookup.getDefault().lookup(SourceFileInfoProvider.class) != null;
                }
            };
            return new Action[]{goToSourceAction};
        }
    }

}
