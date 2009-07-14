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
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbBundle;

/**
 * A panel to display TA threads and their state.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadsPanel extends JPanel implements AdjustmentListener, ActionListener, TableColumnModelListener,
        DataManagerListener {
    // I18N String constants
    private static final ResourceBundle messages = NbBundle.getBundle(ThreadsPanel.class);
    private static final String VIEW_THREADS_ALL = messages.getString("ThreadsPanel_ViewThreadsAll"); // NOI18N
    private static final String VIEW_THREADS_LIVE = messages.getString("ThreadsPanel_ViewThreadsLive"); // NOI18N
    private static final String VIEW_THREADS_FINISHED = messages.getString("ThreadsPanel_ViewThreadsFinished"); // NOI18N
    private static final String VIEW_THREADS_SELECTION = messages.getString("ThreadsPanel_ViewThreadsSelection"); // NOI18N
    private static final String THREADS_TABLE = messages.getString("ThreadsPanel_ThreadsTable"); // NOI18N
    private static final String ENABLE_THREADS_PROFILING = messages.getString("ThreadsPanel_EnableThreadsProfiling"); // NOI18N
    private static final String ZOOM_IN_TOOLTIP = messages.getString("ThreadsPanel_ZoomInToolTip"); // NOI18N
    private static final String ZOOM_OUT_TOOLTIP = messages.getString("ThreadsPanel_ZoomOutToolTip"); // NOI18N
    private static final String FIXED_SCALE_TOOLTIP = messages.getString("ThreadsPanel_FixedScaleToolTip"); // NOI18N
    private static final String SCALE_TO_FIT_TOOLTIP = messages.getString("ThreadsPanel_ScaleToFitToolTip"); // NOI18N
    private static final String THREADS_MONITORING_DISABLED_1_MSG = messages.getString("ThreadsPanel_ThreadsMonitoringDisabled1Msg"); // NOI18N
    private static final String THREADS_MONITORING_DISABLED_2_MSG = messages.getString("ThreadsPanel_ThreadsMonitoringDisabled2Msg"); // NOI18N
    private static final String NO_PROFILING_MSG = messages.getString("ThreadsPanel_NoProfilingMsg"); // NOI18N
    private static final String THREADS_COLUMN_NAME = messages.getString("ThreadsPanel_ThreadsColumnName"); // NOI18N
    private static final String TIMELINE_COLUMN_NAME = messages.getString("ThreadsPanel_TimelineColumnName"); // NOI18N
    private static final String SUMMARY_COLUMN_NAME = messages.getString("ThreadsPanel_SummaryColumnName"); // NOI18N
    private static final String SELECTED_THREADS_ITEM = messages.getString("ThreadsPanel_SelectedThreadsItem"); // NOI18N
    private static final String THREAD_DETAILS_ITEM = messages.getString("ThreadsPanel_ThreadDetailsItem"); // NOI18N
    private static final String TABLE_ACCESS_NAME = messages.getString("ThreadsPanel_TableAccessName"); // NOI18N
    private static final String TABLE_ACCESS_DESCR = messages.getString("ThreadsPanel_TableAccessDescr"); // NOI18N
    private static final String COMBO_ACCESS_NAME = messages.getString("ThreadsPanel_ComboAccessName"); // NOI18N
    private static final String COMBO_ACCESS_DESCR = messages.getString("ThreadsPanel_ComboAccessDescr"); // NOI18N
    private static final String ENABLE_THREADS_MONITORING_BUTTON_ACCESS_NAME = messages.getString("ThreadsPanel_EnableThreadsMonitoringAccessName"); // NOI18N
    private static final String SHOW_LABEL_TEXT = messages.getString("ThreadsPanel_ShowLabelText"); // NOI18N
    // -----
    private static final int NAME_COLUMN_INDEX = 0;
    private static final int DISPLAY_COLUMN_INDEX = 1;
    private static final int SUMMARY_COLUMN_INDEX = 2;
    private static final int RIGHT_DISPLAY_MARGIN = 20; // extra space [pixels] on the right end of the threads display
    private static final int LEFT_DISPLAY_MARGIN = 20;
    private static final int NAME_COLUMN_WIDTH = 100;
    private static final int MIN_NAME_COLUMN_WIDTH = 55;
    static final int MIN_SUMMARY_COLUMN_WIDTH = 62;

    private ArrayList<Integer> filteredDataToDataIndex = new ArrayList<Integer>();
    private CustomTimeLineViewport viewPort;
    private DefaultComboBoxModel comboModel;
    private DefaultComboBoxModel comboModelWithSelection;
    private JButton enableThreadsMonitoringButton;
    private JButton scaleToFitButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JComboBox threadsSelectionCombo;
    private JLabel enableThreadsMonitoringLabel1;
    private JLabel enableThreadsMonitoringLabel2;
    private JLabel enableThreadsMonitoringLabel3;
    private JLabel monitorLegend;
    private JLabel runningLegend;
    private JLabel sleepingLegend;
    private JLabel waitLegend;
    private JMenuItem showOnlySelectedThreads;
    private JMenuItem showThreadsDetails;
    private JPanel contentPanel; // panel with CardLayout containing threadsTable & enable threads profiling notification and button
    private JPanel notificationPanel;
    private JPopupMenu popupMenu;
    private JScrollBar scrollBar; // scrollbar that is displayed in zoomed mode that allows to scroll in history
    private JScrollPane tableScroll;
    private JTable table; // table that displays individual threads
    private JToolBar buttonsToolBar;
    private ThreadsDataManager manager;
    private ThreadsDetailsCallback detailsCallback;
    private boolean internalChange = false; // prevents cycles in event handling
    private boolean internalScrollbarChange = false;
    private boolean scaleToFit = false;
    private boolean threadsMonitoringEnabled = false;
    private boolean trackingEnd = true;
    private float zoomResolutionPerPixel = 50f;
    private long viewEnd;
    private long viewStart = -1;

    /**
     * Creates a new threads panel that displays threads timeline from data provided
     * by specified ThreadsDataManager.
     *
     * @param manager The provider of threads data
     * @param detailsCallback A handler of displaying additional threads details or null, in which case the
     *                        popup menu action to display details will not be present
     */
    public ThreadsPanel(ThreadsDataManager manager, ThreadsDetailsCallback detailsCallback) {
        this.manager = manager;
        this.detailsCallback = detailsCallback;

        // create components

        // contentPanel for threadsTable and enable threads profiling notification
        contentPanel = new JPanel(new CardLayout());

        // threads table components
        table = createViewTable();
        table.setGridColor(UIUtils.TABLE_VERTICAL_GRID_COLOR);
        table.getAccessibleContext().setAccessibleName(TABLE_ACCESS_NAME);
        table.getAccessibleContext().setAccessibleDescription(TABLE_ACCESS_DESCR);
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "DEFAULT_ACTION"); // NOI18N
        table.getActionMap().put("DEFAULT_ACTION", // NOI18N
                new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        performDefaultAction();
                    }
                });

        scrollBar = new ThreadsScrollBar();
        zoomInButton = new JButton(new ImageIcon(ThreadsPanel.class.getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/zoomIn.png"))); // NOI18N
        zoomOutButton = new JButton(new ImageIcon(ThreadsPanel.class.getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/zoomOut.png"))); // NOI18N
        scaleToFitButton = new JButton(new ImageIcon(getClass().getResource(scaleToFit
                ? "/org/netbeans/modules/dlight/visualizers/threadmap/resources/zoom.png" // NOI18N
                : "/org/netbeans/modules/dlight/visualizers/threadmap/resources/scaleToFit.png"))); // NOI18N
        comboModel = new DefaultComboBoxModel(new Object[]{VIEW_THREADS_ALL, VIEW_THREADS_LIVE, VIEW_THREADS_FINISHED});
        comboModelWithSelection = new DefaultComboBoxModel(new Object[]{
                    VIEW_THREADS_ALL, VIEW_THREADS_LIVE, VIEW_THREADS_FINISHED,
                    VIEW_THREADS_SELECTION
                });
        threadsSelectionCombo = new JComboBox(comboModel) {

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(250, getPreferredSize().height);
            }

            ;
        };
        threadsSelectionCombo.getAccessibleContext().setAccessibleName(COMBO_ACCESS_NAME);
        threadsSelectionCombo.getAccessibleContext().setAccessibleDescription(COMBO_ACCESS_DESCR);

        JLabel showLabel = new JLabel(SHOW_LABEL_TEXT);
        showLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        showLabel.setLabelFor(threadsSelectionCombo);

        int mnemCharIndex = 0;
        showLabel.setDisplayedMnemonic(showLabel.getText().charAt(mnemCharIndex));
        showLabel.setDisplayedMnemonicIndex(mnemCharIndex);

        buttonsToolBar = new JToolBar(JToolBar.HORIZONTAL) {

            @Override
            public Component add(Component comp) {
                if (comp instanceof JButton) {
                    UIUtils.fixButtonUI((JButton) comp);
                }

                return super.add(comp);
            }
        };

        JPanel tablePanel = new JPanel();
        JPanel scrollPanel = new JPanel();
        popupMenu = initPopupMenu();

        // set properties
        zoomInButton.setEnabled(!scaleToFit);
        zoomOutButton.setEnabled(!scaleToFit);
        zoomInButton.setToolTipText(ZOOM_IN_TOOLTIP);
        zoomOutButton.setToolTipText(ZOOM_OUT_TOOLTIP);
        scaleToFitButton.setToolTipText(scaleToFit ? FIXED_SCALE_TOOLTIP : SCALE_TO_FIT_TOOLTIP);
        zoomInButton.getAccessibleContext().setAccessibleName(zoomInButton.getToolTipText());
        zoomOutButton.getAccessibleContext().setAccessibleName(zoomOutButton.getToolTipText());
        scaleToFitButton.getAccessibleContext().setAccessibleName(scaleToFitButton.getToolTipText());

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setSelectionBackground(UIUtils.TABLE_SELECTION_BACKGROUND_COLOR);
        table.setSelectionForeground(UIUtils.TABLE_SELECTION_FOREGROUND_COLOR);
        table.setShowGrid(false);
        table.setRowMargin(0);
        table.setRowHeight(23);

        DefaultTableCellRenderer defaultHeaderRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setBackground(Color.WHITE);
                component.setFont(table.getFont().deriveFont(Font.BOLD));

                if (component instanceof JComponent) {
                    ((JComponent) component).setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
                }

                return component;
            }
        };

        table.getTableHeader().setDefaultRenderer(defaultHeaderRenderer);
        table.getTableHeader().setReorderingAllowed(false);

        // fix the first column's width, and make the display column resize
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        table.getColumnModel().getColumn(NAME_COLUMN_INDEX).setMinWidth(MIN_NAME_COLUMN_WIDTH);
        table.getColumnModel().getColumn(NAME_COLUMN_INDEX).setMaxWidth(1000); // this is for some reason needed for the width to actually work
        table.getColumnModel().getColumn(NAME_COLUMN_INDEX).setPreferredWidth(NAME_COLUMN_WIDTH);

        ThreadStateHeaderRenderer headerRenderer = new ThreadStateHeaderRenderer(this);
        headerRenderer.setBackground(Color.WHITE);
        table.getColumnModel().getColumn(DISPLAY_COLUMN_INDEX).setHeaderRenderer(headerRenderer);

        table.getColumnModel().getColumn(SUMMARY_COLUMN_INDEX).setMinWidth(MIN_SUMMARY_COLUMN_WIDTH);
        table.getColumnModel().getColumn(SUMMARY_COLUMN_INDEX).setMaxWidth(MIN_SUMMARY_COLUMN_WIDTH);
        table.getColumnModel().getColumn(SUMMARY_COLUMN_INDEX).setPreferredWidth(MIN_SUMMARY_COLUMN_WIDTH);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.setColumnSelectionAllowed(false);
        columnModel.setColumnMargin(0);
        table.setDefaultRenderer(ThreadNameCellRenderer.class, new ThreadNameCellRenderer(this));
        table.setDefaultRenderer(ThreadStateCellRenderer.class, new ThreadStateCellRenderer(this));
        table.setDefaultRenderer(ThreadSummaryCellRenderer.class, new ThreadSummaryCellRenderer(this));
        buttonsToolBar.setFloatable(false);
        buttonsToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N

        // perform layout
        tablePanel.setLayout(new BorderLayout());
        scrollPanel.setLayout(new BorderLayout());
        scrollPanel.setBackground(Color.WHITE);

        buttonsToolBar.add(zoomInButton);
        buttonsToolBar.add(zoomOutButton);
        buttonsToolBar.add(scaleToFitButton);
        buttonsToolBar.addSeparator();
        buttonsToolBar.add(showLabel);
        buttonsToolBar.add(threadsSelectionCombo);
        scrollPanel.add(scrollBar, BorderLayout.CENTER);
        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        filler.setPreferredSize(new Dimension(MIN_SUMMARY_COLUMN_WIDTH, 0));
        scrollPanel.add(filler, BorderLayout.EAST);
        JPanel rest = new JPanel();
        rest.setLayout(new BorderLayout());
        rest.setBackground(Color.WHITE);
        scrollPanel.add(rest, BorderLayout.CENTER);
        rest.add(scrollBar, BorderLayout.EAST);

        //
        ThreadStateIcon runningIcon = new ThreadStateIcon(ThreadStateColumnImpl.THREAD_STATUS_RUNNING, 18, 9);
        ThreadStateIcon sleepingIcon = new ThreadStateIcon(ThreadStateColumnImpl.THREAD_STATUS_SLEEPING, 18, 9);
        ThreadStateIcon monitorIcon = new ThreadStateIcon(ThreadStateColumnImpl.THREAD_STATUS_MONITOR, 18, 9);
        ThreadStateIcon waitIcon = new ThreadStateIcon(ThreadStateColumnImpl.THREAD_STATUS_WAIT, 18, 9);

        runningLegend = new JLabel(ThreadStateColumnImpl.THREAD_STATUS_RUNNING_STRING, runningIcon, SwingConstants.LEADING);
        runningLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        sleepingLegend = new JLabel(ThreadStateColumnImpl.THREAD_STATUS_SLEEPING_STRING, sleepingIcon, SwingConstants.LEADING);
        sleepingLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        waitLegend = new JLabel(ThreadStateColumnImpl.THREAD_STATUS_WAIT_STRING, waitIcon, SwingConstants.LEADING);
        waitLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        monitorLegend = new JLabel(ThreadStateColumnImpl.THREAD_STATUS_MONITOR_STRING, monitorIcon, SwingConstants.LEADING);
        monitorLegend.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel legendPanel = new JPanel();
        legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        legendPanel.add(runningLegend);
        legendPanel.add(sleepingLegend);

        legendPanel.add(waitLegend);
        legendPanel.add(monitorLegend);

        //legendPanel.add(unknownLegend);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(legendPanel, BorderLayout.EAST);

        //scrollPanel.add(bottomPanel, BorderLayout.SOUTH);
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new BorderLayout());
        dataPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        tableScroll = new JScrollPane();
        tableScroll.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        tableScroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
        tableScroll.getCorner(JScrollPane.UPPER_RIGHT_CORNER).setBackground(Color.WHITE);
        viewPort = new CustomTimeLineViewport(this);
        viewPort.setView(table);
        viewPort.setBackground(table.getBackground());
        tableScroll.setViewport(viewPort);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dataPanel.add(tableScroll, BorderLayout.CENTER);
        dataPanel.add(scrollPanel, BorderLayout.SOUTH);
        tablePanel.add(dataPanel, BorderLayout.CENTER);
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);

        // enable threads profiling components
        notificationPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 15));
        notificationPanel.setBorder(dataPanel.getBorder());
        notificationPanel.setBackground(table.getBackground());

        Border myRolloverBorder = new CompoundBorder(new FlatToolBar.FlatRolloverButtonBorder(Color.GRAY, Color.LIGHT_GRAY),
                new FlatToolBar.FlatMarginBorder());

        enableThreadsMonitoringLabel1 = new JLabel(THREADS_MONITORING_DISABLED_1_MSG);
        enableThreadsMonitoringLabel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 3));
        enableThreadsMonitoringLabel1.setForeground(Color.DARK_GRAY);

        enableThreadsMonitoringButton = new JButton(new ImageIcon(getClass().getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/threadsView.png"))); // NOI18N
        enableThreadsMonitoringButton.setContentAreaFilled(false);
        enableThreadsMonitoringButton.setMargin(new Insets(3, 3, 0, 0));
        enableThreadsMonitoringButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        enableThreadsMonitoringButton.setHorizontalTextPosition(SwingConstants.CENTER);
        enableThreadsMonitoringButton.setRolloverEnabled(true);
        enableThreadsMonitoringButton.setBorder(myRolloverBorder);
        enableThreadsMonitoringButton.getAccessibleContext().setAccessibleName(ENABLE_THREADS_MONITORING_BUTTON_ACCESS_NAME);

        enableThreadsMonitoringLabel2 = new JLabel(THREADS_MONITORING_DISABLED_2_MSG);
        enableThreadsMonitoringLabel2.setBorder(BorderFactory.createEmptyBorder(20, 3, 20, 0));
        enableThreadsMonitoringLabel2.setForeground(Color.DARK_GRAY);

        enableThreadsMonitoringLabel3 = new JLabel(NO_PROFILING_MSG);
        enableThreadsMonitoringLabel3.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 0));
        enableThreadsMonitoringLabel3.setForeground(Color.DARK_GRAY);
        enableThreadsMonitoringLabel3.setVisible(false);

        notificationPanel.add(enableThreadsMonitoringLabel1);
        notificationPanel.add(enableThreadsMonitoringButton);
        notificationPanel.add(enableThreadsMonitoringLabel2);
        notificationPanel.add(enableThreadsMonitoringLabel3);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        contentPanel.add(notificationPanel, ENABLE_THREADS_PROFILING);
        contentPanel.add(tablePanel, THREADS_TABLE);

        add(buttonsToolBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        scrollBar.addAdjustmentListener(this);
        zoomInButton.addActionListener(this);
        zoomOutButton.addActionListener(this);
        scaleToFitButton.addActionListener(this);
        threadsSelectionCombo.addActionListener(this);
        showOnlySelectedThreads.addActionListener(this);

        if (detailsCallback != null) {
            showThreadsDetails.addActionListener(this);
        }

        table.getColumnModel().addColumnModelListener(this);
        table.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                refreshViewData();
                updateScrollbar();
                updateZoomButtonsEnabledState();
                ThreadsPanel.this.revalidate();
            }
        });

        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) || ((e.getKeyCode() == KeyEvent.VK_F10) && (e.getModifiers() == InputEvent.SHIFT_MASK))) {
                    int selectedRow = table.getSelectedRow();

                    if (selectedRow != -1) {
                        Rectangle cellRect = table.getCellRect(selectedRow, 0, false);
                        popupMenu.show(e.getComponent(), ((cellRect.x + table.getSize().width) > 50) ? 50 : 5, cellRect.y);
                    }
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                    int line = table.rowAtPoint(e.getPoint());

                    if ((line != -1) && (!table.isRowSelected(line))) {
                        if (e.isControlDown()) {
                            table.addRowSelectionInterval(line, line);
                        } else {
                            table.setRowSelectionInterval(line, line);
                        }
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedLine = table.rowAtPoint(e.getPoint());

                if (clickedLine != -1) {
                    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if ((e.getModifiers() == InputEvent.BUTTON1_MASK) && (e.getClickCount() == 2)) {
                        performDefaultAction();
                    }
                }
            }
        });
        addHierarchyListener(new HierarchyListener() {

            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        dataChanged();
                    }
                }
            }
        });

        // Disable traversing table cells using TAB and Shift+TAB
        Set<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>(table.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        table.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

        keys = new HashSet<AWTKeyStroke>(table.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        table.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);

        updateScrollbar();
        updateZoomButtonsEnabledState();
        manager.addDataListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    //public BufferedImage getCurrentViewScreenshot(boolean onlyVisibleArea) {
    //    if (onlyVisibleArea) {
    //        return UIUtils.createScreenshot(tableScroll);
    //    } else {
    //        return UIUtils.createScreenshot(table);
    //    }
    //}
    public long getDataEnd() {
        return manager.getEndTime();
    }

    public long getDataStart() {
        return manager.getStartTime();
    }

    public int getDisplayColumnWidth() {
        return table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width;
    }

    public int getDisplayColumnRest() {
        return table.getTableHeader().getHeaderRect(SUMMARY_COLUMN_INDEX).width;
    }

    public ThreadStateColumnImpl getThreadData(int index) {
        return manager.getThreadData(index);
    }

    // ---------------------------------------------------------------------------------------
    // Thread data
    public String getThreadName(int index) {
        return manager.getThreadName(index);
    }

    public long getViewEnd() {
        return viewEnd;
    }

    // ---------------------------------------------------------------------------------------
    // View controller
    public long getViewStart() {
        return viewStart;
    }

    /**
     * Invoked when one of the buttons is pressed
     */
    public void actionPerformed(ActionEvent e) {
        if (internalChange) {
            return;
        }

        if (e.getSource() == scaleToFitButton) {
            if (!scaleToFit) {
                scrollBar.setVisible(true);
                scaleToFitButton.setIcon(new ImageIcon(ThreadsPanel.class.getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/zoom.png"))); // NOI18N
                scaleToFit = true;
            } else {
                scaleToFit = false;
                scaleToFitButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/scaleToFit.png"))); // NOI18N
                scrollBar.setVisible(false);
                scrollBar.setValues(0, 0, 0, 0);
            }

            refreshViewData();
            updateScrollbar();
            updateZoomButtonsEnabledState();
            table.getTableHeader().repaint();
            viewPort.repaint();
        } else if (e.getSource() == zoomInButton) {
            zoomInButton.setEnabled(zoomResolutionPerPixel > 0.1);
            zoomResolutionPerPixel /= 2;
            refreshViewData();
            updateScrollbar();
            updateZoomButtonsEnabledState();
            table.getTableHeader().repaint();
            viewPort.repaint();
        } else if (e.getSource() == zoomOutButton) {
            zoomResolutionPerPixel *= 2;
            refreshViewData();
            updateScrollbar();
            updateZoomButtonsEnabledState();
            table.getTableHeader().repaint();
            viewPort.repaint();
        } else if (e.getSource() == threadsSelectionCombo) {
            if ((threadsSelectionCombo.getModel() == comboModelWithSelection) && (threadsSelectionCombo.getSelectedItem() != VIEW_THREADS_SELECTION)) {
                internalChange = true;

                Object selectedItem = threadsSelectionCombo.getSelectedItem();
                threadsSelectionCombo.setModel(comboModel);
                threadsSelectionCombo.setSelectedItem(selectedItem);
                internalChange = false;
            }

            table.clearSelection();
            dataChanged();
        } else if (e.getSource() == showOnlySelectedThreads) {
            for (int i = filteredDataToDataIndex.size() - 1; i >= 0; i--) {
                if (!table.isRowSelected(i)) {
                    filteredDataToDataIndex.remove(i);
                }
            }

            threadsSelectionCombo.setModel(comboModelWithSelection);
            threadsSelectionCombo.setSelectedItem(VIEW_THREADS_SELECTION);
            table.clearSelection();
        } else if (e.getSource() == showThreadsDetails) {
            performDefaultAction();
        }
    }

    // --- Save Current View action support --------------------------------------
    public void addSaveViewAction(AbstractAction saveViewAction) {
        JButton actionButton = buttonsToolBar.add(saveViewAction);
        buttonsToolBar.remove(actionButton);

        buttonsToolBar.add(actionButton, 0);
        buttonsToolBar.add(new JToolBar.Separator(), 1);
    }

    // ---------------------------------------------------------------------------------------
    // Handling profiling started & finished and threads monitoring enabled & disabled
    public void addThreadsMonitoringActionListener(ActionListener listener) {
        enableThreadsMonitoringButton.addActionListener(listener);
    }

    // ---------------------------------------------------------------------------------------
    // Listeners
    /**
     * Invoked when the scrollbar is moved.
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        // we know we are in zoom mode (in scaleToFit, the scrollbar is disabled)
        if (!internalScrollbarChange) {
            if ((scrollBar.getValue() + scrollBar.getVisibleAmount()) == scrollBar.getMaximum()) {
                trackingEnd = true;
            } else {
                trackingEnd = false;
                viewStart = manager.getStartTime() + scrollBar.getValue();
                viewEnd = viewStart + (long) (zoomResolutionPerPixel * table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width);
                ThreadsPanel.this.repaint();
            }
        }
    }

    public void columnAdded(TableColumnModelEvent e) {
    } // Ignored

    /**
     * Tells listeners that a column was moved due to a margin change.
     */
    public void columnMarginChanged(ChangeEvent e) {
        refreshViewData();
        updateScrollbar();
        updateZoomButtonsEnabledState();

        if (viewPort != null) {
            viewPort.repaint();
        }

        scrollBar.invalidate();
        ThreadsPanel.this.revalidate();
    }

    public void columnMoved(TableColumnModelEvent e) {
    } // Ignored

    public void columnRemoved(TableColumnModelEvent e) {
    } // Ignored

    public void columnSelectionChanged(ListSelectionEvent e) {
    } // Ignored

    /** Called when data in manager change */
    public void dataChanged() {
        UIUtils.runInEventDispatchThread(new Runnable() {

            public void run() {
                refreshUI();
            }
        });
    }

    public void dataReset() {
        filteredDataToDataIndex.clear();
        UIUtils.runInEventDispatchThread(new Runnable() {

            public void run() {
                refreshUI();
            }
        });
    }

    public boolean fitsVisibleArea() {
        return !tableScroll.getVerticalScrollBar().isVisible();
    }

    public boolean hasView() {
        return !notificationPanel.isShowing();
    }

    public void profilingSessionFinished() {
        enableThreadsMonitoringButton.setEnabled(false);
        enableThreadsMonitoringLabel1.setVisible(false);
        enableThreadsMonitoringLabel2.setVisible(false);
        enableThreadsMonitoringButton.setVisible(false);
        enableThreadsMonitoringLabel3.setVisible(true);
    }

    public void profilingSessionStarted() {
        enableThreadsMonitoringButton.setEnabled(true);
        enableThreadsMonitoringLabel1.setVisible(true);
        enableThreadsMonitoringLabel2.setVisible(true);
        enableThreadsMonitoringButton.setVisible(true);
        enableThreadsMonitoringLabel3.setVisible(false);
    }

    public void removeThreadsMonitoringActionListener(ActionListener listener) {
        enableThreadsMonitoringButton.removeActionListener(listener);
    }

    @Override
    public void requestFocus() {
        SwingUtilities.invokeLater(new Runnable() { // must be invoked lazily to override default focus of first component

            public void run() {
                if (table != null) {
                    table.requestFocus();
                }
            }
        });
    }

    public void threadsMonitoringDisabled() {
        threadsMonitoringEnabled = false;
        ((CardLayout) (contentPanel.getLayout())).show(contentPanel, ENABLE_THREADS_PROFILING);
        updateZoomButtonsEnabledState();
        threadsSelectionCombo.setEnabled(false);
    }

    public void threadsMonitoringEnabled() {
        threadsMonitoringEnabled = true;
        ((CardLayout) (contentPanel.getLayout())).show(contentPanel, THREADS_TABLE);
        updateZoomButtonsEnabledState();
        threadsSelectionCombo.setEnabled(true);
    }

    private JTable createViewTable() {
        return new JExtendedTable(new ThreadsTableModel()) {

            @Override
            public void mouseMoved(MouseEvent event) {
                // Identify table row and column at cursor
                int row = rowAtPoint(event.getPoint());
                int column = columnAtPoint(event.getPoint());

                // Only celltip for thread name is supported
                if (getColumnClass(column) != ThreadNameCellRenderer.class) {
                    CellTipManager.sharedInstance().setEnabled(false);

                    return;
                }

                // Return if table cell is the same as in previous event
                if ((row == lastRow) && (column == lastColumn)) {
                    return;
                }

                lastRow = row;
                lastColumn = column;

                if ((row < 0) || (column < 0)) {
                    CellTipManager.sharedInstance().setEnabled(false);

                    return;
                }

                Component cellRenderer = ((ThreadNameCellRenderer) (getCellRenderer(row, column))).getTableCellRendererComponentPersistent(this,
                        getValueAt(row,
                        column),
                        false,
                        false,
                        row,
                        column);
                Rectangle cellRect = getCellRect(row, column, false);

                // Return if celltip is not supported for the cell
                if (cellRenderer == null) {
                    CellTipManager.sharedInstance().setEnabled(false);

                    return;
                }

                int horizontalAlignment = ((ThreadNameCellRenderer) cellRenderer).getHorizontalAlignment();

                if ((horizontalAlignment == SwingConstants.TRAILING) || (horizontalAlignment == SwingConstants.RIGHT)) {
                    rendererRect = new Rectangle((cellRect.x + cellRect.width) - cellRenderer.getPreferredSize().width,
                            cellRect.y, cellRenderer.getPreferredSize().width,
                            cellRenderer.getPreferredSize().height);
                } else {
                    rendererRect = new Rectangle(cellRect.x, cellRect.y, cellRenderer.getPreferredSize().width,
                            cellRenderer.getPreferredSize().height);
                }

                // Return if cell contents is fully visible
                if ((rendererRect.x >= cellRect.x) && ((rendererRect.x + rendererRect.width) <= (cellRect.x + cellRect.width))) {
                    CellTipManager.sharedInstance().setEnabled(false);

                    return;
                }

                while (cellTip.getComponentCount() > 0) {
                    cellTip.remove(0);
                }

                cellTip.add(cellRenderer, BorderLayout.CENTER);
                cellTip.setPreferredSize(new Dimension(rendererRect.width + 2, getRowHeight(row) + 2));

                CellTipManager.sharedInstance().setEnabled(true);
            }
        };
    }

    private JPopupMenu initPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        showOnlySelectedThreads = new JMenuItem(SELECTED_THREADS_ITEM);

        if (detailsCallback != null) {
            Font boldfont = popup.getFont().deriveFont(Font.BOLD);
            showThreadsDetails = new JMenuItem(THREAD_DETAILS_ITEM);
            showThreadsDetails.setFont(boldfont);
            popup.add(showThreadsDetails);
            popup.add(new JSeparator());
        }

        popup.add(showOnlySelectedThreads);

        return popup;
    }

    private void performDefaultAction() {
        int[] array = table.getSelectedRows();

        for (int i = 0; i < array.length; i++) {
            array[i] = filteredDataToDataIndex.get(array[i]).intValue();
        }

        ThreadsPanel.this.detailsCallback.showDetails(array);
    }

    // @AWTBound
    private void refreshUI() {
        if (!isShowing()) {
            return;
        }

        updateFilteredData();
        refreshViewData();
        updateScrollbar();
        updateZoomButtonsEnabledState();
        table.invalidate();
        ThreadsPanel.this.revalidate(); // needed to reflect table height increase when new threads appear
        ThreadsPanel.this.repaint(); // needed to paint the table even if no relayout happens
    }

    /** Updates internal view-related data based on changed conditions (new data, change in layout),
     *  to maintain the view in expected condition after the change.
     */
    private void refreshViewData() {
        if (scaleToFit) {
            long dataLen = manager.getEndTime() - manager.getStartTime();
            int viewLen = table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width;
            float currentResolution = (float) dataLen / Math.max(viewLen - RIGHT_DISPLAY_MARGIN - LEFT_DISPLAY_MARGIN, 1);
            viewStart = manager.getStartTime() - (long) (currentResolution * LEFT_DISPLAY_MARGIN);
            viewEnd = manager.getEndTime() + (long) (currentResolution * RIGHT_DISPLAY_MARGIN);
        } else {
            long rightMarginInTime = (long) (zoomResolutionPerPixel * RIGHT_DISPLAY_MARGIN);
            long leftMarginInTime = (long) (zoomResolutionPerPixel * LEFT_DISPLAY_MARGIN);
            long widthInTime = (long) (zoomResolutionPerPixel * table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width);

            if (viewStart == -1) { // the first data came
                viewStart = manager.getStartTime() - leftMarginInTime;
                viewEnd = viewStart + widthInTime;
            }

            if (trackingEnd) {
                viewEnd = manager.getEndTime() + rightMarginInTime;
                viewStart = viewEnd - widthInTime;

                if (viewStart < (manager.getStartTime() - leftMarginInTime)) { // data do not fill display yet
                    viewStart = manager.getStartTime() - leftMarginInTime;
                    viewEnd = viewStart + widthInTime;
                }
            } else {
                if (viewStart < manager.getStartTime()) {
                    viewStart = manager.getStartTime() - rightMarginInTime;
                }

                viewEnd = viewStart + widthInTime;
            }
        }
    }

    /** Creates new filteredDataToDataIndex according to the current filter criterion */
    private void updateFilteredData() {
        if (threadsSelectionCombo.getSelectedItem() == VIEW_THREADS_SELECTION) {
            return; // do nothing, data already filtered
        }

        filteredDataToDataIndex.clear();

        for (int i = 0; i < manager.getThreadsCount(); i++) {
            // view all threads
            if (threadsSelectionCombo.getSelectedItem().equals(VIEW_THREADS_ALL)) {
                filteredDataToDataIndex.add(new Integer(i));

                continue;
            }

            // view live threads
            if (threadsSelectionCombo.getSelectedItem().equals(VIEW_THREADS_LIVE)) {
                ThreadStateColumnImpl threadData = manager.getThreadData(i);

                if (threadData.size() > 0) {
                    if (threadData.isAlive()) {
                        filteredDataToDataIndex.add(new Integer(i));
                    }
                }

                continue;
            }

            // view finished threads
            if (threadsSelectionCombo.getSelectedItem().equals(VIEW_THREADS_FINISHED)) {
                ThreadStateColumnImpl threadData = manager.getThreadData(i);

                if (threadData.size() > 0) {
                    if (!threadData.isAlive()) {
                        filteredDataToDataIndex.add(new Integer(i));
                    }
                } else {
                    // No state defined -> THREAD_STATUS_ZOMBIE assumed (thread could finish when monitoring was disabled)
                    filteredDataToDataIndex.add(new Integer(i));
                }

                continue;
            }
        }
    }

    private void updateScrollbar() {
        internalScrollbarChange = true;

        if (scrollBar.isVisible() == scaleToFit) {
            scrollBar.setVisible(!scaleToFit);
        }

        if (!scaleToFit) {
            int rightMarginInTime = (int) (zoomResolutionPerPixel * RIGHT_DISPLAY_MARGIN);
            int leftMarginInTime = (int) (zoomResolutionPerPixel * RIGHT_DISPLAY_MARGIN);

            int value = (int) (viewStart - manager.getStartTime()) + leftMarginInTime;
            int extent = (int) (viewEnd - viewStart);
            int intMax = (int) (manager.getEndTime() - manager.getStartTime()) + rightMarginInTime;

            //      System.out.println("max: "+intMax);
            //      System.out.println("value: "+value);
            //      System.out.println("extent: "+extent);
            boolean shouldBeVisible = true;

            if ((value == 0) && ((intMax - (value + extent)) <= 0)) {
                shouldBeVisible = false;
            }

            if (scrollBar.isVisible() != shouldBeVisible) {
                scrollBar.setVisible(shouldBeVisible);
            }

            if (shouldBeVisible) {
                scrollBar.setValues(value, extent, -leftMarginInTime, intMax);
                scrollBar.setBlockIncrement((int) (extent * 0.95f));
                scrollBar.setUnitIncrement(Math.max((int) (zoomResolutionPerPixel * 5), 1)); // at least 1
            }
        }

        internalScrollbarChange = false;
    }

    // ---------------------------------------------------------------------------------------
    // Private methods
    private void updateZoomButtonsEnabledState() {
        if (!threadsMonitoringEnabled) {
            zoomInButton.setEnabled(false);
            zoomOutButton.setEnabled(false);
            scaleToFitButton.setEnabled(false);
        } else {
            if (scaleToFit) {
                zoomInButton.setEnabled(false);
                zoomOutButton.setEnabled(false);
            } else {
                zoomInButton.setEnabled(zoomResolutionPerPixel > 0.1);

                // zoom out is enabled up until the actual data only cover 1/4 of the display area
                int viewWidth = table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width;
                zoomOutButton.setEnabled((zoomResolutionPerPixel * viewWidth) < (2f * (manager.getEndTime() - manager.getStartTime())));
            }

            scaleToFitButton.setEnabled(true);
            scaleToFitButton.setToolTipText(scaleToFit ? FIXED_SCALE_TOOLTIP : SCALE_TO_FIT_TOOLTIP);
        }
    }
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    /** A callback interface - implemented by provider of additional details of a set of threads */
    public interface ThreadsDetailsCallback {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        /** Displays a panel with details about specified threads
         *
         * @param indexes array of int indexes for threads to display
         */
        public void showDetails(int[] indexes);
    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------
    class ThreadsScrollBar extends JScrollBar {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ThreadsScrollBar() {
            super(JScrollBar.HORIZONTAL);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------
        @Override
        public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize();

            return new Dimension(table.getTableHeader().getHeaderRect(DISPLAY_COLUMN_INDEX).width, pref.height);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Model for the table
    private class ThreadsTableModel extends AbstractTableModel {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public Class getColumnClass(int column) {
            // The main purpose of this method is to make numeric values aligned properly inside table cells
            switch (column) {
                case NAME_COLUMN_INDEX:
                    return ThreadNameCellRenderer.class;
                case DISPLAY_COLUMN_INDEX:
                    return ThreadStateCellRenderer.class;
                case SUMMARY_COLUMN_INDEX:
                    return ThreadSummaryCellRenderer.class;
                default:
                    return String.class;
            }
        }

        public int getColumnCount() {
            return 3;
        }

        /**
         * Returns a default name for the column using spreadsheet conventions:
         * A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
         * returns an empty string.
         *
         * @param column the column being queried
         * @return a string containing the default name of <code>column</code>
         */
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case NAME_COLUMN_INDEX:
                    return THREADS_COLUMN_NAME;
                case DISPLAY_COLUMN_INDEX:
                    return TIMELINE_COLUMN_NAME;
                case SUMMARY_COLUMN_INDEX:
                    return SUMMARY_COLUMN_NAME;
                default:
                    return null;
            }
        }

        public int getRowCount() {
            //return manager.getThreadsCount();
            return filteredDataToDataIndex.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case NAME_COLUMN_INDEX:
                    return filteredDataToDataIndex.get(rowIndex);
                case DISPLAY_COLUMN_INDEX:
                    return getThreadData( filteredDataToDataIndex.get(rowIndex).intValue() );
                case SUMMARY_COLUMN_INDEX:
                    return getThreadData( filteredDataToDataIndex.get(rowIndex).intValue() );
                default:
                    return null;
            }
        }
    }
}
