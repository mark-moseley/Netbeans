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
import java.awt.Rectangle;

import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/** 
 *
 * @see SlidingButtonUI
 *
 * @author  Milos Kleint
 */
public class WindowsSlidingButtonUI extends SlidingButtonUI {

    //XXX 
    private static final SlidingButtonUI INSTANCE = new WindowsSlidingButtonUI();
    
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;   
    protected Color focusColor;
    protected static int dashedRectGapX;
    protected static int dashedRectGapY;
    protected static int dashedRectGapWidth;
    protected static int dashedRectGapHeight;
    
    
    /** Private, no need for outer classes to instantiate */
    protected WindowsSlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    

    /** Install a border on the button */
    protected void installBorder (AbstractButton b) {
        // XXX
        b.setBorder (//BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), 
                     BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }
    
    public void installDefaults (AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            try {
                //Null checks so this can be tested on other platforms
                Integer in = ((Integer)UIManager.get("Button.dashedRectGapX"));
                dashedRectGapX = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapY"));
                dashedRectGapY = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapWidth"));
                dashedRectGapWidth = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapHeight"));
                dashedRectGapHeight = in == null ? 3 : in.intValue();
                focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
                defaults_initialized = true;
            } catch (NullPointerException npe) {
                //We're testing on a non windows platform, the defaults don't
                //exist
                dashedRectGapX = 2;
                dashedRectGapY = 2;
                dashedRectGapWidth = 2;
                dashedRectGapHeight = 2;
            }
	}
    }
    
    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }    
    
    protected void paintBackground(Graphics2D g, AbstractButton b) {
        if (((SlidingButton) b).isBlinkState()) {
            g.setColor(WinClassicEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect (0, 0, b.getWidth(), b.getHeight());
        } else {
            super.paintBackground(g, b);
        }
    }    
    
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // This is a special case in which the toggle button in the
        // Rollover JToolBar will render the button in a pressed state
        Color oldColor = g.getColor();
        
        if (((SlidingButton) b).isBlinkState()) {
            g.setColor(WinClassicEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect (0, 0, b.getWidth(), b.getHeight());
        }
        
        int w = b.getWidth();
        int h = b.getHeight();
        UIDefaults table = UIManager.getLookAndFeelDefaults();
        if (b.getModel().isRollover() && (! b.getModel().isPressed() && ! b.getModel().isSelected())) {
            g.setColor(table.getColor("ToggleButton.highlight"));
            g.drawRect(0, 0, w-1, h-1);
            g.drawRect(0, 0, 0, h-1);
            
            Color shade = table.getColor("ToggleButton.shadow");
            Component p = b.getParent();
            if (p != null && p.getBackground().equals(shade)) {
                shade = table.getColor("ToggleButton.darkShadow");
            }
            g.setColor(shade);
            g.drawLine(w-1, 0, w-1, h-1);
            g.drawLine(0, h-1, w-1, h-1);
        } else {
            Color shade = table.getColor("ToggleButton.shadow");
            Component p = b.getParent();
            if (p != null && p.getBackground().equals(shade)) {
                shade = table.getColor("ToggleButton.darkShadow");
            }
            g.setColor(shade);
            g.drawRect(0, 0, w-1, h-1);
            g.setColor(table.getColor("ToggleButton.highlight"));
            g.drawLine(w-1, 0, w-1, h-1);
            g.drawLine(0, h-1, w-1, h-1);
        }
        g.setColor(oldColor);
    }
    
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){

	int width = b.getWidth();
	int height = b.getHeight();
	g.setColor(getFocusColor());
	BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
					  width - dashedRectGapWidth, height - dashedRectGapHeight);
    }
    

    protected Color getFocusColor() {
	return focusColor;
    }
    
    
    
    
    // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getPreferredSize(JComponent c) {
	Dimension d = super.getPreferredSize(c);

	/* Ensure that the width and height of the button is odd,
	 * to allow for the focus line if focus is painted
	 */
        AbstractButton b = (AbstractButton)c;
	if (b.isFocusPainted()) {
	    if(d.width % 2 == 0) { d.width += 1; }
	    if(d.height % 2 == 0) { d.height += 1; }
	}
	return d;
    }
    
    
}
