/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.FunctionsListViewVisualizerConfigurationAccessor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public class FunctionsListViewVisualizer extends JPanel implements
    Visualizer<FunctionsListViewVisualizerConfiguration>, OnTimerTask, ComponentListener, ExplorerManager.Provider {

    private Future task;
    private Future<List<FunctionCall>> queryDataTask;
    private final Object dpQueryLock = new Object();
    private final Object queryLock = new Object();
    private final Object uiLock = new Object();
    private JToolBar buttonsToolbar;
    private JButton refresh;
    private boolean isEmptyContent;
    private boolean isLoadingContent;
    private boolean isShown = true;
    private OutlineView outlineView;
    private final ExplorerManager explorerManager;
    private final FunctionDatatableDescription functionDatatableDescription;
    private final FunctionsListDataProvider dataProvider;
    private final DataTableMetadata metadata;
    private final List<Column> metrics;
    private final FunctionsListViewVisualizerConfiguration configuration;

    public FunctionsListViewVisualizer(FunctionsListDataProvider dataProvider, FunctionsListViewVisualizerConfiguration configuration) {
        explorerManager = new ExplorerManager();
        this.configuration = configuration;
        this.functionDatatableDescription = FunctionsListViewVisualizerConfigurationAccessor.getDefault().getFunctionDatatableDescription(configuration);
        this.metrics = FunctionsListViewVisualizerConfigurationAccessor.getDefault().getMetricsList(configuration);
        this.dataProvider = dataProvider;
        this.metadata = configuration.getMetadata();
        setLoadingContent();
        addComponentListener(this);
        outlineView = new OutlineView(metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnUName());
        outlineView.getOutline().setRootVisible(false);
        outlineView.getOutline().setDefaultRenderer(Object.class, new ExtendedTableCellRendererForNode());
        List<Property> result = new ArrayList<Property>();
        for (Column c : metrics) {
            result.add(new PropertySupport(c.getColumnName(), c.getColumnClass(),
                c.getColumnUName(), c.getColumnUName(), true, false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return null;
                }

                @Override
                public void setValue(Object arg0) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                }
            });

        }
        outlineView.setProperties(result.toArray(new Property[0]));


        VisualizerTopComponentTopComponent.findInstance().addComponentListener(this);

    }

    public FunctionsListViewVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public JComponent getComponent() {
        return this;
    }

    public VisualizerContainer getDefaultContainer() {
        return VisualizerTopComponentTopComponent.getDefault();
    }

    public void refresh() {
        asyncFillModel();
    }



    private void asyncFillModel() {
        synchronized (queryLock) {
            //should I cancel here?
            //Maybe I should not ask if I am already in process??
            if (task != null && !task.isDone() && !task.isCancelled()){
                return;//
            }
            if (task != null && !task.isDone()) {
                task.cancel(true);
            }
            task = DLightExecutorService.submit(new Callable<Boolean>() {

                public Boolean call() {
                    synchronized (dpQueryLock) {
                        if (queryDataTask != null) {
                            return Boolean.TRUE;
                            //queryDataTask.cancel(true);
                        }
                        queryDataTask = DLightExecutorService.submit(new Callable<List<FunctionCall>>() {

                            public List<FunctionCall> call() throws Exception {
                                return dataProvider.getFunctionsList(metadata, functionDatatableDescription, metrics);
                            }
                        }, "FunctionsListViewVisualizer Async dataProvider.gteFuncsiontsList from provider  load for " + configuration.getID() + " from main table " + configuration.getMetadata().getName()); // NOI18N
                    }
                    try {
                        final List<FunctionCall> list = queryDataTask.get();
                        synchronized (dpQueryLock) {
                            queryDataTask = null;
                        }
                        final boolean isEmptyConent = list == null || list.isEmpty();
                        UIThread.invoke(new Runnable() {

                            public void run() {
                                setContent(isEmptyConent);
                                if (isEmptyConent) {
                                    return;
                                }

                                updateList(list);
                            }
                        });
                        return Boolean.TRUE;
                    } catch (ExecutionException ex) {
                        Thread.currentThread().interrupt();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        synchronized (dpQueryLock) {
                            if (queryDataTask!= null){
                                //queryDataTask.cancel(true);TODO: uncomment when ErPrint is ready
                                queryDataTask = null;
                            }
                        }
                    }
                    return Boolean.valueOf(false);
                }
            }, "FunctionsListViewVisualizert Async data load for " + configuration.getID() + " from main table " + configuration.getMetadata().getName()); // NOI18N
        }
    }

    private void syncFillModel() {
    }

    private void updateList(List<FunctionCall> list) {
        synchronized (uiLock) {
            this.explorerManager.setRootContext(new AbstractNode(new FunctionCallChildren(list)));
            setNonEmptyContent();
        }
    }

    private void setEmptyContent() {
        isEmptyContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        JLabel label = new JLabel(timerHandler != null && timerHandler.isSessionAnalyzed() ? AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyAnalyzeMessage(configuration) : AdvancedTableViewVisualizerConfigurationAccessor.getDefault().getEmptyRunningMessage(configuration)); // NOI18N
        JLabel label = new JLabel(NbBundle.getMessage(FunctionsListViewVisualizer.class, "NoDataAvailableYet"));
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    private void setLoadingContent() {
        isEmptyContent = false;
        isLoadingContent = true;
        this.removeAll();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(NbBundle.getMessage(AdvancedTableViewVisualizer.class, "Loading")); // NOI18N
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        this.add(label);
        repaint();
        revalidate();
    }

    private void setContent(boolean isEmpty) {
        if (isLoadingContent && isEmpty) {
            isLoadingContent = false;
            setEmptyContent();
            return;
        }
        if (isLoadingContent && !isEmpty) {
            isLoadingContent = false;
            setNonEmptyContent();
            return;
        }
        if (isEmptyContent && isEmpty) {
            return;
        }
        if (isEmptyContent && !isEmpty) {
            setNonEmptyContent();
            return;
        }
        if (!isEmptyContent && isEmpty) {
            setEmptyContent();
            return;
        }

    }

    private void setNonEmptyContent() {
        isEmptyContent = false;
        this.removeAll();
        this.setLayout(new BorderLayout());
        buttonsToolbar = new JToolBar();
        refresh = new JButton();

        buttonsToolbar.setFloatable(false);
        buttonsToolbar.setOrientation(1);
        buttonsToolbar.setRollover(true);

        // Refresh button...
        refresh.setIcon(ImageLoader.loadIcon("refresh.png")); // NOI18N
        refresh.setFocusable(false);
        refresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refresh.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asyncFillModel();
            }
        });

        buttonsToolbar.add(refresh);



        add(buttonsToolbar, BorderLayout.LINE_START);
