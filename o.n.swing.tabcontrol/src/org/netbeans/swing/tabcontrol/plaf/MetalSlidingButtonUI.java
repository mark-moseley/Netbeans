/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
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
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.text.View;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/** 
 *
 * @see SlidingButtonUI
 *
 * @author  Milos Kleint
 */
public class MetalSlidingButtonUI extends SlidingButtonUI {
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;   
    protected JToggleButton hiddenToggle;
    
    private static final MetalSlidingButtonUI INSTANCE = new MetalSlidingButtonUI();

    private MetalSlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    
    
    
    public void installDefaults (AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            hiddenToggle = new JToggleButton();
            hiddenToggle.setText("");
            JToolBar bar = new JToolBar();
            bar.setRollover(true);
            bar.add(hiddenToggle);
	    defaults_initialized = true;
	}
    }
    
    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }   
    
    public void paint(Graphics g, JComponent c) {
        AbstractButton button = (AbstractButton)c;
        hiddenToggle.setBorderPainted(true);
//        hiddenToggle.setBorderPainted(button.isBorderPainted());
        hiddenToggle.setRolloverEnabled(button.isRolloverEnabled());
        hiddenToggle.setFocusable(button.isFocusable());
        hiddenToggle.setFocusPainted(button.isFocusPainted());
        hiddenToggle.setMargin(button.getMargin());
//        hiddenToggle.setBorder(button.getBorder());
        hiddenToggle.getModel().setRollover(button.getModel().isRollover());
        hiddenToggle.getModel().setPressed(button.getModel().isPressed());
        hiddenToggle.getModel().setArmed(button.getModel().isArmed());
        hiddenToggle.getModel().setSelected(button.getModel().isSelected());
        
        hiddenToggle.setBounds(button.getBounds());
        super.paint(g, c);
    }
    
    protected void paintBackground (Graphics2D g, AbstractButton button) {
        hiddenToggle.paint(g);
    }
    
    protected void paintButtonPressed(Graphics g, AbstractButton b) {    
        hiddenToggle.paint(g);
    }
    
}
