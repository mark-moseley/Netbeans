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

package org.netbeans.lib.profiler.ui.threads;

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.threads.ThreadData;
import org.netbeans.lib.profiler.ui.charts.DynamicPieChartModel;
import org.netbeans.lib.profiler.ui.charts.PieChart;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;


/** A component that shows details for single thread.
 *
 * @author Jiri Sedlacek
 * @author Ian Formanek
 */
public class ThreadDetailsComponent extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    // ---------------------------------------------------------------------------------------
    // Timeline component
    private class SingleThreadState extends JPanel implements MouseListener, MouseMotionListener {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static final int DISPLAY_MARGIN = 20;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Color SELECTION_BRIGHT = new Color(150, 150, 255);
        private final Color SELECTION_DARK = new Color(80, 80, 255);
        private ThreadData threadData;
        private boolean dragging = false;
        private float factor;
        private int focusedThreadDataIndex;
        private int mouseDraggedX = -1;
        private int mousePressedX = -1;
        private int width;
        private long dataEnd;
        private long dataStart;
        private long viewEnd;
        private long viewStart;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public SingleThreadState() {
            addMouseListener(this);
            addMouseMotionListener(this);
            setBackground(TIMELINE_ALIVE_BACKGROUND);
            threadData = new ThreadData(null, null);
            dataStart = 0;
            dataEnd = 0;
            focusedThreadDataIndex = -1;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public int getFocusedThreadDataIndex(Point point) {
            if (!isFocusedX(point.x)) {
                return -1;
            }

            if (!isFocusedY(point.y)) {
                return -1;
            }

            int focused = getFocusedThreadDataIndex(point.x);

            if (focused == threadData.size()) {
                return -1;
            }

            return focused;
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, getFont().getSize() + 20);
        }

        public void mouseClicked(MouseEvent e) {
            int focusedIndex = getFocusedThreadDataIndex(e.getPoint());

            if (focusedIndex == -1) {
                detailsArea.setCaretPosition(detailsArea.getCaretPosition());

                return;
            }

            selectDescriptionAreaLines(focusedIndex, focusedIndex);
        }

        public void mouseDragged(MouseEvent e) {
            if (mousePressedX != -1) {
                dragging = true;
                mouseDraggedX = e.getX();

                if (mousePressedX == mouseDraggedX) {
                    return;
                }

                int startX = mousePressedX;
                int endX = mouseDraggedX;

                if (startX > endX) {
                    int tmp = endX;
                    endX = startX;
                    startX = tmp;
                }

                int startIndex = getFocusedThreadDataIndex(startX);
                int endIndex = getFocusedThreadDataIndex(endX);

                if ((startIndex == -1) && (endIndex == -1)) {
                    detailsArea.setCaretPosition(detailsArea.getCaretPosition());
                    repaint();

                    return;
                }

                if ((startIndex >= threadData.size()) && (endIndex >= threadData.size())) {
                    detailsArea.setCaretPosition(detailsArea.getCaretPosition());
                    repaint();

                    return;
                }

                int offsetStart = (threadData.getFirstState() == CommonConstants.THREAD_STATUS_ZOMBIE) ? 1 : 0;
                int offsetEnd = (threadData.getLastState() == CommonConstants.THREAD_STATUS_ZOMBIE) ? 1 : 0;

                startIndex = Math.max(startIndex, offsetStart);
                endIndex = Math.min(endIndex, threadData.size() - 1 - offsetEnd);

                selectDescriptionAreaLines(startIndex, endIndex);

                repaint();
            } else {
                detailsArea.setCaretPosition(detailsArea.getCaretPosition());
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            focusedThreadDataIndex = -1;
            repaint();
        }

        public void mouseMoved(MouseEvent e) {
            int currentlyFocused = getFocusedThreadDataIndex(e.getPoint());

            if (currentlyFocused != focusedThreadDataIndex) {
                focusedThreadDataIndex = currentlyFocused;
                repaint();
            }
        }

        public void mousePressed(MouseEvent e) {
            if (isFocusedY(e.getY())) {
                mousePressedX = e.getX();
            } else {
                mousePressedX = -1;
            }
        }

        public void mouseReleased(MouseEvent e) {
            dragging = false;
            focusedThreadDataIndex = getFocusedThreadDataIndex(e.getPoint());
            repaint();
        }

        public void paint(Graphics g) {
            super.paint(g);
            viewStart = dataStart;
            viewEnd = dataEnd;
            width = getWidth() - (2 * DISPLAY_MARGIN);
            factor = (float) width / (float) (viewEnd - viewStart);

            if ((viewEnd - viewStart) > 0) {
                paintTimeMarks(g);
                paintThreadData(g);
            }
        }

        private int getFirstVisibleDataUnit() {
            if (threadData.size() > 0) {
                return 0;
            } else {
                return -1;
            }
        }

        private int getFocusedThreadDataIndex(int xpos) {
            if (threadData == null) {
                return -1;
            }

            if ((viewEnd - viewStart) <= 0) {
                return -1;
            }

            int index = getFirstVisibleDataUnit();

            if (index != -1) {
                int x;
                int xx;

                while ((index < threadData.size()) && (threadData.getTimeStampAt(index) <= viewEnd)) {
                    if (threadData.getStateAt(index) != CommonConstants.THREAD_STATUS_ZOMBIE) {
                        x = Math.max((int) ((float) (threadData.getTimeStampAt(index) - viewStart) * factor), 0) + DISPLAY_MARGIN;

                        if (xpos < x) {
                            return -1;
                        }

                        if (index < (threadData.size() - 1)) {
                            xx = Math.min((int) ((float) (threadData.getTimeStampAt(index + 1) - viewStart) * factor), width)
                                 + DISPLAY_MARGIN;
                        } else {
                            xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1) + DISPLAY_MARGIN;
                        }

                        if ((xpos >= x) && (xpos <= xx)) {
                            return index;
                        }
                    }

                    index++;
                }
            }

            return index;
        }

        private boolean isFocusedX(int x) {
            return ((x > DISPLAY_MARGIN) && (x < (getWidth() - DISPLAY_MARGIN)));
        }

        private boolean isFocusedY(int y) {
            return ((y > (7 + getFont().getSize())) && (y <= getHeight()));
        }

        private void paintThreadData(Graphics g) {
            if (threadData != null) {
                int index = getFirstVisibleDataUnit();

                if (index != -1) {
                    if ((viewEnd - viewStart) > 0) {
                        while ((index < threadData.size()) && (threadData.getTimeStampAt(index) <= viewEnd)) {
                            // Thread alive
                            if (threadData.getStateAt(index) != CommonConstants.THREAD_STATUS_ZOMBIE) {
                                paintThreadState(g, index, threadData.getThreadStateColorAt(index));
                            }

                            index++;
                        }
                    }
                }
            }
        }

        private void paintThreadState(Graphics g, int index, Color threadStateColor) {
            int x; // Begin of rectangle
            int xx; // End of rectangle

            int graphicsFontSize = g.getFont().getSize();

            x = Math.max((int) ((float) (threadData.getTimeStampAt(index) - viewStart) * factor), 0) + DISPLAY_MARGIN;

            if (index < (threadData.size() - 1)) {
                xx = Math.min((int) ((float) (threadData.getTimeStampAt(index + 1) - viewStart) * factor), width)
                     + DISPLAY_MARGIN;
            } else {
                xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1) + DISPLAY_MARGIN;
            }

            g.setColor(threadStateColor);
            g.fillRect(x, 11 + graphicsFontSize, xx - x, getHeight() - graphicsFontSize - 14);

            if (dragging) {
                g.setColor(SELECTION_BRIGHT);
                g.drawLine(mousePressedX, (10 + graphicsFontSize) - 2, mouseDraggedX, (10 + graphicsFontSize) - 2);
                g.drawLine(mousePressedX, getHeight() - 1, mouseDraggedX, getHeight() - 1);
                g.setColor(SELECTION_DARK);
                g.drawLine(mousePressedX, (11 + graphicsFontSize) - 2, mouseDraggedX, (11 + graphicsFontSize) - 2);
                g.drawLine(mousePressedX, getHeight() - 2, mouseDraggedX, getHeight() - 2);
            } else if (index == focusedThreadDataIndex) {
                g.setColor(SELECTION_BRIGHT);
                g.drawLine(x, (10 + graphicsFontSize) - 2, xx - 1, (10 + graphicsFontSize) - 2);
                g.drawLine(x, getHeight() - 1, xx - 1, getHeight() - 1);
                g.setColor(SELECTION_DARK);
                g.drawLine(x, (11 + graphicsFontSize) - 2, xx - 1, (11 + graphicsFontSize) - 2);
                g.drawLine(x, getHeight() - 2, xx - 1, getHeight() - 2);
            }
        }

        private void paintTimeMarkString(Graphics g, int currentMark, int optimalUnits, int x, int y) {
            int markStringMillisMargin = 0; // space between mark's string without milliseconds and mark's milliseconds string
            int markStringMillisReduce = 2; // markStringNoMillis.height - markStringMillisReduce = markStringMillis.height

            Font markStringNoMillisFont = g.getFont();
            Font markStringMillisFont = markStringNoMillisFont.deriveFont((float) (markStringNoMillisFont.getSize() - 2));

            String markStringNoMillis = TimeLineUtils.getTimeMarkNoMillisString(currentMark, optimalUnits);
            int wMarkStringNoMillis = g.getFontMetrics().stringWidth(markStringNoMillis); // width of the mark's string without milliseconds
            String markStringMillis = TimeLineUtils.getTimeMarkMillisString(currentMark, optimalUnits);

            if (!markStringMillis.equals("")) {
                markStringMillis = "." + markStringMillis; // NOI18N
            }

            int xMarkStringNoMillis = x - (wMarkStringNoMillis / 2) + 1; // x-position of the mark's string without milliseconds
            int xMarkStringMillis = xMarkStringNoMillis + wMarkStringNoMillis + markStringMillisMargin; // x-position of the mark's milliseconds string

            g.setColor(TimeLineUtils.BASE_TIMELINE_COLOR);
            g.drawString(markStringNoMillis, xMarkStringNoMillis, y);

            g.setFont(markStringMillisFont);
            g.drawString(markStringMillis, xMarkStringMillis, y - markStringMillisReduce + 1);
            g.setFont(markStringNoMillisFont);
        }

        private void paintTimeMarks(Graphics g) {
            g.setFont(g.getFont().deriveFont(Font.BOLD));

            int firstValue = (int) (viewStart - dataStart);
            int lastValue = (int) (viewEnd - dataStart);
            int optimalUnits = TimeLineUtils.getOptimalUnits(factor);

            int firstMark = Math.max((int) (Math.ceil((double) firstValue / optimalUnits) * optimalUnits), 0);
            int currentMark = firstMark - optimalUnits;

            int componentFontSize = getFont().getSize();

            while (currentMark <= (lastValue + optimalUnits)) {
                if (currentMark >= 0) {
                    float currentMarkRel = currentMark - firstValue;
                    int markPosition = (int) (currentMarkRel * factor) + DISPLAY_MARGIN;
                    paintTimeTicks(g, (int) (currentMarkRel * factor) + DISPLAY_MARGIN,
                                   (int) ((currentMarkRel + optimalUnits) * factor) + DISPLAY_MARGIN,
                                   TimeLineUtils.getTicksCount(optimalUnits));
                    g.setColor(TimeLineUtils.BASE_TIMELINE_COLOR);
                    g.drawLine(markPosition, 0, markPosition, 4);
                    paintTimeMarkString(g, currentMark, optimalUnits, markPosition, 5 + componentFontSize);
                    g.setColor(TimeLineUtils.MAIN_TIMELINE_COLOR);
                    g.drawLine(markPosition, 8 + componentFontSize, markPosition, getHeight() - 1);
                }

                currentMark += optimalUnits;
            }

            Font origFont = g.getFont();
            Font plainFont = origFont.deriveFont(Font.PLAIN);
            String sLegend = TimeLineUtils.getUnitsLegend(lastValue, optimalUnits);
            int wLegend = g.getFontMetrics(plainFont).stringWidth(sLegend);

            if ((wLegend + 7) <= width) {
                g.setFont(plainFont);
                g.setColor(getBackground());
                //g.fillRect(width - wLegend - 3 + 2 * DISPLAY_MARGIN, 6, wLegend + 1, 3 + plainFont.getSize());
                g.fillRect(width - wLegend - 6 + (2 * DISPLAY_MARGIN), 5, wLegend + 7, 4 + plainFont.getSize());
                g.setColor(Color.BLACK);
                g.drawString(sLegend, width - wLegend - 2 + (2 * DISPLAY_MARGIN), 5 + plainFont.getSize());
                g.setFont(origFont);
            }
        }

        private void paintTimeTicks(Graphics g, int startPos, int endPos, int count) {
            float timeTicksFactor = (float) (endPos - startPos) / (float) count;

            for (int i = 1; i < count; i++) {
                int x = startPos + (int) (i * timeTicksFactor);
                g.setColor(TimeLineUtils.BASE_TIMELINE_COLOR);
                g.drawLine(x, 0, x, 2);
                g.setColor(TimeLineUtils.TICK_TIMELINE_COLOR);
                g.drawLine(x, 3, x, getHeight() - 1);
            }
        }

        private void selectDescriptionAreaLines(int firstLine, int lastLine) {
            if ((firstLine < 0) || (lastLine < 0)) {
                return;
            }

            if ((firstLine >= threadData.size()) || (lastLine >= threadData.size())) {
                return;
            }

            int offsetStart = (threadData.getFirstState() == CommonConstants.THREAD_STATUS_ZOMBIE) ? 0 : 1;

            if (jTabbedPane1.getSelectedIndex() != 1) {
                jTabbedPane1.setSelectedIndex(1);
            }

            try {
                detailsArea.scrollRectToVisible(detailsArea.modelToView(detailsArea.getLineStartOffset(firstLine + offsetStart)));
                internalAdjustmentEvent = true;
                detailsArea.setCaretPosition(detailsArea.getLineStartOffset(firstLine + offsetStart));
                detailsArea.moveCaretPosition(detailsArea.getLineEndOffset(lastLine + offsetStart));
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            detailsArea.requestFocus();
                        }
                    });
            } catch (Exception ex) {
            }

            ;
        }

        private void updateData(ThreadData threadData, long dataStart, long dataEnd) {
            if (threadData != null) {
                this.threadData = threadData;
            } else {
                this.threadData = new ThreadData(null, null);
            }

            this.dataStart = dataStart;

            // Update dataEnd - for finished threads the timeline stops moving
            if ((this.threadData.size() > 0) && (this.threadData.getLastState() != CommonConstants.THREAD_STATUS_ZOMBIE)) {
                this.dataEnd = dataEnd;
            } else {
                this.dataEnd = this.threadData.getLastTimeStamp();
            }

            repaint();
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.threads.Bundle"); // NOI18N
    private static final String THREAD_ALIVE_STRING = messages.getString("ThreadDetailsComponent_ThreadAliveString"); // NOI18N
    private static final String THREAD_FINISHED_STRING = messages.getString("ThreadDetailsComponent_ThreadFinishedString"); // NOI18N
    private static final String NO_DATA_COLLECTED_STRING = messages.getString("ThreadDetailsComponent_NoDataCollectedString"); // NOI18N
    private static final String THREAD_STATE_UNKNOWN_STRING = messages.getString("ThreadDetailsComponent_ThreadStateUnknownString"); // NOI18N
    private static final String THREAD_STARTED_STRING = messages.getString("ThreadDetailsComponent_ThreadStartedString"); // NOI18N
    private static final String HIDE_BUTTON_NAME = messages.getString("ThreadDetailsComponent_HideButtonName"); // NOI18N
    private static final String TOTAL_LABEL_STRING = messages.getString("ThreadDetailsComponent_TotalLabelString"); // NOI18N
    private static final String GENERAL_TAB_NAME = messages.getString("ThreadDetailsComponent_GeneralTabName"); // NOI18N
    private static final String DETAILS_TAB_NAME = messages.getString("ThreadDetailsComponent_DetailsTabName"); // NOI18N
    private static final String TIMELINE_ACCESS_NAME = messages.getString("ThreadDetailsComponent_TimeLineAccessName"); // NOI18N
    private static final String THREAD_NAME_LABEL_ACCESS_NAME = messages.getString("ThreadDetailsComponent_ThreadNameLabelAccessName"); // NOI18N
    private static final String THREAD_STATE_LABEL_ACCESS_NAME = messages.getString("ThreadDetailsComponent_ThreadStateLabelAccessName"); // NOI18N
    private static final String HIDE_BUTTON_ACCESS_DESCR = messages.getString("ThreadDetailsComponent_HideButtonAccessDescr"); // NOI18N
    private static final String PIECHART_ACCESS_NAME = messages.getString("ThreadDetailsComponent_PieChartAccessName"); // NOI18N
    private static final String TAB_ACCESS_NAME = messages.getString("ThreadDetailsComponent_TabAccessName"); // NOI18N
                                                                                                              // -----
    private static final int THREAD_ICON_SIZE = 9;
    private static ThreadStateIcon runningIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_RUNNING, THREAD_ICON_SIZE,
                                                                     THREAD_ICON_SIZE);
    private static ThreadStateIcon sleepingIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_SLEEPING, THREAD_ICON_SIZE,
                                                                      THREAD_ICON_SIZE);
    private static ThreadStateIcon monitorIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_MONITOR, THREAD_ICON_SIZE,
                                                                     THREAD_ICON_SIZE);
    private static ThreadStateIcon waitIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_WAIT, THREAD_ICON_SIZE,
                                                                  THREAD_ICON_SIZE);
    private static ThreadStateIcon unknownIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_UNKNOWN, THREAD_ICON_SIZE,
                                                                     THREAD_ICON_SIZE);
    private static ThreadStateIcon zombieIcon = new ThreadStateIcon(CommonConstants.THREAD_STATUS_ZOMBIE, THREAD_ICON_SIZE,
                                                                    THREAD_ICON_SIZE);
    private static ThreadStateIcon noneIcon = new ThreadStateIcon(ThreadStateIcon.ICON_NONE, THREAD_ICON_SIZE, THREAD_ICON_SIZE);
    private static final Color TIMELINE_ALIVE_BACKGROUND = Color.WHITE;
    private static final Color TIMELINE_FINISHED_BACKGROUND = new Color(240, 240, 240);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    long monitorTime;
    long runningTime;
    long sleepingTime;
    long unknownTime;
    long waitTime;
    private DynamicPieChartModel pieChartModel;
    private JButton hideButton;
    private JLabel monitorTitleLabel;
    private JLabel monitorValueLabel;
    private JLabel monitorValueRelLabel;
    private JLabel runningTitleLabel;
    private JLabel runningValueLabel;
    private JLabel runningValueRelLabel;
    private JLabel sleepingTitleLabel;
    private JLabel sleepingValueLabel;
    private JLabel sleepingValueRelLabel;
    private JLabel threadClassNameLabel;
    private JLabel threadNameLabel;
    private JLabel threadStateLabel;
    private JLabel totalTitleLabel;
    private JLabel totalValueLabel;
    private JLabel waitTitleLabel;
    private JLabel waitValueLabel;
    private JLabel waitValueRelLabel;

    // ---------------------------------------------------------------------------------------
    // Components declaration & initialization

    // Variables declaration
    private JPanel descriptionPanel;
    private JPanel jPanel10;
    private JPanel jPanel11;
    private JPanel jPanel12;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel9;
    private JPanel tabsPanel;
    private JPanel timeLinePanel;
    private JPanel titlePanel;
    private JScrollPane jScrollPane1;
    private JSeparator jSeparator1;
    private JSeparator jSeparator2;
    private JSeparator jSeparator3;
    private JSeparator jSeparator4;
    private JTabbedPane jTabbedPane1;
    private JTextArea detailsArea;
    private JTextArea threadDescriptionArea;
    private PieChart pieChart;
    private SingleThreadState timeLine;
    private ThreadsDetailsPanel viewManager;
    private boolean internalAdjustmentEvent; // internal flag indicating that AdjustmentEvent was caused by the Append command while "tracking end" of detailsArea
    private boolean supportsSleepingState; // internal flag indicating that threads monitoring engine correctly reports the "sleeping" state
    private byte lastThreadState; // last thread state
    private int lastStatesCount; // number of collected thread states when the last dataUpdate() was called
    private int lastThreadIndex; // index of thread which data were displayed when the last dataUpdate() was called
    private int threadIndex;
    private long lastThreadDataEnd; // timestamp of last data updateState

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ThreadDetailsComponent(ThreadsDetailsPanel viewManager, boolean supportsSleepingState) {
        this(viewManager, -1, supportsSleepingState);
    }

    private ThreadDetailsComponent(ThreadsDetailsPanel viewManager, int index, boolean supportsSleepingState) {
        this.viewManager = viewManager;
        this.supportsSleepingState = supportsSleepingState;

        initPieChartComponents();
        initComponents();
        hookDetailsAreaScrollBar();

        resetData();
        setIndex(index);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setIndex(int index) {
        threadIndex = index;
        updateThreadState();
    }

    public int getIndex() {
        return threadIndex;
    }

    public void dataReset() {
        resetData();
        setIndex(-1);
    }

    private String getPercentValue(float value, float basevalue) {
        int basis = (int) (value / basevalue * 1000f);
        int percent = basis / 10;
        int permille = basis % 10;

        return "" + percent + "." + permille; // NOI18N
    }

    private String getThreadDetail(long timestamp, String description) {
        long relTimeStamp = timestamp - viewManager.getDataStartTime();

        return (" " + TimeLineUtils.getMillisValue(relTimeStamp) + ": " + description + "\n"); // NOI18N
    }

    private long getThreadStateDuration(ThreadData threadData, int index) {
        long startTime = threadData.getTimeStampAt(index);
        long endTime = viewManager.getDataEndTime();

        if (index < (threadData.size() - 1)) {
            endTime = threadData.getTimeStampAt(index + 1);
        }

        return endTime - startTime;
    }

    // --- Private implementation -----
    private void hookDetailsAreaScrollBar() {
        jScrollPane1.getVerticalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener() {
                public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e) {
                    JScrollBar scrollbar = jScrollPane1.getVerticalScrollBar();
                    int value = e.getValue();
                    int increment = scrollbar.getUnitIncrement(-1);

                    if (!internalAdjustmentEvent) { // real AdjustmentEvent from the user

                        if (detailsArea.getCaretPosition() == detailsArea.getText().length()) { // currently "tracking end"

                            if ((scrollbar.getValue() + scrollbar.getVisibleAmount()) < scrollbar.getMaximum()) { // user scrolls up

                                try {
                                    detailsArea.setCaretPosition(detailsArea.getLineStartOffset(value / increment)); // set Caret somewhere in the visible area && not at the end
                                } catch (Exception ex) {
                                }

                                ;
                            }
                        } else {
                            if ((scrollbar.getValue() + scrollbar.getVisibleAmount()) >= scrollbar.getMaximum()) { // user scrolled to the end
                                detailsArea.setCaretPosition(detailsArea.getText().length()); // set Carret at the end => "tracking end"
                            }
                        }
                    }

                    internalAdjustmentEvent = false; // reset internal AdjustmentEvent flag
                }
            });
    }

    private void initComponents() {
        timeLine = new SingleThreadState();
        timeLine.getAccessibleContext().setAccessibleName(TIMELINE_ACCESS_NAME);

        GridBagConstraints gridBagConstraints;

        titlePanel = new JPanel();
        threadNameLabel = new JLabel();
        threadClassNameLabel = new JLabel();
        threadStateLabel = new JLabel();
        jSeparator2 = new JSeparator();
        jPanel9 = new JPanel();
        hideButton = new JButton();
        descriptionPanel = new JPanel();
        threadDescriptionArea = new JTextArea();
        jSeparator1 = new JSeparator();
        tabsPanel = new JPanel();
        jTabbedPane1 = new JTabbedPane();
        jPanel6 = new JPanel();
        jPanel11 = new JPanel();
        jPanel10 = new JPanel();
        runningTitleLabel = new JLabel();
        sleepingTitleLabel = new JLabel();
        waitTitleLabel = new JLabel();
        monitorTitleLabel = new JLabel();
        runningValueLabel = new JLabel();
        runningValueRelLabel = new JLabel();
        sleepingValueLabel = new JLabel();
        sleepingValueRelLabel = new JLabel();
        waitValueLabel = new JLabel();
        waitValueRelLabel = new JLabel();
        monitorValueLabel = new JLabel();
        monitorValueRelLabel = new JLabel();
        jSeparator4 = new JSeparator();
        totalTitleLabel = new JLabel();
        totalValueLabel = new JLabel();
        jPanel12 = new JPanel();
        jPanel7 = new JPanel();
        jScrollPane1 = new JScrollPane();
        detailsArea = new JTextArea();

        jSeparator3 = new JSeparator();
        timeLinePanel = new JPanel();

        setLayout(new GridBagLayout());

        setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(new Insets(5, 5, 5, 5))));
        titlePanel.setLayout(new BorderLayout(5, 5));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

        threadNameLabel.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + 1));
        threadNameLabel.setVerticalAlignment(SwingConstants.CENTER);
        threadNameLabel.setIcon(noneIcon);
        threadNameLabel.setIconTextGap(7);
        threadNameLabel.setBorder(new EmptyBorder(new Insets(5, 7, 0, 0)));
        threadNameLabel.getAccessibleContext().setAccessibleName(THREAD_NAME_LABEL_ACCESS_NAME);

        threadClassNameLabel.setFont(getFont().deriveFont((float) (getFont().getSize() + 1)));
        threadClassNameLabel.setVerticalAlignment(SwingConstants.CENTER);
        threadClassNameLabel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 0)));

        namePanel.add(threadNameLabel);
        namePanel.add(threadClassNameLabel);

        titlePanel.add(namePanel, BorderLayout.WEST);

        threadStateLabel.setVerticalAlignment(SwingConstants.CENTER);
        threadStateLabel.setBorder(new EmptyBorder(new Insets(5, 0, 0, 5)));
        threadStateLabel.getAccessibleContext().setAccessibleName(THREAD_STATE_LABEL_ACCESS_NAME);
        titlePanel.add(threadStateLabel, BorderLayout.CENTER);

        titlePanel.add(jSeparator2, BorderLayout.SOUTH);

        jPanel9.setLayout(new BorderLayout(5, 0));

        hideButton.setText(HIDE_BUTTON_NAME);
        hideButton.setPreferredSize(new Dimension(hideButton.getPreferredSize().width, hideButton.getPreferredSize().height - 4));
        hideButton.setMaximumSize(new Dimension(hideButton.getMaximumSize().width, hideButton.getMaximumSize().height - 4));
        hideButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    viewManager.hideThreadDetails(threadIndex);
                }
                ;
            });
        hideButton.getAccessibleContext().setAccessibleDescription(HIDE_BUTTON_ACCESS_DESCR);
        jPanel9.add(hideButton, BorderLayout.WEST);

        titlePanel.add(jPanel9, BorderLayout.EAST);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(titlePanel, gridBagConstraints);

        descriptionPanel.setLayout(new BorderLayout());

        JPanel threadDescriptionIconPanel = new JPanel();
        threadDescriptionIconPanel.setLayout(new BorderLayout());
        threadDescriptionIconPanel.setBorder(new EmptyBorder(new Insets(0, 5, 5, 0)));

        JLabel threadDescriptionIcon = new JLabel(new ImageIcon(getClass()
                                                                    .getResource("/org/netbeans/lib/profiler/ui/resources/infoIcon.png"))); // NOI18N

        threadDescriptionIconPanel.add(threadDescriptionIcon, BorderLayout.NORTH);
        descriptionPanel.add(threadDescriptionIconPanel, BorderLayout.WEST);

        threadDescriptionArea.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));
        threadDescriptionArea.setBackground(getBackground());
        threadDescriptionArea.setOpaque(false);
        threadDescriptionArea.setWrapStyleWord(true);
        threadDescriptionArea.setLineWrap(true);
        threadDescriptionArea.setEnabled(false);
        threadDescriptionArea.setFont(UIManager.getFont("Label.font")); // NOI18N
        threadDescriptionArea.setDisabledTextColor(UIManager.getColor("Label.foreground")); // NOI18N
        threadDescriptionArea.setCaret(new DefaultCaret() {
                protected void adjustVisibility(Rectangle nloc) {
                    // do nothing to prevent violent scrolling
                }
            });
        descriptionPanel.add(threadDescriptionArea, BorderLayout.CENTER);

        descriptionPanel.add(jSeparator1, BorderLayout.SOUTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(descriptionPanel, gridBagConstraints);

        tabsPanel.setLayout(new BorderLayout());

        jTabbedPane1.setBorder(new EmptyBorder(new Insets(0, 5, 5, 5)));
        jTabbedPane1.getAccessibleContext().setAccessibleName(TAB_ACCESS_NAME);
        jPanel6.setLayout(new BorderLayout());

        jPanel11.setLayout(new BorderLayout());

        jPanel10.setLayout(new GridBagLayout());

        runningTitleLabel.setText(CommonConstants.THREAD_STATUS_RUNNING_STRING);
        runningTitleLabel.setIcon(runningIcon);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        jPanel10.add(runningTitleLabel, gridBagConstraints);

        if (supportsSleepingState) {
            sleepingTitleLabel.setText(CommonConstants.THREAD_STATUS_SLEEPING_STRING);
            sleepingTitleLabel.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
            sleepingTitleLabel.setIcon(sleepingIcon);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            jPanel10.add(sleepingTitleLabel, gridBagConstraints);
        }

        waitTitleLabel.setText(CommonConstants.THREAD_STATUS_WAIT_STRING);
        waitTitleLabel.setIcon(waitIcon);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        jPanel10.add(waitTitleLabel, gridBagConstraints);

        monitorTitleLabel.setText(CommonConstants.THREAD_STATUS_MONITOR_STRING);
        monitorTitleLabel.setIcon(monitorIcon);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel10.add(monitorTitleLabel, gridBagConstraints);

        runningValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        jPanel10.add(runningValueLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 0, 10);
        jPanel10.add(runningValueRelLabel, gridBagConstraints);

        if (supportsSleepingState) {
            sleepingValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(5, 5, 0, 5);
            jPanel10.add(sleepingValueLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(5, 0, 0, 10);
            jPanel10.add(sleepingValueRelLabel, gridBagConstraints);
        }

        waitValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        jPanel10.add(waitValueLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 0, 10);
        jPanel10.add(waitValueRelLabel, gridBagConstraints);

        monitorValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel10.add(monitorValueLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 0, 5, 10);
        jPanel10.add(monitorValueRelLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        jPanel10.add(jSeparator4, gridBagConstraints);

        totalTitleLabel.setText(TOTAL_LABEL_STRING);
        totalTitleLabel.setIcon(noneIcon);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel10.add(totalTitleLabel, gridBagConstraints);

        totalValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jPanel10.add(totalValueLabel, gridBagConstraints);

        jPanel11.add(jPanel10, BorderLayout.WEST);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(new Insets(10, 0, 0, 0)));
        northPanel.add(jPanel11, BorderLayout.NORTH);

        jPanel6.add(northPanel, BorderLayout.CENTER);

        jPanel12.setLayout(new BorderLayout());

        pieChart.setPreferredSize(new Dimension(230, 100));
        pieChart.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        jPanel12.add(pieChart, BorderLayout.NORTH);
        jPanel12.getAccessibleContext().setAccessibleName(PIECHART_ACCESS_NAME);
        pieChart.setAccessibleContext(jPanel12.getAccessibleContext());

        jPanel6.add(jPanel12, BorderLayout.WEST);

        jTabbedPane1.addTab(GENERAL_TAB_NAME, jPanel6);

        jPanel7.setLayout(new BorderLayout());

        detailsArea.setEditable(false);
        detailsArea.setRows(1);
        jScrollPane1.setViewportView(detailsArea);

        jPanel7.add(jScrollPane1, BorderLayout.CENTER);

        jTabbedPane1.addTab(DETAILS_TAB_NAME, jPanel7);

        tabsPanel.add(jTabbedPane1, BorderLayout.CENTER);

        tabsPanel.add(jSeparator3, BorderLayout.SOUTH);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(tabsPanel, gridBagConstraints);

        timeLinePanel.setLayout(new BorderLayout());

        JPanel timeLineContainer = new JPanel();
        timeLineContainer.setLayout(new BorderLayout());
        timeLineContainer.setBorder(new BevelBorder(BevelBorder.LOWERED));
        timeLineContainer.add(timeLine, BorderLayout.CENTER);

        timeLinePanel.add(timeLineContainer, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        add(timeLinePanel, gridBagConstraints);
    }

    private void initPieChartComponents() {
        pieChart = new PieChart();
        pieChart.setFocusable(false);
        pieChartModel = new DynamicPieChartModel();

        if (supportsSleepingState) {
            pieChartModel.setupModel(new String[] {
                                         CommonConstants.THREAD_STATUS_RUNNING_STRING,
                                         CommonConstants.THREAD_STATUS_SLEEPING_STRING, CommonConstants.THREAD_STATUS_WAIT_STRING,
                                         CommonConstants.THREAD_STATUS_MONITOR_STRING
                                     },
                                     new Color[] {
                                         CommonConstants.THREAD_STATUS_RUNNING_COLOR, CommonConstants.THREAD_STATUS_SLEEPING_COLOR,
                                         CommonConstants.THREAD_STATUS_WAIT_COLOR, CommonConstants.THREAD_STATUS_MONITOR_COLOR
                                     });
        } else {
            pieChartModel.setupModel(new String[] {
                                         CommonConstants.THREAD_STATUS_RUNNING_STRING, CommonConstants.THREAD_STATUS_WAIT_STRING,
                                         CommonConstants.THREAD_STATUS_MONITOR_STRING
                                     },
                                     new Color[] {
                                         CommonConstants.THREAD_STATUS_RUNNING_COLOR, CommonConstants.THREAD_STATUS_WAIT_COLOR,
                                         CommonConstants.THREAD_STATUS_MONITOR_COLOR
                                     });
        }

        pieChart.setModel(pieChartModel);
    }

    private void resetData() {
        lastThreadIndex = -1;
        lastStatesCount = 0;
        lastThreadDataEnd = 0;
        lastThreadState = -10;
        runningTime = 0;
        sleepingTime = 0;
        waitTime = 0;
        monitorTime = 0;
        unknownTime = 0;
        detailsArea.setText(""); // NOI18N
        jTabbedPane1.setSelectedIndex(0);
    }

    private void updateThreadState() {
        if (threadIndex == -1) {
            return;
        }

        ThreadData threadData = viewManager.getThreadData(threadIndex);

        // thread name & classname
        threadNameLabel.setText(viewManager.getThreadName(threadIndex));

        if (threadIndex != lastThreadIndex) {
            threadClassNameLabel.setText("[" + viewManager.getThreadClassName(threadIndex) + "]"); // NOI18N
        }

        // thread state
        if ((threadData == null) || (threadData.size() == 0)) {
            threadNameLabel.setIcon(zombieIcon);
            timeLine.setBackground(TIMELINE_FINISHED_BACKGROUND);
            threadStateLabel.setText("(" + THREAD_FINISHED_STRING + ")"); // NOI18N
        } else {
            if (lastThreadState != threadData.getLastState()) {
                switch (threadData.getLastState()) {
                    case CommonConstants.THREAD_STATUS_UNKNOWN:
                        threadNameLabel.setIcon(unknownIcon);
                        timeLine.setBackground(TIMELINE_ALIVE_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_STATE_UNKNOWN_STRING + ")"); // NOI18N

                        break;
                    case CommonConstants.THREAD_STATUS_ZOMBIE:
                        threadNameLabel.setIcon(zombieIcon);
                        timeLine.setBackground(TIMELINE_FINISHED_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_FINISHED_STRING + ")"); // NOI18N

                        break;
                    case CommonConstants.THREAD_STATUS_SLEEPING:

                        if (supportsSleepingState) {
                            threadNameLabel.setIcon(sleepingIcon);
                        } else {
                            threadNameLabel.setIcon(runningIcon);
                        }

                        timeLine.setBackground(TIMELINE_ALIVE_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_ALIVE_STRING + ")"); // NOI18N

                        break;
                    case CommonConstants.THREAD_STATUS_RUNNING:
                        threadNameLabel.setIcon(runningIcon);
                        timeLine.setBackground(TIMELINE_ALIVE_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_ALIVE_STRING + ")"); // NOI18N

                        break;
                    case CommonConstants.THREAD_STATUS_WAIT:
                        threadNameLabel.setIcon(waitIcon);
                        timeLine.setBackground(TIMELINE_ALIVE_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_ALIVE_STRING + ")"); // NOI18N

                        break;
                    case CommonConstants.THREAD_STATUS_MONITOR:
                        threadNameLabel.setIcon(monitorIcon);
                        timeLine.setBackground(TIMELINE_ALIVE_BACKGROUND);
                        threadStateLabel.setText("(" + THREAD_ALIVE_STRING + ")"); // NOI18N

                        break;
                }
            }
        }

        // thread description
        threadDescriptionArea.setText(viewManager.getThreadDescription(threadIndex));

        if ((threadData == null) || (threadData.size() == 0)) {
            if (threadIndex != lastThreadIndex) {
                resetData();

                runningValueLabel.setText("-"); // NOI18N
                runningValueRelLabel.setText("(-%)"); // NOI18N

                if (supportsSleepingState) {
                    sleepingValueLabel.setText("-"); // NOI18N
                    sleepingValueRelLabel.setText("(-%)"); // NOI18N
                }

                waitValueLabel.setText("-"); // NOI18N
                waitValueRelLabel.setText("(-%)"); // NOI18N
                monitorValueLabel.setText("-"); // NOI18N
                monitorValueRelLabel.setText("(-%)"); // NOI18N
                totalValueLabel.setText(NO_DATA_COLLECTED_STRING);

                if (supportsSleepingState) {
                    pieChartModel.setItemValues(new double[] { 0d, 0d, 0d, 0d });
                } else {
                    pieChartModel.setItemValues(new double[] { 0d, 0d, 0d });
                }
            }
        } else {
            // When the threadIndex was changed (component displays data of different thread) details textarea is cleared
            if (threadIndex != lastThreadIndex) {
                resetData();
            }

            StringBuffer detailsToAppend = new StringBuffer();

            for (int i = lastStatesCount; i < threadData.size(); i++) {
                long timeStamp = threadData.getTimeStampAt(i);
                byte state = threadData.getStateAt(i);

                if (i == 0) {
                    detailsToAppend.append(getThreadDetail(timeStamp, THREAD_STARTED_STRING));
                }

                switch (state) {
                    case CommonConstants.THREAD_STATUS_RUNNING:
                        detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_RUNNING_STRING));
                        runningTime += getThreadStateDuration(threadData, i);

                        break;
                    case CommonConstants.THREAD_STATUS_SLEEPING:

                        if (supportsSleepingState) {
                            detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_SLEEPING_STRING));
                            sleepingTime += getThreadStateDuration(threadData, i);
                        } else {
                            detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_RUNNING_STRING));
                            runningTime += getThreadStateDuration(threadData, i);
                        }

                        break;
                    case CommonConstants.THREAD_STATUS_WAIT:
                        detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_WAIT_STRING));
                        waitTime += getThreadStateDuration(threadData, i);

                        break;
                    case CommonConstants.THREAD_STATUS_MONITOR:
                        detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_MONITOR_STRING));
                        monitorTime += getThreadStateDuration(threadData, i);

                        break;
                    case CommonConstants.THREAD_STATUS_ZOMBIE:

                        if (i != 0) {
                            detailsToAppend.append(getThreadDetail(timeStamp, CommonConstants.THREAD_STATUS_ZOMBIE_STRING));
                        }

                        break;
                }
            }

            // If currentThreadState == lastThreadState then the end time of lastThreadState must be incremented
            if (lastStatesCount == threadData.size()) {
                if (threadData.size() != 0) {
                    long timeDiff = viewManager.getDataEndTime() - lastThreadDataEnd;
                    byte state = threadData.getLastState();

                    switch (state) {
                        case CommonConstants.THREAD_STATUS_RUNNING:
                            runningTime += timeDiff;

                            break;
                        case CommonConstants.THREAD_STATUS_SLEEPING:

                            if (supportsSleepingState) {
                                sleepingTime += timeDiff;
                            } else {
                                runningTime += timeDiff;
                            }

                            break;
                        case CommonConstants.THREAD_STATUS_WAIT:
                            waitTime += timeDiff;

                            break;
                        case CommonConstants.THREAD_STATUS_MONITOR:
                            monitorTime += timeDiff;

                            break;
                    }
                }
            }

            //long totalTime = runningTime + sleepingTime + waitTime + monitorTime + unknownTime;
            long totalTime = 0;

            if (supportsSleepingState) {
                totalTime = runningTime + sleepingTime + waitTime + monitorTime;
            } else {
                totalTime = runningTime + waitTime + monitorTime;
            }

            if (supportsSleepingState) {
                pieChartModel.setItemValues(new double[] { runningTime, sleepingTime, waitTime, monitorTime });
            } else {
                pieChartModel.setItemValues(new double[] { runningTime, waitTime, monitorTime });
            }

            runningValueLabel.setText(TimeLineUtils.getMillisValue(runningTime));
            runningValueRelLabel.setText("(" + ((runningTime == 0) ? "0.0" : getPercentValue(runningTime, totalTime)) + "%)"); // NOI18N

            if (supportsSleepingState) {
                sleepingValueLabel.setText(TimeLineUtils.getMillisValue(sleepingTime));
                sleepingValueRelLabel.setText("(" + ((sleepingTime == 0) ? "0.0" : getPercentValue(sleepingTime, totalTime))
                                              + "%)"); // NOI18N
            }

            waitValueLabel.setText(TimeLineUtils.getMillisValue(waitTime));
            waitValueRelLabel.setText("(" + ((waitTime == 0) ? "0.0" : getPercentValue(waitTime, totalTime)) + "%)"); // NOI18N
            monitorValueLabel.setText(TimeLineUtils.getMillisValue(monitorTime));
            monitorValueRelLabel.setText("(" + ((monitorTime == 0) ? "0.0" : getPercentValue(monitorTime, totalTime)) + "%)"); // NOI18N
            totalValueLabel.setText(TimeLineUtils.getMillisValue(totalTime));

            if (detailsToAppend.length() > 0) {
                if (detailsArea.getCaretPosition() == detailsArea.getText().length()) {
                    internalAdjustmentEvent = true; // if "tracking end" set internal AdjustmentEvent flag
                }

                detailsArea.append(detailsToAppend.toString());

                if (lastStatesCount == 0) {
                    detailsArea.setCaretPosition(1); // First text added, carret position can't be at the end ("tracking end" is not default)
                }
            }
        }

        lastThreadIndex = threadIndex;
        lastStatesCount = threadData.size();
        lastThreadDataEnd = viewManager.getDataEndTime();
        lastThreadState = threadData.getLastState();

        // Timeline
        timeLine.updateData(threadData, viewManager.getDataStartTime(), viewManager.getDataEndTime());
    }
}
