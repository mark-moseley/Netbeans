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
package org.netbeans.modules.dlight.indicators.support;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import org.netbeans.modules.dlight.indicators.PlotIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.netbeans.modules.dlight.indicators.graph.RepairPanel;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.UIUtilities;

/**
 * @author Alexey Vladykin
 */
public final class PlotIndicator extends Indicator<PlotIndicatorConfiguration> {

    private final DataRowToPlot dataRowHandler;
    private final GraphPanel<Graph, Legend> panel;
    private final Graph graph;
    private final Legend legend;
    private final JButton button;
    private final int graphCount;

    public PlotIndicator(PlotIndicatorConfiguration configuration) {
        super(configuration);
        this.dataRowHandler = configuration.getDataRowHandler();
        this.graph = createGraph(configuration);
        this.legend = new Legend(configuration.getGraphDescriptors(), configuration.getDetailDescriptors());
        this.button = new JButton(getDefaultAction());
        button.setPreferredSize(new Dimension(120, 2 * button.getFont().getSize()));
        this.panel = new GraphPanel<Graph, Legend>(configuration.getTitle(), graph, legend, null, graph.getVerticalAxis(), button);
        this.graphCount = configuration.getGraphDescriptors().size();
    }

    private static Graph createGraph(PlotIndicatorConfiguration configuration) {
        Graph graph = new Graph(configuration.getGraphScale(), configuration.getLabelRenderer(), configuration.getGraphDescriptors());
        graph.setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));
        Dimension graphSize = new Dimension(GraphConfig.GRAPH_WIDTH, GraphConfig.GRAPH_HEIGHT);
        graph.setMinimumSize(graphSize);
        graph.setPreferredSize(graphSize);
        Dimension axisSize = new Dimension(GraphConfig.VERTICAL_AXIS_WIDTH, GraphConfig.VERTICAL_AXIS_HEIGHT);
        graph.getVerticalAxis().setMinimumSize(axisSize);
        graph.getVerticalAxis().setPreferredSize(axisSize);
        return graph;
    }

    @Override
    protected void repairNeeded(boolean needed) {
        if (needed) {
            final RepairPanel repairPanel = new RepairPanel(getRepairActionProvider().getValidationStatus());
            repairPanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final Future<Boolean> repairResult = getRepairActionProvider().asyncRepair();
                    DLightExecutorService.submit(new Callable<Boolean>() {

                        public Boolean call() throws Exception {
                            UIThread.invoke(new Runnable() {

                                public void run() {
                                    repairPanel.setEnabled(false);
                                }
                            });
                            Boolean retValue = repairResult.get();
                            UIThread.invoke(new Runnable() {

                                public void run() {
                                    repairPanel.setEnabled(true);
                                }
                            });
                            return retValue;
                        }
                    }, "Click On Indicator task"); //NOI18N
                }
            });
            UIThread.invoke(new Runnable() {
                public void run() {
                    panel.setOverlay(repairPanel);
                }
            });
        } else {
            final JEditorPane label = UIUtilities.createJEditorPane(getRepairActionProvider().getMessage(getRepairActionProvider().getValidationStatus()), false, GraphConfig.TEXT_COLOR);
            UIThread.invoke(new Runnable() {
                public void run() {
                    panel.setOverlay(label);
                }
            });
        }
    }

    @Override
    protected void tick() {
        float[] plotData = new float[graphCount];
        Map<String, String> details = new HashMap<String, String>();
        dataRowHandler.tick(plotData, details);
        if (plotData != null) {
            int oldLimit = graph.getUpperLimit();
            int newLimit = graph.calculateUpperLimit(plotData);
            while (oldLimit < newLimit) {
                oldLimit *= 2;
            }
            graph.setUpperLimit(oldLimit);
            graph.addData(plotData);
        }
        for (Map.Entry<String, String> entry : details.entrySet()) {
            legend.updateDetail(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void updated(List<DataRow> data) {
        for (DataRow row : data) {
            dataRowHandler.addDataRow(row);
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }
}
