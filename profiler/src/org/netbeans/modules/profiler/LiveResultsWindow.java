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

package org.netbeans.modules.profiler;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.RuntimeCCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUCCTProvider;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.memory.MemoryCCTProvider;
import org.netbeans.lib.profiler.ui.LiveResultsPanel;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.charts.ChartActionListener;
import org.netbeans.lib.profiler.ui.charts.SynchronousXYChart;
import org.netbeans.lib.profiler.ui.cpu.CPUResUserActionsHandler;
import org.netbeans.lib.profiler.ui.cpu.CodeRegionLivePanel;
import org.netbeans.lib.profiler.ui.cpu.FlatProfilePanel;
import org.netbeans.lib.profiler.ui.cpu.LiveFlatProfilePanel;
import org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModuleContainer;
import org.netbeans.lib.profiler.ui.cpu.statistics.drilldown.DrillDownListener;
import org.netbeans.lib.profiler.ui.graphs.GraphPanel;
import org.netbeans.lib.profiler.ui.memory.LiveAllocResultsPanel;
import org.netbeans.lib.profiler.ui.memory.LiveLivenessResultsPanel;
import org.netbeans.lib.profiler.ui.memory.MemoryResUserActionsHandler;
import org.netbeans.modules.profiler.actions.ResetResultsAction;
import org.netbeans.modules.profiler.actions.TakeSnapshotAction;
import org.netbeans.modules.profiler.spi.ProjectTypeProfiler;
import org.netbeans.modules.profiler.ui.stats.drilldown.hierarchical.DrillDown;
import org.netbeans.modules.profiler.ui.stp.ProfilingSettingsManager;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * An IDE TopComponent to display live profiling results.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class LiveResultsWindow extends TopComponent implements ResultsListener, ProfilingStateListener, HistoryListener,
                                                                     SaveViewAction.ViewProvider {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static final class EmptyLiveResultsPanel extends JPanel implements LiveResultsPanel {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getSortingColumn() {
            return -1;
        }

        public boolean getSortingOrder() {
            return true;
        }

        public BufferedImage getViewImage(boolean b) {
            throw new UnsupportedOperationException();
        }

        public String getViewName() {
            return "Empty live results"; // NOI18N
        }

        public boolean fitsVisibleArea() {
            return false;
        }

        public void handleRemove() {
        }

        public void handleShutdown() {
        }

        public boolean hasView() {
            return false;
        }

        public void reset() {
        }

        public boolean supports(int instrumentationType) {
            return true;
        }

        public void updateLiveResults() {
        }
    }

    public static final class ResultsMonitor implements CPUCCTProvider.Listener, MemoryCCTProvider.Listener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void cctEstablished(RuntimeCCTNode runtimeCCTNode) {
            getDefault().resultsAvailable = true;
            IDEUtils.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        getDefault().updateResultsDisplay();
                    }
                });
        }

        public void cctReset() {
            getDefault().resultsAvailable = false;
        }
    }

    public static class ActivateDrillDownAction extends AbstractAction {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void actionPerformed(ActionEvent e) {
            //      System.out.println("ActivateDrillDownAction Invoked");
            if (TopComponent.getRegistry().getActivated() == LiveResultsWindow.getDefault()) {
                // LiveResultsWindow is active
                TopComponent drillDown = DrillDownWindow.getDefault();

                //        System.out.println(" Drill window: "+drillDown.isOpened());
                if (drillDown.isOpened()) {
                    // DrillDown is visible
                    drillDown.requestActive();
                }
            }
        }
    }

    private final class CPUActionsHandler extends CPUResUserActionsHandler.Adapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void addMethodToRoots(final String className, final String methodName, final String methodSig) {
            Project project = ((NetBeansProfiler) Profiler.getDefault()).getProfiledProject();

            ProfilingSettings[] projectSettings = ProfilingSettingsManager.getDefault().getProfilingSettings(project)
                                                                          .getProfilingSettings();
            List<ProfilingSettings> cpuSettings = new ArrayList<ProfilingSettings>();

            for (ProfilingSettings settings : projectSettings) {
                if (org.netbeans.modules.profiler.ui.stp.Utils.isCPUSettings(settings.getProfilingType())) {
                    cpuSettings.add(settings);
                }
            }

            ProfilingSettings lastProfilingSettings = null;
            String lastProfilingSettingsName = Profiler.getDefault().getLastProfilingSettings().getSettingsName();

            // Resolve current settings
            for (ProfilingSettings settings : cpuSettings) {
                if (settings.getSettingsName().equals(lastProfilingSettingsName)) {
                    lastProfilingSettings = settings;

                    break;
                }
            }

            ProfilingSettings settingsToModify = IDEUtils.selectSettings(project, ProfilingSettings.PROFILE_CPU_PART,
                                                                         cpuSettings.toArray(new ProfilingSettings[cpuSettings
                                                                                                                                                                                                                                               .size()]),
                                                                         lastProfilingSettings);

            if (settingsToModify == null) {
                return; // cancelled by the user
            }

            settingsToModify.addRootMethod(className, methodName, methodSig);

            if (cpuSettings.contains(settingsToModify)) {
                ProfilingSettingsManager.getDefault().storeProfilingSettings(projectSettings, settingsToModify, project);
            } else {
                ProfilingSettings[] newProjectSettings = new ProfilingSettings[projectSettings.length + 1];
                System.arraycopy(projectSettings, 0, newProjectSettings, 0, projectSettings.length);
                newProjectSettings[projectSettings.length] = settingsToModify;
                ProfilingSettingsManager.getDefault().storeProfilingSettings(newProjectSettings, settingsToModify, project);
            }
        }

        public void showReverseCallGraph(final CPUResultsSnapshot snapshot, final int threadId, final int methodId, int view,
                                         int sortingColumn, boolean sortingOrder) {
            throw new IllegalStateException(ERROR_DISPLAYING_CALL_GRAPH_MSG);
        }

        public void showSourceForMethod(final String className, final String methodName, final String methodSig) {
            Profiler.getDefault().openJavaSource(className, methodName, methodSig);
        }

        public void viewChanged(int viewType) {
            if (currentDisplay != null) {
                ((FlatProfilePanel) currentDisplay).prepareResults();
            }
        }
    }

    private static final class GraphTab extends JPanel implements ActionListener, ChartActionListener {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static final ImageIcon zoomInIcon = new ImageIcon(Utilities.loadImage("org/netbeans/lib/profiler/ui/resources/zoomIn.png")); //NOI18N
        private static final ImageIcon zoomOutIcon = new ImageIcon(Utilities.loadImage("org/netbeans/lib/profiler/ui/resources/zoomOut.png")); //NOI18N
        private static final ImageIcon zoomIcon = new ImageIcon(Utilities.loadImage("org/netbeans/lib/profiler/ui/resources/zoom.png")); //NOI18N
        private static final ImageIcon scaleToFitIcon = new ImageIcon(Utilities.loadImage("org/netbeans/lib/profiler/ui/resources/scaleToFit.png")); //NOI18N

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        final GraphPanel panel;
        final JButton scaleToFitButton;
        final JButton zoomInButton;
        final JButton zoomOutButton;
        private final JScrollBar scrollBar;
        private boolean lastTrackingEnd;
        private double lastScale;
        private long lastOffset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public GraphTab(final GraphPanel panel) {
            this.panel = panel;

            setLayout(new BorderLayout());

            final boolean scaleToFit = panel.getChart().isFitToWindow();

            zoomInButton = new JButton(zoomInIcon);
            zoomInButton.setToolTipText(ZOOM_IN_TOOLTIP);
            zoomInButton.getAccessibleContext().setAccessibleName(ZOOM_IN_TOOLTIP);
            zoomOutButton = new JButton(zoomOutIcon);
            zoomOutButton.setToolTipText(ZOOM_OUT_TOOLTIP);
            zoomOutButton.getAccessibleContext().setAccessibleName(ZOOM_OUT_TOOLTIP);
            scaleToFitButton = new JButton(scaleToFit ? zoomIcon : scaleToFitIcon);
            scaleToFitButton.setToolTipText(scaleToFit ? FIXED_SCALE_TOOLTIP : SCALE_TO_FIT_TOOLTIP);
            scaleToFitButton.getAccessibleContext().setAccessibleName(scaleToFit ? FIXED_SCALE_TOOLTIP : SCALE_TO_FIT_TOOLTIP);

            scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

            zoomInButton.setEnabled(!scaleToFit);
            zoomOutButton.setEnabled(!scaleToFit);
            scrollBar.setEnabled(!scaleToFit);

            if (!panel.getChart().containsValidData()) {
                scaleToFitButton.setEnabled(false);
                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);
            }

            final JPanel graphPanel = new JPanel();
            graphPanel.setLayout(new BorderLayout());
            graphPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(0, 0, 0, 0)), new BevelBorder(BevelBorder.LOWERED)));
            graphPanel.add(panel, BorderLayout.CENTER);
            graphPanel.add(scrollBar, BorderLayout.SOUTH);

            final JPanel legendContainer = new JPanel();
            legendContainer.setLayout(new FlowLayout(FlowLayout.TRAILING));

            if (panel.getBigLegendPanel() != null) {
                legendContainer.add(panel.getBigLegendPanel());
            }

            //            add(toolBar, BorderLayout.NORTH);
            add(graphPanel, BorderLayout.CENTER);
            add(legendContainer, BorderLayout.SOUTH);

            zoomInButton.addActionListener(this);
            zoomOutButton.addActionListener(this);
            scaleToFitButton.addActionListener(this);

            panel.getChart().associateJScrollBar(scrollBar);
            panel.getChart().addChartActionListener(this);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        // --- ActionListener -------------------------------------------------------
        public void actionPerformed(final ActionEvent e) {
            final SynchronousXYChart xyChart = panel.getChart();

            if (e.getSource() == scaleToFitButton) {
                if (xyChart.isFitToWindow()) {
                    if (lastTrackingEnd) {
                        xyChart.setTrackingEnd(lastScale);
                    } else {
                        xyChart.setScaleAndOffsetX(lastScale, lastOffset);
                    }
                } else {
                    lastScale = xyChart.getScale();
                    lastOffset = xyChart.getViewOffsetX();
                    lastTrackingEnd = xyChart.isTrackingEnd();
                    xyChart.setFitToWindow();
                }

                //updateButtons();
            } else if (e.getSource() == zoomInButton) {
                xyChart.setScale(xyChart.getScale() * 2);
            } else if (e.getSource() == zoomOutButton) {
                xyChart.setScale(xyChart.getScale() / 2);
            }
        }

        public void chartDataChanged() {
            updateZoomButtons();
        }

        public void chartFitToWindowChanged() {
            if (panel.getChart().isFitToWindow()) {
                scaleToFitButton.setIcon(zoomIcon);
                scaleToFitButton.setToolTipText(FIXED_SCALE_TOOLTIP);
                scaleToFitButton.getAccessibleContext().setAccessibleName(FIXED_SCALE_TOOLTIP);
            } else {
                scaleToFitButton.setIcon(scaleToFitIcon);
                scaleToFitButton.setToolTipText(SCALE_TO_FIT_TOOLTIP);
                scaleToFitButton.getAccessibleContext().setAccessibleName(SCALE_TO_FIT_TOOLTIP);
            }

            updateZoomButtons();
        }

        public void chartPanned() {
        }

        public void chartTrackingEndChanged() {
        }

        public void chartZoomed() {
            updateZoomButtons();
        }

        // --- ChartActionListener -------------------------------------------------
        private void updateZoomButtons() {
            if (!panel.getChart().containsValidData()) {
                scaleToFitButton.setEnabled(false);
                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);
            } else {
                scaleToFitButton.setEnabled(true);

                if (panel.getChart().isFitToWindow()) {
                    zoomInButton.setEnabled(false);
                    zoomOutButton.setEnabled(false);
                } else {
                    if (panel.getChart().isMaximumZoom()) {
                        zoomInButton.setEnabled(false);
                    } else {
                        zoomInButton.setEnabled(true);
                    }

                    if (panel.getChart().isMinimumZoom()) {
                        zoomOutButton.setEnabled(false);
                    } else {
                        zoomOutButton.setEnabled(true);
                    }
                }
            }
        }
    }

    private final class MemoryActionsHandler implements MemoryResUserActionsHandler {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void showSourceForMethod(final String className, final String methodName, final String methodSig) {
            if (className.length() == 1) {
                if (BOOLEAN_CODE.equals(className) || CHAR_CODE.equals(className) || BYTE_CODE.equals(className)
                        || SHORT_CODE.equals(className) || INT_CODE.equals(className) || LONG_CODE.equals(className)
                        || FLOAT_CODE.equals(className) || DOUBLE_CODE.equals(className)) {
                    // primitive type
                    Profiler.getDefault().displayWarning(CANNOT_SHOW_PRIMITIVE_SRC_MSG);

                    return;
                }
            }

            Profiler.getDefault().openJavaSource(className, methodName, methodSig);
        }

        public void showStacksForClass(final int selectedClassId, final int sortingColumn, final boolean sortingOrder) {
            IDEUtils.runInProfilerRequestProcessor(new Runnable() {
                    public void run() {
                        final LoadedSnapshot ls = ResultsManager.getDefault().takeSnapshot();

                        if (ls != null) {
                            IDEUtils.runInEventDispatchThread(new Runnable() {
                                    public void run() {
                                        SnapshotResultsWindow srw = SnapshotResultsWindow.get(ls, sortingColumn, sortingOrder);

                                        if (srw != null) {
                                            srw.displayStacksForClass(selectedClassId, sortingColumn, sortingOrder);
                                        }
                                    }
                                });
                        }
                    }
                });
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.profiler.LiveResultsWindow"); // NOI18N
                                                                                                              // -----
                                                                                                              // I18N String constants
    private static final String UPDATE_RESULTS_AUTOMATICALLY_TOOLTIP = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                           "LiveResultsWindow_UpdateResultsAutomaticallyTooltip"); // NOI18N
    private static final String UPDATE_RESULTS_NOW_TOOLTIP = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                 "LiveResultsWindow_UpdateResultsNowTooltip"); // NOI18N
    private static final String RUN_GC_TOOLTIP = NbBundle.getMessage(LiveResultsWindow.class, "LiveResultsWindow_RunGCTooltip"); // NOI18N
    private static final String NO_PROFILING_RESULTS_LABEL_TEXT = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                      "LiveResultsWindow_NoProfilingResultsLabelText"); // NOI18N
    private static final String ERROR_DISPLAYING_STACK_TRACES_MSG = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                        "LiveResultsWindow_ErrorDisplayingStackTracesMsg"); // NOI18N
    private static final String ERROR_DISPLAYING_CALL_GRAPH_MSG = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                      "LiveResultsWindow_ErrorDisplayingCallGraphMsg"); // NOI18N
    private static final String ERROR_INSTRUMENTING_ROOT_METHOD_MSG = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                          "LiveResultsWindow_ErrorInstrumentingRootMethodMsg"); // NOI18N
    private static final String LIVE_RESULTS_TAB_NAME = NbBundle.getMessage(LiveResultsWindow.class,
                                                                            "LiveResultsWindow_LiveResultsTabName"); // NOI18N
    private static final String HISTORY_TAB_NAME = NbBundle.getMessage(LiveResultsWindow.class, "LiveResultsWindow_HistoryTabName"); // NOI18N
    private static final String LIVE_RESULTS_ACCESS_DESCR = NbBundle.getMessage(LiveResultsWindow.class,
                                                                                "LiveResultsWindow_LiveResultsAccessDescr"); // NOI18N
    private static final String ZOOM_IN_TOOLTIP = NbBundle.getMessage(TelemetryWindow.class, "TelemetryWindow_ZoomInTooltip"); // NOI18N
    private static final String ZOOM_OUT_TOOLTIP = NbBundle.getMessage(TelemetryWindow.class, "TelemetryWindow_ZoomOutTooltip"); // NOI18N
    private static final String FIXED_SCALE_TOOLTIP = NbBundle.getMessage(TelemetryWindow.class,
                                                                          "TelemetryWindow_FixedScaleTooltip"); // NOI18N
    private static final String SCALE_TO_FIT_TOOLTIP = NbBundle.getMessage(TelemetryWindow.class,
                                                                           "TelemetryWindow_ScaleToFitTooltip"); // NOI18N

    // -----
    private static final String HELP_CTX_KEY = "LiveResultsWindow.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static LiveResultsWindow defaultLiveInstance;
    private static final TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();
    private static final Image liveWindowIcon = Utilities.loadImage("org/netbeans/modules/profiler/resources/liveResultsWindow.png"); // NOI18N
    private static final AtomicBoolean resultsDumpForced = new AtomicBoolean(false);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    GraphTab graphTab;
    private JToolBar.Separator graphButtonsSeparator;
    private CPUResUserActionsHandler cpuActionsHandler;
    private Component lastFocusOwner;
    private DrillDown dd = null;
    private EmptyLiveResultsPanel noResultsPanel;
    private JButton runGCButton;
    private JButton updateNowButton;
    private JPanel currentDisplayComponent;
    private JPanel memoryTabPanel;
    private JTabbedPane tabs;
    private JToggleButton autoToggle;

    /*  private JComponent valueFilterComponent;
       private JSlider valueSlider; */
    private JToolBar toolBar;
    private LiveResultsPanel currentDisplay;
    private MemoryResUserActionsHandler memoryActionsHandler;
    private boolean autoRefresh = true;
    private boolean drillDownGroupOpened;
    private volatile boolean profilerRunning = false;
    private volatile boolean resultsAvailable = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public LiveResultsWindow() {
        setName(NbBundle.getMessage(LiveResultsWindow.class, "LAB_ResultsWindowName")); // NOI18N
        setIcon(liveWindowIcon);
        getAccessibleContext().setAccessibleDescription(LIVE_RESULTS_ACCESS_DESCR);
        //        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        memoryActionsHandler = new MemoryActionsHandler();
        cpuActionsHandler = new CPUActionsHandler();

        toolBar = createToolBar();
        toolBar.setBorder(new EmptyBorder(5, 5, 0, 5));

        add(toolBar, BorderLayout.NORTH);

        noResultsPanel = new EmptyLiveResultsPanel();
        noResultsPanel.setLayout(new BorderLayout());
        noResultsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        final JLabel noResultsLabel = new JLabel(NO_PROFILING_RESULTS_LABEL_TEXT);

        noResultsLabel.setFont(noResultsLabel.getFont().deriveFont(14));
        noResultsLabel.setIcon(new javax.swing.ImageIcon(Utilities.loadImage("org/netbeans/modules/profiler/ui/resources/monitoring.png")) //NOI18N
        );
        noResultsLabel.setIconTextGap(10);
        noResultsLabel.setEnabled(false);
        noResultsPanel.add(noResultsLabel, BorderLayout.NORTH);

        currentDisplay = null;
        currentDisplayComponent = noResultsPanel;
        add(noResultsPanel, BorderLayout.CENTER);

        //*************
        memoryTabPanel = new JPanel(new java.awt.BorderLayout());

        tabs = new JTabbedPane();
        // Fix for Issue 115062 (CTRL-PageUp/PageDown should move between snapshot tabs)
        tabs.getActionMap().getParent().remove("navigatePageUp"); // NOI18N
        tabs.getActionMap().getParent().remove("navigatePageDown"); // NOI18N
                                                                    // TODO: implement PreviousViewAction/NextViewAction handling for Live Memory Results

        memoryTabPanel.add(tabs, BorderLayout.CENTER);
        tabs.setTabPlacement(JTabbedPane.BOTTOM);
        tabs.addChangeListener(new ChangeListener() {
                public void stateChanged(final ChangeEvent e) {
                    updateGraphButtons();
                }
            });

        graphTab = new GraphTab(new HistoryPanel(History.getInstance()));

        History.getInstance().addHistoryListener(this);

        graphButtonsSeparator = new JToolBar.Separator();
        toolBar.add(graphButtonsSeparator);
        toolBar.add(graphTab.zoomInButton);
        toolBar.add(graphTab.zoomOutButton);
        toolBar.add(graphTab.scaleToFitButton);

        JPanel toolbarSpacer = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        toolbarSpacer.setOpaque(false);

        final DrillDownWindow drillDownWin = DrillDownWindow.getDefault();
        drillDownWin.closeIfOpened();
        drillDownWin.getPresenter().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (drillDownWin.getPresenter().isSelected()) {
                        drillDownWin.open();
                    } else {
                        drillDownWin.close();
                    }
                }
            });

        toolBar.add(toolbarSpacer);
        toolBar.add(drillDownWin.getPresenter());

        updateGraphButtons();
        hideDrillDown();
        //******************
        
        setFocusable(true);
        setRequestFocusEnabled(true);
        
        Profiler.getDefault().addProfilingStateListener(this);
        ResultsManager.getDefault().addResultsListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized LiveResultsWindow getDefault() {
        if (!hasDefault()) {
            IDEUtils.runInEventDispatchThreadAndWait(new Runnable() {
                    public void run() {
                        defaultLiveInstance = new LiveResultsWindow();
                    }
                });
        }

        return defaultLiveInstance;
    }

    public static void setPaused(boolean value) {
        //        paused = value;
    }

    public static void closeIfOpened() {
        if (hasDefault()) {
            IDEUtils.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        if (defaultLiveInstance.isOpened()) {
                            defaultLiveInstance.close();
                        }
                    }
                });
        }
    }

    // checks if default instance exists to prevent unnecessary instantiation from getDefault()
    public static boolean hasDefault() {
        return defaultLiveInstance != null;
    }

    public void setAutoRefresh(final boolean value) {
        if (autoRefresh != value) {
            autoRefresh = value;
            autoToggle.setSelected(value);
        }
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    // ------------------------------------------------------------------------------------------------------
    // TopComponent behavior
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public int getSortingColumn() {
        if (currentDisplay == null) {
            return CommonConstants.SORTING_COLUMN_DEFAULT;
        } else {
            return currentDisplay.getSortingColumn();
        }
    }

    public boolean getSortingOrder() {
        if (currentDisplay == null) {
            return false;
        } else {
            return currentDisplay.getSortingOrder();
        }
    }

    public BufferedImage getViewImage(boolean onlyVisibleArea) {
        if ((currentDisplayComponent == memoryTabPanel) && (tabs.getSelectedComponent() == graphTab)) {
            return UIUtils.createScreenshot(graphTab.panel);
        }

        if (currentDisplay == null) {
            return null;
        }

        return currentDisplay.getViewImage(onlyVisibleArea);
    }

    public String getViewName() {
        if ((currentDisplayComponent == memoryTabPanel) && (tabs.getSelectedComponent() == graphTab)) {
            return "memory-history-" + History.getInstance().getClassName(); // NOI18N
        }

        if (currentDisplay == null) {
            return null;
        }

        return currentDisplay.getViewName();
    }

    public void componentActivated() {
        super.componentActivated();

        if (lastFocusOwner != null) {
            lastFocusOwner.requestFocus();
        } else if (currentDisplayComponent != null) {
            currentDisplayComponent.requestFocus();
        }
    }

    public void componentDeactivated() {
        super.componentDeactivated();

        lastFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }

    public boolean fitsVisibleArea() {
        if ((currentDisplayComponent == memoryTabPanel) && (tabs.getSelectedComponent() == graphTab)) {
            return true;
        }

        return (currentDisplay != null) && currentDisplay.fitsVisibleArea();
    }

    /** This method is called before the profiling session ends to enable
     * asynchronous live results update if auto refresh is on.
     */
    public void handleShutdown() {
        profilerRunning = false;

        if (isShowing()) {
            //      if (currentDisplay != null) currentDisplay.handleShutdown();

            // TODO update the display
            hideDrillDown(); // close the drilldown; this is because sometimes the JVM can be terminated before we get here - look at Profiler.fireProfilingStateChange
            requestProfilingDataUpdate(false);
        }

        //    if (currentDisplay != null) {
        //      currentDisplay.handleShutdown();
        //      currentDisplay = null;
        //    }
        //    updateResultsDisplay(false);
        //    resetResultsDisplay();
    }

    public void handleStartup() {
        profilerRunning = true;
    }

    // --- Save Current View action support ------------------------------------
    public boolean hasView() {
        if ((currentDisplayComponent == memoryTabPanel) && (tabs.getSelectedComponent() == graphTab)) {
            return true;
        }

        return !noResultsPanel.isShowing() && (currentDisplay != null) && currentDisplay.hasView();
    }

    public void historyLogging() {
        if (currentDisplay instanceof LiveAllocResultsPanel || currentDisplay instanceof LiveLivenessResultsPanel) {
            tabs.setEnabledAt(1, true);
            tabs.setTitleAt(1,
                            NbBundle.getMessage(LiveResultsWindow.class, "LiveResultsWindow_ClassHistoryTabName",
                                                new Object[] { History.getInstance().getClassName() })); // NOI18N
            tabs.setSelectedIndex(1);
        }
    }

    public void ideClosing() {
        hideDrillDown();
    }

    public void instrumentationChanged(int oldInstrType, int currentInstrType) {
        requestProfilingDataUpdate(false);
    }

    public void profilingStateChanged(ProfilingStateEvent e) {
        updateActions(e.getNewState());

        switch (e.getNewState()) {
            case Profiler.PROFILING_INACTIVE:
                handleShutdown();

                break;
            case Profiler.PROFILING_RUNNING:
                handleStartup();

                break;
        }
    }

    /**
     * This method is called periodically every 2400 ms, from the ProfilerMonitor monitor thread, giving the
     * ResultsManager a chance to update live results.
     * Can be called explicitely to force update of the live results outside of the periodic cycle.
     * The results will only be updated (and associated event fired) if the LiveResultsWindow is displayed.
     */
    public boolean refreshLiveResults() {
        if (isAutoRefresh() && isShowing()) {
            requestProfilingDataUpdate(false);

            return true;
        } else {
            // -----------------------------------------------------------------------
            // Temporary workaround to refresh profiling points when LiveResultsWindow is not refreshing
            // TODO: move this code to a separate class performing the update if necessary
            if (NetBeansProfiler.getDefaultNB().processesProfilingPoints()) {
                callForceObtainedResultsDump(runner.getProfilerClient());
            }

            // -----------------------------------------------------------------------
            return false;
        }
    }

    public void resultsAvailable() {
    }

    public void resultsReset() {
        reset();
    }

    public void threadsMonitoringChanged() {
        // ignore
    }

    protected void componentClosed() {
        super.componentClosed();

        //    Profiler.getDefault().removeProfilingStateListener(this);
    }

    protected void componentHidden() {
        super.componentHidden();

        hideDrillDown();
    }

    //
    //  private ProfilerClientListener clientListener = new ProfilerClientListener() {
    //    public void instrumentationChanged(int oldInstrType, int newInstrType) {
    //      deconfigureForInstrType(oldInstrType);
    //      configureForInstrType(newInstrType);
    //    }
    //  };

    /**
     * Called when the topcomponent has been just open
     */
    protected void componentOpened() {
        super.componentOpened();

        //    Profiler.getDefault().addProfilingStateListener(this);
    }

    protected void componentShowing() {
        super.componentShowing();
        updateResultsDisplay();
    }

    /**
     * Subclasses are encouraged to override this method to provide preferred value
     * for unique TopComponent Id returned by getID. Returned value is used as starting
     * value for creating unique TopComponent ID.
     * Value should be preferably unique, but need not be.
     */
    protected String preferredID() {
        return this.getClass().getName();
    }

    /**
     * This should be called when the app is restarted or "Reset Collected Results" is invoked (because once this happened,
     * there are all sorts of data that's going to be deleted/changed, and an attempt to do something with old results displayed
     * here can cause big problems). It should also set the results panel invisible (or is it already happening?) etc.
     */
    void reset() {
        if (currentDisplay != null) {
            currentDisplay.reset();
            resetResultsDisplay();
        }

        resetDrillDown();
    }

    // -- Private implementation -------------------------------------------------------------------------------------------
    private static boolean callForceObtainedResultsDump(final ProfilerClient client) {
        resultsDumpForced.set(true);

        try {
            if (client.getCurrentInstrType() != ProfilerEngineSettings.INSTR_CODE_REGION) {
                client.forceObtainedResultsDump(true);
            }

            return true;
        } catch (ClientUtils.TargetAppOrVMTerminated targetAppOrVMTerminated) {
            return false;
        }
    }

    private boolean isProfiling() {
        return runner.getProfilerClient().getCurrentInstrType() != ProfilerEngineSettings.INSTR_NONE;
    }

    private static boolean checkIfResultsExist(final ProfilerClient client, final int currentInstrType) {
        switch (currentInstrType) {
            case ProfilerEngineSettings.INSTR_RECURSIVE_FULL:
            case ProfilerEngineSettings.INSTR_RECURSIVE_SAMPLED:

            //        return client.getCPUCallGraphBuilder().resultsExist(); // TODO
            case ProfilerEngineSettings.INSTR_OBJECT_ALLOCATIONS:
            case ProfilerEngineSettings.INSTR_OBJECT_LIVENESS:
                return getDefault().resultsAvailable;
            case CommonConstants.INSTR_CODE_REGION:

                try {
                    return client.cpuResultsExist();
                } catch (ClientUtils.TargetAppOrVMTerminated targetAppOrVMTerminated) {
                    return false;
                }
        }

        return false;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar() {
            public Component add(Component comp) {
                if (comp instanceof JButton) {
                    UIUtils.fixButtonUI((JButton) comp);
                }

                return super.add(comp);
            }
        };

        toolBar.setFloatable(false);
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        autoToggle = new JToggleButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/profiler/resources/autoRefresh.png") // NOI18N
        ));
        autoToggle.setSelected(true);
        autoToggle.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    autoRefresh = autoToggle.isSelected();
                }
            });
        autoToggle.setToolTipText(UPDATE_RESULTS_AUTOMATICALLY_TOOLTIP);
        autoToggle.getAccessibleContext().setAccessibleName(UPDATE_RESULTS_AUTOMATICALLY_TOOLTIP);

        updateNowButton = new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/profiler/resources/updateNow.png") // NOI18N
        ));
        updateNowButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    requestProfilingDataUpdate(true);
                }
            });
        updateNowButton.setToolTipText(UPDATE_RESULTS_NOW_TOOLTIP);
        updateNowButton.getAccessibleContext().setAccessibleName(UPDATE_RESULTS_NOW_TOOLTIP);

        runGCButton = new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/profiler/actions/resources/runGC.png") // NOI18N
        ));
        runGCButton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    try {
                        runner.runGC();
                    } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                        Profiler.getDefault().displayError(ex.getMessage());
                        ProfilerLogger.log(ex);
                    }

                    requestProfilingDataUpdate(true);
                }
            });
        runGCButton.setToolTipText(RUN_GC_TOOLTIP);
        runGCButton.getAccessibleContext().setAccessibleName(RUN_GC_TOOLTIP);

        // todo: add profiler listener to enable/disable buttons
        toolBar.add(autoToggle);
        toolBar.add(updateNowButton);
        toolBar.add(runGCButton);
        toolBar.add(new ResetResultsAction());
        toolBar.addSeparator();
        toolBar.add(((Presenter.Toolbar) SystemAction.get(TakeSnapshotAction.class)).getToolbarPresenter());
        toolBar.addSeparator();
        toolBar.add(new SaveViewAction(this));

        /*    toolBar.addSeparator();
        
                                                                      valueSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0) {
                                                                        public Dimension getMaximumSize() {
                                                                          return new Dimension(100, super.getMaximumSize().height);
                                                                        }
                                                                      };
                                                                      toolBar.add(valueSlider);
                                                                      valueFilterComponent = valueSlider;
        
                                                                      valueSlider.addChangeListener(new ChangeListener() {
        
                                                                        public void stateChanged(ChangeEvent e) {
                                                                          // make cubic curve instead of linear
                                                                          double val = (double) valueSlider.getValue() / 10f;
                                                                          val = val * val;
                                                                          if (currentDisplay != null) currentDisplay.updateValueFilter(val);
                                                                        }
                                                                      }); */
        return toolBar;
    }

    private void hideDrillDown() {
        IDEUtils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("LiveResultsGroup"); //NOI18N

                    if (group != null) {
                        group.close();
                    }

                    drillDownGroupOpened = false;
                    DrillDownWindow.getDefault().getPresenter().setEnabled(false);
                }
            });
    }

    private LiveResultsPanel preparePanelForInstrType(int instrumentationType) {
        LiveResultsPanel aPanel = null;
        dd = null;

        switch (instrumentationType) {
            case ProfilerEngineSettings.INSTR_OBJECT_ALLOCATIONS: {
                LiveAllocResultsPanel allocPanel = new LiveAllocResultsPanel(runner, memoryActionsHandler, History.getInstance());
                currentDisplayComponent = memoryTabPanel;
                currentDisplayComponent.setBorder(new EmptyBorder(5, 0, 0, 0));

                if (tabs.getComponentCount() > 0) {
                    tabs.removeAll();
                }

                tabs.addTab(LIVE_RESULTS_TAB_NAME, allocPanel);
                tabs.addTab(HISTORY_TAB_NAME, graphTab);
                tabs.setEnabledAt(1, false);
                aPanel = allocPanel;

                break;
            }
            case ProfilerEngineSettings.INSTR_OBJECT_LIVENESS: {
                LiveLivenessResultsPanel livenessPanel = new LiveLivenessResultsPanel(runner, memoryActionsHandler,
                                                                                      History.getInstance());
                currentDisplayComponent = memoryTabPanel;
                currentDisplayComponent.setBorder(new EmptyBorder(5, 0, 0, 0));

                if (tabs.getComponentCount() > 0) {
                    tabs.removeAll();
                }

                tabs.addTab(LIVE_RESULTS_TAB_NAME, livenessPanel);
                tabs.addTab(HISTORY_TAB_NAME, graphTab);
                tabs.setEnabledAt(1, false);
                aPanel = livenessPanel;

                break;
            }
            case ProfilerEngineSettings.INSTR_RECURSIVE_FULL:
            case ProfilerEngineSettings.INSTR_RECURSIVE_SAMPLED: {
                Project project = NetBeansProfiler.getDefaultNB().getProfiledProject();
                ProjectTypeProfiler ptp = ProjectUtilities.getProjectTypeProfiler(project);

                List additionalStats = new ArrayList();

                dd = new DrillDown(runner.getProfilerClient(), ptp.getMarkHierarchyRoot());

                runner.getProfilerClient().getMarkFilter().removeAllEvaluators();
                runner.getProfilerClient().getMarkFilter().addEvaluator(dd);

                StatisticalModuleContainer container = Lookup.getDefault().lookup(StatisticalModuleContainer.class);
                additionalStats.addAll(container.getAllModules());

                final LiveFlatProfilePanel cpuPanel = new LiveFlatProfilePanel(runner, cpuActionsHandler, additionalStats);
                dd.addListener(new DrillDownListener() {
                        public void dataChanged() {
                        }

                        public void drillDownPathChanged(List list) {
                            cpuPanel.updateLiveResults();
                        }
                    });

                DrillDownWindow.getDefault().setDrillDown(dd, additionalStats);

                currentDisplayComponent = cpuPanel;

                currentDisplayComponent.setBorder(new EmptyBorder(5, 5, 5, 5));
                aPanel = cpuPanel;

                break;
            }
            case ProfilerEngineSettings.INSTR_CODE_REGION: {
                CodeRegionLivePanel regionPanel = new CodeRegionLivePanel(runner.getProfilerClient());
                currentDisplayComponent = regionPanel;
                currentDisplayComponent.setBorder(new EmptyBorder(5, 5, 5, 5));
                aPanel = regionPanel;

                break;
            }
            case ProfilerEngineSettings.INSTR_NONE:
                throw new IllegalStateException(); // this cannot happen
                                                   //        break;
        }

        return aPanel;
    }

    private void requestProfilingDataUpdate(final boolean force) {
        IDEUtils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    if (!isAutoRefresh() && !force) {
                        return;
                    }

                    if (!isProfiling() || !isOpened()) {
                        return;
                    }

                    RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                // send a command to server to generate the newest live data
                                callForceObtainedResultsDump(runner.getProfilerClient());
                            }
                        });
                }
            });
    }

    private void resetDrillDown() {
        if (dd != null) {
            dd.reset();
        }
    }

    private void resetResultsDisplay() {
        if ((currentDisplayComponent != null) && (currentDisplayComponent != noResultsPanel)) {
            remove(currentDisplayComponent);
            currentDisplay = null;
            currentDisplayComponent = noResultsPanel;
            add(noResultsPanel, BorderLayout.CENTER);
            graphButtonsSeparator.setVisible(false);
            graphTab.zoomInButton.setVisible(false);
            graphTab.zoomOutButton.setVisible(false);
            graphTab.scaleToFitButton.setVisible(false);
            revalidate();
            repaint();
            hideDrillDown();
        }
    }

    private void showDrillDown() {
        IDEUtils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("LiveResultsGroup"); //NOI18N

                    if (group != null) {
                        group.open();
                        drillDownGroupOpened = true;
                        DrillDownWindow.getDefault().getPresenter().setEnabled(true);

                        //          if (DrillDownWindow.getDefault().needsDocking()) DrillDownWindow.getDefault().open(); // Do not open DrillDown by default, only on demand by DrillDownWindow.getDefault().getPresenter()
                    } else {
                        LOGGER.severe("LiveResultsGroup not existing!"); // NOI18N
                    }
                }
            });
    }

    private void updateActions(int newState) {
        runGCButton.setEnabled(newState == Profiler.PROFILING_RUNNING);
        updateNowButton.setEnabled(newState == Profiler.PROFILING_RUNNING);
    }

    private void updateDrillDown() {
        if (dd != null) {
            dd.refresh(); // TODO race condition by cleaning the dd variable!
        }

        if (LOGGER.isLoggable(Level.FINE) && (currentDisplayComponent != null)) {
            LOGGER.fine("updating drilldown: " + currentDisplayComponent.getClass().getName()); // NOI18N
        }

        if (profilerRunning && currentDisplayComponent instanceof LiveFlatProfilePanel && (dd != null) && dd.isValid()) {
            LOGGER.fine("Showing drilldown"); // NOI18N

            if (!drillDownGroupOpened && isVisible()) {
                showDrillDown();
            }
        } else {
            LOGGER.fine("Hiding drilldown"); // NOI18N

            if (drillDownGroupOpened) {
                hideDrillDown();
            }
        }
    }

    private void updateGraphButtons() {
        boolean graphVisible = (currentDisplayComponent == memoryTabPanel) && (tabs.getSelectedComponent() == graphTab);
        graphButtonsSeparator.setVisible(graphVisible);
        graphTab.zoomInButton.setVisible(graphVisible);
        graphTab.zoomOutButton.setVisible(graphVisible);
        graphTab.scaleToFitButton.setVisible(graphVisible);
    }

    private void updateResultsDisplay() {
        if (!isOpened()) {
            return; // do nothing if i'm closed 
        }

        if (!resultsDumpForced.getAndSet(false) && !isAutoRefresh()) {
            return; // process only forced results if autorefresh is off
        }

        if (!resultsAvailable) {
            currentDisplay = null;
            currentDisplayComponent = noResultsPanel;

            return;
        }

        // does the current display support the instrumentation in use?
        boolean instrSupported = (currentDisplayComponent != null)
                                 && ((currentDisplay != null)
                                     ? currentDisplay.supports(runner.getProfilerClient().getCurrentInstrType()) : false);

        if (!instrSupported) {
            if (currentDisplayComponent != null) {
                remove(currentDisplayComponent);
            }

            if (currentDisplay != null) {
                currentDisplay.handleRemove();
            }

            currentDisplay = preparePanelForInstrType(runner.getProfilerClient().getCurrentInstrType());
            add(currentDisplayComponent, BorderLayout.CENTER);
            revalidate();
            repaint();
            updateGraphButtons();
            IDEUtils.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        currentDisplayComponent.requestFocusInWindow(); // must be invoked lazily to override default focus behavior
                    }
                });
        }

        if (currentDisplay != null) {
            currentDisplay.updateLiveResults();
        }

        updateDrillDown();
    }
    }
