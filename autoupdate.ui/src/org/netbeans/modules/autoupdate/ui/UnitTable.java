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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.UpdateManager;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class UnitTable extends JTable {
    private UnitCategoryTableModel model = null;
    private static final int DARKER_COLOR_COMPONENT = 10;
    private TableCellRenderer enableRenderer = null;
    private int rowHeight = -1;
    
    /** Creates a new instance of UpdateTable */
    public UnitTable (TableModel model) {
        super (model);
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UnitTable.class, "ACN_UnitTable")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UnitTab.class, "ACD_UnitTable")); // NOI18N        
        this.model = (UnitCategoryTableModel) model;
        setShowGrid (false);
        setColumnsSize ();
        //setFillsViewportHeight(true);        
        setIntercellSpacing (new Dimension (0, 0));
        revalidate ();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return model.isExpansionControlAtRow(row) ? new MoreRenderer() : super.getCellRenderer(row, column);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ////instead of setFillsViewportHeight(true);        
        getParent().setBackground(getBackground());
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        enableRenderer = null;
    }
    
    @Override
    public String getToolTipText (MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint ();
        int rowIndex = rowAtPoint (p);
        int colIndex = columnAtPoint (p);
        int realColumnIndex = convertColumnIndexToModel (colIndex);
        UnitCategoryTableModel model = (UnitCategoryTableModel)getModel ();
        tip = model.getToolTipText (rowIndex, realColumnIndex);
        return tip != null ? tip : super.getToolTipText (e);
    }
    
    void resetEnableRenderer () {
        if (enableRenderer != null) {
            setEnableRenderer (enableRenderer);
            TableCellRenderer defaultRenderer = getDefaultRenderer(String.class);
            
        }
    }
    
    void setEnableRenderer (TableCellRenderer renderer) {
        enableRenderer = renderer;
        TableColumnModel columnModel = getColumnModel();
        columnModel.getColumn(3).setCellRenderer(renderer);
    }
    
    void resortByDefault () {
        ((MyTableHeader) getTableHeader ()).setDefaultSorting ();
    }
    
    void setColumnsSize () {
        int columnCount = model.getColumnCount ();
        for (int i = 0; i < columnCount; i++) {
            TableColumn activeColumn = getColumnModel ().getColumn (i);
            activeColumn.setPreferredWidth (this.model.getPreferredWidth (getTableHeader (), i));
        }
    }
    
    @Override
    public Component prepareRenderer (TableCellRenderer renderer,
            int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer (renderer, rowIndex, vColIndex);
        Color bgColor = getBackground ();
        Color bgColorDarker = getDarkerColor(bgColor);
        
        Unit u = model.getUnitAtRow (rowIndex);
        if (u != null && !u.canBeMarked ()) {
            c.setForeground (Color.gray);
        } else {
            if (vColIndex == 1 && u != null && UpdateManager.TYPE.FEATURE.equals(u.updateUnit.getType())) {
                c.setFont(getFont().deriveFont(java.awt.Font.BOLD));
            } else {
                c.setFont(getFont());
            }
            if (isRowSelected(rowIndex)) {
                c.setForeground(getSelectionForeground());
            } else {
                c.setForeground(getForeground());
            }
        }
        if (!isCellSelected (rowIndex, vColIndex)) {
            if (rowIndex % 2 == 0 && !model.isExpansionControlAtRow(rowIndex)) {
                c.setBackground (bgColorDarker);
            } else {
                c.setBackground (bgColor);
            }
        } else if (model.isExpansionControlAtRow(rowIndex)) {
            c.setBackground (getBackground ());
            c.setForeground(getForeground());
            JComponent jc = (JComponent)c;
            jc.setBorder(BorderFactory.createEmptyBorder());
        }
        if (rowHeight < 0) {
            Font font =  c.getFont();
            FontMetrics fontMetrics = c.getFontMetrics(font);
            int def = new JTable().getRowHeight();
            rowHeight = Math.max(def, fontMetrics.getHeight());
            setRowHeight(rowHeight);
        }
        
        return c;
    }
    
    static Color getDarkerColor(Color color) {
        return new Color(
                Math.abs(color.getRed() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getGreen() - DARKER_COLOR_COMPONENT),
                Math.abs(color.getBlue() - DARKER_COLOR_COMPONENT));
        
    }
    
    @Override
    protected JTableHeader createDefaultTableHeader () {
        return new MyTableHeader ( columnModel );
    }
    
    private class MyTableHeader extends JTableHeader {
        private SortColumnHeaderRenderer sortingRenderer;
        
        public MyTableHeader ( TableColumnModel model ) {
            super ( model );
            addMouseListener ( new MouseAdapter () {
                @Override
                public void mouseClicked (MouseEvent e) {
                    if( e.getClickCount() != 1 || !UnitTable.this.isEnabled()) {
                        return;
                    }
                    int column = columnAtPoint ( e.getPoint () );
                    if( sortingRenderer != null) {
                        Unit u = null;
                        UnitCategoryTableModel model = null;
                        try {
                            model = (UnitCategoryTableModel)getModel ();
                            int row = getSelectedRow ();
                            if (row > -1) {
                                u = model.getUnitAtRow (row);
                            }
                            Object id = getColumnModel ().getColumn (column).getIdentifier ();
                            if (model.isSortAllowed (id)) {
                                sortingRenderer.setSorting (id);
                                repaint ();
                            }
                        } finally {
                            if (u != null) {
                                List<Unit> units = model.getVisibleUnits();
                                int row = (u != null) ? units.indexOf (u) : -1;
                                if (row > -1) {
                                    Unit u2 = model.getUnitAtRow (row);
                                    if (u2 != null) {
                                        if (u.updateUnit.getCodeName ().equals (u2.updateUnit.getCodeName ())) {
                                            getSelectionModel ().setSelectionInterval (row, row);
                                            Rectangle rect = UnitTable.this.getCellRect (row, 0, true);
                                            UnitTable.this.scrollRectToVisible (rect);
                                        }
                                    } 
                                }
                            }
                        }
                    }
                }
            });
            this.setReorderingAllowed ( false );
        }
        
        @Override
        public void setDraggedColumn ( TableColumn aColumn ) {
            if( null != aColumn && aColumn.getModelIndex () == 0 )
                return; //don't allow the first column to be dragged
            super.setDraggedColumn ( aColumn );
        }
        
        @Override
        public void setDefaultRenderer (TableCellRenderer defaultRenderer) {
            if( !(defaultRenderer instanceof SortColumnHeaderRenderer) ) {
                sortingRenderer = new SortColumnHeaderRenderer ((UnitCategoryTableModel)getModel (), defaultRenderer );
                defaultRenderer = sortingRenderer;
            }
            super.setDefaultRenderer ( defaultRenderer );
        }
        
        @Override
        public void setResizingColumn ( TableColumn col ) {
            if( null != getResizingColumn () && null == col ) {
                //maybe could be persistent later
                //storeColumnState();
            }
            super.setResizingColumn ( col );
        }
        
        public void setDefaultSorting () {
            if (sortingRenderer != null) {
                sortingRenderer.setDefaultSorting ();
            }
        }
    }        
    
    private class MoreRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component res = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (res == null || value == null) {
                return res;
            }
            if (column == 1 && res instanceof JLabel) {
                JLabel original = (JLabel)res;
                StringBuilder text = new StringBuilder();
                if (isSelected || hasFocus) {
                    text.append("<b>").append(model.getExpansionControlText()).append("</b>");//NOI18N                    
                } else {
                    text.append(model.getExpansionControlText());
                }
                setEnabled(isSelected);
                original.setText("<html>" + "<a href=\"\">" + text.toString() + "</a></html>");//NOI18N
            } else if (column != 1) {
                res = new JLabel();
            }
            
            return res;
        }
    }    
}
