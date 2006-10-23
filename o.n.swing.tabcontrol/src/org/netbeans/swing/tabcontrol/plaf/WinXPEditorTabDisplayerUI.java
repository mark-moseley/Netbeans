/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Windows xp impl of tabs ui
 *
 * @author Tim Boudreau
 */
public final class WinXPEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    
    private static final Rectangle scratch5 = new Rectangle();
    private static Map<Integer, String[]> buttonIconPaths;

    public WinXPEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinXPEditorTabDisplayerUI ((TabDisplayer) c);
    }    

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 24;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 8;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }
    
    public void paintBackground (Graphics g) {
        g.setColor (displayer.getBackground());
        g.fillRect (0, 0, displayer.getWidth(), displayer.getHeight());
    }

    protected void paintAfterTabs(Graphics g) {
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();

        Insets ins = getTabAreaInsets();

        int y = displayer.getHeight() - WinXPEditorTabCellRenderer.BOTTOM_INSET;
        //Draw the fill line that will be under the white highlight line - this
        //goes across the whole component.
        int selEnd = 0;
        int i = selectionModel.getSelectedIndex();
        g.setColor(WinXPEditorTabCellRenderer.getSelectedTabBottomLineColor());
        g.drawLine(0, y + 1, displayer.getWidth(), y + 1);
        
        //Draw the white highlight under all tabs but the selected one:
        
        //Check if we will need to draw a white line from the left edge to the 
        //selection, and another from the left edge of the selection to the
        //end of the control, skipping the selection area.  If the selection is
        //visible we should skip it.
        int tabsWidth = getTabsAreaWidth();
        boolean needSplitLine = i != -1 && ((i
                < scroll().getLastVisibleTab(tabsWidth) || i
                <= scroll().getLastVisibleTab(tabsWidth)
                && !scroll().isLastTabClipped())
                && i >= scroll().getFirstVisibleTab(tabsWidth));

        g.setColor(UIManager.getColor("controlLtHighlight"));
        if (needSplitLine) {
            //Find the rectangle of the selection to skip it
            getTabRect(i, scratch5);
            //Make sure it's not offscreen
            if (scratch5.width != 0) {
                //draw the first part of the line
                if (r.x < scratch5.x) {
                    g.drawLine(r.x, y, scratch5.x + 1, y);
                }
                //Now draw the second part out to the right edge
                if (scratch5.x + scratch5.width < r.x + r.width) {
                    //Find the right edge of the selected tab rectangle
                    selEnd = scratch5.x + scratch5.width;
                    //If the last tab is not clipped, the final tab is one
                    //pixel smaller; we need to overwrite one pixel of the
                    //border or there will be a small stub sticking down
                    if (!scroll().isLastTabClipped()) {
                        selEnd--;
                    }
                    //Really draw the second part, now that we know where to
                    //start
                    g.drawLine(selEnd, y, r.x + r.width, y);
                }
            }
        } else {
            //The selection is not visible - draw the white highlight line
            //across the entire width of the container
            g.drawLine(r.x, y, r.x + r.width, y);
        }

        //Draw the left and right edges so the area below the tabs looks 
        //closed
        g.setColor(WinXPEditorTabCellRenderer.getBorderColor());
        g.drawLine(0, y - 1, 0, displayer.getHeight());
        g.drawLine(displayer.getWidth() - 1, y - 1, displayer.getWidth() - 1,
                   displayer.getHeight());
        
        //Draw a line tracking the bottom of the tabs under the control
        //buttons, out to the right edge of the control
        
        //Find the last visible tab
        int last = scroll().getLastVisibleTab(tabsWidth);
        int l = 0;
        if (last >= 0) {
            //If it's onscreen (usually will be unless there are no tabs,
            //find the edge of the last tab - it may be scrolled)
            getTabRect(last, scratch5);
            last = scratch5.x + scratch5.width;
        }
        //Draw the dark line under the controls button area that closes the
        //tabs bottom margin on top
        g.drawLine(last, y - 1, displayer.getWidth(), y - 1);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new WinXPEditorTabCellRenderer();
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //TODO add 'pressed' icons
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_left_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/xp_left_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_left_selected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_ROLLOVER];
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_right_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/xp_right_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_right_selected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_ROLLOVER];
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_down_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/xp_down_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_down_selected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_ROLLOVER];
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_max_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/xp_max_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_max_selected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_ROLLOVER];
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_restored_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/xp_restored_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_restored_selected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_ROLLOVER];
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
        }
    }

    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }
}
