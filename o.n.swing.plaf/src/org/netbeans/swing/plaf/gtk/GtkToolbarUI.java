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
/*
 * GtkToolbarUI.java
 *
 * Created on January 17, 2004, 3:00 AM
 */

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/** A ToolbarUI subclass that gets rid of all borders
 * on buttons and provides a finder-style toolbar look.
 * 
 * @author  Tim Boudreau
 */
public class GtkToolbarUI extends BasicToolBarUI implements ContainerListener {
    private Border b = new AdaptiveMatteBorder (true, true, true, true, 4, true);
    /** Creates a new instance of PlainGtkToolbarUI */
    private GtkToolbarUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new GtkToolbarUI();
    }
    
    public void installUI( JComponent c ) {
        super.installUI(c);
        c.setBorder(b);
        c.setOpaque(false);
        c.addContainerListener(this);
        installButtonUIs (c);
    }
    
    public void uninstallUI (JComponent c) {
        super.uninstallUI (c);
        c.setBorder (null);
        c.removeContainerListener(this);
    }
    
    public void paint(Graphics g, JComponent c) {
        GradientPaint gp = new GradientPaint (0f, 0f, 
            UIManager.getColor("controlHighlight"), //NOI18N
            0f, c.getHeight(), 
            UIManager.getColor("control")); //NOI18N
        ((Graphics2D) g).setPaint (gp);
        Insets ins = c.getInsets();
        g.fillRect (ins.left, ins.top, c.getWidth() - (ins.left + ins.top), c.getHeight() - (ins.top + ins.bottom));
    }
    
    
    protected Border createRolloverBorder() {
        return BorderFactory.createEmptyBorder(2,2,2,2);
    }
    
    protected Border createNonRolloverBorder() {
        return createRolloverBorder();
    }
    
    private Border createNonRolloverToggleBorder() {
        return createRolloverBorder();
    }
    
    protected void setBorderToRollover(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
            ((AbstractButton) c).setBorder(BorderFactory.createEmptyBorder());
            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    protected void setBorderToNormal(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    public void setFloating(boolean b, Point p) {
        //nobody wants this
    }
    
    private void installButtonUI (Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setUI(buttonui);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    private void installButtonUIs (Container parent) {
        Component[] c = parent.getComponents();
        for (int i=0; i < c.length; i++) {
            installButtonUI(c[i]);
        }
    }
    
    private static final ButtonUI buttonui = new GtkToolBarButtonUI();
    public void componentAdded(ContainerEvent e) {
        installButtonUI (e.getChild());
    }
    
    public void componentRemoved(ContainerEvent e) {
        //do nothing
    }
}