//        JComponent treeTableView =
//            Models.createView(compoundModel);
//        add(treeTableView, BorderLayout.CENTER);
//        treeModelImpl.fireTreeModelChanged();
//        tableView = new TableView();
        add(outlineView, BorderLayout.CENTER);


        repaint();
        validate();

    }

    public int onTimer() {
        //throw new UnsupportedOperationException("Not supported yet.");
        syncFillModel();
        return 0;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        asyncFillModel();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        synchronized (queryLock) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }
        synchronized (dpQueryLock) {
            if (queryDataTask != null) {
                //queryDataTask.cancel(true);TODO: uncomment when ErPrint is ready
                queryDataTask = null;
            }
        }

        removeComponentListener(this);
        VisualizerTopComponentTopComponent.findInstance().removeComponentListener(this);
    }

    public void timerStopped() {
        if (isEmptyContent) {
            //should set again to chahe Label message
            setEmptyContent();
        }
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        if (isShown) {
            return;
        }
        isShown = isShowing();
        if (isShown) {
            asyncFillModel();
        }
    }

    public void componentHidden(ComponentEvent e) {
        isShown = false;
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public class FunctionCallChildren extends Children.Keys<FunctionCall> {

        private final List<FunctionCall> list;

        public FunctionCallChildren(List<FunctionCall> list) {
            this.list = list;
        }

        protected Node[] createNodes(FunctionCall key) {
            return new Node[]{new FunctionCallNode(key)};
        }

        @Override
        protected void addNotify() {
            setKeys(list);
        }
    }

    private class FunctionCallNode extends AbstractNode {

        private final FunctionCall functionCall;
        private PropertySet propertySet;
        private final Action[] actions;
        private final Action goToSourceAction;

        FunctionCallNode(FunctionCall row) {
            super(Children.LEAF);
            functionCall = row;
            goToSourceAction = new GoToSourceAction(this);
            actions = new Action[]{goToSourceAction};
            propertySet = new PropertySet() {

                @Override
                public Property<?>[] getProperties() {
                    List<Property> result = new ArrayList<Property>();
                    //create for metrics
                    for (final Column metric : metrics) {
                        result.add(new PropertySupport(metric.getColumnName(), metric.getColumnClass(),
                            metric.getColumnUName(), metric.getColumnUName(), true, false) {

                            @Override
                            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                                return functionCall.getMetricValue(metric.getColumnName());
                            }

                            @Override
                            public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                                //throw new UnsupportedOperationException("Not supported yet.");
                                }
                        });
                    }
                    return result.toArray(new Property[0]);


                }
            };
        }

        public FunctionCall getFunctionCall() {
            return functionCall;
        }

        @Override
        public Image getIcon(int type) {
            return null;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return null;
        }

        @Override
        public Action getPreferredAction() {
            return goToSourceAction;
        }

        @Override
        public Action[] getActions(boolean context) {
            return actions;
        //return super.getActions(context);
        }

        @Override
        public String getDisplayName() {
            return functionCall.getFunction().getName() + (functionCall.hasOffset() ? ("+0x" + functionCall.getOffset()) : "");//NOI18N
        }

        @Override
        public PropertySet[] getPropertySets() {
            return new PropertySet[]{propertySet};
        }
    }

    private class GoToSourceAction extends AbstractAction {

        private final FunctionCallNode functionCallNode;

        public GoToSourceAction(FunctionCallNode functionCallNode) {
            super(NbBundle.getMessage(FunctionsListViewVisualizer.class, "GoToSourceActionName"));//NOI18N
            this.functionCallNode = functionCallNode;

        }

        public void actionPerformed(ActionEvent e) {
            FunctionCall functionCall = functionCallNode.getFunctionCall();
            SourceFileInfo sourceFileInfo = dataProvider.getSourceFileInfo(functionCall);
            if (sourceFileInfo == null) {// TODO: what should I do here if there is no source file info
                return;
            }
            SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
            sourceSupportProvider.showSource(sourceFileInfo);
        //System.out.println(sourceFileInfo == null ? " NO SOURCE FILE INFO FOUND" : sourceFileInfo.getFileName() + ":" + sourceFileInfo.getOffset() + ":" + sourceFileInfo.getLine());//NOI18N
        }
    }

    private class ExtendedTableCellRendererForNode extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 0) {//we have
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            PropertyEditor editor = PropertyEditorManager.findEditor(metadata.getColumnByName(functionDatatableDescription.getNameColumn()).getColumnClass());
            if (editor != null && value != null && !(value + "").trim().equals("")) {
                editor.setValue(value);
                return super.getTableCellRendererComponent(table, editor.getAsText(), isSelected, hasFocus, row, column);
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

}
