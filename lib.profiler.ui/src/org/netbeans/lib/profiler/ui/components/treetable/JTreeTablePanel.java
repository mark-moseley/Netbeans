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

package org.netbeans.lib.profiler.ui.components.treetable;

import org.netbeans.lib.profiler.ui.UIConstants;
import org.netbeans.lib.profiler.ui.components.JTreeTable;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.JTableHeader;


/**
 * A subclass of JPanel that provides additional fuctionality for displaying JTreeTable.
 * JTreeTablePanel provides JScrollPane for displaying JTreeTable and JScrollBar for JTree
 * column of JTreeTable if necessary.
 *
 * @author Jiri Sedlacek
 */
public class JTreeTablePanel extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    // Custom TreeTable Viewport
    private class CustomTreeTableViewport extends JViewport {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private JTableHeader tableHeader;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CustomTreeTableViewport(JTreeTable treeTable) {
            super();
            setView(treeTable);
            setBackground(treeTable.getBackground());
            this.tableHeader = treeTable.getTableHeader();
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void paint(Graphics g) {
            super.paint(g);

            if (UIConstants.SHOW_TABLE_VERTICAL_GRID) {
                paintVerticalLines(g);
            }
        }

        private int getEmptySpaceY() {
            if (getView() == null) {
                return 0;
            }

            return getView().getHeight();
        }

        private void paintVerticalLines(Graphics g) {
            int emptySpaceY = getEmptySpaceY();
            Rectangle cellRect;

            if (emptySpaceY > 0) {
                g.setColor(UIConstants.TABLE_VERTICAL_GRID_COLOR);

                for (int i = 0; i < tableHeader.getColumnModel().getColumnCount(); i++) {
                    cellRect = tableHeader.getHeaderRect(i);
                    g.drawLine((cellRect.x + cellRect.width) - 1, emptySpaceY, (cellRect.x + cellRect.width) - 1, getHeight() - 1);
                }
            }
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected CustomTreeTableViewport treeTableViewport;
    protected JPanel scrollBarPanel;
    protected JScrollBar scrollBar;
    protected JScrollPane treeTableScrollPane;
    protected JTreeTable treeTable;
    private boolean columnMarginChanged = false;
    private boolean internalScrollBarChange = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of JTreeTablePanel */
    public JTreeTablePanel(JTreeTable treeTable) {
        super(new BorderLayout());
        this.treeTable = treeTable;

        initComponents();
        hookHeaderColumnResize();
        hookScrollBarValueChange();
        hookTreeCollapsedExpanded();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setCorner(String key, java.awt.Component corner) {
        treeTableScrollPane.setCorner(key, corner);
    }

    public JScrollPane getScrollPane() {
        return treeTableScrollPane;
    }

    public void paint(java.awt.Graphics g) {
        super.paint(g);
        updateScrollBarMaximum();
        updateScrollBarWidth();
    }

    private void hookHeaderColumnResize() {
        treeTable.getTableHeader().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                public void columnAdded(TableColumnModelEvent e) {
                    treeTableViewport.repaint();
                }

                public void columnMoved(TableColumnModelEvent e) {
                    treeTableViewport.repaint();
                }

                public void columnRemoved(TableColumnModelEvent e) {
                    treeTableViewport.repaint();
                }

                public void columnMarginChanged(ChangeEvent e) {
                    internalScrollBarChange = true;
                    columnMarginChanged = true;
                    updateScrollBarMaximum();
                    treeTableViewport.repaint();
                    updateScrollBarWidth();
                    internalScrollBarChange = false;
                }

                public void columnSelectionChanged(ListSelectionEvent e) {
                } // Ignored
            });
    }

    private void hookScrollBarValueChange() {
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    if (!internalScrollBarChange) {
                        treeTable.setTreeCellOffsetX(e.getValue());
                    }
                }
            });
    }

    private void hookTreeCollapsedExpanded() {
        treeTable.getTree().addTreeExpansionListener(new TreeExpansionListener() {
                public void treeCollapsed(TreeExpansionEvent event) {
                    internalScrollBarChange = true;
                    updateScrollBarMaximum();
                    internalScrollBarChange = false;
                }
                ;
                public void treeExpanded(TreeExpansionEvent event) {
                    internalScrollBarChange = true;
                    updateScrollBarMaximum();
                    internalScrollBarChange = false;
                }
            });
    }

    private void initComponents() {
        setBorder(BorderFactory.createLoweredBevelBorder());

        treeTableScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        treeTableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        treeTableViewport = new CustomTreeTableViewport(treeTable);
        treeTableScrollPane.setViewport(treeTableViewport);
        treeTableScrollPane.addMouseWheelListener(treeTable);
        // Enable vertical scrollbar only if needed
        treeTableScrollPane.getVerticalScrollBar().getModel().addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (treeTableScrollPane.getVerticalScrollBar().getModel().getExtent() == treeTableScrollPane.getVerticalScrollBar()
                                                                                                                    .getModel()
                                                                                                                    .getMaximum()) {
                        treeTableScrollPane.getVerticalScrollBar().setEnabled(false);
                    } else {
                        treeTableScrollPane.getVerticalScrollBar().setEnabled(true);
                    }
                }
            });

        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        scrollBar.setUnitIncrement(10);
        scrollBarPanel = new JPanel(new BorderLayout());
        scrollBarPanel.add(scrollBar, BorderLayout.WEST);
        treeTable.setTreeCellOffsetX(0);
        scrollBarPanel.setVisible(false);

        add(treeTableScrollPane, BorderLayout.CENTER);
        add(scrollBarPanel, BorderLayout.SOUTH);
    }

    private void updateScrollBarMaximum() {
        int treeWidth = treeTable.getTree().getPreferredSize().width + 3; // +3 means extra right margin
        int columnWidth = treeTable.getColumnModel().getColumn(0).getWidth();

        if (columnMarginChanged && (treeTable.getTreeCellOffsetX() > 0)
                && ((treeWidth - treeTable.getTreeCellOffsetX()) < columnWidth)) {
            columnMarginChanged = false;

            if ((treeWidth - columnWidth) >= 0) {
                treeTable.setTreeCellOffsetX(treeWidth - columnWidth);
            }
        }

        int scrollBarMaximum = treeWidth - columnWidth;

        if (!isShowing()) {
            return;
        }

        if (scrollBarMaximum <= 0) {
            treeTable.setTreeCellOffsetX(0);
            scrollBarPanel.setVisible(false);
        } else {
            int value = treeTable.getTreeCellOffsetX();
            int extent = treeWidth;
            scrollBarPanel.setVisible(true);
            scrollBar.setValues(value, extent, 0, scrollBarMaximum + extent);
        }
    }

    private void updateScrollBarWidth() {
        scrollBar.setPreferredSize(new Dimension(treeTable.getTableHeader().getHeaderRect(0).width,
                                                 scrollBar.getPreferredSize().height));
        scrollBar.setBlockIncrement((int) (scrollBar.getModel().getExtent() * 0.95f));
        scrollBar.invalidate();
        JTreeTablePanel.this.revalidate();
    }
}
