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

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;


import org.openide.awt.HtmlRenderer;

/**
 * Win XP-like user interface of view type tabs.
 *
 * @author Dafe Simonek
 */
public final class WinXPViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /*********** constants *************/

    /**
     * Space between text and left side of the tab
     */
    private static final int TXT_X_PAD = 8;
    private static final int TXT_Y_PAD = 3;

    private static final int ICON_X_PAD = 2;

    private static final int BUMP_X_PAD = 3;
    private static final int BUMP_Y_PAD_UPPER = 5;
    private static final int BUMP_Y_PAD_BOTTOM = 3;

    private static final int HIGHLIGHTED_RAISE = 1;

    /*********** static fields **********/
    
    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color unselFillBrightC, unselFillDarkC, selFillC, focusFillBrightC, focusFillDarkC, txtC, borderC, bottomBorderC, selBorderC, bgFillC;
    
    private static Map<Integer, String[]> buttonIconPaths;
    private static Map<Integer, String[]> focusedButtonIconPaths;

    /**
     * ******** instance fields ********
     */

    private Dimension prefSize;

    /**
     * rectangle instance used to speedup recurring computations in painting
     * methods
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private WinXPViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 17);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinXPViewTabDisplayerUI((TabDisplayer)c);
    }
     
    public void installUI (JComponent c) {
        super.installUI(c);
        initColors();
        initIcons();
        c.setOpaque(true);
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                17 : fm.getAscent() + 2 * fm.getDescent() + 3;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        // highlighted one is higher then others
        if (!isTabInFront(index) && isMoreThanOne()) {
            y += HIGHLIGHTED_RAISE;
            height -= HIGHLIGHTED_RAISE;
        }
        int txtWidth = width;
        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();
                txtWidth = width - (buttonsSize.width + ICON_X_PAD + 2*TXT_X_PAD);
                buttons.setLocation( x + txtWidth+2*TXT_X_PAD, y + (height-buttonsSize.height)/2 );
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
        }
        
        int highlightedRaiseCompensation = (!isTabInFront(index) && isMoreThanOne()) ? HIGHLIGHTED_RAISE : 0;
        // draw bump (dragger)
        ColorUtil.paintXpTabDragTexture(getDisplayer(), g, x + BUMP_X_PAD, y
                 + BUMP_Y_PAD_UPPER, height - (BUMP_Y_PAD_UPPER
                 + BUMP_Y_PAD_BOTTOM)+highlightedRaiseCompensation);
        HtmlRenderer.renderString(text, g, x + TXT_X_PAD, y + fm.getAscent()
                + TXT_Y_PAD, txtWidth, height, getTxtFont(),
                txtC,
                HtmlRenderer.STYLE_TRUNCATE, true);
    }

    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        boolean isFirst = index == 0;
        boolean isHighlighted = isTabHighlighted(index);

        g.translate(x, y);

        ColorUtil.paintXpTabHeader(isHighlighted ?
                                   ColorUtil.XP_HIGHLIGHTED_TAB :
                                   ColorUtil.XP_REGULAR_TAB, g, 0,
                                   0, width); 
        Color borderColor = isHighlighted ? selBorderC : borderC;
        g.setColor(borderColor);
        if (isFirst) {
            g.drawLine(0, 3, 0, height - 2);
        }
        g.drawLine(width - 1, 3, width - 1, height - 2);
        g.setColor(bottomBorderC);
        g.drawLine(0, height - 1, width - 1, height - 1);

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // shrink rectangle - don't affect border and tab header
        y += 3;
        width -= 1;
        height -= 4;
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = selected && isActive();
        boolean attention = isAttention(index);
        if (focused && !attention) {
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         focusFillBrightC, focusFillDarkC);
        } else if (selected && isMoreThanOne() && !attention) {
            g.setColor(selFillC);
            g.fillRect(x, y, width, height);
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         a, b);         
        } else {
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         unselFillBrightC, unselFillDarkC);
        }
    }

    /**
     * Override to bold font
     */
    protected Font getTxtFont() {
        Font font = super.getTxtFont();
        if (!font.isBold()) {
            font = font.deriveFont(Font.BOLD);
        }
        return font;
    }

    /**
     * @return true if tab with given index should be highlighted with XP
     *         highlight header, false otherwise.
     */
    private boolean isTabHighlighted(int index) {
        if (((OwnController) getController()).getMouseIndex() == index) {
            return true;
        }
        return isTabInFront(index) && isMoreThanOne();
    }

    /**
     * @return true if tab is selected in other tabs or selected and also
     *         active
     */
    private boolean isTabInFront(int index) {
        return isSelected(index) && (isActive() || isMoreThanOne());
    }

    /**
     * @return true if there is more then one tab, false otherwise
     */
    private boolean isMoreThanOne() {
        return getDataModel().size() > 1;
    }

    /**
     * Initialization of colors
     */
    private static void initColors() {
        if (!colorsReady) {
            txtC = UIManager.getColor("TabbedPane.foreground"); // NOI18N
            selFillC = UIManager.getColor("TabbedPane.highlight"); // NOI18N
            focusFillBrightC = UIManager.getColor("tab_focus_fill_bright"); // NOI18N
            focusFillDarkC = UIManager.getColor("tab_focus_fill_dark"); // NOI18N
            unselFillBrightC = UIManager.getColor("tab_unsel_fill_bright"); // NOI18N
            unselFillDarkC = UIManager.getColor("tab_unsel_fill_dark"); // NOI18N
            borderC = UIManager.getColor("tab_border"); // NOI18N
            bottomBorderC = UIManager.getColor("tab_bottom_border"); // NOI18N
            selBorderC = UIManager.getColor("tab_sel_border"); // NOI18N
            bgFillC = UIManager.getColor("workplace_fill"); // NOI18N
            colorsReady = true;
        }
    }
    
    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-close-sel-normal.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-close-sel-pressed.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-close-sel-rollover.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-east.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-west.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-south.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-center.gif"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            //TODO change icon names
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_ maximize_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_maximize_pressed_notselected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_maximize_over_notselected.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_pressed_notselected.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_over_noteselected.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
        }
        
        if( null == focusedButtonIconPaths ) {
            //icons for focused tab
            focusedButtonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-close-focus-normal.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-close-focus-pressed.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-close-focus-rollover.gif"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-east.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-east.gif"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-west.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-west.gif"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-south.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-south.gif"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-center.gif"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-center.gif"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            //TODO change icon names
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_ maximize_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_maximize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_maximize_over.png"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_normal.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/xp_titlebar_restore_over.png"; // NOI18N
            focusedButtonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
        }
    }

    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        Map<Integer, String[]> iconMap = isActive() ? focusedButtonIconPaths : buttonIconPaths;
        String[] paths = iconMap.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }

    public void postTabAction(TabActionEvent e) {
        super.postTabAction(e);
        if( TabDisplayer.COMMAND_MAXIMIZE.equals( e.getActionCommand() ) ) {
            ((OwnController)getController()).updateHighlight( -1 );
        }
    }
    
    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {

        /**
         * holds index of tab in which mouse pointer was lastly located. -1
         * means mouse pointer is out of component's area
         */
        // TBD - should be part of model, not controller
        private int lastIndex = -1;

        /**
         * @return Index of tab in which mouse pointer is currently located.
         */
        public int getMouseIndex() {
            return lastIndex;
        }
        
        /**
         * Triggers visual tab header change when mouse enters/leaves tab in
         * advance to superclass functionality.
         */
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point pos = e.getPoint();
            if( !e.getSource().equals( displayer ) ) {
                pos = SwingUtilities.convertPoint( (Component) e.getSource(), pos, displayer );
            }
            updateHighlight(getLayoutModel().indexOfPoint(pos.x, pos.y));
        }

        /**
         * Resets tab header in advance to superclass functionality
         */
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            if( !inControlButtonsRect(e.getPoint())) {
                updateHighlight(-1);
            }
        }

        /**
         * Invokes repaint of dirty region if needed
         */
        private void updateHighlight(int curIndex) {
            if (curIndex == lastIndex) {
                return;
            }
            // compute region which needs repaint
            TabLayoutModel tlm = getLayoutModel();
            int x, y, w, h;
            Rectangle repaintRect = null;
            if (curIndex != -1) {
                x = tlm.getX(curIndex);
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex);
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex);
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex);
                h = tlm.getH(lastIndex);
                if (repaintRect != null) {
                    repaintRect =
                            repaintRect.union(new Rectangle(x, y, w, h));
                } else {
                    repaintRect = new Rectangle(x, y, w, h);
                }
            }
            // trigger repaint if needed, update index
            if (repaintRect != null) {
                getDisplayer().repaint(repaintRect);
            }
            lastIndex = curIndex;
        }

        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            mouseMoved( e );
        }


    } // end of OwnController
}
