/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JTable;
import javax.swing.JTree; 
import javax.swing.table.TableCellRenderer;
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTreeTableColumnRenderer extends JTree implements TableCellRenderer {
    private NbiTreeTable treeTable;
    
    private int visibleRow = 0;
    
    private NbiTreeTableColumnCellRenderer cellRenderer;
    
    public NbiTreeTableColumnRenderer(final NbiTreeTable treeTable) {
        this.treeTable = treeTable;
        
        setModel(treeTable.getModel().getTreeModel());
        
        setRootVisible(false);
        setShowsRootHandles(true);
        
        setTreeColumnCellRenderer(new NbiTreeTableColumnCellRenderer(treeTable));
        
        setRowHeight(treeTable.getRowHeight());
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        visibleRow = row;
        
        if (selected) {
            setOpaque(true);
            setBackground(treeTable.getSelectionBackground());
            setForeground(treeTable.getSelectionForeground());
        } else {
            setOpaque(false);
            setBackground(treeTable.getBackground());
            setForeground(treeTable.getForeground());
        }
        
        return this;
    }
    
    public void setBounds(int x, int y, int w, int h) {
        if (treeTable != null) {
            super.setBounds(x, 0, w, treeTable.getHeight());
        } else {
            super.setBounds(x, y, w, h);
        }
    }
    
    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
        super.paint(g);
    }
    
    public NbiTreeTableColumnCellRenderer getTreeColumnCellRenderer() {
        return cellRenderer;
    }
    
    public void setTreeColumnCellRenderer(final NbiTreeTableColumnCellRenderer renderer) {
        cellRenderer = renderer;
        setCellRenderer(renderer);
    }
}
