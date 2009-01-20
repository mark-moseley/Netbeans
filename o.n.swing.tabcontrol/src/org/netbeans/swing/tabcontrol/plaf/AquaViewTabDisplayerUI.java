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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

import javax.swing.plaf.ComponentUI;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A view tabs ui for OS-X adapted from the view tabs UI for Metal.
 *
 * @author Tim Boudreau
 */
public final class AquaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    private static final int TXT_X_PAD = 5;
    private static final int ICON_X_PAD = 2;

    /********* static fields ***********/
    
    private static Map<Integer, String[]> buttonIconPaths;
    
    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;

    /**
     * Should be constructed only from createUI method.
     */
    private AquaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); //XXX huh?
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 3;
        height += 1; //align with editor tabs
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * @return true if tab with given index has mouse cursor above and is not
     * the selected one, false otherwise.
     */
    private boolean isMouseOver(int index) {
        return ((OwnController) getController()).getMouseIndex() == index
                && !isSelected(index);
    }

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();

        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int textW = width;

        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();

                textW = width - (buttonsSize.width + ICON_X_PAD + 2*TXT_X_PAD);
                buttons.setLocation( x + textW+2*TXT_X_PAD-2, y + (height-buttonsSize.height)/2 );
            }
        } else {
            textW = width - 2 * TXT_X_PAD;
        }

        if (text.length() == 0) {
            return;
        }

        int textHeight = fm.getHeight();
        int textY;
        int textX = x + TXT_X_PAD;
        if (index == 0)
            textX = x + 5;

        if (textHeight > height) {
            textY = (-1 * ((textHeight - height) / 2)) + fm.getAscent()
                    - 1;
        } else {
            textY = (height / 2) - (textHeight / 2) + fm.getAscent();
        }

        int realTextWidth = (int)HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"), //NOI18N
                          HtmlRenderer.STYLE_TRUNCATE, false);
        realTextWidth = Math.min(realTextWidth, textW);
        if( textW > realTextWidth )
            textX += (textW - realTextWidth) / 2;

        if( isFocused(index) && isSelected(index) ) {
            int highlightY = (height - textHeight)/2;
            Shape s = new RoundRectangle2D.Float(textX, highlightY, realTextWidth, textHeight, 5, 5);

            Graphics2D g2d = (Graphics2D) g;
            Paint p = g2d.getPaint();

            g2d.setColor(UIManager.getColor("NbTabControl.focusedTabBackground")); //NOOI18N
            g2d.fill(s);
            g2d.setPaint(p);
        }

        HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"),
                          HtmlRenderer.STYLE_TRUNCATE, true);
    }
    
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        Color borderColor = UIManager.getColor("NbTabControl.borderColor");
        Color borderShadowColor = UIManager.getColor("NbTabControl.borderShadowColor");
        g.setColor(borderColor);
        if( index > 0 ) {
            g.drawLine(x, y, x, y+height);
            if( !isSelected(index) ) {
                g.setColor(borderShadowColor);
                g.drawLine(x+1, y+1, x+1, y+height-1);
            }
        }
        if( index < getDataModel().size()-1 ) {
            g.setColor(borderColor);
            g.drawLine(x+width, y, x+width, y+height);
            if( !isSelected(index) ) {
                g.setColor(borderShadowColor);
                g.drawLine(x+width-1, y+1, x+width-1, y+height-1);
            }
        }
        g.setColor(borderColor);
        if( !isSelected(index) ) {
            g.drawLine(x, y+height-1, x+width, y+height-1);
        }
        g.drawLine(x, y, x+width, y);
        if( getDataModel().size() == 1 ) {
            g.setColor(UIManager.getColor("NbTabControl.editorTabBackground"));
            g.drawLine(x, y+height-1, x+width, y+height-1);
        }
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        if( isSelected(index) ) {
            Graphics2D g2d = (Graphics2D) g;
            Paint p = g2d.getPaint();
            g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.selectedTabBrighterBackground"),
                    x, y+height/2, UIManager.getColor("NbTabControl.selectedTabDarkerBackground")) );
            g2d.fillRect(x, y, width, height);
            g2d.setPaint(p);

        } else {
            Graphics2D g2d = (Graphics2D) g;
            Paint p = g2d.getPaint();
            if( isMouseOver(index) )
                g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.mouseoverTabBrighterBackground"),
                        x, y+height/2, UIManager.getColor("NbTabControl.mouseoverTabDarkerBackground")) );
            else
                g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.inactiveTabBrighterBackground"),
                        x, y+height/2, UIManager.getColor("NbTabControl.inactiveTabDarkerBackground")) );
            g2d.fillRect(x, y, width, height);
            g2d.setPaint(p);
        }
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
        }
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
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
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point pos = e.getPoint();
            updateHighlight(getLayoutModel().indexOfPoint(pos.x, pos.y));
        }

        /**
         * Resets tab header in advance to superclass functionality
         */
        @Override
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
