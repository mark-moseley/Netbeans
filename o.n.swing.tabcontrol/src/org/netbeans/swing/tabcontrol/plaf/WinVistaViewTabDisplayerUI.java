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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;
import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;


import org.openide.awt.HtmlRenderer;

/**
 * Win Vista-like user interface of view type tabs.
 *
 * @author S. Aubrecht
 */
public final class WinVistaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /*********** constants *************/

    /**
     * Space between text and left side of the tab
     */
    private static final int TXT_X_PAD = 9;
    private static final int TXT_Y_PAD = 3;

    private static final int ICON_X_PAD = 4;

    private static final int BUMP_X_PAD = 3;
    private static final int BUMP_Y_PAD_UPPER = 6;
    private static final int BUMP_Y_PAD_BOTTOM = 3;

    /*********** static fields **********/
    
    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color 
            unselFillBrightUpperC, 
            unselFillDarkUpperC, 
            unselFillBrightLowerC, 
            unselFillDarkLowerC, 
            selFillC, 
            focusFillUpperC, 
            focusFillBrightLowerC, 
            focusFillDarkLowerC, 
            mouseOverFillBrightUpperC, 
            mouseOverFillDarkUpperC, 
            mouseOverFillBrightLowerC, 
            mouseOverFillDarkLowerC, 
            txtC, 
            borderC, 
            selBorderC, 
            borderInnerC;

    private static Map<Integer, String[]> buttonIconPaths;
    
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
    private WinVistaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 17);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinVistaViewTabDisplayerUI((TabDisplayer)c);
    }
     
    public void installUI (JComponent c) {
        super.installUI(c);
        initColors();
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
        if( 0 == index )
            x++;
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
        
        // draw bump (dragger)
        ColorUtil.paintVistaTabDragTexture(getDisplayer(), g, x + BUMP_X_PAD, y
                 + BUMP_Y_PAD_UPPER, height - (BUMP_Y_PAD_UPPER
                 + BUMP_Y_PAD_BOTTOM));
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

        Color borderColor = isHighlighted ? selBorderC : borderC;
        g.setColor(borderColor);
        int left = 0;
        //left
        if (isFirst )
            g.drawLine(0, 0, 0, height - 2);
        //top
        g.drawLine(0, 0, width - 1, 0);
        //right
        if( index < getDataModel().size()-1 && isTabHighlighted(index+1) )
            g.setColor( selBorderC );
        g.drawLine(width - 1, 0, width - 1, height - 2);
        //bottom
        g.setColor(borderC);
        g.drawLine(0, height - 1, width - 1, height - 1);
        
        //inner white border
        g.setColor(borderInnerC);
        //left
        if (isFirst)
            g.drawLine(1, 1, 1, height - 2);
        else
            g.drawLine(0, 1, 0, height - 2);
        //right
        g.drawLine(width-2, 1, width-2, height - 2);
        //top
        g.drawLine(1, 1, width-2, 1);

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // shrink rectangle - don't affect border and tab header
        y += 2;
        height -= 2;
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = selected && isActive();
        boolean attention = isAttention(index);
        boolean mouseOver = isMouseOver(index);
        if (focused && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         focusFillUpperC,  
                                         focusFillBrightLowerC, focusFillDarkLowerC );
        } else if (selected && !attention) {
            g.setColor(selFillC);
            g.fillRect(x, y, width, height);
        } else if (mouseOver && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         mouseOverFillBrightUpperC, mouseOverFillDarkUpperC, 
                                         mouseOverFillBrightLowerC, mouseOverFillDarkLowerC );
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height, a, b);         
        } else {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         unselFillBrightUpperC, unselFillDarkUpperC, 
                                         unselFillBrightLowerC, unselFillDarkLowerC );
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
     * @return true if tab with given index should have highlighted border, false otherwise.
     */
    private boolean isTabHighlighted(int index) {
        if (((OwnController) getController()).getMouseIndex() == index) {
            return true;
        }
        return isSelected(index) && isActive();
    }

    /**
     * @return true if tab with given index has mouse cursor above and is not
     * the selected one, false otherwise.
     */
    private boolean isMouseOver(int index) {
        return ((OwnController) getController()).getMouseIndex() == index
                && !isSelected(index);
    }

    /**
     * Initialization of colors
     */
    private static void initColors() {
        if (!colorsReady) {
            txtC = UIManager.getColor("TabbedPane.foreground"); // NOI18N
            
            selFillC = UIManager.getColor("tab_sel_fill"); // NOI18N
            
            focusFillUpperC = UIManager.getColor("tab_focus_fill_upper"); // NOI18N
            focusFillBrightLowerC = UIManager.getColor("tab_focus_fill_bright_lower"); // NOI18N
            focusFillDarkLowerC = UIManager.getColor("tab_focus_fill_dark_lower"); // NOI18N
            
            unselFillBrightUpperC = UIManager.getColor("tab_unsel_fill_bright_upper"); // NOI18N
            unselFillDarkUpperC = UIManager.getColor("tab_unsel_fill_dark_upper"); // NOI18N
            unselFillBrightLowerC = UIManager.getColor("tab_unsel_fill_bright_lower"); // NOI18N
            unselFillDarkLowerC = UIManager.getColor("tab_unsel_fill_dark_lower"); // NOI18N
            
            mouseOverFillBrightUpperC = UIManager.getColor("tab_mouse_over_fill_bright_upper"); // NOI18N
            mouseOverFillDarkUpperC = UIManager.getColor("tab_mouse_over_fill_dark_upper"); // NOI18N
            mouseOverFillBrightLowerC = UIManager.getColor("tab_mouse_over_fill_bright_lower"); // NOI18N
            mouseOverFillDarkLowerC = UIManager.getColor("tab_mouse_over_fill_dark_lower"); // NOI18N
            
            borderC = UIManager.getColor("tab_border"); // NOI18N
            selBorderC = UIManager.getColor("tab_sel_border"); // NOI18N
            borderInnerC = UIManager.getColor("tab_border_inner"); // NOI18N
            
            colorsReady = true;
        }
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
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
                x = tlm.getX(curIndex)-1;
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex)+2;
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex)-1;
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex)+2;
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
    } // end of OwnController
}
