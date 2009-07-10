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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.Serializable;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadState;

/**
 * @author Jiri Sedlacek
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadStateCellRenderer extends JPanel implements TableCellRenderer, Serializable {
    private Color unselectedBackground;
    private Color unselectedForeground;
    private ThreadData threadData;
    private ThreadsPanel viewManager; // view manager for this cell
    private long dataEnd;
    private long dataStart;
    private long viewEnd;
    private long viewStart;

    /** Creates a new instance of ThreadStateCellRenderer */
    public ThreadStateCellRenderer(ThreadsPanel viewManager) {
        this.viewManager = viewManager;
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = UIUtils.getProfilerResultsBackground();
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     *
     * @param c set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();

        if (p != null) {
            p = p.getParent();
        }

        boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();

        return !colorMatch && super.isOpaque();
    }

    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        if (isSelected) {
            super.setForeground(table.isFocusOwner() ? table.getSelectionForeground() : UIUtils.getUnfocusedSelectionForeground());
            super.setBackground(table.isFocusOwner() ? table.getSelectionBackground() : UIUtils.getUnfocusedSelectionBackground());
        } else {
            if ((row & 0x1) == 0) { //even row
                super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                super.setBackground(UIUtils.getDarker((unselectedBackground != null) ? unselectedBackground : table.getBackground()));
            } else {
                super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
                super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
            }
        }

        if (value instanceof ThreadData) {
            threadData = (ThreadData) value;
        }

        viewStart = viewManager.getViewStart();
        viewEnd = viewManager.getViewEnd();
        dataStart = viewManager.getDataStart();
        dataEnd = viewManager.getDataEnd();

        return this;
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintTimeMarks(g);

        if (threadData != null) {
            int index = getFirstVisibleDataUnit();

            if (index != -1) {
                int width = getWidth();

                if ((viewEnd - viewStart) > 0) {
                    float factor = (float) width / (float) (viewEnd - viewStart);

                    while ((index < threadData.size()) && (threadData.getTimeStampAt(index) <= viewEnd)) {
                        // Thread alive
                        if (threadData.isAlive(index)) {
                            paintThreadState(g, index, threadData.getThreadStateAt(index), factor, width);
                        }

                        index++;
                    }
                }
            }
        }
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void repaint(Rectangle r) {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void revalidate() {
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    public void validate() {
    }

    /**
     * Overridden for performance reasons.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }

    private int getFirstVisibleDataUnit() {
        for (int i = 0; i < threadData.size(); i++) {
            long timestamp = threadData.getTimeStampAt(i);

            if ((timestamp <= viewEnd) && (i == (threadData.size() - 1))) {
                return i; // last data unit before viewEnd
            }

            if (timestamp <= viewStart) {
                if (threadData.getTimeStampAt(i + 1) > viewStart) {
                    return i; // data unit ends between viewStart and viewEnd
                }
            } else {
                if (timestamp <= viewEnd) {
                    return i; // data unit begins between viewStart and viewEnd
                }
            }
        }

        return -1;
    }

    private void paintThreadState(Graphics g, int index, Color threadStateColor, float factor, int width) {
        int x; // Begin of rectangle
        int xx; // End of rectangle

        x = Math.max((int) ((float) (threadData.getTimeStampAt(index) - viewStart) * factor), 0);

        if (index < (threadData.size() - 1)) {
            xx = Math.min((int) ((float) (threadData.getTimeStampAt(index + 1) - viewStart) * factor), width);
        } else {
            xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1);
        }

        g.setColor(threadStateColor);
        g.fillRect(x, 6, xx - x, getHeight() - 12);
    }

    private void paintThreadState(Graphics g, int index, ThreadState threadStateColor, float factor, int width) {
        int x; // Begin of rectangle
        int xx; // End of rectangle

        x = Math.max((int) ((float) (threadData.getTimeStampAt(index) - viewStart) * factor), 0);

        if (index < (threadData.size() - 1)) {
            xx = Math.min((int) ((float) (threadData.getTimeStampAt(index + 1) - viewStart) * factor), width);
        } else {
            xx = Math.min((int) ((dataEnd - viewStart) * factor), width + 1);
        }

        int size = threadStateColor.size();
        int delta = getHeight() - 12;

        int y = 0;
        int rest = 0;
        int oldRest = 0;

        for(int i = 0; i < size; i++) {
            int v = threadStateColor.getState(i);
            String name = threadStateColor.getStateName(i);
            Color c;
            if (name.equals(ThreadState.ShortThreadState.Running.name())) {
                c = ThreadData.THREAD_STATUS_RUNNING_COLOR;
            } else if(name.equals(ThreadState.ShortThreadState.Waiting.name())) {
                c = ThreadData.THREAD_STATUS_WAIT_COLOR;
            } else if(name.equals(ThreadState.ShortThreadState.Blocked.name())) {
                c = ThreadData.THREAD_STATUS_MONITOR_COLOR;
            } else if(name.equals(ThreadState.ShortThreadState.Sleeping.name())) {
                c = ThreadData.THREAD_STATUS_SLEEPING_COLOR;
            } else {
                c = ThreadData.THREAD_STATUS_UNKNOWN_COLOR;
            }
            oldRest = rest;
            rest = (v*delta+rest)%1000;
            int d = (v*delta+oldRest)/1000;
            y += d;
            if (d > 0) {
                g.setColor(c);
                g.fillRect(x, 6+delta-y, xx - x, d);
                //g.fillRect(x, 6, xx - x, getHeight() - 12);
            }
        }
    }

    private void paintTimeMarks(Graphics g) {
        if ((viewEnd - viewStart) > 0) {
            int firstValue = (int) (viewStart - dataStart);
            int lastValue = (int) (viewEnd - dataStart);
            float factor = (float) getWidth() / (float) (viewEnd - viewStart);
            int optimalUnits = TimeLineUtils.getOptimalUnits(factor);

            int firstMark = Math.max((int) (Math.ceil((double) firstValue / optimalUnits) * optimalUnits), 0);

            int currentMark = firstMark - optimalUnits;

            while (currentMark <= (lastValue + optimalUnits)) {
                if (currentMark >= 0) {
                    float currentMarkRel = currentMark - firstValue;
                    int markPosition = (int) (currentMarkRel * factor);
                    paintTimeTicks(g, (int) (currentMarkRel * factor), (int) ((currentMarkRel + optimalUnits) * factor),
                            TimeLineUtils.getTicksCount(optimalUnits));
                    g.setColor(TimeLineUtils.MAIN_TIMELINE_COLOR);
                    g.drawLine(markPosition, 0, markPosition, getHeight() - 1);
                }

                currentMark += optimalUnits;
            }
        }
    }

    private void paintTimeTicks(Graphics g, int startPos, int endPos, int count) {
        float factor = (float) (endPos - startPos) / (float) count;

        g.setColor(TimeLineUtils.TICK_TIMELINE_COLOR);

        for (int i = 1; i < count; i++) {
            int x = startPos + (int) (i * factor);
            g.drawLine(x, 0, x, getHeight() - 1);
        }
    }
}
