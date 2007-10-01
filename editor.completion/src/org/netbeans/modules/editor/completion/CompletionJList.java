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

package org.netbeans.modules.editor.completion;

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

import org.netbeans.editor.LocaleSupport;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
* @author Miloslav Metelka, Dusan Balek
* @version 1.00
*/

public class CompletionJList extends JList {

    private static final int DARKER_COLOR_COMPONENT = 5;

    private final RenderComponent renderComponent;
    
    private Graphics cellPreferredSizeGraphics;

    private int fixedItemHeight;
    private int maxVisibleRowCount;
    private int smartIndex;
    
    public CompletionJList(int maxVisibleRowCount, MouseListener mouseListener, Font font) {
        this.maxVisibleRowCount = maxVisibleRowCount;
        addMouseListener(mouseListener);
        setFont(font);
        setLayoutOrientation(JList.VERTICAL);
        setFixedCellHeight(fixedItemHeight = Math.max(CompletionLayout.COMPLETION_ITEM_HEIGHT, getFontMetrics(getFont()).getHeight()));
        setModel(new Model(Collections.EMPTY_LIST));
        setFocusable(false);

        renderComponent = new RenderComponent();
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new ListCellRenderer() {
            private ListCellRenderer defaultRenderer = new DefaultListCellRenderer();

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if( value instanceof CompletionItem ) {
                    CompletionItem item = (CompletionItem)value;
                    renderComponent.setItem(item);
                    renderComponent.setSelected(isSelected);
                    renderComponent.setSeparator(smartIndex > 0 && smartIndex == index);
                    Color bgColor;
                    Color fgColor;
                    if (isSelected) {
                        bgColor = list.getSelectionBackground();
                        fgColor = list.getSelectionForeground();
                    } else { // not selected
                        bgColor = list.getBackground();
                        if ((index % 2) == 0) { // every second item slightly different
                            bgColor = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
                        }
                        fgColor = list.getForeground();
                    }
                    // quick check Component.setBackground() always fires change
                    if (renderComponent.getBackground() != bgColor) {
                        renderComponent.setBackground(bgColor);
                    }
                    if (renderComponent.getForeground() != fgColor) {
                        renderComponent.setForeground(fgColor);
                    }
                    return renderComponent;

                } else {
                    return defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus);
                }
            }
        });
        getAccessibleContext().setAccessibleName(LocaleSupport.getString("ACSN_CompletionView"));
        getAccessibleContext().setAccessibleDescription(LocaleSupport.getString("ACSD_CompletionView"));
    }
    
    void setData(List data) {
        smartIndex = -1;
        if (data != null) {
            int itemCount = data.size();
            ListModel lm = LazyListModel.create( new Model(data), CompletionImpl.filter, 1.0d, LocaleSupport.getString("completion-please-wait") ); //NOI18N
            ListCellRenderer renderer = getCellRenderer();
            int lmSize = lm.getSize();
            int width = 0;
            int maxWidth = getParent().getParent().getMaximumSize().width;
            boolean stop = false;
            for(int index = 0; index < lmSize; index++) {
                Object value = lm.getElementAt(index);
                Component c = renderer.getListCellRendererComponent(this, value, index, false, false);
                Dimension cellSize = c.getPreferredSize();
                if (cellSize.width > width) {
                    width = cellSize.width;
                    if (width >= maxWidth)
                        stop = true;                    
                }
                if (smartIndex < 0 && value instanceof CompletionItem && ((CompletionItem)value).getSortPriority() >= 0)
                    smartIndex = index;
                if (stop && smartIndex >= 0)
                    break;
            }
            setFixedCellWidth(width);
            setModel(lm);
            
            if (itemCount > 0) {
                setSelectedIndex(0);
            }
            int visibleRowCount = Math.min(itemCount, maxVisibleRowCount);
            setVisibleRowCount(visibleRowCount);
        }
    }
    
    public void up() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() - 1 + size) % size;
            while(idx > 0 && getModel().getElementAt(idx) == null)
                idx--;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
        }
    }

    public void down() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = (getSelectedIndex() + 1) % size;
            while(idx < size && getModel().getElementAt(idx) == null)
                idx++;
            if (idx == size)
                idx = 0;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
        }
    }

    public void pageUp() {
        if (getModel().getSize() > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int idx = Math.max(getSelectedIndex() - pageSize, 0);
            while(idx > 0 && getModel().getElementAt(idx) == null)
                idx--;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
        }
    }

    public void pageDown() {
        int size = getModel().getSize();
        if (size > 0) {
            int pageSize = Math.max(getLastVisibleIndex() - getFirstVisibleIndex(), 0);
            int idx = Math.min(getSelectedIndex() + pageSize, size - 1);
            while(idx < size && getModel().getElementAt(idx) == null)
                idx++;
            if (idx == size) {
                idx = Math.min(getSelectedIndex() + pageSize, size - 1);
                while(idx > 0 && getModel().getElementAt(idx) == null)
                    idx--;
            }
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
        }
    }

    public void begin() {
        if (getModel().getSize() > 0) {
            setSelectedIndex(0);
            ensureIndexIsVisible(0);
        }
    }

    public void end() {
        int size = getModel().getSize();
        if (size > 0) {
            int idx = size - 1;
            while(idx > 0 && getModel().getElementAt(idx) == null)
                idx--;
            setSelectedIndex(idx);
            ensureIndexIsVisible(idx);
        }
    }

    private final class Model extends AbstractListModel {

        List data;

        public Model(List data) {
            this.data = data;
        }
        
        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return (index >= 0 && index < data.size()) ? data.get(index) : null;
        }
    }
    
    private final class RenderComponent extends JComponent {
        
        private CompletionItem item;
        
        private boolean selected;
        private boolean separator;
        
        void setItem(CompletionItem item) {
            this.item = item;
        }
        
        void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        void setSeparator(boolean separator) {
            this.separator = separator;
        }

        public void paintComponent(Graphics g) {
            // Although the JScrollPane without horizontal scrollbar
            // is explicitly set with a preferred size
            // it does not force its items with the only width into which
            // they can render (and still leaves them with the preferred width
            // of the widest item).
            // Therefore the item's render width is taken from the viewport's width.
            int itemRenderWidth = ((JViewport)CompletionJList.this.getParent()).getWidth();
            Color bgColor = getBackground();
            Color fgColor = getForeground();
            int height = getHeight();

            // Clear the background
            g.setColor(bgColor);
            g.fillRect(0, 0, itemRenderWidth, height);
            g.setColor(fgColor);

            // Render the item
            item.render(g, CompletionJList.this.getFont(), getForeground(), bgColor,
                    itemRenderWidth, getHeight(), selected);
            
            if (separator) {
                g.setColor(Color.gray);
                g.drawLine(0, 0, itemRenderWidth, 0);
                g.setColor(fgColor);
            }
        }
        
        public Dimension getPreferredSize() {
            if (cellPreferredSizeGraphics == null) {
                // CompletionJList.this.getGraphics() is null
                cellPreferredSizeGraphics = java.awt.GraphicsEnvironment.
                        getLocalGraphicsEnvironment().getDefaultScreenDevice().
                        getDefaultConfiguration().createCompatibleImage(1, 1).getGraphics();
                assert (cellPreferredSizeGraphics != null);
            }
            return new Dimension(item.getPreferredWidth(cellPreferredSizeGraphics, CompletionJList.this.getFont()),
                    fixedItemHeight);
        }

    }

}
